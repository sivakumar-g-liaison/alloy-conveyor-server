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
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.http.client.ClientProtocolException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jettison.json.JSONException;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.liaison.commons.exception.LiaisonException;
import com.liaison.commons.util.client.http.HTTPRequest;
import com.liaison.commons.util.client.http.HTTPRequest.HTTP_METHOD;
import com.liaison.framework.util.ServiceUtils;
import com.liaison.mailbox.enums.MailBoxStatus;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.service.base.test.BaseServiceTest;
import com.liaison.mailbox.service.dto.configuration.MailBoxDTO;
import com.liaison.mailbox.service.dto.configuration.ProcessorDTO;
import com.liaison.mailbox.service.dto.configuration.ProfileDTO;
import com.liaison.mailbox.service.dto.configuration.request.AddMailboxRequestDTO;
import com.liaison.mailbox.service.dto.configuration.request.AddProcessorToMailboxRequestDTO;
import com.liaison.mailbox.service.dto.configuration.request.AddProfileRequestDTO;
import com.liaison.mailbox.service.dto.configuration.request.ReviseMailBoxRequestDTO;
import com.liaison.mailbox.service.dto.configuration.response.AddMailBoxResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.AddProcessorToMailboxResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.AddProfileResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.GetMailBoxResponseDTO;
import com.liaison.mailbox.service.dto.ui.SearchMailBoxResponseDTO;
import com.liaison.mailbox.service.util.MailBoxUtil;

/**
 * Test class to test mailbox configuration service.
 * 
 * @author veerasamyn
 */
public class MailBoxConfigurationServiceTest extends BaseServiceTest {

