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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

import com.liaison.commons.jaxb.JAXBUtility;
import com.liaison.commons.logging.LogTags;
import com.liaison.commons.message.glass.dom.StatusType;
import com.liaison.dto.queue.WorkTicket;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.dtdm.dao.ProcessorConfigurationDAO;
import com.liaison.mailbox.dtdm.dao.ProcessorConfigurationDAOBase;
import com.liaison.mailbox.dtdm.dao.ProfileConfigurationDAO;
import com.liaison.mailbox.dtdm.dao.ProfileConfigurationDAOBase;
import com.liaison.mailbox.dtdm.model.MailBox;
import com.liaison.mailbox.dtdm.model.Processor;
import com.liaison.mailbox.dtdm.model.RemoteUploader;
import com.liaison.mailbox.dtdm.model.ScheduleProfilesRef;
import com.liaison.mailbox.enums.ExecutionEvents;
import com.liaison.mailbox.enums.ExecutionState;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.enums.SLAVerificationStatus;
import com.liaison.mailbox.rtdm.dao.ProcessorExecutionStateDAO;
import com.liaison.mailbox.rtdm.dao.ProcessorExecutionStateDAOBase;
import com.liaison.mailbox.rtdm.model.ProcessorExecutionState;
import com.liaison.mailbox.service.core.email.EmailNotifier;
import com.liaison.mailbox.service.core.fsm.MailboxFSM;
import com.liaison.mailbox.service.core.fsm.ProcessorStateDTO;
import com.liaison.mailbox.service.core.processor.FileWriter;
import com.liaison.mailbox.service.core.processor.MailBoxProcessorFactory;
import com.liaison.mailbox.service.core.processor.MailBoxProcessorI;
import com.liaison.mailbox.service.core.sla.MailboxWatchDogService;
import com.liaison.mailbox.service.dto.ResponseDTO;
import com.liaison.mailbox.service.dto.configuration.TriggerProcessorRequestDTO;
import com.liaison.mailbox.service.dto.configuration.response.TriggerProfileResponseDTO;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.glass.util.GlassMessage;
import com.liaison.mailbox.service.glass.util.TransactionVisibilityClient;
import com.liaison.mailbox.service.queue.sender.ProcessorSendQueue;
import com.liaison.mailbox.service.util.MailBoxUtil;


/**
 * Class which has mailbox functional related operations.
 *
 * @author veerasamyn
 */
public class MailBoxService implements Runnable {

	private static final Logger LOG = LogManager.getLogger(MailBoxService.class);
	private static final String DEFAULT_FILE_NAME = "NONE";
	private String message;
	private QueueMessageType messageType;
	
