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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.jpa.dao.MailBoxConfigurationDAO;
import com.liaison.mailbox.jpa.dao.MailBoxConfigurationDAOBase;
import com.liaison.mailbox.jpa.model.MailBox;
import com.liaison.mailbox.service.dto.ResponseDTO;
import com.liaison.mailbox.service.dto.configuration.MailBoxDTO;
import com.liaison.mailbox.service.dto.configuration.request.AddMailboxRequestDTO;
import com.liaison.mailbox.service.dto.configuration.request.ReviseMailboxRequestDTO;
import com.liaison.mailbox.service.dto.configuration.response.AddMailBoxResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.DeActivateMailBoxResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.GetMailBoxResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.ReviseMailBoxResponseDTO;

/**
 * Class which has configuration related operations.
 *
 * @author veerasamyn
 */
public class MailBoxConfigurationService {

	private static final Logger LOG = LoggerFactory.getLogger(MailBoxConfigurationService.class);

	/**
	 * Creates Mail Box.
	 * 
	 * @param request The request DTO.
	 * @return The responseDTO.
	 */
	public AddMailBoxResponseDTO createMailBox(AddMailboxRequestDTO request) {

		LOG.info("Entering into create mailbox.");
		MailBox mailBox = new MailBox();
		request.copyToEntity(mailBox);

		MailBoxConfigurationDAO configDao = new MailBoxConfigurationDAOBase();
		configDao.persist(mailBox);

		//Response construction
		AddMailBoxResponseDTO serviceResponse = new AddMailBoxResponseDTO();

		ResponseDTO response = new ResponseDTO();
		response.setMessage(MailBoxConstants.CREATE_MAILBOX_SUCCESS);
		response.setStatus(MailBoxConstants.SUCCESS);

		MailBoxDTO dto = new MailBoxDTO();
		dto.setGuid(String.valueOf(mailBox.getPrimaryKey()));
		serviceResponse.setMailBox(dto);

		LOG.info("Exit from create mailbox.");
		return serviceResponse;
	}

	/**
	 * Get the mailbox using guid.
	 * 
	 * @param guid The guid of the mailbox.
	 * @return The responseDTO.
	 */
	public GetMailBoxResponseDTO getMailBox(String guid) {

		LOG.info("Entering into get mailbox.");
		LOG.info("The retrieve guid is {} ", guid);

		MailBoxConfigurationDAO config = new MailBoxConfigurationDAOBase();
		MailBox mailBox = config.find(MailBox.class, guid);

		//Response Construction
		GetMailBoxResponseDTO serviceResponse = new GetMailBoxResponseDTO();

		ResponseDTO response = new ResponseDTO();
		
		if (null != mailBox) {

			response.setMessage(MailBoxConstants.GET_MAILBOX_SUCCESS);
			response.setStatus(MailBoxConstants.SUCCESS);

			MailBoxDTO dto = new MailBoxDTO();
			dto.copyFromEntity(mailBox);

			serviceResponse.setMailBox(dto);
			serviceResponse.setResponse(response);

			LOG.info("Exit from get mailbox.");
			return serviceResponse;

		} else {
			
			response.setMessage(MailBoxConstants.GET_MAILBOX_SUCCESS);
			response.setStatus(MailBoxConstants.SUCCESS);

			serviceResponse.setResponse(response);

			LOG.info("Exit from get mailbox.");
			return serviceResponse;
		}
	}

	/**
	 * Method revise the mailbox configurations.
	 * 
	 * @param guid The mailbox pguid.
	 */
	public ReviseMailBoxResponseDTO reviseMailBox(ReviseMailboxRequestDTO request) {
		
		LOG.info("Entering into revise mailbox.");
		LOG.info("The revise request guid is {} ", request.getMailbox().getGuid());

		MailBoxConfigurationDAO config = new MailBoxConfigurationDAOBase();
		MailBox mailBox = config.find(MailBox.class, request.getMailbox().getGuid());
		
		//Removing the child items.
		mailBox.getMailboxProperties().clear();

		request.copyToEntity(mailBox);
		
		//updates the mail box data
		config.merge(mailBox);
		
		//Response Construction
		ReviseMailBoxResponseDTO serviceResponse = new ReviseMailBoxResponseDTO();

		ResponseDTO response = new ResponseDTO();
		response.setMessage(MailBoxConstants.REVISE_MAILBOX_SUCCESS);
		response.setStatus(MailBoxConstants.SUCCESS);

		MailBoxDTO dto = new MailBoxDTO();
		dto.setGuid(String.valueOf(mailBox.getPrimaryKey()));
		serviceResponse.setMailBox(dto);

		LOG.info("Exit from revise mailbox.");
		return serviceResponse;
	}

	/**
	 * Method revise the mailbox configurations.
	 * 
	 * @param guid The mailbox pguid.
	 */
	public DeActivateMailBoxResponseDTO deActivateMailBox(String guid) {
		
		LOG.info("Entering into deactivate mailbox.");
		LOG.info("The deactivate request guid is {} ", guid);

		MailBoxConfigurationDAO config = new MailBoxConfigurationDAOBase();
		int updatedCount = config.inActivateMailBox(guid);

		//Temporarily returns the id alone.
		DeActivateMailBoxResponseDTO serviceResponse = new DeActivateMailBoxResponseDTO();
		ResponseDTO response = new ResponseDTO();

		if (1 != updatedCount) {

			response.setMessage(MailBoxConstants.INACTIVE_MAILBOX_FAILURE);
			response.setStatus(MailBoxConstants.FAILURE);

			serviceResponse.setResponse(response);
			return serviceResponse;
		} else {

			response.setMessage(MailBoxConstants.INACTIVE_MAILBOX_SUCCESS);
			response.setStatus(MailBoxConstants.SUCCESS);

			MailBoxDTO dto = new MailBoxDTO();
			dto.setGuid(guid);

			serviceResponse.setResponse(response);
			serviceResponse.setMailBox(dto);

			LOG.info("Exit from revise mailbox.");
			return serviceResponse;
		}
	}
}
