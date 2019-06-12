/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 * <p>
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
import com.liaison.commons.util.client.ftps.G2FTPSClient;
import com.liaison.commons.util.ISO8601Util;
import com.liaison.dto.queue.WorkTicket;
import com.liaison.fs2.metadata.FS2MetaSnapshot;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.dtdm.model.Credential;
import com.liaison.mailbox.dtdm.model.Folder;
import com.liaison.mailbox.dtdm.model.MailBoxProperty;
import com.liaison.mailbox.dtdm.model.Processor;
import com.liaison.mailbox.dtdm.model.ProcessorProperty;
import com.liaison.mailbox.enums.CredentialType;
import com.liaison.mailbox.enums.EntityStatus;
import com.liaison.mailbox.enums.ExecutionState;
import com.liaison.mailbox.enums.FolderType;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.enums.ProcessorType;
import com.liaison.mailbox.rtdm.dao.ProcessorExecutionStateDAOBase;
import com.liaison.mailbox.rtdm.dao.StagedFileDAO;
import com.liaison.mailbox.rtdm.dao.StagedFileDAOBase;
import com.liaison.mailbox.rtdm.model.ProcessorExecutionState;
import com.liaison.mailbox.rtdm.model.StagedFile;
import com.liaison.mailbox.service.core.ProcessorConfigurationService;
import com.liaison.mailbox.service.core.email.EmailInfoDTO;
import com.liaison.mailbox.service.core.email.EmailNotifier;
import com.liaison.mailbox.service.dto.GlassMessageDTO;
import com.liaison.mailbox.service.dto.configuration.CredentialDTO;
import com.liaison.mailbox.service.dto.configuration.DynamicPropertiesDTO;
import com.liaison.mailbox.service.dto.configuration.FolderDTO;
import com.liaison.mailbox.service.dto.configuration.TriggerProcessorRequestDTO;
import com.liaison.mailbox.service.dto.configuration.processor.properties.FTPUploaderPropertiesDTO;
import com.liaison.mailbox.service.dto.configuration.processor.properties.HTTPDownloaderPropertiesDTO;
import com.liaison.mailbox.service.dto.configuration.processor.properties.HTTPUploaderPropertiesDTO;
import com.liaison.mailbox.service.dto.configuration.processor.properties.SFTPUploaderPropertiesDTO;
import com.liaison.mailbox.service.dto.configuration.processor.properties.StaticProcessorPropertiesDTO;
import com.liaison.mailbox.service.dto.configuration.processor.properties.SweeperPropertiesDTO;
import com.liaison.mailbox.service.dto.remote.uploader.RelayFile;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.glass.util.ExecutionTimestamp;
import com.liaison.mailbox.service.glass.util.MailboxGlassMessageUtil;
import com.liaison.mailbox.service.storage.util.StorageUtilities;
import com.liaison.mailbox.service.util.DirectoryCreationUtil;
import com.liaison.mailbox.service.util.MailBoxUtil;
import com.liaison.mailbox.service.util.ProcessorPropertyJsonMapper;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.Comparator;
import java.util.Date;

import static com.liaison.mailbox.MailBoxConstants.FTP;
import static com.liaison.mailbox.MailBoxConstants.FTPS;
import static com.liaison.mailbox.MailBoxConstants.HTTP;
import static com.liaison.mailbox.MailBoxConstants.HTTPS;
import static com.liaison.mailbox.MailBoxConstants.SFTP;

/**
 * Base processor type for all type of processors.
 *
 * @author OFS
 */
public abstract class AbstractProcessor implements ProcessorJavascriptI, ScriptExecutionEnvironment {

    private static final Logger LOGGER = LogManager.getLogger(AbstractProcessor.class);
    private static final String NO_EMAIL_ADDRESS = "There is no email address configured for this mailbox.";
    private static final String WILD_CARD = "*";
    private static final String DOT = ".";
    private static final Object SORT_BY_NAME = "Name";
    private static final Object SORT_BY_SIZE = "Size";

    protected static final String seperator = ": ";

    private int scriptExecutionTimeout = 0;

