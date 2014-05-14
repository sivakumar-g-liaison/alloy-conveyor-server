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

import com.liaison.mailbox.jpa.model.FSMEvent;
import com.liaison.mailbox.service.validation.Mandatory;
import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

/**
 * 
 * 
 * @author OFS
 */
@ApiModel(value = "fsmEvent")
public class FSMEventDTO {

	@ApiModelProperty( value = "Execution ID", required = true)
	private String executionID;
	private String value;
	
	@Mandatory(errorMessage = "Execution ID is mandatory.")
	public String getExecutionID() {
		return executionID;
	}
	public void setExecutionID(String executionID) {
		this.executionID = executionID;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	
	public void copyToEntity(FSMEvent fsmEvent) {
		fsmEvent.setData(this.executionID);
		fsmEvent.setName(this.value);
	}

	public void copyFromEntity(FSMEvent fsmEvent) {
		this.setExecutionID(fsmEvent.getData());
		this.setValue(fsmEvent.getName());
	}
	
}
