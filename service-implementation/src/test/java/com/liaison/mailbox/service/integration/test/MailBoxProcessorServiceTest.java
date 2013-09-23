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
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.codehaus.jackson.JsonGenerationException;
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
import com.liaison.mailbox.service.dto.configuration.ProfileDTO;
import com.liaison.mailbox.service.dto.configuration.request.AddMailboxRequestDTO;
import com.liaison.mailbox.service.dto.configuration.request.AddProcessorToMailboxRequestDTO;
import com.liaison.mailbox.service.dto.configuration.request.AddProfileRequestDTO;
import com.liaison.mailbox.service.dto.configuration.request.ReviseProcessorRequestDTO;
import com.liaison.mailbox.service.dto.configuration.response.AddMailBoxResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.AddProcessorToMailboxResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.AddProfileResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.AddProfileToMailBoxResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.GetProcessorResponseDTO;
import com.liaison.mailbox.service.util.MailBoxUtility;

/**
 * @author karthikeyanm
 * 
 */
public class MailBoxProcessorServiceTest extends BaseServiceTest {

	private String jsonResponse;
	private String jsonRequest;
	private HTTPRequest request;

	private Logger logger = null;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		logger = LoggerFactory.getLogger(MailBoxProcessorServiceTest.class);
	}

	@Test
	public void testAddProcessorToMailBox() throws LiaisonException, JSONException, JsonParseException, JsonMappingException,
			JAXBException, IOException {

		// Add MailBox
		AddMailBoxResponseDTO responseDTO = createMailBox();

		// Add the Profile
		AddProfileResponseDTO profileResponseDTO = addProfile();

		// Add MailBoxSched Profile
		AddProfileToMailBoxResponseDTO mbProfileResponseDTO = createProfileLink(responseDTO, profileResponseDTO);

		// Add Processor
		String jsonRequest = ServiceUtils.readFileFromClassPath("requests/processor/createprocessor.json");
		AddProcessorToMailboxRequestDTO addProcessorDTO = MailBoxUtility.unmarshalFromJSON(jsonRequest,
				AddProcessorToMailboxRequestDTO.class);

		addProcessorDTO.getProcessor().setLinkedMailboxId(responseDTO.getMailBox().getGuid());
		addProcessorDTO.getProcessor().setLinkedProfileId(mbProfileResponseDTO.getMailboxProfileLinkGuid());

		jsonRequest = MailBoxUtility.marshalToJSON(addProcessorDTO);

		String addProcessor = "/" + responseDTO.getMailBox().getGuid() + "/processor";
		request = constructHTTPRequest(getBASE_URL() + addProcessor, HTTP_METHOD.POST, jsonRequest, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		AddProcessorToMailboxResponseDTO processorResponseDTO = MailBoxUtility.unmarshalFromJSON(jsonResponse,
				AddProcessorToMailboxResponseDTO.class);
		Assert.assertEquals(SUCCESS, processorResponseDTO.getResponse().getStatus());

		// Get Processor
		String getProcessor = "/" + responseDTO.getMailBox().getGuid() + "/processor" + "/"
				+ processorResponseDTO.getProcessor().getGuId();
		request = constructHTTPRequest(getBASE_URL() + getProcessor, HTTP_METHOD.GET, null, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		GetProcessorResponseDTO getResponseDTO = MailBoxUtility.unmarshalFromJSON(jsonResponse, GetProcessorResponseDTO.class);

		Assert.assertEquals(SUCCESS, getResponseDTO.getResponse().getStatus());
		Assert.assertEquals(addProcessorDTO.getProcessor().getName(), getResponseDTO.getProcessor().getName());
		Assert.assertEquals(addProcessorDTO.getProcessor().getType(), getResponseDTO.getProcessor().getType());
		Assert.assertEquals(addProcessorDTO.getProcessor().getStatus(), getResponseDTO.getProcessor().getStatus());
		Assert.assertEquals(addProcessorDTO.getProcessor().getJavaScriptURI(), getResponseDTO.getProcessor().getJavaScriptURI());

	}

	@Test
	public void testReadProcessor() throws LiaisonException, JSONException, JsonParseException, JsonMappingException,
			JAXBException,
			IOException {

		AddMailBoxResponseDTO responseDTO = createMailBox();

		AddProfileResponseDTO profileResponseDTO = addProfile();

		AddProfileToMailBoxResponseDTO mbProfileResponseDTO = createProfileLink(responseDTO, profileResponseDTO);

		// Add Processor
		String jsonRequest = ServiceUtils.readFileFromClassPath("requests/processor/createprocessor.json");
		AddProcessorToMailboxRequestDTO addProcessorDTO = MailBoxUtility.unmarshalFromJSON(jsonRequest,
				AddProcessorToMailboxRequestDTO.class);

		addProcessorDTO.getProcessor().setLinkedMailboxId(responseDTO.getMailBox().getGuid());
		addProcessorDTO.getProcessor().setLinkedProfileId(mbProfileResponseDTO.getMailboxProfileLinkGuid());

		jsonRequest = MailBoxUtility.marshalToJSON(addProcessorDTO);

		String addProcessor = "/" + responseDTO.getMailBox().getGuid() + "/processor";
		request = constructHTTPRequest(getBASE_URL() + addProcessor, HTTP_METHOD.POST, jsonRequest, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		AddProcessorToMailboxResponseDTO processorResponseDTO = MailBoxUtility.unmarshalFromJSON(jsonResponse,
				AddProcessorToMailboxResponseDTO.class);
		Assert.assertEquals(SUCCESS, processorResponseDTO.getResponse().getStatus());

		// Get Processor
		String getProcessor = "/" + responseDTO.getMailBox().getGuid() + "/processor" + "/"
				+ processorResponseDTO.getProcessor().getGuId();
		request = constructHTTPRequest(getBASE_URL() + getProcessor, HTTP_METHOD.GET, null, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);
		GetProcessorResponseDTO getResponseDTO = MailBoxUtility.unmarshalFromJSON(jsonResponse,
				GetProcessorResponseDTO.class);

		Assert.assertEquals(SUCCESS, getResponseDTO.getResponse().getStatus());
		Assert.assertEquals(addProcessorDTO.getProcessor().getName(), getResponseDTO.getProcessor().getName());
		Assert.assertEquals(addProcessorDTO.getProcessor().getType(), getResponseDTO.getProcessor().getType());
		Assert.assertEquals(addProcessorDTO.getProcessor().getStatus(), getResponseDTO.getProcessor().getStatus());
		Assert.assertEquals(addProcessorDTO.getProcessor().getJavaScriptURI(), getResponseDTO.getProcessor().getJavaScriptURI());
	}

	@Test
	public void testReviseProcessorUsingLiaisonHttpClient() throws LiaisonException, JSONException, JsonParseException,
			JsonMappingException, JAXBException, IOException {

		// Adding the mailbox
		AddMailBoxResponseDTO responseDTO = createMailBox();

		AddProfileResponseDTO profileResponseDTO = addProfile();

		AddProfileToMailBoxResponseDTO mbProfileResponseDTO = createProfileLink(responseDTO, profileResponseDTO);

		// Add Processor
		jsonRequest = ServiceUtils.readFileFromClassPath("requests/processor/createprocessor.json");
		AddProcessorToMailboxRequestDTO addProcessorDTO = MailBoxUtility.unmarshalFromJSON(jsonRequest,
				AddProcessorToMailboxRequestDTO.class);

		addProcessorDTO.getProcessor().setLinkedMailboxId(responseDTO.getMailBox().getGuid());
		addProcessorDTO.getProcessor().setLinkedProfileId(mbProfileResponseDTO.getMailboxProfileLinkGuid());

		jsonRequest = MailBoxUtility.marshalToJSON(addProcessorDTO);

		String addProcessor = "/" + responseDTO.getMailBox().getGuid() + "/processor";
		request = constructHTTPRequest(getBASE_URL() + addProcessor, HTTP_METHOD.POST, jsonRequest, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		AddProcessorToMailboxResponseDTO processorResponseDTO = MailBoxUtility.unmarshalFromJSON(jsonResponse,
				AddProcessorToMailboxResponseDTO.class);
		Assert.assertEquals(SUCCESS, processorResponseDTO.getResponse().getStatus());

		// Revise Processor
		jsonRequest = ServiceUtils.readFileFromClassPath("requests/processor/reviseprocessor.json");
		ReviseProcessorRequestDTO reviseProcessorDTO = MailBoxUtility.unmarshalFromJSON(jsonRequest,
				ReviseProcessorRequestDTO.class);

		reviseProcessorDTO.getProcessor().setGuid(processorResponseDTO.getProcessor().getGuId());
		reviseProcessorDTO.getProcessor().setLinkedMailboxId(responseDTO.getMailBox().getGuid());
		reviseProcessorDTO.getProcessor().setLinkedProfileId(mbProfileResponseDTO.getMailboxProfileLinkGuid());

		jsonRequest = MailBoxUtility.marshalToJSON(reviseProcessorDTO);

		String reviseProcessor = "/" + responseDTO.getMailBox().getGuid() + "/processor/"
				+ processorResponseDTO.getProcessor().getGuId();
		request = constructHTTPRequest(getBASE_URL() + reviseProcessor, HTTP_METHOD.PUT, jsonRequest, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		Assert.assertEquals(SUCCESS, getResponseStatus(jsonResponse, "reviseProcessorResponse"));

		// Get Processor
		String getProcessor = "/" + responseDTO.getMailBox().getGuid() + "/processor" + "/"
				+ processorResponseDTO.getProcessor().getGuId();
		request = constructHTTPRequest(getBASE_URL() + getProcessor, HTTP_METHOD.GET, null, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		GetProcessorResponseDTO getResponseDTO = MailBoxUtility.unmarshalFromJSON(jsonResponse,
				GetProcessorResponseDTO.class);

		Assert.assertEquals(SUCCESS, getResponseDTO.getResponse().getStatus());
		Assert.assertEquals(reviseProcessorDTO.getProcessor().getName(), getResponseDTO.getProcessor().getName());
		Assert.assertEquals(reviseProcessorDTO.getProcessor().getType(), getResponseDTO.getProcessor().getType());
		Assert.assertEquals(reviseProcessorDTO.getProcessor().getStatus(), getResponseDTO.getProcessor().getStatus());
		Assert.assertEquals(reviseProcessorDTO.getProcessor().getJavaScriptURI(), getResponseDTO.getProcessor()
				.getJavaScriptURI());
	}

	@Test
	public void testDeActivateProcessor() throws LiaisonException, JSONException, JsonGenerationException, JsonMappingException,
			JAXBException, IOException {

		AddMailBoxResponseDTO responseDTO = createMailBox();

		AddProfileResponseDTO profileResponseDTO = addProfile();

		AddProfileToMailBoxResponseDTO mbProfileResponseDTO = createProfileLink(responseDTO, profileResponseDTO);

		// Add Processor
		String jsonRequest = ServiceUtils.readFileFromClassPath("requests/processor/createprocessor.json");
		AddProcessorToMailboxRequestDTO addProcessorDTO = MailBoxUtility.unmarshalFromJSON(jsonRequest,
				AddProcessorToMailboxRequestDTO.class);

		addProcessorDTO.getProcessor().setLinkedMailboxId(responseDTO.getMailBox().getGuid());
		addProcessorDTO.getProcessor().setLinkedProfileId(mbProfileResponseDTO.getMailboxProfileLinkGuid());

		jsonRequest = MailBoxUtility.marshalToJSON(addProcessorDTO);

		String addProcessor = "/" + responseDTO.getMailBox().getGuid() + "/processor";
		request = constructHTTPRequest(getBASE_URL() + addProcessor, HTTP_METHOD.POST, jsonRequest, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		AddProcessorToMailboxResponseDTO processorResponseDTO = MailBoxUtility.unmarshalFromJSON(jsonResponse,
				AddProcessorToMailboxResponseDTO.class);
		Assert.assertEquals(SUCCESS, processorResponseDTO.getResponse().getStatus());

		// Deactivate the processor
		String deActProcessor = "/" + responseDTO.getMailBox().getGuid() + "/processor" + "/"
				+ processorResponseDTO.getProcessor().getGuId();
		request = constructHTTPRequest(getBASE_URL() + deActProcessor, HTTP_METHOD.DELETE, null, logger);
		request.execute();
		Assert.assertEquals(true,
				getResponseStatus(getOutput().toString(), "deActivateProcessorResponse").equals(SUCCESS));

		// Get Processor
		String getProcessor = "/" + responseDTO.getMailBox().getGuid() + "/processor" + "/"
				+ processorResponseDTO.getProcessor().getGuId();
		request = constructHTTPRequest(getBASE_URL() + getProcessor, HTTP_METHOD.GET, null, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		GetProcessorResponseDTO getResponseDTO = MailBoxUtility.unmarshalFromJSON(jsonResponse,
				GetProcessorResponseDTO.class);

		Assert.assertEquals(SUCCESS, getResponseDTO.getResponse().getStatus());
		Assert.assertEquals(MailBoxStatus.INACTIVE.name(), getResponseDTO.getProcessor().getStatus());
	}

	@Test
	public void testReviseProcessorUsingApacheHttpClient() throws ClientProtocolException, IOException, Exception {

		AddMailBoxResponseDTO responseDTO = createMailBox();

		AddProfileResponseDTO profileResponseDTO = addProfile();

		AddProfileToMailBoxResponseDTO mbProfileResponseDTO = createProfileLink(responseDTO, profileResponseDTO);

		// Add Processor
		jsonRequest = ServiceUtils.readFileFromClassPath("requests/processor/createprocessor.json");
		AddProcessorToMailboxRequestDTO addProcessorDTO = MailBoxUtility.unmarshalFromJSON(jsonRequest,
				AddProcessorToMailboxRequestDTO.class);

		addProcessorDTO.getProcessor().setLinkedMailboxId(responseDTO.getMailBox().getGuid());
		addProcessorDTO.getProcessor().setLinkedProfileId(mbProfileResponseDTO.getMailboxProfileLinkGuid());

		jsonRequest = MailBoxUtility.marshalToJSON(addProcessorDTO);

		String addProcessor = "/" + responseDTO.getMailBox().getGuid() + "/processor";
		request = constructHTTPRequest(getBASE_URL() + addProcessor, HTTP_METHOD.POST, jsonRequest, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		AddProcessorToMailboxResponseDTO processorResponseDTO = MailBoxUtility.unmarshalFromJSON(jsonResponse,
				AddProcessorToMailboxResponseDTO.class);
		Assert.assertEquals(SUCCESS, processorResponseDTO.getResponse().getStatus());

		// Revise Processor
		jsonRequest = ServiceUtils.readFileFromClassPath("requests/processor/reviseprocessor.json");
		ReviseProcessorRequestDTO reviseProcessorDTO = MailBoxUtility.unmarshalFromJSON(jsonRequest,
				ReviseProcessorRequestDTO.class);

		reviseProcessorDTO.getProcessor().setGuid(processorResponseDTO.getProcessor().getGuId());
		reviseProcessorDTO.getProcessor().setLinkedMailboxId(responseDTO.getMailBox().getGuid());
		reviseProcessorDTO.getProcessor().setLinkedProfileId(mbProfileResponseDTO.getMailboxProfileLinkGuid());

		jsonRequest = MailBoxUtility.marshalToJSON(reviseProcessorDTO);

		String reviseProcessor = "/" + responseDTO.getMailBox().getGuid() + "/processor/"
				+ processorResponseDTO.getProcessor().getGuId();

		HttpParams params = new BasicHttpParams();
		HttpClient httpClient = new DefaultHttpClient(params);
		HttpResponse response = httpClient.execute(getHttpPut(getBASE_URL() + reviseProcessor, jsonRequest));
		Assert.assertEquals(true, response.getStatusLine().getStatusCode() == 200);
		String responseString = new BasicResponseHandler().handleResponse(response);
		System.out.println(responseString);
		Assert.assertEquals(SUCCESS, getResponseStatus(responseString, "reviseProcessorResponse"));

		// Get Processor
		String getProcessor = "/" + responseDTO.getMailBox().getGuid() + "/processor" + "/"
				+ processorResponseDTO.getProcessor().getGuId();
		request = constructHTTPRequest(getBASE_URL() + getProcessor, HTTP_METHOD.GET, null, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		GetProcessorResponseDTO getResponseDTO = MailBoxUtility.unmarshalFromJSON(jsonResponse,
				GetProcessorResponseDTO.class);

		Assert.assertEquals(SUCCESS, getResponseDTO.getResponse().getStatus());
		Assert.assertEquals(reviseProcessorDTO.getProcessor().getName(), getResponseDTO.getProcessor().getName());
		Assert.assertEquals(reviseProcessorDTO.getProcessor().getType(), getResponseDTO.getProcessor().getType());
		Assert.assertEquals(reviseProcessorDTO.getProcessor().getStatus(), getResponseDTO.getProcessor().getStatus());
		Assert.assertEquals(reviseProcessorDTO.getProcessor().getJavaScriptURI(), getResponseDTO.getProcessor()
				.getJavaScriptURI());

	}

	private HttpPut getHttpPut(String url, String jsonString) throws Exception {

		HttpPut httpPut = new HttpPut(url);

		httpPut.setHeader("Content-Type", "application/json");
		StringEntity entity = new StringEntity(jsonString);
		httpPut.setEntity(entity);
		return httpPut;
	}

	private AddMailBoxResponseDTO createMailBox() throws JAXBException, JsonParseException, JsonMappingException, IOException,
			JsonGenerationException, MalformedURLException, FileNotFoundException, LiaisonException {

		jsonRequest = ServiceUtils.readFileFromClassPath("requests/mailbox/addmailboxrequest.json");
		AddMailboxRequestDTO requestDTO = MailBoxUtility.unmarshalFromJSON(jsonRequest, AddMailboxRequestDTO.class);

		MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
		requestDTO.setMailBox(mbxDTO);

		jsonRequest = MailBoxUtility.marshalToJSON(requestDTO);
		request = constructHTTPRequest(getBASE_URL(), HTTP_METHOD.POST, jsonRequest, logger);
		request.execute();
		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		AddMailBoxResponseDTO responseDTO = MailBoxUtility.unmarshalFromJSON(jsonResponse, AddMailBoxResponseDTO.class);
		Assert.assertEquals(SUCCESS, responseDTO.getResponse().getStatus());
		return responseDTO;
	}

	private AddProfileToMailBoxResponseDTO createProfileLink(AddMailBoxResponseDTO responseDTO,
			AddProfileResponseDTO profileResponseDTO) throws MalformedURLException, FileNotFoundException, LiaisonException,
			JAXBException, JsonParseException, JsonMappingException, IOException {

		String addProfile = "/" + responseDTO.getMailBox().getGuid() + "/profile/" + profileResponseDTO.getProfile().getGuId();
		request = constructHTTPRequest(getBASE_URL() + addProfile, HTTP_METHOD.POST, jsonRequest,
				LoggerFactory.getLogger(MailBoxProfileServiceTest.class));
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		AddProfileToMailBoxResponseDTO mbProfileResponseDTO = MailBoxUtility.unmarshalFromJSON(jsonResponse,
				AddProfileToMailBoxResponseDTO.class);
		Assert.assertEquals(SUCCESS, mbProfileResponseDTO.getResponse().getStatus());
		return mbProfileResponseDTO;
	}

	private AddProfileResponseDTO addProfile() throws JAXBException, JsonGenerationException, JsonMappingException, IOException,
			MalformedURLException, FileNotFoundException, LiaisonException, JsonParseException {

		String profileName = "once" + System.currentTimeMillis();
		ProfileDTO profile = new ProfileDTO();
		profile.setName(profileName);
		AddProfileRequestDTO profileRequstDTO = new AddProfileRequestDTO();
		profileRequstDTO.setProfile(profile);

		jsonRequest = MailBoxUtility.marshalToJSON(profileRequstDTO);
		request = constructHTTPRequest(getBASE_URL() + "/profile", HTTP_METHOD.POST, jsonRequest, logger);
		request.execute();
		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		AddProfileResponseDTO profileResponseDTO = MailBoxUtility.unmarshalFromJSON(jsonResponse, AddProfileResponseDTO.class);
		Assert.assertEquals(SUCCESS, profileResponseDTO.getResponse().getStatus());
		return profileResponseDTO;
	}
}
