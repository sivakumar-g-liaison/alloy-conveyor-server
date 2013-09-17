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

/**
 *
 * @author karthikeyanm
 */
public class ProfileResponseDTO {
	
	private String guId;
	
	public ProfileResponseDTO() {}
	
	public ProfileResponseDTO(String guid){
		this.guId = guid;
	}

	public String getGuId() {
		return guId;
	}

	public void setGuId(String guId) {
		this.guId = guId;
	}
}
