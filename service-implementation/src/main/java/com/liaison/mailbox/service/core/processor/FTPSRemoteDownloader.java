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
import com.liaison.commons.jaxb.JAXBUtility;
import com.liaison.commons.util.client.ftps.G2FTPSClient;
import com.liaison.dto.queue.WorkTicket;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.dtdm.model.Processor;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.service.core.SweeperEventExecutionService;
import com.liaison.mailbox.service.core.processor.helper.FTPSClient;
import com.liaison.mailbox.service.dto.configuration.SweeperEventRequestDTO;
import com.liaison.mailbox.service.dto.configuration.TriggerProcessorRequestDTO;
import com.liaison.mailbox.service.dto.configuration.processor.properties.FTPDownloaderPropertiesDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.executor.javascript.JavaScriptExecutorUtil;
import com.liaison.mailbox.service.queue.sender.SweeperQueueSendClient;
import com.liaison.mailbox.service.util.DirectoryCreationUtil;
import com.liaison.mailbox.service.util.MailBoxUtil;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ws.rs.core.Response;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

/**
 * FTPS remote downloader to perform pull operation, also it has support methods
 * for javascript.
 *
 * @author OFS
 */
public class FTPSRemoteDownloader extends AbstractProcessor implements MailBoxProcessorI {

    private static final Logger LOGGER = LogManager.getLogger(FTPSRemoteDownloader.class);

    /*
     * Required for JS
     */
    private G2FTPSClient ftpsClient;
    private FTPDownloaderPropertiesDTO staticProp;

    @SuppressWarnings("unused")
    private FTPSRemoteDownloader() {
    }

