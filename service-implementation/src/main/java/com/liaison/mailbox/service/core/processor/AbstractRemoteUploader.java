/**
 * Copyright 2016 Liaison Technologies, Inc.
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.mailbox.service.core.processor;

import com.jcraft.jsch.SftpException;
import com.liaison.commons.exception.LiaisonException;
import com.liaison.commons.util.client.ftps.G2FTPSClient;
import com.liaison.commons.util.client.sftp.G2SFTPClient;
import com.liaison.mailbox.dtdm.model.Processor;
import com.liaison.mailbox.enums.ExecutionEvents;
import com.liaison.mailbox.enums.ExecutionState;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.rtdm.dao.StagedFileDAO;
import com.liaison.mailbox.rtdm.dao.StagedFileDAOBase;
import com.liaison.mailbox.rtdm.model.StagedFile;
import com.liaison.mailbox.service.core.fsm.MailboxFSM;
import com.liaison.mailbox.service.dto.GlassMessageDTO;
import com.liaison.mailbox.service.dto.configuration.TriggerProcessorRequestDTO;
import com.liaison.mailbox.service.dto.configuration.processor.properties.FTPUploaderPropertiesDTO;
import com.liaison.mailbox.service.dto.configuration.processor.properties.SFTPUploaderPropertiesDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.executor.javascript.JavaScriptExecutorUtil;
import com.liaison.mailbox.service.glass.util.MailboxGlassMessageUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ws.rs.core.Response;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by VNagarajan on 6/3/2016.
 */
public abstract class AbstractRemoteUploader extends AbstractProcessor implements RemoteUploaderI {

    private static final Logger LOGGER = LogManager.getLogger(AbstractRemoteUploader.class);
    
    private String fileName;
    private String folderPath;
    private boolean directUpload;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFolderPath() {
        return folderPath;
    }

    public void setFolderPath(String folderPath) {
        this.folderPath = folderPath;
    }

    public boolean isDirectUpload() {
        return directUpload;
    }

    public void setDirectUpload(boolean directUpload) {
        this.directUpload = directUpload;
    }

    public AbstractRemoteUploader() {}

    public AbstractRemoteUploader(Processor processor) {
        super(processor);
    }

