package com.liaison.mailbox.jpa.model;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.liaison.commons.jpa.Identifiable;


/**
 * The persistent class for the PROCESSORS database table.
 * 
 */
@Entity
@Table(name="PROCESSORS")
@NamedQuery(name="Processor.findAll", query="SELECT p FROM Processor p")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="PROCSR_TYPE")
public abstract class Processor implements Identifiable {

	private static final long serialVersionUID = 1L;

	private String pguid;
	private String javaScriptUri;
	private String procsrDesc;
	private String procsrProperties;
	private String procsrStatus;
	private String procsrType;

	private Credential credentials;
	private Folder folders;
	private MailboxSchedProfile mailboxSchedProfile;

	public Processor() {
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


	@Column(name="JAVA_SCRIPT_URI", length=512)
	public String getJavaScriptUri() {
		return this.javaScriptUri;
	}

	public void setJavaScriptUri(String javaScriptUri) {
		this.javaScriptUri = javaScriptUri;
	}


	@Column(name="PROCSR_DESC", length=512)
	public String getProcsrDesc() {
		return this.procsrDesc;
	}

	public void setProcsrDesc(String procsrDesc) {
		this.procsrDesc = procsrDesc;
	}


	@Column(name="PROCSR_PROPERTIES", length=2048)
	public String getProcsrProperties() {
		return this.procsrProperties;
	}

	public void setProcsrProperties(String procsrProperties) {
		this.procsrProperties = procsrProperties;
	}


	@Column(name="PROCSR_STATUS", nullable=false, length=128)
	public String getProcsrStatus() {
		return this.procsrStatus;
	}

	public void setProcsrStatus(String procsrStatus) {
		this.procsrStatus = procsrStatus;
	}


	@Column(name="PROCSR_TYPE", nullable=false, length=128)
	public String getProcsrType() {
		return this.procsrType;
	}

	public void setProcsrType(String procsrType) {
		this.procsrType = procsrType;
	}


	//bi-directional many-to-one association to Credential
	@OneToOne(mappedBy="processor", cascade={CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE, CascadeType.REFRESH})
	public Credential getCredentials() {
		return this.credentials;
	}

	public void setCredentials(Credential credentials) {
		this.credentials = credentials;
	}

	//bi-directional one-to-one association to Folder
	@OneToOne(mappedBy="processor", cascade={CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE, CascadeType.REFRESH})
	public Folder getFolders() {
		return this.folders;
	}

	public void setFolders(Folder folders) {
		this.folders = folders;
	}

	//bi-directional many-to-one association to MailboxSchedProfile
	@ManyToOne(cascade={CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE, CascadeType.REFRESH}, fetch=FetchType.LAZY)
	@JoinColumn(name="MAILBOX_SCHED_PROFILES_GUID", nullable=false)
	public MailboxSchedProfile getMailboxSchedProfile() {
		return this.mailboxSchedProfile;
	}

	public void setMailboxSchedProfile(MailboxSchedProfile mailboxSchedProfile) {
		this.mailboxSchedProfile = mailboxSchedProfile;
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