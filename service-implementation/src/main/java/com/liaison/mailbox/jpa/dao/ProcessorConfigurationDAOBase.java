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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liaison.commons.jpa.DAOUtil;
import com.liaison.commons.jpa.GenericDAOBase;
import com.liaison.mailbox.enums.ExecutionState;
import com.liaison.mailbox.enums.MailBoxStatus;
import com.liaison.mailbox.jpa.model.Processor;
import com.liaison.mailbox.service.util.MailBoxUtility;

public class ProcessorConfigurationDAOBase extends GenericDAOBase<Processor> implements ProcessorConfigurationDAO, MailBoxDAO {

	private static final Logger LOG = LoggerFactory.getLogger(ProcessorConfigurationDAOBase.class);

	public ProcessorConfigurationDAOBase() {
		super(PERSISTENCE_UNIT_NAME);
	}
    
	/**
	 * Fetches all the Processor from PROCESSOR database table by profileName and mailbox name pattern.
	 * 
	 * @param profileName
	 *            The profile name.
	 * @param mbxNamePattern
	 *            The MailBox name pattern to exclude
	 * @return The list of processors.
	 */
	@Override
	public List<Processor> findByProfileAndMbxNamePattern(String profileName, String mbxNamePattern, String shardKey) {

		EntityManager entityManager = DAOUtil.getEntityManager(persistenceUnitName);

		try {

			List<?> proc = entityManager.createNamedQuery(FIND_PROCESSOR_BY_PROFILE_AND_MBX_NAME_PATTERN)
					.setParameter(PROF_NAME, profileName)
					.setParameter(STATUS, MailBoxStatus.ACTIVE.value())
					.setParameter(EXEC_STATUS, ExecutionState.PROCESSING.value())
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
				entityManager.close();
			}
		}

	}
	
	/**
	 * Fetches all the Processor PROCESSOR database table by processor pguid.
	 * 
	 * @param processorId
	 *            The processor unique id.
	  * @return a processor.
	 */
	@Override
	public Processor findByProcessorId(String processorId) {

		EntityManager entityManager = DAOUtil.getEntityManager(persistenceUnitName);

		try {
			
			Processor processor = null;
			Object proc = entityManager.createNamedQuery(FIND_PROCESSOR_BY_PROCESSOR_ID)
					.setParameter(PGU_ID, processorId)
					.getSingleResult();
			if (proc != null) {
				processor = (Processor) proc;
			}
			LOG.info("Processor Configuration -Pguid : {}, JavaScriptUri : {}, Desc: {}, Properties : {}, Status : {}",
					processor.getPrimaryKey(), processor.getJavaScriptUri(), processor.getProcsrDesc(),
					processor.getProcsrProperties(), processor.getProcsrStatus());

			return processor;
		} finally {
			if (entityManager != null) {
				entityManager.close();
			}
		}

	}

}
