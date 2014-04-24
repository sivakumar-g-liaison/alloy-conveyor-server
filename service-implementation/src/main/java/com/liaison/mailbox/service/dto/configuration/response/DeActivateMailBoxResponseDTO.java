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
import com.liaison.mailbox.service.dto.configuration.MailBoxResponseDTO;
import com.liaison.mailbox.service.util.MailBoxUtil;

/**
 * 
 * 
 * @author veerasamyn
 */
@JsonRootName("deactivateMailBoxResponse")
public class DeActivateMailBoxResponseDTO implements ResponseBuilder {

	private ResponseDTO response;
	private MailBoxResponseDTO mailBox;

	public MailBoxResponseDTO getMailBox() {
		return mailBox;
	}

	public void setMailBox(MailBoxResponseDTO mailBox) {
		this.mailBox = mailBox;
	}

	public ResponseDTO getResponse() {
		return response;
	}

	public void setResponse(ResponseDTO response) {
		this.response = response;
	}
    
	/**
	 * Method constructs response.
	 *
	 * @throws Exception
	 * @return Response.
	 */
	@Override
	public Response constructResponse() throws Exception {
		String responseBody = MailBoxUtil.marshalToJSON(this);
		return Response.ok(responseBody).header("Content-Type", MediaType.APPLICATION_JSON).build();
	}

}
