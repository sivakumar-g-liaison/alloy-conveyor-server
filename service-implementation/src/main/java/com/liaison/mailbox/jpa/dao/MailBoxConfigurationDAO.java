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
import com.liaison.mailbox.jpa.model.MailBox;

@NamedQueries({
		@NamedQuery(name = MailBoxConfigurationDAO.FIND_BY_MBX_NAME,
				query = "SELECT mbc FROM MailBox mbc where LOWER(mbc.mbxName) like :" + MailBoxConfigurationDAO.MBOX_NAME),
		@NamedQuery(name = MailBoxConfigurationDAO.FIND_ACTIVE_MAILBOX_BY_PGUID,
				query = "SELECT mbc FROM MailBox mbc WHERE mbc.mbxStatus = 'active' and mbc.pguid = :"
						+ MailBoxConfigurationDAO.PGUID),
		@NamedQuery(name = MailBoxConfigurationDAO.FIND_MAILBOX_BY_PGUID_SIID,
						query = "SELECT mbc FROM MailBox mbc"
								+ " inner join mbc.mailboxProcessors prcsr"
								+ " inner join prcsr.serviceInstance sid"
								+ " where mbc.pguid = :" + MailBoxConfigurationDAO.PGUID
								+ " and sid.name = :" + MailBoxConfigurationDAO.SERVICE_INST_ID),
		@NamedQuery(name = MailBoxConfigurationDAO.INACTIVATE_MAILBOX,
				query = "UPDATE MailBox mbc set mbc.mbxStatus = 'inactive' where mbc.pguid = :" + MailBoxConfigurationDAO.PGUID),
		@NamedQuery(name = MailBoxConfigurationDAO.GET_MBX,
				query = "SELECT mbx FROM MailBox mbx"
						+ " inner join mbx.mailboxProcessors prcsr"
						+ " inner join prcsr.scheduleProfileProcessors schd_prof_processor"
						+ " inner join schd_prof_processor.scheduleProfilesRef profile"
						+ " where LOWER(mbx.mbxName) like :" + MailBoxConfigurationDAO.MBOX_NAME
						+ " and profile.schProfName like :" + MailBoxConfigurationDAO.SCHD_PROF_NAME
						+ " order by mbx.mbxName"),
		@NamedQuery(name = MailBoxConfigurationDAO.GET_MBX_BY_GUID_AND_PROCSR_SERVICE_INST_ID,
		query = "SELECT mbx FROM MailBox mbx"
				+ " inner join mbx.mailboxProcessors prcsr"
				+ " inner join prcsr.scheduleProfileProcessors schd_prof_processor"
				+ " inner join schd_prof_processor.scheduleProfilesRef profile"
				+ " where mbx.pguid = :" + MailBoxConfigurationDAO.PGUID
				+ " and prcsr.serviceInstId = :" + MailBoxConfigurationDAO.SERVICE_INST_ID
				+ " order by mbx.mbxName")
})
public interface MailBoxConfigurationDAO extends GenericDAO<MailBox> {

	public static final String FIND_BY_MBX_NAME = "findByMboxName";
	public static final String FIND_ACTIVE_MAILBOX_BY_PGUID = "findActiveMailBoxCompByProfile";
	public static final String INACTIVATE_MAILBOX = "inActivateMailBoxByGUID";
	public static final String PGUID = "pguid";
	public static final String MBOX_NAME = "mbox_name";
	public static final String GET_MBX = "findMailBoxes";
	public static final String SCHD_PROF_NAME = "schd_name";
	public static final String GET_MBX_BY_GUID_AND_PROCSR_SERVICE_INST_ID = "get_mbx_guid_sid";
	public static final String SERVICE_INST_ID = "service_inst_id";
	public static final String FIND_MAILBOX_BY_PGUID_SIID = "findMailBoxBySIId";

	public MailBox findActiveMailBox(String guid);
	
	public MailBox findMailBox(String guid, String serviceInstanceId);

	public int deactiveMailBox(String guid);

	public Set<MailBox> find(String mbxName, String profName);

	public Set<MailBox> findByName(String mbxName);
	
}
