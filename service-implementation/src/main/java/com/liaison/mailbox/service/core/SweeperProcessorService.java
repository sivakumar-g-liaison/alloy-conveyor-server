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

import com.liaison.commons.jaxb.JAXBUtility;
import com.liaison.commons.logging.LogTags;
import com.liaison.dto.queue.WorkTicket;
import com.liaison.fs2.metadata.FS2MetaSnapshot;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.dtdm.model.Processor;
import com.liaison.mailbox.enums.ExecutionState;
import com.liaison.mailbox.service.dto.GlassMessageDTO;
import com.liaison.mailbox.service.dto.configuration.processor.properties.SweeperPropertiesDTO;
import com.liaison.mailbox.service.glass.util.ExecutionTimestamp;
import com.liaison.mailbox.service.glass.util.GlassMessage;
import com.liaison.mailbox.service.glass.util.MailboxGlassMessageUtil;
import com.liaison.mailbox.service.queue.sender.SweeperQueueSendClient;
import com.liaison.mailbox.service.storage.util.StorageUtilities;
import com.liaison.mailbox.service.util.MailBoxUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.codehaus.jettison.json.JSONObject;

import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * To process sweeper files in multiple threads
 *
 * @author OFS
 */
public class SweeperProcessorService implements Callable<String> {
    
    private static final Logger LOGGER = LogManager.getLogger(SweeperProcessorService.class);

    private WorkTicket message;
    private SweeperPropertiesDTO staticProp;
    private static final String seperator = ": ";
    private Processor configurationInstance;

    public WorkTicket getMessage() {
        return message;
    }

    public void setMessage(WorkTicket message) {
        this.message = message;
    }

    public SweeperProcessorService(WorkTicket message, Processor configurationInstance, SweeperPropertiesDTO staticProp) {
        this.message = message;
        this.configurationInstance = configurationInstance;
        this.staticProp = staticProp;
    }

    @Override
    public String call() {
        return this.doProcess(getMessage());
    }

    public String doProcess(WorkTicket workTicket) {

        final Date lensStatusDate = new Date();

        String returnValue = workTicket.getFileName();;
        try {

            //first corner timestamp
            ExecutionTimestamp firstCornerTimeStamp = ExecutionTimestamp.beginTimestamp(GlassMessage.DEFAULT_FIRST_CORNER_NAME);

            //Interrupt signal for async sweeper
            if (MailBoxUtil.isInterrupted(Thread.currentThread().getName())) {
                LOGGER.warn("The executor is gracefully interrupted.");
                return returnValue;
            }
            
            LOGGER.debug("Persist workticket from workticket group to spectrum");
            persistPayloadAndWorkticket(workTicket, staticProp);
            
            String wrkTcktToSbr = JAXBUtility.marshalToJSON(workTicket);
            LOGGER.debug("Workticket posted to SB queue.{}", new JSONObject(wrkTcktToSbr).toString(2));
            SweeperQueueSendClient.post(wrkTcktToSbr, false);
            
            //Fish tag global process id
            try {
                ThreadContext.put(LogTags.GLOBAL_PROCESS_ID, workTicket.getGlobalProcessId());
                logToLens(workTicket, firstCornerTimeStamp, ExecutionState.PROCESSING, lensStatusDate);
                LOGGER.info("Global PID",
                        seperator,
                        workTicket.getGlobalProcessId(),
                        " submitted for file ",
                        workTicket.getFileName());
                 verifyAndDeletePayload(workTicket);
                 return returnValue;
            } finally {
                ThreadContext.clearMap();
            }
        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
            return returnValue;
        }
    }

    /**
     * Method to persist the payload and workticket details in spectrum
     *
     * @param workTicket WorkTicket.
     * @param staticProp sweeper properties
     * @throws IOException
     */
    private void persistPayloadAndWorkticket(WorkTicket workTicket, SweeperPropertiesDTO staticProp) throws IOException {
        LOGGER.debug("Persisting paylaod and workticket in spectrum starts");
        persistPayloadAndWorkticket(staticProp, workTicket);
        LOGGER.info("Payload and workticket are persisted successfully");
    }

    /**
     * overloaded method to persist the payload and workticket
     *
     * @param staticProp staic properties
     * @param workTicket workticket
     * @throws IOException
     */
    private void persistPayloadAndWorkticket(SweeperPropertiesDTO staticProp, WorkTicket workTicket) throws IOException {
        
        File payloadFile = new File(workTicket.getPayloadURI());
        
        Map<String, String> properties = new HashMap<>();
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
        LOGGER.info("Sweeping file {}", workTicket.getPayloadURI());

        // persist payload in spectrum
        try (InputStream payloadToPersist = new FileInputStream(payloadFile)) {
            FS2MetaSnapshot metaSnapshot = StorageUtilities.persistPayload(payloadToPersist, workTicket, properties, false);
            workTicket.setPayloadURI(metaSnapshot.getURI().toString());
        }

        // persist the workticket
        StorageUtilities.persistWorkTicket(workTicket, properties);
    }

    /**
     * Logs the TVAPI and ActivityStatus messages to LENS. This will be invoked for each file.
     *
     * @param wrkTicket workticket for logging
     * @param firstCornerTimeStamp first corner timestamp
     * @param state Execution Status
     */
    protected void logToLens(WorkTicket wrkTicket, ExecutionTimestamp firstCornerTimeStamp, ExecutionState state, Date date) {

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
        if (null != firstCornerTimeStamp) {
            glassMessageDTO.setFirstCornerTimeStamp(firstCornerTimeStamp);
        }
        if (null != date) {
            glassMessageDTO.setStatusDate(date);
            LOGGER.debug("The date value is {}", date.getTime());
        }

        MailboxGlassMessageUtil.logGlassMessage(glassMessageDTO);
    }

   /**
    * Get category from the properties json
    * @return category string
    */
    protected String getCategory() {
        return MailBoxUtil.getCategory(configurationInstance.getProcsrProperties());
    }

   /**
    * verifies whether the payload persisted in spectrum or not and deletes it
    *
    * @param wrkTicket workticket contains payload uri
    * @throws IOException
    */
    private void verifyAndDeletePayload(WorkTicket wrkTicket) throws IOException {

        String payloadURI = wrkTicket.getPayloadURI();
        String filePath = String.valueOf((Object) wrkTicket.getAdditionalContextItem(MailBoxConstants.KEY_FILE_PATH));

        // Delete the file if it exists in spectrum and should be successfully posted to SB Queue.
        if (StorageUtilities.isPayloadExists(wrkTicket.getPayloadURI())) {
            LOGGER.debug("Payload {} exists in spectrum. so deleting the file {}", payloadURI, filePath);
            delete(filePath);
        } else {
            LOGGER.warn("Payload {} does not exist in spectrum. so file {} is not deleted.", payloadURI, filePath);
        }
        LOGGER.info("Global PID",
                seperator,
                wrkTicket.getGlobalProcessId(),
                " deleted the file ",
                wrkTicket.getFileName());
    }

    /**
     * Method is used to move the file to the sweeped folder.
     *
     * @param file the file to be deleted
     * @throws IOException
     */
    private void delete(String file) throws IOException {
        Files.deleteIfExists(Paths.get(file));
    }

}