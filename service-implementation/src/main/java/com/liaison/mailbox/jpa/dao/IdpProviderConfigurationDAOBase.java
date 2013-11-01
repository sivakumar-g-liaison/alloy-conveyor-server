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
import com.liaison.mailbox.jpa.model.IdpProvider;

/**
 * 
 * 
 * @author praveenu
 */
public class IdpProviderConfigurationDAOBase extends GenericDAOBase<IdpProvider> implements IdpProviderConfigurationDAO,
		MailBoxDAO {

	public IdpProviderConfigurationDAOBase() {
		super(PERSISTENCE_UNIT_NAME);
	}

	@Override
	public IdpProvider findByName(String name) {

		EntityManager entityManager = DAOUtil.getEntityManager(persistenceUnitName);
		try {

			@SuppressWarnings("unchecked")
			List<IdpProvider> profiles = entityManager.createNamedQuery(GET_BY_PROVIDER_NAME)
					.setParameter(NAME, name).getResultList();
			Iterator<IdpProvider> iter = profiles.iterator();

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
	public Set<IdpProvider> findAllProviders() {

		Set<IdpProvider> providers = new HashSet<IdpProvider>();

		EntityManager em = DAOUtil.getEntityManager(persistenceUnitName);
		try {

			List<?> object = em.createNamedQuery(GET_ALL_PROV).getResultList();
			Iterator<?> iter = object.iterator();

			while (iter.hasNext()) {
				providers.add((IdpProvider) iter.next());
			}

		} finally {
			if (em != null) {
				em.clear();
			}
		}
		return providers;
	}
	
}
