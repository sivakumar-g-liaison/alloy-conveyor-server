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
 * SFTP remote uploader to perform push operation, also it has support methods
 * for JavaScript.
 * 
 * @author praveenu
 */
public class SFTPRemoteUploader extends AbstractRemoteProcessor implements
		MailBoxProcessor {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(SFTPRemoteUploader.class);

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
	 * @throws MailBoxConfigurationServicesException
	 * 
	 */
	@Override
	public G2SFTPClient getClientWithInjectedConfiguration()
			throws LiaisonException, IOException, JAXBException,
			URISyntaxException, MailBoxServicesException, JsonParseException,
			SymmetricAlgorithmException {

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
	 * 
	 */
	private void executeRequest() throws LiaisonException, IOException,
			JAXBException, URISyntaxException, FS2Exception,
			MailBoxServicesException, SftpException,
			SymmetricAlgorithmException {

		G2SFTPClient sftpRequest = getClientWithInjectedConfiguration();
		sftpRequest.connect();

		if (sftpRequest.openChannel()) {

			String path = getPayloadURI();
			uploadDirectory(sftpRequest, path, getWriteResponseURI());

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
	 * 
	 */
	public void uploadDirectory(G2SFTPClient sftpRequest,
			String localParentDir, String remoteParentDir) throws IOException,
			LiaisonException, SftpException, MailBoxServicesException {

		File localDir = new File(localParentDir);
		File[] subFiles = localDir.listFiles();
		if (subFiles != null && subFiles.length > 0) {
			for (File item : subFiles) {

				if (item.getName().equals(".") || item.getName().equals("..")) {
					// skip parent directory and the directory itself
					continue;
				}
				if (item.isFile()) {

					String remoteFilePath = remoteParentDir + "/"
							+ item.getName();
					// upload the file
					sftpRequest.changeDirectory(remoteParentDir);
					InputStream inputStream = new FileInputStream(item);
					sftpRequest.putFile(new File(remoteFilePath).getName(),
							inputStream);

				} else {
					String remoteFilePath = remoteParentDir + "/"
							+ item.getName();

					Boolean fileExists = true;
					try {
						sftpRequest.getNative().lstat(remoteFilePath);
					} catch (Exception ex) {
						fileExists = false;
					}

					if (!fileExists) {
						// create directory on the server
						sftpRequest.getNative().mkdir(
								new File(remoteFilePath).getName());
					}
					// upload the sub directory
					String parent = remoteParentDir + "/" + item.getName();
					if (remoteParentDir.equals("")) {
						parent = item.getName();
					}
					sftpRequest.changeDirectory(parent);
					localParentDir = item.getAbsolutePath();

					uploadDirectory(sftpRequest, localParentDir, parent);
				}

			}
		}
	}

	@Override
	public void invoke() {

		try {

			LOGGER.info("Entering in invoke.");
			// SFTPRequest executed through JavaScript
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
				// SFTPRequest executed through Java
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
}
