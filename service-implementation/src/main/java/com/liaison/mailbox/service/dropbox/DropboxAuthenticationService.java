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
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
import com.liaison.mailbox.service.dto.dropbox.request.DropboxAuthAndGetManifestRequestDTO;
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
		UMClient.addAccount(UserManagementClient.TYPE_NAME_PASSWORD, serviceRequest.getLoginId(),
				serviceRequest.getPassword(), serviceRequest.getToken());
		UMClient.authenticate();

		if (!UMClient.isSuccessful()) {
			response.setResponse(new AuthenticationResponseDTO(
					com.liaison.usermanagement.enums.Messages.AUTHENTICATION_FAILED,
					com.liaison.usermanagement.enums.Messages.STATUS_FAILURE, UMClient.getAuthenticationToken(),
					UMClient.getSessionDate(), UMClient.getSessionValidTillDate()));
			LOG.debug("Auth failed");
			return response;
		}

		response.setResponse(new AuthenticationResponseDTO(
				com.liaison.usermanagement.enums.Messages.AUTHENTICATION_SUCCESSFULL,
				com.liaison.usermanagement.enums.Messages.STATUS_SUCCESS, UMClient.getAuthenticationToken(), UMClient
						.getSessionDate(), UMClient.getSessionValidTillDate()));

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

		LOG.debug("Entering into get manifest service.");

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

			response = Response
					.ok(mimeStreamResponse)
					.header("Content-Type", MediaType.MULTIPART_FORM_DATA)
					.header(GEMConstants.HEADER_KEY_ACL_SIGNATURE_PUBLIC_KEY_GUID,
							gemManifestFromGEM.getPublicKeyGuid()).build();

			rawMimeBAOS.close();
			mimeStreamResponse.close();

			LOG.debug("Exit from get manifest service.");

			return response;

		} catch (Exception e) {

			LOG.error("Get manifest failed.");

			BodyPart responseMessage = new MimeBodyPart();
			responseMessage.setDataHandler(new DataHandler(new ByteArrayDataSource(
					MailBoxConstants.ACL_MANIFEST_FAILURE_MESSAGE, "text/plain")));

			multiPartResponse.addBodyPart(responseMessage);

			mm.setContent(multiPartResponse);
			mm.writeTo(rawMimeBAOS);
			mimeStreamResponse = new ByteArrayInputStream(rawMimeBAOS.toByteArray());

			response = Response
					.ok(mimeStreamResponse)
					.header("Content-Type", MediaType.MULTIPART_FORM_DATA)
					.header(GEMConstants.HEADER_KEY_ACL_SIGNATURE_PUBLIC_KEY_GUID,
							gemManifestFromGEM.getPublicKeyGuid()).build();

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
	public GEMManifestResponse getManifestAfterAuthentication(DropboxAuthAndGetManifestRequestDTO serviceRequest) {

		GEMManifestResponse manifestFromGEM = null;

		LOG.debug("Entering into retrieve manifest service using GEM client");
		
		try {

			// get manifest from GEM for the given loginId
			GEMACLClient gemClient = new GEMACLClient();
			ManifestRequestDTO manifestRequestDTO = constructACLManifestRequest(serviceRequest.getLoginId());
			String unsignedDocument = GEMUtil.marshalToJSON(manifestRequestDTO);
			String signedDocument = gemClient.signRequestData(unsignedDocument);
			String publicKeyGuid = configuration.getString(GEMConstants.HEADER_KEY_ACL_SIGNATURE_PUBLIC_KEY_GUID);
			manifestFromGEM = gemClient.getACLManifest(unsignedDocument, signedDocument, publicKeyGuid,
					unsignedDocument);

		} catch (Exception e) {
			LOG.error("Dropbox - getting manifest after authentication failed.", e);
			e.printStackTrace();
		}
		
		LOG.debug("Exit from retrieve manifest service using GEM client");

		return manifestFromGEM;
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

	public String isAccountAuthenticatedSuccessfully(DropboxAuthAndGetManifestRequestDTO serviceRequest)
			throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException,
			InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchPaddingException {
		
		LOG.debug("Entering into user authentication using UM client.");

		UserManagementClient UMClient = new UserManagementClient();
		UMClient.addAccount(UserManagementClient.TYPE_NAME_PASSWORD, serviceRequest.getLoginId(),
				serviceRequest.getPassword(), serviceRequest.getToken());
		UMClient.authenticate();
		
		LOG.debug("Exit from user authentication using UM client.");

		if (UMClient.isSuccessful()) {
			String mailboxTokenWithLoginId = new StringBuilder(UMClient.getAuthenticationToken()).append("::")
					.append(serviceRequest.getLoginId()).toString();
			String encryptedEncodedToken = new String(Base64.encode(EncryptionUtil.encrypt(mailboxTokenWithLoginId,
					true)));
			return encryptedEncodedToken;
		}

		return null;
	}
}
