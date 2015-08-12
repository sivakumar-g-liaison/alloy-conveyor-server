/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.mailbox.service.util;

import java.util.List;

/**
 * 
 * @author OFS
 */
public class QueryBuilderUtil {

	/**
	 * This method will construct a string of processor types appended by OR operator instead of using IN Clause because
	 * using IN clause along with TYPE operator is having issues
	 * 
	 * @param specificProcessorTypes
	 * @return
	 */
	public static String constructSqlStringForTypeOperator(List<String> specificProcessorTypes) {

		StringBuilder s = new StringBuilder();

		for (int i = 0; i < specificProcessorTypes.size(); i++) {

			if (i == 0) {
				s.append(" TYPE(processor) = " + specificProcessorTypes.get(i));
			} else {
				s.append(" or TYPE(processor) = " + specificProcessorTypes.get(i));
			}
		}
		return s.toString();

	}

	/**
	 * Generate "in" clause string from the list.
	 * 
	 * @param mailboxIds list of mailboxIds
	 * @return String
	 */
	public static String collectionToSqlString(List<String> list) {

		if (null == list || list.isEmpty()) {
			return null;
		}

		StringBuilder s = new StringBuilder();
		for (final String str : list) {
			s.append("'").append(str).append("'").append(",");
		}

		return s.toString().substring(0, s.toString().length() - 1);
	}
}
