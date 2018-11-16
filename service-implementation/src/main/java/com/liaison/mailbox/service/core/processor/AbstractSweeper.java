/**
 * Copyright 2017 Liaison Technologies, Inc.
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.mailbox.service.core.processor;

import com.liaison.commons.util.ISO8601Util;
import com.liaison.dto.enums.ProcessMode;
import com.liaison.dto.queue.WorkTicket;
import com.liaison.dto.queue.WorkTicketGroup;
import com.liaison.fs2.metadata.FS2MetaSnapshot;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.dtdm.dao.ProcessorConfigurationDAO;
import com.liaison.mailbox.dtdm.dao.ProcessorConfigurationDAOBase;
import com.liaison.mailbox.dtdm.model.Processor;
import com.liaison.mailbox.enums.EntityStatus;
import com.liaison.mailbox.enums.ExecutionState;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.rtdm.model.InboundFile;
import com.liaison.mailbox.service.core.email.EmailNotifier;
import com.liaison.mailbox.service.dto.GlassMessageDTO;
import com.liaison.mailbox.service.dto.SweeperStaticPropertiesDTO;
import com.liaison.mailbox.service.dto.configuration.processor.properties.ConditionalSweeperPropertiesDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.executor.javascript.JavaScriptExecutorUtil;
import com.liaison.mailbox.service.glass.util.ExecutionTimestamp;
import com.liaison.mailbox.service.glass.util.MailboxGlassMessageUtil;
import com.liaison.mailbox.service.storage.util.StorageUtilities;
import com.liaison.mailbox.service.util.DirectoryCreationUtil;
import com.liaison.mailbox.service.util.MailBoxUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.liaison.mailbox.service.util.MailBoxUtil.DATA_FOLDER_PATTERN;

/**
 * 
 * Common functions for DirectorySweeper and ConditionalSweeper.
 * 
 */
public abstract class AbstractSweeper extends AbstractProcessor {
	
    private static final Logger LOGGER = LogManager.getLogger(AbstractSweeper.class);
    private static final String LINE_SEPARATOR = System.lineSeparator();
    
    private String pipelineId;

    public AbstractSweeper() {}
    
    public AbstractSweeper(Processor processor) {
        super(processor);
    }

    public void setPipeLineID(String pipeLineID) {
        this.pipelineId = pipeLineID;
    }

    /**
     * verifies whether the payload persisted in spectrum or not and deletes it
     *
     * @param wrkTicket workticket contains payload uri
     * @throws IOException
     */
    protected void verifyAndDeletePayload(WorkTicket wrkTicket) throws IOException {

        String payloadURI = wrkTicket.getPayloadURI();
        String filePath = String.valueOf((Object) wrkTicket.getAdditionalContextItem(MailBoxConstants.KEY_FILE_PATH));
        // Delete the file if it exists in spectrum and should be successfully posted to SB Queue.
        if (StorageUtilities.isPayloadExists(wrkTicket.getPayloadURI())) {
            LOGGER.debug("Payload {} exists in spectrum. so deleting the file {}", payloadURI, filePath);
            delete(filePath);
        } else {
            LOGGER.warn("Payload {} does not exist in spectrum. so file {} is not deleted.", payloadURI, filePath);
        }
        LOGGER.info(constructMessage("Global PID",
                seperator,
                wrkTicket.getGlobalProcessId(),
                " deleted the file ",
                wrkTicket.getFileName()));
    }

    /**
     * Method to sort work tickets based on name/size/date
     * 
     * @param workTickets worktickets
     * @param sortType sort type
     */
    protected void sortWorkTicket(List<WorkTicket> workTickets, String sortType) {
    
        if (MailBoxConstants.SORT_BY_NAME.equals(sortType)) {
             workTickets.sort(Comparator.comparing(WorkTicket::getFileName));
        } else if(MailBoxConstants.SORT_BY_SIZE.equals(sortType)) {
            workTickets.sort(Comparator.comparing(WorkTicket::getPayloadSize));
        } else {
            ISO8601Util dateUtil = new ISO8601Util();
            workTickets.sort(Comparator.comparing(w -> dateUtil.fromDate(w.getCreatedTime())));
        }
    }
    
