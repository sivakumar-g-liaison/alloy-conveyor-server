/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.dropbox;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.liaison.commons.util.client.http.HTTPRequest;
import com.liaison.commons.util.client.http.HTTPRequest.HTTP_METHOD;
import com.liaison.commons.util.client.http.HTTPResponse;
import com.liaison.gem.service.client.GEMHelper;
import com.liaison.gem.service.client.GEMManifestResponse;
import com.liaison.gem.util.GEMConstants;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.service.dto.dropbox.request.DropboxAuthAndGetManifestRequestDTO;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.util.MailBoxUtil;
import com.liaison.usermanagement.service.client.UserManagementClient;
import com.liaison.usermanagement.service.dto.AuthenticationResponseDTO;
import com.liaison.usermanagement.service.dto.response.AuthenticateUserAccountResponseDTO;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.www.http.HTTP;

import javax.activation.DataHandler;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Base64;
import java.util.Properties;

import static com.google.common.net.HttpHeaders.CONTENT_TYPE;
import static com.liaison.mailbox.MailBoxConstants.ACL;
import static com.liaison.mailbox.MailBoxConstants.TOKEN;
import static com.liaison.mailbox.MailBoxConstants.ACTIVE;
import static com.liaison.mailbox.MailBoxConstants.BYTE_SIZE;
import static com.liaison.mailbox.MailBoxConstants.ACCESS_TOKEN;
import static com.liaison.mailbox.MailBoxConstants.CONFIGURATION;
import static com.liaison.mailbox.MailBoxConstants.AUTHORIZATION;
import static com.liaison.mailbox.MailBoxConstants.TOKEN_TYPE_HINT;
import static com.liaison.mailbox.MailBoxConstants.TOKEN_AUTHORIZATION;
import static com.liaison.mailbox.MailBoxConstants.PROPERTY_ENABLE_SSO;
import static com.liaison.mailbox.MailBoxConstants.SSO_CLIENT_AUTH_URL;
import static com.liaison.mailbox.MailBoxConstants.SSO_USER_INFO_URL;
import static com.liaison.mailbox.MailBoxConstants.MANIFEST_COOKIE_NAME;
import static com.liaison.mailbox.MailBoxConstants.GROUP_GUID_COOKIE_NAME;
import static com.liaison.mailbox.MailBoxConstants.SIGNATURE_COOKIE_NAME;
import static com.liaison.mailbox.MailBoxConstants.BYTE_ARRAY_INITIAL_SIZE;

/**
 * Class which has mailbox configuration related operations.
 *
 * @author OFS
 */
public class DropboxAuthenticationService {

    private static final Logger LOG = LogManager.getLogger(DropboxAuthenticationService.class);    

    /**
     * Method to authenticate user Account by given serviceRequest.
     *
     * @param serviceRequest
     * @return AuthenticateUserAccountResponseDTO
     */
    public AuthenticateUserAccountResponseDTO authenticateAccount(DropboxAuthAndGetManifestRequestDTO serviceRequest) {

        LOG.debug("Entering into user authentication for dropbox.");

        AuthenticateUserAccountResponseDTO response = new AuthenticateUserAccountResponseDTO();
        AuthenticationResponseDTO authResponse = new AuthenticationResponseDTO();

        UserManagementClient UMClient = new UserManagementClient();
        UMClient.addAccount(
                UserManagementClient.TYPE_NAME_PASSWORD,
                serviceRequest.getLoginId(),
                serviceRequest.getPassword(),
                serviceRequest.getToken());
        UMClient.authenticate();

        if (!UMClient.isSuccessful()) {

            authResponse.setMessage(UMClient.getMessage());
            authResponse.setFailureReasonCode(UMClient.getFailureReasonCode());
            authResponse.setAuthenticationStatus(com.liaison.usermanagement.enums.Messages.STATUS_FAILURE.value());
            authResponse.setAuthenticationToken(UMClient.getAuthenticationToken());
            authResponse.setSessionDate(UMClient.getSessionDate());
            authResponse.setSessionValidTillDate(UMClient.getSessionValidTillDate());
            response.setResponse(authResponse);
            LOG.debug("Auth failed");
            return response;
        }

        authResponse.setMessage(UMClient.getMessage());
        authResponse.setAuthenticationStatus(com.liaison.usermanagement.enums.Messages.STATUS_SUCCESS.value());
        authResponse.setAuthenticationToken(UMClient.getAuthenticationToken());
        authResponse.setSessionDate(UMClient.getSessionDate());
        authResponse.setSessionValidTillDate(UMClient.getSessionValidTillDate());
        response.setResponse(authResponse);

        LOG.debug("Exit from user authentication for dropbox.");
        return response;
    }

