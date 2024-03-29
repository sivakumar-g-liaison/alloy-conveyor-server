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

import com.liaison.commons.jpa.DAOUtil;
import com.liaison.commons.jpa.GenericDAOBase;
import com.liaison.commons.util.StringUtil;
import com.liaison.dto.queue.WorkTicket;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.enums.EntityStatus;
import com.liaison.mailbox.enums.ProcessorType;
import com.liaison.mailbox.rtdm.model.StagedFile;
import com.liaison.mailbox.service.dto.GenericSearchFilterDTO;
import com.liaison.mailbox.service.dto.dropbox.StagedFileDTO;
import com.liaison.mailbox.service.util.MailBoxUtil;
import org.apache.commons.collections.CollectionUtils;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This will fetch the Staged file details. 
 *
 * @author OFS
 */
public class StagedFileDAOBase extends GenericDAOBase<StagedFile> implements StagedFileDAO, MailboxRTDMDAO {

    public StagedFileDAOBase() {
        super(PERSISTENCE_UNIT_NAME);
    }

    /**
     *Method to get list of staged files based on search criteria
     *
     */
    @Override
    public List<StagedFile> findStagedFilesOfMailboxes(List<String> mailboxIds, GenericSearchFilterDTO searchFilter, Map<String, Integer> pageOffsetDetails) {

        List<StagedFile> stagedFiles = new ArrayList<>();
        EntityManager entityManager = null;

        try {

            entityManager = DAOUtil.getEntityManager(persistenceUnitName);

            // get Search Filters
            String sortDirection = searchFilter.getSortDirection();
            String fileName = searchFilter.getStagedFileName();
            String status = searchFilter.getStatus();
            int pagingOffset = pageOffsetDetails.get(MailBoxConstants.PAGING_OFFSET);
            int pagingCount = pageOffsetDetails.get(MailBoxConstants.PAGING_COUNT);
            String entityStatus = MailBoxUtil.isEmpty(status) ? EntityStatus.ACTIVE.name() : status.toUpperCase();

            StringBuilder queryString = new StringBuilder().append("select sf from StagedFile sf")
                    .append(" where sf.mailboxId in (:")
                    .append(MAILBOX_IDS)
                    .append(")")
                    .append(" and sf.stagedFileStatus = :")
                    .append(STATUS);

            // GMB-595 - Setting expirationTime based on the status - (Setting only for ACTIVE status).
            if (EntityStatus.ACTIVE.name().equals(entityStatus)) {
                queryString.append(" and sf.expirationTime > :");
                queryString.append(CURRENT_TIME);
            }

            if (!StringUtil.isNullOrEmptyAfterTrim(fileName)) {
                queryString.append(" and LOWER(sf.fileName) like :");
                queryString.append(FILE_NAME);
            }

            if (!StringUtil.isNullOrEmptyAfterTrim(sortDirection)) {
                sortDirection = sortDirection.toUpperCase();
                queryString.append(" order by sf.fileName ").append(sortDirection);
            } else {
                queryString.append(" order by sf.fileName");
            }

            Query query = entityManager.createQuery(queryString.toString());
            query.setParameter(STATUS, entityStatus)
                    .setParameter(MAILBOX_IDS, mailboxIds);
            query.setFirstResult(pagingOffset);
            query.setMaxResults(pagingCount);

            if (!StringUtil.isNullOrEmptyAfterTrim(fileName)) {
                query.setParameter(FILE_NAME, "%" + fileName.toLowerCase() + "%");
            }

            if (EntityStatus.ACTIVE.name().equals(entityStatus)) {
                query.setParameter(CURRENT_TIME, new Timestamp(System.currentTimeMillis()));
            }

            List<?> files = query.getResultList();
            for (Object stagedFile : files) {
                stagedFiles.add((StagedFile) stagedFile);
            }

        } finally {

            if (null != entityManager) {
                entityManager.close();
            }
        }
        return stagedFiles;
    }

