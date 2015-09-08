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

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.liaison.commons.jpa.Identifiable;
import com.liaison.mailbox.rtdm.dao.FSMStateDAO;

/**
 * The persistent class for the FSM_STATE database table.
 * 
 *  @author OFS
 */
@Entity
@Table(name = "FSM_STATE")
@NamedQueries({ @NamedQuery(name = FSMStateDAO.FIND_FSM_STATE_BY_NAME,
query = "SELECT state FROM FSMState state WHERE state.executionId = :" + FSMStateDAO.EXECUTION_ID),
@NamedQuery(name = FSMStateDAO.FIND_ALL_EXECUTING_PROC,
query = "select stateVal from FSMStateValue stateVal"
		+ " where stateVal.createdDate IN (select max(staVal.createdDate) from FSMState sta"
		+ " inner join sta.executionState staVal"
		+ " where staVal.createdDate >= :" + FSMStateDAO.INTERVAL_IN_HOURS + " group by staVal.fsmState)"),
@NamedQuery(name = FSMStateDAO.FIND_EXECUTING_PROC_BY_VALUE,
query = "select stateVal from FSMStateValue stateVal"
		+ " where stateVal.createdDate IN (select max(staVal.createdDate) from FSMState sta"
		+ " inner join sta.executionState staVal"
		+ " where staVal.createdDate >= :" + FSMStateDAO.INTERVAL_IN_HOURS + " and stateVal.value = :" + FSMStateDAO.BY_VALUE
		+ " group by staVal.fsmState)"),
@NamedQuery(name = FSMStateDAO.FIND_EXECUTING_PROC_BY_DATE,
query = "select stateVal from FSMStateValue stateVal"
        + " where stateVal.createdDate IN (select max(staVal.createdDate) from FSMState sta"
        + " inner join sta.executionState staVal"
        + " where stateVal.createdDate >= :" + FSMStateDAO.FROM_DATE + " and stateVal.createdDate <= :" + FSMStateDAO.TO_DATE
        + " group by staVal.fsmState)"),
@NamedQuery(name = FSMStateDAO.FIND_EXECUTING_PROC_BY_VALUE_AND_DATE,
query = "select stateVal from FSMStateValue stateVal"
        + " where stateVal.createdDate IN (select max(staVal.createdDate) from FSMState sta"
        + " inner join sta.executionState staVal"
        + " where stateVal.createdDate >= :" + FSMStateDAO.FROM_DATE + " and stateVal.createdDate <= :" + FSMStateDAO.TO_DATE
        + " and stateVal.value = :" + FSMStateDAO.BY_VALUE
        + " group by staVal.fsmState)"),
@NamedQuery(name = FSMStateDAO.FIND_ALL_EXECUTING_PROC_BY_PROCESSORID,
query = "select stateVal from FSMStateValue stateVal"
        + " where stateVal.createdDate IN (select max(staVal.createdDate) from FSMState sta"
        + " inner join sta.executionState staVal"
        + " where sta.processorId = :" + FSMStateDAO.PROCESSOR_ID
        + " and staVal.createdDate >= :" + FSMStateDAO.INTERVAL_IN_HOURS + ")"),
@NamedQuery(name = FSMStateDAO.FIND_MOST_RECENT_SUCCESSFUL_EXECUTION_OF_PROCESSOR,
query = "select stateVal from FSMStateValue stateVal"
        + " where stateVal.createdDate IN (select max(staVal.createdDate) from FSMState sta"
        + " inner join sta.executionState staVal"
        + " where sta.processorId = :" + FSMStateDAO.PROCESSOR_ID
        + " and staVal.value = :" + FSMStateDAO.BY_VALUE + ")"),
@NamedQuery(name = FSMStateDAO.FIND_NON_SLA_VERIFIED_FSM_EVENTS_BY_VALUE,
query = "select state from FSMState state"
		+ " inner join state.executionState stateValue"
		+ " where state.slaVerificationStatus = :" + FSMStateDAO.SLA_VERIFICATION_STATUS
		+ " and state.processorId = :" + FSMStateDAO.PROCESSOR_ID
		+ " and stateValue.createdDate <= :" +FSMStateDAO.TO_DATE
		+ " and stateValue.value = :" + FSMStateDAO.BY_VALUE),
@NamedQuery(name = FSMStateDAO.FIND_NON_SLA_VERIFIED_FILE_STAGED_EVENTS,
query = "select state from FSMState state"
		+ " inner join state.executionState stateValue"
		+ " where state.slaVerificationStatus = :" + FSMStateDAO.SLA_VERIFICATION_STATUS
		+ " and state.processorId = :" + FSMStateDAO.PROCESSOR_ID
		+ " and stateValue.createdDate < :" +FSMStateDAO.TO_DATE
		+ " and stateValue.value = :" + FSMStateDAO.BY_VALUE)

})
public class FSMState implements Identifiable {

