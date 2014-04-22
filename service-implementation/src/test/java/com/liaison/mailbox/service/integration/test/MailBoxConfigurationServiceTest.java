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

import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import org.testng.Assert;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.apache.http.client.ClientProtocolException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jettison.json.JSONException;

import com.liaison.commons.exception.LiaisonException;
import com.liaison.commons.util.client.http.HTTPRequest;
import com.liaison.commons.util.client.http.HTTPRequest.HTTP_METHOD;
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
 * @author veerasamyn
 */
public class MailBoxConfigurationServiceTest extends BaseServiceTest {

	private Logger logger;
	private String jsonResponse;
	private String jsonRequest;
	private HTTPRequest request;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeMethod
	public void setUp() throws Exception {
		logger = LogManager.getLogger(MailBoxConfigurationServiceTest.class);
	}
    
	/**
	 * Method constructs MailBox with valid data.
	 * 
	 * @throws LiaisonException
	 * @throws JSONException
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws JAXBException
	 * @throws IOException
	 */
	@Test
	public void testCreateMailBox() throws LiaisonException, JSONException, JsonParseException, JsonMappingException,
			JAXBException, IOException {

		// Adding the mailbox
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

		// Get the mailbox
		String url = getBASE_URL() + "/" + responseDTO.getMailBox().getGuid();
		request = constructHTTPRequest(url, HTTP_METHOD.GET, null, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		GetMailBoxResponseDTO getResponseDTO = MailBoxUtility.unmarshalFromJSON(jsonResponse, GetMailBoxResponseDTO.class);

		// Assertion
		Assert.assertEquals(SUCCESS, getResponseDTO.getResponse().getStatus());
		Assert.assertEquals(requestDTO.getMailBox().getName(), getResponseDTO.getMailBox().getName());
		Assert.assertEquals(requestDTO.getMailBox().getDescription(), getResponseDTO.getMailBox().getDescription());
		Assert.assertEquals(requestDTO.getMailBox().getShardKey(), getResponseDTO.getMailBox().getShardKey());
		Assert.assertEquals(MailBoxStatus.ACTIVE.name(), getResponseDTO.getMailBox().getStatus());

		Assert.assertEquals(requestDTO.getMailBox().getProperties().get(0).getName(),
				getResponseDTO.getMailBox().getProperties().get(0).getName());
		Assert.assertEquals(requestDTO.getMailBox().getProperties().get(0).getValue(),
				getResponseDTO.getMailBox().getProperties().get(0).getValue());

	}
    
	/**
	 * Method to test mailbox with valid data.
	 * 
	 * @throws LiaisonException
	 * @throws JSONException
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws JAXBException
	 * @throws IOException
	 */
	@Test
	public void testGetMailBox() throws LiaisonException, JSONException, JsonParseException, JsonMappingException, JAXBException,
			IOException {

		// Adding the mailbox
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

		// Get the mailbox
		String url = getBASE_URL() + "/" + responseDTO.getMailBox().getGuid();
		request = constructHTTPRequest(url, HTTP_METHOD.GET, null, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		GetMailBoxResponseDTO getResponseDTO = MailBoxUtility.unmarshalFromJSON(jsonResponse, GetMailBoxResponseDTO.class);

		// Assertion
		Assert.assertEquals(SUCCESS, getResponseDTO.getResponse().getStatus());
		Assert.assertEquals(requestDTO.getMailBox().getName(), getResponseDTO.getMailBox().getName());
		Assert.assertEquals(requestDTO.getMailBox().getDescription(), getResponseDTO.getMailBox().getDescription());
		Assert.assertEquals(requestDTO.getMailBox().getShardKey(), getResponseDTO.getMailBox().getShardKey());
		Assert.assertEquals(MailBoxStatus.ACTIVE.name(), getResponseDTO.getMailBox().getStatus());

		Assert.assertEquals(requestDTO.getMailBox().getProperties().get(0).getName(),
				getResponseDTO.getMailBox().getProperties().get(0).getName());
		Assert.assertEquals(requestDTO.getMailBox().getProperties().get(0).getValue(),
				getResponseDTO.getMailBox().getProperties().get(0).getValue());

	}
    
	/**
	 * Method to test deactivate mailbox.
	 * 
	 * @throws LiaisonException
	 * @throws JSONException
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws JAXBException
	 * @throws IOException
	 */
	@Test
	public void testDeactivateMailBox() throws LiaisonException, JSONException, JsonParseException, JsonMappingException,
			JAXBException, IOException {

		// Adding the mailbox
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

		// Deactivate the mailbox
		String url = getBASE_URL() + "/" + responseDTO.getMailBox().getGuid();
		request = constructHTTPRequest(url, HTTP_METHOD.DELETE, null, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);
		Assert.assertEquals(SUCCESS, getResponseStatus(jsonResponse, "deactivateMailBoxResponse"));

		// Get the mailbox
		url = getBASE_URL() + "/" + responseDTO.getMailBox().getGuid();
		request = constructHTTPRequest(url, HTTP_METHOD.GET, null, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		GetMailBoxResponseDTO getResponseDTO = MailBoxUtility.unmarshalFromJSON(jsonResponse, GetMailBoxResponseDTO.class);
		Assert.assertEquals(MailBoxStatus.INACTIVE.name(), getResponseDTO.getMailBox().getStatus());

	}
    
	/**
	 * Method to test revise mailBox with liaison HTTPClient.
	 * 
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws Exception
	 */
	@Test
	public void testReviseMailBoxWithLiaisonHTTPClient() throws ClientProtocolException, IOException, Exception {

		// Adding the mailbox
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

		// Constructing the revise
		ReviseMailBoxRequestDTO reviseRequestDTO = new ReviseMailBoxRequestDTO();
		mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), false);
		mbxDTO.setGuid(responseDTO.getMailBox().getGuid());
		reviseRequestDTO.setMailBox(mbxDTO);
		jsonRequest = MailBoxUtility.marshalToJSON(reviseRequestDTO);

		String url = getBASE_URL() + "/" + responseDTO.getMailBox().getGuid();
		request = constructHTTPRequest(url, HTTP_METHOD.PUT, jsonRequest, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);
		Assert.assertEquals(SUCCESS, getResponseStatus(jsonResponse, "reviseMailBoxResponse"));

		// Get the mailbox
		url = getBASE_URL() + "/" + responseDTO.getMailBox().getGuid();
		request = constructHTTPRequest(url, HTTP_METHOD.GET, null, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		GetMailBoxResponseDTO getResponseDTO = MailBoxUtility.unmarshalFromJSON(jsonResponse, GetMailBoxResponseDTO.class);

		// Assertion
		Assert.assertEquals(MailBoxStatus.ACTIVE.name(), getResponseDTO.getMailBox().getStatus());
		Assert.assertEquals(mbxDTO.getName(), getResponseDTO.getMailBox().getName());
		Assert.assertEquals(mbxDTO.getDescription(), getResponseDTO.getMailBox().getDescription());
		Assert.assertEquals(mbxDTO.getShardKey(), getResponseDTO.getMailBox().getShardKey());
		Assert.assertEquals(MailBoxStatus.ACTIVE.name(), getResponseDTO.getMailBox().getStatus());

		Assert.assertEquals(mbxDTO.getProperties().get(0).getName(),
				getResponseDTO.getMailBox().getProperties().get(0).getName());
		Assert.assertEquals(mbxDTO.getProperties().get(0).getValue(),
				getResponseDTO.getMailBox().getProperties().get(0).getValue());

	}
    
	/**
	 * Method to test mailBox with valid mandatory fields.
	 * 
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws JAXBException
	 * @throws IOException
	 * @throws LiaisonException
	 */
	@Test
	public void testCreateMailBox_MandatoryFields_ShouldPass() throws JsonGenerationException, JsonMappingException,
			JAXBException, IOException, LiaisonException {

		MailBoxDTO mailBox = new MailBoxDTO();
		mailBox.setName("TestMailBox");
		mailBox.setStatus("ACTIVE");
		mailBox.setDescription("Test Mailbox");

		// Adding the mailbox
		AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
		requestDTO.setMailBox(mailBox);

		jsonRequest = MailBoxUtility.marshalToJSON(requestDTO);
		request = constructHTTPRequest(getBASE_URL(), HTTP_METHOD.POST, jsonRequest, logger);
		request.execute();
		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		AddMailBoxResponseDTO responseDTO = MailBoxUtility.unmarshalFromJSON(jsonResponse, AddMailBoxResponseDTO.class);
		Assert.assertEquals(SUCCESS, responseDTO.getResponse().getStatus());

		// Get the mailbox
		String url = getBASE_URL() + "/" + responseDTO.getMailBox().getGuid();
		request = constructHTTPRequest(url, HTTP_METHOD.GET, null, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		GetMailBoxResponseDTO getResponseDTO = MailBoxUtility.unmarshalFromJSON(jsonResponse, GetMailBoxResponseDTO.class);

		// Assertion
		Assert.assertEquals(MailBoxStatus.ACTIVE.name(), getResponseDTO.getMailBox().getStatus());
		Assert.assertEquals(mailBox.getName(), getResponseDTO.getMailBox().getName());
	}
    
	/**
	 * Method to test mailBox without mandatory fields.
	 * 
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws JAXBException
	 * @throws IOException
	 * @throws LiaisonException
	 */
	@Test
	public void testCreateMailBox_WithoutMandatoryFields_ShouldFail() throws JsonGenerationException, JsonMappingException,
			JAXBException, IOException, LiaisonException {

		MailBoxDTO mailBox = constructDummyMailBoxDTO(System.currentTimeMillis(), true);

		// Adding the mailbox
		AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
		requestDTO.setMailBox(mailBox);

		jsonRequest = MailBoxUtility.marshalToJSON(requestDTO);
		request = constructHTTPRequest(getBASE_URL(), HTTP_METHOD.POST, jsonRequest, logger);
		request.execute();
		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		AddMailBoxResponseDTO responseDTO = MailBoxUtility.unmarshalFromJSON(jsonResponse, AddMailBoxResponseDTO.class);
		Assert.assertEquals(SUCCESS, responseDTO.getResponse().getStatus());

		// Get the mailbox
		String url = getBASE_URL() + "/" + responseDTO.getMailBox().getGuid();
		request = constructHTTPRequest(url, HTTP_METHOD.GET, null, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		GetMailBoxResponseDTO getResponseDTO = MailBoxUtility.unmarshalFromJSON(jsonResponse, GetMailBoxResponseDTO.class);

		// Assertion
		Assert.assertEquals(MailBoxStatus.ACTIVE.name(), getResponseDTO.getMailBox().getStatus());
		Assert.assertEquals(mailBox.getName(), getResponseDTO.getMailBox().getName());

	}
    
	/**
	 * Method to test mailBox with null.
	 * 
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws JAXBException
	 * @throws IOException
	 * @throws LiaisonException
	 */
	@Test
	public void testCreateMailBox_MailBoxisNull_ShouldFail() throws JsonGenerationException, JsonMappingException,
			JAXBException, IOException, LiaisonException {

		// Adding the mailbox
		AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
		requestDTO.setMailBox(null);

		jsonRequest = MailBoxUtility.marshalToJSON(requestDTO);
		request = constructHTTPRequest(getBASE_URL(), HTTP_METHOD.POST, jsonRequest, logger);
		request.execute();
		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		AddMailBoxResponseDTO responseDTO = MailBoxUtility.unmarshalFromJSON(jsonResponse, AddMailBoxResponseDTO.class);
		Assert.assertEquals(FAILURE, responseDTO.getResponse().getStatus());
		Assert.assertEquals(true,
				responseDTO.getResponse().getMessage().contains(Messages.INVALID_REQUEST.value()));

	}
    
	/**
	 * Method to test mailBox with invalid id.
	 * 
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws JAXBException
	 * @throws IOException
	 * @throws LiaisonException
	 */
	@Test
	public void testGetMailBox_WrongId_ShouldFail() throws JsonGenerationException, JsonMappingException,
			JAXBException, IOException, LiaisonException {

		// Get the mailbox
		String url = getBASE_URL() + "/" + "3434";
		request = constructHTTPRequest(url, HTTP_METHOD.GET, null, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		GetMailBoxResponseDTO getResponseDTO = MailBoxUtility.unmarshalFromJSON(jsonResponse, GetMailBoxResponseDTO.class);

		// Assertion
		Assert.assertEquals(FAILURE, getResponseDTO.getResponse().getStatus());

	}
    
	/**
	 * Method to test revise mailBox with null.
	 * 
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws JAXBException
	 * @throws IOException
	 * @throws LiaisonException
	 * @throws JSONException
	 */
	@Test
	public void testReviseMailBox_Null_ShouldFail() throws JsonGenerationException, JsonMappingException, JAXBException,
			IOException, LiaisonException, JSONException {

		// Constructing the revise
		ReviseMailBoxRequestDTO reviseRequestDTO = new ReviseMailBoxRequestDTO();
		reviseRequestDTO.setMailBox(null);
		jsonRequest = MailBoxUtility.marshalToJSON(reviseRequestDTO);

		String url = getBASE_URL() + "/" + "fasdfasdfdas";
		request = constructHTTPRequest(url, HTTP_METHOD.PUT, jsonRequest, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);
		Assert.assertEquals(FAILURE, getResponseStatus(jsonResponse, "reviseMailBoxResponse"));

	}
    
	/**
	 * Method to test revise mailBox with wrong guid.
	 * 
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws JAXBException
	 * @throws IOException
	 * @throws LiaisonException
	 * @throws JSONException
	 */
	@Test
	public void testReviseMailBox_WrongGuids_ShouldFail() throws JsonGenerationException, JsonMappingException, JAXBException,
			IOException, LiaisonException, JSONException {

		// Constructing the revise
		ReviseMailBoxRequestDTO reviseRequestDTO = new ReviseMailBoxRequestDTO();
		reviseRequestDTO.setMailBox(constructDummyMailBoxDTO(System.currentTimeMillis(), true));
		jsonRequest = MailBoxUtility.marshalToJSON(reviseRequestDTO);

		String url = getBASE_URL() + "/" + "3432432";
		request = constructHTTPRequest(url, HTTP_METHOD.PUT, jsonRequest, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);
		Assert.assertEquals(FAILURE, getResponseStatus(jsonResponse, "reviseMailBoxResponse"));

	}
    
	/**
	 * Method to test revise mailBox with invalid guid.
	 * 
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws JAXBException
	 * @throws IOException
	 * @throws LiaisonException
	 * @throws JSONException
	 */
	@Test
	public void testReviseMailBox_InvalidGuids_ShouldFail() throws JsonGenerationException, JsonMappingException, JAXBException,
			IOException, LiaisonException, JSONException {

		// Constructing the revise
		ReviseMailBoxRequestDTO reviseRequestDTO = new ReviseMailBoxRequestDTO();

		MailBoxDTO mailBox = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
		mailBox.setGuid("123456");
		reviseRequestDTO.setMailBox(mailBox);

		jsonRequest = MailBoxUtility.marshalToJSON(reviseRequestDTO);

		String url = getBASE_URL() + "/" + "123456";
		request = constructHTTPRequest(url, HTTP_METHOD.PUT, jsonRequest, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);
		Assert.assertEquals(FAILURE, getResponseStatus(jsonResponse, "reviseMailBoxResponse"));

	}
    
	/**
	 * Method to test deactivate mailBox with wrong id.
	 * 
	 * @throws LiaisonException
	 * @throws JSONException
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws JAXBException
	 * @throws IOException
	 */
	@Test
	public void testDeactivateMailBox_WrongId_ShouldFail() throws LiaisonException, JSONException, JsonParseException,
			JsonMappingException, JAXBException, IOException {

		// Deactivate the mailbox
		String url = getBASE_URL() + "/" + "123456";
		request = constructHTTPRequest(url, HTTP_METHOD.DELETE, null, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);
		Assert.assertEquals(FAILURE, getResponseStatus(jsonResponse, "deactivateMailBoxResponse"));

	}
    
	/**
	 * Method to test Revise mailBox without mandatory field.
	 * 
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws JAXBException
	 * @throws IOException
	 * @throws LiaisonException
	 * @throws JSONException
	 */
	@Test
	public void testReviseMailBox_WithoutMandatory_ShouldFail() throws JsonGenerationException, JsonMappingException,
			JAXBException,
			IOException, LiaisonException, JSONException {

		// Constructing the revise
		ReviseMailBoxRequestDTO reviseRequestDTO = new ReviseMailBoxRequestDTO();
		reviseRequestDTO.setMailBox(null);
		jsonRequest = MailBoxUtility.marshalToJSON(reviseRequestDTO);

		String url = getBASE_URL() + "/" + "3432432";
		request = constructHTTPRequest(url, HTTP_METHOD.PUT, jsonRequest, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);
		Assert.assertEquals(FAILURE, getResponseStatus(jsonResponse, "reviseMailBoxResponse"));

	}
    
	/**
	 * Method constructs mailbox with empty.
	 * 
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws JAXBException
	 * @throws IOException
	 * @throws LiaisonException
	 */
	@Test
	public void testCreateMailBox_EmptyString_ShouldFail() throws JsonGenerationException, JsonMappingException,
			JAXBException, IOException, LiaisonException {

		// Adding the mailbox
		AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();

		MailBoxDTO dto = new MailBoxDTO();
		dto.setName("");
		requestDTO.setMailBox(dto);

		jsonRequest = MailBoxUtility.marshalToJSON(requestDTO);
		request = constructHTTPRequest(getBASE_URL(), HTTP_METHOD.POST, jsonRequest, logger);
		request.execute();
		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		AddMailBoxResponseDTO responseDTO = MailBoxUtility.unmarshalFromJSON(jsonResponse, AddMailBoxResponseDTO.class);
		Assert.assertEquals(FAILURE, responseDTO.getResponse().getStatus());

	}
    
	/**
	 * Method to constructs mailbox with invalid status.
	 * 
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws JAXBException
	 * @throws IOException
	 * @throws LiaisonException
	 */
	@Test
	public void testCreateMailBox_InValidStatus_ShouldFail() throws JsonGenerationException, JsonMappingException,
			JAXBException, IOException, LiaisonException {

		// Adding the mailbox
		AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();

		MailBoxDTO dto = new MailBoxDTO();
		dto.setName("afdaf");
		dto.setStatus("sfrafda");
		requestDTO.setMailBox(dto);

		jsonRequest = MailBoxUtility.marshalToJSON(requestDTO);
		request = constructHTTPRequest(getBASE_URL(), HTTP_METHOD.POST, jsonRequest, logger);
		request.execute();
		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		AddMailBoxResponseDTO responseDTO = MailBoxUtility.unmarshalFromJSON(jsonResponse, AddMailBoxResponseDTO.class);
		Assert.assertEquals(FAILURE, responseDTO.getResponse().getStatus());
	}
}
