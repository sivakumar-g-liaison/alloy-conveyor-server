package com.liaison.mailbox.jpa.model;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
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
@Table(name="SCHEDULE_PROFILES_REF")
@NamedQuery(name="ScheduleProfilesRef.findAll", query="SELECT s FROM ScheduleProfilesRef s")
public class ScheduleProfilesRef implements Identifiable {

	private static final long serialVersionUID = 1L;

	private String pguid;
	private String schProfName;
	private List<MailboxSchedProfile> mailboxSchedProfiles;

	public ScheduleProfilesRef() {
	}

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(unique=true, nullable=false, length=32)
	public String getPguid() {
		return this.pguid;
	}

	public void setPguid(String pguid) {
		this.pguid = pguid;
	}

	@Column(name="SCH_PROF_NAME", nullable=false, length=128)
	public String getSchProfName() {
		return this.schProfName;
	}

	public void setSchProfName(String schProfName) {
		this.schProfName = schProfName;
	}

	//bi-directional many-to-one association to MailboxSchedProfile
	@OneToMany(mappedBy="scheduleProfilesRef", cascade={CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE, CascadeType.REFRESH})
	public List<MailboxSchedProfile> getMailboxSchedProfiles() {
		return this.mailboxSchedProfiles;
	}

	public void setMailboxSchedProfiles(List<MailboxSchedProfile> mailboxSchedProfiles) {
		this.mailboxSchedProfiles = mailboxSchedProfiles;
	}

	public MailboxSchedProfile addMailboxSchedProfile(MailboxSchedProfile mailboxSchedProfile) {
		getMailboxSchedProfiles().add(mailboxSchedProfile);
		mailboxSchedProfile.setScheduleProfilesRef(this);

		return mailboxSchedProfile;
	}

	public MailboxSchedProfile removeMailboxSchedProfile(MailboxSchedProfile mailboxSchedProfile) {
		getMailboxSchedProfiles().remove(mailboxSchedProfile);
		mailboxSchedProfile.setScheduleProfilesRef(null);

		return mailboxSchedProfile;
	}

	@Override
	@Transient
	public Object getPrimaryKey() {
		return (Object) getPguid();
	}


	@Override
	@Transient
	public  Class getEntityClass() {
		return this.getClass();
	}

}