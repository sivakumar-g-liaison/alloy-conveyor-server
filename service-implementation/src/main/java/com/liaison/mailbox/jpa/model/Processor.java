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

import java.util.Arrays;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
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
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.enums.ProcessorType;

/**
 * The persistent class for the PROCESSORS database table.
 * 
 */
@Entity
@Table(name = "PROCESSOR")
@NamedQuery(name = "Processor.findAll", query = "SELECT p FROM Processor p")
@DiscriminatorColumn(name = "TYPE", discriminatorType = DiscriminatorType.STRING, length = 128)
public class Processor implements Identifiable {

	private static final long serialVersionUID = 1L;

	public static final String TYPE_REMOTEDOWNLOADER = "remotedownloader";
	public static final String TYPE_REMOTEUPLOADER = "remoteuploader";
	public static final String TYPE_SWEEPER = "sweeper";

	private String pguid;
	private String javaScriptUri;
	private String certificateUri;
	private String certificateType;
	private String procsrDesc;
	private String procsrProperties;
	private String procsrStatus;
	private String procsrExecutionStatus;
	private String procsrName;
	private String procsrProtocol;
	private List<Credential> credentials;
	private List<Folder> folders;
	private List<ProcessorProperty> dynamicProperties;

	private MailBox mailbox;
	private List<ScheduleProfileProcessor> scheduleProfileProcessors;

	public Processor() {
	}

	// bi-directional many-to-one association to ProcessorProperty
	@OneToMany(mappedBy = "processor", fetch = FetchType.EAGER, orphanRemoval = true, cascade = { CascadeType.PERSIST,
			CascadeType.MERGE, CascadeType.REMOVE, CascadeType.REFRESH })
	public List<ProcessorProperty> getDynamicProperties() {
		return dynamicProperties;
	}

	public void setDynamicProperties(List<ProcessorProperty> processorProperties) {
		this.dynamicProperties = processorProperties;
	}

	@Id
	@Column(unique = true, nullable = false, length = 32)
	public String getPguid() {
		return this.pguid;
	}

	public void setPguid(String pguid) {
		this.pguid = pguid;
	}

	@Column(name = "JAVASCRIPT_URI", length = 512)
	public String getJavaScriptUri() {
		return this.javaScriptUri;
	}

	public void setJavaScriptUri(String javaScriptUri) {
		this.javaScriptUri = javaScriptUri;
	}

	@Column(name = "CERTIFICATE_URI", length = 512)
	public String getCertificateUri() {
		return certificateUri;
	}

	public void setCertificateUri(String certificateUri) {
		this.certificateUri = certificateUri;
	}

	@Column(name = "CERTIFICATE_TYPE", length = 128)
	public String getCertificateType() {
		return certificateType;
	}

	public void setCertificateType(String certificateType) {
		this.certificateType = certificateType;
	}

	@Column(name = "DESCRIPTION", length = 512)
	public String getProcsrDesc() {
		return this.procsrDesc;
	}

	public void setProcsrDesc(String procsrDesc) {
		this.procsrDesc = procsrDesc;
	}

	@Column(name = "PROPERTIES", length = 2048)
	public String getProcsrProperties() {
		return this.procsrProperties;
	}

	public void setProcsrProperties(String procsrProperties) {
		this.procsrProperties = procsrProperties;
	}

	@Column(name = "STATUS", nullable = false, length = 128)
	public String getProcsrStatus() {
		return this.procsrStatus;
	}

	public void setProcsrStatus(String procsrStatus) {
		this.procsrStatus = procsrStatus;
	}

	@Column(name = "EXEC_STATUS", length = 128)
	public String getProcsrExecutionStatus() {
		return procsrExecutionStatus;
	}

	public void setProcsrExecutionStatus(String procsrExecutionStatus) {
		this.procsrExecutionStatus = procsrExecutionStatus;
	}

	@Column(name = "NAME", length = 512)
	public String getProcsrName() {
		return procsrName;
	}

	public void setProcsrName(String procsrName) {
		this.procsrName = procsrName;
	}

