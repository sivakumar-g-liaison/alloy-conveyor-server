package com.liaison.mailbox.jpa.dao;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liaison.commons.jpa.DAOUtil;
import com.liaison.commons.jpa.GenericDAOBase;
import com.liaison.mailbox.enums.ExecutionStatus;
import com.liaison.mailbox.enums.MailBoxStatus;
import com.liaison.mailbox.jpa.model.Processor;
import com.liaison.mailbox.service.util.MailBoxUtility;

public class ProcessorConfigurationDAOBase extends GenericDAOBase<Processor> implements ProcessorConfigurationDAO, MailBoxDAO {

	private static final Logger LOG = LoggerFactory.getLogger(ProcessorConfigurationDAOBase.class);

	public ProcessorConfigurationDAOBase() {
		super(PERSISTENCE_UNIT_NAME);
	}

	@Override
	public List<Processor> findByProfileAndMbxNamePattern(String profileName, String mbxNamePattern, String shardKey) {

		EntityManager entityManager = DAOUtil.getEntityManager(persistenceUnitName);

		try {

			List<?> proc = entityManager.createNamedQuery(FIND_PROCESSOR_BY_PROFILE_AND_MBX_NAME_PATTERN)
					.setParameter(PROF_NAME, profileName)
					.setParameter(STATUS, MailBoxStatus.ACTIVE.value())
					.setParameter(EXEC_STATUS, ExecutionStatus.RUNNING.value())
					.setParameter(MBX_NAME, (MailBoxUtility.isEmpty(mbxNamePattern) ? "''" : mbxNamePattern + "%"))
					.setParameter(SHARD_KEY, (MailBoxUtility.isEmpty(shardKey) ? "%%" : shardKey))
					.getResultList();

			List<Processor> processors = new ArrayList<Processor>();

			Iterator<?> iter = proc.iterator();
			Processor processor;
			while (iter.hasNext()) {

				processor = (Processor) iter.next();
				processors.add(processor);
				LOG.info("Processor Configuration -Pguid : {}, JavaScriptUri : {}, Desc: {}, Properties : {}, Status : {}",
						processor.getPrimaryKey(), processor.getJavaScriptUri(), processor.getProcsrDesc(),
						processor.getProcsrProperties(), processor.getProcsrStatus());
			}

			return processors;
		} finally {
			if (entityManager != null) {
				entityManager.clear();
			}
		}

	}
}
