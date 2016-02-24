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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.liaison.commons.exception.LiaisonException;
import com.liaison.commons.util.client.http.HTTPRequest;
import com.liaison.commons.util.client.http.HTTPRequest.HTTP_METHOD;
import com.liaison.commons.util.client.http.HTTPResponse;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.service.base.test.BaseServiceTest;
import com.liaison.mailbox.service.dto.configuration.MailBoxDTO;
import com.liaison.mailbox.service.dto.configuration.ProcessorDTO;
import com.liaison.mailbox.service.dto.configuration.ProfileDTO;
import com.liaison.mailbox.service.dto.configuration.processor.properties.ProcessorCredentialPropertyDTO;
import com.liaison.mailbox.service.dto.configuration.processor.properties.ProcessorFolderPropertyDTO;
import com.liaison.mailbox.service.dto.configuration.processor.properties.ProcessorPropertyUITemplateDTO;
import com.liaison.mailbox.service.dto.configuration.request.AddMailboxRequestDTO;
import com.liaison.mailbox.service.dto.configuration.request.AddProcessorToMailboxRequestDTO;
import com.liaison.mailbox.service.dto.configuration.request.AddProfileRequestDTO;
import com.liaison.mailbox.service.dto.configuration.request.ReviseProcessorRequestDTO;
import com.liaison.mailbox.service.dto.configuration.response.AddMailBoxResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.AddProcessorToMailboxResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.AddProfileResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.DropboxTransferContentResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.GetTransferProfilesResponseDTO;
import com.liaison.mailbox.service.dto.dropbox.StagedFileDTO;
import com.liaison.mailbox.service.dto.dropbox.request.DropboxAuthAndGetManifestRequestDTO;
import com.liaison.mailbox.service.dto.dropbox.request.StagePayloadRequestDTO;
import com.liaison.mailbox.service.dto.dropbox.response.DropboxAuthAndGetManifestResponseDTO;
import com.liaison.mailbox.service.dto.dropbox.response.GetStagedFilesResponseDTO;
import com.liaison.mailbox.service.dto.dropbox.response.StagePayloadResponseDTO;
import com.liaison.mailbox.service.util.MailBoxUtil;

/**
 * Test class to test mailbox configuration service.
 *
 * @author OFS
 */

public class DropboxIntegrationServiceTest extends BaseServiceTest {

	private Logger logger;
	private String jsonResponse;
	private String jsonRequest;
	private HTTPRequest request;

	@BeforeClass
	public void setUp() throws Exception {
		logger = LogManager.getLogger(DropboxIntegrationServiceTest.class);
	}

