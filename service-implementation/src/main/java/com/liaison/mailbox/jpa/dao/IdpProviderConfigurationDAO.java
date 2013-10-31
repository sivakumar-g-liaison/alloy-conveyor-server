package com.liaison.mailbox.jpa.dao;

import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import com.liaison.commons.jpa.GenericDAO;
import com.liaison.mailbox.jpa.model.IdpProvider;

@NamedQueries({
		@NamedQuery(name = IdpProviderConfigurationDAO.GET_BY_NAME, query = "select idpType from IdpProvider idpType where idpType.name = :"
				+ IdpProviderConfigurationDAO.NAME),
		@NamedQuery(name = IdpProviderConfigurationDAO.GET_ALL, query = "select idpType from IdpProvider idpType where idpType.name") })

public interface IdpProviderConfigurationDAO extends GenericDAO<IdpProvider> {

	public static final String PGUID = "pguid";
	public static final String GET_ALL = "getAll";
	public static final String NAME = "name";
	public static final String GET_BY_NAME = "getByName";
	

	public IdpProvider findByName(String name);
}
