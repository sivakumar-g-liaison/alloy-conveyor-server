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

import com.liaison.mailbox.jpa.model.Folder;

/**
 * 
 *
 * @author sivakumarg
 */
public class FolderDTO {
	
	private String guId;
	private String folderURI;
	private String folderType;
	private String folderDesc;

	public String getGuId() {
		return guId;
	}
	public void setGuId(String guId) {
		this.guId = guId;
	}
	public String getFolderURI() {
		return folderURI;
	}
	public void setFolderURI(String folderURI) {
		this.folderURI = folderURI;
	}
	public String getFolderType() {
		return folderType;
	}
	public void setFolderType(String folderType) {
		this.folderType = folderType;
	}
	public String getFolderDesc() {
		return folderDesc;
	}
	public void setFolderDesc(String folderDesc) {
		this.folderDesc = folderDesc;
	}
	
	public void copyToEntity(Object entity) {

		Folder folder = (Folder) entity;
		folder.setFldrDesc(this.getFolderDesc());
		folder.setFldrType(this.getFolderType());
		folder.setFldrUri(this.getFolderURI());
		folder.setPguid(this.getGuId());
	}

	public void copyFromEntity(Object entity) {

		Folder folder = (Folder) entity;
		this.setFolderDesc(folder.getFldrDesc());
		this.setFolderType(folder.getFldrType());
		this.setFolderURI(folder.getFldrUri());
		this.setGuId(folder.getPguid());
	}

}
