/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall 
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.services.unit.test;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.liaison.framework.util.ServiceUtils;
import com.liaison.mailbox.enums.Messages;

/**
 * Unit test cases for mailbox profile operations.
 * 
 * @author OFS
 */
public class MailBoxProfileUtilityTest {

	private String data;
	private JSONObject jsonMaiboxProfile;

	@BeforeMethod
	public void setUp() throws JSONException {

		data = ServiceUtils.readFileFromClassPath("requests/profile/profile.json");
		jsonMaiboxProfile = new JSONObject(data);
	}

	@AfterMethod
	public void tearDown() {

	}

	@Test
	public void addProfileToMailBoxRequestTest_ShouldReturn() throws JsonParseException, JsonMappingException, JAXBException,
			IOException, JSONException {

		/* JSONObject jsonAddProfile =
		 * jsonMaiboxProfile.getJSONObject("addProfileToMailBoxRequest"); JSONObject jsonProfile =
		 * jsonAddProfile.getJSONObject("profile");
		 * 
		 * AddProfileToMailBoxRequestDTO serviceRequest = MailBoxUtil .unmarshalFromJSON(data,
		 * AddProfileToMailBoxRequestDTO.class);
		 * 
		 * Assert.assertNotNull(serviceRequest.getMailBoxGuid()); Assert.assertEquals(true,
		 * serviceRequest.getMailBoxGuid().equals(jsonAddProfile.getString("mailBoxGuid")));
		 * 
		 * Assert.assertNotNull(serviceRequest.getProfile().getId()); Assert.assertEquals(true,
		 * serviceRequest.getProfile().getId().equals(jsonProfile.getString("id")));
		 * 
		 * Assert.assertNotNull(serviceRequest.getProfile().getName()); Assert.assertEquals(true,
		 * serviceRequest.getProfile().getName().equals(jsonProfile.getString("name")));
		 * 
		 * Assert.assertNotNull(serviceRequest.getStatus()); Assert.assertEquals(true,
		 * serviceRequest.getStatus().equals(jsonAddProfile.getString("status"))); */

	}
	
    /**
     * Method to test string formation.
     */
	@Test
	public void stringFormatTest() {
		String data = String.format(Messages.CREATE_OPERATION_FAILED.value(), "test");
		System.out.println(data);
	}
}
