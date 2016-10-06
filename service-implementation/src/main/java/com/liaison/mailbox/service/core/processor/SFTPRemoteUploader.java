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

import com.jcraft.jsch.SftpException;
import com.liaison.commons.exception.LiaisonException;
import com.liaison.commons.util.client.sftp.G2SFTPClient;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.dtdm.model.Processor;
import com.liaison.mailbox.enums.ExecutionState;
import com.liaison.mailbox.service.core.processor.helper.ClientFactory;
import com.liaison.mailbox.service.dto.configuration.processor.properties.SFTPUploaderPropertiesDTO;
import com.liaison.mailbox.service.executor.javascript.JavaScriptExecutorUtil;
import com.liaison.mailbox.service.util.MailBoxUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * SFTP remote uploader to perform push operation, also it has support methods
 * for JavaScript.
 *
 * @author OFS
 */
public class SFTPRemoteUploader extends AbstractRemoteUploader {

    private static final Logger LOGGER = LogManager.getLogger(SFTPRemoteUploader.class);

    /*
     * Required for JS
     */
    private G2SFTPClient sftpClient;

    @SuppressWarnings("unused")
    private SFTPRemoteUploader() {
    }

    public SFTPRemoteUploader(Processor processor) {
        super(processor);
    }

    /**
     * Java method to execute the SFTP request to upload the file or folder
     *
     */
    @Override
    protected void executeRequest(String executionId) {

        G2SFTPClient sftpRequest = null;
        try {

            LOGGER.info(constructMessage("Start run"));
            long startTime = System.currentTimeMillis();

            //validates the local path
            String path = validateLocalPath();

            //validates the remote path
            String remotePath = validateRemotePath();

            //retrieve required properties
            SFTPUploaderPropertiesDTO sftpUploaderStaticProperties = (SFTPUploaderPropertiesDTO)getProperties();
            
            boolean recursiveSubdirectories = sftpUploaderStaticProperties.isRecurseSubDirectories();
            setDirectUpload(sftpUploaderStaticProperties.isDirectUpload());
            File[] subFiles = getFilesToUpload(recursiveSubdirectories);
            if (subFiles == null || subFiles.length == 0) {
                LOGGER.info(constructMessage("The given payload location {} doesn't have files to upload."), path);
                return;
            }

            sftpRequest = (G2SFTPClient) getClient();
            sftpRequest.setLogPrefix(constructMessage());
            sftpRequest.connect();
            if (sftpRequest.openChannel()) {

                changeDirectory(sftpRequest, remotePath);
                LOGGER.info(constructMessage("Ready to upload files from local path {} to remote path {}"), path, remotePath);
                uploadDirectory(sftpRequest, path, remotePath, executionId, subFiles);
            }

            long endTime = System.currentTimeMillis();
            LOGGER.info(constructMessage("Number of files processed {}"), totalNumberOfProcessedFiles);
            LOGGER.info(constructMessage("Total time taken to process files {}"), endTime - startTime);
            LOGGER.info(constructMessage("End run"));

        } catch (Exception e) {
            LOGGER.error(constructMessage("Error occurred during sftp upload", seperator, e.getMessage()), e);
            throw new RuntimeException(e);
        } finally {
            if (sftpRequest != null) {
                sftpRequest.disconnect();
            }
        }

    }

    /**
     * Java method to upload the file or folder
     *
     * @param sftpClient sftp client
     * @param localParentDir local payload location
     * @param remoteParentDir remote location
     * @param executionId current execution id
     * @param fsm FSM instance
     * @param files files from local payload location
     * @throws IOException
     * @throws IllegalAccessException
     * @throws LiaisonException
     * @throws SftpException
     */
    private void uploadDirectory(G2SFTPClient sftpClient,
                                 String localParentDir,
                                 String remoteParentDir,
                                 String executionId,
                                 File[] files)
            throws IOException, IllegalAccessException, LiaisonException, SftpException {

        for (File item : files) {
            uploadFile(sftpClient, remoteParentDir, item);

        }

    }

