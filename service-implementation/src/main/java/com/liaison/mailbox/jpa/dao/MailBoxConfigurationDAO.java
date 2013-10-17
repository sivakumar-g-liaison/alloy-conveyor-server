package com.liaison.mailbox.jpa.dao;

import java.util.Set;

import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import com.liaison.commons.jpa.GenericDAO;
import com.liaison.mailbox.jpa.model.MailBox;

@NamedQueries({
		@NamedQuery(name = MailBoxConfigurationDAO.FIND_BY_NAME,
				query = "SELECT mbc FROM MailBox mbc where mbc.mbxName like :" + MailBoxConfigurationDAO.MBX_NAME),
		@NamedQuery(name = MailBoxConfigurationDAO.FIND_ACTIVE_MAILBOX_BY_PGUID,
				query = "SELECT mbc FROM MailBox mbc WHERE mbc.mbxStatus = 'active' and mbc.pguid = :"
						+ MailBoxConfigurationDAO.PGUID),
		@NamedQuery(name = MailBoxConfigurationDAO.INACTIVATE_MAILBOX,
				query = "UPDATE MailBox mbc set mbc.mbxStatus = 'inactive' where mbc.pguid = :" + MailBoxConfigurationDAO.PGUID),
		@NamedQuery(name = MailBoxConfigurationDAO.GET_MBX,
				query = "SELECT mbx FROM MailBox mbx"
						+ " inner join mbx.mailboxProcessors prcsr"
						+ " inner join prcsr.scheduleProfileProcessors schd_prof_processor"
						+ " inner join schd_prof_processor.scheduleProfilesRef profile"
						+ " where mbx.mbxName like :" + MailBoxConfigurationDAO.MBX_NAME
						+ " and profile.schProfName like :" + MailBoxConfigurationDAO.SCHD_PROF_NAME
						+ " order by mbx.mbxName")
})
public interface MailBoxConfigurationDAO extends GenericDAO<MailBox> {

	public static final String FIND_BY_NAME = "findByName";
	public static final String FIND_ACTIVE_MAILBOX_BY_PGUID = "findActiveMailBoxCompByProfile";
	public static final String INACTIVATE_MAILBOX = "inActivateMailBoxByGUID";
	public static final String PGUID = "pguid";
	public static final String MBX_NAME = "mbx_name";
	public static final String GET_MBX = "findMailBoxes";
	public static final String SCHD_PROF_NAME = "schd_name";

	public MailBox findActiveMailBox(String guid);

	public int deactiveMailBox(String guid);

	public Set<MailBox> find(String mbxName, String profName);

	public Set<MailBox> findByName(String mbxName);

}
