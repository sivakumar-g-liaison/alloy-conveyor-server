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
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.xml.bind.JAXBException;

import org.apache.commons.net.ftp.FTPFile;
import org.codehaus.jettison.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonParseException;
import com.jcraft.jsch.SftpException;
import com.liaison.commons.exception.LiaisonException;
import com.liaison.commons.security.pkcs7.SymmetricAlgorithmException;
import com.liaison.commons.util.client.ftps.G2FTPSClient;
import com.liaison.fs2.api.FS2Exception;
import com.liaison.mailbox.enums.ExecutionEvents;
import com.liaison.mailbox.jpa.model.Processor;
import com.liaison.mailbox.service.core.fsm.MailboxFSM;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.util.JavaScriptEngineUtil;
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
	public void invoke(String executionId,MailboxFSM fsm) throws Exception {
		
		LOGGER.info("Entering in invoke.");
		// FTPSRequest executed through JavaScript
		if (!MailBoxUtility.isEmpty(configurationInstance.getJavaScriptUri())) {
			fsm.handleEvent(fsm.createEvent(ExecutionEvents.PROCESSOR_EXECUTION_HANDED_OVER_TO_JS));
			JavaScriptEngineUtil.executeJavaScript(configurationInstance.getJavaScriptUri(), "init", this,LOGGER);

		} else {
			// FTPSRequest executed through Java
			executeRequest();
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
	 * @throws com.liaison.commons.exception.LiaisonException 
	 * @throws JsonParseException 
	 * @throws JSONException 
	 * @throws KeyStoreException 
	 * @throws CertificateException 
	 * @throws NoSuchAlgorithmException 
	 * 
	 */

	protected void executeRequest() throws MailBoxServicesException, LiaisonException, IOException, FS2Exception,
			URISyntaxException, JAXBException, SymmetricAlgorithmException, JsonParseException, com.liaison.commons.exception.LiaisonException, NoSuchAlgorithmException, CertificateException, KeyStoreException, JSONException {

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
	 * @throws com.liaison.commons.exception.LiaisonException 
	 * @throws SftpException
	 * 
	 */

	public void downloadDirectory(G2FTPSClient ftpClient, String currentDir, String localFileDir) throws IOException,
			LiaisonException, URISyntaxException, FS2Exception, MailBoxServicesException, com.liaison.commons.exception.LiaisonException {

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
					ftpClient.changeDirectory(dirToList);
					processResponseLocation(localDir);
					ftpClient.getFile(currentFileName, new BufferedOutputStream(new FileOutputStream(localDir)));
					

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
