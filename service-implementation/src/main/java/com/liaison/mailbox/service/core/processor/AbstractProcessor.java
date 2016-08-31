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

import com.liaison.commons.exception.LiaisonException;
import com.liaison.commons.jaxb.JAXBUtility;
import com.liaison.commons.scripting.javascript.ScriptExecutionEnvironment;
import com.liaison.commons.security.pkcs7.SymmetricAlgorithmException;
import com.liaison.commons.util.client.ftps.G2FTPSClient;
import com.liaison.dto.queue.WorkTicket;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.dtdm.model.Credential;
import com.liaison.mailbox.dtdm.model.Folder;
import com.liaison.mailbox.dtdm.model.MailBoxProperty;
import com.liaison.mailbox.dtdm.model.Processor;
import com.liaison.mailbox.dtdm.model.ProcessorProperty;
import com.liaison.mailbox.enums.CredentialType;
import com.liaison.mailbox.enums.EntityStatus;
import com.liaison.mailbox.enums.ExecutionEvents;
import com.liaison.mailbox.enums.ExecutionState;
import com.liaison.mailbox.enums.FolderType;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.enums.ProcessorType;
import com.liaison.mailbox.rtdm.dao.FSMEventDAOBase;
import com.liaison.mailbox.rtdm.dao.StagedFileDAO;
import com.liaison.mailbox.rtdm.dao.StagedFileDAOBase;
import com.liaison.mailbox.rtdm.model.StagedFile;
import com.liaison.mailbox.service.core.ProcessorConfigurationService;
import com.liaison.mailbox.service.core.email.EmailInfoDTO;
import com.liaison.mailbox.service.core.email.EmailNotifier;
import com.liaison.mailbox.service.core.fsm.MailboxFSM;
import com.liaison.mailbox.service.dto.GlassMessageDTO;
import com.liaison.mailbox.service.dto.configuration.CredentialDTO;
import com.liaison.mailbox.service.dto.configuration.DynamicPropertiesDTO;
import com.liaison.mailbox.service.dto.configuration.FolderDTO;
import com.liaison.mailbox.service.dto.configuration.TriggerProcessorRequestDTO;
import com.liaison.mailbox.service.dto.configuration.processor.properties.ProcessorPropertyUITemplateDTO;
import com.liaison.mailbox.service.dto.configuration.processor.properties.StaticProcessorPropertiesDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.glass.util.MailboxGlassMessageUtil;
import com.liaison.mailbox.service.util.MailBoxUtil;
import com.liaison.mailbox.service.util.ProcessorPropertyJsonMapper;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermissions;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * Base processor type for all type of processors.
 *
 * @author OFS
 */
public abstract class AbstractProcessor implements ProcessorJavascriptI, ScriptExecutionEnvironment {

    private static final Logger LOGGER = LogManager.getLogger(AbstractProcessor.class);
    private static final String FILE_PERMISSION = "rw-rw----";
    private static final String FOLDER_PERMISSION = "rwxrwx---";
    private static final String NO_EMAIL_ADDRESS = "There is no email address configured for this mailbox.";
    private static final String DATA_FOLDER_PATTERN = "com.liaison.data.folder.pattern";
    private static final String DEFAULT_DATA_FOLDER_PATTERN = "glob:/data/{sftp,ftp,ftps}/*/{inbox,outbox}/**";
    private static final String INBOX = "inbox";
    private static final String OUTBOX = "outbox";

    protected static final String seperator = ": ";

    private int scriptExecutionTimeout = 0;

    protected Processor configurationInstance;
    protected String payloadLocation;
    protected int totalNumberOfProcessedFiles;
    protected StringBuilder logPrefix;
    protected TriggerProcessorRequestDTO reqDTO;

    public Properties mailBoxProperties;
    public StaticProcessorPropertiesDTO staticProcessorProperties;

    protected Map<String, StagedFile> stagedFileMap = new HashMap<>();

    public AbstractProcessor() {}

    public AbstractProcessor(Processor configurationInstance) {
        this.configurationInstance = configurationInstance;
    }


