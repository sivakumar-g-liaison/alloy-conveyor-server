/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.gem.service.client.GEMACLClient;
import com.liaison.gem.service.client.GEMManifestResponse;
import com.liaison.gem.service.dto.request.ManifestRequestDTO;
import com.liaison.mailbox.service.dto.configuration.request.AuthenticateUserRequestDTO;
import com.liaison.usermanagement.enums.Messages;
import com.liaison.usermanagement.service.client.UserManagementClient;
import com.liaison.usermanagement.service.dto.AuthenticationResponseDTO;
import com.liaison.usermanagement.service.dto.response.AuthenticateUserAccountResponseDTO;

/**
 * Class which has mailbox configuration related operations.
 *
 * @author OFS
 */
public class DropboxConfigurationService {

	private static final Logger LOG = LogManager.getLogger(DropboxConfigurationService.class);
	private static final String DROPBOX = "Dropbox";
	
	/**
	 * Method to authenticate user Account by given serviceRequest.
	 *
	 * @param serviceRequest
	 * @return AuthenticateUserAccountResponseDTO
	 */
	public AuthenticateUserAccountResponseDTO authenticateAccount(AuthenticateUserRequestDTO serviceRequest) {
		
		AuthenticateUserAccountResponseDTO response = new AuthenticateUserAccountResponseDTO();

		UserManagementClient UMClient = new UserManagementClient();
		UMClient.addAccount(UserManagementClient.TYPE_NAME_PASSWORD,
				serviceRequest.getLoginId(), serviceRequest.getToken());
		UMClient.authenticate();
		if (!UMClient.isSuccessful()) {
			response.setResponse(new AuthenticationResponseDTO(Messages.AUTHENTICATION_FAILED, Messages.STATUS_FAILURE, UMClient.getAuthenticationToken(), UMClient.getSessionDate(), ""));
		} else {
			response.setResponse(new AuthenticationResponseDTO(Messages.AUTHENTICATION_SUCCESSFULL, Messages.STATUS_SUCCESS, UMClient.getAuthenticationToken(), UMClient.getSessionDate(), ""));
		}
		
		return response;
	}
	
	/**
     * validate and construct the manifest based on the request
     *
     * @param request
     * @return
     */
    public GEMManifestResponse getManifest() {    	
    	// get gem manifest response from GEM
		GEMACLClient gemClient = new GEMACLClient();
		GEMManifestResponse gemManifestFromGEM = gemClient.getACLManifest();
		
		return gemManifestFromGEM;
    }
    
    /**
	 * Method to authenticate user Account and give manifest for given request.
	 *
	 * @param serviceRequest
	 * @return AuthenticateUserAccountResponseDTO
	 */
	public AuthenticateUserAccountResponseDTO authenticateAndGetManifest(AuthenticateUserRequestDTO serviceRequest) {
		
		AuthenticateUserAccountResponseDTO response = new AuthenticateUserAccountResponseDTO();

		UserManagementClient UMClient = new UserManagementClient();
		UMClient.addAccount(UserManagementClient.TYPE_NAME_PASSWORD,
				serviceRequest.getLoginId(), serviceRequest.getToken());
		UMClient.authenticate();
		if (!UMClient.isSuccessful()) {
			response.setResponse(new AuthenticationResponseDTO(Messages.AUTHENTICATION_FAILED, Messages.STATUS_FAILURE, UMClient.getAuthenticationToken(), UMClient.getSessionDate(), ""));
		} else {
			response.setResponse(new AuthenticationResponseDTO(Messages.AUTHENTICATION_SUCCESSFULL, Messages.STATUS_SUCCESS, UMClient.getAuthenticationToken(), UMClient.getSessionDate(), ""));
		}
		
		return response;
	}
}
