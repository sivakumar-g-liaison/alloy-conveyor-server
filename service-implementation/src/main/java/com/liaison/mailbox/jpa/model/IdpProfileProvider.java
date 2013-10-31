package com.liaison.mailbox.jpa.model;

import javax.persistence.CascadeType;
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
 * The persistent class for the SCHEDULE_PROFILE_PROCESSORS database table.
 * 
 */
@Entity
@Table(name = "IDP_PROFILE_PROVIDER")
public class IdpProfileProvider implements Identifiable {

	private static final long serialVersionUID = 1L;
	private String pguid;
	private IdpProvider idpProvider;
	private IdpProfile idpProfile;
	
	@Id
	@Column(unique = true, nullable = false, length = 32)
	public String getPguid() {
		return this.pguid;
	}

	public void setPguid(String pguid) {
		this.pguid = pguid;
	}

	
	// bi-directional many-to-one association to Processor
	@ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.EAGER)
	@JoinColumn(name = "IDP_PROVIDER_GUID", nullable = false)
	public IdpProvider getIdpProvider() {
		return idpProvider;
	}

	public void setIdpProvider(IdpProvider idpProvider) {
		this.idpProvider = idpProvider;
	}

	// bi-directional many-to-one association to ScheduleProfilesRef
	@ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.EAGER)
	@JoinColumn(name = "IDP_PROFILE_GUID", nullable = false)
	public IdpProfile getIdpProfile() {
		return idpProfile;
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