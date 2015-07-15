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
import java.security.cert.CertificateException;
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
import com.liaison.commons.util.client.ftps.G2FTPSClient;
import com.liaison.commons.util.client.sftp.StringUtil;
import com.liaison.fs2.api.exceptions.FS2Exception;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.dtdm.model.Processor;
import com.liaison.mailbox.enums.ExecutionEvents;
import com.liaison.mailbox.enums.ExecutionState;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.rtdm.dao.FSMEventDAOBase;
import com.liaison.mailbox.service.core.fsm.MailboxFSM;
import com.liaison.mailbox.service.core.processor.helper.FTPSClient;
import com.liaison.mailbox.service.dto.configuration.TriggerProcessorRequestDTO;
import com.liaison.mailbox.service.dto.configuration.processor.properties.FTPUploaderPropertiesDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.executor.javascript.JavaScriptExecutorUtil;
import com.liaison.mailbox.service.util.MailBoxUtil;

/**
 *
 ** FTPS remote uploader to perform pull operation, also it has support methods
 * for JavaScript.
 *
 * @author OFS
 *
 */
public class FTPSRemoteUploader extends AbstractProcessor implements MailBoxProcessorI {

	private static final Logger LOGGER = LogManager.getLogger(FTPSRemoteUploader.class);

	@SuppressWarnings("unused")
	private FTPSRemoteUploader() {
	}

