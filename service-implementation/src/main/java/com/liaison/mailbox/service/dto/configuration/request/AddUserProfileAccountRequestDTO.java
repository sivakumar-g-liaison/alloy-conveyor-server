package com.liaison.mailbox.service.dto.configuration.request;

import org.codehaus.jackson.map.annotate.JsonRootName;

import com.liaison.mailbox.service.dto.configuration.AccountDTO;
import com.liaison.mailbox.service.dto.configuration.IdpProfileDTO;

@JsonRootName("addUserProfileAccountRequest")
public class AddUserProfileAccountRequestDTO {

	private AccountDTO account;
	private IdpProfileDTO idpProfile;
	
	public AccountDTO getAccount() {
		return account;
	}
	public void setAccount(AccountDTO account) {
		this.account = account;
	}
	public IdpProfileDTO getIdpProfile() {
		return idpProfile;
	}
	public void setIdpProfile(IdpProfileDTO idpProfile) {
		this.idpProfile = idpProfile;
	}
	
	/*public void copyToEntity(Account account, IdpProfile profile) throws MailBoxConfigurationServicesException, JsonGenerationException,
	JsonMappingException, JAXBException, IOException, SymmetricAlgorithmException {
		
		this.getAccount().copyToEntity(account, true);
		this.getIdpProfile().copyToEntity(profile);
	}	*/
}
