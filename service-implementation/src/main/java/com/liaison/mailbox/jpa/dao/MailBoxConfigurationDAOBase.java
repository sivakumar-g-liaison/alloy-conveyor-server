package com.liaison.mailbox.jpa.dao;

import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;

import com.liaison.commons.jpa.DAOUtil;
import com.liaison.commons.jpa.GenericDAOBase;
import com.liaison.mailbox.jpa.model.MailBox;

public class MailBoxConfigurationDAOBase extends GenericDAOBase<MailBox>
		implements MailBoxConfigurationDAO, MailBoxDAO {

	public MailBoxConfigurationDAOBase() {
		super(PERSISTENCE_UNIT_NAME);
	}

	@Override
	public MailBox find(String guid) {

		EntityManager entityManager = DAOUtil.getEntityManager(persistenceUnitName);
		try {

			List<MailBox> mailBox = entityManager.createNamedQuery(FIND_MAILBOX_BY_PGUID).setParameter(PGUID, guid)
					.getResultList();
			Iterator<MailBox> iter = mailBox.iterator();

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
	public MailBox findActiveMailBox(String guid) {

		EntityManager entityManager = DAOUtil.getEntityManager(persistenceUnitName);
		try {

			List<MailBox> mailBox = entityManager.createNamedQuery(FIND_ACTIVE_MAILBOX_BY_PGUID).setParameter(PGUID, guid)
					.getResultList();
			Iterator<MailBox> iter = mailBox.iterator();

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
	public int inactiveMailBox(String guid) {
		EntityManager entityManager = DAOUtil.getEntityManager(persistenceUnitName);
		return entityManager.createNamedQuery(INACTIVATE_MAILBOX).setParameter(PGUID, guid).executeUpdate();
	}
}
