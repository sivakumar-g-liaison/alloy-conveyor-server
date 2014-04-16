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
import com.liaison.mailbox.service.util.MailBoxUtility;

/**
 * @author OFS
 * 
 */

@JsonRootName("reviseProcessorResponse")
public class ReviseProcessorResponseDTO implements ResponseBuilder {

	private ResponseDTO response;
	private ProcessorResponseDTO processor;

	/**
	 * @return the response
	 */
	public ResponseDTO getResponse() {
		return response;
	}

	/**
	 * @param response
	 *            the response to set
	 */
	public void setResponse(ResponseDTO response) {
		this.response = response;
	}

	public ProcessorResponseDTO getProcessor() {
		return processor;
	}

	public void setProcessor(ProcessorResponseDTO processor) {
		this.processor = processor;
	}
    
	/**
	 * Method constructs response.
	 *
	 * @throws Exception
	 * @return Response.
	 */
	@Override
	public Response constructResponse() throws Exception {

		String responseBody = MailBoxUtility.marshalToJSON(this);
		return Response.ok(responseBody).header("Content-Type", MediaType.APPLICATION_JSON).build();
	}

}
