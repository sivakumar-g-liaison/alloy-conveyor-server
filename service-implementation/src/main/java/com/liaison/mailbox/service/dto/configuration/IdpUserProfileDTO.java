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


/**
 * 
 * 
 * @author praveenu
 */
public class IdpUserProfileDTO {

	private String idpProviderGuid;
	private String gatewayTypeGuid;
	private String loginDomain;
	private String guid;
	
	public String getIdpProviderGuid() {
		return idpProviderGuid;
	}
	public void setIdpProviderGuid(String idpProviderGuid) {
		this.idpProviderGuid = idpProviderGuid;
	}
	public String getLoginDomain() {
		return loginDomain;
	}
	public void setLoginDomain(String loginDomain) {
		this.loginDomain = loginDomain;
	}
	public String getGatewayTypeGuid() {
		return gatewayTypeGuid;
	}
	public void setGatewayTypeGuid(String gatewayTypeGuid) {
		this.gatewayTypeGuid = gatewayTypeGuid;
	}
	
	public String getGuid() {
		return guid;
	}
	public void setGuid(String guid) {
		this.guid = guid;
	}
}