    /**
     * getting manifest from GEMClient and construct multipart response
     *
     * @return
     * @throws IOException
     * @throws MessagingException
     */
    public Response getManifest() throws MessagingException, IOException {

        LOG.debug("Entering into get manifest service.");

        Response response = null;
        MimeMultipart multiPartResponse = new MimeMultipart();
        Session session = Session.getDefaultInstance(new Properties());
        MimeMessage mm = new MimeMessage(session);
        ByteArrayOutputStream rawMimeBAOS = new ByteArrayOutputStream(BYTE_ARRAY_INITIAL_SIZE);
        InputStream mimeStreamResponse = null;
        GEMManifestResponse gemManifestFromGEM = null;

        try {
            // get gem manifest response from GEM
            gemManifestFromGEM = GEMHelper.getACLManifest();

            BodyPart responseMessage = new MimeBodyPart();
            responseMessage.setDataHandler(new DataHandler(new ByteArrayDataSource(gemManifestFromGEM.getMessage(),
                    "text/plain")));

            BodyPart plainManifest = new MimeBodyPart();
            plainManifest.setDataHandler(new DataHandler(new ByteArrayDataSource(gemManifestFromGEM.getManifest(),
                    "text/plain")));

            BodyPart signedManifest = new MimeBodyPart();
            signedManifest.setDataHandler(new DataHandler(new ByteArrayDataSource(gemManifestFromGEM.getSignature(),
                    "text/plain")));

            multiPartResponse.addBodyPart(responseMessage);
            multiPartResponse.addBodyPart(plainManifest);
            multiPartResponse.addBodyPart(signedManifest);

            mm.setContent(multiPartResponse);
            mm.writeTo(rawMimeBAOS);
            mimeStreamResponse = new ByteArrayInputStream(rawMimeBAOS.toByteArray());

            ResponseBuilder builder = Response
                    .ok(mimeStreamResponse)
                    .header("Content-Type", MediaType.MULTIPART_FORM_DATA);

            // set signer public key guid in response header based on response from GEM
            if (!MailBoxUtil.isEmpty(gemManifestFromGEM.getPublicKeyGroupGuid())) {
                builder.header(GEMConstants.HEADER_KEY_ACL_SIGNATURE_PUBLIC_KEY_GROUP_GUID, gemManifestFromGEM.getPublicKeyGroupGuid());
            } else if (!MailBoxUtil.isEmpty(gemManifestFromGEM.getPublicKeyGuid())) {
                builder.header(GEMConstants.HEADER_KEY_ACL_SIGNATURE_PUBLIC_KEY_GUID,
                        gemManifestFromGEM.getPublicKeyGuid());
            }
            response = builder.build();

            rawMimeBAOS.close();
            mimeStreamResponse.close();

            LOG.debug("Exit from get manifest service.");

            return response;

        } catch (Exception e) {

            LOG.error("Get manifest failed.", e);

            BodyPart responseMessage = new MimeBodyPart();
            responseMessage.setDataHandler(new DataHandler(new ByteArrayDataSource(
                    MailBoxConstants.ACL_MANIFEST_FAILURE_MESSAGE, "text/plain")));

            multiPartResponse.addBodyPart(responseMessage);

            mm.setContent(multiPartResponse);
            mm.writeTo(rawMimeBAOS);
            mimeStreamResponse = new ByteArrayInputStream(rawMimeBAOS.toByteArray());

            ResponseBuilder builder = Response
                    .ok(mimeStreamResponse)
                    .header("Content-Type", MediaType.MULTIPART_FORM_DATA);

            // set signer public key guid in response header based on response from GEM
            if (!MailBoxUtil.isEmpty(gemManifestFromGEM.getPublicKeyGroupGuid())) {
                builder.header(GEMConstants.HEADER_KEY_ACL_SIGNATURE_PUBLIC_KEY_GROUP_GUID,
                        gemManifestFromGEM.getPublicKeyGroupGuid());
            } else if (!MailBoxUtil.isEmpty(gemManifestFromGEM.getPublicKeyGuid())) {
                builder.header(GEMConstants.HEADER_KEY_ACL_SIGNATURE_PUBLIC_KEY_GUID,
                        gemManifestFromGEM.getPublicKeyGuid());
            }
            response = builder.build();

            rawMimeBAOS.close();
            mimeStreamResponse.close();

            return response;
        }
    }

    /**
     * Method to authenticate user Account and give manifest for given request.
     *
     * @param serviceRequest
     * @return GEMManifestResponse
     */
    public GEMManifestResponse getManifestAfterAuthentication(DropboxAuthAndGetManifestRequestDTO serviceRequest) {
        // get manifest from GEM for the given loginId
        return GEMHelper.getACLManifestByLoginId(serviceRequest.getLoginId(), null);
    }

    public String isAccountAuthenticatedSuccessfully(DropboxAuthAndGetManifestRequestDTO serviceRequest) {

        LOG.debug("Entering into user authentication using UM client.");

        UserManagementClient UMClient = new UserManagementClient();
        UMClient.addAccount(UserManagementClient.TYPE_NAME_PASSWORD,
                serviceRequest.getLoginId(),
                serviceRequest.getPassword(),
                serviceRequest.getToken());
        UMClient.authenticate();

        LOG.debug("Exit from user authentication using UM client.");

        if (UMClient.isSuccessful()) {
            return UMClient.getAuthenticationToken();
        } else {
            throw new MailBoxServicesException(UMClient.getMessage(), Response.Status.UNAUTHORIZED);
        }
    }
    
