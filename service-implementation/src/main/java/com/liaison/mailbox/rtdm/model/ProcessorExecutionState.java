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

import com.liaison.commons.jpa.Identifiable;
import com.liaison.mailbox.rtdm.dao.ProcessorExecutionStateDAO;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import java.util.Date;

/**
 * The persistent class for the PROCESSOR_EXEC_STATE database table.
 *
 *  @author OFS
 */
@Entity
@Table(name = "PROCESSOR_EXEC_STATE")
@NamedQueries({
        @NamedQuery(name = ProcessorExecutionStateDAO.FIND_BY_PROCESSOR_ID,
                query = "SELECT executionState FROM ProcessorExecutionState executionState WHERE executionState.processorId = :" + ProcessorExecutionStateDAO.PROCESSOR_ID),
        @NamedQuery(name = ProcessorExecutionStateDAO.FIND_NON_EXECUTING_PROCESSORS,
                query = "SELECT executionState.processorId FROM ProcessorExecutionState executionState WHERE executionState.executionStatus not like :" + ProcessorExecutionStateDAO.EXEC_STATUS),
        @NamedQuery(name = ProcessorExecutionStateDAO.FIND_EXECUTING_PROCESSORS,
                query = "SELECT executionState FROM ProcessorExecutionState executionState WHERE executionState.executionStatus like :" + ProcessorExecutionStateDAO.EXEC_STATUS),
        @NamedQuery(name = ProcessorExecutionStateDAO.FIND_EXECUTING_PROCESSORS_ALL,
                query = "SELECT count(executionState) FROM ProcessorExecutionState executionState WHERE executionState.executionStatus like :" + ProcessorExecutionStateDAO.EXEC_STATUS),
        @NamedQuery(name = ProcessorExecutionStateDAO.FIND_EXECUTING_PROCESSOR_WITHIN_PERIOD,
                query = "SELECT executionState FROM ProcessorExecutionState executionState"
                        + " WHERE executionState.processorId = :" + ProcessorExecutionStateDAO.PROCESSOR_ID
                        + " AND executionState.lastExecutionDate >= :" + ProcessorExecutionStateDAO.INTERVAL_IN_HOURS )
})
public class ProcessorExecutionState implements Identifiable {

    private static final long serialVersionUID = 1L;

    private String pguid;
    private String processorId;
    private String executionStatus;
    private String lastExecutionState;
    private Date lastExecutionDate;
    private String nodeInUse;
    private String modifiedBy;
    private Date modifiedDate;
    private String threadName;
    private RuntimeProcessors processors;
    private String originatingDc;

    @Id
    @Column(unique = true, nullable = false, length = 32)
    public String getPguid() {
        return pguid;
    }

    public void setPguid(String pguid) {
        this.pguid = pguid;
    }

    @Column(name = "PROCESSOR_ID", nullable = false, length = 32)
    public String getProcessorId() {
        return processorId;
    }

    public void setProcessorId(String processorId) {
        this.processorId = processorId;
    }

    @Column(name = "EXEC_STATUS", length = 128)
    public String getExecutionStatus() {
        return executionStatus;
    }

    public void setExecutionStatus(String executionStatus) {
        this.executionStatus = executionStatus;
    }

    @Column(name = "LAST_EXEC_STATE", length = 32)
    public String getLastExecutionState() {
        return lastExecutionState;
    }

    public void setLastExecutionState(String lastExecutionState) {
        this.lastExecutionState = lastExecutionState;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "LAST_EXEC_DATE")
    public Date getLastExecutionDate() {
        return lastExecutionDate;
    }

    public void setLastExecutionDate(Date lastExecutionDate) {
        this.lastExecutionDate = lastExecutionDate;
    }

    @Column(name = "NODE_IN_USE", length = 128)
    public String getNodeInUse() {
        return nodeInUse;
    }

    public void setNodeInUse(String nodeInUse) {
        this.nodeInUse = nodeInUse;
    }

    @Column(name = "MODIFIED_BY", length = 128)
    public String getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "MODIFIED_DATE", length = 128)
    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    @Column(name = "THREAD_NAME", length = 32)
    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    @OneToOne(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST})
    @PrimaryKeyJoinColumn
    @Fetch(FetchMode.JOIN)
    public RuntimeProcessors getProcessors() {
        return processors;
    }

    public void setProcessors(RuntimeProcessors processors) {
        this.processors = processors;
    }

    @Column(name = "ORIGINATING_DC", length = 16)
    public String getOriginatingDc() {
        return originatingDc;
    }

    public void setOriginatingDc(String originatingDc) {
        this.originatingDc = originatingDc;
    }

    @Override
    @Transient
    public Object getPrimaryKey() {
        return getPguid();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    @Transient
    public Class getEntityClass() {
        return this.getClass();
    }


}
