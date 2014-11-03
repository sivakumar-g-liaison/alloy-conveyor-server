/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.dto.configuration.request;
import com.liaison.usermanagement.validation.Mandatory;

/**
 * 
 * @author OFS
 *
 */
public class ScriptServiceDTO {
	
	private String data;
	private String scriptFileUri;
	private String createdBy;
	
	@Mandatory(errorMessage = "Script Uri is mandatory.")
	public String getScriptFileUri() {
		return scriptFileUri;
	}
	public void setScriptFileUri(String scriptFileUri) {
		this.scriptFileUri = scriptFileUri;
	}
	
	public String getData() {
		return data;
	}
	public void setData(String data) {
		this.data = data;
	}	
	
	public String getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

}
