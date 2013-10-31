package com.liaison.mailbox.service.dto.configuration.request;

import java.util.List;

import org.codehaus.jackson.map.annotate.JsonRootName;

import com.liaison.mailbox.service.dto.configuration.AccountDTO;
import com.liaison.mailbox.service.dto.configuration.IdpProfileDTO;
import com.liaison.mailbox.service.dto.configuration.IdpUserProfileDTO;

@JsonRootName("addUserProfileAccountRequest")
public class AddUserProfileAccountRequestDTO {

	private AccountDTO account;
	private List<IdpUserProfileDTO> idpProfiles;
	
	public AccountDTO getAccount() {
		return account;
	}
	public void setAccount(AccountDTO account) {
		this.account = account;
	}
	public List<IdpUserProfileDTO> getIdpProfiles() {
		return idpProfiles;
	}
	public void setIdpProfiles(List<IdpUserProfileDTO> idpProfiles) {
		this.idpProfiles = idpProfiles;
	}
	
	/*public void copyToEntity(Account account, IdpProfile profile) throws MailBoxConfigurationServicesException, JsonGenerationException,
	JsonMappingException, JAXBException, IOException, SymmetricAlgorithmException {
		
		this.getAccount().copyToEntity(account, true);
		this.getIdpProfile().copyToEntity(profile);
	}	*/
}
