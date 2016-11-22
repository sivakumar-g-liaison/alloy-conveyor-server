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

import com.liaison.commons.jpa.DAOUtil;
import com.liaison.commons.jpa.GenericDAOBase;
import com.liaison.commons.util.UUIDGen;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.enums.ExecutionState;
import com.liaison.mailbox.rtdm.model.ProcessorExecutionState;
import com.liaison.mailbox.rtdm.model.RuntimeProcessors;
import com.liaison.mailbox.service.core.fsm.ProcessorExecutionStateDTO;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import javax.persistence.EntityManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * This will fetch the executing processors details.
 *
 * @author OFS
 */
public class ProcessorExecutionStateDAOBase extends GenericDAOBase<ProcessorExecutionState> implements ProcessorExecutionStateDAO, MailboxRTDMDAO {

    private static final Logger LOGGER = LogManager.getLogger(ProcessorExecutionStateDAOBase.class);

    public ProcessorExecutionStateDAOBase() {
        super(PERSISTENCE_UNIT_NAME);
    }

    /**
     * Find processor execution state by processor id
     *
     * @param processorId
     * @return ProcessorExecutionState
     */
    public ProcessorExecutionState findByProcessorId(String processorId) {

        EntityManager entityManager = null;
        ProcessorExecutionState processorExecutionState = null;
        try {

            entityManager = DAOUtil.getEntityManager(persistenceUnitName);

            @SuppressWarnings("unchecked")
            List<ProcessorExecutionState> processorExecutionStates = entityManager
                    .createNamedQuery(FIND_BY_PROCESSOR_ID)
                    .setParameter(PROCESSOR_ID, processorId)
                    .getResultList();

            if (!processorExecutionStates.isEmpty()) {
                processorExecutionState = processorExecutionStates.get(0);
            }

        } finally {
            if (entityManager != null) {
                entityManager.close();
            }
        }

        return processorExecutionState;
    }

    @Override
    public void addProcessorExecutionState(ProcessorExecutionStateDTO executionStateDTO) {

        RuntimeProcessors processors = new RuntimeProcessors();
        processors.setPguid(UUIDGen.getCustomUUID());
        processors.setProcessorId(executionStateDTO.getProcessorId());

        ProcessorExecutionState prcsrExecution = new ProcessorExecutionState();
        prcsrExecution.setProcessorId(executionStateDTO.getProcessorId());
        prcsrExecution.setExecutionStatus(executionStateDTO.getExecutionStatus());
        prcsrExecution.setModifiedBy(executionStateDTO.getModifiedBy());
        prcsrExecution.setModifiedDate(new Date());
        prcsrExecution.setProcessors(processors);

        persist(prcsrExecution);
        LOGGER.debug("Processor Execution created with status READY initialy");
    }

    /**
     * Update the processor execution state with the given status
     *
     * @param processorExecutionState - ProcessorExecutionState entity that has to be updated
     */
    public void updateProcessorExecutionState(ProcessorExecutionState processorExecutionState) {
        merge(processorExecutionState);
        LOGGER.debug("Processor execution state with id " + processorExecutionState.getProcessorId()
                + " is updated with status " + processorExecutionState.getExecutionStatus() + " for processor id "
                + processorExecutionState.getProcessorId());
    }

    /**
     * Returns the processors that aren't running
     *
     * @return list of processor guid
     */
    @SuppressWarnings("unchecked")
    public List<String> findNonExecutingProcessors() {

        EntityManager entityManager = null;
        List<String> nonExecutionProcessors = new ArrayList<>();

        try {

            entityManager = DAOUtil.getEntityManager(persistenceUnitName);
            nonExecutionProcessors = entityManager
                    .createNamedQuery(FIND_NON_EXECUTING_PROCESSORS)
                    .setParameter(EXEC_STATUS, ExecutionState.PROCESSING.value())
                    .getResultList();

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
     * @param pageOffsetDetails pagination details
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<ProcessorExecutionState> findExecutingProcessors(Map<String, Integer> pageOffsetDetails) {

        EntityManager entityManager = null;
        List<ProcessorExecutionState> runningProcessors = new ArrayList<ProcessorExecutionState>();

        try {

            entityManager = DAOUtil.getEntityManager(persistenceUnitName);
            runningProcessors = entityManager
                    .createNamedQuery(FIND_EXECUTING_PROCESSORS)
                    .setParameter(EXEC_STATUS, ExecutionState.PROCESSING.value())
                    .setFirstResult(pageOffsetDetails.get(MailBoxConstants.PAGING_OFFSET))
                    .setMaxResults(pageOffsetDetails.get(MailBoxConstants.PAGING_COUNT))
                    .getResultList();

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

        EntityManager entityManager = null;
        int count = 0;

        try {

            entityManager = DAOUtil.getEntityManager(persistenceUnitName);
            Long processorsCount = (Long) entityManager
                    .createNamedQuery(FIND_EXECUTING_PROCESSORS_ALL)
                    .setParameter(EXEC_STATUS, ExecutionState.PROCESSING.value())
                    .getSingleResult();

            count = processorsCount.intValue();

        } finally {
            if (entityManager != null) {
                entityManager.close();
            }
        }
        return count;
    }

    /**
     * Finds the processors which matches the given parameters
     *
     * @param node       node in use
     * @param threadName thread name
     * @return list of processor states
     */
    @SuppressWarnings("unchecked")
    public List<ProcessorExecutionState> findProcessors(String node, String threadName) {

        EntityManager entityManager = null;
        try {

            entityManager = DAOUtil.getEntityManager(persistenceUnitName);
            return entityManager
                    .createNamedQuery(FIND_PROCESSORS)
                    .setParameter(EXEC_STATUS, ExecutionState.PROCESSING.value())
                    .setParameter(NODE, node)
                    .setParameter(THREAD_NAME, threadName)
                    .getResultList();

        } finally {
            if (entityManager != null) {
                entityManager.close();
            }
        }
    }

}
