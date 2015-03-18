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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jettison.json.JSONException;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.liaison.commons.exception.LiaisonException;
import com.liaison.commons.util.client.http.HTTPRequest;
import com.liaison.commons.util.client.http.HTTPRequest.HTTP_METHOD;
import com.liaison.mailbox.enums.MailBoxStatus;
import com.liaison.mailbox.service.base.test.BaseServiceTest;
import com.liaison.mailbox.service.dto.configuration.CredentialDTO;
import com.liaison.mailbox.service.dto.configuration.MailBoxDTO;
import com.liaison.mailbox.service.dto.configuration.ProcessorDTO;
import com.liaison.mailbox.service.dto.configuration.ProfileDTO;
import com.liaison.mailbox.service.dto.configuration.processor.properties.ProcessorFolderPropertyDTO;
import com.liaison.mailbox.service.dto.configuration.processor.properties.ProcessorPropertyUITemplateDTO;
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
import com.liaison.mailbox.service.util.MailBoxUtil;

/**
 * @author OFS
 *
 */
public class MailBoxProcessorServiceTest extends BaseServiceTest {

	private String jsonResponse;
	private String jsonRequest;
	private HTTPRequest request;

	private Logger logger = null;
	private AddMailBoxResponseDTO responseDTO;
	private final String serviceInstanceId = "9032A4910A0A52980A0EC676DB33A102";

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeMethod
	public void setUp() throws Exception {
		logger = LogManager.getLogger(MailBoxProcessorServiceTest.class);
		// Adding the mailbox
		responseDTO = createMailBox();
	}

	/**
	 * Method to test mailbox processor.
	 *
	 * @throws LiaisonException
	 * @throws JSONException
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws JAXBException
	 * @throws IOException
	 */
	@Test
	public void testCreateProcessorToMailBox() throws LiaisonException, JSONException, JsonParseException, JsonMappingException,
			JAXBException, IOException {

		AddProcessorToMailboxRequestDTO addProcessorDTO = (AddProcessorToMailboxRequestDTO) getProcessorRequest(
				"RESPONSE_LOCATION", "Sample", "ACTIVE", "REMOTEDOWNLOADER" ,"Dummy_DESCRIPTION", false, "HTTP", true);
		jsonRequest = MailBoxUtil.marshalToJSON(addProcessorDTO);

		String addProcessor = "/" + responseDTO.getMailBox().getGuid() + "/processor" + "?sid=" +serviceInstanceId;
		request = constructHTTPRequest(getBASE_URL() + addProcessor, HTTP_METHOD.POST, jsonRequest, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		AddProcessorToMailboxResponseDTO processorResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse,
				AddProcessorToMailboxResponseDTO.class);
		Assert.assertEquals(SUCCESS, processorResponseDTO.getResponse().getStatus());

		// Get Processor
		String getProcessor = "/" + responseDTO.getMailBox().getGuid() + "/processor" + "/"
				+ processorResponseDTO.getProcessor().getGuId();
		request = constructHTTPRequest(getBASE_URL() + getProcessor, HTTP_METHOD.GET, null, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		GetProcessorResponseDTO getResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, GetProcessorResponseDTO.class);

		Assert.assertEquals(SUCCESS, getResponseDTO.getResponse().getStatus());
		Assert.assertEquals(addProcessorDTO.getProcessor().getName(), getResponseDTO.getProcessor().getName());
		Assert.assertEquals(addProcessorDTO.getProcessor().getType(), getResponseDTO.getProcessor().getType());
		Assert.assertEquals(addProcessorDTO.getProcessor().getStatus(), getResponseDTO.getProcessor().getStatus());
		Assert.assertEquals(addProcessorDTO.getProcessor().getJavaScriptURI(), getResponseDTO.getProcessor().getJavaScriptURI());
		Assert.assertEquals(false, getResponseDTO.getProcessor().getProfiles().isEmpty());

	}

	/**
	 * Method to test mailbox processor without profile.
	 *
	 * @throws LiaisonException
	 * @throws JSONException
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws JAXBException
	 * @throws IOException
	 */
	@Test
	public void testCreateProcessorToMailBox_WithoutProfile_ShouldPass() throws LiaisonException, JSONException,
			JsonParseException,
			JsonMappingException,
			JAXBException, IOException {

		AddProcessorToMailboxRequestDTO addProcessorDTO = (AddProcessorToMailboxRequestDTO) getProcessorRequest(
				"RESPONSE_LOCATION", "Sample", "ACTIVE", "REMOTEDOWNLOADER","Dummy_DESCRIPTION", false, "HTTP", false);
		jsonRequest = MailBoxUtil.marshalToJSON(addProcessorDTO);

		String addProcessor = "/" + responseDTO.getMailBox().getGuid() + "/processor" + "?sid=" +serviceInstanceId;
		request = constructHTTPRequest(getBASE_URL() + addProcessor, HTTP_METHOD.POST, jsonRequest, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		AddProcessorToMailboxResponseDTO processorResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse,
				AddProcessorToMailboxResponseDTO.class);
		Assert.assertEquals(SUCCESS, processorResponseDTO.getResponse().getStatus());

		// Get Processor
		String getProcessor = "/" + responseDTO.getMailBox().getGuid() + "/processor" + "/"
				+ processorResponseDTO.getProcessor().getGuId();
		request = constructHTTPRequest(getBASE_URL() + getProcessor, HTTP_METHOD.GET, null, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		GetProcessorResponseDTO getResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, GetProcessorResponseDTO.class);

		Assert.assertEquals(SUCCESS, getResponseDTO.getResponse().getStatus());
		Assert.assertEquals(addProcessorDTO.getProcessor().getName(), getResponseDTO.getProcessor().getName());
		Assert.assertEquals(addProcessorDTO.getProcessor().getType(), getResponseDTO.getProcessor().getType());
		Assert.assertEquals(addProcessorDTO.getProcessor().getStatus(), getResponseDTO.getProcessor().getStatus());
		Assert.assertEquals(addProcessorDTO.getProcessor().getJavaScriptURI(), getResponseDTO.getProcessor().getJavaScriptURI());

	}

