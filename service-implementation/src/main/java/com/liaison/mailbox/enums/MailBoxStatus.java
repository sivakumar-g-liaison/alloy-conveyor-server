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

public enum MailBoxStatus {

	ACTIVE("active"),
	INACTIVE("inactive");

	private String value;

	private MailBoxStatus(String status) {
		this.value = status;
	}

	public String value() {
		return value;
	}
    
	/**
	 * This method will retrieve the MailBoxStatus by given MailBox status.
	 * 
	 * @param code the MailBox status as string
	 * @return MailBoxStatus
	 */
	public static MailBoxStatus findByCode(String code) {

		MailBoxStatus found = null;
		for (MailBoxStatus value : MailBoxStatus.values()) {

			if (!MailBoxUtil.isEmpty(code) && code.equals(value.value())) {
				found = value;
				break;
			}
		}

		return found;
	}
    
	/**
	 * This method will retrieve the MailBoxStatus by given status from MailBoxDTO.
	 * 
	 * @param name 
	 *        the MailBoxDTO status
	 * @return MailBoxStatus
	 */
	public static MailBoxStatus findByName(String name) {

		MailBoxStatus found = null;
		for (MailBoxStatus value : MailBoxStatus.values()) {

			if (!MailBoxUtil.isEmpty(name) && name.equals(value.name())) {
				found = value;
				break;
			}
		}

		return found;

	}

}