    /**
     * Method is used to move the file to the sweeped folder.
     *
     * @param file the file to be deleted
     * @throws IOException
     */
    protected void delete(String file) throws IOException {
        Files.deleteIfExists(Paths.get(file));
    }

    /**
     * Returns List of WorkTickets from java.io.File
     *
     * @param result workTickets
     * @param pipelineId
     * @return list of worktickets
     * @throws IOException
     */
    protected List<WorkTicket> generateWorkTickets(List<Path> result, String pipelineId) throws IOException {

        List<WorkTicket> workTickets = new ArrayList<>();
        WorkTicket workTicket = null;
        Map<String, Object> additionalContext = null;
        BasicFileAttributes attr = null;
        ISO8601Util dateUtil = new ISO8601Util();

        String folderName = null;
        String fileName = null;
        FileTime createdTime = null;
        FileTime modifiedTime = null;

        for (Path path : result) {

            LOGGER.debug("Obtaining file Attributes for path {}", path);
            additionalContext = new HashMap<String, Object>();

            workTicket = new WorkTicket();
            LOGGER.debug("Payload URI {}", path.toAbsolutePath().toString());
            workTicket.setPayloadURI(path.toAbsolutePath().toString());
            additionalContext.put(MailBoxConstants.KEY_FILE_PATH, path.toAbsolutePath().toString());

            LOGGER.debug("Pipeline ID {}", pipelineId);
            workTicket.setPipelineId(pipelineId);

            attr = Files.readAttributes(path, BasicFileAttributes.class);
            LOGGER.debug("File attributes{}", attr);

            createdTime = attr.creationTime();
            LOGGER.debug("Created Time stamp {}", createdTime);
            workTicket.setCreatedTime(new Date(createdTime.toMillis()));
            workTicket.addHeader(MailBoxConstants.KEY_FILE_CREATED_NAME, dateUtil.fromDate(workTicket.getCreatedTime()));

            modifiedTime = attr.lastModifiedTime();
            LOGGER.debug("Modified Time stamp {}", modifiedTime);
            workTicket.addHeader(MailBoxConstants.KEY_FILE_MODIFIED_NAME, dateUtil.fromDate(new Date(modifiedTime.toMillis())));

            LOGGER.debug("Size stamp {}", attr.size());
            workTicket.setPayloadSize(attr.size());

            fileName = path.toFile().getName();
            LOGGER.debug("Filename {}", fileName);
            workTicket.setFileName(fileName);
            workTicket.addHeader(MailBoxConstants.KEY_FILE_NAME, fileName);

            folderName = path.toFile().getParent();
            LOGGER.debug("Foldername {}", folderName);
            additionalContext.put(MailBoxConstants.KEY_MAILBOX_ID, configurationInstance.getMailbox().getPguid());
            additionalContext.put(MailBoxConstants.KEY_FOLDER_NAME, folderName);
            workTicket.addHeader(MailBoxConstants.KEY_FOLDER_NAME, folderName);

            workTicket.setAdditionalContext(additionalContext);
            workTicket.setProcessMode(ProcessMode.ASYNC);
            workTickets.add(workTicket);
            workTicket.setGlobalProcessId(MailBoxUtil.getGUID());
        }

        LOGGER.debug("WorkTickets size:{}, {}", workTickets.size(), workTickets.toArray());

        return workTickets;
    }

