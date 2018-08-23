/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.core;

import com.google.gson.Gson;
import com.liaison.commons.messagebus.client.exceptions.ClientUnavailableException;
import com.liaison.commons.util.settings.DecryptableConfiguration;
import com.liaison.commons.util.settings.LiaisonArchaiusConfiguration;
import com.liaison.fs2.metadata.FS2MetaSnapshot;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.dtdm.dao.ProcessorConfigurationDAO;
import com.liaison.mailbox.dtdm.dao.ProcessorConfigurationDAOBase;
import com.liaison.mailbox.dtdm.model.Folder;
import com.liaison.mailbox.dtdm.model.Processor;
import com.liaison.mailbox.enums.EntityStatus;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.enums.ProcessorType;
import com.liaison.mailbox.rtdm.dao.DatacenterDAO;
import com.liaison.mailbox.rtdm.dao.DatacenterDAOBase;
import com.liaison.mailbox.rtdm.dao.InboundFileDAO;
import com.liaison.mailbox.rtdm.dao.InboundFileDAOBase;
import com.liaison.mailbox.rtdm.model.InboundFile;
import com.liaison.mailbox.service.dto.ResponseDTO;
import com.liaison.mailbox.service.dto.configuration.processor.properties.ConditionalSweeperPropertiesDTO;
import com.liaison.mailbox.service.dto.configuration.processor.properties.StaticProcessorPropertiesDTO;
import com.liaison.mailbox.service.dto.configuration.processor.properties.SweeperPropertiesDTO;
import com.liaison.mailbox.service.dto.dropbox.InboundFileDTO;
import com.liaison.mailbox.service.dto.dropbox.response.GetInboundFilesResponseDTO;
import com.liaison.mailbox.service.queue.kafka.KafkaMessage;
import com.liaison.mailbox.service.queue.kafka.KafkaMessageService.KafkaMessageType;
import com.liaison.mailbox.service.queue.sender.InboundFileSendQueue;
import com.liaison.mailbox.service.storage.util.StorageUtilities;
import com.liaison.mailbox.service.util.InboundFileUtil;
import com.liaison.mailbox.service.util.MailBoxUtil;
import com.liaison.mailbox.service.util.ProcessorPropertyJsonMapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * service class for inbound file from the event reader
 */
public class InboundFileService extends GridServiceRTDM<InboundFile> implements Runnable {

    private static final Logger LOGGER = LogManager.getLogger(InboundFileService.class);
    private static final String SLASH = File.separator;
    private static final String COMMA = ",";
    private static final String CREATED_BY = "FILESYSTEM_NOTIFICATION";
    private static final String ALL = "ALL";
    private static final int INBOX_POSITION = 5;
    private static final String INBOUND_FILE_QUEUE_DELAY = "com.liaison.mailbox.inboundfile.queue.delay";
    private static final DecryptableConfiguration configuration = LiaisonArchaiusConfiguration.getInstance();
    private String message;
    private static final String INBOUND_FILE = "Inbound files";

    private StaticProcessorPropertiesDTO staticProcessorProperties;

    public InboundFileService(String message) {
        this.message = message;
    }

    public InboundFileService() {
        // TODO Auto-generated constructor stub
    }

