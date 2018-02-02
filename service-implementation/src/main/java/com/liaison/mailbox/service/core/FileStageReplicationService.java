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

import com.liaison.commons.logging.LogTags;
import com.liaison.commons.messagebus.client.exceptions.ClientUnavailableException;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.dtdm.dao.ProcessorConfigurationDAOBase;
import com.liaison.mailbox.dtdm.model.Processor;
import com.liaison.mailbox.rtdm.dao.StagedFileDAO;
import com.liaison.mailbox.rtdm.dao.StagedFileDAOBase;
import com.liaison.mailbox.rtdm.model.StagedFile;
import com.liaison.mailbox.service.core.processor.FileWriter;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.queue.sender.FileStageReplicationSendQueue;
import com.liaison.mailbox.service.storage.util.StorageUtilities;
import com.liaison.mailbox.service.util.MailBoxUtil;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static com.liaison.mailbox.MailBoxConstants.FILE_STAGE_REPLICATION_RETRY_DELAY;
import static com.liaison.mailbox.MailBoxConstants.FILE_STAGE_REPLICATION_RETRY_MAX_COUNT;
import static com.liaison.mailbox.MailBoxConstants.GLOBAL_PROCESS_ID;
import static com.liaison.mailbox.MailBoxConstants.RETRY_COUNT;
import static com.liaison.mailbox.MailBoxConstants.URI;
import static com.liaison.mailbox.MailBoxConstants.KEY_PROCESSOR_ID;
import static com.liaison.mailbox.MailBoxConstants.KEY_TARGET_DIRECTORY;
import static com.liaison.mailbox.MailBoxConstants.KEY_TARGET_DIRECTORY_MODE;
import static com.liaison.mailbox.MailBoxConstants.KEY_FILE_NAME;
import static com.liaison.mailbox.MailBoxConstants.KEY_OVERWRITE;


/**
 * Service to stage the files posted from other dc
 *
 * @author OFS
 */
public class FileStageReplicationService implements Runnable {

    private static final Logger LOGGER = LogManager.getLogger(FileStageReplicationService.class);

    private static final Long DELAY = MailBoxUtil.getEnvironmentProperties().getLong(FILE_STAGE_REPLICATION_RETRY_DELAY, 10000);
    private static final Long MAX_RETRY_COUNT = MailBoxUtil.getEnvironmentProperties().getLong(FILE_STAGE_REPLICATION_RETRY_MAX_COUNT, 10);

    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public FileStageReplicationService() {
        
    }
    
    public FileStageReplicationService(String message) {
        this.message = message;
    }

    @Override
    public void run() {
        try {
            this.stage(getMessage());
        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     * Staged the files which is posted from other dc
     *
     * @param requestString message string from the queue
     */
    public void stage(String requestString) throws JSONException, ClientUnavailableException, IOException {

        JSONObject requestObj = new JSONObject(requestString);
        String fs2uri = (String) requestObj.get(URI);
        String globalProcessId = (String) requestObj.get(GLOBAL_PROCESS_ID);
        String processorId = (String) requestObj.get(KEY_PROCESSOR_ID);
        String targetDirectory = (String) requestObj.get(KEY_TARGET_DIRECTORY);
        String mode = (String) requestObj.get(KEY_TARGET_DIRECTORY_MODE);
        String fileName = (String) requestObj.get(KEY_FILE_NAME);
        String isOverwrite = (String) requestObj.get(KEY_OVERWRITE);
        InputStream payload = null;
        
        int retry = (int) requestObj.get(RETRY_COUNT);

        if (retry > MAX_RETRY_COUNT) {
            LOGGER.warn("Reached maximum retry and dropping this message - {}", message);
            return;
        }
        requestObj.put(RETRY_COUNT, ++retry);
        requestString = requestObj.toString();

        //Initial Check
        StagedFileDAO stagedFileDAO = new StagedFileDAOBase();
        StagedFile stagedFile = stagedFileDAO.findStagedFileByGpid(globalProcessId);

        if (null == stagedFile) {
            //Staged file is not replicated so adding back to queue with delay
            LOGGER.warn("Posting back to queue since staged_file isn't replicated - datacenter");
            FileStageReplicationSendQueue.getInstance().sendMessage(requestString, DELAY);
        } else {

            try {
                
                ThreadContext.put(LogTags.GLOBAL_PROCESS_ID, globalProcessId);
                
                Processor processor = new ProcessorConfigurationDAOBase().find(Processor.class, processorId);
                FileWriter fileWriter = new FileWriter(processor);
                String processorPayloadLocation = fileWriter.getReplicatePayloadLocation(targetDirectory, mode);

                File file = new File(processorPayloadLocation + File.separatorChar + fileName);
                payload = StorageUtilities.retrievePayload(fs2uri);

                if (file.exists()) {
                    if (MailBoxConstants.OVERWRITE_TRUE.equals(isOverwrite)) {
                        persistFile(payload, file);
                    }
                } else {
                    persistFile(payload, file);
                }
                
            } catch (MailBoxServicesException | IllegalArgumentException e) {
                // Payload doesn't exist in BOSS so adding back to queue with delay
                LOGGER.warn("Posting back to queue since payload isn't replicated - datacenter");
                FileStageReplicationSendQueue.getInstance().sendMessage(requestString, DELAY);
            } finally {
                ThreadContext.clearMap();
                if (null != payload) {
                    payload.close();
                }
            }
        }

    }
    
    /**
     * method to persist the file for replication 
     * 
     * @param response
     * @param file
     * @throws IOException
     */
    private void persistFile(InputStream response, File file) throws IOException {
        
        //write the file
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            IOUtils.copy(response, outputStream);
            LOGGER.info("Staged the file successfully - datacenter");
        }
    }

}
