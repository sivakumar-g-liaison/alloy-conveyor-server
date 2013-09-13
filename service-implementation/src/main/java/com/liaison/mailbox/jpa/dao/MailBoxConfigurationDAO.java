package com.liaison.mailbox.jpa.dao;

import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import com.liaison.commons.jpa.GenericDAO;
import com.liaison.mailbox.jpa.model.MailBox;

@NamedQueries({
		@NamedQuery(name = MailBoxConfigurationDAO.FIND_MAILBOX_BY_PGUID,
				query = "SELECT mbc FROM MailBox mbc WHERE mbc.pguid = :" + MailBoxConfigurationDAO.PGUID),
		@NamedQuery(name = MailBoxConfigurationDAO.FIND_ACTIVE_MAILBOX_BY_PGUID,
				query = "SELECT mbc FROM MailBox mbc WHERE mbc.mbxStatus = 'active' and mbc.pguid = :"
						+ MailBoxConfigurationDAO.PGUID),
		@NamedQuery(name = MailBoxConfigurationDAO.INACTIVATE_MAILBOX,
				query = "UPDATE MailBox mbc set mbc.mbxStatus = 'inactive' where mbc.pguid = :" + MailBoxConfigurationDAO.PGUID)
})
public interface MailBoxConfigurationDAO extends GenericDAO<MailBox> {

	public static final String FIND_MAILBOX_BY_PGUID = "findMailBoxCompByProfile";
	public static final String FIND_ACTIVE_MAILBOX_BY_PGUID = "findActiveMailBoxCompByProfile";
	public static final String INACTIVATE_MAILBOX = "inActivateMailBoxByGUID";
	public static final String PGUID = "pguid";

	public MailBox findActiveMailBox(String guid);

	public int deactiveMailBox(String guid);

}
