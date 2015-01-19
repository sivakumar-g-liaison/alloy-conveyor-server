package com.liaison.mailbox.service.dto.configuration.response;

import java.util.List;

import org.codehaus.jackson.map.annotate.JsonRootName;

import com.liaison.mailbox.service.dto.CommonResponseDTO;
import com.liaison.mailbox.service.dto.configuration.ProfileDTO;

@JsonRootName("getTransferProfilesResponse")
public class GetTransferProfilesResponseDTO extends CommonResponseDTO {

	private static final long serialVersionUID = 1L;
	private List <ProfileDTO> transferProfiles;
	
	public List<ProfileDTO> getTransferProfiles() {
		return transferProfiles;
	}
	public void setTransferProfiles(List<ProfileDTO> transferProfiles) {
		this.transferProfiles = transferProfiles;
	}

}
