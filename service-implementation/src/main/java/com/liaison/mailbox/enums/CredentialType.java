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
 * @author praveenu
 * 
 */
public enum CredentialType {

	TRUST_STORE("trust_store"),
	KEY_STORE("key_store"),
	LOGIN_CREDENTIAL("login_credential");

	private final String code;

	private CredentialType(String code) {
		this.code = code;
	}

	@Override
	public String toString() {
		return code;
	}

	public String getCode() {
		return code;
	}

	public static CredentialType findByCode(String code) {

		CredentialType found = null;
		for (CredentialType value : CredentialType.values()) {

			if (!MailBoxUtility.isEmpty(code) && code.equals(value.getCode())) {
				found = value;
				break;
			}
		}

		return found;
	}

	public static CredentialType findByName(String name) {

		CredentialType found = null;
		for (CredentialType value : CredentialType.values()) {

			if (!MailBoxUtility.isEmpty(name) && name.equals(value.name())) {
				found = value;
				break;
			}
		}

		return found;

	}

}
