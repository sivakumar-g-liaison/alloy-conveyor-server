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

import com.liaison.mailbox.enums.MailBoxStatus;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.jpa.dao.MailBoxConfigurationDAO;
import com.liaison.mailbox.jpa.dao.MailBoxConfigurationDAOBase;
import com.liaison.mailbox.jpa.model.MailBox;
import com.liaison.mailbox.jpa.model.MailBoxSchedProfile;
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
 * Class which has mailbox configuration related operations.
 * 
 * @author veerasamyn
 */
public class MailBoxConfigurationService {

	private static final Logger LOG = LoggerFactory.getLogger(MailBoxConfigurationService.class);
	private static final String MAILBOX = "Mailbox";
	private static final String MAILBOX_NAME = "Mailbox Name";
	private static final String MAILBOX_STATUS = "MailBox Status";

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
		try {

			MailBoxDTO mailboxDto = request.getMailBox();
			if (mailboxDto == null) {
				throw new MailBoxConfigurationServicesException(Messages.INVALID_REQUEST);
			}

			if (MailBoxUtility.isEmpty(mailboxDto.getName())) {
				throw new MailBoxConfigurationServicesException(Messages.MANDATORY_FIELD_MISSING, MAILBOX_NAME);
			}

			// status validation
			MailBoxStatus foundStatusType = MailBoxStatus.findByName(mailboxDto.getStatus());
			if (foundStatusType == null) {
				throw new MailBoxConfigurationServicesException(Messages.ENUM_TYPE_DOES_NOT_SUPPORT, MAILBOX_STATUS);
			}

			MailBox mailBox = new MailBox();
			mailboxDto.copyToEntity(mailBox);
			mailBox.setMbxStatus(foundStatusType.value());// Setting the mailbox status
			mailBox.setPguid(MailBoxUtility.getGUID());

			// persisting the mailbox entity
			MailBoxConfigurationDAO configDao = new MailBoxConfigurationDAOBase();
			configDao.persist(mailBox);

			// response message construction
			serviceResponse.setResponse(new ResponseDTO(Messages.CREATED_SUCCESSFULLY, MAILBOX, Messages.SUCCESS));
			serviceResponse.setMailBox(new MailBoxResponseDTO(String.valueOf(mailBox.getPrimaryKey())));
			LOG.info("Exit from create mailbox.");
			return serviceResponse;

		} catch (MailBoxConfigurationServicesException e) {

			LOG.error(Messages.CREATE_OPERATION_FAILED.name(), e);
			serviceResponse.setResponse(new ResponseDTO(Messages.CREATE_OPERATION_FAILED, MAILBOX, Messages.FAILURE, e
					.getMessage()));
			return serviceResponse;

		}

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

