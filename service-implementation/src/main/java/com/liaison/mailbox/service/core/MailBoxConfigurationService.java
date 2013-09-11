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
import com.liaison.mailbox.enums.ErrorCode;
import com.liaison.mailbox.jpa.dao.MailBoxConfigurationDAO;
import com.liaison.mailbox.jpa.dao.MailBoxConfigurationDAOBase;
import com.liaison.mailbox.jpa.model.MailBox;
import com.liaison.mailbox.service.dto.ResponseDTO;
import com.liaison.mailbox.service.dto.configuration.MailBoxDTO;
import com.liaison.mailbox.service.dto.configuration.MailBoxResponseDTO;
import com.liaison.mailbox.service.dto.configuration.request.AddMailboxRequestDTO;
import com.liaison.mailbox.service.dto.configuration.request.ReviseMailBoxRequestDTO;
import com.liaison.mailbox.service.dto.configuration.response.AddMailBoxResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.DeActivateMailBoxResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.GetMailBoxResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.ReviseMailBoxResponseDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.util.MailBoxUtility;

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
	 * @param request
	 *            The request DTO.
	 * @return The responseDTO.
	 */
	public AddMailBoxResponseDTO createMailBox(AddMailboxRequestDTO request) {

		LOG.info("Entering into create mailbox.");

		AddMailBoxResponseDTO serviceResponse = new AddMailBoxResponseDTO();
		ResponseDTO response = null;

		try {

			if (request.getMailBox() == null
					|| MailBoxUtility.isEmpty(request.getMailBox().getName())
					|| MailBoxUtility.isEmpty(request.getMailBox().getStatus())) {
				throw new MailBoxConfigurationServicesException(ErrorCode.MISSING_FIELD.toString());
			}

			MailBox mailBox = new MailBox();
			request.copyToEntity(mailBox);
			mailBox.setPguid(MailBoxUtility.getGUID());

			// persisting the mailbox entity
			MailBoxConfigurationDAO configDao = new MailBoxConfigurationDAOBase();
			configDao.persist(mailBox);

			// response message construction
			response = new ResponseDTO();
			response.setMessage(MailBoxConstants.CREATE_MAILBOX_SUCCESS);
			response.setStatus(MailBoxConstants.SUCCESS);
			serviceResponse.setResponse(response);

			MailBoxResponseDTO dto = new MailBoxResponseDTO();
			dto.setGuid(String.valueOf(mailBox.getPrimaryKey()));
			serviceResponse.setMailBox(dto);

		} catch (Exception e) {

			response = new ResponseDTO();
			response.setMessage(MailBoxConstants.CREATE_MAILBOX_FAILURE + e.getMessage());
			response.setStatus(MailBoxConstants.FAILURE);
			serviceResponse.setResponse(response);
		}

		LOG.info("Exit from create mailbox.");
		return serviceResponse;
	}

	/**
	 * Get the mailbox using guid.
	 * 
	 * @param guid
	 *            The guid of the mailbox.
	 * @return The responseDTO.
	 */
	public GetMailBoxResponseDTO getMailBox(String guid) {

		LOG.info("Entering into get mailbox.");
		LOG.info("The retrieve guid is {} ", guid);

		GetMailBoxResponseDTO serviceResponse = new GetMailBoxResponseDTO();
		ResponseDTO response = null;

		try {

			if (MailBoxUtility.isEmpty(guid)) {
				throw new MailBoxConfigurationServicesException(ErrorCode.MISSING_FIELD.toString());
			}

			// Getting mailbox
			MailBoxConfigurationDAO config = new MailBoxConfigurationDAOBase();
			MailBox mailBox = config.find(MailBox.class, guid);
			if (null == mailBox) {
				throw new MailBoxConfigurationServicesException(ErrorCode.GUID_NOT_AVAIL.toString());
			}

			// Response Construction
			response = new ResponseDTO();
			response.setMessage(MailBoxConstants.GET_MAILBOX_SUCCESS);
			response.setStatus(MailBoxConstants.SUCCESS);

			MailBoxDTO dto = new MailBoxDTO();
			dto.copyFromEntity(mailBox);

			serviceResponse.setMailBox(dto);
			serviceResponse.setResponse(response);

			LOG.info("Exit from get mailbox.");
			return serviceResponse;

		} catch (Exception e) {

			response = new ResponseDTO();
			response.setMessage(MailBoxConstants.GET_MAILBOX_FAILURE + e.getMessage());
			response.setStatus(MailBoxConstants.FAILURE);
			serviceResponse.setResponse(response);
		}

		LOG.info("Exit from get mailbox.");
		return serviceResponse;
	}

	/**
	 * Method revise the mailbox configurations.
	 * 
	 * @param guid
	 *            The mailbox pguid.
	 */
	public ReviseMailBoxResponseDTO reviseMailBox(ReviseMailBoxRequestDTO request, String guid) {

		LOG.info("Entering into revise mailbox.");

		ReviseMailBoxResponseDTO serviceResponse = new ReviseMailBoxResponseDTO();
		ResponseDTO response = null;

		try {

			if (request.getMailBox() == null
					|| MailBoxUtility.isEmpty(request.getMailBox().getName())
					|| MailBoxUtility.isEmpty(request.getMailBox().getStatus())
					|| MailBoxUtility.isEmpty(request.getMailBox().getGuid())) {
				throw new MailBoxConfigurationServicesException(ErrorCode.MISSING_FIELD.toString());
			}

			LOG.info("The revise path guid is {} ", guid);
			LOG.info("The revise request guid is {} ", request.getMailBox().getGuid());
			if (!(request.getMailBox().getGuid().equals(guid))) {
				throw new MailBoxConfigurationServicesException("The given guid doesn't match with request guid.");
			}

			// Getting the mailbox.
			MailBoxConfigurationDAO config = new MailBoxConfigurationDAOBase();
			MailBox mailBox = config.find(MailBox.class, request.getMailBox().getGuid());
			if (null == mailBox) {
				throw new MailBoxConfigurationServicesException(ErrorCode.GUID_NOT_AVAIL.toString());
			}

			// Removing the child items.
			mailBox.getMailboxProperties().clear();

			// updates the mail box data
			request.getMailBox().copyToEntity(mailBox);
			config.merge(mailBox);

			// Response Construction
			response = new ResponseDTO();
			response.setMessage(MailBoxConstants.REVISE_MAILBOX_SUCCESS);
			response.setStatus(MailBoxConstants.SUCCESS);

			MailBoxResponseDTO dto = new MailBoxResponseDTO();
			dto.setGuid(String.valueOf(mailBox.getPrimaryKey()));
			serviceResponse.setMailBox(dto);
			serviceResponse.setResponse(response);

		} catch (Exception e) {

			response = new ResponseDTO();
			response.setMessage(MailBoxConstants.REVISE_MAILBOX_SUCCESS);
			response.setStatus(MailBoxConstants.SUCCESS);
			serviceResponse.setResponse(response);
		}

		LOG.info("Exit from revise mailbox.");
		return serviceResponse;
	}

	/**
	 * Method revise the mailbox configurations.
	 * 
	 * @param guid
	 *            The mailbox pguid.
	 */
	public DeActivateMailBoxResponseDTO deActivateMailBox(String guid) {

		LOG.info("Entering into deactivate mailbox.");
		DeActivateMailBoxResponseDTO serviceResponse = new DeActivateMailBoxResponseDTO();
		ResponseDTO response = null;

		try {

			LOG.info("The deactivate request guid is {} ", guid);
			if (MailBoxUtility.isEmpty(guid)) {
				throw new MailBoxConfigurationServicesException(ErrorCode.MISSING_FIELD.toString());
			}

			MailBoxConfigurationDAO config = new MailBoxConfigurationDAOBase();
			int updatedCount = config.inactiveMailBox(guid);
			if (1 != updatedCount) {
				throw new MailBoxConfigurationServicesException("Failure to update.");
			}

			response = new ResponseDTO();
			response.setMessage(MailBoxConstants.INACTIVE_MAILBOX_SUCCESS);
			response.setStatus(MailBoxConstants.SUCCESS);

			MailBoxResponseDTO dto = new MailBoxResponseDTO();
			dto.setGuid(guid);

			response = new ResponseDTO();
			serviceResponse.setResponse(response);
			serviceResponse.setMailBox(dto);

			LOG.info("Exit from revise mailbox.");
		} catch (Exception e) {

			response.setMessage(MailBoxConstants.INACTIVE_MAILBOX_FAILURE);
			response.setStatus(MailBoxConstants.FAILURE);
			serviceResponse.setResponse(response);

		}
		return serviceResponse;
	}
}
