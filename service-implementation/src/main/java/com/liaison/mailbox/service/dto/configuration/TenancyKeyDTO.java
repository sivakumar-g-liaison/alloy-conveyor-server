package com.liaison.mailbox.service.dto.configuration;

import com.wordnik.swagger.annotations.ApiModel;

@ApiModel(value = "tenancykey")
public class TenancyKeyDTO {

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
}