    @Override
    public void runProcessor(Object dto, MailboxFSM fsm) {

        try {

            setReqDTO((TriggerProcessorRequestDTO) dto);
            // FTPSRequest executed through JavaScript
            if (getProperties().isHandOverExecutionToJavaScript()) {
                fsm.handleEvent(fsm.createEvent(ExecutionEvents.PROCESSOR_EXECUTION_HANDED_OVER_TO_JS));
                JavaScriptExecutorUtil.executeJavaScript(configurationInstance.getJavaScriptUri(), this);
            } else {
                // FTPSRequest executed through Java
                executeRequest(getReqDTO().getExecutionId(), fsm);
            }

        } catch(IOException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * change directory and create if it doesn't exist
     *
     * @param client sftp/ftp client
     * @param remotePath remote path
     * @throws IOException
     * @throws IllegalAccessException
     * @throws LiaisonException
     */
    protected void changeDirectory(Object client, String remotePath) throws IOException, IllegalAccessException, LiaisonException {


        if (client instanceof  G2SFTPClient) {

            boolean isCreateFoldersInRemote = ((SFTPUploaderPropertiesDTO) getProperties()).isCreateFoldersInRemote();
            G2SFTPClient sftpClient = (G2SFTPClient) client;
            // check the given location exists in the remote server
            try {
                sftpClient.getNative().lstat(remotePath);
            } catch (SftpException ex) {
                checkAndCreateDirectory(remotePath, isCreateFoldersInRemote, sftpClient);
            }

            sftpClient.changeDirectory(remotePath);
        } else if (client instanceof G2FTPSClient) {

            boolean isCreateFoldersInRemote = ((FTPUploaderPropertiesDTO) getProperties()).isCreateFoldersInRemote();
            G2FTPSClient ftpsClient = (G2FTPSClient) client;

            boolean dirExists = ftpsClient.getNative().changeWorkingDirectory(remotePath);
            if (!dirExists) {
                // create directory on the server
                checkAndCreateDirectory(remotePath, isCreateFoldersInRemote, ftpsClient);
            }

            ftpsClient.changeDirectory(remotePath);
        }
    }

    private void checkAndCreateDirectory(String remotePath, boolean isCreateFoldersInRemote, Object client) {

        if (!isCreateFoldersInRemote) {
            LOGGER.error(constructMessage("Unable to create directory {} because create folders in remote is not enabled."), remotePath);
            throw new MailBoxServicesException("The remote directory " + remotePath + " does not exist.", Response.Status.CONFLICT);
        } else {
            createDirectoriesInRemote(client, remotePath);
        }
    }

    /**
     * Create directories in the remote server if it is not available
     *
     * @param client sftp/ftps client
     * @param remotePath remote path
     */
    protected void createDirectoriesInRemote(Object client, String remotePath) {

        try {

            for (String directory : remotePath.split(File.separatorChar == '\\' ? "\\\\" : File.separator)) {

                if (directory.isEmpty()) {// For when path starts with /
                    continue;
                }

                if (client instanceof G2SFTPClient) {

                    G2SFTPClient sftpClient = (G2SFTPClient) client;
                    // Info logs are required to track the folders creation and it won't log frequently
                    try {
                        sftpClient.getNative().lstat(directory);
                        LOGGER.debug(constructMessage("The remote directory {} already exists."), directory);
                    } catch (SftpException ex) {
                        LOGGER.info(constructMessage("The remote directory {} doesn't exist."), directory);
                        sftpClient.getNative().mkdir(directory);
                        LOGGER.info(constructMessage("Created remote directory {}"), directory);
                    }

                    sftpClient.getNative().cd(directory);

                } else if (client instanceof G2FTPSClient) {

                    G2FTPSClient ftpClient = (G2FTPSClient) client;
                    // Info logs are required to track the folders creation and it won't log frequently
                    boolean isExist = ftpClient.getNative().changeWorkingDirectory(directory);
                    if (!isExist) {
                        boolean isCreated = ftpClient.getNative().makeDirectory(directory);
                        if (isCreated) {
                            LOGGER.info(constructMessage("The remote directory {} doesn't exist."), directory);
                            ftpClient.getNative().changeWorkingDirectory(directory);
                            LOGGER.info(constructMessage("Created remote directory {}"), directory);
                        } else {
                            throw new MailBoxServicesException("Unable to create directory.", Response.Status.INTERNAL_SERVER_ERROR);
                        }
                    }
                    ftpClient.changeDirectory(directory);
                }

            }
        } catch (SftpException | IOException | LiaisonException e) {
            LOGGER.error(e);
            throw new MailBoxServicesException("Unable to create directory.", Response.Status.INTERNAL_SERVER_ERROR);
        }

    }

    @Override
    public void logToLens(String msg, File file, ExecutionState status) {

        StagedFileDAO stagedFileDAO = new StagedFileDAOBase();
        StagedFile stagedFile = stagedFileMap.get(file.getAbsolutePath());
        if (null == stagedFile) {

            stagedFile = stagedFileDAO.findStagedFilesByProcessorId(configurationInstance.getPguid(),
                    file.getParent(),
                    file.getName());
            if (null == stagedFile) {
                return;
            }
        }
        if (updateStagedFileStatus(status, stagedFileDAO, stagedFile)) {
            return;
        }

        GlassMessageDTO glassMessageDTO = new GlassMessageDTO();
        glassMessageDTO.setGlobalProcessId(stagedFile.getGPID());
        glassMessageDTO.setProcessorType(configurationInstance.getProcessorType());
        glassMessageDTO.setProcessProtocol(configurationInstance.getProcsrProtocol());
        glassMessageDTO.setFileName(file.getName());
        glassMessageDTO.setFilePath(file.getPath());
        glassMessageDTO.setFileLength(file.length());
        glassMessageDTO.setStatus(status);
        glassMessageDTO.setMessage(msg);
        glassMessageDTO.setPipelineId(null);
        glassMessageDTO.setFirstCornerTimeStamp(null);
        
        MailboxGlassMessageUtil.logGlassMessage(glassMessageDTO);
    }

    /**
     * set passive properties for ftps uploader client
     * @param ftpsRequest ftps client
     * @param ftpUploaderStaticProperties uploader properties
     * @throws LiaisonException
     */
    protected void setPassive(G2FTPSClient ftpsRequest, FTPUploaderPropertiesDTO ftpUploaderStaticProperties)
            throws LiaisonException {

        if (ftpUploaderStaticProperties != null) {

            ftpsRequest.setBinary(ftpUploaderStaticProperties.isBinary());
            ftpsRequest.setPassive(ftpUploaderStaticProperties.isPassive());

        }
    }

    /**
     * Creates local folders if not available.
     */
    @Override
    public void createLocalPath() {

        String configuredPath = null;
        try {
            configuredPath = getPayloadURI();
            createPathIfNotAvailable(configuredPath);

        } catch (IOException e) {
            throw new MailBoxConfigurationServicesException(Messages.LOCAL_FOLDERS_CREATION_FAILED, configuredPath,
                    Response.Status.BAD_REQUEST,e.getMessage());
        }

    }

    /**
     * executes core business logic
     *
     * @param executionId
     * @param fsm
     */
    protected abstract void executeRequest(String executionId, MailboxFSM fsm);

    /**
     * get files from staged file db
     *
     * @param recurseSubDirs boolean to check the recursive sub directories
     * @return list of files to be uploaded
     */
    @Override
    public File[] getFilesToUpload(boolean recurseSubDirs) {

        StagedFileDAO dao = new StagedFileDAOBase();
        List<File> files = new ArrayList<>();
        List<StagedFile> stagedFiles;

        //for javascript direct upload
        if (getFileName() != null
                && getFolderPath() != null
                && isDirectUpload()) {

            File[] fileArray = new File[1];
            fileArray[0] = new File(folderPath + File.separatorChar + fileName);
            return fileArray;
        }

        //default profile invocation
        stagedFiles = dao.findStagedFilesForUploader(
                this.configurationInstance.getPguid(),
                new File(validateLocalPath()).getPath(),
                isDirectUpload(),
                recurseSubDirs);

        for (StagedFile stagedFile : stagedFiles) {

            File file = Paths.get(stagedFile.getFilePath() + File.separator + stagedFile.getFileName()).toFile();
            if (file.exists()) {
                files.add(file);
                stagedFileMap.put(file.getAbsolutePath(), stagedFile);
            } else {
                LOGGER.warn("The file {} is not available in the local payload location", file.getName());
            }
        }

        return files.toArray(new File[files.size()]);
    }

}