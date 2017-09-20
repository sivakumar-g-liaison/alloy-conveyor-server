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
import com.liaison.commons.audit.exception.LiaisonAuditableWebApplicationException;
import com.liaison.commons.util.client.sftp.StringUtil;
import com.liaison.gem.service.client.GEMHelper;
import com.liaison.gem.service.client.GEMManifestResponse;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.service.dto.dropbox.response.DropboxAuthAndGetManifestResponseDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.usermanagement.enums.IdpTypes;
import com.liaison.usermanagement.service.client.UserManagementClient;
import com.liaison.usermanagement.service.client.filter.AuthenticationFilter;
import com.liaison.usermanagement.service.client.filter.UserAuthenticationException;
import com.sun.jersey.core.header.InBoundHeaders;
import com.sun.jersey.core.util.Base64;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import com.sun.jersey.spi.container.ResourceFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import java.util.Map;

import static com.liaison.mailbox.MailBoxConstants.MANIFEST_DTO;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * AuthenticationFilter for services requires GUM authentication
 *
 * Required property - usermanagement.url.authenticate
 */
public class ConveyorAuthFilter implements ResourceFilter, ContainerRequestFilter {

    private static final String TOKEN = "token";
    private Logger logger = LogManager.getLogger(AuthenticationFilter.class);

    @Override
    public ContainerRequest filter(ContainerRequest request) {

        String authenticationToken = request.getHeaderValue(MailBoxConstants.DROPBOX_AUTH_TOKEN);
        String loginId = request.getHeaderValue(MailBoxConstants.DROPBOX_LOGIN_ID);
        String aclManifest = request.getHeaderValue(MailBoxConstants.ACL_MANIFEST_HEADER);

        //mandatory validation
        if (StringUtil.isNullOrEmptyAfterTrim(authenticationToken)
                || StringUtil.isNullOrEmptyAfterTrim(aclManifest)
                || StringUtil.isNullOrEmptyAfterTrim(loginId)) {
            throw new MailBoxConfigurationServicesException(Messages.REQUEST_HEADER_PROPERTIES_MISSING, Response.Status.BAD_REQUEST);
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
                throw new MailBoxServicesException(Messages.AUTH_AND_GET_ACL_FAILURE, Response.Status.BAD_REQUEST);
            }

            //refresh the token
            MultivaluedMap<String, String> headers = request.getRequestHeaders();
            headers.putSingle(TOKEN, umClient.getAuthenticationToken());
            headers.putSingle(MANIFEST_DTO, new Gson().toJson(manifestResponse));
            request.setHeaders((InBoundHeaders) headers);
            return request;
        } else {

            String message = "Authentication failure : " + umClient.getMessage();
            logger.error("Authentication failure : " + umClient.getMessage() + ", Failure Message Code : " + umClient.getFailureReasonCode());
            throw new UserAuthenticationException(message);
        }

    }

    @Override
    public ContainerRequestFilter getRequestFilter() {
        return this;
    }

    @Override
    public ContainerResponseFilter getResponseFilter() {
        return null;
    }

}