    /**
     * Returns the stagedfileList based on the search done by given GUID and MAILBOXIDS
     */
    @Override
    public List<StagedFile> findStagedFilesOfMailboxesBasedOnGUID(List<String> mailboxIds, String guid) {

        List<StagedFile> stagedFiles = new ArrayList<>();
        EntityManager entityManager = null;

        try {
            entityManager = DAOUtil.getEntityManager(persistenceUnitName);

            String query = "select sf from StagedFile sf" +
                    " where (sf.pguid) = :" +
                    StagedFileDAO.GUID +
                    " and sf.mailboxId in (:" +
                    MAILBOX_IDS +
                    ")";

            List<?> files = entityManager
                    .createQuery(query)
                    .setParameter(StagedFileDAO.GUID, (guid == null ? "" : guid))
                    .setParameter(MAILBOX_IDS, mailboxIds)
                    .getResultList();

            for (Object file : files) {
                stagedFiles.add((StagedFile) file);
            }

        } finally {

            if (null != entityManager) {
                entityManager.close();
            }
        }
        return stagedFiles;
    }

    @Override
    public long findStagedFilesByParentGlobalProcessId(String parentGlobalProcessId) {
    	
        EntityManager entityManager = null;
        try {
            entityManager = DAOUtil.getEntityManager(persistenceUnitName);

            return (long) entityManager
                    .createQuery(FIND_STAGED_FILES_BY_PARENT_GLOBAL_PROCESS_ID)
                    .setParameter(PARENT_GLOBAL_PROCESS_ID, parentGlobalProcessId)
                    .getSingleResult();
        } finally {
            if (null != entityManager) {
                entityManager.close();
            }
        }
    }

    @Override
    public StagedFile findStagedFilesByGlobalProcessIdWithoutProcessDc(String globalProcessId) {

        EntityManager entityManager = null;
        try {

            entityManager = DAOUtil.getEntityManager(persistenceUnitName);
            List<StagedFile> stagedFiles = entityManager
                    .createQuery(FIND_STAGED_FILE_BY_GLOBAL_PROCESS_ID_WITHOUT_PROCESS_DC, StagedFile.class)
                    .setParameter(GLOBAL_PROCESS_ID, globalProcessId).getResultList();

            if (CollectionUtils.isEmpty(stagedFiles)) {
                return null;
            }
            return stagedFiles.get(0);
        } finally {
            if (null != entityManager) {
                entityManager.close();
            }
        }
    }

    /**
     * Returns staged file entries by filename and file path for file writer processor.
     *
     */
    @Override
    public StagedFile findStagedFileForTriggerFile(String filePath, String fileName, String processorId) {

        EntityManager entityManager = null;

        try {
            entityManager = DAOUtil.getEntityManager(persistenceUnitName);
            List<?> files = entityManager.createNamedQuery(GET_STAGED_FILE_FOR_TRIGGER_FILE)
                    .setParameter(FILE_PATH, filePath)
                    .setParameter(FILE_NAME, fileName)
                    .setParameter(PROCESSOR_ID, processorId)
                    .setParameter(STATUS, EntityStatus.ACTIVE.name())
                    .getResultList();

            if (files != null && !files.isEmpty()) {
                return (StagedFile) files.get(0);
            }
        } finally {
            if (null != entityManager) {
                entityManager.close();
            }
        }
        return null;
    }
    
    /**
     * Returns staged file entries by filename and file path for file writer processor.
     *
     */
    @Override
    public StagedFile findStagedFileForRelayTriggerFile(String processorId, String fileName) {

        EntityManager entityManager = null;

        try {
            entityManager = DAOUtil.getEntityManager(persistenceUnitName);
            List<?> files = entityManager.createNamedQuery(GET_STAGED_FILE_FOR_RELAY_TRIGGER_FILE)
                    .setParameter(PROCESSOR_ID, processorId)
                    .setParameter(FILE_NAME, fileName)
                    .setParameter(STATUS, EntityStatus.ACTIVE.name())
                    .getResultList();

            if (files != null && !files.isEmpty()) {
                return (StagedFile) files.get(0);
            }
        } finally {
            if (null != entityManager) {
                entityManager.close();
            }
        }
        return null;
    }
    
