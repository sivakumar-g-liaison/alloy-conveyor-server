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

import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import com.liaison.commons.exception.LiaisonException;
import com.liaison.commons.jaxb.JAXBUtility;
import com.liaison.commons.util.client.sftp.G2SFTPClient;
import com.liaison.dto.queue.WorkTicket;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.dtdm.model.Processor;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.service.core.SweeperEventExecutionService;
import com.liaison.mailbox.service.core.processor.helper.ClientFactory;
import com.liaison.mailbox.service.dto.configuration.SweeperEventRequestDTO;
import com.liaison.mailbox.service.dto.configuration.TriggerProcessorRequestDTO;
import com.liaison.mailbox.service.dto.configuration.processor.properties.SFTPDownloaderPropertiesDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.executor.javascript.JavaScriptExecutorUtil;
import com.liaison.mailbox.service.queue.sender.SweeperQueueSendClient;
import com.liaison.mailbox.service.util.DirectoryCreationUtil;
import com.liaison.mailbox.service.util.MailBoxUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ws.rs.core.Response;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * SFTP remote downloader to perform pull operation, also it has support methods
 * for JavaScript.
 *
 * @author OFS
 */
public class SFTPRemoteDownloader extends AbstractProcessor implements MailBoxProcessorI {

    private static final Logger LOGGER = LogManager.getLogger(SFTPRemoteDownloader.class);

    /*
     * Required for JS
     */
    private G2SFTPClient sftpClient;
    private SFTPDownloaderPropertiesDTO staticProp;

    @SuppressWarnings("unused")
    private SFTPRemoteDownloader() {
    }

