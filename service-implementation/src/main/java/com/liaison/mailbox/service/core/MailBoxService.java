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
import com.liaison.mailbox.enums.ErrorCode;
import com.liaison.mailbox.jpa.dao.ProcessorConfigurationDAO;
import com.liaison.mailbox.jpa.dao.ProcessorConfigurationDAOBase;
import com.liaison.mailbox.jpa.model.Processor;
import com.liaison.mailbox.service.core.processor.MailBoxProcessor;
import com.liaison.mailbox.service.dto.ResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.TriggerProfileResponseDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
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
		List<Processor> processors;

		try {

			// validates mandatory value.
			if (MailBoxUtility.isEmpty(profileName)) {
				throw new MailBoxConfigurationServicesException(ErrorCode.MISSING_FIELD.value());
			}

			ProcessorConfigurationDAO processorDAO = new ProcessorConfigurationDAOBase();
			if (MailBoxUtility.isEmpty(mailboxNamePattern)) {
				processors = processorDAO.findByProfile(profileName);
			} else {
				processors = processorDAO.findByProfileAndMbxNamePattern(profileName, mailboxNamePattern);
			}

			if (processors == null || processors.isEmpty()) {
				throw new MailBoxConfigurationServicesException("The given profile does not match with any processors.");
			}

			// invoking the Processors
			MailBoxProcessor serviceProcessor = null;
			for (Processor processor : processors) {

				serviceProcessor = MailBoxUtility.getInstance(processor);
				if (null != serviceProcessor) {
					serviceProcessor.invoke();
				} else {
					LOG.info("Could not create instance for the processor type {}", processor.getDiscriminatorValue());
				}
			}

			// Response construction
			response = new ResponseDTO();
			response.setStatus(MailBoxConstants.SUCCESS);
			response.setMessage("Profile" + profileName + "triggered successfully.");
			serviceResponse.setResponse(response);

			return serviceResponse;

		} catch (Exception e) {

			e.printStackTrace();
			response = new ResponseDTO();
			response.setMessage("Error occured while triggering the profile " + profileName + "." + e.getMessage());
			response.setStatus(MailBoxConstants.FAILURE);
			serviceResponse.setResponse(response);
			return serviceResponse;
		}
	}
}
