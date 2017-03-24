/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.core;

import com.liaison.commons.jaxb.JAXBUtility;
import com.liaison.commons.logging.LogTags;
import com.liaison.commons.message.glass.dom.StatusType;
import com.liaison.commons.messagebus.client.exceptions.ClientUnavailableException;
import com.liaison.commons.util.UUIDGen;
import com.liaison.dto.queue.WorkResult;
import com.liaison.dto.queue.WorkTicket;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.dtdm.dao.ProcessorConfigurationDAO;
import com.liaison.mailbox.dtdm.dao.ProcessorConfigurationDAOBase;
import com.liaison.mailbox.dtdm.dao.ProfileConfigurationDAO;
import com.liaison.mailbox.dtdm.dao.ProfileConfigurationDAOBase;
import com.liaison.mailbox.dtdm.model.MailBox;
import com.liaison.mailbox.dtdm.model.Processor;
import com.liaison.mailbox.dtdm.model.ScheduleProfilesRef;
import com.liaison.mailbox.enums.EntityStatus;
import com.liaison.mailbox.enums.ExecutionState;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.enums.ProcessorType;
import com.liaison.mailbox.rtdm.dao.ProcessorExecutionStateDAO;
import com.liaison.mailbox.rtdm.dao.ProcessorExecutionStateDAOBase;
import com.liaison.mailbox.rtdm.dao.RuntimeProcessorsDAOBase;
import com.liaison.mailbox.rtdm.model.ProcessorExecutionState;
import com.liaison.mailbox.service.core.email.EmailNotifier;
import com.liaison.mailbox.service.core.processor.FileWriter;
import com.liaison.mailbox.service.core.processor.MailBoxProcessorFactory;
import com.liaison.mailbox.service.core.processor.MailBoxProcessorI;
import com.liaison.mailbox.service.core.processor.RemoteUploaderI;
import com.liaison.mailbox.service.directory.DirectoryService;
import com.liaison.mailbox.service.dto.ResponseDTO;
import com.liaison.mailbox.service.dto.configuration.TriggerProcessorRequestDTO;
import com.liaison.mailbox.service.dto.configuration.response.TriggerProfileResponseDTO;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.glass.util.GlassMessage;
import com.liaison.mailbox.service.glass.util.TransactionVisibilityClient;
import com.liaison.mailbox.service.queue.sender.MailboxToServiceBrokerWorkResultQueue;
import com.liaison.mailbox.service.queue.sender.ProcessorSendQueue;
import com.liaison.mailbox.service.topic.TopicMessageDTO;
import com.liaison.mailbox.service.util.MailBoxUtil;
import com.liaison.usermanagement.service.dto.DirectoryMessageDTO;
import com.netflix.config.ConfigurationManager;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

import javax.persistence.LockModeType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.liaison.mailbox.MailBoxConstants.FILEWRITER;
import static com.liaison.mailbox.MailBoxConstants.FILE_EXISTS;
import static com.liaison.mailbox.MailBoxConstants.KEY_FILE_PATH;
import static com.liaison.mailbox.MailBoxConstants.KEY_MESSAGE_CONTEXT_URI;
import static com.liaison.mailbox.MailBoxConstants.RESUME;
import static com.liaison.mailbox.service.util.MailBoxUtil.getGUID;


/**
 * Class which has mailbox functional related operations.
 *
 * @author veerasamyn
 */
public class MailBoxService implements Runnable {

	private static final Logger LOG = LogManager.getLogger(MailBoxService.class);
	private static final String DEFAULT_FILE_NAME = "NONE";
    private static final String PROFILE_NAME = "Profile Name";
    private String message;
    private boolean directUpload = false;
	private QueueMessageType messageType;
	
	public enum QueueMessageType {
		WORKTICKET,
		TRIGGERPROFILEREQUEST,
        INTERRUPTTHREAD,
        DIRECTORYOPERATION
	}

	
	public MailBoxService(String message, QueueMessageType messageType) {
		this.message = message;
		this.messageType = messageType;
	}