	public enum QueueMessageType {
		WORKTICKET,
		TRIGGERPROFILEREQUEST
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
	 * @param profileName The name of the profile to trigger
	 * @param mailboxNamePattern The mailbox name pattern to exclude
	 * @return The trigger profile response DTO
	 */
	public TriggerProfileResponseDTO triggerProfile(String profileName, String mailboxNamePattern, String shardKey) {

		TriggerProfileResponseDTO serviceResponse = new TriggerProfileResponseDTO();
		List<Processor> processorMatchingProfile = new ArrayList<Processor>();
		List<String> nonExecutingProcessorIds = new ArrayList<String>();
		List<Processor> nonExecutingProcessorMatchingProfile = new ArrayList<Processor>();

		try {

			// validates mandatory value.
			if (MailBoxUtil.isEmpty(profileName)) {
				throw new MailBoxServicesException(Messages.MANDATORY_FIELD_MISSING, "Profile Name",
						Response.Status.CONFLICT);
			}
			LOG.debug("The given profile name is {}", profileName);

			// Profile validation
			ProfileConfigurationDAO profileDAO = new ProfileConfigurationDAOBase();
			ScheduleProfilesRef profile = profileDAO.findProfileByName(profileName);
			if (null == profile) {
				throw new MailBoxServicesException(Messages.PROFILE_NAME_DOES_NOT_EXIST, profileName,
						Response.Status.CONFLICT);
			}

			// finding the matching processors for the given profile
			ProcessorConfigurationDAO processorDAO = new ProcessorConfigurationDAOBase();
			processorMatchingProfile = processorDAO.findByProfileAndMbxNamePattern(profileName, mailboxNamePattern,
					shardKey);

			// filter processors which are not running currently
			ProcessorExecutionStateDAO processorExecutionStateDAO = new ProcessorExecutionStateDAOBase();
			nonExecutingProcessorIds = processorExecutionStateDAO.findNonExecutingProcessors();

			for (Processor procsr : processorMatchingProfile) {

				if (nonExecutingProcessorIds.contains(procsr.getPguid())) {
					nonExecutingProcessorMatchingProfile.add(procsr);
				}
			}

			if (nonExecutingProcessorMatchingProfile == null || nonExecutingProcessorMatchingProfile.isEmpty()) {
				throw new MailBoxServicesException(Messages.NO_PROC_CONFIG_PROFILE, Response.Status.CONFLICT);
			}

			List<String> messages = new ArrayList<String>();
			TriggerProcessorRequestDTO request = null;
			String executionId = null;
			String message = null;
			for (Processor processor : nonExecutingProcessorMatchingProfile) {

				executionId = MailBoxUtil.getGUID();
				request = new TriggerProcessorRequestDTO(executionId, processor.getPguid(), profileName);
				message = MailBoxUtil.marshalToJSON(request);
				messages.add(message);
				String slaVerificationStatus = (processor instanceof RemoteUploader) ? SLAVerificationStatus.SLA_NOT_VERIFIED.getCode() : SLAVerificationStatus.SLA_NOT_APPLICABLE.getCode();
				addProcessorToFSMState(executionId, processor, profileName, slaVerificationStatus);


			}

			LOG.debug("ABOUT TO get ProcessorSendQueue Instance {}", (Object) messages.toArray(new String[messages.size()]));
			for (String singleMessage : messages) {
				ProcessorSendQueue.getInstance().sendMessage(singleMessage);
			}

			serviceResponse.setResponse(new ResponseDTO(Messages.PROFILE_TRIGGERED_SUCCESSFULLY, profileName,Messages.SUCCESS));
			return serviceResponse;

		} catch (MailBoxServicesException | IOException e) {

			LOG.error(Messages.TRG_PROF_FAILURE.name(), e);
			serviceResponse.setResponse(new ResponseDTO(Messages.TRG_PROF_FAILURE, profileName, Messages.FAILURE,
					e.getMessage()));
			return serviceResponse;

		} catch (Exception e) {

			LOG.error(Messages.TRG_PROF_FAILURE.name(), e);
			serviceResponse.setResponse(new ResponseDTO(Messages.TRG_PROF_FAILURE, profileName, Messages.FAILURE,
					e.getMessage()));
			return serviceResponse;
		}

	}

	/**
	 * Method to add the initial processor execution statue.
	 *
	 * @param executionId
	 * @param processor
	 * @param profileName
	 */
	private void addProcessorToFSMState(String executionId, Processor processor, String profileName,String slaVerficationStatus) {

		ProcessorStateDTO state = new ProcessorStateDTO();
		state.setValues(executionId, processor, profileName,ExecutionState.QUEUED,slaVerficationStatus);
		MailboxFSM fsm = new MailboxFSM();
		fsm.addState(state);

	}

