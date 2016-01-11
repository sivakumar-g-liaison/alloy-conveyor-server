/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall 
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.dto.configuration;

import org.codehaus.jackson.map.annotate.JsonRootName;

/**
 * Data Transfer Object that implements fields required for mailbox configuration request.
 * 
 * @author veerasamyn
 */
@JsonRootName("triggerProcessorRequest")
public class TriggerProcessorRequestDTO {

	private String executionId;
	private String processorId;
	private String profileName;
	
	@SuppressWarnings("unused")
	private TriggerProcessorRequestDTO() {
		super();
	}
	
	public TriggerProcessorRequestDTO(String executionId, String processorId, String profileName) {
		this.executionId = executionId;
		this.processorId = processorId;
		this.profileName = profileName;
	}
	public String getExecutionId() {
		return executionId;
	}
	public void setExecutionId(String executionId) {
		this.executionId = executionId;
	}
	public String getProcessorId() {
		return processorId;
	}
	public void setProcessorId(String processorId) {
		this.processorId = processorId;
	}

	public String getProfileName() {
		return profileName;
	}

	public void setProfileName(String profileName) {
		this.profileName = profileName;
	}
	
}