    public SFTPRemoteDownloader(Processor processor) {
        super(processor);
        try {
            staticProp = (SFTPDownloaderPropertiesDTO) getProperties();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * Java method to execute the SFTPrequest
     *
     */
    private void executeSFTPRequest() {

        G2SFTPClient sftpRequest = null;
        try {

            sftpRequest = (G2SFTPClient) getClient();
            sftpRequest.setLogPrefix(constructMessage());
            sftpRequest.connect();
            LOGGER.info(constructMessage("Start run"));
            long startTime = System.currentTimeMillis();
            if (sftpRequest.openChannel()) {

                String remotePath = getPayloadURI();
                if (MailBoxUtil.isEmpty(remotePath)) {
                    LOGGER.error(constructMessage("The given payload URI is Empty."));
                    throw new MailBoxServicesException("The given payload URI is Empty.", Response.Status.CONFLICT);
                }

                String localPath = getWriteResponseURI();
                if (MailBoxUtil.isEmpty(localPath)) {
                    LOGGER.error(constructMessage("The given remote URI is Empty."));
                    throw new MailBoxServicesException("The given remote URI is Empty.", Response.Status.CONFLICT);
                }

                LOGGER.info(constructMessage("Ready to download files from remote path {} to local path {}"), remotePath, localPath);
                downloadDirectory(sftpRequest, remotePath, localPath);
            }

            // to calculate the elapsed time for processing files
            long endTime = System.currentTimeMillis();
            LOGGER.info(constructMessage("Number of files processed {}"), totalNumberOfProcessedFiles);
            LOGGER.info(constructMessage("Total time taken to process files {}"), endTime - startTime);
            LOGGER.info(constructMessage("End run"));

        } catch (Exception e) {
            LOGGER.error(constructMessage("Error occurred during sftp download", seperator, e.getMessage()), e);
            throw new RuntimeException(e);
        } finally {
            if (sftpRequest != null) {
                sftpRequest.disconnect();
            }
        }
    }

    /**
     * Java method to download the folder and its files
     *
     * @throws IOException
     * @throws LiaisonException
     * @throws MailBoxServicesException
     * @throws SftpException
     * @throws com.liaison.commons.exception.LiaisonException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws SecurityException
     *
     */
    public void downloadDirectory(G2SFTPClient sftpRequest, String currentDir, String localFileDir) throws IOException,
            LiaisonException, MailBoxServicesException, SftpException, SecurityException, IllegalArgumentException {

        //variable to hold the status of file download request execution
        int statusCode = 0;
        String dirToList = "";
        String globalProcessorId;

        if (!currentDir.equals("")) {
            dirToList += currentDir;
        }

        List<String> files = sftpRequest.listFiles(currentDir);
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;
        if(files.size() == 2) {

            for (int i = 1; i >=0 ; i--) {
                if (files.get(i).equals(".") || files.get(i).equals("..")) {
                files.remove(i);
                }
            }
        }
        if (files != null && files.size() > 0) {

            String statusIndicator = staticProp.getFileTransferStatusIndicator();

            for (String aFile : files) {

                if (MailBoxUtil.isInterrupted(Thread.currentThread().getName())) {
                    LOGGER.warn("The executor is gracefully interrupted");
                    return;
                }

                if (aFile.equals(".") || aFile.equals("..")) {
                    // skip parent directory and the directory itself
                    continue;
                }

                boolean isDir = sftpRequest.getNative().stat(dirToList + File.separatorChar + aFile).isDir();
                if (isDir) {

                    if (!staticProp.isIncludeSubDirectories()) {
                        continue;
                    }
                    String localDir = localFileDir + File.separatorChar + aFile;
                    String remotePath = dirToList + File.separatorChar + aFile;
                    File directory = new File(localDir);
                    if (!directory.exists()) {
                        Files.createDirectories(directory.toPath());
                    }
                    sftpRequest.changeDirectory(remotePath);
                    downloadDirectory(sftpRequest, remotePath, localDir);

                } else {

                    // Check if the file to be uploaded is included or not excluded
                    if(!checkFileIncludeOrExclude(staticProp.getIncludedFiles(),
                            aFile,
                            staticProp.getExcludedFiles())) {
                        continue;
                    }

                    String downloadingFileName = (!MailBoxUtil.isEmpty(statusIndicator)) ? aFile + "."
                            + statusIndicator : aFile;
                    String localDir = localFileDir + File.separatorChar + downloadingFileName;
                    sftpRequest.changeDirectory(dirToList);

                    //download and sweep the file use stream when filesystem set as false and direct submit is true
                    if (!staticProp.isUseFileSystem() && staticProp.isDirectSubmit()) {
                        LOGGER.info("Sweep and Post the file to Service Broker using stream without placing the file in NFS");
                        globalProcessorId = sweepFile(sftpRequest, downloadingFileName);
                        LOGGER.info("File posted to service broker and the Global Process Id {}", globalProcessorId);
                        totalNumberOfProcessedFiles++;
                        continue;
                    }

                    createResponseDirectory(localDir);
                    try {// GSB-1337,GSB-1336

                        fos = new FileOutputStream(localDir);
                        bos = new BufferedOutputStream(fos);
                        LOGGER.info(constructMessage("downloading file {} from remote path {} to local path {}"),
                                aFile, currentDir, localFileDir);
                        statusCode = sftpRequest.getFile(aFile, bos);
                        // Check whether the file downloaded successfully if so rename it.
                        if (statusCode == MailBoxConstants.SFTP_FILE_TRANSFER_ACTION_OK) {

                            totalNumberOfProcessedFiles++;
                            LOGGER.info(constructMessage("File {} downloaded successfully"), aFile);
                            if (fos != null) fos.close();
                            if (bos != null) bos.close();

                            // Renames the downloaded file to original extension once the fileStatusIndicator is given by User
                            if (!MailBoxUtil.isEmpty(statusIndicator)) {
                                File downloadedFile = new File(localDir);
                                File currentFile = new File(localFileDir + File.separatorChar + aFile);
                                boolean renameStatus = downloadedFile.renameTo(currentFile);
                                if (renameStatus) {
                                    LOGGER.info(constructMessage("File {} renamed successfully"), aFile);
                                } else {
                                    LOGGER.info(constructMessage("File {} renaming failed"), aFile);
                                }
                            }
                            // Delete the remote files after successful download if user optioned for it
                            if (staticProp.getDeleteFiles()) {
                                sftpRequest.deleteFile(aFile);
                                LOGGER.info("File {} deleted successfully in the remote location", aFile);
                            }
                            // async sweeper process if direct submit is true.
                            // sweep the file using event queue when direct submit is true & filesystem is true
                            if (staticProp.isDirectSubmit()) {
                                // sweep single file process to SB queue
                                globalProcessorId = sweepFile(new File(localFileDir + File.separatorChar + aFile));
                                LOGGER.info("File posted to sweeper event queue and the Global Process Id {}", globalProcessorId);
                            }
                        }
                    } finally {
                        if (bos != null) bos.close();
                        if (fos != null) fos.close();
                    }

                }
            }
        }
        else {
            LOGGER.info(constructMessage("The given payload URI '" + currentDir + "' is empty."));
        }
    }

    @Override
    public void runProcessor(Object dto) {

        LOGGER.debug("Entering in invoke.");
        try {

            setReqDTO((TriggerProcessorRequestDTO) dto);
            // G2SFTP executed through JavaScript
            if (getProperties().isHandOverExecutionToJavaScript()) {
                // Use custom G2JavascriptEngine
                setMaxExecutionTimeout(((SFTPDownloaderPropertiesDTO) getProperties()).getScriptExecutionTimeout());
                JavaScriptExecutorUtil.executeJavaScript(configurationInstance.getJavaScriptUri(), this);

            } else {
                // G2SFTP executed through Java
                executeSFTPRequest();
            }

        } catch (IOException | IllegalAccessException e) {
            throw new RuntimeException(e);
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

    /**
     * This Method create local folders if not available and returns the path.
     *
     * * @param processorDTO it have details of processor
     */
    @Override
    public String createLocalPath() {

        String configuredPath = null;
        try {
            configuredPath = getWriteResponseURI();
            DirectoryCreationUtil.createPathIfNotAvailable(configuredPath);
            return configuredPath;

        } catch (IOException e) {
            throw new MailBoxConfigurationServicesException(Messages.LOCAL_FOLDERS_CREATION_FAILED,
                    configuredPath, Response.Status.BAD_REQUEST);
        }

    }

    @Override
    public String sweepFile(G2SFTPClient sftpClient, String fileName) {

        SweeperEventExecutionService service = new SweeperEventExecutionService();
        LOGGER.info("Sweep and Post the file to Service Broker");
        try {

            SftpATTRS fileAttribute = sftpClient.getNative().stat(fileName);
            SweeperEventRequestDTO sweeperEventRequestDTO = getSweeperEventRequestDTO(fileName,
                    sftpClient.getNative().pwd(),
                    fileAttribute.getSize(),
                    TimeUnit.SECONDS.toMillis(fileAttribute.getMTime()),
                    staticProp.isLensVisibility(),
                    staticProp.getPipeLineID(),
                    staticProp.isSecuredPayload(),
                    staticProp.getContentType());

            WorkTicket workTicket = service.getWorkTicket(sweeperEventRequestDTO);
            int statusCode = service.persistPayloadAndWorkticket(workTicket, sweeperEventRequestDTO, sftpClient,null, fileName);
            if (statusCode != MailBoxConstants.SFTP_FILE_TRANSFER_ACTION_OK) {
                throw new RuntimeException("File download status is not successful - " + statusCode);
            }
            SweeperQueueSendClient.post(JAXBUtility.marshalToJSON(workTicket), false);
            // Delete the remote files after successful download if user optioned for it
            if (staticProp.getDeleteFiles()) {
                sftpClient.deleteFile(sweeperEventRequestDTO.getFileName());
                LOGGER.info("File {} deleted successfully in the remote location", sweeperEventRequestDTO.getFileName());
            }
            service.logToLens(workTicket, sweeperEventRequestDTO);
            LOGGER.info("Global PID : {} submitted for file {}", workTicket.getGlobalProcessId(), workTicket.getFileName());

            return sweeperEventRequestDTO.getGlobalProcessId();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }


}