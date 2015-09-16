package com.liaison.mailbox.service.dto.ui;

import org.codehaus.jackson.map.annotate.JsonRootName;

import com.liaison.mailbox.service.dto.CommonResponseDTO;
import com.liaison.mailbox.service.dto.configuration.ProfileDTO;

/**
 * Data Transfer Object used for retrieving the profile by name.
 * 
 * @author OFS
 */
@JsonRootName("getProfileByNameResponse")
public class GetProfileByNameResponseDTO extends CommonResponseDTO {
	
	private static final long serialVersionUID = 1L;
	
	private ProfileDTO profile;

	public ProfileDTO getProfile() {
		return profile;
	}

	public void setProfile(ProfileDTO profile) {
		this.profile = profile;
	}

}
