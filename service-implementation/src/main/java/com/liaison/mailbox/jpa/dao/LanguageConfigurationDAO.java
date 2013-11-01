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
import com.liaison.mailbox.jpa.model.Language;

@NamedQueries({
		@NamedQuery(name = LanguageConfigurationDAO.GET_BY_LANG_NAME, query = "SELECT l FROM Language l where l.name = :"
				+ LanguageConfigurationDAO.NAME),
		@NamedQuery(name = LanguageConfigurationDAO.GET_ALL_LANG, query = "select l from Language l") })

/**
 * 
 * 
 * @author praveenu
 */
public interface LanguageConfigurationDAO extends GenericDAO<Language> {

	public static final String PGUID = "pguid";
	public static final String GET_ALL_LANG = "get_all_lang";
	public static final String NAME = "name";
	public static final String GET_BY_LANG_NAME = "getByLangName";
	

	public Language findByName(String name);
	public Set<Language> findAllLanguage();
}
