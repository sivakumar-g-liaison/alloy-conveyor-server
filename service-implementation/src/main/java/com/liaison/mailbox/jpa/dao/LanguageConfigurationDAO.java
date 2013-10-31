package com.liaison.mailbox.jpa.dao;

import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import com.liaison.commons.jpa.GenericDAO;
import com.liaison.mailbox.jpa.model.Language;

@NamedQueries({
		@NamedQuery(name = LanguageConfigurationDAO.GET_BY_NAME, query = "SELECT l FROM Language l where l.name = :"
				+ LanguageConfigurationDAO.NAME),
		@NamedQuery(name = LanguageConfigurationDAO.GET_ALL, query = "select l from Language l where l.name") })

public interface LanguageConfigurationDAO extends GenericDAO<Language> {

	public static final String PGUID = "pguid";
	public static final String GET_ALL = "getAll";
	public static final String NAME = "name";
	public static final String GET_BY_NAME = "getByName";
	

	public Language findByName(String name);
}
