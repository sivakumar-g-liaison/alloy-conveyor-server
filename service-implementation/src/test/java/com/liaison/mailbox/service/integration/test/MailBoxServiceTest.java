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

import junit.framework.Assert;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jettison.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liaison.commons.exceptions.LiaisonException;
import com.liaison.commons.util.client.http.HTTPRequest;
import com.liaison.commons.util.client.http.HTTPRequest.HTTP_METHOD;
import com.liaison.framework.util.ServiceUtils;
import com.liaison.mailbox.service.base.test.BaseServiceTest;
import com.liaison.mailbox.service.dto.configuration.ProfileDTO;
import com.liaison.mailbox.service.dto.configuration.request.AddProcessorToMailboxRequestDTO;
import com.liaison.mailbox.service.dto.configuration.request.AddProfileRequestDTO;
import com.liaison.mailbox.service.dto.configuration.response.AddMailBoxResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.AddProfileResponseDTO;
import com.liaison.mailbox.service.util.MailBoxUtility;

/**
 * Test class which tests the mailbox functional services.
 * 
 * @author praveenu
 */
public class MailBoxServiceTest extends BaseServiceTest {

	private Logger logger = null;

	private HTTPRequest request;
	private String jsonRequest;
	private String jsonResponse;

	@Before
	public void setUp() throws Exception {
		logger = LoggerFactory.getLogger(MailBoxServiceTest.class);
	}

	@Test
	public void testTriggerProfile() throws LiaisonException, JSONException, JsonParseException, JsonMappingException,
			JAXBException, IOException {

		// Add the Mailbox
		jsonRequest = ServiceUtils.readFileFromClassPath("requests/mailbox/addmailboxrequest.json");

		request = constructHTTPRequest(getBASE_URL(), HTTP_METHOD.POST, jsonRequest, logger);
		request.execute();
		Assert.assertEquals(SUCCESS, getResponseStatus(getOutput().toString(), "addMailBoxResponse"));

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);
		AddMailBoxResponseDTO responseDTO = MailBoxUtility.unmarshalFromJSON(jsonResponse, AddMailBoxResponseDTO.class);
		Assert.assertEquals(SUCCESS, responseDTO.getResponse().getStatus());

		// Add the Profile
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

		// Add the Processor
		String jsonRequest = ServiceUtils.readFileFromClassPath("requests/processor/createprocessorfortriggerprofile.json");
		AddProcessorToMailboxRequestDTO addProcessorDTO = MailBoxUtility.unmarshalFromJSON(jsonRequest,
				AddProcessorToMailboxRequestDTO.class);

		addProcessorDTO.getProcessor().setLinkedMailboxId(responseDTO.getMailBox().getGuid());
		addProcessorDTO.getProcessor().getLinkedProfiles().add(profileName);

		jsonRequest = MailBoxUtility.marshalToJSON(addProcessorDTO);

		String addProcessor = "/" + responseDTO.getMailBox().getGuid() + "/processor";
		request = constructHTTPRequest(getBASE_URL() + addProcessor, HTTP_METHOD.POST, jsonRequest, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		Assert.assertEquals(true, getResponseStatus(jsonResponse, "addProcessorToMailBoxResponse").equals(SUCCESS));

		// Trigger the profile
		String triggerProfile = "/triggerProfile" + "?name=" + profileName;
		request = constructHTTPRequest(getBASE_URL() + triggerProfile, HTTP_METHOD.POST, null, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		Assert.assertEquals(true, getResponseStatus(jsonResponse, "triggerProfileResponse").equals(SUCCESS));

	}

}