	/**
	 * Method to test mailbox processor without mailbox id.
	 *
	 * @throws LiaisonException
	 * @throws JSONException
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws JAXBException
	 * @throws IOException
	 */
	@Test
	public void testCreateProcessorToMailBox_WithoutMailBoxId_ShouldFail() throws LiaisonException, JSONException,
			JsonParseException,
			JsonMappingException,
			JAXBException, IOException {

		AddProcessorToMailboxRequestDTO addProcessorDTO = (AddProcessorToMailboxRequestDTO) getProcessorRequest(
				"RESPONSE_LOCATION", "Sample", "ACTIVE", "REMOTEDOWNLOADER", "Dummy_DESCRIPTION", false, "HTTP", false);
		addProcessorDTO.getProcessor().setLinkedMailboxId(null);
		jsonRequest = MailBoxUtil.marshalToJSON(addProcessorDTO);

		String addProcessor = "/" + responseDTO.getMailBox().getGuid() + "/processor" + "?sid=" +serviceInstanceId;
		request = constructHTTPRequest(getBASE_URL() + addProcessor, HTTP_METHOD.POST, jsonRequest, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		AddProcessorToMailboxResponseDTO processorResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse,
				AddProcessorToMailboxResponseDTO.class);
		Assert.assertEquals(FAILURE, processorResponseDTO.getResponse().getStatus());

	}

	/**
	 * Method to test mailbox processor with wrong mailbox id.
	 *
	 * @throws LiaisonException
	 * @throws JSONException
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws JAXBException
	 * @throws IOException
	 */
	@Test
	public void testCreateProcessorToMailBox_WithWorngMailBoxId_ShouldFail() throws LiaisonException, JSONException,
			JsonParseException,
			JsonMappingException,
			JAXBException, IOException {

		AddProcessorToMailboxRequestDTO addProcessorDTO = (AddProcessorToMailboxRequestDTO) getProcessorRequest(
				"RESPONSE_LOCATION", "Sample", "ACTIVE", "REMOTEDOWNLOADER", "Dummy_DESCRIPTION", false, "HTTP", false);
		jsonRequest = MailBoxUtil.marshalToJSON(addProcessorDTO);

		String addProcessor = "/" + "1241234123" + "/processor" + "?sid=" +serviceInstanceId;
		request = constructHTTPRequest(getBASE_URL() + addProcessor, HTTP_METHOD.POST, jsonRequest, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		AddProcessorToMailboxResponseDTO processorResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse,
				AddProcessorToMailboxResponseDTO.class);
		Assert.assertEquals(FAILURE, processorResponseDTO.getResponse().getStatus());

	}

	/**
	 * Method to test mailbox processor with wrong profile.
	 *
	 * @throws LiaisonException
	 * @throws JSONException
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws JAXBException
	 * @throws IOException
	 */
	@Test
	public void testCreateProcessorToMailBox_WithWrongProfile_ShouldFail() throws LiaisonException, JSONException,
			JsonParseException,
			JsonMappingException,
			JAXBException, IOException {

		AddProcessorToMailboxRequestDTO addProcessorDTO = (AddProcessorToMailboxRequestDTO) getProcessorRequest(
				"RESPONSE_LOCATION", "Sample", "ACTIVE", "REMOTEDOWNLOADER", "Dummy_DESCRIPTION",false, "HTTP", true);
		addProcessorDTO.getProcessor().getLinkedProfiles().set(0, "dummy profiles");
		jsonRequest = MailBoxUtil.marshalToJSON(addProcessorDTO);

		String addProcessor = "/" + responseDTO.getMailBox().getGuid() + "/processor" + "?sid=" +serviceInstanceId;
		request = constructHTTPRequest(getBASE_URL() + addProcessor, HTTP_METHOD.POST, jsonRequest, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		AddProcessorToMailboxResponseDTO processorResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse,
				AddProcessorToMailboxResponseDTO.class);
		Assert.assertEquals(FAILURE, processorResponseDTO.getResponse().getStatus());

	}

	/**
	 * Method to test mailbox processor without protocol.
	 *
	 * @throws LiaisonException
	 * @throws JSONException
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws JAXBException
	 * @throws IOException
	 */
	@Test
	public void testCreateProcessorToMailBox_WithoutProtocol_ShouldFail() throws LiaisonException, JSONException,
			JsonParseException,
			JsonMappingException,
			JAXBException, IOException {

		AddProcessorToMailboxRequestDTO addProcessorDTO = (AddProcessorToMailboxRequestDTO) getProcessorRequest(
				"RESPONSE_LOCATION", "Sample", "ACTIVE", "REMOTEDOWNLOADER", "Dummy_DESCRIPTION", false, null, true);
		jsonRequest = MailBoxUtil.marshalToJSON(addProcessorDTO);

		String addProcessor = "/" + responseDTO.getMailBox().getGuid() + "/processor" + "?sid=" +serviceInstanceId;
		request = constructHTTPRequest(getBASE_URL() + addProcessor, HTTP_METHOD.POST, jsonRequest, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		AddProcessorToMailboxResponseDTO processorResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse,
				AddProcessorToMailboxResponseDTO.class);
		Assert.assertEquals(FAILURE, processorResponseDTO.getResponse().getStatus());

	}

