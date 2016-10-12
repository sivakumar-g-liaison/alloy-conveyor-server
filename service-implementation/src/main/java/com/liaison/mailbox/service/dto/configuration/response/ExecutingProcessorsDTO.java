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

import java.util.Date;

import com.liaison.mailbox.rtdm.model.ProcessorExecutionState;


/**
 * Data Transfer Object that contains the executing processors details.
 * 
 * @author OFS
 */
public class ExecutingProcessorsDTO {

	private String processorId;
	private String executionStatus;
	private String lastExecutionState;
    private Date lastExecutionDate;
    private String nodeInUse;
    private String modifiedBy;
    private Date modifiedDate;
    private String threadName;

	public String getExecutionStatus() {
		return executionStatus;
	}

	public void setExecutionStatus(String executionStatus) {
		this.executionStatus = executionStatus;
	}

	public String getProcessorId() {
		return processorId;
	}

	public void setProcessorId(String processorId) {
		this.processorId = processorId;
	}
	
	public String getLastExecutionState() {
	    return lastExecutionState;
	}
	
	public void setLastExecutionState(String lastExecutionState) {
	    this.lastExecutionState = lastExecutionState;
	}
	
	public Date getLastExecutionDate() {
	    return lastExecutionDate;
	}
	
	public void setLastExecutionDate(Date lastExecutionDate) {
	    this.lastExecutionDate = lastExecutionDate;
	}
	
	public String getNodeInUse() {
	    return nodeInUse;
	}
	
	public void setNodeInUse(String nodeInUse) {
	    this.nodeInUse = nodeInUse;
	}
	
	public String getThreadName() {
	    return threadName;
	}
	
	public void setThreadName(String threadName) {
	    this.threadName = threadName;
	}
	
	public Date getModifiedDate() {
	    return modifiedDate;
	}
	
	public void setModifiedDate(Date modifiedDate) {
	    this.modifiedDate = modifiedDate;
	}
	
	public String getModifiedBy() {
	    return modifiedBy;
	}
	
	public void setModifiedBy(String modifiedBy) {
	    this.modifiedBy = modifiedBy;
	}
	
	public void copyFromEntity(ProcessorExecutionState processorState) {
	    this.setExecutionStatus(processorState.getExecutionStatus());
	    this.setProcessorId(processorState.getProcessorId());
	    this.setLastExecutionState(processorState.getLastExecutionState());
	    this.setLastExecutionDate(processorState.getLastExecutionDate());
	    this.setNodeInUse(processorState.getNodeInUse());
	    this.setThreadName(processorState.getThreadName());
	    this.setModifiedDate(processorState.getModifiedDate());
	    this.setModifiedBy(processorState.getModifiedBy());
	}

}
