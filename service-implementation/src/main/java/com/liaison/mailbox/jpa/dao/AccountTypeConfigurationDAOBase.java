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
import com.liaison.mailbox.jpa.model.AccountType;

/**
 * 
 * 
 * @author praveenu
 */
public class AccountTypeConfigurationDAOBase extends GenericDAOBase<AccountType> implements AccountTypeConfigurationDAO,
		MailBoxDAO {

	public AccountTypeConfigurationDAOBase() {
		super(PERSISTENCE_UNIT_NAME);
	}

	@Override
	public AccountType findByName(String name) {

		EntityManager entityManager = DAOUtil.getEntityManager(persistenceUnitName);
		try {

			@SuppressWarnings("unchecked")
			List<AccountType> profiles = entityManager.createNamedQuery(GET_BY_NAME)
					.setParameter(NAME, name).getResultList();
			Iterator<AccountType> iter = profiles.iterator();

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
	public Set<AccountType> findAllAccType() {

		Set<AccountType> accounts = new HashSet<AccountType>();

		EntityManager em = DAOUtil.getEntityManager(persistenceUnitName);
		try {

			List<?> object = em.createNamedQuery(GET_ALL_ACC_TYP).getResultList();
			Iterator<?> iter = object.iterator();

			while (iter.hasNext()) {
				accounts.add((AccountType) iter.next());
			}

		} finally {
			if (em != null) {
				em.clear();
			}
		}
		return accounts;
	}
}
