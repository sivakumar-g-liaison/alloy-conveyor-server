package com.liaison.mailbox.jpa.dao;

import com.liaison.commons.jpa.GenericDAOBase;
import com.liaison.mailbox.jpa.model.MailBox;

public class MailBoxConfigurationDAOBase extends GenericDAOBase<MailBox>
implements MailBoxConfigurationDAO,MailBoxDAO {

	public MailBoxConfigurationDAOBase(){
		super(PERSISTENCE_UNIT_NAME);
	}
	
	public MailBox find(String guid) {
		return null;
	}
}
