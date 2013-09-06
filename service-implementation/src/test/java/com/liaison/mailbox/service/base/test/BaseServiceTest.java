/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall 
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.mailbox.service.base.test;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;

import com.liaison.commons.util.client.http.HTTPRequest;
import com.liaison.commons.util.client.http.HTTPRequest.HTTP_METHOD;
import com.liaison.commons.util.client.http.HTTPStringData;
import com.liaison.framework.util.ServiceUtils;
import com.liaison.mailbox.service.util.HTTPStringOutputStream;

/**
 * Base Test class for initial setup and cleanup.
 * 
 * @author karthikeyanm
 * 
 */
public abstract class BaseServiceTest {

	private HTTPStringOutputStream output;
	private static String BASE_URL;

	public static final String SUCCESS = "Success";

	@Before
	public void initialSetUp() throws FileNotFoundException, IOException {

		if (BASE_URL == null) {

			Properties prop = new Properties();
			String properties = ServiceUtils.readFileFromClassPath("config.properties");
			InputStream is = new ByteArrayInputStream(properties.getBytes("UTF-8"));
			prop.load(is);

			setBASE_URL(prop.getProperty("BASE_URL"));
		}

	}

	public HTTPStringOutputStream getOutput() {
		return output;
	}

	public void setOutput(HTTPStringOutputStream output) {
		this.output = output;
	}

	public String getBASE_URL() {
		return BASE_URL;
	}

	public void setBASE_URL(String bASE_URL) {
		BASE_URL = bASE_URL;
	}

	@After
	public void finalCleanUp() {

	}

	/**
	 * Constructs HTTPRequest for integration tests.
	 * 
	 * @param URL
	 *            The service URL
	 * @param method
	 *            HTTP verb
	 * @param input
	 *            The request JSON String
	 * @param logger
	 *            The logger
	 * @return HTTPRequest The HTTPRequest instance for the given URL and method
	 * @throws MalformedURLException
	 * @throws FileNotFoundException
	 */
	public HTTPRequest constructHTTPRequest(String URL, HTTP_METHOD method, String input, Logger logger)
			throws MalformedURLException, FileNotFoundException {

		URL url = new URL(URL);
		HTTPRequest request = new HTTPRequest(method, url, logger);
		request.addHeader("Content-Type", "application/json");
		output = new HTTPStringOutputStream();
		request.setOutputStream(output);
		if (input != null) {
			request.inputData(new HTTPStringData(input));
		}
		return request;
	}

	/**
	 * Common method to get the response status from the response JSON String.
	 * 
	 * @param responseString
	 *            The JSON response String
	 * @param serivceName
	 *            The service object name
	 * @return String The status String
	 * @throws JSONException
	 */
	public String getResponseStatus(String responseString, String serivceName) throws JSONException {

		JSONObject rootJson = new JSONObject(responseString);
		JSONObject serviceJson = rootJson.getJSONObject(serivceName);
		JSONObject responseJson = serviceJson.getJSONObject("response");
		return responseJson.getString("status");
	}

	/**
	 * Method constructs the request JSON String into JSONObject.
	 * 
	 * @param requestString
	 *            The requestJSON String.
	 * @param serivceName
	 *            The service object name
	 * @return {@link JSONObject}
	 * @throws JSONException
	 */
	public JSONObject getRequestJson(String requestString, String serivceName) throws JSONException {

		JSONObject rootJson = new JSONObject(requestString);
		JSONObject serviceJson = rootJson.getJSONObject(serivceName);
		return serviceJson;
	}

}
