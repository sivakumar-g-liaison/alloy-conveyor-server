package com.liaison.mailbox.service.dto.configuration.response;

import java.util.List;

import org.codehaus.jackson.map.annotate.JsonRootName;

import com.liaison.mailbox.service.dto.CommonResponseDTO;
import com.liaison.mailbox.service.dto.configuration.ProfileDTO;

@JsonRootName("getTransferProfilesResponse")
public class GetTransferProfilesResponseDTO extends CommonResponseDTO {

	private static final long serialVersionUID = 1L;
	private List <ProfileDTO> tranferProfiles;
	private String authenticationToken;
	private String aclManifest;
	
	public List<ProfileDTO> getTranferProfiles() {
		return tranferProfiles;
	}
	public void setTranferProfiles(List<ProfileDTO> tranferProfiles) {
		this.tranferProfiles = tranferProfiles;
	}
	public String getAuthenticationToken() {
		return authenticationToken;
	}
	public void setAuthenticationToken(String authenticationToken) {
		this.authenticationToken = authenticationToken;
	}
	public String getAclManifest() {
		return aclManifest;
	}
	public void setAclManifest(String aclManifest) {
		this.aclManifest = aclManifest;
	}
}
