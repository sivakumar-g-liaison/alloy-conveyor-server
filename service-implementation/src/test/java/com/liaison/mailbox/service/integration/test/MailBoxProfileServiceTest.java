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
import com.liaison.commons.util.client.http.HTTPResponse;
import com.liaison.framework.util.ServiceUtils;

/**
 * @author praveenu
 * 
 */
public class MailBoxProfileServiceTest extends BaseServiceTest {

	private String mailBoxId;
	private BaseServiceTest baseServiceTest;

	@Override
	public void initialSetUp() throws FileNotFoundException, IOException {
		super.initialSetUp();
		baseServiceTest = new BaseServiceTest();
		baseServiceTest.initialSetUp();
	}

	@Test
	public void addProfileToMailBox() throws LiaisonException, JSONException, IOException {

		String jsonString = ServiceUtils.readFileFromClassPath("profile.json");
		JSONObject jsonMaiboxProfile = new JSONObject(jsonString);
		JSONObject jsonAddProfile = jsonMaiboxProfile.getJSONObject("addprofiletomailboxrequest");

		mailBoxId = jsonAddProfile.getString("mailBoxGuid");

		String addProfile = "/" + mailBoxId + "/profile";
		HTTPRequest request = baseServiceTest.httpRequest(baseServiceTest.getBASE_URL() + addProfile, HTTP_METHOD.POST,
				jsonString, LoggerFactory.getLogger(MailBoxProfileServiceTest.class));
		HTTPResponse response = request.execute();
		Assert.assertEquals(true, response.getStatusCode() == 200);
	}

	@Test
	public void deactivateProfileFromMailBox() throws LiaisonException, JSONException, IOException {

		JSONObject jsonMaiboxProfile = new JSONObject(baseServiceTest.getOutput().toString());
		JSONObject jsonAddProfileresponse = jsonMaiboxProfile.getJSONObject("addprofiletomailboxresponse");

		String addProfile = "/" + mailBoxId + "/profile/" + jsonAddProfileresponse.getString("mailboxProfileLinkGuid");
		HTTPRequest request = baseServiceTest.httpRequest(baseServiceTest.getBASE_URL() + addProfile, HTTP_METHOD.DELETE, null,
				LoggerFactory.getLogger(MailBoxProfileServiceTest.class));
		HTTPResponse response = request.execute();

		Assert.assertEquals(true, response.getStatusCode() == 200);
	}
}
