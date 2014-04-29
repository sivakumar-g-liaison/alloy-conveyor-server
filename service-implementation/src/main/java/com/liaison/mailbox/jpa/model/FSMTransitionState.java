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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.liaison.commons.jpa.Identifiable;

/**
 * The persistent class for the FSM_TRANSITION_STATE database table.
 * 
 *  @author OFS
 */
@Entity
@Table(name = "FSM_TRANSITION_STATE")
public class FSMTransitionState implements Identifiable {

	private static final long serialVersionUID = 1L;

	private String pguid;
	private FSMStateValue oldStateValue;
	private FSMStateValue newStateValue;

	public FSMTransitionState() {
	}
	
	@Id
	@Column(unique = true, nullable = false, length = 32)
	public String getPguid() {
		return pguid;
	}

	public void setPguid(String pguid) {
		this.pguid = pguid;
	}
	
	@ManyToOne
	@JoinColumn(name = "STATE_VALUE_OLD_GUID", nullable = false)
	public FSMStateValue getOldStateValue() {
		return oldStateValue;
	}

	public void setOldStateValue(FSMStateValue oldStateValue) {
		this.oldStateValue = oldStateValue;
	}

	@ManyToOne
	@JoinColumn(name = "STATE_VALUE_NEW_GUID", nullable = false)
	public FSMStateValue getNewStateValue() {
		return newStateValue;
	}

	public void setNewStateValue(FSMStateValue newStateValue) {
		this.newStateValue = newStateValue;
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
