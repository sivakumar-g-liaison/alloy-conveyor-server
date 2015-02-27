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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.dtdm.model.Processor;
import com.liaison.mailbox.enums.ExecutionEvents;
import com.liaison.mailbox.service.core.fsm.MailboxFSM;
import com.liaison.mailbox.service.core.processor.helper.FTPSClient;
import com.liaison.mailbox.service.dto.configuration.processor.properties.FTPDownloaderPropertiesDTO;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.executor.javascript.JavaScriptExecutorUtil;
import com.liaison.mailbox.service.util.MailBoxUtil;
import com.liaison.mailbox.service.util.ProcessorPropertyJsonMapper;

/**
 * @author OFS
 *
 */
public class FTPSRemoteDownloader extends AbstractProcessor implements MailBoxProcessorI {

	private static final Logger LOGGER = LogManager.getLogger(FTPSRemoteDownloader.class);

	@SuppressWarnings("unused")
	private FTPSRemoteDownloader() {
	}

	public FTPSRemoteDownloader(Processor processor) {
		super(processor);
	}

	@Override
	public void invoke(String executionId, MailboxFSM fsm) {

		LOGGER.debug("Entering in invoke.");
	    try {

		// FTPSRequest executed through JavaScript
			if (getProperties().isHandOverExecutionToJavaScript()) {
				fsm.handleEvent(fsm.createEvent(ExecutionEvents.PROCESSOR_EXECUTION_HANDED_OVER_TO_JS));
				JavaScriptExecutorUtil.executeJavaScript(configurationInstance.getJavaScriptUri(), this);

			} else {
				// FTPSRequest executed through Java
				executeRequest();
			}
	   } catch(JAXBException |IOException | IllegalAccessException | NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
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
	protected void executeRequest() {

		try {

			G2FTPSClient ftpsRequest = (G2FTPSClient) getClient();

			ftpsRequest.enableSessionReuse(true);
			ftpsRequest.connect();
			ftpsRequest.login();
			//GMB-345
			//ftpsRequest.enableDataChannelEncryption();
			
			// retrieve required properties
			/*ArrayList<String> propertyNames = new ArrayList<String>();
			propertyNames.add(MailBoxConstants.PROPERTY_BINARY);
			propertyNames.add(MailBoxConstants.PROPERTY_PASSIVE);
			Map<String, String> requiredProperties = ProcessorPropertyJsonMapper.getProcessorProperties(getProperties(), propertyNames);

			boolean binary = Boolean.getBoolean(requiredProperties.get(MailBoxConstants.PROPERTY_BINARY));
			boolean passive = Boolean.getBoolean(requiredProperties.get(MailBoxConstants.PROPERTY_PASSIVE));*/
			
			FTPDownloaderPropertiesDTO ftpDownloaderStaticProperties = (FTPDownloaderPropertiesDTO)getProperties();
			boolean binary = ftpDownloaderStaticProperties.isBinary();
			boolean passive = ftpDownloaderStaticProperties.isPassive();

			if (ftpDownloaderStaticProperties != null) {
				ftpsRequest.setBinary(binary);
				ftpsRequest.setPassive(passive);
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

		} catch (LiaisonException | JAXBException | IOException | MailBoxServicesException 
				| URISyntaxException |IllegalAccessException | NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
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
			LiaisonException, URISyntaxException, MailBoxServicesException {

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
					createResponseDirectory(localDir);

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

	@Override
	public Object getClient() {
		return FTPSClient.getClient(this);
	}

	@Override
	public void downloadDirectory(Object client, String remotePayloadLocation, String localTargetLocation) {
		
		G2FTPSClient ftpClient = (G2FTPSClient)client;
		try {
			downloadDirectory(ftpClient, remotePayloadLocation, localTargetLocation);
		} catch (MailBoxServicesException | IOException | LiaisonException | URISyntaxException e) {
			throw new RuntimeException(e);
		}		
	}

	@Override
	public void uploadDirectory(Object client, String localPayloadLocation, String remoteTargeLocation) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void cleanup() {
		// TODO Auto-generated method stub
		
	}
}
