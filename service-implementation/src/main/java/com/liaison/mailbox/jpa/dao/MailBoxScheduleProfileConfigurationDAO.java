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
				query = "SELECT mbx_profile from MailBoxSchedProfile mbx_profile where mbx_profile.pguid = :"
						+ MailBoxScheduleProfileConfigurationDAO.PGUID)
})
public interface MailBoxScheduleProfileConfigurationDAO extends GenericDAO<MailBoxSchedProfile> {

	public static final String GET_MAILBOX_PROFILESCHEDULE = "processorMailboxProfile";
	public static final String PGUID = "pguid";

}
