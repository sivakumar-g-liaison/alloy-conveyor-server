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

import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.util.MailBoxUtil;
import com.netflix.config.ConfigurationManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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

    /**
     * Find processor execution state by processor id and updates PROCESSING status
     *
     * @param processorId processor guid
     * @return ProcessorExecutionState
     */
    public Object[] findByProcessorIdAndUpdateStatus(String processorId) {

        EntityManager entityManager = null;
        EntityTransaction tx = null;
        try {

            long startTime = System.currentTimeMillis();
            entityManager = DAOUtil.getEntityManager(persistenceUnitName);

            // explicitly begin txn to acquire a pessimistic_write lock
            tx = entityManager.getTransaction();
            tx.begin();

            Object[] results = null;
            try {
                results = (Object[]) entityManager
                        .createNativeQuery(GET_PROCESSOR_EXECUTION_STATE)
                        .setParameter(PROCESSOR_ID, processorId)
                        .getSingleResult();
                if (ExecutionState.PROCESSING.name().equals(results[1])) {
                    String msg = MailBoxConstants.PROCESSOR_IS_ALREDAY_RUNNING + processorId;
                    throw new MailBoxServicesException(msg, Response.Status.NOT_ACCEPTABLE);
                }
            } catch (NoResultException e) {

                //Entry is missing in PROCESSOR_EXECUTION_STATE
                RuntimeProcessorsDAOBase daoBase = new RuntimeProcessorsDAOBase();
                RuntimeProcessors processors = daoBase.findByProcessorId(processorId);

                ProcessorExecutionState prcsrExecution = new ProcessorExecutionState();
                prcsrExecution.setPguid(processors.getPguid());
                prcsrExecution.setProcessorId(processorId);
                prcsrExecution.setExecutionStatus(ExecutionState.READY.name());
                prcsrExecution.setModifiedDate(new Date());
                prcsrExecution.setProcessors(processors);
                prcsrExecution.setOriginatingDc(DATACENTER_NAME);
                processors.setProcessorExecState(prcsrExecution);

                //insert the processor_exec_state
                entityManager.merge(prcsrExecution);
                entityManager.flush();
            }

            //select the newly created record
            if (null == results) {
                results = (Object[]) entityManager
                        .createNativeQuery(GET_PROCESSOR_EXECUTION_STATE_FOR_UPDATE)
                        .setParameter(PROCESSOR_ID, processorId)
                        .setParameter(EXEC_STATUS, ExecutionState.PROCESSING.name())
                        .getSingleResult();
            }

            //update the processing status
            entityManager.createNativeQuery(UPDATE_PROCESSOR_EXECUTION_STATE_VALUES)
                    .setParameter(PGUID, results[0])
                    .setParameter(EXEC_STATUS, ExecutionState.PROCESSING.name())
                    .setParameter(LAST_EXECUTION_STATE, results[1])
                    .setParameter(THREAD_NAME, String.valueOf(Thread.currentThread().getName()))
                    .setParameter(NODE_IN_USE, ConfigurationManager.getDeploymentContext().getDeploymentServerId())
                    .setParameter(MODIFIED_DATE, new Date())
                    .executeUpdate();

            //commit the transaction
            entityManager.flush();
            tx.commit();

            long endTime = System.currentTimeMillis();
            MailBoxUtil.calculateElapsedTime(startTime, endTime);

            results[1] = ExecutionState.PROCESSING.name();
            return results;
        } catch (NoResultException e) {

            //rollback when no results found
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }

            // Indicates processor is already running or not available in the runtime table
            String msg = MailBoxConstants.PROCESSOR_IS_ALREDAY_RUNNING + processorId;
            throw new MailBoxServicesException(msg, Response.Status.NOT_ACCEPTABLE);
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            if (entityManager != null) {
                entityManager.close();
            }
        }
    }

    @Override
    public void updateProcessorExecutionState(String pguid, String status) {

        EntityManager entityManager = null;
        EntityTransaction tx = null;
        try {

            long startTime = System.currentTimeMillis();

            //update the processing status
            entityManager = DAOUtil.getEntityManager(persistenceUnitName);

            // explicitly begin txn
            tx = entityManager.getTransaction();
            tx.begin();

            entityManager.createNativeQuery(UPDATE_PROCESSOR_EXECUTION_STATE)
                    .setParameter(PGUID, pguid)
                    .setParameter(EXEC_STATUS, status)
                    .executeUpdate();

            //commits the transaction
            tx.commit();

            long endTime = System.currentTimeMillis();
            MailBoxUtil.calculateElapsedTime(startTime, endTime);

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            if (entityManager != null) {
                entityManager.close();
            }
        }
    }
    
    /**
     * Updates processor execution state for the current node on server start.
     * Upldate all "PROCESSING" state processors to "FAILED"
     * 
     * @param node
     */
    @Override
    public void updateStuckProcessorsExecutionState(String node) {
        
        EntityManager entityManager = null;
        EntityTransaction tx = null;
        try {
            
            long startTime = System.currentTimeMillis();
            entityManager = DAOUtil.getEntityManager(persistenceUnitName);
            tx = entityManager.getTransaction();
            tx.begin();
            
            entityManager.createNativeQuery(UPDATE_PROCESSOR_EXECUTION_STATE_ON_INIT)
                    .setParameter(NODE_IN_USE, node)
                    .setParameter(EXEC_STATUS, ExecutionState.PROCESSING.name())
                    .setParameter(NEW_EXEC_STATUS, ExecutionState.FAILED.name())
                    .executeUpdate();
            
            tx.commit();
            
            MailBoxUtil.calculateElapsedTime(startTime, System.currentTimeMillis());
            
        } catch (Exception e) {
            if (null != tx && tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            if (null != entityManager) {
                entityManager.close();
            }
        }
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

        try {

            entityManager = DAOUtil.getEntityManager(persistenceUnitName);
            return entityManager
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
    }
    
    /**
     * Method to get executing processors based on time unit value.
     * 
     * @param timeUnit
     * @param value
     * @return runningProcessorsList
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<ProcessorExecutionState> findExecutingProcessors(TimeUnit timeUnit, int value) {
        
        EntityManager entityManager = null;
        List<ProcessorExecutionState> runningProcessors;
        
        try {

            Date date = new Date(System.currentTimeMillis() - timeUnit.toMillis(value));
            entityManager = DAOUtil.getEntityManager(persistenceUnitName);
            runningProcessors = entityManager
                    .createNamedQuery(FIND_EXECUTING_PROCESSOR_WITH_TRIGGERED_PERIOD)
                    .setParameter(EXEC_STATUS, ExecutionState.PROCESSING.value())
                    .setParameter(MODIFIED_DATE, date)
                    .getResultList();
        } finally {
            if (null != entityManager) {
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
