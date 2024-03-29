/* Copyright Liaison Technologies, Inc. All rights reserved.
*
* This software is the confidential and proprietary information of
* Liaison Technologies, Inc. ("Confidential Information").  You shall 
* not disclose such Confidential Information and shall use it only in
* accordance with the terms of the license agreement you entered into
* with Liaison Technologies.
*/

package com.liaison.mailbox.dtdm.model;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.liaison.commons.jpa.Identifiable;
import com.liaison.mailbox.dtdm.dao.ServiceInstanceDAO;

/**
* The persistent class for the MAILBOX_SERICEINSTANCE database table.
* 
* @author OFS
*/
@Entity
@Table(name = "SERVICE_INSTANCE")
@NamedQueries({ @NamedQuery(name = ServiceInstanceDAO.FIND_BY_SERVICE_INSTANCEID, query = "SELECT sii FROM ServiceInstance sii WHERE sii.name = :"
		+ ServiceInstanceDAO.INTANXE_ID) })
public class ServiceInstance implements Identifiable {

	private static final long serialVersionUID = 1L;

	private String pguid;
	private String name;
	private List<MailboxServiceInstance> mailboxServiceInstances;
	private String originatingDc;
	
	public ServiceInstance() {
	}

    public ServiceInstance(String pguid, String name, String originatingDc) {
        this.pguid = pguid;
        this.name = name;
        this.originatingDc = originatingDc;
    }

    @OneToMany(mappedBy = "serviceInstance", orphanRemoval = true, cascade = { CascadeType.PERSIST,
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

	@Column(name = "ORIGINATING_DC", length = 16)
	public String getOriginatingDc() {
		return originatingDc;
	}

	public void setOriginatingDc(String originatingDc) {
		this.originatingDc = originatingDc;
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