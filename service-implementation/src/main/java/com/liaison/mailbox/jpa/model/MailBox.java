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

import java.math.BigDecimal;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.liaison.commons.jpa.Identifiable;

/**
 * The persistent class for the MAILBOXES database table.
 * 
 */
@Entity
@Table(name = "MAILBOXES")
@NamedQuery(name = "MailBox.findAll", query = "SELECT m FROM MailBox m")
public class MailBox implements Identifiable {

	private static final long serialVersionUID = 1L;

	private String pguid;
	private String mbxDesc;
	private String mbxName;
	private String mbxStatus;
	private BigDecimal serviceInstId;
	private String shardKey;
	private List<MailBoxProperty> mailboxProperties;
	private List<MailBoxSchedProfile> mailboxSchedProfiles;

	public MailBox() {
	}

	@Id
	@Column(unique = true, nullable = false, length = 32)
	public String getPguid() {
		return this.pguid;
	}

	public void setPguid(String pguid) {
		this.pguid = pguid;
	}

	@Column(name = "MBX_DESC", length = 1024)
	public String getMbxDesc() {
		return this.mbxDesc;
	}

	public void setMbxDesc(String mbxDesc) {
		this.mbxDesc = mbxDesc;
	}

	@Column(name = "MBX_NAME", nullable = false, length = 128)
	public String getMbxName() {
		return this.mbxName;
	}

	public void setMbxName(String mbxName) {
		this.mbxName = mbxName;
	}

	@Column(name = "MBX_STATUS", nullable = false, length = 128)
	public String getMbxStatus() {
		return this.mbxStatus;
	}

	public void setMbxStatus(String mbxStatus) {
		this.mbxStatus = mbxStatus;
	}

	@Column(name = "SERVICE_INST_ID")
	public BigDecimal getServiceInstId() {
		return this.serviceInstId;
	}

	public void setServiceInstId(BigDecimal serviceInstId) {
		this.serviceInstId = serviceInstId;
	}

	@Column(name = "SHARD_KEY", length = 512)
	public String getShardKey() {
		return this.shardKey;
	}

	public void setShardKey(String shardKey) {
		this.shardKey = shardKey;
	}

	// bi-directional many-to-one association to MailBoxProperty
	@OneToMany(mappedBy = "mailbox", fetch = FetchType.EAGER, orphanRemoval = true, cascade = { CascadeType.PERSIST,
			CascadeType.MERGE, CascadeType.REMOVE, CascadeType.REFRESH })
	public List<MailBoxProperty> getMailboxProperties() {
		return this.mailboxProperties;
	}

	public void setMailboxProperties(List<MailBoxProperty> mailboxProperties) {
		this.mailboxProperties = mailboxProperties;
	}

	public MailBoxProperty addMailboxProperty(MailBoxProperty mailboxProperty) {
		getMailboxProperties().add(mailboxProperty);
		mailboxProperty.setMailbox(this);

		return mailboxProperty;
	}

	public MailBoxProperty removeMailboxProperty(MailBoxProperty mailboxProperty) {
		getMailboxProperties().remove(mailboxProperty);
		mailboxProperty.setMailbox(null);

		return mailboxProperty;
	}

	// bi-directional many-to-one association to MailBoxSchedProfile
	@OneToMany(mappedBy = "mailbox", cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE, CascadeType.REFRESH },
			fetch = FetchType.EAGER)
	public List<MailBoxSchedProfile> getMailboxSchedProfiles() {
		return this.mailboxSchedProfiles;
	}

	public void setMailboxSchedProfiles(List<MailBoxSchedProfile> mailboxSchedProfiles) {
		this.mailboxSchedProfiles = mailboxSchedProfiles;
	}

	public MailBoxSchedProfile addMailboxSchedProfile(MailBoxSchedProfile mailboxSchedProfile) {
		getMailboxSchedProfiles().add(mailboxSchedProfile);
		mailboxSchedProfile.setMailbox(this);

		return mailboxSchedProfile;
	}

	public MailBoxSchedProfile removeMailboxSchedProfile(MailBoxSchedProfile mailboxSchedProfile) {
		getMailboxSchedProfiles().remove(mailboxSchedProfile);
		mailboxSchedProfile.setMailbox(null);

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