    /**
     * API to construct inbound file and post the payload to spectrum.
     */
    public void createInboundFile() {

        KafkaMessage kafkaMessage = new Gson().fromJson(message, KafkaMessage.class);
        String filePath = kafkaMessage.getFileCreateMessage();
        LOGGER.info("File path received from the event reader is: {}", filePath);

        Path path = Paths.get(filePath);

        if (!path.toFile().exists() && StringUtils.countMatches(filePath, ".") > 1) {
            filePath = filePath.substring(0, filePath.lastIndexOf("."));
            path = Paths.get(filePath);
        }

        if (!path.toFile().exists()) {
            throw new RuntimeException(String.format("File path %s does not exist in the system", filePath));
        }

        if (MailBoxUtil.validateLastModifiedTolerance(path)) {

            try {
                LOGGER.info("The file {} modified time is within the tolerance. Posting it back to queue.", path.getFileName());
                InboundFileSendQueue.post(message, configuration.getLong(INBOUND_FILE_QUEUE_DELAY));
            } catch (Exception e) {

                try {
                    LOGGER.info("Inbound file queue is not available. Retrying once to send the message");
                    InboundFileSendQueue.post(message, configuration.getLong(INBOUND_FILE_QUEUE_DELAY));
                } catch (Exception ex) {
                    throw new RuntimeException("Inbound file queue is not available to send the message");
                }
            }
            return;
        }

        String fileDirectory = filePath.substring(0, filePath.lastIndexOf(SLASH));
        String pathToSearch = extractUserPath(fileDirectory);
        ProcessorConfigurationDAO processorDao = new ProcessorConfigurationDAOBase();
        List<Processor> processors = processorDao.listSweeperProcessorsByFolderUri(pathToSearch);

        if (hasMoreProcessors(processors)) {
            processors = processors.stream()
                    .filter(processor -> EntityStatus.ACTIVE.value().equals(processor.getProcsrStatus()))
                    .collect(Collectors.toList());
        }

        if (hasMoreProcessors(processors)) {
            processors = processors.stream()
                    .filter(processor -> !processor.getScheduleProfileProcessors().isEmpty())
                    .collect(Collectors.toList());
        }

        Processor processor = findMatchingProcessor(processors, fileDirectory);
        if (null == processor) {
            throw new RuntimeException(String.format("No processors matched for the given payload uri: %s, " + "File name: %s", fileDirectory, path.getFileName()));
        }

        if (!path.toFile().exists()) {
            throw new RuntimeException(String.format("File path %s does not exist in the system", filePath));
        }

        FS2MetaSnapshot fs2;
        try {
            fs2 = persistPayload(processor, path.toFile());
        } catch (Exception e) {
            LOGGER.error("Failed to persist the payload and going to retry it.", e);
            try {
                InboundFileSendQueue.post(message, configuration.getLong(INBOUND_FILE_QUEUE_DELAY));
                return;
            } catch (Exception ex) {
                throw new RuntimeException("Inbound file queue is not available to send the message");
            }
        }

        //Deleting the file after persists
        try {

            if (StorageUtilities.isPayloadExists(fs2.getURI().toString())) {
                persistInboundFile(path.getFileName().toString(), path.toFile().length(), processor, fs2.getURI().toString(), fileDirectory);
                Files.deleteIfExists(Paths.get(filePath));
                LOGGER.info("File {} deleted successfully.", path.getFileName().toString());
            } else {
                LOGGER.info("Payload does not exist in boss and going to post to retry queue");
                InboundFileSendQueue.post(message, configuration.getLong(INBOUND_FILE_QUEUE_DELAY));
            }
        } catch (ClientUnavailableException e) {
            try {
                InboundFileSendQueue.post(message, configuration.getLong(INBOUND_FILE_QUEUE_DELAY));
            } catch (Exception ex) {
                throw new RuntimeException("Inbound file queue is not available to send the message");
            }
        } catch (IOException e) {
            LOGGER.error("unable to delete the file" + filePath + "from the disk but we have validated the payload existence in the boss", e);
        }
    }

    /**
     * Helper method to persist the inbound file.
     *
     * @param fileName      file name
     * @param size          payload size
     * @param processor     processor eneity
     * @param fs2Uri        fs2 uri
     * @param fileDirectory directory
     */
    public void persistInboundFile(String fileName, long size, Processor processor, String fs2Uri, String fileDirectory) {

        LOGGER.info("Persisting the inbound file");
        InboundFile inboundFile = new InboundFile();
        inboundFile.setPguid(MailBoxUtil.getGUID());
        inboundFile.setFileName(fileName);
        inboundFile.setFileSize(String.valueOf(size));
        inboundFile.setFilePath(fileDirectory);
        inboundFile.setFileLasModifiedTime(fileLastModifiedTime(fileDirectory, fileName));
        inboundFile.setProcessorId(processor.getPguid());
        inboundFile.setFs2Uri(fs2Uri);
        inboundFile.setCreatedDate(MailBoxUtil.getTimestamp());
        inboundFile.setCreatedBy(CREATED_BY);
        inboundFile.setOriginatingDc(processor.getOriginatingDc());
        inboundFile.setStatus(EntityStatus.ACTIVE.name());
        inboundFile.setTriggerFile(0);

        String processorProcessDc = processor.getProcessDc();
        String inboundFileProcessDc;
        DatacenterDAO dao = new DatacenterDAOBase();

        if (ALL.equals(processorProcessDc)) {
            inboundFileProcessDc = dao.findProcessingDatacenterByName(MailBoxUtil.DATACENTER_NAME);
            inboundFile.setProcessDc(inboundFileProcessDc);
        } else {
            inboundFileProcessDc = dao.findProcessingDatacenterByName(processorProcessDc);
            inboundFile.setProcessDc(inboundFileProcessDc);
        }

        InboundFileDAO inboundFileDao = new InboundFileDAOBase();

        //inactivate the file if already exists.
        InboundFile existingInboundFile = inboundFileDao.findInboundFile(fileDirectory, fileName, inboundFileProcessDc);
        if (null != existingInboundFile) {
            existingInboundFile.setStatus(EntityStatus.INACTIVE.name());
            inboundFileDao.merge(existingInboundFile);
        }

        inboundFileDao.persist(inboundFile);
    }

