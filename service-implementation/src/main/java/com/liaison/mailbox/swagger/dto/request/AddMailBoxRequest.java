/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall 
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.mailbox.swagger.dto.request;

import com.liaison.mailbox.service.dto.configuration.request.AddMailboxRequestDTO;

/**
 * Data Transfer Object for mailbox creation through swagger.
 * 
 * @author OFS
 */
public class AddMailBoxRequest {
	
	private AddMailboxRequestDTO addMailBoxRequest;

	public AddMailboxRequestDTO getAddMailBoxRequest() {
		return addMailBoxRequest;
	}

	public void setAddMailBoxRequest(AddMailboxRequestDTO addMailBoxRequest) {
		this.addMailBoxRequest = addMailBoxRequest;
	}

}