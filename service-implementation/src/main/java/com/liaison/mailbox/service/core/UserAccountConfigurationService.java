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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
import com.liaison.mailbox.jpa.dao.UserProfileConfigurationDAOBase;
import com.liaison.mailbox.jpa.model.Account;
import com.liaison.mailbox.jpa.model.AccountType;
import com.liaison.mailbox.jpa.model.GatewayType;
import com.liaison.mailbox.jpa.model.IdpProfile;
import com.liaison.mailbox.jpa.model.IdpProvider;
import com.liaison.mailbox.jpa.model.Language;
import com.liaison.mailbox.service.dto.ResponseDTO;
import com.liaison.mailbox.service.dto.configuration.AccountDTO;
import com.liaison.mailbox.service.dto.configuration.IdpUserProfileDTO;
import com.liaison.mailbox.service.dto.configuration.request.AddUserAccountRequestDTO;
import com.liaison.mailbox.service.dto.configuration.request.ReviseUserAccountRequestDTO;
import com.liaison.mailbox.service.dto.configuration.response.AddUserAccountResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.DeactivateUserAccountResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.GetUserAccountResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.ReviseUserAccountResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.UserAccountResponseDTO;
import com.liaison.mailbox.service.dto.ui.PrepopulateUserAccountResponseDTO;
import com.liaison.mailbox.service.dto.ui.SearchUserAccountDTO;
import com.liaison.mailbox.service.dto.ui.SearchUserAccountResponseDTO;
import com.liaison.mailbox.service.dto.ui.UserAccountDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.util.MailBoxUtility;

/**
 * 
 * 
 * @author praveenu
 */

public class UserAccountConfigurationService {

