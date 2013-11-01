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

import com.liaison.mailbox.jpa.model.IdpProvider;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;

/**
 * 
 * 
 * @author praveenu
 */
public class IdpProviderDTO {

	private String guid;
	private String name;
	private String idpProviderURI;
	private String pswdResetPolicyURI;
	private String providerDefStorage;
	
	public String getGuid() {
		return guid;
	}
	public void setGuid(String guid) {
		this.guid = guid;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getIdpProviderURI() {
		return idpProviderURI;
	}
	public void setIdpProviderURI(String idpProviderURI) {
		this.idpProviderURI = idpProviderURI;
	}
	public String getPswdResetPolicyURI() {
		return pswdResetPolicyURI;
	}
	public void setPswdResetPolicyURI(String pswdResetPolicyURI) {
		this.pswdResetPolicyURI = pswdResetPolicyURI;
	}
	public String getProviderDefStorage() {
		return providerDefStorage;
	}
	public void setProviderDefStorage(String providerDefStorage) {
		this.providerDefStorage = providerDefStorage;
	}
	
	public void copyToEntity(IdpProvider idpProvider) throws MailBoxConfigurationServicesException {
		
		idpProvider.setIdpProviderUri(this.getIdpProviderURI());
		idpProvider.setProviderDefStorage(this.getProviderDefStorage());
		idpProvider.setName(this.getName());
		idpProvider.setPswdResetPolicyUri(this.getPswdResetPolicyURI());
	}
	
	public void copyFromEntity(IdpProvider idpProvider) throws MailBoxConfigurationServicesException {
		
		this.setGuid(idpProvider.getPguid());
		this.setIdpProviderURI(idpProvider.getIdpProviderUri());
		this.setName(idpProvider.getName());
		this.setProviderDefStorage(idpProvider.getProviderDefStorage());
		this.setPswdResetPolicyURI(idpProvider.getPswdResetPolicyUri());
	}
}
