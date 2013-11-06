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
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

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
import com.liaison.fs2.api.FS2Exception;
import com.liaison.fs2.api.FS2MetaSnapshot;
import com.liaison.fs2.api.FlexibleStorageSystem;
import com.liaison.mailbox.enums.CredentialType;
import com.liaison.mailbox.enums.FolderType;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.jpa.model.Credential;
import com.liaison.mailbox.jpa.model.Folder;
import com.liaison.mailbox.jpa.model.MailBoxProperty;
import com.liaison.mailbox.jpa.model.Processor;
import com.liaison.mailbox.jpa.model.ProcessorProperty;
import com.liaison.mailbox.service.core.EmailNotifier;
import com.liaison.mailbox.service.core.ProcessorConfigurationService;
import com.liaison.mailbox.service.dto.configuration.CredentialDTO;
import com.liaison.mailbox.service.dto.configuration.DynamicPropertiesDTO;
import com.liaison.mailbox.service.dto.configuration.PropertyDTO;
import com.liaison.mailbox.service.dto.configuration.request.HttpOtherRequestHeaderDTO;
import com.liaison.mailbox.service.dto.configuration.request.RemoteProcessorPropertiesDTO;
import com.liaison.mailbox.service.dto.directorysweeper.FileAttributesDTO;
import com.liaison.mailbox.service.dto.directorysweeper.SweepConditions;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.util.FS2InstanceCreator;
import com.liaison.mailbox.service.util.MailBoxCryptoUtil;
import com.liaison.mailbox.service.util.MailBoxUtility;

/**
 * @author praveenu
 * 
 */
