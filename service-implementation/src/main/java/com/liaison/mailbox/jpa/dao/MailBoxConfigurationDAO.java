package com.liaison.mailbox.jpa.dao;

import com.liaison.commons.jpa.GenericDAO;
import com.liaison.mailbox.jpa.model.MailBox;


public interface MailBoxConfigurationDAO extends GenericDAO <MailBox>{
	
	public MailBox find(String guid);

}
