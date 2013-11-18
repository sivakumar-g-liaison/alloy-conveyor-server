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
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
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
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.enums.CredentialType;
import com.liaison.mailbox.enums.ExecutionStatus;
import com.liaison.mailbox.enums.FolderType;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.jpa.dao.ProcessorConfigurationDAO;
import com.liaison.mailbox.jpa.dao.ProcessorConfigurationDAOBase;
import com.liaison.mailbox.jpa.model.Credential;
import com.liaison.mailbox.jpa.model.Folder;
import com.liaison.mailbox.jpa.model.MailBoxProperty;
import com.liaison.mailbox.jpa.model.Processor;
import com.liaison.mailbox.jpa.model.ProcessorProperty;
import com.liaison.mailbox.service.core.EmailNotifier;
import com.liaison.mailbox.service.core.ProcessorConfigurationService;
import com.liaison.mailbox.service.dto.configuration.CredentialDTO;
import com.liaison.mailbox.service.dto.configuration.DynamicPropertiesDTO;
import com.liaison.mailbox.service.dto.configuration.FolderDTO;
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
	private static final ProcessorConfigurationDAO PROCESSOR_DAO = new ProcessorConfigurationDAOBase();

	public static boolean isTrustStore = false;

	protected Processor configurationInstance;
	protected Properties mailBoxProperties;

	public AbstractRemoteProcessor() {
	}

	public AbstractRemoteProcessor(Processor configurationInstance) {
		this.configurationInstance = configurationInstance;
	}

	/**
	 * This will return a HTTP,HTTPS or FTPS client based on the processor type.
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
	 * To Retrieve the credential info from the processor
	 * 
	 * @return array of CredentialDTO
	 * @throws MailBoxConfigurationServicesException
	 * @throws SymmetricAlgorithmException
	 */
	public CredentialDTO[] getProcessorCredentials() throws MailBoxConfigurationServicesException, SymmetricAlgorithmException {

		CredentialDTO[] credentialArray = null;

		if (configurationInstance.getCredentials() != null
				&& !configurationInstance.getCredentials().isEmpty()) {

			List<CredentialDTO> credentialsList = new ArrayList<>();
			CredentialDTO credentialDTO = null;
			for (Credential credential : configurationInstance.getCredentials()) {

				credentialDTO = new CredentialDTO();
				credentialDTO.copyFromEntity(credential);
				credentialsList.add(credentialDTO);
			}

			if (credentialsList.size() > 0) {
				credentialArray = Arrays.copyOf(credentialsList.toArray(), credentialsList.toArray().length,
						CredentialDTO[].class);
			}
		}

		return credentialArray;
	}

	/**
	 * To Retrieve the folder info from the processor
	 * 
	 * @return array of FolderDTO
	 * @throws MailBoxConfigurationServicesException
	 * @throws MailBoxServicesException
	 */
	public FolderDTO[] getProcessorFolders() throws MailBoxConfigurationServicesException {

		FolderDTO[] folderArray = null;

		if (configurationInstance.getFolders() != null
				&& !configurationInstance.getFolders().isEmpty()) {

			List<FolderDTO> foldersDTO = new ArrayList<>();
			FolderDTO folderDTO = null;
			for (Folder folder : configurationInstance.getFolders()) {

				folderDTO = new FolderDTO();
				folderDTO.copyFromEntity(folder);
				foldersDTO.add(folderDTO);
			}

			if (!foldersDTO.isEmpty()) {
				folderArray = Arrays.copyOf(foldersDTO.toArray(), foldersDTO.toArray().length, FolderDTO[].class);
			}
		}

		return folderArray;
	}

	/**
	 * To Retrieve the Payload file from the URI
	 * 
	 * @return array of files
	 * @throws MailBoxConfigurationServicesException
	 * @throws MailBoxServicesException
	 * @throws IOException
	 */
	public File[] getProcessorPayload() throws MailBoxServicesException, IOException {

		File[] files = null;

		if (configurationInstance.getFolders() != null) {

			for (Folder folder : configurationInstance.getFolders()) {

				FolderType foundFolderType = FolderType.findByCode(folder.getFldrType());

				if (null == foundFolderType) {
					throw new MailBoxServicesException(Messages.FOLDERS_CONFIGURATION_INVALID);
				} else if (FolderType.PAYLOAD_LOCATION.equals(foundFolderType)) {

					LOGGER.debug("Started reading the payload files");
					List<File> result = new ArrayList<>();
					listFiles(folder.getFldrUri(), result);

					if (!result.isEmpty()) {
						files = Arrays.copyOf(result.toArray(), result.toArray().length, File[].class);
						LOGGER.debug("Completed reading the payload files");
					}
				}
			}
		}
		return files;
	}

	/**
	 * To Retrive the Payload URI
	 * 
	 * @return Payload URI String
	 * @throws MailBoxConfigurationServicesException
	 * @throws MailBoxServicesException
	 */
	protected String getPayloadURI() throws MailBoxServicesException {

		if (configurationInstance.getFolders() != null) {

			for (Folder folder : configurationInstance.getFolders()) {

				FolderType foundFolderType = FolderType.findByCode(folder.getFldrType());
				if (null == foundFolderType) {
					throw new MailBoxServicesException(Messages.FOLDERS_CONFIGURATION_INVALID);
				} else if (FolderType.PAYLOAD_LOCATION.equals(foundFolderType)) {
					return folder.getFldrUri();
				}
			}
		}
		return null;
	}

	/**
	 * Get HTTPRequest configurations from processor.
	 * 
	 * @return JSON String containing URL, Verb..etc of the processor
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
	protected Properties getMailBoxProperties() throws MailBoxServicesException {

		if (mailBoxProperties != null) {
			return mailBoxProperties;
		} else {

			mailBoxProperties = new Properties();
			if (null != configurationInstance.getDynamicProperties()) {
				for (MailBoxProperty property : configurationInstance.getMailbox().getMailboxProperties()) {
					mailBoxProperties.setProperty(property.getMbxPropName(), property.getMbxPropValue());
				}
			}
			return mailBoxProperties;
		}
	}

	/**
	 * Get the credential URI of TrustStore & Keystore to execute the FTPS uploader/downloader
	 * 
	 * @return String URI
	 * @throws MailBoxServicesException
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

	/**
	 * Get the credential URI for login details to execute the FTPS & SFTP uploader/downloader
	 * 
	 * @return String URI
	 * @throws MailBoxServicesException
	 */
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

	/**
	 * Get the login details from credentialURI to execute the FTPS & SFTP uploader/downloader
	 * 
	 * @return String[]
	 * @throws MailBoxServicesException
	 * @throws URISyntaxException
	 */
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
		String processorName = MailBoxConstants.PROCESSOR;
		if (configurationInstance.getProcsrName() != null) {
			processorName = configurationInstance.getProcsrName().replaceAll(" ", "");
		}
		URI fileLoc = new URI("fs2:" + getWriteResponseURI() + processorName + System.nanoTime());
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

		if (null != configurationInstance.getDynamicProperties()) {

			for (ProcessorProperty property : configurationInstance.getDynamicProperties()) {
				properties.setProperty(property.getProcsrPropName(), property.getProcsrPropValue());
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
		if (!MailBoxUtility.isEmpty(properties.getContentType())) {
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
			SweepConditions sweepConditions, String fileRenameFormat) throws IOException, URISyntaxException,
			MailBoxServicesException, FS2Exception {

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
				for (Path file : stream) {

					if (!file.getFileName().toString().contains(fileRenameFormat)) {
						result.add(file);
					}
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
	 * Method to list file from folder
	 * 
	 * @throws MailBoxServicesException
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 * 
	 */
	private void listFiles(String directoryName, List<File> files) throws MailBoxServicesException {

		File directory = new File(directoryName);

		if (!directory.isDirectory()) {
			LOGGER.info("The given URI {} is not a directory.", directoryName);
			throw new MailBoxServicesException("The given URI '" + directoryName + "' is not a directory.");
		}

		if (!directory.exists()) {
			LOGGER.info("The given directory {} does not exist.", directoryName);
			throw new MailBoxServicesException("The given directory '" + directoryName + "' does not exist.");
		} else {

			// get all the files from a directory
			for (File file : directory.listFiles()) {

				if (file.isFile()) {
					if (!MailBoxConstants.META_FILE_NAME.equals(file.getName())) {
						files.add(file);
					}
				} else if (file.isDirectory()) { // get all files from inner directory.
					if (!MailBoxConstants.PROCESSED_FOLDER.equals(file.getName())) {
						listFiles(file.getAbsolutePath(), files);
					}
				}
			}
		}
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

	/**
	 * Method is used to move the file to the processed folder.
	 * 
	 * @param file
	 *            The source location
	 * @param target
	 *            The target location
	 * @throws IOException
	 */
	public void archiveFile(String filePath) throws IOException {

		File file = new File(filePath);

		Path targetDirectory = file.toPath().getParent().resolve(MailBoxConstants.PROCESSED_FOLDER.toLowerCase());
		if (!Files.exists(targetDirectory)) {
			LOGGER.info("Creating 'processed' folder");
			Files.createDirectories(targetDirectory);
		}
		Path target = targetDirectory.resolve(file.getName());
		// moving to processed folder
		Files.move(file.toPath(), target, StandardCopyOption.ATOMIC_MOVE);
	}

	/**
	 * Method is used to modify the status during failure.
	 */
	protected void modifyProcessorExecutionStatus(ExecutionStatus status) {

		configurationInstance.setProcsrExecutionStatus(status.value());
		PROCESSOR_DAO.merge(configurationInstance);

	}

	/**
	 * Use to validate the given file can be added in the given group.
	 * 
	 * @param fileGroup
	 *            The file attributes group
	 * @param fileAttribute
	 *            The file attribute to be added in the group
	 * @return true if it can be added false otherwise
	 * @throws MailBoxServicesException
	 */
	protected Boolean validateAdditionalGroupFile(List<FileAttributesDTO> fileGroup, FileAttributesDTO fileAttribute)
			throws MailBoxServicesException {

		long maxPayloadSize = Long.parseLong(getMailBoxProperties().getProperty(MailBoxConstants.PAYLOAD_SIZE_THRESHOLD));
		long maxNoOfFiles = Long.parseLong(getMailBoxProperties().getProperty(MailBoxConstants.NUMER_OF_FILES_THRESHOLD));

		if (maxPayloadSize == 0) {
			maxPayloadSize = 131072;
		}

		if (maxNoOfFiles == 0) {
			maxNoOfFiles = 10;
		}

		if (maxNoOfFiles <= fileGroup.size()) {
			return false;
		}

		if (maxPayloadSize <= (getGroupFileSize(fileGroup) + fileAttribute.getSize())) {
			return false;
		}
		return true;
	}

	/**
	 * Get the total file size of the group.
	 * 
	 * @param fileGroup
	 * @return
	 */
	private long getGroupFileSize(List<FileAttributesDTO> fileGroup) {

		long size = 0;

		for (FileAttributesDTO attribute : fileGroup) {
			size += attribute.getSize();
		}

		return size;
	}

}
