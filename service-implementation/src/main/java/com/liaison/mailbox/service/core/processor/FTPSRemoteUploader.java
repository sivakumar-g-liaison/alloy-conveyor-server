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
 * 
 ** FTPS remote uploader to perform pull operation, also it has support methods
 * for JavaScript.
 * @author praveenu
 * 
 */
public class FTPSRemoteUploader extends AbstractRemoteProcessor implements MailBoxProcessor {

	private static final Logger LOGGER = LoggerFactory.getLogger(FTPSRemoteUploader.class);

	@SuppressWarnings("unused")
	private FTPSRemoteUploader() {
	}

	public FTPSRemoteUploader(Processor processor) {
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
	 * Java method to inject the G2FTPS configurations
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
	 * Java method to execute the SFTPrequest to upload the file or folder
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
			
			uploadDirectory(ftpsRequest,path,getWriteResponseURI());
		}else{
			
			InputStream inputStream = new FileInputStream(root);
			ftpsRequest.putFile(root.getName(), inputStream);
		}
		
		ftpsRequest.disconnect();
	}
	
	/**
	 * Java method to upload the file or folder
	 * 
	 * @throws IOException
	 * @throws LiaisonException
	 * @throws SftpException 
	 * 
	 */
	public static void uploadDirectory(G2FTPSClient ftpsRequest, String localParentDir, String remoteParentDir)
			throws IOException, LiaisonException {

		File localDir = new File(localParentDir);
		File[] subFiles = localDir.listFiles();
		if (subFiles != null && subFiles.length > 0) {
			for (File item : subFiles) {
				String remoteFilePath = remoteParentDir
						+ "/" + item.getName();
				if (remoteParentDir.equals("")) {
					remoteFilePath = item.getName();
				}

				if (item.isFile()) {
					// upload the file
					String localFilePath = item.getAbsolutePath();
					File localFile = new File(localFilePath);
					InputStream inputStream = new FileInputStream(localFile);
					ftpsRequest.putFile(new File(remoteFilePath).getName(), inputStream);
					
				} else {
					// create directory on the server
					ftpsRequest.getNative().makeDirectory(remoteFilePath);
					// upload the sub directory
					String parent = remoteParentDir + "/" + item.getName();
					if (remoteParentDir.equals("")) {
						parent = item.getName();
					}

					localParentDir = item.getAbsolutePath();
					uploadDirectory(ftpsRequest, localParentDir,
							parent);
				}
			}
		}
	}
}
