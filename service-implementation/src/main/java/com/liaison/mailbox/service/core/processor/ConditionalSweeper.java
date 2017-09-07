/**
 * Copyright 2017 Liaison Technologies, Inc.
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.core.processor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.Comparator;

import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.liaison.commons.jaxb.JAXBUtility;
import com.liaison.commons.logging.LogTags;
import com.liaison.dto.queue.WorkTicket;
import com.liaison.dto.queue.WorkTicketGroup;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.dtdm.model.Processor;
import com.liaison.mailbox.enums.EntityStatus;
import com.liaison.mailbox.enums.ExecutionState;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.service.dto.SweeperStaticPropertiesDTO;
import com.liaison.mailbox.service.dto.configuration.TriggerProcessorRequestDTO;
import com.liaison.mailbox.service.dto.configuration.processor.properties.ConditionalSweeperPropertiesDTO;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.glass.util.ExecutionTimestamp;
import com.liaison.mailbox.service.glass.util.GlassMessage;
import com.liaison.mailbox.service.util.MailBoxUtil;

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

    private String pipelineId;
    private ConditionalSweeperPropertiesDTO staticProp;
    private String triggerFileNameWithPath;

    public void setPipeLineID(String pipeLineID) {
        this.pipelineId = pipeLineID;
    }

    public void setStaticProp(ConditionalSweeperPropertiesDTO staticProp) {
        this.staticProp = staticProp;
    }

    public void setTriggerFilePath(String triggerFileNameWithPath) {
        this.triggerFileNameWithPath = triggerFileNameWithPath;
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

            postedWorkTickets = new ArrayList<WorkTicket>();

            // Check for in-progress trigger file. If .INP file is available complete active file sweeping.
            String inprogressFileName = findInprogressTriggerFile(inputLocation);
            
            if (!MailBoxUtil.isEmpty(inprogressFileName)) {

                this.setTriggerFilePath(inputLocation + File.separator + inprogressFileName);
                filePathList = listFilePathFromInprogressTriggerFile();
                postToAsyncSweeper(filePathList, postedWorkTickets);
            }

            // get trigger filename from static properties
            String triggerFileName = MailBoxUtil.isEmpty(staticProp.getTriggerFile())
                    ? MailBoxConstants.TRIGGER_FILE_REGEX
                    : staticProp.getTriggerFile();
            this.setTriggerFilePath(inputLocation + File.separator + triggerFileName);

            if (Files.exists(Paths.get(triggerFileNameWithPath))) {
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
                    .forEach(File::delete);
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

        List<WorkTicket> workTicketsToPost = new ArrayList<WorkTicket>();
        List<WorkTicket> workTickets;
        List<List<Path>> groupedFilePathList = groupingFiles(filePathList);

        if (!CollectionUtils.isEmpty(groupedFilePathList)) {
            
            for (List<Path> filePaths : groupedFilePathList) {
                
                workTickets = generateWorkTickets(filePaths, getPipeLineID());
                if (!workTickets.isEmpty()) {
                    
                    emptyFilesCheckInWorkTicket(workTickets, workTicketsToPost, staticProp.isAllowEmptyFiles());
                    LOGGER.debug("There are {} files to process", workTicketsToPost.size());
                    asyncSweeper(workTicketsToPost);
                    postedWorkTickets.addAll(workTicketsToPost);
                    workTicketsToPost.clear();
                }
            }
        } else {
            LOGGER.warn("javascript filter api returned empty results");
        }

        // deleting .INP trigger file after swept the files in the list.
        delete(triggerFileNameWithPath);
    }

    /**
     * Async conditional sweeper posts workticket to queue
     *
     * @param workTickets worktickets
     * @param staticProp sweeper property
     * @throws IllegalAccessException
     * @throws IOException
     * @throws JAXBException
     * @throws JSONException
     */
    private void asyncSweeper(List<WorkTicket> workTickets)
            throws IllegalAccessException, IOException, JAXBException, JSONException {

        // Read from mailbox property - grouping js location
        List<WorkTicketGroup> workTicketGroups = groupingWorkTickets(workTickets);

        // first corner timestamp
        ExecutionTimestamp firstCornerTimeStamp = ExecutionTimestamp.beginTimestamp(GlassMessage.DEFAULT_FIRST_CORNER_NAME);

        if (workTicketGroups.isEmpty()) {
            LOGGER.debug("The file group is empty");
        } else {

            Map<String, String> statusMapFromFile = readMapFromFile(triggerFileNameWithPath);
            Map<String, String> sweptFileStatus;
            for (WorkTicketGroup workTicketGroup : workTicketGroups) {

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
                
                persistPayloadAndWorkticket(workTicketGroup.getWorkTicketGroup(), staticPropertiesDTO);

                String wrkTcktToSbr = JAXBUtility.marshalToJSON(workTicketGroup);
                LOGGER.debug(constructMessage("Workticket posted to SB queue.{}"), new JSONObject(wrkTcktToSbr).toString(2));
                postToSweeperQueue(wrkTcktToSbr);

                sweptFileStatus = new HashMap<String, String>();
                // For glass logging
                for (WorkTicket wrkTicket : workTicketGroup.getWorkTicketGroup()) {

                    // Fish tag global process id
                    try {
                        ThreadContext.put(LogTags.GLOBAL_PROCESS_ID, wrkTicket.getGlobalProcessId());

                        logToLens(wrkTicket, firstCornerTimeStamp, ExecutionState.PROCESSING);
                        LOGGER.info(constructMessage("Global PID",
                                seperator,
                                wrkTicket.getGlobalProcessId(),
                                " submitted for file ",
                                wrkTicket.getFileName()));

                        verifyAndDeletePayload(wrkTicket);
                    } finally {
                        ThreadContext.clearMap();
                    }

                    sweptFileStatus.put(wrkTicket.getAdditionalContext().get(MailBoxConstants.KEY_FILE_PATH).toString(), EntityStatus.INACTIVE.name());
                }

                statusMapFromFile = Stream.concat(statusMapFromFile.entrySet().stream(), sweptFileStatus.entrySet().stream())
                        .collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue(), (entry1, entry2) -> entry2));

                writeMapToFile(statusMapFromFile, triggerFileNameWithPath);

            }
        }
    }

    /**
     * Method is used to list file path from payload directory.
     * 
     * @param root processor payload directory
     * @return
     */
    public List<Path> listFilePathFromPayloadDirectory(String root) {

        LOGGER.info(constructMessage("Scanning Directory: {}"), root);
        Path rootPath = Paths.get(root);
        if (!Files.isDirectory(rootPath)) {
            throw new MailBoxServicesException(Messages.INVALID_DIRECTORY, Response.Status.BAD_REQUEST);
        }

        List<Path> result = new ArrayList<>();
        listFiles(result, rootPath);

        Map<String, String> map = new HashMap<String, String>();
        for (Path path : result) {
            map.put(path.toString(), EntityStatus.ACTIVE.name());
        }
        writeMapToFile(map, triggerFileNameWithPath);

        File triggerFile = new File(triggerFileNameWithPath);
        triggerFile.renameTo(new File(triggerFileNameWithPath + MailBoxConstants.INPROGRESS_EXTENTION));
        this.setTriggerFilePath(triggerFileNameWithPath + MailBoxConstants.INPROGRESS_EXTENTION);

        LOGGER.debug("Result size: {}, results {}", result.size(), result.toArray());
        return result;
    }

    /**
     * Retry generate file list from activeFiles list
     * 
     * @param activeFilesList
     * @return
     */
    protected List<Path> retryGenFileListFromActiveFiles(List<Path> activeFilesList) {

        List<Path> files = new ArrayList<>();
        for (Path file : activeFilesList) {

            if (!MailBoxUtil.validateLastModifiedTolerance(file)) {
                LOGGER.info("There are no changes in the file {} recently. So it is added in the sweeper list.", file.getFileName());
                files.add(file);
                continue;
            }
            LOGGER.info("The file {} is still modified within the tolerance. So it is removed from the current sweep list.", file.getFileName());
        }

        activeFilesList.clear();// Clearing the recently updated files after the second call.
        return files;
    }

    /**
     * This method is used to list file path from in-progress trigger file in case of process stuck
     * 
     * @return path list
     */
    private List<Path> listFilePathFromInprogressTriggerFile() {

        Map<String, String> statusMapFromFile = readMapFromFile(triggerFileNameWithPath);
        List<String> inprogressFiles = statusMapFromFile.keySet().stream().collect(Collectors.toList());
        List<Path> result = new ArrayList<>();
        Path path;
        for (String file : inprogressFiles) {
            path = Paths.get(file);
            result.add(path);
        }

        LOGGER.debug("Result inprogress files size: {}, results {}", result.size(), result.toArray());
        return filterDeletedFiles(result);
    }

    /**
     * Writer map values to trigger file
     * 
     * @param map
     * @param filePath
     */
    private void writeMapToFile(Map<String, String> map, String filePath) {

        try (FileOutputStream fos = new FileOutputStream(new File(filePath));
                ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(map);
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
    @SuppressWarnings("unchecked")
    private Map<String, String> readMapFromFile(String filePath) {

        try (FileInputStream fis = new FileInputStream(new File(filePath));
                ObjectInputStream ois = new ObjectInputStream(fis)) {

            return (HashMap<String, String>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Traverse the directory and list the files Sub Directories will be included if sweepSubDirectories set to true
     *
     * @param rootPath Sweeper Payload Location
     */
    private void listFiles(List<Path> files, Path rootPath) {

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(rootPath)) {

            for (Path file : stream) {

                // Sweep Directories if the property is set to true
                if (Files.isDirectory(file)) {
                    listFiles(files, file);
                    continue;
                }

                // To prevent adding trigger file
                if (!triggerFileNameWithPath.equals(rootPath + File.separator + file.getFileName())) {
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
     * @throws IOException
     */
    private String findInprogressTriggerFile(String payloadLocation) throws IOException {
        String[] inProgressFiles =  new File(payloadLocation)
            .list( (File dirToFilter, String filename) -> filename.endsWith(MailBoxConstants.INPROGRESS_EXTENTION) );
        return inProgressFiles.length > 0 ? inProgressFiles[0] : null;
    }

    /**
     * Method to get the pipe line id from the remote processor properties.
     *
     * @return pipelineId
     * @throws IllegalAccessException
     * @throws IOException
     */
    private String getPipeLineID() throws IOException, IllegalAccessException {

        if (MailBoxUtil.isEmpty(this.pipelineId)) {
            ConditionalSweeperPropertiesDTO sweeperStaticProperties = (ConditionalSweeperPropertiesDTO) getProperties();
            this.setPipeLineID(sweeperStaticProperties.getPipeLineID());
        }

        return this.pipelineId;
    }

    /**
     * Grouping the files based on the payload threshold and no of files threshold.
     *
     * @param workTickets Group of all workTickets in a WorkTicketGroup.
     * @return workticket group
     * @throws IllegalAccessException
     * @throws IOException
     */
    private List<WorkTicketGroup> groupingWorkTickets(List<WorkTicket> workTickets)
            throws IllegalAccessException, IOException {

        List<WorkTicketGroup> workTicketGroups = new ArrayList<>();

        if (workTickets.isEmpty()) {
            LOGGER.info(constructMessage("There are no files available in the directory."));
        }

        sortWorkTicket(workTickets, staticProp.getSort());
        WorkTicketGroup workTicketGroup = new WorkTicketGroup();
        List<WorkTicket> workTicketsInGroup = new ArrayList<WorkTicket>();
        workTicketGroup.setWorkTicketGroup(workTicketsInGroup);

        for (WorkTicket workTicket : workTickets) {

            boolean canAddToGroup = canAddToGroup(workTicketGroup,
                    workTicket,
                    staticProp.getPayloadSizeThreshold(),
                    staticProp.getNumOfFilesThreshold());
            
            if (canAddToGroup) {
                workTicketGroup.getWorkTicketGroup().add(workTicket);
            } else {

                if (!workTicketGroup.getWorkTicketGroup().isEmpty()) {
                    workTicketGroups.add(workTicketGroup);
                }
                workTicketGroup = new WorkTicketGroup();
                workTicketsInGroup = new ArrayList<WorkTicket>();
                workTicketGroup.setWorkTicketGroup(workTicketsInGroup);
                workTicketGroup.getWorkTicketGroup().add(workTicket);
            }
        }

        if (!workTicketGroup.getWorkTicketGroup().isEmpty()) {
            workTicketGroups.add(workTicketGroup);
        }

        return workTicketGroups;
    }

    /**
     * This Method used to add work ticket to post. If empty files allowed adds all work tickets to post else adds the
     * valid payload and deletes the empty file from location.
     * 
     * @param workTickets
     * @throws IOException
     * @throws IllegalAccessException
     */
    protected void emptyFilesCheckInWorkTicket(List<WorkTicket> workTickets, List<WorkTicket> workTicketsToPost, boolean isAllowEmptyFiles)
            throws JAXBException, JSONException, IOException, IllegalAccessException {

        if (!isAllowEmptyFiles) {

            Map<String, String> statusMapFromFile = readMapFromFile(triggerFileNameWithPath);
            HashMap<String, String> deletedFileSatus = new HashMap<String, String>();
            
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
                    logToLens(workTicket, null, ExecutionState.VALIDATION_ERROR);
                    String filePath = String.valueOf((Object) workTicket.getAdditionalContextItem(MailBoxConstants.KEY_FILE_PATH));
                    delete(filePath);
                    deletedFileSatus.put(filePath, EntityStatus.INACTIVE.name());
                }
            }

            statusMapFromFile = Stream.concat(statusMapFromFile.entrySet().stream(), deletedFileSatus.entrySet().stream())
                    .collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue(), (entry1, entry2) -> entry2));

            writeMapToFile(statusMapFromFile, triggerFileNameWithPath);
        } else {
            workTicketsToPost.addAll(workTickets);
        }
    }
    
    /**
     * To filter deleted files in the path list in pay-load location
     * 
     * @param filePathList
     * @return updated file path list
     */
    private List<Path> filterDeletedFiles(List<Path> filePathList) {
        
        List<Path> updatedFilePath =new ArrayList<>();
        HashMap<String, String> deletedFileSatus = new HashMap<String, String>();
        
        for (Path filePath : filePathList) {
            
           if (Files.exists(filePath)) {
               updatedFilePath.add(filePath);
           } else {
               deletedFileSatus.put(filePath.toString(), EntityStatus.INACTIVE.name());
               LOGGER.warn(constructMessage("The file {} is not available in the localtion."), filePath);
           }
        }
        
        if (!deletedFileSatus.isEmpty()) {
            
            Map<String, String> statusMapFromFile = readMapFromFile(triggerFileNameWithPath);
            statusMapFromFile = Stream.concat(statusMapFromFile.entrySet().stream(), deletedFileSatus.entrySet().stream())
                    .collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue(), (entry1, entry2) -> entry2));
            writeMapToFile(statusMapFromFile, triggerFileNameWithPath);
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

}
