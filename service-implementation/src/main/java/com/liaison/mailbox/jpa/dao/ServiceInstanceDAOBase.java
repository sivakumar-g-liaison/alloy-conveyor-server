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
import com.liaison.mailbox.jpa.model.ServiceInstanceId;

public class ServiceInstanceDAOBase extends GenericDAOBase<ServiceInstanceId> implements ServiceInstanceDAO, MailBoxDAO {

	public ServiceInstanceDAOBase() {
		super(PERSISTENCE_UNIT_NAME);
	}

	/*@Override
	@SuppressWarnings("unchecked")
	public ServiceInstanceId findByName(String serviceInstanceId) {

		EntityManager entityManager = DAOUtil.getEntityManager(persistenceUnitName);
		try {

			List<ServiceInstanceId> permissionRefs = entityManager.createNamedQuery(FIND_BY_NAME).setParameter(SERVICE_INSTANCE, serviceInstanceId)
					.getResultList();
			Iterator<ServiceInstanceId> iter = permissionRefs.iterator();

			while (iter.hasNext()) {
				return iter.next();
			}

		} finally {
			if (entityManager != null) {
				entityManager.clear();
			}
		}
		return null;
	}*/
	
	@Override
	@SuppressWarnings("unchecked")
	public ServiceInstanceId findByName(String servInsId) {

		EntityManager entityManager = DAOUtil.getEntityManager(persistenceUnitName);
		try {

			List<ServiceInstanceId> serviceInsIds = entityManager.createNamedQuery(FIND_BY_SERVICE_INSTANCEID).setParameter(INTANXE_ID, servInsId).getResultList();
			Iterator<ServiceInstanceId> iter = serviceInsIds.iterator();

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

