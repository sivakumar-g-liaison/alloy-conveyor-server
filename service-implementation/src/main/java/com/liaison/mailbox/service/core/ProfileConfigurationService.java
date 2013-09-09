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
import com.liaison.mailbox.jpa.dao.MailBoxScheduleProfileConfigurationDAO;
import com.liaison.mailbox.jpa.dao.MailBoxScheduleProfileConfigurationDAOBase;
import com.liaison.mailbox.jpa.model.MailBox;
import com.liaison.mailbox.jpa.model.MailBoxSchedProfile;
import com.liaison.mailbox.jpa.model.ScheduleProfilesRef;
import com.liaison.mailbox.service.dto.ResponseDTO;
import com.liaison.mailbox.service.dto.configuration.request.AddProfileToMailBoxRequestDTO;
import com.liaison.mailbox.service.dto.configuration.response.AddProfileToMailBoxResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.DeactivateMailboxProfileLinkResponseDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;

/**
 * Class which has configuration related operations.
 * 
 * @author praveenu
 */

public class ProfileConfigurationService {

	private static final Logger LOG = LoggerFactory.getLogger(ProfileConfigurationService.class);

	/**
	 * Add Profile to MailBox.
	 * 
	 * @param request
	 *            The request DTO & guid.
	 * @return The responseDTO.
	 */
	public AddProfileToMailBoxResponseDTO addProfileToMailBox(AddProfileToMailBoxRequestDTO request, String mailboxGuid) {

		LOG.info("Add Profile to mailbox");
		AddProfileToMailBoxResponseDTO serviceResponse = new AddProfileToMailBoxResponseDTO();
		ResponseDTO response = null;

		try {

			if (mailboxGuid == null || mailboxGuid.equals("")) {
				throw new MailBoxConfigurationServicesException("MailBox cannot be found");
			}
			ScheduleProfilesRef profile = new ScheduleProfilesRef();
			// Getting the existing mailbox from given GUID
			MailBoxConfigurationDAO config = new MailBoxConfigurationDAOBase();
			MailBox mailbox = config.find(MailBox.class, mailboxGuid);
			if (null == mailbox) {
				throw new MailBoxConfigurationServicesException("The given mailbox GUID is not available in the system.");
			}

			// Construct MailBoxSchedProfile entity
			MailBoxSchedProfile mailBoxSchedProfile = new MailBoxSchedProfile();
			mailBoxSchedProfile.setScheduleProfilesRef(profile);
			mailBoxSchedProfile.setMailbox(mailbox);
			request.copyToEntity(mailBoxSchedProfile);

			// Persist the entity
			MailBoxScheduleProfileConfigurationDAO configDao = new MailBoxScheduleProfileConfigurationDAOBase();
			configDao.persist(mailBoxSchedProfile);

			// Constructing response
			response = new ResponseDTO();
			response.setMessage(MailBoxConstants.ADD_PROFILE_SUCCESS);
			response.setStatus(MailBoxConstants.SUCCESS);

			serviceResponse.setResponse(response);
			serviceResponse.setMailboxProfileLinkGuid(String.valueOf(mailBoxSchedProfile.getPrimaryKey()));

			LOG.info("Exit from add profile to mailbox.");

		} catch (Exception e) {

			response = new ResponseDTO();
			response.setMessage(MailBoxConstants.ADD_PROFILE_FAILURE + e.getMessage());
			response.setStatus(MailBoxConstants.FAILURE);

			serviceResponse.setResponse(response);
		}
		return serviceResponse;

	}

	/**
	 * Deactivate Profile from MailBox.
	 * 
	 * @param request
	 *            The request MailboxGuid & MailboxLinkGuid
	 * @return The responseDTO.
	 */
	public DeactivateMailboxProfileLinkResponseDTO deactivateMailboxProfileLink(String mailboxGuid, String linkGuid) {

		LOG.info("Deactivate Profile from mailbox");
		DeactivateMailboxProfileLinkResponseDTO serviceResponse = new DeactivateMailboxProfileLinkResponseDTO();
		ResponseDTO response = null;

		try {

			if (mailboxGuid == null || mailboxGuid.equals("")) {
				throw new MailBoxConfigurationServicesException("MailBox cannot be found");
			} else if (linkGuid == null || linkGuid.equals("")) {
				throw new MailBoxConfigurationServicesException("MailBox cannot be found");
			}
			// Getting the existing mailBoxSchedProfile from given link GUID
			MailBoxScheduleProfileConfigurationDAO configDao = new MailBoxScheduleProfileConfigurationDAOBase();
			MailBoxSchedProfile mailBoxSchedProfile = configDao.find(MailBoxSchedProfile.class, linkGuid);

			if (null == mailBoxSchedProfile) {
				throw new MailBoxConfigurationServicesException("No such MailBox Profile found");
			}
			configDao.remove(mailBoxSchedProfile);

			// Constructing service response

			response = new ResponseDTO();
			response.setMessage(MailBoxConstants.DEACTIVATE_MAIBOX_PROFILE_SUCCESS);
			response.setStatus(MailBoxConstants.SUCCESS);

			serviceResponse.setResponse(response);
			serviceResponse.setMailboxProfileLinkGuid(String.valueOf(linkGuid));

			LOG.info("Exit from deactivate mailbox profile.");

		} catch (Exception e) {

			response = new ResponseDTO();
			response.setMessage(MailBoxConstants.DEACTIVATE_MAIBOX_PROFILE_FAILURE + e.getMessage());
			response.setStatus(MailBoxConstants.FAILURE);

			serviceResponse.setResponse(response);
		}
		return serviceResponse;
	}
}
