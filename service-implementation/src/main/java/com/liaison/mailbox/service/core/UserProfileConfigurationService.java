package com.liaison.mailbox.service.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liaison.commons.security.pkcs7.SymmetricAlgorithmException;
import com.liaison.mailbox.enums.MailBoxStatus;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.jpa.dao.AccountTypeConfigurationDAOBase;
import com.liaison.mailbox.jpa.dao.GatewayTypeConfigurationDAOBase;
import com.liaison.mailbox.jpa.dao.IdpProviderConfigurationDAOBase;
import com.liaison.mailbox.jpa.dao.LanguageConfigurationDAOBase;
import com.liaison.mailbox.jpa.dao.UserAccountConfigurationDAOBase;
import com.liaison.mailbox.jpa.model.Account;
import com.liaison.mailbox.jpa.model.AccountType;
import com.liaison.mailbox.jpa.model.GatewayType;
import com.liaison.mailbox.jpa.model.IdpProfile;
import com.liaison.mailbox.jpa.model.IdpProvider;
import com.liaison.mailbox.jpa.model.Language;
import com.liaison.mailbox.service.dto.ResponseDTO;
import com.liaison.mailbox.service.dto.configuration.AccountDTO;
import com.liaison.mailbox.service.dto.configuration.IdpUserProfileDTO;
import com.liaison.mailbox.service.dto.configuration.request.AddUserProfileAccountRequestDTO;
import com.liaison.mailbox.service.dto.configuration.request.ReviseUserProfileRequestDTO;
import com.liaison.mailbox.service.dto.configuration.response.AddUserProfileAccountResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.DeactivateUserProfileResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.GetUserProfileResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.ReviseUserProfileResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.UserProfileResponseDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.util.MailBoxUtility;

public class UserProfileConfigurationService {

	private static final Logger LOGGER = LoggerFactory.getLogger(UserProfileConfigurationService.class);
	
	
	/**
	 * Creates User Profile Account.
	 * 
	 * @param serviceRequest
	 *            The AddUserProfileAccountRequestDTO
	 * @return The AddUserProfileAccountResponseDTO
	 * @throws IOException
	 * @throws JAXBException
	 * @throws JsonMappingException
	 * @throws JsonGenerationException
	 * @throws SymmetricAlgorithmException
	 */
	public AddUserProfileAccountResponseDTO createUserProfileAccount(AddUserProfileAccountRequestDTO serviceRequest)
			throws JsonGenerationException, JsonMappingException, JAXBException, IOException, SymmetricAlgorithmException {

		LOGGER.info("call receive to insert the user account ::{}", serviceRequest.getIdpProfiles());
		AddUserProfileAccountResponseDTO serviceResponse = new AddUserProfileAccountResponseDTO();

		try {

			AccountDTO accountDTO = serviceRequest.getAccount();
			if (accountDTO == null) {
				throw new MailBoxConfigurationServicesException(Messages.INVALID_REQUEST);
			}
			
			List<IdpUserProfileDTO> ipdProfileDTOs = serviceRequest.getIdpProfiles();
			if(ipdProfileDTOs == null || ipdProfileDTOs.isEmpty()){
				throw new MailBoxConfigurationServicesException(Messages.INVALID_REQUEST);
			}
			
			AccountTypeConfigurationDAOBase accountDAOBase = new AccountTypeConfigurationDAOBase();
			AccountType accountType = accountDAOBase.findByName(accountDTO.getAccountType().getName());			
			
			LanguageConfigurationDAOBase languageDAOBase = new LanguageConfigurationDAOBase();
			Language language = languageDAOBase.findByName(accountDTO.getLanguage().getName());
			
			Account account = new Account();
			account.setLanguage(language);
			account.setAccountType(accountType);
			serviceRequest.getAccount().copyToEntity(account, true);
			
			IdpProfile profile = null;
			List<IdpProfile> profiles = null;
			
			IdpProviderConfigurationDAOBase idpProviderDAOBase = new IdpProviderConfigurationDAOBase();
			GatewayTypeConfigurationDAOBase gatewayDAOBase = new GatewayTypeConfigurationDAOBase();
				
			IdpProvider idpProvider = null;
			GatewayType gatewayTpe = null;
			profiles = new ArrayList<>();
				
			for (IdpUserProfileDTO dto : ipdProfileDTOs) {
					
				idpProvider = idpProviderDAOBase.findByName(dto.getIdpProvider());
				if (null != idpProvider) {
					profile = new IdpProfile();
					profile.setPguid(MailBoxUtility.getGUID());
					profile.setIdpProvider(idpProvider);
					profile.setLoginDomain(dto.getLoginDomain());
						
					gatewayTpe = gatewayDAOBase.findByName(dto.getGatewayType());
					profile.setGatewayType(gatewayTpe);
					profiles.add(profile);
				}
			}
			
			if (null != profiles) {
				account.setIdpProfiles(profiles);
			}
			
			//persist the account.
			UserAccountConfigurationDAOBase configAccountDAO = new UserAccountConfigurationDAOBase();
			configAccountDAO.persist(account);
			
			serviceResponse.setResponse(new ResponseDTO(Messages.CREATED_SUCCESSFULLY, "PROCESSOR", Messages.SUCCESS));
			serviceResponse.setUserResponse(new UserProfileResponseDTO(String.valueOf(account.getPrimaryKey())));
			LOGGER.info("Exit from create User Profile");
			return serviceResponse;

		} catch (MailBoxConfigurationServicesException e) {

			LOGGER.error(Messages.CREATE_OPERATION_FAILED.name(), e);
			serviceResponse.setResponse(new ResponseDTO(Messages.CREATE_OPERATION_FAILED, "PROCESSOR", Messages.FAILURE, e
					.getMessage()));
			return serviceResponse;

		}

	}
	
