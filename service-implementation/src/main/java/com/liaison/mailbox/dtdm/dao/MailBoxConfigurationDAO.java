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
import java.util.Map;

import com.liaison.commons.jpa.GenericDAO;
import com.liaison.mailbox.dtdm.model.MailBox;
import com.liaison.mailbox.service.dto.GenericSearchFilterDTO;

/**
 * The dao class for the MAILBOX database table.
 * 
 * @author OFS
 */
public interface MailBoxConfigurationDAO extends GenericDAO<MailBox> {

	public static final String FIND_BY_MBX_NAME = "MailBox.findByMboxName";
	public static final String PGUID = "pguid";
	public static final String MBOX_NAME = "mbox_name";
	public static final String GET_MBX = "MailBox.findMailBoxes";
	public static final String SCHD_PROF_NAME = "schd_name";
	public static final String TENANCY_KEYS = "tenancy_keys";
	public static final String FIND_BY_MBX_NAME_AND_TENANCYKEY_NAME = "MailBox.findByMboxNameAndTenancyKeyName";

	public int getMailboxCountByProfile(GenericSearchFilterDTO searchFilter, List <String> tenancyKeys);
	public List<MailBox> find(GenericSearchFilterDTO searchFilter, List <String> tenancyKeys, Map <String, Integer> pageOffsetDetails);

	public int getMailboxCountByName(GenericSearchFilterDTO searchFilter, List<String> tenancyKeys);
	public List<MailBox> findByName(GenericSearchFilterDTO searchFilter, List <String> tenancyKeys, Map <String, Integer> pageOffsetDetails);
	public MailBox findByMailBoxNameAndTenancyKeyName(String mbxName, String tenancyKeyName);
	
	/**
	 * Method to retrieve all mailbox guids linked to a given tenancy keys
	 * @param tenancyKey
	 * @return List of Mailbox Ids linked to given tenancykeys
	 */
	public List<String> findAllMailboxesLinkedToTenancyKeys(List<String> tenancyKeys);
}
