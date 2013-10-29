package com.liaison.mailbox.service.dto.configuration;

import java.util.ArrayList;
import java.util.List;

import com.liaison.mailbox.jpa.model.IdpProfile;
import com.liaison.mailbox.jpa.model.IdpProvider;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.util.MailBoxUtility;

public class IdpProfileDTO {

	private String guid;
	private String loginDomain;
	private GatewayTypeDTO gatewayType;
	private List<IdpProviderDTO> idpProvider;
	
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
	public List<IdpProviderDTO> getIdpProvider() {
		return idpProvider;
	}
	public void setIdpProvider(List<IdpProviderDTO> ipdProvider) {
		this.idpProvider = ipdProvider;
	}
	public GatewayTypeDTO getGatewayType() {
		return gatewayType;
	}
	public void setGatewayType(GatewayTypeDTO gatewayType) {
		this.gatewayType = gatewayType;
	}
	
	public void copyToEntity(IdpProfile idpProfile) throws MailBoxConfigurationServicesException {
		
		idpProfile.setPguid(MailBoxUtility.getGUID());
		idpProfile.setLoginDomain(this.getLoginDomain());
		this.getGatewayType().copyToEntity(idpProfile.getGatewayType());
		
		// Setting the ipdProvider
		IdpProvider idpProvider = null;
		List<IdpProvider> idpProviderList = new ArrayList<>();
		for (IdpProviderDTO idpProviderDTO : this.getIdpProvider()) {

			idpProvider = new IdpProvider();
			idpProviderDTO.copyToEntity(idpProvider);
			idpProviderList.add(idpProvider);
		}
		if (!idpProviderList.isEmpty()) {
			idpProfile.setIdpProvider(idpProviderList);
		}
	}
	public void copyFromEntity(IdpProfile idpProfile) throws MailBoxConfigurationServicesException {
		
		this.setGuid(idpProfile.getPguid());
		this.setLoginDomain(idpProfile.getLoginDomain());
		
		if(null != idpProfile.getGatewayType()){
			
			GatewayTypeDTO gatewayTypeDTO = new GatewayTypeDTO();
			gatewayTypeDTO.copyFromEntity(idpProfile.getGatewayType());
		}
		// Set properties
		if (null != idpProfile.getIdpProvider()) {

			IdpProviderDTO idpProviderDTO = null;
			for (IdpProvider idpProvider : idpProfile.getIdpProvider()) {
				idpProviderDTO = new IdpProviderDTO();
				idpProviderDTO.copyFromEntity(idpProvider);
				this.getIdpProvider().add(idpProviderDTO);
			}
		}
	}
}
