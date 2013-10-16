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
import com.liaison.mailbox.service.dto.configuration.response.AddProfileToMailBoxResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.DeActivateProcessorResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.GetProcessorResponseDTO;
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
	public void testAddProcessorToMailBox() throws LiaisonException, JSONException, JsonParseException, JsonMappingException,
			JAXBException, IOException {

		AddProcessorToMailboxRequestDTO addProcessorDTO = (AddProcessorToMailboxRequestDTO) getProcessorRequest(
				"RESPONSE_LOCATION", "/Sample", "db", "ACTIVE", "REMOTEDOWNLOADER", false, "HTTP");
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
	public void testAddProcessorToMailBoxWithoutProtocol() throws LiaisonException, JSONException, JsonParseException,
			JsonMappingException,
			JAXBException, IOException {

		AddProcessorToMailboxRequestDTO addProcessorDTO = (AddProcessorToMailboxRequestDTO) getProcessorRequest(
				"RESPONSE_LOCATION", "/Sample", "db", "ACTIVE", "REMOTEDOWNLOADER", false, null);
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
	public void testAddProcessorToMailBoxWithWrongProtocol() throws LiaisonException, JSONException, JsonParseException,
			JsonMappingException,
			JAXBException, IOException {

		AddProcessorToMailboxRequestDTO addProcessorDTO = (AddProcessorToMailboxRequestDTO) getProcessorRequest(
				"RESPONSE_LOCATION", "/Sample", "db", "ACTIVE", "REMOTEDOWNLOADER", false, "TEST");
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
	public void testAddProcessorToMailBox_InvalidId() throws LiaisonException, JSONException, JsonParseException,
			JsonMappingException, JAXBException, IOException {

		AddProcessorToMailboxRequestDTO addProcessorDTO = (AddProcessorToMailboxRequestDTO) getProcessorRequest(
				"RESPONSE_LOCATION", "/Sample", "db", "ACTIVE", "REMOTEDOWNLOADER", false, "HTTP");
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
		String getProcessor = "/" + "1452585w1" + "/processor" + "/" + "SSDDCSDSDSD";
		request = constructHTTPRequest(getBASE_URL() + getProcessor, HTTP_METHOD.GET, null, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		GetProcessorResponseDTO getResponseDTO = MailBoxUtility.unmarshalFromJSON(jsonResponse, GetProcessorResponseDTO.class);

		Assert.assertEquals(FAILURE, getResponseDTO.getResponse().getStatus());

	}

	@Test
	public void testAddProcessorToMailBox_NullValue() throws LiaisonException, JSONException, JsonParseException,
			JsonMappingException, JAXBException, IOException {

		AddProcessorToMailboxRequestDTO addProcessorDTO = (AddProcessorToMailboxRequestDTO) getProcessorRequest(
				"RESPONSE_LOCATION", "/Sample", "db", "ACTIVE", "REMOTEDOWNLOADER", false, "HTTP");
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
		String getProcessor = "/" + null + "/processor" + "/" + null;
		request = constructHTTPRequest(getBASE_URL() + getProcessor, HTTP_METHOD.GET, null, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		GetProcessorResponseDTO getResponseDTO = MailBoxUtility.unmarshalFromJSON(jsonResponse, GetProcessorResponseDTO.class);

		Assert.assertEquals(FAILURE, getResponseDTO.getResponse().getStatus());

	}

	@Test
	public void testCreateProcessor_MandotoryValuesOnly() throws LiaisonException, JSONException, JsonParseException,
			JsonMappingException, JAXBException, IOException {

		jsonRequest = MailBoxUtility.marshalToJSON(getProcessorRequest("RESPONSE_LOCATION", "/Sample", "db", "ACTIVE",
				"REMOTEDOWNLOADER", false, "HTTP"));

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
	public void testCreateProcessor_WithInvalidMandatoryValues() throws JAXBException, JsonGenerationException,
			JsonMappingException, IOException, MalformedURLException, FileNotFoundException, LiaisonException, JsonParseException {

		jsonRequest = MailBoxUtility
				.marshalToJSON(getProcessorRequest("ddd755", "/Sample", "db", "fdfd", "85964", false, "HTTP"));

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
	public void testCreateProcessor_WithInvalidMandatoryValuesAsNull() throws JAXBException, JsonGenerationException,
			JsonMappingException, IOException, MalformedURLException, FileNotFoundException, LiaisonException, JsonParseException {

		jsonRequest = MailBoxUtility.marshalToJSON(getProcessorRequest(null, null, null, null, null, false, null));

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
				"RESPONSE_LOCATION", "/Sample", "db", "ACTIVE", "REMOTEDOWNLOADER", false, "HTTP");
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
	public void testReviseProcessorUsingLiaisonHttpClient() throws LiaisonException, JSONException, JsonParseException,
			JsonMappingException, JAXBException, IOException {

		AddProcessorToMailboxRequestDTO addProcessorDTO = (AddProcessorToMailboxRequestDTO) getProcessorRequest(
				"RESPONSE_LOCATION", "/Sample", "db", "ACTIVE", "REMOTEDOWNLOADER", false, "HTTP");
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
				"/Sample", "db", "ACTIVE", "REMOTEDOWNLOADER", true, "HTTP");
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
	public void testReviseProcessorUsingWithoutProtocol() throws LiaisonException, JSONException, JsonParseException,
			JsonMappingException, JAXBException, IOException {

		ReviseProcessorRequestDTO reviseProcessorDTO = (ReviseProcessorRequestDTO) getProcessorRequest("RESPONSE_LOCATION",
				"/Sample", "db", "ACTIVE", "REMOTEDOWNLOADER", true, null);
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
	public void testReviseProcessorUsingWithInvalidProtocol() throws LiaisonException, JSONException, JsonParseException,
			JsonMappingException, JAXBException, IOException {

		ReviseProcessorRequestDTO reviseProcessorDTO = (ReviseProcessorRequestDTO) getProcessorRequest("RESPONSE_LOCATION",
				"/Sample", "db", "ACTIVE", "REMOTEDOWNLOADER", true, "Test");
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
	public void testDeActivateProcessor() throws LiaisonException, JSONException, JsonGenerationException, JsonMappingException,
			JAXBException, IOException {

		AddProcessorToMailboxRequestDTO addProcessorDTO = (AddProcessorToMailboxRequestDTO) getProcessorRequest(
				"RESPONSE_LOCATION", "/Sample", "db", "ACTIVE", "REMOTEDOWNLOADER", false, "HTTP");
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

	private AddProfileToMailBoxResponseDTO createProfileLink(AddMailBoxResponseDTO requestDTO,
			AddProfileResponseDTO profileResponseDTO) throws MalformedURLException, FileNotFoundException, LiaisonException,
			JAXBException, JsonParseException, JsonMappingException, IOException {

		String addProfile = "/" + requestDTO.getMailBox().getGuid() + "/profile/" + profileResponseDTO.getProfile().getGuId();
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

	private Object getProcessorRequest(String folderTye, String folderURI, String credentialType, String processorStatus,
			String processorType, boolean isRevise, String protocolType) throws JsonParseException, JsonMappingException,
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

		String profileName = "TestProfile" + System.nanoTime();
		AddProfileResponseDTO response = addProfile(profileName);
		Assert.assertEquals(SUCCESS, response.getResponse().getStatus());

		List<String> profiles = new ArrayList<>();
		profiles.add(profileName);
		processorDTO.setLinkedProfiles(profiles);

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
