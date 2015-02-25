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
import java.net.URISyntaxException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.ScriptException;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.liaison.commons.jaxb.JAXBUtility;
import com.liaison.commons.message.glass.dom.GatewayType;
import com.liaison.commons.util.ISO8601Util;
import com.liaison.dto.enums.ProcessMode;
import com.liaison.dto.queue.WorkTicket;
import com.liaison.dto.queue.WorkTicketGroup;
import com.liaison.fs2.api.FS2ObjectHeaders;
import com.liaison.fs2.api.exceptions.FS2Exception;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.dtdm.model.Processor;
import com.liaison.mailbox.enums.ExecutionEvents;
import com.liaison.mailbox.enums.ExecutionState;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.enums.ProcessorType;
import com.liaison.mailbox.enums.Protocol;
import com.liaison.mailbox.service.core.fsm.MailboxFSM;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.executor.javascript.JavaScriptExecutorUtil;
import com.liaison.mailbox.service.queue.sender.SweeperQueue;
import com.liaison.mailbox.service.storage.util.PayloadDetail;
import com.liaison.mailbox.service.storage.util.StorageUtilities;
import com.liaison.mailbox.service.util.GlassMessage;
import com.liaison.mailbox.service.util.MailBoxUtil;
import com.liaison.mailbox.service.util.ProcessorPropertyJsonMapper;
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

public class DirectorySweeperProcessor extends AbstractProcessor implements MailBoxProcessorI {

	private static final Logger LOGGER = LogManager.getLogger(DirectorySweeperProcessor.class);

	private String pipeLineID;
	private List<Path> inProgressFiles = new ArrayList<>();

	public void setPipeLineID(String pipeLineID) {
		this.pipeLineID = pipeLineID;
	}

	@SuppressWarnings("unused")
	private DirectorySweeperProcessor() {
		// to force creation of instance only by passing the processor entity
	}

	public DirectorySweeperProcessor(Processor configurationInstance) {
		super(configurationInstance);
	}

	TransactionVisibilityClient glassLogger = null;
	GlassMessage glassMessage = null;

