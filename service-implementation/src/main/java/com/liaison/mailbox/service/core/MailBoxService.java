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
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.jms.JMSException;
import javax.naming.NamingException;
import javax.xml.bind.JAXBException;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liaison.mailbox.enums.ExecutionEvents;
import com.liaison.mailbox.enums.ExecutionState;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.jpa.dao.ProcessorConfigurationDAO;
import com.liaison.mailbox.jpa.dao.ProcessorConfigurationDAOBase;
import com.liaison.mailbox.jpa.dao.ProfileConfigurationDAO;
import com.liaison.mailbox.jpa.dao.ProfileConfigurationDAOBase;
import com.liaison.mailbox.jpa.model.Processor;
import com.liaison.mailbox.jpa.model.ScheduleProfilesRef;
import com.liaison.mailbox.service.core.fsm.MailboxFSM;
import com.liaison.mailbox.service.core.fsm.ProcessorStateDTO;
import com.liaison.mailbox.service.core.processor.MailBoxProcessor;
import com.liaison.mailbox.service.core.processor.MailBoxProcessorFactory;
import com.liaison.mailbox.service.dto.ConfigureJNDIDTO;
import com.liaison.mailbox.service.dto.ResponseDTO;
import com.liaison.mailbox.service.dto.configuration.TriggerProcessorRequestDTO;
import com.liaison.mailbox.service.dto.configuration.response.TriggerProfileResponseDTO;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.util.HornetQJMSUtil;
import com.liaison.mailbox.service.util.MailBoxUtility;

/**
 * Class which has mailbox functional related operations.
 * 
 * @author veerasamyn
 */
public class MailBoxService {

	private static final Logger LOG = LoggerFactory.getLogger(MailBoxService.class);

