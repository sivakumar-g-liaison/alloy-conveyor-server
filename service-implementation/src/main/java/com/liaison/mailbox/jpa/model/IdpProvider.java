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
 * The persistent class for the IDP_PROVIDER database table.
 * 
 */
@Entity
@Table(name="IDP_PROVIDER")
@NamedQuery(name="IdpProvider.findAll", query="SELECT i FROM IdpProvider i")
public class IdpProvider implements Identifiable {

	private static final long serialVersionUID = 1L;
	private String pguid;
	private String idpProviderUri;
	private String name;
	private String providerDefStorage;
	private String pswdResetPolicyUri;
	
	private List<IdpProfileProvider> idpProfileProvider;
	
	public IdpProvider() {
	}


	@Id
	public String getPguid() {
		return this.pguid;
	}

	public void setPguid(String pguid) {
		this.pguid = pguid;
	}


	@Column(name="IDP_PROVIDER_URI")
	public String getIdpProviderUri() {
		return this.idpProviderUri;
	}

	public void setIdpProviderUri(String idpProviderUri) {
		this.idpProviderUri = idpProviderUri;
	}


	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}


	@Column(name="PROVIDER_DEF_STORAGE")
	public String getProviderDefStorage() {
		return this.providerDefStorage;
	}

	public void setProviderDefStorage(String providerDefStorage) {
		this.providerDefStorage = providerDefStorage;
	}


	@Column(name="PSWD_RESET_POLICY_URI")
	public String getPswdResetPolicyUri() {
		return this.pswdResetPolicyUri;
	}

	public void setPswdResetPolicyUri(String pswdResetPolicyUri) {
		this.pswdResetPolicyUri = pswdResetPolicyUri;
	}
	
	// bi-directional many-to-one association to ScheduleProfileProcessor
	@OneToMany(mappedBy = "idpProvider", fetch = FetchType.EAGER, orphanRemoval = true, cascade = { CascadeType.PERSIST,
			CascadeType.MERGE, CascadeType.REMOVE, CascadeType.REFRESH })
	public List<IdpProfileProvider> getIdpProfileProvider() {
		return this.idpProfileProvider;
	}

	public void setIdpProfileProvider(List<IdpProfileProvider> IdpProfileProvider) {
		this.idpProfileProvider = IdpProfileProvider;
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