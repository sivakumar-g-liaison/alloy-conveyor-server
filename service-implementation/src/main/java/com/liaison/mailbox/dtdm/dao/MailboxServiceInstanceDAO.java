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

	public static final String FIND_MBX_SI_GUID = "MailboxServiceInstance.findByMbxAndServiceInsId";
	public static final String COUNT_MBX_SI_GUID = "MailboxServiceInstance.countByMbxAndServiceInsId";
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
	
	/**
     * To check whether MailboxServiceInstance is empty or not by given mailboxGuid and serviceInstanceGuid.
     * 
     * @param mailboxGuid
     * @param serviceInstanceGuid
     * @return int (MailboxServiceInstance count)
     */
	public int getMailboxServiceInstanceCount(String mailboxGuid, String serviceInstanceGuid); 

}