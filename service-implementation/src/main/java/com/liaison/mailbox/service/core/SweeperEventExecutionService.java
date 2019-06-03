/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBException;

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
import com.liaison.mailbox.dtdm.model.ProcessorProperty;
import com.liaison.mailbox.service.dto.SweeperStaticPropertiesDTO;
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

            LOGGER.info("Processor details in consumer {}", sweeperEventDTO.getProcessor());
            //construct workticket for downloaded file.
            workTicket = constructWorkticket(sweeperEventDTO.getFile(), sweeperEventDTO.getStaticProp(), sweeperEventDTO.getMailBoxId());
            globalProcessorId = workTicket.getGlobalProcessId();
            LOGGER.info("Workticket Constructed and Global Processor ID is {}" , globalProcessorId);
            persistPayloadAndWorkticket(workTicket, sweeperEventDTO.getStaticProp(), sweeperEventDTO.getTtlMap(), sweeperEventDTO.getDynamicProperties());

            String workTicketToSb = JAXBUtility.marshalToJSON(workTicket);
            LOGGER.info("Workticket posted to SB queue.{}", new JSONObject(workTicketToSb).toString(2));
            SweeperQueueSendClient.post(workTicketToSb, false);
            verifyAndDeletePayload(workTicket);

        } catch (JAXBException | IOException | IllegalAccessException | JSONException e) {
            LOGGER.error("Payload cannot persist and workticket.");
        } catch (MailBoxServicesException e) {
            
        	LOGGER.error("Persist error cannot persist so retring again for persist payload and workticket");
            String workTicketToSb = null;
            
            try {
            	LOGGER.error("Persist error cannot persist so retring again for persist payload and workticket and cannot persist retried again and also canno proceed also -- first time");
                persistPayloadAndWorkticket(workTicket, sweeperEventDTO.getStaticProp(), sweeperEventDTO.getTtlMap(), sweeperEventDTO.getDynamicProperties());
                LOGGER.error("Persist error cannot persist so retring again for persist payload and workticket and cannot persist retried again and also canno proceed also");
                workTicketToSb = JAXBUtility.marshalToJSON(workTicket);
                LOGGER.info("Workticket posted to SB queue.{}", new JSONObject(workTicketToSb).toString(2));
                SweeperQueueSendClient.post(workTicketToSb, false);
                verifyAndDeletePayload(workTicket);
            } catch (IOException | JAXBException | JSONException e1) {
                LOGGER.error("Persist error cannot persist so retring again for persist payload and workticket and cannot persist retried again");
                return;
            } catch (MailBoxServicesException e2) {
            	LOGGER.error("Persist error cannot persist so retring again for persist payload and workticket and cannot persist retried again--- second time");
                throw e2;
            }
        }
    }

    /**
     * This method is used to construct workticket from the given file.
     *
     * @param java.io.File file
     * @param SFTPDownloaderPropertiesDTO
     * @return workticket
     * @throws IllegalAccessException
     * @throws IOException
     */
    private WorkTicket constructWorkticket(File file, SweeperStaticPropertiesDTO staticProp, String mailBoxId) throws IllegalAccessException, IOException {

        Map<String, Object> additionalContext = new HashMap<String, Object>();
        additionalContext.put(MailBoxConstants.KEY_FILE_PATH, file.getAbsoluteFile());
        additionalContext.put(MailBoxConstants.KEY_MAILBOX_ID, mailBoxId);
        additionalContext.put(MailBoxConstants.KEY_FOLDER_NAME, file.getParent());

        BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
        LOGGER.debug("File attributes{}", attr);

        FileTime modifiedTime = attr.lastModifiedTime();
        LOGGER.debug("Modified Time stamp {}", modifiedTime);

        ISO8601Util dateUtil = new ISO8601Util();
        FileTime createdTime = attr.creationTime();
        LOGGER.debug("Created Time stamp {}", createdTime);

        WorkTicket workTicket = new WorkTicket();
        workTicket.setGlobalProcessId(MailBoxUtil.getGUID());
        workTicket.setPipelineId(staticProp.getPipeLineID());
        workTicket.setProcessMode(ProcessMode.ASYNC);
        workTicket.setPayloadURI(file.getAbsolutePath().toString());
        workTicket.setCreatedTime(new Date(createdTime.toMillis()));
        workTicket.addHeader(MailBoxConstants.KEY_FILE_CREATED_NAME, dateUtil.fromDate(workTicket.getCreatedTime()));
        workTicket.addHeader(MailBoxConstants.KEY_FILE_MODIFIED_NAME, dateUtil.fromDate(new Date(modifiedTime.toMillis())));
        workTicket.setPayloadSize(attr.size());
        workTicket.setFileName(file.getName());
        workTicket.addHeader(MailBoxConstants.KEY_FILE_NAME, file.getName());
        workTicket.addHeader(MailBoxConstants.KEY_FOLDER_NAME, file.getParent());
        workTicket.setAdditionalContext(additionalContext);

        return workTicket;
    }

    /**
     * verifies whether the payload persisted in storage utilities or not and deletes it
     *
     * @param wrkTicket workticket contains payload uri
     * @throws IOException
     */
    private void verifyAndDeletePayload(WorkTicket wrkTicket) throws IOException {

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
     * @param staticProp staic properties
     * @param workTicket workticket
     * @throws IOException
     * @throws JAXBException 
     * @throws JSONException 
     */
    private void persistPayloadAndWorkticket(WorkTicket workTicket, SweeperStaticPropertiesDTO staticProp, Map<String, String> ttlMap, Set<ProcessorProperty> dynamicProperties) throws IOException, JAXBException, JSONException {

        File payloadFile = new File(workTicket.getPayloadURI());
        Map<String, String> properties = new HashMap<>();
//        Map<String, String> ttlMap = processor.getTTLUnitAndTTLNumber();

        if (!ttlMap.isEmpty()) {
            Integer ttlDays = Integer.parseInt(ttlMap.get(MailBoxConstants.TTL_NUMBER));
            workTicket.setTtlDays(MailBoxUtil.convertTTLIntoDays(ttlMap.get(MailBoxConstants.CUSTOM_TTL_UNIT), ttlDays));
        }

        properties.put(MailBoxConstants.PROPERTY_HTTPLISTENER_SECUREDPAYLOAD, String.valueOf(staticProp.isSecuredPayload()));
        properties.put(MailBoxConstants.PROPERTY_LENS_VISIBILITY, String.valueOf(staticProp.isLensVisibility()));
        properties.put(MailBoxConstants.KEY_PIPELINE_ID, staticProp.getPipeLineID());
        properties.put(MailBoxConstants.STORAGE_IDENTIFIER_TYPE, MailBoxUtil.getStorageType(dynamicProperties));

        String contentType = MailBoxUtil.isEmpty(staticProp.getContentType()) ? MediaType.TEXT_PLAIN : staticProp.getContentType();
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