    /**
     * Returns List of WorkTickets from Inbound File
     *
     * @param inboundFilesToProcess workTickets
     * @return list of worktickets
     */
    protected List<WorkTicket> generateWorkTicketsForInboundFile(List<InboundFile> inboundFilesToProcess, String pipelineId) {

        List<WorkTicket> workTickets = new ArrayList<>();
        WorkTicket workTicket = null;
        Map<String, Object> additionalContext = null;
        ISO8601Util dateUtil = new ISO8601Util();

        String folderName = null;
        String fileName = null;
        Timestamp createdTime = null;
        Timestamp modifiedTime = null;

        for (InboundFile inboundFile : inboundFilesToProcess) {

            LOGGER.debug("Obtaining file Attributes for path {}", inboundFile.getFilePath());
            additionalContext = new HashMap<String, Object>();

            workTicket = new WorkTicket();
            LOGGER.debug("Payload URI {}", inboundFile.getFilePath());
            workTicket.setPayloadURI(inboundFile.getFilePath());
            additionalContext.put(MailBoxConstants.KEY_FILE_PATH, inboundFile.getFilePath());

            LOGGER.debug("Pipeline ID {}", pipelineId);
            workTicket.setPipelineId(pipelineId);

            createdTime = inboundFile.getCreatedDate();
            LOGGER.debug("Created Time stamp {}", createdTime);
            workTicket.setCreatedTime(new Date(createdTime.getTime()));
            workTicket.addHeader(MailBoxConstants.KEY_FILE_CREATED_NAME, dateUtil.fromDate(workTicket.getCreatedTime()));

            modifiedTime = inboundFile.getFileLasModifiedTime();
            LOGGER.debug("Modified Time stamp {}", modifiedTime);
            workTicket.addHeader(MailBoxConstants.KEY_FILE_MODIFIED_NAME, dateUtil.fromDate(new Date(modifiedTime.getTime())));

            LOGGER.debug("Size stamp {}", Long.valueOf(inboundFile.getFileSize()));
            workTicket.setPayloadSize(Long.valueOf(inboundFile.getFileSize()));

            fileName = inboundFile.getFileName();
            LOGGER.debug("Filename {}", fileName);
            workTicket.setFileName(fileName);
            workTicket.addHeader(MailBoxConstants.KEY_FILE_NAME, fileName);

            folderName = inboundFile.getFilePath();
            LOGGER.debug("Foldername {}", folderName);
            additionalContext.put(MailBoxConstants.KEY_MAILBOX_ID, configurationInstance.getMailbox().getPguid());
            additionalContext.put(MailBoxConstants.KEY_FOLDER_NAME, folderName);
            workTicket.addHeader(MailBoxConstants.KEY_FOLDER_NAME, folderName);

            workTicket.setAdditionalContext(additionalContext);
            workTicket.setProcessMode(ProcessMode.ASYNC);
            workTickets.add(workTicket);
            workTicket.setGlobalProcessId(inboundFile.getPguid());
            workTicket.setPayloadURI(inboundFile.getFs2Uri());
        }

        LOGGER.debug("WorkTickets size:{}, {}", workTickets.size(), workTickets.toArray());

        return workTickets;
    }

    /**
     * Logs the TVAPI and ActivityStatus messages to LENS. This will be invoked for each file.
     *
     * @param wrkTicket workticket for logging
     * @param firstCornerTimeStamp first corner timestamp
     * @param state Execution Status
     * @param relatedTransactionId parent global process guid
     */
    protected void logToLens(WorkTicket wrkTicket, ExecutionTimestamp firstCornerTimeStamp, ExecutionState state, String relatedTransactionId, Date date) {

        String filePath = wrkTicket.getAdditionalContextItem(MailBoxConstants.KEY_FOLDER_NAME).toString();
        
        StringBuilder message;
        if (ExecutionState.VALIDATION_ERROR.equals(state)) {
            message = new StringBuilder()
                .append("File size is empty ")
                .append(filePath)
                .append(", and empty files are not allowed");
        } else {
            message = new StringBuilder()
                .append("Starting to sweep input folder ")
                .append(filePath)
                .append(" for new files");
        }
       
        GlassMessageDTO glassMessageDTO = new GlassMessageDTO();
        glassMessageDTO.setGlobalProcessId(wrkTicket.getGlobalProcessId());
        glassMessageDTO.setProcessorType(configurationInstance.getProcessorType(), getCategory());
        glassMessageDTO.setProcessProtocol(configurationInstance.getProcsrProtocol());
        glassMessageDTO.setFileName(wrkTicket.getFileName());
        glassMessageDTO.setFilePath(filePath);
        glassMessageDTO.setFileLength(wrkTicket.getPayloadSize());
        glassMessageDTO.setStatus(state);
        glassMessageDTO.setMessage(message.toString());
        glassMessageDTO.setPipelineId(wrkTicket.getPipelineId());
        glassMessageDTO.setRelatedTransactionId(relatedTransactionId);
        if (null != firstCornerTimeStamp) {
            glassMessageDTO.setFirstCornerTimeStamp(firstCornerTimeStamp);
        }
        if (null != date) {
            glassMessageDTO.setStatusDate(date);
        }

        MailboxGlassMessageUtil.logGlassMessage(glassMessageDTO);

    }

