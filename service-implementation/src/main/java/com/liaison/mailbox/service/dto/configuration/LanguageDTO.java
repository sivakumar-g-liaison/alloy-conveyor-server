package com.liaison.mailbox.service.dto.configuration;

import com.liaison.mailbox.jpa.model.Language;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.util.MailBoxUtility;

public class LanguageDTO {

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
	public void copyToEntity(Language language) throws MailBoxConfigurationServicesException {

		language.setPguid(MailBoxUtility.getGUID());
		language.setName(this.getName());
	}
	public void copyFromEntity(Language language) throws MailBoxConfigurationServicesException {
		
		this.setGuid(language.getPguid());
		this.setName(language.getName());
	}
}
