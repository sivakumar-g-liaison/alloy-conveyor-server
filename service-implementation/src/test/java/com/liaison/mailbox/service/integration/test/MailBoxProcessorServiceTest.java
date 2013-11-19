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
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import junit.framework.Assert;

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
import com.liaison.mailbox.enums.MailBoxStatus;
import com.liaison.mailbox.service.base.test.BaseServiceTest;
import com.liaison.mailbox.service.dto.configuration.CredentialDTO;
import com.liaison.mailbox.service.dto.configuration.FolderDTO;
import com.liaison.mailbox.service.dto.configuration.MailBoxDTO;
import com.liaison.mailbox.service.dto.configuration.ProcessorDTO;
import com.liaison.mailbox.service.dto.configuration.ProfileDTO;
import com.liaison.mailbox.service.dto.configuration.request.AddMailboxRequestDTO;
import com.liaison.mailbox.service.dto.configuration.request.AddProcessorToMailboxRequestDTO;
import com.liaison.mailbox.service.dto.configuration.request.AddProfileRequestDTO;
import com.liaison.mailbox.service.dto.configuration.request.ReviseProcessorRequestDTO;
import com.liaison.mailbox.service.dto.configuration.response.AddMailBoxResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.AddProcessorToMailboxResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.AddProfileResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.DeActivateProcessorResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.GetProcessorResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.ReviseProcessorResponseDTO;
import com.liaison.mailbox.service.util.MailBoxUtility;

/**
 * @author
 * 
 */
public class MailBoxProcessorServiceTest extends BaseServiceTest {

	private String jsonResponse;
	private String jsonRequest;
	private HTTPRequest request;

