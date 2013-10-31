/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall 
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.mailbox.service.dto.configuration.request;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.annotate.JsonRootName;

import com.liaison.commons.security.pkcs7.SymmetricAlgorithmException;
import com.liaison.mailbox.jpa.model.Account;
import com.liaison.mailbox.jpa.model.IdpProfile;
import com.liaison.mailbox.jpa.model.Processor;
import com.liaison.mailbox.service.dto.configuration.AccountDTO;
import com.liaison.mailbox.service.dto.configuration.IdpProfileDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;

/**
 * @author karthikeyanm
 * 
 */

@JsonRootName("reviseUserProfileRequest")
public class ReviseUserProfileRequestDTO {

	private AccountDTO account;
	private IdpProfileDTO profile;

	public AccountDTO getAccount() {
		return account;
	}
	public void setAccount(AccountDTO account) {
		this.account = account;
	}
	public IdpProfileDTO getProfile() {
		return profile;
	}
	public void setProfile(IdpProfileDTO profile) {
		this.profile = profile;
	}

	public void copyToEntity(Account entity, IdpProfile profile) throws MailBoxConfigurationServicesException, JsonGenerationException,
			JsonMappingException, JAXBException, IOException, SymmetricAlgorithmException {
		this.getAccount().copyToEntity(entity, false);
		this.getProfile().copyToEntity(profile, false);
	}
	
}
