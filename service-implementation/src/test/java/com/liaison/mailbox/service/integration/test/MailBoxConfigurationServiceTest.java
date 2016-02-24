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

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.JAXBException;

import org.apache.http.client.ClientProtocolException;
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
import com.liaison.framework.util.ServiceUtils;
import com.liaison.mailbox.enums.EntityStatus;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.service.base.test.BaseServiceTest;
import com.liaison.mailbox.service.dto.configuration.MailBoxDTO;
import com.liaison.mailbox.service.dto.configuration.ProcessorDTO;
import com.liaison.mailbox.service.dto.configuration.ProfileDTO;
import com.liaison.mailbox.service.dto.configuration.request.AddMailboxRequestDTO;
import com.liaison.mailbox.service.dto.configuration.request.AddProcessorToMailboxRequestDTO;
import com.liaison.mailbox.service.dto.configuration.request.AddProfileRequestDTO;
import com.liaison.mailbox.service.dto.configuration.request.ReviseMailBoxRequestDTO;
import com.liaison.mailbox.service.dto.configuration.response.AddMailBoxResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.AddProcessorToMailboxResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.AddProfileResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.GetMailBoxResponseDTO;
import com.liaison.mailbox.service.dto.ui.SearchMailBoxResponseDTO;
import com.liaison.mailbox.service.util.MailBoxUtil;

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
	private String serviceInstanceId = "9032A4910A0A52980A0EC676DB33A102";
	private String aclManifest = "H4sIAAAAAAAAAI2TXWvbMBSG7wf7D0GXY+1ix02aXE1J3M6kTcAxG2spQ7POipgsGUkxNSX/fXIcmyixYb4w9jnP+Xyl948fBocHgSiAyxzQYDZ4b6zWniogBujSvioX8odecOV5V8Nx4t3MhsFsdHNtbU/o80nQK5e/CY9oFSF2nJ/6cqJAmB6fkilo3e3caVC1BzFh4BUy/ytnhGkprikU6Jy9Y0qbNcmgL9kD6fcfx56XOM85S4lhUjRcg+3bAJRzYv5IlVXIs7O/xtMUQtsw/h4twl/zeLMKY6drJTnMiQaK02oNCymMNV3ktCSVGWHiP3K2bFLW2qJNfI/X0RNOos26k4zscpUgvM2+9CcTHM6DIZ76/nQ88gIvGOO7xQjfTm5H0/AsSzVF3TNaQflIBLFSWckxzZjd4YtL54oVjFs1O8askoGWO5XWjXyh8NctdkK0A8agTdyE9dHVbuHNHAI+XVA5qIxpfdT8oitLFITvoDvaiW+7+vEtSsKHaJsgl94PXk4Nzu/eWRYSdjCgW1AFS2EJOQgKIi0Px8TqZroOiq7pVstV+PMRr+8vDomVISOq7Lx556Vr6PlcyZ72jqwzVXuDms/9Pzn4MJeJBAAA";

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
		AddMailboxRequestDTO requestDTO = MailBoxUtil.unmarshalFromJSON(jsonRequest, AddMailboxRequestDTO.class);

		MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
		requestDTO.setMailBox(mbxDTO);

		jsonRequest = MailBoxUtil.marshalToJSON(requestDTO);

		String url = getBASE_URL() + "?sid=" +serviceInstanceId;
		request = constructHTTPRequest(url, HTTP_METHOD.POST, jsonRequest, logger);
		request.execute();
		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		AddMailBoxResponseDTO responseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, AddMailBoxResponseDTO.class);
		Assert.assertEquals(SUCCESS, responseDTO.getResponse().getStatus());

		// Get the mailbox
		url = getBASE_URL() +"/"+responseDTO.getMailBox().getGuid()+"?sid=" +serviceInstanceId;
		request = constructHTTPRequest(url, HTTP_METHOD.GET, null, logger);
		request.addHeader("acl-manifest", aclManifest);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		GetMailBoxResponseDTO getResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, GetMailBoxResponseDTO.class);

		// Assertion
		Assert.assertEquals(SUCCESS, getResponseDTO.getResponse().getStatus());
		Assert.assertEquals(requestDTO.getMailBox().getName(), getResponseDTO.getMailBox().getName());
		Assert.assertEquals(requestDTO.getMailBox().getDescription(), getResponseDTO.getMailBox().getDescription());
		Assert.assertEquals(requestDTO.getMailBox().getShardKey(), getResponseDTO.getMailBox().getShardKey());
		Assert.assertEquals(EntityStatus.ACTIVE.name(), getResponseDTO.getMailBox().getStatus());

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
		AddMailboxRequestDTO requestDTO = MailBoxUtil.unmarshalFromJSON(jsonRequest, AddMailboxRequestDTO.class);

		MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
		requestDTO.setMailBox(mbxDTO);

		jsonRequest = MailBoxUtil.marshalToJSON(requestDTO);

		String url = getBASE_URL() + "?sid=" +serviceInstanceId;
		request = constructHTTPRequest(url, HTTP_METHOD.POST, jsonRequest, logger);
		request.addHeader("acl-manifest", aclManifest);
		request.execute();
		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		AddMailBoxResponseDTO responseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, AddMailBoxResponseDTO.class);
		Assert.assertEquals(SUCCESS, responseDTO.getResponse().getStatus());

		// Get the mailbox
		url = getBASE_URL() + "/" + responseDTO.getMailBox().getGuid()+"?sid=" +serviceInstanceId;
		request = constructHTTPRequest(url, HTTP_METHOD.GET, null, logger);
		request.addHeader("acl-manifest", aclManifest);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		GetMailBoxResponseDTO getResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, GetMailBoxResponseDTO.class);

		// Assertion
		Assert.assertEquals(SUCCESS, getResponseDTO.getResponse().getStatus());
		Assert.assertEquals(requestDTO.getMailBox().getName(), getResponseDTO.getMailBox().getName());
		Assert.assertEquals(requestDTO.getMailBox().getDescription(), getResponseDTO.getMailBox().getDescription());
		Assert.assertEquals(requestDTO.getMailBox().getShardKey(), getResponseDTO.getMailBox().getShardKey());
		Assert.assertEquals(EntityStatus.ACTIVE.name(), getResponseDTO.getMailBox().getStatus());

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
		AddMailboxRequestDTO requestDTO = MailBoxUtil.unmarshalFromJSON(jsonRequest, AddMailboxRequestDTO.class);

		MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
		requestDTO.setMailBox(mbxDTO);

		jsonRequest = MailBoxUtil.marshalToJSON(requestDTO);

		String url = getBASE_URL() + "?sid=" +serviceInstanceId;
		request = constructHTTPRequest(url, HTTP_METHOD.POST, jsonRequest, logger);
		request.addHeader("acl-manifest", aclManifest);
		request.execute();
		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		AddMailBoxResponseDTO responseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, AddMailBoxResponseDTO.class);
		Assert.assertEquals(SUCCESS, responseDTO.getResponse().getStatus());

		// Deactivate the mailbox
		url = getBASE_URL() + "/" + responseDTO.getMailBox().getGuid();
		request = constructHTTPRequest(url, HTTP_METHOD.DELETE, null, logger);
		request.addHeader("acl-manifest", aclManifest);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);
        Assert.assertEquals(SUCCESS, getResponse(jsonResponse, "deactivateMailBoxResponse", STATUS));

		// Get the mailbox
		url = getBASE_URL() + "/" + responseDTO.getMailBox().getGuid()+"?sid=" +serviceInstanceId;
		request = constructHTTPRequest(url, HTTP_METHOD.GET, null, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		GetMailBoxResponseDTO getResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, GetMailBoxResponseDTO.class);
		Assert.assertEquals(EntityStatus.INACTIVE.name(), getResponseDTO.getMailBox().getStatus());

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

		jsonRequest = MailBoxUtil.marshalToJSON(requestDTO);

		String url = getBASE_URL() + "?sid=" +serviceInstanceId;
		request = constructHTTPRequest(url, HTTP_METHOD.POST, jsonRequest, logger);
		request.execute();
		request.addHeader("acl-manifest", aclManifest);
		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		AddMailBoxResponseDTO responseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, AddMailBoxResponseDTO.class);
		Assert.assertEquals(SUCCESS, responseDTO.getResponse().getStatus());

		// Constructing the revise
		ReviseMailBoxRequestDTO reviseRequestDTO = new ReviseMailBoxRequestDTO();
		mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), false);
		mbxDTO.setGuid(responseDTO.getMailBox().getGuid());
		reviseRequestDTO.setMailBox(mbxDTO);
		jsonRequest = MailBoxUtil.marshalToJSON(reviseRequestDTO);

	    url = getBASE_URL() + "/" + responseDTO.getMailBox().getGuid()+ "?sid=" +serviceInstanceId;
		request = constructHTTPRequest(url, HTTP_METHOD.PUT, jsonRequest, logger);
		request.addHeader("acl-manifest", aclManifest);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);
		Assert.assertEquals(SUCCESS, getResponse(jsonResponse, "reviseMailBoxResponse", STATUS));

		// Get the mailbox
		url = getBASE_URL() + "/" + responseDTO.getMailBox().getGuid()+ "?sid=" +serviceInstanceId;
		request = constructHTTPRequest(url, HTTP_METHOD.GET, null, logger);
		request.addHeader("acl-manifest", aclManifest);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		GetMailBoxResponseDTO getResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, GetMailBoxResponseDTO.class);

		// Assertion
		Assert.assertEquals(EntityStatus.ACTIVE.name(), getResponseDTO.getMailBox().getStatus());
		Assert.assertEquals(mbxDTO.getName(), getResponseDTO.getMailBox().getName());
		Assert.assertEquals(mbxDTO.getDescription(), getResponseDTO.getMailBox().getDescription());
		Assert.assertEquals(mbxDTO.getShardKey(), getResponseDTO.getMailBox().getShardKey());
		Assert.assertEquals(EntityStatus.ACTIVE.name(), getResponseDTO.getMailBox().getStatus());

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
		mailBox.setName("TestMailBox" + System.currentTimeMillis());
		mailBox.setStatus("ACTIVE");
		mailBox.setTenancyKey("Test Tenancy Key");

		// Adding the mailbox
		AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
		requestDTO.setMailBox(mailBox);

		jsonRequest = MailBoxUtil.marshalToJSON(requestDTO);

		String url = getBASE_URL() + "?sid=" +serviceInstanceId;
		request = constructHTTPRequest(url, HTTP_METHOD.POST, jsonRequest, logger);
		request.addHeader("acl-manifest", aclManifest);
		request.execute();
		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		AddMailBoxResponseDTO responseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, AddMailBoxResponseDTO.class);
		Assert.assertEquals(SUCCESS, responseDTO.getResponse().getStatus());

		// Get the mailbox
		url = getBASE_URL() + "/" + responseDTO.getMailBox().getGuid()+ "?sid=" +serviceInstanceId;
		request = constructHTTPRequest(url, HTTP_METHOD.GET, null, logger);
		request.addHeader("acl-manifest", aclManifest);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		GetMailBoxResponseDTO getResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, GetMailBoxResponseDTO.class);

		// Assertion
		Assert.assertEquals(EntityStatus.ACTIVE.name(), getResponseDTO.getMailBox().getStatus());
		Assert.assertEquals(mailBox.getName(), getResponseDTO.getMailBox().getName());
	}

    /**
     * Method to test mailBox without Name.
     *
     * @throws JsonGenerationException
     * @throws JsonMappingException
     * @throws JAXBException
     * @throws IOException
     * @throws LiaisonException
     */
    @Test
    public void testCreateMailBox_WithoutName_ShouldFail()
            throws JsonGenerationException, JsonMappingException, JAXBException, IOException, LiaisonException {

        MailBoxDTO mailBox = new MailBoxDTO();
        mailBox.setStatus("ACTIVE");
        mailBox.setTenancyKey("test Tenancy Key");

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        requestDTO.setMailBox(mailBox);

        jsonRequest = MailBoxUtil.marshalToJSON(requestDTO);

        String url = getBASE_URL() + "?sid=" + serviceInstanceId;
        request = constructHTTPRequest(url, HTTP_METHOD.POST, jsonRequest, logger);
        request.addHeader("acl-manifest", aclManifest);
        request.execute();
        jsonResponse = getOutput().toString();
        logger.info(jsonResponse);

        AddMailBoxResponseDTO responseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, AddMailBoxResponseDTO.class);
        Assert.assertEquals(FAILURE, responseDTO.getResponse().getStatus());
        Assert.assertEquals(true, responseDTO.getResponse().getMessage().contains(Messages.ERROR_MSG.value()));

    }

    /**
     * Method to test mailBox without Status.
     *
     * @throws JsonGenerationException
     * @throws JsonMappingException
     * @throws JAXBException
     * @throws IOException
     * @throws LiaisonException
     */
    @Test
    public void testCreateMailBox_WithoutStatus_ShouldFail()
            throws JsonGenerationException, JsonMappingException, JAXBException, IOException, LiaisonException {

        MailBoxDTO mailBox = new MailBoxDTO();
        mailBox.setName("TestMailBox" + System.currentTimeMillis());
        mailBox.setTenancyKey("test Tenancy Key");

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        requestDTO.setMailBox(mailBox);

        jsonRequest = MailBoxUtil.marshalToJSON(requestDTO);

        String url = getBASE_URL() + "?sid=" + serviceInstanceId;
        request = constructHTTPRequest(url, HTTP_METHOD.POST, jsonRequest, logger);
        request.addHeader("acl-manifest", aclManifest);
        request.execute();
        jsonResponse = getOutput().toString();
        logger.info(jsonResponse);

        AddMailBoxResponseDTO responseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, AddMailBoxResponseDTO.class);
        Assert.assertEquals(FAILURE, responseDTO.getResponse().getStatus());
        Assert.assertEquals(true, responseDTO.getResponse().getMessage().contains(Messages.ERROR_MSG.value()));

    }

    /**
     * Method to test mailBox without tenancy key.
     *
     * @throws JsonGenerationException
     * @throws JsonMappingException
     * @throws JAXBException
     * @throws IOException
     * @throws LiaisonException
     */
    @Test
    public void testCreateMailBox_WithoutTenancyKey_ShouldFail()
            throws JsonGenerationException, JsonMappingException, JAXBException, IOException, LiaisonException {

        MailBoxDTO mailBox = new MailBoxDTO();
        mailBox.setName("TestMailBox" + System.currentTimeMillis());
        mailBox.setStatus("ACTIVE");

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        requestDTO.setMailBox(mailBox);

        jsonRequest = MailBoxUtil.marshalToJSON(requestDTO);

        String url = getBASE_URL() + "?sid=" + serviceInstanceId;
        request = constructHTTPRequest(url, HTTP_METHOD.POST, jsonRequest, logger);
        request.addHeader("acl-manifest", aclManifest);
        request.execute();
        jsonResponse = getOutput().toString();
        logger.info(jsonResponse);

        AddMailBoxResponseDTO responseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, AddMailBoxResponseDTO.class);
        Assert.assertEquals(FAILURE, responseDTO.getResponse().getStatus());
        Assert.assertEquals(true, responseDTO.getResponse().getMessage().contains(Messages.ERROR_MSG.value()));

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

		MailBoxDTO mailBox = new MailBoxDTO();

		// Adding the mailbox
		AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
		requestDTO.setMailBox(mailBox);

		jsonRequest = MailBoxUtil.marshalToJSON(requestDTO);

		String url = getBASE_URL() + "?sid=" +serviceInstanceId;
		request = constructHTTPRequest(url, HTTP_METHOD.POST, jsonRequest, logger);
		request.addHeader("acl-manifest", aclManifest);
		request.execute();
		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		AddMailBoxResponseDTO responseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, AddMailBoxResponseDTO.class);
		Assert.assertEquals(FAILURE, responseDTO.getResponse().getStatus());

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

		jsonRequest = MailBoxUtil.marshalToJSON(requestDTO);

		String url = getBASE_URL() + "?sid=" +serviceInstanceId;
		request = constructHTTPRequest(url, HTTP_METHOD.POST, jsonRequest, logger);
		request.addHeader("acl-manifest", aclManifest);
		request.execute();
		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		AddMailBoxResponseDTO responseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, AddMailBoxResponseDTO.class);
		Assert.assertEquals(FAILURE, responseDTO.getResponse().getStatus());
		Assert.assertEquals(true,
				responseDTO.getResponse().getMessage().contains(Messages.INVALID_REQUEST.value()));

	}

    /**
     * Method to test get mailBox with null.
     *
     * @throws JsonGenerationException
     * @throws JsonMappingException
     * @throws JAXBException
     * @throws IOException
     * @throws LiaisonException
     */
    @Test
    public void testGetMailBox_MailBoxisNull_ShouldFail()
            throws JsonGenerationException, JsonMappingException, JAXBException, IOException, LiaisonException {

        // Adding the mailbox
        jsonRequest = ServiceUtils.readFileFromClassPath("requests/mailbox/addmailboxrequest.json");
        AddMailboxRequestDTO requestDTO = MailBoxUtil.unmarshalFromJSON(jsonRequest, AddMailboxRequestDTO.class);

        requestDTO.setMailBox(null);

        jsonRequest = MailBoxUtil.marshalToJSON(requestDTO);

        String url = getBASE_URL() + "?sid=" + serviceInstanceId;
        request = constructHTTPRequest(url, HTTP_METHOD.POST, jsonRequest, logger);
        request.addHeader("acl-manifest", aclManifest);
        request.execute();
        jsonResponse = getOutput().toString();
        logger.info(jsonResponse);

        AddMailBoxResponseDTO responseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, AddMailBoxResponseDTO.class);
        Assert.assertEquals(FAILURE, responseDTO.getResponse().getStatus());
        Assert.assertEquals(true, responseDTO.getResponse().getMessage().contains(Messages.INVALID_REQUEST.value()));

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
		String url = getBASE_URL() + "/" + "3434"+"?sid=" +serviceInstanceId;
		request = constructHTTPRequest(url, HTTP_METHOD.GET, null, logger);
		request.addHeader("acl-manifest", aclManifest);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		GetMailBoxResponseDTO getResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, GetMailBoxResponseDTO.class);

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
		jsonRequest = MailBoxUtil.marshalToJSON(reviseRequestDTO);

		String url = getBASE_URL() + "/" + "fasdfasdfdas" + "?sid=" +serviceInstanceId;
		request = constructHTTPRequest(url, HTTP_METHOD.PUT, jsonRequest, logger);
		request.addHeader("acl-manifest", aclManifest);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);
		Assert.assertEquals(FAILURE, getResponse(jsonResponse, "reviseMailBoxResponse", STATUS));

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
		jsonRequest = MailBoxUtil.marshalToJSON(reviseRequestDTO);

		String url = getBASE_URL() + "/" + "3432432" + "?sid=" +serviceInstanceId;
		request = constructHTTPRequest(url, HTTP_METHOD.PUT, jsonRequest, logger);
		request.addHeader("acl-manifest", aclManifest);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);
		Assert.assertEquals(FAILURE, getResponse(jsonResponse, "reviseMailBoxResponse", STATUS));

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

		jsonRequest = MailBoxUtil.marshalToJSON(reviseRequestDTO);

		String url = getBASE_URL() + "/" + "123456" + "?sid=" +serviceInstanceId;
		request = constructHTTPRequest(url, HTTP_METHOD.PUT, jsonRequest, logger);
		request.addHeader("acl-manifest", aclManifest);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);
		Assert.assertEquals(FAILURE, getResponse(jsonResponse, "reviseMailBoxResponse", STATUS));

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
		request.addHeader("acl-manifest", aclManifest);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);
		Assert.assertEquals(FAILURE, getResponse(jsonResponse, "deactivateMailBoxResponse", STATUS));
	}

    /**
     * Method to test Revise mailBox without mailBox name.
     *
     * @throws JsonGenerationException
     * @throws JsonMappingException
     * @throws JAXBException
     * @throws IOException
     * @throws LiaisonException
     * @throws JSONException
     */
    @Test
    public void testReviseMailBox_WithoutName_ShouldFail()
            throws JsonGenerationException, JsonMappingException, JAXBException, IOException, LiaisonException,
            JSONException {

        // Constructing the revise
        ReviseMailBoxRequestDTO reviseRequestDTO = new ReviseMailBoxRequestDTO();
        MailBoxDTO mailBox = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        mailBox.setName(null);
        jsonRequest = MailBoxUtil.marshalToJSON(reviseRequestDTO);

        String url = getBASE_URL() + "/" + "3432432" + "?sid=" + serviceInstanceId;
        request = constructHTTPRequest(url, HTTP_METHOD.PUT, jsonRequest, logger);
        request.addHeader("acl-manifest", aclManifest);
        request.execute();

        jsonResponse = getOutput().toString();
        logger.info(jsonResponse);
        Assert.assertEquals(FAILURE, getResponse(jsonResponse, "reviseMailBoxResponse", STATUS));
        Assert.assertEquals(true,
                getResponse(jsonResponse, "reviseMailBoxResponse", MESSAGE).contains(Messages.INVALID_REQUEST.value()));
    }

    /**
     * Method to test Revise mailBox without mailBox status.
     *
     * @throws JsonGenerationException
     * @throws JsonMappingException
     * @throws JAXBException
     * @throws IOException
     * @throws LiaisonException
     * @throws JSONException
     */
    @Test
    public void testReviseMailBox_WithoutStatus_ShouldFail()
            throws JsonGenerationException, JsonMappingException, JAXBException, IOException, LiaisonException,
            JSONException {

        // Constructing the revise
        ReviseMailBoxRequestDTO reviseRequestDTO = new ReviseMailBoxRequestDTO();
        MailBoxDTO mailBox = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        mailBox.setStatus(null);
        jsonRequest = MailBoxUtil.marshalToJSON(reviseRequestDTO);

        String url = getBASE_URL() + "/" + "3432432" + "?sid=" + serviceInstanceId;
        request = constructHTTPRequest(url, HTTP_METHOD.PUT, jsonRequest, logger);
        request.addHeader("acl-manifest", aclManifest);
        request.execute();

        jsonResponse = getOutput().toString();
        logger.info(jsonResponse);
        Assert.assertEquals(FAILURE, getResponse(jsonResponse, "reviseMailBoxResponse", STATUS));
        Assert.assertEquals(true,
                getResponse(jsonResponse, "reviseMailBoxResponse", MESSAGE).contains(Messages.INVALID_REQUEST.value()));

    }

    /**
     * Method to test Revise mailBox without mailBox status.
     *
     * @throws JsonGenerationException
     * @throws JsonMappingException
     * @throws JAXBException
     * @throws IOException
     * @throws LiaisonException
     * @throws JSONException
     */
    @Test
    public void testReviseMailBox_WithoutTenancyKey_ShouldFail()
            throws JsonGenerationException, JsonMappingException, JAXBException, IOException, LiaisonException,
            JSONException {

        // Constructing the revise
        ReviseMailBoxRequestDTO reviseRequestDTO = new ReviseMailBoxRequestDTO();
        MailBoxDTO mailBox = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        mailBox.setTenancyKey(null);
        jsonRequest = MailBoxUtil.marshalToJSON(reviseRequestDTO);

        String url = getBASE_URL() + "/" + "3432432" + "?sid=" + serviceInstanceId;
        request = constructHTTPRequest(url, HTTP_METHOD.PUT, jsonRequest, logger);
        request.addHeader("acl-manifest", aclManifest);
        request.execute();

        jsonResponse = getOutput().toString();
        logger.info(jsonResponse);
        Assert.assertEquals(FAILURE, getResponse(jsonResponse, "reviseMailBoxResponse", STATUS));
        Assert.assertEquals(true,
                getResponse(jsonResponse, "reviseMailBoxResponse", MESSAGE).contains(Messages.INVALID_REQUEST.value()));

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
    public void testReviseMailBox_WithoutMandatory_ShouldFail()
            throws JsonGenerationException, JsonMappingException, JAXBException, IOException, LiaisonException,
            JSONException {

        // Constructing the revise
        ReviseMailBoxRequestDTO reviseRequestDTO = new ReviseMailBoxRequestDTO();
        reviseRequestDTO.setMailBox(null);
        jsonRequest = MailBoxUtil.marshalToJSON(reviseRequestDTO);

        String url = getBASE_URL() + "/" + "3432432" + "?sid=" + serviceInstanceId;
        request = constructHTTPRequest(url, HTTP_METHOD.PUT, jsonRequest, logger);
        request.addHeader("acl-manifest", aclManifest);
        request.execute();

        jsonResponse = getOutput().toString();
        logger.info(jsonResponse);
        Assert.assertEquals(FAILURE, getResponse(jsonResponse, "reviseMailBoxResponse", STATUS));

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

		jsonRequest = MailBoxUtil.marshalToJSON(requestDTO);

		String url = getBASE_URL() + "?sid=" +serviceInstanceId;
		request = constructHTTPRequest(url, HTTP_METHOD.POST, jsonRequest, logger);
		request.addHeader("acl-manifest", aclManifest);
		request.execute();
		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		AddMailBoxResponseDTO responseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, AddMailBoxResponseDTO.class);
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

		jsonRequest = MailBoxUtil.marshalToJSON(requestDTO);

		String url = getBASE_URL() + "?sid=" +serviceInstanceId;
		request = constructHTTPRequest(url, HTTP_METHOD.POST, jsonRequest, logger);
		request.addHeader("acl-manifest", aclManifest);
		request.execute();
		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		AddMailBoxResponseDTO responseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, AddMailBoxResponseDTO.class);
		Assert.assertEquals(FAILURE, responseDTO.getResponse().getStatus());
	}

	/**
	 * Method to search mailbox with mailbox name and profile name.
	 *
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws JAXBException
	 * @throws IOException
	 * @throws LiaisonException
	 */
	@Test(enabled=false)
	public void testSearchMailBox() throws JsonParseException, JsonMappingException,
		   JAXBException, IOException, LiaisonException {

		// Adding the mailbox
		jsonRequest = ServiceUtils.readFileFromClassPath("requests/mailbox/addmailboxrequest.json");
		AddMailboxRequestDTO requestDTO = MailBoxUtil.unmarshalFromJSON(jsonRequest, AddMailboxRequestDTO.class);

		MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
		requestDTO.setMailBox(mbxDTO);

		jsonRequest = MailBoxUtil.marshalToJSON(requestDTO);

		String url = getBASE_URL() + "?sid=" +serviceInstanceId;
		request = constructHTTPRequest(url, HTTP_METHOD.POST, jsonRequest, logger);
		request.addHeader("acl-manifest", aclManifest);
		request.execute();
		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		AddMailBoxResponseDTO responseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, AddMailBoxResponseDTO.class);
		Assert.assertEquals(SUCCESS, responseDTO.getResponse().getStatus());

		//Add profile
		String profileName = "TestProfile" + System.nanoTime();
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
		Assert.assertEquals(SUCCESS, profileResponseDTO.getResponse().getStatus());

		jsonRequest = ServiceUtils.readFileFromClassPath("requests/processor/createprocessor.json");
		AddProcessorToMailboxRequestDTO addProcessorDTO = MailBoxUtil.unmarshalFromJSON(jsonRequest, AddProcessorToMailboxRequestDTO.class);

		//Linked the mailbox and profile to processor
		ProcessorDTO proDTO = new ProcessorDTO();
		proDTO = addProcessorDTO.getProcessor();
		proDTO.setLinkedMailboxId(responseDTO.getMailBox().getGuid());
		Set<String> profiles = new HashSet<>();
		profiles.add(profileName);
		proDTO.setLinkedProfiles(profiles);
		addProcessorDTO.setProcessor(proDTO);

		jsonRequest = MailBoxUtil.marshalToJSON(addProcessorDTO);

		String addProcessor = "/" + responseDTO.getMailBox().getGuid() + "/processor" + "?sid=" +serviceInstanceId;
		request = constructHTTPRequest(getBASE_URL() + addProcessor, HTTP_METHOD.POST, jsonRequest, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		AddProcessorToMailboxResponseDTO processorResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse,
				AddProcessorToMailboxResponseDTO.class);
		Assert.assertEquals(SUCCESS, processorResponseDTO.getResponse().getStatus());

		//search mailbox by mailbox name
		url = getBASE_URL() + "/?name=" + requestDTO.getMailBox().getName();
		request = constructHTTPRequest(url, HTTP_METHOD.GET, null, logger);
		request.addHeader("acl-manifest", aclManifest);
		request.execute();
		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);
		SearchMailBoxResponseDTO searchResponceDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, SearchMailBoxResponseDTO.class);
		Assert.assertEquals(SUCCESS, searchResponceDTO.getResponse().getStatus());

		//search mailbox by profile name
		url = getBASE_URL() + "/?name=&profile=" + profileName;
		request = constructHTTPRequest(url, HTTP_METHOD.GET, null, logger);
		request.execute();
		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);
		searchResponceDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, SearchMailBoxResponseDTO.class);
		Assert.assertEquals(SUCCESS, searchResponceDTO.getResponse().getStatus());
	}

	/**
	 * Method to search mailbox with empty mailbox name.
	 *
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws JAXBException
	 * @throws IOException
	 * @throws LiaisonException
	 */
	@Test
	public void testSearchMailBox_MailboxNameIsEmpty_ShouldFail() throws JsonParseException, JsonMappingException, JAXBException, IOException, LiaisonException {

		String url = getBASE_URL() + "/?name=";
		request = constructHTTPRequest(url, HTTP_METHOD.GET, null, logger);
		request.addHeader("acl-manifest", aclManifest);
		request.execute();
		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);
		SearchMailBoxResponseDTO searchResponceDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, SearchMailBoxResponseDTO.class);
		Assert.assertEquals(FAILURE, searchResponceDTO.getResponse().getStatus());
	}

	/**
	 * Method to search mailbox with empty profile name.
	 *
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws JAXBException
	 * @throws IOException
	 * @throws LiaisonException
	 */
	@Test
	public void testSearchMailBox_ProfileNameIsEmpty_ShouldFail() throws JsonParseException, JsonMappingException, JAXBException, IOException, LiaisonException {

		String url = getBASE_URL() + "/?name=&profile=";
		request = constructHTTPRequest(url, HTTP_METHOD.GET, null, logger);
		request.addHeader("acl-manifest", aclManifest);
		request.execute();
		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);
		SearchMailBoxResponseDTO searchResponceDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, SearchMailBoxResponseDTO.class);
		Assert.assertEquals(FAILURE, searchResponceDTO.getResponse().getStatus());

	}

}
