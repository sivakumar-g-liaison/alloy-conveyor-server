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
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.rtdm.model.ProcessorExecutionState;
import com.liaison.mailbox.rtdm.model.RuntimeProcessors;
import com.liaison.mailbox.service.core.fsm.ProcessorExecutionStateDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.util.MailBoxUtil;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.ws.rs.core.Response;

import java.util.Date;
import java.util.List;

/**
 * This will fetch the processors details.
 *
 */
public class RuntimeProcessorsDAOBase extends GenericDAOBase<RuntimeProcessors> implements RuntimeProcessorsDAO, MailboxRTDMDAO {

    public RuntimeProcessorsDAOBase() {
        super(PERSISTENCE_UNIT_NAME);
    }

    @Override
    public RuntimeProcessors findByProcessorId(String processorId) {
        EntityManager entityManager = null;

        try {

            entityManager = DAOUtil.getEntityManager(persistenceUnitName);
            List<RuntimeProcessors> processors = entityManager
                    .createNamedQuery(FIND_BY_PROCESSOR_ID, RuntimeProcessors.class)
                    .setParameter(PROCESSOR_ID, processorId)
                    .setParameter(MailBoxConstants.CLUSTER_TYPE, MailBoxUtil.CLUSTER_TYPE)
                    .getResultList();
            if (!processors.isEmpty()) {
                return processors.get(0);
            }

        } finally {
            if (entityManager != null) {
                entityManager.close();
            }
        }
        return null;
    }

    @Override
    public List<String> findNonRunningProcessors(List<String> processorIds) {

        EntityManager entityManager = null;
        EntityTransaction tx = null;
        try {

            //update the processing status
            entityManager = DAOUtil.getEntityManager(persistenceUnitName);

            // explicitly begin txn
            tx = entityManager.getTransaction();
            tx.begin();

            @SuppressWarnings("unchecked")
            List<String> resultList = entityManager.createNativeQuery(FIND_NON_RUNNING_PROCESSORS)
                    .setParameter(PROCESSOR_ID, processorIds)
                    .setParameter(MailBoxConstants.CLUSTER_TYPE, MailBoxUtil.CLUSTER_TYPE)
                    .getResultList();

            //commits the transaction
            tx.commit();
            return resultList;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            throw e;
        }  finally {
            if (entityManager != null) {
                entityManager.close();
            }
        }
    }

    @Override
    public void addProcessor(ProcessorExecutionStateDTO executionStateDTO, String clusterType) {

        RuntimeProcessors processors = new RuntimeProcessors();
        processors.setPguid(UUIDGen.getCustomUUID());
        processors.setProcessorId(executionStateDTO.getProcessorId());
        processors.setOriginatingDc(DATACENTER_NAME);

        ProcessorExecutionState prcsrExecution = new ProcessorExecutionState();
        prcsrExecution.setPguid(processors.getPguid());
        prcsrExecution.setProcessorId(executionStateDTO.getProcessorId());
        prcsrExecution.setExecutionStatus(executionStateDTO.getExecutionStatus());
        prcsrExecution.setModifiedBy(executionStateDTO.getModifiedBy());
        prcsrExecution.setModifiedDate(new Date());
        prcsrExecution.setProcessors(processors);
        prcsrExecution.setOriginatingDc(DATACENTER_NAME);
        processors.setProcessorExecState(prcsrExecution);
        processors.setClusterType(clusterType);

        persist(processors);
    }
    
    /**
     * Update the Runtimeprocessors clusterType by processorId
     * 
     * @param clusterType
     * @param processorId
     */
    @Override
    public void updateClusterType(String clusterType, String processorId) {
        
        EntityManager entityManager = null;
        EntityTransaction tx = null;
        try {
            
            entityManager = DAOUtil.getEntityManager(persistenceUnitName);
            tx = entityManager.getTransaction();
            tx.begin();
            
            //update the Processors ClusterType
            int count = entityManager.createNativeQuery(UPDATES_PROCESSOR_CLUSTERTYPE)
                        .setParameter(MailBoxConstants.CLUSTER_TYPE, clusterType)
                        .setParameter(PROCESSOR_ID, processorId)
                        .executeUpdate();
            
            if (count == 0) {
                throw new MailBoxConfigurationServicesException(Messages.INVALID_PROCESSOR_ID, processorId,
                        Response.Status.BAD_REQUEST);
            }
            //commits the transaction
            tx.commit();
        
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

}
