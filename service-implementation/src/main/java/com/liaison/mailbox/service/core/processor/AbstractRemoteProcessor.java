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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liaison.commons.util.client.http.HTTPRequest;
import com.liaison.framework.fs2.api.FS2Exception;
import com.liaison.framework.fs2.api.FS2Factory;
import com.liaison.framework.fs2.api.FS2MetaSnapshot;
import com.liaison.framework.fs2.api.FlexibleStorageSystem;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.enums.FolderType;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.jpa.model.Folder;
import com.liaison.mailbox.jpa.model.Processor;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.util.MailBoxUtility;

/**
 * @author praveenu
 * 
 */
public abstract class AbstractRemoteProcessor {

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractRemoteProcessor.class);

	protected Processor configurationInstance;

	public AbstractRemoteProcessor() {
	}

	public AbstractRemoteProcessor(Processor configurationInstance) {
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
	 * @throws MailBoxServicesException
	 */
	public File[] getPayload() throws MailBoxServicesException {

		File[] files = null;

		if (configurationInstance.getFolders() != null) {

			for (Folder folder : configurationInstance.getFolders()) {

				if (MailBoxUtility.isEmpty(folder.getFldrType()) || MailBoxUtility.isEmpty(folder.getFldrUri())) {

					throw new MailBoxServicesException(Messages.FOLDERS_CONFIGURATION_INVALID);
				}

				if (folder.getFldrType().equalsIgnoreCase(FolderType.PAYLOAD_LOCATION.toString())) {
					LOGGER.debug("Started reading the payload files");
					files = new File(folder.getFldrUri()).listFiles();
					LOGGER.debug("Payload files received successfully");
				}
			}
		}
		return files;
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
	 * Get the URI to which the response should be written, this can be used if
	 * the JS decides to write the response straight to the file system or
	 * database
	 * 
	 * @return URI
	 * @throws MailBoxConfigurationServicesException
	 */
	public String getWriteResponseURI() throws MailBoxServicesException {

		if (configurationInstance.getFolders() != null) {

			for (Folder folder : configurationInstance.getFolders()) {

				if (MailBoxUtility.isEmpty(folder.getFldrType()) || MailBoxUtility.isEmpty(folder.getFldrUri())) {

					throw new MailBoxServicesException(Messages.FOLDERS_CONFIGURATION_INVALID);

				} else if (folder.getFldrType().equalsIgnoreCase(FolderType.RESPONSE_LOCATION.toString())) {
					return folder.getFldrUri();
				}
			}
		}
		return null;
	}

	/**
	 * call back method to write the response back to MailBox from JS
	 * 
	 * @throws MailBoxServicesException
	 * @throws URISyntaxException
	 * @throws IOException
	 * @throws FS2Exception
	 * 
	 */
	public void writeResponseToMailBox(ByteArrayOutputStream response) throws URISyntaxException, IOException, FS2Exception,
			MailBoxServicesException {

		LOGGER.info("Started writing response");
		FlexibleStorageSystem FS2 = FS2Factory.newInstance(new RemoteProcessorFS2Configuration());
		URI fileLoc = new URI("fs2:" + getWriteResponseURI());
		FS2MetaSnapshot metaSnapShot = FS2.createObjectEntry(fileLoc);
		FS2.writePayloadFromBytes(metaSnapShot.getURI(), response.toByteArray());
		LOGGER.info("Reponse is succefully written" + metaSnapShot.getURI());

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
