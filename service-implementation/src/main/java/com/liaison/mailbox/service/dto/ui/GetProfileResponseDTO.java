/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.dto.ui;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.map.annotate.JsonRootName;

import com.liaison.mailbox.service.dto.CommonResponseDTO;
import com.liaison.mailbox.service.dto.configuration.ProfileDTO;

/**
 * Data Transfer Object used for retrieving the profiles.
 *
 * @author OFS
 */
@JsonRootName("getProfileResponse")
public class GetProfileResponseDTO extends CommonResponseDTO {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private List<ProfileDTO> profiles;
	private ProfileDTO profile;

	private long totalItems = 0L;

	public long getTotalItems() {
		return totalItems;
	}

	public void setTotalItems(long totalItems) {
		this.totalItems = totalItems;
	}

	public List<ProfileDTO> getProfiles() {

		if (null == profiles) {
			profiles = new ArrayList<ProfileDTO>();
		}
		return profiles;
	}

	public void setProfiles(List<ProfileDTO> profiles) {
		this.profiles = profiles;
	}

	public ProfileDTO getProfile() {
		return profile;
	}

	public void setProfile(ProfileDTO profile) {
		this.profile = profile;
	}

}
