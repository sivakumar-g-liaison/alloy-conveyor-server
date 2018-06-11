/**
 * Copyright 2017 Liaison Technologies, Inc.
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.mailbox.service.integration.test;

import com.liaison.commons.util.StreamUtil;
import com.liaison.dto.queue.WorkTicket;
import com.liaison.framework.util.ServiceUtils;
import com.liaison.fs2.api.FS2ObjectHeaders;
import com.liaison.fs2.metadata.FS2MetaSnapshot;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.dtdm.dao.ProcessorConfigurationDAOBase;
import com.liaison.mailbox.dtdm.model.Processor;
import com.liaison.mailbox.rtdm.dao.StagedFileDAO;
import com.liaison.mailbox.rtdm.dao.StagedFileDAOBase;
import com.liaison.mailbox.service.base.test.BaseServiceTest;
import com.liaison.mailbox.service.core.MailBoxConfigurationService;
import com.liaison.mailbox.service.core.ProcessorConfigurationService;
import com.liaison.mailbox.service.core.ProfileConfigurationService;
import com.liaison.mailbox.service.core.processor.HTTPRemoteUploader;
import com.liaison.mailbox.service.dto.configuration.MailBoxDTO;
import com.liaison.mailbox.service.dto.configuration.ProfileDTO;
import com.liaison.mailbox.service.dto.configuration.request.AddMailboxRequestDTO;
import com.liaison.mailbox.service.dto.configuration.request.AddProcessorToMailboxRequestDTO;
import com.liaison.mailbox.service.dto.configuration.request.AddProfileRequestDTO;
import com.liaison.mailbox.service.dto.configuration.request.RemoteProcessorPropertiesDTO;
import com.liaison.mailbox.service.dto.configuration.response.AddMailBoxResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.AddProcessorToMailboxResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.AddProfileResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.GetProcessorResponseDTO;
import com.liaison.mailbox.service.storage.util.StorageUtilities;
import com.liaison.mailbox.service.util.MailBoxUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ProcessorExecutionServiceIT extends BaseServiceTest {

    private static final Logger LOGGER = LogManager.getLogger(ProcessorExecutionServiceIT.class);

    /**
     * Method constructs HTTP Remote UploaderProcessor with direct upload true
     *
     * @throws Exception
     */
    @Test(enabled = false)
    public void testCreateAndReadHttpProcessorAndDirectUpload() throws Exception {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, mbxDTO.getModifiedBy());
        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());

        //Adding Profile
        AddProfileRequestDTO profileRequestDTO = new AddProfileRequestDTO();
        ProfileDTO profileDTO = constructDummyProfileDTO(System.currentTimeMillis());
        profileRequestDTO.setProfile(profileDTO);

        ProfileConfigurationService profileService = new ProfileConfigurationService();
        AddProfileResponseDTO profileResponse = profileService.createProfile(profileRequestDTO);
        Assert.assertEquals(SUCCESS, profileResponse.getResponse().getStatus());

        //Adding the HTTPASYNCPROCESSOR processor
        createHttpAsyncProcessor(mbxDTO, response);
        String asynURL = getBASE_URL_RUNTIME() + "/async?mailboxId=" + response.getMailBox().getGuid();

        // Adding the HTTPRemoteUploader processor
        AddProcessorToMailboxRequestDTO httpUploaderProcRequestDTO = createRemoteUploader(response, profileDTO, asynURL);
        httpUploaderProcRequestDTO.getProcessorLegacy().getRemoteProcessorProperties().setDirectUpload(true);

        ProcessorConfigurationService procService = new ProcessorConfigurationService();
        AddProcessorToMailboxResponseDTO httpUploaderProcResponseDTO = procService.createProcessor(
                response.getMailBox().getGuid(),
                httpUploaderProcRequestDTO,
                serviceInstanceId,
                httpUploaderProcRequestDTO.getProcessor().getModifiedBy());
        GetProcessorResponseDTO httpUploaderProcGetResponseDTO = procService.getProcessor(response.getMailBox().getGuid(), httpUploaderProcResponseDTO.getProcessor().getGuId());
        Assert.assertEquals(SUCCESS, httpUploaderProcGetResponseDTO.getResponse().getStatus());

        FS2MetaSnapshot metaSnapshot = createFs2URL(LOGGER, response);
        persistStageFile(metaSnapshot.getURI().toString(), response, httpUploaderProcGetResponseDTO);

        Processor processor = new ProcessorConfigurationDAOBase().find(Processor.class, httpUploaderProcGetResponseDTO.getProcessor().getGuid());
        HTTPRemoteUploader remoteUploader = new HTTPRemoteUploader(processor);
        remoteUploader.executeRequest();

    }

    /**
     * Method constructs HTTP Remote UploaderProcessor with direct upload false.
     *
     * @throws Exception
     */
    @Test(enabled = false)
    public void testCreateAndReadHttpProcessorAndExecute() throws Exception {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, mbxDTO.getModifiedBy());
        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());

        //Adding Profile
        AddProfileRequestDTO profileRequestDTO = new AddProfileRequestDTO();
        ProfileDTO profileDTO = constructDummyProfileDTO(System.currentTimeMillis());
        profileRequestDTO.setProfile(profileDTO);

        ProfileConfigurationService profileService = new ProfileConfigurationService();
        AddProfileResponseDTO profileResponse = profileService.createProfile(profileRequestDTO);
        Assert.assertEquals(SUCCESS, profileResponse.getResponse().getStatus());

        createHttpAsyncProcessor(mbxDTO, response);
        String asynURL = getBASE_URL_RUNTIME() + "/async?mailboxId=" + response.getMailBox().getGuid();

        //Adding the HTTPRemoteUploader processor
        AddProcessorToMailboxRequestDTO httpUploaderProcRequestDTO = createRemoteUploader(response, profileDTO, asynURL);
        ProcessorConfigurationService procService = new ProcessorConfigurationService();
        AddProcessorToMailboxResponseDTO httpUploaderProcResponseDTO = procService.createProcessor(
                response.getMailBox().getGuid(),
                httpUploaderProcRequestDTO,
                serviceInstanceId,
                httpUploaderProcRequestDTO.getProcessor().getModifiedBy());
        GetProcessorResponseDTO httpUploaderProcGetResponseDTO = procService.getProcessor(response.getMailBox().getGuid(), httpUploaderProcResponseDTO.getProcessor().getGuId());
        Assert.assertEquals(SUCCESS, httpUploaderProcGetResponseDTO.getResponse().getStatus());

        FS2MetaSnapshot metaSnapshot = createFs2URL(LOGGER, response);
        persistStageFile(metaSnapshot.getURI().toString(), response, httpUploaderProcGetResponseDTO);

        Processor processor = new ProcessorConfigurationDAOBase().find(Processor.class, httpUploaderProcGetResponseDTO.getProcessor().getGuid());
        HTTPRemoteUploader remoteUploader = new HTTPRemoteUploader(processor);
        remoteUploader.executeRequest();
    }

    /**
     * Method constructs HTTPS Remote UploaderProcessor with valid data.
     *
     * @throws Exception
     */
    @Test(enabled = false)
    public void testCreateAndReadHttpsProcessorAndExecute() throws Exception {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, mbxDTO.getModifiedBy());
        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());

        //Adding Profile
        AddProfileRequestDTO profileRequestDTO = new AddProfileRequestDTO();
        ProfileDTO profileDTO = constructDummyProfileDTO(System.currentTimeMillis());
        profileRequestDTO.setProfile(profileDTO);

        ProfileConfigurationService profileService = new ProfileConfigurationService();
        AddProfileResponseDTO profileResponse = profileService.createProfile(profileRequestDTO);
        Assert.assertEquals(SUCCESS, profileResponse.getResponse().getStatus());

        createHttpAsyncProcessor(mbxDTO, response);
        String asynURL = getBASE_URL_RUNTIME() + "/async?mailboxId=" + response.getMailBox().getGuid();

        //Adding the HTTPRemoteUploader processor
        AddProcessorToMailboxRequestDTO httpsUploaderProcRequestDTO = createRemoteUploader(response, profileDTO, asynURL);
        httpsUploaderProcRequestDTO.getProcessor().setProtocol(MailBoxConstants.HTTPS);
        ProcessorConfigurationService procService = new ProcessorConfigurationService();
        AddProcessorToMailboxResponseDTO httpsUploaderProcResponseDTO = procService.createProcessor(response.getMailBox().getGuid(), httpsUploaderProcRequestDTO, serviceInstanceId, httpsUploaderProcRequestDTO.getProcessor().getModifiedBy());
        GetProcessorResponseDTO httpsUploaderProcGetResponseDTO = procService.getProcessor(response.getMailBox().getGuid(), httpsUploaderProcResponseDTO.getProcessor().getGuId());
        Assert.assertEquals(SUCCESS, httpsUploaderProcGetResponseDTO.getResponse().getStatus());

        FS2MetaSnapshot metaSnapshot = createFs2URL(LOGGER, response);
        persistStageFile(metaSnapshot.getURI().toString(), response, httpsUploaderProcGetResponseDTO);

        Processor processor = new ProcessorConfigurationDAOBase().find(Processor.class, httpsUploaderProcGetResponseDTO.getProcessor().getGuid());
        HTTPRemoteUploader remoteUploader = new HTTPRemoteUploader(processor);
        remoteUploader.executeRequest();
    }

    private AddProcessorToMailboxRequestDTO createRemoteUploader(AddMailBoxResponseDTO response, ProfileDTO profileDTO, String asynURL) throws IOException {

        AddProcessorToMailboxRequestDTO httpUploaderProcRequestDTO = MailBoxUtil.unmarshalFromJSON(ServiceUtils.readFileFromClassPath("requests/processor/createuploaderprocessor.json"), AddProcessorToMailboxRequestDTO.class);
        constructDummyHttpUploaderProcessor(response, profileDTO, httpUploaderProcRequestDTO);
        httpUploaderProcRequestDTO.getProcessorLegacy().getRemoteProcessorProperties().setDirectUpload(true);
        httpUploaderProcRequestDTO.getProcessorLegacy().getRemoteProcessorProperties().setUrl(asynURL);
        return httpUploaderProcRequestDTO;
    }

    private void createHttpAsyncProcessor(MailBoxDTO mbxDTO, AddMailBoxResponseDTO response) throws IOException {

        AddProcessorToMailboxRequestDTO procRequestDTO = constructHttpProcessorDTO(response.getMailBox().getGuid(), mbxDTO);
        ProcessorConfigurationService httpAsyncProcService = new ProcessorConfigurationService();
        procRequestDTO.getProcessor().setType(MailBoxConstants.HTTPASYNCPROCESSOR);
        procRequestDTO.getProcessor().setModifiedBy("unknown-user");
        AddProcessorToMailboxResponseDTO httpAsyncProcResponseDTO = httpAsyncProcService.createProcessor(response.getMailBox().getGuid(),
                procRequestDTO,
                serviceInstanceId,
                procRequestDTO.getProcessor().getModifiedBy());
        Assert.assertEquals(SUCCESS, httpAsyncProcResponseDTO.getResponse().getStatus());
    }

    private FS2MetaSnapshot createFs2URL(final Logger logger, AddMailBoxResponseDTO response) throws IOException {

        String exampleString = "This is the sample string";
        InputStream stream = new ByteArrayInputStream(exampleString.getBytes(StandardCharsets.UTF_8));

        //Dummy headers
        FS2ObjectHeaders fs2Header = new FS2ObjectHeaders();
        fs2Header.addHeader(MailBoxConstants.KEY_GLOBAL_PROCESS_ID, response.getMailBox().getGuid());
        logger.debug("FS2 Headers set are {}", fs2Header.getHeaders());

        WorkTicket wTicket = new WorkTicket();
        wTicket.setGlobalProcessId(response.getMailBox().getGuid());
        wTicket.setPipelineId(MailBoxUtil.getGUID());
        Map<String, String> properties = new HashMap<>();
        properties.put(MailBoxConstants.PROPERTY_HTTPLISTENER_SECUREDPAYLOAD, String.valueOf(true));

        FS2MetaSnapshot metaSnapshot = StorageUtilities.persistPayload(stream, wTicket, properties, false);
        try (InputStream is = StorageUtilities.retrievePayload(metaSnapshot.getURI().toString())) {
            logger.info("The received payload is \"{}\"", new String(StreamUtil.streamToBytes(is)));
        }
        return metaSnapshot;
    }

    private void constructDummyHttpUploaderProcessor(AddMailBoxResponseDTO response, ProfileDTO profileDTO, AddProcessorToMailboxRequestDTO procRequestDTO) {

        procRequestDTO.getProcessorLegacy().setLinkedMailboxId(response.getMailBox().getGuid());
        procRequestDTO.getProcessor().setType(MailBoxConstants.REMOTEUPLOADER);
        procRequestDTO.getProcessor().setProtocol(MailBoxConstants.HTTP);

        RemoteProcessorPropertiesDTO remoteProperties =  procRequestDTO.getProcessorLegacy().getRemoteProcessorProperties();
        remoteProperties.setHttpListenerAuthCheckRequired(false);
        remoteProperties.setHttpVerb(MailBoxConstants.POST);
        remoteProperties.setConnectionTimeout(60000);
        remoteProperties.setSocketTimeout(60000);
        remoteProperties.setHttpVersion("1.1");
        remoteProperties.setExecution("Once");
        remoteProperties.setContentType("text/plain");
        remoteProperties.setSaveResponsePayload(true);
        remoteProperties.setHttpListenerAuthCheckRequired(false);
        remoteProperties.setDirectUpload(false);
        remoteProperties.setUseFileSystem(false);
        procRequestDTO.getProcessor().setClusterType(MailBoxUtil.CLUSTER_TYPE);

        Set<String> profiles = new HashSet<>();
        profiles.add(profileDTO.getName());
        procRequestDTO.getProcessorLegacy().setLinkedProfiles(profiles);
    }

    private void persistStageFile(String URL, AddMailBoxResponseDTO response, GetProcessorResponseDTO procGetResponseDTO) {

        StagedFileDAO dao = new StagedFileDAOBase();
        WorkTicket workTicket = new WorkTicket();
        workTicket.setFileName("gmb1056__novdp7f37m.txt");
        workTicket.setGlobalProcessId(MailBoxUtil.getGUID());
        workTicket.setPayloadURI(URL);
        workTicket.setAdditionalContext(MailBoxConstants.KEY_FILE_PATH, "");
        workTicket.setAdditionalContext(MailBoxConstants.KEY_MAILBOX_ID, response.getMailBox().getGuid());
        workTicket.setPayloadSize((long) 50);

        dao.persistStagedFile(workTicket, procGetResponseDTO.getProcessor().getGuid(), procGetResponseDTO.getProcessor().getType(), false);
    }
}
