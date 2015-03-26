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

import javax.persistence.EntityManager;

import com.liaison.commons.jpa.DAOUtil;
import com.liaison.commons.jpa.GenericDAOBase;
import com.liaison.commons.util.client.sftp.StringUtil;
import com.liaison.mailbox.enums.MailBoxStatus;
import com.liaison.mailbox.rtdm.model.StagedFile;
import com.liaison.mailbox.service.util.MailBoxUtil;
import com.liaison.mailbox.service.util.QueryBuilderUtil;

/**
 * @author OFS
 * 
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
	public List<StagedFile> findStagedFilesOfMailboxes(List<String> mailboxIds, String fileName, int pagingOffset,
			int pagingCount, String sortField, String sortDirection,String status) {

		List<StagedFile> stagedFiles = new ArrayList<StagedFile>();
		EntityManager entityManager = DAOUtil.getEntityManager(persistenceUnitName);

		try {

			StringBuilder query = new StringBuilder().append("select sf from StagedFile sf")
					.append(" where LOWER(sf.fileName) like :" + FILE_NAME)
					.append(" and sf.mailboxId in (" + QueryBuilderUtil.collectionToSqlString(mailboxIds) + ")")
					.append(" and sf.expirationTime > :"+ CURRENT_TIME)
					.append(" and sf.stagedFileStatus = :"+ STATUS);
			
			if(!StringUtil.isNullOrEmptyAfterTrim(sortDirection)) {
				sortDirection = sortDirection.toUpperCase();
				query.append(" order by sf.fileName " + sortDirection);
			}else {
				query.append(" order by sf.fileName");
			}
			
			List<?> files = entityManager
					.createQuery(query.toString())
					.setParameter(FILE_NAME, "%" + (fileName == null ? "" : fileName.toLowerCase()) + "%")
				    .setParameter(CURRENT_TIME, new Timestamp(System.currentTimeMillis()))
				    .setParameter(STATUS,(MailBoxUtil.isEmpty(status)?MailBoxStatus.ACTIVE.name():status.toUpperCase()))
					.setFirstResult(pagingOffset)
					.setMaxResults(pagingCount)
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
	 * Returns the stagedfileList based on the search done by given GUID and MAILBOXIDS
	 */
	@Override
	public List<StagedFile> findStagedFilesOfMailboxesBasedonGUID(List<String> mailboxIds, String guid) {

		List<StagedFile> stagedFiles = new ArrayList<StagedFile>();
		EntityManager entityManager = DAOUtil.getEntityManager(persistenceUnitName);

		try {

			StringBuilder query = new StringBuilder().append("select sf from StagedFile sf")
					.append(" where (sf.pguid) = :" + StagedFileDAO.GUID)
					.append(" and sf.mailboxId in (" + QueryBuilderUtil.collectionToSqlString(mailboxIds) + ")");
			 
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

			StringBuilder query = new StringBuilder().append("select count(sf) from StagedFile sf")
					.append(" where LOWER(sf.fileName) like :" + FILE_NAME)
					.append(" and sf.mailboxId in (" + QueryBuilderUtil.collectionToSqlString(mailboxIds) + ")")
					.append(" and sf.expirationTime > :"+ CURRENT_TIME)
					.append(" and sf.stagedFileStatus = :"+ STATUS);
			
			totalItems = (Long)entityManager
					.createQuery(query.toString())
					.setParameter(FILE_NAME, "%" + (fileName == null ? "" : fileName.toLowerCase()) + "%")
					.setParameter(CURRENT_TIME, new Timestamp(System.currentTimeMillis()))
					.setParameter(STATUS,(MailBoxUtil.isEmpty(status)?MailBoxStatus.ACTIVE.name():status.toUpperCase()))
					.getSingleResult();
			
			count = totalItems.intValue();
			return count;

		} finally {

			if (null != entityManager) {
				entityManager.close();
			}
		}
	}
}
