package com.liaison.mailbox.jpa.dao;

import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import com.liaison.commons.jpa.GenericDAO;
import com.liaison.mailbox.jpa.model.GatewayType;

@NamedQueries({
	@NamedQuery(name = GatewayTypeConfigurationDAO.GET_BY_GATE_NAME, query = "select gateType from GatewayType gateType where gateType.name = :"
			+ GatewayTypeConfigurationDAO.NAME),
		@NamedQuery(name = GatewayTypeConfigurationDAO.GET_ALL, query = "select gateType from GatewayType gateType") })

public interface GatewayTypeConfigurationDAO extends GenericDAO<GatewayType> {

	public static final String PGUID = "pguid";
	public static final String GET_ALL = "getAll";
	public static final String NAME = "name";
	public static final String GET_BY_GATE_NAME = "getByGateName";
	

	public GatewayType findByName(String name);
}
