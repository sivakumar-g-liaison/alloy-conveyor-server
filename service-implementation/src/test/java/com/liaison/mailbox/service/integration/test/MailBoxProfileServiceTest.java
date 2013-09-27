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
	public void testCreateProfile() throws JsonGenerationException, JsonMappingException, JsonParseException,
			MalformedURLException, FileNotFoundException, JAXBException, IOException, LiaisonException {

		AddProfileResponseDTO profileResponseDTO = addProfile("once" + System.currentTimeMillis());
		Assert.assertEquals(SUCCESS, profileResponseDTO.getResponse().getStatus());
	}

	@Test
	public void testCreateProfile_WithNullasProfileName() throws JsonGenerationException, JsonMappingException,
			JsonParseException, MalformedURLException, FileNotFoundException, JAXBException, IOException, LiaisonException {

		AddProfileResponseDTO profileResponseDTO = addProfile(null);
		Assert.assertEquals(FAILURE, profileResponseDTO.getResponse().getStatus());
	}

	@Test
	public void testCreateProfile_WithDuplicates() throws JsonGenerationException, JsonMappingException, JsonParseException,
			MalformedURLException, FileNotFoundException, JAXBException, IOException, LiaisonException {

		addProfile("OnceIn15Mins");
		AddProfileResponseDTO dupProfileResponseDTO = addProfile("OnceIn15Mins");
		Assert.assertEquals(FAILURE, dupProfileResponseDTO.getResponse().getStatus());
	}

	@Test
	public void testCreateProfile_WithProfileNameEmpty() throws JsonGenerationException, JsonMappingException,
			JsonParseException, MalformedURLException, FileNotFoundException, JAXBException, IOException, LiaisonException {

		AddProfileResponseDTO profileResponseDTO = addProfile("");
		Assert.assertEquals(FAILURE, profileResponseDTO.getResponse().getStatus());
	}

	@Test
	public void testCreateProfile_WithProfileNameAsSpecialCharacter() throws JsonGenerationException, JsonMappingException,
			JsonParseException, MalformedURLException, FileNotFoundException, JAXBException, IOException, LiaisonException {

		AddProfileResponseDTO profileResponseDTO = addProfile("@#$%$!@");
		Assert.assertEquals(FAILURE, profileResponseDTO.getResponse().getStatus());
	}

	@Test
	public void testAddProfileToMailBox() throws LiaisonException, JSONException, IOException, JAXBException {

		// Add the Mailbox
		AddMailBoxResponseDTO responseDTO = createMailBox();
		Assert.assertEquals(SUCCESS, responseDTO.getResponse().getStatus());

		// Add the Profile
		AddProfileResponseDTO profileResponseDTO = addProfile("once" + System.currentTimeMillis());
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
	public void testAddProfileToMailBox_InvalidMailBoxId() throws LiaisonException, JSONException, IOException, JAXBException {

		// Add the Mailbox
		AddMailBoxResponseDTO responseDTO = createMailBox();
		Assert.assertEquals(SUCCESS, responseDTO.getResponse().getStatus());

		// Add the Profile
		AddProfileResponseDTO profileResponseDTO = addProfile("once" + System.currentTimeMillis());
		Assert.assertEquals(SUCCESS, profileResponseDTO.getResponse().getStatus());

		// Create Link
		String addProfile = "/" + "142DERFDFSWWEE6" + "/profile/" + profileResponseDTO.getProfile().getGuId();
		HTTPRequest profileRequest = constructHTTPRequest(getBASE_URL() + addProfile, HTTP_METHOD.POST, jsonRequest,
				LoggerFactory.getLogger(MailBoxProfileServiceTest.class));
		profileRequest.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		AddProfileToMailBoxResponseDTO mbProfileResponseDTO = MailBoxUtility.unmarshalFromJSON(jsonResponse,
				AddProfileToMailBoxResponseDTO.class);

		Assert.assertEquals(FAILURE, mbProfileResponseDTO.getResponse().getStatus());
	}

	@Test
	public void testAddProfileToMailBox_InvalidProfileId() throws LiaisonException, JSONException, IOException, JAXBException {

		// Add the Mailbox
		AddMailBoxResponseDTO responseDTO = createMailBox();
		Assert.assertEquals(SUCCESS, responseDTO.getResponse().getStatus());

		// Add the Profile
		AddProfileResponseDTO profileResponseDTO = addProfile("once" + System.currentTimeMillis());
		Assert.assertEquals(SUCCESS, profileResponseDTO.getResponse().getStatus());

		// Create Link
		String addProfile = "/" + responseDTO.getMailBox().getGuid() + "/profile/" + "DWDDFEEFF1542";
		HTTPRequest profileRequest = constructHTTPRequest(getBASE_URL() + addProfile, HTTP_METHOD.POST, jsonRequest,
				LoggerFactory.getLogger(MailBoxProfileServiceTest.class));
		profileRequest.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		AddProfileToMailBoxResponseDTO mbProfileResponseDTO = MailBoxUtility.unmarshalFromJSON(jsonResponse,
				AddProfileToMailBoxResponseDTO.class);

		Assert.assertEquals(FAILURE, mbProfileResponseDTO.getResponse().getStatus());
	}

	@Test
	public void testAddProfileToMailBox_InvalidCharacters() throws LiaisonException, JSONException, IOException, JAXBException {

		// Add the Mailbox
		AddMailBoxResponseDTO responseDTO = createMailBox();
		Assert.assertEquals(SUCCESS, responseDTO.getResponse().getStatus());

		// Add the Profile
		AddProfileResponseDTO profileResponseDTO = addProfile("once" + System.currentTimeMillis());
		Assert.assertEquals(SUCCESS, profileResponseDTO.getResponse().getStatus());

		// Create Link
		String addProfile = "/" + "!452$SDF" + "/profile/" + "!@#1425";
		HTTPRequest profileRequest = constructHTTPRequest(getBASE_URL() + addProfile, HTTP_METHOD.POST, jsonRequest,
				LoggerFactory.getLogger(MailBoxProfileServiceTest.class));
		profileRequest.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		AddProfileToMailBoxResponseDTO mbProfileResponseDTO = MailBoxUtility.unmarshalFromJSON(jsonResponse,
				AddProfileToMailBoxResponseDTO.class);

		Assert.assertEquals(FAILURE, mbProfileResponseDTO.getResponse().getStatus());
	}

	@Test
	public void testAddProfileToMailBox_NullValues() throws LiaisonException, JSONException, IOException, JAXBException {

		// Add the Mailbox
		AddMailBoxResponseDTO responseDTO = createMailBox();
		Assert.assertEquals(SUCCESS, responseDTO.getResponse().getStatus());

		// Add the Profile
		AddProfileResponseDTO profileResponseDTO = addProfile("once" + System.currentTimeMillis());
		Assert.assertEquals(SUCCESS, profileResponseDTO.getResponse().getStatus());

		// Create Link
		String addProfile = "/" + null + "/profile/" + null;
		HTTPRequest profileRequest = constructHTTPRequest(getBASE_URL() + addProfile, HTTP_METHOD.POST, jsonRequest,
				LoggerFactory.getLogger(MailBoxProfileServiceTest.class));
		profileRequest.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		AddProfileToMailBoxResponseDTO mbProfileResponseDTO = MailBoxUtility.unmarshalFromJSON(jsonResponse,
				AddProfileToMailBoxResponseDTO.class);

		Assert.assertEquals(FAILURE, mbProfileResponseDTO.getResponse().getStatus());
	}

	@Test(expected = JsonParseException.class)
	public void testAddProfileToMailBox_EmptyValues_ShouldThrowException() throws LiaisonException, JSONException, IOException,
			JAXBException {

		// Add the Mailbox
		AddMailBoxResponseDTO responseDTO = createMailBox();
		Assert.assertEquals(SUCCESS, responseDTO.getResponse().getStatus());

		// Add the Profile
		AddProfileResponseDTO profileResponseDTO = addProfile("once" + System.currentTimeMillis());
		Assert.assertEquals(SUCCESS, profileResponseDTO.getResponse().getStatus());

		// Create Link
		String addProfile = "/" + "" + "/profile/" + "";
		HTTPRequest profileRequest = constructHTTPRequest(getBASE_URL() + addProfile, HTTP_METHOD.POST, jsonRequest,
				LoggerFactory.getLogger(MailBoxProfileServiceTest.class));
		profileRequest.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		AddProfileToMailBoxResponseDTO mbProfileResponseDTO = MailBoxUtility.unmarshalFromJSON(jsonResponse,
				AddProfileToMailBoxResponseDTO.class);
	}

	@Test
	public void testAddProfileToMailBox_DuplicateLinks() throws LiaisonException, JSONException, IOException, JAXBException {

		// Add the Mailbox
		AddMailBoxResponseDTO responseDTO = createMailBox();
		Assert.assertEquals(SUCCESS, responseDTO.getResponse().getStatus());

		// Add the Profile
		AddProfileResponseDTO profileResponseDTO = addProfile("once" + System.currentTimeMillis());
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

		// Create Link
		String addDupProfile = "/" + responseDTO.getMailBox().getGuid() + "/profile/" + profileResponseDTO.getProfile().getGuId();
		HTTPRequest dupProfileRequest = constructHTTPRequest(getBASE_URL() + addDupProfile, HTTP_METHOD.POST, jsonRequest,
				LoggerFactory.getLogger(MailBoxProfileServiceTest.class));
		dupProfileRequest.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		AddProfileToMailBoxResponseDTO mbDubProfileResponseDTO = MailBoxUtility.unmarshalFromJSON(jsonResponse,
				AddProfileToMailBoxResponseDTO.class);

		Assert.assertEquals(SUCCESS, mbDubProfileResponseDTO.getResponse().getStatus());
	}

	@Test
	public void testDeactivateProfileFromMailBox() throws LiaisonException, JSONException, IOException, JAXBException {

		// Add the Mailbox
		AddMailBoxResponseDTO responseDTO = createMailBox();
		Assert.assertEquals(SUCCESS, responseDTO.getResponse().getStatus());

		// Add the Profile
		AddProfileResponseDTO profileResponseDTO = addProfile("once" + System.currentTimeMillis());
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

	@Test
	public void testDeactivateProfileFromMailBox_Invalid_MailBoxId() throws LiaisonException, JSONException, IOException,
			JAXBException {

		// Add the Mailbox
		AddMailBoxResponseDTO responseDTO = createMailBox();
		Assert.assertEquals(SUCCESS, responseDTO.getResponse().getStatus());

		// Add the Profile
		AddProfileResponseDTO profileResponseDTO = addProfile("once" + System.currentTimeMillis());
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
		String deactivateProfile = "/" + "1425633662225144" + "/profile/" + mbProfileResponseDTO.getMailboxProfileLinkGuid();
		HTTPRequest deactivateRequest = constructHTTPRequest(getBASE_URL() + deactivateProfile, HTTP_METHOD.DELETE, null,
				LoggerFactory.getLogger(MailBoxProfileServiceTest.class));
		deactivateRequest.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		DeactivateMailboxProfileLinkResponseDTO deactivateResponseDTO = MailBoxUtility.unmarshalFromJSON(jsonResponse,
				DeactivateMailboxProfileLinkResponseDTO.class);
		Assert.assertEquals(FAILURE, deactivateResponseDTO.getResponse().getStatus());
	}

	@Test
	public void testDeactivateProfileFromMailBox_ShouldThrowException() throws LiaisonException, JSONException, IOException,
			JAXBException {

		// Add the Mailbox
		AddMailBoxResponseDTO responseDTO = createMailBox();
		Assert.assertEquals(SUCCESS, responseDTO.getResponse().getStatus());

		// Add the Profile
		AddProfileResponseDTO profileResponseDTO = addProfile("once" + System.currentTimeMillis());
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
		String deactivateProfile = "/" + "1425633662225144" + "/profile/" + mbProfileResponseDTO.getMailboxProfileLinkGuid();
		HTTPRequest deactivateRequest = constructHTTPRequest(getBASE_URL() + deactivateProfile, HTTP_METHOD.DELETE, null,
				LoggerFactory.getLogger(MailBoxProfileServiceTest.class));
		deactivateRequest.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		DeactivateMailboxProfileLinkResponseDTO deactivateResponseDTO = MailBoxUtility.unmarshalFromJSON(jsonResponse,
				DeactivateMailboxProfileLinkResponseDTO.class);
		Assert.assertEquals(FAILURE, deactivateResponseDTO.getResponse().getStatus());
	}

	private AddMailBoxResponseDTO createMailBox() throws JAXBException, JsonParseException, JsonMappingException, IOException,
			JsonGenerationException, MalformedURLException, FileNotFoundException, LiaisonException {

		AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
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

	private AddProfileResponseDTO addProfile(String profileName) throws JAXBException, JsonGenerationException,
			JsonMappingException, IOException, MalformedURLException, FileNotFoundException, LiaisonException, JsonParseException {

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

		return profileResponseDTO;
	}

}
