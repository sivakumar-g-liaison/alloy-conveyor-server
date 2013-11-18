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

public enum ProcessorType {

	REMOTEDOWNLOADER("remotedownloader"),
	REMOTEUPLOADER("remoteuploader"),
	SWEEPER("sweeper");

	private final String code;

	private ProcessorType(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public static ProcessorType findByCode(String code) {

		ProcessorType found = null;
		for (ProcessorType value : ProcessorType.values()) {

			if (!MailBoxUtility.isEmpty(code) && code.equals(value.getCode())) {
				found = value;
				break;
			}
		}

		return found;
	}

	public static ProcessorType findByName(String name) {

		ProcessorType found = null;
		for (ProcessorType value : ProcessorType.values()) {

			if (!MailBoxUtility.isEmpty(name) && name.equals(value.name())) {
				found = value;
				break;
			}
		}

		return found;

	}

}
