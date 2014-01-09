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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.xml.bind.JAXBException;

import org.apache.commons.net.ftp.FTPFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonParseException;
import com.jcraft.jsch.SftpException;
import com.liaison.commons.exceptions.LiaisonException;
import com.liaison.commons.security.pkcs7.SymmetricAlgorithmException;
import com.liaison.commons.util.client.ftps.G2FTPSClient;
import com.liaison.fs2.api.FS2Exception;
import com.liaison.mailbox.enums.ExecutionState;
import com.liaison.mailbox.jpa.model.Processor;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.util.MailBoxUtility;

/**
 * @author praveenu
 * 
 */
public class FTPSRemoteDownloader extends AbstractRemoteProcessor implements MailBoxProcessor {

	private static final Logger LOGGER = LoggerFactory.getLogger(FTPSRemoteDownloader.class);

	@SuppressWarnings("unused")
	private FTPSRemoteDownloader() {
	}

	public FTPSRemoteDownloader(Processor processor) {
		super(processor);
	}

	@Override
	public void invoke() {

		try {

			LOGGER.info("Entering in invoke.");
			// FTPSRequest executed through JavaScript
			if (!MailBoxUtility.isEmpty(configurationInstance.getJavaScriptUri())) {

				ScriptEngineManager manager = new ScriptEngineManager();
				ScriptEngine engine = manager.getEngineByName("JavaScript");

				engine.eval(getJavaScriptString(configurationInstance.getJavaScriptUri()));
				Invocable inv = (Invocable) engine;

				// invoke the method in javascript
				inv.invokeFunction("init", this);

			} else {
				// FTPSRequest executed through Java
				executeRequest();
			}
			modifyProcessorExecutionStatus(ExecutionState.COMPLETED);
		} catch (Exception e) {

			modifyProcessorExecutionStatus(ExecutionState.FAILED);
			sendEmail(null, configurationInstance.getProcsrName() + ":" + e.getMessage(), e, "HTML");
			e.printStackTrace();
			// TODO Re stage and update status in FSM
		}
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
	 * @throws MailBoxConfigurationServicesException
	 * 
	 */
	@Override
	public G2FTPSClient getClientWithInjectedConfiguration() throws LiaisonException, IOException, JAXBException,
			URISyntaxException, MailBoxServicesException, JsonParseException, SymmetricAlgorithmException {

		G2FTPSClient ftpsRequest = getFTPSClient(LOGGER);
		return ftpsRequest;

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
	 * 
	 */

	protected void executeRequest() throws MailBoxServicesException, LiaisonException, IOException, FS2Exception,
			URISyntaxException, JAXBException, SymmetricAlgorithmException {

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
			LOGGER.info("The given URI {} does not exist.", path);
			throw new MailBoxServicesException("The given URI '" + path + "' does not exist.");
		}

		String remotePath = getWriteResponseURI();
		if (MailBoxUtility.isEmpty(remotePath)) {
			LOGGER.info("The given remote URI {} does not exist.", remotePath);
			throw new MailBoxServicesException("The given remote URI '" + remotePath + "' does not exist.");
		}

		ftpsRequest.changeDirectory(path);
		downloadDirectory(ftpsRequest, path, remotePath);
		ftpsRequest.disconnect();
	}

	/**
	 * Java method to download the file or folder
	 * 
	 * @throws IOException
	 * @throws LiaisonException
	 * @throws SftpException
	 * 
	 */

	public void downloadDirectory(G2FTPSClient ftpClient, String currentDir, String localFileDir) throws IOException,
			LiaisonException, URISyntaxException, FS2Exception, MailBoxServicesException {

		String dirToList = "";
		if (!currentDir.equals("")) {
			dirToList += currentDir;
		}
		FTPFile[] files = ftpClient.getNative().listFiles(dirToList);

		if (files != null) {

			for (FTPFile file : files) {

				// File file = new File(fileName);

				if (file.getName().equals(".") || file.getName().equals("..")) {
					// skip parent directory and the directory itself
					continue;
				}
				String currentFileName = file.getName();
				if (file.isFile()) {
					// String remotePath = dirToList + "/" + currentFileName;
					String localDir = localFileDir + File.separatorChar + currentFileName;
					ByteArrayOutputStream stream = new ByteArrayOutputStream();
					ftpClient.changeDirectory(dirToList);
					ftpClient.getFile(currentFileName, stream);
					writeSFTPSResponseToMailBox(stream, localDir);

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
}
