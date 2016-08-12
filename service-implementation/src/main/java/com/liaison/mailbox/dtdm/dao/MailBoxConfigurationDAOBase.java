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
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.Query;

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
            StringBuilder query = new StringBuilder().append("SELECT count(mailbox.pguid) FROM MailBox mailbox")
                    .append(" INNER JOIN mailbox.mailboxProcessors processor")
                    .append(" INNER JOIN processor.scheduleProfileProcessors schedulepros")
                    .append(" INNER JOIN schedulepros.scheduleProfilesRef scheduleprof")
                    .append(" WHERE (lower(mailbox.mbxName) LIKE :")
                    .append(MBOX_NAME).append(")")
                    .append(" AND (scheduleprof.schProfName LIKE :")
                    .append(SCHD_PROF_NAME).append(")");

            //Set Tenancy Key when filter isn't disabled
            if (!searchFilter.isDisableFilters()) {
                query.append(" AND (mailbox.tenancyKey IN (:" + TENANCY_KEYS + "))");
            }

            Query jpaQuery = em.createQuery(query.toString())
                    .setParameter(MBOX_NAME, "%" + (mbxName == null ? "" : mbxName.toLowerCase()) + "%")
                    .setParameter(SCHD_PROF_NAME, "%" + (profName == null ? "" : profName) + "%");

            //Set Tenancy Key when filter isn't disabled
            if (!searchFilter.isDisableFilters()) {
                jpaQuery = jpaQuery.setParameter(TENANCY_KEYS, tenancyKeys);
            }

            totalItems = ((Long) jpaQuery.getSingleResult());
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
		List<String> tenancyKeysLowerCase = null;
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
			    tenancyKeysLowerCase = tenancyKeys.stream().map(String::toLowerCase).collect(Collectors.toList());
				query.append(" where LOWER(mbx.mbxName) like :")
				 	 .append(MBOX_NAME)
				 	 .append(" and LOWER(mbx.tenancyKey) IN (:")
				 	 .append(TENANCY_KEYS)
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
					.setParameter(TENANCY_KEYS, tenancyKeysLowerCase)
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
 
            StringBuilder query = new StringBuilder().append("SELECT count(mailbox.pguid) FROM MailBox mailbox");

            boolean isNameAdded = false;
            if (!MailBoxUtil.isEmpty(searchFilter.getMbxName())) {
                query.append(" WHERE (lower(mailbox.mbxName) LIKE :").append(MBOX_NAME).append(")");
                isNameAdded = true;
            }

            if (!searchFilter.isDisableFilters()) {
                query.append(isNameAdded ?  " AND" : " WHERE");
                query.append(" (mailbox.tenancyKey IN (:" + TENANCY_KEYS + "))");
            }

            Query jpaQuery = entityManager.createQuery(query.toString());
            if (!MailBoxUtil.isEmpty(searchFilter.getMbxName())) {
                jpaQuery = jpaQuery.setParameter(MBOX_NAME, "%" + searchFilter.getMbxName().toLowerCase() + "%");
            }
            if (!searchFilter.isDisableFilters()) {
                jpaQuery = jpaQuery.setParameter(TENANCY_KEYS, tenancyKeys);
            }

            totalItems = ((Long) jpaQuery.getSingleResult());
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
		List<String> tenancyKeysLowerCase = null;

		try {

			StringBuilder query = new StringBuilder().append("SELECT mbx FROM MailBox mbx");
			
			 if (searchFilter.isDisableFilters()){
				 if(!MailBoxUtil.isEmpty(searchFilter.getMbxName())) {
					 query.append(" where LOWER(mbx.mbxName) like :")
					 	  .append(MBOX_NAME);
				 }
			 } else {
			     tenancyKeysLowerCase = tenancyKeys.stream().map(String::toLowerCase).collect(Collectors.toList());
				 query.append(" where LOWER(mbx.mbxName) like :")
				 	.append(MBOX_NAME)
				 	.append(" and LOWER(mbx.tenancyKey) IN (:")
					.append(TENANCY_KEYS)
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
						.setParameter(TENANCY_KEYS, tenancyKeysLowerCase)
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

			mailboxList = entityManager.createNamedQuery(FIND_BY_MBX_NAME_AND_TENANCY_KEY_NAME)
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
							.append(" where mailbox.tenancyKey in (:")
							.append(TENANCY_KEYS)
			                .append(")");
			List<?> mailboxIds = entityManager.createQuery(query.toString())
			            .setParameter(TENANCY_KEYS, tenancyKeys)    
			            .getResultList();
			
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