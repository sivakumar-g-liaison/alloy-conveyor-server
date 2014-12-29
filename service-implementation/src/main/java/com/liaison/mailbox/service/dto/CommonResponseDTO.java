package com.liaison.mailbox.service.dto;

import java.io.Serializable;

public abstract class CommonResponseDTO implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ResponseDTO response;
	
	public ResponseDTO getResponse() {
		return response;
	}

	public void setResponse(ResponseDTO response) {
		this.response = response;
	}

}
