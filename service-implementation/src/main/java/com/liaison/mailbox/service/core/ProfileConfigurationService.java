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
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.jpa.dao.MailBoxConfigurationDAO;
import com.liaison.mailbox.jpa.dao.MailBoxConfigurationDAOBase;
import com.liaison.mailbox.jpa.dao.MailBoxScheduleProfileConfigurationDAO;
import com.liaison.mailbox.jpa.dao.MailBoxScheduleProfileConfigurationDAOBase;
import com.liaison.mailbox.jpa.model.MailBox;
import com.liaison.mailbox.jpa.model.MailBoxSchedProfile;
import com.liaison.mailbox.jpa.model.ScheduleProfilesRef;
import com.liaison.mailbox.service.dto.ResponseDTO;
import com.liaison.mailbox.service.dto.configuration.MailBoxResponseDTO;
import com.liaison.mailbox.service.dto.configuration.ProfileDTO;
import com.liaison.mailbox.service.dto.configuration.request.AddProfileToMailBoxRequestDTO;
import com.liaison.mailbox.service.dto.configuration.response.AddProfileToMailBoxResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.DeactivateMailboxProfileLinkResponseDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.util.MailBoxUtility;

/**
 * Class which has configuration related operations.
 * 
 * @author praveenu
 */

public class ProfileConfigurationService {

	private static final Logger LOG = LoggerFactory.getLogger(ProfileConfigurationService.class);
	private static String PROFILE = "Profile";
	
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
	

		try {

			ProfileDTO profileDTO = request.getProfile(); 
			if(profileDTO == null){
				throw new MailBoxConfigurationServicesException(Messages.INVALID_REQUEST);
			}
			ScheduleProfilesRef profile = new ScheduleProfilesRef();
			// Getting the existing mailbox from given GUID
			MailBoxConfigurationDAO config = new MailBoxConfigurationDAOBase();
			MailBox mailbox = config.find(MailBox.class, mailboxGuid);
			if (mailbox == null) {
				throw new MailBoxConfigurationServicesException(Messages.MBX_DOES_NOT_EXIST,mailboxGuid);
			   }

			// Construct MailBoxSchedProfile entity
			MailBoxSchedProfile mailBoxSchedProfile = new MailBoxSchedProfile();
			mailBoxSchedProfile.setScheduleProfilesRef(profile);
			mailBoxSchedProfile.setMailbox(mailbox);
			request.copyToEntity(mailBoxSchedProfile);

			// Persist the entity
			MailBoxScheduleProfileConfigurationDAO configDao = new MailBoxScheduleProfileConfigurationDAOBase();
			configDao.persist(mailBoxSchedProfile);
			
			// response message construction			
			serviceResponse.setResponse(new ResponseDTO(Messages.CREATED_SUCCESSFULLY,PROFILE,Messages.SUCCESS));			
			serviceResponse.setMailboxProfileLinkGuid(String.valueOf(mailBoxSchedProfile.getPrimaryKey()));
			LOG.info("Exit from add profile to mailbox.");
			return serviceResponse;			
           
		} catch (MailBoxConfigurationServicesException e) {
			
			LOG.error(Messages.CREATE_OPERATION_FAILED.name(), e);			
			serviceResponse.setResponse(new ResponseDTO(Messages.CREATE_OPERATION_FAILED,PROFILE,Messages.FAILURE,e.getMessage()));
			return serviceResponse;
		}

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
		LOG.info("input mailbox id {}",mailboxGuid);
		LOG.info("input linker id {}",linkGuid);
		DeactivateMailboxProfileLinkResponseDTO serviceResponse = new DeactivateMailboxProfileLinkResponseDTO();
		try {

			// Getting the existing mailBoxSchedProfile from given link GUID
			MailBoxScheduleProfileConfigurationDAO configDao = new MailBoxScheduleProfileConfigurationDAOBase();
			MailBoxSchedProfile mailBoxSchedProfile = configDao.find(MailBoxSchedProfile.class, linkGuid);

			if (mailBoxSchedProfile == null) {
				throw new MailBoxConfigurationServicesException(Messages.MBX_PROFILE_LINK_DOES_NOT_EXIST,linkGuid);
			}
			configDao.remove(mailBoxSchedProfile);
			
			// response message construction			
			serviceResponse.setResponse(new ResponseDTO(Messages.DEACTIVATION_SUCCESSFUL,PROFILE,Messages.SUCCESS));			
			serviceResponse.setMailboxProfileLinkGuid(String.valueOf(linkGuid));
			LOG.info("Exit from deactivate profile.");
			return serviceResponse;		
			
            } catch (MailBoxConfigurationServicesException e) {

            	LOG.error(Messages.DEACTIVATION_FAILED.value(), e);			
    			serviceResponse.setResponse(new ResponseDTO(Messages.DEACTIVATION_FAILED,PROFILE,Messages.FAILURE,e.getMessage()));
    			return serviceResponse;

			} 

	}
}
