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
import java.util.Arrays;
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
	public void runProcessor(String executionId, MailboxFSM fsm) {

		LOGGER.debug("Entering in invoke.");
	    try {

		// FTPSRequest executed through JavaScript
			if (getProperties().isHandOverExecutionToJavaScript()) {
				fsm.handleEvent(fsm.createEvent(ExecutionEvents.PROCESSOR_EXECUTION_HANDED_OVER_TO_JS));
				JavaScriptExecutorUtil.executeJavaScript(configurationInstance.getJavaScriptUri(), this);

			} else {
				// FTPSRequest executed through Java
				run();
			}
	   } catch(JAXBException |IOException | IllegalAccessException | NoSuchFieldException e) {
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

			String path = getPayloadURI();
			if (MailBoxUtil.isEmpty(path)) {
				LOGGER.info("The given payload URI is Empty.");
				throw new MailBoxServicesException("The given payload URI is Empty.", Response.Status.CONFLICT);
			}

			String remotePath = getWriteResponseURI();
			if (MailBoxUtil.isEmpty(remotePath)) {
				LOGGER.info("The given remote URI is Empty.");
				throw new MailBoxServicesException("The given remote URI is Empty.", Response.Status.CONFLICT);
			}

			LOGGER.info("Processor named {} with pguid {} of type {} belongs to Mailbox {} starts to process files",
					configurationInstance.getProcsrName(), configurationInstance.getPguid(),
					configurationInstance.getProcessorType().getCode(), configurationInstance.getMailbox().getPguid());
			startTime = System.currentTimeMillis();

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
			LOGGER.debug("Going to download files from remote directory {} to local directory {}", remotePath, path);
			downloadDirectory(ftpsRequest, path, remotePath);
			ftpsRequest.disconnect();

			// to calculate the elapsed time for running a processor
			long endTime = System.currentTimeMillis();
			LOGGER.info("Processor named {} with pguid {} of type {} belongs to Mailbox  {} ends processing of files",
					configurationInstance.getProcsrName(),configurationInstance.getPguid(), configurationInstance.getProcessorType().getCode(),
					configurationInstance.getMailbox().getPguid());
			LOGGER.info("Number of files Processed {}", totalNumberOfProcessedFiles);
			LOGGER.info("Total time taken to process files {}", endTime - startTime);

		} catch (LiaisonException | JAXBException | IOException | MailBoxServicesException
				| URISyntaxException |IllegalAccessException | NoSuchFieldException e) {
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
		FTPDownloaderPropertiesDTO ftpDownloaderStaticProperties = (FTPDownloaderPropertiesDTO)getProperties();

		if (!currentDir.equals("")) {
			dirToList += currentDir;
		}
		FTPFile[] files = ftpClient.getNative().listFiles(dirToList);
		BufferedOutputStream bos = null;
		FileOutputStream fos = null;
		if (files != null) {
			String statusIndicator = ftpDownloaderStaticProperties.getFileTransferStatusIndicator();
			String excludedFiles = ftpDownloaderStaticProperties.getExcludedFiles();
			String includedFiles = ftpDownloaderStaticProperties.getIncludedFiles();
			List<String> includeList = (!MailBoxUtil.isEmpty(includedFiles))? Arrays.asList(includedFiles.split(",")) : null;
			List<String> excludedList = (!MailBoxUtil.isEmpty(excludedFiles)) ? Arrays.asList(excludedFiles.split(",")) : null;
			for (FTPFile file : files) {

				if (file.getName().equals(".") || file.getName().equals("..")) {
					// skip parent directory and the directory itself
					continue;
				}
				String currentFileName = file.getName();
				if (file.isFile()) {
					// Check if the file to be downloaded is included or not excluded
					boolean downloadFile = checkFileIncludeorExclude(includeList, currentFileName, excludedList);
					//file must not be downloaded
					if(!downloadFile) {
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
							LOGGER.info("downloading file {}  from remote folder {} to local folder {} while running Processor {} of type {}",
									currentFileName, currentDir, localFileDir, configurationInstance.getPguid(),
									configurationInstance.getProcessorType().getCode());
							statusCode = ftpClient.getFile(currentFileName, bos);
						// Check whether the file downloaded successfully if so rename it.
						if (statusCode == 226 || statusCode == 250) {
							LOGGER.info("File {} downloaded successfully", currentFileName);
							totalNumberOfProcessedFiles++;
							fos.close();
							bos.close();

							// Renames the downloaded file to original extension once the fileStatusIndicator is  given by User
							if (!MailBoxUtil.isEmpty(statusIndicator)) {
								//Constructs the original file filename
								File actualFileName = new File(localFileDir + File.separatorChar + currentFileName);
								boolean renameStatus =  new File(localDir).renameTo(actualFileName);
								if (renameStatus) {
									LOGGER.info("File {} renamed successfully", currentFileName);
								} else {
									LOGGER.info("File {} renaming failed", currentFileName);
								}
							}
							// Delete the remote files after successful download if user opt for it
							if (ftpDownloaderStaticProperties.getDeleteFiles()) {
								ftpClient.deleteFile(file.getName());
								LOGGER.info("File {} deleted successfully", currentFileName);

							}
						}
						} finally {
							if (bos != null)
								bos.close();
							if (fos != null)
								fos.close();
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
