/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall 
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.dto.configuration.request;

import com.liaison.commons.security.pkcs7.SymmetricAlgorithmException;
import com.liaison.mailbox.service.util.MailBoxCryptoUtil;

public class CredentialInfoModel {

	private String username;
	private String password;
	private String fileURI;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() throws SymmetricAlgorithmException {
		return getDecryptedString(password);
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getDecryptedString(String encryptedValue) throws SymmetricAlgorithmException {
		return MailBoxCryptoUtil.doPasswordEncryption(encryptedValue, 2);
	}

	public String getFileURI() {
		return fileURI;
	}

	public void setFileURI(String fileURI) {
		this.fileURI = fileURI;
	}

}
