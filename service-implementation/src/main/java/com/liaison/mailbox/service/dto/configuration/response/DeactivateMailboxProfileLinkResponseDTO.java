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

import java.io.IOException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;

import com.liaison.commons.jaxb.JAXBUtility;
import com.liaison.mailbox.service.dto.ResponseBuilder;
import com.liaison.mailbox.service.dto.ResponseDTO;

/**
 * 
 *
 * @author praveenu
 */
public class DeactivateMailboxProfileLinkResponseDTO implements ResponseBuilder {

	private ResponseDTO response;
	private String mailboxProfileLinkGuid;
	
	public String getMailboxProfileLinkGuid() {
		return mailboxProfileLinkGuid;
	}
	
	public void setMailboxProfileLinkGuid(String mailboxProfileLinkGuid) {
		this.mailboxProfileLinkGuid = mailboxProfileLinkGuid;
	}
	
	public ResponseDTO getResponse() {
		return response;
	}
	
	public void setResponse(ResponseDTO response) {
		this.response = response;
	}

	@Override
	public Response constructResponse(String mediaType)
			throws JsonGenerationException, JsonMappingException, JAXBException, IOException {
		
		String responseBody;
		if (MediaType.APPLICATION_XML.equals(mediaType)) {

			responseBody = JAXBUtility.marshalToXML(this);
			return Response.ok(responseBody).header("Content-Type", MediaType.APPLICATION_XML).build();
		} else {

			responseBody = JAXBUtility.marshalToJSON(this);
			return Response.ok(responseBody).header("Content-Type", MediaType.APPLICATION_JSON).build();
		}
	}
}
