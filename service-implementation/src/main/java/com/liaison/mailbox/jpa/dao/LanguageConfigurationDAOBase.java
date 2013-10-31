package com.liaison.mailbox.jpa.dao;

import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;

import com.liaison.commons.jpa.DAOUtil;
import com.liaison.commons.jpa.GenericDAOBase;
import com.liaison.mailbox.jpa.model.Language;

public class LanguageConfigurationDAOBase extends GenericDAOBase<Language> implements LanguageConfigurationDAO,
		MailBoxDAO {

	public LanguageConfigurationDAOBase() {
		super(PERSISTENCE_UNIT_NAME);
	}

	@Override
	public Language findByName(String name) {

		EntityManager entityManager = DAOUtil.getEntityManager(persistenceUnitName);
		try {

			@SuppressWarnings("unchecked")
			List<Language> profiles = entityManager.createNamedQuery(GET_BY_LANG_NAME)
					.setParameter(NAME, name).getResultList();
			Iterator<Language> iter = profiles.iterator();

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
