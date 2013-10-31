package com.liaison.mailbox.jpa.model;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
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
	private Account account;
	
	private List<IdpProfileProvider> idpProfileProvider;

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

	@OneToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY)
	@JoinColumn(name = "ACCOUNT_GUID", nullable = false)
	public Account getAccount() {
		return account;
	}


	public void setAccount(Account account) {
		this.account = account;
	}
	
	// bi-directional many-to-one association to ScheduleProfileProcessor
	@OneToMany(mappedBy = "idpProfile", cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE,
				CascadeType.REFRESH })
	public List<IdpProfileProvider> getIdpProfileProvider() {
		return this.idpProfileProvider;
	}

	public void setIdpProfileProvider(List<IdpProfileProvider> idpProfileProvider) {
		this.idpProfileProvider = idpProfileProvider;
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