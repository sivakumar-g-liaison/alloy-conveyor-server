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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.liaison.commons.jpa.Identifiable;


/**
 * The persistent class for the IDP_PROFILE database table.
 * 
 */
@Entity
@Table(name="IDP_PROFILE")
@NamedQuery(name="IdpProfile.findAll", query="SELECT i FROM IdpProfile i")
public class IdpProfile implements Identifiable {

	private static final long serialVersionUID = 1L;
	private String pguid;
	private String loginDomain;
	private GatewayType gatewayType;	
	private IdpProvider idpProvider;

	public IdpProfile() {
	}

	@Id
	public String getPguid() {
		return this.pguid;
	}

	public void setPguid(String pguid) {
		this.pguid = pguid;
	}

	@Column(name="LOGIN_DOMAIN")
	public String getLoginDomain() {
		return this.loginDomain;
	}

	public void setLoginDomain(String loginDomain) {
		this.loginDomain = loginDomain;
	}

	//bi-directional one-to-one association to GatewayType
	@OneToOne
	@Column(name="GATEWAY_TYPE_GUID")
	public GatewayType getGatewayType() {
		return this.gatewayType;
	}

	public void setGatewayType(GatewayType gatewayType) {
		this.gatewayType = gatewayType;
	}
	
	@OneToOne
    @JoinColumn(name="IDP_TYPE_GUID")
	public IdpProvider getIdpProvider() {
		return this.idpProvider;
	}

	public void setIdpProvider(IdpProvider idpProvider) {
		this.idpProvider = idpProvider;
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