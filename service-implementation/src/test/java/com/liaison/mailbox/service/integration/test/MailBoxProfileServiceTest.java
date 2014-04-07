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
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liaison.commons.exception.LiaisonException;
import com.liaison.commons.util.client.http.HTTPRequest;
import com.liaison.commons.util.client.http.HTTPRequest.HTTP_METHOD;
import com.liaison.mailbox.service.base.test.BaseServiceTest;
import com.liaison.mailbox.service.dto.configuration.ProfileDTO;
import com.liaison.mailbox.service.dto.configuration.request.AddProfileRequestDTO;
import com.liaison.mailbox.service.dto.configuration.response.AddProfileResponseDTO;
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

		AddProfileResponseDTO profileResponseDTO = addProfile(System.nanoTime() + "#$%^%&@");
		Assert.assertEquals(SUCCESS, profileResponseDTO.getResponse().getStatus());
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