	/**
	 * Method to test mailbox processor with wrong protocol.
	 *
	 * @throws LiaisonException
	 * @throws JSONException
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws JAXBException
	 * @throws IOException
	 */
	@Test
	public void testCreateProcessorToMailBox_WithWrongProtocol_ShouldFail() throws LiaisonException, JSONException,
			JsonParseException,
			JsonMappingException,
			JAXBException, IOException {

		AddProcessorToMailboxRequestDTO addProcessorDTO = (AddProcessorToMailboxRequestDTO) getProcessorRequest(
				"RESPONSE_LOCATION", "Sample", "ACTIVE", "REMOTEDOWNLOADER", "Dummy_DESCRIPTION", false, "TEST", true);
		jsonRequest = MailBoxUtil.marshalToJSON(addProcessorDTO);

		String addProcessor = "/" + responseDTO.getMailBox().getGuid() + "/processor" + "?sid=" +serviceInstanceId;
		request = constructHTTPRequest(getBASE_URL() + addProcessor, HTTP_METHOD.POST, jsonRequest, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		AddProcessorToMailboxResponseDTO processorResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse,
				AddProcessorToMailboxResponseDTO.class);
		Assert.assertEquals(FAILURE, processorResponseDTO.getResponse().getStatus());

	}

	/**
	 * Method to test mailbox processor with URI as null.
	 *
	 * @throws LiaisonException
	 * @throws JSONException
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws JAXBException
	 * @throws IOException
	 */
	@Test
	public void testReadProcessor_NullValueInURI_ShouldFail() throws LiaisonException, JSONException,
			JsonParseException,
			JsonMappingException, JAXBException, IOException {

		// Get Processor
		String getProcessor = "/" + null + "/processor" + "/" + null;
		request = constructHTTPRequest(getBASE_URL() + getProcessor, HTTP_METHOD.GET, null, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		GetProcessorResponseDTO getResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, GetProcessorResponseDTO.class);

		Assert.assertEquals(FAILURE, getResponseDTO.getResponse().getStatus());

	}

	/**
	 * Method to test mailbox processor with mandotory values only.
	 *
	 * @throws LiaisonException
	 * @throws JSONException
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws JAXBException
	 * @throws IOException
	 */
	@Test
	public void testCreateProcessorToMailBox_MandotoryValuesOnly_ShouldPass() throws LiaisonException, JSONException,
			JsonParseException,
			JsonMappingException, JAXBException, IOException {

		jsonRequest = MailBoxUtil.marshalToJSON(getProcessorRequest("RESPONSE_LOCATION", "Sample", "ACTIVE",
				"REMOTEDOWNLOADER", "Dummy_DESCRIPTION", false, "HTTP", true));

		String addProcessor = "/" + responseDTO.getMailBox().getGuid() + "/processor" + "?sid=" +serviceInstanceId;
		request = constructHTTPRequest(getBASE_URL() + addProcessor, HTTP_METHOD.POST, jsonRequest, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		AddProcessorToMailboxResponseDTO processorResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse,
				AddProcessorToMailboxResponseDTO.class);
		Assert.assertEquals(SUCCESS, processorResponseDTO.getResponse().getStatus());

	}

	/**
	 * Method to test mailbox processor with invalid mandotory values only.
	 *
	 * @throws JAXBException
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws IOException
	 * @throws MalformedURLException
	 * @throws FileNotFoundException
	 * @throws LiaisonException
	 * @throws JsonParseException
	 */
	@Test
	public void testCreateProcessorToMailBox_WithInvalidMandatoryValues_ShouldFail() throws JAXBException,
			JsonGenerationException,
			JsonMappingException, IOException, MalformedURLException, FileNotFoundException, LiaisonException, JsonParseException {

		jsonRequest = MailBoxUtil
				.marshalToJSON(getProcessorRequest("ddd755", "Sample", "fdfd", "85964", "Dummy_DESCRIPTION", false, "HTTP", true));

		String addProcessor = "/" + responseDTO.getMailBox().getGuid() + "/processor" + "?sid=" +serviceInstanceId;
		request = constructHTTPRequest(getBASE_URL() + addProcessor, HTTP_METHOD.POST, jsonRequest, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		AddProcessorToMailboxResponseDTO processorResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse,
				AddProcessorToMailboxResponseDTO.class);
		Assert.assertEquals(FAILURE, processorResponseDTO.getResponse().getStatus());
	}

	/**
	 * Method to test mailbox processor with mandotory values as null.
	 *
	 * @throws JAXBException
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws IOException
	 * @throws MalformedURLException
	 * @throws FileNotFoundException
	 * @throws LiaisonException
	 * @throws JsonParseException
	 */
	@Test
	public void testCreateProcessorToMailBox_WithMandatoryValuesAsNull__ShouldFail() throws JAXBException,
			JsonGenerationException,
			JsonMappingException, IOException, MalformedURLException, FileNotFoundException, LiaisonException, JsonParseException {

		jsonRequest = MailBoxUtil.marshalToJSON(getProcessorRequest(null, null, null, null, null, false, null, true));

		String addProcessor = "/" + responseDTO.getMailBox().getGuid() + "/processor" + "?sid=" +serviceInstanceId;
		request = constructHTTPRequest(getBASE_URL() + addProcessor, HTTP_METHOD.POST, jsonRequest, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		AddProcessorToMailboxResponseDTO processorResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse,
				AddProcessorToMailboxResponseDTO.class);
		Assert.assertEquals(FAILURE, processorResponseDTO.getResponse().getStatus());
	}

