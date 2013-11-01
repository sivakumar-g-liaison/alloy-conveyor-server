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
	private List<String> accountType;
	private List<String> gatewayType;
	private List<String> language;
	private List<String> idpProvider;

	public List<String> getAccountType() {

		if (null == accountType) {
			accountType = new ArrayList<String>();
		}
		return accountType;
	}

	public void setAccountType(List<String> accountType) {
		this.accountType = accountType;
	}
	
	public List<String> getGatewayType() {
		if (null == gatewayType) {
			gatewayType = new ArrayList<String>();
		}
		return gatewayType;
	}

	public void setGatewayType(List<String> gatewayType) {
		this.gatewayType = gatewayType;
	}

	public List<String> getLanguage() {
		if (null == language) {
			language = new ArrayList<String>();
		}
		return language;
	}

	public void setLanguage(List<String> language) {
		this.language = language;
	}
	
	public List<String> getIdpProvider() {
		
		if (null == idpProvider) {
			idpProvider = new ArrayList<String>();
		}
		return idpProvider;
	}

	public void setIdpProvider(List<String> idpProvider) {
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