    public Processor getConfigurationInstance() {
        return configurationInstance;
    }

    public int getTotalNumberOfProcessedFiles() {
        return totalNumberOfProcessedFiles;
    }

    public void setTotalNumberOfProcessedFiles(int totalNumberOfProcessedFiles) {
        this.totalNumberOfProcessedFiles = totalNumberOfProcessedFiles;
    }

    public void setReqDTO(TriggerProcessorRequestDTO reqDTO) {
        this.reqDTO = reqDTO;
    }

    public TriggerProcessorRequestDTO getReqDTO() {
        return this.reqDTO;
    }

    /**
     * Method to construct log messages for easy visibility
     *
     * @param messages append to prefix, please make sure the order of the inputs
     * @return constructed string
     */
    public String constructMessage(String... messages) {

        if (null == logPrefix) {

            logPrefix = new StringBuilder()
                    .append("CronJob")
                    .append(seperator)
                    .append((reqDTO != null) ? reqDTO.getProfileName() : "NONE")
                    .append(seperator)
                    .append(configurationInstance.getProcessorType().name())
                    .append(seperator)
                    .append(configurationInstance.getProcsrName())
                    .append(seperator)
                    .append(configurationInstance.getMailbox().getMbxName())
                    .append(seperator)
                    .append(configurationInstance.getMailbox().getPguid())
                    .append(seperator);
        }

        StringBuilder msgBuf = new StringBuilder().append(logPrefix);
        for (String str : messages) {
            msgBuf.append(str);
        }

        return msgBuf.toString();
    }

