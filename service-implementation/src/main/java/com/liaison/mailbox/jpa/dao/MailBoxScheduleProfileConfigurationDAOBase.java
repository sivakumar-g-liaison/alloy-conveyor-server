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

import com.liaison.commons.jpa.GenericDAOBase;
import com.liaison.mailbox.jpa.model.MailBoxSchedProfile;

public class MailBoxScheduleProfileConfigurationDAOBase extends GenericDAOBase<MailBoxSchedProfile> implements
		MailBoxScheduleProfileConfigurationDAO, MailBoxDAO {

	public MailBoxScheduleProfileConfigurationDAOBase() {
		super(PERSISTENCE_UNIT_NAME);
	}

}
