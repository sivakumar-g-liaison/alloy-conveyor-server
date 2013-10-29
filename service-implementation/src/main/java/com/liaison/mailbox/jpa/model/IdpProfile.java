package com.liaison.mailbox.jpa.model;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;


/**
 * The persistent class for the IDP_PROFILE database table.
 * 
 */
@Entity
@Table(name="IDP_PROFILE")
@NamedQuery(name="IdpProfile.findAll", query="SELECT i FROM IdpProfile i")
public class IdpProfile implements com.liaison.commons.jpa.Identifiable {
	private static final long serialVersionUID = 1L;
	private String pguid;
	private String accountGuid;
	private String gatewayTypeGuid;
	private String idpTypeGuid;
	private String loginDomain;
	private Account account;
	private GatewayType gatewayType;
	private List<IdpProvider> idpProvider;

	public IdpProfile() {
	}


	@Id
	public String getPguid() {
		return this.pguid;
	}

	public void setPguid(String pguid) {
		this.pguid = pguid;
	}


	@Column(name="ACCOUNT_GUID")
	public String getAccountGuid() {
		return this.accountGuid;
	}

	public void setAccountGuid(String accountGuid) {
		this.accountGuid = accountGuid;
	}


	@Column(name="GATEWAY_TYPE_GUID")
	public String getGatewayTypeGuid() {
		return this.gatewayTypeGuid;
	}

	public void setGatewayTypeGuid(String gatewayTypeGuid) {
		this.gatewayTypeGuid = gatewayTypeGuid;
	}


	@Column(name="IDP_TYPE_GUID")
	public String getIdpTypeGuid() {
		return this.idpTypeGuid;
	}

	public void setIdpTypeGuid(String idpTypeGuid) {
		this.idpTypeGuid = idpTypeGuid;
	}


	@Column(name="LOGIN_DOMAIN")
	public String getLoginDomain() {
		return this.loginDomain;
	}

	public void setLoginDomain(String loginDomain) {
		this.loginDomain = loginDomain;
	}


	//bi-directional one-to-one association to Account
	@OneToOne(mappedBy = "idpProfile", fetch = FetchType.EAGER, orphanRemoval = true, cascade = { CascadeType.PERSIST,
			CascadeType.MERGE, CascadeType.REMOVE, CascadeType.REFRESH })
	@JoinColumn(name="PGUID")
	public Account getAccount() {
		return this.account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}


	//bi-directional one-to-one association to GatewayType
	@OneToOne(mappedBy = "idpProfile", fetch = FetchType.EAGER, orphanRemoval = true, cascade = { CascadeType.PERSIST,
			CascadeType.MERGE, CascadeType.REMOVE, CascadeType.REFRESH })
	@JoinColumn(name="PGUID")
	public GatewayType getGatewayType() {
		return this.gatewayType;
	}

	public void setGatewayType(GatewayType gatewayType) {
		this.gatewayType = gatewayType;
	}


	//bi-directional one-to-one association to IdpProvider
	@OneToOne(mappedBy = "idpProfile", fetch = FetchType.EAGER, orphanRemoval = true, cascade = { CascadeType.PERSIST,
			CascadeType.MERGE, CascadeType.REMOVE, CascadeType.REFRESH })
	@JoinColumn(name="PGUID")
	public List<IdpProvider> getIdpProvider() {
		return this.idpProvider;
	}

	public void setIdpProvider(List<IdpProvider> idpProvider) {
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