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
import com.liaison.mailbox.rtdm.model.ProcessorExecutionState;
import com.liaison.mailbox.rtdm.model.RuntimeProcessors;
import com.liaison.mailbox.service.core.fsm.ProcessorExecutionStateDTO;

import javax.persistence.EntityManager;
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
    public void addProcessor(ProcessorExecutionStateDTO executionStateDTO) {

        RuntimeProcessors processors = new RuntimeProcessors();
        processors.setPguid(UUIDGen.getCustomUUID());
        processors.setProcessorId(executionStateDTO.getProcessorId());

        ProcessorExecutionState prcsrExecution = new ProcessorExecutionState();
        prcsrExecution.setPguid(processors.getPguid());
        prcsrExecution.setProcessorId(executionStateDTO.getProcessorId());
        prcsrExecution.setExecutionStatus(executionStateDTO.getExecutionStatus());
        prcsrExecution.setModifiedBy(executionStateDTO.getModifiedBy());
        prcsrExecution.setModifiedDate(new Date());
        prcsrExecution.setProcessors(processors);
        processors.setProcessorExecState(prcsrExecution);

        persist(processors);
    }

}
