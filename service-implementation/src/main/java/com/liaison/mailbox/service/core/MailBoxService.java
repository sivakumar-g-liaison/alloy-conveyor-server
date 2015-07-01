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
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

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
import com.liaison.mailbox.service.core.email.GenericEmailInfoDTO;
import com.liaison.mailbox.service.core.fsm.MailboxFSM;
import com.liaison.mailbox.service.core.fsm.ProcessorStateDTO;
import com.liaison.mailbox.service.core.processor.MailBoxProcessorFactory;
import com.liaison.mailbox.service.core.processor.MailBoxProcessorI;
import com.liaison.mailbox.service.dto.ResponseDTO;
import com.liaison.mailbox.service.dto.configuration.TriggerProcessorRequestDTO;
import com.liaison.mailbox.service.dto.configuration.response.TriggerProfileResponseDTO;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.queue.ProcessorQueue;
import com.liaison.mailbox.service.util.MailBoxUtil;


/**
 * Class which has mailbox functional related operations.
 *
 * @author veerasamyn
 */
public class MailBoxService {

	private static final Logger LOG = LogManager.getLogger(MailBoxService.class);

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
			LOG.info("The given profile name is {}", profileName);

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

			LOG.debug("ABOUT TO get ProcessorQueue Instance {}", (Object)messages.toArray(new String[messages.size()]));
			ProcessorQueue.getInstance().sendMessages(messages.toArray(new String[messages.size()]));

