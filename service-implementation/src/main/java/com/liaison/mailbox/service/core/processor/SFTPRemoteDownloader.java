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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.codehaus.jettison.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonParseException;
import com.jcraft.jsch.SftpException;
import com.liaison.commons.exceptions.LiaisonException;
import com.liaison.commons.security.pkcs7.SymmetricAlgorithmException;
import com.liaison.commons.util.client.sftp.G2SFTPClient;
import com.liaison.fs2.api.FS2Exception;
import com.liaison.mailbox.enums.ExecutionEvents;
import com.liaison.mailbox.jpa.model.Processor;
import com.liaison.mailbox.service.core.fsm.MailboxFSM;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.util.JavaScriptEngineUtil;
import com.liaison.mailbox.service.util.MailBoxUtility;

/**
 * SFTP remote downloader to perform pull operation, also it has support methods
 * for JavaScript.
 * 
 * @author praveenu
 */
public class SFTPRemoteDownloader extends AbstractRemoteProcessor implements MailBoxProcessor {

	private static final Logger LOGGER = LoggerFactory.getLogger(SFTPRemoteDownloader.class);

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
	 * @throws com.liaison.commons.exception.LiaisonException 
	 * @throws JSONException 
	 * @throws MailBoxConfigurationServicesException
	 * 
	 */
	@Override
	public G2SFTPClient getClientWithInjectedConfiguration() throws LiaisonException, IOException, JAXBException,
			URISyntaxException, MailBoxServicesException, JsonParseException, SymmetricAlgorithmException, com.liaison.commons.exception.LiaisonException, JSONException {

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
	 * @throws com.liaison.commons.exception.LiaisonException 
	 * @throws JSONException 
	 * @throws JsonParseException 
	 * 
	 * @throws MailBoxConfigurationServicesException
	 * 
	 */
	private void executeSFTPRequest() throws LiaisonException, IOException, JAXBException, URISyntaxException,
			FS2Exception, MailBoxServicesException, SymmetricAlgorithmException, SftpException, com.liaison.commons.exception.LiaisonException, JsonParseException, JSONException {

		G2SFTPClient sftpRequest = getClientWithInjectedConfiguration();
		sftpRequest.connect();

		if (sftpRequest.openChannel()) {

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

			downloadDirectory(sftpRequest, path, remotePath);
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
	 * @throws com.liaison.commons.exception.LiaisonException 
	 * 
	 */
	public void downloadDirectory(G2SFTPClient sftpRequest, String currentDir, String localFileDir) throws IOException,
			LiaisonException, URISyntaxException, FS2Exception, MailBoxServicesException, SftpException, com.liaison.commons.exception.LiaisonException {

		String dirToList = "";
		if (!currentDir.equals("")) {
			dirToList += currentDir;
		}

		List<String> files = sftpRequest.listFiles(currentDir);

		if (files != null && files.size() > 0) {

			for (String aFile : files) {

				File root = new File(aFile);
				if (root.getName().equals(".") || root.getName().equals("..")) {
					// skip parent directory and the directory itself
					continue;
				}
				boolean isDir = sftpRequest.getNative().stat(dirToList + File.separatorChar + aFile).isDir();
				
				
				if (isDir) {

					String localDir = localFileDir + File.separatorChar + root.getName();
					String remotePath = dirToList + File.separatorChar + root.getName();
					File directory = new File(localDir);
					if (!directory.exists()) {
						Files.createDirectories(directory.toPath());
					}
					sftpRequest.changeDirectory(remotePath);
					downloadDirectory(sftpRequest, remotePath, localDir);

				} else {

					// String remotePath = dirToList + "/" + root.getName();
					String localDir = localFileDir + File.separatorChar + root.getName();
					sftpRequest.changeDirectory(dirToList);
					processResponseLocation(localDir);
					sftpRequest.getFile(root.getName(), new BufferedOutputStream(new FileOutputStream(localDir)));
				}
			}
		}
	}

	@Override
	public void invoke(String executionId,MailboxFSM fsm) throws Exception {
		
		LOGGER.info("Entering in invoke.");
		// G2SFTP executed through JavaScript
		if (!MailBoxUtility.isEmpty(configurationInstance.getJavaScriptUri())) {

			fsm.handleEvent(fsm.createEvent(ExecutionEvents.PROCESSOR_EXECUTION_HANDED_OVER_TO_JS));
			
			// Use custom G2JavascriptEngine
			JavaScriptEngineUtil.executeJavaScript(configurationInstance.getJavaScriptUri(), "init", this,LOGGER);

		} else {
			// G2SFTP executed through Java
			executeSFTPRequest();
		}
	}
}