	private Logger logger;
	private String jsonResponse;
	private String jsonRequest;
	private HTTPRequest request;
	private String serviceInstanceId = "9032A4910A0A52980A0EC676DB33A102";
	private String aclManifest = "eyAgDQogICAiZW52ZWxvcGUiOnsgIA0KICAgICAgImNyZWF0ZWREYXRlIjoiMjAxNC0wNC0yM1QxNjowNzoxOC0wNDAwIiwNCiAgICAgICJnbG9iYWxJZCI6IiIsDQogICAgICAicGFyZW50SWQiOiIiLA0KICAgICAgInByb2Nlc3NJZCI6IiINCiAgIH0sDQogICAicGxhdGZvcm0iOlsgIA0KICAgICAgeyAgDQogICAgICAgICAicGxhdGZvcm1OYW1lIjoiU0VSVklDRV9CUk9LRVIiLA0KICAgICAgICAgInJvbGVCYXNlZEFjY2Vzc0NvbnRyb2wiOlsgIA0KICAgICAgICAgICAgeyAgDQogICAgICAgICAgICAgICAiZG9tYWluTmFtZSI6IlNFUlZJQ0VfQlJPS0VSIiwNCiAgICAgICAgICAgICAgICJkb21haW5UeXBlIjoiU09BX1NFUlZJQ0UiLA0KICAgICAgICAgICAgICAgInJvbGUiOlsgIA0KICAgICAgICAgICAgICAgICAgIk1haWxib3hBZG1pbiINCiAgICAgICAgICAgICAgIF0sDQogICAgICAgICAgICAgICAicHJpdmlsZWdlIjpbICANCiAgICAgICAgICAgICAgICAgIHsgIA0KICAgICAgICAgICAgICAgICAgICAgInJlc291cmNlIjoicHJvY2VzcyIsDQogICAgICAgICAgICAgICAgICAgICAicmVzb3VyY2VUeXBlIjoiUmVzdFJlc291cmNlIiwNCiAgICAgICAgICAgICAgICAgICAgICJyZXNvdXJjZUNvbnRleHQiOiIqIiwNCiAgICAgICAgICAgICAgICAgICAgICJwZXJtaXNzaW9uIjpbICANCiAgICAgICAgICAgICAgICAgICAgICAgIHsgIA0KICAgICAgICAgICAgICAgICAgICAgICAgICAgInZhbHVlIjoiKiIsDQogICAgICAgICAgICAgICAgICAgICAgICAgICAicGVybWlzc2lvblR5cGUiOiJXSElURUxJU1QiDQogICAgICAgICAgICAgICAgICAgICAgICB9DQogICAgICAgICAgICAgICAgICAgICBdDQogICAgICAgICAgICAgICAgICB9LA0KICAgICAgICAgICAgICAgICAgeyAgDQogICAgICAgICAgICAgICAgICAgICAicmVzb3VyY2UiOiJ2MS9tYWlsYm94IiwNCiAgICAgICAgICAgICAgICAgICAgICJyZXNvdXJjZVR5cGUiOiJSZXN0UmVzb3VyY2UiLA0KICAgICAgICAgICAgICAgICAgICAgInJlc291cmNlQ29udGV4dCI6IioiLA0KICAgICAgICAgICAgICAgICAgICAgInBlcm1pc3Npb24iOlsgIA0KICAgICAgICAgICAgICAgICAgICAgICAgeyAgDQogICAgICAgICAgICAgICAgICAgICAgICAgICAidmFsdWUiOiIqIiwNCiAgICAgICAgICAgICAgICAgICAgICAgICAgICJwZXJtaXNzaW9uVHlwZSI6IldISVRFTElTVCINCiAgICAgICAgICAgICAgICAgICAgICAgIH0sDQogICAgICAgICAgICAgICAgICAgICAgICB7ICANCiAgICAgICAgICAgICAgICAgICAgICAgICAgICJ2YWx1ZSI6ImRlYWN0aXZhdGVNYWlsQm94IiwNCiAgICAgICAgICAgICAgICAgICAgICAgICAgICJwZXJtaXNzaW9uVHlwZSI6IldISVRFTElTVCINCiAgICAgICAgICAgICAgICAgICAgICAgIH0NCiAgICAgICAgICAgICAgICAgICAgIF0NCiAgICAgICAgICAgICAgICAgIH0sDQogICAgICAgICAgICAgICAgICB7ICANCiAgICAgICAgICAgICAgICAgICAgICJyZXNvdXJjZSI6InYxL21haWxib3giLA0KICAgICAgICAgICAgICAgICAgICAgInJlc291cmNlVHlwZSI6IlJlc3RSZXNvdXJjZSIsDQogICAgICAgICAgICAgICAgICAgICAicmVzb3VyY2VDb250ZXh0IjoiKiIsDQogICAgICAgICAgICAgICAgICAgICAicGVybWlzc2lvbiI6WyAgDQogICAgICAgICAgICAgICAgICAgICAgICB7ICANCiAgICAgICAgICAgICAgICAgICAgICAgICAgICJ2YWx1ZSI6InNlYXJjaE1haWxCb3giLA0KICAgICAgICAgICAgICAgICAgICAgICAgICAgInBlcm1pc3Npb25UeXBlIjoiV0hJVEVMSVNUIg0KICAgICAgICAgICAgICAgICAgICAgICAgfQ0KICAgICAgICAgICAgICAgICAgICAgXQ0KICAgICAgICAgICAgICAgICAgfSwNCiAgICAgICAgICAgICAgICAgIHsgIA0KICAgICAgICAgICAgICAgICAgICAgInJlc291cmNlIjoidjEvbWFpbGJveCIsDQogICAgICAgICAgICAgICAgICAgICAicmVzb3VyY2VUeXBlIjoiUmVzdFJlc291cmNlIiwNCiAgICAgICAgICAgICAgICAgICAgICJyZXNvdXJjZUNvbnRleHQiOiIqIiwNCiAgICAgICAgICAgICAgICAgICAgICJwZXJtaXNzaW9uIjpbICANCiAgICAgICAgICAgICAgICAgICAgICAgIHsgIA0KICAgICAgICAgICAgICAgICAgICAgICAgICAgInZhbHVlIjoic2VhcmNoTWFpbEJveCIsDQogICAgICAgICAgICAgICAgICAgICAgICAgICAicGVybWlzc2lvblR5cGUiOiJXSElURUxJU1QiDQogICAgICAgICAgICAgICAgICAgICAgICB9DQogICAgICAgICAgICAgICAgICAgICBdDQogICAgICAgICAgICAgICAgICB9DQogICAgICAgICAgICAgICBdDQogICAgICAgICAgICB9LA0KICAgICAgICAgICAgeyAgDQogICAgICAgICAgICAgICAiZG9tYWluTmFtZSI6Ik1BSUxCT1giLA0KICAgICAgICAgICAgICAgImRvbWFpblR5cGUiOiJTT0FfU0VSVklDRSIsDQogICAgICAgICAgICAgICAicm9sZSI6WyAgDQogICAgICAgICAgICAgICAgICAiTWFpbGJveEFkbWluIg0KICAgICAgICAgICAgICAgXSwNCiAgICAgICAgICAgICAgICJwcml2aWxlZ2UiOlsgIA0KICAgICAgICAgICAgICAgICAgeyAgDQogICAgICAgICAgICAgICAgICAgICAicmVzb3VyY2UiOiJwcm9jZXNzIiwNCiAgICAgICAgICAgICAgICAgICAgICJyZXNvdXJjZVR5cGUiOiJSZXN0UmVzb3VyY2UiLA0KICAgICAgICAgICAgICAgICAgICAgInJlc291cmNlQ29udGV4dCI6IioiLA0KICAgICAgICAgICAgICAgICAgICAgInBlcm1pc3Npb24iOlsgIA0KICAgICAgICAgICAgICAgICAgICAgICAgeyAgDQogICAgICAgICAgICAgICAgICAgICAgICAgICAidmFsdWUiOiIqIiwNCiAgICAgICAgICAgICAgICAgICAgICAgICAgICJwZXJtaXNzaW9uVHlwZSI6IldISVRFTElTVCINCiAgICAgICAgICAgICAgICAgICAgICAgIH0NCiAgICAgICAgICAgICAgICAgICAgIF0NCiAgICAgICAgICAgICAgICAgIH0NCiAgICAgICAgICAgICAgIF0NCiAgICAgICAgICAgIH0sDQogICAgICAgICAgICB7ICANCiAgICAgICAgICAgICAgICJkb21haW5OYW1lIjoiVVNFUk1BTkFHRU1FTlQiLA0KICAgICAgICAgICAgICAgImRvbWFpblR5cGUiOiJTT0FfU0VSVklDRSIsDQogICAgICAgICAgICAgICAicm9sZSI6WyAgDQogICAgICAgICAgICAgICAgICAiTWFpbGJveEFkbWluIg0KICAgICAgICAgICAgICAgXSwNCiAgICAgICAgICAgICAgICJwcml2aWxlZ2UiOlsgIA0KICAgICAgICAgICAgICAgICAgeyAgDQogICAgICAgICAgICAgICAgICAgICAicmVzb3VyY2UiOiJwcm9jZXNzIiwNCiAgICAgICAgICAgICAgICAgICAgICJyZXNvdXJjZVR5cGUiOiJSZXN0UmVzb3VyY2UiLA0KICAgICAgICAgICAgICAgICAgICAgInJlc291cmNlQ29udGV4dCI6IioiLA0KICAgICAgICAgICAgICAgICAgICAgInBlcm1pc3Npb24iOlsgIA0KICAgICAgICAgICAgICAgICAgICAgICAgeyAgDQogICAgICAgICAgICAgICAgICAgICAgICAgICAidmFsdWUiOiIqIiwNCiAgICAgICAgICAgICAgICAgICAgICAgICAgICJwZXJtaXNzaW9uVHlwZSI6IldISVRFTElTVCINCiAgICAgICAgICAgICAgICAgICAgICAgIH0NCiAgICAgICAgICAgICAgICAgICAgIF0NCiAgICAgICAgICAgICAgICAgIH0NCiAgICAgICAgICAgICAgIF0NCiAgICAgICAgICAgIH0sDQogICAgICAgICAgICB7ICANCiAgICAgICAgICAgICAgICJkb21haW5OYW1lIjoiQk9FSU5HIiwNCiAgICAgICAgICAgICAgICJkb21haW5UeXBlIjoiU09BX1NFUlZJQ0UiLA0KICAgICAgICAgICAgICAgInJvbGUiOlsgIA0KICAgICAgICAgICAgICAgICAgIk1haWxib3hBZG1pbiINCiAgICAgICAgICAgICAgIF0sDQogICAgICAgICAgICAgICAicHJpdmlsZWdlIjpbICANCiAgICAgICAgICAgICAgICAgIHsgIA0KICAgICAgICAgICAgICAgICAgICAgInJlc291cmNlIjoicHJvY2VzcyIsDQogICAgICAgICAgICAgICAgICAgICAicmVzb3VyY2VUeXBlIjoiUmVzdFJlc291cmNlIiwNCiAgICAgICAgICAgICAgICAgICAgICJyZXNvdXJjZUNvbnRleHQiOiIqIiwNCiAgICAgICAgICAgICAgICAgICAgICJwZXJtaXNzaW9uIjpbICANCiAgICAgICAgICAgICAgICAgICAgICAgIHsgIA0KICAgICAgICAgICAgICAgICAgICAgICAgICAgInZhbHVlIjoiKiIsDQogICAgICAgICAgICAgICAgICAgICAgICAgICAicGVybWlzc2lvblR5cGUiOiJXSElURUxJU1QiDQogICAgICAgICAgICAgICAgICAgICAgICB9DQogICAgICAgICAgICAgICAgICAgICBdDQogICAgICAgICAgICAgICAgICB9DQogICAgICAgICAgICAgICBdDQogICAgICAgICAgICB9DQogICAgICAgICBdLA0KICAgICAgICAgIm5lc3RlZFNlcnZpY2VEZXBlbmRlbmN5Q29udHJhaW50IjpbICANCiAgICAgICAgICAgIHsgIA0KICAgICAgICAgICAgICAgInNlcnZpY2VOYW1lIjoiTWFpbGJveCIsDQogICAgICAgICAgICAgICAicHJpbWFyeUlkIjoiOTAzMkE0OTEwQTBBNTI5ODBBMEVDNjc2REIzM0ExMDIiLA0KICAgICAgICAgICAgICAgIm5lc3RlZFNlcnZpY2VJZCI6WyAgDQoNCiAgICAgICAgICAgICAgIF0sDQogICAgICAgICAgICAgICAibmVzdGVkU2VydmljZURlcGVuZGVuY3kiOlsgIA0KDQogICAgICAgICAgICAgICBdDQogICAgICAgICAgICB9DQogICAgICAgICBdDQogICAgICB9DQogICBdDQp9DQo=";

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeMethod
	public void setUp() throws Exception {
		logger = LogManager.getLogger(MailBoxConfigurationServiceTest.class);
	}
    
