package com.liaison.mailbox.jpa.dao;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;

import com.liaison.commons.jpa.DAOUtil;
import com.liaison.commons.jpa.GenericDAOBase;
import com.liaison.mailbox.jpa.model.Processor;

public class ProcessorConfigurationDAOBase extends GenericDAOBase<Processor> implements ProcessorConfigurationDAO, MailBoxDAO {

	public ProcessorConfigurationDAOBase() {
		super(PERSISTENCE_UNIT_NAME);
	}

	@Override
	public void deactivate(String guId) {

		Processor processor = this.find(Processor.class, guId);
		if (processor != null) {
			processor.setProcsrStatus("inactive");
			this.merge(processor);
		}

	}

	@Override
	public Processor find(String guid) {

		EntityManager entityManager = DAOUtil.getEntityManager(persistenceUnitName);
		try {

			List<Processor> processor = entityManager.createNamedQuery(FIND_PROCESSOR_BY_PGUID).setParameter(PGUID, guid)
					.getResultList();
			Iterator<Processor> iter = processor.iterator();

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
	public List<Processor> findByProfile(String profileName) {

		EntityManager entityManager = DAOUtil.getEntityManager(persistenceUnitName);

		try {

			List<?> proc = entityManager.createNamedQuery(FIND_PROCESSOR_BY_PROFILE)
					.setParameter(PROF_NAME, profileName).getResultList();

			List<Processor> processors = new ArrayList<Processor>();

			Iterator<?> iter = proc.iterator();
			while (iter.hasNext()) {
				processors.add((Processor) iter.next());
			}

			return processors;

		} finally {
			if (entityManager != null) {
				entityManager.clear();
			}
		}
	}

	@Override
	public List<Processor> findByProfileAndMbxNamePattern(String profileName, String mbxNamePattern) {

		EntityManager entityManager = DAOUtil.getEntityManager(persistenceUnitName);

		try {
			List<?> proc = entityManager.createNamedQuery(FIND_PROCESSOR_BY_PROFILE_AND_MBX_NAME_PATTERN)
					.setParameter(PROF_NAME, profileName).setParameter(MBX_NAME, "%" + mbxNamePattern + "%")
					.getResultList();

			List<Processor> processors = new ArrayList<Processor>();

			Iterator<?> iter = proc.iterator();
			while (iter.hasNext()) {
				processors.add((Processor) iter.next());
			}

			return processors;
		} finally {
			if (entityManager != null) {
				entityManager.clear();
			}
		}

	}
}