			serviceResponse.setResponse(new ResponseDTO(Messages.PROFILE_TRIGGERED_SUCCESSFULLY, profileName,Messages.SUCCESS));
			return serviceResponse;

		} catch (MailBoxServicesException | JAXBException | IOException e) {

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
	 * The method gets the processor based on given processor id.
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

		Processor processor = null;
		ProcessorExecutionState processorExecutionState = null;
		String processorId = null;
		String executionId = null;
		MailboxFSM fsm = new MailboxFSM();
		ProcessorConfigurationDAO processorDAO = new ProcessorConfigurationDAOBase();
		ProcessorExecutionStateDAO processorExecutionStateDAO = new ProcessorExecutionStateDAOBase();
		try {

			LOG.info("#####################----PROCESSOR EXECUTION BLOCK-AFTER CONSUMING FROM QUEUE---############################################");


			TriggerProcessorRequestDTO dto = MailBoxUtil.unmarshalFromJSON(triggerProfileRequest,
					TriggerProcessorRequestDTO.class);

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

			LOG.info("The given processor id is {}", processorId);
			LOG.info("The triggered profile name is {}", dto.getProfileName());
			LOG.info("The execution id is {}", executionId);

			// determine SLA status
			String slaVerificationStatus = (processor instanceof RemoteUploader)
					   ? SLAVerificationStatus.SLA_NOT_VERIFIED.getCode()
					   : SLAVerificationStatus.SLA_NOT_APPLICABLE.getCode();
			// Initiate FSM
			processor = processorDAO.find(Processor.class, processorId);
			ProcessorStateDTO processorQueued = new ProcessorStateDTO();
			processorQueued.setValues(executionId, processor,	dto.getProfileName(), ExecutionState.QUEUED, slaVerificationStatus);
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

			LOG.info("Verified if {} is already running and it is not", processorId);
			MailBoxProcessorI processorService = MailBoxProcessorFactory.getInstance(processor);

			if (processorService == null) {
				LOG.info("Could not create instance for the processor type {}", processor.getProcessorType());
				fsm.handleEvent(fsm.createEvent(ExecutionEvents.PROCESSOR_EXECUTION_FAILED));
			}

			MailBox mbx = processor.getMailbox();
            LOG.info("The execution started for the prcsr named {} which is linked to profile {}  and it belongs to the mbx {} and mbx pguid is {}",
                    processor.getProcsrName(), dto.getProfileName(), mbx.getMbxName(), mbx.getPguid());
			LOG.info("The Processer type is {}", processor.getProcessorType());
			processorExecutionState.setExecutionStatus(ExecutionState.PROCESSING.value());
			processorExecutionStateDAO.merge(processorExecutionState);
			fsm.handleEvent(fsm.createEvent(ExecutionEvents.PROCESSOR_EXECUTION_STARTED));

			processorService.runProcessor(dto, fsm);
			processorExecutionState.setExecutionStatus(ExecutionState.COMPLETED.value());
			processorExecutionStateDAO.merge(processorExecutionState);
			fsm.handleEvent(fsm.createEvent(ExecutionEvents.PROCESSOR_EXECUTION_COMPLETED));
			LOG.info("The execution completed for the prcsr named {} which is linked to profile {} and it belongs to the mbx {} and mbx pguid is {}",
                    processor.getProcsrName(), dto.getProfileName(), mbx.getMbxName(), mbx.getPguid());
			LOG.info("#################################################################");

		} catch (MailBoxServicesException e) {

			fsm.handleEvent(fsm.createEvent(ExecutionEvents.PROCESSOR_EXECUTION_FAILED));
			if (processorExecutionState != null) {
				processorExecutionState.setExecutionStatus(ExecutionState.FAILED.value());
				processorExecutionStateDAO.merge(processorExecutionState);
			}

			// send email to the configured mail id in case of failure
			String emailSubject = constructSubject(processor);
			List <String> emailAddress = processor.getEmailAddress();
			notifyUser(processor, emailAddress, emailSubject, ExceptionUtils.getStackTrace(e));
			LOG.error(emailSubject, e);

		} catch (Exception e) {


			fsm.handleEvent(fsm.createEvent(ExecutionEvents.PROCESSOR_EXECUTION_FAILED));
			if (processorExecutionState != null) {
				processorExecutionState.setExecutionStatus(ExecutionState.FAILED.value());
				processorExecutionStateDAO.merge(processorExecutionState);
			}

			// send email to the configured mail id in case of failure
			String emailSubject = constructSubject(processor);
			List <String> emailAddress = processor.getEmailAddress();
			notifyUser(processor, emailAddress, emailSubject, ExceptionUtils.getStackTrace(e));
			LOG.error(emailSubject, e);
		}
	}

	/**
	 * Method to construct subject based on details of processor
	 *
	 * @param processor - The processor for which execution fails
	 * @return email Subject as String
	 */
	private String constructSubject(Processor processor) {

		LOG.debug("constructing subject for the mail to be sent for processor execution failure");
		StringBuilder subjectBuilder = new StringBuilder()
											.append("Processor:")
											.append(processor.getProcsrName())
											.append(" execution failed for the mailbox ")
											.append(processor.getMailbox().getMbxName())
											.append("(")
											.append(processor.getMailbox().getPguid())
											.append(")");
		return subjectBuilder.toString();
	}

	/**
	 * Method to construct the Email Helper DTO from given processor details and the failure Reason.
	 *
	 * @param processor - Processor for which the execution failed
	 * @param emailAddress - Address to which email has to be sent
	 * @param emailSubject - subject of the email
	 * @param failureReason - Reason for failure
	 * @return GenericEmailInfoDTO instance
	 */
	private GenericEmailInfoDTO constructEmailHelperDTO(Processor processor, List<String> emailAddress, String emailSubject, String failureReason) {

		LOG.debug("Ready to construct Email Helper DTO for the mail to be sent for processor execution failure");
		GenericEmailInfoDTO emailInfoDTO = new GenericEmailInfoDTO();
		emailInfoDTO.setMailboxId(null != processor ? processor.getMailbox().getPguid() : null);
		emailInfoDTO.setMailboxName(null != processor ? processor.getMailbox().getMbxName() : null);
		emailInfoDTO.setProcessorName(null !=  processor ? processor.getProcsrName() : null);
		emailInfoDTO.setType("HTML");
		emailInfoDTO.setEmailBody(failureReason);
		emailInfoDTO.setToEmailAddrList(emailAddress);
		emailInfoDTO.setSubject(emailSubject);
		LOG.debug("Email Helper DTO Constructed for the mail to be sent for processor execution failure");
		return emailInfoDTO;

	}

	/**
	 * Method to construct the Email Helper DTO from given processor details and the failure Reason.
	 *
	 * @param processor - Processor for which the execution failed
	 * @param failureReason - Reason for failure
	 * @param emailAddress - Address to which email has to be sent
	 * @param emailSubject - subject of the email
	 */
	private void notifyUser (Processor processor, List<String> emailAddress, String emailSubject, String failureReason) {

		GenericEmailInfoDTO emailInfoDTO = constructEmailHelperDTO(processor, emailAddress, emailSubject, failureReason);
		MailBoxUtil.sendEmail(emailInfoDTO);
	}

}
