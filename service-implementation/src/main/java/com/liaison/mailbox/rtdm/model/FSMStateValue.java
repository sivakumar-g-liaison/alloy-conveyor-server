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

import java.sql.Timestamp;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.liaison.commons.jpa.Identifiable;

/**
 * The persistent class for the FSM_STATE_VALUE database table.
 * 
 *  @author OFS
 */
@Entity
@Table(name = "FSM_STATE_VALUE")
public class FSMStateValue implements Identifiable {

	private static final long serialVersionUID = 1L;

	private String pguid;
	private String value;
	private Timestamp createdDate;
	private List<FSMTransitionState> oldTransitionStates;
	private List<FSMTransitionState> newTransitionStates;
	private FSMState fsmState;
	
	public FSMStateValue () {
		
	}
	
	@Id
	@Column(unique = true, nullable = false, length = 32)
	public String getPguid() {
		return pguid;
	}

	public void setPguid(String pguid) {
		this.pguid = pguid;
	}

	@Column(name = "VALUE", nullable = false, length = 2048)
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Column(name = "CREATED_DATE", nullable = false)
	public Timestamp getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Timestamp createdDate) {
		this.createdDate = createdDate;
	}
	
	@OneToMany(mappedBy = "newStateValue", fetch = FetchType.EAGER)
	public List<FSMTransitionState> getNewTransitionStates() {
		return newTransitionStates;
	}

	public void setNewTransitionStates(List<FSMTransitionState> newTransitionStates) {
		this.newTransitionStates = newTransitionStates;
	}

	@OneToMany(mappedBy = "oldStateValue", fetch = FetchType.EAGER)
	public List<FSMTransitionState> getOldTransitionStates() {
		return oldTransitionStates;
	}

	public void setOldTransitionStates(List<FSMTransitionState> oldTransitionStates) {
		this.oldTransitionStates = oldTransitionStates;
	}
	
	@ManyToOne
	@JoinColumn(name = "FSM_STATE_GUID", nullable = false)
	public FSMState getFsmState() {
		return fsmState;
	}

	public void setFsmState(FSMState fsmState) {
		this.fsmState = fsmState;
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
