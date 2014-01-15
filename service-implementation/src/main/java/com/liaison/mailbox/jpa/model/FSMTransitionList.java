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

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.liaison.commons.jpa.Identifiable;

/**
 * The persistent class for the FSM_TRANSITION_LIST database table.
 */
@Entity
@Table(name = "FSM_TRANSITION_LIST")
public class FSMTransitionList implements Identifiable {
	
	private static final long serialVersionUID = 1L;

	private String pguid;
	private Date cretedDate;
	private FSMEvent fsmEvent;
	
	@JoinColumn(name = "EVENT_GUID", nullable = false)
	public FSMEvent getFsmEvent() {
		return fsmEvent;
	}

	public void setFsmEvent(FSMEvent fsmEvent) {
		this.fsmEvent = fsmEvent;
	}

	@Id
	@Column(unique = true, nullable = false, length = 32)
	public String getPguid() {
		return pguid;
	}

	public void setPguid(String pguid) {
		this.pguid = pguid;
	}

	@Column(name = "CREATED_DATE", nullable = false)
	public Date getCretedDate() {
		return cretedDate;
	}

	public void setCretedDate(Date cretedDate) {
		this.cretedDate = cretedDate;
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
