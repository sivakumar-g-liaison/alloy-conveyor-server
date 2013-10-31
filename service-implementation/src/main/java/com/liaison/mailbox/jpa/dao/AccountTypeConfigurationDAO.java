package com.liaison.mailbox.jpa.dao;

import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import com.liaison.commons.jpa.GenericDAO;
import com.liaison.mailbox.jpa.model.AccountType;

@NamedQueries({
		@NamedQuery(name = AccountTypeConfigurationDAO.GET_BY_NAME, query = "select accType from AccountType accType where accType.name = :"
				+ AccountTypeConfigurationDAO.NAME),
		@NamedQuery(name = AccountTypeConfigurationDAO.GET_ALL, query = "select accType from AccountType accType where accType.name") })

public interface AccountTypeConfigurationDAO extends GenericDAO<AccountType> {

	public static final String PGUID = "pguid";
	public static final String GET_ALL = "getAll";
	public static final String NAME = "name";
	public static final String GET_BY_NAME = "getByName";
	

	public AccountType findByName(String name);
}