    public FTPSRemoteDownloader(Processor processor) {
        super(processor);
        try {
            staticProp = (FTPDownloaderPropertiesDTO) getProperties();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public void runProcessor(Object dto) {

        LOGGER.debug("Entering in invoke.");
        try {

            setReqDTO((TriggerProcessorRequestDTO) dto);
            // FTPSRequest executed through JavaScript
            if (getProperties().isHandOverExecutionToJavaScript()) {

                setMaxExecutionTimeout(((FTPDownloaderPropertiesDTO) getProperties()).getScriptExecutionTimeout());
                JavaScriptExecutorUtil.executeJavaScript(configurationInstance.getJavaScriptUri(), this);
            } else {
                // FTPSRequest executed through Java
                run();
            }
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Java method to execute the SFTPrequest to download the file or folder
     */
    protected void run() {

        G2FTPSClient ftpsRequest = null;
        try {

            ftpsRequest = (G2FTPSClient) getClient();
            ftpsRequest.setLogPrefix(constructMessage());
            ftpsRequest.enableSessionReuse(true);
            ftpsRequest.connect();
            ftpsRequest.login();
            long startTime = 0;

            // retrieve required properties
            FTPDownloaderPropertiesDTO ftpDownloaderStaticProperties = (FTPDownloaderPropertiesDTO)getProperties();
            ftpsRequest.setBinary(ftpDownloaderStaticProperties.isBinary());
            ftpsRequest.setPassive(ftpDownloaderStaticProperties.isPassive());

            LOGGER.info(constructMessage("Start run"));
            startTime = System.currentTimeMillis();

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
            ftpsRequest.changeDirectory(remotePath);

            downloadDirectory(ftpsRequest, remotePath, localPath);

            // to calculate the elapsed time for processing files
            long endTime = System.currentTimeMillis();
            LOGGER.info(constructMessage("Number of files processed {}"), totalNumberOfProcessedFiles);
            LOGGER.info(constructMessage("Total time taken to process files {}"), endTime - startTime);
            LOGGER.info(constructMessage("End run"));

        } catch (LiaisonException | IOException | MailBoxServicesException
                | IllegalAccessException e) {

            LOGGER.error(constructMessage("Error occurred during ftp(s) download", seperator, e.getMessage()), e);
            throw new RuntimeException(e);
        }
        finally {
            disconnect(ftpsRequest);
        }
    }

    /**
     * Java method to download the file or folder
     *
     * @throws IOException
     * @throws LiaisonException
     * @throws com.liaison.commons.exception.LiaisonException
     * @throws IllegalArgumentException
     * @throws SecurityException
     *
     */
    public void downloadDirectory(G2FTPSClient ftpClient, String currentDir, String localFileDir) throws IOException,
            LiaisonException, MailBoxServicesException,
            SecurityException, IllegalArgumentException {

        //variable to hold the status of file download request execution
        int statusCode = 0;
        String dirToList = "";
        String globalProcessorId;

        if (!currentDir.equals("")) {
            dirToList += currentDir;
        }

        FTPFile[] files = ftpClient.getNative().listFiles(dirToList);
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;
        if (files != null && files.length > 0) {

            String statusIndicator = staticProp.getFileTransferStatusIndicator();
            for (FTPFile file : files) {

                if (MailBoxUtil.isInterrupted(Thread.currentThread().getName())) {
                    LOGGER.warn("The executor is gracefully interrupted");
                    return;
                }

                if (file.getName().equals(".") || file.getName().equals("..")) {
                    // skip parent directory and the directory itself
                    continue;
                }
                String currentFileName = file.getName();
                if (file.isFile()) {

                    // Check if the file to be downloaded is included or not excluded
                    if(!checkFileIncludeOrExclude(staticProp.getIncludedFiles(),
                            currentFileName,
                            staticProp.getExcludedFiles())) {
                        continue;
                    }

                    String downloadingFileName = (!MailBoxUtil.isEmpty(statusIndicator)) ? currentFileName + "."
                            + statusIndicator : currentFileName;
                    String localDir = localFileDir + File.separatorChar + downloadingFileName;
                    ftpClient.changeDirectory(dirToList);

                    //download and sweep the file use stream when filesystem set as false and direct submit is true
                    if (!staticProp.isUseFileSystem() && staticProp.isDirectSubmit()) {
                        LOGGER.info("Sweep and Post the file to Service Broker using stream without placing the file in NFS");
                        globalProcessorId = sweepFile(ftpClient, downloadingFileName);
                        LOGGER.info("File posted to service broker and the Global Process Id {}", globalProcessorId);
                        totalNumberOfProcessedFiles++;
                        continue;
                    }
                    createResponseDirectory(localDir);

                    try {// GSB-1337,GSB-1336

                        fos = new FileOutputStream(localDir);
                        bos = new BufferedOutputStream(fos);
                        LOGGER.info(constructMessage("downloading file {}  from remote path {} to local path {}"),
                                currentFileName, currentDir, localFileDir);
                        statusCode = ftpClient.getFile(currentFileName, bos);
                    } finally {
                        if (bos != null) {
                            bos.close();
                        }
                        if (fos != null) {
                            fos.close();
                        }
                    }

                    // Check whether the file downloaded successfully if so rename it.
                    if (statusCode == MailBoxConstants.CLOSING_DATA_CONNECTION
                            || statusCode == MailBoxConstants.FTP_FILE_TRANSFER_ACTION_OK) {

                        LOGGER.info(constructMessage("File {} downloaded successfully"), currentFileName);
                        totalNumberOfProcessedFiles++;

                        // Renames the downloaded file to original extension once the fileStatusIndicator is  given by User
                        if (!MailBoxUtil.isEmpty(statusIndicator)) {

                            //Constructs the original file filename
                            File actualFileName = new File(localFileDir + File.separatorChar + currentFileName);
                            boolean renameStatus =  new File(localDir).renameTo(actualFileName);
                            if (renameStatus) {
                                LOGGER.info(constructMessage("File {} renamed successfully"), currentFileName);
                            } else {
                                LOGGER.info(constructMessage("File {} renaming failed"), currentFileName);
                            }
                        }

                        // Delete the remote files after successful download if user opt for it
                        if (staticProp.getDeleteFiles()) {
                            ftpClient.deleteFile(file.getName());
                            LOGGER.info(constructMessage("File {} deleted successfully in the remote location"), currentFileName);
                        }
                        if (staticProp.isDirectSubmit()) {
                            // async sweeper process if direct submit is true.
                            // sweep the file using event queue when direct submit is true & filesystem is true
                            globalProcessorId = sweepFile(new File(localFileDir + File.separatorChar + currentFileName));
                            LOGGER.info("File posted to sweeper event queue and the Global Process Id {}",globalProcessorId);
                        }
                    }

                } else {

                    if (!staticProp.isIncludeSubDirectories()) {
                        continue;
                    }
                    String localDir = localFileDir + File.separatorChar + currentFileName;
                    String remotePath = dirToList + File.separatorChar + currentFileName;
                    File directory = new File(localDir);
                    if (!directory.exists()) {
                        Files.createDirectories(directory.toPath());
                    }
                    ftpClient.changeDirectory(remotePath);
                    downloadDirectory(ftpClient, remotePath, localDir);
                }
            }
        } else {
            LOGGER.info(constructMessage("The given payload URI '" + currentDir + "' is empty."));
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
                    configuredPath, Response.Status.BAD_REQUEST,e.getMessage());
        }

    }

    @Override
    public String sweepFile(G2FTPSClient ftpsClient, String fileName) {

        LOGGER.info("Sweep and Post the file to Service Broker");
        SweeperEventExecutionService service = new SweeperEventExecutionService();

        try {

            FTPFile ftpFile = getFTPFile(ftpsClient, fileName);
            long fileModifiedTime = (ftpFile != null && ftpFile.getTimestamp() != null) ? ftpFile.getTimestamp().getTimeInMillis() : 0;
            long fileSize = (ftpFile != null) ? ftpFile.getSize() : -1;
            SweeperEventRequestDTO sweeperEventRequestDTO = getSweeperEventRequestDTO(fileName,
                    ftpsClient.getNative().printWorkingDirectory(),
                    fileSize,
                    fileModifiedTime,
                    staticProp.isLensVisibility(),
                    staticProp.getPipeLineID(),
                    staticProp.isSecuredPayload(),
                    staticProp.getContentType());

            WorkTicket workTicket = service.getWorkTicket(sweeperEventRequestDTO);
            int statusCode = service.persistPayloadAndWorkticket(workTicket, sweeperEventRequestDTO, null,ftpsClient, fileName);
            if (statusCode != MailBoxConstants.FTP_FILE_TRANSFER_ACTION_OK
                    && statusCode != MailBoxConstants.CLOSING_DATA_CONNECTION) {
                throw new RuntimeException("File download status is not successful - " + statusCode);
            }

            String workTicketToSb = JAXBUtility.marshalToJSON(workTicket);
            SweeperQueueSendClient.post(workTicketToSb, false);
            // Delete the remote files after successful download if user optioned for it
            if (staticProp.getDeleteFiles()) {
                ftpsClient.deleteFile(fileName);
                LOGGER.info("File {} deleted successfully in the remote location", fileName);
            }

            service.logToLens(workTicket, sweeperEventRequestDTO);
            LOGGER.info("Global PID : {} submitted for file {}", workTicket.getGlobalProcessId(), workTicket.getFileName());
            return sweeperEventRequestDTO.getGlobalProcessId();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }

    }

    /**
     *  Hack to get file size and modification time in the single ftp command
     * @param client
     * @param fileName
     * @return
     */
    private FTPFile getFTPFile(G2FTPSClient client, String fileName) {

        try {

            FTPClient ftpClient = client.getNative();
            //Assumption there would be only one file
            FTPFile[] files = ftpClient.listFiles(fileName);
            return files[0];
        } catch (IOException e) {
            LOGGER.error("Unable to get modified time", e);
            return null;
        }
    }
}