    /**
     * This method is used to validate the access token against SSO
     * 
     * @param accessToken
     * @return boolean
     */
    public boolean validateToken(String accessToken) {
        
        //validate token
        OutputStream outputStream = null;
        try {
            
            LOG.debug("Access Token : " + accessToken);
            outputStream = new ByteArrayOutputStream(BYTE_SIZE);
            
            URL url = new URL(CONFIGURATION.getString(SSO_CLIENT_AUTH_URL));
            HTTPRequest request = new HTTPRequest(HTTP_METHOD.POST, url);
            
            request.addHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON);
            request.setOutputStream(outputStream);            
            String authorization =  Base64.getEncoder().encodeToString(TOKEN_AUTHORIZATION.getBytes());
            request.addHeader(AUTHORIZATION, "Basic " + authorization);
            
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty(TOKEN, accessToken.trim());
            jsonObject.addProperty(TOKEN_TYPE_HINT, ACCESS_TOKEN);                
            request.inputData(jsonObject.toString());
            
            HTTPResponse response = request.execute();
            int statusCode = response.getStatusCode();
            if (statusCode < HTTP.OK || statusCode > HTTP.MULTIPLE_CHOICE) {
                throw new RuntimeException("Failed to validate the access token against SSO : " + statusCode);
            }
            
            if (null == outputStream.toString()) {
                throw new RuntimeException("Received Ok but did not received any response from SSO : " + statusCode);
            }
            
            JsonObject json = new JsonParser().parse(outputStream.toString()).getAsJsonObject();
            return json.get(ACTIVE).getAsBoolean();
        } catch (Exception e) {
            throw new MailBoxServicesException("Failed to validate the access token against SSO : " + e.getMessage(), Response.Status.UNAUTHORIZED);
        } finally {
            if (null != outputStream) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    LOG.error("Failed to close the output stream");
                }
            }
        }
    }
    
    /**
     * This method is used to authenticated sso/Usermanagement based on the configuration
     * 
     * @param authenticationToken
     * @param dropboxAuthAndGetManifestRequestDTO
     */
    public String authenticateToken(String authenticationToken, DropboxAuthAndGetManifestRequestDTO dropboxAuthAndGetManifestRequestDTO) {
        
        if (CONFIGURATION.getBoolean(PROPERTY_ENABLE_SSO, true)) {
            return validateToken(authenticationToken) ? authenticationToken : null;
        } else {            
            return isAccountAuthenticatedSuccessfully(dropboxAuthAndGetManifestRequestDTO);                   
        }        
    }
    
    /**
     * This method is used to get the GEMManifestResponse from SSO
     * 
     * @param accessToken
     * @return
     */
    public GEMManifestResponse getManifestBySsoAccessToken(String accessToken) {
        
        String manifest = null;
        String signature = null;
        String pkGroupGuid = null;
        OutputStream outputStream = null;
        GEMManifestResponse manifestResponse = null;
        try {
            
            outputStream = new ByteArrayOutputStream(BYTE_SIZE);
            URL url = new URL(CONFIGURATION.getString(SSO_USER_INFO_URL));
            HTTPRequest request = new HTTPRequest(HTTP_METHOD.GET, url);
            
            String userAuthorization = String.format("Bearer %s", accessToken);
            request.addHeader(AUTHORIZATION, userAuthorization);
            request.setOutputStream(outputStream);
            
            HTTPResponse response = request.execute();           
            int statusCode = response.getStatusCode();
            if (statusCode < HTTP.OK || statusCode > HTTP.MULTIPLE_CHOICE) {                
                throw new RuntimeException("Failed to get the manifest from SSO using access token: " + statusCode);
            }            
            
            JsonObject userInfoJson = new JsonParser().parse(outputStream.toString()).getAsJsonObject();
            JsonObject aclJson = userInfoJson.get(ACL).getAsJsonObject();
    
            manifest = aclJson.get(MANIFEST_COOKIE_NAME).toString();
            signature = aclJson.get(SIGNATURE_COOKIE_NAME).getAsString();
            pkGroupGuid = aclJson.get(GROUP_GUID_COOKIE_NAME).getAsString();
            
            manifestResponse = new GEMManifestResponse(null, manifest, signature, pkGroupGuid, null);          
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (null != outputStream) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    LOG.error("failed to close the output stream");
                }
            }
        }
        
        return manifestResponse;
    }
    
    /**
     * This method is used to get the manifest from sso.GEM based on configuration
     * 
     * @param accessToken
     * @param serviceRequest
     * @return
     */
    public GEMManifestResponse getManifestAfterAuthentication(String accessToken, DropboxAuthAndGetManifestRequestDTO serviceRequest) {
        
        if (CONFIGURATION.getBoolean(PROPERTY_ENABLE_SSO, true)) {
            return getManifestBySsoAccessToken(accessToken);
        } else {            
            return getManifestAfterAuthentication(serviceRequest);                   
        }
    }
}
