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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateEncodingException;
import java.util.Arrays;
import java.util.Date;
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
import com.liaison.commons.util.client.sftp.StringUtil;
import com.liaison.fs2.api.exceptions.FS2Exception;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.dtdm.model.Processor;
import com.liaison.mailbox.enums.ExecutionEvents;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.rtdm.dao.FSMEventDAOBase;
import com.liaison.mailbox.service.core.fsm.MailboxFSM;
import com.liaison.mailbox.service.core.processor.helper.ClientFactory;
import com.liaison.mailbox.service.dto.configuration.TriggerProcessorRequestDTO;
import com.liaison.mailbox.service.dto.configuration.processor.properties.SFTPUploaderPropertiesDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.executor.javascript.JavaScriptExecutorUtil;
import com.liaison.mailbox.service.util.MailBoxUtil;

/**
 * SFTP remote uploader to perform push operation, also it has support methods
 * for JavaScript.
 *
 * @author OFS
 */
public class SFTPRemoteUploader extends AbstractProcessor implements MailBoxProcessorI {

	private static final Logger LOGGER = LogManager.getLogger(SFTPRemoteUploader.class);

	@SuppressWarnings("unused")
	private SFTPRemoteUploader() {
	}

	public SFTPRemoteUploader(Processor processor) {
		super(processor);
	}

	/**
	 * Java method to execute the SFTPrequest to upload the file or folder
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
	 */
	private void executeRequest(String executionId, MailboxFSM fsm) throws URISyntaxException {

		try {

			G2SFTPClient sftpRequest = (G2SFTPClient) getClient();
			sftpRequest.connect();

			String path = getPayloadURI();

			if (MailBoxUtil.isEmpty(path)) {
				LOGGER.info("The given payload URI is Empty.");
				throw new MailBoxServicesException("The given payload URI is Empty.", Response.Status.CONFLICT);
			}

			LOGGER.info("Processor named {} with pguid {} of type {} belongs to Mailbox {} starts to process files",
					configurationInstance.getProcsrName(), configurationInstance.getPguid(),
					configurationInstance.getProcessorType().getCode(), configurationInstance.getMailbox().getPguid());
			long startTime = System.currentTimeMillis();
			if (sftpRequest.openChannel()) {

			    String remotePath = getWriteResponseURI();
				if (MailBoxUtil.isEmpty(remotePath)) {
					LOGGER.info("The given remote URI is Empty.");
					throw new MailBoxServicesException("The given remote URI is Empty.", Response.Status.CONFLICT);
				}

				//GMB-320 - Creates directory to the remote folder
				for (String directory : remotePath.split(File.separatorChar=='\\' ? "\\\\" : File.separator)) {

					if (directory.isEmpty()) {//For when path starts with /
						continue;
					}

					try {
						sftpRequest.getNative().lstat(directory);
						LOGGER.info("The remote directory{} already exists.", directory);
						sftpRequest.changeDirectory(directory);
					} catch (Exception ex) {
						sftpRequest.getNative().mkdir(directory);
						LOGGER.info("The remote directory{} is not exist.So created that.", directory);
						sftpRequest.changeDirectory(directory);
					}
				}
				LOGGER.debug("Going to upload files from local directory {} to remote directory {}", path, remotePath);
				uploadDirectory(sftpRequest, path, remotePath, executionId, fsm);

			}
			// remove the private key once connection established successfully
			removePrivateKeyFromTemp();
			sftpRequest.disconnect();
			long endTime = System.currentTimeMillis();
			LOGGER.info("Processor {} of type {} belongs to Mailbox  {} ends processing of files",
					configurationInstance.getPguid(), configurationInstance.getProcessorType().getCode(),
					configurationInstance.getMailbox().getPguid());
			LOGGER.info("Number of files Processed {}", totalNumberOfProcessedFiles);
			LOGGER.info("Total time taken to process files {}", endTime - startTime);

		} catch (LiaisonException | MailBoxServicesException | IOException
				| SftpException | SymmetricAlgorithmException | NoSuchFieldException
				| SecurityException | IllegalArgumentException | IllegalAccessException | JAXBException e) {
			throw new RuntimeException(e);
		}

	}

	/**
	 * Java method to upload the file or folder
	 *
	 * @throws IOException
	 * @throws LiaisonException
	 * @throws SftpException
	 * @throws MailBoxServicesException
	 * @throws com.liaison.commons.exception.LiaisonException
	 * @throws JAXBException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 * @throws URISyntaxException
	 *
	 */

