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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.liaison.commons.jpa.Identifiable;

/**
* The persistent class for the MAILBOX_SVC_INSTANCE database table.
* 
*/
@Entity
@Table(name = "MAILBOX_SVC_INSTANCE")
@NamedQuery(name = "MailboxServiceInstance.findAll", query = "SELECT msi FROM MailboxServiceInstance msi")
public class MailboxServiceInstance implements Identifiable {

	private static final long serialVersionUID = 1L;

	private String pguid;
	private MailBox mailbox;
	private ServiceInstance serviceInstance;

	public MailboxServiceInstance() {
	}

	@Id
	@Column(unique = true, nullable = false, length = 32)
	public String getPguid() {
		return this.pguid;
	}

	public void setPguid(String pguid) {
		this.pguid = pguid;
	}

	@ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.EAGER)
	@JoinColumn(name = "MAILBOX_GUID", nullable = false)
	public MailBox getMailbox() {
		return mailbox;
	}

	public void setMailbox(MailBox mailbox) {
		this.mailbox = mailbox;
	}

	@ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.EAGER)
	@JoinColumn(name = "SERVICE_INSTANCE_GUID", nullable = false)
	public ServiceInstance getServiceInstanceId() {
		return serviceInstance;
	}

	public void setServiceInstanceId(ServiceInstance serviceInstance) {
		this.serviceInstance = serviceInstance;
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