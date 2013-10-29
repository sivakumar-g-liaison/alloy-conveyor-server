package com.liaison.mailbox.jpa.model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;


/**
 * The persistent class for the GATEWAY_TYPE database table.
 * 
 */
@Entity
@Table(name="GATEWAY_TYPE")
@NamedQuery(name="GatewayType.findAll", query="SELECT g FROM GatewayType g")
public class GatewayType implements com.liaison.commons.jpa.Identifiable {
	private static final long serialVersionUID = 1L;
	private String pguid;
	private String name;
	private IdpProfile idpProfile;

	public GatewayType() {
	}


	@Id
	public String getPguid() {
		return this.pguid;
	}

	public void setPguid(String pguid) {
		this.pguid = pguid;
	}


	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}


	//bi-directional one-to-one association to IdpProfile
	@OneToOne(mappedBy = "gatewayType", fetch = FetchType.EAGER, orphanRemoval = true, cascade = { CascadeType.PERSIST,
			CascadeType.MERGE, CascadeType.REMOVE, CascadeType.REFRESH })
	public IdpProfile getIdpProfile() {
		return this.idpProfile;
	}

	public void setIdpProfile(IdpProfile idpProfile) {
		this.idpProfile = idpProfile;
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