	/**
	 * The method executes the processor based on given processor id.
	 *
	 * Unique id for processor
	 *
	 * @return The trigger profile response DTO
	 * @throws IOException
	 * @throws JAXBException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 */
	public void executeProcessor(String triggerProfileRequest) {
		
		LOG.debug("Consumed Trigger profile request [" + triggerProfileRequest + "]");
	    String processorId = null;
	    String executionId = null;
		Processor processor = null;
		ProcessorExecutionState processorExecutionState = null;
		TriggerProcessorRequestDTO dto = null;
		MailboxFSM fsm = new MailboxFSM();
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
			ProcessorStateDTO processorQueued = new ProcessorStateDTO();
			processorQueued.setValues(executionId, processor,	dto.getProfileName(), ExecutionState.QUEUED, SLAVerificationStatus.SLA_NOT_APPLICABLE.getCode());
			fsm.addDefaultStateTransitionRules(processorQueued);

			// retrieve the processor execution status from run-time DB
			processorExecutionState = processorExecutionStateDAO.findByProcessorId(processor.getPguid());

			if (null == processorExecutionState) {
				LOG.info("Processor Execution state is not available in run time DB for processor {}",
						processor.getPguid());
				throw new MailBoxServicesException(Messages.INVALID_PROCESSOR_EXECUTION_STATUS,
						Response.Status.CONFLICT);
			}

			if (ExecutionState.PROCESSING.value().equalsIgnoreCase(processorExecutionState.getExecutionStatus())) {

				fsm.handleEvent(fsm.createEvent(ExecutionEvents.SKIP_AS_ALREADY_RUNNING));
				LOG.info("The processor is already in progress , validated via DB." + processor.getPguid());
				return;
			}

			MailBoxProcessorI processorService = MailBoxProcessorFactory.getInstance(processor);

			if (processorService == null) {
				LOG.info("Could not create instance for the processor type {}", processor.getProcessorType());
				fsm.handleEvent(fsm.createEvent(ExecutionEvents.PROCESSOR_EXECUTION_FAILED));
			}

			MailBox mbx = processor.getMailbox();
            LOG.info("CronJob : {} : {} : {} : {} : {} : Handover execution to the processor service",
                    dto.getProfileName(),
                    processor.getProcessorType().name(),
                    processor.getProcsrName(),
                    mbx.getMbxName(),
                    mbx.getPguid());

			LOG.debug("The Processor type is {}", processor.getProcessorType());
			startTime = System.currentTimeMillis();
			processorExecutionState.setExecutionStatus(ExecutionState.PROCESSING.value());
			processorExecutionStateDAO.merge(processorExecutionState);
			fsm.handleEvent(fsm.createEvent(ExecutionEvents.PROCESSOR_EXECUTION_STARTED));
			endTime = System.currentTimeMillis();
			LOG.debug("Calculating elapsed time for changing processor state to PROCESSING");
			MailBoxUtil.calculateElapsedTime(startTime, endTime);

			processorService.runProcessor(dto, fsm);
			startTime = System.currentTimeMillis();
			processorExecutionState.setExecutionStatus(ExecutionState.COMPLETED.value());
			processorExecutionStateDAO.merge(processorExecutionState);
			fsm.handleEvent(fsm.createEvent(ExecutionEvents.PROCESSOR_EXECUTION_COMPLETED));
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

		} catch (MailBoxServicesException e) {

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
		    
			fsm.handleEvent(fsm.createEvent(ExecutionEvents.PROCESSOR_EXECUTION_FAILED));
			if (processorExecutionState != null) {
				processorExecutionState.setExecutionStatus(ExecutionState.FAILED.value());
				processorExecutionStateDAO.merge(processorExecutionState);
			}

			// send email to the configured mail id in case of failure
			String emailSubject = EmailNotifier.constructSubject(processor, false);
			EmailNotifier.sendEmail(processor, emailSubject, e);

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

			fsm.handleEvent(fsm.createEvent(ExecutionEvents.PROCESSOR_EXECUTION_FAILED));
			if (processorExecutionState != null) {
				processorExecutionState.setExecutionStatus(ExecutionState.FAILED.value());
				processorExecutionStateDAO.merge(processorExecutionState);
			}

			// send email to the configured mail id in case of failure
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
	    String processorId = null;
	    String payloadURI = null;
	    String processorType = null;

	    WorkTicket workTicket = null;
	    Processor processor = null;
        ProcessorExecutionState processorExecutionState = null;

        MailboxFSM fsm = new MailboxFSM();
        ProcessorConfigurationDAO processorDAO = new ProcessorConfigurationDAOBase();
        ProcessorExecutionStateDAO processorExecutionStateDAO = new ProcessorExecutionStateDAOBase();

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

            processorId = workTicket.getAdditionalContextItem(MailBoxConstants.KEY_PROCESSOR_ID);

            if (!MailBoxUtil.isEmpty(processorId)) {
                processor = processorDAO.findActiveProcessorById(processorId);
            } else {
                processor = new MailboxWatchDogService().getSpecificProcessorofMailbox(mailboxId);
            }

            
            //Check the processor is null or not
            if (null == processor) {

                StringBuilder errorMessage = new StringBuilder();
                if (!MailBoxUtil.isEmpty(processorId)) {
                    errorMessage.append("Unable to find a processor type of uploader/filewriter")
                        .append(" for the given processor guid ")
                        .append(processorId);
                } else {
                    errorMessage.append("Unable to find a processor type of uploader/filewriter")
                        .append(" for the given mailbox guid ")
                        .append(mailboxId);
                }
                throw new MailBoxServicesException(errorMessage.toString(), Response.Status.NOT_FOUND);
            }

            processorType = processor.getProcessorType().name();
            glassMessage.logProcessingStatus(processor.getProcsrProtocol(), "Consumed workticket from queue", processor.getProcessorType().name(), StatusType.RUNNING);

            // determine SLA status
			String slaVerificationStatus = (processor instanceof RemoteUploader)
					   ? SLAVerificationStatus.SLA_NOT_APPLICABLE.getCode()
					   : SLAVerificationStatus.SLA_NOT_VERIFIED.getCode();

            // Initiate FSM Starts
            // retrieve the processor execution status of corresponding uploader from run-time DB
            processorExecutionState = processorExecutionStateDAO.findByProcessorId(processor.getPguid());
            ProcessorStateDTO processorStaged = new ProcessorStateDTO();
            //In the PROFILE_NAME column of fsm state table,'NONE' will be persisted.
            processorStaged.setValues(MailBoxUtil.getGUID(), processor, DEFAULT_FILE_NAME, ExecutionState.STAGED, slaVerificationStatus);
            fsm.addState(processorStaged);

            processorExecutionState.setExecutionStatus(ExecutionState.STAGED.value());
            processorExecutionStateDAO.merge(processorExecutionState);
            fsm.handleEvent(fsm.createEvent(ExecutionEvents.FILE_STAGED));
            //Initiate FSM Ends
            
            // check if file Name is available in the payloadTicketRequest if so save the file with the
            // provided file Name if not save with processor Name with Timestamp
            String stagedFileName = MailBoxUtil.isEmpty(workTicket.getFileName()) ? (processor.getProcsrName() + System.nanoTime()) : workTicket.getFileName();
            workTicket.setFileName(stagedFileName);
            FileWriter processorService = new FileWriter(processor);
            MailBox mbx = processor.getMailbox();
            LOG.debug("CronJob : NONE : {} : {} : {} : {} : Handover execution to the filewriter service",
            		processorType,
                    processor.getProcsrName(),
                    mbx.getMbxName(),
                    mbx.getPguid());

            processorService.runProcessor(workTicket, fsm);
            glassMessage.setOutSize(workTicket.getPayloadSize());
			glassMessage.setOutboundFileName(stagedFileName);

            //DUPLICATE LENS LOGGING BASED ON FILE_EXISTS
            if (workTicket.getAdditionalContextItem(MailBoxConstants.FILE_EXISTS) == null) {

            	transactionVisibilityClient.logToGlass(glassMessage);
            	glassMessage.logProcessingStatus(StatusType.SUCCESS, "File Staged successfully", MailBoxConstants.FILEWRITER);

            	// send notification for successful file staging
            	String emailSubject = workTicket.getFileName() + "' is available for pick up";
            	String emailBody = "File '" +  workTicket.getFileName() + "' is available for pick up";
            	EmailNotifier.sendEmail(processor, emailSubject, emailBody, true);
            } else {

            	glassMessage.setStatus(ExecutionState.DUPLICATE);
            	transactionVisibilityClient.logToGlass(glassMessage);
            	glassMessage.logProcessingStatus(StatusType.SUCCESS, "File isn't staged because duplicate file exists at the target location", MailBoxConstants.FILEWRITER);
            }

            LOG.debug("CronJob : NONE : {} : {} : {} : {} : Filewriter service execution is completed",
            		processorType,
                    processor.getProcsrName(),
                    mbx.getMbxName(),
                    mbx.getPguid());

        } catch (Exception e) {

        	LOG.error(e);
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
                glassMessage.logProcessingStatus(StatusType.ERROR, "File Stage Failed :" + e.getMessage(), MailBoxConstants.FILEWRITER, ExceptionUtils.getStackTrace(e));
                glassMessage.logFourthCornerTimestamp();
            }
            //GLASS LOGGING ENDS//

            fsm.handleEvent(fsm.createEvent(ExecutionEvents.PROCESSOR_EXECUTION_FAILED));
            if (processorExecutionState != null) {
                processorExecutionState.setExecutionStatus(ExecutionState.FAILED.value());
                processorExecutionStateDAO.merge(processorExecutionState);
            }

            // send email to the configured mail id in case of failure
            EmailNotifier.sendEmail(processor, EmailNotifier.constructSubject(processor, false), e);

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
		} else {
			throw new RuntimeException(String.format("Cannot process Message from Queue %s", message));
		}
	}

}
