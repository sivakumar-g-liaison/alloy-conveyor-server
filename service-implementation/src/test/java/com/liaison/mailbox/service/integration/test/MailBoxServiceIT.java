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

import com.liaison.commons.util.client.http.HTTPRequest;
import com.liaison.commons.util.client.http.HTTPRequest.HTTP_METHOD;
import com.liaison.framework.util.ServiceUtils;
import com.liaison.mailbox.service.base.test.BaseServiceTest;
import com.liaison.mailbox.service.dto.configuration.MailBoxDTO;
import com.liaison.mailbox.service.dto.configuration.ProfileDTO;
import com.liaison.mailbox.service.dto.configuration.processor.properties.ProcessorPropertyUITemplateDTO;
import com.liaison.mailbox.service.dto.configuration.request.AddMailboxRequestDTO;
import com.liaison.mailbox.service.dto.configuration.request.AddProcessorToMailboxRequestDTO;
import com.liaison.mailbox.service.dto.configuration.request.AddProfileRequestDTO;
import com.liaison.mailbox.service.dto.configuration.response.AddMailBoxResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.AddProfileResponseDTO;
import com.liaison.mailbox.service.util.MailBoxUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test class which tests the mailbox functional services.
 *
 * @author OFS
 */
public class MailBoxServiceIT extends BaseServiceTest {

	private Logger logger = LogManager.getLogger(MailBoxServiceIT.class);
	private HTTPRequest request;
	private String jsonResponse;

	/**
	 * Method to test trigger profile.
	 */
	@Test
	public void testTriggerProfile() throws Exception {

		// Add the Mailbox
		String jsonRequest1 = ServiceUtils.readFileFromClassPath("requests/mailbox/addmailboxrequest.json");
		AddMailboxRequestDTO requestDTO = MailBoxUtil.unmarshalFromJSON(jsonRequest1, AddMailboxRequestDTO.class);

		MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
		requestDTO.setMailBox(mbxDTO);

		jsonRequest1 = MailBoxUtil.marshalToJSON(requestDTO);

		String url = getBASE_URL() + "?sid=" + serviceInstanceId;
		request = constructHTTPRequest(url, HTTP_METHOD.POST, jsonRequest1, logger);
		request.addHeader("acl-manifest", aclManifest);
		request.execute();
        Assert.assertEquals(SUCCESS, getResponse(getOutput().toString(), "addMailBoxResponse", STATUS));

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);
		AddMailBoxResponseDTO responseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, AddMailBoxResponseDTO.class);
		Assert.assertEquals(SUCCESS, responseDTO.getResponse().getStatus());

		// Add the Profile
		String profileName = "once" + System.currentTimeMillis();
		ProfileDTO profile = new ProfileDTO();
		profile.setName(profileName);
		AddProfileRequestDTO profileRequstDTO = new AddProfileRequestDTO();
		profileRequstDTO.setProfile(profile);

		jsonRequest1 = MailBoxUtil.marshalToJSON(profileRequstDTO);
		request = constructHTTPRequest(getBASE_URL() + "/profile", HTTP_METHOD.POST, jsonRequest1, logger);
		request.execute();
		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		AddProfileResponseDTO profileResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, AddProfileResponseDTO.class);
		Assert.assertEquals(SUCCESS, profileResponseDTO.getResponse().getStatus());

		// Add the Processor
		String jsonRequest = ServiceUtils.readFileFromClassPath("requests/processor/createprocessorfortriggerprofile.json");
		AddProcessorToMailboxRequestDTO addProcessorDTO = MailBoxUtil.unmarshalFromJSON(jsonRequest, AddProcessorToMailboxRequestDTO.class);
		// constructing ProcessorPropertyUITemplateDTO and staticProperies
		String propertiesJson = ServiceUtils.readFileFromClassPath("processor/properties/REMOTEDOWNLOADER.HTTP.json");
		ProcessorPropertyUITemplateDTO processorProperties = MailBoxUtil.unmarshalFromJSON(propertiesJson, ProcessorPropertyUITemplateDTO.class);
		addProcessorDTO.getProcessor().setProcessorPropertiesInTemplateJson(processorProperties);
		// constructing folderDTO
		constructFolderProperties(addProcessorDTO.getProcessor().getProcessorPropertiesInTemplateJson(), "\\data\\ftp\\ftpup\\inbox\\", "/FTPUp/DEVTEST/");  
		addProcessorDTO.getProcessor().setLinkedMailboxId(responseDTO.getMailBox().getGuid());
		addProcessorDTO.getProcessor().getLinkedProfiles().add(profileName);
		addProcessorDTO.getProcessor().setCreateConfiguredLocation(false);
		jsonRequest = MailBoxUtil.marshalToJSON(addProcessorDTO);

		String addProcessor = "/" + responseDTO.getMailBox().getGuid() + "/processor" + "?sid=" +serviceInstanceId;
		request = constructHTTPRequest(getBASE_URL() + addProcessor, HTTP_METHOD.POST, jsonRequest, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);
		Assert.assertEquals(true, getResponse(jsonResponse, "addProcessorToMailBoxResponse", STATUS).equals(SUCCESS));        
		// Trigger the profile
		String triggerProfile = "/trigger/profile" + "?name=" + profileName;
		request = constructHTTPRequest(getBASE_URL() + triggerProfile, HTTP_METHOD.POST, null, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		Assert.assertNotNull(jsonResponse);
	}

	/**
	 * Method to test trigger profile with profile as null.
	 */
	@Test
	public void testTriggerProfile_ProfileIsNull() throws Exception {

		String triggerProfile = "/trigger/profile" + "?name=" +null;
		request = constructHTTPRequest(getBASE_URL() + triggerProfile, HTTP_METHOD.POST, null, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

        Assert.assertEquals(true, getResponse(jsonResponse, "triggerProfileResponse", STATUS).equals(FAILURE));
	}

	/**
	 * Method to test trigger profile with profile as invalid.
	 */
	@Test
	public void testTriggerProfile_ProfileIsInvalid() throws Exception {

		String triggerProfile = "/trigger/profile" + "?name=" + System.currentTimeMillis()+"INVALID_PROFILE";
		request = constructHTTPRequest(getBASE_URL() + triggerProfile, HTTP_METHOD.POST, null, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

        Assert.assertEquals(true, getResponse(jsonResponse, "triggerProfileResponse", STATUS).equals(FAILURE));
	}

	/**
	 * Method to test trigger profile with profile as empty.
	 */
	@Test
	public void testTriggerProfile_ProfileIsEmpty() throws Exception {

		String triggerProfile = "/trigger/profile" + "?name=";
		request = constructHTTPRequest(getBASE_URL() + triggerProfile, HTTP_METHOD.POST, null, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

        Assert.assertEquals(true, getResponse(jsonResponse, "triggerProfileResponse", STATUS).equals(FAILURE));
	}
}
