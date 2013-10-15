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

import org.codehaus.jackson.map.annotate.JsonRootName;

/**
 * 
 * 
 * @author praveenu
 */
@JsonRootName("addProfileToMailBoxRequest")
public class AddProfileToMailBoxRequestDTO {

	private String profileGuid;
	private String mailBoxGuid;
	private String status;

	public String getMailBoxGuid() {
		return mailBoxGuid;
	}

	public void setMailBoxGuid(String mailBoxGuid) {
		this.mailBoxGuid = mailBoxGuid;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getProfileGuid() {
		return profileGuid;
	}

	public void setProfileGuid(String profileGuid) {
		this.profileGuid = profileGuid;
	}

}
