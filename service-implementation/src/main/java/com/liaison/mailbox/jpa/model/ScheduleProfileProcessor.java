package com.liaison.mailbox.jpa.model;

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

import com.liaison.commons.jpa.Identifiable;

/**
 * The persistent class for the SCHEDULE_PROFILE_PROCESSORS database table.
 * 
 */
@Entity
@Table(name = "SCHEDULE_PROFILE_PROCESSORS")
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
	@ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY)
	@JoinColumn(name = "PROCESSOR_GUID", nullable = false)
	public Processor getProcessor() {
		return this.processor;
	}

	public void setProcessor(Processor processor) {
		this.processor = processor;
	}

	// bi-directional many-to-one association to ScheduleProfilesRef
	@ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY)
	@JoinColumn(name = "SCHEDULE_PROFILE_REF_GUID", nullable = false)
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