	/**
	 * Get the user profile using guid.
	 * 
	 * @param guid
	 *            The guid of the mailbox.
	 * @return The responseDTO.
	 * @throws SymmetricAlgorithmException
	 * @throws IOException
	 * @throws JAXBException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 */
	public GetUserProfileResponseDTO getUserProfile(String guid) throws JsonParseException, JsonMappingException, JAXBException,
			IOException, SymmetricAlgorithmException {

		LOGGER.info("Entering into get profile.");
		LOGGER.info("The retrieve guid is {} ", guid);

		GetUserProfileResponseDTO serviceResponse = new GetUserProfileResponseDTO();

		try {

			// Getting mailbox
			UserAccountConfigurationDAOBase configDao = new UserAccountConfigurationDAOBase();
			Account account = configDao.find(Account.class, guid);
			if (account == null) {
				throw new MailBoxConfigurationServicesException(Messages.MBX_DOES_NOT_EXIST, guid);
			}

			AccountDTO dto = new AccountDTO();
			dto.copyFromEntity(account);
			
			List <IdpProfile> profiles = account.getIdpProfiles();
			List<IdpUserProfileDTO> userProfileDto =new ArrayList<>();
			for (IdpProfile profile : profiles){
				
				IdpUserProfileDTO profileDTO = new IdpUserProfileDTO();
				profileDTO.setGuid(profile.getPguid());
				profileDTO.setGatewayType(profile.getGatewayType().getName());
				profileDTO.setLoginDomain(profile.getLoginDomain());
				profileDTO.setIdpProvider(profile.getIdpProvider().getName());
				userProfileDto.add(profileDTO);
			}
			
			serviceResponse.setAccount(dto);
			serviceResponse.setIdpProfiles(userProfileDto);
			serviceResponse.setResponse(new ResponseDTO(Messages.READ_SUCCESSFUL, "PROFILE", Messages.SUCCESS));
			LOGGER.info("Exit from get mailbox.");
			return serviceResponse;

		} catch (MailBoxConfigurationServicesException e) {

			LOGGER.error(Messages.READ_OPERATION_FAILED.name(), e);
			serviceResponse
					.setResponse(new ResponseDTO(Messages.READ_OPERATION_FAILED, "PROFILE", Messages.FAILURE, e.getMessage()));
			return serviceResponse;
		}
	}
	
