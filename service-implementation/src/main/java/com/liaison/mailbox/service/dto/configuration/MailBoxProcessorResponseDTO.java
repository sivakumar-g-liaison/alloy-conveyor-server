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

import com.liaison.mailbox.enums.MailBoxStatus;
import com.liaison.mailbox.jpa.model.Processor;
import com.liaison.mailbox.jpa.model.ScheduleProfileProcessor;

/**
 * Data Transfer Object for processor details in MailBox.
 * 
 * @author veerasamyn
 */
public class MailBoxProcessorResponseDTO {

	private String guid;
	private String name;
	private String type;
	private String protocol;
	private String status;
	private List<ProfileDTO> profiles;

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

	public List<ProfileDTO> getProfiles() {

		if (null == profiles) {
			profiles = new ArrayList<>();
		}

		return profiles;
	}

	public void setProfiles(List<ProfileDTO> profiles) {
		this.profiles = profiles;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * Copies all the data from processor to DTO.
	 * 
	 * @param processor
	 */
	public void copyFromEntity(Processor processor) {

		this.setGuid(processor.getPguid());
		this.setType(processor.getProcessorType().name());
		this.setName(processor.getProcsrName());
		this.setProtocol(processor.getProcsrProtocol());

		MailBoxStatus status = MailBoxStatus.findByCode(processor.getProcsrStatus());
		this.setStatus(status.name());

		if (null != processor.getScheduleProfileProcessors()) {

			ProfileDTO profile = null;
			for (ScheduleProfileProcessor scheduleProfileProcessor : processor.getScheduleProfileProcessors()) {

				profile = new ProfileDTO();
				profile.copyFromEntity(scheduleProfileProcessor.getScheduleProfilesRef());
				this.getProfiles().add(profile);
			}

		}
	}
}
