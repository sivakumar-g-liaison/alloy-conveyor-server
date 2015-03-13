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

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.datanucleus.util.Base64;

import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.service.dto.dropbox.request.DropboxAuthAndGetManifestRequestDTO;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.util.EncryptionUtil;

/**
 * @author OFS
 *
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
		} catch (InvalidKeyException | InvalidAlgorithmParameterException | NoSuchAlgorithmException
				| NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException
				| UnsupportedEncodingException e) {
			throw new MailBoxServicesException("Token cannot be processed." + e.getMessage(),
					Response.Status.BAD_REQUEST);
		}
	}

	public static String[] retrieveAuthTokenDetails(String token) throws InvalidKeyException,
			InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchPaddingException,
			IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {

		LOGGER.debug("Retrieval of Token Details");
		byte[] decodedToken = Base64.decode(token);
		String decryptedToken = EncryptionUtil.decrypt(decodedToken, true);
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
