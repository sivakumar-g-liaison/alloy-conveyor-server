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
import java.security.cert.CertificateEncodingException;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;

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
import com.liaison.commons.util.client.sftp.G2SFTPClient;
import com.liaison.fs2.api.exceptions.FS2Exception;
import com.liaison.mailbox.dtdm.model.Processor;
import com.liaison.mailbox.enums.ExecutionEvents;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.service.core.fsm.MailboxFSM;
import com.liaison.mailbox.service.core.processor.helper.ClientFactory;
import com.liaison.mailbox.service.dto.configuration.processor.properties.SFTPDownloaderPropertiesDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.executor.javascript.JavaScriptExecutorUtil;
import com.liaison.mailbox.service.util.MailBoxUtil;

/**
 * SFTP remote downloader to perform pull operation, also it has support methods
 * for JavaScript.
 *
 * @author OFS
 */
public class SFTPRemoteDownloader extends AbstractProcessor implements MailBoxProcessorI {

	private static final Logger LOGGER = LogManager.getLogger(SFTPRemoteDownloader.class);

	@SuppressWarnings("unused")
	private SFTPRemoteDownloader() {
	}

	public SFTPRemoteDownloader(Processor processor) {
		super(processor);
	}

	/**
	 * Java method to execute the SFTPrequest
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 *
	 * @throws IOException
	 * @throws LiaisonException
	 * @throws JAXBException
	 * @throws SymmetricAlgorithmException
	 * @throws SftpException
	 * @throws com.liaison.commons.exception.LiaisonException
	 * @throws JSONException
	 * @throws JsonParseException
	 * @throws BootstrapingFailedException
	 * @throws CMSException
	 * @throws NoSuchAlgorithmException
	 * @throws KeyStoreException
	 * @throws OperatorCreationException
	 * @throws UnrecoverableKeyException
	 * @throws CertificateEncodingException
	 *
	 * @throws MailBoxConfigurationServicesException
	 *
	 */
	private void executeSFTPRequest() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, JAXBException {

		try {
			G2SFTPClient sftpRequest = (G2SFTPClient) getClient();
			sftpRequest.connect();

			if (sftpRequest.openChannel()) {

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

				downloadDirectory(sftpRequest, path, remotePath);
			}
			// remove the private key once connection established successfully
			removePrivateKeyFromTemp();
			sftpRequest.disconnect();
		} catch (LiaisonException | MailBoxServicesException | IOException | URISyntaxException
				| SftpException | SymmetricAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Java method to download the folder and its files
	 *
	 * @throws IOException
	 * @throws LiaisonException
	 * @throws URISyntaxException
	 * @throws FS2Exception
	 * @throws MailBoxServicesException
	 * @throws SftpException
	 * @throws com.liaison.commons.exception.LiaisonException
	 * @throws JAXBException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 *
	 */
	public void downloadDirectory(G2SFTPClient sftpRequest, String currentDir, String localFileDir) throws IOException,
			LiaisonException, URISyntaxException, MailBoxServicesException, SftpException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, JAXBException {
		
		//variable to hold the status of file download request execution
		int statusCode = 0;
		String dirToList = "";
		SFTPDownloaderPropertiesDTO sftpDownloaderStaticProperties = (SFTPDownloaderPropertiesDTO)getProperties();

		if (!currentDir.equals("")) {
			dirToList += currentDir;
		}

		List<String> files = sftpRequest.listFiles(currentDir);
		BufferedOutputStream bos = null;
		FileOutputStream fos = null;
		if (files != null && files.size() > 0) {
			String statusIndicator = sftpDownloaderStaticProperties.getFileTransferStatusIndicator();
			String excludedFiles = sftpDownloaderStaticProperties.getExcludedFiles();
			String includedFiles = sftpDownloaderStaticProperties.getIncludedFiles();
			List<String> includeList = (!MailBoxUtil.isEmpty(includedFiles))? Arrays.asList(includedFiles.split(",")) : null;
			List<String> excludedList = (!MailBoxUtil.isEmpty(excludedFiles)) ? Arrays.asList(excludedFiles.split(",")) : null;
			
			for (String aFile : files) {
				if (aFile.equals(".") || aFile.equals("..")) {
					// skip parent directory and the directory itself
					continue;
				}
	
				boolean isDir = sftpRequest.getNative().stat(dirToList + File.separatorChar + aFile).isDir();
				if (isDir) {

					String localDir = localFileDir + File.separatorChar + aFile;
					String remotePath = dirToList + File.separatorChar + aFile;
					File directory = new File(localDir);
					if (!directory.exists()) {
						Files.createDirectories(directory.toPath());
					}
					sftpRequest.changeDirectory(remotePath);
					downloadDirectory(sftpRequest, remotePath, localDir);

				} else {
	
					String currentFileName = aFile;
					// Check if the file to be downloaded is included or not excluded
					boolean downloadFile = checkFileIncludeorExclude(includeList, currentFileName, excludedList);
					//file must not be downloaded
					if(!downloadFile) {
						continue;
					}
					
					String downloadingFileName = (!MailBoxUtil.isEmpty(statusIndicator)) ? currentFileName + "."
							+ statusIndicator : currentFileName;
					String localDir = localFileDir + File.separatorChar + downloadingFileName;
					sftpRequest.changeDirectory(dirToList);
					createResponseDirectory(localDir);

					try {// GSB-1337,GSB-1336

						fos = new FileOutputStream(localDir);
						bos = new BufferedOutputStream(fos);
						statusCode = sftpRequest.getFile(currentFileName, bos);
						// Check whether the file downloaded successfully if so rename it.
						if (statusCode == 0) {
							LOGGER.info("File downloaded successfully");
							if (fos != null) fos.close();
							if (bos != null) bos.close();
							 
							// Renames the downloaded file to original extension once the fileStatusIndicator is given by User
							if (!MailBoxUtil.isEmpty(statusIndicator)) {
								File downloadedFile = new File(localDir);
								File currentFile = new File(localFileDir + File.separatorChar + currentFileName);
								boolean renameStatus = downloadedFile.renameTo(currentFile);
								if (renameStatus) {
									LOGGER.info("File renamed successfully");
								} else {
									LOGGER.info("File renaming failed");
								}
							}
							// Delete the remote files after successful download if user optioned for it
							if (sftpDownloaderStaticProperties.getDeleteFiles()) {
								sftpRequest.deleteFile(aFile);
								LOGGER.info("File deleted successfully");
							}
						}
					} finally {
						if (bos != null) bos.close();
						if (fos != null) fos.close();
					}

				}
			}
		}
	}

	@Override
	public void runProcessor(String executionId,MailboxFSM fsm) {

		LOGGER.debug("Entering in invoke.");
		try {
			// G2SFTP executed through JavaScript
			if (getProperties().isHandOverExecutionToJavaScript()) {

				fsm.handleEvent(fsm.createEvent(ExecutionEvents.PROCESSOR_EXECUTION_HANDED_OVER_TO_JS));

				// Use custom G2JavascriptEngine
				JavaScriptExecutorUtil.executeJavaScript(configurationInstance.getJavaScriptUri(), this);

			} else {
				// G2SFTP executed through Java
				executeSFTPRequest();
			}

		} catch(JAXBException |IOException |IllegalAccessException | NoSuchFieldException e) {
			throw new RuntimeException(e);
		}

	}

	@Override
	public Object getClient() {
		return ClientFactory.getClient(this);
	}

	@Override
	public void downloadDirectory(Object client, String remotePayloadLocation, String localTargetLocation) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, JAXBException {

		G2SFTPClient sftpClient = (G2SFTPClient)client;
	    try {
			downloadDirectory(sftpClient, remotePayloadLocation, localTargetLocation);
		} catch (MailBoxServicesException | IOException | LiaisonException | URISyntaxException | SftpException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void uploadDirectory(Object client, String localPayloadLocation, String remoteTargetLocation) {
		// TODO Auto-generated method stub

	}

	@Override
	public void cleanup() {
		// To remove the private key retrieved from key manager
		try {
			removePrivateKeyFromTemp();
		} catch (MailBoxServicesException | IOException | SymmetricAlgorithmException e) {
			throw new RuntimeException(e);
		}
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
					configuredPath, Response.Status.BAD_REQUEST);
		}

	}
}