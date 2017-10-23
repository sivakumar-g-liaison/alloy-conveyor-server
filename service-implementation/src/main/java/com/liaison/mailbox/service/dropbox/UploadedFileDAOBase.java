/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.dropbox;

import com.liaison.commons.jpa.DAOUtil;
import com.liaison.commons.jpa.GenericDAOBase;
import com.liaison.commons.util.StringUtil;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.rtdm.dao.MailboxRTDMDAO;
import com.liaison.mailbox.rtdm.dao.UploadedFileDAO;
import com.liaison.mailbox.rtdm.model.UploadedFile;
import com.liaison.mailbox.service.dto.GenericSearchFilterDTO;
import com.liaison.mailbox.service.util.MailBoxUtil;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * This will fetch the uploaded file details.
 */
public class UploadedFileDAOBase extends GenericDAOBase<UploadedFile> implements UploadedFileDAO, MailboxRTDMDAO {

    public UploadedFileDAOBase() {
        super(PERSISTENCE_UNIT_NAME);
    }

    @Override
    public int getUploadedFilesCountByUserId(String loginId, String fileName) {

        EntityManager entityManager = null;
        int count;

        try {

            entityManager = DAOUtil.getEntityManager(persistenceUnitName);

            //by using criteria
            boolean nameIsEmpty = MailBoxUtil.isEmpty(fileName);
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
            Root<UploadedFile> fromUploadedFile = criteriaQuery.from(UploadedFile.class);

            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(fromUploadedFile.get("userId"), loginId));
            predicates.add(criteriaBuilder.greaterThan(fromUploadedFile.get("expiryDate"), new Date()));
            if (!nameIsEmpty) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(fromUploadedFile.get("fileName")), fileName.toLowerCase() + "%"));
            }

            TypedQuery<Long> tQueryCount = entityManager.createQuery(criteriaQuery
                    .select(criteriaBuilder.count(fromUploadedFile))
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

    @Override
    public List<UploadedFile> fetchUploadedFiles(String loginId,
                                                 GenericSearchFilterDTO searchFilter,
                                                 Map<String, Integer> pageOffsetDetails) {

        EntityManager entityManager = null;
        List<UploadedFile> uploadedFiles;

        try {

            entityManager = DAOUtil.getEntityManager(persistenceUnitName);

            //Named query execution
            // get Search Filters
            String fileName = searchFilter.getUploadedFileName();
            boolean nameIsEmpty = MailBoxUtil.isEmpty(fileName);

            //by using criteria
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<UploadedFile> criteriaQuery = criteriaBuilder.createQuery(UploadedFile.class);
            Root<UploadedFile> fromUploadedFile = criteriaQuery.from(UploadedFile.class);

            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(fromUploadedFile.get(USER_ID), loginId));
            predicates.add(criteriaBuilder.greaterThan(fromUploadedFile.get(EXPIRY_DATE), new Date()));
            if (!nameIsEmpty) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(fromUploadedFile.get(FILE_NAME)), fileName.toLowerCase() + "%"));
            }

            TypedQuery<UploadedFile> tQuery = entityManager.createQuery(criteriaQuery
                    .select(fromUploadedFile)
                    .where(predicates.toArray(new Predicate[]{}))
                    .distinct(true)
                    .orderBy(isDescendingSort(searchFilter.getSortDirection())
                            ? criteriaBuilder.desc(fromUploadedFile.get(searchFilter.getSortField()))
                            : criteriaBuilder.asc(fromUploadedFile.get(searchFilter.getSortField()))));

            uploadedFiles = tQuery.setFirstResult(pageOffsetDetails.get(MailBoxConstants.PAGING_OFFSET))
                    .setMaxResults(pageOffsetDetails.get(MailBoxConstants.PAGING_COUNT))
                    .getResultList();

        } finally {
            if (null != entityManager) {
                entityManager.close();
            }
        }
        return uploadedFiles;
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

}
