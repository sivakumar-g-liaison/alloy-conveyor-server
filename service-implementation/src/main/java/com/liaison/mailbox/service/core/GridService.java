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

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.liaison.commons.jpa.DAOUtil;
import com.liaison.mailbox.dtdm.dao.FilterObject;
import com.liaison.mailbox.dtdm.dao.MailboxDTDMDAO;

/**
 * 
 * @author OFS
 */

public abstract class GridService<T> {

	private static final Logger LOGGER = LogManager.getLogger(GridService.class);

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
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public GridResult<T> getGridItems(final Class clazz, final String filterText, final String sortInfo,
			final String page, final String pageSize) {

		LOGGER.debug("Entering into getGridItems.");
		Map<String, List<com.liaison.mailbox.dtdm.dao.FilterObject>> searchTextObjectList = new HashMap<>();
		Map<String, Object> sortInfoMap = new HashMap<>();

		// Generate JSON string from the object list
		Gson gson = new Gson();

		EntityManager entityManager = null;

		try {

			if (filterText != null && !filterText.isEmpty()) {
				searchTextObjectList = gson.fromJson(filterText, new TypeToken<Map<String, List<FilterObject>>>() {
				}.getType());
			}

			if (sortInfo != null && !sortInfo.isEmpty()) {
				sortInfoMap = gson.fromJson(sortInfo, new TypeToken<Map<String, Object>>() {
				}.getType());
			}

			entityManager = DAOUtil.getEntityManager(persistenceUnitName);
			CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

			CriteriaQuery<T> query = criteriaBuilder.createQuery(clazz);
			Root<T> gemRequest = query.from(clazz);
			query.select(gemRequest);


			if (sortInfoMap.get(SORT_FIELDS) != null) {
				addSort(sortInfoMap, criteriaBuilder, query, gemRequest);
			}

			List<Predicate> predicateList = new ArrayList<>();

			addPredicates(searchTextObjectList, clazz, criteriaBuilder, gemRequest, predicateList);


			// Add created predicates to where criteria
			Predicate[] predicates = new Predicate[predicateList.size()];
			predicateList.toArray(predicates);
			if (predicateList != null && !predicateList.isEmpty()) {
				query.where(predicates);
			}

			// Total count needed for pagination
			CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);
			countQuery.select(criteriaBuilder.count(countQuery.from(clazz)));
			if (predicateList != null && !predicateList.isEmpty()) {
				countQuery.where(predicates);
			}
			Long count = entityManager.createQuery(countQuery).getSingleResult();

			// Execute query
			List<T> mailboxMgtEntities;
			if ((page != null && !page.isEmpty()) && (pageSize != null && !pageSize.isEmpty())) {

				int pageNumber = Integer.valueOf(page);
				// Sets default value if it is less than 0
				pageNumber = pageNumber < 0 ? 1 : pageNumber;

				int currentPageSize = Integer.valueOf(pageSize);
				// Sets default value if it is less than 0
				currentPageSize = currentPageSize < 0 ? 10 : currentPageSize;

				// calculates first record as per the page size.
				int first = (pageNumber - 1) * currentPageSize;

				mailboxMgtEntities = entityManager.createQuery(query).setFirstResult(first).setMaxResults(
						currentPageSize).getResultList();

			} else {
				mailboxMgtEntities = entityManager.createQuery(query).getResultList();
			}


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
	 * Adds sort to the criteria
	 * 
	 * @param sortInfoMap
	 * @param criteriaBuilder
	 * @param query
	 * @param gemRequest
	 */
	@SuppressWarnings("unchecked")
	private void addSort(final Map<String, Object> sortInfoMap, final CriteriaBuilder criteriaBuilder,
			final CriteriaQuery<T> query, final Root<T> gemRequest) {

		LOGGER.debug("Entering into addSort.");
		// Get sort data
		ArrayList<String> fieldsValue = (ArrayList<String>) sortInfoMap.get(SORT_FIELDS);
		ArrayList<String> directionsValue = (ArrayList<String>) sortInfoMap.get(SORT_DIRECTIONS);

		// Loop through sort data
		for (int i = 0; i < fieldsValue.size(); i++) {

			String fieldName = fieldsValue.get(i).toString();
			String direction = directionsValue.get(i).toString();
            if(fieldName.equals("name")) fieldName = "schProfName";
			Path<Object> field = null;
			field = gemRequest.get(fieldName);

			if (direction.equals(SORT_DIRECTION_ASC)) {
				query.orderBy(criteriaBuilder.asc(field));
			} else if (direction.equals(SORT_DIRECTION_DESC)) {
				query.orderBy(criteriaBuilder.desc(field));
			}
		}
		LOGGER.debug("Exit from addSort.");
	}

	/**
	 * Adds predicates to the criteria
	 * 
	 * @param searchTextObjectList
	 * @param criteriaBuilder
	 * @param gemRequest
	 * @param metamodel
	 * @return
	 * @throws ParseException
	 */
	private List<Predicate> addPredicates(final Map<String, List<FilterObject>> searchTextObjectList, Class<T> clazz,
			final CriteriaBuilder criteriaBuilder, final Root<T> gemRequest, List<Predicate> predicateList)
			throws ParseException {

		Predicate pred = null;

		if (searchTextObjectList.get(FILTER_TEXT) != null) {

			// Searching functionality
			List<FilterObject> searchFilterObjects = searchTextObjectList.get(FILTER_TEXT);
			for (FilterObject entry : searchFilterObjects) {

				String field = entry.getField();
                if(field.equals("name")) field = "schProfName";
				Expression<String> path = null;

				entry.setText(entry.getText().trim().toUpperCase());
				path = gemRequest.get(field);
				pred = criteriaBuilder.like(criteriaBuilder.upper(path), "%" + entry.getText() + "%");
				predicateList.add(pred);
			}
		}

		return predicateList;
	}
}
