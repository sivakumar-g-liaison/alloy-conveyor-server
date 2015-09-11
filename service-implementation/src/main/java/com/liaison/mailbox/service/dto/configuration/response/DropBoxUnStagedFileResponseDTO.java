package com.liaison.mailbox.service.dto.configuration.response;


import org.codehaus.jackson.map.annotate.JsonRootName;

import com.liaison.mailbox.service.dto.CommonResponseDTO;

/**
 * Data Transfer Object used for sending unstaged file Responses.
 * 
 * @author OFS
 */
@JsonRootName("getUnStagedFileResponse")
public class DropBoxUnStagedFileResponseDTO  extends CommonResponseDTO {

	private static final long serialVersionUID = 1L;
	private String guid;

	public String getGUID() {
		return guid;
	}
	public void setGUID(String guid) {
		this.guid = guid;
	}

}