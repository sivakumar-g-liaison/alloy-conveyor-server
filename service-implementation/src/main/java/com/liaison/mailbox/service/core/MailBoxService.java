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

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liaison.mailbox.enums.ExecutionStatus;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.jpa.dao.ProcessorConfigurationDAO;
import com.liaison.mailbox.jpa.dao.ProcessorConfigurationDAOBase;
import com.liaison.mailbox.jpa.dao.ProfileConfigurationDAO;
import com.liaison.mailbox.jpa.dao.ProfileConfigurationDAOBase;
import com.liaison.mailbox.jpa.model.Processor;
import com.liaison.mailbox.jpa.model.ScheduleProfilesRef;
import com.liaison.mailbox.service.core.processor.MailBoxProcessor;
import com.liaison.mailbox.service.core.processor.MailBoxProcessorFactory;
import com.liaison.mailbox.service.dto.ResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.TriggerProfileResponseDTO;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.util.MailBoxUtility;

/**
 * Class which has mailbox functional related operations.
 * 
 * @author veerasamyn
 */
public class MailBoxService {

	private static final Logger LOG = LoggerFactory.getLogger(MailBoxService.class);

	private static List<Processor> synchronizedProcessors = new ArrayList<>();

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
			validateProcessorExecution(processorMatchingProfile);
			if (processorMatchingProfile.isEmpty()) {
				LOG.info("The processor is already in progress.");
			}

			// invoking the Processors
			MailBoxProcessor processorService = null;
			for (Processor processor : processorMatchingProfile) {

				processorService = MailBoxProcessorFactory.getInstance(processor);
				if (processorService != null) {

					LOG.info("The Processer id is {}", processor.getPguid());
					LOG.info("The Processer type is {}", processor.getProcessorType());
					processor.setProcsrExecutionStatus(ExecutionStatus.RUNNING.value());
					processorDAO.merge(processor);
					processorService.invoke();
					processor.setProcsrExecutionStatus(ExecutionStatus.COMPLETED.value());
					processorDAO.merge(processor);

				} else {
					LOG.info("Could not create instance for the processor type {}", processor.getProcessorType());
				}
			}

			if (processorMatchingProfile != null) {
				removeExecutedProcessor(processorMatchingProfile);
			}

			// response message construction
			serviceResponse.setResponse(new ResponseDTO(Messages.PROFILE_TRIGGERED_SUCCESSFULLY, profileName, Messages.SUCCESS));
			return serviceResponse;

		} catch (MailBoxServicesException e) {

			LOG.error(Messages.TRG_PROF_FAILURE.name(), e);
			serviceResponse
					.setResponse(new ResponseDTO(Messages.TRG_PROF_FAILURE, profileName, Messages.FAILURE, e.getMessage()));
			return serviceResponse;

		}
	}

	private synchronized void validateProcessorExecution(List<Processor> processors) {

		List<Processor> existingProcessors = new ArrayList<>();

		for (Processor prcsr : synchronizedProcessors) {
			for (Processor inputPrcsr : processors) {
				if (prcsr.equals(inputPrcsr)) {
					existingProcessors.add(inputPrcsr);
				}
			}
		}

		if (!existingProcessors.isEmpty()) {
			processors.removeAll(existingProcessors);
		}

		if (!processors.isEmpty()) {
			synchronizedProcessors.addAll(processors);
		}

	}

	private synchronized void removeExecutedProcessor(List<Processor> processors) {
		synchronizedProcessors.removeAll(processors);
	}
}
