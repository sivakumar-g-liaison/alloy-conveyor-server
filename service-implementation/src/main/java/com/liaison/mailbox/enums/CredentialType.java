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
 * Contains the list of credential types.
 * 
 * @author OFS
 */
public enum CredentialType {

	TRUSTSTORE_CERT("TRUSTSTORE_CERT"),
	SSH_KEYPAIR("SSH_KEYPAIR"),
	LOGIN_CREDENTIAL("LOGIN_CREDENTIAL");

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
    
	/**
	 * This  method will retrieve the CredentialType by given Credential type.
	 * 
	 * @param code 
	 *        The Credential type 
	 * @return CredentialType
	 */
	public static CredentialType findByCode(String code) {

		CredentialType found = null;
		for (CredentialType value : CredentialType.values()) {

			if (!MailBoxUtil.isEmpty(code) && code.equals(value.getCode())) {
				found = value;
				break;
			}
		}

		return found;
	}
	
   /**
    * This  method will retrieve the CredentialType by given credentialType from CredentialDTO.
    * 
    * @param name 
    *        The credential type
    * @return CredentialType
    */
	public static CredentialType findByName(String name) {

		CredentialType found = null;
		for (CredentialType value : CredentialType.values()) {

			if (!MailBoxUtil.isEmpty(name) && name.equals(value.name())) {
				found = value;
				break;
			}
		}

		return found;

	}

}
