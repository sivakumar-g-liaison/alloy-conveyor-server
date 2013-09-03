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

import com.liaison.mailbox.jpa.model.ScheduleProfilesRef;

/**
 * 
 * 
 * @author praveenu
 */
public class ProfileDTO {

	private String name;
	private String id;

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

	public void copyToEntity(ScheduleProfilesRef profile) {
		profile.setSchProfName(this.getName());
	}

	public void copyFromEntity(ScheduleProfilesRef profile) {
		this.setId(profile.getPguid());
		this.setName(profile.getSchProfName());
	}
}
