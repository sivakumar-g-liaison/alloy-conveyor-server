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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;

import org.apache.commons.net.ftp.FTPFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jcraft.jsch.SftpException;
import com.liaison.commons.exception.LiaisonException;
import com.liaison.commons.jaxb.JAXBUtility;
import com.liaison.commons.util.client.ftps.G2FTPSClient;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.dtdm.model.Processor;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.service.core.processor.helper.FTPSClient;
import com.liaison.mailbox.service.dto.configuration.SweeperEventRequestDTO;
import com.liaison.mailbox.service.dto.configuration.TriggerProcessorRequestDTO;
import com.liaison.mailbox.service.dto.configuration.processor.properties.FTPDownloaderPropertiesDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.executor.javascript.JavaScriptExecutorUtil;
import com.liaison.mailbox.service.queue.sender.SweeperEventSendQueue;
import com.liaison.mailbox.service.util.DirectoryCreationUtil;
import com.liaison.mailbox.service.util.MailBoxUtil;

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

        } catch (LiaisonException | JAXBException | IOException | MailBoxServicesException
                | URISyntaxException |IllegalAccessException | NoSuchFieldException e) {

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
     * @throws JAXBException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws SecurityException
     * @throws NoSuchFieldException
     * @throws SftpException
     *
     */
    public void downloadDirectory(G2FTPSClient ftpClient, String currentDir, String localFileDir) throws IOException,
            LiaisonException, URISyntaxException, MailBoxServicesException, NoSuchFieldException,
            SecurityException, IllegalArgumentException, IllegalAccessException, JAXBException {

        //variable to hold the status of file download request execution
        int statusCode = 0;
        String dirToList = "";

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

                        if (staticProp.isDirectSubmit()) {
                            // sweep single file process to SB queue
                            String globalProcessorId = sweepFile(new File(localFileDir + File.separatorChar + currentFileName));
                            LOGGER.info("File posted to sweeper event queue and the Global Process Id {}",globalProcessorId);
                        }
                        // Delete the remote files after successful download if user opt for it
                        if (staticProp.getDeleteFiles()) {
                            ftpClient.deleteFile(file.getName());
                            LOGGER.info(constructMessage("File {} deleted successfully in the remote location"), currentFileName);
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

    /**
     * Method to use single file post into Sweeper event queue for store file into BOSS
     * 
     * @param file downloaded File
     * @return Globalprocessid.
     */
    @Override
    public String sweepFile(File file) {

        SweeperEventRequestDTO sweeperEventRequestDTO = new SweeperEventRequestDTO(file);
        sweeperEventRequestDTO.setLensVisibility(staticProp.isLensVisibility());
        sweeperEventRequestDTO.setPipeLineID(staticProp.getPipeLineID());
        sweeperEventRequestDTO.setSecuredPayload(staticProp.isSecuredPayload());
        sweeperEventRequestDTO.setContentType(staticProp.getContentType());
        sweeperEventRequestDTO.setMailBoxId(configurationInstance.getMailbox().getPguid());
        sweeperEventRequestDTO.setTtlMap(configurationInstance.getTTLUnitAndTTLNumber());
        sweeperEventRequestDTO.setGlobalProcessId(MailBoxUtil.getGUID());
        sweeperEventRequestDTO.setStorageType(MailBoxUtil.getStorageType(configurationInstance.getDynamicProperties()));

        try {
            String message = JAXBUtility.marshalToJSON(sweeperEventRequestDTO);
            SweeperEventSendQueue.post(message);
        } catch (Throwable e) {
            String msg = "Failed to sweeper the file "+ file.getName() + " and the error message is " + e.getMessage();
            LOGGER.error(msg, e);
            throw new RuntimeException(msg);
        }

        return sweeperEventRequestDTO.getGlobalProcessId();
    }

    /**
     * Method to use list of downloaded files post into sweeper event queue
     * 
     * @param files  downloaded files
     * @return globalProcessorIds    
     */
    @Override
    public String[] sweepFiles(File[] files) {

        List<String> globalProcessorIds = new ArrayList<String>();
        for (File file:files) {
            // sweep each file and add global processorIds.
            globalProcessorIds.add(sweepFile(file));
        }
        return globalProcessorIds.toArray(new String[globalProcessorIds.size()]);
    }
}