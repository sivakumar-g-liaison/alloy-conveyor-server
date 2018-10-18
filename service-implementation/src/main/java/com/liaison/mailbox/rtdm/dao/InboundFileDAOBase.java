/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.rtdm.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import com.liaison.commons.jpa.DAOUtil;
import com.liaison.commons.jpa.GenericDAOBase;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.enums.EntityStatus;
import com.liaison.mailbox.rtdm.model.InboundFile;
import com.liaison.mailbox.service.util.MailBoxUtil;

/**
 * This will fetch the Inbound file details. 
 */
public class InboundFileDAOBase extends GenericDAOBase<InboundFile> implements InboundFileDAO, MailboxRTDMDAO {

    public InboundFileDAOBase() {
        super(PERSISTENCE_UNIT_NAME);
    }

    @Override
    public List<InboundFile> findInboundFiles(String filePath, String processorGuid) {
        
        EntityManager entityManager = null;
        try {

            entityManager = DAOUtil.getEntityManager(persistenceUnitName);
            
            return entityManager.createQuery(FIND_INBOUND_FILES_BY_FILE_PATH, InboundFile.class)
                    .setParameter(FILE_PATH, filePath)
                    .setParameter(PROCESSOR_GUID, processorGuid)
                    .setParameter(MailBoxConstants.PROCESS_DC, DATACENTER_NAME)
                    .setParameter(STATUS, EntityStatus.ACTIVE.name())
                    .getResultList();

        } finally {
            if (null != entityManager) {
                entityManager.close();
            }
        }
    }

    @Override
    public List<InboundFile> findInboundFilesByRecurse(String filePath, String processorGuid) {

        EntityManager entityManager = null;
        try {

            entityManager = DAOUtil.getEntityManager(persistenceUnitName);

            return entityManager.createQuery(FIND_INBOUND_FILES_BY_FILE_PATH_RECURSE, InboundFile.class)
                    .setParameter(FILE_PATH, filePath + "%")
                    .setParameter(PROCESSOR_GUID, processorGuid)
                    .setParameter(MailBoxConstants.PROCESS_DC, DATACENTER_NAME)
                    .setParameter(STATUS, EntityStatus.ACTIVE.name())
                    .getResultList();

        } finally {
            if (null != entityManager) {
                entityManager.close();
            }
        }
    }

    @Override
    public List<InboundFile> findInboundFilesForConditionalSweeper(String filePath, String processorGuid, String triggerFileGuid) {
        
        EntityManager entityManager = null;
        try {

            entityManager = DAOUtil.getEntityManager(persistenceUnitName);
            
            return entityManager.createQuery(FIND_INBOUND_FILES_FOR_CONDITIONAL_SWEEPER_BY_FILE_PATH, InboundFile.class)
                    .setParameter(FILE_PATH, filePath)
                    .setParameter(TRIGGER_FILE_GUID, triggerFileGuid)
                    .setParameter(PROCESSOR_GUID, processorGuid)
                    .setParameter(MailBoxConstants.PROCESS_DC, DATACENTER_NAME)
                    .setParameter(STATUS, EntityStatus.ACTIVE.name())
                    .getResultList();

        } finally {
            if (null != entityManager) {
                entityManager.close();
            }
        }
    }

    @Override
    public List<InboundFile> findInboundFilesForInprogressTriggerFile(String filePath, String parentGuid) {
        
        EntityManager entityManager = null;
        try {

            entityManager = DAOUtil.getEntityManager(persistenceUnitName);
            
            return entityManager.createQuery(FIND_INBOUND_FILES_BY_INPROGRESS_TRIGGER_FILE, InboundFile.class)
                    .setParameter(PARENT_GUID, parentGuid)
                    .setParameter(MailBoxConstants.PROCESS_DC, DATACENTER_NAME)
                    .getResultList();

        } finally {
            if (null != entityManager) {
                entityManager.close();
            }
        }
    }

    @Override
    public List<InboundFile> findInboundFilesForConditionalSweeperByRecurse(String filePath, String processorGuid, String triggerFileGuid) {

        EntityManager entityManager = null;
        try {

            entityManager = DAOUtil.getEntityManager(persistenceUnitName);

            return entityManager.createQuery(FIND_INBOUND_FILES_FOR_CONDITIONAL_SWEEPER_BY_FILE_PATH_RECURSE, InboundFile.class)
                    .setParameter(FILE_PATH, filePath + "%")
                    .setParameter(PROCESSOR_GUID, processorGuid)
                    .setParameter(TRIGGER_FILE_GUID, triggerFileGuid)
                    .setParameter(MailBoxConstants.PROCESS_DC, DATACENTER_NAME)
                    .setParameter(STATUS, EntityStatus.ACTIVE.name())
                    .getResultList();

        } finally {
            if (null != entityManager) {
                entityManager.close();
            }
        }
    }