	@Override
	public void invoke(String executionId,MailboxFSM fsm) {

		try {

			//GLASS LOGGING BEGINS//
			glassLogger = new TransactionVisibilityClient(executionId);
			glassMessage = new GlassMessage();
			glassMessage.setCategory(ProcessorType.SWEEPER);
			glassMessage.setProtocol(Protocol.SWEEPER.getCode());
			glassMessage.setExecutionId(executionId);
			glassMessage.setPipelineId(getPipeLineID());
			
			//GLASS LOGGING ENDS//
			boolean handoverExecutionToJS = getProperties().isHandOverExecutionToJavaScript();
			if (handoverExecutionToJS) {
				fsm.handleEvent(fsm.createEvent(ExecutionEvents.PROCESSOR_EXECUTION_HANDED_OVER_TO_JS));
				// Use custom G2JavascriptEngine
				JavaScriptExecutorUtil.executeJavaScript(configurationInstance.getJavaScriptUri(), this);
			} else {
				executeRequest();
			}
		} catch(JAXBException |IOException |IllegalAccessException | NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}

	private void executeRequest() {

		try {

		// Get root from folders input_folder
		String inputLocation = getPayloadURI();
		
		// retrieve required properties
		ArrayList<String> propertyNames = new ArrayList<String>();
		propertyNames.add(MailBoxConstants.FILE_RENAME_FORMAT_PROP_NAME);
		propertyNames.add(MailBoxConstants.PROPERTY_SWEEPED_FILE_LOCATION);
		Map<String, String> requiredProperties = ProcessorPropertyJsonMapper.getProcessorProperties(getProperties(), propertyNames);

		String fileRenameFormat = requiredProperties.get(MailBoxConstants.PROPERTY_FILE_RENAME_FORMAT);
		String sweepedFileLocation = requiredProperties.get(MailBoxConstants.PROPERTY_SWEEPED_FILE_LOCATION);
		
		fileRenameFormat = (fileRenameFormat == null) ? MailBoxConstants.SWEEPED_FILE_EXTN : fileRenameFormat;

		long timeLimit = MailBoxUtil.getEnvironmentProperties().getLong(MailBoxConstants.LAST_MODIFIED_TOLERANCE);
		// Validation of the necessary properties
		if (MailBoxUtil.isEmpty(inputLocation)) {
			throw new MailBoxServicesException(Messages.LOCATION_NOT_CONFIGURED, MailBoxConstants.PAYLOAD_LOCATION, Response.Status.CONFLICT);
		}

        LOGGER.debug("Is progress list is empty: {}", inProgressFiles.isEmpty());
        List<WorkTicket> workTickets = (inProgressFiles.isEmpty())
				? sweepDirectory(inputLocation, false, false, fileRenameFormat, timeLimit)
				: validateInprogressFiles(inProgressFiles, timeLimit);

		if (workTickets.isEmpty()) {
			LOGGER.info("There are no files to process.");
		} else {
            LOGGER.debug("There are {} files to process", workTickets.size());
			// Read from mailbox property - grouping js location
			List<WorkTicketGroup> workTicketGroups = groupingWorkTickets(workTickets);

			sweepedFileLocation = replaceTokensInFolderPath(sweepedFileLocation);
			if (!MailBoxUtil.isEmpty(sweepedFileLocation)) {
                LOGGER.info("Sweeped File Location ({}) is not available, so system is creating.", sweepedFileLocation);

				// If the given sweeped file location is not available then system will create that.
				Path path = Paths.get(sweepedFileLocation);
				if (!Files.isDirectory(path)) {
                    LOGGER.info("Creating Directories {}", path);

                    Files.createDirectories(path);
				} else {
                    LOGGER.info("Not creating, {} Is Directory", path);
                }
			}

			// Renaming the file
            LOGGER.debug("ABOUT TO MARK AS SWEEPED");
			markAsSweeped(workTickets, fileRenameFormat, sweepedFileLocation);

			if (workTicketGroups.isEmpty()) {
				LOGGER.info("The file group is empty, so NOP");
			} else {
				for (WorkTicketGroup workTicketGroup : workTicketGroups) {;
					String wrkTcktToSbr = constructMetaDataJson(workTicketGroup);
					LOGGER.info("Returns json response.{}", new JSONObject(wrkTcktToSbr).toString(2));
					postToSweeperQueue(wrkTcktToSbr);
					//For glass logging :(
					for (WorkTicket wrkTicket : workTicketGroup.getWorkTicketGroup()){
						glassMessage.setGlobalPId(wrkTicket.getGlobalProcessId());
						glassMessage.setStatus(ExecutionState.STAGED);

						if(inputLocation.contains("ftps")){
							glassMessage.setInAgent(GatewayType.FTPS);
						}if(inputLocation.contains("sftp")){
							glassMessage.setInAgent(GatewayType.SSH);
						}else if(inputLocation.contains("ftp")){
							glassMessage.setInAgent(GatewayType.FTP);
						}

						glassLogger.logToGlass(glassMessage);
					}
				}

			}
		}

		// call again when in-progress file list is not empty
		if (!inProgressFiles.isEmpty()) {
			executeRequest();
		}

		} catch (MailBoxServicesException | IOException | URISyntaxException
				| FS2Exception | JAXBException | NoSuchMethodException | ScriptException 
				| JSONException | IllegalAccessException | NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}


	/**
	 * Method is used to retrieve all the WorkTickets from the given mailbox.
	 *
	 * @param root
	 *            The mailbox root directory
	 * @param includeSubDir
	 * @param listDirectoryOnly
	 * @return List of WorkTicket
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws MailBoxServicesException
	 * @throws FS2Exception
	 * @throws JAXBException
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 * @throws SecurityException 
	 * @throws NoSuchFieldException 
	 */
	public List<WorkTicket> sweepDirectory(String root, boolean includeSubDir, boolean listDirectoryOnly,
			String fileRenameFormat, long timeLimit) throws IOException, URISyntaxException,
			MailBoxServicesException, FS2Exception, JAXBException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {

        LOGGER.debug("SweepingDirectory: {}", root);
		Path rootPath = Paths.get(root);
		if (!Files.isDirectory(rootPath)) {
			throw new MailBoxServicesException(Messages.INVALID_DIRECTORY, Response.Status.BAD_REQUEST);
		}

		List<Path> result = new ArrayList<>();
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(rootPath, defineFilter(listDirectoryOnly))) {
			for (Path file : stream) {
                LOGGER.debug("Sweeping file {}", file.toString());
				if (!file.getFileName().toString().contains(fileRenameFormat)) {

					if (validateLastModifiedTolerance(timeLimit, file)) {
						LOGGER.info("The file {} is in progress. So added in the in-progress list.", file.toString());

                        inProgressFiles.add(file);
						continue;
					}
					result.add(file);
				}
			}
		} catch (IOException e) {
			throw e; //TODO Code review- what's this catch throw for?
		}


        LOGGER.debug("Result size: {}, results {}", result.size(), result.toArray());
		return generateWorkTickets(result);
	}

	/**
	 * Method to get the pipe line id from the remote processor properties.
	 *
	 * @return
	 * @throws JAXBException
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 * @throws SecurityException 
	 * @throws NoSuchFieldException 
	 */
	private String getPipeLineID() throws JAXBException, JsonParseException, JsonMappingException, 
							IOException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {

		if (MailBoxUtil.isEmpty(this.pipeLineID)) {
			this.setPipeLineID(ProcessorPropertyJsonMapper.getProcessorProperty(getProperties(), MailBoxConstants.PROPERTY_PIPELINEID));
		}

		return this.pipeLineID;
	}

	/**
	 * Grouping the files based on the payload threshold and no of files threshold.
	 *
	 * @param workTickets
	 *            Group of all workTickets in a WorkTicketGroup.
	 * @return List of WorkTicketGroup
	 * @throws ScriptException
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws NoSuchMethodException
	 * @throws MailBoxServicesException
	 * @throws JAXBException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 * @throws SecurityException 
	 * @throws NoSuchFieldException 
	 */
	private List<WorkTicketGroup> groupingWorkTickets(List<WorkTicket> workTickets) throws ScriptException,
	IOException, URISyntaxException, NoSuchMethodException, MailBoxServicesException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, JAXBException {

		String groupingJsPath = configurationInstance.getJavaScriptUri();
		List<WorkTicketGroup> workTicketGroups = new ArrayList<>();

		if (!MailBoxUtil.isEmpty(groupingJsPath)) {

			// Use custom G2JavascriptEngine
			//TODO
			JavaScriptExecutorUtil.executeJavaScript(groupingJsPath, "init", workTickets, LOGGER);

		} else {

			if (workTickets.isEmpty()) {
				LOGGER.info("There are no files available in the directory.");
			}

			WorkTicketGroup workTicketGroup = new WorkTicketGroup();
			List <WorkTicket> workTicketsInGroup = new ArrayList <WorkTicket>();
			workTicketGroup.setWorkTicketGroup(workTicketsInGroup);
			for (WorkTicket workTicket : workTickets) {

				if (canAddToGroup(workTicketGroup, workTicket)) {
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
	 *            The input message to queue.
	 */
	private void postToSweeperQueue(String input)   {

        SweeperQueue.getInstance().sendMessages(input);
        LOGGER.debug("DirectorySweeper push postToQueue, message: {}", input);
	}

	/**
	 * Method is used to rename the processed files using given file rename
	 * format. If sweepedFileLocation is available in the mailbox files will be moved to the given
	 * location.
	 *
	 * @param workTickets
	 *            WorkTickets list.
	 * @param fileRenameFormat
	 *            The file rename format
	 * @throws IOException
	 * @throws JSONException
	 * @throws FS2Exception
	 * @throws URISyntaxException
	 * @throws JAXBException
	 * @throws JsonParseException
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 * @throws SecurityException 
	 * @throws NoSuchFieldException 
	 */
	public void markAsSweeped(List<WorkTicket> workTickets, String fileRenameFormat, String sweepedFileLocation)
			throws IOException, JSONException, FS2Exception, URISyntaxException, JsonParseException, JAXBException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {

		Path target = null;
		Path oldPath = null;
		Path newPath = null;
		PayloadDetail detail = null;

		if (!MailBoxUtil.isEmpty(sweepedFileLocation)) {
			target = Paths.get(sweepedFileLocation);
		}

		LOGGER.info("Renaming the processed files");
		for (WorkTicket workTicket : workTickets) {

			File payloadFile = new File(workTicket.getPayloadURI());
			oldPath = payloadFile.toPath();
			newPath = (target == null) ? oldPath.getParent().resolve(oldPath.toFile().getName() + fileRenameFormat)
						: target.resolve(oldPath.toFile().getName() + fileRenameFormat);
			String globalProcessId  = MailBoxUtil.getGUID();
			workTicket.setGlobalProcessId(globalProcessId);
			
			// retrieve required properties
			ArrayList<String> propertyNames = new ArrayList<String>();
			propertyNames.add(MailBoxConstants.PROPERTY_HTTPLISTENER_SECUREDPAYLOAD);
			propertyNames.add(MailBoxConstants.PROPERTY_DELETE_FILE_AFTER_SWEEP);
			Map<String, String> requiredProperties = ProcessorPropertyJsonMapper.getProcessorProperties(getProperties(), propertyNames);

			boolean securedPayload = Boolean.getBoolean(requiredProperties.get(MailBoxConstants.PROPERTY_HTTPLISTENER_SECUREDPAYLOAD));
			boolean deleteAfterSweep = Boolean.getBoolean(requiredProperties.get(MailBoxConstants.PROPERTY_DELETE_FILE_AFTER_SWEEP));
			// persist payload in spectrum
			try (InputStream payloadToPersist = new FileInputStream(payloadFile)) {
				FS2ObjectHeaders fs2Header = constructFS2Headers(workTicket);
				detail = StorageUtilities.persistPayload(payloadToPersist, globalProcessId,
						fs2Header, securedPayload);
				payloadToPersist.close();
			}

            if(deleteAfterSweep){
            	LOGGER.debug("Deleting file after sweep");
            	 delete(oldPath);
             }else{
            	 LOGGER.debug("Moving file after sweep");
            	move(oldPath, newPath);

            }
			//GSB-1353- After discussion with Joshua and Sean
			workTicket.setPayloadURI(detail.getMetaSnapshot().getURI().toString());
		}

		LOGGER.info("Renaming the processed files - done");

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
	private void move(Path file, Path target) throws IOException {
		Files.move(file, target, StandardCopyOption.REPLACE_EXISTING);
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
	private void delete(Path file) throws IOException {
		Files.deleteIfExists(file);
	}

	/**
	 * Method is used to construct the MetaData JSON from the workticketgroup dto.
	 *
	 * @param workTicketGroup
	 *            The workticketGroup object
	 * @return String MetaData JSON string
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws IOException
	 * @throws JAXBException
	 */

	private String constructMetaDataJson(WorkTicketGroup workTicketGroup) throws JsonGenerationException,
	JsonMappingException, IOException, JAXBException {

        LOGGER.debug("Construct MetaData for workTicketGroup of size :{}, {}", workTicketGroup.getWorkTicketGroup().size(), workTicketGroup.getWorkTicketGroup().toArray());
        String jsonResponse = JAXBUtility.marshalToJSON(workTicketGroup);
        LOGGER.debug("Constructed MetaData: {} ", jsonResponse);

        return jsonResponse;
    }

	/**
	 * Returns List of WorkTickets from  java.io.File
	 *
	 * @param result
	 *            workTickets
	 * @return list of workTickets
	 * @throws JAXBException
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 * @throws SecurityException 
	 * @throws NoSuchFieldException 
	 */
	private List<WorkTicket> generateWorkTickets(List<Path> result) throws JAXBException, IOException,
								NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {

		List<WorkTicket> workTickets = new ArrayList<>();
		WorkTicket workTicket = null;
		Map<String, Object> additionalContext = new HashMap<String, Object>();
		BasicFileAttributes attr = null;
		ISO8601Util dateUtil = new ISO8601Util();

		String folderName = null;
		String fileName = null;
		FileTime createdTime = null;
		FileTime modifiedTime = null;

		for (Path path : result) {

            LOGGER.debug("Obtaining file Attributes for path {}", path);

			workTicket = new WorkTicket();
            LOGGER.debug("Payload URI {}", path.toAbsolutePath().toString());
			workTicket.setPayloadURI(path.toAbsolutePath().toString());

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
		    additionalContext.put(MailBoxConstants.KEY_FOLDER_NAME, folderName);
		    workTicket.addHeader(MailBoxConstants.KEY_FOLDER_NAME, folderName);

		    workTicket.setAdditionalContext(additionalContext);
		    workTicket.setProcessMode(ProcessMode.ASYNC);
			workTickets.add(workTicket);
		}

        LOGGER.debug("WorkTickets size:{}, {}", workTickets.size(), workTickets.toArray());

        return workTickets;
	}

	/**
	 * Checks whether a file is modified with in the given time limit
	 *
	 * @param timelimit
	 *            to check the file is modified with in the given time limit
	 * @param file
	 *            File object
	 * @return true if it is updated with in the given time limit, false otherwise
	 */
	public boolean validateLastModifiedTolerance(long timelimit, Path file) {

		long system = System.currentTimeMillis();
		long lastmo = file.toFile().lastModified();

        LOGGER.debug("System time millis: {}, Last Modified {}, timelimit: {}", system, lastmo, timelimit);
        LOGGER.debug("(system - lastmo)/1000) = {}", ((system - lastmo)/1000));

		if (((system - lastmo)/1000) < timelimit) {
			return true;
		}

		return false;
	}

	/**
	 * Checks recently in-progress files are completed or not
	 *
	 * @param inprogressFiles
	 *            list of recently update files
	 * @param timelimit
	 *            period of the recent modifications
	 * @return list of file attribute dto
	 * @throws JAXBException
	 * @throws IOException
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 * @throws SecurityException 
	 * @throws NoSuchFieldException 
	 */
	private List<WorkTicket> validateInprogressFiles(List<Path> inprogressFiles, long timelimit)
			throws JAXBException, IOException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {

		List<Path> files = new ArrayList<>();
		for (Path file : inprogressFiles) {

			if (!validateLastModifiedTolerance(timelimit, file)) {
				LOGGER.info("There are no changes in the file {} recently. So it is added in the sweeper list.", file.getFileName());
				files.add(file);
				continue;
			}
			LOGGER.info("The file {} is still in progress. So it is removed from the in-progress list.", file.getFileName());
		}

		inprogressFiles.clear();//Clearing the recently updated files after the second call.
		return generateWorkTickets(files);
	}

	/**
	 * Method to construct FS2ObjectHeaders from the given workTicket
	 *
	 * @param workTicket
	 * @return FS2ObjectHeaders
	 * @throws IOException
	 * @throws MailBoxServicesException
	 */
	private FS2ObjectHeaders constructFS2Headers(WorkTicket workTicket) throws MailBoxServicesException, IOException {

		FS2ObjectHeaders fs2Header = new FS2ObjectHeaders();
		fs2Header.addHeader(MailBoxConstants.KEY_GLOBAL_PROCESS_ID, workTicket.getGlobalProcessId());
		fs2Header.addHeader(MailBoxConstants.KEY_RAW_PAYLOAD_SIZE, workTicket.getPayloadSize().toString());
		fs2Header.addHeader(MailBoxConstants.KEY_PIPELINE_ID, workTicket.getPipelineId());
		fs2Header.addHeader(MailBoxConstants.KEY_PAYLOAD_DESCRIPTION, String.format(MailBoxConstants.PAYLOAD_DESCRIPTION_VALUE, getPayloadURI()));
		fs2Header.addHeader(MailBoxConstants.KEY_SERVICE_INSTANCE_ID, configurationInstance.getServiceInstance().getName());
		fs2Header.addHeader(MailBoxConstants.KEY_TENANCY_KEY, (MailBoxConstants.PIPELINE_FULLY_QUALIFIED_PACKAGE + ":" + workTicket.getPipelineId()));
		LOGGER.debug("FS2 Headers set are {}", fs2Header.getHeaders());
		return fs2Header;
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
	 * @throws IOException 
	 * @throws JAXBException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 * @throws SecurityException 
	 * @throws NoSuchFieldException 
	 */
	protected Boolean canAddToGroup(WorkTicketGroup workTicketGroup, WorkTicket workTicket) throws MailBoxServicesException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, JAXBException, IOException {

		long maxPayloadSize = 0;
		long maxNoOfFiles = 0;

		try {

			// retrieve required properties
			ArrayList<String> propertyNames = new ArrayList<String>();
			propertyNames.add(MailBoxConstants.PROPERTY_PAYLOAD_SIZE_THRESHOLD);
			propertyNames.add(MailBoxConstants.PROPERTY_NO_OF_FILES_THRESHOLD);
			Map<String, String> requiredProperties = ProcessorPropertyJsonMapper.getProcessorProperties(getProperties(), propertyNames);

			String payloadSize = requiredProperties.get(MailBoxConstants.PROPERTY_PAYLOAD_SIZE_THRESHOLD);
			String maxFile = requiredProperties.get(MailBoxConstants.PROPERTY_NO_OF_FILES_THRESHOLD);
		
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void downloadDirectory(Object client, String remotePayloadLocation, String localTargetLocation) {
		// TODO Auto-generated method stub

	}

	@Override
	public void uploadDirectory(Object client, String localPayloadLocation, String remoteTargetLocation) {
		// TODO Auto-generated method stub

	}

	@Override
	public void cleanup() {
		// TODO Auto-generated method stub

	}
}
