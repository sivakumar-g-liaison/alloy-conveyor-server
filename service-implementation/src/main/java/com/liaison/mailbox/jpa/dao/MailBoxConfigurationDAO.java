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

import java.util.List;
import java.util.Set;

import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import com.liaison.commons.jpa.GenericDAO;
import com.liaison.mailbox.jpa.model.MailBox;

@NamedQueries({
		@NamedQuery(name = MailBoxConfigurationDAO.GET_MBX,
				query = "SELECT mbx FROM MailBox mbx"
						+ " inner join mbx.mailboxProcessors prcsr"
						+ " inner join prcsr.scheduleProfileProcessors schd_prof_processor"
						+ " inner join schd_prof_processor.scheduleProfilesRef profile"
						+ " where LOWER(mbx.mbxName) like :" + MailBoxConfigurationDAO.MBOX_NAME
						+ " and profile.schProfName like :" + MailBoxConfigurationDAO.SCHD_PROF_NAME
						+ " order by mbx.mbxName")
})
public interface MailBoxConfigurationDAO extends GenericDAO<MailBox> {

	public static final String FIND_BY_MBX_NAME = "findByMboxName";
	public static final String PGUID = "pguid";
	public static final String MBOX_NAME = "mbox_name";
	public static final String GET_MBX = "findMailBoxes";
	public static final String SCHD_PROF_NAME = "schd_name";
	public static final String SERVICE_INST_ID = "service_inst_id";

	public Set<MailBox> find(String mbxName, String profName, List<String> serviceInstanceIds);

	public Set<MailBox> findByName(String mbxName, List<String> serviceInstanceIds);
	
}