    /**
     * Uploads files to remote location
     *
     * @param sftpRequest sftp client
     * @param localParentDir local payload location
     * @param remoteParentDir remote payload location
     * @param file file to be uploaded
     * @throws IOException
     * @throws LiaisonException
     */
    private void uploadFile(G2SFTPClient sftpRequest,
                            String remoteParentDir,
                            File file) throws IOException, LiaisonException, IllegalAccessException {

        int replyCode;
        String filePath = file.getParent();
        SFTPUploaderPropertiesDTO staticProp = (SFTPUploaderPropertiesDTO) getProperties();
        String currentFileName = file.getName();

        // Check if the file to be uploaded is included or not excluded
        if (!checkFileIncludeorExclude(staticProp.getIncludedFiles(),
                currentFileName,
                staticProp.getExcludedFiles())) {
            return;
        }

        //add status indicator if specified to indicate that uploading is in progress
        String statusIndicator = staticProp.getFileTransferStatusIndicator();
        String uploadingFileName = (!MailBoxUtil.isEmpty(statusIndicator))
                ? currentFileName + "." + statusIndicator
                : currentFileName;

        // upload the file
        try (InputStream inputStream = new FileInputStream(file)) {

            sftpRequest.changeDirectory(remoteParentDir);
            LOGGER.info(constructMessage("uploading file {} from local path {} to remote path {}"),
                    currentFileName, filePath, remoteParentDir);
            replyCode = sftpRequest.putFile(uploadingFileName, inputStream);
        }

        // Check whether the file uploaded successfully
        if (replyCode == MailBoxConstants.SFTP_FILE_TRANSFER_ACTION_OK) {

            LOGGER.info(constructMessage("File {} uploaded successfully"), currentFileName);

            // Renames the uploaded file to original extension if the fileStatusIndicator is given by User
            if (!MailBoxUtil.isEmpty(statusIndicator)) {
                int renameStatus = sftpRequest.renameFile(uploadingFileName, currentFileName);
                if (renameStatus == MailBoxConstants.SFTP_FILE_TRANSFER_ACTION_OK) {
                    LOGGER.info(constructMessage("File {} renamed successfully"), currentFileName);
                } else {
                    LOGGER.info(constructMessage("File {} renaming failed"), currentFileName);
                }
            }

            // delete files once successfully uploaded
            deleteFile(file);
            StringBuilder message = new StringBuilder()
                    .append("File ")
                    .append(currentFileName)
                    .append(" uploaded successfully")
                    .append(" to remote path ")
                    .append(remoteParentDir);
            // Glass Logging
            logToLens(message.toString(), file, ExecutionState.COMPLETED);
            totalNumberOfProcessedFiles++;
        } else {

            StringBuilder message = new StringBuilder()
                    .append("Failed to upload file ")
                    .append(currentFileName)
                    .append(" from local path ")
                    .append(filePath)
                    .append(" to remote path ")
                    .append(remoteParentDir);

            // Glass Logging
            logGlassMessage(message.toString(), file, ExecutionState.FAILED);
        }
    }

    @Override
    public Object getClient() {
        sftpClient = (G2SFTPClient) ClientFactory.getClient(this);
        return sftpClient;
    }

    @Override
    public void cleanup() {
        if (null != sftpClient) {
            sftpClient.disconnect();
        }
    }

    @Override
    public void doDirectUpload(String fileName, String folderPath) {

        setDirectUpload(true);
        boolean isHandOverExecutionToJavaScript = false;
        int scriptExecutionTimeout;
        try {
            isHandOverExecutionToJavaScript = ((SFTPUploaderPropertiesDTO) getProperties()).isHandOverExecutionToJavaScript();
            scriptExecutionTimeout = ((SFTPUploaderPropertiesDTO) getProperties()).getScriptExecutionTimeout();
        } catch (IllegalArgumentException | IllegalAccessException | IOException e) {
            throw new RuntimeException(e);
        }

        if (isHandOverExecutionToJavaScript) {
            setFileName(fileName);
            setFolderPath(folderPath);
            setMaxExecutionTimeout(scriptExecutionTimeout);
            JavaScriptExecutorUtil.executeJavaScript(configurationInstance.getJavaScriptUri(), this);
        } else {

            G2SFTPClient sftpRequest = null;
            try {

                String remotePath = validateRemotePath();

                sftpRequest = (G2SFTPClient) getClient();
                sftpRequest.setLogPrefix(constructMessage());
                sftpRequest.connect();

                if (sftpRequest.openChannel()) {

                    changeDirectory(sftpRequest, remotePath);
                    LOGGER.info(constructMessage("Ready to upload file {} from local path {} to remote path {}"),
                            fileName,
                            folderPath,
                            remotePath);
                    uploadFile(sftpRequest,
                            remotePath,
                            new File(folderPath + File.separatorChar + fileName));

                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                if (sftpRequest != null) {
                    sftpRequest.disconnect();
                }
            }
        }
    }

    @Override
    protected int getScriptExecutionTimeout() throws IOException, IllegalAccessException {
        return ((SFTPUploaderPropertiesDTO) getProperties()).getScriptExecutionTimeout();
    }

}