/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.liaison.commons.jpa.DAOUtil;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.dtdm.dao.FilterObject;
import com.liaison.mailbox.dtdm.dao.FilterText;
import com.liaison.mailbox.enums.EntityStatus;
import com.liaison.mailbox.enums.ExecutionState;
import com.liaison.mailbox.enums.FilterMatchMode;
import com.liaison.mailbox.enums.UppercaseEnumAdapter;
import com.liaison.mailbox.rtdm.dao.MailboxRTDMDAO;
import com.liaison.mailbox.rtdm.model.ProcessorExecutionState;
import com.liaison.mailbox.rtdm.model.StagedFile;
import com.liaison.mailbox.service.util.MailBoxUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class contains the methods that are used to populate RTDM data in the grid.
 * 
 * @author OFS
 */
public abstract class GridServiceRTDM<T> {

    private static final Logger LOGGER = LogManager.getLogger(GridServiceRTDM.class);

    public static final String FILTER_TEXT = "filterText";
    public static final String SORT_FIELDS = "fields";
    public static final String SORT_DIRECTIONS = "directions";
    public static final String SORT_DIRECTION_ASC = "asc";
    public static final String SORT_DIRECTION_DESC = "desc";
    public static final String STATUS = "stagedFileStatus";
    public static final String CREATED_DATE = "createdDate";
    public static final String NAME = "name";
    public static final String FILE_NAME = "fileName";
    public static final String FILE_STATUS = "status";
    public static final String MAILBOX_GUID = "mailboxGuid";

    private final String persistenceUnitName = MailboxRTDMDAO.PERSISTENCE_UNIT_NAME;

    public static class GridResult<T> {
	
        private final long totalItems;
        private final List<T> resultList;

        public GridResult(final List<T> resultList, final long totalItems) {
            this.resultList = resultList;
            this.totalItems = totalItems;
        }

        public long getTotalItems() {
            return totalItems;
        }
        public List<T> getResultList() {
            return resultList;
        }
    }

    @SuppressWarnings({"serial", "hiding"})
    private class CriteriaQueryExpressionHolder<T> implements Serializable {

        private HashMap<ParameterExpression<String>, String> predicateStrings = new HashMap<ParameterExpression<String>, String>();
        private HashMap<ParameterExpression<Date>, Date> predicateDates = new HashMap<ParameterExpression<Date>, Date>();
        private Predicate predicate;

        public Predicate getPredicate() {
            return this.predicate;
        }

        public void setPredicate(Predicate predicate) {
            this.predicate = predicate;
        }

        public void put(ParameterExpression<String> key, String value) {
            predicateStrings.put(key, value);
        }

        public void putDate(ParameterExpression<Date> key, Date value) {
            predicateDates.put(key, value);
        }

        public void setParametersCount(TypedQuery<Long> query) {
            for (ParameterExpression<String> p : predicateStrings.keySet()) {
                query.setParameter(p, predicateStrings.get(p));
            }
            for (ParameterExpression<Date> p : predicateDates.keySet()) {
                query.setParameter(p, predicateDates.get(p));
            }
        }
    }
	
