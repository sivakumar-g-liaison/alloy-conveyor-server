package com.liaison.mailbox.service.dto.configuration.response;

import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jackson.map.annotate.JsonRootName;

import com.liaison.mailbox.service.dto.ResponseBuilder;
import com.liaison.mailbox.service.dto.ResponseDTO;
import com.liaison.mailbox.service.dto.configuration.TenancyKeyDTO;
import com.liaison.mailbox.service.util.MailBoxUtil;

@JsonRootName("getTenancyKeysResponse")
public class GetTenancyKeysResponseDTO implements ResponseBuilder {
	
	private ResponseDTO response;
	private List <TenancyKeyDTO> tenancyKeys;
	
	public ResponseDTO getResponse() {
		return response;
	}

	public void setResponse(ResponseDTO response) {
		this.response = response;
	}

	public List<TenancyKeyDTO> getTenancyKeys() {
		return tenancyKeys;
	}

	public void setTenancyKeys(List<TenancyKeyDTO> tenancyKeys) {
		this.tenancyKeys = tenancyKeys;
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
