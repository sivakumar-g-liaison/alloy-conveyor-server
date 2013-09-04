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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.liaison.commons.jpa.Identifiable;

/**
 * The persistent class for the MAILBOX_SCHED_PROFILES database table.
 * 
 */
@Entity
@Table(name = "MAILBOX_SCHED_PROFILES")
@NamedQuery(name = "MailBoxSchedProfile.findAll", query = "SELECT m FROM MailBoxSchedProfile m")
public class MailBoxSchedProfile implements Identifiable {

	private static final long serialVersionUID = 1L;

	private String pguid;
	private String mbxProfileStatus;
	private MailBox mailbox;
	private ScheduleProfilesRef scheduleProfilesRef;
	private List<Processor> processors;

	public MailBoxSchedProfile() {
	}

	@Id
	@Column(unique = true, nullable = false, length = 32)
	public String getPguid() {
		return this.pguid;
	}

	public void setPguid(String pguid) {
		this.pguid = pguid;
	}

	@Column(name = "MBX_PROFILE_STATUS", nullable = false, length = 128)
	public String getMbxProfileStatus() {
		return this.mbxProfileStatus;
	}

	public void setMbxProfileStatus(String mbxProfileStatus) {
		this.mbxProfileStatus = mbxProfileStatus;
	}

	// bi-directional many-to-one association to MailBox
	@ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY)
	@JoinColumn(name = "MAILBOX_PROFILE_GUID", nullable = false)
	public MailBox getMailbox() {
		return this.mailbox;
	}

	public void setMailbox(MailBox mailbox) {
		this.mailbox = mailbox;
	}

	// bi-directional many-to-one association to ScheduleProfilesRef
	@ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.REMOVE }, fetch = FetchType.LAZY)
	@JoinColumn(name = "SCHEDULE_PROFILES_REF_GUID", nullable = false)
	public ScheduleProfilesRef getScheduleProfilesRef() {
		return this.scheduleProfilesRef;
	}

	public void setScheduleProfilesRef(ScheduleProfilesRef scheduleProfilesRef) {
		this.scheduleProfilesRef = scheduleProfilesRef;
	}

	// bi-directional many-to-one association to Processor
	@OneToMany(mappedBy = "mailboxSchedProfile", cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH })
	public List<Processor> getProcessors() {
		return this.processors;
	}

	public void setProcessors(List<Processor> processors) {
		this.processors = processors;
	}

	public Processor addProcessor(Processor processor) {
		getProcessors().add(processor);
		processor.setMailboxSchedProfile(this);

		return processor;
	}

	public Processor removeProcessor(Processor processor) {
		getProcessors().remove(processor);
		processor.setMailboxSchedProfile(null);

		return processor;
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