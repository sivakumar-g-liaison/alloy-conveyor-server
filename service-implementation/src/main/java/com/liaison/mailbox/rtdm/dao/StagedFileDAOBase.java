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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;

import com.liaison.commons.jpa.DAOUtil;
import com.liaison.commons.jpa.GenericDAOBase;
import com.liaison.mailbox.rtdm.model.StagedFile;

/**
 * @author OFS
 * 
 */
public class StagedFileDAOBase extends  GenericDAOBase<StagedFile> implements StagedFileDAO, MailboxRTDMDAO {

	public StagedFileDAOBase() {
		super(PERSISTENCE_UNIT_NAME);
	}

	@Override
	public List<StagedFile> findStagedFilesOfMailboxes(List<String> mailboxIds) {
		
		List <StagedFile> stagedFiles = new ArrayList<StagedFile>();
		EntityManager entityManager = DAOUtil.getEntityManager(persistenceUnitName);
		
		try {
			
			StringBuilder query = new StringBuilder().append("select sf from StagedFile sf")
							.append(" where sf.mailboxId in (" +collectionToSqlString(mailboxIds) + ")");
			
			List<?> files = entityManager.createQuery(query.toString()).getResultList();
			
			Iterator<?> iterator = files.iterator();
			
			while (iterator.hasNext()) {
				StagedFile stagedFile = (StagedFile)iterator.next();
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
	 * Generate "in" clause string from the list.
	 *
	 * @param mailboxIds list of mailboxIds
	 * @return String
	 */
	private String collectionToSqlString(List<String> mailboxIds) {

		if (null == mailboxIds || mailboxIds.isEmpty()) {
			return null;
		}

		StringBuilder s = new StringBuilder();
		for (final String str : mailboxIds) {
			s.append("'").append(str).append("'").append(",");
		}

		return s.toString().substring(0, s.toString().length() - 1);
	}
}
