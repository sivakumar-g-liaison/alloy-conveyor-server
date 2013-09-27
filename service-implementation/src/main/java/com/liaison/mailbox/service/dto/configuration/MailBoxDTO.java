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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.enums.MailBoxStatus;
import com.liaison.mailbox.jpa.model.MailBox;
import com.liaison.mailbox.jpa.model.MailBoxProperty;
import com.liaison.mailbox.jpa.model.MailBoxSchedProfile;
import com.liaison.mailbox.jpa.model.Processor;
import com.liaison.mailbox.service.validation.DataValidation;
import com.liaison.mailbox.service.validation.Mandatory;

/**
 * 
 * 
 * @author veerasamyn
 */
public class MailBoxDTO {

	private String guid;
	private String name;
	private String description;
	private String status;
	private Integer serviceInstId;
	private String shardKey;
	private List<MailBoxPropertyDTO> properties;
	private List<ProfileDTO> profiles;

	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	@Mandatory(errorMessage = "MailBox name is mandatory.")
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

	@Mandatory(errorMessage = "MailBox status is mandatory.")
	@DataValidation(errorMessage = "MailBox status set to a value that is not supported.", type = MailBoxConstants.MBX_STATUS)
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Integer getServiceInstId() {
		return serviceInstId;
	}

	public void setServiceInstId(Integer serviceInstId) {
		this.serviceInstId = serviceInstId;
	}

	public String getShardKey() {
		return shardKey;
	}

	public void setShardKey(String shardKey) {
		this.shardKey = shardKey;
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

	public List<ProfileDTO> getProfiles() {

		if (null == profiles) {
			profiles = new ArrayList<>();
		}

		return profiles;
	}

	public void setProfiles(List<ProfileDTO> profiles) {
		this.profiles = profiles;
	}

	/**
	 * Copies all data from DTO to entity except PGUID.
	 * 
	 * @param mailBox
	 *            The MailBox Entity
	 */
	public void copyToEntity(MailBox mailBox) {

		mailBox.setMbxName(this.getName());
		mailBox.setMbxDesc(this.getDescription());
		mailBox.setShardKey(this.getShardKey());
		if (null != this.getServiceInstId()) {
			mailBox.setServiceInstId(new BigDecimal(this.getServiceInstId()));
		}

		MailBoxProperty property = null;
		List<MailBoxProperty> properties = new ArrayList<>();
		for (MailBoxPropertyDTO propertyDTO : this.getProperties()) {
			property = new MailBoxProperty();
			// property.setMailbox(mailBox); -- GANESH COMMENTED THIS OUT TO REMOVE OWNER
			// INCONSISTENT ERROR.STRANGE THOUGH.
			propertyDTO.copyToEntity(property);
			properties.add(property);

		}
		mailBox.setMailboxProperties(properties);

		MailBoxStatus status = MailBoxStatus.findByName(this.getStatus());
		mailBox.setMbxStatus(status.value());
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

		MailBoxPropertyDTO propertyDTO = null;
		for (MailBoxProperty property : mailBox.getMailboxProperties()) {

			propertyDTO = new MailBoxPropertyDTO();
			propertyDTO.copyFromEntity(property);
			this.getProperties().add(propertyDTO);
		}

		ProfileDTO profile = null;
		MailBoxProcessorResponseDTO processorDTO = null;
		for (MailBoxSchedProfile schedProfile : mailBox.getMailboxSchedProfiles()) {

			profile = new ProfileDTO();
			profile.copyFromEntity(schedProfile.getScheduleProfilesRef());

			for (Processor processor : schedProfile.getProcessors()) {

				processorDTO = new MailBoxProcessorResponseDTO();
				processorDTO.copyFromEntity(processor);
				profile.getProcessors().add(processorDTO);
			}
			this.getProfiles().add(profile);
		}

	}
}
