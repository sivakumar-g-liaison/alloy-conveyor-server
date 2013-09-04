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
	public AddProfileToMailBoxResponseDTO addProfileToMailBox(AddProfileToMailBoxRequestDTO request, String guid) {

		LOG.info("Add Profile to mailbox");

		ScheduleProfilesRef profile = new ScheduleProfilesRef();

		// Getting the existing mailbox from given GUID
		MailBoxConfigurationDAO config = new MailBoxConfigurationDAOBase();
		MailBox mailbox = config.find(MailBox.class, guid);

		// Construct MailBoxSchedProfile entity
		MailBoxSchedProfile mailBoxSchedProfile = new MailBoxSchedProfile();
		mailBoxSchedProfile.setScheduleProfilesRef(profile);
		mailBoxSchedProfile.setMailbox(mailbox);
		request.copyToEntity(mailBoxSchedProfile);

		// Persist the entity
		MailBoxScheduleProfileConfigurationDAO configDao = new MailBoxScheduleProfileConfigurationDAOBase();
		configDao.persist(mailBoxSchedProfile);

		// Constructing response
		AddProfileToMailBoxResponseDTO serviceResponse = new AddProfileToMailBoxResponseDTO();

		ResponseDTO response = new ResponseDTO();
		response.setMessage(MailBoxConstants.ADD_PROFILE_SUCCESS);
		response.setStatus(MailBoxConstants.SUCCESS);

		serviceResponse.setResponse(response);
		serviceResponse.setMailboxProfileLinkGuid(String.valueOf(mailBoxSchedProfile.getPrimaryKey()));

		LOG.info("Exit from add profile to mailbox.");
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

		// Getting the existing mailBoxSchedProfile from given link GUID
		MailBoxScheduleProfileConfigurationDAO configDao = new MailBoxScheduleProfileConfigurationDAOBase();
		MailBoxSchedProfile mailBoxSchedProfile = configDao.find(MailBoxSchedProfile.class, linkGuid);
		configDao.remove(mailBoxSchedProfile);

		// Constructing service response
		DeactivateMailboxProfileLinkResponseDTO serviceResponse = new DeactivateMailboxProfileLinkResponseDTO();

		ResponseDTO response = new ResponseDTO();
		response.setMessage(MailBoxConstants.DEACTIVATE_MAIBOX_PROFILE_SUCCESS);
		response.setStatus(MailBoxConstants.SUCCESS);

		serviceResponse.setResponse(response);
		serviceResponse.setMailboxProfileLinkGuid(String.valueOf(linkGuid));

		LOG.info("Exit from deactivate mailbox profile.");
		return serviceResponse;
	}
}
