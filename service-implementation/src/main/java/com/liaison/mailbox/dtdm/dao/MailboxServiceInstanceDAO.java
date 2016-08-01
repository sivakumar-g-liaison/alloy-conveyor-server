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

import com.liaison.commons.jpa.GenericDAO;
import com.liaison.mailbox.dtdm.model.MailboxServiceInstance;

/**
 * The dao class for the MAILBOX_SERVICE_INSTANCE database table.
 * 
 * @author OFS
 */
public interface MailboxServiceInstanceDAO extends GenericDAO<MailboxServiceInstance> {

	String FIND_MBX_SI_GUID = "MailboxServiceInstance.findByMbxAndServiceInsId";
	String COUNT_MBX_SI_GUID = "MailboxServiceInstance.countByMbxAndServiceInsId";
	String SERVICE_INSTANCE_GUID = "serviceInstanceeGuid";
	String GUID_MBX = "guidOfMbx";
    
	/**
     * To check whether MailboxServiceInstance is empty or not by given mailboxGuid and serviceInstanceGuid.
     * 
     * @param mailboxGuid mailbox guid
     * @param serviceInstanceGuid service instance guid
     * @return int (MailboxServiceInstance count)
     */
	int getMailboxServiceInstanceCount(String mailboxGuid, String serviceInstanceGuid);

}