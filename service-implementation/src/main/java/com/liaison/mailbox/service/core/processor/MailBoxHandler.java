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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liaison.commons.util.client.http.HTTPRequest;
import com.liaison.framework.fs2.api.FS2Exception;
import com.liaison.framework.fs2.api.FS2Factory;
import com.liaison.framework.fs2.api.FS2MetaSnapshot;
import com.liaison.framework.fs2.api.FlexibleStorageSystem;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.jpa.model.Folder;
import com.liaison.mailbox.jpa.model.Processor;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.util.MailBoxUtility;

/**
 * @author praveenu
 * 
 */
public abstract class MailBoxHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(MailBoxHandler.class);

	protected Processor configurationInstance;

	public MailBoxHandler() {
	}

	public MailBoxHandler(Processor configurationInstance) {
		this.configurationInstance = configurationInstance;
	}

	/**
	 * This will return a HTTP ,FTP,HTTPS or FTPS client based on the processor
	 * type.
	 * 
	 * @return
	 */
	public Object getClient() {

		switch (configurationInstance.getProcessorType()) {

			case MailBoxConstants.REMOTE_DOWNLOADER:
				return new HTTPRequest(null, LOGGER);
			case MailBoxConstants.REMOTE_UPLOADER:
				return new HTTPRequest(null, LOGGER);
			default:
				return null;
		}

	}

	/**
	 * To Retrive the Payload file from the URI
	 * 
	 * @return List of files
	 * @throws MailBoxConfigurationServicesException
	 */
	public List<File> getPayload() throws MailBoxConfigurationServicesException {

		if (configurationInstance.getFolders() != null) {

			for (Folder folder : configurationInstance.getFolders()) {

				if (MailBoxUtility.isEmpty(folder.getFldrType()) || MailBoxUtility.isEmpty(folder.getFldrUri())) {

					throw new MailBoxConfigurationServicesException("Payload file location unavailable");

				} else if (folder.getFldrType().equalsIgnoreCase("INPUT")) {

					LOGGER.info("Started receving the payload files");

					List<File> payloadFiles = new ArrayList<File>();
					File[] files = new File(folder.getFldrUri()).listFiles();

					for (File file : files) {
						if (file.isFile()) {
							payloadFiles.add(file);
						}
					}
					LOGGER.info("Payload files received successfully");

					return payloadFiles;
				}
			}
		}
		return null;
	}

	/**
	 * Get HTTPRequest configurations from mailbox processor.
	 * 
	 * @return JSON String processor properties
	 */
	public String getClientConfiguration() {

		LOGGER.info("Returns HTTP/S configurations to HTTPClient");
		return configurationInstance.getProcsrProperties();
	}

	/**
	 * Get HTTPRequest with injected configurations.
	 * 
	 * @return configured HTTPRequest
	 */
	public abstract Object getClientWithInjectedConfiguration();

	/**
	 * Get the URI to which the response should be written, this can be used if
	 * the JS decides to write the response straight to the file system or
	 * database
	 * 
	 * @return URI
	 * @throws MailBoxConfigurationServicesException
	 */
	public String getWriteResponseURI() throws MailBoxConfigurationServicesException {

		if (configurationInstance.getFolders() != null) {

			for (Folder folder : configurationInstance.getFolders()) {

				if (MailBoxUtility.isEmpty(folder.getFldrType()) || MailBoxUtility.isEmpty(folder.getFldrUri())) {

					throw new MailBoxConfigurationServicesException("File location unavailable");

				} else if (folder.getFldrType().equalsIgnoreCase("OUTPUT")) {
					return folder.getFldrUri();
				}
			}
		}
		return null;
	}

	/**
	 * call back method to write the response back to MailBox from JS
	 * 
	 */
	public void writeResponseToMailBox(ByteArrayOutputStream response) throws MailBoxConfigurationServicesException {

		try {
			LOGGER.info("Started writing response");

			FlexibleStorageSystem FS2 = FS2Factory.newInstance(new RemoteProcessorFS2Configuration());
			URI fileLoc = new URI("fs2:" + getWriteResponseURI());
			FS2MetaSnapshot metaSnapShot = FS2.createObjectEntry(fileLoc);
			FS2.writePayloadFromBytes(metaSnapShot.getURI(), response.toByteArray());

			LOGGER.info("Reponse is succefully written" + metaSnapShot.getURI());

		} catch (URISyntaxException | FS2Exception | IOException e) {
			throw new MailBoxConfigurationServicesException("Failure is writing the response" + e.getLocalizedMessage());
		}
	}

	/**
	 * Get the list of dynamic properties of the MailBox known only to java
	 * script
	 * 
	 * @return MailBox dynamic properties
	 */
	public Object getDynamicProperties() {

		// TODO
		return configurationInstance.getProcessorProperties();
	}

	public Object getPassword() {
		// TODO
		return null;
	}

	public Object getCertificate() {
		// TODO
		return null;
	}
}
