/**
 * Copyright 2017 Liaison Technologies, Inc.
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.core.processor;

import com.liaison.commons.jaxb.JAXBUtility;
import com.liaison.commons.logging.LogTags;
import com.liaison.dto.queue.WorkTicket;
import com.liaison.fs2.metadata.FS2MetaSnapshot;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.dtdm.model.Processor;
import com.liaison.mailbox.enums.EntityStatus;
import com.liaison.mailbox.enums.ExecutionState;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.service.core.sweeper.helper.RelayWildcardFileFilter;
import com.liaison.mailbox.service.dto.SweeperStaticPropertiesDTO;
import com.liaison.mailbox.service.dto.configuration.TriggerProcessorRequestDTO;
import com.liaison.mailbox.service.dto.configuration.processor.properties.ConditionalSweeperPropertiesDTO;
import com.liaison.mailbox.service.dto.configuration.processor.properties.TriggerFileContentDTO;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.glass.util.ExecutionTimestamp;
import com.liaison.mailbox.service.glass.util.GlassMessage;
import com.liaison.mailbox.service.queue.sender.SweeperQueueSendClient;
import com.liaison.mailbox.service.storage.util.StorageUtilities;
import com.liaison.mailbox.service.util.MailBoxUtil;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * ConditionalSweeper
 *
 * <P>
 * ConditionalSweeper checks for existence of a trigger file 
 * and only moves files matching trigger criteria.
 *
 */

public class ConditionalSweeper extends AbstractSweeper implements MailBoxProcessorI {

    private static final Logger LOGGER = LogManager.getLogger(ConditionalSweeper.class);

    private ConditionalSweeperPropertiesDTO staticProp;
    private String triggerFileNameWithPath;
    private String triggerFileNameForWorkTicket;

    public void setStaticProp(ConditionalSweeperPropertiesDTO staticProp) {
        this.staticProp = staticProp;
    }

    public void setTriggerFilePath(String triggerFileNameWithPath) {
        this.triggerFileNameWithPath = triggerFileNameWithPath;
    }

    public void setTriggerFileNameForWorkTicket(String triggerFileNameForWorkTicket) {
        this.triggerFileNameForWorkTicket = triggerFileNameForWorkTicket;
    }

    public String getTriggerFileNameForWorkTicket() {
        return triggerFileNameForWorkTicket;
    }

    @SuppressWarnings("unused")
    private ConditionalSweeper() {
        // to force creation of instance only by passing the processor entity
    }

    public ConditionalSweeper(Processor configurationInstance) {
        super(configurationInstance);
    }


    @Override
    public void runProcessor(Object dto) {
        setReqDTO((TriggerProcessorRequestDTO) dto);
        run();
    }