	/**
	 * Method revise the mailbox configurations.
	 * 
	 * @param guid
	 *            The mailbox pguid.
	 * @throws SymmetricAlgorithmException 
	 * @throws IOException 
	 * @throws JAXBException 
	 * @throws JsonMappingException 
	 * @throws JsonGenerationException 
	 */
	public ReviseUserProfileResponseDTO reviseUserProfile(ReviseUserProfileRequestDTO request, String guid) throws JsonGenerationException, JsonMappingException, JAXBException, IOException, SymmetricAlgorithmException {

		LOGGER.info("Entering into revise mailbox.The revise request is for {} ", guid);

		ReviseUserProfileResponseDTO serviceResponse = new ReviseUserProfileResponseDTO();

		try {

			AccountDTO accountDTO = request.getAccount();
			if (accountDTO == null) {
				throw new MailBoxConfigurationServicesException(Messages.INVALID_REQUEST);
			}

			// Validation
			if (!guid.equals(accountDTO.getGuid())) {
				throw new MailBoxConfigurationServicesException(Messages.GUID_DOES_NOT_MATCH, "Profile");
			}
			
			List<IdpUserProfileDTO> profileDTO = request.getIdpProfiles();
			
			if (profileDTO == null) {
				throw new MailBoxConfigurationServicesException(Messages.INVALID_REQUEST);
			}

			// Getting the mailbox.
			UserAccountConfigurationDAOBase configDao = new UserAccountConfigurationDAOBase();
			Account retrievedAccount = configDao.find(Account.class, guid);
			if (retrievedAccount == null) {
				throw new MailBoxConfigurationServicesException(Messages.GUID_NOT_AVAIL);
			}

			accountDTO.copyToEntity(retrievedAccount, false);
			
			retrievedAccount.getIdpProfiles().clear();
			
			IdpProfile profile = null;
			List<IdpProfile> profiles = null;
			
			IdpProviderConfigurationDAOBase idpProviderDAOBase = new IdpProviderConfigurationDAOBase();
			GatewayTypeConfigurationDAOBase gatewayDAOBase = new GatewayTypeConfigurationDAOBase();
				
			IdpProvider idpProvider = null;
			GatewayType gatewayTpe = null;
			profiles = new ArrayList<>();
				
			for (IdpUserProfileDTO dto : profileDTO) {
					
				idpProvider = idpProviderDAOBase.findByName(dto.getIdpProvider());
				if (null != idpProvider) {
					profile = new IdpProfile();
					profile.setPguid(dto.getGuid());
					profile.setIdpProvider(idpProvider);
					profile.setLoginDomain(dto.getLoginDomain());
						
					gatewayTpe = gatewayDAOBase.findByName(dto.getGatewayType());
					profile.setGatewayType(gatewayTpe);
					profiles.add(profile);
				}
			}
			
			if (null != profiles) {
				retrievedAccount.setIdpProfiles(profiles);
			}
			
			configDao.merge(retrievedAccount);

			// response message construction
			serviceResponse.setResponse(new ResponseDTO(Messages.REVISED_SUCCESSFULLY, "Profile", Messages.SUCCESS));
			serviceResponse.setProfile(new UserProfileResponseDTO(String.valueOf(retrievedAccount.getPrimaryKey())));
			LOGGER.info("Exit from revise profile");
			return serviceResponse;

		} catch (MailBoxConfigurationServicesException e) {

			LOGGER.error(Messages.REVISE_OPERATION_FAILED.name(), e);
			serviceResponse.setResponse(new ResponseDTO(Messages.REVISE_OPERATION_FAILED, "Profile", Messages.FAILURE, e
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
	public DeactivateUserProfileResponseDTO deactivateProfile(String guid) {

		LOGGER.info("Entering into deactivate mailbox.");
		DeactivateUserProfileResponseDTO serviceResponse = new DeactivateUserProfileResponseDTO();
		try {

			LOGGER.info("The deactivate request is for {} ", guid);

			UserAccountConfigurationDAOBase configDao = new UserAccountConfigurationDAOBase();
			Account account = configDao.find(Account.class, guid);
			if (account == null) {
				throw new MailBoxConfigurationServicesException(Messages.MBX_DOES_NOT_EXIST, guid);
			}

			// Changing the mailbox status
			account.setActiveState(MailBoxStatus.INACTIVE.value().toUpperCase());
			configDao.merge(account);

			// response message construction
			serviceResponse.setResponse(new ResponseDTO(Messages.DEACTIVATION_SUCCESSFUL, "Profile", Messages.SUCCESS));
			serviceResponse.setProfile(new UserProfileResponseDTO(String.valueOf(account.getPrimaryKey())));;
			LOGGER.info("Exit from deactivate mailbox.");
			return serviceResponse;

		} catch (MailBoxConfigurationServicesException e) {

			LOGGER.error(Messages.DEACTIVATION_FAILED.value(), e);
			serviceResponse.setResponse(new ResponseDTO(Messages.DEACTIVATION_FAILED, "Profile", Messages.FAILURE, e.getMessage()));
			return serviceResponse;

		}
	}

	
}
