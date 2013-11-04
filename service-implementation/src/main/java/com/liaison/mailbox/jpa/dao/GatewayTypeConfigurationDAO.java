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
import com.liaison.mailbox.jpa.model.GatewayType;

@NamedQueries({
	@NamedQuery(name = GatewayTypeConfigurationDAO.GET_BY_GATE_NAME, query = "select gateType from GatewayType gateType where gateType.pguid = :"
			+ GatewayTypeConfigurationDAO.NAME),
		@NamedQuery(name = GatewayTypeConfigurationDAO.GET_ALL_GATE, query = "select gateType from GatewayType gateType") })

/**
 * 
 * 
 * @author praveenu
 */
public interface GatewayTypeConfigurationDAO extends GenericDAO<GatewayType> {

	public static final String PGUID = "pguid";
	public static final String GET_ALL_GATE = "get_all_gate";
	public static final String NAME = "name";
	public static final String GET_BY_GATE_NAME = "getByGateName";
	

	public GatewayType findByName(String name);
	public Set<GatewayType> findAllGateType();
}
