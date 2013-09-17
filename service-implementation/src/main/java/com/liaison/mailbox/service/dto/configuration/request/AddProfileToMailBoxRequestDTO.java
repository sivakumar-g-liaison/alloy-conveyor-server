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

import com.liaison.mailbox.jpa.model.MailBoxSchedProfile;
import com.liaison.mailbox.service.util.MailBoxUtility;

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

	public void copyToEntity(MailBoxSchedProfile mailboxProfile) {

		mailboxProfile.setPguid(MailBoxUtility.getGUID());
		mailboxProfile.setMbxProfileStatus(this.getStatus());
	}

	public void copyFromEntity(MailBoxSchedProfile mailboxProfile) {

		this.setMailBoxGuid(mailboxProfile.getPguid());
		this.setStatus(mailboxProfile.getMbxProfileStatus());
	}
}