	/**
	 * Method constructs MailBox with valid data.
	 * 
	 * @throws LiaisonException
	 * @throws JSONException
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws JAXBException
	 * @throws IOException
	 */
	@Test
	public void testCreateMailBox() throws LiaisonException, JSONException, JsonParseException, JsonMappingException,
			JAXBException, IOException {

		// Adding the mailbox
		jsonRequest = ServiceUtils.readFileFromClassPath("requests/mailbox/addmailboxrequest.json");
		AddMailboxRequestDTO requestDTO = MailBoxUtil.unmarshalFromJSON(jsonRequest, AddMailboxRequestDTO.class);

		MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
		requestDTO.setMailBox(mbxDTO);
		
		jsonRequest = MailBoxUtil.marshalToJSON(requestDTO);
		
		String url = getBASE_URL() + "?sid=" +serviceInstanceId;
		request = constructHTTPRequest(url, HTTP_METHOD.POST, jsonRequest, logger);
		request.execute();
		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		AddMailBoxResponseDTO responseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, AddMailBoxResponseDTO.class);
		Assert.assertEquals(SUCCESS, responseDTO.getResponse().getStatus());

		// Get the mailbox
		url = getBASE_URL() +"/"+responseDTO.getMailBox().getGuid()+"?sid=" +serviceInstanceId;
		request = constructHTTPRequest(url, HTTP_METHOD.GET, null, logger);
		request.addHeader("acl-manifest", aclManifest);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		GetMailBoxResponseDTO getResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, GetMailBoxResponseDTO.class);

		// Assertion
		Assert.assertEquals(SUCCESS, getResponseDTO.getResponse().getStatus());
		Assert.assertEquals(requestDTO.getMailBox().getName(), getResponseDTO.getMailBox().getName());
		Assert.assertEquals(requestDTO.getMailBox().getDescription(), getResponseDTO.getMailBox().getDescription());
		Assert.assertEquals(requestDTO.getMailBox().getShardKey(), getResponseDTO.getMailBox().getShardKey());
		Assert.assertEquals(MailBoxStatus.ACTIVE.name(), getResponseDTO.getMailBox().getStatus());

		Assert.assertEquals(requestDTO.getMailBox().getProperties().get(0).getName(),
				getResponseDTO.getMailBox().getProperties().get(0).getName());
		Assert.assertEquals(requestDTO.getMailBox().getProperties().get(0).getValue(),
				getResponseDTO.getMailBox().getProperties().get(0).getValue());

	}
    
	/**
	 * Method to test mailbox with valid data.
	 * 
	 * @throws LiaisonException
	 * @throws JSONException
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws JAXBException
	 * @throws IOException
	 */
	@Test
	public void testGetMailBox() throws LiaisonException, JSONException, JsonParseException, JsonMappingException, JAXBException,
			IOException {

		// Adding the mailbox
		jsonRequest = ServiceUtils.readFileFromClassPath("requests/mailbox/addmailboxrequest.json");
		AddMailboxRequestDTO requestDTO = MailBoxUtil.unmarshalFromJSON(jsonRequest, AddMailboxRequestDTO.class);

		MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
		requestDTO.setMailBox(mbxDTO);

		jsonRequest = MailBoxUtil.marshalToJSON(requestDTO);
		
		String url = getBASE_URL() + "?sid=" +serviceInstanceId;
		request = constructHTTPRequest(url, HTTP_METHOD.POST, jsonRequest, logger);
		request.addHeader("acl-manifest", aclManifest);
		request.execute();
		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		AddMailBoxResponseDTO responseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, AddMailBoxResponseDTO.class);
		Assert.assertEquals(SUCCESS, responseDTO.getResponse().getStatus());

		// Get the mailbox
		url = getBASE_URL() + "/" + responseDTO.getMailBox().getGuid()+"?sid=" +serviceInstanceId;
		request = constructHTTPRequest(url, HTTP_METHOD.GET, null, logger);
		request.addHeader("acl-manifest", aclManifest);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		GetMailBoxResponseDTO getResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, GetMailBoxResponseDTO.class);

		// Assertion
		Assert.assertEquals(SUCCESS, getResponseDTO.getResponse().getStatus());
		Assert.assertEquals(requestDTO.getMailBox().getName(), getResponseDTO.getMailBox().getName());
		Assert.assertEquals(requestDTO.getMailBox().getDescription(), getResponseDTO.getMailBox().getDescription());
		Assert.assertEquals(requestDTO.getMailBox().getShardKey(), getResponseDTO.getMailBox().getShardKey());
		Assert.assertEquals(MailBoxStatus.ACTIVE.name(), getResponseDTO.getMailBox().getStatus());

		Assert.assertEquals(requestDTO.getMailBox().getProperties().get(0).getName(),
				getResponseDTO.getMailBox().getProperties().get(0).getName());
		Assert.assertEquals(requestDTO.getMailBox().getProperties().get(0).getValue(),
				getResponseDTO.getMailBox().getProperties().get(0).getValue());

	}
    
	/**
	 * Method to test deactivate mailbox.
	 * 
	 * @throws LiaisonException
	 * @throws JSONException
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws JAXBException
	 * @throws IOException
	 */
	@Test
	public void testDeactivateMailBox() throws LiaisonException, JSONException, JsonParseException, JsonMappingException,
			JAXBException, IOException {

		// Adding the mailbox
		jsonRequest = ServiceUtils.readFileFromClassPath("requests/mailbox/addmailboxrequest.json");
		AddMailboxRequestDTO requestDTO = MailBoxUtil.unmarshalFromJSON(jsonRequest, AddMailboxRequestDTO.class);

		MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
		requestDTO.setMailBox(mbxDTO);

		jsonRequest = MailBoxUtil.marshalToJSON(requestDTO);
		
		String url = getBASE_URL() + "?sid=" +serviceInstanceId;
		request = constructHTTPRequest(url, HTTP_METHOD.POST, jsonRequest, logger);
		request.addHeader("acl-manifest", aclManifest);
		request.execute();
		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		AddMailBoxResponseDTO responseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, AddMailBoxResponseDTO.class);
		Assert.assertEquals(SUCCESS, responseDTO.getResponse().getStatus());

		// Deactivate the mailbox
		url = getBASE_URL() + "/" + responseDTO.getMailBox().getGuid();
		request = constructHTTPRequest(url, HTTP_METHOD.DELETE, null, logger);
		request.addHeader("acl-manifest", aclManifest);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);
		Assert.assertEquals(SUCCESS, getResponseStatus(jsonResponse, "deactivateMailBoxResponse"));

		// Get the mailbox
		url = getBASE_URL() + "/" + responseDTO.getMailBox().getGuid()+"?sid=" +serviceInstanceId;
		request = constructHTTPRequest(url, HTTP_METHOD.GET, null, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		GetMailBoxResponseDTO getResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, GetMailBoxResponseDTO.class);
		Assert.assertEquals(MailBoxStatus.INACTIVE.name(), getResponseDTO.getMailBox().getStatus());

	}
    
	/**
	 * Method to test revise mailBox with liaison HTTPClient.
	 * 
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws Exception
	 */
	@Test
	public void testReviseMailBoxWithLiaisonHTTPClient() throws ClientProtocolException, IOException, Exception {

		// Adding the mailbox
		AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();

		MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
		requestDTO.setMailBox(mbxDTO);

		jsonRequest = MailBoxUtil.marshalToJSON(requestDTO);
		
		String url = getBASE_URL() + "?sid=" +serviceInstanceId;
		request = constructHTTPRequest(url, HTTP_METHOD.POST, jsonRequest, logger);
		request.execute();
		request.addHeader("acl-manifest", aclManifest);
		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		AddMailBoxResponseDTO responseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, AddMailBoxResponseDTO.class);
		Assert.assertEquals(SUCCESS, responseDTO.getResponse().getStatus());

		// Constructing the revise
		ReviseMailBoxRequestDTO reviseRequestDTO = new ReviseMailBoxRequestDTO();
		mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), false);
		mbxDTO.setGuid(responseDTO.getMailBox().getGuid());
		reviseRequestDTO.setMailBox(mbxDTO);
		jsonRequest = MailBoxUtil.marshalToJSON(reviseRequestDTO);

	    url = getBASE_URL() + "/" + responseDTO.getMailBox().getGuid()+ "?sid=" +serviceInstanceId;
		request = constructHTTPRequest(url, HTTP_METHOD.PUT, jsonRequest, logger);
		request.addHeader("acl-manifest", aclManifest);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);
		Assert.assertEquals(SUCCESS, getResponseStatus(jsonResponse, "reviseMailBoxResponse"));

		// Get the mailbox
		url = getBASE_URL() + "/" + responseDTO.getMailBox().getGuid()+ "?sid=" +serviceInstanceId;
		request = constructHTTPRequest(url, HTTP_METHOD.GET, null, logger);
		request.addHeader("acl-manifest", aclManifest);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		GetMailBoxResponseDTO getResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, GetMailBoxResponseDTO.class);

		// Assertion
		Assert.assertEquals(MailBoxStatus.ACTIVE.name(), getResponseDTO.getMailBox().getStatus());
		Assert.assertEquals(mbxDTO.getName(), getResponseDTO.getMailBox().getName());
		Assert.assertEquals(mbxDTO.getDescription(), getResponseDTO.getMailBox().getDescription());
		Assert.assertEquals(mbxDTO.getShardKey(), getResponseDTO.getMailBox().getShardKey());
		Assert.assertEquals(MailBoxStatus.ACTIVE.name(), getResponseDTO.getMailBox().getStatus());

		Assert.assertEquals(mbxDTO.getProperties().get(0).getName(),
				getResponseDTO.getMailBox().getProperties().get(0).getName());
		Assert.assertEquals(mbxDTO.getProperties().get(0).getValue(),
				getResponseDTO.getMailBox().getProperties().get(0).getValue());

	}
    
	/**
	 * Method to test mailBox with valid mandatory fields.
	 * 
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws JAXBException
	 * @throws IOException
	 * @throws LiaisonException
	 */
	@Test
	public void testCreateMailBox_MandatoryFields_ShouldPass() throws JsonGenerationException, JsonMappingException,
			JAXBException, IOException, LiaisonException {

		MailBoxDTO mailBox = new MailBoxDTO();
		mailBox.setName("TestMailBox");
		mailBox.setStatus("ACTIVE");
		mailBox.setDescription("Test Mailbox");
		mailBox.setTenancyKey("Test Tenancy Key");

		// Adding the mailbox
		AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
		requestDTO.setMailBox(mailBox);

		jsonRequest = MailBoxUtil.marshalToJSON(requestDTO);
		
		String url = getBASE_URL() + "?sid=" +serviceInstanceId;
		request = constructHTTPRequest(url, HTTP_METHOD.POST, jsonRequest, logger);
		request.addHeader("acl-manifest", aclManifest);
		request.execute();
		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		AddMailBoxResponseDTO responseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, AddMailBoxResponseDTO.class);
		Assert.assertEquals(SUCCESS, responseDTO.getResponse().getStatus());

		// Get the mailbox
		url = getBASE_URL() + "/" + responseDTO.getMailBox().getGuid()+ "?sid=" +serviceInstanceId;
		request = constructHTTPRequest(url, HTTP_METHOD.GET, null, logger);
		request.addHeader("acl-manifest", aclManifest);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		GetMailBoxResponseDTO getResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, GetMailBoxResponseDTO.class);

		// Assertion
		Assert.assertEquals(MailBoxStatus.ACTIVE.name(), getResponseDTO.getMailBox().getStatus());
		Assert.assertEquals(mailBox.getName(), getResponseDTO.getMailBox().getName());
	}
    
	/**
	 * Method to test mailBox without mandatory fields.
	 * 
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws JAXBException
	 * @throws IOException
	 * @throws LiaisonException
	 */
	@Test
	public void testCreateMailBox_WithoutMandatoryFields_ShouldFail() throws JsonGenerationException, JsonMappingException,
			JAXBException, IOException, LiaisonException {

		MailBoxDTO mailBox = constructDummyMailBoxDTO(System.currentTimeMillis(), true);

		// Adding the mailbox
		AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
		requestDTO.setMailBox(mailBox);

		jsonRequest = MailBoxUtil.marshalToJSON(requestDTO);
		
		String url = getBASE_URL() + "?sid=" +serviceInstanceId;
		request = constructHTTPRequest(url, HTTP_METHOD.POST, jsonRequest, logger);
		request.addHeader("acl-manifest", aclManifest);
		request.execute();
		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		AddMailBoxResponseDTO responseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, AddMailBoxResponseDTO.class);
		Assert.assertEquals(SUCCESS, responseDTO.getResponse().getStatus());

		// Get the mailbox
		url = getBASE_URL() + "/" + responseDTO.getMailBox().getGuid()+ "?sid=" +serviceInstanceId;
		request = constructHTTPRequest(url, HTTP_METHOD.GET, null, logger);
		request.addHeader("acl-manifest", aclManifest);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		GetMailBoxResponseDTO getResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, GetMailBoxResponseDTO.class);

		// Assertion
		Assert.assertEquals(MailBoxStatus.ACTIVE.name(), getResponseDTO.getMailBox().getStatus());
		Assert.assertEquals(mailBox.getName(), getResponseDTO.getMailBox().getName());

	}
    
	/**
	 * Method to test mailBox with null.
	 * 
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws JAXBException
	 * @throws IOException
	 * @throws LiaisonException
	 */
	@Test
	public void testCreateMailBox_MailBoxisNull_ShouldFail() throws JsonGenerationException, JsonMappingException,
			JAXBException, IOException, LiaisonException {

		// Adding the mailbox
		AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
		requestDTO.setMailBox(null);

		jsonRequest = MailBoxUtil.marshalToJSON(requestDTO);
		
		String url = getBASE_URL() + "?sid=" +serviceInstanceId;
		request = constructHTTPRequest(url, HTTP_METHOD.POST, jsonRequest, logger);
		request.addHeader("acl-manifest", aclManifest);
		request.execute();
		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		AddMailBoxResponseDTO responseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, AddMailBoxResponseDTO.class);
		Assert.assertEquals(FAILURE, responseDTO.getResponse().getStatus());
		Assert.assertEquals(true,
				responseDTO.getResponse().getMessage().contains(Messages.INVALID_REQUEST.value()));

	}
    
	/**
	 * Method to test mailBox with invalid id.
	 * 
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws JAXBException
	 * @throws IOException
	 * @throws LiaisonException
	 */
	@Test
	public void testGetMailBox_WrongId_ShouldFail() throws JsonGenerationException, JsonMappingException,
			JAXBException, IOException, LiaisonException {

		// Get the mailbox
		String url = getBASE_URL() + "/" + "3434"+"?sid=" +serviceInstanceId;
		request = constructHTTPRequest(url, HTTP_METHOD.GET, null, logger);
		request.addHeader("acl-manifest", aclManifest);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		GetMailBoxResponseDTO getResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, GetMailBoxResponseDTO.class);

		// Assertion
		Assert.assertEquals(FAILURE, getResponseDTO.getResponse().getStatus());

	}
    
	/**
	 * Method to test revise mailBox with null.
	 * 
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws JAXBException
	 * @throws IOException
	 * @throws LiaisonException
	 * @throws JSONException
	 */
	@Test
	public void testReviseMailBox_Null_ShouldFail() throws JsonGenerationException, JsonMappingException, JAXBException,
			IOException, LiaisonException, JSONException {

		// Constructing the revise
		ReviseMailBoxRequestDTO reviseRequestDTO = new ReviseMailBoxRequestDTO();
		reviseRequestDTO.setMailBox(null);
		jsonRequest = MailBoxUtil.marshalToJSON(reviseRequestDTO);

		String url = getBASE_URL() + "/" + "fasdfasdfdas" + "?sid=" +serviceInstanceId;
		request = constructHTTPRequest(url, HTTP_METHOD.PUT, jsonRequest, logger);
		request.addHeader("acl-manifest", aclManifest);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);
		Assert.assertEquals(FAILURE, getResponseStatus(jsonResponse, "reviseMailBoxResponse"));

	}
    
	/**
	 * Method to test revise mailBox with wrong guid.
	 * 
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws JAXBException
	 * @throws IOException
	 * @throws LiaisonException
	 * @throws JSONException
	 */
	@Test
	public void testReviseMailBox_WrongGuids_ShouldFail() throws JsonGenerationException, JsonMappingException, JAXBException,
			IOException, LiaisonException, JSONException {

		// Constructing the revise
		ReviseMailBoxRequestDTO reviseRequestDTO = new ReviseMailBoxRequestDTO();
		reviseRequestDTO.setMailBox(constructDummyMailBoxDTO(System.currentTimeMillis(), true));
		jsonRequest = MailBoxUtil.marshalToJSON(reviseRequestDTO);

		String url = getBASE_URL() + "/" + "3432432" + "?sid=" +serviceInstanceId;
		request = constructHTTPRequest(url, HTTP_METHOD.PUT, jsonRequest, logger);
		request.addHeader("acl-manifest", aclManifest);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);
		Assert.assertEquals(FAILURE, getResponseStatus(jsonResponse, "reviseMailBoxResponse"));

	}
    
	/**
	 * Method to test revise mailBox with invalid guid.
	 * 
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws JAXBException
	 * @throws IOException
	 * @throws LiaisonException
	 * @throws JSONException
	 */
	@Test
	public void testReviseMailBox_InvalidGuids_ShouldFail() throws JsonGenerationException, JsonMappingException, JAXBException,
			IOException, LiaisonException, JSONException {

		// Constructing the revise
		ReviseMailBoxRequestDTO reviseRequestDTO = new ReviseMailBoxRequestDTO();

		MailBoxDTO mailBox = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
		mailBox.setGuid("123456");
		reviseRequestDTO.setMailBox(mailBox);

		jsonRequest = MailBoxUtil.marshalToJSON(reviseRequestDTO);

		String url = getBASE_URL() + "/" + "123456" + "?sid=" +serviceInstanceId;
		request = constructHTTPRequest(url, HTTP_METHOD.PUT, jsonRequest, logger);
		request.addHeader("acl-manifest", aclManifest);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);
		Assert.assertEquals(FAILURE, getResponseStatus(jsonResponse, "reviseMailBoxResponse"));

	}
    
	/**
	 * Method to test deactivate mailBox with wrong id.
	 * 
	 * @throws LiaisonException
	 * @throws JSONException
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws JAXBException
	 * @throws IOException
	 */
	@Test
	public void testDeactivateMailBox_WrongId_ShouldFail() throws LiaisonException, JSONException, JsonParseException,
			JsonMappingException, JAXBException, IOException {

		// Deactivate the mailbox
		String url = getBASE_URL() + "/" + "123456";
		request = constructHTTPRequest(url, HTTP_METHOD.DELETE, null, logger);
		request.addHeader("acl-manifest", aclManifest);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);
		Assert.assertEquals(FAILURE, getResponseStatus(jsonResponse, "deactivateMailBoxResponse"));
	}
    
	/**
	 * Method to test Revise mailBox without mandatory field.
	 * 
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws JAXBException
	 * @throws IOException
	 * @throws LiaisonException
	 * @throws JSONException
	 */
	@Test
	public void testReviseMailBox_WithoutMandatory_ShouldFail() throws JsonGenerationException, JsonMappingException,
			JAXBException,
			IOException, LiaisonException, JSONException {

		// Constructing the revise
		ReviseMailBoxRequestDTO reviseRequestDTO = new ReviseMailBoxRequestDTO();
		reviseRequestDTO.setMailBox(null);
		jsonRequest = MailBoxUtil.marshalToJSON(reviseRequestDTO);

		String url = getBASE_URL() + "/" + "3432432" + "?sid=" +serviceInstanceId;
		request = constructHTTPRequest(url, HTTP_METHOD.PUT, jsonRequest, logger);
		request.addHeader("acl-manifest", aclManifest);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);
		Assert.assertEquals(FAILURE, getResponseStatus(jsonResponse, "reviseMailBoxResponse"));

	}
    
	/**
	 * Method constructs mailbox with empty.
	 * 
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws JAXBException
	 * @throws IOException
	 * @throws LiaisonException
	 */
	@Test
	public void testCreateMailBox_EmptyString_ShouldFail() throws JsonGenerationException, JsonMappingException,
			JAXBException, IOException, LiaisonException {

		// Adding the mailbox
		AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();

		MailBoxDTO dto = new MailBoxDTO();
		dto.setName("");
		requestDTO.setMailBox(dto);

		jsonRequest = MailBoxUtil.marshalToJSON(requestDTO);
		
		String url = getBASE_URL() + "?sid=" +serviceInstanceId;
		request = constructHTTPRequest(url, HTTP_METHOD.POST, jsonRequest, logger);
		request.addHeader("acl-manifest", aclManifest);
		request.execute();
		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		AddMailBoxResponseDTO responseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, AddMailBoxResponseDTO.class);
		Assert.assertEquals(FAILURE, responseDTO.getResponse().getStatus());

	}
    
	/**
	 * Method to constructs mailbox with invalid status.
	 * 
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws JAXBException
	 * @throws IOException
	 * @throws LiaisonException
	 */
	@Test
	public void testCreateMailBox_InValidStatus_ShouldFail() throws JsonGenerationException, JsonMappingException,
			JAXBException, IOException, LiaisonException {

		// Adding the mailbox
		AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();

		MailBoxDTO dto = new MailBoxDTO();
		dto.setName("afdaf");
		dto.setStatus("sfrafda");
		requestDTO.setMailBox(dto);

		jsonRequest = MailBoxUtil.marshalToJSON(requestDTO);
		
		String url = getBASE_URL() + "?sid=" +serviceInstanceId;
		request = constructHTTPRequest(url, HTTP_METHOD.POST, jsonRequest, logger);
		request.addHeader("acl-manifest", aclManifest);
		request.execute();
		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		AddMailBoxResponseDTO responseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, AddMailBoxResponseDTO.class);
		Assert.assertEquals(FAILURE, responseDTO.getResponse().getStatus());
	}
	
	/**
	 * Method to search mailbox with mailbox name and profile name.
	 * 
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws JAXBException
	 * @throws IOException
	 * @throws LiaisonException
	 */
	@Test
	public void testSearchMailBox() throws JsonParseException, JsonMappingException,
		   JAXBException, IOException, LiaisonException {
		
		// Adding the mailbox
		jsonRequest = ServiceUtils.readFileFromClassPath("requests/mailbox/addmailboxrequest.json");
		AddMailboxRequestDTO requestDTO = MailBoxUtil.unmarshalFromJSON(jsonRequest, AddMailboxRequestDTO.class);

		MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
		requestDTO.setMailBox(mbxDTO);
		
		jsonRequest = MailBoxUtil.marshalToJSON(requestDTO);
		
		String url = getBASE_URL() + "?sid=" +serviceInstanceId;
		request = constructHTTPRequest(url, HTTP_METHOD.POST, jsonRequest, logger);
		request.addHeader("acl-manifest", aclManifest);
		request.execute();
		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		AddMailBoxResponseDTO responseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, AddMailBoxResponseDTO.class);
		Assert.assertEquals(SUCCESS, responseDTO.getResponse().getStatus());
		
		//Add profile 
		String profileName = "TestProfile" + System.nanoTime();
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
		Assert.assertEquals(SUCCESS, profileResponseDTO.getResponse().getStatus());
		
		jsonRequest = ServiceUtils.readFileFromClassPath("requests/processor/createprocessor.json");
		AddProcessorToMailboxRequestDTO addProcessorDTO = MailBoxUtil.unmarshalFromJSON(jsonRequest, AddProcessorToMailboxRequestDTO.class);
		
		//Linked the mailbox and profile to processor
		ProcessorDTO proDTO = new ProcessorDTO();
		proDTO = addProcessorDTO.getProcessor();
		proDTO.setLinkedMailboxId(responseDTO.getMailBox().getGuid());
		List<String> profiles = new ArrayList<>();
		profiles.add(profileName);
		proDTO.setLinkedProfiles(profiles);
		addProcessorDTO.setProcessor(proDTO);
		
		jsonRequest = MailBoxUtil.marshalToJSON(addProcessorDTO);
		
		String addProcessor = "/" + responseDTO.getMailBox().getGuid() + "/processor" + "?sid=" +serviceInstanceId;
		request = constructHTTPRequest(getBASE_URL() + addProcessor, HTTP_METHOD.POST, jsonRequest, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		AddProcessorToMailboxResponseDTO processorResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse,
				AddProcessorToMailboxResponseDTO.class);
		Assert.assertEquals(SUCCESS, processorResponseDTO.getResponse().getStatus());
		
		//search mailbox by mailbox name
		url = getBASE_URL() + "/?name=" + requestDTO.getMailBox().getName();
		request = constructHTTPRequest(url, HTTP_METHOD.GET, null, logger);
		request.addHeader("acl-manifest", aclManifest);
		request.execute();
		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);
		SearchMailBoxResponseDTO searchResponceDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, SearchMailBoxResponseDTO.class);
		Assert.assertEquals(SUCCESS, searchResponceDTO.getResponse().getStatus());
		
		//search mailbox by profile name
		url = getBASE_URL() + "/?name=&profile=" + profileName;
		request = constructHTTPRequest(url, HTTP_METHOD.GET, null, logger);
		request.execute();
		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);
		searchResponceDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, SearchMailBoxResponseDTO.class);
		Assert.assertEquals(SUCCESS, searchResponceDTO.getResponse().getStatus());
	}
	
	/**
	 * Method to search mailbox with empty mailbox name.
	 * 
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws JAXBException
	 * @throws IOException
	 * @throws LiaisonException
	 */
	@Test
	public void testSearchMailBox_MailboxNameIsEmpty_ShouldFail() throws JsonParseException, JsonMappingException, JAXBException, IOException, LiaisonException {
		
		String url = getBASE_URL() + "/?name=";
		request = constructHTTPRequest(url, HTTP_METHOD.GET, null, logger);
		request.addHeader("acl-manifest", aclManifest);
		request.execute();
		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);
		SearchMailBoxResponseDTO searchResponceDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, SearchMailBoxResponseDTO.class);
		Assert.assertEquals(FAILURE, searchResponceDTO.getResponse().getStatus());
	}
	
	/**
	 * Method to search mailbox with empty profile name.
	 * 
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws JAXBException
	 * @throws IOException
	 * @throws LiaisonException
	 */
	@Test
	public void testSearchMailBox_ProfileNameIsEmpty_ShouldFail() throws JsonParseException, JsonMappingException, JAXBException, IOException, LiaisonException {
		
		String url = getBASE_URL() + "/?name=&profile=";
		request = constructHTTPRequest(url, HTTP_METHOD.GET, null, logger);
		request.addHeader("acl-manifest", aclManifest);
		request.execute();
		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);
		SearchMailBoxResponseDTO searchResponceDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, SearchMailBoxResponseDTO.class);
		Assert.assertEquals(FAILURE, searchResponceDTO.getResponse().getStatus());
			
	}
	
}
