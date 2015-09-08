/**
 * Copyright 2014 Liaison Technologies, Inc.
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.mailbox.service.dto.dropbox.request;

import org.codehaus.jackson.map.annotate.JsonRootName;

/**
 * Data Transfer Object that contains the fields required for authenticating and retrieving manifest.
 *
 * @author OFS
 */
@JsonRootName("authAndGetACLRequest")
public class DropboxAuthAndGetManifestRequestDTO {

	private String loginId;
	private String password;
	private String token;

	public String getLoginId() {
		return loginId;
	}
	public void setLoginId(String loginId) {
		this.loginId = loginId;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
}
