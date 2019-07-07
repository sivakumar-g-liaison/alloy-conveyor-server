/**
 * Copyright 2014 Liaison Technologies, Inc.
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.core.processor;

import com.liaison.commons.jaxb.JAXBUtility;
import com.liaison.commons.logging.LogTags;
import com.liaison.commons.util.ISO8601Util;
import com.liaison.commons.util.client.http.HTTPRequest;
import com.liaison.commons.util.client.http.HTTPResponse;
import com.liaison.dto.enums.ProcessMode;
import com.liaison.dto.queue.WorkTicket;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.dtdm.model.Processor;
import com.liaison.mailbox.enums.EntityStatus;
import com.liaison.mailbox.enums.ExecutionState;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.rtdm.dao.InboundFileDAO;
import com.liaison.mailbox.rtdm.dao.InboundFileDAOBase;
import com.liaison.mailbox.rtdm.model.InboundFile;
import com.liaison.mailbox.service.dto.configuration.TriggerProcessorRequestDTO;
import com.liaison.mailbox.service.dto.configuration.processor.properties.SweeperPropertiesDTO;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.executor.javascript.JavaScriptExecutorUtil;
import com.liaison.mailbox.service.glass.util.ExecutionTimestamp;
import com.liaison.mailbox.service.glass.util.GlassMessage;
import com.liaison.mailbox.service.queue.sender.SweeperQueueSendClient;
import com.liaison.mailbox.service.storage.util.StorageUtilities;
import com.liaison.mailbox.service.util.MailBoxUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.liaison.mailbox.MailBoxConstants.BYTE_ARRAY_INITIAL_SIZE;
import static com.liaison.mailbox.MailBoxConstants.CONFIGURATION_CONNECTION_TIMEOUT;
import static com.liaison.mailbox.MailBoxConstants.CONFIGURATION_SERVICE_BROKER_ASYNC_URI;
import static com.liaison.mailbox.MailBoxConstants.CONFIGURATION_SOCKET_TIMEOUT;

/**
 * EnhancedDirectorySweeper
 *
 * <P>
 * EnhancedDirectorySweeper sweeps the files from mail box and creates meta data about file and post it to the queue.
 *
 */

public class EnhancedDirectorySweeper extends AbstractProcessor implements MailBoxProcessorI {

    private static final Logger LOGGER = LogManager.getLogger(EnhancedDirectorySweeper.class);

    private SweeperPropertiesDTO staticProp;

    public void setStaticProp(SweeperPropertiesDTO staticProp) {
        this.staticProp = staticProp;
    }

    @SuppressWarnings("unused")
    private EnhancedDirectorySweeper() {
        // to force creation of instance only by passing the processor entity
    }

    public EnhancedDirectorySweeper(Processor configurationInstance) {
        super(configurationInstance);
    }

    @Override
    public void runProcessor(Object dto) {
        setReqDTO((TriggerProcessorRequestDTO) dto);
        run();
    }

