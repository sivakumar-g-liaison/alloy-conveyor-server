/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.dto.configuration.request;

import org.codehaus.jackson.map.annotate.JsonRootName;

import com.liaison.commons.util.client.sftp.StringUtil;
import com.liaison.usermanagement.validation.Mandatory;

/**
 * @author OFS
 *
 */
@JsonRootName("authenticateRequest")
public class AuthenticateUserRequestDTO {
	private String loginId;
    private String token;

    @Mandatory(errorMessage = "Login id is mandatory")
    public String getLoginId() {
        return StringUtil.isNullOrEmptyAfterTrim(loginId) ? loginId :loginId.toLowerCase();
    }

    public void setLoginId(String loginId) {
        this.loginId = StringUtil.isNullOrEmptyAfterTrim(loginId) ? loginId :loginId.toLowerCase();
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