    protected Processor configurationInstance;
    protected String payloadLocation;
    protected int totalNumberOfProcessedFiles;
    protected StringBuilder logPrefix;
    protected TriggerProcessorRequestDTO reqDTO;

    public Properties mailBoxProperties;
    public StaticProcessorPropertiesDTO staticProcessorProperties;

    private boolean directUploadEnabled;
    private boolean useFileSystem;
    private String responseFs2Uri;
    private String pipelineId;

    protected Map<String, StagedFile> stagedFileMap = new HashMap<>();

    public AbstractProcessor() {
    }

    public AbstractProcessor(Processor configurationInstance) {

        this.configurationInstance = configurationInstance;
        if (null != configurationInstance.getProcsrProperties()) {
            setDirectUploadEnabled(MailBoxUtil.isDirectUploadEnabled(configurationInstance.getProcsrProperties()));
            setUseFileSystem(MailBoxUtil.isUseFileSystemEnabled(configurationInstance.getProcsrProperties()));
        }
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

    public boolean isDirectUploadEnabled() {
        return directUploadEnabled;
    }

    private void setDirectUploadEnabled(boolean directUploadEnabled) {
        this.directUploadEnabled = directUploadEnabled;
    }

    protected boolean canUseFileSystem() {
        return useFileSystem;
    }

    public void setUseFileSystem(boolean canUseFileSystem) {
        this.useFileSystem = canUseFileSystem;
    }

    public String getResponseFs2Uri() {
        return responseFs2Uri;
    }

    public void setResponseFs2Uri(String responseFs2Uri) {
        this.responseFs2Uri = responseFs2Uri;
    }

    public void setResponseUri(String uri) {
        this.setResponseFs2Uri(uri);
    }

    public void setPipeLineID(String pipeLineID) {
        this.pipelineId = pipeLineID;
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
     * @throws IllegalAccessException
     */
    public StaticProcessorPropertiesDTO getProperties() throws IOException, IllegalAccessException {

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
     */
    public CredentialDTO[] getCredentials() throws MailBoxConfigurationServicesException {

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
     */
    public FolderDTO[] getFolders() {

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
     */
    public String getPayloadURI() {

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
    public String getFileWriteLocation() {

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
     */
    public Properties getMailBoxProperties() {

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
     * @throws IOException
     */
    public void writeResponseToMailBox(ByteArrayOutputStream response) throws IOException, MailBoxServicesException {

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
     * @throws IOException
     */
    public void writeResponseToMailBox(ByteArrayOutputStream response, String filename)
            throws IOException, MailBoxServicesException {

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
        
        HTTPDownloaderPropertiesDTO staticProp = null;
        try {
            staticProp = (HTTPDownloaderPropertiesDTO) getProperties();
        } catch (IllegalAccessException e) {
            LOGGER.debug("Caught exception while getting HTTP Downloader properties");
        }
        // async sweeper process if direct submit is true.
        if (staticProp != null && staticProp.isDirectSubmit()) {
            // sweep single file process to SB queue
            String globalProcessorId = sweepFile(file);
            LOGGER.info("File posted to sweeper event queue and the Global Process Id {}", globalProcessorId);
        }
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
     */
    protected void deleteDirectory(File file, String payloadLocation) {

        if (file.isDirectory()) {

            // if it is a directory then it will be deleted only if did not contain any files.
            // if the directory is not empty then uploading of files has failed and will not be deleted
            String[] subFiles = file.list();
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
    public String replaceTokensInFolderPath(String folderPath) {

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
     */
    public Credential getCredentialOfSpecificType(CredentialType type) throws MailBoxServicesException {

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
     * @throws MailBoxServicesException
     */
    public void createResponseDirectory(String fileName) throws IOException, MailBoxServicesException {

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
     * Returns false if file is excluded. Otherwise returns true. Include is higher priority then exclude.
     * @param includedFiles - List of extensions to be included
     * @param currentFileName - name of the file to be uploaded
     * @param excludedFiles - List of extensions to be excluded
     * @return boolean - uploading or downloading or directory sweeping process takes place only if it is true.
     */
    public boolean checkFileIncludeOrExclude(String includedFiles, String currentFileName, String excludedFiles) {

        List<String> includedList = (!MailBoxUtil.isEmpty(includedFiles)) ? Arrays.asList(includedFiles.split(",")) : null;
        List<String> excludedList = (!MailBoxUtil.isEmpty(excludedFiles)) ? Arrays.asList(excludedFiles.split(",")) : null;

        // check if file is in include list
        if (!CollectionUtils.isEmpty(includedList)) {
            return (validateExtension(includedList, currentFileName)
                    || validateWildcard(includedList, currentFileName));
        }
        
        // check if file is not in excluded list
        return !(!CollectionUtils.isEmpty(excludedList) &&
                (validateExtension(excludedList, currentFileName)
                        || validateWildcard(excludedList, currentFileName)));
    }
    
    /**
     * Method to validate the filename extension matches with the given list extensions.
     * 
     * @param list
     * @param fileName
     * @return true if contains extension else false.
     */
    private boolean validateExtension(List<String> list, String fileName) {
        
        List<String> legacyFilter = list.stream()
                .filter(line -> !line.contains(WILD_CARD))
                .map(line -> (line.contains(DOT) ? line.replace(DOT, "") : line))
                .collect(Collectors.toList());
        
        // returns true if the filename matches the extension otherwise false
        return legacyFilter.stream()
                .filter(line -> FilenameUtils.isExtension(fileName, line))
                .collect(Collectors.toList())
                .size() > 0;
    }
    
    /**
     * Method to validate the filename matches with the given list wildcard.
     * 
     * @param list
     * @param fileName
     * @return true if matches wildcard else false.
     */
    private boolean validateWildcard(List<String> list, String fileName) {
        
        List<String> wildcardFilter = list.stream()
                .filter(line -> line.contains(WILD_CARD))
                .collect(Collectors.toList());
        
        // returns true if the filename matches the wildcard otherwise false
        return wildcardFilter.stream()
                .filter(line -> FilenameUtils.wildcardMatch(fileName, line))
                .collect(Collectors.toList())
                .size() > 0;
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
            } else if (maxCount > stagedFile.getFailureNotificationCount()) {
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
        StagedFile stagedFile = dao.findStagedFileByGpid(workticket.getGlobalProcessId());
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
        glassMessageDTO.setProcessorType(configurationInstance.getProcessorType(), getCategory());
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

        StagedFileDAO stagedFileDAO = new StagedFileDAOBase();
        StagedFile stagedFile = stagedFileDAO.findStagedFilesByProcessorId(configurationInstance.getPguid(), file.getParent(), file.getName());

        if (null != stagedFile) {

            if (updateStagedFileStatus(status, stagedFileDAO, stagedFile)) {
                return;
            }

            GlassMessageDTO glassMessageDTO = new GlassMessageDTO();
            glassMessageDTO.setGlobalProcessId(stagedFile.getGPID());
            glassMessageDTO.setProcessorType(configurationInstance.getProcessorType(), getCategory());
            glassMessageDTO.setProcessProtocol(configurationInstance.getProcsrProtocol());
            glassMessageDTO.setFileName(file.getName());
            glassMessageDTO.setFilePath(file.getPath());
            glassMessageDTO.setFileLength(file.length());
            glassMessageDTO.setStatus(status);
            glassMessageDTO.setMessage(msg);
            glassMessageDTO.setPipelineId(null);
            glassMessageDTO.setFirstCornerTimeStamp(null);

            //sets receiver ip
            glassMessageDTO.setReceiverIp(getHost());

            MailboxGlassMessageUtil.logGlassMessage(glassMessageDTO);

        }
    }

    @Override
    public void logToLens(String msg, File file, ExecutionState status, Exception e) {
        LOGGER.info("Entered into log To Lens Method " + configurationInstance.getPguid() + " : " +  file.getParent() + " : " +  file.getName() + " : " +  ExceptionUtils.getStackTrace(e));
        StagedFileDAO stagedFileDAO = new StagedFileDAOBase();
        StagedFile stagedFile = stagedFileDAO.findStagedFilesByProcessorId(configurationInstance.getPguid(), file.getParent(), file.getName());
         
        if (null != stagedFile) {
            LOGGER.info("Staged File Name - {}", stagedFile.getFileName());
            if (updateStagedFileStatus(status, stagedFileDAO, stagedFile)) {
                return;
            }
            GlassMessageDTO glassMessageDTO = new GlassMessageDTO();
            glassMessageDTO.setGlobalProcessId(stagedFile.getGPID());
            glassMessageDTO.setProcessorType(configurationInstance.getProcessorType(), getCategory());
            glassMessageDTO.setProcessProtocol(configurationInstance.getProcsrProtocol());
            glassMessageDTO.setFileName(file.getName());
            glassMessageDTO.setFilePath(file.getPath());
            glassMessageDTO.setFileLength(file.length());
            glassMessageDTO.setStatus(status);
            glassMessageDTO.setMessage(msg);
            glassMessageDTO.setPipelineId(null);
            glassMessageDTO.setFirstCornerTimeStamp(null);
            
            if (null != e) {
                glassMessageDTO.setTechDescription(ExceptionUtils.getStackTrace(e));
                LOGGER.info("Admin error Details -------- {}", glassMessageDTO.getTechDescription());
            }    
             //sets receiver ip
            glassMessageDTO.setReceiverIp(getHost());
            MailboxGlassMessageUtil.logGlassMessage(glassMessageDTO);
         }
    }

    @Override
    public void logToLens(String msg, RelayFile file, ExecutionState status) {
        throw new RuntimeException("Not Implemented");
    }

    @Override
    public void logToLens(String msg, RelayFile file, ExecutionState status, Exception e) {
        throw new RuntimeException("Not Implemented");
    }

    /**
     * To store the response stream in storage
     *
     * @param stream
     * @return String - fs2 uri
     * @throws Exception
     */
    public String persistResponse(InputStream stream, RelayFile file) {

        WorkTicket workTicket = new WorkTicket();
        workTicket.setGlobalProcessId(file.getGlobalProcessId());
        if (StringUtils.isNotEmpty(file.getTenancyKey())) {
            workTicket.setAdditionalContext(MailBoxConstants.KEY_TENANCY_KEY, file.getTenancyKey());
        }
        FS2MetaSnapshot response = StorageUtilities.persistPayload(stream, workTicket, new HashMap<>());
        return response.getURI().toString();
    }

    /**
     * To store the response stream in storage
     *
     * @param stream
     * @return String - fs2 uri
     * @throws Exception
     */
    public String persistResponse(ByteArrayOutputStream stream, RelayFile file) {

        WorkTicket workTicket = new WorkTicket();
        workTicket.setGlobalProcessId(file.getGlobalProcessId());
        if (StringUtils.isNotEmpty(file.getTenancyKey())) {
            workTicket.setAdditionalContext(MailBoxConstants.KEY_TENANCY_KEY, file.getTenancyKey());
        }
        FS2MetaSnapshot response = StorageUtilities.persistPayload(stream, workTicket, new HashMap<>());
        return response.getURI().toString();
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

    @Override
    public RelayFile[] getRelayFiles(boolean recurseSubDirs) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public File getTriggerFile(String triggerFileName) {
        throw new RuntimeException("Not implemented");
    }
    
    @Override
    public RelayFile getRelayTriggerFile(String triggerFileName) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void deleteTriggerFile(File triggerFile) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void deleteRelayTriggerFile(RelayFile relayFile) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public boolean isClassicSweeper() {
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
        if (MailBoxUtil.isEmpty(path) && !directUploadEnabled) {
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

    /**
     * This method used to check the interrupt status of a thread.
     *
     * @return boolean true if it is interrupted
     */
    public boolean isProcessorInterrupted() {

        String processorId = getReqDTO().getProcessorId();
        ProcessorExecutionStateDAOBase processorDao = new ProcessorExecutionStateDAOBase();
        ProcessorExecutionState processorExecutionState = processorDao.findByProcessorId(processorId);
        return MailBoxUtil.isInterrupted(processorExecutionState.getThreadName());
    }

    /**
     * Reads the host from url for lens logging
     * Handles the exception gracefully
     *
     * @return host
     */
    protected String getHost() {

        try {

            String url;
            switch (configurationInstance.getProcsrProtocol().toUpperCase()) {
                case SFTP:
                    url = ((SFTPUploaderPropertiesDTO) getProperties()).getUrl();
                    break;
                case FTP:
                case FTPS:
                    url = ((FTPUploaderPropertiesDTO) getProperties()).getUrl();
                    break;
                case HTTP:
                case HTTPS:
                    url = ((HTTPUploaderPropertiesDTO) getProperties()).getUrl();
                    break;
                default:
                    return null;
            }

            URI uri = new URI(url);
            return uri.getHost();
        } catch (URISyntaxException | IllegalAccessException | IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * Get category from the properties json
     * @return category string
     */
    protected String getCategory() {
        return MailBoxUtil.getCategory(configurationInstance.getProcsrProperties());
    }

    /**
     * Logs the TVAPI and ActivityStatus messages to LENS. This will be invoked for each file.
     *
     * @param wrkTicket workticket for logging
     * @param firstCornerTimeStamp first corner timestamp
     * @param state Execution Status
     */
    protected void logToLens(WorkTicket wrkTicket, ExecutionTimestamp firstCornerTimeStamp, ExecutionState state, Date date) {
         String filePath = wrkTicket.getAdditionalContextItem(MailBoxConstants.KEY_FOLDER_NAME).toString();
        StringBuilder message;
        if (ExecutionState.VALIDATION_ERROR.equals(state)) {
            message = new StringBuilder().append("File size is empty ").append(filePath).append(", and empty files are not allowed");
        } else {
            message = new StringBuilder().append("Starting to sweep input folder ").append(filePath).append(" for new files");
        }
        GlassMessageDTO glassMessageDTO = new GlassMessageDTO();
        glassMessageDTO.setGlobalProcessId(wrkTicket.getGlobalProcessId());
        glassMessageDTO.setProcessorType(configurationInstance.getProcessorType(), getCategory());
        glassMessageDTO.setProcessProtocol(configurationInstance.getProcsrProtocol());
        glassMessageDTO.setFileName(wrkTicket.getFileName());
        glassMessageDTO.setFilePath(filePath);
        glassMessageDTO.setFileLength(wrkTicket.getPayloadSize());
        glassMessageDTO.setStatus(state);
        glassMessageDTO.setMessage(message.toString());
        glassMessageDTO.setPipelineId(wrkTicket.getPipelineId());
        if (null != firstCornerTimeStamp) {
            glassMessageDTO.setFirstCornerTimeStamp(firstCornerTimeStamp);
        }
        if (null != date) {
            glassMessageDTO.setStatusDate(date);
            LOGGER.debug("The date value is {}", date.getTime());
        }
         MailboxGlassMessageUtil.logGlassMessage(glassMessageDTO);
    }

    /**
     * This Method create local folders if not available and returns the path.
     *
     * @param processorDTO it have details of processor
     *
     */
    @Override
    public String createLocalPath() {
         String configuredPath = null;
        try {
            configuredPath = getPayloadURI();
            DirectoryCreationUtil.createPathIfNotAvailable(configuredPath);
            return configuredPath;
         } catch (IOException e) {
            throw new MailBoxConfigurationServicesException(Messages.LOCAL_FOLDERS_CREATION_FAILED, configuredPath, Response.Status.BAD_REQUEST, e.getMessage());
        }
    }

    /**
     * Method to sort work tickets based on name/size/date
     * 
     * @param workTickets worktickets
     * @param sortType sort type
     */
    protected void sortWorkTicket(List<WorkTicket> workTickets, String sortType) {
    
        if (SORT_BY_NAME.equals(sortType)) {
             workTickets.sort(Comparator.comparing(WorkTicket::getFileName));
        } else if(SORT_BY_SIZE.equals(sortType)) {
            workTickets.sort(Comparator.comparing(WorkTicket::getPayloadSize));
        } else {
            ISO8601Util dateUtil = new ISO8601Util();
            workTickets.sort(Comparator.comparing(w -> dateUtil.fromDate(w.getCreatedTime())));
        }
    }

    /**
     * Verifies the payload size
     *
     * @param workTicket workticket
     * @return true if payload size is not 0
     */
    protected boolean isPayloadValid(WorkTicket workTicket) {
        return !(0 == workTicket.getPayloadSize());
    }

    /**
     * logs sweeper failed status
     * @param workTicket workticket
     * @param e exception
     */
    protected void logSweeperFailedStatus(WorkTicket workTicket, Exception e) {
        GlassMessageDTO glassMessageDTO = new GlassMessageDTO();
        glassMessageDTO.setGlobalProcessId(workTicket.getGlobalProcessId());
        glassMessageDTO.setProcessorType(configurationInstance.getProcessorType(), getCategory());
        glassMessageDTO.setProcessProtocol(configurationInstance.getProcsrProtocol());
        glassMessageDTO.setFileName(workTicket.getFileName());
        glassMessageDTO.setStatus(ExecutionState.FAILED);
        glassMessageDTO.setPipelineId(workTicket.getPipelineId());
        glassMessageDTO.setMessage(e.getMessage());
        MailboxGlassMessageUtil.logGlassMessage(glassMessageDTO);
    }

    /**
     * Method is used to map the values with Mailbox constants variables.
     * 
     * @param staticProp
     * @param workTicket
     * @return properties
     */
    protected Map<String, String> setProperties(SweeperPropertiesDTO staticProp, WorkTicket workTicket) {
        Map<String, String> properties = new HashMap<>();
        Map<String, String> ttlMap = configurationInstance.getTTLUnitAndTTLNumber();
        if (!ttlMap.isEmpty()) {
            Integer ttlNumber = Integer.parseInt(ttlMap.get(MailBoxConstants.TTL_NUMBER));
            workTicket.setTtlDays(MailBoxUtil.convertTTLIntoDays(ttlMap.get(MailBoxConstants.CUSTOM_TTL_UNIT), ttlNumber));
        }
         properties.put(MailBoxConstants.PROPERTY_HTTPLISTENER_SECUREDPAYLOAD, String.valueOf(staticProp.isSecuredPayload()));
        properties.put(MailBoxConstants.PROPERTY_LENS_VISIBILITY, String.valueOf(staticProp.isLensVisibility()));
        properties.put(MailBoxConstants.KEY_PIPELINE_ID, staticProp.getPipeLineID());
        properties.put(MailBoxConstants.STORAGE_IDENTIFIER_TYPE, MailBoxUtil.getStorageType(configurationInstance.getDynamicProperties()));
         String contentType = MailBoxUtil.isEmpty(staticProp.getContentType()) ? MediaType.TEXT_PLAIN : staticProp.getContentType();
        properties.put(MailBoxConstants.CONTENT_TYPE, contentType);
        workTicket.addHeader(MailBoxConstants.CONTENT_TYPE.toLowerCase(), contentType);
        LOGGER.info(constructMessage("Sweeping file {}"), workTicket.getPayloadURI());
        return properties;
    }

    /**
     * Method to get the pipe line id from the remote processor properties.
     *
     * @return pipelineId
     * @throws IllegalAccessException
     * @throws IOException
     */
    protected String getPipeLineID() throws IOException, IllegalAccessException {
        if (MailBoxUtil.isEmpty(pipelineId)) {
            SweeperPropertiesDTO sweeperStaticProperties = (SweeperPropertiesDTO) getProperties();
            this.setPipeLineID(sweeperStaticProperties.getPipeLineID());
        }
        return pipelineId;
    }

    /**
     * Method to use single file sweeps to storage utilities and also post workticket to service broker queue
     * 
     * @param file downloaded File
     * @param staticProp SFTPDownloaderprocessor properties.
     */
    @Override
    public String sweepFile(File file) {
    	throw new RuntimeException("Not Implemented");
    }

    /**
     * Method to use list of downloaded files sweeps to storage Utilities and also post the worktickets to service broker queue
     * 
     * @param files  downloaded files
     * @param sweeperStaticPropertiesDTO   
     */
    @Override
    public String[] sweepFiles(File[] files) {
        throw new RuntimeException("Not Implemented");
    }
}