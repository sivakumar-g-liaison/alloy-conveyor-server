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
import java.security.cert.CertificateException;
import java.util.Date;

import javax.xml.bind.JAXBException;

import org.codehaus.jettison.json.JSONException;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.google.gson.JsonParseException;
import com.jcraft.jsch.SftpException;
import com.liaison.commons.exception.LiaisonException;
import com.liaison.commons.security.pkcs7.SymmetricAlgorithmException;
import com.liaison.commons.util.client.ftps.G2FTPSClient;
import com.liaison.fs2.api.FS2Exception;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.enums.ExecutionEvents;
import com.liaison.mailbox.jpa.dao.FSMEventDAOBase;
import com.liaison.mailbox.jpa.model.Processor;
import com.liaison.mailbox.service.core.fsm.MailboxFSM;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.util.JavaScriptEngineUtil;
import com.liaison.mailbox.service.util.MailBoxUtility;

/**
 * 
 ** FTPS remote uploader to perform pull operation, also it has support methods
 * for JavaScript.
 * 
 * @author praveenu
 * 
 */
public class FTPSRemoteUploader extends AbstractRemoteProcessor implements MailBoxProcessor {

	private static final Logger LOGGER = LogManager.getLogger(FTPSRemoteUploader.class);

	@SuppressWarnings("unused")
	private FTPSRemoteUploader() {
	}

	public FTPSRemoteUploader(Processor processor) {
		super(processor);
	}

	@Override
	public void invoke(String executionId,MailboxFSM fsm) throws Exception {
		
		LOGGER.info("Entering in invoke.");
		// FTPSRequest executed through JavaScript
		if (!MailBoxUtility.isEmpty(configurationInstance.getJavaScriptUri())) {
			fsm.handleEvent(fsm.createEvent(ExecutionEvents.PROCESSOR_EXECUTION_HANDED_OVER_TO_JS));
			JavaScriptEngineUtil.executeJavaScript(configurationInstance.getJavaScriptUri(), "init", this,LOGGER);

		} else {
			// FTPSRequest executed through Java
			executeRequest(executionId, fsm);
		}
	}

