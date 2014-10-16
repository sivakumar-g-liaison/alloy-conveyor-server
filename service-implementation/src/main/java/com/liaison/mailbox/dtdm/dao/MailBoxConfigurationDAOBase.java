/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall 
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.dtdm.dao;

import java.util.List;

import javax.persistence.EntityManager;

import com.liaison.commons.jpa.DAOUtil;
import com.liaison.commons.jpa.GenericDAOBase;
import com.liaison.commons.util.client.sftp.StringUtil;
import com.liaison.mailbox.dtdm.model.MailBox;

/**
 * @author OFS
 * 
 */
public class MailBoxConfigurationDAOBase extends GenericDAOBase<MailBox>
		implements MailBoxConfigurationDAO, MailboxDTDMDAO  {

	public MailBoxConfigurationDAOBase() {
		super(PERSISTENCE_UNIT_NAME);
		
	}
	
	/**
	 * Fetches the count of all MailBoxes from  MAILBOX database table by given mailbox name, profile name and service instance ids.
	 * 
	 * @param mbxName
	 * @param profName
	 * @return count of mailbox retrieved
	 */
	@Override
	public int getMailboxCountByProtocol(String mbxName, String profName, List <String> tenancyKeys) {

		Long totalItems = null;
		int count = 0;
		
		EntityManager em = DAOUtil.getEntityManager(persistenceUnitName);
		try {

			StringBuilder query = new StringBuilder().append("SELECT count(mbx) FROM MailBox mbx")
					.append(" inner join mbx.mailboxProcessors prcsr")
					.append(" inner join prcsr.scheduleProfileProcessors schd_prof_processor")
					.append(" inner join schd_prof_processor.scheduleProfilesRef profile")
					.append(" where LOWER(mbx.mbxName) like :" + MBOX_NAME)
					.append(" and LOWER(mbx.tenancyKey) IN (" + collectionToSqlString(tenancyKeys) + ")")
					.append(" and profile.schProfName like :" + SCHD_PROF_NAME);
			
			totalItems = (Long)em
					.createQuery(query.toString())
					.setParameter(MBOX_NAME, "%" + (mbxName == null ? "" : mbxName.toLowerCase()) + "%")
					.setParameter(SCHD_PROF_NAME, "%" + (profName == null ? "" : profName) + "%")
					.getSingleResult();
			
			count = totalItems.intValue();

		} finally {
			if (em != null) {
				em.clear();
			}
		}
		return count;
	}
	
	/**
	 * Fetches all MailBox from  MAILBOX database table by given mailbox name, profile name and service instance ids.
	 * 
	 * @param mbxName
	 * @param profName
	 * @return list of mailbox
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<MailBox> find(String mbxName, String profName, List <String> tenancyKeys, int pagingOffset, int pagingCount, String sortField, String sortDirection) {

		List<MailBox> mailBoxes = null;
		EntityManager em = DAOUtil.getEntityManager(persistenceUnitName);
		try {

			StringBuilder query = new StringBuilder().append("SELECT mbx FROM MailBox mbx")
					.append(" inner join mbx.mailboxProcessors prcsr")
					.append(" inner join prcsr.scheduleProfileProcessors schd_prof_processor")
					.append(" inner join schd_prof_processor.scheduleProfilesRef profile")
					.append(" where LOWER(mbx.mbxName) like :" + MBOX_NAME)
					.append(" and LOWER(mbx.tenancyKey) IN (" + collectionToSqlString(tenancyKeys) + ")")
					.append(" and profile.schProfName like :" + SCHD_PROF_NAME);
			
			if(!(StringUtil.isNullOrEmptyAfterTrim(sortField) && StringUtil.isNullOrEmptyAfterTrim(sortDirection))) {
				
				sortDirection = sortDirection.toUpperCase();
				switch (sortField.toLowerCase()) {
					case "name":
						query.append(" order by mbx.mbxName " + sortDirection);
						break;
					case "description":
						query.append(" order by mbx.mbxDesc " + sortDirection);
						break;
					case "status":
						query.append(" order by mbx.mbxStatus " + sortDirection);
						break;
				}
			}else {
				query.append(" order by mbx.mbxName");
			}
			mailBoxes = em
					.createQuery(query.toString())
					.setParameter(MBOX_NAME, "%" + (mbxName == null ? "" : mbxName.toLowerCase()) + "%")
					.setParameter(SCHD_PROF_NAME, "%" + (profName == null ? "" : profName) + "%")
					.setFirstResult(pagingOffset)
					.setMaxResults(pagingCount)
					.getResultList();

		} finally {
			if (em != null) {
				em.clear();
			}
		}
		return mailBoxes;
	}
	
	/**
	 * Fetches the count of all  MailBoxes from  MAILBOX database table by given mailbox name.
	 * 
	 * @param mbxName
	 * @return count of mailboxes retrieved
	 */
	@Override
	public int getMailboxCountByName(String mbxName, List<String> tenancyKeys) {

		EntityManager entityManager = DAOUtil.getEntityManager(persistenceUnitName);
		Long totalItems = null;
		int count = 0;

		try {

			StringBuilder query = new StringBuilder().append("SELECT count(mbx) FROM MailBox mbx")
					.append(" where LOWER(mbx.mbxName) like :" + MBOX_NAME)
					.append(" and LOWER(mbx.tenancyKey) IN (" + collectionToSqlString(tenancyKeys) + ")");
			totalItems = (Long)entityManager.createQuery(query.toString())
					.setParameter(MBOX_NAME, "%" + mbxName.toLowerCase() + "%")
					.getSingleResult();

			count = totalItems.intValue();

		} finally {
			if (entityManager != null) {
				entityManager.close();
			}
		}
		
		return count;
	}

	/**
	 * Fetches all  MailBox from  MAILBOX database table by given mailbox name.
	 * 
	 * @param mbxName
	 * @return list of mailbox
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<MailBox> findByName(String mbxName, List<String> tenancyKeys, int pagingOffset, int pagingCount, String sortField, String sortDirection) {

		EntityManager entityManager = DAOUtil.getEntityManager(persistenceUnitName);
		List<MailBox> mailboxList = null;

		try {

			StringBuilder query = new StringBuilder().append("SELECT mbx FROM MailBox mbx")
					.append(" where LOWER(mbx.mbxName) like :" + MBOX_NAME)
					.append(" and LOWER(mbx.tenancyKey) IN (" + collectionToSqlString(tenancyKeys) + ")");
			
			if(!(StringUtil.isNullOrEmptyAfterTrim(sortField) && StringUtil.isNullOrEmptyAfterTrim(sortDirection))) {
				
				sortDirection = sortDirection.toUpperCase();
				switch (sortField.toLowerCase()) {
					case "name":
						query.append(" order by mbx.mbxName " + sortDirection);
						break;
					case "description":
						query.append(" order by mbx.mbxDesc " + sortDirection);
						break;
					case "status":
						query.append(" order by mbx.mbxStatus " + sortDirection);
						break;
				}
			}else {
				query.append(" order by mbx.mbxName");
			}
			
			mailboxList = entityManager.createQuery(query.toString())
					.setParameter(MBOX_NAME, "%" + mbxName.toLowerCase() + "%")
					.setFirstResult(pagingOffset)
					.setMaxResults(pagingCount)
					.getResultList();

		} finally {
			if (entityManager != null) {
				entityManager.close();
			}
		}
		
		return mailboxList;
	}

	/**
	 * Generate "in" clause string from the list.
	 *
	 * @param sids list of service instance ids
	 * @return String
	 */
	private String collectionToSqlString(List<String> sids) {

		if (null == sids || sids.isEmpty()) {
			return null;
		}

		StringBuilder s = new StringBuilder();
		for (String str : sids) {
			if (s.length() > 0) {
				s.append(", ");
			}
			s.append(str);
		}

		return s.toString().replaceAll("(\\w+)", "\'$1\'");
	}

}
