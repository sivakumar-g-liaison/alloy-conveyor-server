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
import com.liaison.mailbox.jpa.model.MailBoxSchedProfile;

@NamedQueries({
		@NamedQuery(name = MailBoxScheduleProfileConfigurationDAO.GET_MAILBOX_PROFILESCHEDULE,
				query = "SELECT mbx_profile from MailBoxSchedProfile mbx_profile"),

		@NamedQuery(name = MailBoxScheduleProfileConfigurationDAO.GET_MBX,
				query = "SELECT mbx_profile from MailBoxSchedProfile mbx_profile"
						+ " inner join mbx_profile.scheduleProfilesRef schProf"
						+ " inner join mbx_profile.mailbox mbx"
						+ " where mbx.mbxName like :" + MailBoxScheduleProfileConfigurationDAO.MBX_NAME
						+ " and schProf.schProfName like :" + MailBoxScheduleProfileConfigurationDAO.SCHD_PROF_NAME)
})
public interface MailBoxScheduleProfileConfigurationDAO extends GenericDAO<MailBoxSchedProfile> {

	public static final String GET_MAILBOX_PROFILESCHEDULE = "processorMailboxProfile";
	public static final String GET_MBX = "findMailBoxes";
	public static final String PGUID = "pguid";
	public static final String MBX_NAME = "mbx_name";
	public static final String SCHD_PROF_NAME = "schd_name";

}
