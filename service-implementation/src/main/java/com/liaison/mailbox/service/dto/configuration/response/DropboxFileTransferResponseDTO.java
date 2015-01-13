package com.liaison.mailbox.service.dto.configuration.response;

import org.codehaus.jackson.map.annotate.JsonRootName;

import com.liaison.mailbox.service.dto.CommonResponseDTO;

@JsonRootName("dropBoxFileTransferResponse")
public class DropboxFileTransferResponseDTO extends CommonResponseDTO {
	
	private static final long serialVersionUID = 1L;
	
	private String authenticationToken;

	public String getAuthenticationToken() {
		return authenticationToken;
	}

	public void setAuthenticationToken(String authenticationToken) {
		this.authenticationToken = authenticationToken;
	}

}