    /**
     *Method to number of staged files based on search criteria
     *
     */
    @Override
    public int getStagedFilesCountByName(List<String> mailboxIds, String fileName, String status) {

        EntityManager entityManager = null;
        Long totalItems;
        int count;
        boolean isActive;

        try {

            entityManager = DAOUtil.getEntityManager(persistenceUnitName);

            String entityStatus = MailBoxUtil.isEmpty(status) ? EntityStatus.ACTIVE.name() : status.toUpperCase();

            StringBuilder query = new StringBuilder().append("select count(sf) from StagedFile sf")
                    .append(" where LOWER(sf.fileName) like :")
                    .append(FILE_NAME)
                    .append(" and sf.mailboxId in (:")
                    .append(MAILBOX_IDS)
                    .append(")");

            isActive = (EntityStatus.ACTIVE.name().equals(entityStatus));
            if (isActive) {
                query.append(" and sf.expirationTime > :")
                        .append(CURRENT_TIME);
            }
            query.append(" and sf.stagedFileStatus = :")
                    .append(STATUS);

            Query queryResult = entityManager.createQuery(query.toString());
            if (isActive) {
                queryResult.setParameter(CURRENT_TIME, new Timestamp(System.currentTimeMillis()));
            }

            queryResult.setParameter(FILE_NAME, "%" + (fileName == null ? "" : fileName.toLowerCase()) + "%")
                    .setParameter(STATUS, entityStatus)
                    .setParameter(MAILBOX_IDS, mailboxIds);

            totalItems = (Long) queryResult.getSingleResult();
            count = totalItems.intValue();
            return count;

        } finally {

            if (null != entityManager) {
                entityManager.close();
            }
        }
    }

    @Override
    public void persistStagedFile(WorkTicket workticket,
                                  String processorId,
                                  String processorType,
                                  boolean directUploadEnabled) {

        EntityManager entityManager = null;
        StagedFile stagedFileEntity;

        try {

            entityManager = DAOUtil.getEntityManager(persistenceUnitName);

            StagedFileDTO stagedFileDto = new StagedFileDTO(workticket);
            stagedFileDto.setExpirationTime("0");
            stagedFileDto.setProcessorId(processorId);
            stagedFileDto.setProcessorType(processorType);

            if (directUploadEnabled) {
                stagedFileDto.setStatus(EntityStatus.STAGED.value());
            }

            stagedFileEntity = new StagedFile();
            stagedFileEntity.copyFromDto(stagedFileDto, true);
            persist(stagedFileEntity);

        } finally {

            if (null != entityManager) {
                entityManager.close();
            }
        }
    }