    /**
     * Method to return static properties stored in DB of a processor
     *
     * @return StaticProcessorPropertiesDTO
     * @throws IOException
     * @throws SecurityException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    public StaticProcessorPropertiesDTO getProperties() throws IllegalArgumentException, IllegalAccessException, IOException {

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
     * To Retrieve the Payload URI
     *
     * @throws MailBoxConfigurationServicesException
     * @throws MailBoxServicesException
     */
    public String getPayloadURI() throws MailBoxServicesException {

        if (!MailBoxUtil.isEmpty(payloadLocation)) {
            return payloadLocation;
        }

        if (configurationInstance.getFolders() != null) {

            for (Folder folder : configurationInstance.getFolders()) {

                FolderType foundFolderType = FolderType.findByCode(folder.getFldrType());
                if (null == foundFolderType) {
                    throw new MailBoxServicesException(Messages.FOLDERS_CONFIGURATION_INVALID, Response.Status.CONFLICT);
                } else if (FolderType.PAYLOAD_LOCATION.equals(foundFolderType)) {
                    payloadLocation = replaceTokensInFolderPath(folder.getFldrUri());
                    return payloadLocation;
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
                } else if (ProcessorType.REMOTEUPLOADER.equals(configurationInstance.getProcessorType())
                        && FolderType.PAYLOAD_LOCATION.equals(foundFolderType)) {
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
     */
    public void writeResponseToMailBox(ByteArrayOutputStream response) throws URISyntaxException, IOException, MailBoxServicesException {

        LOGGER.debug("Started writing response");
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
     */
    public void writeResponseToMailBox(ByteArrayOutputStream response, String filename)
            throws URISyntaxException, IOException, MailBoxServicesException {

        LOGGER.debug("Started writing response");
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
        LOGGER.info("Response is successfully written" + file.getAbsolutePath());
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
     */
    public void addorUpdateCustomProperty(String dynamicProperties) throws JAXBException, IOException {

        ProcessorConfigurationService service = new ProcessorConfigurationService();
        DynamicPropertiesDTO dynamicPropertiesDTO = JAXBUtility.unmarshalFromJSON(dynamicProperties, DynamicPropertiesDTO.class);
        service.addOrUpdateProcessorProperties(String.valueOf(configurationInstance.getPrimaryKey()), dynamicPropertiesDTO);
    }

    /**
     * Deletes the given directory
     *
     * @param file directory path
     * @param payloadLocation payload location
     * @throws IOException
     */
    protected void deleteDirectory(File file, String payloadLocation) throws IOException {

        if (file.isDirectory()) {

            // if it is a directory then it will be deleted only if did not contain any files.
            // if the directory is not empty then uploading of files has failed and will not be deleted
            String [] subFiles = file.list();
            if (null != subFiles && subFiles.length == 0) {

                // The directory should not be the actual payload location configured
                if (payloadLocation.equals(file.getPath())) {
                    return;
                }

                file.delete();
                LOGGER.info(constructMessage("Folder {} deleted successfully in the local payload location"), file.getName());
            }
        } else {
            deleteFile(file);
        }
    }

    /**
     * Deletes the given file
     * @param file file obj
     */
    protected void deleteFile(File file) {

        // Delete the local files after successful upload if user opt for it
        if (file.exists()) {
            file.delete();
            LOGGER.info(constructMessage("File {} deleted successfully in the local payload location"), file.getName());
        } else {
            LOGGER.warn(constructMessage("File {} deleted by other process in the local payload location"), file.getName());
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
        LOGGER.debug("The Processed Folder Path is" + processedFolderPath);
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
        sendEmail(toEmailAddrList, subject, emailBody, type, false);
    }

    /**
     * Send email notifications
     *
     * @param toEmailAddrList
     *            The extra receivers. The default receiver will be available in the mailbox.
     * @param subject
     *            The notification subject
     * @param emailBody
     *            The body of the notification
     * @param type
     *            The notification type(TEXT/HTML).
     * @param isOverwrite
     *            To overwrite the configured mail address in the mailbox.
     */
    public void sendEmail(List<String> toEmailAddrList, String subject, String emailBody, String type, boolean isOverwrite) {

        if (isOverwrite) {

            if (!MailBoxUtil.isEmptyList(toEmailAddrList)) {
                constructAndSendEmail(toEmailAddrList, emailBody, subject, type);
            } else {
                LOGGER.debug(NO_EMAIL_ADDRESS);
                return;
            }
        } else {

            List<String> configuredEmailAddress = configurationInstance.getEmailAddress();
            boolean configuredEmailList = MailBoxUtil.isEmptyList(configuredEmailAddress);
            boolean toEmailList = MailBoxUtil.isEmptyList(toEmailAddrList);

            if ((configuredEmailList) && (toEmailList)) {
                LOGGER.debug(NO_EMAIL_ADDRESS);
                return;
            }

            if (!configuredEmailList && !toEmailList) {
                toEmailAddrList.addAll(configuredEmailAddress);
            } else if (!configuredEmailList) {
                toEmailAddrList = configuredEmailAddress;
            }

            constructAndSendEmail(toEmailAddrList, emailBody, subject, type);
        }
    }

    /**
     * Construct and send the email.
     *
     * @param toEmailAddrList
     * @param emailBody
     * @param subject
     * @param type
     */
    private void constructAndSendEmail(List<String> toEmailAddrList,
                                       String emailBody, String subject, String type) {

        // construct the email helper dto which contains all details
        EmailInfoDTO emailInfoDTO = new EmailInfoDTO();
        emailInfoDTO.setEmailBody(emailBody);
        emailInfoDTO.setSubject(subject);
        emailInfoDTO.setType(type);
        emailInfoDTO.setToEmailAddrList(toEmailAddrList);

        EmailNotifier.sendEmail(emailInfoDTO);
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
     * @param fileName The source location
     * @throws IOException
     * @throws URISyntaxException
     * @throws MailBoxServicesException
     */
    public void createResponseDirectory(String fileName) throws URISyntaxException, IOException, MailBoxServicesException {

        LOGGER.debug("Started writing response");

        String responseLocation = getWriteResponseURI();
        if (MailBoxUtil.isEmpty(responseLocation)) {
            throw new MailBoxServicesException(Messages.LOCATION_NOT_CONFIGURED, MailBoxConstants.RESPONSE_LOCATION, Response.Status.CONFLICT);
        }

        File directory = new File(responseLocation);
        if (!directory.exists()) {
            Files.createDirectories(directory.toPath());
        }

        if (MailBoxUtil.isEmpty(fileName)) {

            LOGGER.error("The given URI {} does not exist.", fileName);
            throw new MailBoxServicesException("The given URI '" + fileName + "' does not exist.", Response.Status.CONFLICT);
        }
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
        String pattern = MailBoxUtil.getEnvironmentProperties().getString(DATA_FOLDER_PATTERN, DEFAULT_DATA_FOLDER_PATTERN);
        PathMatcher pathMatcher = fileSystem.getPathMatcher(pattern);
        if(!pathMatcher.matches(filePathToCreate)){
            throw new MailBoxConfigurationServicesException(Messages.FOLDER_DOESNT_MATCH_PATTERN, pattern.substring(5), Response.Status.BAD_REQUEST);
        }

        //check availability of /data/*/* folder
        if(!Files.exists(filePathToCreate.subpath(0, 3))){
            throw new MailBoxConfigurationServicesException(Messages.HOME_FOLDER_DOESNT_EXIST_ALREADY,filePathToCreate.subpath(0, 3).toString(), Response.Status.BAD_REQUEST);
        }

        createFoldersAndAssignProperPermissions(filePathToCreate);
    }

    private String getGroupFor(String protocol) {
        return MailBoxUtil.getEnvironmentProperties().getString(protocol+".group.name");
    }

    /**
     * Returns false if file is excluded. Otherwise returns true. Include is higher priority then exclude.
     * @param includedFiles - List of extensions to be included
     * @param currentFileName - name of the file to be uploaded
     * @param excludedFiles - List of extensions to be excluded
     * @return boolean - uploading or downloading or directory sweeping process takes place only if it is true.
     */
    public boolean checkFileIncludeorExclude(String includedFiles, String currentFileName, String excludedFiles) {

        List<String> includeList = (!MailBoxUtil.isEmpty(includedFiles)) ? Arrays.asList(includedFiles.split(",")) : null;
        List<String> excludedList = (!MailBoxUtil.isEmpty(excludedFiles)) ? Arrays.asList(excludedFiles.split(",")) : null;

        //Add period to fileExtension since include/exclude list contains extension with period
        String fileExtension = "." + FilenameUtils.getExtension(currentFileName);
        //check if file is in include list
        if(null != includeList && !includeList.isEmpty()) {
            boolean fileIncluded = (includeList.contains(fileExtension))? true : false;
            return fileIncluded;
        }

        //check if file is not in excluded list
        if(null != excludedList && !excludedList.isEmpty() && excludedList.contains(fileExtension)) {
            return false;
        }
        return true;
    }

    /**
     * Method to create the given path and assign proper group and permissions to the created folders
     *
     * @param filePathToCreate - file Path which is to be created
     * @throws IOException
     */
    protected void createFoldersAndAssignProperPermissions(Path filePathToCreate)
            throws IOException {

        FileSystem fileSystem = FileSystems.getDefault();
        Files.createDirectories(filePathToCreate);
        LOGGER.debug("Fodlers {} created.Starting with Group change.", filePathToCreate);
        UserPrincipalLookupService lookupService = fileSystem.getUserPrincipalLookupService();
        String group = getGroupFor(filePathToCreate.getName(1).toString());
        LOGGER.debug("group  name - {}", group);
        GroupPrincipal fileGroup = lookupService.lookupPrincipalByGroupName(group);

        // skip when reaching inbox/outbox
        String pathToCreate = filePathToCreate.getFileName().toString();
        while (!(INBOX.equals(pathToCreate) ||
                OUTBOX.equals(pathToCreate))) {

            LOGGER.debug("setting the group of  {} to {}", filePathToCreate, group);
            Files.getFileAttributeView(filePathToCreate, PosixFileAttributeView.class, LinkOption.NOFOLLOW_LINKS).setGroup(fileGroup);
            Files.setPosixFilePermissions(filePathToCreate, PosixFilePermissions.fromString(FOLDER_PERMISSION));

            // if it is PROCESSED/ERROR Folder then skip assigning permissions to parent folders.
            if ((MailBoxConstants.PROCESSED_FOLDER.equals(pathToCreate) ||
                    MailBoxConstants.ERROR_FOLDER.equals(pathToCreate))) {
                LOGGER.debug("setting file permissions of PROCESSED/ERROR Folder is done. Skipping permission setting for parent folders as it is not needed.");
                break;
            }

            filePathToCreate = filePathToCreate.getParent();
            pathToCreate = filePathToCreate.getFileName().toString();
        }

        LOGGER.debug("Done setting group");
    }

    /**
     * Logs TVAPI status and event message in LENS
     *
     * @param message Message String to be logged in LENS event log
     * @param file java.io.File
     * @param status Status of the LENS logging
     */
    protected void logGlassMessage(String message, File file, ExecutionState status) {

        StagedFileDAO stagedFileDAO = new StagedFileDAOBase();
        StagedFile stagedFile = stagedFileDAO.findStagedFilesByProcessorId(configurationInstance.getPguid(), file.getParent(), file.getName());

        if (null != stagedFile) {

            if (updateStagedFileStatus(status, stagedFileDAO, stagedFile)) {
                return;
            }

            GlassMessageDTO glassMessageDTO = new GlassMessageDTO();
            glassMessageDTO.setGlobalProcessId(stagedFile.getGPID());
            glassMessageDTO.setProcessorType(configurationInstance.getProcessorType());
            glassMessageDTO.setProcessProtocol(configurationInstance.getProcsrProtocol());
            glassMessageDTO.setFileName(file.getName());
            glassMessageDTO.setFilePath(file.getPath());
            glassMessageDTO.setFileLength(file.length());
            glassMessageDTO.setStatus(status);
            glassMessageDTO.setMessage(message.toString());
            glassMessageDTO.setPipelineId(null);
            glassMessageDTO.setFirstCornerTimeStamp(null);
            
            MailboxGlassMessageUtil.logGlassMessage(glassMessageDTO);
                   
        }
    }

    protected boolean updateStagedFileStatus(ExecutionState status, StagedFileDAO stagedFileDAO, StagedFile stagedFile) {

        // Log running status
        if (ExecutionState.COMPLETED.equals(status)) {

            // Inactivate the stagedFile
            stagedFile.setStagedFileStatus(EntityStatus.INACTIVE.value());
            stagedFileDAO.merge(stagedFile);

        } else {

            String configCount = (String) getMailBoxProperties().get(MailBoxConstants.LENS_NOTIFICATION_FOR_UPLOADER_FAILURE);
            int maxCount = (MailBoxUtil.isEmpty(configCount))
                    ? MailBoxUtil.getEnvironmentProperties().getInt(MailBoxConstants.DEFAULT_LENS_FAILURE_NOTIFICATION_COUNT, 3)
                    : Integer.valueOf(configCount);

            // Update failure status only on notified times
            if (null == stagedFile.getFailureNotificationCount()) {
                stagedFile.setFailureNotificationCount(1);
                stagedFileDAO.merge(stagedFile);
            } else if (maxCount > stagedFile.getFailureNotificationCount().intValue()) {
                // Notification count update
                stagedFile.setFailureNotificationCount((stagedFile.getFailureNotificationCount() + 1));
                stagedFileDAO.merge(stagedFile);
            } else {
                return true;
            }

        }
        return false;
    }

    /**
     * To update staged file status.
     *
     * @param workticket
     * @param status
     */
    public void updateStagedFileStatus(WorkTicket workticket, String status) {

        StagedFileDAOBase dao = new StagedFileDAOBase();
        String path = workticket.getAdditionalContext().get(MailBoxConstants.KEY_FILE_PATH).toString();
        StagedFile stagedFile = dao.findStagedFilesByProcessorId(configurationInstance.getPguid(), path, workticket.getFileName());

        if (null != stagedFile) {

            stagedFile.setStagedFileStatus(status);
            stagedFile.setFailureNotificationCount(1);
            dao.merge(stagedFile);
        }
    }

    /**
     * Logs duplicate status in lens for the overwrite true case
     *
     * @param fileName
     * @param filePath
     * @param dupGpid
     *            the global process id of the old transaction
     * @param gpid
     *            the global process id of the current transaction which is going to overwrite the old txn
     */
    protected void logDuplicateStatus(String fileName, String filePath, String dupGpid, String gpid) {

        StringBuilder message = new StringBuilder()
                .append("File ")
                .append(fileName)
                .append(" is overwritten by another process - ")
                .append(gpid);
        
        GlassMessageDTO glassMessageDTO = new GlassMessageDTO();
        glassMessageDTO.setGlobalProcessId(dupGpid);
        glassMessageDTO.setProcessorType(configurationInstance.getProcessorType());
        glassMessageDTO.setProcessProtocol(configurationInstance.getProcsrProtocol());
        glassMessageDTO.setFileName(fileName);
        glassMessageDTO.setFilePath(filePath);
        glassMessageDTO.setFileLength(0);
        glassMessageDTO.setStatus(ExecutionState.DUPLICATE);
        glassMessageDTO.setMessage(message.toString());
        glassMessageDTO.setPipelineId(null);
        glassMessageDTO.setFirstCornerTimeStamp(null);

        MailboxGlassMessageUtil.logGlassMessage(glassMessageDTO);
    }

    @Override
    public void logToLens(String msg, File file, ExecutionState status) {
        throw new RuntimeException("Not Implemented");
    }

    /**
     * Checks processor has the interrupt signal
     *
     * @param executionId execution id
     * @param fsm FSM instance
     * @return true if there is an interrupt signal
     */
    protected boolean isThereAnInterruptSignal(String executionId, MailboxFSM fsm) {

        FSMEventDAOBase eventDAO = new FSMEventDAOBase();
        if(eventDAO.isThereAInterruptSignal(executionId)) {

            LOGGER.debug("The executor with execution id  " + executionId + " is gracefully interrupted");
            fsm.createEvent(ExecutionEvents.INTERRUPTED, executionId);
            fsm.handleEvent(fsm.createEvent(ExecutionEvents.INTERRUPTED));
            return true;
        }
        return false;
    }

    /**
     * disconnect the ftp client
     *
     * @param ftpsRequest ftp/s client
     */
    protected void disconnect(G2FTPSClient ftpsRequest) {
        if (ftpsRequest != null) {
            try {
                ftpsRequest.disconnect();
            } catch (LiaisonException e) {
                LOGGER.error(constructMessage("Error occurred during disconnect FTPSClient", seperator, e.getMessage()), e);
                throw new RuntimeException(e);
            }
        }
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
        LOGGER.error(error.getLocalizedMessage(), error);
    }

    /**
     * @param msg
     */
    @Override
    public void logDebug(String msg) {
        LOGGER.debug(msg);
    }

    @Override
    public File[] getFilesToUpload(boolean recurseSubDirs) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * validates remote path
     *
     * @return remote path
     * @throws IOException
     */
    protected String validateRemotePath() {

        String remotePath = getWriteResponseURI();
        if (MailBoxUtil.isEmpty(remotePath)) {
            throw new MailBoxServicesException("The given remote path is Empty.", Response.Status.CONFLICT);
        }
        return remotePath;
    }

    /**
     * Validates the local path
     *
     * @return local path
     * @throws IOException
     */
    protected String validateLocalPath() {

        String path = getPayloadURI();
        if (MailBoxUtil.isEmpty(path)) {
            throw new MailBoxServicesException("The given local path is Empty.", Response.Status.CONFLICT);
        }
        return path;
    }

    public void setMaxExecutionTimeout(int executionTimeout) {
        this.scriptExecutionTimeout = executionTimeout;
    }
    
    @Override
    public int getMaxExecutionTimeout() {
        return (int) TimeUnit.MINUTES.toMillis(scriptExecutionTimeout);
    }
    
    @Override
    public String getOrganization() {
        return this.getConfigurationInstance().getMailbox().getTenancyKey();
    }
}