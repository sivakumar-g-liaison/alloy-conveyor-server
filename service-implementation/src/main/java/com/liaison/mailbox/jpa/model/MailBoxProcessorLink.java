package com.liaison.mailbox.jpa.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.liaison.commons.jpa.Identifiable;

/**
 * The persistent class for the MAILBOX_PROCESSORS database table.
 * 
 */
@Entity
@Table(name = "MAILBOX_PROCESSORS")
public class MailBoxProcessorLink implements Identifiable {

	private static final long serialVersionUID = 1L;

	private String pguid;
	private MailBox mailbox;
	private Processor processor;

	public MailBoxProcessorLink() {
	}

	@Id
	@Column(unique = true, nullable = false, length = 32)
	public String getPguid() {
		return this.pguid;
	}

	public void setPguid(String pguid) {
		this.pguid = pguid;
	}

	// bi-directional many-to-one association to MailBox
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "MAILBOX_GUID", nullable = false)
	public MailBox getMailbox() {
		return this.mailbox;
	}

	public void setMailbox(MailBox mailbox) {
		this.mailbox = mailbox;
	}

	// bi-directional many-to-one association to Processor
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "PROCESSOR_GUID", nullable = false)
	public Processor getProcessor() {
		return this.processor;
	}

	public void setProcessor(Processor processor) {
		this.processor = processor;
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