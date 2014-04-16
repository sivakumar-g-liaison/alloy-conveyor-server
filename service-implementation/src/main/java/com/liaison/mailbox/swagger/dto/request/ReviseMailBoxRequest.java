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

import com.liaison.mailbox.service.dto.configuration.request.ReviseMailBoxRequestDTO;

/**
 * 
 * @author OFS
 */
public class ReviseMailBoxRequest {
	
	private ReviseMailBoxRequestDTO reviseMailBoxRequest;

	protected ReviseMailBoxRequestDTO getReviseMailBoxRequest() {
		return reviseMailBoxRequest;
	}

	protected void setReviseMailBoxRequest(
			ReviseMailBoxRequestDTO reviseMailBoxRequest) {
		this.reviseMailBoxRequest = reviseMailBoxRequest;
	}
	
}