    public MailBoxService() {
    }

    /**
     * The method gets the list of processors from the given profile, mailboxNamePattern and invokes the processor.
     *
     * @param profileName        The name of the profile to trigger
     * @param mailboxNamePattern The mailbox name pattern to exclude
     * @return The trigger profile response DTO
     */
    public TriggerProfileResponseDTO triggerProfile(String profileName, String mailboxNamePattern, String shardKey) {

        TriggerProfileResponseDTO serviceResponse = new TriggerProfileResponseDTO();
        List<String> processorMatchingProfile = null;
        List<String> nonExecutingProcessorMatchingProfile = null;

        try {

            // validates mandatory value.
            if (MailBoxUtil.isEmpty(profileName)) {
                throw new MailBoxServicesException(Messages.MANDATORY_FIELD_MISSING, PROFILE_NAME, Response.Status.CONFLICT);
            }
            LOG.debug("The given profile name is {}", profileName);

            // Profile validation
            ProfileConfigurationDAO profileDAO = new ProfileConfigurationDAOBase();
            ScheduleProfilesRef profile = profileDAO.findProfileByName(profileName);
            if (null == profile) {
                throw new MailBoxServicesException(Messages.PROFILE_NAME_DOES_NOT_EXIST, profileName, Response.Status.CONFLICT);
            }

            // finding the matching processors for the given profile
            ProcessorConfigurationDAO processorDAO = new ProcessorConfigurationDAOBase();
            processorMatchingProfile = processorDAO.findByProfileAndMbxNamePattern(profileName, mailboxNamePattern, shardKey);
            if (processorMatchingProfile == null || processorMatchingProfile.isEmpty()) {
                throw new MailBoxServicesException(Messages.NO_PROC_CONFIG_PROFILE, Response.Status.CONFLICT);
            }

            //find non running processors
            nonExecutingProcessorMatchingProfile = new RuntimeProcessorsDAOBase().findNonRunningProcessors(processorMatchingProfile);
            if (nonExecutingProcessorMatchingProfile == null || nonExecutingProcessorMatchingProfile.isEmpty()) {
                throw new MailBoxServicesException(Messages.NO_PROC_CONFIG_PROFILE, Response.Status.CONFLICT);
            }

            List<String> messages = new ArrayList<>();
            TriggerProcessorRequestDTO request = null;
            for (String processorId : nonExecutingProcessorMatchingProfile) {
                request = new TriggerProcessorRequestDTO(getGUID(), processorId, profileName);
                messages.add(MailBoxUtil.marshalToJSON(request));
            }

            LOG.debug("ABOUT TO get ProcessorSendQueue Instance {}", (Object) messages.toArray(new String[messages.size()]));
            for (String message : messages) {
                ProcessorSendQueue.getInstance().sendMessage(message);
            }

            serviceResponse.setResponse(new ResponseDTO(Messages.PROFILE_TRIGGERED_SUCCESSFULLY, profileName, Messages.SUCCESS));
            return serviceResponse;

        } catch (Exception e) {

            LOG.error(Messages.TRG_PROF_FAILURE.name(), e);
            serviceResponse.setResponse(new ResponseDTO(Messages.TRG_PROF_FAILURE, profileName, Messages.FAILURE, e.getMessage()));
            return serviceResponse;
        }

    }

