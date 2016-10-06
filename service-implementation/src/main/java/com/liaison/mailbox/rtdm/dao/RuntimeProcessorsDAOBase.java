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

import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;

import com.liaison.commons.jpa.DAOUtil;
import com.liaison.commons.jpa.GenericDAOBase;
import com.liaison.mailbox.rtdm.model.RuntimeProcessors;
import com.liaison.mailbox.service.util.MailBoxUtil;

/**
 * This will fetch the processors details.
 *
 */
public class RuntimeProcessorsDAOBase extends  GenericDAOBase<RuntimeProcessors> implements RuntimeProcessorsDAO, MailboxRTDMDAO{

    
    public RuntimeProcessorsDAOBase() {
        super(PERSISTENCE_UNIT_NAME);
    }
    
    @Override
    public RuntimeProcessors findByProcessorId(String processorId) {
        EntityManager entityManager = DAOUtil.getEntityManager(persistenceUnitName);
        
        try {
            @SuppressWarnings("unchecked")
            List<RuntimeProcessors> processors = entityManager.createNamedQuery(FIND_BY_PROCESSOR_ID)
                .setParameter(PROCESSOR_ID, processorId).getResultList();
            Iterator<RuntimeProcessors> iter = processors.iterator();
            
            while (iter.hasNext()) {
                return iter.next();
            }
            
        } finally {
            if (entityManager != null) {
                entityManager.close();
            }
        }
        return null;
    }

    @Override
    public void addProcessors(String processorId) {
        RuntimeProcessors processors = new RuntimeProcessors();
        processors.setPguid(MailBoxUtil.getGUID());
        processors.setProcessorId(processorId);
    }

}
