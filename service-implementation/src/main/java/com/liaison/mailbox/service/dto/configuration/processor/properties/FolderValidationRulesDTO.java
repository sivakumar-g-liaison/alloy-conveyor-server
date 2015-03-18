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
 * @author OFS
 *
 */
public class FolderValidationRulesDTO {

	private String folderURIPattern;
	private String folderDescPattern;
	private String minLength;
	private String maxLength;

	public String getFolderURIPattern() {
		return folderURIPattern;
	}
	public void setFolderURIPattern(String folderURIPattern) {
		this.folderURIPattern = folderURIPattern;
	}
	public String getFolderDescPattern() {
		return folderDescPattern;
	}
	public void setFolderDescPattern(String folderDescPattern) {
		this.folderDescPattern = folderDescPattern;
	}
	public String getMinLength() {
		return minLength;
	}
	public void setMinLength(String minLength) {
		this.minLength = minLength;
	}
	public String getMaxLength() {
		return maxLength;
	}
	public void setMaxLength(String maxLength) {
		this.maxLength = maxLength;
	}



}
