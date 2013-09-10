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

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.liaison.commons.jpa.Identifiable;

/**
 * The persistent class for the PROCESSORS database table.
 * 
 */
@Entity
@Table(name = "PROCESSORS")
@NamedQuery(name = "Processor.findAll", query = "SELECT p FROM Processor p")
@DiscriminatorColumn(name = "PROCSR_TYPE", discriminatorType = DiscriminatorType.STRING, length = 128)
public class Processor implements Identifiable {

	private static final long serialVersionUID = 1L;

	private String pguid;
	private String javaScriptUri;
	private String procsrDesc;
	private String procsrProperties;
	private String procsrStatus;
	// private String procsrType;
	private List<Credential> credentials;
	private List<Folder> folders;
	private MailBoxSchedProfile mailboxSchedProfile;
	
	private List<ProcessorProperty> processorProperties;

	public Processor() {
	}

	// bi-directional many-to-one association to ProcessorProperty
	@OneToMany(mappedBy = "processor", fetch = FetchType.EAGER, orphanRemoval = true, cascade = { CascadeType.PERSIST,
			CascadeType.MERGE, CascadeType.REMOVE, CascadeType.REFRESH })
	public List<ProcessorProperty> getProcessorProperties() {
		return processorProperties;
	}

	public void setProcessorProperties(List<ProcessorProperty> processorProperties) {
		this.processorProperties = processorProperties;
	}

	@Id
	@Column(unique = true, nullable = false, length = 32)
	public String getPguid() {
		return this.pguid;
	}

	public void setPguid(String pguid) {
		this.pguid = pguid;
	}

	@Column(name = "JAVA_SCRIPT_URI", length = 512)
	public String getJavaScriptUri() {
		return this.javaScriptUri;
	}

	public void setJavaScriptUri(String javaScriptUri) {
		this.javaScriptUri = javaScriptUri;
	}

	@Column(name = "PROCSR_DESC", length = 512)
	public String getProcsrDesc() {
		return this.procsrDesc;
	}

	public void setProcsrDesc(String procsrDesc) {
		this.procsrDesc = procsrDesc;
	}

	@Column(name = "PROCSR_PROPERTIES", length = 2048)
	public String getProcsrProperties() {
		return this.procsrProperties;
	}

	public void setProcsrProperties(String procsrProperties) {
		this.procsrProperties = procsrProperties;
	}

	@Column(name = "PROCSR_STATUS", nullable = false, length = 128)
	public String getProcsrStatus() {
		return this.procsrStatus;
	}

	public void setProcsrStatus(String procsrStatus) {
		this.procsrStatus = procsrStatus;
	}

	/*
	 * @Column(name="PROCSR_TYPE", nullable=false, length=128) public String getProcsrType() {
	 * return this.procsrType; } public void setProcsrType(String procsrType) { this.procsrType =
	 * procsrType; }
	 */

	// bi-directional many-to-one association to Credential
	@OneToMany(mappedBy = "processor", fetch = FetchType.EAGER, orphanRemoval=true, cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE,
			CascadeType.REFRESH })
	public List<Credential> getCredentials() {
		return this.credentials;
	}

	public void setCredentials(List<Credential> credentials) {
		this.credentials = credentials;
	}

	public Credential addCredential(Credential credential) {
		getCredentials().add(credential);
		credential.setProcessor(this);

		return credential;
	}

	public Credential removeCredential(Credential credential) {
		getCredentials().remove(credential);
		credential.setProcessor(null);

		return credential;
	}

	// bi-directional many-to-one association to Folder
	@OneToMany(mappedBy = "processor", fetch = FetchType.EAGER, orphanRemoval=true, cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE,
			CascadeType.REFRESH })
	public List<Folder> getFolders() {
		return this.folders;
	}

	public void setFolders(List<Folder> folders) {
		this.folders = folders;
	}

	public Folder addFolder(Folder folder) {
		getFolders().add(folder);
		folder.setProcessor(this);

		return folder;
	}

	public Folder removeFolder(Folder folder) {
		getFolders().remove(folder);
		folder.setProcessor(null);

		return folder;
	}

	// bi-directional many-to-one association to MailBoxSchedProfile
	@ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE, CascadeType.REFRESH }, fetch = FetchType.LAZY)
	@JoinColumn(name = "MAILBOX_SCHED_PROFILES_GUID", nullable = false)
	public MailBoxSchedProfile getMailboxSchedProfile() {
		return this.mailboxSchedProfile;
	}

	public void setMailboxSchedProfile(MailBoxSchedProfile mailboxSchedProfile) {
		this.mailboxSchedProfile = mailboxSchedProfile;
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