	/**
	 *  Method to test retrieve Processor.
	 *
	 * @throws LiaisonException
	 * @throws JSONException
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws JAXBException
	 * @throws IOException
	 */
	@Test
	public void testReadProcessor() throws LiaisonException, JSONException, JsonParseException, JsonMappingException,
			JAXBException, IOException {

		AddProcessorToMailboxRequestDTO addProcessorDTO = (AddProcessorToMailboxRequestDTO) getProcessorRequest(
				"RESPONSE_LOCATION", "Sample", "ACTIVE", "REMOTEDOWNLOADER", "Dummy_DESCRIPTION", false, "HTTP", true);
		jsonRequest = MailBoxUtil.marshalToJSON(addProcessorDTO);

		String addProcessor = "/" + responseDTO.getMailBox().getGuid() + "/processor" + "?sid=" +serviceInstanceId;
		request = constructHTTPRequest(getBASE_URL() + addProcessor, HTTP_METHOD.POST, jsonRequest, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		AddProcessorToMailboxResponseDTO processorResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse,
				AddProcessorToMailboxResponseDTO.class);
		Assert.assertEquals(SUCCESS, processorResponseDTO.getResponse().getStatus());

		// Get Processor
		String getProcessor = "/" + responseDTO.getMailBox().getGuid() + "/processor" + "/"
				+ processorResponseDTO.getProcessor().getGuId();
		request = constructHTTPRequest(getBASE_URL() + getProcessor, HTTP_METHOD.GET, null, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);
		GetProcessorResponseDTO getResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, GetProcessorResponseDTO.class);

