package com.liaison.mailbox.service.dto.configuration.response;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jackson.map.annotate.JsonRootName;

import com.liaison.mailbox.service.dto.ResponseBuilder;
import com.liaison.mailbox.service.dto.ResponseDTO;
import com.liaison.mailbox.service.util.MailBoxUtility;

@JsonRootName("addUserProfileAccountRequest")
public class AddUserProfileAccountResponseDTO implements ResponseBuilder {

	private ResponseDTO response;
	private UserProfileResponseDTO userResponse;

	public ResponseDTO getResponse() {
		return response;
	}

	public void setResponse(ResponseDTO response) {
		this.response = response;
	}
	
	public UserProfileResponseDTO getUserResponse() {
		return userResponse;
	}
	
	public void setUserResponse(UserProfileResponseDTO userResponse) {
		this.userResponse = userResponse;
	}
	
	@Override
	public Response constructResponse() throws Exception {

		String responseBody = MailBoxUtility.marshalToJSON(this);
		return Response.ok(responseBody).header("Content-Type", MediaType.APPLICATION_JSON).build();
	}

}
