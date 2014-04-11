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

import com.liaison.mailbox.service.dto.ResponseBuilder;
import com.liaison.mailbox.service.dto.ResponseDTO;
import com.liaison.mailbox.service.util.MailBoxUtility;

public class GetExecutingProcessorResponseDTO implements ResponseBuilder {

	private ResponseDTO response;
	private List<GetExecutingProcessorDTO> executingProcessors;
	private String hitCounter;

	public List<GetExecutingProcessorDTO> getExecutingProcessor() {

		if (null == executingProcessors) {
			executingProcessors = new ArrayList<GetExecutingProcessorDTO>();
		}
		return executingProcessors;
	}

	public void setExecutingProcessor(List<GetExecutingProcessorDTO> executingProcessors) {
		this.executingProcessors = executingProcessors;
	}

	public ResponseDTO getResponse() {
		return response;
	}

	public void setResponse(ResponseDTO response) {
		this.response = response;
	}
	
	public void setHitCounter(String hitCounter) {
		this.hitCounter = hitCounter;
	}

	@Override
	public Response constructResponse() throws Exception {
		String responseBody = MailBoxUtility.marshalToJSON(this);
		return Response.ok(responseBody).header("Content-Type", MediaType.APPLICATION_JSON).build();
	}

}
