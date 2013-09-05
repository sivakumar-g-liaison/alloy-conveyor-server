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
 */
@Entity
@Table(name = "SCHEDULE_PROFILES_REF")
@NamedQuery(name = "ScheduleProfilesRef.findAll", query = "SELECT s FROM ScheduleProfilesRef s")
public class ScheduleProfilesRef implements Identifiable {

	private static final long serialVersionUID = 1L;

	private String pguid;
	private String schProfName;
	private List<MailBoxSchedProfile> mailboxSchedProfiles;

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

	@Column(name = "SCH_PROF_NAME", nullable = false, length = 128)
	public String getSchProfName() {
		return this.schProfName;
	}

	public void setSchProfName(String schProfName) {
		this.schProfName = schProfName;
	}

	// bi-directional many-to-one association to MailBoxSchedProfile
	@OneToMany(mappedBy = "scheduleProfilesRef", cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE,
			CascadeType.REFRESH })
	public List<MailBoxSchedProfile> getMailboxSchedProfiles() {
		return this.mailboxSchedProfiles;
	}

	public void setMailboxSchedProfiles(List<MailBoxSchedProfile> mailboxSchedProfiles) {
		this.mailboxSchedProfiles = mailboxSchedProfiles;
	}

	public MailBoxSchedProfile addMailboxSchedProfile(MailBoxSchedProfile mailboxSchedProfile) {
		getMailboxSchedProfiles().add(mailboxSchedProfile);
		mailboxSchedProfile.setScheduleProfilesRef(this);

		return mailboxSchedProfile;
	}

	public MailBoxSchedProfile removeMailboxSchedProfile(MailBoxSchedProfile mailboxSchedProfile) {
		getMailboxSchedProfiles().remove(mailboxSchedProfile);
		mailboxSchedProfile.setScheduleProfilesRef(null);

		return mailboxSchedProfile;
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