public abstract class AbstractRemoteProcessor {

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractRemoteProcessor.class);

	private static final EmailNotifier NOTIFIER = new EmailNotifier();

	public static boolean isTrustStore = false;

	protected Processor configurationInstance;

	public AbstractRemoteProcessor() {
	}

	public AbstractRemoteProcessor(Processor configurationInstance) {
		this.configurationInstance = configurationInstance;
	}

	/**
	 * This will return a HTTP ,FTP,HTTPS or FTPS client based on the processor type.
	 * 
	 * @return The Object based on processor type.
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
					File file = new File(folder.getFldrUri());
					
					if(file.isFile()){
						LOGGER.debug("Payload files received successfully");
						files = new File[1];
						files[0] = file;
						return files;
					}else{
						LOGGER.debug("Payload files received successfully");
						return file.listFiles();
					}
				}
			}
		}
		return files;
	}

	/**
	 * To Retrive the Payload URI
	 * 
	 * @return List of files
	 * @throws MailBoxConfigurationServicesException
	 * @throws MailBoxServicesException
	 */
	public String getPayloadURI() throws MailBoxServicesException {

		if (configurationInstance.getFolders() != null) {

			for (Folder folder : configurationInstance.getFolders()) {

				FolderType foundFolderType = FolderType.findByCode(folder.getFldrType());
				if (null == foundFolderType) {
					throw new MailBoxServicesException(Messages.FOLDERS_CONFIGURATION_INVALID);
				} else if (FolderType.INPUT_FOLDER.equals(foundFolderType)) {
					return folder.getFldrUri();
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
	 * Get the URI to which the response should be written, this can be used if the JS decides to
	 * write the response straight to the file system or database
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
				} else if (FolderType.OUTPUT_FOLDER.equals(foundFolderType)) {
					return folder.getFldrUri();
				}
			}
		}
		return null;
	}

	/**
	 * Get the URI to which the mailbox sweeper should be happen
	 * 
	 * @return URI
	 * @throws MailBoxConfigurationServicesException
	 */
	protected List<PropertyDTO> getMailBoxProperties() throws MailBoxServicesException {

		List<PropertyDTO> mailBoxProperties = new ArrayList<>();

		PropertyDTO prop = null;
		for (MailBoxProperty property : configurationInstance.getMailbox().getMailboxProperties()) {

			prop = new PropertyDTO();
			prop.copyFromEntity(property, true);
			mailBoxProperties.add(prop);
		}

		return mailBoxProperties;
	}

	/**
	 * Get the URI to which the response should be written, this can be used if the JS decides to
	 * write the response straight to the file system or database
	 * 
	 * @return URI
	 * @throws MailBoxConfigurationServicesException
	 */
	protected String getCredentialURI() throws MailBoxServicesException {

		if (configurationInstance.getCredentials() != null) {

			for (Credential credential : configurationInstance.getCredentials()) {

				CredentialType foundCredentailType = CredentialType.findByCode(credential.getCredsType());
				if (null == foundCredentailType) {

					throw new MailBoxServicesException(Messages.CREDENTIAL_CONFIGURATION_INVALID);
				} else if (CredentialType.TRUST_STORE.equals(foundCredentailType)) {

					isTrustStore = true;
					return credential.getCredsIdpUri();
				} else if (CredentialType.KEY_STORE.equals(foundCredentailType)) {

					isTrustStore = false;
					return credential.getCredsIdpUri();
				}
			}
		}
		return null;
	}

	protected String getUserCredentialURI() throws MailBoxServicesException {

		if (configurationInstance.getCredentials() != null) {

			for (Credential credential : configurationInstance.getCredentials()) {

				CredentialType foundCredentailType = CredentialType.findByCode(credential.getCredsType());

				if (null == foundCredentailType) {
					throw new MailBoxServicesException(Messages.CREDENTIAL_CONFIGURATION_INVALID);
				} else if (CredentialType.LOGIN_CREDENTIAL.equals(foundCredentailType)) {
					return credential.getCredsIdpUri();
				}
			}
		}
		return null;
	}

	protected String[] getUserCredetial(String credentialURI) throws URISyntaxException, MailBoxServicesException {

		if (MailBoxUtility.isEmpty(credentialURI)) {
			throw new MailBoxServicesException(Messages.CREDENTIAL_CONFIGURATION_INVALID);
		}
		URI uri = new URI(credentialURI);
		String[] userData = uri.getRawAuthority().split("@")[0].split(":");
		return userData;
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
		FlexibleStorageSystem FS2 = FS2InstanceCreator.getFS2Instance();
		URI fileLoc = new URI("fs2:" + getWriteResponseURI());
		FS2MetaSnapshot metaSnapShot = FS2.createObjectEntry(fileLoc);
		FS2.writePayloadFromBytes(metaSnapShot.getURI(), response.toByteArray());
		LOGGER.info("Reponse is succefully written" + metaSnapShot.getURI());

	}

	/**
	 * call back method to write the file response back to MailBox from JS
	 * 
	 * @throws MailBoxServicesException
	 * @throws URISyntaxException
	 * @throws IOException
	 * @throws FS2Exception
	 * 
	 */
	public void writeFileResponseToMailBox(ByteArrayOutputStream response, String filename) throws URISyntaxException,
			IOException, FS2Exception,
			MailBoxServicesException {

		LOGGER.info("Started writing response");
		FlexibleStorageSystem FS2 = FS2InstanceCreator.getFS2Instance();
		URI fileLoc = new URI("fs2:" + getWriteResponseURI() + filename);
		FS2MetaSnapshot metaSnapShot = FS2.createObjectEntry(fileLoc);
		FS2.writePayloadFromBytes(metaSnapShot.getURI(), response.toByteArray());
		LOGGER.info("Reponse is succefully written" + getWriteResponseURI() + "/" + metaSnapShot.getURI());

	}

	/**
	 * Get the list of dynamic properties of the MailBox known only to java script
	 * 
	 * @return MailBox dynamic properties
	 */
	public Properties getDynamicProperties() {

		Properties properties = new Properties();
		
		if(null != configurationInstance.getDynamicProperties()){
			
			for (ProcessorProperty property : configurationInstance.getDynamicProperties()) {

				properties.setProperty(property.getProcsrPropName(),property.getProcsrPropValue());
			}
		}
		return properties;
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
	 * @throws SymmetricAlgorithmException 
	 */
	public Object getProcessorCredentials() throws SymmetricAlgorithmException {
		
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
	 * @throws URISyntaxException
	 */
	public Object getClientWithInjectedConfiguration() throws JsonParseException, JsonMappingException, JAXBException,
			IOException, LiaisonException, URISyntaxException, MailBoxServicesException {

		LOGGER.info("Started injecting HTTP/S configurations to HTTPClient");
		// Create HTTPRequest and set the properties
		HTTPRequest request = new HTTPRequest(null, LOGGER);

		// Convert the json string to DTO
		RemoteProcessorPropertiesDTO properties = MailBoxUtility.unmarshalFromJSON(
				configurationInstance.getProcsrProperties(), RemoteProcessorPropertiesDTO.class);

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

		File file = new File(URI);
		if (file.isFile()) { // Added because of this java.nio.file.NotDirectoryException

			String content = FileUtils.readFileToString(file, "UTF-8");
			return content;
		} else {

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
	}

	/**
	 * Sent notifications for trigger system failure.
	 * 
	 * @param toEmailAddrList
	 *            The extra receivers. The default receiver will be available in the mailbox.
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

	/**
	 * Method is used to retrieve all the files attributes from the given mailbox. This method
	 * supports both FS2 and Java File API
	 * 
	 * @param root
	 *            The mailbox root directory
	 * @param includeSubDir
	 * @param listDirectoryOnly
	 * @param sweepConditions
	 * @return
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws MailBoxServicesException
	 * @throws FS2Exception
	 */
	public List<FileAttributesDTO> sweepDirectory(String root, boolean includeSubDir, boolean listDirectoryOnly,
			SweepConditions sweepConditions) throws IOException, URISyntaxException, MailBoxServicesException, FS2Exception {

		List<FileAttributesDTO> fileAttributes = new ArrayList<>();

		if (root.startsWith("fs2:")) {

			FlexibleStorageSystem FS2 = FS2InstanceCreator.getFS2Instance();
			URI fileLoc = new URI(root);

			Set<FS2MetaSnapshot> childrens = FS2.listChildren(fileLoc);

			FileAttributesDTO attribute = null;
			for (FS2MetaSnapshot file : childrens) {

				attribute = new FileAttributesDTO();
				attribute.setFilePath((file.getURI() == null ? "" : file.getURI().toString()));
				attribute.setTimestamp((file.createdOn() == null ? "" : file.createdOn().toString()));
				fileAttributes.add(attribute);
			}

		} else {

			List<Path> result = new ArrayList<>();

			try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(root), defineFilter(listDirectoryOnly))) {
				for (Path entry : stream) {
					result.add(entry);
				}
			} catch (IOException e) {
				throw e;
			}

			FileAttributesDTO attribute = null;
			for (Path path : result) {

				attribute = new FileAttributesDTO();
				attribute.setFilePath(path.toAbsolutePath().toString());
				BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
				attribute.setTimestamp(attr.creationTime().toString());
				attribute.setSize(attr.size());
				attribute.setFilename(path.toFile().getName());
				fileAttributes.add(attribute);
			}
		}

		return fileAttributes;
	}

	/**
	 * Creates a filter for directories only.
	 * 
	 * @return Object which implements DirectoryStream.Filter interface and that accepts directories
	 *         only.
	 */
	public DirectoryStream.Filter<Path> defineFilter(
			final boolean listDirectoryOnly) {

		DirectoryStream.Filter<Path> filter = new DirectoryStream.Filter<Path>() {

			@Override
			public boolean accept(Path entry) throws IOException {

				return listDirectoryOnly
						? Files.isDirectory(entry)
						: Files.isRegularFile(entry);
			}
		};

		return filter;
	}

}
