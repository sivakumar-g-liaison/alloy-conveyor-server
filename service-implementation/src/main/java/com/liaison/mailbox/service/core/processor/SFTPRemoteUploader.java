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
import com.liaison.mailbox.enums.ExecutionState;
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
	private void executeRequest(String executionId, MailboxFSM fsm) {

		try {

			G2SFTPClient sftpRequest = (G2SFTPClient) getClient();
			sftpRequest.setLogPrefix(constructMessage());
			sftpRequest.connect();

			LOGGER.info(constructMessage("Start run"));
			long startTime = System.currentTimeMillis();

			String path = getPayloadURI();

			if (MailBoxUtil.isEmpty(path)) {
				LOGGER.info(constructMessage("The given payload URI is Empty."));
				throw new MailBoxServicesException("The given payload URI is Empty.", Response.Status.CONFLICT);
			}

			if (sftpRequest.openChannel()) {

			    String remotePath = getWriteResponseURI();
				if (MailBoxUtil.isEmpty(remotePath)) {
					LOGGER.info(constructMessage("The given remote URI is Empty."));
					throw new MailBoxServicesException("The given remote URI is Empty.", Response.Status.CONFLICT);
				}

				//GMB-320 - Creates directory to the remote folder
				for (String directory : remotePath.split(File.separatorChar=='\\' ? "\\\\" : File.separator)) {

					if (directory.isEmpty()) {//For when path starts with /
						continue;
					}

					try {
						sftpRequest.getNative().lstat(directory);
						LOGGER.info(constructMessage("The remote directory {} already exists."), directory);
					} catch (Exception ex) {
						sftpRequest.getNative().mkdir(directory);
						LOGGER.info(constructMessage("The remote directory {} is not exist.So created that."), directory);
					}
					sftpRequest.changeDirectory(directory);
				}
				LOGGER.info(constructMessage("Ready to upload files from local path {} to remote path {}"), path, remotePath);
				uploadDirectory(sftpRequest, path, remotePath, executionId, fsm);

			}
			sftpRequest.disconnect();
			long endTime = System.currentTimeMillis();
            LOGGER.info(constructMessage("Number of files processed {}"), totalNumberOfProcessedFiles);
            LOGGER.info(constructMessage("Total time taken to process files {}"), endTime - startTime);
            LOGGER.info(constructMessage("End run"));

		} catch (LiaisonException | MailBoxServicesException | IOException
				| SftpException | NoSuchFieldException
				| SecurityException | IllegalArgumentException | IllegalAccessException
				| JAXBException | URISyntaxException e) {
            LOGGER.error(constructMessage("Error occurred during sftp upload", seperator, e.getMessage()), e);
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
		SFTPUploaderPropertiesDTO staticProp = (SFTPUploaderPropertiesDTO) getProperties();
		FSMEventDAOBase eventDAO = new FSMEventDAOBase();

		Date lastCheckTime = new Date();
		String constantInterval = MailBoxUtil.getEnvironmentProperties().getString(MailBoxConstants.DEFAULT_INTERRUPT_SIGNAL_FREQUENCY_IN_SEC);

		if (subFiles != null && subFiles.length > 0) {

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

					if (MailBoxConstants.PROCESSED_FOLDER.equals(item.getName()) ||
					        MailBoxConstants.ERROR_FOLDER.equals(item.getName())) {
						// skip processed folder
						LOGGER.info(constructMessage("skipping processed/error folder"));
						continue;
					}

					String remoteFilePath = remoteParentDir + File.separatorChar + item.getName();
					try {
						sftpRequest.getNative().lstat(remoteFilePath);
					} catch (Exception ex) {
					    //happens when the directory is not available in the remote server
						sftpRequest.getNative().mkdir(remoteFilePath);
					}

					// upload the sub directory
					sftpRequest.changeDirectory(remoteFilePath);
					String localDr = localParentDir + File.separatorChar + item.getName();
	                uploadDirectory(sftpRequest, localDr, remoteFilePath, executionId, fsm);
					replyCode = MailBoxConstants.SFTP_FILE_TRANSFER_ACTION_OK;

				} else {

				    String currentFileName = item.getName();

				    //File Modification Check
				    if (MailBoxUtil.validateLastModifiedTolerance(item.toPath())) {
		                LOGGER.info(constructMessage("The file {} is still in progress, so it is skipped."), currentFileName);
		                continue;
		            }

					// Check if the file to be uploaded is included or not excluded
					if(!checkFileIncludeorExclude(staticProp.getIncludedFiles(),
					        currentFileName,
					        staticProp.getExcludedFiles())) {
					    continue;
					}

					//add status indicator if specified to indicate that uploading is in progress
					String statusIndicator = staticProp.getFileTransferStatusIndicator();
					String uploadingFileName = (!MailBoxUtil.isEmpty(statusIndicator)) ? currentFileName + "."
							+ statusIndicator : currentFileName;

					// upload the file
				    try (InputStream inputStream = new FileInputStream(item)) {
				        sftpRequest.changeDirectory(remoteParentDir);
						LOGGER.info(constructMessage("uploading file {} from local path {} to remote path {}"),
								currentFileName, localParentDir, remoteParentDir);
	                    replyCode = sftpRequest.putFile(uploadingFileName, inputStream);
				    }

				    // Check whether the file uploaded successfully
					if (replyCode == MailBoxConstants.SFTP_FILE_TRANSFER_ACTION_OK) {

						totalNumberOfProcessedFiles++;
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

						deleteOrArchiveTheFiles(staticProp.getDeleteFiles(),
						        staticProp.getProcessedFileLocation(), 
						        item);
						StringBuilder message = new StringBuilder()
													.append("File ")
													.append(currentFileName)
													.append(" uploaded successfully")
													.append(" to remote path ")
													.append(remoteParentDir);
						// Glass Logging 
						logGlassMessage(message.toString(), item, ExecutionState.COMPLETED);
					} else {
						
						archiveFiles(staticProp.getErrorFileLocation(), item);
						StringBuilder message = new StringBuilder()
													.append("Failed to upload file ")
													.append(currentFileName)
													.append(" from local path ")
													.append(localDir)
													.append(" to remote path ")
													.append(remoteParentDir);
						// Glass Logging 
						logGlassMessage(message.toString(), item, ExecutionState.FAILED);
					}
				}
			}
		}
		else {
			LOGGER.info(constructMessage("The given payload URI '" + localDir + "' is empty."));
		}
	}

	@Override
	public void runProcessor(Object dto, MailboxFSM fsm) {

		LOGGER.debug("Entering in invoke.");

		try {

		    setReqDTO((TriggerProcessorRequestDTO) dto);
			// SFTPRequest executed through JavaScript
			if (getProperties().isHandOverExecutionToJavaScript()) {

				fsm.handleEvent(fsm.createEvent(ExecutionEvents.PROCESSOR_EXECUTION_HANDED_OVER_TO_JS));

				// Use custom G2JavascriptEngine
				JavaScriptExecutorUtil.executeJavaScript(configurationInstance.getJavaScriptUri(), this);

			} else {
				// SFTPRequest executed through Java
				executeRequest(getReqDTO().getExecutionId(), fsm);
			}
		} catch(JAXBException |IOException |IllegalAccessException | NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	 }

	public boolean checkFileExistence() throws Exception {

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
	public void cleanup() {
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

    @Override
    public void logToLens(String msg, File file, ExecutionState status) {
        logGlassMessage(msg, file, status);
    }

}