    /**
     * Method to clean up stale files in the payload location of sweeper
     * 
     * @param staleFileTTL
     */
    public void cleanupStaleFiles(int staleFileTTL) {

        List<String> staleFiles = new ArrayList<>();
        String payloadLocation = getPayloadURI();

        //validates sweeper location
        final Path payloadPath = Paths.get(payloadLocation);
        FileSystem fileSystem = FileSystems.getDefault();
        PathMatcher pathMatcher = fileSystem.getPathMatcher(DATA_FOLDER_PATTERN);

        if (!Files.isDirectory(payloadPath) || !pathMatcher.matches(payloadPath)) {

            //inactivate the mailboxes which doesn't have valid directory
            ProcessorConfigurationDAO dao = new ProcessorConfigurationDAOBase();
            configurationInstance.setProcsrStatus(EntityStatus.INACTIVE.name());
            configurationInstance.setModifiedBy(MailBoxConstants.WATCH_DOG_SERVICE);
            configurationInstance.setModifiedDate(new Date());
            dao.merge(configurationInstance);
            throw new MailBoxServicesException(Messages.INVALID_DIRECTORY, Response.Status.BAD_REQUEST);
        }

        // filter and delete stale files in the sweeper location
        deleteStaleFiles(payloadPath, staleFiles, staleFileTTL);

        // notify deletion of stale files to users through mail configured in mailbox
        if (!staleFiles.isEmpty()) {
            notifyDeletionOfStaleFiles(staleFiles, payloadLocation);
        }
    }

