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
 * The persistent class for the PROCESSORS database table.
 * 
 */
@Entity
@Table(name="PROCESSORS")
@NamedQuery(name="ProcessorForMerge.findAll", query="SELECT p FROM ProcessorForMerge p")
public class ProcessorForMerge implements Identifiable {
	
	private static final long serialVersionUID = 1L;
	
	private String pguid;
	private int procsrExecutionOrder;
	private MailBoxSchedProfile mailboxSchedProfile;

	public ProcessorForMerge() {
	}


	@Id
	@Column(unique=true, nullable=false, length=32)
	public String getPguid() {
		return this.pguid;
	}

	public void setPguid(String pguid) {
		this.pguid = pguid;
	}

	@Column(name="PROCSR_EXECUTION_ORDER")
	public int getProcsrExecutionOrder() {
		return this.procsrExecutionOrder;
	}

	public void setProcsrExecutionOrder(int procsrExecutionOrder) {
		this.procsrExecutionOrder = procsrExecutionOrder;
	}

	// bi-directional many-to-one association to MailBoxSchedProfile
	@ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE, CascadeType.REFRESH }, fetch = FetchType.EAGER)
	@JoinColumn(name = "MAILBOX_SCHED_PROFILES_GUID", nullable = false)
	public MailBoxSchedProfile getMailboxSchedProfile() {
		return this.mailboxSchedProfile;
	}

	public void setMailboxSchedProfile(MailBoxSchedProfile mailboxSchedProfile) {
		this.mailboxSchedProfile = mailboxSchedProfile;
	}

	@Override
	@Transient
	public Object getPrimaryKey() {
		// TODO Auto-generated method stub
		return getPguid();
	}


	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	@Transient
	public Class getEntityClass() {
		return this.getClass();
	}

}