	/**
	 * The method gets the list of processors from the given profile,
	 * mailboxNamePattern and invokes the processor.
	 * 
	 * @param profileName
	 *            The name of the profile to trigger
	 * @param mailboxNamePattern
	 *            The mailbox name pattern to exclude
	 * @return The trigger profile response DTO
	 */
	public TriggerProfileResponseDTO triggerProfile(String profileName, String mailboxNamePattern, String shardKey) {

		TriggerProfileResponseDTO serviceResponse = new TriggerProfileResponseDTO();
		List<Processor> processorMatchingProfile = new ArrayList<>();

		try {

			// validates mandatory value.
			if (MailBoxUtility.isEmpty(profileName)) {
				throw new MailBoxServicesException(Messages.MANDATORY_FIELD_MISSING, "Profile Name");
			}
			LOG.info("The given profile name is {}", profileName);

			// Profile validation
			ProfileConfigurationDAO profileDAO = new ProfileConfigurationDAOBase();
			ScheduleProfilesRef profile = profileDAO.findProfileByName(profileName);
			if (null == profile) {
				throw new MailBoxServicesException(Messages.PROFILE_NAME_DOES_NOT_EXIST, profileName);
			}

			// finding the matching processors for the given profile
			ProcessorConfigurationDAO processorDAO = new ProcessorConfigurationDAOBase();
			processorMatchingProfile = processorDAO.findByProfileAndMbxNamePattern(profileName, mailboxNamePattern, shardKey);

			if (processorMatchingProfile == null || processorMatchingProfile.isEmpty()) {
				throw new MailBoxServicesException(Messages.NO_PROC_CONFIG_PROFILE);
			}

			List<String> messages = new ArrayList<String>();
			TriggerProcessorRequestDTO request = null;
			String executionId = null;
			String message = null;
			for (Processor processor : processorMatchingProfile) {

				executionId = MailBoxUtility.getGUID();
				request = new TriggerProcessorRequestDTO(executionId, processor.getPguid(), profileName);
				message = MailBoxUtility.marshalToJSON(request);
				messages.add(message);
				addProcessorToFSMState(executionId, processor, profileName);
			}

			ConfigureJNDIDTO conf = getConfigureJNDIDTO();
			conf.setMessages(messages);
			HornetQJMSUtil.postMessages(conf);
			// response message construction
			serviceResponse.setResponse(new ResponseDTO(Messages.PROFILE_TRIGGERED_SUCCESSFULLY, profileName, Messages.SUCCESS));
			return serviceResponse;

		} catch (MailBoxServicesException | JAXBException | IOException e) {

			LOG.error(Messages.TRG_PROF_FAILURE.name(), e);
			serviceResponse.setResponse(new ResponseDTO(Messages.TRG_PROF_FAILURE, profileName, Messages.FAILURE, e.getMessage()));
			return serviceResponse;

		} catch(Exception e){
			
			LOG.error(Messages.TRG_PROF_FAILURE.name(), e);
			serviceResponse.setResponse(new ResponseDTO(Messages.TRG_PROF_FAILURE, profileName, Messages.FAILURE, e.getMessage()));
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
	private void addProcessorToFSMState(String executionId, Processor processor, String profileName) {

		ProcessorStateDTO state = ProcessorStateDTO.getProcessorStateInstance(executionId, processor, profileName, ExecutionState.QUEUED, null);
		MailboxFSM fsm = new MailboxFSM();
		fsm.addState(state);

	}

	/**
	 * The method gets the processor based on given processor id.
	 * 
	 * @param processorId
	 *            Unique id for processor
	 * @return The trigger profile response DTO
	 * @throws IOException
	 * @throws JAXBException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 */
	public void executeProcessor(String request) {
		
		Processor processor = null;
		String processorId = null;
		String executionId = null;
		MailboxFSM fsm = new MailboxFSM();
		ProcessorConfigurationDAO processorDAO = new ProcessorConfigurationDAOBase();		
		
		try {
			
			LOG.info("#####################----PROCESSOR EXECUTION BLOCK---############################################");
			
			TriggerProcessorRequestDTO dto = MailBoxUtility.unmarshalFromJSON(request, TriggerProcessorRequestDTO.class);

			// validates mandatory value.			
			processorId = dto.getProcessorId();
			if (MailBoxUtility.isEmpty(processorId)) {
				throw new MailBoxServicesException(Messages.MANDATORY_FIELD_MISSING, "Processor Id");
			}
			
			executionId = dto.getExecutionId();
			if (MailBoxUtility.isEmpty(executionId)) {
				throw new MailBoxServicesException(Messages.MANDATORY_FIELD_MISSING, "Execution Id");
			}
			
			LOG.info("The given processor id is {}", processorId);
			LOG.info("The triggered profile name is {}", dto.getProfileName());
			LOG.info("The execution id is {}", executionId);
			
			//Initiate FSM	
			processor = processorDAO.findByProcessorId(processorId);
			ProcessorStateDTO processorQueued = ProcessorStateDTO.getProcessorStateInstance(executionId, processor, dto.getProfileName(),ExecutionState.QUEUED, null);
			fsm.addDefaultStateTransitionRules(processorQueued);
			
				if (ExecutionState.PROCESSING.value().equalsIgnoreCase(processor.getProcsrExecutionStatus())) {
				
					fsm.handleEvent(fsm.createEvent(ExecutionEvents.SKIP_AS_ALREADY_RUNNING));
					LOG.info("The processor is already in progress , validated via DB." + processor.getPguid());
					return;
			    }
			    	
			    LOG.info("Verified if {} is already running and it is not",processorId);
				MailBoxProcessor processorService = MailBoxProcessorFactory.getInstance(processor);
				
				if(processorService == null){
				 LOG.info("Could not create instance for the processor type {}", processor.getProcessorType());
				 fsm.handleEvent(fsm.createEvent(ExecutionEvents.PROCESSOR_EXECUTION_FAILED));
				}
				
			    LOG.info("The Processer type is {}", processor.getProcessorType());
				// To Maintain the running processor ids in a separate table in DB
				ProcessorSemaphore.addToProcessorExecutionList(processor.getPguid());
				processor.setProcsrExecutionStatus(ExecutionState.PROCESSING.value());
				processorDAO.merge(processor);
		        fsm.handleEvent(fsm.createEvent(ExecutionEvents.PROCESSOR_EXECUTION_STARTED));
		        
		        processorService.invoke(executionId,fsm);
		        
		        processor.setProcsrExecutionStatus(ExecutionState.COMPLETED.value());
			    processorDAO.merge(processor);
		        fsm.handleEvent(fsm.createEvent(ExecutionEvents.PROCESSOR_EXECUTION_COMPLETED));

				// Remove the processor from Database
				ProcessorSemaphore.removeExecutedProcessor(processor.getPguid());
			
			LOG.info("#################################################################");
			
		} catch (MailBoxServicesException e){
			
			e.printStackTrace();
			
		}
		catch (Exception e){
			
			fsm.handleEvent(fsm.createEvent(ExecutionEvents.PROCESSOR_EXECUTION_FAILED));
			processor.setProcsrExecutionStatus(ExecutionState.FAILED.value());
			processorDAO.merge(processor);
			sendEmail(processor.getEmailAddress(), processor.getProcsrName() + ":" + e.getMessage(), e, "HTML");
			e.printStackTrace();
		}
		
	}
	
	private ConfigureJNDIDTO getConfigureJNDIDTO()  throws NamingException, JMSException, IOException {
		
		MailBoxUtility.getEnvironmentProperties();
		String providerURL = MailBoxUtility.getEnvironmentProperties().getString("providerurl");
		String queueName = MailBoxUtility.getEnvironmentProperties().getString("mailBoxProcessorQueue");


		ConfigureJNDIDTO jndidto = new ConfigureJNDIDTO();
		jndidto.setInitialContextFactory("org.jnp.interfaces.NamingContextFactory");
		jndidto.setProviderURL(providerURL);
		jndidto.setQueueName(queueName);
		jndidto.setUrlPackagePrefixes("org.jboss.naming");
		return jndidto;
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

	private void sendEmail(List<String> toEmailAddrList, String subject, String emailBody, String type) {

		EmailNotifier notifier = new EmailNotifier();
		notifier.sendEmail(toEmailAddrList, subject, emailBody, type);
	}

	/**
	 * Sent notifications for trigger system failure.
	 * 
	 * @param toEmailAddrList
	 *            The extra receivers. The default receiver will be available in
	 *            the mailbox.
	 * @param subject
	 *            The notification subject
	 * @param exc
	 *            The exception as body content
	 * @param type
	 *            The notification type(TEXT/HTML).
	 */
	private void sendEmail(List<String> toEmailAddrList, String subject, Exception exc, String type) {

		sendEmail(toEmailAddrList, subject, ExceptionUtils.getStackTrace(exc), type);
	}

}
