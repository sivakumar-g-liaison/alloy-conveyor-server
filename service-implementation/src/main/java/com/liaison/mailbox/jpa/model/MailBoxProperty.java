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
 * The persistent class for the MAILBOX_PROPERTIES database table.
 * 
 */
@Entity
@Table(name = "MAILBOX_PROPERTIES")
@NamedQuery(name = "MailBoxProperty.findAll", query = "SELECT m FROM MailBoxProperty m")
public class MailBoxProperty implements Identifiable {

	private static final long serialVersionUID = 1L;

	private String pguid;
	private String mbxPropName;
	private String mbxPropValue;
	private MailBox mailbox;

	public MailBoxProperty() {
	}

	@Id
	@Column(unique = true, nullable = false, length = 32)
	public String getPguid() {
		return this.pguid;
	}

	public void setPguid(String pguid) {
		this.pguid = pguid;
	}

	@Column(name = "MBX_PROP_NAME", length = 128)
	public String getMbxPropName() {
		return this.mbxPropName;
	}

	public void setMbxPropName(String mbxPropName) {
		this.mbxPropName = mbxPropName;
	}

	@Column(name = "MBX_PROP_VALUE", length = 512)
	public String getMbxPropValue() {
		return this.mbxPropValue;
	}

	public void setMbxPropValue(String mbxPropValue) {
		this.mbxPropValue = mbxPropValue;
	}

	// bi-directional many-to-one association to MailBox
	@ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE, CascadeType.REFRESH }, fetch = FetchType.LAZY)
	@JoinColumn(name = "MAILBOX_GUID", nullable = false)
	public MailBox getMailbox() {
		return this.mailbox;
	}

	public void setMailbox(MailBox mailbox) {
		this.mailbox = mailbox;
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