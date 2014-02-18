/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall 
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.jpa.dao;

import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;

import com.liaison.commons.jpa.DAOUtil;
import com.liaison.commons.jpa.GenericDAOBase;
import com.liaison.mailbox.jpa.model.MailboxServiceInstance;


public class MailboxServiceInstanceDAOBase extends GenericDAOBase<MailboxServiceInstance> implements MailboxServiceInstanceDAO, MailBoxDAO {

	public MailboxServiceInstanceDAOBase() {
		super(PERSISTENCE_UNIT_NAME);
	}

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
}
