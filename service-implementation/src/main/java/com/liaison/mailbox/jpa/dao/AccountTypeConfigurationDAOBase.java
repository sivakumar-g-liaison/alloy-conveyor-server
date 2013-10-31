package com.liaison.mailbox.jpa.dao;

import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;

import com.liaison.commons.jpa.DAOUtil;
import com.liaison.commons.jpa.GenericDAOBase;
import com.liaison.mailbox.jpa.model.AccountType;

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
}