    /**
     * Traverse the directory and deletes the stale files
     *
     * @param rootPath Payload location
     * @param fileNames Deleted files list
     */
    public void deleteStaleFiles(Path rootPath, List<String> fileNames, int staleFileTTL) {

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(rootPath)) {

            String fileName = null;
            for (Path file : stream) {

                fileName = file.getFileName().toString();
                //Sweep Directories if the property is set to true
                if (Files.isDirectory(file)) {
                    deleteStaleFiles(file, fileNames, staleFileTTL);
                    continue;
                }

                if (!MailBoxUtil.isFileExpired(file.toFile().lastModified(), staleFileTTL)) {
                    continue;
                }

                Files.deleteIfExists(file);
                fileNames.add(fileName);
                LOGGER.info("Stale file {} deleted successfully in sweeper location {} ", fileName, file.toAbsolutePath().toString());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Method to notify deletion of stale files to customer
     * 
     * @param staleFiles - list of stale files
     * @param payloadLocation - payload location in which stale files are to be cleaned up
     */
    protected void notifyDeletionOfStaleFiles(List<String> staleFiles, String payloadLocation) {
        
        // stale file deletion email subject
        String emailSubject = String.format(MailBoxConstants.STALE_FILE_NOTIFICATION_SUBJECT,
                                    configurationInstance.getProcessorType()); 
        
        // stale file deletion email content
        String emailContentPrefix = String.format(MailBoxConstants.STALE_FILE_NOTIFICATION_EMAIL_CONTENT,
                                           configurationInstance.getProcessorType(),
                                           configurationInstance.getProcsrName(),
                                           payloadLocation);
        StringBuilder body = new StringBuilder(emailContentPrefix)
            .append(LINE_SEPARATOR)
            .append(LINE_SEPARATOR)
            .append(MailBoxConstants.STALE_FILE_NOTIFICATION_EMAIL_FILES)
            .append(LINE_SEPARATOR)
            .append(StringUtils.join(staleFiles, LINE_SEPARATOR));
        EmailNotifier.sendEmail(configurationInstance, emailSubject, body.toString(), true);
        
    }
    
    /**
     * overloaded method to persist the payload and workticket
     *
     * @param workTicket workticket
     * @param staticProp
     * @throws IOException
     */
    protected void persistPayloadAndWorkticket(WorkTicket workTicket, SweeperStaticPropertiesDTO staticProp) throws IOException {

        File payloadFile = new File(workTicket.getPayloadURI());

        Map<String, String> properties = new HashMap<String, String>();
        Map<String, String> ttlMap = configurationInstance.getTTLUnitAndTTLNumber();
        if (!ttlMap.isEmpty()) {
            Integer ttlNumber = Integer.parseInt(ttlMap.get(MailBoxConstants.TTL_NUMBER));
            workTicket.setTtlDays(MailBoxUtil.convertTTLIntoDays(ttlMap.get(MailBoxConstants.CUSTOM_TTL_UNIT), ttlNumber));
        }

        properties.put(MailBoxConstants.PROPERTY_HTTPLISTENER_SECUREDPAYLOAD, String.valueOf(staticProp.isSecuredPayload()));
        properties.put(MailBoxConstants.PROPERTY_LENS_VISIBILITY, String.valueOf(staticProp.isLensVisibility()));
        properties.put(MailBoxConstants.KEY_PIPELINE_ID, staticProp.getPipeLineID());
        properties.put(MailBoxConstants.STORAGE_IDENTIFIER_TYPE, MailBoxUtil.getStorageType(configurationInstance.getDynamicProperties()));

        String contentType = MailBoxUtil.isEmpty(staticProp.getContentType()) ? MediaType.TEXT_PLAIN : staticProp.getContentType();
        properties.put(MailBoxConstants.CONTENT_TYPE, contentType);
        workTicket.addHeader(MailBoxConstants.CONTENT_TYPE.toLowerCase(), contentType);
        LOGGER.info(constructMessage("Sweeping file {}"), workTicket.getPayloadURI());

        // persist payload in spectrum
        try (InputStream payloadToPersist = new FileInputStream(payloadFile)) {
            FS2MetaSnapshot metaSnapshot = StorageUtilities.persistPayload(payloadToPersist, workTicket, properties, false);
            workTicket.setPayloadURI(metaSnapshot.getURI().toString());
        }

        // persist the workticket
        StorageUtilities.persistWorkTicket(workTicket, properties);
    }

    /**
     * overloaded method to persist the workticket
     *
     * @param workTicket workticket
     * @param staticProp
     */
    protected void persistWorkticket(WorkTicket workTicket, SweeperStaticPropertiesDTO staticProp) {

        Map<String, String> properties = new HashMap<String, String>();
        Map<String, String> ttlMap = configurationInstance.getTTLUnitAndTTLNumber();
        if (!ttlMap.isEmpty()) {
            Integer ttlNumber = Integer.parseInt(ttlMap.get(MailBoxConstants.TTL_NUMBER));
            workTicket.setTtlDays(MailBoxUtil.convertTTLIntoDays(ttlMap.get(MailBoxConstants.CUSTOM_TTL_UNIT), ttlNumber));
        }

        properties.put(MailBoxConstants.PROPERTY_HTTPLISTENER_SECUREDPAYLOAD, String.valueOf(staticProp.isSecuredPayload()));
        properties.put(MailBoxConstants.PROPERTY_LENS_VISIBILITY, String.valueOf(staticProp.isLensVisibility()));
        properties.put(MailBoxConstants.KEY_PIPELINE_ID, staticProp.getPipeLineID());
        properties.put(MailBoxConstants.STORAGE_IDENTIFIER_TYPE, MailBoxUtil.getStorageType(configurationInstance.getDynamicProperties()));

        String contentType = MailBoxUtil.isEmpty(staticProp.getContentType()) ? MediaType.TEXT_PLAIN : staticProp.getContentType();
        properties.put(MailBoxConstants.CONTENT_TYPE, contentType);
        workTicket.addHeader(MailBoxConstants.CONTENT_TYPE.toLowerCase(), contentType);
        LOGGER.info(constructMessage("Sweeping file {}"), workTicket.getPayloadURI());

        // persist the workticket
        StorageUtilities.persistWorkTicket(workTicket, properties);
    }
    
    /**
     * Grouping file paths
     * 
     * @param files
     * @return list of path list
     * @throws IOException 
     * @throws IllegalAccessException 
     */
    @SuppressWarnings("unchecked")
    protected List<List<Path>> groupingFiles(List<Path> files) throws IllegalAccessException, IOException {

        List<List<Path>> groupedFilePath = new ArrayList<>();
        String groupingJsPath = configurationInstance.getJavaScriptUri();
        boolean isFilterFileUsingJavaScript = getProperties().isHandOverExecutionToJavaScript();

        if (isFilterFileUsingJavaScript && !MailBoxUtil.isEmpty(groupingJsPath)) {
            groupedFilePath = (List<List<Path>>) JavaScriptExecutorUtil.executeJavaScript(groupingJsPath, MailBoxConstants.FILTER, this, files);
        } else {
            groupedFilePath.add(files);
        }

        return groupedFilePath;
    }

    /**
     * Grouping file paths for inbound files
     * 
     * @param files
     * @return list of path list
     * @throws IOException 
     * @throws IllegalAccessException 
     */
    @SuppressWarnings("unchecked")
    protected List<List<InboundFile>> groupingFilesForInboundFile(List<InboundFile> files) throws IllegalAccessException, IOException {

        List<List<InboundFile>> groupedFilePath = new ArrayList<>();
        String groupingJsPath = configurationInstance.getJavaScriptUri();
        boolean isFilterFileUsingJavaScript = getProperties().isHandOverExecutionToJavaScript();

        if (isFilterFileUsingJavaScript && !MailBoxUtil.isEmpty(groupingJsPath)) {
            groupedFilePath = (List<List<InboundFile>>) JavaScriptExecutorUtil.executeJavaScript(groupingJsPath, MailBoxConstants.FILTER, this, files);
        } else {
            groupedFilePath.add(files);
        }

        return groupedFilePath;
    }

    /**
     * Logs the TVAPI and ActivityStatus messages to LENS of the parent PGUID. This will be invoked at the end of the transaction.
     * @param relatedTransactionId
     * @param pipelineId
     */
    protected void logToLens(String relatedTransactionId, String pipelineId) {
        GlassMessageDTO glassMessageDTO = new GlassMessageDTO();
        glassMessageDTO.setGlobalProcessId(relatedTransactionId);
        glassMessageDTO.setProcessorType(configurationInstance.getProcessorType(), getCategory());
        glassMessageDTO.setProcessProtocol(configurationInstance.getProcsrProtocol());
        glassMessageDTO.setStatus(ExecutionState.QUEUED);
        glassMessageDTO.setPipelineId(pipelineId);
        glassMessageDTO.setMessage("Processed trigger file");
        MailboxGlassMessageUtil.logGlassMessage(glassMessageDTO);
    }

    /**
     * Method to get the pipe line id from the remote processor properties.
     *
     * @return pipelineId
     * @throws IllegalAccessException
     * @throws IOException
     */
    protected String getPipeLineID() throws IOException, IllegalAccessException {
        if (MailBoxUtil.isEmpty(this.pipelineId)) {
            ConditionalSweeperPropertiesDTO sweeperStaticProperties = (ConditionalSweeperPropertiesDTO) getProperties();
            this.setPipeLineID(sweeperStaticProperties.getPipeLineID());
        }
         return this.pipelineId;
    }

    /**
     * Method to construct workticket
     * @param parentGlobalProcessId
     * @param payloadfileUri
     * @return
     * @throws IOException
     * @throws IllegalAccessException
     */
    protected WorkTicket constructWorkTicket(String parentGlobalProcessId, String payloadfileUri) throws IOException, IllegalAccessException {
        WorkTicket workTicket = new WorkTicket();
        workTicket.setGlobalProcessId(parentGlobalProcessId);
        workTicket.setPipelineId(getPipeLineID());
        workTicket.setPayloadURI(payloadfileUri);
        workTicket.setAdditionalContext(MailBoxConstants.KEY_IS_BATCH_TRANSACTION, true);
        workTicket.setProcessMode(ProcessMode.ASYNC);
        return workTicket;
    }
}