	private Logger logger = null;
	private AddMailBoxResponseDTO responseDTO;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		logger = LoggerFactory.getLogger(MailBoxProcessorServiceTest.class);
		// Adding the mailbox
		responseDTO = createMailBox();
	}

	@Test
	public void testCreateProcessorToMailBox() throws LiaisonException, JSONException, JsonParseException, JsonMappingException,
			JAXBException, IOException {

		AddProcessorToMailboxRequestDTO addProcessorDTO = (AddProcessorToMailboxRequestDTO) getProcessorRequest(
				"RESPONSE_LOCATION", "/Sample", "LOGIN_CREDENTIAL", "ACTIVE", "REMOTEDOWNLOADER", false, "HTTP", true);
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
		Assert.assertEquals(false, getResponseDTO.getProcessor().getProfiles().isEmpty());

	}

	@Test
	public void testCreateProcessorToMailBox_WithoutProfile_ShouldPass() throws LiaisonException, JSONException,
			JsonParseException,
			JsonMappingException,
			JAXBException, IOException {

		AddProcessorToMailboxRequestDTO addProcessorDTO = (AddProcessorToMailboxRequestDTO) getProcessorRequest(
				"RESPONSE_LOCATION", "/Sample", "LOGIN_CREDENTIAL", "ACTIVE", "REMOTEDOWNLOADER", false, "HTTP", false);
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
	public void testCreateProcessorToMailBox_WithoutMailBoxId_ShouldFail() throws LiaisonException, JSONException,
			JsonParseException,
			JsonMappingException,
			JAXBException, IOException {

		AddProcessorToMailboxRequestDTO addProcessorDTO = (AddProcessorToMailboxRequestDTO) getProcessorRequest(
				"RESPONSE_LOCATION", "/Sample", "LOGIN_CREDENTIAL", "ACTIVE", "REMOTEDOWNLOADER", false, "HTTP", false);
		addProcessorDTO.getProcessor().setLinkedMailboxId(null);
		jsonRequest = MailBoxUtility.marshalToJSON(addProcessorDTO);

		String addProcessor = "/" + responseDTO.getMailBox().getGuid() + "/processor";
		request = constructHTTPRequest(getBASE_URL() + addProcessor, HTTP_METHOD.POST, jsonRequest, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		AddProcessorToMailboxResponseDTO processorResponseDTO = MailBoxUtility.unmarshalFromJSON(jsonResponse,
				AddProcessorToMailboxResponseDTO.class);
		Assert.assertEquals(FAILURE, processorResponseDTO.getResponse().getStatus());

	}

	@Test
	public void testCreateProcessorToMailBox_WithWorngMailBoxId_ShouldFail() throws LiaisonException, JSONException,
			JsonParseException,
			JsonMappingException,
			JAXBException, IOException {

		AddProcessorToMailboxRequestDTO addProcessorDTO = (AddProcessorToMailboxRequestDTO) getProcessorRequest(
				"RESPONSE_LOCATION", "/Sample", "LOGIN_CREDENTIAL", "ACTIVE", "REMOTEDOWNLOADER", false, "HTTP", false);
		jsonRequest = MailBoxUtility.marshalToJSON(addProcessorDTO);

		String addProcessor = "/" + "1241234123" + "/processor";
		request = constructHTTPRequest(getBASE_URL() + addProcessor, HTTP_METHOD.POST, jsonRequest, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		AddProcessorToMailboxResponseDTO processorResponseDTO = MailBoxUtility.unmarshalFromJSON(jsonResponse,
				AddProcessorToMailboxResponseDTO.class);
		Assert.assertEquals(FAILURE, processorResponseDTO.getResponse().getStatus());

	}

	@Test
	public void testCreateProcessorToMailBox_WithWrongProfile_ShouldFail() throws LiaisonException, JSONException,
			JsonParseException,
			JsonMappingException,
			JAXBException, IOException {

		AddProcessorToMailboxRequestDTO addProcessorDTO = (AddProcessorToMailboxRequestDTO) getProcessorRequest(
				"RESPONSE_LOCATION", "/Sample", "LOGIN_CREDENTIAL", "ACTIVE", "REMOTEDOWNLOADER", false, "HTTP", true);
		addProcessorDTO.getProcessor().getLinkedProfiles().set(0, "dummy profiles");
		jsonRequest = MailBoxUtility.marshalToJSON(addProcessorDTO);

		String addProcessor = "/" + responseDTO.getMailBox().getGuid() + "/processor";
		request = constructHTTPRequest(getBASE_URL() + addProcessor, HTTP_METHOD.POST, jsonRequest, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		AddProcessorToMailboxResponseDTO processorResponseDTO = MailBoxUtility.unmarshalFromJSON(jsonResponse,
				AddProcessorToMailboxResponseDTO.class);
		Assert.assertEquals(FAILURE, processorResponseDTO.getResponse().getStatus());

	}

	@Test
	public void testCreateProcessorToMailBox_WithoutProtocol_ShouldFail() throws LiaisonException, JSONException,
			JsonParseException,
			JsonMappingException,
			JAXBException, IOException {

		AddProcessorToMailboxRequestDTO addProcessorDTO = (AddProcessorToMailboxRequestDTO) getProcessorRequest(
				"RESPONSE_LOCATION", "/Sample", "LOGIN_CREDENTIAL", "ACTIVE", "REMOTEDOWNLOADER", false, null, true);
		jsonRequest = MailBoxUtility.marshalToJSON(addProcessorDTO);

		String addProcessor = "/" + responseDTO.getMailBox().getGuid() + "/processor";
		request = constructHTTPRequest(getBASE_URL() + addProcessor, HTTP_METHOD.POST, jsonRequest, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		AddProcessorToMailboxResponseDTO processorResponseDTO = MailBoxUtility.unmarshalFromJSON(jsonResponse,
				AddProcessorToMailboxResponseDTO.class);
		Assert.assertEquals(FAILURE, processorResponseDTO.getResponse().getStatus());

	}

	@Test
	public void testCreateProcessorToMailBox_WithWrongProtocol_ShouldFail() throws LiaisonException, JSONException,
			JsonParseException,
			JsonMappingException,
			JAXBException, IOException {

		AddProcessorToMailboxRequestDTO addProcessorDTO = (AddProcessorToMailboxRequestDTO) getProcessorRequest(
				"RESPONSE_LOCATION", "/Sample", "LOGIN_CREDENTIAL", "ACTIVE", "REMOTEDOWNLOADER", false, "TEST", true);
		jsonRequest = MailBoxUtility.marshalToJSON(addProcessorDTO);

		String addProcessor = "/" + responseDTO.getMailBox().getGuid() + "/processor";
		request = constructHTTPRequest(getBASE_URL() + addProcessor, HTTP_METHOD.POST, jsonRequest, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		AddProcessorToMailboxResponseDTO processorResponseDTO = MailBoxUtility.unmarshalFromJSON(jsonResponse,
				AddProcessorToMailboxResponseDTO.class);
		Assert.assertEquals(FAILURE, processorResponseDTO.getResponse().getStatus());

	}

	@Test
	public void testReadProcessor_NullValueInURI_ShouldFail() throws LiaisonException, JSONException,
			JsonParseException,
			JsonMappingException, JAXBException, IOException {

		// Get Processor
		String getProcessor = "/" + null + "/processor" + "/" + null;
		request = constructHTTPRequest(getBASE_URL() + getProcessor, HTTP_METHOD.GET, null, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		GetProcessorResponseDTO getResponseDTO = MailBoxUtility.unmarshalFromJSON(jsonResponse, GetProcessorResponseDTO.class);

		Assert.assertEquals(FAILURE, getResponseDTO.getResponse().getStatus());

	}

	@Test
	public void testCreateProcessorToMailBox_MandotoryValuesOnly_ShouldPass() throws LiaisonException, JSONException,
			JsonParseException,
			JsonMappingException, JAXBException, IOException {

		jsonRequest = MailBoxUtility.marshalToJSON(getProcessorRequest("RESPONSE_LOCATION", "/Sample", "LOGIN_CREDENTIAL",
				"ACTIVE",
				"REMOTEDOWNLOADER", false, "HTTP", true));

		String addProcessor = "/" + responseDTO.getMailBox().getGuid() + "/processor";
		request = constructHTTPRequest(getBASE_URL() + addProcessor, HTTP_METHOD.POST, jsonRequest, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		AddProcessorToMailboxResponseDTO processorResponseDTO = MailBoxUtility.unmarshalFromJSON(jsonResponse,
				AddProcessorToMailboxResponseDTO.class);
		Assert.assertEquals(SUCCESS, processorResponseDTO.getResponse().getStatus());

	}

	@Test
	public void testCreateProcessorToMailBox_WithInvalidMandatoryValues_ShouldFail() throws JAXBException,
			JsonGenerationException,
			JsonMappingException, IOException, MalformedURLException, FileNotFoundException, LiaisonException, JsonParseException {

		jsonRequest = MailBoxUtility
				.marshalToJSON(getProcessorRequest("ddd755", "/Sample", "LOGIN_CREDENTIAL", "fdfd", "85964", false, "HTTP", true));

		String addProcessor = "/" + responseDTO.getMailBox().getGuid() + "/processor";
		request = constructHTTPRequest(getBASE_URL() + addProcessor, HTTP_METHOD.POST, jsonRequest, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		AddProcessorToMailboxResponseDTO processorResponseDTO = MailBoxUtility.unmarshalFromJSON(jsonResponse,
				AddProcessorToMailboxResponseDTO.class);
		Assert.assertEquals(FAILURE, processorResponseDTO.getResponse().getStatus());
	}

	@Test
	public void testCreateProcessorToMailBox_WithMandatoryValuesAsNull__ShouldFail() throws JAXBException,
			JsonGenerationException,
			JsonMappingException, IOException, MalformedURLException, FileNotFoundException, LiaisonException, JsonParseException {

		jsonRequest = MailBoxUtility.marshalToJSON(getProcessorRequest(null, null, null, null, null, false, null, true));

		String addProcessor = "/" + responseDTO.getMailBox().getGuid() + "/processor";
		request = constructHTTPRequest(getBASE_URL() + addProcessor, HTTP_METHOD.POST, jsonRequest, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		AddProcessorToMailboxResponseDTO processorResponseDTO = MailBoxUtility.unmarshalFromJSON(jsonResponse,
				AddProcessorToMailboxResponseDTO.class);
		Assert.assertEquals(FAILURE, processorResponseDTO.getResponse().getStatus());
	}

	@Test
	public void testReadProcessor() throws LiaisonException, JSONException, JsonParseException, JsonMappingException,
			JAXBException, IOException {

		AddProcessorToMailboxRequestDTO addProcessorDTO = (AddProcessorToMailboxRequestDTO) getProcessorRequest(
				"RESPONSE_LOCATION", "/Sample", "LOGIN_CREDENTIAL", "ACTIVE", "REMOTEDOWNLOADER", false, "HTTP", true);
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
		GetProcessorResponseDTO getResponseDTO = MailBoxUtility.unmarshalFromJSON(jsonResponse, GetProcessorResponseDTO.class);

		Assert.assertEquals(SUCCESS, getResponseDTO.getResponse().getStatus());
		Assert.assertEquals(addProcessorDTO.getProcessor().getName(), getResponseDTO.getProcessor().getName());
		Assert.assertEquals(addProcessorDTO.getProcessor().getType(), getResponseDTO.getProcessor().getType());
		Assert.assertEquals(addProcessorDTO.getProcessor().getStatus(), getResponseDTO.getProcessor().getStatus());
		Assert.assertEquals(addProcessorDTO.getProcessor().getJavaScriptURI(), getResponseDTO.getProcessor().getJavaScriptURI());
	}

	@Test
	public void testReadProcessor_WithInvalidProcessorId_ShouldFail() throws LiaisonException, JSONException, JsonParseException,
			JsonMappingException,
			JAXBException, IOException {

		String getProcessor = "/" + responseDTO.getMailBox().getGuid() + "/processor" + "/" + "DummyId";
		request = constructHTTPRequest(getBASE_URL() + getProcessor, HTTP_METHOD.GET, null, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);
		GetProcessorResponseDTO getResponseDTO = MailBoxUtility.unmarshalFromJSON(jsonResponse, GetProcessorResponseDTO.class);

		Assert.assertEquals(FAILURE, getResponseDTO.getResponse().getStatus());
	}

	@Test
	public void testReadProcessor_WithInvalidMailBox_ShouldFail() throws LiaisonException, JSONException, JsonParseException,
			JsonMappingException,
			JAXBException, IOException {

		AddProcessorToMailboxRequestDTO addProcessorDTO = (AddProcessorToMailboxRequestDTO) getProcessorRequest(
				"RESPONSE_LOCATION", "/Sample", "LOGIN_CREDENTIAL", "ACTIVE", "REMOTEDOWNLOADER", false, "HTTP", true);
		jsonRequest = MailBoxUtility.marshalToJSON(addProcessorDTO);

		String addProcessor = "/" + responseDTO.getMailBox().getGuid() + "/processor";
		request = constructHTTPRequest(getBASE_URL() + addProcessor, HTTP_METHOD.POST, jsonRequest, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		AddProcessorToMailboxResponseDTO processorResponseDTO = MailBoxUtility.unmarshalFromJSON(jsonResponse,
				AddProcessorToMailboxResponseDTO.class);
		Assert.assertEquals(SUCCESS, processorResponseDTO.getResponse().getStatus());

		AddProcessorToMailboxRequestDTO addSecProcessorDTO = (AddProcessorToMailboxRequestDTO) getProcessorRequest(
				"RESPONSE_LOCATION", "/Sample", "LOGIN_CREDENTIAL", "ACTIVE", "REMOTEDOWNLOADER", false, "HTTP", true);
		jsonRequest = MailBoxUtility.marshalToJSON(addSecProcessorDTO);

		String addProcessorReq = "/" + responseDTO.getMailBox().getGuid() + "/processor";
		request = constructHTTPRequest(getBASE_URL() + addProcessorReq, HTTP_METHOD.POST, jsonRequest, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		AddProcessorToMailboxResponseDTO processorSecResponseDTO = MailBoxUtility.unmarshalFromJSON(jsonResponse,
				AddProcessorToMailboxResponseDTO.class);
		Assert.assertEquals(SUCCESS, processorSecResponseDTO.getResponse().getStatus());

		// Get Processor
		String getProcessor = "/" + responseDTO.getMailBox().getGuid() + "/processor" + "/"
				+ processorResponseDTO.getProcessor().getGuId();
		request = constructHTTPRequest(getBASE_URL() + getProcessor, HTTP_METHOD.GET, null, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);
		GetProcessorResponseDTO getResponseDTO = MailBoxUtility.unmarshalFromJSON(jsonResponse, GetProcessorResponseDTO.class);

		Assert.assertEquals(SUCCESS, getResponseDTO.getResponse().getStatus());
		Assert.assertEquals(addProcessorDTO.getProcessor().getName(), getResponseDTO.getProcessor().getName());
		Assert.assertEquals(addProcessorDTO.getProcessor().getType(), getResponseDTO.getProcessor().getType());
		Assert.assertEquals(addProcessorDTO.getProcessor().getStatus(), getResponseDTO.getProcessor().getStatus());
		Assert.assertEquals(addProcessorDTO.getProcessor().getJavaScriptURI(), getResponseDTO.getProcessor().getJavaScriptURI());
	}

	@Test
	public void testReviseProcessor() throws LiaisonException, JSONException, JsonParseException,
			JsonMappingException, JAXBException, IOException {

		AddProcessorToMailboxRequestDTO addProcessorDTO = (AddProcessorToMailboxRequestDTO) getProcessorRequest(
				"RESPONSE_LOCATION", "/Sample", "LOGIN_CREDENTIAL", "ACTIVE", "REMOTEDOWNLOADER", false, "HTTP", true);
		jsonRequest = MailBoxUtility.marshalToJSON(addProcessorDTO);

		String addProcessor = "/" + responseDTO.getMailBox().getGuid() + "/processor";
		request = constructHTTPRequest(getBASE_URL() + addProcessor, HTTP_METHOD.POST, jsonRequest, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		AddProcessorToMailboxResponseDTO processorResponseDTO = MailBoxUtility.unmarshalFromJSON(jsonResponse,
				AddProcessorToMailboxResponseDTO.class);
		Assert.assertEquals(SUCCESS, processorResponseDTO.getResponse().getStatus());

		ReviseProcessorRequestDTO reviseProcessorDTO = (ReviseProcessorRequestDTO) getProcessorRequest("RESPONSE_LOCATION",
				"/Sample", "LOGIN_CREDENTIAL", "ACTIVE", "REMOTEDOWNLOADER", true, "HTTP", true);
		reviseProcessorDTO.getProcessor().setGuid(processorResponseDTO.getProcessor().getGuId());
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
		GetProcessorResponseDTO getResponseDTO = MailBoxUtility.unmarshalFromJSON(jsonResponse, GetProcessorResponseDTO.class);

		Assert.assertEquals(SUCCESS, getResponseDTO.getResponse().getStatus());
		Assert.assertEquals(reviseProcessorDTO.getProcessor().getName(), getResponseDTO.getProcessor().getName());
		Assert.assertEquals(reviseProcessorDTO.getProcessor().getType(), getResponseDTO.getProcessor().getType());
		Assert.assertEquals(reviseProcessorDTO.getProcessor().getStatus(), getResponseDTO.getProcessor().getStatus());
		Assert.assertEquals(reviseProcessorDTO.getProcessor().getJavaScriptURI(), getResponseDTO.getProcessor()
				.getJavaScriptURI());
	}

	@Test
	public void testReviseProcessor_WithoutProtocol_ShouldFail() throws LiaisonException, JSONException, JsonParseException,
			JsonMappingException, JAXBException, IOException {

		ReviseProcessorRequestDTO reviseProcessorDTO = (ReviseProcessorRequestDTO) getProcessorRequest("RESPONSE_LOCATION",
				"/Sample", "LOGIN_CREDENTIAL", "ACTIVE", "REMOTEDOWNLOADER", true, null, true);
		reviseProcessorDTO.getProcessor().setGuid("123456");
		jsonRequest = MailBoxUtility.marshalToJSON(reviseProcessorDTO);

		String reviseProcessor = "/" + responseDTO.getMailBox().getGuid() + "/processor/"
				+ "123456";
		request = constructHTTPRequest(getBASE_URL() + reviseProcessor, HTTP_METHOD.PUT, jsonRequest, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		Assert.assertEquals(FAILURE, getResponseStatus(jsonResponse, "reviseProcessorResponse"));

	}

	@Test
	public void testReviseProcessor_WithInvalidProtocol_ShouldFail() throws LiaisonException, JSONException, JsonParseException,
			JsonMappingException, JAXBException, IOException {

		ReviseProcessorRequestDTO reviseProcessorDTO = (ReviseProcessorRequestDTO) getProcessorRequest("RESPONSE_LOCATION",
				"/Sample", "LOGIN_CREDENTIAL", "ACTIVE", "REMOTEDOWNLOADER", true, "Test", true);
		reviseProcessorDTO.getProcessor().setGuid("123456");
		jsonRequest = MailBoxUtility.marshalToJSON(reviseProcessorDTO);

		String reviseProcessor = "/" + responseDTO.getMailBox().getGuid() + "/processor/"
				+ "123456";
		request = constructHTTPRequest(getBASE_URL() + reviseProcessor, HTTP_METHOD.PUT, jsonRequest, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		Assert.assertEquals(FAILURE, getResponseStatus(jsonResponse, "reviseProcessorResponse"));

	}

	@Test
	public void testReviseProcessor_WithoutMailBoxId_ShouldFail() throws LiaisonException, JSONException, JsonParseException,
			JsonMappingException,
			JAXBException, IOException {

		ReviseProcessorRequestDTO reviseProcessorDTO = (ReviseProcessorRequestDTO) getProcessorRequest(
				"RESPONSE_LOCATION", "/Sample", "LOGIN_CREDENTIAL", "ACTIVE", "REMOTEDOWNLOADER", true, "HTTP", false);
		reviseProcessorDTO.getProcessor().setLinkedMailboxId(null);
		reviseProcessorDTO.getProcessor().setGuid("21412341234");
		jsonRequest = MailBoxUtility.marshalToJSON(reviseProcessorDTO);

		String reviseProcessor = "/" + responseDTO.getMailBox().getGuid() + "/processor/"
				+ "123456";
		request = constructHTTPRequest(getBASE_URL() + reviseProcessor, HTTP_METHOD.PUT, jsonRequest, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		ReviseProcessorResponseDTO processorResponseDTO = MailBoxUtility.unmarshalFromJSON(jsonResponse,
				ReviseProcessorResponseDTO.class);
		Assert.assertEquals(FAILURE, processorResponseDTO.getResponse().getStatus());

	}

	@Test
	public void testReviseProcessor_WithNullValueInURI_ShouldFail() throws LiaisonException, JSONException, JsonParseException,
			JsonMappingException,
			JAXBException, IOException {

		ReviseProcessorRequestDTO reviseProcessorDTO = (ReviseProcessorRequestDTO) getProcessorRequest(
				"RESPONSE_LOCATION", "/Sample", "LOGIN_CREDENTIAL", "ACTIVE", "REMOTEDOWNLOADER", true, "HTTP", false);
		reviseProcessorDTO.getProcessor().setLinkedMailboxId("null");
		reviseProcessorDTO.getProcessor().setGuid("dfasfasfdafd");
		jsonRequest = MailBoxUtility.marshalToJSON(reviseProcessorDTO);

		String reviseProcessor = "/" + null + "/processor/" + null;
		request = constructHTTPRequest(getBASE_URL() + reviseProcessor, HTTP_METHOD.PUT, jsonRequest, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		ReviseProcessorResponseDTO processorResponseDTO = MailBoxUtility.unmarshalFromJSON(jsonResponse,
				ReviseProcessorResponseDTO.class);
		Assert.assertEquals(FAILURE, processorResponseDTO.getResponse().getStatus());

	}

	@Test
	public void testReviseProcessor_WithWorngMailBoxId_ShouldFail() throws LiaisonException, JSONException, JsonParseException,
			JsonMappingException,
			JAXBException, IOException {

		// Add
		AddProcessorToMailboxRequestDTO addProcessorDTO = (AddProcessorToMailboxRequestDTO) getProcessorRequest(
				"RESPONSE_LOCATION", "/Sample", "LOGIN_CREDENTIAL", "ACTIVE", "REMOTEDOWNLOADER", false, "HTTP", true);
		jsonRequest = MailBoxUtility.marshalToJSON(addProcessorDTO);

		String addProcessor = "/" + responseDTO.getMailBox().getGuid() + "/processor";
		request = constructHTTPRequest(getBASE_URL() + addProcessor, HTTP_METHOD.POST, jsonRequest, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		AddProcessorToMailboxResponseDTO processorResponseDTO = MailBoxUtility.unmarshalFromJSON(jsonResponse,
				AddProcessorToMailboxResponseDTO.class);
		Assert.assertEquals(SUCCESS, processorResponseDTO.getResponse().getStatus());

		// Revise
		ReviseProcessorRequestDTO reviseProcessorDTO = (ReviseProcessorRequestDTO) getProcessorRequest(
				"RESPONSE_LOCATION", "/Sample", "LOGIN_CREDENTIAL", "ACTIVE", "REMOTEDOWNLOADER", true, "HTTP", false);
		jsonRequest = MailBoxUtility.marshalToJSON(reviseProcessorDTO);
		reviseProcessorDTO.getProcessor().setGuid(processorResponseDTO.getProcessor().getGuId());

		String reviseProcessor = "/" + addProcessorDTO.getProcessor().getLinkedMailboxId() + "/processor/"
				+ "123456";
		request = constructHTTPRequest(getBASE_URL() + reviseProcessor, HTTP_METHOD.PUT, jsonRequest, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		ReviseProcessorResponseDTO revProcessorResponseDTO = MailBoxUtility.unmarshalFromJSON(jsonResponse,
				ReviseProcessorResponseDTO.class);
		Assert.assertEquals(FAILURE, revProcessorResponseDTO.getResponse().getStatus());

	}

	@Test
	public void testReviseProcessor_WithoutProfile_ShouldFail() throws LiaisonException, JSONException, JsonParseException,
			JsonMappingException,
			JAXBException, IOException {

		// AddProcessor
		AddProcessorToMailboxRequestDTO addProcessorDTO = (AddProcessorToMailboxRequestDTO) getProcessorRequest(
				"RESPONSE_LOCATION", "/Sample", "LOGIN_CREDENTIAL", "ACTIVE", "REMOTEDOWNLOADER", false, "HTTP", true);
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
		GetProcessorResponseDTO getResponseDTO = MailBoxUtility.unmarshalFromJSON(jsonResponse, GetProcessorResponseDTO.class);

		Assert.assertEquals(SUCCESS, getResponseDTO.getResponse().getStatus());
		Assert.assertEquals(false, getResponseDTO.getProcessor().getProfiles().isEmpty());

		// Revise
		ReviseProcessorRequestDTO reviseProcessorDTO = (ReviseProcessorRequestDTO) getProcessorRequest(
				"RESPONSE_LOCATION", "/Sample", "LOGIN_CREDENTIAL", "ACTIVE", "REMOTEDOWNLOADER", true, "HTTP", false);
		reviseProcessorDTO.getProcessor().setGuid(processorResponseDTO.getProcessor().getGuId());

		jsonRequest = MailBoxUtility.marshalToJSON(reviseProcessorDTO);
		String reviseProcessor = "/" + reviseProcessorDTO.getProcessor().getLinkedMailboxId() + "/processor/"
				+ processorResponseDTO.getProcessor().getGuId();
		request = constructHTTPRequest(getBASE_URL() + reviseProcessor, HTTP_METHOD.PUT, jsonRequest, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		ReviseProcessorResponseDTO revProcessorResponseDTO = MailBoxUtility.unmarshalFromJSON(jsonResponse,
				ReviseProcessorResponseDTO.class);
		Assert.assertEquals(SUCCESS, revProcessorResponseDTO.getResponse().getStatus());

		// Get Processor
		request = constructHTTPRequest(getBASE_URL() + getProcessor, HTTP_METHOD.GET, null, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);
		getResponseDTO = MailBoxUtility.unmarshalFromJSON(jsonResponse, GetProcessorResponseDTO.class);

		Assert.assertEquals(SUCCESS, getResponseDTO.getResponse().getStatus());
		Assert.assertEquals(true, getResponseDTO.getProcessor().getProfiles().isEmpty());
	}

	@Test
	public void testReviseProcessor_WithInvaidProfile_ShouldFail() throws LiaisonException, JSONException, JsonParseException,
			JsonMappingException,
			JAXBException, IOException {

		// AddProcessor
		AddProcessorToMailboxRequestDTO addProcessorDTO = (AddProcessorToMailboxRequestDTO) getProcessorRequest(
				"RESPONSE_LOCATION", "/Sample", "LOGIN_CREDENTIAL", "ACTIVE", "REMOTEDOWNLOADER", false, "HTTP", true);
		jsonRequest = MailBoxUtility.marshalToJSON(addProcessorDTO);

		String addProcessor = "/" + responseDTO.getMailBox().getGuid() + "/processor";
		request = constructHTTPRequest(getBASE_URL() + addProcessor, HTTP_METHOD.POST, jsonRequest, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		AddProcessorToMailboxResponseDTO processorResponseDTO = MailBoxUtility.unmarshalFromJSON(jsonResponse,
				AddProcessorToMailboxResponseDTO.class);
		Assert.assertEquals(SUCCESS, processorResponseDTO.getResponse().getStatus());

		// Revise
		ReviseProcessorRequestDTO reviseProcessorDTO = (ReviseProcessorRequestDTO) getProcessorRequest(
				"RESPONSE_LOCATION", "/Sample", "LOGIN_CREDENTIAL", "ACTIVE", "REMOTEDOWNLOADER", true, "HTTP", true);
		reviseProcessorDTO.getProcessor().setGuid(processorResponseDTO.getProcessor().getGuId());
		reviseProcessorDTO.getProcessor().getLinkedProfiles().set(0, "dummy profiles");

		jsonRequest = MailBoxUtility.marshalToJSON(reviseProcessorDTO);
		String reviseProcessor = "/" + reviseProcessorDTO.getProcessor().getLinkedMailboxId() + "/processor/"
				+ processorResponseDTO.getProcessor().getGuId();
		request = constructHTTPRequest(getBASE_URL() + reviseProcessor, HTTP_METHOD.PUT, jsonRequest, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		ReviseProcessorResponseDTO revProcessorResponseDTO = MailBoxUtility.unmarshalFromJSON(jsonResponse,
				ReviseProcessorResponseDTO.class);
		Assert.assertEquals(FAILURE, revProcessorResponseDTO.getResponse().getStatus());

	}

	@Test
	public void testDeactivateProcessor() throws LiaisonException, JSONException, JsonGenerationException, JsonMappingException,
			JAXBException, IOException {

		AddProcessorToMailboxRequestDTO addProcessorDTO = (AddProcessorToMailboxRequestDTO) getProcessorRequest(
				"RESPONSE_LOCATION", "/Sample", "LOGIN_CREDENTIAL", "ACTIVE", "REMOTEDOWNLOADER", false, "HTTP", true);
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
		String deactProcessor = "/" + responseDTO.getMailBox().getGuid() + "/processor" + "/"
				+ processorResponseDTO.getProcessor().getGuId();
		request = constructHTTPRequest(getBASE_URL() + deactProcessor, HTTP_METHOD.DELETE, null, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		DeActivateProcessorResponseDTO responseDeactivateDTO = MailBoxUtility.unmarshalFromJSON(jsonResponse,
				DeActivateProcessorResponseDTO.class);
		Assert.assertEquals(SUCCESS, responseDeactivateDTO.getResponse().getStatus());

		// Get Processor
		String getProcessor = "/" + responseDTO.getMailBox().getGuid() + "/processor" + "/"
				+ processorResponseDTO.getProcessor().getGuId();
		request = constructHTTPRequest(getBASE_URL() + getProcessor, HTTP_METHOD.GET, null, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		GetProcessorResponseDTO getResponseDTO = MailBoxUtility.unmarshalFromJSON(jsonResponse, GetProcessorResponseDTO.class);

		Assert.assertEquals(SUCCESS, getResponseDTO.getResponse().getStatus());
		Assert.assertEquals(MailBoxStatus.INACTIVE.name(), getResponseDTO.getProcessor().getStatus());
	}

	@Test
	public void testDeactivateProcessor_WithWrongProcessorId__ShouldFail() throws LiaisonException, JSONException,
			JsonGenerationException,
			JsonMappingException,
			JAXBException, IOException {

		// Deactivate the processor
		String deactProcessor = "/" + responseDTO.getMailBox().getGuid() + "/processor" + "/"
				+ "DummyId";
		request = constructHTTPRequest(getBASE_URL() + deactProcessor, HTTP_METHOD.DELETE, null, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		DeActivateProcessorResponseDTO responseDeactivateDTO = MailBoxUtility.unmarshalFromJSON(jsonResponse,
				DeActivateProcessorResponseDTO.class);
		Assert.assertEquals(FAILURE, responseDeactivateDTO.getResponse().getStatus());

	}

	@Test
	public void testDeactivateProcessor_WithNullValueInURI__ShouldFail() throws LiaisonException, JSONException,
			JsonGenerationException,
			JsonMappingException,
			JAXBException, IOException {

		// Deactivate the processor
		String deactProcessor = "/" + null + "/processor" + "/" + null;
		request = constructHTTPRequest(getBASE_URL() + deactProcessor, HTTP_METHOD.DELETE, null, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		DeActivateProcessorResponseDTO responseDeactivateDTO = MailBoxUtility.unmarshalFromJSON(jsonResponse,
				DeActivateProcessorResponseDTO.class);
		Assert.assertEquals(FAILURE, responseDeactivateDTO.getResponse().getStatus());

	}

	private AddMailBoxResponseDTO createMailBox() throws JAXBException, JsonParseException, JsonMappingException, IOException,
			JsonGenerationException, MalformedURLException, FileNotFoundException, LiaisonException {

		AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
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

	private Object getProcessorRequest(String folderTye, String folderURI, String credentialType, String processorStatus,
			String processorType, boolean isRevise, String protocolType, boolean setProfile) throws JsonParseException,
			JsonMappingException,
			JsonGenerationException,
			MalformedURLException, FileNotFoundException, JAXBException, IOException, LiaisonException {

		List<CredentialDTO> credetnialList = new ArrayList<CredentialDTO>();
		List<FolderDTO> folderList = new ArrayList<FolderDTO>();

		FolderDTO folderDto = new FolderDTO();
		folderDto.setFolderType(folderTye);
		folderDto.setFolderURI(folderURI);
		folderList.add(folderDto);

		CredentialDTO credentialDTO = new CredentialDTO();
		credentialDTO.setCredentialType(credentialType);
		credetnialList.add(credentialDTO);

		ProcessorDTO processorDTO = new ProcessorDTO();
		processorDTO.setCredentials(credetnialList);
		processorDTO.setFolders(folderList);
		processorDTO.setStatus(processorStatus);
		processorDTO.setType(processorType);
		processorDTO.setProtocol(protocolType);
		processorDTO.setLinkedMailboxId(responseDTO.getMailBox().getGuid());

		if (setProfile) {

			String profileName = "TestProfile" + System.nanoTime();
			AddProfileResponseDTO response = addProfile(profileName);
			Assert.assertEquals(SUCCESS, response.getResponse().getStatus());

			List<String> profiles = new ArrayList<>();
			profiles.add(profileName);
			processorDTO.setLinkedProfiles(profiles);
		}

		if (isRevise) {
			ReviseProcessorRequestDTO reviseDTO = new ReviseProcessorRequestDTO();
			reviseDTO.setProcessor(processorDTO);
			return reviseDTO;
		} else {
			AddProcessorToMailboxRequestDTO addProcessorDTO = new AddProcessorToMailboxRequestDTO();
			addProcessorDTO.setProcessor(processorDTO);
			return addProcessorDTO;
		}
	}

	private AddProfileResponseDTO addProfile(String profileName) throws JAXBException, JsonGenerationException,
			JsonMappingException, IOException, MalformedURLException, FileNotFoundException, LiaisonException, JsonParseException {

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

		return profileResponseDTO;
	}
}
