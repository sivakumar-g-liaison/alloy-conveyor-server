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

import com.liaison.mailbox.jpa.model.MailBoxSchedProfile;
import com.liaison.mailbox.jpa.model.ScheduleProfilesRef;
import com.liaison.mailbox.service.dto.configuration.ProfileDTO;

/**
 * 
 *
 * @author praveenu
 */
public class AddProfileToMailboxRequestDTO {

	private ProfileDTO profile;
	private String mailboxGuid;
	private String status;

	public String getMailboxGuid() {
		return mailboxGuid;
	}
	
	public void setMailboxGuid(String mailboxGuid) {
		this.mailboxGuid = mailboxGuid;
	}
	
	public ProfileDTO getProfile() {
		return profile;
	}
	
	public void setProfile(ProfileDTO profile) {
		this.profile = profile;
	}
	
	public String getStatus() {
		return status;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}
	
	public void copyToEntity(MailBoxSchedProfile mailboxProfile) {
		
		//mailboxProfile.setPguid(this.getMailboxGuid());
		mailboxProfile.setMbxProfileStatus(this.getStatus());
		
		if (this.getProfile() != null) {
			
			ScheduleProfilesRef profile = new ScheduleProfilesRef();
			this.getProfile().copyToEntity(profile);
			mailboxProfile.setScheduleProfilesRef(profile);
		}
	}
	
	public void copyFromEntity(MailBoxSchedProfile mailboxProfile) {
		
		this.setMailboxGuid(mailboxProfile.getPguid());
		this.setStatus(mailboxProfile.getMbxProfileStatus());
		
		if (this.getProfile() != null && mailboxProfile.getScheduleProfilesRef() != null) {
			this.getProfile().copyFromEntity(mailboxProfile.getScheduleProfilesRef());
		} else if (this.getProfile() == null && mailboxProfile.getScheduleProfilesRef() != null) {
			ProfileDTO profileDTO = new ProfileDTO();
			profileDTO.copyFromEntity(mailboxProfile.getScheduleProfilesRef());
			this.setProfile(profileDTO);
		}
	}
}
