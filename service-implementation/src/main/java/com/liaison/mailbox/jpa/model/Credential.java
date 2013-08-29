package com.liaison.mailbox.jpa.model;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.liaison.commons.jpa.Identifiable;


/**
 * The persistent class for the CREDENTIALS database table.
 * 
 */
@Entity
@Table(name="CREDENTIALS")
@NamedQuery(name="Credential.findAll", query="SELECT c FROM Credential c")
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

	public Credential() {
	}

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(unique=true, nullable=false, length=32)
	public String getPguid() {
		return this.pguid;
	}

	public void setPguid(String pguid) {
		this.pguid = pguid;
	}

	@Column(name="CREDS_IDP_TYPE", length=128)
	public String getCredsIdpType() {
		return this.credsIdpType;
	}

	public void setCredsIdpType(String credsIdpType) {
		this.credsIdpType = credsIdpType;
	}

	@Column(name="CREDS_IDP_URI", length=128)
	public String getCredsIdpUri() {
		return this.credsIdpUri;
	}

	public void setCredsIdpUri(String credsIdpUri) {
		this.credsIdpUri = credsIdpUri;
	}

	@Column(name="CREDS_PASSWORD", length=128)
	public String getCredsPassword() {
		return this.credsPassword;
	}

	public void setCredsPassword(String credsPassword) {
		this.credsPassword = credsPassword;
	}

	@Column(name="CREDS_TYPE", nullable=false, length=128)
	public String getCredsType() {
		return this.credsType;
	}

	public void setCredsType(String credsType) {
		this.credsType = credsType;
	}

	@Column(name="CREDS_URI", length=128)
	public String getCredsUri() {
		return this.credsUri;
	}

	public void setCredsUri(String credsUri) {
		this.credsUri = credsUri;
	}

	@Column(name="CREDS_USERNAME", length=128)
	public String getCredsUsername() {
		return this.credsUsername;
	}

	public void setCredsUsername(String credsUsername) {
		this.credsUsername = credsUsername;
	}

	//bi-directional many-to-one association to Processor
	@OneToOne(cascade={CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE, CascadeType.REFRESH}, fetch=FetchType.LAZY)
	@JoinColumn(name="PROCESSORS_GUID", nullable=false)
	public Processor getProcessor() {
		return this.processor;
	}

	public void setProcessor(Processor processor) {
		this.processor = processor;
	}

	@Override
	@Transient
	public Object getPrimaryKey() {
		return (Object) getPguid();
	}


	@Override
	@Transient
	public  Class getEntityClass() {
		return this.getClass();
	}

}