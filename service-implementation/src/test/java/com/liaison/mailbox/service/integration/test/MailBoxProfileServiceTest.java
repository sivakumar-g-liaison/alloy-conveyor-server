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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

import javax.xml.bind.JAXBException;

import junit.framework.Assert;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jettison.json.JSONException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liaison.commons.exceptions.LiaisonException;
import com.liaison.commons.util.client.http.HTTPRequest;
import com.liaison.commons.util.client.http.HTTPRequest.HTTP_METHOD;
import com.liaison.framework.util.ServiceUtils;
import com.liaison.mailbox.service.base.test.BaseServiceTest;
import com.liaison.mailbox.service.dto.configuration.MailBoxDTO;
import com.liaison.mailbox.service.dto.configuration.ProfileDTO;
import com.liaison.mailbox.service.dto.configuration.request.AddMailboxRequestDTO;
import com.liaison.mailbox.service.dto.configuration.request.AddProfileRequestDTO;
import com.liaison.mailbox.service.dto.configuration.response.AddMailBoxResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.AddProfileResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.AddProfileToMailBoxResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.DeactivateMailboxProfileLinkResponseDTO;
import com.liaison.mailbox.service.util.MailBoxUtility;

/**
 * Test class which tests the mailbox profile operations.
 * 
 * @author praveenu
 */
public class MailBoxProfileServiceTest extends BaseServiceTest {

	private Logger logger;
	private HTTPRequest request;
	private String jsonRequest;
	private String jsonResponse;

	@Override
	public void initialSetUp() throws FileNotFoundException, IOException {
		super.initialSetUp();
		logger = LoggerFactory.getLogger(MailBoxProfileServiceTest.class);
	}

	@Test
	public void testAddProfileToMailBox() throws LiaisonException, JSONException, IOException, JAXBException {

		// Add the Mailbox
		AddMailBoxResponseDTO responseDTO = createMailBox();
		Assert.assertEquals(SUCCESS, responseDTO.getResponse().getStatus());

		// Add the Profile
		AddProfileResponseDTO profileResponseDTO = addProfile();
		Assert.assertEquals(SUCCESS, profileResponseDTO.getResponse().getStatus());

		// Create Link
		String addProfile = "/" + responseDTO.getMailBox().getGuid() + "/profile/" + profileResponseDTO.getProfile().getGuId();
		HTTPRequest profileRequest = constructHTTPRequest(getBASE_URL() + addProfile, HTTP_METHOD.POST, jsonRequest,
				LoggerFactory.getLogger(MailBoxProfileServiceTest.class));
		profileRequest.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		AddProfileToMailBoxResponseDTO mbProfileResponseDTO = MailBoxUtility.unmarshalFromJSON(jsonResponse,
				AddProfileToMailBoxResponseDTO.class);

		Assert.assertEquals(SUCCESS, mbProfileResponseDTO.getResponse().getStatus());
	}

	@Test
	public void testDeactivateProfileFromMailBox() throws LiaisonException, JSONException, IOException, JAXBException {

		// Add the Mailbox
		AddMailBoxResponseDTO responseDTO = createMailBox();
		Assert.assertEquals(SUCCESS, responseDTO.getResponse().getStatus());

		// Add the Profile
		AddProfileResponseDTO profileResponseDTO = addProfile();
		Assert.assertEquals(SUCCESS, profileResponseDTO.getResponse().getStatus());

		// Create Link
		String addProfile = "/" + responseDTO.getMailBox().getGuid() + "/profile/" + profileResponseDTO.getProfile().getGuId();
		HTTPRequest profileRequest = constructHTTPRequest(getBASE_URL() + addProfile, HTTP_METHOD.POST, jsonRequest,
				LoggerFactory.getLogger(MailBoxProfileServiceTest.class));
		profileRequest.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		AddProfileToMailBoxResponseDTO mbProfileResponseDTO = MailBoxUtility.unmarshalFromJSON(jsonResponse,
				AddProfileToMailBoxResponseDTO.class);
		Assert.assertEquals(SUCCESS, mbProfileResponseDTO.getResponse().getStatus());

		// Deactivate Link
		String deactivateProfile = "/" + responseDTO.getMailBox().getGuid() + "/profile/"
				+ mbProfileResponseDTO.getMailboxProfileLinkGuid();
		HTTPRequest deactivateRequest = constructHTTPRequest(getBASE_URL() + deactivateProfile, HTTP_METHOD.DELETE, null,
				LoggerFactory.getLogger(MailBoxProfileServiceTest.class));
		deactivateRequest.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		DeactivateMailboxProfileLinkResponseDTO deactivateResponseDTO = MailBoxUtility.unmarshalFromJSON(jsonResponse,
				DeactivateMailboxProfileLinkResponseDTO.class);
		Assert.assertEquals(SUCCESS, deactivateResponseDTO.getResponse().getStatus());
	}

	private AddMailBoxResponseDTO createMailBox() throws JAXBException, JsonParseException, JsonMappingException, IOException,
			JsonGenerationException, MalformedURLException, FileNotFoundException, LiaisonException {

		jsonRequest = ServiceUtils.readFileFromClassPath("requests/mailbox/addmailboxrequest.json");
		AddMailboxRequestDTO requestDTO = MailBoxUtility.unmarshalFromJSON(jsonRequest, AddMailboxRequestDTO.class);

		MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
		requestDTO.setMailBox(mbxDTO);

		jsonRequest = MailBoxUtility.marshalToJSON(requestDTO);
		request = constructHTTPRequest(getBASE_URL(), HTTP_METHOD.POST, jsonRequest, logger);
		request.execute();
		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		AddMailBoxResponseDTO responseDTO = MailBoxUtility.unmarshalFromJSON(jsonResponse, AddMailBoxResponseDTO.class);
		Assert.assertEquals(SUCCESS, responseDTO.getResponse().getStatus());
		return responseDTO;
	}

	private AddProfileResponseDTO addProfile() throws JAXBException, JsonGenerationException, JsonMappingException, IOException,
			MalformedURLException, FileNotFoundException, LiaisonException, JsonParseException {

		String profileName = "once" + System.currentTimeMillis();
		ProfileDTO profile = new ProfileDTO();
		profile.setName(profileName);
		AddProfileRequestDTO profileRequstDTO = new AddProfileRequestDTO();
		profileRequstDTO.setProfile(profile);

		jsonRequest = MailBoxUtility.marshalToJSON(profileRequstDTO);
		request = constructHTTPRequest(getBASE_URL() + "/profile", HTTP_METHOD.POST, jsonRequest, logger);
		request.execute();
		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		AddProfileResponseDTO profileResponseDTO = MailBoxUtility.unmarshalFromJSON(jsonResponse, AddProfileResponseDTO.class);
		Assert.assertEquals(SUCCESS, profileResponseDTO.getResponse().getStatus());
		return profileResponseDTO;
	}
}
