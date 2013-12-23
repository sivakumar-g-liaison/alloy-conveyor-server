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

import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXBException;

import junit.framework.Assert;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jettison.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Resources;
import com.liaison.commons.exceptions.LiaisonException;
import com.liaison.commons.util.client.http.HTTPRequest;
import com.liaison.commons.util.client.http.HTTPRequest.HTTP_METHOD;
import com.liaison.commons.util.client.http.HTTPResponse;
import com.liaison.framework.util.ServiceUtils;
import com.liaison.mailbox.enums.MailBoxStatus;
import com.liaison.mailbox.enums.Messages;
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
 * @author Rajesh Kumar
 */
public class KeyManagerIntegrationServiceTest extends BaseServiceTest {

	private Logger logger;
	private String jsonResponse;
	private String jsonRequest;
	private HTTPRequest request;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		logger = LoggerFactory.getLogger(KeyManagerIntegrationServiceTest.class);
	}

	//@Test
	public void testCreateTrustStore() throws LiaisonException, JSONException, JsonParseException, JsonMappingException,
			JAXBException, IOException {

		// Adding the mailbox
		jsonRequest = ServiceUtils.readFileFromClassPath("requests/keymanager/truststorerequest.json");
		
		 // prepare post method  
        HttpPost httpPost = new HttpPost("http://10.0.6.101:8080/key-management-1.0.1/upload/truststore"); 
        DefaultHttpClient httpclient = new DefaultHttpClient();
       
        StringBody jsonRequestBody = new StringBody(jsonRequest, ContentType.APPLICATION_JSON);
        FileBody keyStore = new FileBody(new File("C:\\g2truststore.jks"));
        HttpEntity reqEntity = MultipartEntityBuilder.create()
                .addPart("jsonRequest", jsonRequestBody)
                .addPart("keystore", keyStore)
                .build();
               
        httpPost.setEntity(reqEntity);
        HttpResponse response = httpclient.execute(httpPost);
        System.out.println(response.getStatusLine());
	}
	
	//@Test
	public void testUploadPublicKey() throws LiaisonException, JSONException, JsonParseException, JsonMappingException,
			JAXBException, IOException {

		// Adding the mailbox
		jsonRequest = ServiceUtils.readFileFromClassPath("requests/keymanager/publickeyrequest.json");
		
		 // prepare post method  
        HttpPost httpPost = new HttpPost("http://10.0.6.101:8080/key-management-1.0.1/upload/public"); 
        DefaultHttpClient httpclient = new DefaultHttpClient();
       
        StringBody jsonRequestBody = new StringBody(jsonRequest, ContentType.APPLICATION_JSON);
        FileBody publicKeyCert = new FileBody(new File("C:\\publickey.cer"));
        HttpEntity reqEntity = MultipartEntityBuilder.create()
                .addPart("request", jsonRequestBody)
                .addPart("key", publicKeyCert)
                .build();
               
        httpPost.setEntity(reqEntity);
        HttpResponse response = httpclient.execute(httpPost);
        System.out.println(response.getStatusLine());
	}
	
	//@Test
	public void testUpdateTrustStore() throws LiaisonException, JSONException, JsonParseException, JsonMappingException,
			JAXBException, IOException {

		// Adding the mailbox
		jsonRequest = ServiceUtils.readFileFromClassPath("requests/keymanager/truststore_update_request.json");
		
		 // prepare post method  
        HttpPut httpPut = new HttpPut("http://10.0.6.101:8080/key-management-1.0.1/update/truststore/0C3A3BC50A0037B00665D98D2D86079D"); 
        DefaultHttpClient httpclient = new DefaultHttpClient();
        
        httpPut.addHeader("Content-Type", "application/json");
        httpPut.setEntity(new StringEntity(jsonRequest));
        
        HttpResponse response = httpclient.execute(httpPut);
        System.out.println(response.getStatusLine());
	}
	
	//@Test
	public void testFetchTrustStore() throws LiaisonException, JSONException, JsonParseException, JsonMappingException,
			JAXBException, IOException {

		// Get the mailbox
		String url = "http://10.0.6.101:8080/key-management-1.0.1/fetch/truststore/current/0C3A3BC00A0037B00665D98DB3096BC8";
		request = constructHTTPRequest(url, HTTP_METHOD.GET, null, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		System.out.println(jsonResponse);
	}
}
