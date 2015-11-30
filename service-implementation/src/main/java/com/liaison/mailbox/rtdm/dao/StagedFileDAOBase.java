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

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.liaison.commons.jpa.DAOUtil;
import com.liaison.commons.jpa.GenericDAOBase;
import com.liaison.commons.util.client.sftp.StringUtil;
import com.liaison.dto.queue.WorkTicket;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.enums.EntityStatus;
import com.liaison.mailbox.rtdm.model.StagedFile;
import com.liaison.mailbox.service.dto.GenericSearchFilterDTO;
import com.liaison.mailbox.service.dto.dropbox.StagedFileDTO;
import com.liaison.mailbox.service.util.MailBoxUtil;
import com.liaison.mailbox.service.util.QueryBuilderUtil;

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

		List<StagedFile> stagedFiles = new ArrayList<StagedFile>();
		EntityManager entityManager = DAOUtil.getEntityManager(persistenceUnitName);

		try {

		   // get Search Filters
		    String sortDirection = searchFilter.getSortDirection();
		    String fileName = searchFilter.getStagedFileName();
		    String status = searchFilter.getStatus();
		    int pagingOffset = pageOffsetDetails.get(MailBoxConstants.PAGING_OFFSET);
		    int pagingCount = pageOffsetDetails.get(MailBoxConstants.PAGING_COUNT);
		    String entityStatus = MailBoxUtil.isEmpty(status)?EntityStatus.ACTIVE.name():status.toUpperCase();

			StringBuilder queryString = new StringBuilder().append("select sf from StagedFile sf")
					.append(" where sf.mailboxId in (")
					.append(QueryBuilderUtil.collectionToSqlString(mailboxIds))
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
				sortDirection=sortDirection.toUpperCase();
				queryString.append(" order by sf.fileName " + sortDirection);
			} else {
				queryString.append(" order by sf.fileName");
			}

			Query query = entityManager.createQuery(queryString.toString());
		    query.setParameter(STATUS, entityStatus);
			query.setFirstResult(pagingOffset);
			query.setMaxResults(pagingCount);

			if (!StringUtil.isNullOrEmptyAfterTrim(fileName)) {
				query.setParameter(FILE_NAME, "%" + fileName.toLowerCase() + "%");
			}

			if (EntityStatus.ACTIVE.name().equals(entityStatus)) {
				query.setParameter(CURRENT_TIME, new Timestamp(System.currentTimeMillis()));
			}

			List<?> files = query.getResultList();

			Iterator<?> iterator = files.iterator();

			while (iterator.hasNext()) {
				StagedFile stagedFile = (StagedFile) iterator.next();
				stagedFiles.add(stagedFile);
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
	public List<StagedFile> findStagedFilesOfMailboxesBasedonGUID(List<String> mailboxIds, String guid) {

		List<StagedFile> stagedFiles = new ArrayList<StagedFile>();
		EntityManager entityManager = DAOUtil.getEntityManager(persistenceUnitName);

		try {

			StringBuilder query = new StringBuilder().append("select sf from StagedFile sf")
					.append(" where (sf.pguid) = :")
					.append(StagedFileDAO.GUID)
					.append(" and sf.mailboxId in (")
					.append(QueryBuilderUtil.collectionToSqlString(mailboxIds))
					.append(")");

			List<?> files = entityManager
					.createQuery(query.toString())
					.setParameter(StagedFileDAO.GUID, (guid == null ? "" : guid))
					.getResultList();

			Iterator<?> iterator = files.iterator();
			while (iterator.hasNext()) {
				StagedFile stagedFile = (StagedFile) iterator.next();
				stagedFiles.add(stagedFile);
			}

		} finally {

			if (null != entityManager) {
				entityManager.close();
			}
		}
		return stagedFiles;
	}


	/**
	 *Method to number of staged files based on search criteria
	 *
	 */
	@Override
	public int getStagedFilesCountByName(List<String> mailboxIds, String fileName,String status) {

		EntityManager entityManager = DAOUtil.getEntityManager(persistenceUnitName);
		Long totalItems = null;
		int count = 0;

		try {

			String entityStatus = MailBoxUtil.isEmpty(status) ? EntityStatus.ACTIVE.name() : status.toUpperCase();
			
			StringBuilder query = new StringBuilder().append("select count(sf) from StagedFile sf")
					.append(" where LOWER(sf.fileName) like :")
					.append(FILE_NAME)
					.append(" and sf.mailboxId in (")
					.append(QueryBuilderUtil.collectionToSqlString(mailboxIds))
					.append(")");
			
			if (EntityStatus.ACTIVE.name().equals(entityStatus)) {
				query.append(" and sf.expirationTime > :")
				     .append(CURRENT_TIME);
			}
			query.append(" and sf.stagedFileStatus = :")
				 .append(STATUS);
			
			Query queryResult = entityManager.createQuery(query.toString());
			if(EntityStatus.ACTIVE.name().equals(entityStatus)) {
				queryResult.setParameter(CURRENT_TIME, new Timestamp(System.currentTimeMillis()));
			}
			
			queryResult.setParameter(FILE_NAME, "%" + (fileName == null ? "" : fileName.toLowerCase()) + "%")
			           .setParameter(STATUS, entityStatus);

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
    public void persistStagedFile(WorkTicket workticket, String processorId, String processorType) {

        EntityManager entityManager = DAOUtil.getEntityManager(persistenceUnitName);
        StagedFile stagedFileEntity = null;

        try {

        	StagedFileDTO stagedFileDto = new StagedFileDTO(workticket);
        	stagedFileDto.setExpirationTime("0");
        	stagedFileDto.setProcessorId(processorId);
        	stagedFileDto.setProcessorType(processorType);

            stagedFileEntity = new StagedFile();
            stagedFileEntity.copyFromDto(stagedFileDto, true);
            stagedFileEntity.setPguid(workticket.getGlobalProcessId());

            persist(stagedFileEntity);

        } finally {

            if (null != entityManager) {
                entityManager.close();
            }
        }
    }


	@SuppressWarnings("unchecked")
    @Override
	public StagedFile findStagedFilesByProcessorId(String processorId, String fileName) {

		EntityManager em = DAOUtil.getEntityManager(persistenceUnitName);

		try {

			// query
			StringBuilder query = new StringBuilder()
					.append("select sf from StagedFile sf")
					.append(" where sf.processorId =:")
					.append(PROCESSOR_ID)
					.append(" and sf.stagedFileStatus =:")
					.append(STATUS)
					.append(" and sf.fileName =:")
					.append(FILE_NAME);

			List<StagedFile> stagedFiles = em.createQuery(query.toString())
					.setParameter(PROCESSOR_ID, processorId)
					.setParameter(STATUS, EntityStatus.ACTIVE.value())
					.setParameter(FILE_NAME, fileName)
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
	public List<StagedFile> findStagedFilesByProcessorId(String processorId) {

		EntityManager em = DAOUtil.getEntityManager(persistenceUnitName);

		try {

			// query
			StringBuilder query = new StringBuilder().append("select sf from StagedFile sf")
					.append(" where sf.processorId =:")
					.append(PROCESSOR_ID)
					.append(" and sf.stagedFileStatus =:")
					.append(STATUS);

			List<StagedFile> stagedFiles = em.createQuery(query.toString())
					.setParameter(PROCESSOR_ID, processorId)
					.setParameter(STATUS, EntityStatus.ACTIVE.value()).getResultList();

			return stagedFiles;
		} finally {

			if (null != em) {
			    em.close();
			}
		}
	}

}
