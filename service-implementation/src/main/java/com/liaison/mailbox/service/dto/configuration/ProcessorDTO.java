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

import com.liaison.mailbox.enums.MailBoxStatus;
import com.liaison.mailbox.jpa.model.Credential;
import com.liaison.mailbox.jpa.model.Folder;
import com.liaison.mailbox.jpa.model.MailBoxSchedProfile;
import com.liaison.mailbox.jpa.model.Processor;
import com.liaison.mailbox.jpa.model.ProcessorProperty;
import com.liaison.mailbox.service.dto.configuration.request.HttpRemoteDownloaderPropertiesDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.util.MailBoxUtility;

/**
 * 
 * 
 * @author sivakumarg
 */
public class ProcessorDTO {

	private String guid;
	private String name;
	private String type;
	private HttpRemoteDownloaderPropertiesDTO remoteDownloaderProperties;
	private String javaScriptURI;
	private String description;
	private String status;
	private String linkedMailboxId;
	private String linkedProfileId;
	private List<FolderDTO> folders;
	private List<CredentialDTO> credentials;
	private List<ProcessorPropertyDTO> dynamicProperties;
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

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public HttpRemoteDownloaderPropertiesDTO getRemoteDownloaderProperties() {
		return remoteDownloaderProperties;
	}

	public void setRemoteDownloaderProperties(HttpRemoteDownloaderPropertiesDTO remoteDownloaderProperties) {
		this.remoteDownloaderProperties = remoteDownloaderProperties;
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

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getLinkedMailboxId() {
		return linkedMailboxId;
	}

	public void setLinkedMailboxId(String linkedMailboxId) {
		this.linkedMailboxId = linkedMailboxId;
	}

	public String getLinkedProfileId() {
		return linkedProfileId;
	}

	public void setLinkedProfileId(String linkedProfileId) {
		this.linkedProfileId = linkedProfileId;
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

	public List<ProcessorPropertyDTO> getDynamicProperties() {

		if (dynamicProperties == null) {
			dynamicProperties = new ArrayList<ProcessorPropertyDTO>();
		}
		return dynamicProperties;
	}

	public void setDynamicProperties(List<ProcessorPropertyDTO> dynamicProperties) {
		this.dynamicProperties = dynamicProperties;
	}

	public int getExecutionOrder() {
		return executionOrder;
	}

	public void setExecutionOrder(int executionOrder) {
		this.executionOrder = executionOrder;
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
	 */
	public void copyToEntity(Processor processor, boolean isCreate) throws MailBoxConfigurationServicesException,
			JsonGenerationException, JsonMappingException, JAXBException, IOException {

		if (isCreate) {
			processor.setPguid(MailBoxUtility.getGUID());
		}

		HttpRemoteDownloaderPropertiesDTO propertiesDTO = this.getRemoteDownloaderProperties();
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
		for (ProcessorPropertyDTO propertyDTO : this.getDynamicProperties()) {

			property = new ProcessorProperty();
			propertyDTO.copyToEntity(property);
			properties.add(property);
		}
		if (!properties.isEmpty()) {
			processor.setDynamicProperties(properties);
		}
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
	 */
	public void copyFromEntity(Processor processor) throws JsonParseException, JsonMappingException, JAXBException, IOException,
			MailBoxConfigurationServicesException {

		this.setGuid(processor.getPguid());
		this.setDescription(processor.getProcsrDesc());

		String propertyJSON = processor.getProcsrProperties();
		if (!MailBoxUtility.isEmpty(propertyJSON)) {

			HttpRemoteDownloaderPropertiesDTO propertiesDTO = MailBoxUtility.unmarshalFromJSON(propertyJSON,
					HttpRemoteDownloaderPropertiesDTO.class);
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

		if (processor.getMailboxSchedProfile() != null) {

			MailBoxSchedProfile mbxProfile = processor.getMailboxSchedProfile();
			this.setLinkedMailboxId(mbxProfile.getMailbox().getPguid());
			this.setLinkedProfileId(mbxProfile.getPguid());
		}

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

			ProcessorPropertyDTO propertyDTO = null;
			for (ProcessorProperty property : processor.getDynamicProperties()) {
				propertyDTO = new ProcessorPropertyDTO();
				propertyDTO.copyFromEntity(property);
				this.getDynamicProperties().add(propertyDTO);
			}
		}
	}
}
