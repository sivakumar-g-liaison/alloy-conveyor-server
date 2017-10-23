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
import com.liaison.mailbox.rtdm.dao.StagedFileDAO;
import com.liaison.mailbox.rtdm.dao.StagedFileDAOBase;
import com.liaison.mailbox.rtdm.model.StagedFile;
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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


/**
 * Service to stage the replicated files
 *
 * @author OFS
 */
public class FileStageReplicationService implements Runnable {

    private static final Logger LOGGER = LogManager.getLogger(FileStageReplicationService.class);
    private static final String URI = "uri";
    private static final String GLOBAL_PROCESS_ID = "globalProcessId";
    private static final String PAYLOAD_LOCATION = "payloadLocation";

    private String message;
    private Long delay;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getDelay() {
        return delay;
    }

    public void setDelay(Long delay) {
        this.delay = delay;
    }

    public FileStageReplicationService(String message) {
        this.message = message;
        this.delay = MailBoxUtil.getEnvironmentProperties().getLong("com.liaison.mailbox.file.stage.replication.retry.delay", 10000);
    }

    @Override
    public void run() {
        try {
            this.stage(getMessage());
        } catch (JSONException | ClientUnavailableException | IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     * Method to in-activate staged file entry and update lens status.
     *
     * @param requestString message string from the queue
     */
    public void stage(String requestString) throws JSONException, ClientUnavailableException, IOException {

        JSONObject requestObj = new JSONObject(requestString);
        String fs2uri = (String) requestObj.get(URI);
        String globalProcessId = (String) requestObj.get(GLOBAL_PROCESS_ID);
        String payloadLocation = (String) requestObj.get(PAYLOAD_LOCATION);

        //Initial Check
        StagedFileDAO stagedFileDAO = new StagedFileDAOBase();
        StagedFile stagedFile = stagedFileDAO.findStagedFileByGpid(globalProcessId);

        if (null == stagedFile) {
            //Staged file is not replicated so adding back to queue with delay
            LOGGER.warn("Posting back to queue since staged_file isn't replicated - datacenter");
            FileStageReplicationSendQueue.getInstance().sendMessage(requestString, delay);
        } else {

            //write the file
            InputStream response = null;
            FileOutputStream outputStream = null;
            try {

                ThreadContext.put(LogTags.GLOBAL_PROCESS_ID, globalProcessId);
                response = StorageUtilities.retrievePayload(fs2uri);
                Path file = Files.createFile(Paths.get(payloadLocation));
                outputStream = new FileOutputStream(file.toFile());
                IOUtils.copy(response, outputStream);
                LOGGER.info("Staged the file successfully - datacenter");

            } catch (MailBoxServicesException e) {
                //Payload doesn't exist in BOSS so adding back to queue with delay
                LOGGER.warn("Posting back to queue since payload isn't replicated - datacenter");
                FileStageReplicationSendQueue.getInstance().sendMessage(requestString, delay);
            } finally {
                if (null != response) {
                    response.close();
                }
                if (null != outputStream) {
                    outputStream.close();
                }
                ThreadContext.clearMap();
            }

        }

    }

}
