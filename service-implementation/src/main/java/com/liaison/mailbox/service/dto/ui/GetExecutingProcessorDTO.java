/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall 
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.dto.ui;

import com.liaison.mailbox.rtdm.model.FSMStateValue;

/**
 * Data Transfer Object that contains the fields required for retrieving the executing processors.
 * 
 * @author OFS
 */
public class GetExecutingProcessorDTO {
	
	private String executionId;
	private String processorName;
	private String value;
	private String profileName;
	private String createdDate;
	
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public String getCreatedDate() {
		return createdDate;
	}
	public void setCreatedDate(String createdDate) {
		this.createdDate = createdDate;
	}
	
	public String getExecutionId() {
		return executionId;
	}
	public void setExecutionId(String executionId) {
		this.executionId = executionId;
	}
	
	public String getProcessorName() {
		return processorName;
	}
	public void setProcessorName(String processorName) {
		this.processorName = processorName;
	}
	
	public String getProfileName() {
		return profileName;
	}
	public void setProfileName(String profileName) {
		this.profileName = profileName;
	}
	
	public void copyFromEntity(FSMStateValue fsmv) {
		
		this.setExecutionId(fsmv.getFsmState().getExecutionId());
		this.setProcessorName(fsmv.getFsmState().getProcessorName());
		this.setValue(fsmv.getValue());
		this.setProfileName(fsmv.getFsmState().getProfileName());
		this.setCreatedDate(fsmv.getCreatedDate().toString());
		
	}
		
}
