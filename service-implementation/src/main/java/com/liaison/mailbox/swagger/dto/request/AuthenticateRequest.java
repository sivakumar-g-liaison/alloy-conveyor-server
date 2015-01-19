/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.mailbox.swagger.dto.request;

import com.liaison.mailbox.service.dto.dropbox.request.DropboxAuthAndGetManifestRequestDTO;

/**
 * @author OFS
 *
 */
public class AuthenticateRequest {

	private DropboxAuthAndGetManifestRequestDTO authenticateRequest;

	public DropboxAuthAndGetManifestRequestDTO getAuthenticateRequest() {
		return authenticateRequest;
	}

	public void setAuthenticateRequest(DropboxAuthAndGetManifestRequestDTO authenticateRequest) {
		this.authenticateRequest = authenticateRequest;
	}

}
