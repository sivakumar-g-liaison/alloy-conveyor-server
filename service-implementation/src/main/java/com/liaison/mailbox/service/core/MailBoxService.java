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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.enums.ErrorMessages;
import com.liaison.mailbox.jpa.dao.ProcessorConfigurationDAO;
import com.liaison.mailbox.jpa.dao.ProcessorConfigurationDAOBase;
import com.liaison.mailbox.jpa.model.Processor;
import com.liaison.mailbox.service.core.processor.MailBoxPrcoessorFactory;
import com.liaison.mailbox.service.core.processor.MailBoxProcessor;
import com.liaison.mailbox.service.dto.ResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.TriggerProfileResponseDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
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
	 * @return
	 */
	public TriggerProfileResponseDTO triggerProfile(String profileName, String mailboxNamePattern) {

		TriggerProfileResponseDTO serviceResponse = new TriggerProfileResponseDTO();
		ResponseDTO response = null;
		List<Processor> processorMatchingProfile;

		try {

			// validates mandatory value.
			if (MailBoxUtility.isEmpty(profileName)) {
				throw new MailBoxConfigurationServicesException(ErrorMessages.MISSING_FIELD.value());
			}
			LOG.info("The given profile name is {}", profileName);

			// finding the matching processors for the given profile
			ProcessorConfigurationDAO processorDAO = new ProcessorConfigurationDAOBase();
			processorMatchingProfile = processorDAO.findByProfileAndMbxNamePattern(profileName, mailboxNamePattern);
			if (processorMatchingProfile == null || processorMatchingProfile.isEmpty()) {
				throw new MailBoxServicesException(ErrorMessages.NO_PROC_CONFIG_PROFILE.value());
			}

			// invoking the Processors
			MailBoxProcessor processorService = null;
			for (Processor processor : processorMatchingProfile) {

				processorService = MailBoxPrcoessorFactory.getInstance(processor);
				if (null != processorService) {
					LOG.info("The running processor is {}", processor.getDiscriminatorValue());
					processorService.invoke();
				} else {
					LOG.info("Could not create instance for the processor type {}", processor.getDiscriminatorValue());
				}
			}

			// Response construction
			response = new ResponseDTO();
			response.setStatus(MailBoxConstants.SUCCESS);
			response.setMessage("Profile " + profileName + " triggered successfully.");
			serviceResponse.setResponse(response);

			return serviceResponse;

		} catch (MailBoxServicesException e) {

			e.printStackTrace();
			response = new ResponseDTO();
			response.setMessage(ErrorMessages.TRG_PROF_FAILURE.value() + profileName + "." + e.getMessage());
			response.setStatus(MailBoxConstants.FAILURE);
			serviceResponse.setResponse(response);
			return serviceResponse;

		} catch (Exception e) {

			e.printStackTrace();
			response = new ResponseDTO();
			response.setMessage(ErrorMessages.TRG_PROF_FAILURE.value() + profileName + "." + e.getMessage());
			response.setStatus(MailBoxConstants.FAILURE);
			serviceResponse.setResponse(response);
			return serviceResponse;
		}
	}
}