	public void uploadDirectory(G2SFTPClient sftpRequest, String localParentDir, String remoteParentDir, String executionId, MailboxFSM fsm)
			throws IOException, LiaisonException, SftpException, MailBoxServicesException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, JAXBException, URISyntaxException {

		// variable to hold the status of file upload request execution
		int replyCode = -1;
		File localDir = new File(localParentDir);
		File[] subFiles = localDir.listFiles();
		SFTPUploaderPropertiesDTO sftpUploaderStaticProperties = (SFTPUploaderPropertiesDTO)getProperties();
		FSMEventDAOBase eventDAO = new FSMEventDAOBase();

		Date lastCheckTime = new Date();
		String constantInterval = MailBoxUtil.getEnvironmentProperties().getString(MailBoxConstants.DEFAULT_INTERRUPT_SIGNAL_FREQUENCY_IN_SEC);

		if (subFiles != null && subFiles.length > 0) {
			String statusIndicator = sftpUploaderStaticProperties.getFileTransferStatusIndicator();
			String includedFiles = sftpUploaderStaticProperties.getIncludedFiles();
			String excludedFiles = sftpUploaderStaticProperties.getExcludedFiles();
			List<String> includeList = (!MailBoxUtil.isEmpty(includedFiles))? Arrays.asList(includedFiles.split(",")) : null;
			List<String> excludeList = (!MailBoxUtil.isEmpty(excludedFiles)) ? Arrays.asList(excludedFiles.split(",")) : null;
			for (File item : subFiles) {
				//interrupt signal check has to be done only if execution Id is present
				if(!StringUtil.isNullOrEmptyAfterTrim(executionId) && ((new Date().getTime() - lastCheckTime.getTime())/1000) > Long.parseLong(constantInterval)) {
					lastCheckTime = new Date();
					if(eventDAO.isThereAInterruptSignal(executionId)) {
						LOGGER.info("##########################################################################");
						LOGGER.info("The executor with execution id  "+executionId+" is gracefully interrupted");
						LOGGER.info("#############################################################################");
						fsm.createEvent(ExecutionEvents.INTERRUPTED, executionId);
						fsm.handleEvent(fsm.createEvent(ExecutionEvents.INTERRUPTED));
						return;
					}
				}

				if (item.getName().equals(".") || item.getName().equals("..")) {
					// skip parent directory and the directory itself
					continue;
				}
				if (item.isDirectory()) {

					if (MailBoxConstants.PROCESSED_FOLDER.equals(item.getName())) {
						// skip processed folder
						continue;
					}

					String remoteFilePath = remoteParentDir + File.separatorChar + item.getName();

					Boolean fileExists = true;
					try {
						sftpRequest.getNative().lstat(remoteFilePath);
					} catch (Exception ex) {
						fileExists = false;
					}
					if (!fileExists) {
						// create directory on the server
						sftpRequest.getNative().mkdir(remoteFilePath);
					}
					// upload the sub directory
					sftpRequest.changeDirectory(remoteFilePath);
					String localDr = localParentDir + File.separatorChar + item.getName();
	                uploadDirectory(sftpRequest, localDr, remoteFilePath, executionId, fsm);
					replyCode = 0;

				} else {

					String currentFileName = item.getName();
					// Check if the file to be uploaded is included or not excluded
					boolean uploadFile = checkFileIncludeorExclude(includeList, currentFileName, excludeList);
					//file must not be uploaded
					if(!uploadFile) {
						continue;
					}

					//add status indicator if specified to indicate that uploading is in progress
					String uploadingFileName = (!MailBoxUtil.isEmpty(statusIndicator)) ? currentFileName + "."
							+ statusIndicator : currentFileName;

					// upload the file
				    try (InputStream inputStream = new FileInputStream(item)) {
				        sftpRequest.changeDirectory(remoteParentDir);
						LOGGER.info("uploading file {}  from local folder {} to remote folder {} while running Processor {} of type {}",
								currentFileName, localParentDir, remoteParentDir, configurationInstance.getPguid(),
								configurationInstance.getProcessorType().getCode());
	                    replyCode = sftpRequest.putFile(uploadingFileName, inputStream);
				    }

				    // Check whether the file uploaded successfully
					if (replyCode == 0) {
						LOGGER.info("File {} uploaded successfully", currentFileName);
						// Renames the uploaded file to original extension if the fileStatusIndicator is given by User
						if (!MailBoxUtil.isEmpty(statusIndicator)) {
							int renameStatus = sftpRequest.renameFile(uploadingFileName, currentFileName);
							if (renameStatus == 0) {
								LOGGER.info("File {} renamed successfully", currentFileName);
							} else {
								LOGGER.info("File {} renaming failed", currentFileName);
							}
						}
						// Delete the local files after successful upload if user opt for it
						if (sftpUploaderStaticProperties.getDeleteFiles()) {
							item.delete();
							LOGGER.info("File {} deleted successfully", currentFileName);
						} else {
							// File is not opted to be deleted. Hence moved to processed folder
							String processedFileLocation = replaceTokensInFolderPath(sftpUploaderStaticProperties.getProcessedFileLocation());
							if (MailBoxUtil.isEmpty(processedFileLocation)) {
								archiveFile(item.getAbsolutePath(), false);
							} else {
								archiveFile(item, processedFileLocation);
							}
						}

					} else {

						// File Uploading failed so move the file to error folder
						String errorFileLocation = replaceTokensInFolderPath(sftpUploaderStaticProperties.getErrorFileLocation());
						if (MailBoxUtil.isEmpty(errorFileLocation)) {
							archiveFile(item.getAbsolutePath(), true);
						} else {
							archiveFile(item, errorFileLocation);
						}
					}
				}
			}
		}
	}

