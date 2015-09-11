/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.rtdm.dao;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liaison.commons.jpa.DAOUtil;
import com.liaison.commons.jpa.GenericDAOBase;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.enums.ExecutionState;
import com.liaison.mailbox.rtdm.model.ProcessorExecutionState;
import com.liaison.mailbox.service.util.MailBoxUtil;

/**
 * This will fetch the executing processors details.
 *
 * @author OFS
 */
public class ProcessorExecutionStateDAOBase extends  GenericDAOBase<ProcessorExecutionState> implements ProcessorExecutionStateDAO, MailboxRTDMDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProcessorExecutionStateDAOBase.class);

	public ProcessorExecutionStateDAOBase () {
		super(PERSISTENCE_UNIT_NAME);
	}

	public ProcessorExecutionState findByProcessorId(String processorId) {

		EntityManager entityManager = DAOUtil.getEntityManager(persistenceUnitName);
		ProcessorExecutionState processorExecutionState = null;
		try {

			@SuppressWarnings("unchecked")
			List<ProcessorExecutionState> processorExecutionStates = entityManager.createNamedQuery(FIND_BY_PROCESSOR_ID)
					.setParameter(PROCESSOR_ID, processorId).getResultList();
			Iterator<ProcessorExecutionState> iter = processorExecutionStates.iterator();

			while (iter.hasNext()) {
				return iter.next();
			}

		} finally {
			if (entityManager != null) {
				entityManager.close();
			}
		}



		return processorExecutionState;

	}

	@Override
	public void addProcessorExecutionState(String processorId, String executionStatus) {
		ProcessorExecutionState prcsrExecution = new ProcessorExecutionState();
		prcsrExecution.setPguid(MailBoxUtil.getGUID());
		prcsrExecution.setProcessorId(processorId);
		prcsrExecution.setExecutionStatus(executionStatus);

		persist(prcsrExecution);
		LOGGER.info("Processor Execution created with status READY initialy");
	}
	
	/**
	 * Update the processor execution state with the given status
	 * 
	 * @param processorExecutionState - ProcessorExecutionState entity that has to be updated
	 */
	public void updateProcessorExecutionState(ProcessorExecutionState processorExecutionState) {
		merge(processorExecutionState);
		LOGGER.info("Processor execution state with id " + processorExecutionState.getPguid()
				+ " is updated with status " + processorExecutionState.getExecutionStatus() + " for processor id "
				+ processorExecutionState.getProcessorId());
	}

	public List <String> findNonExecutingProcessors() {

		EntityManager entityManager = DAOUtil.getEntityManager(persistenceUnitName);
		List <String> nonExecutionProcessors = new ArrayList<String>();

		try {

			List<?> nonExecutingsProcsrs = entityManager.createNamedQuery(FIND_NON_EXECUTING_PROCESSORS)
					.setParameter(EXEC_STATUS, ExecutionState.PROCESSING.value()).getResultList();
			Iterator<?> iter = nonExecutingsProcsrs.iterator();

			while (iter.hasNext()) {
				nonExecutionProcessors.add((String) iter.next());
			}

		} finally {
			if (entityManager != null) {
				entityManager.close();
			}
		}
		return nonExecutionProcessors;
	}
	
	/**
	 * Method to find the executing processors based on page offset
	 * 
	 * @param pageOffsetDetails
	 */
	@Override
	public List<String> findExecutingProcessors(Map<String, Integer> pageOffsetDetails) {

		EntityManager entityManager = DAOUtil.getEntityManager(persistenceUnitName);
		List<String> runningProcessors = new ArrayList<String>();

		try {

			List<?> executingsProcsrs = entityManager.createNamedQuery(FIND_EXECUTING_PROCESSORS)
					.setParameter(EXEC_STATUS, ExecutionState.PROCESSING.value())
					.setFirstResult(pageOffsetDetails.get(MailBoxConstants.PAGING_OFFSET))
					.setMaxResults(pageOffsetDetails.get(MailBoxConstants.PAGING_COUNT)).getResultList();

			Iterator<?> iter = executingsProcsrs.iterator();

			while (iter.hasNext()) {
				runningProcessors.add((String) iter.next());
			}

		} finally {
			if (entityManager != null) {
				entityManager.close();
			}
		}
		return runningProcessors;
	}

	/**
	 * Method to find the count of all executing processors
	 */
	@Override
	public int findAllExecutingProcessors() {

		EntityManager entityManager = DAOUtil.getEntityManager(persistenceUnitName);
		Long processorsCount = null;
		int count;

		try {

			processorsCount = (Long) entityManager.createNamedQuery(FIND_EXECUTING_PROCESSORS_ALL)
					.setParameter(EXEC_STATUS, ExecutionState.PROCESSING.value()).getSingleResult();

			count = processorsCount.intValue();

		} finally {
			if (entityManager != null) {
				entityManager.close();
			}
		}
		return count;
	}

}
