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
import com.liaison.commons.logging.LogTags;
import com.liaison.commons.util.client.ftps.G2FTPSClient;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.dtdm.model.Processor;
import com.liaison.mailbox.enums.ExecutionState;
import com.liaison.mailbox.rtdm.dao.StagedFileDAO;
import com.liaison.mailbox.rtdm.dao.StagedFileDAOBase;
import com.liaison.mailbox.rtdm.model.StagedFile;
import com.liaison.mailbox.service.core.processor.helper.FTPSClient;
import com.liaison.mailbox.service.dto.configuration.processor.properties.FTPUploaderPropertiesDTO;
import com.liaison.mailbox.service.dto.remote.uploader.RelayFile;
import com.liaison.mailbox.service.executor.javascript.JavaScriptExecutorUtil;
import com.liaison.mailbox.service.util.MailBoxUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

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
     */
    @Override
    protected void executeRequest() {

        G2FTPSClient ftpsRequest = null;
        try {

            //validates the local path
            String path = validateLocalPath();

            //validates the remote path
            String remotePath = validateRemotePath();

            // retrieve required properties
            FTPUploaderPropertiesDTO ftpUploaderStaticProperties = (FTPUploaderPropertiesDTO)getProperties();

            boolean recursiveSubdirectories = ftpUploaderStaticProperties.isRecurseSubDirectories();
            setDirectUpload(ftpUploaderStaticProperties.isDirectUpload());

            // TODO use processor properties
            Object[] subFiles = (this.canUseFileSystem())
                    ? getFilesToUpload(recursiveSubdirectories)
                    : getRelayFiles(recursiveSubdirectories);

            if (subFiles == null || subFiles.length == 0) {
                LOGGER.info(constructMessage("The given payload location {} doesn't have files to upload."), path);
                return;
            }

            ftpsRequest = (G2FTPSClient) getClient();
            ftpsRequest.setLogPrefix(constructMessage());
            ftpsRequest.connect();
            ftpsRequest.login();

            // set required properties
            setPassive(ftpsRequest, ftpUploaderStaticProperties);

            LOGGER.info(constructMessage("Start run"));
            long startTime = System.currentTimeMillis();

            changeDirectory(ftpsRequest, remotePath);
            LOGGER.info(constructMessage("Ready to upload files from local path {} to remote path {}"), path, remotePath);
            uploadDirectory(ftpsRequest, remotePath, subFiles);

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
    private void uploadDirectory(G2FTPSClient ftpsRequest,
                                String remoteParentDir,
                                Object[] files)
            throws IOException, IllegalAccessException, LiaisonException {

        for (Object file : files) {

            //FTPS
            if (MailBoxUtil.isInterrupted(Thread.currentThread().getName())) {
                LOGGER.warn("The executor is gracefully interrupted");
                return;
            }

            if (file instanceof File) {
                uploadFile(ftpsRequest, remoteParentDir, (File) file);
            } else {

                try {
                    RelayFile relayFile = ((RelayFile) file);
                    ThreadContext.put(LogTags.GLOBAL_PROCESS_ID, relayFile.getGlobalProcessId());
                    uploadFile(ftpsRequest, remoteParentDir, relayFile);
                } finally {
                    ThreadContext.clearMap();
                }
            }
        }
    }

    /**
     * Upload files to remote location
     *
     * @param ftpsRequest ftps client
     * @param remoteParentDir remote payload location
     * @param file file
     * @throws IOException
     * @throws LiaisonException
     * @throws IllegalAccessException
     */
    private void uploadFile(G2FTPSClient ftpsRequest, String remoteParentDir, File file)
            throws IOException, LiaisonException, IllegalAccessException {

        int replyCode;
        String filePath = file.getParent();
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
            LOGGER.info(constructMessage("uploading file {} from local path {} to remote path {}"),
                    currentFileName, filePath, remoteParentDir);
            replyCode = ftpsRequest.putFile(uploadingFileName, inputStream);
        }

        // Check whether the file is uploaded successfully
        if (replyCode == MailBoxConstants.CLOSING_DATA_CONNECTION
                || replyCode == MailBoxConstants.FTP_FILE_TRANSFER_ACTION_OK) {

            LOGGER.info(constructMessage("File {} uploaded successfully"), currentFileName);
            // Renames the uploaded file to original extension once the fileStatusIndicator is given by User
            renameFile(ftpsRequest, statusIndicator, currentFileName, uploadingFileName);

            deleteFile(file);
            String message = "File " +
                    currentFileName +
                    " uploaded successfully to " +
                    getHost(staticProp.getUrl()) +
                    " and the remote path " +
                    remoteParentDir;

            // Glass Logging
            logToLens(message, file, ExecutionState.COMPLETED);
            totalNumberOfProcessedFiles++;
        } else {

            String message = "Failed to upload file " +
                    currentFileName +
                    " from local path " +
                    filePath +
                    " to remote path " +
                    remoteParentDir;

            // Glass Logging
            logToLens(message, file, ExecutionState.FAILED);
        }
    }

    /**
     * Renames the file once it is uploaded successfully
     *
     * @param ftpsRequest ftp request
     * @param statusIndicator status indicator .prg or .tst etc
     * @param currentFileName source filename
     * @param uploadingFileName target filename
     * @throws LiaisonException
     */
    private void renameFile(G2FTPSClient ftpsRequest, String statusIndicator, String currentFileName, String uploadingFileName) throws LiaisonException {

        if (!MailBoxUtil.isEmpty(statusIndicator)) {
            int renameStatus = ftpsRequest.renameFile(uploadingFileName, currentFileName);
            if (renameStatus == MailBoxConstants.FTP_FILE_TRANSFER_ACTION_OK) {
                LOGGER.info(constructMessage("File {} renamed successfully"), currentFileName);
            } else {
                LOGGER.info(constructMessage("File {} renaming failed"), currentFileName);
            }
        }
    }

    /**
     * Upload files to remote location
     *
     * @param ftpsRequest ftps client
     * @param remoteParentDir remote payload location
     * @param file file
     * @throws IOException
     * @throws LiaisonException
     * @throws IllegalAccessException
     */
    private void uploadFile(G2FTPSClient ftpsRequest, String remoteParentDir, RelayFile file)
            throws IOException, IllegalAccessException, LiaisonException {

        int replyCode;
        String filePath = file.getParent();
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
        try (InputStream inputStream = file.getPayloadInputStream()) {
            LOGGER.info(constructMessage("uploading file from storage {} to remote path {}"), file.getPayloadUri(), remoteParentDir);
            replyCode = ftpsRequest.putFile(uploadingFileName, inputStream);
        }

        // Check whether the file is uploaded successfully
        if (replyCode == MailBoxConstants.CLOSING_DATA_CONNECTION
                || replyCode == MailBoxConstants.FTP_FILE_TRANSFER_ACTION_OK) {

            LOGGER.info(constructMessage("File {} uploaded successfully"), currentFileName);
            // Renames the uploaded file to original extension once the fileStatusIndicator is given by User
            renameFile(ftpsRequest, statusIndicator, currentFileName, uploadingFileName);

            String message = "File " +
                    currentFileName +
                    " uploaded successfully to " +
                    getHost(staticProp.getUrl()) +
                    " and the remote path is " +
                    remoteParentDir;

            // Glass Logging
            logToLens(message, file, ExecutionState.COMPLETED);
            totalNumberOfProcessedFiles++;
        } else {

            String message = "Failed to upload file " +
                    currentFileName +
                    " from local path " +
                    filePath +
                    " to remote path " +
                    remoteParentDir;

            // Glass Logging
            logToLens(message, file, ExecutionState.FAILED);
        }
    }

    @Override
    public Object getClient() {
        ftpsClient = (G2FTPSClient) FTPSClient.getClient(this);
        return ftpsClient;
    }

    @Override
    public void cleanup() {
        try {
            disconnect(ftpsClient);
        } catch (RuntimeException e) {//handle gracefully for scripts
            LOGGER.error(constructMessage("Failed to close connection"));
        }
    }

    @Override
    public void doDirectUpload(String fileName, String folderPath, String globalProcessId) {

        setDirectUpload(true);
        boolean isHandOverExecutionToJavaScript = false;
        int scriptExecutionTimeout ;
        try {
            isHandOverExecutionToJavaScript = (getProperties()).isHandOverExecutionToJavaScript();
            scriptExecutionTimeout = ((FTPUploaderPropertiesDTO) getProperties()).getScriptExecutionTimeout();
        } catch (IllegalArgumentException | IllegalAccessException | IOException e) {
            throw new RuntimeException(e);
        }

        if (isHandOverExecutionToJavaScript) {

            setFileName(fileName);
            setFolderPath(folderPath);
            setMaxExecutionTimeout(scriptExecutionTimeout);
            JavaScriptExecutorUtil.executeJavaScript(configurationInstance.getJavaScriptUri(), this);
        } else {

            G2FTPSClient ftpsRequest = null;
            try {

                String remotePath = validateRemotePath();

                ftpsRequest = (G2FTPSClient) getClient();
                ftpsRequest.setLogPrefix(constructMessage());
                ftpsRequest.connect();
                ftpsRequest.login();

                // retrieve required properties
                FTPUploaderPropertiesDTO ftpUploaderStaticProperties = (FTPUploaderPropertiesDTO) getProperties();
                setPassive(ftpsRequest, ftpUploaderStaticProperties);

                changeDirectory(ftpsRequest, remotePath);
                LOGGER.info(constructMessage("Ready to upload file {} from local path {} to remote path {}"),
                        fileName,
                        folderPath,
                        remotePath);

                if (this.canUseFileSystem()) {
                    uploadFile(ftpsRequest, remotePath, new File(folderPath + File.separatorChar + fileName));
                } else {

                    StagedFileDAO dao = new StagedFileDAOBase();
                    StagedFile stagedFile = dao.findStagedFileByGpid(globalProcessId);

                    RelayFile file = new RelayFile();
                    file.copy(stagedFile);
                    uploadFile(ftpsRequest, remotePath, file);
                }

            } catch (Exception e) {
                LOGGER.error(constructMessage("Error occurred during direct upload", seperator, e.getMessage()), e);
                throw new RuntimeException(e);
            } finally {
                disconnect(ftpsRequest);
            }

        }
    }

    @Override
    protected int getScriptExecutionTimeout() throws IOException, IllegalAccessException {
        return ((FTPUploaderPropertiesDTO) getProperties()).getScriptExecutionTimeout();
    }

}