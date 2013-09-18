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

import javax.xml.bind.JAXBException;

import junit.framework.Assert;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liaison.commons.exceptions.LiaisonException;
import com.liaison.commons.util.client.http.HTTPRequest;
import com.liaison.commons.util.client.http.HTTPRequest.HTTP_METHOD;
import com.liaison.framework.util.ServiceUtils;
import com.liaison.mailbox.service.base.test.BaseServiceTest;
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
		jsonRequest = ServiceUtils.readFileFromClassPath("requests/mailbox/addmailboxrequest.json");

		request = constructHTTPRequest(getBASE_URL(), HTTP_METHOD.POST, jsonRequest, logger);
		request.execute();
		Assert.assertEquals(SUCCESS, getResponseStatus(getOutput().toString(), "addMailBoxResponse"));

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);
		AddMailBoxResponseDTO responseDTO = MailBoxUtility.unmarshalFromJSON(jsonResponse, AddMailBoxResponseDTO.class);
		Assert.assertEquals(SUCCESS, responseDTO.getResponse().getStatus());

		// Add the Profile
		jsonRequest = ServiceUtils.readFileFromClassPath("requests/profile/profile.json");

		request = constructHTTPRequest(getBASE_URL() + "/profile", HTTP_METHOD.POST, jsonRequest, logger);
		request.execute();
		Assert.assertEquals(SUCCESS, getResponseStatus(getOutput().toString(), "addProfileResponse"));

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);
		AddProfileResponseDTO profileResponseDTO = MailBoxUtility.unmarshalFromJSON(jsonResponse, AddProfileResponseDTO.class);

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
		jsonRequest = ServiceUtils.readFileFromClassPath("requests/mailbox/addmailboxrequest.json");

		request = constructHTTPRequest(getBASE_URL(), HTTP_METHOD.POST, jsonRequest, logger);
		request.execute();
		Assert.assertEquals(SUCCESS, getResponseStatus(getOutput().toString(), "addMailBoxResponse"));

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);
		AddMailBoxResponseDTO responseDTO = MailBoxUtility.unmarshalFromJSON(jsonResponse, AddMailBoxResponseDTO.class);

		// Add the Profile
		jsonRequest = ServiceUtils.readFileFromClassPath("requests/profile/profile.json");

		request = constructHTTPRequest(getBASE_URL() + "/profile", HTTP_METHOD.POST, jsonRequest, logger);
		request.execute();
		Assert.assertEquals(SUCCESS, getResponseStatus(getOutput().toString(), "addProfileResponse"));

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);
		AddProfileResponseDTO profileResponseDTO = MailBoxUtility.unmarshalFromJSON(jsonResponse, AddProfileResponseDTO.class);

		String addProfile = "/" + responseDTO.getMailBox().getGuid() + "/profile/" + profileResponseDTO.getProfile().getGuId();
		HTTPRequest profileRequest = constructHTTPRequest(getBASE_URL() + addProfile, HTTP_METHOD.POST, jsonRequest,
				LoggerFactory.getLogger(MailBoxProfileServiceTest.class));
		profileRequest.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		AddProfileToMailBoxResponseDTO mbProfileResponseDTO = MailBoxUtility.unmarshalFromJSON(jsonResponse,
				AddProfileToMailBoxResponseDTO.class);

		Assert.assertEquals(SUCCESS, mbProfileResponseDTO.getResponse().getStatus());

		JSONObject jsonMaiboxProfile = new JSONObject(getOutput().toString());
		JSONObject jsonAddProfileresponse = jsonMaiboxProfile.getJSONObject("addProfileToMailBoxResponse");

		String deactivateProfile = "/" + responseDTO.getMailBox().getGuid() + "/profile/"
				+ jsonAddProfileresponse.getString("mailboxProfileLinkGuid");
		HTTPRequest deactivateRequest = constructHTTPRequest(getBASE_URL() + deactivateProfile, HTTP_METHOD.DELETE, null,
				LoggerFactory.getLogger(MailBoxProfileServiceTest.class));
		deactivateRequest.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		DeactivateMailboxProfileLinkResponseDTO deactivateResponseDTO = MailBoxUtility.unmarshalFromJSON(jsonResponse,
				DeactivateMailboxProfileLinkResponseDTO.class);
		Assert.assertEquals(SUCCESS, deactivateResponseDTO.getResponse().getStatus());
	}
}
