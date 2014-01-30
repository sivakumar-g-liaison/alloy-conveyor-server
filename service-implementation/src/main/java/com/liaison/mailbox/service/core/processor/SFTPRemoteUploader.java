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

import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonParseException;
import com.jcraft.jsch.SftpException;
import com.liaison.commons.exceptions.LiaisonException;
import com.liaison.commons.security.pkcs7.SymmetricAlgorithmException;
import com.liaison.commons.util.client.sftp.G2SFTPClient;
import com.liaison.fs2.api.FS2Exception;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.jpa.model.Processor;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.util.JavaScriptEngineUtil;
import com.liaison.mailbox.service.util.MailBoxUtility;

/**
 * SFTP remote uploader to perform push operation, also it has support methods
 * for JavaScript.
 * 
 * @author praveenu
 */
public class SFTPRemoteUploader extends AbstractRemoteProcessor implements MailBoxProcessor {

	private static final Logger LOGGER = LoggerFactory.getLogger(SFTPRemoteUploader.class);

	@SuppressWarnings("unused")
	private SFTPRemoteUploader() {
	}

	public SFTPRemoteUploader(Processor processor) {
		super(processor);
	}

	/**
	 * Java method to inject the G2SFTP configurations
	 * 
	 * @throws IOException
	 * @throws LiaisonException
	 * @throws JAXBException
	 * @throws MailBoxServicesException
	 * @throws URISyntaxException
	 * @throws SymmetricAlgorithmException
	 * @throws JsonParseException
	 * @throws com.liaison.commons.exception.LiaisonException 
	 * @throws MailBoxConfigurationServicesException
	 * 
	 */
	@Override
	public G2SFTPClient getClientWithInjectedConfiguration() throws LiaisonException, IOException, JAXBException,
			URISyntaxException, MailBoxServicesException, JsonParseException, SymmetricAlgorithmException, com.liaison.commons.exception.LiaisonException {

		G2SFTPClient sftpRequest = getSFTPClient(LOGGER);

		return sftpRequest;

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
	 * 
	 */
	private void executeRequest() throws LiaisonException, IOException, JAXBException, URISyntaxException,
			FS2Exception, MailBoxServicesException, SftpException, SymmetricAlgorithmException, com.liaison.commons.exception.LiaisonException {

		G2SFTPClient sftpRequest = getClientWithInjectedConfiguration();
		sftpRequest.connect();

		String path = getPayloadURI();

		if (MailBoxUtility.isEmpty(path)) {
			LOGGER.info("The given URI {} does not exist.", path);
			throw new MailBoxServicesException("The given URI '" + path + "' does not exist.");
		}

		if (sftpRequest.openChannel()) {

			String remotePath = getWriteResponseURI();
			if (MailBoxUtility.isEmpty(remotePath)) {
				LOGGER.info("The given remote URI {} does not exist.", remotePath);
				throw new MailBoxServicesException("The given remote URI '" + remotePath + "' does not exist.");
			}
			Boolean dirExists = true;
			try {
				sftpRequest.getNative().lstat(remotePath);
			} catch (Exception ex) {
				dirExists = false;
			}
			if (!dirExists) {
				// create directory on the server
				sftpRequest.getNative().mkdir(new File(remotePath).getName());
			}

			sftpRequest.changeDirectory(remotePath);
			uploadDirectory(sftpRequest, path, remotePath);

		}
		sftpRequest.disconnect();

	}

	/**
	 * Java method to upload the file or folder
	 * 
	 * @throws IOException
	 * @throws LiaisonException
	 * @throws SftpException
	 * @throws MailBoxServicesException
	 * @throws com.liaison.commons.exception.LiaisonException 
	 * 
	 */
	public void uploadDirectory(G2SFTPClient sftpRequest, String localParentDir, String remoteParentDir)
			throws IOException, LiaisonException, SftpException, MailBoxServicesException, com.liaison.commons.exception.LiaisonException {

		File localDir = new File(localParentDir);
		File[] subFiles = localDir.listFiles();
		// variable to hold the status of file upload request execution
		int replyCode = 0;
		if (subFiles != null && subFiles.length > 0) {
			for (File item : subFiles) {

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
						sftpRequest.getNative().mkdir(new File(remoteFilePath).getName());
					}
					// upload the sub directory
					sftpRequest.changeDirectory(remoteFilePath);
					localParentDir = item.getAbsolutePath();
					uploadDirectory(sftpRequest, localParentDir, remoteFilePath);

				} else {

					// upload the file
					sftpRequest.changeDirectory(remoteParentDir);
					InputStream inputStream = new FileInputStream(item);
					replyCode = sftpRequest.putFile(item.getName(), inputStream);
					inputStream.close();
				}
				// archiveFile(item.getAbsolutePath());

				if (null != item) {
					
					// File Uploading done successfully so move the file to processed folder
					if (replyCode == 226 || replyCode == 250) {
						
						String processedFileLocation = processMountLocation(getDynamicProperties().getProperty(
								MailBoxConstants.PROCESSED_FILE_LOCATION));
						if (MailBoxUtility.isEmpty(processedFileLocation)) {
							archiveFile(item.getAbsolutePath(), false);
						} else {
							archiveFile(item, processedFileLocation);
						}
					} else {
						
						// File Uploading failed so move the file to error folder
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
		}
	}

	@Override
	public void invoke() throws Exception {
		
		LOGGER.info("Entering in invoke.");
		// SFTPRequest executed through JavaScript
		if (!MailBoxUtility.isEmpty(configurationInstance.getJavaScriptUri())) {

			/*ScriptEngineManager manager = new ScriptEngineManager();
			ScriptEngine engine = manager.getEngineByName("JavaScript");

			engine.eval(getJavaScriptString(configurationInstance.getJavaScriptUri()));
			Invocable inv = (Invocable) engine;

			// invoke the method in javascript
			inv.invokeFunction("init", this);*/
			
			// Use custom G2JavascriptEngine
			JavaScriptEngineUtil.executeJavaScript(configurationInstance.getJavaScriptUri(), "init", this);

		} else {
			// SFTPRequest executed through Java
			executeRequest();
		}
	}
}
