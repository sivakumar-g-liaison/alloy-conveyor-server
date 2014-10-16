/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall 
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.mailbox.dtdm.dao;

import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import com.liaison.commons.jpa.GenericDAO;
import com.liaison.mailbox.dtdm.model.MailboxServiceInstance;

/**
 * @author OFS
 * 
 */
@NamedQueries({ @NamedQuery(name = MailboxServiceInstanceDAO.FIND_MBX_SI_GUID, query = "SELECT msi FROM MailboxServiceInstance msi where msi.mailbox.pguid = :"
		+ MailboxServiceInstanceDAO.GUID_MBX + " AND msi.serviceInstance.pguid = :" + MailboxServiceInstanceDAO.SERVICE_INSTANCE_GUID) })

public interface MailboxServiceInstanceDAO extends GenericDAO<MailboxServiceInstance> {

	public static final String FIND_MBX_SI_GUID = "findByMbxAndServiceInsId";
	public static final String SERVICE_INSTANCE_GUID = "serviceInstanceeGuid";
	public static final String GUID_MBX = "guidOfMbx";
    
	/**
	 * Find MailboxServiceInstance by given mailboxGuid and serviceInstanceGuid.
	 * 
	 * @param mailboxGuid
	 * @param serviceInstanceGuid
	 * @return MailboxServiceInstance
	 */
	public MailboxServiceInstance findByGuids(String mailboxGuid, String serviceInstanceGuid);

}