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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.liaison.commons.exception.LiaisonException;
import com.liaison.commons.util.client.http.HTTPRequest;
import com.liaison.commons.util.client.http.HTTPRequest.HTTP_METHOD;
import com.liaison.mailbox.service.base.test.BaseServiceTest;
import com.liaison.mailbox.service.dto.configuration.ProfileDTO;
import com.liaison.mailbox.service.dto.configuration.request.AddProfileRequestDTO;
import com.liaison.mailbox.service.dto.configuration.response.AddProfileResponseDTO;
import com.liaison.mailbox.service.dto.ui.GetProfileResponseDTO;
import com.liaison.mailbox.service.util.MailBoxUtil;

/**
 * Test class which tests the mailbox profile operations.
 */
public class MailBoxProfileResourceIT extends BaseServiceTest {

	private Logger logger = LogManager.getLogger(MailBoxProfileResourceIT.class);
	private HTTPRequest request;
	private String jsonRequest;
	private String jsonResponse;
    
	/**
	 * Method to test Profile.
	 */
	@Test
	public void testCreateProfile() throws Exception {

		AddProfileResponseDTO profileResponseDTO = addProfile("once" + System.currentTimeMillis());
		Assert.assertEquals(SUCCESS, profileResponseDTO.getResponse().getStatus());
	}
    
	/**
	 * Method to test profile with profile name as null.
	 */
	@Test
	public void testCreateProfile_WithNullasProfileName() throws Exception {

		AddProfileResponseDTO profileResponseDTO = addProfile(null);
		Assert.assertEquals(FAILURE, profileResponseDTO.getResponse().getStatus());
	}
    
	/**
	 * Method to test profile with duplicate data. 
	 */
	@Test
	public void testCreateProfile_WithDuplicates() throws Exception {

		addProfile("OnceIn15Mins");
		AddProfileResponseDTO dupProfileResponseDTO = addProfile("OnceIn15Mins");
		Assert.assertEquals(FAILURE, dupProfileResponseDTO.getResponse().getStatus());
	}
    
	/**
	 * Method to test profile with profile name as empty.
	 */
	@Test
	public void testCreateProfile_WithProfileNameEmpty() throws Exception {

		AddProfileResponseDTO profileResponseDTO = addProfile("");
		Assert.assertEquals(FAILURE, profileResponseDTO.getResponse().getStatus());
	}
    
	/**
	 * Method to test profile with invalid profile name.
	 */
	@Test
	public void testCreateProfile_WithProfileNameAsSpecialCharacter() throws Exception {

		AddProfileResponseDTO profileResponseDTO = addProfile(System.nanoTime() + "#$%^%&@");
		Assert.assertEquals(SUCCESS, profileResponseDTO.getResponse().getStatus());
	}
	
	/**
	 * Method to test readProfile.
	 */
	@Test
	public void testReadProfile() throws Exception {
		
		//add profile
	    AddProfileResponseDTO profileResponseDTO = addProfile("once" + System.currentTimeMillis());
	    Assert.assertEquals(SUCCESS, profileResponseDTO.getResponse().getStatus());
		
	   //Read Profile
		request = constructHTTPRequest(getBASE_URL() + "/profile", HTTP_METHOD.GET, null, logger);
		request.execute();
		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);
	    
		GetProfileResponseDTO getProfileResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, GetProfileResponseDTO.class);
		Assert.assertNotNull(getProfileResponseDTO, null);
		Assert.assertEquals(SUCCESS, getProfileResponseDTO.getResponse().getStatus());
	}
	
	/**
	 * Method to test search profile With valid profile name.
	 */
	@Test(enabled=false)
	public void testSearchProfile() throws Exception {
		
		//add profile
		ProfileDTO profile = new ProfileDTO();
		profile.setName("once" + System.currentTimeMillis());
		AddProfileRequestDTO profileRequstDTO = new AddProfileRequestDTO();
		profileRequstDTO.setProfile(profile);

		jsonRequest = MailBoxUtil.marshalToJSON(profileRequstDTO);
		request = constructHTTPRequest(getBASE_URL() + "/profile", HTTP_METHOD.POST, jsonRequest, logger);
		request.execute();
		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		AddProfileResponseDTO profileResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, AddProfileResponseDTO.class);
		 Assert.assertEquals(SUCCESS, profileResponseDTO.getResponse().getStatus());
		 
	    //searchProfile.
	    GetProfileResponseDTO getProfileResponseDTO = searchProfile(profileRequstDTO.getProfile().getName());
	    Assert.assertNotNull(getProfileResponseDTO, null);
	    Assert.assertEquals(SUCCESS, getProfileResponseDTO.getResponse().getStatus());
	}
	
	/**
	 * Method to test search profile with profile name as null.
	 */
	@Test(enabled=false)
	public void testSearchProfile_WithNullasProfileName() throws Exception {

		GetProfileResponseDTO getProfileResponseDTO = searchProfile(null);
	    Assert.assertEquals(FAILURE, getProfileResponseDTO.getResponse().getStatus());
	}
    
	/**
	 * Method to test search profile with invalid profile name.
	 */
	@Test(enabled=false)
	public void testSearchProfile_InvalidProfileName() throws Exception {

		GetProfileResponseDTO getProfileResponseDTO = searchProfile(System.nanoTime() + "INVALID_PROFILE_NAME");
	    Assert.assertEquals(FAILURE, getProfileResponseDTO.getResponse().getStatus());
	}
	
	/**
	 * Method to constructs profile.
	 * 
	 * @param profileName
	 * @return AddProfileResponseDTO
	 */
	private AddProfileResponseDTO addProfile(String profileName) throws Exception {

		ProfileDTO profile = new ProfileDTO();
		profile.setName(profileName);
		AddProfileRequestDTO profileRequstDTO = new AddProfileRequestDTO();
		profileRequstDTO.setProfile(profile);

		jsonRequest = MailBoxUtil.marshalToJSON(profileRequstDTO);
		request = constructHTTPRequest(getBASE_URL() + "/profile", HTTP_METHOD.POST, jsonRequest, logger);
		request.execute();
		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		AddProfileResponseDTO profileResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, AddProfileResponseDTO.class);

		return profileResponseDTO;
	}
	
	/**
	 * Method to Retrieve profile by given name.
	 * @return GetProfileResponseDTO
	 * @throws Exception
	 */
	private GetProfileResponseDTO searchProfile(String profileName) throws Exception {
		
		String filterText = "{filterText:[{field:name,text:" + profileName + "}]}";
	    String url = getBASE_URL() + "/profile?filterText=" + filterText;
		request = constructHTTPRequest(url, HTTP_METHOD.GET, null, logger);
		request.execute();
		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		GetProfileResponseDTO profileResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, GetProfileResponseDTO.class);

		return profileResponseDTO;		
	}

}
