/**
 * Copyright 2014 Liaison Technologies, Inc.
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.core.processor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.liaison.commons.jaxb.JAXBUtility;
import com.liaison.commons.logging.LogTags;
import com.liaison.commons.message.glass.dom.StatusType;
import com.liaison.commons.messagebus.client.exceptions.ClientUnavailableException;
import com.liaison.commons.util.ISO8601Util;
import com.liaison.dto.enums.ProcessMode;
import com.liaison.dto.queue.WorkTicket;
import com.liaison.dto.queue.WorkTicketGroup;
import com.liaison.fs2.metadata.FS2MetaSnapshot;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.dtdm.model.Processor;
import com.liaison.mailbox.enums.ExecutionEvents;
import com.liaison.mailbox.enums.ExecutionState;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.service.core.fsm.MailboxFSM;
import com.liaison.mailbox.service.dto.configuration.TriggerProcessorRequestDTO;
import com.liaison.mailbox.service.dto.configuration.processor.properties.SweeperPropertiesDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.executor.javascript.JavaScriptExecutorUtil;
import com.liaison.mailbox.service.queue.sender.SweeperQueue;
import com.liaison.mailbox.service.storage.util.StorageUtilities;
import com.liaison.mailbox.service.util.GlassMessage;
import com.liaison.mailbox.service.util.MailBoxUtil;
import com.liaison.mailbox.service.util.TransactionVisibilityClient;

/**
 * DirectorySweeper
 *
 * <P>
 * DirectorySweeper sweeps the files from mail box and creates meta data about file and post it to
 * the queue.
 *
 * @author veerasamyn
 */

public class DirectorySweeper extends AbstractProcessor implements MailBoxProcessorI {

	private static final Logger LOGGER = LogManager.getLogger(DirectorySweeper.class);

	private String pipelineId;
	private List<Path> activeFiles = new ArrayList<>();
    protected int totalNumberOfDeletedFiles;

    public void setPipeLineID(String pipeLineID) {
		this.pipelineId = pipeLineID;
	}

	@SuppressWarnings("unused")
	private DirectorySweeper() {
		// to force creation of instance only by passing the processor entity
	}

	public DirectorySweeper(Processor configurationInstance) {
		super(configurationInstance);
	}


