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
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

import com.liaison.commons.security.pkcs7.SymmetricAlgorithmException;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.enums.MailBoxStatus;
import com.liaison.mailbox.enums.Protocol;
import com.liaison.mailbox.jpa.model.Credential;
import com.liaison.mailbox.jpa.model.Folder;
import com.liaison.mailbox.jpa.model.Processor;
import com.liaison.mailbox.jpa.model.ProcessorProperty;
import com.liaison.mailbox.service.dto.configuration.request.RemoteProcessorPropertiesDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.util.MailBoxUtility;
import com.liaison.mailbox.service.validation.DataValidation;
import com.liaison.mailbox.service.validation.Mandatory;

/**
 * 
 * 
 * @author sivakumarg
 */
public class ProcessorDTO {

	private String guid;
	private String name;
	private String type;
	private RemoteProcessorPropertiesDTO remoteProcessorProperties;
	private String javaScriptURI;
	private String description;
	private String status;
	private String protocol;
	private String linkedMailboxId;
	private List<String> linkedProfilesId;
	private List<FolderDTO> folders;
	private List<CredentialDTO> credentials;
	private List<PropertyDTO> dynamicProperties;
	private int executionOrder;

	public ProcessorDTO() {
		super();
	}

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

	@Mandatory(errorMessage = "Processor type is mandatory.")
	@DataValidation(errorMessage = "Processor type set to a value that is not supported.", type = MailBoxConstants.PROCESSOR_TYPE)
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public RemoteProcessorPropertiesDTO getRemoteDownloaderProperties() {
		return remoteProcessorProperties;
	}

	public void setRemoteDownloaderProperties(RemoteProcessorPropertiesDTO remoteDownloaderProperties) {
		this.remoteProcessorProperties = remoteDownloaderProperties;
	}

	public String getJavaScriptURI() {
		return javaScriptURI;
	}