	@Column(name = "PROTOCOL", nullable = false, length = 128)
	public String getProcsrProtocol() {
		return procsrProtocol;
	}

	public void setProcsrProtocol(String procsrProtocol) {
		this.procsrProtocol = procsrProtocol;
	}

	// bi-directional many-to-one association to Credential
	@OneToMany(mappedBy = "processor", fetch = FetchType.EAGER, orphanRemoval = true, cascade = { CascadeType.PERSIST,
			CascadeType.MERGE, CascadeType.REMOVE,
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
	@OneToMany(mappedBy = "processor", fetch = FetchType.EAGER, orphanRemoval = true, cascade = { CascadeType.PERSIST,
			CascadeType.MERGE, CascadeType.REMOVE,
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

	// bi-directional many-to-one association to MailBox
	@ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.EAGER)
	@JoinColumn(name = "MAILBOX_GUID", nullable = false)
	public MailBox getMailbox() {
		return this.mailbox;
	}

	public void setMailbox(MailBox mailbox) {
		this.mailbox = mailbox;
	}

	// bi-directional many-to-one association to ScheduleProfileProcessor
	@OneToMany(mappedBy = "processor", fetch = FetchType.EAGER, orphanRemoval = true, cascade = { CascadeType.PERSIST,
			CascadeType.MERGE, CascadeType.REMOVE, CascadeType.REFRESH })
	public List<ScheduleProfileProcessor> getScheduleProfileProcessors() {
		return this.scheduleProfileProcessors;
	}

	public void setScheduleProfileProcessors(List<ScheduleProfileProcessor> scheduleProfileProcessors) {
		this.scheduleProfileProcessors = scheduleProfileProcessors;
	}

	public ScheduleProfileProcessor addScheduleProfileProcessor(ScheduleProfileProcessor scheduleProfileProcessor) {
		getScheduleProfileProcessors().add(scheduleProfileProcessor);
		scheduleProfileProcessor.setProcessor(this);

		return scheduleProfileProcessor;
	}

	public ScheduleProfileProcessor removeScheduleProfileProcessor(ScheduleProfileProcessor scheduleProfileProcessor) {
		getScheduleProfileProcessors().remove(scheduleProfileProcessor);
		scheduleProfileProcessor.setProcessor(null);

		return scheduleProfileProcessor;
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

	/**
	 * Method returns the processor type from the discriminator value.
	 * 
	 * @return The Processor type
	 */
	@Transient
	public ProcessorType getProcessorType() {

		DiscriminatorValue val = this.getClass().getAnnotation(DiscriminatorValue.class);
		String code = val.value();
		return ProcessorType.findByCode(code);
	}

	/**
	 * Factory method returns Processor instance corresponding to the input value.
	 * 
	 * @param processorType
	 *            enumeration indicating the type of processor type.
	 * @return a new instance of Processor of the give type.
	 */
	@Transient
	public static Processor processorInstanceFactory(ProcessorType processorType) {

		Processor processor = null;

		if (ProcessorType.REMOTEDOWNLOADER.equals(processorType)) {
			processor = new RemoteDownloader();
		} else if (ProcessorType.REMOTEUPLOADER.equals(processorType)) {
			processor = new RemoteUploader();
		} else {
			processor = new Sweeper();
		}

		return processor;
	}

	/**
	 * Gets the configured email receivers from the mailbox for the processor.
	 * 
	 * @return List of receivers
	 */
	@Transient
	public List<String> getEmailAddress() {

		MailBox mailBox = getMailbox();
		List<MailBoxProperty> properties = mailBox.getMailboxProperties();

		if (null != properties) {

			for (MailBoxProperty property : properties) {

				if (MailBoxConstants.MBX_RCVR_PROPERTY.equals(property.getMbxPropName())) {
					String address = property.getMbxPropValue();
					return Arrays.asList(address.split(","));
				}
			}
		}

		return null;

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((pguid == null) ? 0 : pguid.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Processor)) {
			return false;
		}
		Processor other = (Processor) obj;
		if (pguid == null) {
			if (other.pguid != null) {
				return false;
			}
		} else if (!pguid.equals(other.pguid)) {
			return false;
		}
		return true;
	}

}