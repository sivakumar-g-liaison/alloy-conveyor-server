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
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.FileUtils;
import org.codehaus.jackson.map.JsonMappingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonParseException;
import com.liaison.commons.exceptions.LiaisonException;
import com.liaison.commons.jaxb.JAXBUtility;
import com.liaison.commons.security.pkcs7.SymmetricAlgorithmException;
import com.liaison.commons.util.client.http.HTTPRequest;
import com.liaison.framework.fs2.api.FS2Exception;
import com.liaison.framework.fs2.api.FS2Factory;
import com.liaison.framework.fs2.api.FS2MetaSnapshot;
import com.liaison.framework.fs2.api.FlexibleStorageSystem;
import com.liaison.mailbox.enums.FolderType;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.jpa.model.Folder;
import com.liaison.mailbox.jpa.model.Processor;
import com.liaison.mailbox.service.core.EmailNotifier;
import com.liaison.mailbox.service.core.ProcessorConfigurationService;
import com.liaison.mailbox.service.dto.configuration.DynamicPropertiesDTO;
import com.liaison.mailbox.service.dto.configuration.request.HttpOtherRequestHeaderDTO;
import com.liaison.mailbox.service.dto.configuration.request.HttpRemoteDownloaderPropertiesDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.util.MailBoxCryptoUtil;
import com.liaison.mailbox.service.util.MailBoxUtility;

/**
 * @author praveenu
 * 
 */
