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
import java.net.URL;
import java.util.Properties;

import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import com.liaison.commons.exception.LiaisonException;
import com.liaison.commons.util.client.http.HTTPRequest;
import com.liaison.commons.util.client.http.HTTPRequest.HTTP_METHOD;
import com.liaison.commons.util.client.http.HTTPStringData;
import com.liaison.framework.util.ServiceUtils;
import com.liaison.mailbox.enums.MailBoxStatus;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.service.dto.configuration.MailBoxDTO;
import com.liaison.mailbox.service.dto.configuration.PropertyDTO;
import com.liaison.mailbox.service.util.HTTPStringOutputStream;

/**
 * Base Test class for initial setup and cleanup.
 * 
 * @author OFS
 * 
 */
public abstract class BaseServiceTest {

	private HTTPStringOutputStream output;
	private static String BASE_URL;
	private static String KMS_BASE_URL;

	public static final String SUCCESS = Messages.SUCCESS.value();
	public static final String FAILURE = Messages.FAILURE.value();

	@BeforeMethod
	public void initialSetUp() throws FileNotFoundException, IOException {

		if (BASE_URL == null) {

			Properties prop = new Properties();
			String properties = ServiceUtils.readFileFromClassPath("config.properties");
			InputStream is = new ByteArrayInputStream(properties.getBytes("UTF-8"));
			prop.load(is);

			setBASE_URL(prop.getProperty("BASE_URL"));
			setKMS_BASE_URL(prop.getProperty("KMS_BASE_URL"));
			System.setProperty("archaius.deployment.applicationId", prop.getProperty("APPLICATION_ID"));
            System.setProperty("archaius.deployment.environment", prop.getProperty("ENVIRONMENT"));
			// close the stream
			is.close();
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

	public static String getKMS_BASE_URL() {
		return KMS_BASE_URL;
	}

	public static void setKMS_BASE_URL(String kMS_BASE_URL) {
		KMS_BASE_URL = kMS_BASE_URL;
	}
	
	@AfterMethod
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
	 * @throws LiaisonException
	 * @throws IOException 
	 */
	public HTTPRequest constructHTTPRequest(String URL, HTTP_METHOD method, String input, Logger logger)
			throws LiaisonException, IOException {

		URL url = new URL(URL);
		HTTPRequest request = new HTTPRequest(method, url);
		request.setLogger(logger);
		request.setSocketTimeout(60000);
		request.addHeader("Content-Type", "application/json");
		output = new HTTPStringOutputStream();
		request.setOutputStream(output);
		if (input != null) {
			request.inputData(new HTTPStringData(input));
		}
		output.close();
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

	/**
	 * Construct dummy mailbox DTO for testing.
	 * 
	 * @param uniqueValue
	 * @return
	 */
	public MailBoxDTO constructDummyMailBoxDTO(Long uniqueValue, boolean isCreate) {

		MailBoxDTO mailBoxDTO = new MailBoxDTO();
		PropertyDTO property = new PropertyDTO();

		if (isCreate) {

			mailBoxDTO.setName("MBX_TEST" + uniqueValue);
			mailBoxDTO.setDescription("MBX_TEST_DESCRIPTION" + uniqueValue);
			mailBoxDTO.setShardKey("MBX_SHARD_KEY" + uniqueValue);
			mailBoxDTO.setTenancyKey("MBX_TENANCY_KEY" + uniqueValue);
			mailBoxDTO.setStatus(MailBoxStatus.ACTIVE.name());

			property.setName("MBX_SIZE");
			property.setValue("1024");

		} else {

			mailBoxDTO.setName("MBX_REV_TEST" + uniqueValue);
			mailBoxDTO.setDescription("MBX_REV_TEST_DESCRIPTION" + uniqueValue);
			mailBoxDTO.setShardKey("MBX_REV_SHARD_KEY" + uniqueValue);
			mailBoxDTO.setStatus(MailBoxStatus.ACTIVE.name());
			mailBoxDTO.setTenancyKey("MBX_TENANCY_KEY" + uniqueValue);
			
			property.setName("MBX_REV_SIZE");
			property.setValue("1024");

		}

		mailBoxDTO.getProperties().add(property);
		return mailBoxDTO;
	}

}
