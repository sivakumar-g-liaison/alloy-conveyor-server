/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.dtdm.dao;

import java.util.List;

import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import com.liaison.commons.jpa.GenericDAO;
import com.liaison.mailbox.dtdm.model.MailBox;

/**
 * @author OFS
 *
 */
@NamedQueries({
		@NamedQuery(name = MailBoxConfigurationDAO.GET_MBX,
				query = "SELECT mbx FROM MailBox mbx"
						+ " inner join mbx.mailboxProcessors prcsr"
						+ " inner join prcsr.scheduleProfileProcessors schd_prof_processor"
						+ " inner join schd_prof_processor.scheduleProfilesRef profile"
						+ " where LOWER(mbx.mbxName) like :" + MailBoxConfigurationDAO.MBOX_NAME
						+ " and profile.schProfName like :" + MailBoxConfigurationDAO.SCHD_PROF_NAME
						+ " order by mbx.mbxName"),
	  @NamedQuery(name = MailBoxConfigurationDAO.FIND_BY_MBX_NAME_AND_TENANCYKEY_NAME, query = "SELECT mbx from MailBox mbx "
				        + "WHERE mbx.mbxName =:" + MailBoxConfigurationDAO.MBOX_NAME + " and mbx.tenancyKey =:" + MailBoxConfigurationDAO.TENANCY_KEYS)
})
public interface MailBoxConfigurationDAO extends GenericDAO<MailBox> {

	public static final String FIND_BY_MBX_NAME = "findByMboxName";
	public static final String PGUID = "pguid";
	public static final String MBOX_NAME = "mbox_name";
	public static final String GET_MBX = "findMailBoxes";
	public static final String SCHD_PROF_NAME = "schd_name";
	public static final String TENANCY_KEYS = "tenancy_keys";
	public static final String FIND_BY_MBX_NAME_AND_TENANCYKEY_NAME = "findByMboxNameAndTenancyKeyName";

	public int getMailboxCountByProtocol(String mbxName, String profName, List <String> tenancyKeys);
	public List<MailBox> find(String mbxName, String profName, List <String> tenancyKeys, int startOffset, int count, String sortField, String sortDirection);

	public int getMailboxCountByName(String mbxName, List<String> tenancyKeys);
	public List<MailBox> findByName(String mbxName, List <String> tenancyKeys, int startOffset, int count, String sortField, String sortDirection);
	public MailBox findByMailBoxNameAndTenancyKeyName(String mbxName, String tenancyKeyName);
	
	/**
	 * Method to retrieve all mailbox guids linked to a given tenancy keys
	 * @param tenancyKey
	 * @return List of Mailbox Ids linked to given tenancykeys
	 */
	public List<String> findAllMailboxesLinkedToTenancyKeys(List<String> tenancyKeys);
}
