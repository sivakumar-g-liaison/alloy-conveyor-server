package com.liaison.dropbox.authenticator.util;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.service.dropbox.DropboxAuthenticationService;
import com.liaison.mailbox.service.dto.dropbox.request.DropboxAuthAndGetManifestRequestDTO;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.util.EncryptionUtil;
import com.liaison.mailbox.service.util.MailBoxUtil;

public class DropboxAuthenticatorUtil {
	
	private static final Logger LOGGER = LogManager.getLogger(DropboxAuthenticatorUtil.class);
	
	public static Response authenticateAndGetManifest(HttpServletRequest request) {
		
		
		// retrieve request headers from request
		LOGGER.info("get request headers");
		String aclManifest = MailBoxUtil.getEnvironmentProperties().getString("dummy.acl.manifest.json");//request.getHeader(MailBoxConstants.ACL_MANIFEST_HEADER);
		String aclSignature = "dummy-signature";//request.getHeader(MailBoxConstants.ACL_SIGNATURE_HEADER);
		String aclSignerGuid = "dummy-signer-guid";//request.getHeader(MailBoxConstants.ACL_SIGNER_GUID_HEADER);
		String mailboxToken = "dummy-token";//request.getHeader(MailBoxConstants.AUTH_TOKEN);
		
		//TODO: Remove the comments once UMClient is modified to support authentication using authenticationToken.
		// retrieve login id from mailbox token
		/*String loginId = getPartofToken(mailboxToken, MailBoxConstants.LOGIN_ID);
		
		// retrieve authentication token from mailbox token
		String authenticationToken = getPartofToken(mailboxToken, MailBoxConstants.UM_AUTH_TOKEN);
		
		// authenticate and authorize
		Response authResponse =  null;
				
		DropboxAuthenticationService authService = new DropboxAuthenticationService();
		DropboxAuthAndGetManifestRequestDTO dropboxAuthAndGetManifestRequestDTO = constructAuthenticationRequest(loginId, null, authenticationToken);
		authResponse = authService.authenticateAndGetManifest(dropboxAuthAndGetManifestRequestDTO); */
		
		// Dummy Response always returning success is sent 
		Response authResponse =  Response.ok().header(MailBoxConstants.ACL_MANIFEST_HEADER, aclManifest)
				.header(MailBoxConstants.ACL_SIGNED_MANIFEST_HEADER, aclSignature).header(MailBoxConstants.ACL_SIGNER_GUID_HEADER, aclSignerGuid)
				.header(MailBoxConstants.DROPBOX_AUTH_TOKEN, mailboxToken)
				.status(Response.Status.OK).build();
		return authResponse;
		
	}
	
	public static DropboxAuthAndGetManifestRequestDTO constructAuthenticationRequest(String UserName, String password, String authenticationToken) {
		
		DropboxAuthAndGetManifestRequestDTO dropboxAuthAndGetManifestRequestDTO = new DropboxAuthAndGetManifestRequestDTO();
		dropboxAuthAndGetManifestRequestDTO.setLoginId(UserName);
		dropboxAuthAndGetManifestRequestDTO.setPassword(password);
		dropboxAuthAndGetManifestRequestDTO.setToken(authenticationToken);
		return dropboxAuthAndGetManifestRequestDTO;
	}
	
	public static Map<String, String> retrieveResponseHeaders(Response response) {
		
		HashMap <String, String> headers = new HashMap<String, String>();
		MultivaluedMap<String, Object> metadata = response.getMetadata();
		String aclManifest = metadata.get(MailBoxConstants.ACL_MANIFEST_HEADER).get(0).toString();
		String aclSignature = metadata.get(MailBoxConstants.ACL_SIGNED_MANIFEST_HEADER).get(0).toString();
		String aclSignerGuid = metadata.get(MailBoxConstants.ACL_SIGNER_GUID_HEADER).get(0).toString();
		String token = metadata.get(MailBoxConstants.DROPBOX_AUTH_TOKEN).get(0).toString();
		headers.put(MailBoxConstants.ACL_MANIFEST_HEADER, aclManifest);
		headers.put(MailBoxConstants.ACL_SIGNED_MANIFEST_HEADER, aclSignature);
		headers.put(MailBoxConstants.ACL_SIGNER_GUID_HEADER, aclSignerGuid);
		headers.put(MailBoxConstants.DROPBOX_AUTH_TOKEN, token);
		return headers;
	}
	
	public static String [] retrieveAuthTokenDetails(String token) throws InvalidKeyException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException  {

		LOGGER.debug("Retrieval of Token Details");
		String decryptedToken = EncryptionUtil.decrypt(token.getBytes(MailBoxConstants.CHARSETNAME), true);
		LOGGER.debug("decryptedToken token {} ",decryptedToken);
		// Retrieval of recent revision date from token
		return decryptedToken.split(MailBoxConstants.TOKEN_SEPARATOR);
	}
	
	private static String getPartofToken(String mailboxToken, String detailString)  {

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
		} catch (InvalidKeyException | InvalidAlgorithmParameterException
				| NoSuchAlgorithmException | NoSuchPaddingException
				| IllegalBlockSizeException | BadPaddingException
				| UnsupportedEncodingException e) {
			throw new MailBoxServicesException("Token cannot be processed." + e.getMessage(), Response.Status.BAD_REQUEST);
		}


	}
	

}
