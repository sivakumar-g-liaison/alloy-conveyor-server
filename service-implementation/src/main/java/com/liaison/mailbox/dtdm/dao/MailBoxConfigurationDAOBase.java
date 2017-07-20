/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.dtdm.dao;

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

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Performs mailbox fetch operations. 
 *
 * @author OFS

 */
public class MailBoxConfigurationDAOBase extends GenericDAOBase<MailBox>
        implements MailBoxConfigurationDAO, MailboxDTDMDAO {

    private static final String NAME = "name";
    private static final String DESCRIPTION = "description";
    private static final String STATUS = "status";
    private static final String MBX_CLUSTER_TYPE = "clustertype";
    private static final String MBX_SERVICE_INSTANCES = "mailboxServiceInstances";
    private static final String SERVICE_INSTANCE = "serviceInstance";
    private static final String TENANCY_KEY = "tenancyKey";
    private static final String MBX_NAME = "mbxName";
    private static final String MBX_STATUS = "mbxStatus";
    private static final String MBX_ROCESSORS = "mailboxProcessors";
    private static final String SCHEDULE_PROFILE_PROCESSORS = "scheduleProfileProcessors";
    private static final String SCHEDULE_PROFILES_REF = "scheduleProfilesRef";
    private static final String SCH_PROF_NAME = "schProfName";
    private static final String MBX_DESC = "mbxDesc";
    private static final String SORT_DIR_DESC = "DESC";
    private static final String TENANCY_KEY_VALUE = "tenancykey";

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
    public int getMailboxCountByProfile(GenericSearchFilterDTO searchFilter, List<String> tenancyKeys) {

        int count;

        EntityManager em = null;
        try {

            em = DAOUtil.getEntityManager(persistenceUnitName);
            CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
            CriteriaQuery<Long> query = criteriaBuilder.createQuery(Long.class);
            Root<MailBox> fromMailBox = query.from(MailBox.class);

            Join<MailBox, Processor> joinProcessor;
            Join<Processor, ScheduleProfileProcessor> joinScheduleProfileProcessor;
            Join<ScheduleProfileProcessor, ScheduleProfilesRef> joinScheduleProfilesRef;
            Join<MailBox, MailboxServiceInstance> joinMailboxServiceInstance;
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
            String clusterType = searchFilter.getClusterType();
            String status = searchFilter.getStatus();
            
            List<Predicate> predicates = new ArrayList<Predicate>();
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(fromMailBox.get(MBX_NAME)), safeLikeParameter(mbxName)));
            predicates.add(criteriaBuilder.like(joinScheduleProfilesRef.get(SCH_PROF_NAME), "%" + (profName == null ? "" : profName) + "%"));
            
            if (!MailBoxUtil.isEmpty(status)) {
                predicates.add(criteriaBuilder.equal(fromMailBox.get(MBX_STATUS), status));
            } else {
                predicates.add(criteriaBuilder.notEqual(fromMailBox.get(MBX_STATUS), EntityStatus.DELETED.value()));
            }
            
            if (!MailBoxUtil.isEmpty(clusterType)) {
                predicates.add(criteriaBuilder.equal(fromMailBox.get(MailBoxConstants.CLUSTER_TYPE), clusterType));
            } else {
                predicates.add(fromMailBox.get(MailBoxConstants.CLUSTER_TYPE).in(MailBoxUtil.getClusterTypes()));
            }
           
            if (!searchFilter.isDisableFilters()) {
                predicates.add(fromMailBox.get(TENANCY_KEY).in(tenancyKeys));
            }

            if (!searchFilter.isMinResponse() && null != joinServiceInstance) {
                predicates.add(criteriaBuilder.equal(joinServiceInstance.get(NAME), searchFilter.getServiceInstanceId()));
            }

            TypedQuery<Long> tQueryCount = em.createQuery(query
                    .select(criteriaBuilder.count(fromMailBox))
                    .where(predicates.toArray(new Predicate[]{}))
                    .distinct(true));

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
    public List<MailBox> find(GenericSearchFilterDTO searchFilter, List<String> tenancyKeys, Map<String, Integer> pageOffsetDetails) {

        List<MailBox> mailBoxes;
        List<String> tenancyKeysLowerCase;
        EntityManager em = null;
        try {

            em = DAOUtil.getEntityManager(persistenceUnitName);
            CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
            CriteriaQuery<MailBox> query = criteriaBuilder.createQuery(MailBox.class);
            Root<MailBox> fromMailBox = query.from(MailBox.class);

            Join<MailBox, Processor> joinProcessor;
            Join<Processor, ScheduleProfileProcessor> joinScheduleProfileProcessor;
            Join<ScheduleProfileProcessor, ScheduleProfilesRef> joinScheduleProfilesRef;
            Join<MailBox, MailboxServiceInstance> joinMailboxServiceInstance;
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
            String clusterType = searchFilter.getClusterType();
            String status = searchFilter.getStatus();

            List<Predicate> predicates = new ArrayList<>();
			predicates.add(criteriaBuilder.like(criteriaBuilder.lower(fromMailBox.get(MBX_NAME)), safeLikeParameter(mbxName)));
			predicates.add(criteriaBuilder.like(joinScheduleProfilesRef.get(SCH_PROF_NAME), "%" + (profName == null ? "" : profName) + "%"));

            if (!MailBoxUtil.isEmpty(clusterType)) {
                predicates.add(criteriaBuilder.equal(fromMailBox.get(MailBoxConstants.CLUSTER_TYPE), clusterType));
            } else {
                predicates.add(fromMailBox.get(MailBoxConstants.CLUSTER_TYPE).in(MailBoxUtil.getClusterTypes()));
            }
			
            if (!MailBoxUtil.isEmpty(status)) {
                predicates.add(criteriaBuilder.equal(fromMailBox.get(MBX_STATUS), status));
            } else {
                predicates.add(criteriaBuilder.notEqual(fromMailBox.get(MBX_STATUS), EntityStatus.DELETED.value()));
            }
            
			if (!searchFilter.isDisableFilters()) {
			    tenancyKeysLowerCase = tenancyKeys.stream().map(String::toLowerCase).collect(Collectors.toList());
			    predicates.add(criteriaBuilder.lower(fromMailBox.get(TENANCY_KEY)).in(tenancyKeysLowerCase));
			}
			
			if (!searchFilter.isMinResponse() && null != joinServiceInstance) {
			    predicates.add(criteriaBuilder.equal(joinServiceInstance.get(NAME), searchFilter.getServiceInstanceId()));
			}

            TypedQuery<MailBox> tQuery = em.createQuery(query
                    .select(fromMailBox)
                    .where(predicates.toArray(new Predicate[]{}))
                    .distinct(true)
                    .orderBy(isDescendingSort(searchFilter.getSortDirection())
                            ? criteriaBuilder.desc(fromMailBox.get(getSortField(searchFilter.getSortField())))
                            : criteriaBuilder.asc(fromMailBox.get(getSortField(searchFilter.getSortField())))));

            mailBoxes = tQuery.setFirstResult(pageOffsetDetails.get(MailBoxConstants.PAGING_OFFSET))
                    .setMaxResults(pageOffsetDetails.get(MailBoxConstants.PAGING_COUNT))
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
                case MBX_CLUSTER_TYPE:
                    field = MailBoxConstants.CLUSTER_TYPE;
                    break;
                case TENANCY_KEY_VALUE:
                    field = TENANCY_KEY;
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
    
    private String safeLikeParameter(String name) {
        return "%" + (name == null ? "" : name.toLowerCase()) + "%";
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
        int count;
        boolean isMatchModeEquals = !searchFilter.getMatchMode().equals(GenericSearchFilterDTO.MATCH_MODE_LIKE);

        try {

            entityManager = DAOUtil.getEntityManager(persistenceUnitName);
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<Long> query = criteriaBuilder.createQuery(Long.class);
            Root<MailBox> fromMailBox = query.from(MailBox.class);

            Join<MailBox, MailboxServiceInstance> joinMailboxServiceInstance;
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
                    predicates.add(criteriaBuilder.like(criteriaBuilder.lower(fromMailBox.get(MBX_NAME)), safeLikeParameter(searchFilter.getMbxName().toLowerCase())));
                }
            }

            if (!searchFilter.isDisableFilters()) {
                predicates.add(fromMailBox.get(TENANCY_KEY).in(tenancyKeys));
            }
            
            String clusterType = searchFilter.getClusterType();
            String status = searchFilter.getStatus();
            
            if (!MailBoxUtil.isEmpty(clusterType)) {
                predicates.add(criteriaBuilder.equal(fromMailBox.get(MailBoxConstants.CLUSTER_TYPE), clusterType));
            } else {
                predicates.add(fromMailBox.get(MailBoxConstants.CLUSTER_TYPE).in(MailBoxUtil.getClusterTypes()));
            }
            
            if (!MailBoxUtil.isEmpty(status)) {
                predicates.add(criteriaBuilder.equal(fromMailBox.get(MBX_STATUS), status));
            } else {
                predicates.add(criteriaBuilder.notEqual(fromMailBox.get(MBX_STATUS), EntityStatus.DELETED.value()));
            }

            if (!searchFilter.isMinResponse() && null != joinServiceInstance) {
                predicates.add(criteriaBuilder.equal(joinServiceInstance.get(NAME), searchFilter.getServiceInstanceId()));
            }

            TypedQuery<Long> tQueryCount = entityManager.createQuery(query
                    .select(criteriaBuilder.count(fromMailBox))
                    .where(predicates.toArray(new Predicate[]{}))
                    .distinct(true));

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
    public List<MailBox> findByName(GenericSearchFilterDTO searchFilter, List<String> tenancyKeys, Map<String, Integer> pageOffsetDetails) {

        EntityManager entityManager = null;
        List<MailBox> mailboxList;
        List<String> tenancyKeysLowerCase;
        boolean isMatchModeEquals = !searchFilter.getMatchMode().equals(GenericSearchFilterDTO.MATCH_MODE_LIKE);

        try {

            entityManager = DAOUtil.getEntityManager(persistenceUnitName);
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<MailBox> query = criteriaBuilder.createQuery(MailBox.class);
            Root<MailBox> fromMailBox = query.from(MailBox.class);

            Join<MailBox, MailboxServiceInstance> joinMailboxServiceInstance;
            Join<MailboxServiceInstance, ServiceInstance> joinServiceInstance = null;
            if (!searchFilter.isMinResponse() && !MailBoxUtil.isEmpty(searchFilter.getServiceInstanceId())) {
                joinMailboxServiceInstance = fromMailBox.join(MBX_SERVICE_INSTANCES, JoinType.INNER);
                joinServiceInstance = joinMailboxServiceInstance.join(SERVICE_INSTANCE, JoinType.INNER);
            }

            List<Predicate> predicates = new ArrayList<>();
            if (searchFilter.isDisableFilters()) {
                if (!MailBoxUtil.isEmpty(searchFilter.getMbxName())) {
                    if (isMatchModeEquals) {
                        predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(fromMailBox.get(MBX_NAME)), searchFilter.getMbxName().toLowerCase()));
                    } else {
                        predicates.add(criteriaBuilder.like(criteriaBuilder.lower(fromMailBox.get(MBX_NAME)), safeLikeParameter(searchFilter.getMbxName().toLowerCase())));
                    }
                }
            } else {
                tenancyKeysLowerCase = tenancyKeys.stream().map(String::toLowerCase).collect(Collectors.toList());
                if (isMatchModeEquals) {
                    predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(fromMailBox.get(MBX_NAME)), searchFilter.getMbxName().toLowerCase()));
                } else {
                    predicates.add(criteriaBuilder.like(criteriaBuilder.lower(fromMailBox.get(MBX_NAME)), safeLikeParameter(searchFilter.getMbxName().toLowerCase())));
                }
                predicates.add(criteriaBuilder.lower(fromMailBox.get(TENANCY_KEY)).in(tenancyKeysLowerCase));
            }
           
            String clusterType = searchFilter.getClusterType();
            String status = searchFilter.getStatus();
            
            if (!MailBoxUtil.isEmpty(clusterType)) {
                predicates.add(criteriaBuilder.equal(fromMailBox.get(MailBoxConstants.CLUSTER_TYPE), clusterType));
            } else {
                predicates.add(fromMailBox.get(MailBoxConstants.CLUSTER_TYPE).in(MailBoxUtil.getClusterTypes()));
            }
            
            if (!MailBoxUtil.isEmpty(status)) {
                predicates.add(criteriaBuilder.equal(fromMailBox.get(MBX_STATUS), status));
            } else {
                predicates.add(criteriaBuilder.notEqual(fromMailBox.get(MBX_STATUS), EntityStatus.DELETED.value()));
            }

            if (!searchFilter.isMinResponse() && null != joinServiceInstance) {
                predicates.add(criteriaBuilder.equal(joinServiceInstance.get(NAME), searchFilter.getServiceInstanceId()));
            }

            TypedQuery<MailBox> tQuery = entityManager.createQuery(query
                    .select(fromMailBox)
                    .where(predicates.toArray(new Predicate[]{}))
                    .distinct(true)
                    .orderBy(isDescendingSort(searchFilter.getSortDirection())
                            ? criteriaBuilder.desc(fromMailBox.get(getSortField(searchFilter.getSortField())))
                            : criteriaBuilder.asc(fromMailBox.get(getSortField(searchFilter.getSortField())))));

            mailboxList = tQuery.setFirstResult(pageOffsetDetails.get(MailBoxConstants.PAGING_OFFSET))
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

        EntityManager entityManager = null;
        List<MailBox> mailboxList;
        MailBox appEntity;

        try {

            entityManager = DAOUtil.getEntityManager(persistenceUnitName);
            mailboxList = entityManager.createNamedQuery(FIND_BY_MBX_NAME_AND_TENANCY_KEY_NAME)
                    .setParameter(MBOX_NAME, (MailBoxUtil.isEmpty(mbxName) ? "''" : mbxName))
                    .setParameter(TENANCY_KEYS, (MailBoxUtil.isEmpty(tenancyKeyName) ? "''" : tenancyKeyName))
                    .setParameter(STATUS, EntityStatus.DELETED.value())
                    .setParameter(MailBoxConstants.CLUSTER_TYPE, MailBoxUtil.getClusterTypes())
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

        List<String> linkedMailboxIds = new ArrayList<>();
        EntityManager entityManager = null;

        try {

            entityManager = DAOUtil.getEntityManager(persistenceUnitName);
            String query = "SELECT mailbox.pguid FROM MailBox mailbox" +
                    " WHERE mailbox.tenancyKey IN (:" + TENANCY_KEYS + ")" +
                    " AND mailbox.mbxStatus <> :" + MailBoxConfigurationDAO.STATUS +
                    " AND mailbox.clusterType = :" + MailBoxConstants.CLUSTER_TYPE;

            List<?> mailboxIds = entityManager.createQuery(query)
                    .setParameter(TENANCY_KEYS, tenancyKeys)
                    .setParameter(STATUS, EntityStatus.DELETED.value())
                    .setParameter(MailBoxConstants.CLUSTER_TYPE, MailBoxUtil.CLUSTER_TYPE)
                    .getResultList();

            for (Object mailboxId : mailboxIds) {
                linkedMailboxIds.add((String) mailboxId);
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
        List<MailBox> mailboxList;
        MailBox appEntity;

        try {

            entityManager = DAOUtil.getEntityManager(persistenceUnitName);
            mailboxList = entityManager.createNamedQuery(GET_MBX_BY_NAME)
                    .setParameter(MBOX_NAME, (MailBoxUtil.isEmpty(mbxName) ? "''" : mbxName))
                    .setParameter(STATUS, EntityStatus.DELETED.value())
                    .setParameter(MailBoxConstants.CLUSTER_TYPE, MailBoxUtil.getClusterTypes())
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

            if (entity != null) {
                if (EntityStatus.DELETED.name().equals(entity.getMbxStatus())) {
                    return null;
                }

                if (MailBoxConstants.LOWSECURE.equals(MailBoxUtil.CLUSTER_TYPE)
                        && !MailBoxUtil.CLUSTER_TYPE.equals(entity.getClusterType())) {
                    return null;
                }
            }
            return entity;
        } finally {
            if (entityManager != null) {
                entityManager.close();
            }
        }
    }

    @Override
    public String getClusterType(String mailboxId) {

        EntityManager entityManager = null;
        String clusterType = null;

        try {

            entityManager = DAOUtil.getEntityManager(persistenceUnitName);
            clusterType = entityManager.createNamedQuery(GET_CLUSTER_TYPE_BY_MAILBOX_GUID, String.class)
                    .setParameter(PGUID, mailboxId)
                    .getSingleResult();

        } finally {
            if (entityManager != null) {
                entityManager.close();
            }
        }
        return clusterType;
    }
}