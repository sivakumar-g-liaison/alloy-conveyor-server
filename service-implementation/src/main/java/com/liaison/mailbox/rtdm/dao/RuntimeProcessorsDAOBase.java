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
import com.liaison.mailbox.rtdm.model.ProcessorExecutionState;
import com.liaison.mailbox.rtdm.model.RuntimeProcessors;
import com.liaison.mailbox.service.core.fsm.ProcessorExecutionStateDTO;
import com.liaison.mailbox.service.util.MailBoxUtil;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

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
    @SuppressWarnings("unchecked")
    public RuntimeProcessors findByProcessorId(String processorId) {
        EntityManager entityManager = null;

        try {

            entityManager = DAOUtil.getEntityManager(persistenceUnitName);
            @SuppressWarnings("unchecked")
            List<RuntimeProcessors> processors = entityManager
                    .createNamedQuery(FIND_BY_PROCESSOR_ID)
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
    public void addProcessor(ProcessorExecutionStateDTO executionStateDTO) {

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
        processors.setClusterType(MailBoxUtil.CLUSTER_TYPE);

        persist(processors);
    }

}
