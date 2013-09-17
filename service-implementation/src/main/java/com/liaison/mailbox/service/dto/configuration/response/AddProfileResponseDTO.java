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
import org.codehaus.jackson.map.annotate.JsonRootName;

import com.liaison.mailbox.service.dto.ResponseBuilder;
import com.liaison.mailbox.service.dto.ResponseDTO;
import com.liaison.mailbox.service.util.MailBoxUtility;

/**
 * 
 * @author karthikeyanm
 */
@JsonRootName("addProfileResponse")
public class AddProfileResponseDTO implements ResponseBuilder {

	private ResponseDTO response;

	private ProfileResponseDTO profile;
	
	public ResponseDTO getResponse() {
		return response;
	}

	public void setResponse(ResponseDTO response) {
		this.response = response;
	}

	public ProfileResponseDTO getProfile() {
		return profile;
	}
	
	public void setProfile(ProfileResponseDTO profile) {
		this.profile = profile;
	}
	
	@Override
	public Response constructResponse() throws JsonGenerationException, JsonMappingException, JAXBException, IOException {
		String responseBody = MailBoxUtility.marshalToJSON(this);
		return Response.ok(responseBody).header("Content-Type", MediaType.APPLICATION_JSON).build();
	}
}
