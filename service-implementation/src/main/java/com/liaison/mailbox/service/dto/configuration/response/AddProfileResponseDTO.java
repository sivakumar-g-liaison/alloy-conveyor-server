/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.dto.configuration.response;

import org.codehaus.jackson.map.annotate.JsonRootName;

import com.liaison.mailbox.service.dto.CommonResponseDTO;

/**
 * Data Transfer Object used for sending profile creation Responses.
 *
 * @author OFS
 */
@JsonRootName("addProfileResponse")
public class AddProfileResponseDTO extends CommonResponseDTO {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private ProfileResponseDTO profile;

	public ProfileResponseDTO getProfile() {
		return profile;
	}

	public void setProfile(ProfileResponseDTO profile) {
		this.profile = profile;
	}
}