	@Override
	public void runProcessor(TriggerProcessorRequestDTO dto, MailboxFSM fsm) {

		LOGGER.debug("Entering in invoke.");

		try {

			// SFTPRequest executed through JavaScript
			if (getProperties().isHandOverExecutionToJavaScript()) {

				fsm.handleEvent(fsm.createEvent(ExecutionEvents.PROCESSOR_EXECUTION_HANDED_OVER_TO_JS));

				// Use custom G2JavascriptEngine
				JavaScriptExecutorUtil.executeJavaScript(configurationInstance.getJavaScriptUri(), this);

			} else {
				// SFTPRequest executed through Java
				executeRequest(dto.getExecutionId(), fsm);
			}
		} catch(JAXBException |IOException |IllegalAccessException | NoSuchFieldException | URISyntaxException e) {
			throw new RuntimeException(e);
		}
	 }

	public boolean checkFileExistence() throws MailBoxServicesException, CertificateEncodingException, UnrecoverableKeyException, JsonParseException, OperatorCreationException, KeyStoreException, NoSuchAlgorithmException, LiaisonException, IOException, JAXBException, URISyntaxException, SymmetricAlgorithmException, JSONException, CMSException, BootstrapingFailedException {

		LOGGER.debug ("Entering file Existence check for SFTP Uploader processor");
		boolean isFileExists = false;
		G2SFTPClient sftpRequest = (G2SFTPClient) getClient();
		sftpRequest.connect();

		if (sftpRequest.openChannel()) {

		    String remotePath = getWriteResponseURI();
			if (MailBoxUtil.isEmpty(remotePath)) {
				LOGGER.info("The given remote URI is Empty.");
				throw new MailBoxServicesException("The given remote URI is Empty.", Response.Status.CONFLICT);
			}

			List <String> files = sftpRequest.listFiles(remotePath);
			isFileExists = (null != files && !files.isEmpty());
		}

		sftpRequest.disconnect();
		LOGGER.debug("File Eixstence check completed for SFTP Uploader. File exists - {}", isFileExists);
		return isFileExists;
	}

	@Override
	public Object getClient() {
		return ClientFactory.getClient(this);
	}

	@Override
	public void downloadDirectory(Object client, String localTargetLocation, String remotePayloadLocation) {
		// TODO Auto-generated method stub

	}

	@Override
	public void uploadDirectory(Object client, String localPayloadLocation, String remoteTargetLocation) {

		G2SFTPClient sftpRequest = (G2SFTPClient)client;
		try {
			uploadDirectory(sftpRequest, localPayloadLocation, remoteTargetLocation, null, null);
		} catch (MailBoxServicesException | IOException | LiaisonException | SftpException
				| NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException | JAXBException | URISyntaxException e) {
			throw new RuntimeException(e);
		}

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
	 *
	 */
	@Override
	public void createLocalPath() {

		String configuredPath = null;
		try {
			configuredPath = getPayloadURI();
			createPathIfNotAvailable(configuredPath);

		} catch (IOException e) {
			throw new MailBoxConfigurationServicesException(Messages.LOCAL_FOLDERS_CREATION_FAILED,
					configuredPath, Response.Status.BAD_REQUEST);
		}

	}
}
