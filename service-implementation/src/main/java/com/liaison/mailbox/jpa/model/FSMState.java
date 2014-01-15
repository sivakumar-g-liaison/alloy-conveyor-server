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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.liaison.commons.jpa.Identifiable;

/**
 * The persistent class for the FSM_STATE database table.
 */
@Entity
@Table(name = "FSM_STATE")
public class FSMState implements Identifiable {

	private static final long serialVersionUID = 1L;

	private String pguid;
	private String name;
	List<FSMStateValue> fsmStateValues;
	
	public FSMState() {
		
	}

	@Id
	@Column(unique = true, nullable = false, length = 32)
	public String getPguid() {
		return pguid;
	}

	public void setPguid(String pguid) {
		this.pguid = pguid;
	}

	@Column(name = "NAME", nullable = false, length = 1024)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@OneToMany(mappedBy = "fsmState", fetch = FetchType.EAGER)
	public List<FSMStateValue> getFsmStateValues() {
		return fsmStateValues;
	}

	public void setFsmStateValues(List<FSMStateValue> fsmStateValues) {
		this.fsmStateValues = fsmStateValues;
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
