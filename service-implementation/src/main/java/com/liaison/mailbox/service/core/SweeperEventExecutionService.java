/**
 * Copyright 2019 Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.core;

import com.liaison.commons.exception.LiaisonException;
import com.liaison.commons.jaxb.JAXBUtility;
import com.liaison.commons.logging.LogTags;
import com.liaison.commons.messagebus.client.exceptions.ClientUnavailableException;
import com.liaison.commons.util.UUIDGen;
import com.liaison.commons.util.client.ftps.G2FTPSClient;
import com.liaison.commons.util.client.http.HTTPRequest;
import com.liaison.commons.util.client.http.HTTPResponse;
import com.liaison.commons.util.client.sftp.G2SFTPClient;
import com.liaison.dto.enums.ProcessMode;
import com.liaison.dto.queue.WorkTicket;
import com.liaison.fs2.metadata.FS2MetaSnapshot;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.enums.ExecutionState;
import com.liaison.mailbox.enums.ProcessorType;
import com.liaison.mailbox.service.dto.GlassMessageDTO;
import com.liaison.mailbox.service.dto.configuration.SweeperEventRequestDTO;
import com.liaison.mailbox.service.glass.util.ExecutionTimestamp;
import com.liaison.mailbox.service.glass.util.GlassMessage;
import com.liaison.mailbox.service.glass.util.MailboxGlassMessageUtil;
import com.liaison.mailbox.service.queue.sender.SweeperEventSendQueue;
import com.liaison.mailbox.service.queue.sender.SweeperQueueSendClient;
import com.liaison.mailbox.service.storage.util.StorageUtilities;
import com.liaison.mailbox.service.util.MailBoxUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.liaison.mailbox.MailBoxConstants.KEY_PROCESSOR_ID;
import static com.liaison.mailbox.MailBoxConstants.SWEEPER_EVENT_RETRY_DELAY;
import static com.liaison.mailbox.MailBoxConstants.SWEEPER_EVENT_RETRY_MAX_COUNT;

public class SweeperEventExecutionService implements Runnable {

    private String message;
    private static final Logger LOGGER = LogManager.getLogger(SweeperEventExecutionService.class);

    private static final Long SWEEPER_EVENT_DELAY = MailBoxUtil.getEnvironmentProperties().getLong(SWEEPER_EVENT_RETRY_DELAY, 60000);
    private static final int SWEEPER_EVENT_MAX_RETRY_COUNT = MailBoxUtil.getEnvironmentProperties().getInt(SWEEPER_EVENT_RETRY_MAX_COUNT, 10);

    public SweeperEventExecutionService() {
    }

    public SweeperEventExecutionService(String message) {
        this.message = message;
    }

    @Override
    public void run() {
        doProcess();
    }

    private void doProcess() {

        //first corner timestamp
        String globalProcessorId;
        SweeperEventRequestDTO sweeperEventDTO;
        WorkTicket workTicket;

        try {

            sweeperEventDTO = JAXBUtility.unmarshalFromJSON(message, SweeperEventRequestDTO.class);

            //Fish tag global process id
            ThreadContext.clearMap(); //set new context after clearing
            ThreadContext.put(LogTags.GLOBAL_PROCESS_ID, sweeperEventDTO.getGlobalProcessId());
            ThreadContext.put(KEY_PROCESSOR_ID, sweeperEventDTO.getProcessorId());
            //construct workticket for downloaded file.
            workTicket = getWorkTicket(sweeperEventDTO);
            globalProcessorId = workTicket.getGlobalProcessId();
            LOGGER.info("Workticket Constructed and Global Processor ID is {}" , globalProcessorId);
            try {
                persistPayloadAndWorkticket(workTicket, sweeperEventDTO, null, null, null, null);
            } catch (Exception e) {
                try {
                    //replacing the guid since sometimes payload can be persisted half and failed
                    sweeperEventDTO.setGlobalProcessId(UUIDGen.getCustomUUID());
                    //Increasing retry count
                    sweeperEventDTO.setRetryCount(sweeperEventDTO.getRetryCount() + 1);
                    if (sweeperEventDTO.getRetryCount() <=  SWEEPER_EVENT_MAX_RETRY_COUNT) {
                        LOGGER.info("Retry count({}) for the file {}, filePath {}",
                                sweeperEventDTO.getRetryCount(),
                                sweeperEventDTO.getFileName(),
                                sweeperEventDTO.getFilePath());
                        SweeperEventSendQueue.post(JAXBUtility.marshalToJSON(sweeperEventDTO), SWEEPER_EVENT_DELAY);
                        return;
                    } else {
                        LOGGER.error("Retry count({}) reached the maximum for the file {}, filePath {} and dropping the file",
                                sweeperEventDTO.getRetryCount(),
                                sweeperEventDTO.getFileName(),
                                sweeperEventDTO.getFilePath());
                    }
                } catch (ClientUnavailableException cue) {
                    //do not do anything and hope we don't reach here
                    LOGGER.error("Unable to post message to hornetq", cue);
                    return;
                }
            }

            String workTicketToSb = JAXBUtility.marshalToJSON(workTicket);
            LOGGER.debug("Workticket posted to SB queue.{}", new JSONObject(workTicketToSb).toString(2));
            SweeperQueueSendClient.post(workTicketToSb, false);
            verifyAndDeletePayload(workTicket);
            logToLens(workTicket, sweeperEventDTO);
            LOGGER.info("Global PID : {} submitted for file {}", workTicket.getGlobalProcessId(), workTicket.getFileName());

        } catch (JAXBException | IOException | JSONException e) {
            LOGGER.error("Failed to persist payload and workticket or Cannot marshal workticket into JSON.");
        } finally {
            ThreadContext.clearMap();
        }
    }

    /**
     * construct workticket from the sweeperEventRequest dto
     *
     * @param sweeperEventRequestDTO sweeper event request dto
     * @return workticket
     */
    public WorkTicket getWorkTicket(SweeperEventRequestDTO sweeperEventRequestDTO) {
        Map<String, Object> additionalContext = new HashMap<>();
        additionalContext.put(MailBoxConstants.KEY_FILE_PATH, sweeperEventRequestDTO.getFilePath() + File.separatorChar + sweeperEventRequestDTO.getFileName());
        additionalContext.put(MailBoxConstants.KEY_MAILBOX_ID, sweeperEventRequestDTO.getMailBoxId());
        additionalContext.put(MailBoxConstants.KEY_FOLDER_NAME, sweeperEventRequestDTO.getFilePath());

        WorkTicket workTicket = new WorkTicket();
        workTicket.setGlobalProcessId(sweeperEventRequestDTO.getGlobalProcessId());
        workTicket.setPipelineId(sweeperEventRequestDTO.getPipeLineID());
        workTicket.setProcessMode(ProcessMode.ASYNC);
        workTicket.addHeader(MailBoxConstants.KEY_FILE_MODIFIED_NAME, sweeperEventRequestDTO.getModifiedTime());
        workTicket.setPayloadSize(sweeperEventRequestDTO.getSize());
        workTicket.setFileName(sweeperEventRequestDTO.getFileName());
        workTicket.addHeader(MailBoxConstants.KEY_FILE_NAME, sweeperEventRequestDTO.getFileName());
        workTicket.addHeader(MailBoxConstants.KEY_FOLDER_NAME, sweeperEventRequestDTO.getFilePath());
        workTicket.setAdditionalContext(additionalContext);
        return workTicket;
    }

    /**
     * verifies whether the payload persisted in storage utilities or not and deletes it
     *
     * @param wrkTicket workticket contains payload uri
     */
    private void verifyAndDeletePayload(WorkTicket wrkTicket) throws IOException {

        String payloadURI = wrkTicket.getPayloadURI();
        String filePath = wrkTicket.getAdditionalContextItem(MailBoxConstants.KEY_FILE_PATH);
        // Delete the file if it exists in storage utilities and it should be successfully posted to SB Queue.
        if (StorageUtilities.isPayloadExists(wrkTicket.getPayloadURI())) {
            LOGGER.info("Payload {} exists in storage utilities. so deleting the file {},", payloadURI, filePath);
            deleteFile(filePath);
        } else {
            LOGGER.warn("Payload {} does not exist in storage utilities. so file {} is not deleted.", payloadURI, filePath);
        }
        LOGGER.info("Global PID : {} deleted the file {}", wrkTicket.getGlobalProcessId(), wrkTicket.getFileName());
     }

    /**
     * This method is used to persist the payload and workticket in storage utilities.
     *
     * @param sweeperEventRequestDTO staic properties
     * @param workTicket workticket
     */
    public int persistPayloadAndWorkticket(WorkTicket workTicket,
                                             SweeperEventRequestDTO sweeperEventRequestDTO,
                                             G2SFTPClient sftpClient,
                                             G2FTPSClient g2FTPSClient,
                                             HTTPRequest httpRequest,
                                             String fileName) throws IOException, LiaisonException {

        Map<String, String> properties = getProperties(workTicket, sweeperEventRequestDTO);
        LOGGER.info("Sweeping file {}", workTicket.getFileName());

        OutputStream outputStream = null;
        int statusCode=0;
        try {
            if (null != sftpClient) {
                outputStream = StorageUtilities.getPayloadOutputStream(workTicket, properties);
                statusCode = sftpClient.getFile(fileName, outputStream);
            } else if (null != g2FTPSClient) {
                outputStream = StorageUtilities.getPayloadOutputStream(workTicket, properties);
                statusCode = g2FTPSClient.getFile(fileName, outputStream);
            } else if (null != httpRequest) {
                outputStream = StorageUtilities.getPayloadOutputStream(workTicket, properties);
                LOGGER.info("Completed store payload in boss {}", outputStream);
                httpRequest.setOutputStream(outputStream);
                HTTPResponse response = httpRequest.execute();
                LOGGER.info("Status of response {}", response.getStatusCode());
                statusCode = response.getStatusCode();
            } else {
                //payloadToPersist is closed in the StorageUtilities
                File payloadFile = new File(sweeperEventRequestDTO.getFilePath() + File.separatorChar + sweeperEventRequestDTO.getFileName());
                InputStream payloadToPersist = new FileInputStream(payloadFile);
                FS2MetaSnapshot metaSnapshot = StorageUtilities.persistPayload(payloadToPersist, workTicket, properties, false);
                workTicket.setPayloadURI(metaSnapshot.getURI().toString());
                LOGGER.info("Payload persisted successfully  - {}", workTicket.getPayloadURI());
            }
        } finally {
            if (null != outputStream) {
                outputStream.close();
            }
        }

        // persist the workticket
        String workticketUri = StorageUtilities.persistWorkTicket(workTicket, properties);
        LOGGER.info("Workticket persisted successfully  - {}", workticketUri);
        return statusCode;
    }

    public Map<String, String> getProperties(WorkTicket workTicket, SweeperEventRequestDTO sweeperEventRequestDTO) {

        Map<String, String> properties = new HashMap<>();
        if (!sweeperEventRequestDTO.getTtlMap().isEmpty()) {
            Integer ttlDays = Integer.parseInt(sweeperEventRequestDTO.getTtlMap().get(MailBoxConstants.TTL_NUMBER));
            workTicket.setTtlDays(MailBoxUtil.convertTTLIntoDays(sweeperEventRequestDTO.getTtlMap().get(MailBoxConstants.CUSTOM_TTL_UNIT), ttlDays));
        }

        properties.put(MailBoxConstants.PROPERTY_HTTPLISTENER_SECUREDPAYLOAD, String.valueOf(sweeperEventRequestDTO.isSecuredPayload()));
        properties.put(MailBoxConstants.PROPERTY_LENS_VISIBILITY, String.valueOf(sweeperEventRequestDTO.isLensVisibility()));
        properties.put(MailBoxConstants.KEY_PIPELINE_ID, sweeperEventRequestDTO.getPipeLineID());
        properties.put(MailBoxConstants.STORAGE_IDENTIFIER_TYPE, sweeperEventRequestDTO.getStorageType());

        String contentType = MailBoxUtil.isEmpty(sweeperEventRequestDTO.getContentType()) ? MediaType.TEXT_PLAIN : sweeperEventRequestDTO.getContentType();
        properties.put(MailBoxConstants.CONTENT_TYPE, contentType);
        workTicket.addHeader(MailBoxConstants.CONTENT_TYPE.toLowerCase(), contentType);
        return properties;
    }

    public void logToLens(WorkTicket workTicket,
                          SweeperEventRequestDTO dto) {
        GlassMessageDTO glassMessageDTO = new GlassMessageDTO();
        glassMessageDTO.setGlobalProcessId(workTicket.getGlobalProcessId());
        glassMessageDTO.setProcessorType(ProcessorType.findByName(dto.getProcessorType()), dto.getCategory());
        glassMessageDTO.setProcessProtocol(dto.getProtocol());
        glassMessageDTO.setFileName(workTicket.getFileName());
        glassMessageDTO.setFilePath(dto.getProtocol());
        glassMessageDTO.setFileLength(workTicket.getPayloadSize());
        glassMessageDTO.setStatus(ExecutionState.PROCESSING);
        glassMessageDTO.setMessage("File " + workTicket.getFileName() + " posted successfully to service broker");
        glassMessageDTO.setPipelineId(workTicket.getPipelineId());
        glassMessageDTO.setFirstCornerTimeStamp(ExecutionTimestamp.beginTimestamp(GlassMessage.DEFAULT_FIRST_CORNER_NAME));
        glassMessageDTO.setStatusDate(new Date());
        MailboxGlassMessageUtil.logGlassMessage(glassMessageDTO);
    }

    /**
     * Deletes the given file
     * @param file file obj
     */
    protected void deleteFile(String file) throws IOException {

        // Delete the local files after successful upload if user opt for it
        Path path = Paths.get(file);
        if (Files.exists(path)) {
            Files.delete(path);
        }
    }
}