	/**
	 * The method executes the processor based on given processor id.
	 *
	 * Unique id for processor
	 *
	 * @return The trigger profile response DTO
	 */
	public void executeProcessor(String triggerProfileRequest) {
		
		LOG.debug("Consumed Trigger profile request [" + triggerProfileRequest + "]");
	    String processorId = null;
	    String executionId = null;
		Processor processor = null;
        Object[] results = null;
		TriggerProcessorRequestDTO dto = null;

		ProcessorConfigurationDAO processorDAO = new ProcessorConfigurationDAOBase();
		ProcessorExecutionStateDAO processorExecutionStateDAO = new ProcessorExecutionStateDAOBase();

		long actualStartTime = System.currentTimeMillis();
		long startTime = 0;
		long endTime = 0;
		try {

			dto = MailBoxUtil.unmarshalFromJSON(triggerProfileRequest, TriggerProcessorRequestDTO.class);

			// validates mandatory value.
			processorId = dto.getProcessorId();
			if (MailBoxUtil.isEmpty(processorId)) {
				throw new MailBoxServicesException(Messages.MANDATORY_FIELD_MISSING, "Processor Id",
						Response.Status.CONFLICT);
			}

			executionId = dto.getExecutionId();
			if (MailBoxUtil.isEmpty(executionId)) {
				throw new MailBoxServicesException(Messages.MANDATORY_FIELD_MISSING, "Execution Id",
						Response.Status.CONFLICT);
			}

			LOG.debug("The given processor id is {}", processorId);
			LOG.debug("The triggered profile name is {}", dto.getProfileName());

			// Initiate FSM
			processor = processorDAO.find(Processor.class, processorId);

			// retrieve the processor execution status from run-time DB
            results = processorExecutionStateDAO.findByProcessorIdAndUpdateStatus(processor.getPguid());
            if (null == results) {
                // Indicates processor is already running or not available in the runtime table
                LOG.warn(MailBoxConstants.PROCESSOR_IS_ALREDAY_RUNNING + processorId);
                return;
            }

			MailBoxProcessorI processorService = MailBoxProcessorFactory.getInstance(processor);
			if (processorService == null) {
				LOG.info("Could not create instance for the processor type {}", processor.getProcessorType());
			}

			MailBox mbx = processor.getMailbox();
            LOG.info("CronJob : {} : {} : {} : {} : {} : Handover execution to the processor service",
                    dto.getProfileName(),
                    processor.getProcessorType().name(),
                    processor.getProcsrName(),
                    mbx.getMbxName(),
                    mbx.getPguid());

			LOG.debug("The Processor type is {}", processor.getProcessorType());
			processorService.runProcessor(dto);
			startTime = System.currentTimeMillis();
            processorExecutionStateDAO.updateProcessorExecutionState(String.valueOf(results[0]), ExecutionState.COMPLETED.name());

			endTime = System.currentTimeMillis();
			LOG.debug("Calculating elapsed time for changing processor state to COMPLETED");
			MailBoxUtil.calculateElapsedTime(startTime, endTime);

			LOG.info("CronJob : {} : {} : {} : {} : {} : Processor service exectuion is completed",
                    dto.getProfileName(),
                    processor.getProcessorType().name(),
                    processor.getProcsrName(),
                    mbx.getMbxName(),
                    mbx.getPguid());
			LOG.debug("Total Time taken is {} ", (actualStartTime - endTime));

		} catch (Exception e) {

		    if (processor == null) {
                LOG.error("Processor execution failed", e);
            } else {
                LOG.error("CronJob : {} : {} : {} : {} : {} : Processor execution failed : {}",
                        dto.getProfileName(),
                        processor.getProcessorType().name(),
                        processor.getProcsrName(),
                        processor.getMailbox().getMbxName(),
                        processor.getMailbox().getPguid(),
                        e.getMessage(), e);
            }

            if (results != null) {
                processorExecutionStateDAO.updateProcessorExecutionState(String.valueOf(results[0]), ExecutionState.FAILED.name());
            }

            //send email to the configured mail id in case of failure
            EmailNotifier.sendEmail(processor, EmailNotifier.constructSubject(processor, false), e);

		}
		LOG.debug("Processor processed Trigger profile request [" + triggerProfileRequest + "]");

	}

