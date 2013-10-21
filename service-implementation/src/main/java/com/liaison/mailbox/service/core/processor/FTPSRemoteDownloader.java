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

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.xml.bind.JAXBException;

import org.apache.commons.net.ftp.FTPFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.SftpException;
import com.liaison.commons.exceptions.LiaisonException;
import com.liaison.commons.util.client.ftps.G2FTPSClient;
import com.liaison.fs2.api.FS2Exception;
import com.liaison.mailbox.jpa.model.Processor;
import com.liaison.mailbox.service.dto.configuration.request.RemoteProcessorPropertiesDTO;
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
			// HTTPRequest executed through JavaScript
			if (!MailBoxUtility.isEmpty(configurationInstance.getJavaScriptUri())) {

				ScriptEngineManager manager = new ScriptEngineManager();
				ScriptEngine engine = manager.getEngineByName("JavaScript");

				engine.eval(getJavaScriptString(configurationInstance.getJavaScriptUri()));
				Invocable inv = (Invocable) engine;

				// invoke the method in javascript
				inv.invokeFunction("init", this);

			} else {
				// HTTPRequest executed through Java
				executeRequest();
			}

		} catch (Exception e) {
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
	 * 
	 * @throws MailBoxConfigurationServicesException
	 * 
	 */
	public G2FTPSClient getClientWithInjectedConfiguration() throws LiaisonException, IOException, JAXBException {

		// Convert the json string to DTO
		RemoteProcessorPropertiesDTO properties = MailBoxUtility.unmarshalFromJSON(
				configurationInstance.getProcsrProperties(), RemoteProcessorPropertiesDTO.class);

		G2FTPSClient ftpsRequest = new G2FTPSClient();
		ftpsRequest.setURI(properties.getUrl());
		ftpsRequest.setDiagnosticLogger(LOGGER);
		ftpsRequest.setCommandLogger(LOGGER);
		ftpsRequest.setConnectionTimeout(properties.getConnectionTimeout());
		
		ftpsRequest.setSocketTimeout(properties.getSocketTimeout());
		ftpsRequest.setRetryCount(properties.getRetryAttempts());
		ftpsRequest.setUser("username");
		ftpsRequest.setPassword("xxxxxx");
		ftpsRequest.setTrustManagerKeyStore("E:/keystore.jks");
		ftpsRequest.setTrustManagerKeyStoreType("jks");
		ftpsRequest.setTrustManagerKeyStorePassword("xxxxxx");
		
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
	 * 
	 */
	
	protected void executeRequest() throws MailBoxServicesException, LiaisonException, IOException, FS2Exception,
			URISyntaxException, JAXBException {

		G2FTPSClient ftpsRequest = getClientWithInjectedConfiguration();
		ftpsRequest.connect();
		ftpsRequest.login();
		ftpsRequest.setBinary(false);
		ftpsRequest.setPassive(true);
		
		String path = getPayloadURI();
		File root = new File(path);
		
		if(root.isDirectory()){
			downloadDirectory(ftpsRequest, path);
		}else{
			
			ByteArrayOutputStream response = new ByteArrayOutputStream();
			ftpsRequest.getFile(root.getName(), response);
			writeFileResponseToMailBox(response,"/"+root.getName());
		}
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
	
	public void downloadDirectory(G2FTPSClient ftpClient, String currentDir) 
			throws IOException, LiaisonException, URISyntaxException, FS2Exception, MailBoxServicesException {
		
		String dirToList = "";
		if (!currentDir.equals("")) {
			dirToList += currentDir;
		}

		FTPFile[] subFiles = ftpClient.getNative().listFiles(dirToList);

		if (subFiles != null && subFiles.length > 0) {
			
			for (FTPFile aFile : subFiles) {
				String currentFileName = aFile.getName();
				if (currentFileName.equals(".") || currentFileName.equals("..")) {
					// skip parent directory and the directory itself
					continue;
				}
				String filePath = currentDir + "/" + currentFileName;
				if (currentDir.equals("")) {
					filePath =  currentFileName;
				}

				if (aFile.isDirectory()) {
					downloadDirectory(ftpClient, currentFileName);
				} else {
					// download the file
					LOGGER.info(aFile.getName()+" File started downloading.");
					ByteArrayOutputStream stream = new ByteArrayOutputStream();
					ftpClient.getFile(filePath, stream);
					writeFileResponseToMailBox(stream, "/"+aFile.getName());
					LOGGER.info(aFile.getName()+" File completed downloading.");
				}
			}
		}
	}
	
	
}
