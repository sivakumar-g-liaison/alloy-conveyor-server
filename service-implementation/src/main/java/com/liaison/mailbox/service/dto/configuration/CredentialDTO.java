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

import javax.ws.rs.core.Response;

import com.liaison.commons.security.pkcs12.SymmetricAlgorithmException;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.enums.CredentialType;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.jpa.model.Credential;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.util.MailBoxCryptoUtil;
import com.liaison.mailbox.service.util.MailBoxUtil;
import com.liaison.mailbox.service.validation.DataValidation;
import com.liaison.mailbox.service.validation.Mandatory;
import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

/**
 *
 *
 * @author OFS
 */

@ApiModel(value = "credential")
public class CredentialDTO {

	private String guId;
	@ApiModelProperty( value = "Credential Type", required = true)
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

	/**
	 *Copies all data from DTO to entity.
	 *
	 * @param entity
	 *        the Credential Entity
	 * @throws SymmetricAlgorithmException
	 * @throws MailBoxConfigurationServicesException
	 */

	public void copyToEntity(Object entity) throws SymmetricAlgorithmException, MailBoxConfigurationServicesException {

		Credential credential = (Credential) entity;
		credential.setCredsIdpUri(this.getIdpURI());

		if (!MailBoxUtil.isEmpty(this.getPassword())) {
			credential.setCredsPassword(this.getPassword());
		}
		CredentialType foundCredentialType = CredentialType.findByName(this.getCredentialType());
		if (foundCredentialType == null) {
			throw new MailBoxConfigurationServicesException(Messages.ENUM_TYPE_DOES_NOT_SUPPORT, "Credential", Response.Status.BAD_REQUEST);
		}

		credential.setCredsType(foundCredentialType.getCode());

		credential.setCredsIdpType(this.getIdpType());
		credential.setCredsUri(this.getCredentialURI());
		credential.setCredsUsername(this.getUserId());
		credential.setPguid(this.getGuId());

	}

	/**
	 *Copies all data from entity to DTO.
	 *
	 * @param entity
	 *        the Credential Entity
	 * @throws SymmetricAlgorithmException
	 * @throws MailBoxConfigurationServicesException
	 */
	public void copyFromEntity(Object entity) throws SymmetricAlgorithmException, MailBoxConfigurationServicesException {

		Credential credential = (Credential) entity;

		this.setIdpURI(credential.getCredsIdpUri());

		if (!MailBoxUtil.isEmpty(credential.getCredsPassword())) {

			this.setPassword(credential.getCredsPassword());
		}

		CredentialType foundCredentialType = CredentialType.findByCode(credential.getCredsType());
		if (foundCredentialType == null) {
			throw new MailBoxConfigurationServicesException(Messages.ENUM_TYPE_DOES_NOT_SUPPORT, "Credential", Response.Status.BAD_REQUEST);
		}
		this.setCredentialType(foundCredentialType.name());

		this.setIdpType(credential.getCredsIdpType());
		this.setCredentialURI(credential.getCredsUri());
		this.setUserId(credential.getCredsUsername());
		this.setGuId(credential.getPguid());
	}

	public String getDecryptedString(String encryptedValue) throws SymmetricAlgorithmException {
		return MailBoxCryptoUtil.doPasswordEncryption(encryptedValue, 2);
	}
}
