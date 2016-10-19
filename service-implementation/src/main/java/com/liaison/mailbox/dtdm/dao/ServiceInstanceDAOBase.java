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
import com.liaison.mailbox.dtdm.model.ServiceInstance;

/**
 * Performs the Service Instance fetch operations.
 * 
 * @author OFS
 */
public class ServiceInstanceDAOBase extends GenericDAOBase<ServiceInstance> implements ServiceInstanceDAO, MailboxDTDMDAO {

	public ServiceInstanceDAOBase() {
		super(PERSISTENCE_UNIT_NAME);
	}
	
	/**
	 * Fetches ServiceInstance from SERVICE_INSTANCE database table by given servInsId.
	 * 
	 * @param servInsId
	 * @return ServiceInstance
	 */
	@Override
	@SuppressWarnings("unchecked")
	public ServiceInstance findById(String servInsId) {

        EntityManager entityManager = null;
		try {

            entityManager = DAOUtil.getEntityManager(persistenceUnitName);
			List<ServiceInstance> serviceInsIds = entityManager.createNamedQuery(FIND_BY_SERVICE_INSTANCEID).setParameter(INTANXE_ID, servInsId).getResultList();
			Iterator<ServiceInstance> iter = serviceInsIds.iterator();

			while (iter.hasNext()) {
				return iter.next();
			}

		} finally {
			if (entityManager != null) {
                entityManager.close();
			}
		}
		return null;
	}
}
