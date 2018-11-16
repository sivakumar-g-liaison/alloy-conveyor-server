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
import com.liaison.commons.util.ISO8601Util;
import com.liaison.commons.util.client.http.HTTPRequest;
import com.liaison.commons.util.client.http.HTTPResponse;
import com.liaison.dto.enums.ProcessMode;
import com.liaison.dto.queue.WorkTicket;
import com.liaison.fs2.metadata.FS2MetaSnapshot;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.dtdm.dao.ProcessorConfigurationDAO;
import com.liaison.mailbox.dtdm.dao.ProcessorConfigurationDAOBase;
import com.liaison.mailbox.dtdm.model.Processor;
import com.liaison.mailbox.enums.EntityStatus;
import com.liaison.mailbox.enums.ExecutionState;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.service.core.SweeperProcessorService;
import com.liaison.mailbox.service.core.email.EmailNotifier;
import com.liaison.mailbox.service.dto.configuration.TriggerProcessorRequestDTO;
import com.liaison.mailbox.service.dto.configuration.processor.properties.SweeperPropertiesDTO;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.executor.javascript.JavaScriptExecutorUtil;
import com.liaison.mailbox.service.glass.util.ExecutionTimestamp;
import com.liaison.mailbox.service.glass.util.GlassMessage;
import com.liaison.mailbox.service.storage.util.StorageUtilities;
import com.liaison.mailbox.service.thread.pool.SweeperProcessThreadPool;
import com.liaison.mailbox.service.util.MailBoxUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
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
import java.util.concurrent.ThreadPoolExecutor;

