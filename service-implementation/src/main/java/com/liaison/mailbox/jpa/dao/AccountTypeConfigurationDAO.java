/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall 
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.jpa.dao;

import java.util.Set;

import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import com.liaison.commons.jpa.GenericDAO;
import com.liaison.mailbox.jpa.model.AccountType;

@NamedQueries({
		@NamedQuery(name = AccountTypeConfigurationDAO.GET_BY_NAME, query = "select accType from AccountType accType where accType.name = :"
				+ AccountTypeConfigurationDAO.NAME),
		@NamedQuery(name = AccountTypeConfigurationDAO.GET_ALL_ACC_TYP, query = "select accType from AccountType accType") })

/**
 * 
 * 
 * @author praveenu
 */
public interface AccountTypeConfigurationDAO extends GenericDAO<AccountType> {

	public static final String PGUID = "pguid";
	public static final String GET_ALL_ACC_TYP = "get_all_acc_type";
	public static final String NAME = "name";
	public static final String GET_BY_NAME = "getByName";
	

	public AccountType findByName(String name);
	public Set<AccountType> findAllAccType();
}
