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

import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import com.liaison.commons.jpa.GenericDAO;
import com.liaison.mailbox.jpa.model.ServiceInstanceId;

@NamedQueries({ @NamedQuery(name = ServiceInstanceDAO.FIND_BY_SERVICE_INSTANCEID, query = "SELECT sii FROM ServiceInstanceId sii WHERE sii.name = :"
		+ ServiceInstanceDAO.INTANXE_ID) })
public interface ServiceInstanceDAO extends GenericDAO<ServiceInstanceId> {

	public static final String FIND_BY_SERVICE_INSTANCEID = "findByServiceInstIdss";
	public static final String INTANXE_ID = "serv_id";
   
	/**
	 * Find by serviceInstanceId.
	 * 
	 * @param serviceInstanceId
	 * @return ServiceInstanceId
	 */
	public ServiceInstanceId findByName(String serviceInstanceId);

}
