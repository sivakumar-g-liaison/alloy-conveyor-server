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
import com.liaison.mailbox.service.util.MailBoxUtil;

/**
 * @author OFS
 * 
 */
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
					.setParameter(MBX_NAME, (MailBoxUtil.isEmpty(mbxNamePattern) ? "''" : mbxNamePattern + "%"))
					.setParameter(SHARD_KEY, (MailBoxUtil.isEmpty(shardKey) ? "%%" : shardKey))
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
	 * Checks the mailbox has the processor or not.
	 * 
	 * @param guid pguid of the mailbox
	 * @return boolean
	 */
	@Override
	public boolean isMailboxHasProcessor(String guid) {

		EntityManager entityManager = DAOUtil.getEntityManager(persistenceUnitName);
		boolean status = false;

		try {

			LOG.info("Fetching the processor count starts.");

			long count = (Long) entityManager.createNamedQuery(FIND_PROCESSOR_COUNT)
					.setParameter(PGUID, guid)
					.getSingleResult();
			if (count > 0) {
 				status = true; 
			}

		} finally {
			if (entityManager != null) {
				entityManager.close();
			}
		}

		LOG.info("Fetching the processor count ends.");
		return status;
	}

	/**
	 * Retrieves the list of processor from the given mailbox guid and service instance guid(name).
	 * 
	 * @param mbxGuid pguid of the mailbox
	 * @param siGuid service instance id(name)
	 * @return list of processor
	 */
	@Override
	public List<Processor> findProcessorByMbxAndServiceInstance(String mbxGuid, String siGuid) {

		EntityManager entityManager = DAOUtil.getEntityManager(persistenceUnitName);
		List<Processor> processors = new ArrayList<Processor>();

		try {

			LOG.info("find processor by mbx and service instacne starts.");

			StringBuffer query = new StringBuffer().append("select processor from Processor processor")
					.append(" inner join processor.mailbox mbx")
					.append(" where mbx.pguid = :" + PGUID)
					.append(" and processor.pguid in (select prcsr.pguid from Processor prcsr")
					.append(" inner join prcsr.serviceInstance si")
					.append(" where si.name like :" + SERV_INST_ID + ")");

			List<?> proc = entityManager.createQuery(query.toString())
					.setParameter(PGUID, mbxGuid)
					.setParameter(SERV_INST_ID, siGuid)
					.getResultList();

			Iterator<?> iter = proc.iterator();
			Processor processor;
			while (iter.hasNext()) {

				processor = (Processor) iter.next();
				processors.add(processor);
			}

		} finally {
			if (entityManager != null) {
				entityManager.close();
			}
		}

		LOG.info("find processor by mbx and service instacne ends.");
		return processors;
	}

	/**
	 * Retrieves list of processor from the given mailbox guid
	 * 
	 * @param mbxGuid the mailbox guid
	 * @return list of processor
	 */
	@Override
	public List<Processor> findProcessorByMbx(String mbxGuid) {

		EntityManager entityManager = DAOUtil.getEntityManager(persistenceUnitName);
		List<Processor> processors = new ArrayList<Processor>();

		try {

			LOG.info("Fetching the processor count starts.");

			StringBuffer query = new StringBuffer().append("select processor from Processor processor")
					.append(" inner join processor.mailbox mbx")
					.append(" where mbx.pguid = :" + PGUID);

			List<?> proc = entityManager.createQuery(query.toString())
					.setParameter(PGUID, mbxGuid)
					.getResultList();

			Iterator<?> iter = proc.iterator();
			Processor processor;
			while (iter.hasNext()) {

				processor = (Processor) iter.next();
				processors.add(processor);
				LOG.info("Processor Configuration -Pguid : {}, JavaScriptUri : {}, Desc: {}, Properties : {}, Status : {}",
						processor.getPrimaryKey(), processor.getJavaScriptUri(), processor.getProcsrDesc(),
						processor.getProcsrProperties(), processor.getProcsrStatus());
			}

		} finally {
			if (entityManager != null) {
				entityManager.close();
			}
		}

		return processors;
	}

	/**
	 * Retrieves list of all processors of specific type from given mailbox guid
	 * 
	 * @param type the processor type
	 * @param mbxGuid the mailbox guid
	 * @return list of processors
	 */
	@Override
	public List<Processor> findProcessorByTypeAndMbx(String type, String mbxGuid) {
		
		EntityManager entityManager = DAOUtil.getEntityManager(persistenceUnitName);
		List<Processor> processors = new ArrayList<Processor>();

		try {

			LOG.info("Fetching the processor count starts.");

			StringBuffer query = new StringBuffer().append("select processor from Processor processor")
					.append(" inner join processor.mailbox mbx")
					.append(" where mbx.pguid = :" + PGUID)
					.append(" and processor.type = :" + PROCESSOR_TYPE );

			List<?> proc = entityManager.createQuery(query.toString())
					.setParameter(PGUID, mbxGuid)
					.setParameter(PROCESSOR_TYPE, (type == null)? "" : type.toLowerCase())
					.getResultList();

			Iterator<?> iter = proc.iterator();
			Processor processor;
			while (iter.hasNext()) {

				processor = (Processor) iter.next();
				processors.add(processor);
				LOG.info("Processor Configuration -Pguid : {}, JavaScriptUri : {}, Desc: {}, Properties : {}, Status : {}",
						processor.getPrimaryKey(), processor.getJavaScriptUri(), processor.getProcsrDesc(),
						processor.getProcsrProperties(), processor.getProcsrStatus());
			}

		} finally {
			if (entityManager != null) {
				entityManager.close();
			}
		}

		return processors;
	}

}
