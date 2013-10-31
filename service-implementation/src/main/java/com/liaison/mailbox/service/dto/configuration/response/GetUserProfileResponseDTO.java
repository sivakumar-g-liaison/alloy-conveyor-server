/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall 
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.dto.configuration.response;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jackson.map.annotate.JsonRootName;

import com.liaison.mailbox.service.dto.ResponseBuilder;
import com.liaison.mailbox.service.dto.ResponseDTO;
import com.liaison.mailbox.service.dto.configuration.AccountDTO;
import com.liaison.mailbox.service.dto.configuration.IdpProfileDTO;
import com.liaison.mailbox.service.util.MailBoxUtility;

/**
 * 
 * 
 * @author praveenu
 */
@JsonRootName("getMailBoxResponse")
public class GetUserProfileResponseDTO implements ResponseBuilder {

	private ResponseDTO response;
	private AccountDTO account;
	private IdpProfileDTO ipdProfile;

	public ResponseDTO getResponse() {
		return response;
	}

	public void setResponse(ResponseDTO response) {
		this.response = response;
	}
	
	public AccountDTO getAccount() {
		return account;
	}

	public void setAccount(AccountDTO account) {
		this.account = account;
	}

	public IdpProfileDTO getIpdProfile() {
		return ipdProfile;
	}

	public void setIpdProfile(IdpProfileDTO ipdProfile) {
		this.ipdProfile = ipdProfile;
	}

	@Override
	public Response constructResponse() throws Exception {
		String responseBody = MailBoxUtility.marshalToJSON(this);
		return Response.ok(responseBody).header("Content-Type", MediaType.APPLICATION_JSON).build();
	}

	
}