	public void setJavaScriptURI(String javaScriptURI) {
		this.javaScriptURI = javaScriptURI;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Mandatory(errorMessage = "Processor status is mandatory.")
	@DataValidation(errorMessage = "Processor status set to a value that is not supported.", type = MailBoxConstants.MBX_STATUS)
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@Mandatory(errorMessage = "Processor protocol is mandatory.")
	@DataValidation(errorMessage = "Processor protocol set to a value that is not supported.", type = MailBoxConstants.PROCESSOR_PROTOCOL)
	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getLinkedMailboxId() {
		return linkedMailboxId;
	}

	public void setLinkedMailboxId(String linkedMailboxId) {
		this.linkedMailboxId = linkedMailboxId;
	}

	public List<FolderDTO> getFolders() {
		if (folders == null) {
			folders = new ArrayList<FolderDTO>();
		}
		return folders;
	}

	public void setFolders(List<FolderDTO> folders) {
		this.folders = folders;
	}

	public List<CredentialDTO> getCredentials() {
		if (credentials == null) {
			credentials = new ArrayList<CredentialDTO>();
		}
		return credentials;
	}

	public void setCredentials(List<CredentialDTO> credentials) {
		this.credentials = credentials;
	}

	public List<PropertyDTO> getDynamicProperties() {

		if (dynamicProperties == null) {
			dynamicProperties = new ArrayList<PropertyDTO>();
		}
		return dynamicProperties;
	}

	public void setDynamicProperties(List<PropertyDTO> dynamicProperties) {
		this.dynamicProperties = dynamicProperties;
	}

	public int getExecutionOrder() {
		return executionOrder;
	}

	public void setExecutionOrder(int executionOrder) {
		this.executionOrder = executionOrder;
	}

	public List<String> getLinkedProfilesId() {
		return linkedProfilesId;
	}

	public void setLinkedProfilesId(List<String> linkedProfilesId) {
		this.linkedProfilesId = linkedProfilesId;
	}

	/**
	 * Method is used to copy the values from DTO to Entity. It does not create relationship between
	 * MailBoxSchedProfile and Processor. That step will be done in the service.
	 * 
	 * @param processor
	 *            The processor entity
	 * @param isCreate
	 *            The boolean value use to differentiate create and revise processor operation.
	 * @throws MailBoxConfigurationServicesException
	 * @throws IOException
	 * @throws JAXBException
	 * @throws JsonMappingException
	 * @throws JsonGenerationException
	 * @throws SymmetricAlgorithmException
	 */
	public void copyToEntity(Processor processor, boolean isCreate) throws MailBoxConfigurationServicesException,
			JsonGenerationException, JsonMappingException, JAXBException, IOException, SymmetricAlgorithmException {

		if (isCreate) {
			processor.setPguid(MailBoxUtility.getGUID());
		}

		RemoteProcessorPropertiesDTO propertiesDTO = this.getRemoteDownloaderProperties();
		if (null != propertiesDTO) {
			String propertiesJSON = MailBoxUtility.marshalToJSON(this.getRemoteDownloaderProperties());
			processor.setProcsrProperties(propertiesJSON);
		}

		processor.setProcsrDesc(this.getDescription());
		processor.setProcsrName(this.getName());
		processor.setJavaScriptUri(this.getJavaScriptURI());

		// Setting the folders.
		Folder folder = null;
		List<Folder> folders = new ArrayList<>();
		for (FolderDTO folderDTO : this.getFolders()) {

			folder = new Folder();
			folderDTO.copyToEntity(folder);

			folder.setPguid(MailBoxUtility.getGUID());
			folders.add(folder);
		}

		if (!folders.isEmpty()) {
			processor.setFolders(folders);
		}

		// Setting the credentials
		Credential credential = null;
		List<Credential> credentialList = new ArrayList<>();
		for (CredentialDTO credentialDTO : this.getCredentials()) {

			credential = new Credential();
			credentialDTO.copyToEntity(credential);

			credential.setPguid(MailBoxUtility.getGUID());
			credentialList.add(credential);
		}

		if (!credentialList.isEmpty()) {
			processor.setCredentials(credentialList);
		}

		// Setting the property
		ProcessorProperty property = null;
		List<ProcessorProperty> properties = new ArrayList<>();
		for (PropertyDTO propertyDTO : this.getDynamicProperties()) {

			property = new ProcessorProperty();
			propertyDTO.copyToEntity(property, false);
			properties.add(property);
		}
		if (!properties.isEmpty()) {
			processor.setDynamicProperties(properties);
		}

		// Set the protocol
		Protocol protocol = Protocol.findByName(this.getProtocol());
		processor.setProcsrProtocol(protocol.getCode());

		// Set the status
		MailBoxStatus foundStatusType = MailBoxStatus.findByName(this.getStatus());
		processor.setProcsrStatus(foundStatusType.value());

	}

	/**
	 * Copies the values from Entity to DTO.
	 * 
	 * @param processor
	 * @throws IOException
	 * @throws JAXBException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 * @throws MailBoxConfigurationServicesException
	 * @throws SymmetricAlgorithmException
	 */
	public void copyFromEntity(Processor processor) throws JsonParseException, JsonMappingException, JAXBException, IOException,
			MailBoxConfigurationServicesException, SymmetricAlgorithmException {

		this.setGuid(processor.getPguid());
		this.setDescription(processor.getProcsrDesc());

		String propertyJSON = processor.getProcsrProperties();
		if (!MailBoxUtility.isEmpty(propertyJSON)) {

			RemoteProcessorPropertiesDTO propertiesDTO = MailBoxUtility.unmarshalFromJSON(propertyJSON,
					RemoteProcessorPropertiesDTO.class);
			this.setRemoteDownloaderProperties(propertiesDTO);
		}

		String status = processor.getProcsrStatus();
		if (!MailBoxUtility.isEmpty(status)) {
			MailBoxStatus foundStatus = MailBoxStatus.findByCode(status);
			this.setStatus(foundStatus.name());
		}

		this.setExecutionOrder(processor.getExecutionOrder());
		this.setType(processor.getProcessorType().name());
		this.setJavaScriptURI(processor.getJavaScriptUri());
		this.setName(processor.getProcsrName());

		// Set folders
		if (null != processor.getFolders()) {

			FolderDTO folderDTO = null;
			for (Folder folder : processor.getFolders()) {
				folderDTO = new FolderDTO();
				folderDTO.copyFromEntity(folder);
				this.getFolders().add(folderDTO);
			}
		}

		// Set credentials
		if (null != processor.getCredentials()) {

			CredentialDTO credentialDTO = null;
			for (Credential credential : processor.getCredentials()) {
				credentialDTO = new CredentialDTO();
				credentialDTO.copyFromEntity(credential);
				this.getCredentials().add(credentialDTO);
			}
		}

		// Set properties
		if (null != processor.getDynamicProperties()) {

			PropertyDTO propertyDTO = null;
			for (ProcessorProperty property : processor.getDynamicProperties()) {
				propertyDTO = new PropertyDTO();
				propertyDTO.copyFromEntity(property, false);
				this.getDynamicProperties().add(propertyDTO);
			}
		}

		// Set protocol
		this.setProtocol(Protocol.findByCode(processor.getProcsrProtocol()).name());
	}
}
