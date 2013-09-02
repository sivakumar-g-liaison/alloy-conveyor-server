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

import com.liaison.mailbox.jpa.model.Credential;

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
	
	public void copyToEntity(Object entity) {

		Credential credential = (Credential) entity;
		credential.setCredsIdpType(this.getIdpType());
		credential.setCredsIdpUri(this.getIdpURI());
		credential.setCredsPassword(this.getPassword());
		credential.setCredsType(this.getCredentialType());
		credential.setCredsUri(this.getCredentialURI());
		credential.setCredsUsername(this.getUserId());
		credential.setPguid(this.getGuId());
		
	}

	public void copyFromEntity(Object entity) {

		Credential credential = (Credential) entity;
		
		this.setIdpType(credential.getCredsIdpType());
		this.setIdpURI(credential.getCredsIdpUri());
		this.setPassword(credential.getCredsPassword());
		this.setCredentialType(credential.getCredsType());
		this.setCredentialURI(credential.getCredsUri());
		this.setUserId(credential.getCredsUsername());
		this.setGuId(credential.getPguid());
	}
}