    /**
     * Method used to retrieve the items to be populated in the grid
     * 
     * @param clazz
     * @param filterText
     * @param sortInfo
     * @param page
     * @param pageSize
     * @return gridResult
    */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public GridResult<T> getGridItems(final Class<T> clazz, final String filterText, final String sortInfo,
            final String page, final String pageSize) {

        LOGGER.debug("Entering into getGridItems.");
        FilterText filterTextObj = new FilterText();
        Map<String, Object> sortInfoMap = new HashMap<>();
        Gson gson = new Gson();

        EntityManager entityManager = null;

        try {

            if (!MailBoxUtil.isEmpty(filterText)) {
                GsonBuilder builder = new GsonBuilder();
                builder.registerTypeAdapter(FilterMatchMode.class, new UppercaseEnumAdapter());
                filterTextObj = builder.create().fromJson(filterText, FilterText.class);
            }

            if (!MailBoxUtil.isEmpty(sortInfo)) {
                sortInfoMap = gson.fromJson(sortInfo, new TypeToken<Map<String, Object>>(){}.getType());
            }

            entityManager = DAOUtil.getEntityManager(persistenceUnitName);
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

            CriteriaQuery<T> query = criteriaBuilder.createQuery(clazz);
            Root<T> request = query.from(clazz);
            query.select(request);

            List<Predicate> predicateList = new ArrayList<>();

            CriteriaQueryExpressionHolder holderFilter = createSearchCriteria(
                    filterTextObj, clazz, criteriaBuilder, request,
                    predicateList);
            Predicate filterPred = holderFilter.getPredicate();

            if (null != sortInfoMap.get(SORT_FIELDS)) {
                addSort(sortInfoMap, criteriaBuilder, query, request);
            }

            // Create where condition
            List<Predicate> wherePredicateList = new ArrayList<Predicate>();
            if (filterPred != null) {
                wherePredicateList.add(filterPred);
            }

            if (!predicateList.isEmpty()) {
                wherePredicateList.addAll(predicateList);
            }

            Predicate wherePredicate = criteriaBuilder.and(criteriaBuilder.and(wherePredicateList
                    .toArray(new Predicate[wherePredicateList.size()])));

            if (null != predicateList && !predicateList.isEmpty()) {
                query.where(wherePredicate);
            }

            Long count = getCount(clazz, entityManager, criteriaBuilder,
                    predicateList, holderFilter, wherePredicate);

            List<T> entities;
            TypedQuery<T> tQueryObject = entityManager.createQuery(query);

            // Set object query parameters
            if (holderFilter != null) {
                holderFilter.setParametersCount(tQueryObject);
            }

            if (!MailBoxUtil.isEmpty(page) && !MailBoxUtil.isEmpty(pageSize)) {

                int pageNumber = Integer.valueOf(page);
                // Sets default value if it is less than 0
                pageNumber = pageNumber < 0 ? 1 : pageNumber;

                int currentPageSize = Integer.valueOf(pageSize);
                // Sets default value if it is less than 0
                currentPageSize = currentPageSize < 0 ? 10 : currentPageSize;

                // calculates first record as per the page size.
                int first = (pageNumber - 1) * currentPageSize;

                // Execute query with paging options
                entities = tQueryObject.setFirstResult(first)
                        .setMaxResults(currentPageSize).getResultList();

            } else {
                // Execute query without paging options
                entities = tQueryObject.getResultList();
            }

            LOGGER.debug("Exit from getGridItems.");
            return new GridResult<T>(entities, count);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (null != entityManager) {
                entityManager.close();
            }
        }
    }

    /**
     * Helper method to retrieve the items count
     * 
     * @param clazz
     * @param entityManager
     * @param criteriaBuilder
     * @param predicateList
     * @param holderFilter
     * @param wherePredicate
     * @return count
    */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Long getCount(final Class<T> clazz, EntityManager entityManager, 
            CriteriaBuilder criteriaBuilder, List<Predicate> predicateList,
            CriteriaQueryExpressionHolder holderFilter, Predicate wherePredicate) {

        // Count query
        CriteriaQuery<Long> countQuery = criteriaBuilder
                .createQuery(Long.class);
        Root<T> countRoot = countQuery.from(clazz);
        countQuery.select(criteriaBuilder.count(countRoot));
        entityManager.createQuery(countQuery);

        if (predicateList != null && !predicateList.isEmpty()) {
            countQuery.where(wherePredicate);
        }

        TypedQuery<Long> tQueryCount = entityManager.createQuery(countQuery);

        // Set count query parameters
        if (holderFilter != null) {
            holderFilter.setParametersCount(tQueryCount);
        }

        Long count = tQueryCount.getSingleResult();
        return count;
    }

    /**
     * Helper method used to construct the sort fields in query
     * 
     * @param sortInfoMap
     * @param criteriaBuilder
     * @param query
     * @param request
    */
    @SuppressWarnings("unchecked")
    private void addSort(final Map<String, Object> sortInfoMap, final CriteriaBuilder criteriaBuilder, 
            final CriteriaQuery<T> query, final Root<T> request) {

        LOGGER.debug("Entering into add sort");

        // Getting sort fields and directions
        ArrayList<String> fieldsValue = (ArrayList<String>) sortInfoMap
                .get(SORT_FIELDS);
        ArrayList<String> directionsValue = (ArrayList<String>) sortInfoMap
                .get(SORT_DIRECTIONS);

        for (int i = 0; i < fieldsValue.size(); i++) {

            String fieldName = fieldsValue.get(i).toString();
            String direction = directionsValue.get(i).toString();

            Path<Object> field = null;
            if (NAME.equals(fieldName)) {
                field = request.get(FILE_NAME);
            } else if (FILE_STATUS.equals(fieldName)) {
                field = request.get(STATUS);
            } else if (MAILBOX_GUID.equals(fieldName)) {
                field = request.get(MailBoxConstants.KEY_MAILBOX_ID);
            } else {
                field = request.get(fieldName);
            }

            query.orderBy((direction.equals(SORT_DIRECTION_ASC))
                    ? criteriaBuilder.asc(field)
                    : criteriaBuilder.desc(field));
        }

        LOGGER.debug("Exit from addSort.");
    }

