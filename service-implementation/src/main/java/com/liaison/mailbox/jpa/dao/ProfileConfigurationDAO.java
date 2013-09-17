package com.liaison.mailbox.jpa.dao;

import com.liaison.commons.jpa.GenericDAO;
import com.liaison.mailbox.jpa.model.ScheduleProfilesRef;

public interface ProfileConfigurationDAO extends GenericDAO<ScheduleProfilesRef> {

	public static final String PGUID = "pguid";
}
