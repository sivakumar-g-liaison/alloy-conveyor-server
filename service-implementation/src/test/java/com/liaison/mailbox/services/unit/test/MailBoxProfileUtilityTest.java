package com.liaison.mailbox.services.unit.test;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.liaison.framework.util.ServiceUtils;
import com.liaison.mailbox.enums.Messages;

/**
 * Unit test cases for mailbox profile operations.
 * 
 * @author praveenu
 */
public class MailBoxProfileUtilityTest {

	private String data;
	private JSONObject jsonMaiboxProfile;

	@Before
	public void setUp() throws JSONException {

		data = ServiceUtils.readFileFromClassPath("requests/profile/profile.json");
		jsonMaiboxProfile = new JSONObject(data);
	}

	@After
	public void tearDown() {

	}

	@Test
	public void addProfileToMailBoxRequestTest_ShouldReturn() throws JsonParseException, JsonMappingException, JAXBException,
			IOException, JSONException {

		/* JSONObject jsonAddProfile =
		 * jsonMaiboxProfile.getJSONObject("addProfileToMailBoxRequest"); JSONObject jsonProfile =
		 * jsonAddProfile.getJSONObject("profile");
		 * 
		 * AddProfileToMailBoxRequestDTO serviceRequest = MailBoxUtility .unmarshalFromJSON(data,
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

	@Test
	public void stringFormatTest() {
		String data = String.format(Messages.CREATE_OPERATION_FAILED.value(), "test");
		System.out.println(data);
	}
}
