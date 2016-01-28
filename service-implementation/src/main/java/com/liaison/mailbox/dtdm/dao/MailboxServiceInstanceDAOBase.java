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

import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;

import com.liaison.commons.jpa.DAOUtil;
import com.liaison.commons.jpa.GenericDAOBase;
import com.liaison.mailbox.dtdm.model.MailboxServiceInstance;

/**
 * Service instance guid's are fetch through MailboxServiceInstanceDAOBase.
 * 
 * @author OFS
 */
public class MailboxServiceInstanceDAOBase extends GenericDAOBase<MailboxServiceInstance> implements MailboxServiceInstanceDAO, MailboxDTDMDAO {

	public MailboxServiceInstanceDAOBase() {
		super(PERSISTENCE_UNIT_NAME);
	}
    
	/**
	 * Fetches  MailboxServiceInstance from  MAILBOX_SVC_INSTANCE database table.
	 * 
	 * @param mailboxGuid
	 * @param serviceInstanceGuid
	 * @return MailboxServiceInstance
	 */
	@Override
	@SuppressWarnings("unchecked")
	public MailboxServiceInstance findByGuids(String mailboxGuid, String serviceInstanceGuid) {

		EntityManager entityManager = DAOUtil.getEntityManager(persistenceUnitName);
		try {

			List<MailboxServiceInstance> mailboxServiceInstances = entityManager.createNamedQuery(FIND_MBX_SI_GUID).setParameter(GUID_MBX, mailboxGuid)
					.setParameter(SERVICE_INSTANCE_GUID, serviceInstanceGuid)
					.getResultList();
			Iterator<MailboxServiceInstance> iter = mailboxServiceInstances.iterator();

			while (iter.hasNext()) {
				return iter.next();
			}

		} finally {
			if (entityManager != null) {
				entityManager.clear();
			}
		}
		return null;
	}

	/**
     * Returns MailboxServiceInstance count from  MAILBOX_SVC_INSTANCE database table.
     * 
     * @param mailboxGuid
     * @param serviceInstanceGuid
     * @return MailboxServiceInstance count
     */
    @Override
    public int getMailboxServiceInstanceCount(String mailboxGuid, String serviceInstanceGuid) {
        
        EntityManager entityManager = DAOUtil.getEntityManager(persistenceUnitName);
        Long mailboxServiceInstanceCount = null;
        int count;

        try {

            mailboxServiceInstanceCount = (Long) entityManager.createNamedQuery(COUNT_MBX_SI_GUID)
                    .setParameter(GUID_MBX, mailboxGuid)
                    .setParameter(SERVICE_INSTANCE_GUID, serviceInstanceGuid)
                    .getSingleResult();

            count = mailboxServiceInstanceCount.intValue();

        } finally {
            if (entityManager != null) {
                entityManager.close();
            }
        }
        return count;
    }	
}