import static com.liaison.mailbox.MailBoxConstants.BYTE_ARRAY_INITIAL_SIZE;
import static com.liaison.mailbox.MailBoxConstants.CONFIGURATION_CONNECTION_TIMEOUT;
import static com.liaison.mailbox.MailBoxConstants.CONFIGURATION_SERVICE_BROKER_ASYNC_URI;
import static com.liaison.mailbox.MailBoxConstants.CONFIGURATION_SOCKET_TIMEOUT;
import static com.liaison.mailbox.service.util.MailBoxUtil.DATA_FOLDER_PATTERN;
import static com.liaison.mailbox.service.util.MailBoxUtil.getEnvironmentProperties;

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

    private static final String STALE_FILE_NOTIFICATION_SUBJECT = "Deleting Stale files in Sweeper";
    private static final String STALE_FILE_NOTIFICATION_EMAIL_FILES = "Files :";
    private static final String STALE_FILE_NOTIFICATION_EMAIL_CONTENT = "Deleting stale files in Sweeper named '%s' from the location '%s'";
    private static final String LINE_SEPARATOR = System.lineSeparator();
    private static final String WATCH_DOG_SERVICE = "WatchDog Service";
    private static final String SWEEPER_MULTI_THREAD_ENABLED = "com.liaison.mailbox.sweeper.multi.thread.enabled";
    private static final boolean IS_SWEEPER_MULTI_THREAD_ENABLED = getEnvironmentProperties().getBoolean(SWEEPER_MULTI_THREAD_ENABLED, true);

    private List<Path> activeFiles = new ArrayList<>();
    private SweeperPropertiesDTO staticProp;

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
			            
			            if (isPayloadValid(workTicket)) {

			                workTicketsToPost.add(workTicket);
			            } else {
			                
			                //Interrupt signal for empty files
			                if (MailBoxUtil.isInterrupted(Thread.currentThread().getName())) {
			                    LOGGER.warn(constructMessage("The executor is gracefully interrupted."));
			                    return;
			                }
			                
			                LOGGER.warn(constructMessage("The file {} is empty and empty files not allowed"), workTicket.getFileName());
			                logToLens(workTicket, null, ExecutionState.VALIDATION_ERROR, null);
			                String filePath = String.valueOf((Object) workTicket.getAdditionalContextItem(MailBoxConstants.KEY_FILE_PATH));
			                delete(filePath);
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
        } catch (MailBoxServicesException | IOException | IllegalAccessException e) {
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
     */
    private void asyncSweeper(List<WorkTicket> workTickets, SweeperPropertiesDTO staticProp) {

        for (WorkTicket workTicket : workTickets) {
            ThreadPoolExecutor executorService = SweeperProcessThreadPool.getExecutorService();
            if (IS_SWEEPER_MULTI_THREAD_ENABLED && executorService.getActiveCount() < executorService.getCorePoolSize()) {
                LOGGER.info(constructMessage("Number of active thread count {}"), SweeperProcessThreadPool.getExecutorService().getActiveCount());
                SweeperProcessThreadPool.getExecutorService().submit(new SweeperProcessorService(workTicket, configurationInstance, staticProp));
            }  else {
                LOGGER.info("Active thread count reached maximum core pool size");
                new SweeperProcessorService(workTicket, configurationInstance,  staticProp).doProcess(workTicket);
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
        int socketTimeout = MailBoxUtil.getEnvironmentProperties().getInt(CONFIGURATION_SOCKET_TIMEOUT);

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

                logToLens(workTicket, firstCornerTimeStamp, ExecutionState.PROCESSING, null);
                LOGGER.info(constructMessage("Global PID",
                        seperator,
                        workTicket.getGlobalProcessId(),
                        " submitted for file ",
                        workTicket.getFileName()));

                try (ByteArrayOutputStream responseStream = new ByteArrayOutputStream(BYTE_ARRAY_INITIAL_SIZE)) {

                    HTTPRequest request = HTTPRequest.post(serviceBrokerUri)
                            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                            .connectionTimeout(connectionTimeout)
                            .socketTimeout(socketTimeout)
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
                    logSweeperFailedStatus(workTicket, e);
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

        result = filterFiles(result);
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
                if (!checkFileIncludeOrExclude(staticProp.getIncludedFiles(),
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
     * overloaded method to persist the payload and workticket
     *
     * @param staticProp staic properties
     * @param workTicket workticket
     * @throws IOException
     */
    private void persistPayloadAndWorkticket(SweeperPropertiesDTO staticProp, WorkTicket workTicket) throws IOException {

        File payloadFile = new File(workTicket.getPayloadURI());
        Map<String, String> properties = setProperties(staticProp, workTicket);

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
		files = filterFiles(files);

		return generateWorkTickets(files);
	}

	@Override
	public Object getClient() {
		return null;
	}

	@Override
	public void cleanup() {
	}
    
    /**
     * Method to clean up stale files in the payload location of sweeper
     * 
     * @throws MailBoxServicesException
     */
    public void cleanupStaleFiles() throws MailBoxServicesException {

        List<String> staleFiles = new ArrayList<>();
        String payloadLocation = getPayloadURI();

        //validates sweeper location
        final Path payloadPath = Paths.get(payloadLocation);
        FileSystem fileSystem = FileSystems.getDefault();
        PathMatcher pathMatcher = fileSystem.getPathMatcher(DATA_FOLDER_PATTERN);

        if (!Files.isDirectory(payloadPath) || !pathMatcher.matches(payloadPath)) {

            //inactivate the mailboxes which doesn't have valid directory
            ProcessorConfigurationDAO dao = new ProcessorConfigurationDAOBase();
            configurationInstance.setProcsrStatus(EntityStatus.INACTIVE.name());
            configurationInstance.setModifiedBy(WATCH_DOG_SERVICE);
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
   
    /**
     * Filter file paths for directory sweeper if javascript execution is enabled
     * 
     * @param files
     * @return list of file path
     * @throws IOException 
     * @throws IllegalAccessException 
     */
    @SuppressWarnings("unchecked")
    protected List<Path> filterFiles(List<Path> files) throws IllegalAccessException, IOException {

        String groupingJsPath = configurationInstance.getJavaScriptUri();
        boolean isFilterFileUsingJavaScript = getProperties().isHandOverExecutionToJavaScript();

        if (isFilterFileUsingJavaScript && !MailBoxUtil.isEmpty(groupingJsPath)) {
            return (List<Path>) JavaScriptExecutorUtil.executeJavaScript(groupingJsPath, MailBoxConstants.FILTER, this, files);
        }

        return files;
    }

    @Override
    public boolean isClassicSweeper() {
        return true;
    }

}
