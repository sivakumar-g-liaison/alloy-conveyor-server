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
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.util.MailBoxUtility;

/**
 * 
 * 
 * @author praveenu
 */
public class IdpProfileDTO {

	private String guid;
	private String loginDomain;
	private GatewayTypeDTO gatewayType;
	private List<IdpUserProfileDTO> idpProvider;
	
	public String getGuid() {
		return guid;
	}
	public void setGuid(String guid) {
		this.guid = guid;
	}
	
	public String getLoginDomain() {
		return loginDomain;
	}
	public void setLoginDomain(String loginDomain) {
		this.loginDomain = loginDomain;
	}
	public List<IdpUserProfileDTO> getIdpProvider() {
		return idpProvider;
	}
	public void setIdpProvider(List<IdpUserProfileDTO> ipdProvider) {
		this.idpProvider = ipdProvider;
	}
	public GatewayTypeDTO getGatewayType() {
		return gatewayType;
	}
	public void setGatewayType(GatewayTypeDTO gatewayType) {
		this.gatewayType = gatewayType;
	}
	
	public void copyToEntity(IdpProfile idpProfile, boolean iscreate) throws MailBoxConfigurationServicesException {
		
		if(iscreate){
			idpProfile.setPguid(MailBoxUtility.getGUID());
		}
		idpProfile.setLoginDomain(this.getLoginDomain());
		
		// Setting the property
		//IdpProvider idpProvider = null;
		//List<IdpProvider> idpProviderList = new ArrayList<>();
		/*for (IdpProviderDTO idpProviderDTO : this.getIdpProvider()) {

			idpProvider = new IdpProvider();
			idpProviderDTO.copyToEntity(idpProvider);
			idpProviderList.add(idpProvider);
		}*/
		/*if (!idpProviderList.isEmpty()) {
			idpProfile.setIdpProvider(idpProviderList);
		}*/
	}
	public void copyFromEntity(IdpProfile idpProfile) throws MailBoxConfigurationServicesException {
		
		this.setGuid(idpProfile.getPguid());
		this.setLoginDomain(idpProfile.getLoginDomain());
		
		if(null != idpProfile.getGatewayType()){
			
			GatewayTypeDTO gatewayTypeDTO = new GatewayTypeDTO();
			gatewayTypeDTO.copyFromEntity(idpProfile.getGatewayType());
		}
		
		
	}
}
