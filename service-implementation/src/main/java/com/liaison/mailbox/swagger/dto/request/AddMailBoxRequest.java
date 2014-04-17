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
 * 
 * @author OFS
 */
public class AddMailBoxRequest {
	
	private AddMailboxRequestDTO addMailboxRequest;

	protected AddMailboxRequestDTO getAddMailboxRequest() {
		return addMailboxRequest;
	}

	protected void setAddMailboxRequest(AddMailboxRequestDTO addMailboxRequest) {
		this.addMailboxRequest = addMailboxRequest;
	}
	
}