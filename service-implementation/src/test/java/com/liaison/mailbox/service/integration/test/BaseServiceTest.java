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
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;

import com.liaison.commons.util.client.http.HTTPRequest;
import com.liaison.commons.util.client.http.HTTPRequest.HTTP_METHOD;
import com.liaison.commons.util.client.http.HTTPStringData;
import com.liaison.mailbox.service.util.HTTPStringOutputStream;

/**
 * Base Test class for initial setup and cleanup
 * 
 * @author karthikeyanm
 * 
 */
public class BaseServiceTest {

	private static HTTPStringOutputStream output;
	private String BASE_URL;

	@Before
	public void initialSetUp() throws FileNotFoundException, IOException {

		Properties prop = new Properties();
		InputStream is = this.getClass().getResourceAsStream("/config.properties");
		prop.load(is);

		setBASE_URL(prop.getProperty("BASE_URL"));

	}

	public HTTPStringOutputStream getOutput() {
		return output;
	}

	public void setOutput(HTTPStringOutputStream output) {
		BaseServiceTest.output = output;
	}

	@After
	public void finalCleanUp() {

	}

	public HTTPRequest httpRequest(String URL, HTTP_METHOD method, String input, Logger logger) throws MalformedURLException,
			FileNotFoundException {

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

	public String getBASE_URL() {
		return BASE_URL;
	}

	public void setBASE_URL(String bASE_URL) {
		BASE_URL = bASE_URL;
	}

}
