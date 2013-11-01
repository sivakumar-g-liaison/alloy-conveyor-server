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
import com.liaison.mailbox.jpa.model.Account;

/**
 * 
 * 
 * @author praveenu
 */
public class UserAccountConfigurationDAOBase extends GenericDAOBase<Account> implements UserAccountConfigurationDAO, MailBoxDAO {

	public UserAccountConfigurationDAOBase() {
		super(PERSISTENCE_UNIT_NAME);
	}
	
	@Override
	public Set<Account> find(String accType, String providerName, String loginId) {

		Set<Account> accounts = new HashSet<Account>();

		EntityManager em = DAOUtil.getEntityManager(persistenceUnitName);
		try {

			List<?> object = em.createNamedQuery(GET_TYPE_ACCOUNT)
					.setParameter(UserAccountConfigurationDAO.ACC_TYPE_NAME, (accType == null ? "" : accType) + "%")
					.setParameter(UserAccountConfigurationDAO.IDP_PRO_NAME, (providerName == null ? "" : providerName) + "%")
					.setParameter(UserAccountConfigurationDAO.ACC_NAME, (loginId == null ? "" : loginId) + "%")
					.getResultList();
			Iterator<?> iter = object.iterator();

			while (iter.hasNext()) {
				accounts.add((Account) iter.next());
			}

		} finally {
			if (em != null) {
				em.clear();
			}
		}
		return accounts;
	}
	
	@Override
	public Set<Account> findAllAcc() {

		Set<Account> accounts = new HashSet<Account>();

		EntityManager em = DAOUtil.getEntityManager(persistenceUnitName);
		try {

			List<?> object = em.createNamedQuery(GET_ALL_ACC)
					/*.setParameter(UserAccountConfigurationDAO.ACC_TYPE_NAME, "%" + (accType == null ? "" : accType) + "%")
					.setParameter(UserAccountConfigurationDAO.IDP_PRO_NAME, "%" + (providerName == null ? "" : providerName) + "%")
					.setParameter(UserAccountConfigurationDAO.ACC_NAME, "%" + (loginId == null ? "" : loginId) + "%")*/
					.getResultList();
			Iterator<?> iter = object.iterator();

			while (iter.hasNext()) {
				accounts.add((Account) iter.next());
			}

		} finally {
			if (em != null) {
				em.clear();
			}
		}
		return accounts;
	}


}
