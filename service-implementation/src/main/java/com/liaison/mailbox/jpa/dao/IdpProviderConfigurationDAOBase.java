package com.liaison.mailbox.jpa.dao;

import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;

import com.liaison.commons.jpa.DAOUtil;
import com.liaison.commons.jpa.GenericDAOBase;
import com.liaison.mailbox.jpa.model.IdpProvider;

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
			List<IdpProvider> profiles = entityManager.createNamedQuery(GET_BY_NAME)
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
	
	
}
