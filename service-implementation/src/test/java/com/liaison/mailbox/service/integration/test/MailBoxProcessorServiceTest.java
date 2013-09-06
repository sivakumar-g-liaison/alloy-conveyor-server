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
 * @author karthikeyanm
 *
 */
public class MailBoxProcessorServiceTest extends BaseServiceTest {

	private static String processorId;
	private String jsonStr;
	
	private Logger logger = null;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		logger = LoggerFactory.getLogger(MailBoxProcessorServiceTest.class);
	}

	@Test
	public void addProcessorToMailBox() throws MalformedURLException, LiaisonException, FileNotFoundException, JSONException {

		jsonStr = ServiceUtils.readFileFromClassPath("requests/processor/createprocessor.json");
		String addProcessor = "/" + "mbid" + "/processor";
		HTTPRequest request = constructHTTPRequest(getBASE_URL() + addProcessor, HTTP_METHOD.POST, jsonStr, logger);
		request.execute();
		Assert.assertEquals(true, 
				getResponseStatus(getOutput().toString(), "addProcessorToMailBoxResponse").equals(SUCCESS));
	}
	
	@Test
	public void readProcessor() throws MalformedURLException, FileNotFoundException, LiaisonException, JSONException {
		
		processorId = "EED43D87NULL0046NULL5BE63F148E62";
		String getProcessor = "/processor" + "/" + processorId;
		HTTPRequest request = constructHTTPRequest(getBASE_URL() + getProcessor, HTTP_METHOD.GET, null, logger);
		request.execute();
		Assert.assertEquals(true, 
				getResponseStatus(getOutput().toString(), "getProcessorResponse").equals(SUCCESS));
	}
	
	@Test
	public void reviseProcessorUsingLiaisonHttpClient() throws MalformedURLException, LiaisonException, FileNotFoundException, JSONException {

		jsonStr = ServiceUtils.readFileFromClassPath("requests/processor/reviseprocessor.json");
		String revProcessor = "/" + "mbid" + "/processor" + "/" + "pid";
		HTTPRequest request = constructHTTPRequest(getBASE_URL() + revProcessor, HTTP_METHOD.PUT, jsonStr, logger);
		request.execute();
		Assert.assertEquals(true, 
				getResponseStatus(getOutput().toString(), "addProcessorToMailBoxResponse").equals(SUCCESS));
	}
	
	
	@Test
	public void deActivateProcessor() throws MalformedURLException, FileNotFoundException, LiaisonException, JSONException {
		
		processorId = "EED43D87NULL0046NULL5BE63F148E62";
		String deActProcessor = "/" + "mbid" + "/processor" + "/" + processorId;
		HTTPRequest request = constructHTTPRequest(getBASE_URL() + deActProcessor, HTTP_METHOD.DELETE, null, logger);
		request.execute();
		Assert.assertEquals(true, 
				getResponseStatus(getOutput().toString(), "deActivateProcessorResponse").equals(SUCCESS));
	}
	
	@Test
	public void reviseProcessor() throws ClientProtocolException, IOException, Exception {
		
		HttpParams params = new BasicHttpParams();
		HttpClient httpClient =  new DefaultHttpClient(params);
		String revProcessor = "/" + "mbid" + "/processor" + "/" + "pid";
		HttpResponse response = httpClient.execute(
				getHttpPut(getBASE_URL() + revProcessor, ServiceUtils.readFileFromClassPath("requests/processor/reviseprocessor.json")));
		Assert.assertEquals(true, 
				response.getStatusLine().getStatusCode() == 200);
	}
	
	private HttpPut getHttpPut(String url, String jsonString) throws Exception {

		HttpPut httpPut = new HttpPut(url);
		
		httpPut.setHeader("Content-Type", "application/json");
		StringEntity entity = new StringEntity(jsonString);
		httpPut.setEntity(entity);
		return httpPut;
	}
}
