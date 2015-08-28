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

import java.io.Serializable;

import com.liaison.mailbox.dtdm.model.ScheduleProfilesRef;
import com.liaison.mailbox.service.validation.Mandatory;
import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

/**
 * Data Transfer Object for profile details.
 * 
 * @author OFS
 */
@ApiModel(value = "profile")
public class ProfileDTO implements Serializable {

	private static final long serialVersionUID = 1L;
	@ApiModelProperty( value = "Profile name", required = true)
	private String name;
	private String id;

	@Mandatory(errorMessage = "Profile name is mandatory.")
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
    
	/**
	 *  Copies the name from dto to profile.
	 *  
	 * @param profile
	 */
	public void copyToEntity(ScheduleProfilesRef profile) {
		profile.setSchProfName(this.getName());
	}
    /**
     * Copies the name and pguid from profile to dto.
     * 
     * @param profile
     */
	public void copyFromEntity(ScheduleProfilesRef profile) {
		this.setId(profile.getPguid());
		this.setName(profile.getSchProfName());
	}
}
