package com.liaison.mailbox.jpa.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liaison.commons.jpa.GenericDAOBase;
import com.liaison.mailbox.jpa.model.IdpProfile;

public class UserProfileConfigurationDAOBase extends GenericDAOBase<IdpProfile> implements MailBoxDAO{

	private static final Logger LOG = LoggerFactory.getLogger(UserProfileConfigurationDAOBase.class);

	public UserProfileConfigurationDAOBase() {
		super(PERSISTENCE_UNIT_NAME);
	}
}
