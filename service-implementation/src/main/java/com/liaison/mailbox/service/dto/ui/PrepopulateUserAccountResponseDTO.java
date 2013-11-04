/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall 
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.dto.ui;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jackson.map.annotate.JsonRootName;

import com.liaison.mailbox.service.dto.ResponseBuilder;
import com.liaison.mailbox.service.dto.ResponseDTO;
import com.liaison.mailbox.service.util.MailBoxUtility;

/**
 * 
 * 
 * @author praveenu
 */
@JsonRootName("prepopulateUserAccountResponse")
public class PrepopulateUserAccountResponseDTO implements ResponseBuilder {

	private ResponseDTO response;
	private List<UserAccountDTO> accountType;
	private List<UserAccountDTO> gatewayType;
	private List<UserAccountDTO> language;
	private List<UserAccountDTO> idpProvider;

	public List<UserAccountDTO> getAccountType() {

		if (null == accountType) {
			accountType = new ArrayList<UserAccountDTO>();
		}
		return accountType;
	}

	public void setAccountType(List<UserAccountDTO> accountType) {
		this.accountType = accountType;
	}
	
	public List<UserAccountDTO> getGatewayType() {
		if (null == gatewayType) {
			gatewayType = new ArrayList<UserAccountDTO>();
		}
		return gatewayType;
	}

	public void setGatewayType(List<UserAccountDTO> gatewayType) {
		this.gatewayType = gatewayType;
	}

	public List<UserAccountDTO> getLanguage() {
		if (null == language) {
			language = new ArrayList<UserAccountDTO>();
		}
		return language;
	}

	public void setLanguage(List<UserAccountDTO> language) {
		this.language = language;
	}
	
	public List<UserAccountDTO> getIdpProvider() {
		
		if (null == idpProvider) {
			idpProvider = new ArrayList<UserAccountDTO>();
		}
		return idpProvider;
	}

	public void setIdpProvider(List<UserAccountDTO> idpProvider) {
		this.idpProvider = idpProvider;
	}


	public ResponseDTO getResponse() {
		return response;
	}

	public void setResponse(ResponseDTO response) {
		this.response = response;
	}

	@Override
	public Response constructResponse() throws Exception {
		String responseBody = MailBoxUtility.marshalToJSON(this);
		return Response.ok(responseBody).header("Content-Type", MediaType.APPLICATION_JSON).build();
	}

}
