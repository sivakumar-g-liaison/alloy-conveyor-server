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

import com.liaison.mailbox.service.dto.configuration.request.InterruptExecutionEventRequestDTO;

/**
 * Data Transfer Object for interrupt the execution through swagger.
 * 
 * @author OFS
 */
public class InterruptExecutionRequest {
	
	private InterruptExecutionEventRequestDTO interruptExecutionEventRequest;

	public InterruptExecutionEventRequestDTO getInterruptExecutionEventRequest() {
		return interruptExecutionEventRequest;
	}

	public void setInterruptExecutionEventRequest(
			InterruptExecutionEventRequestDTO interruptExecutionEventRequest) {
		this.interruptExecutionEventRequest = interruptExecutionEventRequest;
	}
	
}
