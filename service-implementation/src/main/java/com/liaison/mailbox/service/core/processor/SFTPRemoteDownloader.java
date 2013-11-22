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
import java.util.List;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonParseException;
import com.jcraft.jsch.SftpException;
import com.liaison.commons.exceptions.LiaisonException;
import com.liaison.commons.security.pkcs7.SymmetricAlgorithmException;
import com.liaison.commons.util.client.sftp.G2SFTPClient;
import com.liaison.fs2.api.FS2Exception;
import com.liaison.mailbox.enums.ExecutionStatus;
import com.liaison.mailbox.jpa.model.Processor;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.util.MailBoxUtility;

/**
 * SFTP remote downloader to perform pull operation, also it has support methods
 * for JavaScript.
 * 
 * @author praveenu
 */
public class SFTPRemoteDownloader extends AbstractRemoteProcessor implements
		MailBoxProcessor {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(SFTPRemoteDownloader.class);

	@SuppressWarnings("unused")
	private SFTPRemoteDownloader() {
	}

	public SFTPRemoteDownloader(Processor processor) {
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
	 * @throws MailBoxConfigurationServicesException
	 * 
	 */
	@Override
	public G2SFTPClient getClientWithInjectedConfiguration()
			throws LiaisonException, IOException, JAXBException,
			URISyntaxException, MailBoxServicesException, JsonParseException,
			SymmetricAlgorithmException {

		// Convert the json string to DTO
		G2SFTPClient sftpRequest = getSFTPClient(LOGGER);

		return sftpRequest;

	}

	/**
	 * Java method to execute the SFTPrequest
	 * 
	 * @throws IOException
	 * @throws LiaisonException
	 * @throws JAXBException
	 * @throws SymmetricAlgorithmException
	 * @throws SftpException
	 * 
	 * @throws MailBoxConfigurationServicesException
	 * 
	 */
	private void executeSFTPRequest() throws LiaisonException, IOException,
			JAXBException, URISyntaxException, FS2Exception,
			MailBoxServicesException, SymmetricAlgorithmException,
			SftpException {

		G2SFTPClient sftpRequest = getClientWithInjectedConfiguration();
		sftpRequest.connect();

		if (sftpRequest.openChannel()) {

			String path = getPayloadURI();
			downloadDirectory(sftpRequest, path, getWriteResponseURI());
		}
		sftpRequest.disconnect();
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
	 * 
	 */
	public void downloadDirectory(G2SFTPClient sftpRequest, String currentDir,
			String loadDir) throws IOException, LiaisonException,
			URISyntaxException, FS2Exception, MailBoxServicesException,
			SftpException {

		String dirToList = "";
		if (!currentDir.equals("")) {
			dirToList += currentDir;
		}

		List<String> files = sftpRequest.listFiles(currentDir);

		if (files != null && files.size() > 0) {

			for (String aFile : files) {
				File root = new File(aFile);
				boolean attrs = sftpRequest.getNative()
						.stat(dirToList + "/" + aFile).isDir();
				String currentFileName = root.getName();
				if (currentFileName.equals(".") || currentFileName.equals("..")) {
					// skip parent directory and the directory itself
					continue;
				}

				if (attrs) {

					String remotePath = dirToList + "/" + currentFileName;
					String localDir = loadDir + "/" + currentFileName;
					File directory = new File(localDir);
					if (!directory.exists()) {
						Files.createDirectory(directory.toPath());
					}
					downloadDirectory(sftpRequest, remotePath, localDir);

				} else {
					String remotePath = dirToList + "/" + currentFileName;
					ByteArrayOutputStream stream = new ByteArrayOutputStream();
					sftpRequest.getFile(remotePath, stream);
					writeSFTPSResponseToMailBox(stream, loadDir + "/"
							+ currentFileName);
				}
			}
		}
	}

	@Override
	public void invoke() {

		try {

			LOGGER.info("Entering in invoke.");
			// G2SFTP executed through JavaScript
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
				// G2SFTP executed through Java
				executeSFTPRequest();
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
}
