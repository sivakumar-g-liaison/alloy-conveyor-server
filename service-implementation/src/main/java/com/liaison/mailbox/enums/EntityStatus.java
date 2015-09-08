/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall 
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.enums;

import com.liaison.mailbox.service.util.MailBoxUtil;

/**
 * Contains the status for entity.
 * 
 * @author OFS
 */
public enum EntityStatus {

	ACTIVE("ACTIVE"),
	INACTIVE("INACTIVE");

	private String value;

	private EntityStatus(String status) {
		this.value = status;
	}

	public String value() {
		return value;
	}
    
	/**
	 * This method will retrieve the EntityStatus by given MailBox status.
	 * 
	 * @param code the MailBox status as string
	 * @return EntityStatus
	 */
	public static EntityStatus findByCode(String code) {

		EntityStatus found = null;
		for (EntityStatus value : EntityStatus.values()) {

			if (!MailBoxUtil.isEmpty(code) && code.equals(value.value())) {
				found = value;
				break;
			}
		}

		return found;
	}
    
	/**
	 * This method will retrieve the EntityStatus by given status from MailBoxDTO.
	 * 
	 * @param name 
	 *        the MailBoxDTO status
	 * @return EntityStatus
	 */
	public static EntityStatus findByName(String name) {

		EntityStatus found = null;
		for (EntityStatus value : EntityStatus.values()) {

			if (!MailBoxUtil.isEmpty(name) && name.equals(value.name())) {
				found = value;
				break;
			}
		}

		return found;

	}

}
