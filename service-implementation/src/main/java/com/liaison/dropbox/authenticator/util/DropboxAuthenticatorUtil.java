package com.liaison.dropbox.authenticator.util;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.service.dropbox.DropboxAuthenticationService;
import com.liaison.mailbox.service.dto.configuration.request.AuthenticateUserRequestDTO;
import com.liaison.mailbox.service.util.MailBoxUtil;

public class DropboxAuthenticatorUtil {
	
	private static final Logger LOGGER = LogManager.getLogger(DropboxAuthenticatorUtil.class);
	
	public static Response authenticateAndGetManifest(HttpServletRequest request) {
		
		
		// retrieve request headers from request
		String aclManifest = MailBoxUtil.getEnvironmentProperties().getString("dummy.acl.manifest.json");//request.getHeader(MailBoxConstants.ACL_MANIFEST_HEADER);
		String aclSignature = "dummy-signature";//request.getHeader(MailBoxConstants.ACL_SIGNATURE_HEADER);
		String aclSignerGuid = "dummy-signer-guid";//request.getHeader(MailBoxConstants.ACL_SIGNER_GUID_HEADER);
		String token = "dummy-token";//request.getHeader(MailBoxConstants.AUTH_TOKEN);
		
		// authenticate and authorize
		Response authResponse =  null;
				
		DropboxAuthenticationService authService = new DropboxAuthenticationService();
		AuthenticateUserRequestDTO authenticationDTO = constructAuthenticationRequest("user Name", "password", token);
		//authResponse = authService.authenticateAndGetManifest(authenticationDTO);
		
		// Dummy Response always returning success is sent 
		authResponse =  Response.ok().header(MailBoxConstants.ACL_MANIFEST_HEADER, aclManifest)
				.header(MailBoxConstants.ACL_SIGNED_MANIFEST_HEADER, aclSignature).header(MailBoxConstants.ACL_SIGNER_GUID_HEADER, aclSignerGuid)
				.header(MailBoxConstants.AUTH_TOKEN, token)
				.status(Response.Status.OK).build();
		return authResponse;
		
	}
	
	public static AuthenticateUserRequestDTO constructAuthenticationRequest(String UserName, String password, String authenticationToken) {
		
		AuthenticateUserRequestDTO authenticationDTO = new AuthenticateUserRequestDTO();
		authenticationDTO.setLoginId(UserName);
		authenticationDTO.setToken(password);
		return authenticationDTO;
	}
	
	public static Map<String, String> retrieveResponseHeaders(Response response) {
		
		HashMap <String, String> headers = new HashMap<String, String>();
		MultivaluedMap<String, Object> metadata = response.getMetadata();
		String aclManifest = metadata.get(MailBoxConstants.ACL_MANIFEST_HEADER).get(0).toString();
		String aclSignature = metadata.get(MailBoxConstants.ACL_SIGNED_MANIFEST_HEADER).get(0).toString();
		String aclSignerGuid = metadata.get(MailBoxConstants.ACL_SIGNER_GUID_HEADER).get(0).toString();
		String token = metadata.get(MailBoxConstants.AUTH_TOKEN).get(0).toString();
		headers.put(MailBoxConstants.ACL_MANIFEST_HEADER, aclManifest);
		headers.put(MailBoxConstants.ACL_SIGNED_MANIFEST_HEADER, aclSignature);
		headers.put(MailBoxConstants.ACL_SIGNER_GUID_HEADER, aclSignerGuid);
		headers.put(MailBoxConstants.AUTH_TOKEN, token);
		return headers;
	}
	

}
