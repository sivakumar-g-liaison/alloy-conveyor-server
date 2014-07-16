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
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.liaison.commons.jpa.Identifiable;

/**
 * The persistent class for the MAILBOXES database table.
 * 
 *  @author OFS
 */
@Entity
@Table(name = "MAILBOX")
@NamedQuery(name = "MailBox.findAll", query = "SELECT m FROM MailBox m")
public class MailBox implements Identifiable {

	private static final long serialVersionUID = 1L;

	private String pguid;
	private String mbxDesc;
	private String mbxName;
	private String mbxStatus;
	private String shardKey;
	private List<MailBoxProperty> mailboxProperties;
	private List<Processor> mailboxProcessors;
	private List<MailboxServiceInstance> mailboxServiceInstances;
	private String tenancyKey;
	

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

	@Column(name = "DESCRIPTION", length = 1024)
	public String getMbxDesc() {
		return this.mbxDesc;
	}

	public void setMbxDesc(String mbxDesc) {
		this.mbxDesc = mbxDesc;
	}

	@Column(name = "NAME", nullable = false, length = 128)
	public String getMbxName() {
		return this.mbxName;
	}

	public void setMbxName(String mbxName) {
		this.mbxName = mbxName;
	}

	@Column(name = "STATUS", nullable = false, length = 128)
	public String getMbxStatus() {
		return this.mbxStatus;
	}

	public void setMbxStatus(String mbxStatus) {
		this.mbxStatus = mbxStatus;
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

	// bi-directional many-to-one association to MailBoxProcessor
	@OneToMany(mappedBy = "mailbox", orphanRemoval = true, cascade = { CascadeType.PERSIST, CascadeType.MERGE,
			CascadeType.REMOVE, CascadeType.REFRESH })
	public List<Processor> getMailboxProcessors() {
		return this.mailboxProcessors;
	}

	public void setMailboxProcessors(List<Processor> mailboxProcessors) {
		this.mailboxProcessors = mailboxProcessors;
	}
	
	@OneToMany(mappedBy = "mailbox", orphanRemoval = true, cascade = { CascadeType.PERSIST,
			CascadeType.MERGE, CascadeType.REMOVE, CascadeType.REFRESH }, fetch = FetchType.EAGER)
	public List<MailboxServiceInstance> getMailboxServiceInstances() {
		return mailboxServiceInstances;
	}

	public void setMailboxServiceInstances(List<MailboxServiceInstance> mailboxServiceInstances) {
		this.mailboxServiceInstances = mailboxServiceInstances;
	}
	
	@Column(name = "TENANCY_KEY", nullable = false, length = 32)
	public String getTenancyKey() {
		return tenancyKey;
	}

	public void setTenancyKey(String tenancyKey) {
		this.tenancyKey = tenancyKey;
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

	@Override
	public int hashCode() {

		final int prime = 31;
		int result = 1;
		result = prime * result + ((pguid == null) ? 0 : pguid.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {

		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof MailBox)) {
			return false;
		}
		MailBox other = (MailBox) obj;
		if (pguid == null) {
			if (other.pguid != null) {
				return false;
			}
		} else if (!pguid.equals(other.pguid)) {
			return false;
		}
		return true;
	}

}