    /**
     * Adding the predicates to criteria builder
     * 
     * @param filterTextObj
     * @param clazz
     * @param criteriaBuilder
     * @param request
     * @param andPredicateList
     * @return holder
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private CriteriaQueryExpressionHolder createSearchCriteria(final FilterText searchTextObjectList, Class<T> clazz,
            final CriteriaBuilder criteriaBuilder, final Root<T> request, List<Predicate> andPredicatesList) {

        CriteriaQueryExpressionHolder holder = new CriteriaQueryExpressionHolder();
        Expression<String> pathString = null;

        if (null != searchTextObjectList.getFilterTextListObject()) {

            List<FilterObject> searchFilterObjects = searchTextObjectList.getFilterTextListObject();
            for (FilterObject entry : searchFilterObjects) {

                String field = entry.getField();

                if (NAME.equals(field)) {
                    pathString = request.get(FILE_NAME);
                } else if (MAILBOX_GUID.equals(field)) {
                    pathString = request.get(MailBoxConstants.KEY_MAILBOX_ID);
                } else if (FILE_STATUS.equals(field)) {
                    pathString = request.get(STATUS);
                } else {
                    pathString = request.get(field);
                }

                ParameterExpression<String> parameterExp = criteriaBuilder.parameter(String.class);
                if (MailBoxConstants.GLOBAL_PROCESS_ID.equals(field)
                        || MailBoxConstants.KEY_PROCESSOR_ID.equals(field)
                        || MAILBOX_GUID.equals(field)
                        || FILE_STATUS.equals(field)) {
                    andPredicatesList.add(criteriaBuilder.equal(pathString, parameterExp));
                    holder.put(parameterExp, entry.getText());
                } else if (CREATED_DATE.equals(field)) {
	
                    Date[] dates = getDates(entry.getText());
                    andPredicatesList.add(criteriaBuilder.greaterThan(pathString, parameterExp));
                    holder.putDate(parameterExp, dates[0]);
                    andPredicatesList.add(criteriaBuilder.lessThan(pathString, parameterExp));
                    holder.putDate(parameterExp, dates[0]);

                } else {

                    andPredicatesList.add(criteriaBuilder.like(
                            criteriaBuilder.upper(pathString), parameterExp));
                    holder.put(parameterExp, "%" + entry.getText().toUpperCase() + "%");
                }
            }
        }
        
        if (ProcessorExecutionState.class.getName().equals(clazz.getName())) {
            andPredicatesList.add(criteriaBuilder.equal(request.get("executionStatus"), ExecutionState.PROCESSING.value()));
        } else if (StagedFile.class.getName().equals(clazz.getName())) {
            pathString = request.get(STATUS);
            andPredicatesList.add(criteriaBuilder.notEqual(pathString, EntityStatus.INACTIVE.name())); 
        }

        if (StagedFile.class.getName().equals(clazz.getName())) {
            pathString = request.get(MailBoxConstants.CLUSTER_TYPE);
            andPredicatesList.add(pathString.in(MailBoxUtil.getClusterTypes()));
        } else if (ProcessorExecutionState.class.getName().equals(clazz.getName())) {
            pathString = request.get("processors").get(MailBoxConstants.CLUSTER_TYPE);
            andPredicatesList.add(pathString.in(MailBoxUtil.getClusterTypes()));
        }

        // adding all the predicates
        holder.setPredicate(criteriaBuilder.and(andPredicatesList
                .toArray(new Predicate[andPredicatesList.size()])));

        return holder;
    }
	
    /**
     * Helper method to construct the date.
     * 
     * @param dateFilterText
     * @return dates
     */
    private Date[] getDates(String dateFilterText) {
        
        Date[] dates = new Date[2];
        String[] data = dateFilterText.split(" - ");
        try {
            
            dates[0] = new SimpleDateFormat(data[2]).parse(data[0]);
            dates[1] = new SimpleDateFormat(data[2]).parse(data[1]);
            Calendar cal = Calendar.getInstance();
            cal.setTime(dates[1]);
            cal.add(Calendar.DAY_OF_MONTH, 1);
            dates[1] = cal.getTime();

        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        return dates;
    }
}
