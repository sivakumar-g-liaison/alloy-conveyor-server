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

import junit.framework.Assert;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import com.liaison.commons.exceptions.LiaisonException;
import com.liaison.commons.util.client.http.HTTPRequest;
import com.liaison.commons.util.client.http.HTTPRequest.HTTP_METHOD;
import com.liaison.framework.util.ServiceUtils;
import com.liaison.mailbox.service.base.test.BaseServiceTest;

/**
 * Test class which tests the mailbox profile operations.
 * 
 * @author praveenu
 */
public class MailBoxProfileServiceTest extends BaseServiceTest {

	private static String mailBoxId;

	@Override
	public void initialSetUp() throws FileNotFoundException, IOException {
		super.initialSetUp();
	}

	@Test
	public void addProfileToMailBox() throws LiaisonException, JSONException, IOException {

		// reads the addprofile input from JSON file
		String jsonString = ServiceUtils.readFileFromClassPath("requests/profile/profile.json");

		// Convert string to JSON to fetch the MailBoxId from string
		JSONObject jsonAddProfile = getRequestJson(jsonString, "addProfileToMailBoxRequest");
		mailBoxId = jsonAddProfile.getString("mailBoxGuid");

		// URL construction & HTTPRequest execution
		String addProfile = "/" + mailBoxId + "/profile";
		HTTPRequest request = constructHTTPRequest(getBASE_URL() + addProfile, HTTP_METHOD.POST, jsonString,
				LoggerFactory.getLogger(MailBoxProfileServiceTest.class));
		request.execute();

		// JSON decoding to obtain status of the request
		String status = getResponseStatus(getOutput().toString(), "addProfileToMailBoxResponse");

		Assert.assertEquals(true, status.equals("Success"));
	}

	@Test
	public void deactivateProfileFromMailBox() throws LiaisonException, JSONException, IOException {

		String jsonString = ServiceUtils.readFileFromClassPath("requests/profile/profile.json");

		JSONObject jsonAddProfile = getRequestJson(jsonString, "addProfileToMailBoxRequest");
		mailBoxId = jsonAddProfile.getString("mailBoxGuid");

		String addProfile = "/" + mailBoxId + "/profile";

		HTTPRequest addRequest = constructHTTPRequest(getBASE_URL() + addProfile, HTTP_METHOD.POST, jsonString,
				LoggerFactory.getLogger(MailBoxProfileServiceTest.class));
		addRequest.execute();

		JSONObject jsonMaiboxProfile = new JSONObject(getOutput().toString());
		JSONObject jsonAddProfileresponse = jsonMaiboxProfile.getJSONObject("addProfileToMailBoxResponse");

		String deactivateProfile = addProfile + "/" + jsonAddProfileresponse.getString("mailboxProfileLinkGuid");
		HTTPRequest deactivateRequest = constructHTTPRequest(getBASE_URL() + deactivateProfile, HTTP_METHOD.DELETE, null,
				LoggerFactory.getLogger(MailBoxProfileServiceTest.class));
		deactivateRequest.execute();

		String status = getResponseStatus(getOutput().toString(), "deactivateMailBoxProfileLink");

		Assert.assertEquals(true, status.equals("Success"));
	}
}
