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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBException;

import com.liaison.commons.exception.LiaisonException;
import com.liaison.commons.util.client.sftp.G2SFTPClient;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.liaison.commons.jaxb.JAXBUtility;
import com.liaison.commons.util.ISO8601Util;
import com.liaison.dto.enums.ProcessMode;
import com.liaison.dto.queue.WorkTicket;
import com.liaison.fs2.metadata.FS2MetaSnapshot;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.service.dto.configuration.SweeperEventRequestDTO;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.queue.sender.SweeperQueueSendClient;
import com.liaison.mailbox.service.storage.util.StorageUtilities;
import com.liaison.mailbox.service.util.MailBoxUtil;

public class SweeperEventExecutionService implements Runnable {

    private String message;
    private static final Logger LOGGER = LogManager.getLogger(SweeperEventExecutionService.class);

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

        String globalProcessorId = null;
        SweeperEventRequestDTO sweeperEventDTO = null;
        WorkTicket workTicket = null;

        try {

            sweeperEventDTO = JAXBUtility.unmarshalFromJSON(message, SweeperEventRequestDTO.class);
            //construct workticket for downloaded file.
            workTicket = constructWorkticket(sweeperEventDTO);
            globalProcessorId = workTicket.getGlobalProcessId();
            LOGGER.info("Workticket Constructed and Global Processor ID is {}" , globalProcessorId);
            try {
                persistPayloadAndWorkticket(workTicket, sweeperEventDTO);
            } catch (MailBoxServicesException e) {
                LOGGER.error("Failed to persist payload and workticket in storage utilities. So retrying again");
                try {
                    persistPayloadAndWorkticket(workTicket, sweeperEventDTO);
                } catch (IOException | MailBoxServicesException e2) {
                    LOGGER.error("Retrying Failed.  Cannot persist payload and workticket in storage utilities.");
                    throw e2;
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
     * This method is used to construct workticket from the given file.
     *
     * @param sweeperEventRequestDTO
     * @return workticket
     * @throws IOException
     */
    private WorkTicket constructWorkticket(SweeperEventRequestDTO sweeperEventRequestDTO) throws IOException {
        return getWorkTicket(sweeperEventRequestDTO, sweeperEventRequestDTO.getFile().getAbsolutePath(), sweeperEventRequestDTO.getFile().getParent());
    }
    
    public WorkTicket getWorkTicket(SweeperEventRequestDTO sweeperEventRequestDTO, String filePath, String folderName) throws IOException {
        Map<String, Object> additionalContext = new HashMap<>();
        additionalContext.put(MailBoxConstants.KEY_FILE_PATH, filePath);
        additionalContext.put(MailBoxConstants.KEY_MAILBOX_ID, sweeperEventRequestDTO.getMailBoxId());
        additionalContext.put(MailBoxConstants.KEY_FOLDER_NAME, folderName);

        //Commented for sweepFile(GSFTPFilenmae, filename)
//        BasicFileAttributes attr = Files.readAttributes(sweeperEventRequestDTO.getFile().toPath(), BasicFileAttributes.class);
//        LOGGER.debug("File attributes{}", attr);
//
//        FileTime modifiedTime = attr.lastModifiedTime();
//        LOGGER.debug("Modified Time stamp {}", modifiedTime);
//
//        ISO8601Util dateUtil = new ISO8601Util();
//        FileTime createdTime = attr.creationTime();
//        LOGGER.debug("Created Time stamp {}", createdTime);

        WorkTicket workTicket = new WorkTicket();
        workTicket.setGlobalProcessId(sweeperEventRequestDTO.getGlobalProcessId());
        workTicket.setPipelineId(sweeperEventRequestDTO.getPipeLineID());
        workTicket.setProcessMode(ProcessMode.ASYNC);
        workTicket.setPayloadURI(filePath);
//        workTicket.setCreatedTime(new Date(createdTime.toMillis()));
//        workTicket.addHeader(MailBoxConstants.KEY_FILE_CREATED_NAME, dateUtil.fromDate(workTicket.getCreatedTime()));
//        workTicket.addHeader(MailBoxConstants.KEY_FILE_MODIFIED_NAME, dateUtil.fromDate(new Date(modifiedTime.toMillis())));
//        workTicket.setPayloadSize(attr.size());
        workTicket.setFileName(filePath);
        workTicket.addHeader(MailBoxConstants.KEY_FILE_NAME, filePath);
        workTicket.addHeader(MailBoxConstants.KEY_FOLDER_NAME, folderName);
        workTicket.setAdditionalContext(additionalContext);
        return workTicket;
    }

    /**
     * verifies whether the payload persisted in storage utilities or not and deletes it
     *
     * @param wrkTicket workticket contains payload uri
     */
    public void verifyAndDeletePayload(WorkTicket wrkTicket) {

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
    public void persistPayloadAndWorkticket(WorkTicket workTicket, SweeperEventRequestDTO sweeperEventRequestDTO) throws IOException {

        File payloadFile = new File(workTicket.getPayloadURI());
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
        LOGGER.info("Sweeping file {}", workTicket.getPayloadURI());

        try (InputStream payloadToPersist = new FileInputStream(payloadFile)) {
            FS2MetaSnapshot metaSnapshot = StorageUtilities.persistPayload(payloadToPersist, workTicket, properties, false);
            workTicket.setPayloadURI(metaSnapshot.getURI().toString());
        }

        // persist the workticket
        StorageUtilities.persistWorkTicket(workTicket, properties);
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
                                             String fileName) throws IOException, LiaisonException {

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
        LOGGER.info("Sweeping file {}", workTicket.getPayloadURI());


        OutputStream outputStream = null;
        try {
            outputStream = StorageUtilities.getPayloadOutputStream(workTicket, properties);
            sftpClient.getFile(sweeperEventRequestDTO.getFile().getName(), outputStream);
        } finally {
            if (null != outputStream) {
                outputStream.close();
            }
        }
        // persist the workticket
        StorageUtilities.persistWorkTicket(workTicket, properties);
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