    /**
     * Helper method to persist the payload.
     *
     * @param processor processor entity
     * @param file      file instance
     * @return FS2MetaSnapshot
     */
    private FS2MetaSnapshot persistPayload(Processor processor, File file) {

        Map<String, String> properties = new HashMap<>();

        try {

            LOGGER.info("Persisting the payload data");
            if (processor.getProcessorType().getCode().equals(ProcessorType.CONDITIONALSWEEPER.getCode())) {

                ConditionalSweeperPropertiesDTO conditionalSweeperProp = (ConditionalSweeperPropertiesDTO) getProperties(processor);
                properties.put(MailBoxConstants.PROPERTY_HTTPLISTENER_SECUREDPAYLOAD, String.valueOf(conditionalSweeperProp.isSecuredPayload()));
                properties.put(MailBoxConstants.PROPERTY_LENS_VISIBILITY, String.valueOf(conditionalSweeperProp.isLensVisibility()));
                properties.put(MailBoxConstants.KEY_PIPELINE_ID, conditionalSweeperProp.getPipeLineID());
            } else {

                SweeperPropertiesDTO sweeperProp = (SweeperPropertiesDTO) getProperties(processor);
                properties.put(MailBoxConstants.PROPERTY_HTTPLISTENER_SECUREDPAYLOAD, String.valueOf(sweeperProp.isSecuredPayload()));
                properties.put(MailBoxConstants.PROPERTY_LENS_VISIBILITY, String.valueOf(sweeperProp.isLensVisibility()));
                properties.put(MailBoxConstants.KEY_PIPELINE_ID, sweeperProp.getPipeLineID());
            }
            properties.put(MailBoxConstants.STORAGE_IDENTIFIER_TYPE, MailBoxUtil.getStorageType(processor.getDynamicProperties()));

            //Input stream will be closed in the persist payload
            return StorageUtilities.persistPayload(new FileInputStream(file), properties, MailBoxUtil.getGUID(), processor.getMailbox().getTenancyKey());
        } catch (Exception e) {
            throw new RuntimeException("Inbound file queue is not available to send the message");
        }
    }

