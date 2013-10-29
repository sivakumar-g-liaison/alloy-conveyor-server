package com.liaison.mailbox.jpa.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liaison.commons.jpa.GenericDAOBase;
import com.liaison.mailbox.jpa.model.Account;

public class UserAccountConfigurationDAOBase extends GenericDAOBase<Account> implements MailBoxDAO {

	private static final Logger LOG = LoggerFactory.getLogger(UserAccountConfigurationDAOBase.class);

	public UserAccountConfigurationDAOBase() {
		super(PERSISTENCE_UNIT_NAME);
	}
}
