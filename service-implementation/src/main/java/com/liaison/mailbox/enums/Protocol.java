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

public enum Protocol {

	FTP("ftp"),
	FTPS("ftps"),
	SFTP("sftp"),
	HTTP("http"),
	HTTPS("https"),
	SWEEPER("sweeper");

	private final String code;

	private Protocol(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public static Protocol findByCode(String code) {

		Protocol found = null;
		for (Protocol value : Protocol.values()) {

			if (!MailBoxUtility.isEmpty(code) && code.equals(value.getCode())) {
				found = value;
				break;
			}
		}

		return found;
	}

	public static Protocol findByName(String name) {

		Protocol found = null;
		for (Protocol value : Protocol.values()) {

			if (!MailBoxUtility.isEmpty(name) && name.equals(value.name())) {
				found = value;
				break;
			}
		}

		return found;

	}

}