    @Override
    public InboundFile findInprogressTriggerFile(String payloadLocation, String processorGuid) {

        EntityManager entityManager = null;
        InboundFile inboundFile = null;

        try {

            entityManager = DAOUtil.getEntityManager(persistenceUnitName);
            inboundFile = entityManager.createNamedQuery(GET_INPROGRESS_TRIGGER_FILE, InboundFile.class)
                    .setParameter(InboundFileDAO.FILE_PATH, payloadLocation)
                    .setParameter(InboundFileDAO.PROCESSOR_GUID, processorGuid)
                    .setParameter(MailBoxConstants.PROCESS_DC, DATACENTER_NAME)
                    .getResultList().stream().findFirst().orElse(null);

        } finally {
            if (entityManager != null) {
                entityManager.close();
            }
        }
        return inboundFile;
    }

    @Override
    public InboundFile findInboundFileForTriggerFile(String payloadLocation, String triggerFileName, String datacenter, String processorGuid) {

        EntityManager entityManager = null;
        try {

            entityManager = DAOUtil.getEntityManager(persistenceUnitName);

            return entityManager.createQuery(FIND_INBOUND_FILE_BY_TRIGGER_FILE, InboundFile.class)
                    .setParameter(FILE_PATH, payloadLocation)
                    .setParameter(FILE_NAME, triggerFileName)
                    .setParameter(InboundFileDAO.PROCESSOR_GUID, processorGuid)
                    .setParameter(MailBoxConstants.PROCESS_DC, datacenter)
                    .setParameter(STATUS, EntityStatus.ACTIVE.name())
                    .getResultList().stream().findFirst().orElse(null);

        } finally {
            if (null != entityManager) {
                entityManager.close();
            }
        }
    }

    @Override
    public InboundFile findInboundFile(String payloadLocation, String fileName, String datacenter) {

        EntityManager entityManager = null;
        try {

            entityManager = DAOUtil.getEntityManager(persistenceUnitName);

            return entityManager.createQuery(FIND_INBOUND_FILE, InboundFile.class)
                    .setParameter(FILE_PATH, payloadLocation)
                    .setParameter(FILE_NAME, fileName)
                    .setParameter(MailBoxConstants.PROCESS_DC, datacenter)
                    .setParameter(STATUS, EntityStatus.ACTIVE.name())
                    .getResultList().stream().findFirst().orElse(null);

        } finally {
            if (null != entityManager) {
                entityManager.close();
            }
        }
    }

    @Override
    public void updateInboundFileStatusByGuid(String guid,  String status, String modifiedBy) {
        
        EntityManager entityManager = null;
        EntityTransaction tx = null;
        try {
            
            entityManager = DAOUtil.getEntityManager(persistenceUnitName);
            tx = entityManager.getTransaction();
            tx.begin();
            
            //update the InboundFile Status
            entityManager.createNativeQuery(UPDATE_INBOUND_FILE_STATUS_BY_GUID)
                .setParameter(STATUS, status)
                .setParameter(GUID, guid)
                .setParameter(MODIFIED_BY, modifiedBy)
                .setParameter(MODIFIED_DATE, MailBoxUtil.getTimestamp())
                .executeUpdate();
            
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

    @Override
    public void updateParentGuidForConditionalSweeper(List<String> guids, String parentGuid) {
        
        EntityManager entityManager = null;
        EntityTransaction tx = null;
        try {
            
            entityManager = DAOUtil.getEntityManager(persistenceUnitName);
            tx = entityManager.getTransaction();
            tx.begin();
            
            //update the parent guid of inbound file
            entityManager.createNativeQuery(UPDATE_PARENT_GUID_FOR_CONDITIONAL_SWEEPER_FILES)
                .setParameter(PARENT_GUID, parentGuid)
                .setParameter(GUID, guids)
                .executeUpdate();
            
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

    @Override
    public void updateInboundFileProcessDCByProcessorGUid(List<String> processorGuids, String newProcessDc) {

        EntityManager entityManager = null;
        EntityTransaction tx = null;
        try {

            entityManager = DAOUtil.getEntityManager(persistenceUnitName);
            tx = entityManager.getTransaction();
            tx.begin();

            //update the parent guid of inbound file
            entityManager.createNativeQuery(UPDATE_PROCESS_DC_BY_PROCESSOR_GUID)
                    .setParameter(PROCESS_DC, newProcessDc)
                    .setParameter(STATUS, EntityStatus.INACTIVE.name())
                    .setParameter(GUID, processorGuids)
                    .executeUpdate();

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
