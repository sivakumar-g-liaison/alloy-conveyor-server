/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.mailbox.service.dto.dropbox.response;

import java.io.Serializable;

/**
 * @author OFS
 * 
 */
public class StagedFileResponseDTO implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private String guid;

	public StagedFileResponseDTO() { // Added for Deserialization
	}

	public StagedFileResponseDTO(String guid) {
		this.guid = guid;
	}

	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}
}