    /**
     * Method to process enhanced directory sweeper
     */
    private void run() {

        try {

            String inputLocation = getPayloadURI();
            this.setStaticProp((SweeperPropertiesDTO) getProperties());

            if (MailBoxUtil.isEmpty(inputLocation)) {
                throw new MailBoxServicesException(Messages.LOCATION_NOT_CONFIGURED, MailBoxConstants.PAYLOAD_LOCATION, Response.Status.CONFLICT);
            }

            long startTime = System.currentTimeMillis();
            LOGGER.info(constructMessage("Start run"));

            InboundFileDAO inboundFileDAO = new InboundFileDAOBase();
            List<InboundFile> inboundFilesToProcess = new ArrayList<>();
            List<InboundFile> inboundFiles;

            if (staticProp.isSweepSubDirectories()) {
                inboundFiles = inboundFileDAO.findInboundFilesByRecurse(inputLocation, configurationInstance.getPguid());
            } else {
                inboundFiles = inboundFileDAO.findInboundFiles(inputLocation, configurationInstance.getPguid());
            }

            for (InboundFile inboundFile : inboundFiles) {

                if (!checkFileIncludeOrExclude(staticProp.getIncludedFiles(), inboundFile.getFileName(), staticProp.getExcludedFiles())) {
                    continue;
                }

                if (!inboundFile.getProcessDc().equals(inboundFile.getOriginatingDc())) {
                    if (StorageUtilities.isPayloadExists(inboundFile.getFs2Uri())) {
                        inboundFilesToProcess.add(inboundFile);
                    }
                } else {
                    inboundFilesToProcess.add(inboundFile);
                }
            }

            inboundFilesToProcess = filterInboundFiles(inboundFilesToProcess);

            List<WorkTicket> workTickets = generateWorkTickets(inboundFilesToProcess);
            List<WorkTicket> workTicketsToPost = new ArrayList<>();

            if (!workTickets.isEmpty()) {

                if (!staticProp.isAllowEmptyFiles()) {

                    for (WorkTicket workTicket : workTickets) {

                        if (isPayloadValid(workTicket)) {
                            workTicketsToPost.add(workTicket);
                        } else {

                            // Interrupt signal for empty files
                            if (MailBoxUtil.isInterrupted(Thread.currentThread().getName())) {
                                LOGGER.warn(constructMessage("The executor is gracefully interrupted."));
                                return;
                            }

                            LOGGER.warn(constructMessage("The file {} is empty and empty files not allowed"), workTicket.getFileName());
                            logToLens(workTicket, null, ExecutionState.VALIDATION_ERROR, null);
                            // update inbound file status to inactive
                            inboundFileDAO.updateInboundFileStatusByGuid(workTicket.getGlobalProcessId(), EntityStatus.INACTIVE.name(), getReqDTO().getProfileName());
                        }
                    }
                } else {
                    workTicketsToPost.addAll(workTickets);
                }

                LOGGER.debug("There are {} files to process", workTicketsToPost.size());
                if (ProcessMode.SYNC.name().equals(staticProp.getProcessMode())) {
                    syncSweeper(workTicketsToPost, staticProp);
                } else {
                    asyncSweeper(workTicketsToPost, staticProp);
                }
            }

            long endTime = System.currentTimeMillis();
            LOGGER.info(constructMessage("Number of files processed {}"), workTicketsToPost.size());
            LOGGER.info(constructMessage("Total time taken to process files {}"), endTime - startTime);
            LOGGER.info(constructMessage("End run"));
        } catch (Exception e) {
            LOGGER.error(constructMessage("Error occurred while scanning the mailbox", seperator, e.getMessage()), e);
            throw new RuntimeException(e);
        } finally {
            //clear the context
            ThreadContext.clearMap();
        }
    }

