package com.liaison.mailbox.jpa.dao;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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

			List<?> mailBox = entityManager.createNamedQuery(FIND_ACTIVE_MAILBOX_BY_PGUID).setParameter(PGUID, guid)
					.getResultList();
			Iterator<?> iter = mailBox.iterator();

			while (iter.hasNext()) {
				return (MailBox) iter.next();
			}

		} finally {
			if (entityManager != null) {
				entityManager.close();
			}
		}
		return null;
	}
	
	@Override
	public MailBox findMailBox(String guid, String serviceInstId) {

		EntityManager entityManager = DAOUtil.getEntityManager(persistenceUnitName);
		try {

			List<?> mailBox = entityManager.createNamedQuery(FIND_MAILBOX_BY_PGUID_SIID).setParameter(PGUID, guid).setParameter(SERVICE_INST_ID, serviceInstId)
					.getResultList();
			Iterator<?> iter = mailBox.iterator();

			while (iter.hasNext()) {
				return (MailBox) iter.next();
			}

		} finally {
			if (entityManager != null) {
				entityManager.close();
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
	public Set<MailBox> find(String mbxName, String profName) {

		Set<MailBox> mailBoxes = new HashSet<MailBox>();

		EntityManager em = DAOUtil.getEntityManager(persistenceUnitName);
		try {

			List<?> object = em
					.createNamedQuery(GET_MBX)
					.setParameter(MailBoxConfigurationDAO.MBOX_NAME, "%" + (mbxName == null ? "" : mbxName.toLowerCase()) + "%")
					.setParameter(MailBoxConfigurationDAO.SCHD_PROF_NAME, "%" + (profName == null ? "" : profName) + "%")
					.getResultList();
			Iterator<?> iter = object.iterator();

			while (iter.hasNext()) {
				mailBoxes.add((MailBox) iter.next());
			}

		} finally {
			if (em != null) {
				em.clear();
			}
		}
		return mailBoxes;
	}

	@Override
	public Set<MailBox> findByName(String mbxName) {

		EntityManager entityManager = DAOUtil.getEntityManager(persistenceUnitName);
		Set<MailBox> mailBoxes = new HashSet<MailBox>();

		try {

			List<?> object = entityManager.createNamedQuery(FIND_BY_MBX_NAME)
					.setParameter(MBOX_NAME, "%" + mbxName.toLowerCase() + "%")
					.getResultList();
			Iterator<?> iter = object.iterator();

			while (iter.hasNext()) {
				mailBoxes.add((MailBox) iter.next());
			}

		} finally {
			if (entityManager != null) {
				entityManager.close();
			}
		}
		return mailBoxes;
	}

}
