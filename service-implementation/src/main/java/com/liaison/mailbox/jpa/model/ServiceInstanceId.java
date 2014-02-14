/* Copyright Liaison Technologies, Inc. All rights reserved.
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
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.liaison.commons.jpa.Identifiable;

/**
* The persistent class for the MAILBOX_SERICEINSTANCE database table.
* 
*/
@Entity
@Table(name = "SERVICE_INSTANCE")
public class ServiceInstanceId implements Identifiable {

	private static final long serialVersionUID = 1L;

	private String pguid;
	private String name;
	private List<MailboxServiceInstance> mailboxServiceInstances;
	
	public ServiceInstanceId() {
	}
	
	@OneToMany(mappedBy = "serviceInstanceId", fetch = FetchType.EAGER, orphanRemoval = true, cascade = { CascadeType.PERSIST,
			CascadeType.MERGE, CascadeType.REMOVE, CascadeType.REFRESH })
	public List<MailboxServiceInstance> getMailboxServiceInstances() {
		return mailboxServiceInstances;
	}

	public void setMailboxServiceInstances(List<MailboxServiceInstance> mailboxServiceInstances) {
		this.mailboxServiceInstances = mailboxServiceInstances;
	}

	@Id
	@Column(unique = true, nullable = false, length = 32)
	public String getPguid() {
		return this.pguid;
	}

	public void setPguid(String pguid) {
		this.pguid = pguid;
	}

	@Column(name = "SERVICE_INSTANCE_ID", nullable = false, length = 32)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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