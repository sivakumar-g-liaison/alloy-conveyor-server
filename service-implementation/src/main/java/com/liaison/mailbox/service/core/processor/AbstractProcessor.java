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
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermissions;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.map.JsonMappingException;

import com.google.gson.JsonParseException;
import com.liaison.commons.jaxb.JAXBUtility;
import com.liaison.commons.security.pkcs7.SymmetricAlgorithmException;
import com.liaison.fs2.api.exceptions.FS2Exception;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.dtdm.model.Credential;
import com.liaison.mailbox.dtdm.model.Folder;
import com.liaison.mailbox.dtdm.model.MailBoxProperty;
import com.liaison.mailbox.dtdm.model.Processor;
import com.liaison.mailbox.dtdm.model.ProcessorProperty;
import com.liaison.mailbox.enums.CredentialType;
import com.liaison.mailbox.enums.FolderType;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.service.core.ProcessorConfigurationService;
import com.liaison.mailbox.service.core.email.EmailNotifier;
import com.liaison.mailbox.service.dto.configuration.CredentialDTO;
import com.liaison.mailbox.service.dto.configuration.DynamicPropertiesDTO;
import com.liaison.mailbox.service.dto.configuration.FolderDTO;
import com.liaison.mailbox.service.dto.configuration.processor.properties.ProcessorPropertyUITemplateDTO;
import com.liaison.mailbox.service.dto.configuration.processor.properties.StaticProcessorPropertiesDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.util.MailBoxUtil;
import com.liaison.mailbox.service.util.ProcessorPropertyJsonMapper;

/**
 * @author OFS
 *
 */
public abstract class AbstractProcessor implements ProcessorJavascriptI {

	private static final Logger LOGGER = LogManager.getLogger(AbstractProcessor.class);

	protected Processor configurationInstance;
	public Properties mailBoxProperties;
	public ProcessorPropertyUITemplateDTO processorPropertiesTemplate;
	public StaticProcessorPropertiesDTO staticProcessorProperties;

	public AbstractProcessor() {}

	public AbstractProcessor(Processor configurationInstance) {
		this.configurationInstance = configurationInstance;
	}


	public Processor getConfigurationInstance() {
		return configurationInstance;
	}

	/**
	 * Construct DTO from Entity.
	 *
	 * @return the remoteProcessorProperties
	 * @throws IOException
	 * @throws JAXBException
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 * @throws SecurityException 
	 * @throws NoSuchFieldException 
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 */
	public ProcessorPropertyUITemplateDTO getPropertiesInTemplateJsonFormat() throws JAXBException, IOException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {

		if (null == processorPropertiesTemplate) {
			processorPropertiesTemplate = ProcessorPropertyJsonMapper.getHydratedUIPropertyTemplate(configurationInstance.getProcsrProperties(), configurationInstance);
		}

		return processorPropertiesTemplate;
	}
	
