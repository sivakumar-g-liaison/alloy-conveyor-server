package com.liaison.mailbox.jpa.dao;

import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;

import com.liaison.commons.jpa.DAOUtil;
import com.liaison.commons.jpa.GenericDAOBase;
import com.liaison.mailbox.jpa.model.GatewayType;

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
			List<GatewayType> profiles = entityManager.createNamedQuery(GET_BY_NAME)
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
}
