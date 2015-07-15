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
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.List;

import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;

import org.apache.commons.net.ftp.FTPFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.operator.OperatorCreationException;
import org.codehaus.jettison.json.JSONException;

import com.google.gson.JsonParseException;
import com.jcraft.jsch.SftpException;
import com.liaison.commons.exception.BootstrapingFailedException;
import com.liaison.commons.exception.LiaisonException;
import com.liaison.commons.security.pkcs7.SymmetricAlgorithmException;
import com.liaison.commons.util.client.ftps.G2FTPSClient;
import com.liaison.fs2.api.exceptions.FS2Exception;
import com.liaison.mailbox.dtdm.model.Processor;
import com.liaison.mailbox.enums.ExecutionEvents;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.service.core.fsm.MailboxFSM;
import com.liaison.mailbox.service.core.processor.helper.FTPSClient;
import com.liaison.mailbox.service.dto.configuration.TriggerProcessorRequestDTO;
import com.liaison.mailbox.service.dto.configuration.processor.properties.FTPDownloaderPropertiesDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.executor.javascript.JavaScriptExecutorUtil;
import com.liaison.mailbox.service.util.MailBoxUtil;

/**
 * @author OFS
 *
 */
public class FTPSRemoteDownloader extends AbstractProcessor implements MailBoxProcessorI {

	private static final Logger LOGGER = LogManager.getLogger(FTPSRemoteDownloader.class);

	@SuppressWarnings("unused")
	private FTPSRemoteDownloader() {
	}

	public FTPSRemoteDownloader(Processor processor) {
		super(processor);
	}

	@Override
	public void runProcessor(Object dto, MailboxFSM fsm) {

		LOGGER.debug("Entering in invoke.");
	    try {

	    	setReqDTO((TriggerProcessorRequestDTO) dto);
	    	// FTPSRequest executed through JavaScript
			if (getProperties().isHandOverExecutionToJavaScript()) {
				fsm.handleEvent(fsm.createEvent(ExecutionEvents.PROCESSOR_EXECUTION_HANDED_OVER_TO_JS));
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
	 *
	 * @throws IOException
	 * @throws LiaisonException
	 * @throws JAXBException
	 * @throws SftpException
	 * @throws URISyntaxException
	 * @throws FS2Exception
	 * @throws MailBoxServicesException
	 * @throws SymmetricAlgorithmException
	 * @throws com.liaison.commons.exception.LiaisonException
	 * @throws JsonParseException
	 * @throws JSONException
	 * @throws KeyStoreException
	 * @throws CertificateException
	 * @throws NoSuchAlgorithmException
	 * @throws CMSException
	 * @throws OperatorCreationException
	 * @throws UnrecoverableKeyException
	 * @throws BootstrapingFailedException
	 *
	 */
	protected void run() {

		try {

			G2FTPSClient ftpsRequest = (G2FTPSClient) getClient();

			ftpsRequest.enableSessionReuse(true);
			ftpsRequest.connect();
			ftpsRequest.login();
			long startTime = 0;
			//GMB-345
			//ftpsRequest.enableDataChannelEncryption();

			// retrieve required properties
			FTPDownloaderPropertiesDTO ftpDownloaderStaticProperties = (FTPDownloaderPropertiesDTO)getProperties();
			boolean binary = ftpDownloaderStaticProperties.isBinary();
			boolean passive = ftpDownloaderStaticProperties.isPassive();

			if (ftpDownloaderStaticProperties != null) {
				ftpsRequest.setBinary(binary);
				ftpsRequest.setPassive(passive);
			}

			LOGGER.info(constructMessage("Start run"));
			startTime = System.currentTimeMillis();

			String path = getPayloadURI();
			if (MailBoxUtil.isEmpty(path)) {
				LOGGER.info(constructMessage("The given payload URI is Empty."));
				throw new MailBoxServicesException("The given payload URI is Empty.", Response.Status.CONFLICT);
			}

			String remotePath = getWriteResponseURI();
			if (MailBoxUtil.isEmpty(remotePath)) {
				LOGGER.info(constructMessage("The given remote URI is Empty."));
				throw new MailBoxServicesException("The given remote URI is Empty.", Response.Status.CONFLICT);
			}

			ftpsRequest.changeDirectory(path);

			// For testing purpose
			LOGGER.debug("The payload location is {}", path);
			LOGGER.debug("The current working directory is {}", ftpsRequest.currentWorkingDirectory());
			List<String> files = ftpsRequest.listFiles();
			for (String file : files) {
				LOGGER.debug("The payload is {}", file);
			}
			if (files.isEmpty()) {
				LOGGER.debug("The payload location({}) is empty", path);
			}
			// For testing purpose
			LOGGER.info(constructMessage("Ready to download files from remote path {} to local path {}"), remotePath, path);
			downloadDirectory(ftpsRequest, path, remotePath);
			ftpsRequest.disconnect();

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
			LiaisonException, URISyntaxException, MailBoxServicesException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, JAXBException {

		//variable to hold the status of file download request execution
		int statusCode = 0;
		String dirToList = "";
		FTPDownloaderPropertiesDTO staticProp = (FTPDownloaderPropertiesDTO) getProperties();

		if (!currentDir.equals("")) {
			dirToList += currentDir;
		}

		FTPFile[] files = ftpClient.getNative().listFiles(dirToList);
		BufferedOutputStream bos = null;
		FileOutputStream fos = null;
		if (files != null && files.length > 0) {

			String statusIndicator = staticProp.getFileTransferStatusIndicator();
			for (FTPFile file : files) {

				if (file.getName().equals(".") || file.getName().equals("..")) {
					// skip parent directory and the directory itself
					continue;
				}
				String currentFileName = file.getName();
				if (file.isFile()) {

					// Check if the file to be downloaded is included or not excluded
                    if(!checkFileIncludeorExclude(staticProp.getIncludedFiles(),
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
					if (statusCode == 226 || statusCode == 250) {

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
							LOGGER.info(constructMessage("File {} deleted successfully"), currentFileName);
						}
					}

				} else {

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
		return FTPSClient.getClient(this);
	}

	@Override
	public void downloadDirectory(Object client, String remotePayloadLocation, String localTargetLocation) {

		G2FTPSClient ftpClient = (G2FTPSClient)client;
		try {
			downloadDirectory(ftpClient, remotePayloadLocation, localTargetLocation);
		} catch (MailBoxServicesException | IOException | LiaisonException | URISyntaxException | NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException | JAXBException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void uploadDirectory(Object client, String localPayloadLocation, String remoteTargeLocation) {
		// TODO Auto-generated method stub

	}

	@Override
	public void cleanup() {
		// TODO Auto-generated method stub

	}

	/**
	 * This Method create local folders if not available.
	 *
	 * * @param processorDTO it have details of processor
	 */
	@Override
	public void createLocalPath() {

		String configuredPath = null;
		try {
			configuredPath = getWriteResponseURI();
			createPathIfNotAvailable(configuredPath);

		} catch (IOException e) {
			throw new MailBoxConfigurationServicesException(Messages.LOCAL_FOLDERS_CREATION_FAILED,
					configuredPath, Response.Status.BAD_REQUEST,e.getMessage());
		}

	}
}