	@Override
	public void runProcessor(Object dto, MailboxFSM fsm) {

		try {

		    setReqDTO((TriggerProcessorRequestDTO) dto);
			if (getProperties().isHandOverExecutionToJavaScript()) {
				fsm.handleEvent(fsm.createEvent(ExecutionEvents.PROCESSOR_EXECUTION_HANDED_OVER_TO_JS));
				// Use custom G2JavascriptEngine
				JavaScriptExecutorUtil.executeJavaScript(configurationInstance.getJavaScriptUri(), this);
			 } else {
				run(getReqDTO().getExecutionId());
			}
		} catch (IOException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	private void run(String executionId) {

        try {

            // Get root from folders input_folder
            String inputLocation = getPayloadURI();

            // retrieve required properties
            SweeperPropertiesDTO staticProp = (SweeperPropertiesDTO) getProperties();

            // Validation of the necessary properties
            if (MailBoxUtil.isEmpty(inputLocation)) {
            	throw new MailBoxServicesException(Messages.LOCATION_NOT_CONFIGURED, MailBoxConstants.PAYLOAD_LOCATION, Response.Status.CONFLICT);
            }

            long startTime = System.currentTimeMillis();
            LOGGER.info(constructMessage("Start run"));           

            LOGGER.debug("Is in-progress file list is empty: {}", activeFiles.isEmpty());
            List<WorkTicket> workTickets = (activeFiles.isEmpty())
            								? sweepDirectory(inputLocation , false, staticProp)
            								: retryGenWrkTktForActiveFiles(activeFiles);

            if (!workTickets.isEmpty()) {

                LOGGER.debug("There are {} files to process", workTickets.size());
            	// Read from mailbox property - grouping js location
            	List<WorkTicketGroup> workTicketGroups = groupingWorkTickets(workTickets, staticProp);

                LOGGER.debug("Persist workticket to spectrum");
                persistPaylaodAndWorkticket(workTickets, staticProp);

            	if (workTicketGroups.isEmpty()) {
            		LOGGER.info("The file group is empty");
            	} else {
            		for (WorkTicketGroup workTicketGroup : workTicketGroups) {

            			String wrkTcktToSbr = JAXBUtility.marshalToJSON(workTicketGroup);
            			LOGGER.info(constructMessage("Workticket posted to SB queue.{}"), new JSONObject(wrkTcktToSbr).toString(2));
            			postToSweeperQueue(wrkTcktToSbr);

            			// For glass logging
            			for (WorkTicket wrkTicket : workTicketGroup.getWorkTicketGroup()) {

            	            //Fish tag global process id
            	            ThreadContext.clearMap(); //set new context after clearing
            	            ThreadContext.put(LogTags.GLOBAL_PROCESS_ID, wrkTicket.getGlobalProcessId());

            			    logToLens(inputLocation, wrkTicket);
            				LOGGER.info(constructMessage("Global PID",
            				        seperator,
            				        wrkTicket.getGlobalProcessId(),
            				        " submitted for file ",
            				        wrkTicket.getFileName()));
            				
            				String payloadURI = wrkTicket.getPayloadURI();
        					String filePath = String.valueOf(wrkTicket.getAdditionalContextItem(MailBoxConstants.KEY_FILE_PATH));
            				// Delete the file if it exists in spectrum and should be successfully posted to SB Queue.
            				if (StorageUtilities.isPayloadExists(wrkTicket.getPayloadURI())) {
            					LOGGER.info("Payload {} exists in spectrum. so deleting the file {}", payloadURI, filePath);
                				delete(filePath);
            				} else {
            					LOGGER.info("Payload {} does not exist in spectrum. so file {} is not deleted.", payloadURI, filePath);
            				}
            				LOGGER.info(constructMessage("Global PID",
            				        seperator,
            				        wrkTicket.getGlobalProcessId(),
            				        " deleted the file ",
            				        wrkTicket.getFileName()));
            			}
            		}
            	}
            
            }

            // retry when in-progress file list is not empty
            if (!activeFiles.isEmpty()) {
            	run(executionId);
            }
            long endTime = System.currentTimeMillis();

            LOGGER.info(constructMessage("Number of files processed {}"), workTickets.size());
            LOGGER.info(constructMessage("Total time taken to process files {}"), endTime - startTime);
            LOGGER.info(constructMessage("End run"));
        } catch (MailBoxServicesException | IOException | JAXBException | JSONException | IllegalAccessException e) {
            LOGGER.error(constructMessage("Error occurred while scanning the mailbox", seperator, e.getMessage()), e);
        	throw new RuntimeException(e);
        } finally {
        	//clear the context
        	ThreadContext.clearMap();
        }
	}

	/**
	 * Method is used to retrieve all the WorkTickets from the given mailbox.
	 *
	 * @param root The mailbox root directory
	 * @param listDirectoryOnly Sweep file only
	 * @param staticProp sweeper properties
	 * @return list of worktickets
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws IOException
	 */
	public List<WorkTicket> sweepDirectory(String root, boolean listDirectoryOnly, SweeperPropertiesDTO staticProp) throws IllegalArgumentException, IllegalAccessException, IOException {

        LOGGER.info(constructMessage("Scanning Directory: {}"), root);
		Path rootPath = Paths.get(root);
		if (!Files.isDirectory(rootPath)) {
			throw new MailBoxServicesException(Messages.INVALID_DIRECTORY, Response.Status.BAD_REQUEST);
		}

		List<Path> result = new ArrayList<>();
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(rootPath, defineFilter(listDirectoryOnly))) {
			for (Path file : stream) {

				String fileName = file.getFileName().toString();
				// Check if the file to be uploaded is included or not excluded
				if (!checkFileIncludeorExclude(staticProp.getIncludedFiles(),
                        fileName,
                        staticProp.getExcludedFiles())) {
                    continue;
                }

                LOGGER.debug("Sweeping file {}", file.toString());
				if (MailBoxUtil.validateLastModifiedTolerance(file)) {
					LOGGER.info(constructMessage("The file {} is modified within tolerance. So added in the in-progress list."), file.toString());
					activeFiles.add(file);
					continue;
				}
				result.add(file);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

        LOGGER.debug("Result size: {}, results {}", result.size(), result.toArray());
		return generateWorkTickets(result);
	}

	/**
	 * Method to get the pipe line id from the remote processor properties.
	 *
	 * @return pipelineId
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws IOException
	 */
	private String getPipeLineID() throws IllegalArgumentException, IllegalAccessException, IOException {

		if (MailBoxUtil.isEmpty(this.pipelineId)) {
			SweeperPropertiesDTO sweeperStaticProperties = (SweeperPropertiesDTO) getProperties();
			this.setPipeLineID(sweeperStaticProperties.getPipeLineID());
		}

		return this.pipelineId;
	}

	/**
	 * Grouping the files based on the payload threshold and no of files threshold.
	 *
	 * @param workTickets Group of all workTickets in a WorkTicketGroup.
	 * @param staticProp sweeper properties
	 * @return workticket group
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws IOException
	 */
	private List<WorkTicketGroup> groupingWorkTickets(List<WorkTicket> workTickets, SweeperPropertiesDTO staticProp) throws IllegalArgumentException, IllegalAccessException, IOException {

		String groupingJsPath = configurationInstance.getJavaScriptUri();
		List<WorkTicketGroup> workTicketGroups = new ArrayList<>();

		if (!MailBoxUtil.isEmpty(groupingJsPath)) {
			JavaScriptExecutorUtil.executeJavaScript(groupingJsPath, "init", workTickets, LOGGER);
		} else {

			if (workTickets.isEmpty()) {
				LOGGER.info(constructMessage("There are no files available in the directory."));
			}

			WorkTicketGroup workTicketGroup = new WorkTicketGroup();
			List <WorkTicket> workTicketsInGroup = new ArrayList <WorkTicket>();
			workTicketGroup.setWorkTicketGroup(workTicketsInGroup);
			for (WorkTicket workTicket : workTickets) {

				if (canAddToGroup(workTicketGroup, workTicket, staticProp)) {
					workTicketGroup.getWorkTicketGroup().add(workTicket);
				} else {

					if (!workTicketGroup.getWorkTicketGroup().isEmpty()) {
						workTicketGroups.add(workTicketGroup);
					}
					workTicketGroup = new WorkTicketGroup();
					workTicketsInGroup = new ArrayList <WorkTicket>();
					workTicketGroup.setWorkTicketGroup(workTicketsInGroup);
					workTicketGroup.getWorkTicketGroup().add(workTicket);

				}
			}

			if (!workTicketGroup.getWorkTicketGroup().isEmpty()) {
				workTicketGroups.add(workTicketGroup);
			}
		}
		return workTicketGroups;
	}


    /**
	 * Method to post meta data to rest service/ queue.
	 *
	 * @param input
	 * The input message to queue.
	 */
	private void postToSweeperQueue(String input)   {

        try {
			SweeperQueue.getInstance().sendMessages(input);
		} catch (ClientUnavailableException e) {
			throw new RuntimeException(e);
		}
        LOGGER.debug("DirectorySweeper push postToQueue, message: {}", input);
	}

	/**
	 * Method to persist the payload and workticket details in spectrum
	 *
	 * @param workTickets WorkTickets list.
	 * @param staticProp sweeper properties
	 * @throws IOException
	 */
	public void persistPaylaodAndWorkticket(List<WorkTicket> workTickets, SweeperPropertiesDTO staticProp) throws IOException {

		LOGGER.debug(constructMessage("Persisting paylaod and workticket in spectrum starts"));
		for (WorkTicket workTicket : workTickets) {

			File payloadFile = new File(workTicket.getPayloadURI());

			Map <String, String> properties = new HashMap <String, String>();
			Map<String,String> ttlMap = configurationInstance.getTTLUnitAndTTLNumber();
			if (!ttlMap.isEmpty()) {
				Integer ttlNumber = Integer.parseInt(ttlMap.get(MailBoxConstants.TTL_NUMBER));
				workTicket.setTtlDays(MailBoxUtil.convertTTLIntoDays(ttlMap.get(MailBoxConstants.CUSTOM_TTL_UNIT), ttlNumber));
			}

			properties.put(MailBoxConstants.PROPERTY_HTTPLISTENER_SECUREDPAYLOAD, String.valueOf(staticProp.isSecuredPayload()));
			properties.put(MailBoxConstants.PROPERTY_LENS_VISIBILITY, String.valueOf(staticProp.isLensVisibility()));
			properties.put(MailBoxConstants.KEY_PIPELINE_ID, staticProp.getPipeLineID());

			LOGGER.info("Sweeping file {}", workTicket.getPayloadURI());

			// persist payload in spectrum
			try (InputStream payloadToPersist = new FileInputStream(payloadFile)) {
				FS2MetaSnapshot metaSnapshot = StorageUtilities.persistPayload(payloadToPersist, workTicket, properties, false);
				workTicket.setPayloadURI(metaSnapshot.getURI().toString());
			}
			
			// persist the workticket
			StorageUtilities.persistWorkTicket(workTicket, properties);
		}

		LOGGER.info(constructMessage("Payload and workticket are persisted successfully"));
	}

	/**
	 * Method is used to move the file to the sweeped folder.
	 *
	 * @param file
	 *            The source location
	 * @param target
	 *            The target location
	 * @throws IOException
	 */
	private void delete(String file) throws IOException {
		Files.deleteIfExists(Paths.get(file));
	}

	/**
	 * Returns List of WorkTickets from java.io.File
	 *
	 * @param result workTickets
	 * @return list of worktickets
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws IOException
	 */
	private List<WorkTicket> generateWorkTickets(List<Path> result) throws IllegalArgumentException, IllegalAccessException, IOException {

		List<WorkTicket> workTickets = new ArrayList<>();
		WorkTicket workTicket = null;
		Map<String, Object> additionalContext = null;
		BasicFileAttributes attr = null;
		ISO8601Util dateUtil = new ISO8601Util();

		String folderName = null;
		String fileName = null;
		FileTime createdTime = null;
		FileTime modifiedTime = null;

		for (Path path : result) {

            LOGGER.debug("Obtaining file Attributes for path {}", path);
            additionalContext = new HashMap<String, Object>();

			workTicket = new WorkTicket();
            LOGGER.debug("Payload URI {}", path.toAbsolutePath().toString());
			workTicket.setPayloadURI(path.toAbsolutePath().toString());
			additionalContext.put(MailBoxConstants.KEY_FILE_PATH, path.toAbsolutePath().toString());

            LOGGER.debug("Pipeline ID {}", getPipeLineID());
			workTicket.setPipelineId(getPipeLineID());

			attr = Files.readAttributes(path, BasicFileAttributes.class);
            LOGGER.debug("File attributes{}", attr);

            createdTime = attr.creationTime();
            LOGGER.debug("Created Time stamp {}", createdTime);
            workTicket.setCreatedTime(new Date(createdTime.toMillis()));
            workTicket.addHeader(MailBoxConstants.KEY_FILE_CREATED_NAME, dateUtil.fromDate(workTicket.getCreatedTime()));

            modifiedTime = attr.lastModifiedTime();
            LOGGER.debug("Modified Time stamp {}", modifiedTime);
            workTicket.addHeader(MailBoxConstants.KEY_FILE_MODIFIED_NAME, dateUtil.fromDate(new Date(modifiedTime.toMillis())));

            LOGGER.debug("Size stamp {}", attr.size());
			workTicket.setPayloadSize(attr.size());

			fileName = path.toFile().getName();
            LOGGER.debug("Filename {}", fileName);
			workTicket.setFileName(fileName);
			workTicket.addHeader(MailBoxConstants.KEY_FILE_NAME, fileName);

			folderName = path.toFile().getParent();
		    LOGGER.debug("Foldername {}", folderName);
		    additionalContext.put(MailBoxConstants.KEY_MAILBOX_ID, configurationInstance.getMailbox().getPguid());
		    additionalContext.put(MailBoxConstants.KEY_FOLDER_NAME, folderName);
		    workTicket.addHeader(MailBoxConstants.KEY_FOLDER_NAME, folderName);

		    workTicket.setAdditionalContext(additionalContext);
		    workTicket.setProcessMode(ProcessMode.ASYNC);
			workTickets.add(workTicket);
			workTicket.setGlobalProcessId(MailBoxUtil.getGUID());
		}

        LOGGER.debug("WorkTickets size:{}, {}", workTickets.size(), workTickets.toArray());

        return workTickets;
	}

	/**
	 * Checks recently in-progress files are completed or not
	 *
	 * @param activeFilesList
	 *            list of recently update files
	 * @return list of worktickets
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws IOException
	 */
	private List<WorkTicket> retryGenWrkTktForActiveFiles(List<Path> activeFilesList) throws IllegalArgumentException, IllegalAccessException, IOException {

		List<Path> files = new ArrayList<>();
		for (Path file : activeFilesList) {

			if (!MailBoxUtil.validateLastModifiedTolerance(file)) {
				LOGGER.info("There are no changes in the file {} recently. So it is added in the sweeper list.", file.getFileName());
				files.add(file);
				continue;
			}
			LOGGER.info("The file {} is still modified within the tolerance. So it is removed from the current sweep list.", file.getFileName());
		}

		activeFilesList.clear();//Clearing the recently updated files after the second call.
		return generateWorkTickets(files);
	}

	/**
	 * Use to validate the given file can be added in the given group.
	 *
	 * @param workTicketGroup workticket group
	 * @param workTicket workticket
	 * @param staticProp sweeper properties
	 * @return true or false based on the size and number of files check
	 */
	protected Boolean canAddToGroup(WorkTicketGroup workTicketGroup, WorkTicket workTicket, SweeperPropertiesDTO staticProp) {

		long maxPayloadSize = 0;
		long maxNoOfFiles = 0;

		try {

			// retrieve required properties
			String payloadSize = staticProp.getPayloadSizeThreshold();
			String maxFile = staticProp.getNumOfFilesThreshold();

			if (!MailBoxUtil.isEmpty(payloadSize)) {
				maxPayloadSize = Long.parseLong(payloadSize);
			}
			if (!MailBoxUtil.isEmpty(maxFile)) {
				maxNoOfFiles = Long.parseLong(maxFile);
			}

		} catch (NumberFormatException e) {
			throw new MailBoxServicesException("The given threshold size is not a valid one.", Response.Status.CONFLICT);
		}

		if (maxPayloadSize == 0) {
			maxPayloadSize = 131072;
		}
		if (maxNoOfFiles == 0) {
			maxNoOfFiles = 10;
		}

		if (maxNoOfFiles <= workTicketGroup.getWorkTicketGroup().size()) {
			return false;
		}

		if (maxPayloadSize <= (getWorkTicketGroupFileSize(workTicketGroup) + workTicket.getPayloadSize())) {
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
	private long getWorkTicketGroupFileSize(WorkTicketGroup workTicketGroup) {

		long size = 0;

		for (WorkTicket workTicket : workTicketGroup.getWorkTicketGroup()) {
			size += workTicket.getPayloadSize();
		}

		return size;
	}

	@Override
	public Object getClient() {
		return null;
	}

	@Override
	public void cleanup() {
	}

	/**
	 * This Method create local folders if not available.
	 *
	 * * @param processorDTO it have details of processor
	 *
	 */
	@Override
	public void createLocalPath() {

		String configuredPath = null;
		try {
			configuredPath = getPayloadURI();
			createPathIfNotAvailable(configuredPath);

		} catch (IOException e) {
			throw new MailBoxConfigurationServicesException(Messages.LOCAL_FOLDERS_CREATION_FAILED,
					configuredPath, Response.Status.BAD_REQUEST,e.getMessage());
		}

	}

	/**
	 * Logs the TVAPI and ActivityStatus messages to LENS. This will be invoked for each file.
	 *
     * @param inputLocation folderPath for logging
     * @param wrkTicket workticket for logging
     */
    protected void logToLens(String inputLocation, WorkTicket wrkTicket) {

        //GLASS LOGGING BEGINS//
        TransactionVisibilityClient transactionVisibilityClient  = new TransactionVisibilityClient();
        GlassMessage glassMessage = new GlassMessage();
        glassMessage.setCategory(configurationInstance.getProcessorType());
        glassMessage.setProtocol(configurationInstance.getProcsrProtocol());
        glassMessage.setPipelineId(wrkTicket.getPipelineId());

        //Log running status
        glassMessage.setGlobalPId(wrkTicket.getGlobalProcessId());
        glassMessage.logProcessingStatus(StatusType.RUNNING, "Starting to sweep input folders for new files", configurationInstance.getProcsrProtocol(), configurationInstance.getProcessorType().name());
        glassMessage.setStatus(ExecutionState.PROCESSING);
        glassMessage.setInAgent(inputLocation);
        glassMessage.setInboundFileName(wrkTicket.getFileName());
        Long payloadSize = wrkTicket.getPayloadSize();
        if (payloadSize != null) {
            glassMessage.setInSize(payloadSize);
        }

        // Log FIRST corner
        glassMessage.logFirstCornerTimestamp();
        transactionVisibilityClient.logToGlass(glassMessage);
        // Log running status
        glassMessage.logProcessingStatus(StatusType.QUEUED, "Sweeper - Workticket queued for file " +  wrkTicket.getFileName(), configurationInstance.getProcsrProtocol(), configurationInstance.getProcessorType().name());
    }	
}
