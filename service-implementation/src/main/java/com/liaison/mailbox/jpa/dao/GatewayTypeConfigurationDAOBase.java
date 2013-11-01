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

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;

import com.liaison.commons.jpa.DAOUtil;
import com.liaison.commons.jpa.GenericDAOBase;
import com.liaison.mailbox.jpa.model.GatewayType;

/**
 * 
 * 
 * @author praveenu
 */
public class GatewayTypeConfigurationDAOBase extends GenericDAOBase<GatewayType> implements GatewayTypeConfigurationDAO,
		MailBoxDAO {

	public GatewayTypeConfigurationDAOBase() {
		super(PERSISTENCE_UNIT_NAME);
	}

	@Override
	public GatewayType findByName(String name) {

		EntityManager entityManager = DAOUtil.getEntityManager(persistenceUnitName);
		try {

			@SuppressWarnings("unchecked")
			List<GatewayType> profiles = entityManager.createNamedQuery(GET_BY_GATE_NAME)
					.setParameter(NAME, name).getResultList();
			Iterator<GatewayType> iter = profiles.iterator();

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
	
	@Override
	public Set<GatewayType> findAllGateType() {

		Set<GatewayType> gatewayTypes = new HashSet<GatewayType>();

		EntityManager em = DAOUtil.getEntityManager(persistenceUnitName);
		try {

			List<?> object = em.createNamedQuery(GET_ALL_GATE).getResultList();
			Iterator<?> iter = object.iterator();

			while (iter.hasNext()) {
				gatewayTypes.add((GatewayType) iter.next());
			}

		} finally {
			if (em != null) {
				em.clear();
			}
		}
		return gatewayTypes;
	}
}
