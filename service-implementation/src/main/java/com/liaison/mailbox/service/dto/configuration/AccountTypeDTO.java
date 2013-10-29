package com.liaison.mailbox.service.dto.configuration;

import com.liaison.mailbox.jpa.model.AccountType;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.util.MailBoxUtility;

public class AccountTypeDTO {
	
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
	
	public void copyToEntity(AccountType accountType) throws MailBoxConfigurationServicesException {
		
		accountType.setPguid(MailBoxUtility.getGUID());
		accountType.setName(this.getName());
	}
	
	public void copyFromEntity(AccountType entity) throws MailBoxConfigurationServicesException {
		
		this.setGuid(entity.getPguid());
		this.setName(entity.getName());
	}
}
