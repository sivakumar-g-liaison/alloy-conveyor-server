/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall 
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.dto.configuration;

import com.liaison.commons.security.pkcs7.SymmetricAlgorithmException;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.enums.CredentialType;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.jpa.model.Credential;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.util.MailBoxCryptoUtil;
import com.liaison.mailbox.service.util.MailBoxUtility;
import com.liaison.mailbox.service.validation.DataValidation;
import com.liaison.mailbox.service.validation.Mandatory;

/**
 * 
 * 
 * @author sivakumarg
 */
public class CredentialDTO {

	private String guId;
	private String credentialType;
	private String credentialURI;
	private String userId;
	private String password;
	private String idpType;
	private String idpURI;

	public String getGuId() {
		return guId;
	}

	public void setGuId(String guId) {
		this.guId = guId;
	}

	@Mandatory(errorMessage = "Credential Type is mandatory.")
	@DataValidation(errorMessage = "Credential type set to a value that is not supported.", type = MailBoxConstants.CREDENTIAL_TYPE)
	public String getCredentialType() {
		return credentialType;
	}

	public void setCredentialType(String credentialType) {
		this.credentialType = credentialType;
	}

	public String getCredentialURI() {
		return credentialURI;
	}

	public void setCredentialURI(String credentialURI) {
		this.credentialURI = credentialURI;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getIdpType() {
		return idpType;
	}

	public void setIdpType(String idpType) {
		this.idpType = idpType;
	}

	public String getIdpURI() {
		return idpURI;
	}

	public void setIdpURI(String idpURI) {
		this.idpURI = idpURI;
	}

	public void copyToEntity(Object entity) throws SymmetricAlgorithmException, MailBoxConfigurationServicesException {

		Credential credential = (Credential) entity;
		credential.setCredsIdpType(this.getIdpType());
		credential.setCredsIdpUri(this.getIdpURI());

		if (!MailBoxUtility.isEmpty(this.getPassword())) {
			credential.setCredsPassword(MailBoxCryptoUtil.doPasswordEncryption(this.getPassword(), 1));
		}
		CredentialType foundCredentialType = CredentialType.findByName(this.getCredentialType());
		if (foundCredentialType == null) {
			throw new MailBoxConfigurationServicesException(Messages.ENUM_TYPE_DOES_NOT_SUPPORT, "Credential");
		}
		credential.setCredsType(foundCredentialType.getCode());
		credential.setCredsUri(this.getCredentialURI());
		credential.setCredsUsername(this.getUserId());
		credential.setPguid(this.getGuId());

	}

	public void copyFromEntity(Object entity) throws SymmetricAlgorithmException, MailBoxConfigurationServicesException {

		Credential credential = (Credential) entity;

		this.setIdpType(credential.getCredsIdpType());
		this.setIdpURI(credential.getCredsIdpUri());

		if (!MailBoxUtility.isEmpty(credential.getCredsPassword())) {

			this.setPassword(MailBoxCryptoUtil.doPasswordEncryption(credential.getCredsPassword(), 2));
		}
		CredentialType foundCredentialType = CredentialType.findByCode(credential.getCredsType());
		if (foundCredentialType == null) {
			throw new MailBoxConfigurationServicesException(Messages.ENUM_TYPE_DOES_NOT_SUPPORT, "Credential");
		}
		this.setCredentialType(foundCredentialType.name());
		this.setCredentialURI(credential.getCredsUri());
		this.setUserId(credential.getCredsUsername());
		this.setGuId(credential.getPguid());
	}

	public String getDecryptedString(String encryptedValue) throws SymmetricAlgorithmException {
		return MailBoxCryptoUtil.doPasswordEncryption(encryptedValue, 2);
	}
}
