/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.rtdm.model;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;


/**
 * The persistent class for the PROCESSORS database table.
 *
 *  @author OFS
 */
@Entity
@Table(name = "PROCESSORS")
public class RuntimeProcessors {

    private String pguid;
    private String processorId;
    private ProcessorExecutionState processorExecState;
    private String originatingDc;
    private String clusterType;

    @Column(name = "PGUID", length = 32)
    @Id
    public String getPguid() {
        return pguid;
    }

    public void setPguid(String pguid) {
        this.pguid = pguid;
    }

    @Column(name = "PROCESSOR_ID", length = 32)
    public String getProcessorId() {
        return processorId;
    }

    public void setProcessorId(String processorId) {
        this.processorId = processorId;
    }

    @OneToOne(fetch = FetchType.EAGER, mappedBy = "processors", cascade = CascadeType.ALL)
    public ProcessorExecutionState getProcessorExecState() {
        return processorExecState;
    }

    public void setProcessorExecState(ProcessorExecutionState processorExecState) {
        this.processorExecState = processorExecState;
    }

    @Column(name = "ORIGINATING_DC", length = 16)
    public String getOriginatingDc() {
        return originatingDc;
    }

    public void setOriginatingDc(String originatingDc) {
        this.originatingDc = originatingDc;
    }
    
    @Column(name = "CLUSTER_TYPE", nullable = false, length = 32)
    public String getClusterType() {
        return clusterType;
    }
    
    public void setClusterType(String clusterType) {
        this.clusterType = clusterType;
    }
}
