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

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

import com.liaison.commons.security.pkcs7.SymmetricAlgorithmException;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.dtdm.model.MailBox;
import com.liaison.mailbox.dtdm.model.MailBoxProperty;
import com.liaison.mailbox.dtdm.model.Processor;
import com.liaison.mailbox.enums.EntityStatus;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.validation.DataValidation;
import com.liaison.mailbox.service.validation.Mandatory;
import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

/**
 * 
 * 
 * @author veerasamyn
 */
@ApiModel(value = "mailbox")
public class MailBoxDTO implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private String guid;
	@ApiModelProperty(value = "Mailbox name", required = true)
	private String name;
	private String description;
	@ApiModelProperty(value = "Mailbox status", required = true)
	private String status;
	private String shardKey;
	private String tenancyKey;
	private String tenancyKeyDisplayName;	

	private List<PropertyDTO> properties;
	private List<MailBoxProcessorResponseDTO> processors;

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

	public String getShardKey() {
		return shardKey;
	}

	public void setShardKey(String shardKey) {
		this.shardKey = shardKey;
	}

	@Mandatory(errorMessage = "MailBox Tenancy Key is mandatory.")
	public String getTenancyKey() {
		return tenancyKey;
	}

	public void setTenancyKey(String tenancyKey) {
		this.tenancyKey = tenancyKey;
	}

	public List<PropertyDTO> getProperties() {

		if (null == properties) {
			properties = new ArrayList<>();
		}
		return properties;
	}

	public void setProperties(List<PropertyDTO> properties) {
		this.properties = properties;
	}

	public List<MailBoxProcessorResponseDTO> getProcessors() {

		if (null == processors) {
			processors = new ArrayList<>();
		}

		return processors;
	}

	public void setProcessors(List<MailBoxProcessorResponseDTO> processors) {
		this.processors = processors;
	}

	public String getTenancyKeyDisplayName() {
		return tenancyKeyDisplayName;
	}

	public void setTenancyKeyDisplayName(String tenancyKeyDisplayName) {
		this.tenancyKeyDisplayName = tenancyKeyDisplayName;
	}

	/**
	 * Copies all data from DTO to entity except PGUID.
	 * 
	 * @param mailBox
	 *            The MailBox Entity
	 * @throws IOException 
	 */
	public void copyToEntity(MailBox mailBox) throws IOException {

		mailBox.setMbxName(this.getName());
		mailBox.setMbxDesc(this.getDescription());
		mailBox.setShardKey(this.getShardKey());
		mailBox.setTenancyKey(this.getTenancyKey());

		MailBoxProperty property = null;
		List<MailBoxProperty> properties = new ArrayList<>();
		for (PropertyDTO propertyDTO : this.getProperties()) {
			property = new MailBoxProperty();
			property.setMailbox(mailBox); 
			propertyDTO.copyToEntity(property, true);
			properties.add(property);

		}
		mailBox.setMailboxProperties(properties);

		EntityStatus status = EntityStatus.findByName(this.getStatus());
		mailBox.setMbxStatus(status.value());
	}

	/**
	 * Copies all data from Entity to DTO.
	 * 
	 * @param mailBox
	 *            The MailBox Entity
	 * @throws SymmetricAlgorithmException
	 * @throws MailBoxConfigurationServicesException
	 * @throws IOException
	 * @throws JAXBException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 */
	public void copyFromEntity(MailBox mailBox) throws JsonParseException, JsonMappingException, JAXBException,
			IOException,
			MailBoxConfigurationServicesException, SymmetricAlgorithmException {

		this.setGuid(mailBox.getPguid());
		this.setName(mailBox.getMbxName());
		this.setDescription(mailBox.getMbxDesc());

		EntityStatus status = EntityStatus.findByCode(mailBox.getMbxStatus());
		this.setStatus(status.name());

		this.setShardKey(mailBox.getShardKey());
		this.setTenancyKey(mailBox.getTenancyKey());

		PropertyDTO propertyDTO = null;
		for (MailBoxProperty property : mailBox.getMailboxProperties()) {

			propertyDTO = new PropertyDTO();
			propertyDTO.copyFromEntity(property, true);
			this.getProperties().add(propertyDTO);
		}

		MailBoxProcessorResponseDTO prcsrDTO = null;
		for (Processor prcsr : mailBox.getMailboxProcessors()) {

			prcsrDTO = new MailBoxProcessorResponseDTO();
			prcsrDTO.copyFromEntity(prcsr);
			this.getProcessors().add(prcsrDTO);
		}

	}	
}
