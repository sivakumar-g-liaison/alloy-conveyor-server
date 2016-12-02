/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall 
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.dtdm.model;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.liaison.commons.jpa.Identifiable;
import com.liaison.mailbox.dtdm.dao.MailboxServiceInstanceDAO;

/**
* The persistent class for the MAILBOX_SVC_INSTANCE database table.
* 
*  @author OFS
*/
@Entity
@Table(name = "MAILBOX_SVC_INSTANCE")
@NamedQueries({ 
        @NamedQuery(name = MailboxServiceInstanceDAO.FIND_MBX_SI_GUID, query = "SELECT msi FROM MailboxServiceInstance msi where msi.mailbox.pguid = :"
            + MailboxServiceInstanceDAO.GUID_MBX + " AND msi.serviceInstance.pguid = :" + MailboxServiceInstanceDAO.SERVICE_INSTANCE_GUID),
        @NamedQuery(name = MailboxServiceInstanceDAO.COUNT_MBX_SI_GUID, query = "SELECT count(msi) FROM MailboxServiceInstance msi where msi.mailbox.pguid = :"
            + MailboxServiceInstanceDAO.GUID_MBX + " AND msi.serviceInstance.pguid = :" + MailboxServiceInstanceDAO.SERVICE_INSTANCE_GUID),
        @NamedQuery(name = "MailboxServiceInstance.findAll", query = "SELECT msi FROM MailboxServiceInstance msi")
})
public class MailboxServiceInstance implements Identifiable {

	private static final long serialVersionUID = 1L;

	private String pguid;
	private MailBox mailbox;
	private ServiceInstance serviceInstance;
	private String originatingDc;

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

	@ManyToOne(cascade = { CascadeType.REFRESH })
	@JoinColumn(name = "MAILBOX_GUID", nullable = false)
	public MailBox getMailbox() {
		return mailbox;
	}

	public void setMailbox(MailBox mailbox) {
		this.mailbox = mailbox;
	}

	@ManyToOne(cascade = { CascadeType.REFRESH }, fetch = FetchType.EAGER)
	@JoinColumn(name = "SERVICE_INSTANCE_GUID", nullable = false)
	@Fetch(value = FetchMode.SELECT)
	public ServiceInstance getServiceInstance() {
		return serviceInstance;
	}

	public void setServiceInstance(ServiceInstance serviceInstance) {
		this.serviceInstance = serviceInstance;
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