    @SuppressWarnings("unchecked")
    @Override
    public StagedFile findStagedFilesByProcessorId(String processorId, String targetLocation, String fileName) {

        EntityManager em = null;

        try {

            em = DAOUtil.getEntityManager(persistenceUnitName);

            List<String> statuses = new ArrayList<>();
            statuses.add(EntityStatus.STAGED.name());
            statuses.add(EntityStatus.ACTIVE.name());
            statuses.add(EntityStatus.FAILED.name());

            List<StagedFile> stagedFiles = em.createQuery(FIND_STAGED_FILE.toString())
                    .setParameter(PROCESSOR_ID, processorId)
                    .setParameter(STATUS, statuses)
                    .setParameter(FILE_NAME, fileName)
                    .setParameter(FILE_PATH, targetLocation)
                    .setParameter(MailBoxConstants.CLUSTER_TYPE, MailBoxUtil.CLUSTER_TYPE)
                    .getResultList();

            return (stagedFiles.isEmpty()) ? null : stagedFiles.get(0);
        } finally {

            if (null != em) {
                em.close();
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public StagedFile findStagedFileByProcessorIdAndGpid(String processorId, String gpid) {

       EntityManager em = null;

       try {

           List<String> statuses = new ArrayList<>();
           statuses.add(EntityStatus.STAGED.name());
           statuses.add(EntityStatus.ACTIVE.name());
           statuses.add(EntityStatus.FAILED.name());

           em = DAOUtil.getEntityManager(persistenceUnitName);
           List<StagedFile> stagedFiles = em.createQuery(FIND_STAGED_FILE_BY_GPID_AND_PROCESSID)
                   .setParameter(PROCESSOR_ID, processorId)
                   .setParameter(GLOBAL_PROCESS_ID, gpid)
                   .setParameter(STATUS, statuses)
                   .getResultList();

           return (stagedFiles.isEmpty()) ? null : stagedFiles.get(0);
       } finally {
           if (null != em) {
               em.close();
           }
       }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<StagedFile> findStagedFilesByProcessorId(String processId) {

        EntityManager em = null;

        try {

            List<String> statuses = new ArrayList<>();
            statuses.add(EntityStatus.STAGED.name());
            statuses.add(EntityStatus.ACTIVE.name());
            statuses.add(EntityStatus.FAILED.name());

            em = DAOUtil.getEntityManager(persistenceUnitName);

            return (List<StagedFile>) em.createQuery(FIND_STAGED_FILE_BY_PROCESSID)
                    .setParameter(PROCESSOR_ID, processId)
                    .setParameter(STATUS, statuses)
                    .setParameter(PROCESS_DC, DATACENTER_NAME)
                    .getResultList();
        } finally {
            if (null != em) {
                em.close();
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public StagedFile findStagedFileByProcessorIdAndFileName(String processorId, String fileName) {

        EntityManager em = null;

        try {

            List<String> statuses = new ArrayList<>();
            statuses.add(EntityStatus.STAGED.name());
            statuses.add(EntityStatus.ACTIVE.name());
            statuses.add(EntityStatus.FAILED.name());

            em = DAOUtil.getEntityManager(persistenceUnitName);
            List<StagedFile> stagedFiles = em.createQuery(FIND_STAGED_FILES_BY_PROCESSID_AND_NAME)
                    .setParameter(PROCESSOR_ID, processorId)
                    .setParameter(FILE_NAME, fileName)
                    .setParameter(STATUS, statuses)
                    .setParameter(PROCESS_DC, DATACENTER_NAME)
                    .setMaxResults(1).getResultList();

            return (stagedFiles.isEmpty()) ? null : stagedFiles.get(0);
        } finally {
            if (null != em) {
                em.close();
            }
        }
    }
    
    /**
     * Returns the staged files of the given processor
     */
    @Override
    public List<StagedFile> findStagedFilesForUploader(String processorId, String filePath, boolean directUpload, boolean recurseSubDir) {

        List<StagedFile> stagedFiles = new ArrayList<>();
        EntityManager entityManager = null;

        try {

            entityManager = DAOUtil.getEntityManager(persistenceUnitName);

            Query query;
            if (directUpload) {

                List<String> statuses = new ArrayList<>();
                statuses.add(EntityStatus.FAILED.name());
                statuses.add(EntityStatus.ACTIVE.name());

                //to retrieve failed files for profile invocation if direct upload is enabled
                if (MailBoxUtil.isEmpty(filePath)) {
                    query = entityManager.createQuery(GET_STAGED_FILE_BY_PRCSR_GUID_AND_DIR_UPLOAD.toString());
                } else {
                    if (recurseSubDir) {
                        query = entityManager.createQuery(GET_STAGED_FILE_BY_PRCSR_GUID_FOR_DIR_UPLOAD_FILE_PATH_RECURSE.toString());
                        query.setParameter(FILE_PATH, filePath + "%");
                    } else {
                        query = entityManager.createQuery(GET_STAGED_FILE_BY_PRCSR_GUID_AND_FILE_PATH_DIR_UPLOAD.toString());
                        query.setParameter(FILE_PATH, filePath);
                    }	
                }

                query.setParameter(STATUS, statuses);
            } else {

                //to retrieve both active and failed files for profile invocation if direct upload is disabled
                if (recurseSubDir) {
                    query = entityManager.createQuery(GET_STAGED_FILE_BY_PRCSR_GUID_AND_FILE_PATH_RECURSE.toString());
                    query.setParameter(FILE_PATH, filePath + "%");
                } else {
                    query = entityManager.createQuery(GET_STAGED_FILE_BY_PRCSR_GUID_AND_FILE_PATH.toString());
                    query.setParameter(FILE_PATH, filePath);
                }

                query.setParameter(STATUS, EntityStatus.INACTIVE.name());
            }

            query.setParameter(PROCESSOR_ID, processorId);
            query.setParameter(MailBoxConstants.CLUSTER_TYPE, MailBoxUtil.CLUSTER_TYPE);
            query.setParameter(PROCESS_DC, DATACENTER_NAME);

            List<?> files = query.getResultList();
            for (Object file : files) {
                stagedFiles.add((StagedFile) file);
            }

        } finally {
            if (null != entityManager) {
                entityManager.close();
            }
        }
        return stagedFiles;
    }

    /**
     * Returns staged file
     * @param gpid 
     */
    @Override
    public StagedFile findStagedFileByGpid(String gpid) {

        EntityManager entityManager = null;

        try {

            entityManager = DAOUtil.getEntityManager(persistenceUnitName);

            List<?> files = entityManager.createNamedQuery(FIND_BY_GPID)
                    .setParameter(PROCESS_DC, DATACENTER_NAME)
                    .setParameter(GLOBAL_PROCESS_ID, gpid)
                    .setParameter(STATUS, EntityStatus.INACTIVE.name())
                    .setParameter(MailBoxConstants.CLUSTER_TYPE, MailBoxUtil.CLUSTER_TYPE)
                    .setMaxResults(1).getResultList();
            if (files != null && !files.isEmpty()) {
                return (StagedFile) files.get(0);
            }

        } finally {
            if (null != entityManager) {
                entityManager.close();
            }
        }
        return null;
    }

    /**
     * Returns staged file entries by filename and file path for file writer processor.
     */
    @Override
    public StagedFile findStagedFilesByFileNameAndPath(String filePath, String fileName, String status, List<String> processorTypes) {

        EntityManager entityManager = null;

        try {
            entityManager = DAOUtil.getEntityManager(persistenceUnitName);
            List<?> files = entityManager.createNamedQuery(GET_STAGED_FILE_BY_FILE_NAME_AND_FILE_PATH)
                    .setParameter(FILE_PATH, filePath)
                    .setParameter(FILE_NAME, fileName)
                    .setParameter(TYPE, processorTypes)
                    .setParameter(STATUS, status)
                    .setParameter(MailBoxConstants.CLUSTER_TYPE, MailBoxUtil.CLUSTER_TYPE)
                    .getResultList();

            if (files != null && !files.isEmpty()) {
                return (StagedFile) files.get(0);
            }
        } finally {
            if (null != entityManager) {
                entityManager.close();
            }
        }
        return null;
    }
    
    /**
     * Update the StagedFile Status by processorId
     * 
     * @param processorId
     * @param status
     */
    public void updateStagedFileStatusByProcessorId(String processorId,  String status) {
        
        EntityManager entityManager = null;
        EntityTransaction tx = null;
        try {
            
            entityManager = DAOUtil.getEntityManager(persistenceUnitName);
            tx = entityManager.getTransaction();
            tx.begin();
            
            //update the StagedFile Status
            entityManager.createNativeQuery(UPDATE_STAGED_FILE_STATUS_BY_PROCESSORID)
                .setParameter(STATUS, status)
                .setParameter(PROCESSOR_ID, processorId)
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

    /**
     * Update the StagedFile Status for trigger file entry
     * 
     * @param processorId
     * @param status
     */
    public void updateTriggerFileStatusInStagedFile(String processorId, String status, String fileName, String filePath) {
        
        EntityManager entityManager = null;
        EntityTransaction tx = null;
        try {
            
            entityManager = DAOUtil.getEntityManager(persistenceUnitName);
            tx = entityManager.getTransaction();
            tx.begin();
            
            //update the StagedFile Status
            entityManager.createNativeQuery(UPDATE_TRIGGER_FILE_STATUS_IN_STAGED_FILE)
                .setParameter(STATUS, status)
                .setParameter(PROCESSOR_ID, processorId)
                .setParameter(FILE_NAME, fileName)
                .setParameter(FILE_PATH, filePath)
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

    /**
     * Update the StagedFile Status for relay trigger file entry
     * 
     * @param processorId
     * @param status
     */
    public void updateRelayTriggerFileStatusInStagedFile(String processorId, String status, String fileName) {
        
        EntityManager entityManager = null;
        EntityTransaction tx = null;
        try {
            
            entityManager = DAOUtil.getEntityManager(persistenceUnitName);
            tx = entityManager.getTransaction();
            tx.begin();
            
            //update the StagedFile Status
            entityManager.createNativeQuery(UPDATE_RELAY_TRIGGER_FILE_STATUS_IN_STAGED_FILE)
                .setParameter(STATUS, status)
                .setParameter(PROCESSOR_ID, processorId)
                .setParameter(FILE_NAME, fileName)
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
    public void persist(StagedFile entity) {
        
        entity.setOriginatingDc(DATACENTER_NAME);
        entity.setProcessDc(DATACENTER_NAME);
        super.persist(entity);
    }
    

    /**
     * Update the StagedFile process dc
     * 
     * @param existingProcessDC
     * @param newProcessDC
     */
    public void updateStagedFileProcessDC(String existingProcessDC, String newProcessDC) {

        EntityManager entityManager = null;
        EntityTransaction tx = null;
        try {

            entityManager = DAOUtil.getEntityManager(persistenceUnitName);
            tx = entityManager.getTransaction();
            tx.begin();

            // update the StagedFile Status
            entityManager.createNativeQuery(UPDATE_STAGED_FILE_BY_PROCESS_DC)
                .setParameter(EXISTING_PROCESS_DC, existingProcessDC)
                .setParameter(NEW_PROCESS_DC, newProcessDC)
                .executeUpdate();

            // commits the transaction
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
    
    /**
     * Update the StagedFile process dc
     *
     * @param newProcessDC
     */
    @Override
    public void updateStagedFileProcessDC(String newProcessDC) {

        EntityManager entityManager = null;
        EntityTransaction tx = null;
        try {

            entityManager = DAOUtil.getEntityManager(persistenceUnitName);
            tx = entityManager.getTransaction();
            tx.begin();

            // update the StagedFile Status
            entityManager.createNativeQuery(UPDATE_STAGED_FILE_PROCESS_DC)
                    .setParameter(NEW_PROCESS_DC, newProcessDC)
                    .executeUpdate();

            // commits the transaction
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

    /**
     * Update the StagedFile process dc
     *
     * @param newProcessDC
     */
    @Override
    public void updateStagedFileProcessDCByProcessorGuid(List<String> processorGuids, String newProcessDC) {

        EntityManager entityManager = null;
        EntityTransaction tx = null;
        try {

            entityManager = DAOUtil.getEntityManager(persistenceUnitName);
            tx = entityManager.getTransaction();
            tx.begin();

            // update the StagedFile Status
            entityManager.createNativeQuery(UPDATE_STAGED_FILE_PROCESS_DC_PROCESSOR_GUID)
                    .setParameter(NEW_PROCESS_DC, newProcessDC)
                    .setParameter(PROCESSOR_ID, processorGuids)
                    .executeUpdate();

            // commits the transaction
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
