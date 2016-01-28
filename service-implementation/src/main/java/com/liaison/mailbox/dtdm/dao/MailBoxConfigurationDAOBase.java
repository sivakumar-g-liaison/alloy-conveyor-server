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

import java.math.BigDecimal;
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
 * Performs mailbox fetch operations. 
 * 
 * @author OFS

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
	public int getMailboxCountByProfile(GenericSearchFilterDTO searchFilter, List <String> tenancyKeys) {

		Long totalItems = null;
		int count = 0;

		EntityManager em = DAOUtil.getEntityManager(persistenceUnitName);
		try {
			
			String mbxName = searchFilter.getMbxName();
			String profName = searchFilter.getProfileName();			
			StringBuilder query = new StringBuilder().append("SELECT count(mailbox.pguid) FROM MAILBOX mailbox")
					.append(" INNER JOIN PROCESSOR processor ON mailbox.pguid = processor.MAILBOX_GUID")
					.append(" INNER JOIN SCHED_PROCESSOR schedulepros ON processor.pguid = schedulepros.PROCESSOR_GUID")
					.append(" INNER JOIN SCHED_PROFILE scheduleprof ON schedulepros.SCHED_PROFILE_GUID = scheduleprof.pguid");
			
			query.append(" WHERE (lower(mailbox.NAME) LIKE :")
			        .append(MBOX_NAME) 
			        .append(")");	
			
			if (searchFilter.isDisableFilters() == false) {
            	query.append(" AND (lower(mailbox.TENANCY_KEY) IN (")
					 .append(QueryBuilderUtil.collectionToSqlString(tenancyKeys).toLowerCase())
					 .append("))");
            }
			
			query.append(" AND (scheduleprof.NAME LIKE :")
			        .append(SCHD_PROF_NAME)
			        .append(")");

			totalItems = ((BigDecimal) em.createNativeQuery(query.toString())
					.setParameter(MBOX_NAME, "%" + (mbxName == null ? "" : mbxName.toLowerCase()) + "%")
					.setParameter(SCHD_PROF_NAME, "%" + (profName == null ? "" : profName) + "%")
					.getSingleResult()).longValue();

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
					.append(" inner join schd_prof_processor.scheduleProfilesRef profile");
			
			if (searchFilter.isDisableFilters()) {
				query.append(" where LOWER(mbx.mbxName) like :")
				     .append(MBOX_NAME)					 
					 .append(" and profile.schProfName like :")
					 .append(SCHD_PROF_NAME);
			} else {
				query.append(" where LOWER(mbx.mbxName) like :")
				 	 .append(MBOX_NAME)
				 	 .append(" and LOWER(mbx.tenancyKey) IN (")
				 	 .append(QueryBuilderUtil.collectionToSqlString(tenancyKeys).toLowerCase())
				 	 .append(")")
				 	 .append(" and profile.schProfName like :")
				 	 .append(SCHD_PROF_NAME);
			}
			if (!(StringUtil.isNullOrEmptyAfterTrim(sortField) && StringUtil.isNullOrEmptyAfterTrim(sortDirection))) {

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
			} else {
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
	public int getMailboxCountByName(GenericSearchFilterDTO searchFilter, List<String> tenancyKeys) {
		 
        EntityManager entityManager = DAOUtil.getEntityManager(persistenceUnitName);
        Long totalItems = null;
        int count = 0;
 
        try {
 
            StringBuilder query = new StringBuilder().append("SELECT count(mailbox.pguid) FROM MAILBOX mailbox");
            
            if (searchFilter.isDisableFilters()) {
            	if(!MailBoxUtil.isEmpty(searchFilter.getMbxName())) {
            	    query.append(" WHERE (lower(mailbox.NAME) LIKE :")
                    .append(MBOX_NAME)
                    .append(")");
            	}
            } else {
            	query.append(" WHERE (lower(mailbox.NAME) LIKE :")
                .append(MBOX_NAME)
                .append(")")
            	.append(" AND (lower(mailbox.TENANCY_KEY) IN(")
				.append(QueryBuilderUtil.collectionToSqlString(tenancyKeys).toLowerCase())
				.append("))");
            }	
            
            if (searchFilter.isDisableFilters() && MailBoxUtil.isEmpty(searchFilter.getMbxName())) {
	            totalItems = ((BigDecimal) entityManager.createNativeQuery(query.toString()).getSingleResult()).longValue();
            } else {
            	totalItems = ((BigDecimal) entityManager.createNativeQuery(query.toString())
	                    .setParameter(MBOX_NAME, "%" + searchFilter.getMbxName().toLowerCase() + "%")
	                    .getSingleResult()).longValue();
            }
 
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

			StringBuilder query = new StringBuilder().append("SELECT mbx FROM MailBox mbx");
			
			 if (searchFilter.isDisableFilters()){
				 if(!MailBoxUtil.isEmpty(searchFilter.getMbxName())) {
					 query.append(" where LOWER(mbx.mbxName) like :")
					 	  .append(MBOX_NAME);
				 }
			 } else {
				 query.append(" where LOWER(mbx.mbxName) like :")
				 	.append(MBOX_NAME)
				 	.append(" and LOWER(mbx.tenancyKey) IN (")
					.append(QueryBuilderUtil.collectionToSqlString(tenancyKeys).toLowerCase())
					.append(")");
			 }
					
			String sortDirection = searchFilter.getSortDirection();
			String sortField=searchFilter.getSortField();

			if (!(StringUtil.isNullOrEmptyAfterTrim(sortField) && StringUtil.isNullOrEmptyAfterTrim(sortDirection))) {
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
			} else {
				query.append(" order by mbx.mbxName");
			}
			
			if (searchFilter.isDisableFilters() && MailBoxUtil.isEmpty(searchFilter.getMbxName())) {
				mailboxList = entityManager.createQuery(query.toString())					
						.setFirstResult(pageOffsetDetails.get(MailBoxConstants.PAGING_OFFSET))
						.setMaxResults(pageOffsetDetails.get(MailBoxConstants.PAGING_COUNT))
						.getResultList();
			} else {			
				mailboxList = entityManager.createQuery(query.toString())
						.setParameter(MBOX_NAME, "%" + searchFilter.getMbxName().toLowerCase() + "%")
						.setFirstResult(pageOffsetDetails.get(MailBoxConstants.PAGING_OFFSET))
						.setMaxResults(pageOffsetDetails.get(MailBoxConstants.PAGING_COUNT))
						.getResultList();
			}

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
							.append(" where mailbox.tenancyKey in (")
							.append(QueryBuilderUtil.collectionToSqlString(tenancyKeys))
							.append(")");
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
	
	
	@SuppressWarnings("unchecked")
	@Override
	public MailBox getMailboxByName(String mbxName) {
		
		EntityManager entityManager = DAOUtil.getEntityManager(persistenceUnitName);
		List<MailBox> mailboxList = null;
		MailBox appEntity = null;

		try {

			mailboxList = entityManager.createNamedQuery(GET_MBX_BY_NAME)
					.setParameter(MBOX_NAME, (MailBoxUtil.isEmpty(mbxName) ? "''" : mbxName))
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
}