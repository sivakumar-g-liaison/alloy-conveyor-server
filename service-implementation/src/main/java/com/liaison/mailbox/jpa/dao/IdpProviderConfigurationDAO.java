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
import com.liaison.mailbox.jpa.model.IdpProvider;

@NamedQueries({
		@NamedQuery(name = IdpProviderConfigurationDAO.GET_BY_PROVIDER_NAME, query = "select idpType from IdpProvider idpType where idpType.name = :"
				+ IdpProviderConfigurationDAO.NAME),
		@NamedQuery(name = IdpProviderConfigurationDAO.GET_ALL_PROV, query = "select idpType from IdpProvider idpType") })

/**
 * 
 * 
 * @author praveenu
 */
public interface IdpProviderConfigurationDAO extends GenericDAO<IdpProvider> {

	public static final String PGUID = "pguid";
	public static final String GET_ALL_PROV = "get_all_prov";
	public static final String NAME = "name";
	public static final String GET_BY_PROVIDER_NAME = "getByProviderName";
	

	public IdpProvider findByName(String name);
	public Set<IdpProvider> findAllProviders();
}