	/**
	 * Java method to inject the G2FTPS configurations
	 * 
	 * @throws IOException
	 * @throws LiaisonException
	 * @throws JAXBException
	 * @throws MailBoxServicesException
	 * @throws URISyntaxException
	 * @throws SymmetricAlgorithmException
	 * @throws JsonParseException
	 * @throws com.liaison.commons.exception.LiaisonException 
	 * @throws JSONException 
	 * @throws KeyStoreException 
	 * @throws CertificateException 
	 * @throws NoSuchAlgorithmException 
	 * @throws MailBoxConfigurationServicesException
	 * 
	 */
	@Override
	public G2FTPSClient getClientWithInjectedConfiguration() throws LiaisonException, IOException, JAXBException,
			URISyntaxException, MailBoxServicesException, JsonParseException, SymmetricAlgorithmException, com.liaison.commons.exception.LiaisonException, NoSuchAlgorithmException, CertificateException, KeyStoreException, JSONException {

		G2FTPSClient ftpsRequest = getFTPSClient(LOGGER);
		return ftpsRequest;

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
	 * @throws KeyStoreException 
	 * @throws CertificateException 
	 * @throws NoSuchAlgorithmException 
	 * @throws JsonParseException 
	 * 
	 */
	protected void executeRequest(String executionId, MailboxFSM fsm) throws MailBoxServicesException, LiaisonException, IOException, FS2Exception,
			URISyntaxException, JAXBException, SymmetricAlgorithmException, com.liaison.commons.exception.LiaisonException, JsonParseException, NoSuchAlgorithmException, CertificateException, KeyStoreException, JSONException {

		G2FTPSClient ftpsRequest = getClientWithInjectedConfiguration();
		
		ftpsRequest.enableSessionReuse(true);
		ftpsRequest.connect();
		ftpsRequest.login();
		
		ftpsRequest.enableDataChannelEncryption();
		if (getRemoteProcessorProperty() != null) {

			ftpsRequest.setBinary(getRemoteProcessorProperty().isBinary());
			ftpsRequest.setPassive(getRemoteProcessorProperty().isPassive());
		}
		String path = getPayloadURI();
		if (MailBoxUtility.isEmpty(path)) {
			LOGGER.info("The given payload URI is Empty.");
			throw new MailBoxServicesException("The given payload configuration is Empty.");
		}

		String remotePath = getWriteResponseURI();
		if (MailBoxUtility.isEmpty(remotePath)) {
			LOGGER.info("The given remote URI is Empty.");
			throw new MailBoxServicesException("The given remote configuration is Empty.");
		}

		boolean dirExists = ftpsRequest.getNative().changeWorkingDirectory(remotePath);
		if (!dirExists) {
			// create directory on the server
			ftpsRequest.getNative().makeDirectory(remotePath);
		}
		ftpsRequest.changeDirectory(remotePath);

		uploadDirectory(ftpsRequest, path, remotePath, executionId, fsm);
		ftpsRequest.disconnect();
	}

	/**
	 * Java method to upload the file or folder
	 * 
	 * @throws IOException
	 * @throws LiaisonException
	 * @throws com.liaison.commons.exception.LiaisonException 
	 * @throws SftpException
	 * 
	 */
	public void uploadDirectory(G2FTPSClient ftpsRequest, String localParentDir, String remoteParentDir, String executionId, MailboxFSM fsm)
			throws IOException, LiaisonException, com.liaison.commons.exception.LiaisonException,MailBoxServicesException {
		
		//TODO find appropriate place to trigger event fsm.handleEvent(fsm.createEvent(ExecutionEvents.GRACEFULLY_INTERRUPTED));

		File localDir = new File(localParentDir);
		File[] subFiles = localDir.listFiles();
		// variable to hold the status of file upload request execution
		int replyCode = 0;
		
		Date lastCheckTime = new Date();
		String constantInterval = MailBoxUtility.getEnvironmentProperties().getString("fsmEventCheckIntervalInSeconds");

		FSMEventDAOBase eventDAO = new FSMEventDAOBase();
		
		if (subFiles != null && subFiles.length > 0) {
			for (File item : subFiles) {
				
				//interrupt signal check
				if(((new Date().getTime() - lastCheckTime.getTime())/1000) > Long.parseLong(constantInterval)) {
					if(eventDAO.isThereAInterruptSignal(executionId)) {
						fsm.createEvent(ExecutionEvents.INTERRUPTED, executionId);
						fsm.handleEvent(fsm.createEvent(ExecutionEvents.GRACEFULLY_INTERRUPTED));
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
				if (item.isFile()) {

					// upload file
					ftpsRequest.changeDirectory(remoteParentDir);
					InputStream inputStream = new FileInputStream(item);
					replyCode = ftpsRequest.putFile(item.getName(), inputStream);
					inputStream.close();

				} else {

					if (MailBoxConstants.PROCESSED_FOLDER.equals(item.getName())) {
						// skip processed folder
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

				if (null != item) {
					
					// File Uploading done successfully so move the file to processed folder
					if(replyCode == 226 || replyCode == 250) {
						String processedFileLocation = processMountLocation(getDynamicProperties().getProperty(
								MailBoxConstants.PROCESSED_FILE_LOCATION));
						if (MailBoxUtility.isEmpty(processedFileLocation)) {
							archiveFile(item.getAbsolutePath(), false);
						} else {
							archiveFile(item, processedFileLocation);
						}
					} else {
						// File uploading failed so move the file to error folder
						String errorFileLocation = processMountLocation(getDynamicProperties().getProperty(
								MailBoxConstants.ERROR_FILE_LOCATION));
						if (MailBoxUtility.isEmpty(errorFileLocation)) {
							archiveFile(item.getAbsolutePath(), true);
						} else {
							archiveFile(item, errorFileLocation);
						}
					}
					
				}
			}
		} else {			 
			LOGGER.info("The given payload URI'" + localDir + "' does not exist.");
			throw new MailBoxServicesException("The given payload configuration '" + localDir + "' does not exist.");
		}
	}
}
