package com.liaison.mailbox.service.dto.configuration;

import com.liaison.mailbox.jpa.model.GatewayType;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.util.MailBoxUtility;


public class GatewayTypeDTO {

	private String guid;
	private String name;
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
	
	public void copyToEntity(GatewayType gatewayType) throws MailBoxConfigurationServicesException {

		gatewayType.setPguid(MailBoxUtility.getGUID());
		gatewayType.setName(this.getName());
	}
	public void copyFromEntity(GatewayType gatewayType) throws MailBoxConfigurationServicesException {
		
		this.setGuid(gatewayType.getPguid());
		this.setName(gatewayType.getName());
	}
}
