/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall 
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.dtdm.model;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.liaison.commons.jpa.Identifiable;

/**
 * The persistent class for the SCHEDULE_PROFILE_PROCESSORS database table.
 * 
 * @author OFS
 */
@Entity
@Table(name = "SCHED_PROCESSOR")
@NamedQuery(name = "ScheduleProfileProcessor.findAll", query = "SELECT s FROM ScheduleProfileProcessor s")
public class ScheduleProfileProcessor implements Identifiable {

	private static final long serialVersionUID = 1L;
	private String pguid;
	private Processor processor;
	private ScheduleProfilesRef scheduleProfilesRef;

	public ScheduleProfileProcessor() {
	}

	@Id
	@Column(unique = true, nullable = false, length = 32)
	public String getPguid() {
		return this.pguid;
	}

	public void setPguid(String pguid) {
		this.pguid = pguid;
	}

	// bi-directional many-to-one association to Processor
	@ManyToOne(cascade = {CascadeType.REFRESH})
	@JoinColumn(name = "PROCESSOR_GUID", nullable = false)
	public Processor getProcessor() {
		return this.processor;
	}

	public void setProcessor(Processor processor) {
		this.processor = processor;
	}

	// bi-directional many-to-one association to ScheduleProfilesRef
	@ManyToOne(cascade = {CascadeType.REFRESH}, fetch = FetchType.EAGER)
	@JoinColumn(name = "SCHED_PROFILE_GUID", nullable = false)
	@Fetch(value = FetchMode.SELECT)
	public ScheduleProfilesRef getScheduleProfilesRef() {
		return this.scheduleProfilesRef;
	}

	public void setScheduleProfilesRef(ScheduleProfilesRef scheduleProfilesRef) {
		this.scheduleProfilesRef = scheduleProfilesRef;
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