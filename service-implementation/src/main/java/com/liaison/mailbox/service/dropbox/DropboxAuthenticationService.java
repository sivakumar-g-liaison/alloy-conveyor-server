/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.dropbox;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.datanucleus.util.Base64;

import com.liaison.commons.util.settings.DecryptableConfiguration;
import com.liaison.commons.util.settings.LiaisonConfigurationFactory;
import com.liaison.gem.service.client.GEMACLClient;
import com.liaison.gem.service.client.GEMManifestResponse;
import com.liaison.gem.service.dto.EnvelopeDTO;
import com.liaison.gem.service.dto.request.ManifestRequestDTO;
import com.liaison.gem.service.dto.request.ManifestRequestGEM;
import com.liaison.gem.service.dto.request.ManifestRequestPlatform;
import com.liaison.gem.util.GEMConstants;
import com.liaison.gem.util.GEMUtil;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.service.dto.dropbox.request.DropboxAuthAndGetManifestRequestDTO;
import com.liaison.mailbox.service.dto.dropbox.response.DropboxAuthAndGetManifestResponseDTO;
import com.liaison.mailbox.service.util.EncryptionUtil;
import com.liaison.usermanagement.service.client.UserManagementClient;
import com.liaison.usermanagement.service.dto.AuthenticationResponseDTO;
import com.liaison.usermanagement.service.dto.response.AuthenticateUserAccountResponseDTO;

/**
 * Class which has mailbox configuration related operations.
 * 
 * @author OFS
 */
public class DropboxAuthenticationService {

	private static final DecryptableConfiguration configuration = LiaisonConfigurationFactory.getConfiguration();
	private static final Logger LOG = LogManager.getLogger(DropboxAuthenticationService.class);
	private static final String PROPERTY_PLATFORM_NAME = "com.liaison.acl.request.runtime.platform.name";
	

	/**
	 * Method to authenticate user Account by given serviceRequest.
	 * 
	 * @param serviceRequest
	 * @return AuthenticateUserAccountResponseDTO
	 */
	public AuthenticateUserAccountResponseDTO authenticateAccount(DropboxAuthAndGetManifestRequestDTO serviceRequest) {

		LOG.debug("Entering into user authentication for dropbox.");

		AuthenticateUserAccountResponseDTO response = new AuthenticateUserAccountResponseDTO();

		UserManagementClient UMClient = new UserManagementClient();
		UMClient.addAccount(UserManagementClient.TYPE_NAME_PASSWORD, serviceRequest.getLoginId(),serviceRequest.getPassword(), serviceRequest.getToken());
		UMClient.authenticate();
		
		if (!UMClient.isSuccessful()) {
			response.setResponse(new AuthenticationResponseDTO(com.liaison.usermanagement.enums.Messages.AUTHENTICATION_FAILED,
															   com.liaison.usermanagement.enums.Messages.STATUS_FAILURE,
															   UMClient.getAuthenticationToken(),
															   UMClient.getSessionDate(), UMClient.getSessionValidTillDate()));
			LOG.debug("Auth failed");
			return response;
		}
		
		response.setResponse(new AuthenticationResponseDTO(com.liaison.usermanagement.enums.Messages.AUTHENTICATION_SUCCESSFULL,
															   com.liaison.usermanagement.enums.Messages.STATUS_SUCCESS,
															   UMClient.getAuthenticationToken(),
															   UMClient.getSessionDate(), UMClient.getSessionValidTillDate()));
		
		LOG.debug("Exit from user authentication for dropbox.");
		return response;
	}

	/**
	 * getting manifest from GEMClient and construct multipart response 
	 * 
	 * @param request
	 * @return
	 * @throws IOException
	 * @throws MessagingException
	 */
	public Response getManifest() throws MessagingException, IOException {

		Response response = null;
		MimeMultipart multiPartResponse = new MimeMultipart();
		Session session = Session.getDefaultInstance(new Properties());
		MimeMessage mm = new MimeMessage(session);
		ByteArrayOutputStream rawMimeBAOS = new ByteArrayOutputStream(4096);
		InputStream mimeStreamResponse = null;
		GEMManifestResponse gemManifestFromGEM = null;
		
		try {
			// get gem manifest response from GEM
			GEMACLClient gemClient = new GEMACLClient();
			gemManifestFromGEM = gemClient.getACLManifest();

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

			response = Response.ok(mimeStreamResponse).header("Content-Type", MediaType.MULTIPART_FORM_DATA)
					.header(GEMConstants.HEADER_KEY_ACL_SIGNATURE_PUBLIC_KEY_GUID, gemManifestFromGEM.getPublicKeyGuid()).build();

			rawMimeBAOS.close();
			mimeStreamResponse.close();

			return response;

		} catch (Exception e) {
			
			BodyPart responseMessage = new MimeBodyPart();
			responseMessage.setDataHandler(new DataHandler(new ByteArrayDataSource(MailBoxConstants.ACL_MANIFEST_FAILURE_MESSAGE,
					"text/plain")));

			multiPartResponse.addBodyPart(responseMessage);

			mm.setContent(multiPartResponse);
			mm.writeTo(rawMimeBAOS);
			mimeStreamResponse = new ByteArrayInputStream(rawMimeBAOS.toByteArray());

			response = Response.ok(mimeStreamResponse).header("Content-Type", MediaType.MULTIPART_FORM_DATA)
					.header(GEMConstants.HEADER_KEY_ACL_SIGNATURE_PUBLIC_KEY_GUID, gemManifestFromGEM.getPublicKeyGuid()).build();

			rawMimeBAOS.close();
			mimeStreamResponse.close();
			
			return response;
		}
	}

