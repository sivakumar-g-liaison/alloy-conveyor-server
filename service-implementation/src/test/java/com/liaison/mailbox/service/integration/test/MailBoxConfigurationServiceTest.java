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

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
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
import com.liaison.mailbox.service.base.test.BaseServiceTest;
import com.liaison.mailbox.service.dto.configuration.request.ReviseMailBoxRequestDTO;
import com.liaison.mailbox.service.dto.configuration.response.AddMailBoxResponseDTO;
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
	public void testCreateMailBox() throws MalformedURLException, FileNotFoundException, LiaisonException, JSONException {

		jsonRequest = ServiceUtils.readFileFromClassPath("requests/mailbox/addmailboxrequest.json");

		request = constructHTTPRequest(getBASE_URL(), HTTP_METHOD.POST, jsonRequest, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);
		Assert.assertEquals(SUCCESS, getResponseStatus(jsonResponse, "addMailBoxResponse"));

	}

	@Test
	public void testGetMailBox() throws LiaisonException, JSONException, JsonParseException, JsonMappingException, JAXBException,
			IOException {

		jsonRequest = ServiceUtils.readFileFromClassPath("requests/mailbox/addmailboxrequest.json");

		request = constructHTTPRequest(getBASE_URL(), HTTP_METHOD.POST, jsonRequest, logger);
		request.execute();
		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		Assert.assertEquals(SUCCESS, getResponseStatus(jsonResponse, "addMailBoxResponse"));

		AddMailBoxResponseDTO responseDTO = MailBoxUtility.unmarshalFromJSON(jsonResponse, AddMailBoxResponseDTO.class);

		String url = getBASE_URL() + "/" + responseDTO.getMailBox().getGuid();
		request = constructHTTPRequest(url, HTTP_METHOD.GET, null, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);
		Assert.assertEquals(SUCCESS, getResponseStatus(jsonResponse, "getMailBoxResponse"));

	}

	@Test
	public void testDeactivateMailBox() throws LiaisonException, JSONException, JsonParseException, JsonMappingException,
			JAXBException, IOException {

		jsonRequest = ServiceUtils.readFileFromClassPath("requests/mailbox/addmailboxrequest.json");

		request = constructHTTPRequest(getBASE_URL(), HTTP_METHOD.POST, jsonRequest, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);
		Assert.assertEquals(SUCCESS, getResponseStatus(jsonResponse, "addMailBoxResponse"));

		AddMailBoxResponseDTO responseDTO = MailBoxUtility.unmarshalFromJSON(jsonResponse, AddMailBoxResponseDTO.class);

		String url = getBASE_URL() + "/" + responseDTO.getMailBox().getGuid();
		request = constructHTTPRequest(url, HTTP_METHOD.DELETE, null, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);
		Assert.assertEquals(SUCCESS, getResponseStatus(jsonResponse, "deactivateMailBoxResponse"));

	}

	@Test
	public void testReviseMailBoxWithLiaisonHTTPClient() throws ClientProtocolException, IOException, Exception {

		jsonRequest = ServiceUtils.readFileFromClassPath("requests/mailbox/addmailboxrequest.json");

		request = constructHTTPRequest(getBASE_URL(), HTTP_METHOD.POST, jsonRequest, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);
		Assert.assertEquals(SUCCESS, getResponseStatus(jsonResponse, "addMailBoxResponse"));

		AddMailBoxResponseDTO responseDTO = MailBoxUtility.unmarshalFromJSON(jsonResponse, AddMailBoxResponseDTO.class);

		jsonRequest = ServiceUtils.readFileFromClassPath("requests/mailbox/revisemailboxrequest.json");
		ReviseMailBoxRequestDTO requestDTO = MailBoxUtility.unmarshalFromJSON(jsonRequest, ReviseMailBoxRequestDTO.class);
		requestDTO.getMailBox().setGuid(responseDTO.getMailBox().getGuid());
		jsonRequest = MailBoxUtility.marshalToJSON(requestDTO);

		String url = getBASE_URL() + "/" + responseDTO.getMailBox().getGuid();
		request = constructHTTPRequest(url, HTTP_METHOD.PUT, jsonRequest, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);
		Assert.assertEquals(SUCCESS, getResponseStatus(jsonResponse, "reviseMailBoxResponse"));

	}

	@Test
	public void testReviseMailBoxWithApacheHTTPClient() throws ClientProtocolException, IOException, Exception {

		jsonRequest = ServiceUtils.readFileFromClassPath("requests/mailbox/addmailboxrequest.json");

		request = constructHTTPRequest(getBASE_URL(), HTTP_METHOD.POST, jsonRequest, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);
		Assert.assertEquals(SUCCESS, getResponseStatus(jsonResponse, "addMailBoxResponse"));

		AddMailBoxResponseDTO responseDTO = MailBoxUtility.unmarshalFromJSON(jsonResponse, AddMailBoxResponseDTO.class);

		jsonRequest = ServiceUtils.readFileFromClassPath("requests/mailbox/revisemailboxrequest.json");

		String url = getBASE_URL() + "/" + responseDTO.getMailBox().getGuid();

		ReviseMailBoxRequestDTO requestDTO = MailBoxUtility.unmarshalFromJSON(jsonRequest, ReviseMailBoxRequestDTO.class);
		requestDTO.getMailBox().setGuid(responseDTO.getMailBox().getGuid());

		jsonRequest = MailBoxUtility.marshalToJSON(requestDTO);

		HttpParams params = new BasicHttpParams();
		HttpClient httpClient = new DefaultHttpClient(params);
		HttpResponse response = httpClient.execute(getHttpPut(url, jsonRequest));
		Assert.assertEquals(true, response.getStatusLine().getStatusCode() == 200);

	}

	private HttpPut getHttpPut(String url, String jsonString) throws Exception {

		HttpPut httpPut = new HttpPut(url);

		httpPut.setHeader("Content-Type", "application/json");
		StringEntity entity = new StringEntity(jsonString);
		httpPut.setEntity(entity);
		return httpPut;
	}
}
