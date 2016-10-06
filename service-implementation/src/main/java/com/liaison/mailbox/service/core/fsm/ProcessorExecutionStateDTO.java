/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.core.fsm;

import java.util.Date;

public class ProcessorExecutionStateDTO {

    private String pguid;
    private String processorId;
    private String executionStatus;
    private String lastExecutionState;
    private Date lastExecutionDate;
    private String nodeInUse;
    private String modifiedBy;
    private Date modifiedDate;
    private String executionType;
    
    public String getPguid() {
        return pguid;
    }
    public void  setPguid(String pguid) {
        this.pguid = pguid;
    }
    public String getProcessorId() {
        return processorId;
    }
    public void setProcessorId(String processorId) {
        this.processorId = processorId;
    }
    public String getExecutionStatus() {
        return executionStatus;
    }
    public void setExecutionStatus(String executionStatus) {
        this.executionStatus = executionStatus;
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
    public String getModifiedBy() {
        return modifiedBy;
    }
    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }
    public Date getModifiedDate() {
        return modifiedDate;
    }
    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }
    public String getExecutionType() {
        return executionType;
    }
    public void setExecutionType(String executionType) {
        this.executionType = executionType;
    }
    
}
