/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.mailbox.service.dto.configuration.processor.properties;

/**
 * Data Transfer Object for folder properties of the processor.
 * 
 * @author OFS
 */
public class ProcessorFolderPropertyDTO {

	private String folderURI;
	private String folderDisplayType;
	private String folderType;
	private String folderDesc;
	private boolean readOnly;
	private boolean mandatory;
	private boolean valueProvided;
	private FolderValidationRulesDTO validationRules;

	public FolderValidationRulesDTO getValidationRules() {
		return validationRules;
	}
	public void setValidationRules(FolderValidationRulesDTO validationRules) {
		this.validationRules = validationRules;
	}
	public String getFolderURI() {
		return folderURI;
	}
	public void setFolderURI(String folderURI) {
		this.folderURI = folderURI;
	}
	public String getFolderDisplayType() {
		return folderDisplayType;
	}
	public void setFolderDisplayType(String folderDisplayType) {
		this.folderDisplayType = folderDisplayType;
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
	public boolean isReadOnly() {
		return readOnly;
	}
	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}
	public boolean isMandatory() {
		return mandatory;
	}
	public void setMandatory(boolean mandatory) {
		this.mandatory = mandatory;
	}
	public boolean isValueProvided() {
		return valueProvided;
	}
	public void setValueProvided(boolean valueProvided) {
		this.valueProvided = valueProvided;
	}


}
