/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.integration.test;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.liaison.commons.exception.LiaisonException;
import com.liaison.commons.jaxb.JAXBUtility;
import com.liaison.commons.util.client.http.HTTPRequest;
import com.liaison.commons.util.client.http.HTTPRequest.HTTP_METHOD;
import com.liaison.commons.util.client.http.HTTPResponse;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.service.base.test.BaseServiceTest;
import com.liaison.mailbox.service.dto.dropbox.request.DropboxAuthAndGetManifestRequestDTO;
import com.liaison.mailbox.service.dto.dropbox.response.DropboxAuthAndGetManifestResponseDTO;
import com.liaison.mailbox.service.dto.dropbox.response.GetStagedFilesResponseDTO;
import com.liaison.mailbox.service.util.MailBoxUtil;

/**
 * Test class to test mailbox configuration service.
 * 
 * @author OFS
 */

public class DropboxIntegrationServiceTest extends BaseServiceTest {
    
	private Logger logger;
	private String jsonResponse;
	private String jsonRequest;
	private HTTPRequest request;
	
	private static String USER_ID = "demouserjan22@liaison.dev";
	private static String PASSWORD = "TG9yZDAyZ2FuZXNoIQ==";
	
	@BeforeClass
	public void setUp() throws Exception {
		logger = LogManager.getLogger(DropboxIntegrationServiceTest.class);
	}	
	
	@Test
	public void testGetListOfStagedFilesAfteAuthenticateAndGetManifest() throws LiaisonException, IOException, JAXBException {
		
		//authenticate user account
		DropboxAuthAndGetManifestRequestDTO reqDTO = constructAuthenticationRequest();
		jsonRequest = MailBoxUtil.marshalToJSON(reqDTO);
		
		String authAndManifestURL = getBASE_URL_DROPBOX() + "/authAndGetACL";
		request = constructHTTPRequest(authAndManifestURL, HTTP_METHOD.POST, jsonRequest, logger);
		HTTPResponse authResponse = request.execute();
		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);
		
		DropboxAuthAndGetManifestResponseDTO authResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, DropboxAuthAndGetManifestResponseDTO.class);
		Assert.assertEquals(SUCCESS, authResponseDTO.getResponse().getStatus());
		
		//getStaged files
		String getStagedFilesURL = getBASE_URL_DROPBOX() + "/stagedFiles";
		request = constructHTTPRequest(getStagedFilesURL, HTTP_METHOD.GET, "", logger);
		request.addHeader(MailBoxConstants.ACL_MANIFEST_HEADER, authResponse.getHeader(MailBoxConstants.ACL_MANIFEST_HEADER));
		request.addHeader(MailBoxConstants.DROPBOX_AUTH_TOKEN, authResponse.getHeader(MailBoxConstants.DROPBOX_AUTH_TOKEN));
		request.execute();
		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);
		
		GetStagedFilesResponseDTO getStagedFilesResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, GetStagedFilesResponseDTO.class);

		Assert.assertEquals(SUCCESS, getStagedFilesResponseDTO.getResponse().getStatus());
	}
	
	private DropboxAuthAndGetManifestRequestDTO constructAuthenticationRequest() {
		DropboxAuthAndGetManifestRequestDTO dto = new DropboxAuthAndGetManifestRequestDTO();
		dto.setLoginId(USER_ID);
		dto.setPassword(PASSWORD);
		return dto;
	}
}