		Assert.assertEquals(SUCCESS, getResponseDTO.getResponse().getStatus());
		Assert.assertEquals(addProcessorDTO.getProcessor().getName(), getResponseDTO.getProcessor().getName());
		Assert.assertEquals(addProcessorDTO.getProcessor().getType(), getResponseDTO.getProcessor().getType());
		Assert.assertEquals(addProcessorDTO.getProcessor().getStatus(), getResponseDTO.getProcessor().getStatus());
		Assert.assertEquals(addProcessorDTO.getProcessor().getJavaScriptURI(), getResponseDTO.getProcessor().getJavaScriptURI());
	}

	/**
	 * Method to test retrieve Processor with invalid processor id.
	 *
	 * @throws LiaisonException
	 * @throws JSONException
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws JAXBException
	 * @throws IOException
	 */
	@Test
	public void testReadProcessor_WithInvalidProcessorId_ShouldFail() throws LiaisonException, JSONException, JsonParseException,
			JsonMappingException,
			JAXBException, IOException {

		String getProcessor = "/" + responseDTO.getMailBox().getGuid() + "/processor" + "/" + "DummyId";
		request = constructHTTPRequest(getBASE_URL() + getProcessor, HTTP_METHOD.GET, null, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);
		GetProcessorResponseDTO getResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, GetProcessorResponseDTO.class);

		Assert.assertEquals(FAILURE, getResponseDTO.getResponse().getStatus());
	}

	/**
	 * Method to test retrieve Processor with invalid mailbox.
	 *
	 * @throws LiaisonException
	 * @throws JSONException
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws JAXBException
	 * @throws IOException
	 */
	@Test
	public void testReadProcessor_WithInvalidMailBox_ShouldFail() throws LiaisonException, JSONException, JsonParseException,
			JsonMappingException,
			JAXBException, IOException {

		AddProcessorToMailboxRequestDTO addProcessorDTO = (AddProcessorToMailboxRequestDTO) getProcessorRequest(
				"RESPONSE_LOCATION", "Sample", "ACTIVE", "REMOTEDOWNLOADER", "Dummy_DESCRIPTION",false, "HTTP", true);
		jsonRequest = MailBoxUtil.marshalToJSON(addProcessorDTO);

		String addProcessor = "/" + responseDTO.getMailBox().getGuid() + "/processor" + "?sid=" +serviceInstanceId;
		request = constructHTTPRequest(getBASE_URL() + addProcessor, HTTP_METHOD.POST, jsonRequest, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		AddProcessorToMailboxResponseDTO processorResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse,
				AddProcessorToMailboxResponseDTO.class);
		Assert.assertEquals(SUCCESS, processorResponseDTO.getResponse().getStatus());

		AddProcessorToMailboxRequestDTO addSecProcessorDTO = (AddProcessorToMailboxRequestDTO) getProcessorRequest(
				"RESPONSE_LOCATION", "Sample", "ACTIVE", "REMOTEDOWNLOADER", "Dummy_DESCRIPTION", false, "HTTP", true);
		jsonRequest = MailBoxUtil.marshalToJSON(addSecProcessorDTO);

		String addProcessorReq = "/" + responseDTO.getMailBox().getGuid() + "/processor" + "?sid=" +serviceInstanceId;
		request = constructHTTPRequest(getBASE_URL() + addProcessorReq, HTTP_METHOD.POST, jsonRequest, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		AddProcessorToMailboxResponseDTO processorSecResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse,
				AddProcessorToMailboxResponseDTO.class);
		Assert.assertEquals(SUCCESS, processorSecResponseDTO.getResponse().getStatus());

		// Get Processor
		String getProcessor = "/" + responseDTO.getMailBox().getGuid() + "/processor" + "/"
				+ processorResponseDTO.getProcessor().getGuId();
		request = constructHTTPRequest(getBASE_URL() + getProcessor, HTTP_METHOD.GET, null, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);
		GetProcessorResponseDTO getResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, GetProcessorResponseDTO.class);

		Assert.assertEquals(SUCCESS, getResponseDTO.getResponse().getStatus());
		Assert.assertEquals(addProcessorDTO.getProcessor().getName(), getResponseDTO.getProcessor().getName());
		Assert.assertEquals(addProcessorDTO.getProcessor().getType(), getResponseDTO.getProcessor().getType());
		Assert.assertEquals(addProcessorDTO.getProcessor().getStatus(), getResponseDTO.getProcessor().getStatus());
		Assert.assertEquals(addProcessorDTO.getProcessor().getJavaScriptURI(), getResponseDTO.getProcessor().getJavaScriptURI());
	}

	/**
	 * Method to test revise operation for Processor with valid data.
	 *
	 * @throws LiaisonException
	 * @throws JSONException
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws JAXBException
	 * @throws IOException
	 */
	@Test
	public void testReviseProcessor() throws LiaisonException, JSONException, JsonParseException,
			JsonMappingException, JAXBException, IOException {

		AddProcessorToMailboxRequestDTO addProcessorDTO = (AddProcessorToMailboxRequestDTO) getProcessorRequest(
				"RESPONSE_LOCATION", "sample", "ACTIVE", "REMOTEDOWNLOADER", "Dummy_DESCRIPTION", false, "HTTP", true);
		jsonRequest = MailBoxUtil.marshalToJSON(addProcessorDTO);

		String addProcessor = "/" + responseDTO.getMailBox().getGuid() + "/processor" + "?sid=" +serviceInstanceId;
		request = constructHTTPRequest(getBASE_URL() + addProcessor, HTTP_METHOD.POST, jsonRequest, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		AddProcessorToMailboxResponseDTO processorResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse,
				AddProcessorToMailboxResponseDTO.class);
		Assert.assertEquals(SUCCESS, processorResponseDTO.getResponse().getStatus());

		ReviseProcessorRequestDTO reviseProcessorDTO = (ReviseProcessorRequestDTO) getProcessorRequest("RESPONSE_LOCATION",
				"sample", "ACTIVE", "REMOTEDOWNLOADER", "Dummy_DESCRIPTION", true, "HTTP", true);
		reviseProcessorDTO.getProcessor().setGuid(processorResponseDTO.getProcessor().getGuId());
		jsonRequest = MailBoxUtil.marshalToJSON(reviseProcessorDTO);

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
		GetProcessorResponseDTO getResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, GetProcessorResponseDTO.class);

		Assert.assertEquals(SUCCESS, getResponseDTO.getResponse().getStatus());
		Assert.assertEquals(reviseProcessorDTO.getProcessor().getName(), getResponseDTO.getProcessor().getName());
		Assert.assertEquals(reviseProcessorDTO.getProcessor().getType(), getResponseDTO.getProcessor().getType());
		Assert.assertEquals(reviseProcessorDTO.getProcessor().getStatus(), getResponseDTO.getProcessor().getStatus());
		Assert.assertEquals(reviseProcessorDTO.getProcessor().getJavaScriptURI(), getResponseDTO.getProcessor()
				.getJavaScriptURI());
	}

	/**
	 * Method to test revise operation for Processor without protocol.
	 *
	 * @throws LiaisonException
	 * @throws JSONException
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws JAXBException
	 * @throws IOException
	 */
	@Test
	public void testReviseProcessor_WithoutProtocol_ShouldFail() throws LiaisonException, JSONException, JsonParseException,
			JsonMappingException, JAXBException, IOException {

		ReviseProcessorRequestDTO reviseProcessorDTO = (ReviseProcessorRequestDTO) getProcessorRequest("RESPONSE_LOCATION",
				"sample", "ACTIVE", "REMOTEDOWNLOADER", "Dummy_DESCRIPTION", true, null, true);
		reviseProcessorDTO.getProcessor().setGuid("123456");
		jsonRequest = MailBoxUtil.marshalToJSON(reviseProcessorDTO);

		String reviseProcessor = "/" + responseDTO.getMailBox().getGuid() + "/processor/"
				+ "123456";
		request = constructHTTPRequest(getBASE_URL() + reviseProcessor, HTTP_METHOD.PUT, jsonRequest, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		Assert.assertEquals(FAILURE, getResponseStatus(jsonResponse, "reviseProcessorResponse"));

	}

	/**
	 * Method to test revise operation for Processor with invalid protocol.
	 *
	 * @throws LiaisonException
	 * @throws JSONException
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws JAXBException
	 * @throws IOException
	 */
	@Test
	public void testReviseProcessor_WithInvalidProtocol_ShouldFail() throws LiaisonException, JSONException, JsonParseException,
			JsonMappingException, JAXBException, IOException {

		ReviseProcessorRequestDTO reviseProcessorDTO = (ReviseProcessorRequestDTO) getProcessorRequest("RESPONSE_LOCATION",
				"sample", "ACTIVE", "REMOTEDOWNLOADER", "Dummy_DESCRIPTION", true, "Test", true);
		reviseProcessorDTO.getProcessor().setGuid("123456");
		jsonRequest = MailBoxUtil.marshalToJSON(reviseProcessorDTO);

		String reviseProcessor = "/" + responseDTO.getMailBox().getGuid() + "/processor/"
				+ "123456";
		request = constructHTTPRequest(getBASE_URL() + reviseProcessor, HTTP_METHOD.PUT, jsonRequest, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		Assert.assertEquals(FAILURE, getResponseStatus(jsonResponse, "reviseProcessorResponse"));

	}

	/**
	 * Method to test revise operation for Processor without mailbox id.
	 *
	 * @throws LiaisonException
	 * @throws JSONException
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws JAXBException
	 * @throws IOException
	 */
	@Test
	public void testReviseProcessor_WithoutMailBoxId_ShouldFail() throws LiaisonException, JSONException, JsonParseException,
			JsonMappingException,
			JAXBException, IOException {

		ReviseProcessorRequestDTO reviseProcessorDTO = (ReviseProcessorRequestDTO) getProcessorRequest(
				"RESPONSE_LOCATION", "sample", "ACTIVE", "REMOTEDOWNLOADER", "Dummy_DESCRIPTION", true, "HTTP", false);
		reviseProcessorDTO.getProcessor().setLinkedMailboxId(null);
		reviseProcessorDTO.getProcessor().setGuid("21412341234");
		jsonRequest = MailBoxUtil.marshalToJSON(reviseProcessorDTO);

		String reviseProcessor = "/" + responseDTO.getMailBox().getGuid() + "/processor/"
				+ "123456";
		request = constructHTTPRequest(getBASE_URL() + reviseProcessor, HTTP_METHOD.PUT, jsonRequest, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		ReviseProcessorResponseDTO processorResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse,
				ReviseProcessorResponseDTO.class);
		Assert.assertEquals(FAILURE, processorResponseDTO.getResponse().getStatus());

	}

	/**
	 * Method to test revise operation for Processor with URI as null.
	 *
	 * @throws LiaisonException
	 * @throws JSONException
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws JAXBException
	 * @throws IOException
	 */
	@Test
	public void testReviseProcessor_WithNullValueInURI_ShouldFail() throws LiaisonException, JSONException, JsonParseException,
			JsonMappingException,
			JAXBException, IOException {

		ReviseProcessorRequestDTO reviseProcessorDTO = (ReviseProcessorRequestDTO) getProcessorRequest(
				"RESPONSE_LOCATION", "sample", "ACTIVE", "REMOTEDOWNLOADER", "Dummy_DESCRIPTION", true, "HTTP", false);
		reviseProcessorDTO.getProcessor().setLinkedMailboxId("null");
		reviseProcessorDTO.getProcessor().setGuid("dfasfasfdafd");
		jsonRequest = MailBoxUtil.marshalToJSON(reviseProcessorDTO);

		String reviseProcessor = "/" + null + "/processor/" + null;
		request = constructHTTPRequest(getBASE_URL() + reviseProcessor, HTTP_METHOD.PUT, jsonRequest, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		ReviseProcessorResponseDTO processorResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse,
				ReviseProcessorResponseDTO.class);
		Assert.assertEquals(FAILURE, processorResponseDTO.getResponse().getStatus());

	}

	/**
	 * Method to test revise operation for Processor with wrong mailbox id.
	 *
	 * @throws LiaisonException
	 * @throws JSONException
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws JAXBException
	 * @throws IOException
	 */
	@Test
	public void testReviseProcessor_WithWorngMailBoxId_ShouldFail() throws LiaisonException, JSONException, JsonParseException,
			JsonMappingException,
			JAXBException, IOException {

		// Add
		AddProcessorToMailboxRequestDTO addProcessorDTO = (AddProcessorToMailboxRequestDTO) getProcessorRequest(
				"RESPONSE_LOCATION", "sample", "ACTIVE", "REMOTEDOWNLOADER", "Dummy_DESCRIPTION", false, "HTTP", true);
		jsonRequest = MailBoxUtil.marshalToJSON(addProcessorDTO);

		String addProcessor = "/" + responseDTO.getMailBox().getGuid() + "/processor" + "?sid=" +serviceInstanceId;
		request = constructHTTPRequest(getBASE_URL() + addProcessor, HTTP_METHOD.POST, jsonRequest, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		AddProcessorToMailboxResponseDTO processorResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse,
				AddProcessorToMailboxResponseDTO.class);
		Assert.assertEquals(SUCCESS, processorResponseDTO.getResponse().getStatus());

		// Revise
		ReviseProcessorRequestDTO reviseProcessorDTO = (ReviseProcessorRequestDTO) getProcessorRequest(
				"RESPONSE_LOCATION", "sample", "ACTIVE", "REMOTEDOWNLOADER", "Dummy_DESCRIPTION", true, "HTTP", false);
		jsonRequest = MailBoxUtil.marshalToJSON(reviseProcessorDTO);
		reviseProcessorDTO.getProcessor().setGuid(processorResponseDTO.getProcessor().getGuId());

		String reviseProcessor = "/" + addProcessorDTO.getProcessor().getLinkedMailboxId() + "/processor/"
				+ "123456";
		request = constructHTTPRequest(getBASE_URL() + reviseProcessor, HTTP_METHOD.PUT, jsonRequest, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		ReviseProcessorResponseDTO revProcessorResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse,
				ReviseProcessorResponseDTO.class);
		Assert.assertEquals(FAILURE, revProcessorResponseDTO.getResponse().getStatus());

	}

	/**
	 * Method to test revise operation for Processor without profile.
	 *
	 * @throws LiaisonException
	 * @throws JSONException
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws JAXBException
	 * @throws IOException
	 */
	@Test
	public void testReviseProcessor_WithoutProfile_ShouldFail() throws LiaisonException, JSONException, JsonParseException,
			JsonMappingException,
			JAXBException, IOException {

		// AddProcessor
		AddProcessorToMailboxRequestDTO addProcessorDTO = (AddProcessorToMailboxRequestDTO) getProcessorRequest(
				"RESPONSE_LOCATION", "sample", "ACTIVE", "REMOTEDOWNLOADER", "Dummy_DESCRIPTION",  false, "HTTP", true);
		jsonRequest = MailBoxUtil.marshalToJSON(addProcessorDTO);

		String addProcessor = "/" + responseDTO.getMailBox().getGuid() + "/processor" + "?sid=" +serviceInstanceId;
		request = constructHTTPRequest(getBASE_URL() + addProcessor, HTTP_METHOD.POST, jsonRequest, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		AddProcessorToMailboxResponseDTO processorResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse,
				AddProcessorToMailboxResponseDTO.class);
		Assert.assertEquals(SUCCESS, processorResponseDTO.getResponse().getStatus());

		// Get Processor
		String getProcessor = "/" + responseDTO.getMailBox().getGuid() + "/processor" + "/"
				+ processorResponseDTO.getProcessor().getGuId();
		request = constructHTTPRequest(getBASE_URL() + getProcessor, HTTP_METHOD.GET, null, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);
		GetProcessorResponseDTO getResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, GetProcessorResponseDTO.class);

		Assert.assertEquals(SUCCESS, getResponseDTO.getResponse().getStatus());
		Assert.assertEquals(false, getResponseDTO.getProcessor().getProfiles().isEmpty());

		// Revise
		ReviseProcessorRequestDTO reviseProcessorDTO = (ReviseProcessorRequestDTO) getProcessorRequest(
				"RESPONSE_LOCATION", "sample", "ACTIVE", "REMOTEDOWNLOADER", "Dummy_DESCRIPTION", true, "HTTP", false);
		reviseProcessorDTO.getProcessor().setGuid(processorResponseDTO.getProcessor().getGuId());

		jsonRequest = MailBoxUtil.marshalToJSON(reviseProcessorDTO);
		String reviseProcessor = "/" + reviseProcessorDTO.getProcessor().getLinkedMailboxId() + "/processor/"
				+ processorResponseDTO.getProcessor().getGuId();
		request = constructHTTPRequest(getBASE_URL() + reviseProcessor, HTTP_METHOD.PUT, jsonRequest, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		ReviseProcessorResponseDTO revProcessorResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse,
				ReviseProcessorResponseDTO.class);
		Assert.assertEquals(SUCCESS, revProcessorResponseDTO.getResponse().getStatus());

		// Get Processor
		request = constructHTTPRequest(getBASE_URL() + getProcessor, HTTP_METHOD.GET, null, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);
		getResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, GetProcessorResponseDTO.class);

		Assert.assertEquals(SUCCESS, getResponseDTO.getResponse().getStatus());
		Assert.assertEquals(true, getResponseDTO.getProcessor().getProfiles().isEmpty());
	}

	/**
	 * Method to test revise operation for Processor with invalid profile.
	 *
	 * @throws LiaisonException
	 * @throws JSONException
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws JAXBException
	 * @throws IOException
	 */
	@Test
	public void testReviseProcessor_WithInvaidProfile_ShouldFail() throws LiaisonException, JSONException, JsonParseException,
			JsonMappingException,
			JAXBException, IOException {

		// AddProcessor
		AddProcessorToMailboxRequestDTO addProcessorDTO = (AddProcessorToMailboxRequestDTO) getProcessorRequest(
				"RESPONSE_LOCATION", "sample", "ACTIVE", "REMOTEDOWNLOADER", "Dummy_DESCRIPTION", false, "HTTP", true);
		jsonRequest = MailBoxUtil.marshalToJSON(addProcessorDTO);

		String addProcessor = "/" + responseDTO.getMailBox().getGuid() + "/processor" + "?sid=" +serviceInstanceId;
		request = constructHTTPRequest(getBASE_URL() + addProcessor, HTTP_METHOD.POST, jsonRequest, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		AddProcessorToMailboxResponseDTO processorResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse,
				AddProcessorToMailboxResponseDTO.class);
		Assert.assertEquals(SUCCESS, processorResponseDTO.getResponse().getStatus());

		// Revise
		ReviseProcessorRequestDTO reviseProcessorDTO = (ReviseProcessorRequestDTO) getProcessorRequest(
				"RESPONSE_LOCATION", "sample", "ACTIVE", "REMOTEDOWNLOADER", "Dummy_DESCRIPTION", true, "HTTP", true);
		reviseProcessorDTO.getProcessor().setGuid(processorResponseDTO.getProcessor().getGuId());
		reviseProcessorDTO.getProcessor().getLinkedProfiles().set(0, "dummy profiles");

		jsonRequest = MailBoxUtil.marshalToJSON(reviseProcessorDTO);
		String reviseProcessor = "/" + reviseProcessorDTO.getProcessor().getLinkedMailboxId() + "/processor/"
				+ processorResponseDTO.getProcessor().getGuId();
		request = constructHTTPRequest(getBASE_URL() + reviseProcessor, HTTP_METHOD.PUT, jsonRequest, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		ReviseProcessorResponseDTO revProcessorResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse,
				ReviseProcessorResponseDTO.class);
		Assert.assertEquals(FAILURE, revProcessorResponseDTO.getResponse().getStatus());

	}

	/**
	 * Method to test deactive processor.
	 *
	 * @throws LiaisonException
	 * @throws JSONException
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws JAXBException
	 * @throws IOException
	 */
	@Test
	public void testDeactivateProcessor() throws LiaisonException, JSONException, JsonGenerationException, JsonMappingException,
			JAXBException, IOException {

		AddProcessorToMailboxRequestDTO addProcessorDTO = (AddProcessorToMailboxRequestDTO) getProcessorRequest(
				"RESPONSE_LOCATION", "sample", "ACTIVE", "REMOTEDOWNLOADER", "Dummy_DESCRIPTION", false, "HTTP", true);
		jsonRequest = MailBoxUtil.marshalToJSON(addProcessorDTO);

		String addProcessor = "/" + responseDTO.getMailBox().getGuid() + "/processor" + "?sid=" +serviceInstanceId;
		request = constructHTTPRequest(getBASE_URL() + addProcessor, HTTP_METHOD.POST, jsonRequest, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		AddProcessorToMailboxResponseDTO processorResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse,
				AddProcessorToMailboxResponseDTO.class);
		Assert.assertEquals(SUCCESS, processorResponseDTO.getResponse().getStatus());

		// Deactivate the processor
		String deactProcessor = "/" + responseDTO.getMailBox().getGuid() + "/processor" + "/"
				+ processorResponseDTO.getProcessor().getGuId();
		request = constructHTTPRequest(getBASE_URL() + deactProcessor, HTTP_METHOD.DELETE, null, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		DeActivateProcessorResponseDTO responseDeactivateDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse,
				DeActivateProcessorResponseDTO.class);
		Assert.assertEquals(SUCCESS, responseDeactivateDTO.getResponse().getStatus());

		// Get Processor
		String getProcessor = "/" + responseDTO.getMailBox().getGuid() + "/processor" + "/"
				+ processorResponseDTO.getProcessor().getGuId();
		request = constructHTTPRequest(getBASE_URL() + getProcessor, HTTP_METHOD.GET, null, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		GetProcessorResponseDTO getResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, GetProcessorResponseDTO.class);

		Assert.assertEquals(SUCCESS, getResponseDTO.getResponse().getStatus());
		Assert.assertEquals(MailBoxStatus.INACTIVE.name(), getResponseDTO.getProcessor().getStatus());
	}

	/**
	 * Method to test deactive processor with wrong processor id.
	 *
	 * @throws LiaisonException
	 * @throws JSONException
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws JAXBException
	 * @throws IOException
	 */
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

		DeActivateProcessorResponseDTO responseDeactivateDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse,
				DeActivateProcessorResponseDTO.class);
		Assert.assertEquals(FAILURE, responseDeactivateDTO.getResponse().getStatus());

	}

	/**
	 * Method to test deactive processor with URI as null.
	 *
	 * @throws LiaisonException
	 * @throws JSONException
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws JAXBException
	 * @throws IOException
	 */
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

		DeActivateProcessorResponseDTO responseDeactivateDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse,
				DeActivateProcessorResponseDTO.class);
		Assert.assertEquals(FAILURE, responseDeactivateDTO.getResponse().getStatus());

	}

	/**
	 * Method constructs Mailbox.
	 *
	 * @return AddMailBoxResponseDTO
	 * @throws JAXBException
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 * @throws JsonGenerationException
	 * @throws MalformedURLException
	 * @throws FileNotFoundException
	 * @throws LiaisonException
	 */
	private AddMailBoxResponseDTO createMailBox() throws JAXBException, JsonParseException, JsonMappingException, IOException,
			JsonGenerationException, MalformedURLException, FileNotFoundException, LiaisonException {

		AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
		MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
		requestDTO.setMailBox(mbxDTO);

		jsonRequest = MailBoxUtil.marshalToJSON(requestDTO);
		request = constructHTTPRequest(getBASE_URL() + "?sid=" +serviceInstanceId, HTTP_METHOD.POST, jsonRequest, logger);
		request.execute();
		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		AddMailBoxResponseDTO responseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, AddMailBoxResponseDTO.class);
		Assert.assertEquals(SUCCESS, responseDTO.getResponse().getStatus());
		return responseDTO;
	}

	/**
	 * Method retrieves processor request.
	 *
	 * @param folderTye
	 * @param folderURI
	 * @param credentialType
	 * @param processorStatus
	 * @param processorType
	 * @param isRevise
	 * @param protocolType
	 * @param setProfile
	 * @return Object
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws JsonGenerationException
	 * @throws MalformedURLException
	 * @throws FileNotFoundException
	 * @throws JAXBException
	 * @throws IOException
	 * @throws LiaisonException
	 */
	private Object getProcessorRequest(String folderTye, String folderURI, String processorStatus,
			String processorType, String processorDescription, boolean isRevise, String protocolType, boolean setProfile) throws JsonParseException,
			JsonMappingException,
			JsonGenerationException,
			MalformedURLException, FileNotFoundException, JAXBException, IOException, LiaisonException {

		List<CredentialDTO> credetnialList = new ArrayList<CredentialDTO>();
		List<ProcessorFolderPropertyDTO> folderList = new ArrayList<ProcessorFolderPropertyDTO>();
		ProcessorFolderPropertyDTO folderPropertyDto = new ProcessorFolderPropertyDTO();
		folderPropertyDto.setFolderType(folderTye);
		folderPropertyDto.setFolderURI(folderURI);
		folderPropertyDto.setFolderDesc("someDesc");
		folderList.add(folderPropertyDto);

		ProcessorDTO processorDTO = new ProcessorDTO();
		processorDTO.setCredentials(credetnialList);

		processorDTO.setProcessorPropertiesInTemplateJson(new ProcessorPropertyUITemplateDTO());
		processorDTO.getProcessorPropertiesInTemplateJson().setFolderProperties(folderList);
		processorDTO.setStatus(processorStatus);
		processorDTO.setName(System.currentTimeMillis() + "");

		processorDTO.setDescription(processorDescription);

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

	/**
	 * Method to add profile.
	 *
	 * @param profileName
	 * @return AddProfileResponseDTO
	 * @throws JAXBException
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws IOException
	 * @throws MalformedURLException
	 * @throws FileNotFoundException
	 * @throws LiaisonException
	 * @throws JsonParseException
	 */
	private AddProfileResponseDTO addProfile(String profileName) throws JAXBException, JsonGenerationException,
			JsonMappingException, IOException, MalformedURLException, FileNotFoundException, LiaisonException, JsonParseException {

		ProfileDTO profile = new ProfileDTO();
		profile.setName(profileName);
		AddProfileRequestDTO profileRequstDTO = new AddProfileRequestDTO();
		profileRequstDTO.setProfile(profile);

		jsonRequest = MailBoxUtil.marshalToJSON(profileRequstDTO);
		request = constructHTTPRequest(getBASE_URL() + "/profile", HTTP_METHOD.POST, jsonRequest, logger);
		request.execute();
		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		AddProfileResponseDTO profileResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, AddProfileResponseDTO.class);

		return profileResponseDTO;
	}
}