	/**
	 * The method writes the payload into local payload location using filewriter or uploaders
	 * This servers watchdog functionality
	 *
	 * @param request
	 */
	public void executeFileWriter(String request) {

		LOG.info("Consumed WORKTICKET [" + request + "]");
	    String mailboxId = null;
	    String payloadURI = null;
	    String processorType = null;
        boolean directUploadEnabled = false;

	    WorkTicket workTicket = null;
	    Processor processor = null;
        FileWriter processorService = null;

        ProcessorConfigurationDAO processorDAO = new ProcessorConfigurationDAOBase();

        TransactionVisibilityClient transactionVisibilityClient = new TransactionVisibilityClient();
        GlassMessage glassMessage = null;

        try {

            workTicket = JAXBUtility.unmarshalFromJSON(request, WorkTicket.class);

            //Fish tag global process id
            ThreadContext.clearMap(); //set new context after clearing
            ThreadContext.put(LogTags.GLOBAL_PROCESS_ID, workTicket.getGlobalProcessId());

            //Glass message begins
            glassMessage = new GlassMessage(workTicket);
            glassMessage.setStatus(ExecutionState.READY);

            // validates mandatory value.
            mailboxId = workTicket.getAdditionalContextItem(MailBoxConstants.KEY_MAILBOX_ID);
            if (MailBoxUtil.isEmpty(mailboxId)) {
                throw new MailBoxServicesException(Messages.MANDATORY_FIELD_MISSING, "Mailbox Id", Response.Status.CONFLICT);
            }

            payloadURI = workTicket.getPayloadURI();
            if (MailBoxUtil.isEmpty(payloadURI)) {
                throw new MailBoxServicesException(Messages.MANDATORY_FIELD_MISSING, "Spectrum URL", Response.Status.CONFLICT);
            }

            LOG.debug("Received mailbox id - {}", mailboxId);
            LOG.debug("Received payloadURI is {}", payloadURI);

            processor = validateAndGetProcessor(mailboxId, workTicket, processorDAO);
            processorType = processor.getProcessorType().name();
            glassMessage.logProcessingStatus(processor.getProcsrProtocol(), "Consumed workticket from queue", processor.getProcessorType().name(), StatusType.RUNNING);

            // check if file Name is available in the payloadTicketRequest if so save the file with the
            // provided file Name if not save with processor Name with Timestamp
            String stagedFileName = MailBoxUtil.isEmpty(workTicket.getFileName()) ? (processor.getProcsrName() + System.nanoTime()) : workTicket.getFileName();
            workTicket.setFileName(stagedFileName);
            processorService = new FileWriter(processor);
            MailBox mbx = processor.getMailbox();
            LOG.debug("CronJob : NONE : {} : {} : {} : {} : Handover execution to the filewriter service",
            		processorType,
                    processor.getProcsrName(),
                    mbx.getMbxName(),
                    mbx.getPguid());

            //sets the direct upload details in workticet and it would be used to set the STAGED_FILE status
/*            if (processor instanceof RemoteUploader) {
                if (processorService.isDirectUploadEnabled()) {
                    workTicket.setAdditionalContext(DIRECT_UPLOAD, directUploadEnabled);
                }
            }*/

            processorService.runProcessor(workTicket);
            glassMessage.setOutSize(workTicket.getPayloadSize());
            glassMessage.setOutboundFileName(stagedFileName);
            if (null != workTicket.getAdditionalContextItem(KEY_FILE_PATH)) {
                glassMessage.setOutAgent(workTicket.getAdditionalContextItem(KEY_FILE_PATH).toString());
            }

            //DUPLICATE LENS LOGGING BASED ON FILE_EXISTS
            if (workTicket.getAdditionalContextItem(FILE_EXISTS) == null) {

            	transactionVisibilityClient.logToGlass(glassMessage);
                glassMessage.logProcessingStatus(StatusType.SUCCESS, "File Staged successfully", FILEWRITER);

            	// send notification for successful file staging
            	String emailSubject = workTicket.getFileName() + " is available for pick up";
            	String emailBody = "File '" +  workTicket.getFileName() + "' is available for pick up";
            	EmailNotifier.sendEmail(processor, emailSubject, emailBody, true);

                //file writer to kick of an uploader
                if (processorService.isDirectUploadEnabled()) {
                    directUpload(processor, workTicket);
                }

                //Sends response back to SB
                Object isResume = workTicket.getAdditionalContextItem(RESUME);
                if ((isResume != null && ((Boolean) isResume))
                        && (processor instanceof com.liaison.mailbox.dtdm.model.FileWriter
                        || directUpload)) {
                    WorkResult result = constructWorkResult(workTicket, null);
                    MailboxToServiceBrokerWorkResultQueue.getInstance().sendMessage(JAXBUtility.marshalToJSON(result));
                }

            } else {

            	glassMessage.setStatus(ExecutionState.DUPLICATE);
            	transactionVisibilityClient.logToGlass(glassMessage);
                glassMessage.logProcessingStatus(StatusType.SUCCESS, "File isn't staged because duplicate file exists at the target location", FILEWRITER);
            }

            LOG.debug("CronJob : NONE : {} : {} : {} : {} : Filewriter service execution is completed",
            		processorType,
                    processor.getProcsrName(),
                    mbx.getMbxName(),
                    mbx.getPguid());

        } catch (Exception e) {

            LOG.error(e.getMessage(), e);
            if (processor == null) {
                LOG.error("File Staging failed", e);
            } else {
                LOG.error("CronJob : NONE : {} : {} : {} : {} : File Staging failed : {}",
                		processorType,
                        processor.getProcsrName(),
                        processor.getMailbox().getMbxName(),
                        processor.getMailbox().getPguid(),
                        e.getMessage(), e);
            }

            //GLASS LOGGING CORNER 4 //
            if (null != glassMessage) {
                glassMessage.setStatus(ExecutionState.FAILED);
                transactionVisibilityClient.logToGlass(glassMessage);
                if (directUpload && null != processorService) {
                    processorService.updateStagedFileStatus(workTicket, EntityStatus.FAILED.name());
                }
                String msg = (directUpload) ? "Direct upload Failed" : "File Stage Failed";
                glassMessage.logProcessingStatus(StatusType.ERROR, msg, FILEWRITER, ExceptionUtils.getStackTrace(e));
            }
            //GLASS LOGGING ENDS//

            // send email to the configured mail id in case of failure
            EmailNotifier.sendEmail(processor, EmailNotifier.constructSubject(processor, false), e);

            //sends the response to SB
            if (null != workTicket) {

                Object isResume = workTicket.getAdditionalContextItem(RESUME);
                if (isResume != null && ((Boolean) isResume)) {
                    WorkResult result = constructWorkResult(workTicket, e);
                    try {
                        MailboxToServiceBrokerWorkResultQueue.getInstance().sendMessage(JAXBUtility.marshalToJSON(result));
                    } catch (IOException | JAXBException | ClientUnavailableException ioe) {
                        LOG.error(ioe.getMessage(), ioe);
                    }
                }

            }

        } finally {
        	//Clearing thread context map
        	ThreadContext.clearMap();
        }

        LOG.info("Processed WORKTICKET [" + request + "]");
	}

