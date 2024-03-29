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

import javax.ws.rs.core.Response;

import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.dtdm.model.Folder;
import com.liaison.mailbox.enums.FolderType;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.util.MailBoxUtil;
import com.liaison.mailbox.service.validation.DataValidation;
import com.liaison.mailbox.service.validation.Mandatory;
import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

/**
 * Data Transfer Object for folder details.
 *
 * @author OFS
 */

@ApiModel(value = "folder")
public class FolderDTO {

	private String guId;
	private String folderURI;
	@ApiModelProperty( value = "Folder Type", required = true)
	private String folderType;
	private String folderDesc;

	public String getGuId() {
		return guId;
	}

	public void setGuId(String guId) {
		this.guId = guId;
	}

	@Mandatory(errorMessage = "Folder URI is mandatory.")
	public String getFolderURI() {
		return folderURI;
	}

	public void setFolderURI(String folderURI) {
		this.folderURI = folderURI;
	}

	@Mandatory(errorMessage = "Folder Type is mandatory.")
	@DataValidation(errorMessage = "Folder type set to a value that is not supported.", type = MailBoxConstants.FOLDER_TYPE)
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

	/**
	 * Copies all data from dto to entity.
	 *
	 * @param entity
	 *        the Folder Entity
	 * @throws MailBoxConfigurationServicesException
	 */
	public void copyToEntity(Object entity) throws MailBoxConfigurationServicesException {

		Folder folder = (Folder) entity;
		folder.setFldrDesc(this.getFolderDesc());

		FolderType foundFolderType = FolderType.findByName(this.getFolderType());
		if (foundFolderType == null) {
			throw new MailBoxConfigurationServicesException(Messages.ENUM_TYPE_DOES_NOT_SUPPORT, "Folder", Response.Status.BAD_REQUEST);
		}

		folder.setFldrType(foundFolderType.getCode());
		folder.setFldrUri(this.getFolderURI());
		folder.setPguid(MailBoxUtil.getGUID());
		folder.setOriginatingDc(MailBoxUtil.DATACENTER_NAME);
	}

	/**
	 * Copies all data from Entity to DTO.
	 *
	 * @param entity
	 *          The Folder Entity
	 * @throws MailBoxConfigurationServicesException
	 */
	public void copyFromEntity(Object entity) throws MailBoxConfigurationServicesException {

		Folder folder = (Folder) entity;
		this.setFolderDesc(folder.getFldrDesc());

		FolderType foundFolderType = FolderType.findByCode(folder.getFldrType());
		if (foundFolderType == null) {
			throw new MailBoxConfigurationServicesException(Messages.ENUM_TYPE_DOES_NOT_SUPPORT, "Folder", Response.Status.BAD_REQUEST);
		}

		this.setFolderType(foundFolderType.name());
		this.setFolderURI(folder.getFldrUri());
		this.setGuId(folder.getPguid());
	}

}
