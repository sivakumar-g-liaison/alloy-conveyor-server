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
import java.security.cert.CertificateException;
import java.util.List;

import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;

import org.apache.commons.net.ftp.FTPFile;
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
import com.liaison.mailbox.dtdm.model.Processor;
import com.liaison.mailbox.enums.ExecutionEvents;
import com.liaison.mailbox.service.core.fsm.MailboxFSM;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.util.JavaScriptEngineUtil;
import com.liaison.mailbox.service.util.MailBoxUtil;

/**
 * @author OFS
 *
 */
public class FTPSRemoteDownloader extends AbstractRemoteProcessor implements MailBoxProcessor {

	private static final Logger LOGGER = LogManager.getLogger(FTPSRemoteDownloader.class);

	@SuppressWarnings("unused")
	private FTPSRemoteDownloader() {
	}

	public FTPSRemoteDownloader(Processor processor) {
		super(processor);
	}

	@Override
	public void invoke(String executionId, MailboxFSM fsm) throws Exception {

		LOGGER.debug("Entering in invoke.");
		// FTPSRequest executed through JavaScript
		if (!MailBoxUtil.isEmpty(configurationInstance.getJavaScriptUri())) {
			fsm.handleEvent(fsm.createEvent(ExecutionEvents.PROCESSOR_EXECUTION_HANDED_OVER_TO_JS));
			JavaScriptEngineUtil.executeJavaScript(configurationInstance.getJavaScriptUri(), "init", this, LOGGER);

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
	 * @throws BootstrapingFailedException
	 * @throws OperatorCreationException
	 * @throws CMSException
	 * @throws UnrecoverableKeyException
	 * @throws MailBoxConfigurationServicesException
	 *
	 */
	@Override
	public G2FTPSClient getClientWithInjectedConfiguration() throws LiaisonException, IOException, JAXBException,
			URISyntaxException, MailBoxServicesException, JsonParseException, SymmetricAlgorithmException,
			com.liaison.commons.exception.LiaisonException, NoSuchAlgorithmException, CertificateException,
			KeyStoreException, JSONException, UnrecoverableKeyException, OperatorCreationException, CMSException,
			BootstrapingFailedException {

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
	 * @throws CMSException
	 * @throws OperatorCreationException
	 * @throws UnrecoverableKeyException
	 * @throws BootstrapingFailedException
	 *
	 */
	protected void executeRequest() throws MailBoxServicesException, LiaisonException, IOException, FS2Exception,
			URISyntaxException, JAXBException, SymmetricAlgorithmException, JsonParseException,
			com.liaison.commons.exception.LiaisonException, NoSuchAlgorithmException, CertificateException,
			KeyStoreException, JSONException, UnrecoverableKeyException, OperatorCreationException, CMSException,
			BootstrapingFailedException {

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
		if (MailBoxUtil.isEmpty(path)) {
			LOGGER.info("The given payload URI is Empty.");
			throw new MailBoxServicesException("The given payload configuration is Empty.", Response.Status.CONFLICT);
		}

		String remotePath = getWriteResponseURI();
		if (MailBoxUtil.isEmpty(remotePath)) {
			LOGGER.info("The given remote URI is Empty.");
			throw new MailBoxServicesException("The given remote configuration is Empty.", Response.Status.CONFLICT);
		}

		ftpsRequest.changeDirectory(path);

		// For testing purpose
		LOGGER.debug("The payload location is {}", path);
		LOGGER.debug("The current working directory is {}", ftpsRequest.currentWorkingDirectory());
		List<String> files = ftpsRequest.listFiles();
		for (String file : files) {
			LOGGER.debug("The payload is {}", file);
		}
		if (files.isEmpty()) {
			LOGGER.debug("The payload location({}) is empty", path);
		}
		// For testing purpose

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
			LiaisonException, URISyntaxException, FS2Exception, MailBoxServicesException,
			com.liaison.commons.exception.LiaisonException {

		String dirToList = "";
		if (!currentDir.equals("")) {
			dirToList += currentDir;
		}
		FTPFile[] files = ftpClient.getNative().listFiles(dirToList);
		BufferedOutputStream bos = null;
		FileOutputStream fos = null;

		if (files != null) {

			for (FTPFile file : files) {

				if (file.getName().equals(".") || file.getName().equals("..")) {
					// skip parent directory and the directory itself
					continue;
				}

				String currentFileName = file.getName();
				if (file.isFile()) {

					String localDir = localFileDir + File.separatorChar + currentFileName;
					ftpClient.changeDirectory(dirToList);
					processResponseLocation(localDir);

					try {// GSB-1337,GSB-1336

						fos = new FileOutputStream(localDir);
						bos = new BufferedOutputStream(fos);
						ftpClient.getFile(currentFileName, bos);
					} finally {
					    if (bos != null) bos.close();
					    if (fos != null) fos.close();
					}
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