    private void run() {

        List<WorkTicket> postedWorkTickets;
        List<Path> filePathList;

        try {

            // Get root from folders input_folder
            String inputLocation = getPayloadURI();

            // retrieve required properties
            this.setStaticProp((ConditionalSweeperPropertiesDTO) getProperties());

            // Validation of the necessary properties
            if (MailBoxUtil.isEmpty(inputLocation)) {
                throw new MailBoxServicesException(Messages.LOCATION_NOT_CONFIGURED, MailBoxConstants.PAYLOAD_LOCATION, Response.Status.CONFLICT);
            }

            long startTime = System.currentTimeMillis();
            LOGGER.info(constructMessage("Start run"));

            postedWorkTickets = new ArrayList<>();

            // Check for in-progress trigger file. If .INP file is available complete active file sweeping.
            String inprogressFileName = findInprogressTriggerFile(inputLocation);
            
            if (!MailBoxUtil.isEmpty(inprogressFileName)) {

                this.setTriggerFilePath(inputLocation + File.separator + inprogressFileName);
                setTriggerFileNameForWorkTicket(FilenameUtils.removeExtension(inprogressFileName));
                filePathList = listFilePathFromInProgressTriggerFile();
                postToAsyncSweeper(filePathList, postedWorkTickets);
            }

            // get trigger filename from static properties
            String triggerFileName = getTriggerFile(inputLocation, staticProp.getTriggerFile(), staticProp.getIncludeFiles());
            setTriggerFileNameForWorkTicket(triggerFileName);
            this.setTriggerFilePath(inputLocation + File.separator + triggerFileName);

            Path triggerFilePath = Paths.get(triggerFileNameWithPath);
            if (!MailBoxUtil.isEmpty(triggerFileName) && Files.exists(triggerFilePath)) {
                if (MailBoxUtil.validateLastModifiedTolerance(triggerFilePath)) {
                    LOGGER.warn(constructMessage("Trigger file is modified within the tolerance limit {} seconds and it will be picked up in the next iteration after the threshold limit"), String.valueOf(MailBoxUtil.CONFIGURATION.getLong(MailBoxConstants.LAST_MODIFIED_TOLERANCE)));
                    return;
                }
                filePathList = listFilePathFromPayloadDirectory(inputLocation);
                postToAsyncSweeper(filePathList, postedWorkTickets);
            } else {
                LOGGER.info(constructMessage("Trigger file {} isn't available in the root path {}"), triggerFileName, inputLocation);
            }

            // delete empty directories including pay-load location.
            if (staticProp.isDeleteEmptyDirectoryAfterSwept()) {

                Files.walk(Paths.get(inputLocation))
                    .sorted(Comparator.reverseOrder())
                    .filter(p -> Files.isDirectory(p))
                    .filter(p -> (new File(p.toUri()).listFiles().length == 0))
                    .filter(p -> (!new File(p.toUri()).getPath().equals(inputLocation)))
                    .map(Path::toFile)
                    .forEach(file -> LOGGER.info("Deleting the empty directory {} and delete status is {}", file.toPath(), file.delete()));
            }

            long endTime = System.currentTimeMillis();

            LOGGER.info(constructMessage("Number of files processed {}"), postedWorkTickets.size());
            LOGGER.info(constructMessage("Total time taken to process files {}"), endTime - startTime);
            LOGGER.info(constructMessage("End run"));
        } catch (MailBoxServicesException | IOException | IllegalAccessException | JAXBException | JSONException e) {
            throw new RuntimeException(e);
        } finally {
            // clear the context
            ThreadContext.clearMap();
        }
    }
    
    /**
     * This method is used to get the trigger file with wild card pattern.
     * 
     * @param inputLocation
     * @param triggerFileName
     * @param includeFileName
     * @return
     */
    private String getTriggerFile(String inputLocation, String triggerFileName, String includeFileName) {
        // if trigger name pattern is not empty check with wild card pattern and return last updated file name
        // else return default 'trigger'
        if (!MailBoxUtil.isEmpty(triggerFileName)) {

            String[] wildcards = {triggerFileName, !MailBoxUtil.isEmpty(includeFileName) ? includeFileName : MailBoxConstants.WILD_CARD_PATTERN};
            FileFilter triggerFileFilter = new RelayWildcardFileFilter(wildcards);
            List<File> triggerFileList;

            Stream<Path> pathStream = null;
            try {

                //Files.list needs the special attention since it is using DirectoryStream in the back
                //So, we have use try with resources or close the stream manually to avoid the leak in FileDescriptor
                pathStream = Files.list(Paths.get(inputLocation));
                triggerFileList = pathStream
                        .map(Path::toFile)
                        .filter(triggerFileFilter::accept)
                        .sorted(LastModifiedFileComparator.LASTMODIFIED_REVERSE)
                        .collect(Collectors.toList());
            } catch (IOException ex) {
                throw new RuntimeException("Error listing files in " +inputLocation, ex);
            } finally {
                if (null != pathStream) {
                    pathStream.close();
                }
            }
            return !triggerFileList.isEmpty() ? triggerFileList.get(0).getName() : null;
        } 
        return MailBoxConstants.TRIGGER_FILE_REGEX;
    }

    /**
     * This method groups the file path, generate work ticket and post the work tickets to sweeper.
     * 
     * @param filePathList
     * @param postedWorkTickets
     * @throws IOException
     * @throws IllegalAccessException
     * @throws JSONException
     * @throws JAXBException
     */
    private void postToAsyncSweeper(List<Path> filePathList, List<WorkTicket> postedWorkTickets)
            throws IllegalAccessException, IOException, JAXBException, JSONException {

        List<WorkTicket> workTicketsToPost = new ArrayList<>();
        List<WorkTicket> workTickets;
        List<List<Path>> groupedFilePathList = groupingFiles(filePathList);

        if (!CollectionUtils.isEmpty(groupedFilePathList)) {
            
            String pipeLineID = getPipeLineID();
            
            for (List<Path> filePaths : groupedFilePathList) {
                
                workTickets = generateWorkTickets(filePaths, pipeLineID);
                if (!workTickets.isEmpty()) {
                    
                    emptyFilesCheckInWorkTicket(workTickets, workTicketsToPost, staticProp.isAllowEmptyFiles());
                    LOGGER.debug("There are {} files to process", workTicketsToPost.size());
                    asyncSweeper(workTicketsToPost);
                    postedWorkTickets.addAll(workTicketsToPost);
                    workTicketsToPost.clear();
                }
            }
            SweeperQueueSendClient.post(constructBatchWorkticket(), false);
        } else {
            LOGGER.warn("javascript filter api returned empty results");
        }

        // deleting .INP trigger file after swept the files in the list.
        delete(triggerFileNameWithPath);
    }
    
