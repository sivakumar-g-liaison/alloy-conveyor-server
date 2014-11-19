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
import java.security.KeyStore;
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
import com.liaison.fs2.api.exceptions.FS2Exception;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.dtdm.model.Credential;
import com.liaison.mailbox.dtdm.model.Processor;
import com.liaison.mailbox.enums.CredentialType;
import com.liaison.mailbox.enums.ExecutionEvents;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.rtdm.dao.FSMEventDAOBase;
import com.liaison.mailbox.service.core.fsm.MailboxFSM;
import com.liaison.mailbox.service.dto.configuration.request.RemoteProcessorPropertiesDTO;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.util.JavaScriptEngineUtil;
import com.liaison.mailbox.service.util.KMSUtil;
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
	public void invoke(String executionId,MailboxFSM fsm) throws Exception {

		LOGGER.debug("Entering in invoke.");
		// FTPSRequest executed through JavaScript
		if (!MailBoxUtil.isEmpty(configurationInstance.getJavaScriptUri())) {
			fsm.handleEvent(fsm.createEvent(ExecutionEvents.PROCESSOR_EXECUTION_HANDED_OVER_TO_JS));
			JavaScriptEngineUtil.executeJavaScript(configurationInstance.getJavaScriptUri(), this);

		} else {
			// FTPSRequest executed through Java
			executeRequest(executionId, fsm);
		}
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
	 * @throws BootstrapingFailedException
	 * @throws CMSException
	 * @throws OperatorCreationException
	 * @throws UnrecoverableKeyException
	 *
	 */
	protected void executeRequest(String executionId, MailboxFSM fsm) throws Exception {

		G2FTPSClient ftpsRequest = (G2FTPSClient) getClient();

		//ftpsRequest.enableSessionReuse(true);
		ftpsRequest.connect();
		ftpsRequest.login();

		//ftpsRequest.enableDataChannelEncryption();
		if (getProperties() != null) {

			ftpsRequest.setBinary(getProperties().isBinary());
			ftpsRequest.setPassive(getProperties().isPassive());
		}
		String path = getPayloadURI();
		if (MailBoxUtil.isEmpty(path)) {
			LOGGER.info("The given payload URI is Empty.");
			throw new MailBoxServicesException("The given payload configuration is Empty.", Response.Status.CONFLICT);
		}

		String remotePath = getWriteResponseURI();
		if (MailBoxUtil.isEmpty(remotePath)) {
			LOGGER.info("The given remote URI is Empty.");
			throw new MailBoxServicesException("The given remote configuration is Empty.", Response.Status.CONFLICT);
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

		File localDir = new File(localParentDir);
		File[] subFiles = localDir.listFiles();
		// variable to hold the status of file upload request execution
		int replyCode = 0;

		Date lastCheckTime = new Date();
		String constantInterval = MailBoxUtil.getEnvironmentProperties().getString("check.for.interrupt.signal.frequency.in.sec");

		FSMEventDAOBase eventDAO = new FSMEventDAOBase();

		if (subFiles != null && subFiles.length > 0) {
			for (File item : subFiles) {

				//interrupt signal check
				if(((new Date().getTime() - lastCheckTime.getTime())/1000) > Long.parseLong(constantInterval)) {
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
				if (item.isFile()) {
				    // upload file
				    try (InputStream inputStream = new FileInputStream(item)) {
				        ftpsRequest.changeDirectory(remoteParentDir);
	                    replyCode = ftpsRequest.putFile(item.getName(), inputStream);
				    }

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
						String processedFileLocation = replaceTokensInFolderPath(getCustomProperties().getProperty(
								MailBoxConstants.PROCESSED_FILE_LOCATION));
						if (MailBoxUtil.isEmpty(processedFileLocation)) {
							archiveFile(item.getAbsolutePath(), false);
						} else {
							archiveFile(item, processedFileLocation);
						}
					} else {
						// File uploading failed so move the file to error folder
						String errorFileLocation = replaceTokensInFolderPath(getCustomProperties().getProperty(
								MailBoxConstants.ERROR_FILE_LOCATION));
						if (MailBoxUtil.isEmpty(errorFileLocation)) {
							archiveFile(item.getAbsolutePath(), true);
						} else {
							archiveFile(item, errorFileLocation);
						}
					}

				}
			}
		} else {
			LOGGER.info("The given payload URI'" + localDir + "' does not exist.");
			throw new MailBoxServicesException("The given payload configuration '" + localDir + "' does not exist.", Response.Status.CONFLICT);
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

		G2FTPSClient ftpsRequest = (G2FTPSClient) getClient();

		boolean isFileExists = false;

		//ftpsRequest.enableSessionReuse(true);
		ftpsRequest.connect();
		ftpsRequest.login();

		//ftpsRequest.enableDataChannelEncryption();
		if (getProperties() != null) {

			ftpsRequest.setBinary(getProperties().isBinary());
			ftpsRequest.setPassive(getProperties().isPassive());
		}

		String remotePath = getWriteResponseURI();
		if (MailBoxUtil.isEmpty(remotePath)) {
			LOGGER.info("The given remote URI is Empty.");
			throw new MailBoxServicesException("The given remote configuration is Empty.", Response.Status.CONFLICT);
		}

		boolean dirExists = ftpsRequest.getNative().changeWorkingDirectory(remotePath);
		if (dirExists) {
			ftpsRequest.changeDirectory(remotePath);
			List <String> files = ftpsRequest.listFiles();
			isFileExists = (null !=  files && !files.isEmpty());
		}
		ftpsRequest.disconnect();
		return isFileExists;
		}

	@Override
	public Object getClient() throws Exception {

		// Convert the json string to DTO
		RemoteProcessorPropertiesDTO properties = MailBoxUtil
				.unmarshalFromJSON(configurationInstance.getProcsrProperties(),
						RemoteProcessorPropertiesDTO.class);

		G2FTPSClient ftpsRequest = new G2FTPSClient();
		ftpsRequest.setURI(properties.getUrl());
		ftpsRequest.setDiagnosticLogger(LOGGER);
		ftpsRequest.setCommandLogger(LOGGER);
		ftpsRequest.setConnectionTimeout(properties.getConnectionTimeout());

		ftpsRequest.setSocketTimeout(properties.getSocketTimeout());
		ftpsRequest.setRetryCount(properties.getRetryAttempts());

		Credential loginCredential = getCredentialOfSpecificType(CredentialType.LOGIN_CREDENTIAL);

		/*
		 * For FTPS, SFTP, and FTP processors credential password will be
		 * getting from KM
		 */
		if ((loginCredential != null)) {

			String passwordFromKMS = KMSUtil.getSecretFromKMS(loginCredential.getCredsPassword());

			if (!MailBoxUtil.isEmpty(loginCredential.getCredsUsername())
					&& !MailBoxUtil.isEmpty(passwordFromKMS)) {
				ftpsRequest.setUser(loginCredential.getCredsUsername());
				ftpsRequest.setPassword(passwordFromKMS);
			}
		}

		// Configure keystore for HTTPS request
		if (configurationInstance.getProcsrProtocol().equalsIgnoreCase("ftps")) {

			KeyStore trustStore = KeyStore.getInstance(KeyStore
					.getDefaultType());
			Credential trustStoreCredential = getCredentialOfSpecificType(CredentialType.TRUSTSTORE_CERT);
			if (trustStoreCredential != null) {
				// If no certificate is configured then use default global
				// trustoreid
				String trustStoreID = (MailBoxUtil.isEmpty(trustStoreCredential
						.getCredsIdpUri())) ? (MailBoxUtil
						.getEnvironmentProperties()
						.getString("mailbox.global.trustgroup.id"))
						: trustStoreCredential.getCredsIdpUri();
				InputStream instream = KMSUtil.fetchTrustStore(trustStoreID);

				if (instream == null) {
					throw new MailBoxServicesException(Messages.CERTIFICATE_RETRIEVE_FAILED, Response.Status.BAD_REQUEST);
				}

				try {

					trustStore.load(instream, null);

				} finally {
				    try {
                        if (null != instream) {
                            instream.close();
                        }
                    } catch (IOException e) {
                        LOGGER.error("Cannot close stream while fetching trustore from key manager.");
                    }
				}

				ftpsRequest.setTrustStore(trustStore);
			}

		}
		return ftpsRequest;
	}

}
