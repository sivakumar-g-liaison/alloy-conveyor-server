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
import com.liaison.commons.messagebus.client.exceptions.ClientUnavailableException;
import com.liaison.commons.util.UUIDGen;
import com.liaison.commons.util.client.ftps.G2FTPSClient;
import com.liaison.commons.util.client.sftp.G2SFTPClient;
import com.liaison.dto.enums.ProcessMode;
import com.liaison.dto.queue.WorkTicket;
import com.liaison.fs2.metadata.FS2MetaSnapshot;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.service.dto.configuration.SweeperEventRequestDTO;
import com.liaison.mailbox.service.queue.sender.SweeperEventSendQueue;
import com.liaison.mailbox.service.queue.sender.SweeperQueueSendClient;
import com.liaison.mailbox.service.storage.util.StorageUtilities;
import com.liaison.mailbox.service.util.MailBoxUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

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

        String globalProcessorId;
        SweeperEventRequestDTO sweeperEventDTO;
        WorkTicket workTicket;

        try {

            sweeperEventDTO = JAXBUtility.unmarshalFromJSON(message, SweeperEventRequestDTO.class);
            //construct workticket for downloaded file.
            workTicket = getWorkTicket(sweeperEventDTO);
            globalProcessorId = workTicket.getGlobalProcessId();
            LOGGER.info("Workticket Constructed and Global Processor ID is {}" , globalProcessorId);
            try {
                persistPayloadAndWorkticket(workTicket, sweeperEventDTO, null, null, null);
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
                    //do not do anything
                    LOGGER.error("Unable to post message to hornetq", cue);
                    return;
                }
            }

            String workTicketToSb = JAXBUtility.marshalToJSON(workTicket);
            LOGGER.info("Workticket posted to SB queue.{}", new JSONObject(workTicketToSb).toString(2));
            SweeperQueueSendClient.post(workTicketToSb, false);
            verifyAndDeletePayload(workTicket);

        } catch (JAXBException | IOException | JSONException e) {
            LOGGER.error("Failed to persist payload and workticket or Cannot marshal workticket into JSON.");
        }
    }

    /**
     * construct workticket from the sweeperEventRequest dto
     *
     * @param sweeperEventRequestDTO
     * @return
     */
    public WorkTicket getWorkTicket(SweeperEventRequestDTO sweeperEventRequestDTO) {
        Map<String, Object> additionalContext = new HashMap<>();
        additionalContext.put(MailBoxConstants.KEY_FILE_PATH, sweeperEventRequestDTO.getFilePath());
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
    private void verifyAndDeletePayload(WorkTicket wrkTicket) {

        String payloadURI = wrkTicket.getPayloadURI();
        File filePath = wrkTicket.getAdditionalContextItem(MailBoxConstants.KEY_FILE_PATH);

        // Delete the file if it exists in storage utilities and it should be successfully posted to SB Queue.
        if (StorageUtilities.isPayloadExists(wrkTicket.getPayloadURI())) {
            LOGGER.debug("Payload {} exists in storage utilities. so deleting the file {}", payloadURI, filePath.getName());
            deleteFile(filePath);
        } else {
            LOGGER.warn("Payload {} does not exist in storage utilities. so file {} is not deleted.", payloadURI, filePath.getName());
        }
        LOGGER.info("Global PID : {} deleted the file {}", wrkTicket.getGlobalProcessId(), wrkTicket.getFileName());
     }

    /**
     * This method is used to persist the payload and workticket in storage utilities.
     *
     * @param sweeperEventRequestDTO staic properties
     * @param workTicket workticket
     * @throws IOException
     */
    public void persistPayloadAndWorkticket(WorkTicket workTicket,
                                             SweeperEventRequestDTO sweeperEventRequestDTO,
                                             G2SFTPClient sftpClient,
                                             G2FTPSClient g2FTPSClient,
                                             String fileName) throws IOException, LiaisonException {

        File payloadFile = new File(workTicket.getFileName());
        Map<String, String> properties = getProperties(workTicket, sweeperEventRequestDTO);
        LOGGER.info("Sweeping file {}", workTicket.getPayloadURI());

        OutputStream outputStream = null;
        try {
            if (null != sftpClient) {
                outputStream = StorageUtilities.getPayloadOutputStream(workTicket, properties);
                sftpClient.getFile(fileName, outputStream);
            } else if (null != g2FTPSClient) {
                outputStream = StorageUtilities.getPayloadOutputStream(workTicket, properties);
                g2FTPSClient.getFile(fileName, outputStream);
            } else {
                //payloadToPersist is closed in the StorageUtilities
                InputStream payloadToPersist = new FileInputStream(payloadFile);
                FS2MetaSnapshot metaSnapshot = StorageUtilities.persistPayload(payloadToPersist, workTicket, properties, false);
                workTicket.setPayloadURI(metaSnapshot.getURI().toString());
            }
        } finally {
            if (null != outputStream) {
                outputStream.close();
            }
        }

        // persist the workticket
        StorageUtilities.persistWorkTicket(workTicket, properties);
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

    /**
     * Deletes the given file
     * @param file file obj
     */
    protected void deleteFile(File file) {

        // Delete the local files after successful upload if user opt for it
        if (file.exists()) {
            file.delete();
        }
    }
}