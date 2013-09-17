package com.liaison.mailbox.jpa.dao;

import com.liaison.commons.jpa.GenericDAOBase;
import com.liaison.mailbox.jpa.model.ScheduleProfilesRef;

public class ProfileConfigurationDAOBase extends GenericDAOBase<ScheduleProfilesRef>
		implements ProfileConfigurationDAO, MailBoxDAO {

	public ProfileConfigurationDAOBase() {
		super(PERSISTENCE_UNIT_NAME);
	}
}
