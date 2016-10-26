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

import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.liaison.mailbox.dtdm.dao.FilterText;
import com.liaison.mailbox.dtdm.model.FilterMatchMode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.liaison.commons.jpa.DAOUtil;
import com.liaison.mailbox.dtdm.dao.FilterObject;
import com.liaison.mailbox.dtdm.dao.MailboxDTDMDAO;

/**
 * This class contains the methods that are used to populate data in the grid.
 *
 * @author OFS
 */

public abstract class GridService<T> {

	private static final Logger LOGGER = LogManager
			.getLogger(GridService.class);

	public static final String FILTER_TEXT = "filterText";
	public static final String SORT_FIELDS = "fields";
	public static final String SORT_DIRECTIONS = "directions";
	public static final String SORT_DIRECTION_ASC = "asc";
	public static final String SORT_DIRECTION_DESC = "desc";

	private final String persistenceUnitName = MailboxDTDMDAO.PERSISTENCE_UNIT_NAME;

	public static class GridResult<T> {
		private final long totalItems;
		private final List<T> resultList;

		public GridResult(final List<T> resultList, final long totalItems) {
			this.resultList = resultList;
			this.totalItems = totalItems;
		}

		public List<T> getResultList() {
			return resultList;
		}

		public long getTotalItems() {
			return totalItems;
		}
	}

	@SuppressWarnings({ "serial", "hiding" })
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
	 * Method provides filter and sorting.
	 *
	 * @param clazz
	 * @param filterText
	 * @param sortInfo
	 * @param page
	 * @param pageSize
	 * @return
	 */

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public GridResult<T> getGridItems(final Class<T> clazz,
			final String filterText, final String sortInfo, final String page,
			final String pageSize) {

		LOGGER.debug("Entering into getGridItems.");
//		Map<String, List<FilterObject>> searchTextObjectList = new HashMap<>();
		FilterText filterTextObj = new FilterText();
		Map<String, Object> sortInfoMap = new HashMap<>();

		// Generate JSON string from the object list
		Gson gson = new Gson();

		EntityManager entityManager = null;

		try {

			if (filterText != null && !filterText.isEmpty()) {
				/*searchTextObjectList = gson.fromJson(filterText,
						new TypeToken<Map<String, List<FilterObject>>>() {
						}.getType());*/
				filterTextObj = gson.fromJson(filterText, FilterText.class);
			}

			if (sortInfo != null && !sortInfo.isEmpty()) {
				sortInfoMap = gson.fromJson(sortInfo,
						new TypeToken<Map<String, Object>>() {
						}.getType());
			}

			entityManager = DAOUtil.getEntityManager(persistenceUnitName);
			CriteriaBuilder criteriaBuilder = entityManager
					.getCriteriaBuilder();

			CriteriaQuery<T> query = criteriaBuilder.createQuery(clazz);
			Root<T> gemRequest = query.from(clazz);
			query.select(gemRequest);

			List<Predicate> predicateList = new ArrayList<>();

			// Filtering params
			CriteriaQueryExpressionHolder holderFilter = createSearchCriteria(
					filterTextObj, clazz, criteriaBuilder, gemRequest,
					predicateList);
			Predicate filterPred = holderFilter.getPredicate();

			if (sortInfoMap.get(SORT_FIELDS) != null) {
				addSort(sortInfoMap, criteriaBuilder, query, gemRequest);
			}

			// Create where condition
			List<Predicate> wherePredicateList = new ArrayList<Predicate>();
			if (filterPred != null) {
				wherePredicateList.add(filterPred);
			}

			if (!predicateList.isEmpty()) {
				wherePredicateList.addAll(predicateList);
			}

			Predicate wherePredicate = criteriaBuilder
					.and(criteriaBuilder.and(wherePredicateList
							.toArray(new Predicate[wherePredicateList.size()])));
			if (predicateList != null && !predicateList.isEmpty()) {

				query.where(wherePredicate);
			}

			Long count = getCount(clazz, entityManager, criteriaBuilder,
					predicateList, holderFilter, wherePredicate);

			// Execute query

			List<T> mailboxMgtEntities;
			TypedQuery<T> tQueryObject = entityManager.createQuery(query);
			// Set object query parameters
			if (holderFilter != null) {
				holderFilter.setParametersCount(tQueryObject);
			}
			if ((page != null && !page.isEmpty())
					&& (pageSize != null && !pageSize.isEmpty())) {

				int pageNumber = Integer.valueOf(page);
				// Sets default value if it is less than 0
				pageNumber = pageNumber < 0 ? 1 : pageNumber;

				int currentPageSize = Integer.valueOf(pageSize);
				// Sets default value if it is less than 0
				currentPageSize = currentPageSize < 0 ? 10 : currentPageSize;

				// calculates first record as per the page size.
				int first = (pageNumber - 1) * currentPageSize;

				// Execute query with paging options
				mailboxMgtEntities = tQueryObject.setFirstResult(first)
						.setMaxResults(currentPageSize).getResultList();

			} else {

				// Execute query without paging options
				mailboxMgtEntities = tQueryObject.getResultList();
			}

			/*
			 * for (T gemEntity : mailboxMgtEntities) {
			 * GemModelDetacher.detachGem(entityManager, gemEntity, 1); }
			 */

			LOGGER.debug("Exit from getGridItemsWithEnterprise.");
			return new GridResult<T>(mailboxMgtEntities, count);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (entityManager != null) {
				entityManager.close();
			}
		}
	}

