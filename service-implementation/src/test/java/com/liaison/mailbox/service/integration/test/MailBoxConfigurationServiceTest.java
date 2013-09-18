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

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
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
import com.liaison.mailbox.enums.MailBoxStatus;
import com.liaison.mailbox.service.base.test.BaseServiceTest;
import com.liaison.mailbox.service.dto.configuration.MailBoxDTO;
import com.liaison.mailbox.service.dto.configuration.request.AddMailboxRequestDTO;
import com.liaison.mailbox.service.dto.configuration.request.ReviseMailBoxRequestDTO;
import com.liaison.mailbox.service.dto.configuration.response.AddMailBoxResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.GetMailBoxResponseDTO;
import com.liaison.mailbox.service.util.MailBoxUtility;

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

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		logger = LoggerFactory.getLogger(MailBoxConfigurationServiceTest.class);
	}

	@Test
	public void testCreateMailBox() throws LiaisonException, JSONException, JsonParseException, JsonMappingException,
			JAXBException, IOException {

		// Adding the mailbox
		jsonRequest = ServiceUtils.readFileFromClassPath("requests/mailbox/addmailboxrequest.json");
		AddMailboxRequestDTO requestDTO = MailBoxUtility.unmarshalFromJSON(jsonRequest, AddMailboxRequestDTO.class);

		MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
		requestDTO.setMailBox(mbxDTO);

		jsonRequest = MailBoxUtility.marshalToJSON(requestDTO);
		request = constructHTTPRequest(getBASE_URL(), HTTP_METHOD.POST, jsonRequest, logger);
		request.execute();
		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		Assert.assertEquals(SUCCESS, getResponseStatus(jsonResponse, "addMailBoxResponse"));

		AddMailBoxResponseDTO responseDTO = MailBoxUtility.unmarshalFromJSON(jsonResponse, AddMailBoxResponseDTO.class);

		// Get the mailbox
		String url = getBASE_URL() + "/" + responseDTO.getMailBox().getGuid();
		request = constructHTTPRequest(url, HTTP_METHOD.GET, null, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		GetMailBoxResponseDTO getResponseDTO = MailBoxUtility.unmarshalFromJSON(jsonResponse, GetMailBoxResponseDTO.class);

		// Assertion
		Assert.assertEquals(SUCCESS, getResponseStatus(jsonResponse, "getMailBoxResponse"));
		Assert.assertEquals(requestDTO.getMailBox().getName(), getResponseDTO.getMailBox().getName());
		Assert.assertEquals(requestDTO.getMailBox().getDescription(), getResponseDTO.getMailBox().getDescription());
		Assert.assertEquals(requestDTO.getMailBox().getServiceInstId(), getResponseDTO.getMailBox().getServiceInstId());
		Assert.assertEquals(requestDTO.getMailBox().getShardKey(), getResponseDTO.getMailBox().getShardKey());
		Assert.assertEquals(MailBoxStatus.ACTIVE.name(), getResponseDTO.getMailBox().getStatus());

		Assert.assertEquals(requestDTO.getMailBox().getProperties().get(0).getName(),
				getResponseDTO.getMailBox().getProperties().get(0).getName());
		Assert.assertEquals(requestDTO.getMailBox().getProperties().get(0).getValue(),
				getResponseDTO.getMailBox().getProperties().get(0).getValue());

	}

	@Test
	public void testGetMailBox() throws LiaisonException, JSONException, JsonParseException, JsonMappingException, JAXBException,
			IOException {

		// Adding the mailbox
		jsonRequest = ServiceUtils.readFileFromClassPath("requests/mailbox/addmailboxrequest.json");
		AddMailboxRequestDTO requestDTO = MailBoxUtility.unmarshalFromJSON(jsonRequest, AddMailboxRequestDTO.class);

		MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
		requestDTO.setMailBox(mbxDTO);

		jsonRequest = MailBoxUtility.marshalToJSON(requestDTO);
		request = constructHTTPRequest(getBASE_URL(), HTTP_METHOD.POST, jsonRequest, logger);
		request.execute();
		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		Assert.assertEquals(SUCCESS, getResponseStatus(jsonResponse, "addMailBoxResponse"));

		AddMailBoxResponseDTO responseDTO = MailBoxUtility.unmarshalFromJSON(jsonResponse, AddMailBoxResponseDTO.class);

		// Get the mailbox
		String url = getBASE_URL() + "/" + responseDTO.getMailBox().getGuid();
		request = constructHTTPRequest(url, HTTP_METHOD.GET, null, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		GetMailBoxResponseDTO getResponseDTO = MailBoxUtility.unmarshalFromJSON(jsonResponse, GetMailBoxResponseDTO.class);

		// Assertion
		Assert.assertEquals(SUCCESS, getResponseStatus(jsonResponse, "getMailBoxResponse"));
		Assert.assertEquals(requestDTO.getMailBox().getName(), getResponseDTO.getMailBox().getName());
		Assert.assertEquals(requestDTO.getMailBox().getDescription(), getResponseDTO.getMailBox().getDescription());
		Assert.assertEquals(requestDTO.getMailBox().getServiceInstId(), getResponseDTO.getMailBox().getServiceInstId());
		Assert.assertEquals(requestDTO.getMailBox().getShardKey(), getResponseDTO.getMailBox().getShardKey());
		Assert.assertEquals(MailBoxStatus.ACTIVE.name(), getResponseDTO.getMailBox().getStatus());

		Assert.assertEquals(requestDTO.getMailBox().getProperties().get(0).getName(),
				getResponseDTO.getMailBox().getProperties().get(0).getName());
		Assert.assertEquals(requestDTO.getMailBox().getProperties().get(0).getValue(),
				getResponseDTO.getMailBox().getProperties().get(0).getValue());

	}

	@Test
	public void testDeactivateMailBox() throws LiaisonException, JSONException, JsonParseException, JsonMappingException,
			JAXBException, IOException {

		// Adding the mailbox
		jsonRequest = ServiceUtils.readFileFromClassPath("requests/mailbox/addmailboxrequest.json");
		AddMailboxRequestDTO requestDTO = MailBoxUtility.unmarshalFromJSON(jsonRequest, AddMailboxRequestDTO.class);

		MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
		requestDTO.setMailBox(mbxDTO);

		jsonRequest = MailBoxUtility.marshalToJSON(requestDTO);
		request = constructHTTPRequest(getBASE_URL(), HTTP_METHOD.POST, jsonRequest, logger);
		request.execute();
		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		Assert.assertEquals(SUCCESS, getResponseStatus(jsonResponse, "addMailBoxResponse"));
		AddMailBoxResponseDTO responseDTO = MailBoxUtility.unmarshalFromJSON(jsonResponse, AddMailBoxResponseDTO.class);

		// Deactivate the mailbox
		String url = getBASE_URL() + "/" + responseDTO.getMailBox().getGuid();
		request = constructHTTPRequest(url, HTTP_METHOD.DELETE, null, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);
		Assert.assertEquals(SUCCESS, getResponseStatus(jsonResponse, "deactivateMailBoxResponse"));

		// Get the mailbox
		url = getBASE_URL() + "/" + responseDTO.getMailBox().getGuid();
		request = constructHTTPRequest(url, HTTP_METHOD.GET, null, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		GetMailBoxResponseDTO getResponseDTO = MailBoxUtility.unmarshalFromJSON(jsonResponse, GetMailBoxResponseDTO.class);
		Assert.assertEquals(MailBoxStatus.INACTIVE.name(), getResponseDTO.getMailBox().getStatus());

	}

	// @Test
	public void testReviseMailBoxWithLiaisonHTTPClient() throws ClientProtocolException, IOException, Exception {

		// Adding the mailbox
		AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();

		MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
		requestDTO.setMailBox(mbxDTO);

		jsonRequest = MailBoxUtility.marshalToJSON(requestDTO);
		request = constructHTTPRequest(getBASE_URL(), HTTP_METHOD.POST, jsonRequest, logger);
		request.execute();
		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		Assert.assertEquals(SUCCESS, getResponseStatus(jsonResponse, "addMailBoxResponse"));
		AddMailBoxResponseDTO responseDTO = MailBoxUtility.unmarshalFromJSON(jsonResponse, AddMailBoxResponseDTO.class);

		// Constructing the revise
		ReviseMailBoxRequestDTO reviseRequestDTO = new ReviseMailBoxRequestDTO();
		mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), false);
		mbxDTO.setGuid(responseDTO.getMailBox().getGuid());
		reviseRequestDTO.setMailBox(mbxDTO);
		jsonRequest = MailBoxUtility.marshalToJSON(reviseRequestDTO);

		String url = getBASE_URL() + "/" + responseDTO.getMailBox().getGuid();
		request = constructHTTPRequest(url, HTTP_METHOD.PUT, jsonRequest, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);
		Assert.assertEquals(SUCCESS, getResponseStatus(jsonResponse, "reviseMailBoxResponse"));

		// Get the mailbox
		url = getBASE_URL() + "/" + responseDTO.getMailBox().getGuid();
		request = constructHTTPRequest(url, HTTP_METHOD.GET, null, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		GetMailBoxResponseDTO getResponseDTO = MailBoxUtility.unmarshalFromJSON(jsonResponse, GetMailBoxResponseDTO.class);
		Assert.assertEquals(MailBoxStatus.INACTIVE.name(), getResponseDTO.getMailBox().getStatus());

		// Assertion
		Assert.assertEquals(SUCCESS, getResponseStatus(jsonResponse, "getMailBoxResponse"));
		Assert.assertEquals(mbxDTO.getName(), getResponseDTO.getMailBox().getName());
		Assert.assertEquals(mbxDTO.getDescription(), getResponseDTO.getMailBox().getDescription());
		Assert.assertEquals(mbxDTO.getServiceInstId(), getResponseDTO.getMailBox().getServiceInstId());
		Assert.assertEquals(mbxDTO.getShardKey(), getResponseDTO.getMailBox().getShardKey());
		Assert.assertEquals(MailBoxStatus.ACTIVE.name(), getResponseDTO.getMailBox().getStatus());

		Assert.assertEquals(mbxDTO.getProperties().get(0).getName(),
				getResponseDTO.getMailBox().getProperties().get(0).getName());
		Assert.assertEquals(mbxDTO.getProperties().get(0).getValue(),
				getResponseDTO.getMailBox().getProperties().get(0).getValue());

	}

	@Test
	public void testReviseMailBoxWithApacheHTTPClient() throws ClientProtocolException, IOException, Exception {

		// Adding the mailbox
		AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();

		MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
		requestDTO.setMailBox(mbxDTO);

		jsonRequest = MailBoxUtility.marshalToJSON(requestDTO);
		request = constructHTTPRequest(getBASE_URL(), HTTP_METHOD.POST, jsonRequest, logger);
		request.execute();
		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		Assert.assertEquals(SUCCESS, getResponseStatus(jsonResponse, "addMailBoxResponse"));
		AddMailBoxResponseDTO responseDTO = MailBoxUtility.unmarshalFromJSON(jsonResponse, AddMailBoxResponseDTO.class);

		// Constructing the revise
		String url = getBASE_URL() + "/" + responseDTO.getMailBox().getGuid();
		ReviseMailBoxRequestDTO reviseRequestDTO = new ReviseMailBoxRequestDTO();
		mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), false);
		mbxDTO.setGuid(responseDTO.getMailBox().getGuid());
		reviseRequestDTO.setMailBox(mbxDTO);

		jsonRequest = MailBoxUtility.marshalToJSON(reviseRequestDTO);

		HttpParams params = new BasicHttpParams();
		HttpClient httpClient = new DefaultHttpClient(params);
		HttpResponse response = httpClient.execute(getHttpPut(url, jsonRequest));
		Assert.assertEquals(true, response.getStatusLine().getStatusCode() == 200);
		String responseString = new BasicResponseHandler().handleResponse(response);
		System.out.println(responseString);
		Assert.assertEquals(SUCCESS, getResponseStatus(responseString, "reviseMailBoxResponse"));

		// Get the mailbox
		url = getBASE_URL() + "/" + responseDTO.getMailBox().getGuid();
		request = constructHTTPRequest(url, HTTP_METHOD.GET, null, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		GetMailBoxResponseDTO getResponseDTO = MailBoxUtility.unmarshalFromJSON(jsonResponse, GetMailBoxResponseDTO.class);

		// Assertion
		Assert.assertEquals(SUCCESS, getResponseStatus(jsonResponse, "getMailBoxResponse"));
		Assert.assertEquals(mbxDTO.getName(), getResponseDTO.getMailBox().getName());
		Assert.assertEquals(mbxDTO.getDescription(), getResponseDTO.getMailBox().getDescription());
		Assert.assertEquals(mbxDTO.getServiceInstId(), getResponseDTO.getMailBox().getServiceInstId());
		Assert.assertEquals(mbxDTO.getShardKey(), getResponseDTO.getMailBox().getShardKey());
		Assert.assertEquals(MailBoxStatus.ACTIVE.name(), getResponseDTO.getMailBox().getStatus());

		Assert.assertEquals(mbxDTO.getProperties().get(0).getName(),
				getResponseDTO.getMailBox().getProperties().get(0).getName());
		Assert.assertEquals(mbxDTO.getProperties().get(0).getValue(),
				getResponseDTO.getMailBox().getProperties().get(0).getValue());

	}

	private HttpPut getHttpPut(String url, String jsonString) throws Exception {

		HttpPut httpPut = new HttpPut(url);

		httpPut.setHeader("Content-Type", "application/json");
		StringEntity entity = new StringEntity(jsonString);
		httpPut.setEntity(entity);
		return httpPut;
	}
}
