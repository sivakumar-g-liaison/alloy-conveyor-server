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

import java.util.List;

import com.liaison.mailbox.jpa.model.IdpProfile;

/**
 * 
 * 
 * @author praveenu
 */
public class IdpUserProfileDTO {

	private String idpProvider;
	private String gatewayType;
	private String loginDomain;
	private String guid;
	
	public String getIdpProvider() {
		return idpProvider;
	}
	public void setIdpProvider(String idpProvider) {
		this.idpProvider = idpProvider;
	}
	public String getLoginDomain() {
		return loginDomain;
	}
	public void setLoginDomain(String loginDomain) {
		this.loginDomain = loginDomain;
	}
	public String getGatewayType() {
		return gatewayType;
	}
	public void setGatewayType(String gatewayType) {
		this.gatewayType = gatewayType;
	}
	
	public void copyFromEntity(List<IdpProfile> idpProfiles){
		
		
	}
	public String getGuid() {
		return guid;
	}
	public void setGuid(String guid) {
		this.guid = guid;
	}
}
