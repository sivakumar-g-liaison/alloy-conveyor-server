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

import java.util.ArrayList;
import java.util.List;

import com.liaison.mailbox.jpa.model.Credential;
import com.liaison.mailbox.jpa.model.Folder;
import com.liaison.mailbox.jpa.model.MailBoxSchedProfile;
import com.liaison.mailbox.jpa.model.Processor;
import com.liaison.mailbox.jpa.model.ProcessorProperty;
import com.liaison.mailbox.jpa.model.RemoteDownloader;
import com.liaison.mailbox.jpa.model.RemoteUploader;
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
	private String properties;
	private String javaScriptURI;
	private String description;
	private String status;
	private String linkedMailboxId;
	private String linkedProfileId;
	private List<FolderDTO> folders;
	private List<CredentialDTO> credentials;
	private List<ProcessorPropertyDTO> procsrProperties;
	private int executionOrder;

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

	public String getProperties() {
		
		return properties;
	}

	public void setProperties(String properties) {
		this.properties = properties;
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
		if(folders == null){
			return new ArrayList<FolderDTO>();
		}
		return folders;
	}

	public void setFolders(List<FolderDTO> folders) {
		this.folders = folders;
	}

	public List<CredentialDTO> getCredentials() {
		if(credentials == null){
			return new ArrayList<CredentialDTO>();
		}
		return credentials;
	}

	public void setCredentials(List<CredentialDTO> credentials) {
		this.credentials = credentials;
	}

	public List<ProcessorPropertyDTO> getProcsrProperties() {
		if(procsrProperties == null){
			return new ArrayList<ProcessorPropertyDTO>();
		}
		return procsrProperties;
	}

	public void setProcsrProperties(List<ProcessorPropertyDTO> procsrProperties) {
		this.procsrProperties = procsrProperties;
	}

	public int getExecutionOrder() {
		return executionOrder;
	}

	public void setExecutionOrder(int executionOrder) {
		this.executionOrder = executionOrder;
	}

	public void copyToEntity(Processor processor, boolean isCreate) {

		if (isCreate) {
			processor.setPguid(MailBoxUtility.getGUID());
		}
		processor.setProcsrDesc(this.getDescription());		
		processor.setProcsrProperties(this.getProperties());
		
		processor.setProcsrStatus(this.getStatus());
		// processor.setProcsrType(this.getType());
		processor.setJavaScriptUri(this.getJavaScriptURI());

		// Set the RemoteDownloader properties
		if (processor instanceof RemoteDownloader) {
		}

		// Set the RemoteUploader properties
		if (processor instanceof RemoteUploader) {
		}

		// TODO missing in JSON
		// processor.setMailboxSchedProfile();

		Folder folder = null;
		List<Folder> folders = new ArrayList<>();
		for (FolderDTO folderDTO : this.getFolders()) {

			folder = new Folder();
			folderDTO.copyToEntity(folder);

			folder.setPguid(MailBoxUtility.getGUID());
			folders.add(folder);
		}

		// TODO JSON have list of folder. But model have single folder
		if (!folders.isEmpty()) {
			processor.setFolders(folders);
		}

		Credential credential = null;
		List<Credential> credentialList = new ArrayList<>();
		for (CredentialDTO credentialDTO : this.getCredentials()) {

			credential = new Credential();
			credentialDTO.copyToEntity(credential);

			credential.setPguid(MailBoxUtility.getGUID());
			credentialList.add(credential);
		}

		// TODO JSON have list of credential. But model have single credential
		if (!credentialList.isEmpty()) {
			processor.setCredentials(credentialList);
		}

		ProcessorProperty property = null;
		List<ProcessorProperty> properties = new ArrayList<>();

		for (ProcessorPropertyDTO propertyDTO : this.getProcsrProperties()) {

			property = new ProcessorProperty();
			propertyDTO.copyToEntity(property);
			properties.add(property);
		}

		processor.setProcessorProperties(properties);
	}

	public void copyFromEntity(Processor processor) {

		this.setGuid(processor.getPguid());
		this.setDescription(processor.getProcsrDesc());		
		this.setProperties(processor.getProcsrProperties());
		this.setStatus(processor.getProcsrStatus());
		this.setExecutionOrder(processor.getExecutionOrder());
		// this.setType(processor.getProcsrType());
		this.setJavaScriptURI(processor.getJavaScriptUri());

		if (processor.getMailboxSchedProfile() != null) {

			MailBoxSchedProfile mbxProfile = processor.getMailboxSchedProfile();
			this.setLinkedMailboxId(mbxProfile.getMailbox().getPguid());
			this.setLinkedProfileId(mbxProfile.getPguid());
		}

		// Set the RemoteDownloader properties
		if (processor instanceof RemoteDownloader) {
		}

		// Set the RemoteUploader properties
		if (processor instanceof RemoteUploader) {
		}

		// Set folders
		FolderDTO folderDTO = null;
		List<FolderDTO> folders = new ArrayList<>();
		if (null != processor.getFolders()) {

			for (Folder folder : processor.getFolders()) {
				folderDTO = new FolderDTO();
				folderDTO.copyFromEntity(folder);
				folders.add(folderDTO);
			}
		}
		this.setFolders(folders);

		// Set credentials
		CredentialDTO credentialDTO = null;
		List<CredentialDTO> credentials = new ArrayList<>();
		if (null != processor.getCredentials()) {

			for (Credential credential : processor.getCredentials()) {
				credentialDTO = new CredentialDTO();
				credentialDTO.copyFromEntity(credential);
				credentials.add(credentialDTO);
			}
		}
		this.setCredentials(credentials);

		// Set properties
		ProcessorPropertyDTO propertyDTO = null;
		List<ProcessorPropertyDTO> properties = new ArrayList<>();
		if (null != processor.getProcessorProperties()) {

			for (ProcessorProperty property : processor.getProcessorProperties()) {
				propertyDTO = new ProcessorPropertyDTO();
				propertyDTO.copyFromEntity(property);
				properties.add(propertyDTO);
			}
		}
		this.setProcsrProperties(properties);
	}
}
