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

import junit.framework.Assert;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
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

/**
 * Test class to test mailbox configuration service.
 * 
 * @author veerasamyn
 */
public class MailBoxConfigurationServiceTest extends BaseServiceTest {

	private Logger logger = null;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		logger = LoggerFactory.getLogger(MailBoxConfigurationServiceTest.class);
	}

	@Test
	public void testCreateMailBox() throws MalformedURLException, FileNotFoundException, LiaisonException, JSONException {

		String jsonString = ServiceUtils.readFileFromClassPath("requests/mailbox/addmailboxrequest.json");

		HTTPRequest request = constructHTTPRequest(getBASE_URL(), HTTP_METHOD.POST, jsonString, logger);
		request.execute();

		Assert.assertEquals(SUCCESS, getResponseStatus(getOutput().toString(), "addMailBoxResponse"));

	}

	@Test
	public void testGetMailBox() throws MalformedURLException, FileNotFoundException, LiaisonException, JSONException {

		String url = getBASE_URL() + "/" + "F346B900NULL0073NULLDAF922B166CD";
		HTTPRequest request = constructHTTPRequest(url, HTTP_METHOD.GET, null, logger);
		request.execute();

		Assert.assertEquals(SUCCESS, getResponseStatus(getOutput().toString(), "getMailBoxResponse"));

	}

	@Test
	public void testDeactivateMailBox() throws MalformedURLException, FileNotFoundException, LiaisonException, JSONException {

		String url = getBASE_URL() + "/" + "F346B900NULL0073NULLDAF922B166CD";
		HTTPRequest request = constructHTTPRequest(url, HTTP_METHOD.DELETE, null, logger);
		request.execute();

		Assert.assertEquals(SUCCESS, getResponseStatus(getOutput().toString(), "deactivateMailBoxResponse"));

	}

	@Test
	public void testReviseMailBoxWithLiaisonHTTPClient() throws ClientProtocolException, IOException, Exception {

		String jsonString = ServiceUtils.readFileFromClassPath("requests/mailbox/revisemailboxrequest.json");

		String url = getBASE_URL() + "/" + "F346B900NULL0073NULLDAF922B166CD";

		HttpParams params = new BasicHttpParams();
		HttpClient httpClient = new DefaultHttpClient(params);
		HttpResponse response = httpClient.execute(getHttpPut(url, jsonString));
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
