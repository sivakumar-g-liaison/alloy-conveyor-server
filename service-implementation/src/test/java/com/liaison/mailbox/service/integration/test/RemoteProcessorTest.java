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
import com.liaison.framework.util.ServiceUtils;
import com.liaison.mailbox.jpa.model.Folder;
import com.liaison.mailbox.jpa.model.Processor;
import com.liaison.mailbox.service.base.test.BaseServiceTest;
import com.liaison.mailbox.service.core.processor.HttpRemoteDownloader;
import com.liaison.mailbox.service.dto.configuration.CredentialDTO;
import com.liaison.mailbox.service.dto.configuration.FolderDTO;
import com.liaison.mailbox.service.dto.configuration.MailBoxDTO;
import com.liaison.mailbox.service.dto.configuration.ProcessorDTO;
import com.liaison.mailbox.service.dto.configuration.ProfileDTO;
import com.liaison.mailbox.service.dto.configuration.request.AddMailboxRequestDTO;
import com.liaison.mailbox.service.dto.configuration.request.AddProcessorToMailboxRequestDTO;
import com.liaison.mailbox.service.dto.configuration.request.AddProfileRequestDTO;
import com.liaison.mailbox.service.dto.configuration.request.RemoteProcessorPropertiesDTO;
import com.liaison.mailbox.service.dto.configuration.response.AddMailBoxResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.AddProcessorToMailboxResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.AddProfileResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.AddProfileToMailBoxResponseDTO;
import com.liaison.mailbox.service.util.MailBoxUtility;

/**
 * @author praveenu
 * 
 */
public class RemoteProcessorTest extends BaseServiceTest {

	private String jsonResponse;
	private String jsonRequest;
	private HTTPRequest request;
	private Logger logger = null;
	private AddMailBoxResponseDTO responseDTO;
	private AddProfileResponseDTO profileResponseDTO;
	private AddProfileToMailBoxResponseDTO mbProfileResponseDTO;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		logger = LoggerFactory.getLogger(MailBoxProcessorServiceTest.class);
		// Adding the mailbox
		responseDTO = createMailBox();
		profileResponseDTO = addProfile();
		mbProfileResponseDTO = createProfileLink(responseDTO, profileResponseDTO);
	}

	@Test
	public void testRemoteDownloader() throws JsonParseException, JsonMappingException, LiaisonException, JSONException,
			JAXBException, IOException {

		Processor processor = new Processor();
		processor.setProcsrName("Processor");
		processor.setProcsrStatus("ACTIVE");
		String remoteProperties = ServiceUtils.readFileFromClassPath("requests/processor/remoteprocessor.json");
		processor.setProcsrProperties(remoteProperties);

		List<Folder> folders = new ArrayList<Folder>();
		Folder folder = new Folder();
		folder.setFldrType("RESPONSE_LOCATION");
		folder.setFldrUri("/sample");
		folders.add(folder);
		processor.setFolders(folders);
		HttpRemoteDownloader downloader = new HttpRemoteDownloader(processor);
		HTTPRequest request = downloader.getClientWithInjectedConfiguration();

		Assert.assertEquals(200, request.execute().getStatusCode());
	}

	public AddProcessorToMailboxResponseDTO createProcessor() throws LiaisonException, JSONException, JsonParseException,
			JsonMappingException, JAXBException, IOException {

		AddProcessorToMailboxRequestDTO addProcessorDTO = getProcessorRequest("RESPONSE_LOCATION", "db", "/sample", "ACTIVE",
				"REMOTEDOWNLOADER", null);
		jsonRequest = MailBoxUtility.marshalToJSON(addProcessorDTO);

		String addProcessor = "/" + responseDTO.getMailBox().getGuid() + "/processor";
		request = constructHTTPRequest(getBASE_URL() + addProcessor, HTTP_METHOD.POST, jsonRequest, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		AddProcessorToMailboxResponseDTO processorResponseDTO = MailBoxUtility.unmarshalFromJSON(jsonResponse,
				AddProcessorToMailboxResponseDTO.class);
		return processorResponseDTO;
	}

	private AddProcessorToMailboxRequestDTO getProcessorRequest(String folderTye, String credentialType, String folderURI,
			String processorStatus, String processorType, String javaScriptURI) throws JsonParseException, JsonMappingException,
			JAXBException, IOException {

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
		processorDTO.setRemoteDownloaderProperties(getRemoteProcessorPropertiesString());
		processorDTO.setType(processorType);
		processorDTO.setJavaScriptURI(javaScriptURI);
		processorDTO.setLinkedMailboxId(responseDTO.getMailBox().getGuid());
		processorDTO.setLinkedProfileId(mbProfileResponseDTO.getMailboxProfileLinkGuid());

		AddProcessorToMailboxRequestDTO addProcessorDTO = new AddProcessorToMailboxRequestDTO();
		addProcessorDTO.setProcessor(processorDTO);
		return addProcessorDTO;
	}

	private RemoteProcessorPropertiesDTO getRemoteProcessorPropertiesString() throws JsonParseException, JsonMappingException,
			JAXBException, IOException {

		String remoteProperties = ServiceUtils.readFileFromClassPath("requests/processor/remoteprocessor.json");
		RemoteProcessorPropertiesDTO remoteDTO = MailBoxUtility.unmarshalFromJSON(remoteProperties,
				RemoteProcessorPropertiesDTO.class);
		return remoteDTO;

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

}
