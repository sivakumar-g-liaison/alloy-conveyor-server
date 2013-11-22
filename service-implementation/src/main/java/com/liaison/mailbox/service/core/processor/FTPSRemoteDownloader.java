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
import com.liaison.mailbox.enums.ExecutionStatus;
import com.liaison.mailbox.jpa.model.Processor;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.util.MailBoxUtility;

/**
 * @author praveenu
 * 
 */
public class FTPSRemoteDownloader extends AbstractRemoteProcessor implements
		MailBoxProcessor {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(FTPSRemoteDownloader.class);

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
			if (!MailBoxUtility.isEmpty(configurationInstance
					.getJavaScriptUri())) {

				ScriptEngineManager manager = new ScriptEngineManager();
				ScriptEngine engine = manager.getEngineByName("JavaScript");

				engine.eval(getJavaScriptString(configurationInstance
						.getJavaScriptUri()));
				Invocable inv = (Invocable) engine;

				// invoke the method in javascript
				inv.invokeFunction("init", this);

			} else {
				// FTPSRequest executed through Java
				executeRequest();
			}
			modifyProcessorExecutionStatus(ExecutionStatus.COMPLETED);
		} catch (Exception e) {

			modifyProcessorExecutionStatus(ExecutionStatus.FAILED);
			sendEmail(
					null,
					configurationInstance.getProcsrName() + ":"
							+ e.getMessage(), e.getMessage(), "HTML");
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
	public G2FTPSClient getClientWithInjectedConfiguration()
			throws LiaisonException, IOException, JAXBException,
			URISyntaxException, MailBoxServicesException, JsonParseException,
			SymmetricAlgorithmException {

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

	protected void executeRequest() throws MailBoxServicesException,
			LiaisonException, IOException, FS2Exception, URISyntaxException,
			JAXBException, SymmetricAlgorithmException {

		G2FTPSClient ftpsRequest = getClientWithInjectedConfiguration();
		ftpsRequest.connect();
		ftpsRequest.login();
		ftpsRequest.setBinary(false);
		ftpsRequest.setPassive(true);

		String path = getPayloadURI();

		if (MailBoxUtility.isEmpty(path)) {
			LOGGER.info("The given URI {} does not exist.", path);
			throw new MailBoxServicesException("The given URI '" + path
					+ "' does not exist.");
		}
		/*
		 * File root = new File(path);
		 * 
		 * if (root.isDirectory()) { downloadDirectory(ftpsRequest, path); }
		 * else {
		 * 
		 * ByteArrayOutputStream response = new ByteArrayOutputStream();
		 * ftpsRequest.getFile(root.getName(), response);
		 * writeFileResponseToMailBox(response, "/" + root.getName()); }
		 */

		downloadDirectory(ftpsRequest, path, getWriteResponseURI());
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

	public void downloadDirectory(G2FTPSClient ftpClient, String currentDir,
			String loadDir) throws IOException, LiaisonException,
			URISyntaxException, FS2Exception, MailBoxServicesException {

		String dirToList = "";
		if (!currentDir.equals("")) {
			dirToList += currentDir;
		}

		FTPFile[] files = ftpClient.getNative().listFiles(dirToList);

		if (files != null) {

			for (FTPFile file : files) {
				String currentFileName = file.getName();
				if (currentFileName.equals(".") || currentFileName.equals("..")) {
					// skip parent directory and the directory itself
					continue;
				}

				if (file.isDirectory()) {

					String remotePath = dirToList + "/" + currentFileName;
					String localDir = loadDir + "/" + currentFileName;
					File directory = new File(localDir);
					if (!directory.exists()) {
						Files.createDirectory(directory.toPath());
					}
					downloadDirectory(ftpClient, remotePath, localDir);

				} else {
					String remotePath = dirToList + "/" + currentFileName;
					ByteArrayOutputStream stream = new ByteArrayOutputStream();
					ftpClient.getFile(remotePath, stream);
					writeSFTPSResponseToMailBox(stream, loadDir + "/"
							+ currentFileName);
				}
			}
		}
	}

}
