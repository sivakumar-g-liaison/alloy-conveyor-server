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

import com.liaison.mailbox.service.dto.configuration.request.AddProcessorToMailboxRequestDTO;

/**
 * Data Transfer Object for processor creation through swagger.
 * 
 * @author OFS
 */
public class AddProcessorToMailBoxRequest {
	
	private AddProcessorToMailboxRequestDTO addProcessorToMailBoxRequest;

	public AddProcessorToMailboxRequestDTO getAddProcessorToMailBoxRequest() {
		return addProcessorToMailBoxRequest;
	}

	public void setAddProcessorToMailBoxRequest(
			AddProcessorToMailboxRequestDTO addProcessorToMailBoxRequest) {
		this.addProcessorToMailBoxRequest = addProcessorToMailBoxRequest;
	}
	
}
