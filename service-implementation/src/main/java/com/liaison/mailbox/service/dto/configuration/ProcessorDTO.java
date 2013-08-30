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
import com.liaison.mailbox.jpa.model.Processor;
import com.liaison.mailbox.jpa.model.RemoteDownloader;
import com.liaison.mailbox.jpa.model.RemoteUploader;

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
		return folders;
	}
	public void setFolders(List<FolderDTO> folders) {
		this.folders = folders;
	}
	public List<CredentialDTO> getCredentials() {
		return credentials;
	}
	public void setCredentials(List<CredentialDTO> credentials) {
		this.credentials = credentials;
	}
	
	public void copyToEntity(Processor processor) {

		processor.setPguid(this.getGuid());
		processor.setProcsrDesc(this.getDescription());
		processor.setProcsrProperties(this.getProperties());
		processor.setProcsrStatus(this.getStatus());
		//processor.setProcsrType(this.getType());
		processor.setJavaScriptUri(this.getJavaScriptURI());
		
		//Set the RemoteDownloader properties
		if (processor instanceof RemoteDownloader) {
		}
		
		//Set the RemoteUploader properties
		if (processor instanceof RemoteUploader) {
		}		
		
		//TODO missing in JSON
		//processor.setMailboxSchedProfile();
		
		Folder folder = null;
		List<Folder> folders = new ArrayList<>();
		for (FolderDTO folderDTO : this.getFolders()) {

			folder = new Folder();
			folderDTO.copyToEntity(folder);
			folders.add(folder);
		}
		
		//TODO JSON have list of folder. But model have single folder
		if (!folders.isEmpty()) {
			processor.setFolders(folders);
		}
		
		Credential credential = null;
		List<Credential> credentialList = new ArrayList<>();
		for (CredentialDTO credentialDTO : this.getCredentials()) {

			credential = new Credential();
			credentialDTO.copyToEntity(credential);
			credentialList.add(credential);
		}
		
		//TODO JSON have list of credential. But model have single credential
		if (!credentialList.isEmpty()) {
			processor.setCredentials(credentialList);
		}
	}

	public void copyFromEntity(Processor processor) {

		this.setGuid(processor.getPguid());
		this.setDescription(processor.getProcsrDesc());
		this.setProperties(processor.getProcsrProperties());
		this.setStatus(processor.getProcsrStatus());
		//this.setType(processor.getProcsrType());
		this.setJavaScriptURI(processor.getJavaScriptUri());
		
		//TODO missing in JSON
		//processor.getMailboxSchedProfile();
		
		//Set the RemoteDownloader properties
		if (processor instanceof RemoteDownloader) {
		}
		
		//Set the RemoteUploader properties
		if (processor instanceof RemoteUploader) {
		}	

		FolderDTO folderDTO = new FolderDTO();
		Folder folder = processor.getFolders().get(0);
		folderDTO.copyFromEntity(folder);
		
		this.setFolders(new ArrayList<FolderDTO>());
		this.getFolders().add(folderDTO);
		
		CredentialDTO credentialDTO = new CredentialDTO();
		Credential credential = processor.getCredentials().get(0);
		credentialDTO.copyFromEntity(credential);
		this.setCredentials(new ArrayList<CredentialDTO>());
		this.getCredentials().add(credentialDTO);
	}
}
