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
import com.liaison.commons.util.client.sftp.G2SFTPClient;
import com.liaison.framework.util.ServiceUtils;
import com.liaison.fs2.api.FS2Exception;
import com.liaison.mailbox.jpa.model.Processor;
import com.liaison.mailbox.service.dto.configuration.request.RemoteProcessorPropertiesDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.util.MailBoxUtility;

/**
 * SFTP remote uploader to perform push operation, also it has support methods for JavaScript.
 * 
 * @author praveenu
 */
public class SFTPRemoteUploader extends AbstractRemoteProcessor implements MailBoxProcessor {

	private static final Logger LOGGER = LoggerFactory.getLogger(SFTPRemoteUploader.class);

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
	 * @throws MailBoxConfigurationServicesException
	 * 
	 */
	public G2SFTPClient getClientWithInjectedConfiguration() throws LiaisonException, IOException, JAXBException {

		String json = ServiceUtils.readFileFromClassPath("requests/processor/sftp.json");
		// Convert the json string to DTO
		RemoteProcessorPropertiesDTO properties = MailBoxUtility.unmarshalFromJSON(
				json, RemoteProcessorPropertiesDTO.class);

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
		//sftpRequest.setKnownHosts("C:/Documents and Settings/praveenu/.ssh/known_hosts");
		
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
	 * 
	 */
	private void executeRequest() throws LiaisonException, IOException, JAXBException, URISyntaxException, 
									FS2Exception, MailBoxServicesException, SftpException{
		
		G2SFTPClient sftpRequest =getClientWithInjectedConfiguration();
		sftpRequest.connect();
		
		if (sftpRequest.openChannel()) {
		
			sftpRequest.changeDirectory(getWriteResponseURI());
			String path = getPayloadURI();
			File root = new File(path);
			
			if(root.isDirectory()){
				uploadDirectory(sftpRequest,path,"");
			}else{
				InputStream inputStream = new FileInputStream(root);
				sftpRequest.putFile(root.getPath(), inputStream);
			}
		}
		sftpRequest.disconnect();
	}
	
	/**
	 * Java method to upload the file or folder
	 * 
	 * @throws IOException
	 * @throws LiaisonException
	 * @throws SftpException 
	 * 
	 */
	public static void uploadDirectory(G2SFTPClient sftpRequest, String localParentDir, String remoteParentDir)
			throws IOException, LiaisonException, SftpException {

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
					sftpRequest.putFile(new File(remoteFilePath).getPath(), inputStream);
					
				} else {
					// create directory on the server
					sftpRequest.getNative().mkdir(new File(remoteFilePath).getPath());
					// upload the sub directory
					String parent = remoteParentDir + "/" + item.getName();
					if (remoteParentDir.equals("")) {
						parent = item.getName();
					}

					localParentDir = item.getAbsolutePath();
					uploadDirectory(sftpRequest, localParentDir,
							parent);
				}
			}
		}
	}

	@Override
	public void invoke() {

		try {

			LOGGER.info("Entering in invoke.");
			// SFTPRequest executed through JavaScript
			if (!MailBoxUtility.isEmpty(configurationInstance.getJavaScriptUri())) {

				ScriptEngineManager manager = new ScriptEngineManager();
				ScriptEngine engine = manager.getEngineByName("JavaScript");

				engine.eval(getJavaScriptString(configurationInstance.getJavaScriptUri()));
				Invocable inv = (Invocable) engine;

				// invoke the method in javascript
				inv.invokeFunction("init", this);

			} else {
				// SFTPRequest executed through Java
				executeRequest();
			}

		} catch (Exception e) {
			e.printStackTrace();
			// TODO Re stage and update status in FSM
		}
	}
}