	/**
	 * Method to authenticate user Account and give manifest for given request.
	 * 
	 * @param serviceRequest
	 * @return AuthenticateUserAccountResponseDTO
	 */
	public Response authenticateAndGetManifest(DropboxAuthAndGetManifestRequestDTO serviceRequest) {

		LOG.debug("Entering into user authentication and get manifest service for dropbox.");

		Response response = null;
		DropboxAuthAndGetManifestResponseDTO responseEntity = null;
       
		
		UserManagementClient UMClient = new UserManagementClient();
		try {
			
			UMClient.addAccount(UserManagementClient.TYPE_NAME_PASSWORD, serviceRequest.getLoginId(),serviceRequest.getPassword(), serviceRequest.getToken());
			
			// Calling UM for authentication
			UMClient.authenticate();
	
			// handling UM authentication response
			if (!UMClient.isSuccessful()) {	
				LOG.error("Dropbox - user authentication failed");	
				responseEntity = new DropboxAuthAndGetManifestResponseDTO(Messages.AUTHENTICATION_FAILURE,Messages.FAILURE);
				return Response.status(401).header("Content-Type", MediaType.APPLICATION_JSON).entity(responseEntity).build();
			} 
	
			// if authenticated successfully get manifest from GEM for the given loginId
			GEMACLClient gemClient = new GEMACLClient();
			ManifestRequestDTO manifestRequestDTO = constructACLManifestRequest(serviceRequest.getLoginId()); 
			String unsignedDocument = GEMUtil.marshalToJSON(manifestRequestDTO);
            String signedDocument = gemClient.signRequestData(unsignedDocument);
            String publicKeyGuid = configuration.getString(GEMConstants.HEADER_KEY_ACL_SIGNATURE_PUBLIC_KEY_GUID);
            GEMManifestResponse manifestFromGEM = gemClient.getACLManifest(unsignedDocument, signedDocument, publicKeyGuid, unsignedDocument);
            
			responseEntity = new DropboxAuthAndGetManifestResponseDTO(Messages.AUTHENTICATION_SUCCESS,Messages.SUCCESS);
			String mailboxTokenWithLoginId = new StringBuilder(UMClient.getAuthenticationToken()).append("::").append(serviceRequest.getLoginId()).toString();
			String encryptedEncodedToken = new String(Base64.encode(EncryptionUtil.encrypt(mailboxTokenWithLoginId, true)));

			ResponseBuilder builder = Response.ok().entity(responseEntity).status(Response.Status.OK)							   .header("Content-Type", MediaType.APPLICATION_JSON)
							   .header(MailBoxConstants.DROPBOX_AUTH_TOKEN, encryptedEncodedToken) //re encrypted token like E(UMClient.getAuthenticationToken()::loginId) 
							   .header(MailBoxConstants.ACL_MANIFEST_HEADER, manifestFromGEM.getManifest())
							   .header(MailBoxConstants.ACL_SIGNED_MANIFEST_HEADER, manifestFromGEM.getSignature())
							   .header(GEMConstants.HEADER_KEY_ACL_SIGNATURE_PUBLIC_KEY_GUID,manifestFromGEM.getPublicKeyGuid());
			response = builder.build();
			
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("Dropbox - getting manifest after authentication failed.",e);
			responseEntity = new DropboxAuthAndGetManifestResponseDTO(Messages.AUTH_AND_GET_ACL_FAILURE,Messages.FAILURE);
			response = Response.status(400).header("Content-Type", MediaType.APPLICATION_JSON).entity(responseEntity).build();
		}
		
		LOG.debug("Exiting from user authentication and get manifest service for dropbox.");

		return response;
	}
	
	private ManifestRequestDTO constructACLManifestRequest(String loginID) {

        LOG.debug("Constructing the gem manifest request with default values");
        // Construct Envelope
        EnvelopeDTO envelope = new EnvelopeDTO();
        envelope.setUserId(loginID);

        // Construct Platform
        ManifestRequestPlatform platform = new ManifestRequestPlatform();
        platform.setName(configuration.getString(PROPERTY_PLATFORM_NAME));

        // Construct ManifestRequestGEM
        ManifestRequestGEM manifestRequestGEM = new ManifestRequestGEM();
        manifestRequestGEM.setEnvelope(envelope);
        manifestRequestGEM.getPlatforms().add(platform);

        // Construct ManifestRequestDTO
        ManifestRequestDTO manifestRequest = new ManifestRequestDTO();
        manifestRequest.setAcl(manifestRequestGEM);

        return manifestRequest;
    }
}
