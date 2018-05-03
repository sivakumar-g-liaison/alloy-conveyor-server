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
import com.liaison.mailbox.enums.EntityStatus;
import com.liaison.mailbox.rtdm.dao.StagedFileDAO;
import com.liaison.mailbox.rtdm.dao.StagedFileDAOBase;
import com.liaison.mailbox.rtdm.model.StagedFile;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.queue.sender.FileStageReplicationSendQueue;
import com.liaison.mailbox.service.storage.util.StorageUtilities;
import com.liaison.mailbox.service.util.DirectoryCreationUtil;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.liaison.mailbox.MailBoxConstants.FILE_STAGE_REPLICATION_RETRY_DELAY;
import static com.liaison.mailbox.MailBoxConstants.FILE_STAGE_REPLICATION_RETRY_MAX_COUNT;
import static com.liaison.mailbox.MailBoxConstants.GLOBAL_PROCESS_ID;
import static com.liaison.mailbox.MailBoxConstants.RETRY_COUNT;
import static com.liaison.mailbox.MailBoxConstants.URI;
import static com.liaison.mailbox.MailBoxConstants.KEY_PROCESSOR_ID;
import static com.liaison.mailbox.MailBoxConstants.KEY_FILE_NAME;
import static com.liaison.mailbox.MailBoxConstants.KEY_FILE_PATH;
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
     * @param requestMessage message string from the queue
     */
    public void stage(String requestMessage) throws JSONException, ClientUnavailableException, IOException {

        JSONObject requestObj = new JSONObject(requestMessage);
        String fs2uri = requestObj.getString(URI);
        String globalProcessId = requestObj.getString(GLOBAL_PROCESS_ID);
        String fileName = requestObj.getString(KEY_FILE_NAME);
        String processorPayloadLocation = requestObj.getString(KEY_FILE_PATH);
        String isOverwrite = requestObj.getString(KEY_OVERWRITE);
        InputStream payload = null;

        int retry = (int) requestObj.get(RETRY_COUNT);
        if (retry > MAX_RETRY_COUNT) {
            LOGGER.warn("Reached maximum retry and dropping this message - {}", requestMessage);
            return;
        }
        requestObj.put(RETRY_COUNT, ++retry);
        requestMessage = requestObj.toString();

        //Initial Check
        StagedFileDAO stagedFileDAO = new StagedFileDAOBase();
        StagedFile stagedFile = stagedFileDAO.findStagedFilesByGlobalProcessIdWithoutProcessDc(globalProcessId);
        if (null == stagedFile) {
            //Staged file is not replicated so adding back to queue with delay
            LOGGER.warn("Posting back to queue since staged_file isn't replicated - datacenter - gpid {}", globalProcessId);
            FileStageReplicationSendQueue.getInstance().sendMessage(requestMessage, DELAY);
        } else {

            if (EntityStatus.INACTIVE.value().equalsIgnoreCase(stagedFile.getStagedFileStatus())) {
                LOGGER.warn("Staged file is inactive status and there is no need to stage it - {}", globalProcessId);
                return;
            }

            try {

                ThreadContext.put(LogTags.GLOBAL_PROCESS_ID, globalProcessId);
                Path path = Paths.get(processorPayloadLocation);
                if (Files.notExists(path)) {
                    DirectoryCreationUtil.createPathIfNotAvailable(processorPayloadLocation);
                }

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
                LOGGER.warn("Posting back to queue since payload isn't replicated - datacenter - gpid {}", globalProcessId);
                FileStageReplicationSendQueue.getInstance().sendMessage(requestMessage, DELAY);
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
        FileOutputStream outputStream = null;
        
        try {
            outputStream = new FileOutputStream(file);
            IOUtils.copy(response, outputStream);
            LOGGER.info("Staged the file successfully - datacenter");
        } finally {
            if (null != outputStream) {
                outputStream.close();
            }
        }
    }

}
