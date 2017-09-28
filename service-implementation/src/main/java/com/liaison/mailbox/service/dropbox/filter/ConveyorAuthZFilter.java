/**
 * Copyright 2017 Liaison Technologies, Inc.
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.mailbox.service.dropbox.filter;

import com.google.gson.Gson;
import com.liaison.commons.acl.util.ACLPermissionCheckRuntimeException;
import com.liaison.commons.util.StringUtil;
import com.liaison.gem.service.client.GEMHelper;
import com.liaison.gem.service.client.GEMManifestResponse;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.usermanagement.enums.IdpTypes;
import com.liaison.usermanagement.service.client.UserManagementClient;
import com.liaison.usermanagement.service.client.filter.AuthenticationFilter;
import com.liaison.usermanagement.service.client.filter.UserAuthenticationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;

import static com.liaison.mailbox.MailBoxConstants.MANIFEST_DTO;
import static com.liaison.mailbox.MailBoxConstants.UM_AUTH_TOKEN;

/**
 * AuthenticationFilter for services requires GUM authentication
 *
 * Required property - usermanagement.url.authenticate
 */
public class ConveyorAuthZFilter implements ContainerRequestFilter {

    private Logger logger = LogManager.getLogger(AuthenticationFilter.class);

    @Override
    public void filter(ContainerRequestContext requestContext) {

        String authenticationToken = requestContext.getHeaderString(MailBoxConstants.DROPBOX_AUTH_TOKEN);
        String loginId = requestContext.getHeaderString(MailBoxConstants.DROPBOX_LOGIN_ID);
        String aclManifest = requestContext.getHeaderString(MailBoxConstants.ACL_MANIFEST_HEADER);

        //mandatory validation
        if (StringUtil.isNullOrEmptyAfterTrim(authenticationToken)
                || StringUtil.isNullOrEmptyAfterTrim(aclManifest)
                || StringUtil.isNullOrEmptyAfterTrim(loginId)) {
            throw new UserAuthenticationException(Messages.REQUEST_HEADER_PROPERTIES_MISSING.value());
        }

        //validate token
        UserManagementClient umClient = new UserManagementClient();
        umClient.addAccount(IdpTypes.NAME_PASSWORD.name(), loginId, null, authenticationToken);

        //authenticate
        logger.info("General authentication Filter..");
        umClient.authenticate();

        if (umClient.isSuccessful()) {

            // validation
            GEMManifestResponse manifestResponse = GEMHelper.getACLManifestByLoginId(loginId, null);
            if (manifestResponse == null) {
                throw new ACLPermissionCheckRuntimeException(Messages.AUTH_AND_GET_ACL_FAILURE.value());
            }

            requestContext.getHeaders().putSingle(UM_AUTH_TOKEN, authenticationToken);
            requestContext.getHeaders().putSingle(MANIFEST_DTO, new Gson().toJson(manifestResponse));

        } else {

            String message = "Authentication failure : " + umClient.getMessage();
            logger.error("Authentication failure : " + umClient.getMessage() + ", Failure Message Code : " + umClient.getFailureReasonCode());
            throw new UserAuthenticationException(message);
        }

    }

}
