/**
 * Copyright 2014 Liaison Technologies, Inc.
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.core.processor;

import com.liaison.commons.jaxb.JAXBUtility;
import com.liaison.commons.logging.LogTags;
import com.liaison.commons.messagebus.client.exceptions.ClientUnavailableException;
import com.liaison.commons.util.ISO8601Util;
import com.liaison.commons.util.client.http.HTTPRequest;
import com.liaison.commons.util.client.http.HTTPResponse;
import com.liaison.dto.enums.ProcessMode;
import com.liaison.dto.queue.WorkTicket;
import com.liaison.dto.queue.WorkTicketGroup;
import com.liaison.fs2.metadata.FS2MetaSnapshot;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.dtdm.dao.ProcessorConfigurationDAO;
import com.liaison.mailbox.dtdm.dao.ProcessorConfigurationDAOBase;
import com.liaison.mailbox.dtdm.model.Processor;
import com.liaison.mailbox.enums.EntityStatus;
import com.liaison.mailbox.enums.ExecutionState;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.service.core.email.EmailNotifier;
import com.liaison.mailbox.service.dto.GlassMessageDTO;
import com.liaison.mailbox.service.dto.configuration.TriggerProcessorRequestDTO;
import com.liaison.mailbox.service.dto.configuration.processor.properties.SweeperPropertiesDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.executor.javascript.JavaScriptExecutorUtil;
import com.liaison.mailbox.service.glass.util.ExecutionTimestamp;
import com.liaison.mailbox.service.glass.util.GlassMessage;
import com.liaison.mailbox.service.glass.util.MailboxGlassMessageUtil;
import com.liaison.mailbox.service.queue.sender.SweeperQueueSendClient;
import com.liaison.mailbox.service.storage.util.StorageUtilities;
import com.liaison.mailbox.service.util.MailBoxUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.liaison.mailbox.MailBoxConstants.BYTE_ARRAY_INITIAL_SIZE;
import static com.liaison.mailbox.MailBoxConstants.CONFIGURATION_CONNECTION_TIMEOUT;
import static com.liaison.mailbox.MailBoxConstants.CONFIGURATION_SERVICE_BROKER_ASYNC_URI;

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
	private static final String PROCESS = "process";
	private static final int MAX_PAYLOAD_SIZE_IN_WORKTICKET_GROUP = 131072;
	private static final int MAX_NUMBER_OF_FILES_IN_GROUP = 10;

    private static final String STALE_FILE_NOTIFICATION_SUBJECT = "Deleting Stale files in Sweeper";
    private static final String STALE_FILE_NOTIFICATION_EMAIL_FILES = "Files :";
    private static final String STALE_FILE_NOTIFICATION_EMAIL_CONTENT = "Deleting stale files in Sweeper named '%s' from the location '%s'";
    private static final String LINE_SEPARATOR = System.lineSeparator();
    private static final Object SORT_BY_NAME = "Name";
    private static final Object SORT_BY_SIZE = "Size";

    private String pipelineId;
    private List<Path> activeFiles = new ArrayList<>();
    private SweeperPropertiesDTO staticProp;

    public void setPipeLineID(String pipeLineID) {
		this.pipelineId = pipeLineID;
	}

    public void setStaticProp(SweeperPropertiesDTO staticProp) {
        this.staticProp = staticProp;
    }

	@SuppressWarnings("unused")
	private DirectorySweeper() {
		// to force creation of instance only by passing the processor entity
	}

	public DirectorySweeper(Processor configurationInstance) {
		super(configurationInstance);
	}


	@Override
	public void runProcessor(Object dto) {
        setReqDTO((TriggerProcessorRequestDTO) dto);
        run();
	}

	private void run() {

        try {

            // Get root from folders input_folder
            String inputLocation = getPayloadURI();

            // retrieve required properties
            this.setStaticProp((SweeperPropertiesDTO) getProperties());

            // Validation of the necessary properties
            if (MailBoxUtil.isEmpty(inputLocation)) {
            	throw new MailBoxServicesException(Messages.LOCATION_NOT_CONFIGURED, MailBoxConstants.PAYLOAD_LOCATION, Response.Status.CONFLICT);
            }

            long startTime = System.currentTimeMillis();
            LOGGER.info(constructMessage("Start run"));           

            LOGGER.debug("Is in-progress file list is empty: {}", activeFiles.isEmpty());
            List<WorkTicket> workTicketsToPost = new ArrayList<WorkTicket>();
            List<WorkTicket> workTickets = (activeFiles.isEmpty())
            								? sweepDirectory(inputLocation, staticProp)
            								: retryGenWrkTktForActiveFiles(activeFiles);

			if (!workTickets.isEmpty()) {
			    
			    if (!staticProp.isAllowEmptyFiles()) {
			        
			        for (WorkTicket workTicket : workTickets) {
			            
			            if (0 == workTicket.getPayloadSize()) {
			                LOGGER.warn(constructMessage("The file {} is empty and empty files not allowed"), workTicket.getFileName());
			                logToLens(workTicket, null, ExecutionState.VALIDATION_ERROR);
			                String filePath = String.valueOf((Object) workTicket.getAdditionalContextItem(MailBoxConstants.KEY_FILE_PATH));
			                delete(filePath);
			            } else {
			                workTicketsToPost.add(workTicket);
			            }
			        }
			    } else {
			        workTicketsToPost.addAll(workTickets);
			    }

				LOGGER.debug("There are {} files to process", workTicketsToPost.size());
				if (ProcessMode.SYNC.name().equals(staticProp.getProcessMode())) {
					syncSweeper(workTicketsToPost, staticProp);
				} else {
					asyncSweeper(workTicketsToPost, staticProp);
				}
			}

            // retry when in-progress file list is not empty
            if (!activeFiles.isEmpty()) {
            	run();
            }
            long endTime = System.currentTimeMillis();

            LOGGER.info(constructMessage("Number of files processed {}"), workTicketsToPost.size());
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
     * Async directory sweeper posts workticket to queue
     *
     * @param workTickets worktickets
     * @param staticProp sweeper property
     * @throws IllegalAccessException
     * @throws IOException
     * @throws JAXBException
     * @throws JSONException
     */
    private void asyncSweeper(List<WorkTicket> workTickets, SweeperPropertiesDTO staticProp)
            throws IllegalAccessException, IOException, JAXBException, JSONException {

        // Read from mailbox property - grouping js location
        List<WorkTicketGroup> workTicketGroups = groupingWorkTickets(workTickets, staticProp);

        //first corner timestamp
        ExecutionTimestamp firstCornerTimeStamp = ExecutionTimestamp.beginTimestamp(GlassMessage.DEFAULT_FIRST_CORNER_NAME);

        if (workTicketGroups.isEmpty()) {
            LOGGER.debug("The file group is empty");
        } else {

            for (WorkTicketGroup workTicketGroup : workTicketGroups) {

                //Interrupt signal for async sweeper
                if (MailBoxUtil.isInterrupted(Thread.currentThread().getName())) {
                    LOGGER.warn(constructMessage("The executor is gracefully interrupted."));
                    return;
                }

                LOGGER.debug("Persist workticket from workticket group to spectrum");
                persistPayloadAndWorkticket(workTicketGroup.getWorkTicketGroup(), staticProp);

                String wrkTcktToSbr = JAXBUtility.marshalToJSON(workTicketGroup);
                LOGGER.debug(constructMessage("Workticket posted to SB queue.{}"), new JSONObject(wrkTcktToSbr).toString(2));
                postToSweeperQueue(wrkTcktToSbr);

                // For glass logging
                for (WorkTicket wrkTicket : workTicketGroup.getWorkTicketGroup()) {

                    //Fish tag global process id
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

                }
            }
        }
    }

   /**
     * sync directory sweeper posts wotkticket to REST endpoint
     *
     * @param workTickets worktickets
     * @param staticProp sweeper properties
     */
    private void syncSweeper(List<WorkTicket> workTickets, SweeperPropertiesDTO staticProp) {

        //first corner timestamp
        ExecutionTimestamp firstCornerTimeStamp = ExecutionTimestamp.beginTimestamp(GlassMessage.DEFAULT_FIRST_CORNER_NAME);

        //sort the worktickets
        sortWorkTicket(workTickets, staticProp.getSort());

        String serviceBrokerUri = MailBoxUtil.getEnvironmentProperties().getString(CONFIGURATION_SERVICE_BROKER_ASYNC_URI);
        if (MailBoxUtil.isEmpty(serviceBrokerUri)) {
            throw new RuntimeException("Service Broker URI not configured ('" + CONFIGURATION_SERVICE_BROKER_ASYNC_URI + "'), cannot process sync");
        }

        int connectionTimeout = MailBoxUtil.getEnvironmentProperties().getInt(CONFIGURATION_CONNECTION_TIMEOUT);

        try {

            for (WorkTicket workTicket : workTickets) {

                //Interrupt signal for sync sweeper
                if (MailBoxUtil.isInterrupted(Thread.currentThread().getName())) {
                    LOGGER.warn("The executor is gracefully interrupted.");
                    return;
                }

                ThreadContext.put(LogTags.GLOBAL_PROCESS_ID, workTicket.getGlobalProcessId());
                persistPayloadAndWorkticket(staticProp, workTicket);
                workTicket.setProcessMode(ProcessMode.SYNC);

                logToLens(workTicket, firstCornerTimeStamp, ExecutionState.PROCESSING);
                LOGGER.info(constructMessage("Global PID",
                        seperator,
                        workTicket.getGlobalProcessId(),
                        " submitted for file ",
                        workTicket.getFileName()));

                try (ByteArrayOutputStream responseStream = new ByteArrayOutputStream(BYTE_ARRAY_INITIAL_SIZE)) {

                    HTTPRequest request = HTTPRequest.post(serviceBrokerUri)
                            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                            .connectionTimeout(connectionTimeout)
                            .inputData(JAXBUtility.marshalToJSON(workTicket))
                            .outputStream(responseStream);

                    // execute request and handle response
                    HTTPResponse response = request.execute();
                    if (!MailBoxUtil.isSuccessful(response.getStatusCode())) {
                        throw new RuntimeException("Failed to process the payload in sync sweeper - " + response.getStatusCode());
                    } else {
                        verifyAndDeletePayload(workTicket);
                    }

                } catch (Exception e) {

                    GlassMessageDTO glassMessageDTO = new GlassMessageDTO();
                    glassMessageDTO.setGlobalProcessId(workTicket.getGlobalProcessId());
                    glassMessageDTO.setProcessorType(configurationInstance.getProcessorType());
                    glassMessageDTO.setProcessProtocol(configurationInstance.getProcsrProtocol());
                    glassMessageDTO.setFileName(workTicket.getFileName());
                    glassMessageDTO.setStatus(ExecutionState.FAILED);
                    glassMessageDTO.setMessage(e.getMessage());
                    MailboxGlassMessageUtil.logGlassMessage(glassMessageDTO);

                    throw new RuntimeException(e);
                }
            }

        } catch (IOException e) {
            LOGGER.error(constructMessage("Error occurred in sync sweeper", seperator, e.getMessage()), e);
            throw new RuntimeException(e);
        } finally {
            //Fish tag global process id
            ThreadContext.clearMap();
        }

    }

    /**
     * verifies whether the payload persisted in spectrum or not and deletes it
     *
     * @param wrkTicket workticket contains payload uri
     * @throws IOException
     */
    private void verifyAndDeletePayload(WorkTicket wrkTicket) throws IOException {

        String payloadURI = wrkTicket.getPayloadURI();
        String filePath = String.valueOf((Object) wrkTicket.getAdditionalContextItem(MailBoxConstants.KEY_FILE_PATH));
        // Delete the file if it exists in spectrum and should be successfully posted to SB Queue.
        if (StorageUtilities.isPayloadExists(wrkTicket.getPayloadURI())) {
            LOGGER.debug("Payload {} exists in spectrum. so deleting the file {}", payloadURI, filePath);
            delete(filePath);
        } else {
            LOGGER.warn("Payload {} does not exist in spectrum. so file {} is not deleted.", payloadURI, filePath);
        }
        LOGGER.info(constructMessage("Global PID",
                seperator,
                wrkTicket.getGlobalProcessId(),
                " deleted the file ",
                wrkTicket.getFileName()));
    }

	/**
	 * Method is used to retrieve all the WorkTickets from the given mailbox.
	 *
	 * @param root The mailbox root directory
	 * @param staticProp sweeper properties
	 * @return list of worktickets
	 * @throws IllegalAccessException
	 * @throws IOException
	 */
	public List<WorkTicket> sweepDirectory(String root, SweeperPropertiesDTO staticProp) throws IllegalAccessException, IOException {

        LOGGER.info(constructMessage("Scanning Directory: {}"), root);
		Path rootPath = Paths.get(root);
		if (!Files.isDirectory(rootPath)) {
			throw new MailBoxServicesException(Messages.INVALID_DIRECTORY, Response.Status.BAD_REQUEST);
		}

		List<Path> result = new ArrayList<>();
        listFiles(result, rootPath, staticProp);

        LOGGER.debug("Result size: {}, results {}", result.size(), result.toArray());
		return generateWorkTickets(result);
	}

    /**
     * Traverse the directory and list the files
     * Sub Directories will be included if sweepSubDirectories set to true
     *
     * @param rootPath Sweeper Payload Location
     * @param staticProp sweeper properties
     */
    private void listFiles(List<Path> files, Path rootPath, SweeperPropertiesDTO staticProp) {

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(rootPath)) {

            String fileName = null;
            for (Path file : stream) {

                fileName = file.getFileName().toString();
                //Sweep Directories if the property is set to true
                if (Files.isDirectory(file)) {
                    if (staticProp.isSweepSubDirectories()) {
                        listFiles(files, file, staticProp);
                    }
                    continue;
                }

                // Check if the file to be uploaded is included or not excluded
                if (!checkFileIncludeorExclude(staticProp.getIncludedFiles(),
                        fileName,
                        staticProp.getExcludedFiles())) {
                    continue;
                }

                LOGGER.debug(constructMessage("Sweeping file {}"), file.toString());
                if (MailBoxUtil.validateLastModifiedTolerance(file)) {
                    LOGGER.info(constructMessage("The file {} is modified within tolerance. So added in the in-progress list."), file.toString());
                    activeFiles.add(file);
                    continue;
                }
                
                files.add(file);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
	 * @throws IllegalAccessException
	 * @throws IOException
	 */
	private List<WorkTicketGroup> groupingWorkTickets(List<WorkTicket> workTickets, SweeperPropertiesDTO staticProp) throws IllegalAccessException, IOException {

		String groupingJsPath = configurationInstance.getJavaScriptUri();
		List<WorkTicketGroup> workTicketGroups = new ArrayList<>();

		if (!MailBoxUtil.isEmpty(groupingJsPath)) {
			JavaScriptExecutorUtil.executeJavaScript(groupingJsPath, PROCESS, workTickets, LOGGER);
		} else {

			if (workTickets.isEmpty()) {
				LOGGER.info(constructMessage("There are no files available in the directory."));
			}
			
			sortWorkTicket(workTickets, staticProp.getSort());
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
	 * Method to sort work tickets based on name/size/date
	 * 
	 * @param workTickets worktickets
	 * @param sortType sort type
	 */
    private void sortWorkTicket(List<WorkTicket> workTickets, String sortType) {
    
        if (SORT_BY_NAME.equals(sortType)) {
             workTickets.sort((w1, w2) -> w1.getFileName().compareTo(w2.getFileName()));
        } else if(SORT_BY_SIZE.equals(sortType)) {
            workTickets.sort((w1, w2) -> w1.getPayloadSize().compareTo(w2.getPayloadSize()));
        } else {
            ISO8601Util dateUtil = new ISO8601Util();
            workTickets.sort((w1, w2) -> dateUtil.fromDate(w1.getCreatedTime()).compareTo(dateUtil.fromDate(w2.getCreatedTime())));
        }
    }

    /**
	 * Method to post meta data to rest service/ queue.
	 *
	 * @param input
	 * The input message to queue.
	 */
	private void postToSweeperQueue(String input)   {

        try {
			SweeperQueueSendClient.getInstance().sendMessage(input);
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
	private void persistPayloadAndWorkticket(List<WorkTicket> workTickets, SweeperPropertiesDTO staticProp) throws IOException {

		LOGGER.debug(constructMessage("Persisting paylaod and workticket in spectrum starts"));
		for (WorkTicket workTicket : workTickets) {
			persistPayloadAndWorkticket(staticProp, workTicket);
		}

		LOGGER.info(constructMessage("Payload and workticket are persisted successfully"));
	}

    /**
     * overloaded method to persist the payload and workticket
     *
     * @param staticProp staic properties
     * @param workTicket workticket
     * @throws IOException
     */
    private void persistPayloadAndWorkticket(SweeperPropertiesDTO staticProp, WorkTicket workTicket) throws IOException {

        File payloadFile = new File(workTicket.getPayloadURI());

        Map<String, String> properties = new HashMap<String, String>();
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

        // persist payload in spectrum
        try (InputStream payloadToPersist = new FileInputStream(payloadFile)) {
            FS2MetaSnapshot metaSnapshot = StorageUtilities.persistPayload(payloadToPersist, workTicket, properties, false);
            workTicket.setPayloadURI(metaSnapshot.getURI().toString());
        }

        // persist the workticket
        StorageUtilities.persistWorkTicket(workTicket, properties);
    }

	/**
	 * Method is used to move the file to the sweeped folder.
	 *
	 * @param file the file to be deleted
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
	 * @throws IllegalAccessException
	 * @throws IOException
	 */
	private List<WorkTicket> generateWorkTickets(List<Path> result) throws IOException, IllegalAccessException {

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
	private Boolean canAddToGroup(WorkTicketGroup workTicketGroup, WorkTicket workTicket, SweeperPropertiesDTO
            staticProp) {

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
			maxPayloadSize = MAX_PAYLOAD_SIZE_IN_WORKTICKET_GROUP;
		}
		if (maxNoOfFiles == 0) {
			maxNoOfFiles = MAX_NUMBER_OF_FILES_IN_GROUP;
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
	 * @param workTicketGroup
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
     * @param wrkTicket workticket for logging
	 * @param firstCornerTimeStamp first corner timestamp
	 * @param state Execution Status
     */
    protected void logToLens(WorkTicket wrkTicket, ExecutionTimestamp firstCornerTimeStamp, ExecutionState state) {

        String filePath = wrkTicket.getAdditionalContextItem(MailBoxConstants.KEY_FOLDER_NAME).toString();
        
        StringBuilder message;
        if (ExecutionState.VALIDATION_ERROR.equals(state)) {
            message = new StringBuilder()
                .append("File size is empty ")
                .append(filePath)
                .append(", and empty files are not allowed");
        } else {
            message = new StringBuilder()
                .append("Starting to sweep input folder ")
                .append(filePath)
                .append(" for new files");
        }
       
        GlassMessageDTO glassMessageDTO = new GlassMessageDTO();
        glassMessageDTO.setGlobalProcessId(wrkTicket.getGlobalProcessId());
        glassMessageDTO.setProcessorType(configurationInstance.getProcessorType());
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

        MailboxGlassMessageUtil.logGlassMessage(glassMessageDTO);

    }	
    
    /**
     * Method to clean up stale files in the payload location of sweeper
     * 
     * @throws MailBoxServicesException
     * @throws IOException
     */
    public void cleanupStaleFiles() throws MailBoxServicesException, IOException {

        List<String> staleFiles = new ArrayList<>();
        String payloadLocation = getPayloadURI();

        //validates sweeper location
        final Path payloadPath = Paths.get(payloadLocation);
        FileSystem fileSystem = FileSystems.getDefault();
        String pattern = MailBoxUtil.getEnvironmentProperties().getString(DATA_FOLDER_PATTERN, DEFAULT_DATA_FOLDER_PATTERN);
        PathMatcher pathMatcher = fileSystem.getPathMatcher(pattern);

        if (!Files.isDirectory(payloadPath) || !pathMatcher.matches(payloadPath)) {

            //inactivate the mailboxes which doesn't have valid directory
            ProcessorConfigurationDAO dao = new ProcessorConfigurationDAOBase();
            configurationInstance.setProcsrStatus(EntityStatus.INACTIVE.name());
            configurationInstance.setModifiedBy("WatchDog Service");
            configurationInstance.setModifiedDate(new Date());
            dao.merge(configurationInstance);
            throw new MailBoxServicesException(Messages.INVALID_DIRECTORY, Response.Status.BAD_REQUEST);
        }

        // filter and delete stale files in the sweeper location
        deleteStaleFiles(payloadPath, staleFiles);

        // notify deletion of stale files to users through mail configured in mailbox
        if (!staleFiles.isEmpty()) {
			notifyDeletionOfStaleFiles(staleFiles, payloadLocation);
		}
    }
    
    /**
     * Traverse the directory and deletes the stale files
     *
     * @param rootPath Payload location
     * @param fileNames Deleted files list
     */
    public void deleteStaleFiles(Path rootPath, List<String> fileNames) {

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(rootPath)) {

            String fileName = null;
            for (Path file : stream) {

                fileName = file.getFileName().toString();
                //Sweep Directories if the property is set to true
                if (Files.isDirectory(file)) {
                    deleteStaleFiles(file, fileNames);
                    continue;
                }

                int staleFileTTL = ((SweeperPropertiesDTO) getProperties()).getStaleFileTTL();
                if (!MailBoxUtil.isFileExpired(file.toFile().lastModified(), staleFileTTL)) {
                    continue;
                }

                Files.deleteIfExists(file);
                fileNames.add(fileName);
                LOGGER.info("Stale file {} deleted successfully in sweeper location {} ", fileName, file.toAbsolutePath().toString());
            }
        } catch (IOException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
	
	/**
	 * Method to notify deletion of stale files to customer
	 * 
	 * @param staleFiles - list of stale files
	 * @param payloadLocation - payload location in which stale files are to be cleaned up
	 */
	private void notifyDeletionOfStaleFiles(List<String> staleFiles, String payloadLocation) {
		
		// stale file deletion email subject
		String emailSubject = String.format(STALE_FILE_NOTIFICATION_SUBJECT, 
									configurationInstance.getPguid(), 
									configurationInstance.getProcsrName(), 
									payloadLocation);
		
		// stale file deletion email content
		String emailContentPrefix = String.format(STALE_FILE_NOTIFICATION_EMAIL_CONTENT, 
										   configurationInstance.getProcsrName(),
										   payloadLocation);
		StringBuilder body = new StringBuilder(emailContentPrefix)
			.append(LINE_SEPARATOR)
			.append(LINE_SEPARATOR)
			.append(STALE_FILE_NOTIFICATION_EMAIL_FILES)
			.append(LINE_SEPARATOR)
			.append(StringUtils.join(staleFiles, LINE_SEPARATOR));
		EmailNotifier.sendEmail(configurationInstance, emailSubject, body.toString(), true);
		
	}
}
