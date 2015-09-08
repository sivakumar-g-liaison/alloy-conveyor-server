/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.dropbox.authenticator.util;

import javax.ws.rs.core.Response;

import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.service.dto.dropbox.request.DropboxAuthAndGetManifestRequestDTO;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.util.EncryptionUtil;

/**
 * Utilities for Dropbox authentication.
 * 
 * @author OFS
 */
public class DropboxAuthenticatorUtil {

	private static final Logger LOGGER = LogManager.getLogger(DropboxAuthenticatorUtil.class);

	public static String getPartofToken(String mailboxToken, String detailString) {

		String[] tokenDetails;
		try {
			tokenDetails = retrieveAuthTokenDetails(mailboxToken);

			if ((tokenDetails == null) || (tokenDetails.length != 2)) {
				return null;
			}

			switch (detailString) {
			case MailBoxConstants.UM_AUTH_TOKEN:
				return tokenDetails[0];
			case MailBoxConstants.LOGIN_ID:
				return tokenDetails[1];
			default:
				return null;
			}
		} catch (Exception e) {
			throw new MailBoxServicesException("Token cannot be processed." + e.getMessage(), Response.Status.BAD_REQUEST);
		}
	}

	public static String[] retrieveAuthTokenDetails(String token) {

		LOGGER.debug("Retrieval of Token Details");
		byte[] decodedToken = Base64.decodeBase64(token.getBytes());
		String decryptedToken = new String(EncryptionUtil.encrypt(decodedToken, MailBoxConstants.STATIC_KEY, MailBoxConstants.IV_BYTES, MailBoxConstants.DECRYPT_MODE));
		LOGGER.debug("decryptedToken token {} ", decryptedToken);
		// Retrieval of recent revision date from token
		return decryptedToken.split(MailBoxConstants.TOKEN_SEPARATOR);
	}

	public static DropboxAuthAndGetManifestRequestDTO constructAuthenticationRequest(String UserName, String password,
			String authenticationToken) {

		DropboxAuthAndGetManifestRequestDTO dropboxAuthAndGetManifestRequestDTO = new DropboxAuthAndGetManifestRequestDTO();
		dropboxAuthAndGetManifestRequestDTO.setLoginId(UserName);
		dropboxAuthAndGetManifestRequestDTO.setPassword(password);
		dropboxAuthAndGetManifestRequestDTO.setToken(authenticationToken);
		return dropboxAuthAndGetManifestRequestDTO;
	}
}