		try {

			// Getting mailbox
			MailBoxConfigurationDAO configDao = new MailBoxConfigurationDAOBase();
			MailBox mailBox = configDao.find(MailBox.class, guid);
			if (mailBox == null) {
				throw new MailBoxConfigurationServicesException(Messages.MBX_DOES_NOT_EXIST, guid);
			}

			MailBoxDTO dto = new MailBoxDTO();
			dto.copyFromEntity(mailBox);

			serviceResponse.setMailBox(dto);
			serviceResponse.setResponse(new ResponseDTO(Messages.READ_SUCCESSFUL, MAILBOX, Messages.SUCCESS));
			LOG.info("Exit from get mailbox.");
			return serviceResponse;

		} catch (MailBoxConfigurationServicesException e) {

			LOG.error(Messages.READ_OPERATION_FAILED.name(), e);
			serviceResponse
					.setResponse(new ResponseDTO(Messages.READ_OPERATION_FAILED, MAILBOX, Messages.FAILURE, e.getMessage()));
			return serviceResponse;
		}

	}

	/**
	 * Method revise the mailbox configurations.
	 * 
	 * @param guid
	 *            The mailbox pguid.
	 */
	public ReviseMailBoxResponseDTO reviseMailBox(ReviseMailBoxRequestDTO request, String guid) {

		LOG.info("Entering into revise mailbox.The revise request is for {} ", guid);

		ReviseMailBoxResponseDTO serviceResponse = new ReviseMailBoxResponseDTO();

		try {

			MailBoxDTO mailboxDto = request.getMailBox();
			if (mailboxDto == null) {
				throw new MailBoxConfigurationServicesException(Messages.INVALID_REQUEST);
			}

			if (MailBoxUtility.isEmpty(request.getMailBox().getName())) {
				throw new MailBoxConfigurationServicesException(Messages.MANDATORY_FIELD_MISSING, MAILBOX_NAME);
			}

			// Getting the mailbox.
			MailBoxConfigurationDAO configDao = new MailBoxConfigurationDAOBase();
			MailBox retrievedMailBox = configDao.find(MailBox.class, guid);
			if (retrievedMailBox == null) {
				throw new MailBoxConfigurationServicesException(Messages.GUID_NOT_AVAIL);
			}

			// Removing the child items.
			retrievedMailBox.getMailboxProperties().clear();

			// updates the mail box data
			mailboxDto.copyToEntity(retrievedMailBox);
			if (retrievedMailBox.getMbxStatus() == null) {
				retrievedMailBox.setMbxStatus(MailBoxStatus.ACTIVE.value());
			}
			configDao.merge(retrievedMailBox);

			// response message construction
			serviceResponse.setResponse(new ResponseDTO(Messages.REVISED_SUCCESSFULLY, MAILBOX, Messages.SUCCESS));
			serviceResponse.setMailBox(new MailBoxResponseDTO(String.valueOf(retrievedMailBox.getPrimaryKey())));
			LOG.info("Exit from revise mailbox.");
			return serviceResponse;

		} catch (MailBoxConfigurationServicesException e) {

			LOG.error(Messages.REVISE_OPERATION_FAILED.name(), e);
			serviceResponse.setResponse(new ResponseDTO(Messages.REVISE_OPERATION_FAILED, MAILBOX, Messages.FAILURE, e
					.getMessage()));
			return serviceResponse;
		}

	}

	/**
	 * Method revise the mailbox configurations.
	 * 
	 * @param guid
	 *            The mailbox pguid.
	 */
	public DeActivateMailBoxResponseDTO deactivateMailBox(String guid) {

		LOG.info("Entering into deactivate mailbox.");
		DeActivateMailBoxResponseDTO serviceResponse = new DeActivateMailBoxResponseDTO();
		try {

			LOG.info("The deactivate request is for {} ", guid);

			MailBoxConfigurationDAO configDao = new MailBoxConfigurationDAOBase();
			MailBox retrievedMailBox = configDao.find(MailBox.class, guid);
			if (retrievedMailBox == null) {
				throw new MailBoxConfigurationServicesException(Messages.MBX_DOES_NOT_EXIST, guid);
			}

			// Changing the status in MailBoxSchedProfile if it is avail
			List<MailBoxSchedProfile> retrievedSchedProfiles = retrievedMailBox.getMailboxSchedProfiles();
			if (null != retrievedSchedProfiles) {
				for (MailBoxSchedProfile schedProfile : retrievedSchedProfiles) {
					schedProfile.setMbxProfileStatus(MailBoxStatus.INACTIVE.value());
				}
			}
			// Changing the mailbox status
			retrievedMailBox.setMbxStatus(MailBoxStatus.INACTIVE.value());
			configDao.merge(retrievedMailBox);

			// response message construction
			serviceResponse.setResponse(new ResponseDTO(Messages.DEACTIVATION_SUCCESSFUL, MAILBOX, Messages.SUCCESS));
			serviceResponse.setMailBox(new MailBoxResponseDTO(guid));
			LOG.info("Exit from deactivate mailbox.");
			return serviceResponse;

		} catch (MailBoxConfigurationServicesException e) {

			LOG.error(Messages.DEACTIVATION_FAILED.value(), e);
			serviceResponse.setResponse(new ResponseDTO(Messages.DEACTIVATION_FAILED, MAILBOX, Messages.FAILURE, e.getMessage()));
			return serviceResponse;

		}
	}
}