	@Override
	public void run() {
		
		if (QueueMessageType.TRIGGERPROFILEREQUEST.equals(this.messageType)) {
			this.executeProcessor(message);
		} else if (QueueMessageType.WORKTICKET.equals(this.messageType)) {
			this.executeFileWriter(message);
		} else if (QueueMessageType.INTERRUPTTHREAD.equals(this.messageType)){
            this.interruptThread(message);
        } else if (QueueMessageType.DIRECTORYOPERATION.equals(this.messageType)) {
            this.directoryOperation(message);
        } else {
			throw new RuntimeException(String.format("Cannot process Message from Queue %s", message));
		}
	}

    /**
     * Validates the incoming processor guid and returns the processor entity
     *
     * @param mailboxId
     * @param workTicket
     * @param processorDAO
     * @return
     */
    private Processor validateAndGetProcessor(String mailboxId, WorkTicket workTicket, ProcessorConfigurationDAO processorDAO) {

        Processor processor = null;
        String processorId = workTicket.getAdditionalContextItem(MailBoxConstants.KEY_PROCESSOR_ID);
        if (!MailBoxUtil.isEmpty(processorId)) {
            processor = processorDAO.findActiveProcessorById(processorId);
        } else {
            processor = getProcessorsForMailbox(mailboxId);
        }

        //Check the processor is null or not
        if (null == processor) {

            StringBuilder errorMessage = new StringBuilder();
			errorMessage.append("Unable to find a processor type of uploader/filewriter");
			if (!MailBoxUtil.isEmpty(processorId)) {
				errorMessage.append(" for the given processor guid ").append(processorId);
			} else {
                errorMessage.append(" for the given mailbox guid ").append(mailboxId);
            }
            throw new MailBoxServicesException(errorMessage.toString(), Response.Status.NOT_FOUND);
        }
        return processor;
    }

