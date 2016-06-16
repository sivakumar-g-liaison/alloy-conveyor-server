/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.mailbox.service.core.processor;

import com.liaison.commons.exception.LiaisonException;
import com.liaison.commons.util.client.ftps.G2FTPSClient;
import com.liaison.commons.util.client.sftp.StringUtil;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.dtdm.model.Processor;
import com.liaison.mailbox.enums.ExecutionState;
import com.liaison.mailbox.service.core.fsm.MailboxFSM;
import com.liaison.mailbox.service.core.processor.helper.FTPSClient;
import com.liaison.mailbox.service.dto.configuration.processor.properties.FTPUploaderPropertiesDTO;
import com.liaison.mailbox.service.util.MailBoxUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

/**
 *
 * FTPS remote uploader to perform pull operation, also it has support methods
 * for JavaScript.
 *
 * @author OFS
 *
 */
public class FTPSRemoteUploader extends AbstractRemoteUploader {

    private static final Logger LOGGER = LogManager.getLogger(FTPSRemoteUploader.class);

    /*
     * Required for JS
     */
    private G2FTPSClient ftpsClient;

    @SuppressWarnings("unused")
    private FTPSRemoteUploader() {
    }

    public FTPSRemoteUploader(Processor processor) {
        super(processor);
    }

    /**
     * Java method to execute the FTPRequest to upload the file or folder
     *
     * @param executionId current execution id
     * @param fsm fsm instance
     */
    @Override
    protected void executeRequest(String executionId, MailboxFSM fsm) {

        G2FTPSClient ftpsRequest = null;
        try {

            //validates the local path
            String path = validateLocalPath();

            //validates the remote path
            String remotePath = validateRemotePath();

            //Checks the local path has files to upload
            //TODO recurseSubDirs
            File[] subFiles = getFilesToUpload(false);
            if (subFiles == null || subFiles.length == 0) {
                LOGGER.info(constructMessage("The given payload location {} doesn't have files to upload."), path);
                return;
            }

            ftpsRequest = (G2FTPSClient) getClient();
            ftpsRequest.setLogPrefix(constructMessage());
            ftpsRequest.connect();
            ftpsRequest.login();

            // retrieve required properties
            FTPUploaderPropertiesDTO ftpUploaderStaticProperties = (FTPUploaderPropertiesDTO) getProperties();
            setPassive(ftpsRequest, ftpUploaderStaticProperties);

            LOGGER.info(constructMessage("Start run"));
            long startTime = System.currentTimeMillis();

            changeDirectory(ftpsRequest, remotePath);
            LOGGER.info(constructMessage("Ready to upload files from local path {} to remote path {}"), path, remotePath);
            uploadDirectory(ftpsRequest, path, remotePath, executionId, fsm, subFiles);

            long endTime = System.currentTimeMillis();
            LOGGER.info(constructMessage("Number of files processed {}"), totalNumberOfProcessedFiles);
            LOGGER.info(constructMessage("Total time taken to process files {}"), endTime - startTime);
            LOGGER.info(constructMessage("End run"));

        } catch (Exception e) {
            LOGGER.error(constructMessage("Error occurred during ftp(s) upload", seperator, e.getMessage()), e);
            throw new RuntimeException(e);
        } finally {
            disconnect(ftpsRequest);
        }
    }

    /**
     * Java method to upload the file or folder
     *
     * @throws IOException
     * @throws IllegalAccessException
     * @throws LiaisonException
     */
    public void uploadDirectory(G2FTPSClient ftpsRequest,
                                String localParentDir,
                                String remoteParentDir,
                                String executionId,
                                MailboxFSM fsm,
                                File[] subFiles) throws IOException, IllegalAccessException, LiaisonException {

        Date lastCheckTime = new Date();
        String constantInterval = MailBoxUtil.getEnvironmentProperties().getString(MailBoxConstants.DEFAULT_INTERRUPT_SIGNAL_FREQUENCY_IN_SEC);

        for (File item : subFiles) {

            //interrupt signal check has to be done only if execution Id is present
            if (!StringUtil.isNullOrEmptyAfterTrim(executionId)
                    && ((new Date().getTime() - lastCheckTime.getTime()) / 1000) > Long.parseLong(constantInterval)) {

                if (isThereAnInterruptSignal(executionId, fsm)) {
                    return;
                }
                lastCheckTime = new Date();
            }

            //FTPS
            uploadFile(ftpsRequest, localParentDir, remoteParentDir, item);
        }
    }

