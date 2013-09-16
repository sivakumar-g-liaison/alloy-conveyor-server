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

import com.liaison.mailbox.jpa.model.ScheduleProfilesRef;
import com.liaison.mailbox.service.util.MailBoxUtility;

/**
 * 
 * 
 * @author praveenu
 */
public class ProfileDTO {

	private String name;
	private String id;

	private List<MailBoxProcessorResponseDTO> processors;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<MailBoxProcessorResponseDTO> getProcessors() {
		if (null == processors) {
			this.processors = new ArrayList<>();
		}
		return processors;
	}

	public void setProcessors(List<MailBoxProcessorResponseDTO> processors) {
		this.processors = processors;
	}

	public void copyToEntity(ScheduleProfilesRef profile) {
		profile.setPguid(MailBoxUtility.getGUID());
		profile.setSchProfName(this.getName());
	}

	public void copyFromEntity(ScheduleProfilesRef profile) {
		this.setId(profile.getPguid());
		this.setName(profile.getSchProfName());
	}
}
