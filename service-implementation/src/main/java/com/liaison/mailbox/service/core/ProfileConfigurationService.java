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

import com.liaison.mailbox.enums.MailBoxStatus;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.jpa.dao.MailBoxConfigurationDAO;
import com.liaison.mailbox.jpa.dao.MailBoxConfigurationDAOBase;
import com.liaison.mailbox.jpa.dao.MailBoxScheduleProfileConfigurationDAO;
import com.liaison.mailbox.jpa.dao.MailBoxScheduleProfileConfigurationDAOBase;
import com.liaison.mailbox.jpa.dao.ProfileConfigurationDAO;
import com.liaison.mailbox.jpa.dao.ProfileConfigurationDAOBase;
import com.liaison.mailbox.jpa.model.MailBox;
import com.liaison.mailbox.jpa.model.MailBoxSchedProfile;
import com.liaison.mailbox.jpa.model.ScheduleProfilesRef;
import com.liaison.mailbox.service.dto.ResponseDTO;
import com.liaison.mailbox.service.dto.configuration.ProfileDTO;
import com.liaison.mailbox.service.dto.configuration.request.AddProfileRequestDTO;
import com.liaison.mailbox.service.dto.configuration.response.AddProfileResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.AddProfileToMailBoxResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.DeactivateMailboxProfileLinkResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.ProfileResponseDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.util.MailBoxUtility;
import com.liaison.mailbox.service.validation.GenericValidator;

/**
 * Class which has configuration related operations.
 * 
 * @author praveenu
 */

public class ProfileConfigurationService {

	private static final Logger LOG = LoggerFactory.getLogger(ProfileConfigurationService.class);
	private static String PROFILE = "Profile";
	private static String MAILBOX_PROFILE = "Mailbox-Profile Link";

	private final GenericValidator validator = new GenericValidator();

	/**
	 * Creates Profile.
	 * 
	 * @param request
	 *            The request DTO.
	 * @return The responseDTO.
	 */
	public AddProfileResponseDTO createProfile(AddProfileRequestDTO request) {

		LOG.info("Entering into profile creation.");
		AddProfileResponseDTO serviceResponse = new AddProfileResponseDTO();

		try {

			ProfileDTO profileDTO = request.getProfile();
			if (profileDTO == null) {
				throw new MailBoxConfigurationServicesException(Messages.INVALID_REQUEST);
			}

			validator.validate(profileDTO);

			ProfileConfigurationDAO configDao = new ProfileConfigurationDAOBase();

			if (configDao.findProfileByName(profileDTO.getName()) != null) {
				throw new MailBoxConfigurationServicesException(Messages.PROFILE_ALREADY_EXISTS);
			}

			ScheduleProfilesRef profile = new ScheduleProfilesRef();
			profileDTO.copyToEntity(profile);
			profile.setPguid(MailBoxUtility.getGUID());

			// persisting the profile entity
			configDao.persist(profile);

			// response message construction
			serviceResponse.setResponse(new ResponseDTO(Messages.CREATED_SUCCESSFULLY, PROFILE, Messages.SUCCESS));
			serviceResponse.setProfile(new ProfileResponseDTO(String.valueOf(profile.getPrimaryKey())));

			LOG.info("Exiting from profile creation.");

			return serviceResponse;
		} catch (MailBoxConfigurationServicesException e) {

			LOG.error(Messages.CREATE_OPERATION_FAILED.name(), e);
			serviceResponse.setResponse(new ResponseDTO(Messages.CREATE_OPERATION_FAILED, PROFILE, Messages.FAILURE, e
					.getMessage()));

			return serviceResponse;
		}

	}

