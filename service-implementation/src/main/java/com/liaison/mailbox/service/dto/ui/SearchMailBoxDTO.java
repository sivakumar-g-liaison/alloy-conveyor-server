/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall 
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.dto.ui;

import java.util.ArrayList;
import java.util.List;

import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.enums.MailBoxStatus;
import com.liaison.mailbox.jpa.model.MailBox;
import com.liaison.mailbox.jpa.model.MailBoxProperty;
import com.liaison.mailbox.service.dto.configuration.PropertyDTO;

/**
 * 
 * 
 * @author veerasamyn
 */
public class SearchMailBoxDTO {

	private String guid;
	private String name;
	private String description;
	private String status;
	private Integer serviceInstId;
	private String shardKey;
	private List<PropertyDTO> properties;
	private String profiles;
	private boolean incomplete;

	public String getGuid() {
		return guid;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public String getStatus() {
		return status;
	}

	public String getProfiles() {
		return profiles;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public void setProfiles(String profiles) {

		if (null == this.getProfiles()) {
			this.profiles = profiles;
		} else {
			this.profiles += " " + profiles;
		}
	}

	public Integer getServiceInstId() {
		return serviceInstId;
	}

	public String getShardKey() {
		return shardKey;
	}

	public List<PropertyDTO> getProperties() {

		if (null == properties) {
			properties = new ArrayList<PropertyDTO>();
		}
		return properties;
	}

	public void setServiceInstId(Integer serviceInstId) {
		this.serviceInstId = serviceInstId;
	}

	public void setShardKey(String shardKey) {
		this.shardKey = shardKey;
	}

	public void setProperties(List<PropertyDTO> properties) {
		this.properties = properties;
	}

	public boolean isIncomplete() {
		return incomplete;
	}

	public void setIncomplete(boolean incomplete) {
		this.incomplete = incomplete;
	}

	/**
	 * Copies all data from Entity to DTO.
	 * 
	 * @param mailBox
	 *            The MailBox Entity
	 */
	public void copyFromEntity(MailBox mailBox) {

		this.setGuid(mailBox.getPguid());
		this.setName(mailBox.getMbxName());
		this.setDescription(mailBox.getMbxDesc());

		MailBoxStatus status = MailBoxStatus.findByCode(mailBox.getMbxStatus());
		this.setStatus(status.name());

		this.setShardKey(mailBox.getShardKey());

		if (null != mailBox.getServiceInstId()) {
			this.setServiceInstId(mailBox.getServiceInstId().intValue());
		}

		PropertyDTO propertyDTO = null;
		for (MailBoxProperty property : mailBox.getMailboxProperties()) {

			propertyDTO = new PropertyDTO();
			propertyDTO.copyFromEntity(property, true);
			this.getProperties().add(propertyDTO);
		}

		// Boolean to denote the mailbox has processor or not
		boolean isMbxHasProcessors = false;
		if (mailBox.getMailboxProcessors() != null && !mailBox.getMailboxProcessors().isEmpty()) {
			isMbxHasProcessors = true;
		}

		if (MailBoxStatus.ACTIVE.value().equals(mailBox.getMbxStatus()) && !isMbxHasProcessors) {
			this.setStatus(MailBoxConstants.INCOMPLETE_STATUS);
		}

	}
}
