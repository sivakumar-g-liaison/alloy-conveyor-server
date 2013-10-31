package com.liaison.mailbox.service.dto.configuration;

import java.util.List;

import com.liaison.mailbox.jpa.model.IdpProfile;


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
