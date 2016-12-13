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

import javax.xml.bind.JAXBException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

/**
 * Test class which tests the mailbox functional services.
 *
 * @author OFS
 */
public class MailBoxServiceIT extends BaseServiceTest {

	private Logger logger = null;
	private String aclManifest = "H4sIAAAAAAAAAO1YbW/aMBD+K5U/TqRNokACn5aW0EUroaLRJrWqIpMckVfHjpwQlVb973NeKKBWXcdWEVV8QbLvfPfwnO8ewyMCVgDlKaDBIwoF4ByiofxAA6SrWldR+4qq+7o6MPoD1Tg2e8Y16qCY8hmmboQGbEFpB6VYAMs31oKHkGXrjUUGolyhguEYC/wLs6+UYJJxdhxBgWqPERFZ7uEENo9d4O29BuTp0k5TSkKcE85q25NMTHE+5yJBg5vH50V9Gp3rMo3gFE5xBpEdlgjPOMvlVuUe8QQT9uwcDJ0fgev5wWR6Lg/WVn9ZMoXklu2517bvTrxnm8tyEAzTlxHGE8/97kyb9DKZNNrDseuh25IrUhAKMVQgBGR8IcIywJfSv1k2eaeQ5dOVx9pafgu4z6WD5KsgISgzwe9ASJcUREKyrOJIhi8wXaxir01N9J/fXN+5cK989HT71PlnLGXxEizrDYm8HPvFEuuyQnTG7xuC9ovmDpZKW5iJcI4lmDTd93WpZwqUTSRbIoOaoD2DSihNlZCSvZepAlJe3v/JyEnI2ZzEJ7Hgi3QHUCAEFwrjOZmvBvHHYGNypGZ7B1hB3FKJIST8aCJizMhDFV7bRSlGPVMdmYap2n1dNcyRZmjGmTOybN1ynJ7R3dCNAkDgoFq9JR3bo7ehciEqiLvw+N5RXvJxuTa9TWjn/dxvuF4600A3Jmd+oKraDiUb1zoQlDWIxepO/HXNXg+zKtMr1sCOEsI+teC3SdbaJfhtegp9ZsF/2e6nE8f1dnq/1ydfebs3ho95wLfr3h6my6Gf29XPrh/4Ekgof8LrvaMrYBGIrRfYTmJOGpEec0bkRVdCs6tapmYq4XxmKIY615SZZYZKV7Ow1Y90sGawMRH+COog/gfxbwUzn3tYyP5iMjFEV3Xdh5CWvcjCZfU/n2x8mfqm9PwNJYKk5vgUAAA=";
	private String aclManifest_UnknownOrganization = "H4sIAAAAAAAAAM1UTU/jMBD9KysfVw1qA8uuctpAWYhUgtR6tRIIoWkym7XwR2S7EVHV/87YSSkc9sQBFMnKzDy/9zLxeMtQdyhNiyzbssoieKzntLCMpdPZaTI9To6nfJZm306yk/To9PuPWzZhjTRrkEXNMr2RcsJasKj9q9iaCp07JDYObYjYb3rhhqPzHDXoqv8pBQhn9FGNHRuAv4R1vgSFr3cv4G1u9HrW520rRQVeGE381yDk2jyxHZmQ4P8aq1h2t30JBgp2mZKWNRLPwGGdV8HtudGeUhFeGwVCj+DF4PALx+qfNtI0At10RgQDivehe+xmeZmXxW3Oi5vypVZoj1aD3DOtygd+cX4Vdwd5EmP5/LooKXMf+iY6IbHBaMKiMxtbhY1fA34MR70l9XC5Rxyq4SvwyROgBg+JgrZFS3ValXAudom4O5CbPfGhNFL/uSr4xaJYcba7303ebST8PgUaGlR0Sj7WS5Oq4YSQp05E0Ae6ecQ++SydGRuSrK15fN+JoWfCNOlhvRpI59iirpHGPQ4ZDYaPnKPmOB372Y1zoMD2h/vjDVtI3/1fIhSji2dVzm3b3QQAAA==";

	private HTTPRequest request;
	private String jsonRequest;

	private String jsonResponse;

	@BeforeMethod
	public void setUp() throws Exception {
		logger = LogManager.getLogger(MailBoxServiceIT.class);
	}

	/**
	 * Method to test triggerprofile.
	 */
	@Test
	public void testTriggerProfile() throws Exception {

		// Add the Mailbox
		String serviceInstanceId = "9032A4910A0A52980A0EC676DB33A102";
		jsonRequest = ServiceUtils.readFileFromClassPath("requests/mailbox/addmailboxrequest.json");
		AddMailboxRequestDTO requestDTO = MailBoxUtil.unmarshalFromJSON(jsonRequest, AddMailboxRequestDTO.class);

		MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
		requestDTO.setMailBox(mbxDTO);

		jsonRequest = MailBoxUtil.marshalToJSON(requestDTO);

		String url = getBASE_URL() + "?sid=" +serviceInstanceId;
		request = constructHTTPRequest(url, HTTP_METHOD.POST, jsonRequest, logger);
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

		jsonRequest = MailBoxUtil.marshalToJSON(profileRequstDTO);
		request = constructHTTPRequest(getBASE_URL() + "/profile", HTTP_METHOD.POST, jsonRequest, logger);
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
	 *
	 * @throws LiaisonException
	 * @throws JSONException
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws JAXBException
	 * @throws IOException
	 */
	@Test
	public void testTriggerProfile_ProfileIsNull() throws LiaisonException, JSONException, JsonParseException, JsonMappingException,
	         JAXBException, IOException {

		String triggerProfile = "/trigger/profile" + "?name=" +null;
		request = constructHTTPRequest(getBASE_URL() + triggerProfile, HTTP_METHOD.POST, null, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

        Assert.assertEquals(true, getResponse(jsonResponse, "triggerProfileResponse", STATUS).equals(FAILURE));
	}

	/**
	 * Method to test trigger profile with profile as invalid.
	 *
	 * @throws LiaisonException
	 * @throws JSONException
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws JAXBException
	 * @throws IOException
	 */
	@Test
	public void testTriggerProfile_ProfileIsInvalid() throws LiaisonException, JSONException, JsonParseException, JsonMappingException,
            JAXBException, IOException {

		String triggerProfile = "/trigger/profile" + "?name=" + System.currentTimeMillis()+"INVALID_PROFILE";
		request = constructHTTPRequest(getBASE_URL() + triggerProfile, HTTP_METHOD.POST, null, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

        Assert.assertEquals(true, getResponse(jsonResponse, "triggerProfileResponse", STATUS).equals(FAILURE));
	}

	/**
	 * Method to test trigger profile with profile as empty.
	 *
	 * @throws LiaisonException
	 * @throws JSONException
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws JAXBException
	 * @throws IOException
	 */
	@Test
	public void testTriggerProfile_ProfileIsEmpty() throws LiaisonException, JSONException, JsonParseException, JsonMappingException,
            JAXBException, IOException {

		String triggerProfile = "/trigger/profile" + "?name=";
		request = constructHTTPRequest(getBASE_URL() + triggerProfile, HTTP_METHOD.POST, null, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

        Assert.assertEquals(true, getResponse(jsonResponse, "triggerProfileResponse", STATUS).equals(FAILURE));
	}
}
