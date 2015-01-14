package com.liaison.mailbox.service.dto.configuration.response;

import java.util.List;

import org.codehaus.jackson.map.annotate.JsonRootName;

import com.liaison.mailbox.service.dto.CommonResponseDTO;
import com.liaison.mailbox.service.dto.configuration.ProfileDTO;

@JsonRootName("getTransferProfilesResponse")
public class GetTransferProfilesResponseDTO extends CommonResponseDTO {

	private static final long serialVersionUID = 1L;
	private List <ProfileDTO> tranferProfiles;
	
	public List<ProfileDTO> getTranferProfiles() {
		return tranferProfiles;
	}
	public void setTranferProfiles(List<ProfileDTO> tranferProfiles) {
		this.tranferProfiles = tranferProfiles;
	}

}
