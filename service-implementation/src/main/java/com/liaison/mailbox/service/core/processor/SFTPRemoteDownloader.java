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
import java.util.List;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liaison.commons.exceptions.LiaisonException;
import com.liaison.commons.util.client.sftp.G2SFTPClient;
import com.liaison.framework.fs2.api.FS2Exception;
import com.liaison.mailbox.jpa.model.Processor;
import com.liaison.mailbox.service.dto.configuration.request.RemoteProcessorPropertiesDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
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
	 * 
	 * @throws MailBoxConfigurationServicesException
	 * 
	 */
	public G2SFTPClient getClientWithInjectedConfiguration() throws LiaisonException, IOException, JAXBException {

		// Convert the json string to DTO
		RemoteProcessorPropertiesDTO properties = MailBoxUtility.unmarshalFromJSON(
				configurationInstance.getProcsrProperties(), RemoteProcessorPropertiesDTO.class);

		G2SFTPClient sftpRequest = new G2SFTPClient();
		sftpRequest.setURI(properties.getUrl());
		sftpRequest.setDiagnosticLogger(LOGGER);
		sftpRequest.setCommandLogger(LOGGER);
		sftpRequest.setTimeout(properties.getConnectionTimeout());
		sftpRequest.setRetryInterval(properties.getRetryInterval());
		sftpRequest.setRetryCount(properties.getRetryAttempts());
		sftpRequest.setUser("g2testusr");
		sftpRequest.setPassword("mpxEukvePd4V");
		sftpRequest.setStrictHostChecking(false);
		
		return sftpRequest;
		
	}
	
	/**
	 * Java method to execute the SFTPrequest
	 * 
	 * @throws IOException
	 * @throws LiaisonException
	 * @throws JAXBException
	 * 
	 * @throws MailBoxConfigurationServicesException
	 * 
	 */
	private void executeSFTPRequest() throws LiaisonException, IOException, JAXBException, URISyntaxException, FS2Exception, MailBoxServicesException{
		
		G2SFTPClient sftpRequest =getClientWithInjectedConfiguration();
		sftpRequest.connect();
		
		if (sftpRequest.openChannel()) {
			
			//String path = "/home/g2testusr/Directory S/Zippy/Demo.js";
			String path = getPayloadURI();
			File root = new File(path);
			
			if(root.isDirectory()){
				downloadDirectory(sftpRequest, path);
			}else{
				
				ByteArrayOutputStream response = new ByteArrayOutputStream();
				sftpRequest.getFile(path, response);
				writeFileResponseToMailBox(response,"/"+root.getName());
			}
		}
		sftpRequest.disconnect();
	}
	
	/**
	 * Java method to download the folder and its files
	 * 
	 * @throws IOException
	 * @throws LiaisonException
	 * @throws URISyntaxException
	 *  @throws FS2Exception
	 * @throws MailBoxServicesException
	 * 
	 */
	public void downloadDirectory(G2SFTPClient sftpRequest, String currentDir) 
			throws IOException, LiaisonException, URISyntaxException, FS2Exception, MailBoxServicesException {
		
		String dirToList = "";
		if (!currentDir.equals("")) {
			dirToList += currentDir;
		}

		List<String> files = sftpRequest.listFiles(dirToList);

		if (files != null && files.size() > 0) {
			
			for (String aFile : files) {
				File root = new File(aFile);
				String currentFileName = root.getName();
				if (currentFileName.equals(".") || currentFileName.equals("..")) {
					// skip parent directory and the directory itself
					continue;
				}
				String filePath = currentDir + "/" + currentFileName;
				if (currentDir.equals("")) {
					filePath =  currentFileName;
				}

				if (root.isDirectory()) {
					downloadDirectory(sftpRequest, currentFileName);
				} else {
					// download the file
					ByteArrayOutputStream stream = new ByteArrayOutputStream();
					sftpRequest.getFile(filePath, stream);
					
					writeFileResponseToMailBox(stream, "/"+root.getName());
				}
			}
		}
	}
	
	@Override
	public void invoke() {

		try {

			LOGGER.info("Entering in invoke.");
			// G2SFTP executed through JavaScript
			if (!MailBoxUtility.isEmpty(configurationInstance.getJavaScriptUri())) {

				ScriptEngineManager manager = new ScriptEngineManager();
				ScriptEngine engine = manager.getEngineByName("JavaScript");

				engine.eval(getJavaScriptString(configurationInstance.getJavaScriptUri()));
				Invocable inv = (Invocable) engine;

				// invoke the method in javascript
				inv.invokeFunction("init", this);

			} else {
				// G2SFTP executed through Java
				executeSFTPRequest();
			}

		} catch (Exception e) {
			e.printStackTrace();
			// TODO Re stage and update status in FSM
		}
	}

}