	/**
	 * Add Profile to MailBox.
	 * 
	 * @param mailBoxGuid
	 *            The id of the mailbox.
	 * @param profileGuid
	 *            The id of the scheduler profile.
	 * @return The responseDTO.
	 */
	public AddProfileToMailBoxResponseDTO addProfileToMailBox(String mailBoxGuid, String profileGuid) {

		LOG.info("Add Profile to mailbox");
		AddProfileToMailBoxResponseDTO serviceResponse = new AddProfileToMailBoxResponseDTO();

		try {

			// Getting the existing mailbox from given GUID
			MailBoxConfigurationDAO mbConfig = new MailBoxConfigurationDAOBase();
			MailBox mailbox = mbConfig.find(MailBox.class, mailBoxGuid);
			if (mailbox == null) {
				throw new MailBoxConfigurationServicesException(Messages.MBX_DOES_NOT_EXIST, mailBoxGuid);
			}

			// Getting the existing profile from given GUID
			ProfileConfigurationDAO proConfig = new ProfileConfigurationDAOBase();
			ScheduleProfilesRef profile = proConfig.find(ScheduleProfilesRef.class, profileGuid);
			if (profile == null) {
				throw new MailBoxConfigurationServicesException(Messages.PROFILE_DOES_NOT_EXIST, profileGuid);
			}

			// Construct MailBoxSchedProfile entity
			MailBoxSchedProfile mailBoxSchedProfile = new MailBoxSchedProfile();
			mailBoxSchedProfile.setPguid(MailBoxUtility.getGUID());
			mailBoxSchedProfile.setMailbox(mailbox);
			mailBoxSchedProfile.setScheduleProfilesRef(profile);
			mailBoxSchedProfile.setMbxProfileStatus(MailBoxStatus.ACTIVE.value());

			// Persist the entity
			MailBoxScheduleProfileConfigurationDAO configDao = new MailBoxScheduleProfileConfigurationDAOBase();
			configDao.persist(mailBoxSchedProfile);

			// response message construction
			serviceResponse.setResponse(new ResponseDTO(Messages.CREATED_SUCCESSFULLY, MAILBOX_PROFILE, Messages.SUCCESS));
			serviceResponse.setMailboxProfileLinkGuid(String.valueOf(mailBoxSchedProfile.getPrimaryKey()));
			LOG.info("Exit from add profile to mailbox.");
			return serviceResponse;

		} catch (MailBoxConfigurationServicesException e) {

			LOG.error(Messages.CREATE_OPERATION_FAILED.name(), e);
			serviceResponse.setResponse(new ResponseDTO(Messages.CREATE_OPERATION_FAILED, MAILBOX_PROFILE, Messages.FAILURE, e
					.getMessage()));
			return serviceResponse;
		}

	}

	/**
	 * Deactivate Profile from MailBox.
	 * 
	 * @param mailboxGuid
	 *            The mailbox id
	 * @param linkGuid
	 *            The mailbox profile link id
	 * @return The responseDTO.
	 */
	public DeactivateMailboxProfileLinkResponseDTO deactivateMailboxProfileLink(String mailboxGuid, String linkGuid) {

		LOG.info("Deactivate Profile from mailbox");
		LOG.info("input mailbox id {}", mailboxGuid);
		LOG.info("input linker id {}", linkGuid);
		DeactivateMailboxProfileLinkResponseDTO serviceResponse = new DeactivateMailboxProfileLinkResponseDTO();
		try {

			// Getting the existing mailBoxSchedProfile from given link GUID
			MailBoxScheduleProfileConfigurationDAO configDao = new MailBoxScheduleProfileConfigurationDAOBase();
			MailBoxSchedProfile mailBoxSchedProfile = configDao.find(MailBoxSchedProfile.class, linkGuid);

			if (mailBoxSchedProfile == null) {
				throw new MailBoxConfigurationServicesException(Messages.MBX_PROFILE_LINK_DOES_NOT_EXIST, linkGuid);
			}

			if (!mailboxGuid.equals(mailBoxSchedProfile.getMailbox().getPrimaryKey())) {
				throw new MailBoxConfigurationServicesException(Messages.MBX_PROFILE_LINK_DOES_NOT_MATCH, linkGuid);
			}

			configDao.remove(mailBoxSchedProfile);

			// response message construction
			serviceResponse.setResponse(new ResponseDTO(Messages.DEACTIVATION_SUCCESSFUL, PROFILE, Messages.SUCCESS));
			serviceResponse.setMailboxProfileLinkGuid(String.valueOf(linkGuid));
			LOG.info("Exit from deactivate profile.");
			return serviceResponse;

		} catch (MailBoxConfigurationServicesException e) {

			LOG.error(Messages.DEACTIVATION_FAILED.value(), e);
			serviceResponse.setResponse(new ResponseDTO(Messages.DEACTIVATION_FAILED, PROFILE, Messages.FAILURE, e.getMessage()));
			return serviceResponse;

		}

	}
}
