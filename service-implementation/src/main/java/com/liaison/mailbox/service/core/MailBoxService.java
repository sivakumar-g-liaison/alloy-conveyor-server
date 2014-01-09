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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.jms.JMSException;
import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liaison.framework.util.ServiceUtils;
import com.liaison.mailbox.enums.ExecutionState;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.jpa.dao.ProcessorConfigurationDAO;
import com.liaison.mailbox.jpa.dao.ProcessorConfigurationDAOBase;
import com.liaison.mailbox.jpa.dao.ProfileConfigurationDAO;
import com.liaison.mailbox.jpa.dao.ProfileConfigurationDAOBase;
import com.liaison.mailbox.jpa.model.Processor;
import com.liaison.mailbox.jpa.model.ScheduleProfilesRef;
import com.liaison.mailbox.service.core.processor.MailBoxProcessor;
import com.liaison.mailbox.service.core.processor.MailBoxProcessorFactory;
import com.liaison.mailbox.service.dto.ConfigureJNDIDTO;
import com.liaison.mailbox.service.dto.ResponseDTO;
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
	 * The method gets the list of processors from the given profile, mailboxNamePattern and invokes
	 * the processor.
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
			for (Processor processor : processorMatchingProfile) {
				messages.add(processor.getPguid());
			}

			ConfigureJNDIDTO conf = getConfigureJNDIDTO();
			conf.setMessages(messages);
			HornetQJMSUtil.postMessages(conf);
			// response message construction
			serviceResponse.setResponse(new ResponseDTO(Messages.PROFILE_TRIGGERED_SUCCESSFULLY, profileName, Messages.SUCCESS));
			return serviceResponse;

		} catch (MailBoxServicesException e) {

			LOG.error(Messages.TRG_PROF_FAILURE.name(), e);
			serviceResponse
					.setResponse(new ResponseDTO(Messages.TRG_PROF_FAILURE, profileName, Messages.FAILURE, e.getMessage()));
			return serviceResponse;

		} catch (NamingException e) {
			LOG.error(Messages.TRG_PROF_FAILURE.name(), e);
			serviceResponse
					.setResponse(new ResponseDTO(Messages.TRG_PROF_FAILURE, profileName, Messages.FAILURE, e.getMessage()));
			return serviceResponse;
			
		} catch (JMSException e) {
			
			LOG.error(Messages.TRG_PROF_FAILURE.name(), e);
			serviceResponse
					.setResponse(new ResponseDTO(Messages.TRG_PROF_FAILURE, profileName, Messages.FAILURE, e.getMessage()));
			return serviceResponse;
		} catch (UnsupportedEncodingException e) {
			
			LOG.error(Messages.TRG_PROF_FAILURE.name(), e);
			serviceResponse
					.setResponse(new ResponseDTO(Messages.TRG_PROF_FAILURE, profileName, Messages.FAILURE, e.getMessage()));
			return serviceResponse;
		}  catch (Exception e) {
			
			LOG.error(Messages.TRG_PROF_FAILURE.name(), e);
			serviceResponse
					.setResponse(new ResponseDTO(Messages.TRG_PROF_FAILURE, profileName, Messages.FAILURE, e.getMessage()));
			return serviceResponse;
		}
	}
	
	
	/**
	 * The method gets the processor based on given processor id.
	 * 
	 * @param processorId
	 *            Unique id for processor
	 * @return The trigger profile response DTO
	 */
	public TriggerProfileResponseDTO executeProcessor(String processorId) {

		TriggerProfileResponseDTO serviceResponse = new TriggerProfileResponseDTO();
		Processor processor = null;

		try {

			// validates mandatory value.
			if (MailBoxUtility.isEmpty(processorId)) {
				throw new MailBoxServicesException(Messages.MANDATORY_FIELD_MISSING, "Processor Id");
			}
			LOG.info("The given processor id is {}", processorId);

			// finding the matching processor for the given processor id
			ProcessorConfigurationDAO processorDAO = new ProcessorConfigurationDAOBase();
			processor = processorDAO.findByProcessorId(processorId);

			if (processor == null) {
				throw new MailBoxServicesException(Messages.NO_PROC_CONFIG_PROFILE);
			}

			if (ExecutionState.PROCESSING.value().equalsIgnoreCase(processor.getProcsrExecutionStatus()))  {
				LOG.info("The processor is already in progress , validated via DB."+processor.getPguid());
			} else {
				
				MailBoxProcessor processorService = MailBoxProcessorFactory.getInstance(processor);
				
				// To Maintain the running processor ids in a separate table in DB
				ProcessorSemaphore.addToProcessorExecutionList(processor.getPguid());
				if (processorService != null) {

					LOG.info("The Processer id is {}", processor.getPguid());
					LOG.info("The Processer type is {}", processor.getProcessorType());
					processor.setProcsrExecutionStatus(ExecutionState.PROCESSING.value());
					processorDAO.merge(processor);
					processorService.invoke();

					// Remove the processor from Database
					ProcessorSemaphore.removeExecutedProcessor(processor.getPguid());
				} else {
					LOG.info("Could not create instance for the processor type {}", processor.getProcessorType());
				}
			}
			// response message construction
			serviceResponse.setResponse(new ResponseDTO(Messages.PROCESSOR_EXECUTION_SUCCESSFULLY, processorId, Messages.SUCCESS));
			return serviceResponse;

		} catch (MailBoxServicesException e) {

			LOG.error(Messages.TRG_PROF_FAILURE.name(), e);
			serviceResponse
					.setResponse(new ResponseDTO(Messages.PROCESSOR_EXECUTION_FAILED, processorId, Messages.FAILURE, e.getMessage()));
			return serviceResponse;

		}
	}
	
	private ConfigureJNDIDTO getConfigureJNDIDTO()  throws NamingException, JMSException, IOException {
		
		Properties properties = MailBoxUtility.getEnvironmentProperties();
		String providerURL = properties.getProperty("providerurl");
		String queueName =properties.getProperty("mailBoxProcessorQueue");

		ConfigureJNDIDTO jndidto = new ConfigureJNDIDTO();
		jndidto.setInitialContextFactory("org.jnp.interfaces.NamingContextFactory");
		jndidto.setProviderURL(providerURL);
		jndidto.setQueueName(queueName);
		jndidto.setUrlPackagePrefixes("org.jboss.naming");
		return jndidto;
	}

}
