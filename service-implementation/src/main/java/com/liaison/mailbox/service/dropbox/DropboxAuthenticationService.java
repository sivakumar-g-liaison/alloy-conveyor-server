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

import com.liaison.gem.service.client.GEMHelper;
import com.liaison.gem.service.client.GEMManifestResponse;
import com.liaison.gem.util.GEMConstants;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.service.dto.dropbox.request.DropboxAuthAndGetManifestRequestDTO;
import com.liaison.mailbox.service.util.MailBoxUtil;
import com.liaison.usermanagement.service.client.UserManagementClient;
import com.liaison.usermanagement.service.client.filter.UserAuthenticationException;
import com.liaison.usermanagement.service.dto.AuthenticationResponseDTO;
import com.liaison.usermanagement.service.dto.response.AuthenticateUserAccountResponseDTO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
import java.util.Properties;

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
            throw new UserAuthenticationException(UMClient.getMessage());
        }
    }
}