    /**
     * Helper method to find the last modified time of file.
     *
     * @param fileDirectory
     * @param fileName
     * @return timestamp
     */
    private Timestamp fileLastModifiedTime(String fileDirectory, String fileName) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(Paths.get(fileDirectory + SLASH + fileName).toFile().lastModified());
        return new Timestamp(cal.getTime().getTime());
    }

    /**
     * Method to return static properties stored in DB of a processor
     *
     * @param processor
     * @return StaticProcessorPropertiesDTO
     * @throws IOException
     * @throws IllegalAccessException
     */
    public StaticProcessorPropertiesDTO getProperties(Processor processor) throws IOException, IllegalAccessException {

        if (null == staticProcessorProperties) {
            staticProcessorProperties = ProcessorPropertyJsonMapper.getProcessorBasedStaticPropsFromJson(processor.getProcsrProperties(), processor);
        }
        return staticProcessorProperties;
    }

    /**
     * Helper method to check more than one processor exists
     *
     * @param processors
     * @return boolean
     */
    private boolean hasMoreProcessors(List<Processor> processors) {
        return null != processors && processors.size() > 1;
    }

    /**
     * Helper method to get the matching processor
     *
     * @param processors
     * @param fileDirectory
     * @return Processor
     */
    public Processor findMatchingProcessor(List<Processor> processors, String fileDirectory) {

        for (Processor processor : processors) {
            for (Folder folder : processor.getFolders()) {
                if (folder.getFldrUri().equals(fileDirectory)) {
                    LOGGER.info("Matching sweeper processor {} payload location is {} ", processor.getProcsrName(), folder.getFldrUri());
                    return processor;
                }
            }
        }

        fileDirectory = fileDirectory.substring(0, fileDirectory.lastIndexOf(SLASH));
        //Check if we reaches the user inbox location
        if (StringUtils.countMatches(fileDirectory, SLASH) == 3) {
            return null;
        }
        return findMatchingProcessor(processors, fileDirectory);
    }

    /**
     * Helper method to extract up to users inbox path alone
     *
     * @param createFilePath
     * @return string
     */
    public String extractUserPath(String createFilePath) {
        int position = StringUtils.ordinalIndexOf(createFilePath, SLASH, INBOX_POSITION);
        return createFilePath.substring(0, position);
    }

    @Override
    public void run() {
        this.createInboundFile();
    }

    /**
     * Method to list the Inbound files
     *
     * @param page
     * @param pageSize
     * @param sortInfo
     * @param filterText
     * @return serviceResponse
     */
    public GetInboundFilesResponseDTO getInboundFiles(String page, String pageSize, String sortInfo, String filterText) {

        LOGGER.debug("Entering into get all StagedFiles.");
        GetInboundFilesResponseDTO serviceResponse = new GetInboundFilesResponseDTO();

        try {

            GridResult<InboundFile> result = getGridItems(InboundFile.class, filterText, sortInfo,
                    page, pageSize);
            List<InboundFile> inboundFiles = result.getResultList();
            List<InboundFileDTO> inboundFileDTO = new ArrayList<>();

            if (CollectionUtils.isEmpty(inboundFiles)) {
                serviceResponse.setResponse(new ResponseDTO(Messages.NO_COMPONENT_EXISTS, INBOUND_FILE, Messages.SUCCESS));
                serviceResponse.setInboundFiles(inboundFileDTO);
                return serviceResponse;
            }

            InboundFileDTO inboundFile;
            for (InboundFile file : inboundFiles) {
                inboundFile = new InboundFileDTO();
                inboundFile.copyFromEntity(file);
                inboundFileDTO.add(inboundFile);
            }

            // response message construction
            serviceResponse.setResponse(new ResponseDTO(Messages.READ_SUCCESSFUL, INBOUND_FILE, Messages.SUCCESS));
            serviceResponse.setInboundFiles(inboundFileDTO);
            serviceResponse.setTotalItems((int) result.getTotalItems());

            LOGGER.debug("Exiting from get all InboundFiles.");
            return serviceResponse;
        } catch (Exception e) {

            LOGGER.error(Messages.READ_OPERATION_FAILED.name(), e);
            serviceResponse.setResponse(new ResponseDTO(Messages.READ_OPERATION_FAILED, INBOUND_FILE, Messages.FAILURE,
                    e.getMessage()));
            return serviceResponse;
        }
    }

    public Response constructAndCreateInboundFile(String requestString) {

        List<String> errorItems = new ArrayList<>();
        Map<String, String> errorResponse = new HashMap<>();

        String[] listOfUrls = requestString.split(COMMA);
        for (String url : listOfUrls) {
            if (InboundFileUtil.checkFilePath(url, errorItems, errorResponse)) {

                KafkaMessage message = new KafkaMessage();
                message.setMessageType(KafkaMessageType.FILE_CREATE);
                message.setFileCreateMessage(url);
                this.message = new Gson().toJson(message);

                try {
                    LOGGER.info("Entering into Inbound file service");
                    createInboundFile();
                } catch (Throwable ex) {

                    LOGGER.error("Inbound File Creation is failed", ex);
                    errorItems.add(url);
                    errorResponse.put(url, "Inbound file is not created.");
                }
            }
        }
        return InboundFileUtil.constructResponse(errorItems, listOfUrls, errorResponse);
    }
}