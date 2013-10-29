package com.liaison.mailbox.service.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liaison.commons.security.pkcs7.SymmetricAlgorithmException;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.jpa.dao.UserAccountConfigurationDAOBase;
import com.liaison.mailbox.jpa.model.Account;
import com.liaison.mailbox.jpa.model.AccountType;
import com.liaison.mailbox.jpa.model.GatewayType;
import com.liaison.mailbox.jpa.model.IdpProfile;
import com.liaison.mailbox.jpa.model.IdpProvider;
import com.liaison.mailbox.jpa.model.Language;
import com.liaison.mailbox.service.dto.ResponseDTO;
import com.liaison.mailbox.service.dto.configuration.AccountDTO;
import com.liaison.mailbox.service.dto.configuration.IdpProfileDTO;
import com.liaison.mailbox.service.dto.configuration.request.AddUserProfileAccountRequestDTO;
import com.liaison.mailbox.service.dto.configuration.response.AddUserProfileAccountResponseDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;

public class UserProfileConfigurationService {

	private static final Logger LOGGER = LoggerFactory.getLogger(UserProfileConfigurationService.class);
	
	
	/**
	 * Creates processor for the mailbox.
	 * 
	 * @param serviceRequest
	 *            The AddProcessorToMailboxRequestDTO
	 * @return The AddProcessorToMailboxResponseDTO
	 * @throws IOException
	 * @throws JAXBException
	 * @throws JsonMappingException
	 * @throws JsonGenerationException
	 * @throws SymmetricAlgorithmException
	 */
	public AddUserProfileAccountResponseDTO createUserProfileAccount(AddUserProfileAccountRequestDTO serviceRequest)
			throws JsonGenerationException, JsonMappingException, JAXBException, IOException, SymmetricAlgorithmException {

		LOGGER.info("call receive to insert the user account ::{}", serviceRequest.getIdpProfile());
		AddUserProfileAccountResponseDTO serviceResponse = new AddUserProfileAccountResponseDTO();

		try {

			AccountDTO accountDTO = serviceRequest.getAccount();
			if (accountDTO == null) {
				throw new MailBoxConfigurationServicesException(Messages.INVALID_REQUEST);
			}
			
			IdpProfileDTO ipdProfileDTO = serviceRequest.getIdpProfile();
			if(ipdProfileDTO == null){
				throw new MailBoxConfigurationServicesException(Messages.INVALID_REQUEST);
			}

			Language language = new Language();
			AccountType accountType = new AccountType();
			
			Account account = new Account();
			account.setLanguage(language);
			account.setAccountType(accountType);
			serviceRequest.getAccount().copyToEntity(account, true);

			IdpProfile profile = new IdpProfile();
			List<IdpProvider> List = new ArrayList<>();
			GatewayType gatewayType = new GatewayType();
			
			profile.setIdpProvider(List);
			profile.setGatewayType(gatewayType);
			serviceRequest.getIdpProfile().copyToEntity(profile);

			// persist the account.
			UserAccountConfigurationDAOBase configAccountDAO = new UserAccountConfigurationDAOBase();
			configAccountDAO.persist(account);
			
			serviceResponse.setResponse(new ResponseDTO(Messages.CREATED_SUCCESSFULLY, "PROCESSOR", Messages.SUCCESS));
			LOGGER.info("Exit from create processor.");
			return serviceResponse;

		} catch (MailBoxConfigurationServicesException e) {

			LOGGER.error(Messages.CREATE_OPERATION_FAILED.name(), e);
			serviceResponse.setResponse(new ResponseDTO(Messages.CREATE_OPERATION_FAILED, "PROCESSOR", Messages.FAILURE, e
					.getMessage()));
			return serviceResponse;

		}

	}
}