    /**
     * Method to construct batch work ticket.
     * 
     * @return json string of the workTicket
     * @throws JsonGenerationException
     * @throws JsonMappingException
     * @throws JAXBException
     * @throws IOException
     * @throws IllegalAccessException 
     */
    private String constructBatchWorkticket()
            throws JsonGenerationException, JsonMappingException, JAXBException, IOException, IllegalAccessException {

        TriggerFileContentDTO relatedTransactionDto = readMapFromFile(triggerFileNameWithPath);
        WorkTicket workTicket = constructWorkTicket(relatedTransactionDto.getParentGlobalProcessId(), relatedTransactionDto.getTriggerFileUri());
        return JAXBUtility.marshalToJSON(workTicket);
    }

    /**
     * Async conditional sweeper posts workticket to queue
     *
     * @param workTickets worktickets
     * @throws IOException
     * @throws JAXBException
     * @throws JSONException
     */
    private void asyncSweeper(List<WorkTicket> workTickets)
            throws IOException, JAXBException, JSONException {

        final Date lensStatusDate = new Date();

        // first corner timestamp
        ExecutionTimestamp firstCornerTimeStamp = ExecutionTimestamp.beginTimestamp(GlassMessage.DEFAULT_FIRST_CORNER_NAME);
        TriggerFileContentDTO relatedTransactionDto = readMapFromFile(triggerFileNameWithPath);
        Map<String, String> statusMapFromFile = relatedTransactionDto.getFilePathStatusIndex();
        String relatedTransactionId = relatedTransactionDto.getParentGlobalProcessId();
        Map<String, String> sweptFileStatus;

        List<WorkTicket> updatedWorkTickets = addAdditionalDetails(workTickets);
        for (WorkTicket workTicket : updatedWorkTickets) {
            // Interrupt signal for async sweeper
            if (MailBoxUtil.isInterrupted(Thread.currentThread().getName())) {
                LOGGER.warn(constructMessage("The executor is gracefully interrupted."));
                return;
            }
            LOGGER.debug("Persist workticket from workticket group to spectrum");
            SweeperStaticPropertiesDTO staticPropertiesDTO = new SweeperStaticPropertiesDTO();
            staticPropertiesDTO.setContentType(staticProp.getContentType());
            staticPropertiesDTO.setLensVisibility(staticProp.isLensVisibility());
            staticPropertiesDTO.setPipeLineID(staticProp.getPipeLineID());
            staticPropertiesDTO.setSecuredPayload(staticProp.isSecuredPayload());

            persistPayloadAndWorkticket(workTicket, staticPropertiesDTO);
            String wrkTcktToSbr = JAXBUtility.marshalToJSON(workTicket);
            LOGGER.debug(constructMessage("Workticket posted to SB queue.{}"), new JSONObject(wrkTcktToSbr).toString(2));
            SweeperQueueSendClient.post(wrkTcktToSbr, false);
            sweptFileStatus = new HashMap<String, String>();

            // For glass logging
            // Fish tag global process id
            try {
                ThreadContext.put(LogTags.GLOBAL_PROCESS_ID, workTicket.getGlobalProcessId());
                logToLens(workTicket, firstCornerTimeStamp, ExecutionState.PROCESSING, relatedTransactionId, lensStatusDate);
                LOGGER.info(constructMessage("Global PID",
                        seperator,
                        workTicket.getGlobalProcessId(),
                        " submitted for file ",
                        workTicket.getFileName()));
                verifyAndDeletePayload(workTicket);
            } finally {
                ThreadContext.clearMap();
            }
            sweptFileStatus.put(workTicket.getAdditionalContext().get(MailBoxConstants.KEY_FILE_PATH).toString(), EntityStatus.INACTIVE.name());
            statusMapFromFile = Stream.concat(statusMapFromFile.entrySet().stream(), sweptFileStatus.entrySet().stream())
                   .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (entry1, entry2) -> entry2));
            relatedTransactionDto.setFilePathStatusIndex(statusMapFromFile);
            writeMapToFile(relatedTransactionDto, triggerFileNameWithPath);
        }

        //post parent gpid
        logToLens(relatedTransactionDto.getParentGlobalProcessId(), staticProp.getPipeLineID());
    }

    /**
     * Process the worktickets and set additional contexts
     *
     * @param workTickets list of worktickets
     * @return
     */
    private List<WorkTicket> addAdditionalDetails(List<WorkTicket> workTickets) {

        if (workTickets.isEmpty()) {
            LOGGER.info(constructMessage("There are no files available in the directory."));
        }

        sortWorkTicket(workTickets, staticProp.getSort());
        int totalFileCount = workTickets.size();
        int currentFileCount = 0;
        TriggerFileContentDTO relatedTransactionDto = readMapFromFile(triggerFileNameWithPath);
        String relatedTransactionId = relatedTransactionDto.getParentGlobalProcessId();

        for (WorkTicket workTicket : workTickets) {

            currentFileCount++;
            workTicket.setAdditionalContext(MailBoxConstants.KEY_FILE_COUNT, currentFileCount + MailBoxConstants.FILE_COUNT_SEPARATOR + totalFileCount);
            workTicket.setAdditionalContext(MailBoxConstants.KEY_TRIGGER_FILE_NAME, getTriggerFileNameForWorkTicket());
            workTicket.setAdditionalContext(MailBoxConstants.KEY_TRIGGER_FILE_PARENT_GPID, relatedTransactionId);
            workTicket.setAdditionalContext(MailBoxConstants.KEY_FILE_GROUP, true);
            workTicket.setAdditionalContext(MailBoxConstants.KEY_TRIGGER_FILE_URI, relatedTransactionDto.getTriggerFileUri());
        }

        return workTickets;
    }

    /**
     * Method is used to list file path from payload directory.
     * 
     * @param root processor payload directory
     * @return
     */
    public List<Path> listFilePathFromPayloadDirectory(String root) {

        LOGGER.info(constructMessage("Scanning Directory: {}"), root);
        TriggerFileContentDTO triggerFileContentDto = new TriggerFileContentDTO();
        Path rootPath = Paths.get(root);
        if (!Files.isDirectory(rootPath)) {
            throw new MailBoxServicesException(Messages.INVALID_DIRECTORY, Response.Status.BAD_REQUEST);
        }

        // Get include file pattern and trigger file pattern if both are having wild card patterns. 
        String includeFileName = !MailBoxUtil.isEmpty(staticProp.getIncludeFiles()) ? staticProp.getIncludeFiles() : MailBoxConstants.WILD_CARD_PATTERN;
        FileFilter wildcardFileFilter = new RelayWildcardFileFilter(includeFileName);
        String triggerFileName = MailBoxUtil.isEmpty(staticProp.getTriggerFile()) ? MailBoxConstants.TRIGGER_FILE_REGEX : staticProp.getTriggerFile();
        FileFilter triggerFileFilter = new RelayWildcardFileFilter(triggerFileName);
        
        List<Path> result = new ArrayList<>();
        // Get payload files matching with include file filter and not matching trigger file 
        listFiles(result, rootPath, wildcardFileFilter, triggerFileFilter);

        Map<String, String> map = new HashMap<>();
        for (Path path : result) {
            map.put(path.toString(), EntityStatus.ACTIVE.name());
        }
        triggerFileContentDto.setFilePathStatusIndex(map);
        triggerFileContentDto.setParentGlobalProcessId(MailBoxUtil.getGUID());

        File triggerFile = new File(triggerFileNameWithPath);
        persistTriggerFile(triggerFile, triggerFileContentDto);
        writeMapToFile(triggerFileContentDto, triggerFileNameWithPath);
        
        triggerFile.renameTo(new File(triggerFileNameWithPath + MailBoxConstants.INPROGRESS_EXTENTION));
        this.setTriggerFilePath(triggerFileNameWithPath + MailBoxConstants.INPROGRESS_EXTENTION);

        LOGGER.debug("Result size: {}, results {}", result.size(), result.toArray());
        return result;
    }
    
    /**
     * Persist the trigger file
     * @param triggerFile
     * @param triggerFileContentDto
     */
    private void persistTriggerFile(File triggerFile, TriggerFileContentDTO triggerFileContentDto) {
        
        Map<String, String> properties = new HashMap<String, String>();
        properties.put(MailBoxConstants.PROPERTY_HTTPLISTENER_SECUREDPAYLOAD, String.valueOf(staticProp.isSecuredPayload()));
        properties.put(MailBoxConstants.PROPERTY_LENS_VISIBILITY, String.valueOf(staticProp.isLensVisibility()));
        properties.put(MailBoxConstants.KEY_PIPELINE_ID, staticProp.getPipeLineID());
        properties.put(MailBoxConstants.STORAGE_IDENTIFIER_TYPE, MailBoxUtil.getStorageType(configurationInstance.getDynamicProperties()));

        // persist payload in spectrum
        try { 
            InputStream payloadToPersist = new FileInputStream(triggerFile);
            // payloadToPersist stream is closed in StorageUtilities.persistPayload method
            FS2MetaSnapshot metaSnapshot = StorageUtilities.persistPayload(payloadToPersist, properties , triggerFileContentDto.getParentGlobalProcessId(), null);
            triggerFileContentDto.setTriggerFileUri(metaSnapshot.getURI().toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * This method is used to list file path from in-progress trigger file in case of process stuck
     * @return path list
     */
    private List<Path> listFilePathFromInProgressTriggerFile() {

        TriggerFileContentDTO triggerFileContentDto = readMapFromFile(triggerFileNameWithPath);
        Map<String, String> statusMapFromFile = triggerFileContentDto.getFilePathStatusIndex();
        String relatedTransactionId = triggerFileContentDto.getParentGlobalProcessId();
        Map<String, String> activeMapFromFile = statusMapFromFile.entrySet()
                                                  .stream()
                                                  .filter(a->a.getValue().equals(EntityStatus.ACTIVE.name()))
                                                  .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        List<String> inProgressFiles = new ArrayList<>(activeMapFromFile.keySet());
        List<Path> result = new ArrayList<>();
        for (String file : inProgressFiles) {
            result.add(Paths.get(file));
        }

        LOGGER.debug("Result inprogress files size: {}, results {}", result.size(), result.toArray());
        return filterDeletedFiles(result, relatedTransactionId);
    }

    /**
     * Writer map values to trigger file
     * 
     * @param filePath
     * @param relatedTransactionDto 
     */
    private void writeMapToFile(TriggerFileContentDTO relatedTransactionDto, String filePath) {

        try (FileOutputStream fos = new FileOutputStream(new File(filePath));
                ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(relatedTransactionDto);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Read file path status map from file.
     * 
     * @param filePath
     * @return map with file path and status
     */
    private TriggerFileContentDTO readMapFromFile(String filePath) {

        try (FileInputStream fis = new FileInputStream(new File(filePath));
                ObjectInputStream ois = new ObjectInputStream(fis)) {
            return (TriggerFileContentDTO) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Traverse the directory and list the files Sub Directories will be included if sweepSubDirectories set to true
     *
     * @param rootPath Sweeper Payload Location
     */
    private void listFiles(List<Path> files, Path rootPath, FileFilter wildcardFileFilter, FileFilter triggerFileFilter) {

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(rootPath)) {

            for (Path file : stream) {

                // Sweep Directories if the property is set to true
                if (Files.isDirectory(file)) {
                    if (staticProp.isSweepSubDirectories()) {
                        listFiles(files, file, wildcardFileFilter, triggerFileFilter);
                    }
                    continue;
                }

                if (MailBoxUtil.validateLastModifiedTolerance(file)) {
                    LOGGER.info(constructMessage("The file {} is modified within tolerance. So file is not added to the list"), file.toString());
                    continue;
                }
                
                // To prevent adding trigger file and not support with trigger file pattern and support with include file pattern 
                if (!triggerFileNameWithPath.equals(rootPath + File.separator + file.getFileName()) 
                        && !triggerFileFilter.accept(file.toFile()) && wildcardFileFilter.accept(file.toFile())) {
                    files.add(file);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Finds in-progress file name(.INP)
     * 
     * @param payloadLocation
     * @return fileName
     */
    private String findInprogressTriggerFile(String payloadLocation) {

        //Validate the given payload location
        Path rootPath = Paths.get(payloadLocation);
        if (!Files.isDirectory(rootPath)) {
            throw new MailBoxServicesException(Messages.INVALID_DIRECTORY, Response.Status.BAD_REQUEST);
        }

        String[] inProgressFiles =  new File(payloadLocation)
            .list( (File dirToFilter, String filename) -> filename.endsWith(MailBoxConstants.INPROGRESS_EXTENTION));
        return inProgressFiles.length > 0 ? inProgressFiles[0] : null;
    }

    /**
     * This Method used to add work ticket to post. If empty files allowed adds all work tickets to post else adds the
     * valid payload and deletes the empty file from location.
     * 
     * @param workTickets
     * @throws IOException
     */
    private void emptyFilesCheckInWorkTicket(List<WorkTicket> workTickets, List<WorkTicket> workTicketsToPost, boolean isAllowEmptyFiles)
            throws IOException {

        if (!isAllowEmptyFiles) {

            TriggerFileContentDTO triggerFileContentDto = readMapFromFile(triggerFileNameWithPath);
            Map<String, String> statusMapFromFile = triggerFileContentDto.getFilePathStatusIndex();
            String relatedTransactionId = triggerFileContentDto.getParentGlobalProcessId();
            HashMap<String, String> deletedFileStatus = new HashMap<>();
            
            for (WorkTicket workTicket : workTickets) {

                if (isPayloadValid(workTicket)) {
                    workTicketsToPost.add(workTicket);
                } else {

                    // Interrupt signal for empty files
                    if (MailBoxUtil.isInterrupted(Thread.currentThread().getName())) {
                        LOGGER.warn(constructMessage("The executor is gracefully interrupted."));
                        return;
                    }

                    LOGGER.warn(constructMessage("The file {} is empty and empty files not allowed"), workTicket.getFileName());
                    logToLens(workTicket, null, ExecutionState.VALIDATION_ERROR, relatedTransactionId, null);
                    String filePath = String.valueOf((Object) workTicket.getAdditionalContextItem(MailBoxConstants.KEY_FILE_PATH));
                    delete(filePath);
                    deletedFileStatus.put(filePath, EntityStatus.INACTIVE.name());
                }
            }

            statusMapFromFile = Stream.concat(statusMapFromFile.entrySet().stream(), deletedFileStatus.entrySet().stream())
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (entry1, entry2) -> entry2));
            triggerFileContentDto.setFilePathStatusIndex(statusMapFromFile);

            writeMapToFile(triggerFileContentDto, triggerFileNameWithPath);
        } else {
            workTicketsToPost.addAll(workTickets);
        }
    }
    
    /**
     * To filter deleted files in the path list in pay-load location
     * 
     * @param filePathList
     * @param relatedTransactionId 
     * @return updated file path list
     */
    private List<Path> filterDeletedFiles(List<Path> filePathList, String relatedTransactionId) {
        
        List<Path> updatedFilePath =new ArrayList<>();
        TriggerFileContentDTO relatedTransactionDto = readMapFromFile(triggerFileNameWithPath);
        HashMap<String, String> deletedFileStatus = new HashMap<>();
        
        for (Path filePath : filePathList) {
            
           if (Files.exists(filePath)) {
               updatedFilePath.add(filePath);
           } else {
               deletedFileStatus.put(filePath.toString(), EntityStatus.INACTIVE.name());
               LOGGER.warn(constructMessage("The file {} is not available in the localtion."), filePath);
           }
        }
        
        if (!deletedFileStatus.isEmpty()) {
            
            Map<String, String> statusMapFromFile = relatedTransactionDto.getFilePathStatusIndex();
            statusMapFromFile = Stream.concat(statusMapFromFile.entrySet().stream(), deletedFileStatus.entrySet().stream())
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (entry1, entry2) -> entry2));
            relatedTransactionDto.setFilePathStatusIndex(statusMapFromFile);
            relatedTransactionDto.setParentGlobalProcessId(relatedTransactionId);

            writeMapToFile(relatedTransactionDto, triggerFileNameWithPath);
        }
        
        return updatedFilePath;
    }

    @Override
    public Object getClient() {
        return null;
    }

    @Override
    public void cleanup() {
    }

    @Override
    public boolean isClassicSweeper() {
        return true;
    }

}
