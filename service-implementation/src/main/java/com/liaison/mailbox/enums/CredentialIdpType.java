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

/**
 * @author OFS
 *
 */
public enum CredentialIdpType {
	
	FTPS("ftps"),
	SFTP("sftp");

	private final String code;

	private CredentialIdpType(String code) {
		this.code = code;
	}

	@Override
	public String toString() {
		return code;
	}

	public String getCode() {
		return code;
	}

	public static CredentialIdpType findByCode(String code) {

		CredentialIdpType found = null;
		for (CredentialIdpType value : CredentialIdpType.values()) {

			if (!MailBoxUtility.isEmpty(code) && code.equals(value.getCode())) {
				found = value;
				break;
			}
		}

		return found;
	}

	public static CredentialIdpType findByName(String name) {

		CredentialIdpType found = null;
		for (CredentialIdpType value : CredentialIdpType.values()) {

			if (!MailBoxUtility.isEmpty(name) && name.equals(value.name())) {
				found = value;
				break;
			}
		}

		return found;

	}
}