	/**
	 * Method to return static properties stored in DB of a processor
	 * 
	 * @return StaticProcessorPropertiesDTO
	 * @throws JAXBException
	 * @throws IOException
	 * @throws NoSuchFieldException
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public StaticProcessorPropertiesDTO getProperties() throws JAXBException, IOException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {

		if (null == staticProcessorProperties) {
			staticProcessorProperties = ProcessorPropertyJsonMapper.getProcessorBasedStaticPropsFromJson(configurationInstance.getProcsrProperties(), configurationInstance);
		}

		return staticProcessorProperties;
	}

	/**
	 * To Retrieve the credential info from the processor
	 *
	 * @return array of CredentialDTO
	 * @throws MailBoxConfigurationServicesException
	 * @throws SymmetricAlgorithmException
	 */
	public CredentialDTO[] getCredentials() throws MailBoxConfigurationServicesException, SymmetricAlgorithmException {

		CredentialDTO[] credentialArray = null;

		if (configurationInstance.getCredentials() != null && !configurationInstance.getCredentials().isEmpty()) {

			List<CredentialDTO> credentialsList = new ArrayList<>();
			CredentialDTO credentialDTO = null;
			for (Credential credential : configurationInstance.getCredentials()) {

				credentialDTO = new CredentialDTO();
				credentialDTO.copyFromEntity(credential);
				credentialsList.add(credentialDTO);
			}

			if (credentialsList.size() > 0) {
				credentialArray = Arrays.copyOf(credentialsList.toArray(), credentialsList.toArray().length, CredentialDTO[].class);
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
	public FolderDTO[] getFolders() throws MailBoxConfigurationServicesException {

		FolderDTO[] folderArray = null;

		if (configurationInstance.getFolders() != null && !configurationInstance.getFolders().isEmpty()) {

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
	 * Returns array of files from the configured payload location. It reads from the local directory. It would be useful for uploaders.
	 *
	 * @return array of files
	 * @throws MailBoxConfigurationServicesException
	 * @throws MailBoxServicesException
	 * @throws IOException
	 */
	@Override
	public File[] getFilesToUpload() throws MailBoxServicesException, IOException {

		File[] files = null;

		if (configurationInstance.getFolders() != null) {

			for (Folder folder : configurationInstance.getFolders()) {

				FolderType foundFolderType = FolderType.findByCode(folder.getFldrType());

				if (null == foundFolderType) {
					throw new MailBoxServicesException(Messages.FOLDERS_CONFIGURATION_INVALID, Response.Status.CONFLICT);
				} else if (FolderType.PAYLOAD_LOCATION.equals(foundFolderType)) {

					LOGGER.debug("Started reading the payload files");
					List<File> result = new ArrayList<>();
					fetchFiles(replaceTokensInFolderPath(folder.getFldrUri()), result);

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
	 * To Retrieve the Payload URI
	 *
	 * @return Payload URI String
	 * @throws MailBoxConfigurationServicesException
	 * @throws MailBoxServicesException
	 */
	public String getPayloadURI() throws MailBoxServicesException, IOException {

		if (configurationInstance.getFolders() != null) {

			for (Folder folder : configurationInstance.getFolders()) {

				FolderType foundFolderType = FolderType.findByCode(folder.getFldrType());
				if (null == foundFolderType) {
					throw new MailBoxServicesException(Messages.FOLDERS_CONFIGURATION_INVALID, Response.Status.CONFLICT);
				} else if (FolderType.PAYLOAD_LOCATION.equals(foundFolderType)) {
					return replaceTokensInFolderPath(folder.getFldrUri());
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
	public String getPropertiesJson() {

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
	public String getWriteResponseURI() throws MailBoxServicesException, IOException {

		if (configurationInstance.getFolders() != null) {

			for (Folder folder : configurationInstance.getFolders()) {

				FolderType foundFolderType = FolderType.findByCode(folder.getFldrType());
				if (null == foundFolderType) {
					throw new MailBoxServicesException(Messages.FOLDERS_CONFIGURATION_INVALID, Response.Status.CONFLICT);
				} else if (FolderType.RESPONSE_LOCATION.equals(foundFolderType)) {
					return replaceTokensInFolderPath(folder.getFldrUri());
				}
			}
		}
		return null;
	}

	/**
	 * To Retrieve the File write URI
	 *
	 * @return File Write Location as String
	 * @throws MailBoxConfigurationServicesException
	 * @throws MailBoxServicesException
	 */
	public String getFileWriteLocation() throws MailBoxServicesException, IOException {

		if (configurationInstance.getFolders() != null) {

			for (Folder folder : configurationInstance.getFolders()) {

				FolderType foundFolderType = FolderType.findByCode(folder.getFldrType());
				if (null == foundFolderType) {
					throw new MailBoxServicesException(Messages.FOLDERS_CONFIGURATION_INVALID, Response.Status.CONFLICT);
				} else if (FolderType.FILE_WRITE_LOCATION.equals(foundFolderType)) {
					return replaceTokensInFolderPath(folder.getFldrUri());
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
	public Properties getMailBoxProperties() throws MailBoxServicesException {

		if (mailBoxProperties != null) {
			return mailBoxProperties;
		} else {

			mailBoxProperties = new Properties();
			if (null != configurationInstance.getMailbox().getMailboxProperties()) {
				for (MailBoxProperty property : configurationInstance.getMailbox().getMailboxProperties()) {
					mailBoxProperties.setProperty(property.getMbxPropName(), property.getMbxPropValue());
				}
			}
			return mailBoxProperties;
		}
	}

	/**
	 * call back method to write the response back to MailBox from JS
	 *
	 * @throws MailBoxServicesException
	 * @throws URISyntaxException
	 * @throws IOException
	 * @throws FS2Exception
	 */
	public void writeResponseToMailBox(ByteArrayOutputStream response) throws URISyntaxException, IOException, MailBoxServicesException {

		LOGGER.info("Started writing response");
		String processorName = MailBoxConstants.PROCESSOR;
		if (configurationInstance.getProcsrName() != null) {
			processorName = configurationInstance.getProcsrName().replaceAll(" ", "");
		}
		String fileName = processorName + System.nanoTime();

		writeResponseToMailBox(response, fileName);

	}

	/**
	 * call back method to write the file response back to MailBox from JS
	 *
	 * @throws MailBoxServicesException
	 * @throws URISyntaxException
	 * @throws IOException
	 * @throws FS2Exception
	 */
	public void writeResponseToMailBox(ByteArrayOutputStream response, String filename) throws URISyntaxException, IOException,
			MailBoxServicesException {

		LOGGER.info("Started writing response");
		String responseLocation = getWriteResponseURI();

		if (MailBoxUtil.isEmpty(responseLocation)) {
			throw new MailBoxServicesException(Messages.LOCATION_NOT_CONFIGURED, MailBoxConstants.RESPONSE_LOCATION, Response.Status.CONFLICT);
		}

		File directory = new File(responseLocation);
		if (!directory.exists()) {
			Files.createDirectories(directory.toPath());
		}

		File file = new File(directory.getAbsolutePath() + File.separatorChar + filename);
		Files.write(file.toPath(), response.toByteArray());
		LOGGER.info("Reponse is successfully written" + file.getAbsolutePath());
		if (response != null) {
		    response.close();
		}
	}

	/**
	 * Get the list of custom properties of the MailBox only known to java
	 * script
	 *
	 * @return MailBox dynamic properties
	 */
	public Properties getCustomProperties() {

		Properties properties = new Properties();
		if (null != configurationInstance.getDynamicProperties()) {

			for (ProcessorProperty property : configurationInstance.getDynamicProperties()) {
				properties.setProperty(property.getProcsrPropName(), property.getProcsrPropValue());
			}
		}
		return properties;
	}

	/**
	 * Update the custom property list of the MailBox known only to java script
	 *
	 * @throws IOException
	 * @throws JAXBException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 *
	 */
	public void addorUpdateCustomProperty(String dynamicProperties) throws JAXBException, IOException {

		ProcessorConfigurationService service = new ProcessorConfigurationService();
		DynamicPropertiesDTO dynamicPropertiesDTO = JAXBUtility.unmarshalFromJSON(dynamicProperties, DynamicPropertiesDTO.class);
		service.addOrUpdateProcessorProperties(String.valueOf(configurationInstance.getPrimaryKey()), dynamicPropertiesDTO);
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
	private void fetchFiles(String path, List<File> files) throws MailBoxServicesException {

		if (MailBoxUtil.isEmpty(path)) {
			LOGGER.info("The given URI {} does not exist.", path);
			throw new MailBoxServicesException("The given URI '" + path + "' does not exist.", Response.Status.CONFLICT);
		}

		// Modified to support both file and directory.
		File location = new File(path);
		if (location.isFile()) {

			if (location.exists()) {
				files.add(location);
			} else {
				LOGGER.info("The given file {} does not exist.", path);
				throw new MailBoxServicesException("The given file '" + path + "' does not exist.", Response.Status.CONFLICT);
			}

		} else {

			if (!location.exists()) {
				LOGGER.info("The given directory {} does not exist.", path);
				throw new MailBoxServicesException("The given directory '" + path + "' does not exist.", Response.Status.CONFLICT);
			} else {

				// get all the files from a directory
				for (File file : location.listFiles()) {

					if (file.isFile()) {
						if (!MailBoxConstants.META_FILE_NAME.equals(file.getName())) {
							files.add(file);
						}
					} else if (file.isDirectory() && !MailBoxConstants.PROCESSED_FOLDER.equals(file.getName())) {
						// recursively get all files from sub directory.
						fetchFiles(file.getAbsolutePath(), files);

					}
				}
			}
		}
	}

	/**
	 * Creates a filter for directories only.
	 *
	 * @return Object which implements DirectoryStream.Filter interface and that
	 *         accepts directories only.
	 */
	public DirectoryStream.Filter<Path> defineFilter(final boolean listDirectoryOnly) {

		DirectoryStream.Filter<Path> filter = new DirectoryStream.Filter<Path>() {

			@Override
			public boolean accept(Path entry) throws IOException {

				return listDirectoryOnly ? Files.isDirectory(entry) : Files.isRegularFile(entry);
			}
		};

		return filter;
	}

	/**
	 * Method is used to move the file to the processed folder.
	 *
	 * @param filePath
	 *            The source location
	 * @throws IOException
	 */
	public void archiveFile(String filePath, boolean isError) throws IOException {

		File file = new File(filePath);
		String targetFolder = (isError) ? MailBoxConstants.ERROR_FOLDER : MailBoxConstants.PROCESSED_FOLDER;
		Path targetDirectory = file.toPath().getParent().resolve(targetFolder);
		if (!Files.exists(targetDirectory)) {
			LOGGER.info("Creating target(processed/error) folder");
			Files.createDirectories(targetDirectory);
		}
		Path target = targetDirectory.resolve(file.getName());
		// moving to processed/error folder
		Files.move(file.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
	}

	/**
	 * Method is used to move the files to the processed folder.
	 *
	 * @param filePath
	 *            The source location
	 * @throws IOException
	 */
	public void archiveFiles(File[] files, boolean isError) throws IOException {
		for (File file : files) {
			archiveFile(file.getAbsolutePath(), isError);
		}
	}

	/**
	 * Method is used to move the uploaded file to the given folder.
	 *
	 * @param file
	 *            File to be moved
	 * @param processedFileLcoation
	 *            The source location
	 * @throws IOException
	 */
	public void archiveFile(File file, String processedFileLcoation) throws IOException {

		Path oldPath = null;
		Path newPath = null;

		oldPath = Paths.get(file.toURI());
		newPath = Paths.get(processedFileLcoation).resolve(file.getName());

		if (!Files.exists(newPath.getParent())) {
			Files.createDirectories(newPath.getParent());
		}

		Files.move(oldPath, newPath, StandardCopyOption.REPLACE_EXISTING);
	}

	/**
	 * Method is used to move the uploaded file to the given folder.
	 *
	 * @param filePath
	 *            The source location
	 * @throws IOException
	 */
	public void archiveFiles(File[] files, String processedFileLcoation) throws IOException {

		for (File file : files) {
			archiveFile(file, processedFileLcoation);
		}
	}

	/**
	 * Method is used to process the folder path given by user and replace the
	 * mount location with proper value form properties file.
	 *
	 * @param folderPath
	 *            The folder path given by user
	 *
	 * @return processedFolderPath The folder path with mount location
	 *
	 */
	public String replaceTokensInFolderPath(String folderPath)  {

		String processedFolderPath = null;

		if (folderPath != null && folderPath.toUpperCase().contains(MailBoxConstants.MOUNT_LOCATION)) {
			String mountLocationValue = MailBoxUtil.getEnvironmentProperties().getString("MOUNT_POINT");
			processedFolderPath = folderPath.replaceAll(MailBoxConstants.MOUNT_LOCATION_PATTERN, mountLocationValue);
		} else {
			return folderPath;
		}
		LOGGER.info("The Processed Folder Path is" + processedFolderPath);
		return processedFolderPath;
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
		if ((configuredEmailAddress == null || configuredEmailAddress.isEmpty()) && (toEmailAddrList == null || toEmailAddrList.isEmpty())) {
			LOGGER.info("There is no email address configured for this mailbox.");
		}

		if (null != configuredEmailAddress && null != toEmailAddrList) {
			toEmailAddrList.addAll(configuredEmailAddress);
		} else if (null != configuredEmailAddress) {
			toEmailAddrList = configuredEmailAddress;
		}

		EmailNotifier notifier = new EmailNotifier();
		notifier.sendEmail(toEmailAddrList, subject, emailBody, type);
	}

	/**
	 * Get the credential Details configured for a processor
	 *
	 *
	 * @return String URI
	 * @throws MailBoxServicesException
	 * @throws SymmetricAlgorithmException
	 */
	public Credential getCredentialOfSpecificType(CredentialType type) throws MailBoxServicesException, SymmetricAlgorithmException {

		if (configurationInstance.getCredentials() != null) {

			for (Credential credential : configurationInstance.getCredentials()) {
				CredentialType foundCredentailType = CredentialType.findByCode(credential.getCredsType());
				if (credential.getCredsType() == null) {
					throw new MailBoxServicesException(Messages.CREDENTIAL_CONFIGURATION_INVALID, Response.Status.CONFLICT);
				} else if (foundCredentailType.equals(type)) {

					if (credential.getCredsType().equalsIgnoreCase(MailBoxConstants.SSH_KEYPAIR) && credential.getCredsIdpType().equalsIgnoreCase("PRIVATE")) {
						return credential;
					}
					if (!credential.getCredsIdpType().equalsIgnoreCase(MailBoxConstants.SSH_KEYPAIR))
						return credential;
				}
			}
		}
		return null;
	}

	/**
	 * Method is used to process the response location and create folders if not
	 * already exists for SFTP/FTP(S).
	 *
	 * @param filename
	 *            The source location
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws MailBoxServicesException
	 */
	public void createResponseDirectory(String fileName) throws URISyntaxException, IOException, MailBoxServicesException {

		LOGGER.info("Started writing response");

		String responseLocation = getWriteResponseURI();
		if (MailBoxUtil.isEmpty(responseLocation)) {
			throw new MailBoxServicesException(Messages.LOCATION_NOT_CONFIGURED, MailBoxConstants.RESPONSE_LOCATION, Response.Status.CONFLICT);
		}

		File directory = new File(responseLocation);
		if (!directory.exists()) {
			Files.createDirectories(directory.toPath());
		}

		if (MailBoxUtil.isEmpty(fileName)) {

			LOGGER.info("The given URI {} does not exist.", fileName);
			throw new MailBoxServicesException("The given URI '" + fileName + "' does not exist.", Response.Status.CONFLICT);
		}
	}

	/**
	 * Method used to remove the privatekey downloaded from keymanager once
	 * successfully authenticated using key
	 *
	 * @param fileLocation
	 * @throws IOException
	 * @throws SymmetricAlgorithmException
	 * @throws MailBoxServicesException
	 */
	public void removePrivateKeyFromTemp() throws IOException, MailBoxServicesException, SymmetricAlgorithmException {

		LOGGER.info("Trigerring - Remove privateKey downloaded from keyManager");
		Credential sshKeyPairCredential = getCredentialOfSpecificType(CredentialType.SSH_KEYPAIR);
		if (null != sshKeyPairCredential && sshKeyPairCredential.getCredsUri() != null) {
			String fileLocation = MailBoxUtil.getEnvironmentProperties().getString("ssh.private.key.temp.location") + sshKeyPairCredential.getCredsUri()
					+ ".txt";
			File privateKeyFile = new File(fileLocation);
			if (privateKeyFile.exists())
				privateKeyFile.delete();
			LOGGER.info("privateKey downloaded from keyManager removed from local file system");
			return;
		}

		LOGGER.info("Trigerring - The private key file path not configured.");
	}

	
	/**
	 * This Method create local folders if not available.
	 * 
	 * * @param processorDTO it have details of processor
	 * 
	 * @throws IOException
	 */
	public void createPathIfNotAvailable(String localPath) throws IOException {

	    if (MailBoxUtil.isEmpty(localPath)) {
	        LOGGER.debug("Given path is empty, so not creating folders..");
	        return;
	    }

		File fileDirectory = new File(localPath);
        if (fileDirectory.exists()) {
            LOGGER.debug("Not creating folders..");
            return;
        }

		Path filePathToCreate = fileDirectory.toPath();
		LOGGER.debug("Setting on to create - {}", filePathToCreate);
		FileSystem fileSystem = FileSystems.getDefault();	
		String pattern = MailBoxUtil.getEnvironmentProperties().getString("com.liaison.data.folder.pattern","glob:/data/{sftp,ftp,ftps}/*/{inbox,outbox}/**");
		PathMatcher pathMatcher = fileSystem.getPathMatcher(pattern);
		if(!pathMatcher.matches(filePathToCreate)){
			throw new MailBoxConfigurationServicesException(Messages.FOLDER_DOESNT_MATCH_PATTERN, pattern.substring(5), Response.Status.BAD_REQUEST);	
		}
		
		//check availability of /data/*/* folder
		if(!Files.exists(filePathToCreate.subpath(0, 3))){
			throw new MailBoxConfigurationServicesException(Messages.HOME_FOLDER_DOESNT_EXIST_ALREADY,filePathToCreate.subpath(0, 3).toString(), Response.Status.BAD_REQUEST);
		}
		
		
		Files.createDirectories(filePathToCreate);
		LOGGER.debug("Fodlers {} created.Starting with Group change.", filePathToCreate);
		UserPrincipalLookupService lookupService = fileSystem.getUserPrincipalLookupService();
		String group = getGroupFor(filePathToCreate.getName(1).toString());
		LOGGER.debug("group  name - {}", group);				
		GroupPrincipal fileGroup = lookupService.lookupPrincipalByGroupName(group);
		//skip when reaching inbox/outbox
		while(!(filePathToCreate.getFileName().toString().equals("inbox") || filePathToCreate.getFileName().toString().equals("outbox"))){
			
			LOGGER.debug("setting the group of  {} to {}",filePathToCreate, group);	
			Files.getFileAttributeView(filePathToCreate, PosixFileAttributeView.class, LinkOption.NOFOLLOW_LINKS).setGroup(fileGroup);
			Files.setPosixFilePermissions(filePathToCreate, PosixFilePermissions.fromString("rwxrwx---"));
			filePathToCreate = filePathToCreate.getParent();
		 }
		
		LOGGER.debug("Done setting group");
		
	}
	
	private String getGroupFor(String protocol) {
		return MailBoxUtil.getEnvironmentProperties().getString(protocol+".group.name");
 	}

	@Override
	public void updateState() {
		// TODO Auto-generated method stub

	}

	/**
	 * Logging methods for javascript
	 */
	/**
	 *
	 * @param msg
	 */
	@Override
	public void logInfo(String msg) {
		LOGGER.info(msg);
	}

	/**
	 * @param msg
	 */
	@Override
	public void logError(String msg) {
		LOGGER.error(msg);
	}

	/**
	 * @param error
	 */
	@Override
	public void logError(Object error) {
		logError(error.toString());
	}

	/**
	 * @param error
	 */
	@Override
	public void logError(Throwable error) {
		logError(error.getLocalizedMessage());
	}

	/**
	 * @param msg
	 */
	@Override
	public void logDebug(String msg) {
		LOGGER.debug(msg);
	}

}
