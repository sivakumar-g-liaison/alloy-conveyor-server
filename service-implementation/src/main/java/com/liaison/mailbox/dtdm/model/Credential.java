/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall 
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.dtdm.model;

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
 * The persistent class for the CREDENTIALS database table.
 * 
 *  @author OFS
 */
@Entity
@Table(name = "CREDENTIAL")
@NamedQuery(name = "Credential.findAll", query = "SELECT c FROM Credential c")
public class Credential implements Identifiable {

	private static final long serialVersionUID = 1L;

	private String pguid;
	private String credsIdpType;
	private String credsIdpUri;
	private String credsPassword;
	private String credsType;
	private String credsUri;
	private String credsUsername;
	private Processor processor;
	private String originatingDc;
	
	
	public Credential() {
	}

	@Id
	@Column(unique = true, nullable = false, length = 32)
	public String getPguid() {
		return this.pguid;
	}

	public void setPguid(String pguid) {
		this.pguid = pguid;
	}

	@Column(name = "IDP_TYPE", length = 128)
	public String getCredsIdpType() {
		return this.credsIdpType;
	}

	public void setCredsIdpType(String credsIdpType) {
		this.credsIdpType = credsIdpType;
	}

	@Column(name = "IDP_URI", length = 128)
	public String getCredsIdpUri() {
		return this.credsIdpUri;
	}

	public void setCredsIdpUri(String credsIdpUri) {
		this.credsIdpUri = credsIdpUri;
	}

	@Column(name = "PASSWORD", length = 128)
	public String getCredsPassword() {
		return this.credsPassword;
	}

	public void setCredsPassword(String credsPassword) {
		this.credsPassword = credsPassword;
	}

	@Column(name = "TYPE", nullable = false, length = 128)
	public String getCredsType() {
		return this.credsType;
	}

	public void setCredsType(String credsType) {
		this.credsType = credsType;
	}

	@Column(name = "URI_GUID", length = 128)
	public String getCredsUri() {
		return this.credsUri;
	}

	public void setCredsUri(String credsUri) {
		this.credsUri = credsUri;
	}

	@Column(name = "USERNAME", length = 128)
	public String getCredsUsername() {
		return this.credsUsername;
	}

	public void setCredsUsername(String credsUsername) {
		this.credsUsername = credsUsername;
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
	
	@Column(name = "ORIGINATING_DC", length = 16)
	public String getOriginatingDc() {
		return originatingDc;
	}

	public void setOriginatingDc(String originatingDc) {
		this.originatingDc = originatingDc;
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