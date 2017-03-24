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
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.JoinType;

import com.liaison.commons.jpa.DAOUtil;
import com.liaison.commons.jpa.GenericDAOBase;
import com.liaison.commons.util.client.sftp.StringUtil;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.dtdm.model.MailBox;
import com.liaison.mailbox.dtdm.model.MailboxServiceInstance;
import com.liaison.mailbox.dtdm.model.Processor;
import com.liaison.mailbox.dtdm.model.ScheduleProfileProcessor;
import com.liaison.mailbox.dtdm.model.ScheduleProfilesRef;
import com.liaison.mailbox.dtdm.model.ServiceInstance;
import com.liaison.mailbox.enums.EntityStatus;
import com.liaison.mailbox.service.dto.GenericSearchFilterDTO;
import com.liaison.mailbox.service.util.MailBoxUtil;

/**
 * Performs mailbox fetch operations. 
 * 
 * @author OFS

 */
public class MailBoxConfigurationDAOBase extends GenericDAOBase<MailBox>
		implements MailBoxConfigurationDAO, MailboxDTDMDAO  {

    private static final String NAME = "name";
    private static final String DESCRIPTION = "description";
    private static final String STATUS = "status";
    private static final String MBX_SERVICE_INSTANCES= "mailboxServiceInstances";
    private static final String SERVICE_INSTANCE= "serviceInstance";
    private static final String TENANCY_KEY = "tenancyKey";
    private static final String MBX_NAME = "mbxName";
    private static final String MBX_STATUS = "mbxStatus";
    private static final String MBX_ROCESSORS  ="mailboxProcessors";
    private static final String SCHEDULE_PROFILE_PROCESSORS  ="scheduleProfileProcessors";
    private static final String SCHEDULE_PROFILES_REF ="scheduleProfilesRef";
    private static final String SCH_PROF_NAME = "schProfName";
    private static final String MBX_DESC = "mbxDesc";
    private static final String SORT_DIR_DESC = "DESC";

    public MailBoxConfigurationDAOBase() {
		super(PERSISTENCE_UNIT_NAME);

	}

	/**
	 * Fetches the count of all MailBoxes from  MAILBOX database table by given mailbox name, profile name and service instance ids.
	 *
	 * @param searchFilter
	 * @param tenancyKeys
	 * @return count of mailbox retrieved
	 */
	@Override
	public int getMailboxCountByProfile(GenericSearchFilterDTO searchFilter, List <String> tenancyKeys) {

		int count = 0;

        EntityManager em = null;
		try {

            em = DAOUtil.getEntityManager(persistenceUnitName);
            CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
            CriteriaQuery<Long> query = criteriaBuilder.createQuery(Long.class);
            Root<MailBox> fromMailBox = query.from(MailBox.class);
            
            Join<MailBox, Processor> joinProcessor = null;
            Join<Processor, ScheduleProfileProcessor> joinScheduleProfileProcessor = null;
            Join<ScheduleProfileProcessor, ScheduleProfilesRef> joinScheduleProfilesRef = null;
            Join<MailBox, MailboxServiceInstance> joinMailboxServiceInstance = null;
            Join<MailboxServiceInstance, ServiceInstance> joinServiceInstance = null;
            
            joinProcessor = fromMailBox.join(MBX_ROCESSORS, JoinType.INNER);
            joinScheduleProfileProcessor = joinProcessor.join(SCHEDULE_PROFILE_PROCESSORS, JoinType.INNER);
            joinScheduleProfilesRef = joinScheduleProfileProcessor.join(SCHEDULE_PROFILES_REF, JoinType.INNER);
            if (!searchFilter.isMinResponse() && !MailBoxUtil.isEmpty(searchFilter.getServiceInstanceId())) {
                joinMailboxServiceInstance = fromMailBox.join(MBX_SERVICE_INSTANCES, JoinType.INNER);
                joinServiceInstance = joinMailboxServiceInstance.join(SERVICE_INSTANCE, JoinType.INNER);
            }
            
            String mbxName = searchFilter.getMbxName();
            String profName = searchFilter.getProfileName();
            
            List<Predicate> predicates = new ArrayList<Predicate>();
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(fromMailBox.get(MBX_NAME)), "%" + (mbxName == null ? "" : mbxName.toLowerCase()) + "%"));
            predicates.add(criteriaBuilder.like(joinScheduleProfilesRef.get(SCH_PROF_NAME), "%" + (profName == null ? "" : profName) + "%"));
            predicates.add(criteriaBuilder.notEqual(fromMailBox.get(MBX_STATUS), EntityStatus.DELETED.value()));
            
            if (!searchFilter.isDisableFilters()) {
                predicates.add(fromMailBox.get(TENANCY_KEY).in(tenancyKeys));
            }
            
            if (!searchFilter.isMinResponse() && null != joinServiceInstance) {
                predicates.add(criteriaBuilder.equal(joinServiceInstance.get(NAME), searchFilter.getServiceInstanceId()));
            }
            
            TypedQuery<Long> tQueryCount = em.createQuery(query
                    .select(criteriaBuilder.count(fromMailBox))
                    .where(predicates.toArray(new Predicate[] {})));
            
            count = tQueryCount.getSingleResult().intValue();

		} finally {
			if (em != null) {
                em.close();
			}
		}
		return count;
	}

	/**
	 * Fetches all MailBox from  MAILBOX database table by given mailbox name, profile name and service instance ids.
	 *
	 * @param searchFilter
	 * @param tenancyKeys
     * @param pageOffsetDetails
	 * @return list of mailbox
	 */
	@Override
	public List<MailBox> find(GenericSearchFilterDTO searchFilter, List <String> tenancyKeys,Map <String, Integer> pageOffsetDetails) {

		List<MailBox> mailBoxes = null;
		List<String> tenancyKeysLowerCase = null;
        EntityManager em = null;
		try {

            em = DAOUtil.getEntityManager(persistenceUnitName);
            CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
            CriteriaQuery<MailBox> query = criteriaBuilder.createQuery(MailBox.class);
            Root<MailBox> fromMailBox = query.from(MailBox.class);
            
            Join<MailBox, Processor> joinProcessor = null;
            Join<Processor, ScheduleProfileProcessor> joinScheduleProfileProcessor = null;
            Join<ScheduleProfileProcessor, ScheduleProfilesRef> joinScheduleProfilesRef = null;
            Join<MailBox, MailboxServiceInstance> joinMailboxServiceInstance = null;
            Join<MailboxServiceInstance, ServiceInstance> joinServiceInstance = null;
            
            joinProcessor = fromMailBox.join(MBX_ROCESSORS, JoinType.INNER);
            joinScheduleProfileProcessor = joinProcessor.join(SCHEDULE_PROFILE_PROCESSORS, JoinType.INNER);
            joinScheduleProfilesRef = joinScheduleProfileProcessor.join(SCHEDULE_PROFILES_REF, JoinType.INNER);
            
            if (!searchFilter.isMinResponse() && !MailBoxUtil.isEmpty(searchFilter.getServiceInstanceId())) {
                joinMailboxServiceInstance = fromMailBox.join(MBX_SERVICE_INSTANCES, JoinType.INNER);
                joinServiceInstance = joinMailboxServiceInstance.join(SERVICE_INSTANCE, JoinType.INNER);
            }

            String mbxName = searchFilter.getMbxName();
            String profName = searchFilter.getProfileName();

            List<Predicate> predicates = new ArrayList<>();
			predicates.add(criteriaBuilder.like(criteriaBuilder.lower(fromMailBox.get(MBX_NAME)), "%" + (mbxName == null ? "" : mbxName.toLowerCase()) + "%"));
			predicates.add(criteriaBuilder.like(joinScheduleProfilesRef.get(SCH_PROF_NAME), "%" + (profName == null ? "" : profName) + "%"));
			predicates.add(criteriaBuilder.notEqual(fromMailBox.get(MBX_STATUS), EntityStatus.DELETED.value()));
			
			if (!searchFilter.isDisableFilters()) {
			    tenancyKeysLowerCase = tenancyKeys.stream().map(String::toLowerCase).collect(Collectors.toList());
			    predicates.add(criteriaBuilder.lower(fromMailBox.get(TENANCY_KEY)).in(tenancyKeysLowerCase));
			}
			
			if (!searchFilter.isMinResponse() && null != joinServiceInstance) {
			    predicates.add(criteriaBuilder.equal(joinServiceInstance.get(NAME), searchFilter.getServiceInstanceId()));
			}
			
			TypedQuery<MailBox> tQuery = em.createQuery(query
			        .select(fromMailBox)
			        .where(predicates.toArray(new Predicate[] {}))
			        .orderBy(isDescendingSort(searchFilter.getSortDirection())
			                ? criteriaBuilder.desc(fromMailBox.get(getSortField(searchFilter.getSortField())))
			                : criteriaBuilder.asc(fromMailBox.get(getSortField(searchFilter.getSortField())))));
			
			mailBoxes = tQuery.setFirstResult(pageOffsetDetails.get(MailBoxConstants.PAGING_OFFSET))
			        .setMaxResults( pageOffsetDetails.get(MailBoxConstants.PAGING_COUNT))
			        .getResultList();

		} finally {
			if (em != null) {
                em.close();
			}
		}
		return mailBoxes;
	}

	/**
	 * Method to get sort field.
	 * 
	 * @param sortField
	 * @return sortField
	 */
    private String getSortField(String sortField) {

        String field = null;
        if (!StringUtil.isNullOrEmptyAfterTrim(sortField)) {

            switch (sortField.toLowerCase()) {
                case NAME:
                    field = MBX_NAME;
                    break;
                case DESCRIPTION:
                    field = MBX_DESC;
                    break;
                case STATUS:
                    field = MBX_STATUS;
                    break;
            }
        } else {
            field = MBX_NAME;
        }
        return field;
    }
    
    /**
     * Method to check the sort direction.
     * 
     * @param sortDirection
     * @return boolean
     */
    private boolean isDescendingSort(String sortDirection) {
        return !StringUtil.isNullOrEmptyAfterTrim(sortDirection) && sortDirection.toUpperCase().equals(SORT_DIR_DESC);
    }

    /**
	 * Fetches the count of all  MailBoxes from  MAILBOX database table by given mailbox name.
	 *
	 * @param searchFilter
     * @param tenancyKeys
	 * @return count of mailboxes retrieved
	 */
	@Override
	public int getMailboxCountByName(GenericSearchFilterDTO searchFilter, List<String> tenancyKeys) {
		 
        EntityManager entityManager = null;
        int count = 0;
 		boolean isMatchModeEquals = !searchFilter.getMatchMode().equals(GenericSearchFilterDTO.MATCH_MODE_LIKE);

        try {

            entityManager = DAOUtil.getEntityManager(persistenceUnitName);
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<Long> query = criteriaBuilder.createQuery(Long.class);
            Root<MailBox> fromMailBox = query.from(MailBox.class);
            
            Join<MailBox, MailboxServiceInstance> joinMailboxServiceInstance = null;
            Join<MailboxServiceInstance, ServiceInstance> joinServiceInstance = null;
            if (!searchFilter.isMinResponse() && !MailBoxUtil.isEmpty(searchFilter.getServiceInstanceId())) {
                joinMailboxServiceInstance = fromMailBox.join(MBX_SERVICE_INSTANCES, JoinType.INNER);
                joinServiceInstance = joinMailboxServiceInstance.join(SERVICE_INSTANCE, JoinType.INNER);
            }
            
            List<Predicate> predicates = new ArrayList<>();
            if (!MailBoxUtil.isEmpty(searchFilter.getMbxName())) {
                if (isMatchModeEquals) {
                    predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(fromMailBox.get(MBX_NAME)), searchFilter.getMbxName().toLowerCase()));
                } else {
                    predicates.add(criteriaBuilder.like(criteriaBuilder.lower(fromMailBox.get(MBX_NAME)), "%"+searchFilter.getMbxName().toLowerCase()+"%"));
                }
            }

            if (!searchFilter.isDisableFilters()) {
                predicates.add(fromMailBox.get(TENANCY_KEY).in(tenancyKeys));
            }

            predicates.add(criteriaBuilder.notEqual(fromMailBox.get(MBX_STATUS), EntityStatus.DELETED.value()));
            
            if (!searchFilter.isMinResponse() && null != joinServiceInstance) {
                predicates.add(criteriaBuilder.equal(joinServiceInstance.get(NAME), searchFilter.getServiceInstanceId()));
            }
            
            TypedQuery<Long> tQueryCount = entityManager.createQuery(query
                    .select(criteriaBuilder.count(fromMailBox))
                    .where(predicates.toArray(new Predicate[] {})));

            count = tQueryCount.getSingleResult().intValue();

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
	 * @param searchFilter
     * @param tenancyKeys
     * @param pageOffsetDetails
	 * @return list of mailbox
	 */
	@Override
	public List<MailBox> findByName(GenericSearchFilterDTO searchFilter, List<String> tenancyKeys, Map <String, Integer> pageOffsetDetails) {

        EntityManager entityManager = null;
		List<MailBox> mailboxList = null;
		List<String> tenancyKeysLowerCase = null;
		boolean isMatchModeEquals = !searchFilter.getMatchMode().equals(GenericSearchFilterDTO.MATCH_MODE_LIKE);

		try {

            entityManager = DAOUtil.getEntityManager(persistenceUnitName);
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<MailBox> query = criteriaBuilder.createQuery(MailBox.class);
            Root<MailBox> fromMailBox = query.from(MailBox.class);

            Join<MailBox, MailboxServiceInstance> joinMailboxServiceInstance = null;
            Join<MailboxServiceInstance, ServiceInstance> joinServiceInstance = null;
            if (!searchFilter.isMinResponse() && !MailBoxUtil.isEmpty(searchFilter.getServiceInstanceId())) {
                joinMailboxServiceInstance = fromMailBox.join(MBX_SERVICE_INSTANCES, JoinType.INNER);
                joinServiceInstance = joinMailboxServiceInstance.join(SERVICE_INSTANCE, JoinType.INNER);
            }

            List<Predicate> predicates = new ArrayList<>();
            if (searchFilter.isDisableFilters()) {
                if(!MailBoxUtil.isEmpty(searchFilter.getMbxName())) {
                    if (isMatchModeEquals) {
                        predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(fromMailBox.get(MBX_NAME)), searchFilter.getMbxName().toLowerCase()));
                    } else {
                        predicates.add(criteriaBuilder.like(criteriaBuilder.lower(fromMailBox.get(MBX_NAME)), "%"+searchFilter.getMbxName().toLowerCase()+"%"));
                    }
                }
            } else {
                tenancyKeysLowerCase = tenancyKeys.stream().map(String::toLowerCase).collect(Collectors.toList());
                if (isMatchModeEquals) {
                    predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(fromMailBox.get(MBX_NAME)), searchFilter.getMbxName().toLowerCase()));
                } else {
                    predicates.add(criteriaBuilder.like(criteriaBuilder.lower(fromMailBox.get(MBX_NAME)), "%"+searchFilter.getMbxName().toLowerCase()+"%"));
                }
                predicates.add(criteriaBuilder.lower(fromMailBox.get(TENANCY_KEY)).in(tenancyKeysLowerCase));
            }

            predicates.add(criteriaBuilder.notEqual(fromMailBox.get(MBX_STATUS), EntityStatus.DELETED.value()));

            if (!searchFilter.isMinResponse() && null != joinServiceInstance) {
                predicates.add(criteriaBuilder.equal(joinServiceInstance.get(NAME), searchFilter.getServiceInstanceId()));
            }

            TypedQuery<MailBox> tQuery = entityManager.createQuery(query
                    .select(fromMailBox)
                    .where(predicates.toArray(new Predicate[] {}))
                    .orderBy(isDescendingSort(searchFilter.getSortDirection())
                            ? criteriaBuilder.desc(fromMailBox.get(getSortField(searchFilter.getSortField())))
                            : criteriaBuilder.asc(fromMailBox.get(getSortField(searchFilter.getSortField())))));

            mailboxList = tQuery.setFirstResult(pageOffsetDetails.get(MailBoxConstants.PAGING_OFFSET))
                    .setMaxResults( pageOffsetDetails.get(MailBoxConstants.PAGING_COUNT))
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

        EntityManager entityManager = null;
		List<MailBox> mailboxList = null;
		MailBox appEntity = null;

		try {

            entityManager = DAOUtil.getEntityManager(persistenceUnitName);
			mailboxList = entityManager.createNamedQuery(FIND_BY_MBX_NAME_AND_TENANCY_KEY_NAME)
					.setParameter(MBOX_NAME,  (MailBoxUtil.isEmpty(mbxName) ? "''" : mbxName))
					.setParameter(TENANCY_KEYS, (MailBoxUtil.isEmpty(tenancyKeyName) ? "''" : tenancyKeyName))
					.setParameter(STATUS, EntityStatus.DELETED.value())
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
        EntityManager entityManager = null;
		
		try {

            entityManager = DAOUtil.getEntityManager(persistenceUnitName);
			StringBuilder query = new StringBuilder().append("select mailbox.pguid from MailBox mailbox")
							.append(" where mailbox.tenancyKey in (:")
							.append(TENANCY_KEYS)
			                .append(")")
			                .append(" and mailbox.mbxStatus <> :"+ MailBoxConfigurationDAO.STATUS);
			List<?> mailboxIds = entityManager.createQuery(query.toString())
			            .setParameter(TENANCY_KEYS, tenancyKeys)
			            .setParameter(STATUS, EntityStatus.DELETED.value())
			            .getResultList();

            for (Object mailboxId1 : mailboxIds) {

                String mailboxId = (String) mailboxId1;
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
		
        EntityManager entityManager = null;
		List<MailBox> mailboxList = null;
		MailBox appEntity = null;

		try {

            entityManager = DAOUtil.getEntityManager(persistenceUnitName);
			mailboxList = entityManager.createNamedQuery(GET_MBX_BY_NAME)
					.setParameter(MBOX_NAME, (MailBoxUtil.isEmpty(mbxName) ? "''" : mbxName))
					.setParameter(STATUS, EntityStatus.DELETED.value())
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
    public MailBox find(Class<MailBox> entityClass, Object primaryKey) {

        EntityManager entityManager = null;
        try {

            entityManager = DAOUtil.getEntityManager(persistenceUnitName);
            MailBox entity = DAOUtil.find(entityClass, primaryKey, entityManager);
            if (entity != null && EntityStatus.DELETED.name().equals(entity.getMbxStatus())) {
                entity = null;
            }
            return entity;
        } finally {
            if (entityManager != null) {
                entityManager.close();
            }
        }
    }
}