    /**
     * Invoke the uploader for the staged file
     *
     * @param processor  processor instance
     * @param workticket workticket
     */
    private void directUpload(Processor processor, WorkTicket workticket) {

        directUpload = true;
        RemoteUploaderI directUploader = MailBoxProcessorFactory.getUploaderInstance(processor);
        String path = workticket.getAdditionalContext().get(KEY_FILE_PATH).toString();
        directUploader.doDirectUpload(workticket.getFileName(), path, workticket.getGlobalProcessId());
    }

    /**
     * Constructs the work result for pause/resume in SB
     *
     * @param workTicket workticket
     * @param e exception
     * @return workResult
     */
    private WorkResult constructWorkResult(WorkTicket workTicket, Exception e) {

        WorkResult workResult = new WorkResult();
        workResult.setPipelineId(workTicket.getPipelineId());
        workResult.setProcessId(workTicket.getGlobalProcessId());
        workResult.setTaskId(workTicket.getTaskId());
        workResult.addHeader(KEY_MESSAGE_CONTEXT_URI, (String) workTicket.getAdditionalContextItem(KEY_MESSAGE_CONTEXT_URI));

        if (null != e) {
            workResult.setStatus(500);
            workResult.setErrorMessage(e.getMessage());
        }

        return workResult;
    }

    /**
     * This method is used to kill running threads for the processors which are stopped.
     *
     * @param topicMessage topic message dto
     */
    public void interruptThread(String topicMessage) {

        try {

            TopicMessageDTO message = JAXBUtility.unmarshalFromJSON(topicMessage, TopicMessageDTO.class);
            if (MailBoxUtil.getNode().equals(message.getNodeInUse())) {
                new ProcessorExecutionConfigurationService().interruptAndUpdateStatus(message);
            }


        } catch (JAXBException | IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * This method is used to create directories for created/updated 
     * user account by user management.
     * 
     * @param queueMessage message from GUM
     */
    public void directoryOperation(String queueMessage) {
        
        try {
            
            DirectoryMessageDTO message = JAXBUtility.unmarshalFromJSON(queueMessage, DirectoryMessageDTO.class);
            new DirectoryService().executeDirectoryOperation(message);
        } catch (JAXBException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Method to get the processor of type RemoteUploader/fileWriter of Mailbox
     * associated with given mailbox
     *
     * @param mailboxId mailbox guid
     * @return Processor
     */
    private Processor getProcessorsForMailbox(String mailboxId) {

        LOG.debug("Retrieving processors of type uploader or filewriter for mailbox {}", mailboxId);

        List<String> processorTypes = new ArrayList<>();
        processorTypes.add(ProcessorType.FILEWRITER.name());
        processorTypes.add(ProcessorType.REMOTEUPLOADER.name());

        // get processor of type remote uploader of given mailbox id
        ProcessorConfigurationDAO processorDAO = new ProcessorConfigurationDAOBase();
        List<Processor> processors = processorDAO.findActiveProcessorsByTypeAndMailbox(mailboxId, processorTypes);

        // always get the first available processor because there
        // will be either one uploader or file writer available for each mailbox
        Processor processor = (null != processors && processors.size() > 0) ? processors.get(0) : null;
        return processor;

    }

}