	/**
	 * @param clazz
	 * @param entityManager
	 * @param criteriaBuilder
	 * @param predicateList
	 * @param holderFilter
	 * @param wherePredicate
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Long getCount(final Class<T> clazz, EntityManager entityManager,
			CriteriaBuilder criteriaBuilder, List<Predicate> predicateList,
			CriteriaQueryExpressionHolder holderFilter, Predicate wherePredicate) {

		// Count query
		CriteriaQuery<Long> countQuery = criteriaBuilder
				.createQuery(Long.class);
		Root<T> countRoot = countQuery.from(clazz);
		countQuery.select(criteriaBuilder.count(countRoot));
		// DO NOT COMMENT THE BELOW LINE
		// java.lang.IllegalArgumentException:
		// org.hibernate.hql.internal.ast.QuerySyntaxException: Invalid path:
		// 'generatedAlias1.status'
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
	 * Adds sort to the criteria
	 *
	 * @param sortInfoMap
	 * @param criteriaBuilder
	 * @param query
	 * @param gemRequest
	 */
	@SuppressWarnings("unchecked")
	private void addSort(final Map<String, Object> sortInfoMap,
			final CriteriaBuilder criteriaBuilder,
			final CriteriaQuery<T> query, final Root<T> gemRequest) {

		LOGGER.debug("Entering into addSort.");
		// Get sort data
		ArrayList<String> fieldsValue = (ArrayList<String>) sortInfoMap
				.get(SORT_FIELDS);
		ArrayList<String> directionsValue = (ArrayList<String>) sortInfoMap
				.get(SORT_DIRECTIONS);

		// Loop through sort data
		for (int i = 0; i < fieldsValue.size(); i++) {

			String fieldName = fieldsValue.get(i).toString();
			String direction = directionsValue.get(i).toString();

			Path<Object> field = null;

			if ("name".equals(fieldName)) {
				field = gemRequest.get("schProfName");
			} else {
				field = gemRequest.get(fieldName);
			}
			query.orderBy((direction.equals(SORT_DIRECTION_ASC))
					? criteriaBuilder.asc(field)
					: criteriaBuilder.desc(field));
		}
		LOGGER.debug("Exit from addSort.");
	}

	/**
	 * Adds predicates to the criteria
	 *
	 * @param searchTextObjectList
	 * @param criteriaBuilder
	 * @param gemRequest
	 * 
	 * @return
	 * @throws ParseException
	 */

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private CriteriaQueryExpressionHolder createSearchCriteria(
			final FilterText searchTextObjectList,
			Class<T> clazz, final CriteriaBuilder criteriaBuilder,
			final Root<T> gemRequest, List<Predicate> andPredicatesList)
			throws ParseException {

		CriteriaQueryExpressionHolder holder = new CriteriaQueryExpressionHolder();

		if (searchTextObjectList.getFilterTextListObject() != null) {

			// Searching functionality
			List<FilterObject> searchFilterObjects = searchTextObjectList.getFilterTextListObject();
			for (FilterObject entry : searchFilterObjects) {

				String field = entry.getField();

				Expression<String> pathString = null;

				if ("name".equals(field)) {
					pathString = gemRequest.get("schProfName");
				} else {
					pathString = gemRequest.get(field);
				}

				ParameterExpression<String> parameterExp = criteriaBuilder
						.parameter(String.class);
				if (searchTextObjectList.getMatchMode() == null || searchTextObjectList.getMatchMode() == FilterMatchMode.LIKE) {
					andPredicatesList.add(criteriaBuilder.like(
							criteriaBuilder.upper(pathString), parameterExp));
					holder.put(parameterExp, "%" + entry.getText().toUpperCase() + "%");
				} else if (searchTextObjectList.getMatchMode() == FilterMatchMode.EQUALS) {
					andPredicatesList.add(criteriaBuilder.equal(pathString, parameterExp));
					holder.put(parameterExp, entry.getText());
				}
			}
		}

		if (andPredicatesList != null) {
			holder.setPredicate(criteriaBuilder.and(andPredicatesList
					.toArray(new Predicate[andPredicatesList.size()])));
		}

		return holder;
	}

}