    /**
     * Upload files to remote location
     *
     * @param ftpsRequest ftps client
     * @param localParentDir local payload location
     * @param remoteParentDir remote payload location
     * @param file file
     * @throws IOException
     * @throws LiaisonException
     * @throws IllegalAccessException
     */
    private void uploadFile(G2FTPSClient ftpsRequest, String localParentDir, String remoteParentDir, File file)
            throws IOException, LiaisonException, IllegalAccessException {

        int replyCode;
        FTPUploaderPropertiesDTO staticProp = (FTPUploaderPropertiesDTO) getProperties();
        String statusIndicator = staticProp.getFileTransferStatusIndicator();
        String currentFileName = file.getName();

        // Check if the file to be uploaded is included or not excluded file must not be uploaded
        if (!checkFileIncludeorExclude(staticProp.getIncludedFiles(),
                currentFileName,
                staticProp.getExcludedFiles())) {
            return;
        }

        //add status indicator if specified to indicate that uploading is in progress
        String uploadingFileName = (!MailBoxUtil.isEmpty(statusIndicator))
                ? currentFileName + "." + statusIndicator
                : currentFileName;

        // upload file
        try (InputStream inputStream = new FileInputStream(file)) {
            ftpsRequest.changeDirectory(remoteParentDir);
            LOGGER.info(constructMessage("uploading file {} from local path {} to remote path {}"),
                    currentFileName, localParentDir, remoteParentDir);
            replyCode = ftpsRequest.putFile(uploadingFileName, inputStream);
        }

        // Check whether the file is uploaded successfully
        if (replyCode == MailBoxConstants.CLOSING_DATA_CONNECTION
                || replyCode == MailBoxConstants.FTP_FILE_TRANSFER_ACTION_OK) {

            LOGGER.info(constructMessage("File {} uploaded successfully"), currentFileName);
            // Renames the uploaded file to original extension once the fileStatusIndicator is given by User
            if (!MailBoxUtil.isEmpty(statusIndicator)) {
                int renameStatus = ftpsRequest.renameFile(uploadingFileName, currentFileName);
                if (renameStatus == MailBoxConstants.FTP_FILE_TRANSFER_ACTION_OK) {
                    LOGGER.info(constructMessage("File {} renamed successfully"), currentFileName);
                } else {
                    LOGGER.info(constructMessage("File {} renaming failed"), currentFileName);
                }
            }

            deleteFile(file);
            StringBuilder message = new StringBuilder()
                    .append("File ")
                    .append(currentFileName)
                    .append(" uploaded successfully")
                    .append(" to remote path ")
                    .append(remoteParentDir);

            // Glass Logging
            logGlassMessage(message.toString(), file, ExecutionState.COMPLETED);
            totalNumberOfProcessedFiles++;
        } else {

            StringBuilder message = new StringBuilder()
                    .append("Failed to upload file ")
                    .append(currentFileName)
                    .append(" from local path ")
                    .append(localParentDir)
                    .append(" to remote path ")
                    .append(remoteParentDir);

            // Glass Logging
            logGlassMessage(message.toString(), file, ExecutionState.FAILED);
        }
    }

    @Override
    public Object getClient() {
        ftpsClient = (G2FTPSClient) FTPSClient.getClient(this);
        return ftpsClient;
    }

    @Override
    public void cleanup() {
        if (null != ftpsClient) {
            try {
                ftpsClient.disconnect();
            } catch (LiaisonException e) {
                LOGGER.error(constructMessage("Failed to close connection"), e);
            }
        }
    }

}