public abstract class AbstractRemoteProcessor {

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractRemoteProcessor.class);

	private static final EmailNotifier NOTIFIER = new EmailNotifier();

	private static FlexibleStorageSystem FS2 = null;

	protected Processor configurationInstance;

	public AbstractRemoteProcessor() {
	}

	public AbstractRemoteProcessor(Processor configurationInstance) {
		this.configurationInstance = configurationInstance;
	}

	/**
	 * Instantiate the FS2. It gets the mount location from properties.
	 * 
	 * @return The FlexibleStorageSystem instance
	 * @throws IOException
	 */
	public static FlexibleStorageSystem getFS2Instance() throws IOException {

		if (null == FS2) {
			FS2 = FS2Factory.newInstance(new RemoteProcessorFS2Configuration());
		}
		return FS2;
	}

	/**
	 * This will return a HTTP ,FTP,HTTPS or FTPS client based on the processor
	 * type.
	 * 
	 * @return
	 */
	public Object getClient() {

		switch (configurationInstance.getProcessorType()) {

			case REMOTEDOWNLOADER:
				return new HTTPRequest(null, LOGGER);
			case REMOTEUPLOADER:
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

				FolderType foundFolderType = FolderType.findByCode(folder.getFldrType());
				if (null == foundFolderType) {
					throw new MailBoxServicesException(Messages.FOLDERS_CONFIGURATION_INVALID);
				} else if (FolderType.PAYLOAD_LOCATION.equals(foundFolderType)) {

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

				FolderType foundFolderType = FolderType.findByCode(folder.getFldrType());
				if (null == foundFolderType) {
					throw new MailBoxServicesException(Messages.FOLDERS_CONFIGURATION_INVALID);
				} else if (FolderType.RESPONSE_LOCATION.equals(foundFolderType)) {
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
		FlexibleStorageSystem FS2 = getFS2Instance();
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

		return configurationInstance.getDynamicProperties();
	}

	/**
	 * Update the dynamic property list of the MailBox known only to java script
	 * 
	 * @throws IOException
	 * @throws JAXBException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 * 
	 */
	public void addUpdateDynamicProperty(String dynamicProperties) throws JsonParseException, JsonMappingException,
			JAXBException, IOException {

		ProcessorConfigurationService service = new ProcessorConfigurationService();
		DynamicPropertiesDTO dynamicPropertiesDTO = JAXBUtility.unmarshalFromJSON(dynamicProperties, DynamicPropertiesDTO.class);
		service.addOrUpdateProcessorProperties(String.valueOf(configurationInstance.getPrimaryKey()), dynamicPropertiesDTO);

	}

	/**
	 * Get the credentials of the MailBox known only to java script
	 * 
	 * @return MailBox dynamic properties
	 */
	public Object getPassword() {
		return configurationInstance.getCredentials();
	}

	public Object getCertificate() {
		// TODO
		return null;
	}

	/**
	 * Get the decrypted value of a String, known only to java script
	 * 
	 * @param encryptedValue
	 *            The text to be decrypted
	 * @return decrypted data as String
	 */
	public String getDecryptedString(String encryptedValue) throws SymmetricAlgorithmException {
		return MailBoxCryptoUtil.doPasswordEncryption(encryptedValue, 2);
	}

	/**
	 * Get HTTPRequest with injected configurations.
	 * 
	 * @return configured HTTPRequest
	 * @throws MailBoxServicesException
	 * @throws IOException
	 * @throws JAXBException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 * @throws LiaisonException
	 */
	protected HTTPRequest getClientWithInjectedConfiguration() throws JsonParseException, JsonMappingException, JAXBException,
			IOException, LiaisonException {

		LOGGER.info("Started injecting HTTP/S configurations to HTTPClient");
		// Create HTTPRequest and set the properties
		HTTPRequest request = new HTTPRequest(null, LOGGER);

		// Convert the json string to DTO
		HttpRemoteDownloaderPropertiesDTO properties = MailBoxUtility.unmarshalFromJSON(
				configurationInstance.getProcsrProperties(), HttpRemoteDownloaderPropertiesDTO.class);

		// Set url to HTTPRequest
		URL url = new URL(properties.getUrl());
		request.setUrl(url);

		// Set configurations
		request.setVersion(properties.getHttpVersion());
		request.setMethod(properties.getHttpVerb());
		request.setNumberOfRetries(properties.getRetryAttempts());
		request.setSocketTimeout(properties.getSocketTimeout());
		request.setConnectionTimeout(properties.getConnectionTimeout());
		request.setPort(properties.getPort());
		request.setChunkedEncoding(properties.isChunkedEncoding());

		// Set the Other header to HttpRequest
		if (properties.getOtherRequestHeader() != null) {
			for (HttpOtherRequestHeaderDTO header : properties.getOtherRequestHeader()) {
				request.addHeader(header.getName(), header.getValue());
			}
		}

		// Set the content type header to HttpRequest
		if (MailBoxUtility.isEmpty(properties.getContentType())) {
			request.addHeader("Content-Type", properties.getContentType());
		}

		LOGGER.info("Returns HTTP/S configured HTTPClient");

		return request;

	}

	/**
	 * Method to read the javascript file as string
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 * 
	 */
	protected String getJavaScriptString(String URI) throws IOException, URISyntaxException {

		StringBuffer buffer = new StringBuffer();
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(URI))) {
			for (Path entry : stream) {
				String content = FileUtils.readFileToString(entry.toFile(), "UTF-8");
				buffer.append(content);
			}
		} catch (IOException e) {
			throw e;
		}
		return buffer.toString();
	}

	/**
	 * Sent notifications for trigger system failure.
	 * 
	 * @param toEmailAddrList
	 *            The extra receivers. The default receiver will be available in
	 *            the mailbox.
	 * @param subject
	 *            The notification subject
	 * @param emailBody
	 *            The body of the notification
	 * @param type
	 *            The notification type(TEXT/HTML).
	 */
	public void sendEmail(List<String> toEmailAddrList, String subject, String emailBody, String type) {

		List<String> configuredEmailAddress = configurationInstance.getEmailAddress();
		if (null != configuredEmailAddress) {
			toEmailAddrList.addAll(configuredEmailAddress);
		}

		NOTIFIER.sendEmail(toEmailAddrList, subject, emailBody, type);
	}

}
