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

import java.util.List;

import org.codehaus.jackson.map.annotate.JsonRootName;

import com.liaison.mailbox.service.dto.configuration.AccountDTO;
import com.liaison.mailbox.service.dto.configuration.IdpUserProfileDTO;


/**
 * 
 * 
 * @author praveenu
 */
@JsonRootName("addUserAccountRequest")
public class AddUserAccountRequestDTO {

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
	
}