	private static final long serialVersionUID = 1L;

	private String pguid;
	private String executionId;
	private String processorId;
	private String processorName;
	private String processorType;
	private String mailboxId;
	private String profileName;
	private String stateNotes;
	private String slaVerificationStatus;
	
	List<FSMStateValue> executionState;
	
	@Id
	@Column(unique = true, nullable = false, length = 32)
	public String getPguid() {
		return pguid;
	}

	public void setPguid(String pguid) {
		this.pguid = pguid;
	}

	@OneToMany(mappedBy = "fsmState", fetch = FetchType.EAGER, orphanRemoval = true, cascade = { CascadeType.PERSIST,
			CascadeType.MERGE, CascadeType.REMOVE, CascadeType.REFRESH })
	@OrderBy(value="createdDate DESC")
	public List<FSMStateValue> getExecutionState() {
		return executionState;
	}

	public void setExecutionState(List<FSMStateValue> executionState) {
		this.executionState = executionState;
	}
	
	
	@Column(name = "EXECUTION_ID", nullable = false, length = 32)
	public String getExecutionId() {
		return executionId;
	}

	public void setExecutionId(String processorId) {
		this.executionId = processorId;
	}
	
	@Column(name = "PROCESSOR_ID", nullable = false, length = 32)
	public String getProcessorId() {
		return processorId;
	}

	public void setProcessorId(String processorId) {
		this.processorId = processorId;
	}

	@Column(name = "PROCESSOR_NAME", nullable = false, length = 512)
	public String getProcessorName() {
		return processorName;
	}

	public void setProcessorName(String processorName) {
		this.processorName = processorName;
	}

	@Column(name = "PROCESSOR_TYPE", nullable = false, length = 128)
	public String getProcessorType() {
		return processorType;
	}

	public void setProcessorType(String processorType) {
		this.processorType = processorType;
	}

	@Column(name = "MAILBOX_ID", nullable = false, length = 32)
	public String getMailboxId() {
		return mailboxId;
	}

	public void setMailboxId(String mailboxId) {
		this.mailboxId = mailboxId;
	}

	@Column(name = "PROFILE_NAME", nullable = false, length = 128)
	public String getProfileName() {
		return profileName;
	}

	public void setProfileName(String profileName) {
		this.profileName = profileName;
	}

	@Column(name = "STATE_NOTES", nullable = true, length = 1024)
	public String getStateNotes() {
		return stateNotes;
	}

	public void setStateNotes(String stateNotes) {
		this.stateNotes = stateNotes;
	}
	
	@Column(name = "SLA_VERIFICATION_STATUS", nullable = false, length = 128)
	public String getSlaVerificationStatus() {
		return slaVerificationStatus;
	}

	public void setSlaVerificationStatus(String slaVerificationStatus) {
		this.slaVerificationStatus = slaVerificationStatus;
	}

	@Override
	@Transient
	public Object getPrimaryKey() {
		return getPguid();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	@Transient
	public Class getEntityClass() {
		return this.getClass();
	}

}
