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

import com.liaison.mailbox.service.util.MailBoxUtility;

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

	public static MailBoxStatus findByCode(String code) {

		MailBoxStatus found = null;
		for (MailBoxStatus value : MailBoxStatus.values()) {

			if (!MailBoxUtility.isEmpty(code)) {
				if (code.equals(value.value())) {
					found = value;
					break;
				}
			}
		}

		return found;
	}

	public static MailBoxStatus findByName(String name) {

		MailBoxStatus found = null;
		for (MailBoxStatus value : MailBoxStatus.values()) {

			if (!MailBoxUtility.isEmpty(name)) {
				if (name.equals(value.name())) {
					found = value;
					break;
				}
			}
		}

		return found;

	}

}