	public FTPSRemoteUploader(Processor processor) {
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
				run(getReqDTO().getExecutionId(), fsm);
			}

		} catch(Exception e) {
			throw new RuntimeException(e);
		}

	}

	/**
	 * Java method to execute the SFTPrequest to upload the file or folder
	 * @throws IllegalArgumentException
	 * @throws SecurityException
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
	 * @throws KeyStoreException
	 * @throws CertificateException
	 * @throws NoSuchAlgorithmException
	 * @throws JsonParseException
	 * @throws BootstrapingFailedException
	 * @throws CMSException
	 * @throws OperatorCreationException
	 * @throws UnrecoverableKeyException
	 *
	 */
	protected void run(String executionId, MailboxFSM fsm) {

		try {

			G2FTPSClient ftpsRequest = (G2FTPSClient) getClient();

			//ftpsRequest.enableSessionReuse(true);
			ftpsRequest.connect();
			ftpsRequest.login();

			//ftpsRequest.enableDataChannelEncryption();

			// retrieve required properties
			FTPUploaderPropertiesDTO ftpUploaderStaticProperties = (FTPUploaderPropertiesDTO)getProperties();

			if (ftpUploaderStaticProperties != null) {

				boolean binary = ftpUploaderStaticProperties.isBinary();
				boolean passive = ftpUploaderStaticProperties.isPassive();
				ftpsRequest.setBinary(binary);
				ftpsRequest.setPassive(passive);

			}

			LOGGER.info(constructMessage("Start run"));
			long startTime = System.currentTimeMillis();

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

			boolean dirExists = ftpsRequest.getNative().changeWorkingDirectory(remotePath);
			if (!dirExists) {
				// create directory on the server
			    LOGGER.info(constructMessage("The remote directory {} is not exist.So created that."), remotePath);
				ftpsRequest.getNative().makeDirectory(remotePath);
			}
			ftpsRequest.changeDirectory(remotePath);

			LOGGER.info(constructMessage("Ready to upload files from local path {} to remote path {}"), path, remotePath);
			uploadDirectory(ftpsRequest, path, remotePath, executionId, fsm);
			ftpsRequest.disconnect();

			long endTime = System.currentTimeMillis();
            LOGGER.info(constructMessage("Number of files processed {}"), totalNumberOfProcessedFiles);
            LOGGER.info(constructMessage("Total time taken to process files {}"), endTime - startTime);
            LOGGER.info(constructMessage("End run"));

		} catch (LiaisonException
		        | MailBoxServicesException
		        | JAXBException
		        | IOException
		        | NoSuchFieldException
		        | IllegalAccessException
		        | SecurityException
		        | IllegalArgumentException
		        | URISyntaxException e) {
		    LOGGER.error(constructMessage("Error occurred during ftp(s) upload", seperator, e.getMessage()), e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Java method to upload the file or folder
	 *
	 * @throws IOException
	 * @throws LiaisonException
	 * @throws com.liaison.commons.exception.LiaisonException
	 * @throws JAXBException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 * @throws URISyntaxException
	 * @throws SftpException
	 *
	 */

	public void uploadDirectory(G2FTPSClient ftpsRequest, String localParentDir, String remoteParentDir, String executionId, MailboxFSM fsm)
			throws IOException, LiaisonException, MailBoxServicesException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, JAXBException, URISyntaxException {

		// variable to hold the status of file upload request execution
		int replyCode = 0;
		File localDir = new File(localParentDir);
		File[] subFiles = localDir.listFiles();
		FTPUploaderPropertiesDTO staticProp = (FTPUploaderPropertiesDTO) getProperties();
		Date lastCheckTime = new Date();
		String constantInterval = MailBoxUtil.getEnvironmentProperties().getString(MailBoxConstants.DEFAULT_INTERRUPT_SIGNAL_FREQUENCY_IN_SEC);

		FSMEventDAOBase eventDAO = new FSMEventDAOBase();

		if (subFiles != null && subFiles.length > 0) {

			String statusIndicator = staticProp.getFileTransferStatusIndicator();
			for (File item : subFiles) {

				//interrupt signal check has to be done only if execution Id is present
				if(!StringUtil.isNullOrEmptyAfterTrim(executionId) && ((new Date().getTime() - lastCheckTime.getTime())/1000) > Long.parseLong(constantInterval)) {
					if(eventDAO.isThereAInterruptSignal(executionId)) {
						fsm.createEvent(ExecutionEvents.INTERRUPTED, executionId);
						fsm.handleEvent(fsm.createEvent(ExecutionEvents.INTERRUPTED));
						LOGGER.info("##########################################################################");
						LOGGER.info("The executor with execution id  "+executionId+" is gracefully interrupted");
						LOGGER.info("#############################################################################");
						return;
					}
					lastCheckTime = new Date();
				}

				if (item.getName().equals(".") || item.getName().equals("..")) {
					// skip parent directory and the directory itself
					continue;
				}

				String currentFileName = item.getName();
				if (item.isFile()) {

				    // Check if the file to be uploaded is included or not excluded
                    //file must not be uploaded
                    if(!checkFileIncludeorExclude(staticProp.getIncludedFiles(),
                            currentFileName,
                            staticProp.getExcludedFiles())) {
                        continue;
                    }

					//add status indicator if specified to indicate that uploading is in progress
					String uploadingFileName = (!MailBoxUtil.isEmpty(statusIndicator)) ? currentFileName + "."
							+ statusIndicator : currentFileName;

					// upload file
				    try (InputStream inputStream = new FileInputStream(item)) {
				        ftpsRequest.changeDirectory(remoteParentDir);
						LOGGER.info(constructMessage("uploading file {} from local path {} to remote path {}"),
								currentFileName, localParentDir, remoteParentDir);
	                    replyCode = ftpsRequest.putFile(uploadingFileName, inputStream);
				    }

                    // Check whether the file is uploaded successfully
                    if (replyCode == 226 || replyCode == 250) {

						LOGGER.info(constructMessage("File {} uploaded successfully"), currentFileName);
						totalNumberOfProcessedFiles++;
						// Renames the uploaded file to original extension once the fileStatusIndicator is given by User
						if (!MailBoxUtil.isEmpty(statusIndicator)) {
							int renameStatus = ftpsRequest.renameFile(uploadingFileName, currentFileName);
							if (renameStatus == 250) {
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
                        logGlassMessage(message, item, ExecutionState.COMPLETED);
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
                            logGlassMessage(message, item, ExecutionState.FAILED);
					}

				} else {

					if (MailBoxConstants.PROCESSED_FOLDER.equals(item.getName())
					        || MailBoxConstants.ERROR_FOLDER.equals(item.getName())) {
						// skip processed folder
						LOGGER.info(constructMessage("skipping processed/error folder"));
						continue;
					}

					String remoteFilePath = remoteParentDir + File.separatorChar + item.getName();

					boolean dirExists = ftpsRequest.getNative().changeWorkingDirectory(remoteFilePath);
					if (!dirExists) {
						// create directory on the server
						ftpsRequest.getNative().makeDirectory(remoteFilePath);
					}
					ftpsRequest.changeDirectory(remoteFilePath);
					uploadDirectory(ftpsRequest, item.getAbsolutePath(), remoteFilePath, executionId, fsm);
					replyCode = 250;
				}
			}
		} else {
			LOGGER.info(constructMessage("The given payload URI '" + localDir + "' is empty."));
		}
	}

	/**
	 * Method which checks whether files exists in the remote target location of the processor
	 *
	 * @return
	 * @throws IOException
	 * @throws UnrecoverableKeyException
	 * @throws JsonParseException
	 * @throws NoSuchAlgorithmException
	 * @throws CertificateException
	 * @throws KeyStoreException
	 * @throws OperatorCreationException
	 * @throws LiaisonException
	 * @throws JAXBException
	 * @throws URISyntaxException
	 * @throws MailBoxServicesException
	 * @throws SymmetricAlgorithmException
	 * @throws JSONException
	 * @throws CMSException
	 * @throws BootstrapingFailedException
	 */
	public boolean checkFileExistence() throws Exception {

		LOGGER.debug ("Entering file Existence check for FTP Uploader processor");
		G2FTPSClient ftpsRequest = (G2FTPSClient) getClient();

		boolean isFileExists = false;

		//ftpsRequest.enableSessionReuse(true);
		ftpsRequest.connect();
		ftpsRequest.login();

		//ftpsRequest.enableDataChannelEncryption();
		// retrieve required properties
		FTPUploaderPropertiesDTO ftpUploaderStaticProperties = (FTPUploaderPropertiesDTO)getProperties();

		if (ftpUploaderStaticProperties != null) {

			boolean binary = ftpUploaderStaticProperties.isBinary();
			boolean passive = ftpUploaderStaticProperties.isPassive();

			ftpsRequest.setBinary(binary);
			ftpsRequest.setPassive(passive);

		}

		String remotePath = getWriteResponseURI();
		if (MailBoxUtil.isEmpty(remotePath)) {
			LOGGER.info("The given remote URI is Empty.");
			throw new MailBoxServicesException("The given remote URI is Empty.", Response.Status.CONFLICT);
		}

		boolean dirExists = ftpsRequest.getNative().changeWorkingDirectory(remotePath);
		if (dirExists) {
			ftpsRequest.changeDirectory(remotePath);
			List <String> files = ftpsRequest.listFiles();
			isFileExists = (null !=  files && !files.isEmpty());
		}
		ftpsRequest.disconnect();
		LOGGER.debug("File Eixstence check completed for FTP Uploader. File exists - {}", isFileExists);
		return isFileExists;
		}

	@Override
	public Object getClient() {
		return FTPSClient.getClient(this);
	}

    	@Override
	public void downloadDirectory(Object client, String remotePayloadLocation, String localTargetLocation) {
		// TODO Auto-generated method stub

	}

	@Override
	public void uploadDirectory(Object client, String localPayloadLocation, String remoteTargetLocation) throws NoSuchFieldException, SecurityException,
				IllegalArgumentException, IllegalAccessException, JAXBException {
		G2FTPSClient ftpRequest = (G2FTPSClient)client;
		try {
			uploadDirectory(ftpRequest, localPayloadLocation, remoteTargetLocation, null, null);
		} catch (MailBoxServicesException | IOException | LiaisonException | URISyntaxException   e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void cleanup() {
		// TODO Auto-generated method stub

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
					configuredPath, Response.Status.BAD_REQUEST,e.getMessage());
		}

	}
}
