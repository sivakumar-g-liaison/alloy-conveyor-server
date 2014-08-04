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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.jpa.dao.ProfileConfigurationDAO;
import com.liaison.mailbox.jpa.dao.ProfileConfigurationDAOBase;
import com.liaison.mailbox.jpa.dao.ProfileOperationDelegate;
import com.liaison.mailbox.jpa.model.ScheduleProfilesRef;
import com.liaison.mailbox.service.dto.ResponseDTO;
import com.liaison.mailbox.service.dto.configuration.ProfileDTO;
import com.liaison.mailbox.service.dto.configuration.request.AddProfileRequestDTO;
import com.liaison.mailbox.service.dto.configuration.request.ReviseProfileRequestDTO;
import com.liaison.mailbox.service.dto.configuration.response.AddProfileResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.ProfileResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.ReviseProfileResponseDTO;
import com.liaison.mailbox.service.dto.ui.GetProfileResponseDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.util.MailBoxUtil;
import com.liaison.mailbox.service.validation.GenericValidator;

/**
 * Class which has configuration related operations.
 * 
 * @author OFS
 */

public class ProfileConfigurationService {

	private static final Logger LOG = LogManager.getLogger(ProfileConfigurationService.class);
	private static final String PROFILE = "Profile";


	/**
	 * Creates Profile.
	 * 
	 * @param request
	 *            The request DTO.
	 * @return The responseDTO.
	 */
	public AddProfileResponseDTO createProfile(AddProfileRequestDTO request) {

		LOG.debug("Entering into profile creation.");
		AddProfileResponseDTO serviceResponse = new AddProfileResponseDTO();

		try {

			ProfileDTO profileDTO = request.getProfile();
			if (profileDTO == null) {
				throw new MailBoxConfigurationServicesException(Messages.INVALID_REQUEST);
			}
			
			GenericValidator validator = new GenericValidator();
			validator.validate(profileDTO);

			ProfileConfigurationDAO configDao = new ProfileConfigurationDAOBase();

			if (configDao.findProfileByName(profileDTO.getName()) != null) {
				throw new MailBoxConfigurationServicesException(Messages.PROFILE_ALREADY_EXISTS);
			}

			ScheduleProfilesRef profile = new ScheduleProfilesRef();
			profileDTO.copyToEntity(profile);
			profile.setPguid(MailBoxUtil.getGUID());

			// persisting the profile entity
			configDao.persist(profile);

			// response message construction
			serviceResponse.setResponse(new ResponseDTO(Messages.CREATED_SUCCESSFULLY, PROFILE, Messages.SUCCESS));
			serviceResponse.setProfile(new ProfileResponseDTO(String.valueOf(profile.getPrimaryKey())));

			LOG.debug("Exiting from profile creation.");

			return serviceResponse;
		} catch (MailBoxConfigurationServicesException e) {

			LOG.error(Messages.CREATE_OPERATION_FAILED.name(), e);
			serviceResponse.setResponse(new ResponseDTO(Messages.CREATE_OPERATION_FAILED, PROFILE, Messages.FAILURE, e
					.getMessage()));

			return serviceResponse;
		}

	}
	
	/**
	 * Updates Profile.
	 * 
	 * @param request
	 *            The request DTO.
	 * @return The responseDTO.
	 */
	public ReviseProfileResponseDTO updateProfile (ReviseProfileRequestDTO request) throws MailBoxConfigurationServicesException {

		LOG.debug("Entering into profile updation.");
		ReviseProfileResponseDTO serviceResponse = new ReviseProfileResponseDTO();

		try {

			ProfileDTO profileDTO = request.getProfile();
			if (profileDTO == null) {
				throw new MailBoxConfigurationServicesException(Messages.INVALID_REQUEST);
			}
			
			GenericValidator validator = new GenericValidator();
			validator.validate(profileDTO);

			ProfileConfigurationDAO configDao = new ProfileConfigurationDAOBase();
			
			ScheduleProfilesRef retreivedProfile = configDao.find(ScheduleProfilesRef.class, profileDTO.getId());
			if(retreivedProfile == null) {
				throw new MailBoxConfigurationServicesException(Messages.GUID_NOT_AVAIL);
			}
			
			if(!(retreivedProfile.getSchProfName().equals(profileDTO.getName()))) {
				
				if (configDao.findProfileByName(profileDTO.getName()) != null) {
					throw new MailBoxConfigurationServicesException(Messages.PROFILE_ALREADY_EXISTS);
				}
			}
				
			retreivedProfile.setSchProfName(profileDTO.getName());
			profileDTO.copyToEntity(retreivedProfile);

			// update the profile entity
			configDao.merge(retreivedProfile);

			// response message construction
			serviceResponse.setResponse(new ResponseDTO(Messages.REVISED_SUCCESSFULLY, PROFILE, Messages.SUCCESS));
			serviceResponse.setProfile(new ProfileResponseDTO(String.valueOf(retreivedProfile.getPrimaryKey())));

			LOG.debug("Exiting from profile updation.");

			return serviceResponse;
		
		} catch (MailBoxConfigurationServicesException e) {

			LOG.error(Messages.REVISE_OPERATION_FAILED.name(), e);
			serviceResponse.setResponse(new ResponseDTO(Messages.REVISE_OPERATION_FAILED, PROFILE, Messages.FAILURE, e
					.getMessage()));

			return serviceResponse;
		}

	}
	
