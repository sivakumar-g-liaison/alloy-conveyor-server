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

	String FIND_BY_MBX_NAME = "MailBox.findByMboxName";
	String PGUID = "pguid";
	String MBOX_NAME = "mbox_name";
	String GET_MBX = "MailBox.findMailBoxes";
	String SCHD_PROF_NAME = "schd_name";
	String TENANCY_KEYS = "tenancy_keys";
	String FIND_BY_MBX_NAME_AND_TENANCY_KEY_NAME = "MailBox.findByMboxNameAndTenancyKeyName";
	String GET_MBX_BY_NAME = "MailBox.getMailboxByName";
	String STATUS = "status";

	/**
	 * retrieve number of mailboxes that linked with profile
	 *
	 * @param searchFilter filters
	 * @param tenancyKeys tenancy ket
     * @return count
     */
	int getMailboxCountByProfile(GenericSearchFilterDTO searchFilter, List<String> tenancyKeys);

	/**
	 * search mailboxes by the given filter
	 *
	 * @param searchFilter search filter
	 * @param tenancyKeys tenancy keys
	 * @param pageOffsetDetails page details
     * @return list of mailbox
     */
	List<MailBox> find(GenericSearchFilterDTO searchFilter, List<String> tenancyKeys, Map<String, Integer> pageOffsetDetails);

	/**
	 * retrieve mailbox count by name
	 *
	 * @param searchFilter search filter
	 * @param tenancyKeys tenancy keys
     * @return count
     */
	int getMailboxCountByName(GenericSearchFilterDTO searchFilter, List<String> tenancyKeys);

	/**
	 * search mailboxes by the given filter
	 *
	 * @param searchFilter search filter
	 * @param tenancyKeys tenancy keys
	 * @param pageOffsetDetails page details
     * @return list of mailbox
     */
	List<MailBox> findByName(GenericSearchFilterDTO searchFilter, List<String> tenancyKeys, Map<String, Integer> pageOffsetDetails);

	/**
	 * find mailbox by name and tenancy key
	 *
	 * @param mbxName mailbox name
	 * @param tenancyKeyName tenancy key name
     * @return mailbox entity
     */
	MailBox findByMailBoxNameAndTenancyKeyName(String mbxName, String tenancyKeyName);

	/**
	 * Method to retrieve all mailbox guids linked to a given tenancy keys
	 * @param tenancyKeys tenancy keys
	 * @return List of Mailbox Ids linked to given tenancykeys
	 */
	List<String> findAllMailboxesLinkedToTenancyKeys(List<String> tenancyKeys);

	/**
	 * retrieve mailbox based on given name
	 * 
	 * @param name mailbox name
	 * @return mailbox entity
	 */
	MailBox getMailboxByName(String name);
}
