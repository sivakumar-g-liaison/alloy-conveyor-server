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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import com.liaison.commons.jpa.DAOUtil;
import com.liaison.commons.jpa.GenericDAOBase;
import com.liaison.commons.util.client.sftp.StringUtil;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.dtdm.model.MailBox;
import com.liaison.mailbox.service.dto.GenericSearchFilterDTO;
import com.liaison.mailbox.service.util.MailBoxUtil;
import com.liaison.mailbox.service.util.QueryBuilderUtil;

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
					.append(" and LOWER(mbx.tenancyKey) IN (" + QueryBuilderUtil.collectionToSqlString(tenancyKeys).toLowerCase() + ")")
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
	public List<MailBox> find(GenericSearchFilterDTO searchFilter, List <String> tenancyKeys,Map <String, Integer> pageOffsetDetails) {

		List<MailBox> mailBoxes = null;
		EntityManager em = DAOUtil.getEntityManager(persistenceUnitName);
		try {

			
			String sortDirection = searchFilter.getSortDirection();
			String sortField=searchFilter.getSortField();
			String mbxName=searchFilter.getMbxName();
			String profName=searchFilter.getProfileName();
			StringBuilder query = new StringBuilder().append("SELECT distinct mbx FROM MailBox mbx")
					.append(" inner join mbx.mailboxProcessors prcsr")
					.append(" inner join prcsr.scheduleProfileProcessors schd_prof_processor")
					.append(" inner join schd_prof_processor.scheduleProfilesRef profile")
					.append(" where LOWER(mbx.mbxName) like :" + MBOX_NAME)
					.append(" and LOWER(mbx.tenancyKey) IN (" + QueryBuilderUtil.collectionToSqlString(tenancyKeys).toLowerCase() + ")")
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
					.setFirstResult( pageOffsetDetails.get(MailBoxConstants.PAGING_OFFSET))
					.setMaxResults( pageOffsetDetails.get(MailBoxConstants.PAGING_COUNT))
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
					.append(" and LOWER(mbx.tenancyKey) IN (" + QueryBuilderUtil.collectionToSqlString(tenancyKeys).toLowerCase() + ")");
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
	public List<MailBox> findByName(GenericSearchFilterDTO searchFilter, List<String> tenancyKeys, Map <String, Integer> pageOffsetDetails) {

		EntityManager entityManager = DAOUtil.getEntityManager(persistenceUnitName);
		List<MailBox> mailboxList = null;

		try {

			StringBuilder query = new StringBuilder().append("SELECT mbx FROM MailBox mbx")
					.append(" where LOWER(mbx.mbxName) like :" + MBOX_NAME)
					.append(" and LOWER(mbx.tenancyKey) IN (" + QueryBuilderUtil.collectionToSqlString(tenancyKeys).toLowerCase() + ")");
			String sortDirection = searchFilter.getSortDirection();
			String sortField=searchFilter.getSortField();

			if(!(StringUtil.isNullOrEmptyAfterTrim(sortField) && StringUtil.isNullOrEmptyAfterTrim(sortDirection))) {
				sortDirection=sortDirection.toUpperCase();
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
					.setParameter(MBOX_NAME, "%" + searchFilter.getMbxName().toLowerCase() + "%")
					.setFirstResult(pageOffsetDetails.get(MailBoxConstants.PAGING_OFFSET))
					.setMaxResults(pageOffsetDetails.get(MailBoxConstants.PAGING_COUNT))
					.getResultList();

		} finally {
			if (entityManager != null) {
				entityManager.close();
			}
		}

		return mailboxList;
	}

	/**
	 * Fetches MailBox from  MAILBOX database table by given mailbox name and tenancyKey name.
	 *
	 * @param mbxName
	 * * @param tenancyKeyName
	 * @return MailBox
	 */
	@SuppressWarnings("unchecked")
	@Override
	public MailBox findByMailBoxNameAndTenancyKeyName(String mbxName, String tenancyKeyName) {

		EntityManager entityManager = DAOUtil.getEntityManager(persistenceUnitName);
		List<MailBox> mailboxList = null;
		MailBox appEntity = null;

		try {

			mailboxList = entityManager.createNamedQuery(FIND_BY_MBX_NAME_AND_TENANCYKEY_NAME)
					.setParameter(MBOX_NAME,  (MailBoxUtil.isEmpty(mbxName) ? "''" : mbxName))
					.setParameter(TENANCY_KEYS, (MailBoxUtil.isEmpty(tenancyKeyName) ? "''" : tenancyKeyName))
					.getResultList();

			if ((mailboxList == null) || (mailboxList.size() == 0)) {
                return null;
            }

            appEntity = mailboxList.get(0);

		} finally {
			if (entityManager != null) {
				entityManager.close();
			}
		}

		return appEntity;
	}
	
	@Override
	public List<String> findAllMailboxesLinkedToTenancyKeys(List<String> tenancyKeys) {
		
		List <String> linkedMailboxIds = new ArrayList<String>();
		EntityManager entityManager = DAOUtil.getEntityManager(persistenceUnitName);
		
		try {
			
			StringBuilder query = new StringBuilder().append("select mailbox.pguid from MailBox mailbox")
							.append(" where mailbox.tenancyKey in (" + QueryBuilderUtil.collectionToSqlString(tenancyKeys) + ")");
			List<?> mailboxIds = entityManager.createQuery(query.toString()).getResultList();
			
			Iterator<?> iter = mailboxIds.iterator();
			
			while (iter.hasNext()) {
				
				String mailboxId = (String)iter.next();
				linkedMailboxIds.add(mailboxId);		
			}
			
		} finally {
			if (null != entityManager) {
				entityManager.close();				
			}
		}
		
		return linkedMailboxIds;
		
	}

}
