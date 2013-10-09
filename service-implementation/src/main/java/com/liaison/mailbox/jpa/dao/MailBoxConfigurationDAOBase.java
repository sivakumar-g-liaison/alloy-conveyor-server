package com.liaison.mailbox.jpa.dao;

import java.util.ArrayList;
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
	public int deactiveMailBox(String guid) {
		EntityManager entityManager = DAOUtil.getEntityManager(persistenceUnitName);
		return entityManager.createNamedQuery(INACTIVATE_MAILBOX).setParameter(PGUID, guid).executeUpdate();
	}

	@Override
	public List<MailBox> find(String mbxName, String profName) {

		List<MailBox> mailBoxSchedProfiles = new ArrayList<MailBox>();

		EntityManager em = DAOUtil.getEntityManager(persistenceUnitName);
		try {

			List<?> object = em.createNamedQuery(GET_MBX)
					.setParameter(MailBoxConfigurationDAO.MBX_NAME, "%" + (mbxName == null ? "" : mbxName) + "%")
					.setParameter(MailBoxConfigurationDAO.SCHD_PROF_NAME, "%" + (profName == null ? "" : profName) + "%")
					.getResultList();
			Iterator<?> iter = object.iterator();

			while (iter.hasNext()) {
				mailBoxSchedProfiles.add((MailBox) iter.next());
			}

		} finally {
			if (em != null) {
				em.clear();
			}
		}
		return mailBoxSchedProfiles;
	}
}