    /**
     * Returns List of WorkTickets from Inbound File
     *
     * @param inboundFilesToProcess workTickets
     * @return list of worktickets
     * @throws IllegalAccessException
     * @throws IOException
     */
    private List<WorkTicket> generateWorkTickets(List<InboundFile> inboundFilesToProcess) throws IllegalAccessException, IOException {

        List<WorkTicket> workTickets = new ArrayList<>();
        WorkTicket workTicket;
        Map<String, Object> additionalContext;
        ISO8601Util dateUtil = new ISO8601Util();

        String folderName;
        String fileName;
        Timestamp createdTime;
        Timestamp modifiedTime;

        for (InboundFile inboundFile : inboundFilesToProcess) {

            LOGGER.debug("Obtaining file Attributes for path {}", inboundFile.getFilePath());
            additionalContext = new HashMap<>();

            workTicket = new WorkTicket();
            LOGGER.debug("Payload URI {}", inboundFile.getFilePath());
            workTicket.setPayloadURI(inboundFile.getFilePath());
            additionalContext.put(MailBoxConstants.KEY_FILE_PATH, inboundFile.getFilePath());

            LOGGER.debug("Pipeline ID {}", getPipeLineID());
            workTicket.setPipelineId(getPipeLineID());

            createdTime = inboundFile.getCreatedDate();
            LOGGER.debug("Created Time stamp {}", createdTime);
            workTicket.setCreatedTime(new Date(createdTime.getTime()));
            workTicket.addHeader(MailBoxConstants.KEY_FILE_CREATED_NAME, dateUtil.fromDate(workTicket.getCreatedTime()));

            modifiedTime = inboundFile.getFileLasModifiedTime();
            LOGGER.debug("Modified Time stamp {}", modifiedTime);
            workTicket.addHeader(MailBoxConstants.KEY_FILE_MODIFIED_NAME,
                    dateUtil.fromDate(new Date(modifiedTime.getTime())));

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
     * Async directory sweeper posts workticket to queue
     *
     * @param workTickets worktickets
     * @param staticProp sweeper property
     * @throws IOException
     * @throws JAXBException
     * @throws JSONException
     */
    private void asyncSweeper(List<WorkTicket> workTickets, SweeperPropertiesDTO staticProp)
            throws IOException, JAXBException, JSONException {

        final Date lensStatusDate = new Date();

        // first corner timestamp
        ExecutionTimestamp firstCornerTimeStamp = ExecutionTimestamp.beginTimestamp(GlassMessage.DEFAULT_FIRST_CORNER_NAME);
        InboundFileDAO inboundFileDao = new InboundFileDAOBase();

        for (WorkTicket workTicket : workTickets) {

            if (MailBoxUtil.isInterrupted(Thread.currentThread().getName())) {
                LOGGER.warn(constructMessage("The executor is gracefully interrupted."));
                return;
            }

            persistWorkticket(staticProp, workTicket);

            String wrkTcktToSbr = JAXBUtility.marshalToJSON(workTicket);
            LOGGER.debug(constructMessage("Workticket posted to SB queue.{}"), new JSONObject(wrkTcktToSbr).toString(2));
            SweeperQueueSendClient.post(wrkTcktToSbr, false);

            try {
                ThreadContext.put(LogTags.GLOBAL_PROCESS_ID, workTicket.getGlobalProcessId());
                logToLens(workTicket, firstCornerTimeStamp, ExecutionState.PROCESSING, lensStatusDate);
                LOGGER.info(constructMessage("Global PID", seperator, workTicket.getGlobalProcessId(), " submitted for file ", workTicket.getFileName()));
                inboundFileDao.updateInboundFileStatusByGuid(workTicket.getGlobalProcessId(), EntityStatus.INACTIVE.name(), getReqDTO().getProfileName());
            } finally {
                ThreadContext.clearMap();
            }
        }
    }

    /**
     * sync directory sweeper posts wotkticket to REST endpoint
     *
     * @param workTickets worktickets
     * @param staticProp sweeper properties
     */
    private void syncSweeper(List<WorkTicket> workTickets, SweeperPropertiesDTO staticProp) {

        // first corner timestamp
        ExecutionTimestamp firstCornerTimeStamp = ExecutionTimestamp.beginTimestamp(GlassMessage.DEFAULT_FIRST_CORNER_NAME);

        // sort the worktickets
        sortWorkTicket(workTickets, staticProp.getSort());

        String serviceBrokerUri = MailBoxUtil.getEnvironmentProperties().getString(CONFIGURATION_SERVICE_BROKER_ASYNC_URI);
        if (MailBoxUtil.isEmpty(serviceBrokerUri)) {
            throw new RuntimeException("Service Broker URI not configured ('" + CONFIGURATION_SERVICE_BROKER_ASYNC_URI + "'), cannot process sync");
        }

        int connectionTimeout = MailBoxUtil.getEnvironmentProperties().getInt(CONFIGURATION_CONNECTION_TIMEOUT);
        int socketTimeout = MailBoxUtil.getEnvironmentProperties().getInt(CONFIGURATION_SOCKET_TIMEOUT);

        InboundFileDAO inboundFileDAO = new InboundFileDAOBase();
        try {

            for (WorkTicket workTicket : workTickets) {

                // Interrupt signal for sync sweeper
                if (MailBoxUtil.isInterrupted(Thread.currentThread().getName())) {
                    LOGGER.warn("The executor is gracefully interrupted.");
                    return;
                }

                ThreadContext.put(LogTags.GLOBAL_PROCESS_ID, workTicket.getGlobalProcessId());
                persistWorkticket(staticProp, workTicket);
                workTicket.setProcessMode(ProcessMode.SYNC);

                logToLens(workTicket, firstCornerTimeStamp, ExecutionState.PROCESSING, null);
                LOGGER.info(constructMessage("Global PID", seperator, workTicket.getGlobalProcessId(), " submitted for file ", workTicket.getFileName()));

                ByteArrayOutputStream responseStream = null;
                try {

                    responseStream = new ByteArrayOutputStream(BYTE_ARRAY_INITIAL_SIZE);
                    HTTPRequest request = HTTPRequest.post(serviceBrokerUri)
                                                .header(HttpHeaders.CONTENT_TYPE,MediaType.APPLICATION_JSON)
                                                .connectionTimeout(connectionTimeout)
                                                .socketTimeout(socketTimeout)
                                                .inputData(JAXBUtility.marshalToJSON(workTicket))
                                                .outputStream(responseStream);

                    // execute request and handle response
                    HTTPResponse response = request.execute();
                    if (!MailBoxUtil.isSuccessful(response.getStatusCode())) {
                        throw new RuntimeException("Failed to process the payload in sync sweeper - " + response.getStatusCode());
                    } else {
                        inboundFileDAO.updateInboundFileStatusByGuid(workTicket.getGlobalProcessId(), EntityStatus.INACTIVE.name(), getReqDTO().getProfileName());
                    }

                } catch (Exception e) {
                    logSweeperFailedStatus(workTicket, e);
                    throw new RuntimeException(e);
                } finally {
                    if (null != responseStream) {
                        responseStream.close();
                    }
                }
            }

        } catch (IOException e) {
            LOGGER.error(constructMessage("Error occurred in sync sweeper", seperator, e.getMessage()), e);
            throw new RuntimeException(e);
        } finally {
            // Fish tag global process id
            ThreadContext.clearMap();
        }

    }

    /**
     * overloaded method to persist the payload and workticket
     *
     * @param staticProp staic properties
     * @param workTicket workticket
     */
    private void persistWorkticket(SweeperPropertiesDTO staticProp, WorkTicket workTicket) {

        Map<String, String> properties = setProperties(staticProp, workTicket);
        // persist the workticket
        StorageUtilities.persistWorkTicket(workTicket, properties);
    }

    @Override
    public Object getClient() {
        return null;
    }

    @Override
    public void cleanup() {
    }

    /**
     * Filter inbound files for directory sweeper if javascript execution is enabled
     * 
     * @param inboundFiles
     * @return
     * @throws IllegalAccessException
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    protected List<InboundFile> filterInboundFiles(List<InboundFile> inboundFiles)
            throws IllegalAccessException, IOException {

        String groupingJsPath = configurationInstance.getJavaScriptUri();
        boolean isFilterFileUsingJavaScript = getProperties().isHandOverExecutionToJavaScript();

        if (isFilterFileUsingJavaScript && !MailBoxUtil.isEmpty(groupingJsPath)) {
            return (List<InboundFile>) JavaScriptExecutorUtil.executeJavaScript(groupingJsPath, MailBoxConstants.FILTER, this, inboundFiles);
        }

        return inboundFiles;
    }

    @Override
    public boolean isClassicSweeper() {
        return false;
    }
}