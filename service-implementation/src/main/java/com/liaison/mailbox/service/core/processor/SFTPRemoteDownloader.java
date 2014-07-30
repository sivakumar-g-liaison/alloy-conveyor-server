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
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateEncodingException;
import java.util.List;

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
import com.liaison.commons.security.pkcs12.SymmetricAlgorithmException;
import com.liaison.commons.util.client.sftp.G2SFTPClient;
import com.liaison.fs2.api.FS2Exception;
import com.liaison.mailbox.enums.ExecutionEvents;
import com.liaison.mailbox.jpa.model.Processor;
import com.liaison.mailbox.service.core.fsm.MailboxFSM;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.util.JavaScriptEngineUtil;
import com.liaison.mailbox.service.util.MailBoxUtil;

/**
 * SFTP remote downloader to perform pull operation, also it has support methods
 * for JavaScript.
 * 
 * @author OFS
 */
public class SFTPRemoteDownloader extends AbstractRemoteProcessor implements MailBoxProcessor {

	private static final Logger LOGGER = LogManager.getLogger(SFTPRemoteDownloader.class);

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
	 * @throws BootstrapingFailedException 
	 * @throws CMSException 
	 * @throws NoSuchAlgorithmException 
	 * @throws KeyStoreException 
	 * @throws OperatorCreationException 
	 * @throws UnrecoverableKeyException 
	 * @throws CertificateEncodingException 
	 * @throws MailBoxConfigurationServicesException
	 * 
	 */
	@Override
	public G2SFTPClient getClientWithInjectedConfiguration() throws LiaisonException, IOException, JAXBException,
			URISyntaxException, MailBoxServicesException, JsonParseException, SymmetricAlgorithmException, com.liaison.commons.exception.LiaisonException, JSONException, CertificateEncodingException, UnrecoverableKeyException, OperatorCreationException, KeyStoreException, NoSuchAlgorithmException, CMSException, BootstrapingFailedException {

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
	 * @throws BootstrapingFailedException 
	 * @throws CMSException 
	 * @throws NoSuchAlgorithmException 
	 * @throws KeyStoreException 
	 * @throws OperatorCreationException 
	 * @throws UnrecoverableKeyException 
	 * @throws CertificateEncodingException 
	 * 
	 * @throws MailBoxConfigurationServicesException
	 * 
	 */
	private void executeSFTPRequest() throws LiaisonException, IOException, JAXBException, URISyntaxException,
			FS2Exception, MailBoxServicesException, SymmetricAlgorithmException, SftpException, com.liaison.commons.exception.LiaisonException, JsonParseException, JSONException, CertificateEncodingException, UnrecoverableKeyException, OperatorCreationException, KeyStoreException, NoSuchAlgorithmException, CMSException, BootstrapingFailedException {

		G2SFTPClient sftpRequest = getClientWithInjectedConfiguration();
		sftpRequest.connect();

		if (sftpRequest.openChannel()) {
			
			String path = getPayloadURI();
			if (MailBoxUtil.isEmpty(path)) {
				LOGGER.info("The given payload URI is Empty.");
				throw new MailBoxServicesException("The given payload configuration is Empty.");
			}
			String remotePath = getWriteResponseURI();
			if (MailBoxUtil.isEmpty(remotePath)) {
				LOGGER.info("The given remote URI is Empty.");
				throw new MailBoxServicesException("The given remote configuration is Empty.");
			}

			downloadDirectory(sftpRequest, path, remotePath);
		}
		// remove the private key once connection established successfully
		removePrivateKey();
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
		
		LOGGER.debug("Entering in invoke.");
		// G2SFTP executed through JavaScript
		if (!MailBoxUtil.isEmpty(configurationInstance.getJavaScriptUri())) {

			fsm.handleEvent(fsm.createEvent(ExecutionEvents.PROCESSOR_EXECUTION_HANDED_OVER_TO_JS));
			
			// Use custom G2JavascriptEngine
			JavaScriptEngineUtil.executeJavaScript(configurationInstance.getJavaScriptUri(), "init", this,LOGGER);

		} else {
			// G2SFTP executed through Java
			executeSFTPRequest();
		}
	}
}