	@Test
	public void testDropboxServices() throws JsonGenerationException, JsonMappingException, JAXBException, IOException, LiaisonException {

		//createMailBox
		AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
		MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
		requestDTO.setMailBox(mbxDTO);
		jsonRequest = MailBoxUtil.marshalToJSON(requestDTO);
		String url = getBASE_URL() + "?sid=" +serviceInstanceId;
		request = constructHTTPRequest(url, HTTP_METHOD.POST, jsonRequest, logger);
		request.execute();
		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);
		AddMailBoxResponseDTO mailboxResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, AddMailBoxResponseDTO.class);
		Assert.assertEquals(SUCCESS, mailboxResponseDTO.getResponse().getStatus());

		//create Profile
		String profileName = "once" + System.currentTimeMillis();
		AddProfileResponseDTO profileResponseDTO = addProfile(profileName);
		Assert.assertEquals(SUCCESS, profileResponseDTO.getResponse().getStatus());

		//createProcesser
		AddProcessorToMailboxRequestDTO addProcessorDTO = (AddProcessorToMailboxRequestDTO) getProcessorRequest(
				"RESPONSE_LOCATION", "Sample", "ACTIVE", "DROPBOXPROCESSOR" ,"Dummy_DESCRIPTION", false, "DROPBOXPROCESSOR", profileName, mailboxResponseDTO.getMailBox().getGuid());
		jsonRequest = MailBoxUtil.marshalToJSON(addProcessorDTO);
		String addProcessor = "/" + mailboxResponseDTO.getMailBox().getGuid() + "/processor" + "?sid=" +serviceInstanceId;
		request = constructHTTPRequest(getBASE_URL() + addProcessor, HTTP_METHOD.POST, jsonRequest, logger);
		request.execute();
		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);
		AddProcessorToMailboxResponseDTO processorResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse,
				AddProcessorToMailboxResponseDTO.class);
		Assert.assertEquals(SUCCESS, processorResponseDTO.getResponse().getStatus());

		//authenticate user account
		DropboxAuthAndGetManifestRequestDTO reqDTO = constructAuthenticationRequest();
		jsonRequest = MailBoxUtil.marshalToJSON(reqDTO);
		String authAndManifestURL = getBASE_URL_DROPBOX() + "/authAndGetACL";
		request = constructHTTPRequest(authAndManifestURL, HTTP_METHOD.POST, jsonRequest, logger);
		HTTPResponse authResponse = request.execute();
		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);
		DropboxAuthAndGetManifestResponseDTO authResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, DropboxAuthAndGetManifestResponseDTO.class);
		Assert.assertEquals(SUCCESS, authResponseDTO.getResponse().getStatus());

		//get transfer profiles
		String getTransferProfilesURL = getBASE_URL_DROPBOX() + "/transferProfiles";
		request = constructHTTPRequest(getTransferProfilesURL, HTTP_METHOD.GET, "", logger);
		request.addHeader(MailBoxConstants.ACL_MANIFEST_HEADER, authResponse.getHeader(MailBoxConstants.ACL_MANIFEST_HEADER));
		request.addHeader(MailBoxConstants.DROPBOX_AUTH_TOKEN, authResponse.getHeader(MailBoxConstants.DROPBOX_AUTH_TOKEN));
		request.execute();
		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);
		GetTransferProfilesResponseDTO getTransferProfilesRespDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, GetTransferProfilesResponseDTO.class);
		Assert.assertEquals(SUCCESS, getTransferProfilesRespDTO.getResponse().getStatus());

		//upload payload
		String uploadPayloadURL = getBASE_URL_DROPBOX() + "/transferContent?transferProfileId="+profileResponseDTO.getProfile().getGuId();
		request = constructHTTPRequest(uploadPayloadURL, HTTP_METHOD.POST, "DUMMY DATA TO STORE IN SPECTRUM", logger);
		request.addHeader(MailBoxConstants.ACL_MANIFEST_HEADER, authResponse.getHeader(MailBoxConstants.ACL_MANIFEST_HEADER));
		request.addHeader(MailBoxConstants.DROPBOX_AUTH_TOKEN, authResponse.getHeader(MailBoxConstants.DROPBOX_AUTH_TOKEN));
		request.execute();
		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);
		DropboxTransferContentResponseDTO uploadPayloadResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, DropboxTransferContentResponseDTO.class);
		Assert.assertEquals(SUCCESS, uploadPayloadResponseDTO.getResponse().getStatus());

		//stage payload
		StagePayloadRequestDTO stagePayloadReq = constructStagePayloadReq(mailboxResponseDTO.getMailBox().getGuid());
		String stagePayloadReqBody = MailBoxUtil.marshalToJSON(stagePayloadReq);
		String stagePayloadURL = getBASE_URL_DROPBOX() + "/stagedFiles";
		request = constructHTTPRequest(stagePayloadURL, HTTP_METHOD.POST, stagePayloadReqBody, logger);
		request.addHeader(MailBoxConstants.ACL_MANIFEST_HEADER, authResponse.getHeader(MailBoxConstants.ACL_MANIFEST_HEADER));
		request.addHeader(MailBoxConstants.DROPBOX_AUTH_TOKEN, authResponse.getHeader(MailBoxConstants.DROPBOX_AUTH_TOKEN));
		request.execute();
		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);
		StagePayloadResponseDTO stagePayloadRespDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, StagePayloadResponseDTO.class);
		Assert.assertEquals(SUCCESS, stagePayloadRespDTO.getResponse().getStatus());

		//getStaged files
		String getStagedFilesURL = getBASE_URL_DROPBOX() + "/stagedFiles";
		request = constructHTTPRequest(getStagedFilesURL, HTTP_METHOD.GET, "", logger);
		request.addHeader(MailBoxConstants.ACL_MANIFEST_HEADER, authResponse.getHeader(MailBoxConstants.ACL_MANIFEST_HEADER));
		request.addHeader(MailBoxConstants.DROPBOX_AUTH_TOKEN, authResponse.getHeader(MailBoxConstants.DROPBOX_AUTH_TOKEN));
		request.execute();
		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);
		GetStagedFilesResponseDTO getStagedFilesResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, GetStagedFilesResponseDTO.class);
		Assert.assertEquals(SUCCESS, getStagedFilesResponseDTO.getResponse().getStatus());

		//download payload
		String downloadPayloadURL = getBASE_URL_DROPBOX() + "/stagedFiles/" + stagePayloadRespDTO.getStagedFile().getGuid();
		request = constructHTTPRequest(downloadPayloadURL, HTTP_METHOD.GET, "", logger);
		request.addHeader(MailBoxConstants.ACL_MANIFEST_HEADER, authResponse.getHeader(MailBoxConstants.ACL_MANIFEST_HEADER));
		request.addHeader(MailBoxConstants.DROPBOX_AUTH_TOKEN, authResponse.getHeader(MailBoxConstants.DROPBOX_AUTH_TOKEN));
		HTTPResponse downloadPayloadResponse = request.execute();
		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);
		Assert.assertEquals(200, downloadPayloadResponse.getStatusCode());
	}

	@Test
	public void testDropboxAuthenticateS_InvalidData() throws JsonGenerationException, JsonMappingException, JAXBException, IOException, LiaisonException {

		//authenticate user account with invalid data
		DropboxAuthAndGetManifestRequestDTO reqDTO = constructAuthenticationRequest();
		reqDTO.setLoginId("USERID"+System.currentTimeMillis()+"@liaison.dev");
		reqDTO.setPassword("PASSWORD"+System.currentTimeMillis()+"@liaison.dev");
		jsonRequest = MailBoxUtil.marshalToJSON(reqDTO);
		String authAndManifestURL = getBASE_URL_DROPBOX() + "/authAndGetACL";
		request = constructHTTPRequest(authAndManifestURL, HTTP_METHOD.POST, jsonRequest, logger);
		request.execute();
		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);
		DrpboxResponseTest authResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, DrpboxResponseTest.class);
		Assert.assertEquals(FAILURE, authResponseDTO.getStatus());
		Assert.assertEquals(Messages.AUTHENTICATION_FAILURE.value(), authResponseDTO.getMessage());

		//authenticate user account with EmptyDta
		reqDTO = constructAuthenticationRequest();
		reqDTO.setLoginId("");
		reqDTO.setPassword("");
		jsonRequest = MailBoxUtil.marshalToJSON(reqDTO);
		authAndManifestURL = getBASE_URL_DROPBOX() + "/authAndGetACL";
		request = constructHTTPRequest(authAndManifestURL, HTTP_METHOD.POST, jsonRequest, logger);
		request.execute();
		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);
		authResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, DrpboxResponseTest.class);
		Assert.assertEquals(FAILURE, authResponseDTO.getStatus());
		Assert.assertEquals(Messages.AUTHENTICATION_FAILURE.value(), authResponseDTO.getMessage());

		//authenticate user account with null
		reqDTO = constructAuthenticationRequest();
		reqDTO.setLoginId(null);
		reqDTO.setPassword(null);
		jsonRequest = MailBoxUtil.marshalToJSON(reqDTO);
		authAndManifestURL = getBASE_URL_DROPBOX() + "/authAndGetACL";
		request = constructHTTPRequest(authAndManifestURL, HTTP_METHOD.POST, jsonRequest, logger);
		request.execute();
		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);
		authResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, DrpboxResponseTest.class);
		Assert.assertEquals(FAILURE, authResponseDTO.getStatus());
		Assert.assertEquals(Messages.AUTHENTICATION_FAILURE.value(), authResponseDTO.getMessage());
	}

	@Test
	public void testGetTransferProfiles_InvalidAuthResponse() throws JsonGenerationException, JsonMappingException, JAXBException, IOException, LiaisonException {

		//authenticate user account with invalid data
		DropboxAuthAndGetManifestRequestDTO reqDTO = constructAuthenticationRequest();
		reqDTO.setLoginId("USERID"+System.currentTimeMillis()+"@liaison.dev");
		reqDTO.setPassword("PASSWORD"+System.currentTimeMillis()+"@liaison.dev");
		jsonRequest = MailBoxUtil.marshalToJSON(reqDTO);
		String authAndManifestURL = getBASE_URL_DROPBOX() + "/authAndGetACL";
		request = constructHTTPRequest(authAndManifestURL, HTTP_METHOD.POST, jsonRequest, logger);
		HTTPResponse authResponse = request.execute();
		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);
		DrpboxResponseTest authResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, DrpboxResponseTest.class);
		Assert.assertEquals(FAILURE, authResponseDTO.getStatus());
		Assert.assertEquals(Messages.AUTHENTICATION_FAILURE.value(), authResponseDTO.getMessage());

		//get transfer profiles with invalid authResponse
		String getTransferProfilesURL = getBASE_URL_DROPBOX() + "/transferProfiles";
		request = constructHTTPRequest(getTransferProfilesURL, HTTP_METHOD.GET, "", logger);
		request.addHeader(MailBoxConstants.ACL_MANIFEST_HEADER, authResponse.getHeader(MailBoxConstants.ACL_MANIFEST_HEADER));
		request.addHeader(MailBoxConstants.DROPBOX_AUTH_TOKEN, authResponse.getHeader(MailBoxConstants.DROPBOX_AUTH_TOKEN));
		request.execute();
		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);
		Assert.assertEquals(jsonResponse, Messages.REQUEST_HEADER_PROPERTIES_MISSING.value());
	}

	@Test
	public void testDropboxUploadPayload_InvalidId() throws JsonGenerationException, JsonMappingException, JAXBException, IOException, LiaisonException {

		//authenticate user account
		DropboxAuthAndGetManifestRequestDTO reqDTO = constructAuthenticationRequest();
		jsonRequest = MailBoxUtil.marshalToJSON(reqDTO);
		String authAndManifestURL = getBASE_URL_DROPBOX() + "/authAndGetACL";
		request = constructHTTPRequest(authAndManifestURL, HTTP_METHOD.POST, jsonRequest, logger);
		HTTPResponse authResponse = request.execute();
		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);
		DropboxAuthAndGetManifestResponseDTO authResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, DropboxAuthAndGetManifestResponseDTO.class);
		Assert.assertEquals(SUCCESS, authResponseDTO.getResponse().getStatus());

		//upload payload with invalid profile pguid
		String processorMessage = "There are no Dropbox Processor available";
		String uploadPayloadURL = getBASE_URL_DROPBOX() + "/transferContent?transferProfileId="+"123456789";
		request = constructHTTPRequest(uploadPayloadURL, HTTP_METHOD.POST, "DUMMY DATA TO STORE IN SPECTRUM", logger);
		request.addHeader(MailBoxConstants.ACL_MANIFEST_HEADER, authResponse.getHeader(MailBoxConstants.ACL_MANIFEST_HEADER));
		request.addHeader(MailBoxConstants.DROPBOX_AUTH_TOKEN, authResponse.getHeader(MailBoxConstants.DROPBOX_AUTH_TOKEN));
		request.execute();
		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);
		Assert.assertEquals(jsonResponse, processorMessage);
		//upload payload with empty profile pguid
		uploadPayloadURL = getBASE_URL_DROPBOX() + "/transferContent?transferProfileId="+"";
		request = constructHTTPRequest(uploadPayloadURL, HTTP_METHOD.POST, "DUMMY DATA TO STORE IN SPECTRUM", logger);
		request.addHeader(MailBoxConstants.ACL_MANIFEST_HEADER, authResponse.getHeader(MailBoxConstants.ACL_MANIFEST_HEADER));
		request.addHeader(MailBoxConstants.DROPBOX_AUTH_TOKEN, authResponse.getHeader(MailBoxConstants.DROPBOX_AUTH_TOKEN));
		request.execute();
		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);
		Assert.assertEquals(jsonResponse, processorMessage);

		//upload payload with null profile pguid
		uploadPayloadURL = getBASE_URL_DROPBOX() + "/transferContent?transferProfileId="+null;
		request = constructHTTPRequest(uploadPayloadURL, HTTP_METHOD.POST, "DUMMY DATA TO STORE IN SPECTRUM", logger);
		request.addHeader(MailBoxConstants.ACL_MANIFEST_HEADER, authResponse.getHeader(MailBoxConstants.ACL_MANIFEST_HEADER));
		request.addHeader(MailBoxConstants.DROPBOX_AUTH_TOKEN, authResponse.getHeader(MailBoxConstants.DROPBOX_AUTH_TOKEN));
		request.execute();
		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);
		Assert.assertEquals(jsonResponse, processorMessage);
    }

	@Test
	public void testDropboxStagePayload_InvalidData() throws JsonGenerationException, JsonMappingException, JAXBException, IOException, LiaisonException {

		//authenticate user account
		DropboxAuthAndGetManifestRequestDTO reqDTO = constructAuthenticationRequest();
		jsonRequest = MailBoxUtil.marshalToJSON(reqDTO);
		String authAndManifestURL = getBASE_URL_DROPBOX() + "/authAndGetACL";
		request = constructHTTPRequest(authAndManifestURL, HTTP_METHOD.POST, jsonRequest, logger);
		HTTPResponse authResponse = request.execute();
		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);
		DropboxAuthAndGetManifestResponseDTO authResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, DropboxAuthAndGetManifestResponseDTO.class);
		Assert.assertEquals(SUCCESS, authResponseDTO.getResponse().getStatus());

		//stage payload with null stageFile
		StagePayloadRequestDTO stagePayloadReq = constructStagePayloadReq("MailBox"+System.currentTimeMillis());
		stagePayloadReq.setStagedFile(null);
		String stagePayloadReqBody = MailBoxUtil.marshalToJSON(stagePayloadReq);
		String stagePayloadURL = getBASE_URL_DROPBOX() + "/stagedFiles";
		request = constructHTTPRequest(stagePayloadURL, HTTP_METHOD.POST, stagePayloadReqBody, logger);
		request.addHeader(MailBoxConstants.ACL_MANIFEST_HEADER, authResponse.getHeader(MailBoxConstants.ACL_MANIFEST_HEADER));
		request.addHeader(MailBoxConstants.DROPBOX_AUTH_TOKEN, authResponse.getHeader(MailBoxConstants.DROPBOX_AUTH_TOKEN));
		request.execute();
		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);
		StagePayloadResponseDTO stagePayloadRespDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, StagePayloadResponseDTO.class);
		Assert.assertEquals(FAILURE, stagePayloadRespDTO.getResponse().getStatus());
	}

	@Test
	public void testGetStagedFiles_InvalidAuthResponse() throws JsonGenerationException, JsonMappingException, JAXBException, IOException, LiaisonException {

		//authenticate user account with invalid data
		DropboxAuthAndGetManifestRequestDTO reqDTO = constructAuthenticationRequest();
		reqDTO.setLoginId("USERID"+System.currentTimeMillis()+"@liaison.dev");
		reqDTO.setPassword("PASSWORD"+System.currentTimeMillis()+"@liaison.dev");
		jsonRequest = MailBoxUtil.marshalToJSON(reqDTO);
		String authAndManifestURL = getBASE_URL_DROPBOX() + "/authAndGetACL";
		request = constructHTTPRequest(authAndManifestURL, HTTP_METHOD.POST, jsonRequest, logger);
		HTTPResponse authResponse = request.execute();
		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);
		DrpboxResponseTest authResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, DrpboxResponseTest.class);
		Assert.assertEquals(FAILURE, authResponseDTO.getStatus());
		Assert.assertEquals(Messages.AUTHENTICATION_FAILURE.value(), authResponseDTO.getMessage());

		//getStaged files with invalid authResponse
		String getStagedFilesURL = getBASE_URL_DROPBOX() + "/stagedFiles";
		request = constructHTTPRequest(getStagedFilesURL, HTTP_METHOD.GET, "", logger);
		request.addHeader(MailBoxConstants.ACL_MANIFEST_HEADER, authResponse.getHeader(MailBoxConstants.ACL_MANIFEST_HEADER));
		request.addHeader(MailBoxConstants.DROPBOX_AUTH_TOKEN, authResponse.getHeader(MailBoxConstants.DROPBOX_AUTH_TOKEN));
		request.execute();
		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);
		Assert.assertEquals(jsonResponse, Messages.REQUEST_HEADER_PROPERTIES_MISSING.value());
	}

	@Test
	public void testdownloadpayload_InvalidId() throws LiaisonException, IOException, JAXBException {

		//authenticate user account
		DropboxAuthAndGetManifestRequestDTO reqDTO = constructAuthenticationRequest();
		jsonRequest = MailBoxUtil.marshalToJSON(reqDTO);
		String authAndManifestURL = getBASE_URL_DROPBOX() + "/authAndGetACL";
		request = constructHTTPRequest(authAndManifestURL, HTTP_METHOD.POST, jsonRequest, logger);
		HTTPResponse authResponse = request.execute();
		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);
		DropboxAuthAndGetManifestResponseDTO authResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, DropboxAuthAndGetManifestResponseDTO.class);
		Assert.assertEquals(SUCCESS, authResponseDTO.getResponse().getStatus());

		//download payload with invalid Staged file id
		String stageFileId = "123456789";
		String downloadPayloadURL = getBASE_URL_DROPBOX() + "/stagedFiles/" + stageFileId;
		request = constructHTTPRequest(downloadPayloadURL, HTTP_METHOD.GET, "", logger);
		request.addHeader(MailBoxConstants.ACL_MANIFEST_HEADER, authResponse.getHeader(MailBoxConstants.ACL_MANIFEST_HEADER));
		request.addHeader(MailBoxConstants.DROPBOX_AUTH_TOKEN, authResponse.getHeader(MailBoxConstants.DROPBOX_AUTH_TOKEN));
		request.execute();
		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);
		Assert.assertEquals(jsonResponse, Messages.STAGED_FILEID_DOES_NOT_EXIST.value().replaceAll("%s", stageFileId));

		//download payload with null Staged file id
	    downloadPayloadURL = getBASE_URL_DROPBOX() + "/stagedFiles/" + null;
		request = constructHTTPRequest(downloadPayloadURL, HTTP_METHOD.GET, "", logger);
		request.addHeader(MailBoxConstants.ACL_MANIFEST_HEADER, authResponse.getHeader(MailBoxConstants.ACL_MANIFEST_HEADER));
		request.addHeader(MailBoxConstants.DROPBOX_AUTH_TOKEN, authResponse.getHeader(MailBoxConstants.DROPBOX_AUTH_TOKEN));
		request.execute();
		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);
		Assert.assertEquals(jsonResponse, Messages.STAGED_FILEID_DOES_NOT_EXIST.value().replaceAll("%s", "null"));
	}

	private Object getProcessorRequest(String folderTye, String folderURI, String processorStatus,
			String processorType, String processorDescription, boolean isRevise, String protocolType, String profileName, String mailboxPguid) throws JsonParseException,
			JsonMappingException,
			JsonGenerationException,
			MalformedURLException, FileNotFoundException, JAXBException, IOException, LiaisonException {

		ProcessorDTO processorDTO = new ProcessorDTO();

		List<ProcessorCredentialPropertyDTO> credetnialList = new ArrayList<ProcessorCredentialPropertyDTO>();
		processorDTO.getProcessorPropertiesInTemplateJson().setCredentialProperties(credetnialList);

		List<ProcessorFolderPropertyDTO> folderList = new ArrayList<ProcessorFolderPropertyDTO>();
		processorDTO.setProcessorPropertiesInTemplateJson(new ProcessorPropertyUITemplateDTO());
		processorDTO.getProcessorPropertiesInTemplateJson().setFolderProperties(folderList);
		processorDTO.setStatus(processorStatus);
		processorDTO.setName(System.currentTimeMillis() + "");
		processorDTO.setDescription(processorDescription);

		processorDTO.setType(processorType);
		processorDTO.setProtocol(protocolType);
		processorDTO.setLinkedMailboxId(mailboxPguid);

		Set<String> profiles = new HashSet<>();
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

	/**
	 * Method to constructs profile.
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

	private StagePayloadRequestDTO constructStagePayloadReq (String mbxGuid) {

		StagePayloadRequestDTO req = new StagePayloadRequestDTO();
		StagedFileDTO stagedFile = new StagedFileDTO();
		stagedFile.setFileSize("200");
		stagedFile.setMailboxGuid(mbxGuid);
		stagedFile.setName("someName");
		stagedFile.setPath("somePath");
		stagedFile.setSpectrumUri(spectrumUri);

		req.setStagedFile(stagedFile);

		return req;
	}

	private DropboxAuthAndGetManifestRequestDTO constructAuthenticationRequest() {
		DropboxAuthAndGetManifestRequestDTO dto = new DropboxAuthAndGetManifestRequestDTO();
		dto.setLoginId(USER_ID);
		dto.setPassword(PASSWORD);
		return dto;
	}
}
