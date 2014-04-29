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
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.liaison.commons.jpa.Identifiable;

/**
 * The persistent class for the SCHEDULE_PROFILES_REF database table.
 * 
 * @author OFS
 */
@Entity
@Table(name = "SCHED_PROFILE")
@NamedQuery(name = "ScheduleProfilesRef.findAll", query = "SELECT s FROM ScheduleProfilesRef s")
public class ScheduleProfilesRef implements Identifiable {

	private static final long serialVersionUID = 1L;

	private String pguid;
	private String schProfName;
	private List<ScheduleProfileProcessor> scheduleProfileProcessors;

	public ScheduleProfilesRef() {
	}

	@Id
	@Column(unique = true, nullable = false, length = 32)
	public String getPguid() {
		return this.pguid;
	}

	public void setPguid(String pguid) {
		this.pguid = pguid;
	}

	@Column(name = "NAME", nullable = false, length = 128)
	public String getSchProfName() {
		return this.schProfName;
	}

	public void setSchProfName(String schProfName) {
		this.schProfName = schProfName;
	}

	// bi-directional many-to-one association to ScheduleProfileProcessor
	@OneToMany(mappedBy = "scheduleProfilesRef", cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE,
			CascadeType.REFRESH })
	public List<ScheduleProfileProcessor> getScheduleProfileProcessors() {
		return this.scheduleProfileProcessors;
	}

	public void setScheduleProfileProcessors(List<ScheduleProfileProcessor> scheduleProfileProcessors) {
		this.scheduleProfileProcessors = scheduleProfileProcessors;
	}

	public ScheduleProfileProcessor addScheduleProfileProcessor(ScheduleProfileProcessor scheduleProfileProcessor) {
		getScheduleProfileProcessors().add(scheduleProfileProcessor);
		scheduleProfileProcessor.setScheduleProfilesRef(this);

		return scheduleProfileProcessor;
	}

	public ScheduleProfileProcessor removeScheduleProfileProcessor(ScheduleProfileProcessor scheduleProfileProcessor) {
		getScheduleProfileProcessors().remove(scheduleProfileProcessor);
		scheduleProfileProcessor.setScheduleProfilesRef(null);

		return scheduleProfileProcessor;
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