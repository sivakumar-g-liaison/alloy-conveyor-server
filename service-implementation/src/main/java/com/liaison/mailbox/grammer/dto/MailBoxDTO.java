/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall 
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.grammer.dto;

import java.util.ArrayList;
import java.util.List;

import com.liaison.mailbox.jpa.model.MailBox;
import com.liaison.mailbox.jpa.model.MailBoxProperty;

/**
 * 
 *
 * @author veerasamyn
 */
public class MailBoxDTO {

	private String name;
	private String description;
	private String status;
	private List<MailBoxPropertyDTO> properties;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public List<MailBoxPropertyDTO> getProperties() {

		if (null == properties) {
			properties = new ArrayList<>();
		}
		return properties;
	}

	public void setProperties(List<MailBoxPropertyDTO> properties) {
		this.properties = properties;
	}

	public void copyToEntity(Object entity) {

		MailBox mailBox = (MailBox) entity;
		mailBox.setMbxName(this.getName());
		mailBox.setMbxDesc(this.getDescription());
		mailBox.setMbxStatus(this.getStatus());

		MailBoxProperty property = null;
		List<MailBoxProperty> properties = new ArrayList<>();
		for (MailBoxPropertyDTO propertyDTO : this.getProperties()) {

			property = new MailBoxProperty();
			propertyDTO.copyToEntity(property);
			properties.add(property);
		}
		
	}

	public void copyFromEntity(Object entity) {

		MailBox mailBox = (MailBox) entity;
		this.setName(mailBox.getMbxName());
		this.setDescription(mailBox.getMbxDesc());
		this.setStatus(mailBox.getMbxStatus());

		MailBoxPropertyDTO propertyDTO = null;
		for (MailBoxProperty property : mailBox.getMailboxProperties()) {
			propertyDTO = new MailBoxPropertyDTO();
			propertyDTO.copyFromEntity(property);
			this.getProperties().add(propertyDTO);
		}
	}

}