	private static final Logger LOGGER = LoggerFactory.getLogger(UserAccountConfigurationService.class);
	
	
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
	public AddUserAccountResponseDTO createUserAccount(AddUserAccountRequestDTO serviceRequest)
			throws JsonGenerationException, JsonMappingException, JAXBException, IOException, SymmetricAlgorithmException {

		LOGGER.info("call receive to insert the user account ::{}", serviceRequest.getIdpProfiles());
		AddUserAccountResponseDTO serviceResponse = new AddUserAccountResponseDTO();

		try {

			AccountDTO accountDTO = serviceRequest.getAccount();
			if (accountDTO == null) {
				throw new MailBoxConfigurationServicesException(Messages.INVALID_ACC_REQUEST);
			}
			
			List<IdpUserProfileDTO> ipdProfileDTOs = serviceRequest.getIdpProfiles();
			if(ipdProfileDTOs == null || ipdProfileDTOs.isEmpty()){
				throw new MailBoxConfigurationServicesException(Messages.INVALID_ACC_REQUEST);
			}
			
			AccountTypeConfigurationDAOBase accountDAOBase = new AccountTypeConfigurationDAOBase();
			AccountType accountType = accountDAOBase.findByName(accountDTO.getAccountType().getGuid());			
			
			LanguageConfigurationDAOBase languageDAOBase = new LanguageConfigurationDAOBase();
			Language language = languageDAOBase.findByName(accountDTO.getLanguage().getGuid());
			
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
					
				idpProvider = idpProviderDAOBase.findByName(dto.getIdpProviderGuid());
				if (null != idpProvider) {
					profile = new IdpProfile();
					profile.setPguid(MailBoxUtility.getGUID());
					profile.setIdpProvider(idpProvider);
					profile.setLoginDomain(dto.getLoginDomain());
						
					gatewayTpe = gatewayDAOBase.findByName(dto.getGatewayTypeGuid());
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
			
			serviceResponse.setResponse(new ResponseDTO(Messages.CREATED_SUCCESSFULLY, "USER ACCOUNT", Messages.SUCCESS));
			serviceResponse.setAccount(new UserAccountResponseDTO(String.valueOf(account.getPrimaryKey())));
			LOGGER.info("Exit from create User Account");
			return serviceResponse;

		} catch (MailBoxConfigurationServicesException e) {

			LOGGER.error(Messages.CREATE_OPERATION_FAILED.name(), e);
			serviceResponse.setResponse(new ResponseDTO(Messages.CREATE_OPERATION_FAILED, "USER ACCOUNT", Messages.FAILURE, e
					.getMessage()));
			return serviceResponse;

		}

	}
	
	/**
	 * Get the user profile using guid.
	 * 
	 * @param guid
	 *            The guid of the account.
	 * @return The responseDTO.
	 * @throws SymmetricAlgorithmException
	 * @throws IOException
	 * @throws JAXBException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 */
	public GetUserAccountResponseDTO getUserAccount(String guid) throws JsonParseException, JsonMappingException, JAXBException,
			IOException, SymmetricAlgorithmException {

		LOGGER.info("Entering into get user account.");
		LOGGER.info("The retrieve guid is {} ", guid);

		GetUserAccountResponseDTO serviceResponse = new GetUserAccountResponseDTO();

		try {

			// Getting account
			UserAccountConfigurationDAOBase configDao = new UserAccountConfigurationDAOBase();
			Account account = configDao.find(Account.class, guid);
			if (account == null) {
				throw new MailBoxConfigurationServicesException(Messages.ACC_DOES_NOT_EXIST, guid);
			}

			AccountDTO dto = new AccountDTO();
			dto.copyFromEntity(account);
			
			List <IdpProfile> profiles = account.getIdpProfiles();
			List<IdpUserProfileDTO> userProfileDto =new ArrayList<>();
			for (IdpProfile profile : profiles){
				
				IdpUserProfileDTO profileDTO = new IdpUserProfileDTO();
				profileDTO.setGuid(profile.getPguid());
				profileDTO.setGatewayTypeGuid(profile.getGatewayType().getPguid());
				profileDTO.setLoginDomain(profile.getLoginDomain());
				profileDTO.setIdpProviderGuid(profile.getIdpProvider().getPguid());
				userProfileDto.add(profileDTO);
			}
			
			serviceResponse.setAccount(dto);
			serviceResponse.setIdpProfiles(userProfileDto);
			serviceResponse.setResponse(new ResponseDTO(Messages.READ_SUCCESSFUL, "USER ACCOUNT", Messages.SUCCESS));
			LOGGER.info("Exit from get user account.");
			return serviceResponse;

		} catch (MailBoxConfigurationServicesException e) {

			LOGGER.error(Messages.READ_OPERATION_FAILED.name(), e);
			serviceResponse
					.setResponse(new ResponseDTO(Messages.READ_OPERATION_FAILED, "USER ACCOUNT", Messages.FAILURE, e.getMessage()));
			return serviceResponse;
		}
	}
	
	/**
	 * Method revise the account configurations.
	 * 
	 * @param guid
	 *            The account pguid.
	 * @throws SymmetricAlgorithmException 
	 * @throws IOException 
	 * @throws JAXBException 
	 * @throws JsonMappingException 
	 * @throws JsonGenerationException 
	 */
	public ReviseUserAccountResponseDTO reviseUserAccount(ReviseUserAccountRequestDTO request, String guid) throws JsonGenerationException, JsonMappingException, JAXBException, IOException, SymmetricAlgorithmException {

		LOGGER.info("Entering into revise user account.The revise request is for {} ", guid);

		ReviseUserAccountResponseDTO serviceResponse = new ReviseUserAccountResponseDTO();

		try {

			AccountDTO accountDTO = request.getAccount();
			if (accountDTO == null) {
				throw new MailBoxConfigurationServicesException(Messages.INVALID_ACC_REQUEST);
			}

			// Validation
			if (!guid.equals(accountDTO.getGuid())) {
				throw new MailBoxConfigurationServicesException(Messages.GUID_DOES_NOT_MATCH, "USER ACCOUNT");
			}
			
			List<IdpUserProfileDTO> profileDTO = request.getIdpProfiles();
			
			if (profileDTO == null) {
				throw new MailBoxConfigurationServicesException(Messages.INVALID_ACC_REQUEST);
			}

			// Getting the account.
			UserAccountConfigurationDAOBase configDao = new UserAccountConfigurationDAOBase();
			Account retrievedAccount = configDao.find(Account.class, guid);
			if (retrievedAccount == null) {
				throw new MailBoxConfigurationServicesException(Messages.GUID_NOT_AVAIL);
			}

			accountDTO.copyToEntity(retrievedAccount, false);
			
			AccountTypeConfigurationDAOBase accountDAOBase = new AccountTypeConfigurationDAOBase();
			AccountType accountType = accountDAOBase.findByName(accountDTO.getAccountType().getGuid());			
			
			LanguageConfigurationDAOBase languageDAOBase = new LanguageConfigurationDAOBase();
			Language language = languageDAOBase.findByName(accountDTO.getLanguage().getGuid());
			
			retrievedAccount.setAccountType(accountType);
			retrievedAccount.setLanguage(language);
			
			IdpProfile profile = null;
			List<IdpProfile> profiles = null;
			
			IdpProviderConfigurationDAOBase idpProviderDAOBase = new IdpProviderConfigurationDAOBase();
			GatewayTypeConfigurationDAOBase gatewayDAOBase = new GatewayTypeConfigurationDAOBase();
				
			IdpProvider idpProvider = null;
			GatewayType gatewayTpe = null;
			profiles = retrievedAccount.getIdpProfiles();
			
			if(profiles != null){
				profiles.clear();
			}
				
			for (IdpUserProfileDTO dto : profileDTO) {
					
				idpProvider = idpProviderDAOBase.findByName(dto.getIdpProviderGuid());
				if (null != idpProvider) {
					UserProfileConfigurationDAOBase configProfileDao = new UserProfileConfigurationDAOBase();
					profile = configProfileDao.find(IdpProfile.class, dto.getGuid());
					profile.setIdpProvider(idpProvider);
					profile.setLoginDomain(dto.getLoginDomain());
						
					gatewayTpe = gatewayDAOBase.findByName(dto.getGatewayTypeGuid());
					profile.setGatewayType(gatewayTpe);
					profiles.add(profile);
				}
			}
			
			if (null != profiles) {
				retrievedAccount.setIdpProfiles(profiles);
			}
			
			configDao.merge(retrievedAccount);

			// response message construction
			serviceResponse.setResponse(new ResponseDTO(Messages.REVISED_SUCCESSFULLY, "USER ACCOUNT", Messages.SUCCESS));
			serviceResponse.setAccount(new UserAccountResponseDTO(String.valueOf(retrievedAccount.getPrimaryKey())));
			LOGGER.info("Exit from revise user account");
			return serviceResponse;

		} catch (MailBoxConfigurationServicesException e) {

			LOGGER.error(Messages.REVISE_OPERATION_FAILED.name(), e);
			serviceResponse.setResponse(new ResponseDTO(Messages.REVISE_OPERATION_FAILED, "USER ACCOUNT", Messages.FAILURE, e
					.getMessage()));
			return serviceResponse;
		}

	}
	
	/**
	 * Method deactivate the account configurations.
	 * 
	 * @param guid
	 *            The account pguid.
	 */
	public DeactivateUserAccountResponseDTO deactivateUserAccount(String guid) {

		LOGGER.info("Entering into deactivate user account.");
		DeactivateUserAccountResponseDTO serviceResponse = new DeactivateUserAccountResponseDTO();
		try {

			LOGGER.info("The deactivate request is for {} ", guid);

			UserAccountConfigurationDAOBase configDao = new UserAccountConfigurationDAOBase();
			Account account = configDao.find(Account.class, guid);
			if (account == null) {
				throw new MailBoxConfigurationServicesException(Messages.ACC_DOES_NOT_EXIST, guid);
			}

			// Changing the account status
			account.setActiveState(MailBoxStatus.INACTIVE.value().toUpperCase());
			configDao.merge(account);

			// response message construction
			serviceResponse.setResponse(new ResponseDTO(Messages.DEACTIVATION_SUCCESSFUL, "USER ACCOUNT", Messages.SUCCESS));
			serviceResponse.setAccount(new UserAccountResponseDTO(String.valueOf(account.getPrimaryKey())));;
			LOGGER.info("Exit from deactivate user account.");
			return serviceResponse;

		} catch (MailBoxConfigurationServicesException e) {

			LOGGER.error(Messages.DEACTIVATION_FAILED.value(), e);
			serviceResponse.setResponse(new ResponseDTO(Messages.DEACTIVATION_FAILED, "USER ACCOUNT", Messages.FAILURE, e.getMessage()));
			return serviceResponse;

		}
	}
	
	public SearchUserAccountResponseDTO searchUserAccount(String accType, String provName, String loginId) {

		LOGGER.info("Entering into search user account.");

		SearchUserAccountResponseDTO serviceResponse = new SearchUserAccountResponseDTO();

		try {

			// Getting user account
			UserAccountConfigurationDAOBase configDao = new UserAccountConfigurationDAOBase();
			//Set<Account> retrievedMailBoxes = configDao.find(accType,null,null);
			Set<Account> retrievedMailBoxes = configDao.findAllAcc();

			if (null == retrievedMailBoxes || retrievedMailBoxes.isEmpty()) {
				throw new MailBoxConfigurationServicesException(Messages.NO_COMPONENT_EXISTS, "USER ACCOUNT");
			}

			// Constructing the SearchUserAccountDTO from retrieved accounts
			List<SearchUserAccountDTO> searchMailBoxDTOList = new ArrayList<SearchUserAccountDTO>();
			SearchUserAccountDTO serachMailBoxDTO = null;
			for (Account acc : retrievedMailBoxes) {

				serachMailBoxDTO = new SearchUserAccountDTO();
				serachMailBoxDTO.setGuid(acc.getPguid());
				serachMailBoxDTO.setLoginId(acc.getLoginId());
				serachMailBoxDTO.setAccountType(acc.getAccountType().getName());
				
				List<UserAccountDTO> idpProvider = new ArrayList<>();
				UserAccountDTO userAccount = null;
				for(IdpProfile profile : acc.getIdpProfiles()){
					
					userAccount = new UserAccountDTO();
					userAccount.setId(profile.getIdpProvider().getPguid());
					userAccount.setName(profile.getIdpProvider().getName());
					idpProvider.add(userAccount);
				}
				serachMailBoxDTO.setIdpProvider(idpProvider);
				serachMailBoxDTO.setStatus(acc.getActiveState());
				searchMailBoxDTOList.add(serachMailBoxDTO);
			}

			// Constructing the responses.
			serviceResponse.setUserAccount(searchMailBoxDTOList);
			serviceResponse.setResponse(new ResponseDTO(Messages.SEARCH_SUCCESSFUL, "USER ACCOUNT", Messages.SUCCESS));
			LOGGER.info("Exit from search user account.");
			return serviceResponse;

		} catch (MailBoxConfigurationServicesException e) {

			LOGGER.error(Messages.READ_OPERATION_FAILED.name(), e);
			serviceResponse
					.setResponse(new ResponseDTO(Messages.SEARCH_OPERATION_FAILED, "USER ACCOUNT", Messages.FAILURE, e.getMessage()));
			return serviceResponse;
		}

	}

	public PrepopulateUserAccountResponseDTO prePopulate() {

		LOGGER.info("Entering into data populate user account.");

		PrepopulateUserAccountResponseDTO serviceResponse = new PrepopulateUserAccountResponseDTO();

		try {

			// Getting user account
			AccountTypeConfigurationDAOBase configAccountDao = new AccountTypeConfigurationDAOBase();
			Set<AccountType> retrievedAccoutType = configAccountDao.findAllAccType();
			LanguageConfigurationDAOBase configLangDao = new LanguageConfigurationDAOBase();
			Set<Language> retrievedLanguage = configLangDao.findAllLanguage();
			GatewayTypeConfigurationDAOBase configGateDao = new GatewayTypeConfigurationDAOBase();
			Set<GatewayType> retrievedGateway = configGateDao.findAllGateType();
			IdpProviderConfigurationDAOBase configProviderDAO = new IdpProviderConfigurationDAOBase();
			Set<IdpProvider> retrievedProvider = configProviderDAO.findAllProviders();

			// Constructing the DTO from retrieved fields
			List<UserAccountDTO> accTypeDTO = new ArrayList<UserAccountDTO>();
			List<UserAccountDTO> gateTypeDTO = new ArrayList<UserAccountDTO>();
			List<UserAccountDTO> langDTO = new ArrayList<UserAccountDTO>();
			List<UserAccountDTO> providerDTO = new ArrayList<UserAccountDTO>();
			
			UserAccountDTO userAccountDTO = null;
			for (AccountType acc : retrievedAccoutType) {

				userAccountDTO = new UserAccountDTO();
				userAccountDTO.setId(acc.getPguid());
				userAccountDTO.setName(acc.getName());
				accTypeDTO.add(userAccountDTO);
			}
			for (GatewayType gate : retrievedGateway) {

				userAccountDTO = new UserAccountDTO();
				userAccountDTO.setId(gate.getPguid());
				userAccountDTO.setName(gate.getName());
				gateTypeDTO.add(userAccountDTO);
			}
			for (Language lang : retrievedLanguage) {

				userAccountDTO = new UserAccountDTO();
				userAccountDTO.setId(lang.getPguid());
				userAccountDTO.setName(lang.getName());
				langDTO.add(userAccountDTO);
			}
			for (IdpProvider prov : retrievedProvider) {

				userAccountDTO = new UserAccountDTO();
				userAccountDTO.setId(prov.getPguid());
				userAccountDTO.setName(prov.getName());
				providerDTO.add(userAccountDTO);
			}

			// Constructing the responses.
			serviceResponse.setAccountType(accTypeDTO);
			serviceResponse.setGatewayType(gateTypeDTO);
			serviceResponse.setLanguage(langDTO);
			serviceResponse.setIdpProvider(providerDTO);
			serviceResponse.setResponse(new ResponseDTO(Messages.DATA_PREPOPULATE, "USER ACCOUNT", Messages.SUCCESS));
			LOGGER.info("Exit from data populate user account.");
			return serviceResponse;

		} catch (Exception e) {

			LOGGER.error(Messages.READ_OPERATION_FAILED.name(), e);
			serviceResponse
					.setResponse(new ResponseDTO(Messages.SEARCH_OPERATION_FAILED, "USER ACCOUNT", Messages.FAILURE, e.getMessage()));
			return serviceResponse;
		}
	}
}
