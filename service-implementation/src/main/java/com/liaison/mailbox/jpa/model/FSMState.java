/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall 
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.jpa.model;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.liaison.commons.jpa.Identifiable;

/**
 * The persistent class for the FSM_STATE database table.
 * 
 *  @author OFS
 */
@Entity
@Table(name = "FSM_STATE")
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