	/**
	 * Retrieves all profiles.
	 * 
	 * @return The GetProfileResponseDTO.
	 */
	public GetProfileResponseDTO getProfiles() {

		LOG.debug("Entering into get all profiles.");
		GetProfileResponseDTO serviceResponse = new GetProfileResponseDTO();

		try {

			ProfileConfigurationDAO configDao = new ProfileConfigurationDAOBase();
			List<ScheduleProfilesRef> profiles = configDao.fetch(new ProfileOperationDelegate());
			List<ProfileDTO> profilesDTO = new ArrayList<ProfileDTO>();
			if (profiles == null || profiles.isEmpty()) {
				serviceResponse.setResponse(new ResponseDTO(Messages.NO_COMPONENT_EXISTS, PROFILE, Messages.SUCCESS));
				serviceResponse.setProfiles(profilesDTO);
				return serviceResponse;
			}
			
			ProfileDTO profile = null;
			for (ScheduleProfilesRef prof : profiles) {
				profile = new ProfileDTO();
				profile.copyFromEntity(prof);
				profilesDTO.add(profile);
			}

			// response message construction
			serviceResponse.setResponse(new ResponseDTO(Messages.READ_SUCCESSFUL, PROFILE, Messages.SUCCESS));
			serviceResponse.setProfiles(profilesDTO);

			LOG.debug("Exiting from get all profiles operation.");

			return serviceResponse;
		} catch (Exception e) {

			LOG.error(Messages.READ_OPERATION_FAILED.name(), e);
			serviceResponse.setResponse(new ResponseDTO(Messages.READ_OPERATION_FAILED, PROFILE, Messages.FAILURE, e
					.getMessage()));

			return serviceResponse;
		}

	}
	
	/**
	 * Retrieves profiles based on the given profile name.
	 * 
	 * @return The GetProfileResponseDTO.
	 */
	
	public GetProfileResponseDTO searchProfiles(String profName) {
		
		LOG.debug("Entering into search profiles by name.");
		GetProfileResponseDTO serviceResponse = new GetProfileResponseDTO();
		
		try {
			ProfileConfigurationDAO configDao = new ProfileConfigurationDAOBase();
			List <ScheduleProfilesRef> profiles = configDao.findProfilesByName(profName);
			
			if (profiles == null || profiles.isEmpty()) {
				throw new MailBoxConfigurationServicesException(Messages.NO_SUCH_COMPONENT_EXISTS, PROFILE);
			}
			List<ProfileDTO> profilesDTO = new ArrayList<ProfileDTO>();
			ProfileDTO profile = null;
			for (ScheduleProfilesRef prof : profiles) {
				profile = new ProfileDTO();
				profile.copyFromEntity(prof);
				profilesDTO.add(profile);
			}

			// response message construction
			serviceResponse.setResponse(new ResponseDTO(Messages.READ_SUCCESSFUL, PROFILE, Messages.SUCCESS));
			serviceResponse.setProfiles(profilesDTO);

			LOG.debug("Exiting from searching profiles operation.");

			return serviceResponse;
			
		}  catch (MailBoxConfigurationServicesException e) {

			LOG.error(Messages.READ_OPERATION_FAILED.name(), e);
			serviceResponse.setResponse(new ResponseDTO(Messages.READ_OPERATION_FAILED, PROFILE, Messages.FAILURE, e
					.getMessage()));

			return serviceResponse;
		}

		
		
	}

}
