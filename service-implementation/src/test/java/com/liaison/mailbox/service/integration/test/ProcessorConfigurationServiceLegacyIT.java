/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.integration.test;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBException;

import com.liaison.commons.jaxb.JAXBUtility;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.liaison.framework.util.ServiceUtils;
import com.liaison.mailbox.enums.CredentialType;
import com.liaison.mailbox.enums.EntityStatus;
import com.liaison.mailbox.enums.ProcessorType;
import com.liaison.mailbox.enums.Protocol;
import com.liaison.mailbox.service.base.test.BaseServiceTest;
import com.liaison.mailbox.service.base.test.InitInitialDualDBContext;
import com.liaison.mailbox.service.core.MailBoxConfigurationService;
import com.liaison.mailbox.service.core.ProcessorConfigurationService;
import com.liaison.mailbox.service.core.ProfileConfigurationService;
import com.liaison.mailbox.service.dto.configuration.CredentialDTO;
import com.liaison.mailbox.service.dto.configuration.FolderDTO;
import com.liaison.mailbox.service.dto.configuration.MailBoxDTO;
import com.liaison.mailbox.service.dto.configuration.ProcessorLegacyDTO;
import com.liaison.mailbox.service.dto.configuration.ProfileDTO;
import com.liaison.mailbox.service.dto.configuration.processor.properties.ProcessorPropertyDTO;
import com.liaison.mailbox.service.dto.configuration.request.AddMailboxRequestDTO;
import com.liaison.mailbox.service.dto.configuration.request.AddProcessorToMailboxRequestDTO;
import com.liaison.mailbox.service.dto.configuration.request.AddProfileRequestDTO;
import com.liaison.mailbox.service.dto.configuration.request.RemoteProcessorPropertiesDTO;
import com.liaison.mailbox.service.dto.configuration.request.ReviseProcessorRequestDTO;
import com.liaison.mailbox.service.dto.configuration.response.AddMailBoxResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.AddProcessorToMailboxResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.AddProfileResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.GetProcessorResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.ReviseProcessorResponseDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.util.MailBoxUtil;

/**
 *
 * @author OFS
 *
 */
public class ProcessorConfigurationServiceLegacyIT extends BaseServiceTest {

    private String aclManifest = "H4sIAAAAAAAAAO1YbW/aMBD+K5U/TqRNokACn5aW0EUroaLRJrWqIpMckVfHjpwQlVb973NeKKBWXcdWEVV8QbLvfPfwnO8ewyMCVgDlKaDBIwoF4ByiofxAA6SrWldR+4qq+7o6MPoD1Tg2e8Y16qCY8hmmboQGbEFpB6VYAMs31oKHkGXrjUUGolyhguEYC/wLs6+UYJJxdhxBgWqPERFZ7uEENo9d4O29BuTp0k5TSkKcE85q25NMTHE+5yJBg5vH50V9Gp3rMo3gFE5xBpEdlgjPOMvlVuUe8QQT9uwcDJ0fgev5wWR6Lg/WVn9ZMoXklu2517bvTrxnm8tyEAzTlxHGE8/97kyb9DKZNNrDseuh25IrUhAKMVQgBGR8IcIywJfSv1k2eaeQ5dOVx9pafgu4z6WD5KsgISgzwe9ASJcUREKyrOJIhi8wXaxir01N9J/fXN+5cK989HT71PlnLGXxEizrDYm8HPvFEuuyQnTG7xuC9ovmDpZKW5iJcI4lmDTd93WpZwqUTSRbIoOaoD2DSihNlZCSvZepAlJe3v/JyEnI2ZzEJ7Hgi3QHUCAEFwrjOZmvBvHHYGNypGZ7B1hB3FKJIST8aCJizMhDFV7bRSlGPVMdmYap2n1dNcyRZmjGmTOybN1ynJ7R3dCNAkDgoFq9JR3bo7ehciEqiLvw+N5RXvJxuTa9TWjn/dxvuF4600A3Jmd+oKraDiUb1zoQlDWIxepO/HXNXg+zKtMr1sCOEsI+teC3SdbaJfhtegp9ZsF/2e6nE8f1dnq/1ydfebs3ho95wLfr3h6my6Gf29XPrh/4Ekgof8LrvaMrYBGIrRfYTmJOGpEec0bkRVdCs6tapmYq4XxmKIY615SZZYZKV7Ow1Y90sGawMRH+COog/gfxbwUzn3tYyP5iMjFEV3Xdh5CWvcjCZfU/n2x8mfqm9PwNJYKk5vgUAAA=";
    private String ftpURL = "ftp://10.146.18.10:21";
    private String ftpsURL = "ftps://10.146.18.15:21";
    private String sftpURL = "sftp://10.146.18.20:22";
    private String password = "5E701F715FDE4162938F413FECF53910";
    private String ftpUserId = "ftp_user_dev-int";
    private String ftpsUserId = "ftps_user_dev-int";
    private String sftpUserId = "sftp_user_dev-int";
    private String loginType = CredentialType.LOGIN_CREDENTIAL.getCode();
    private String trustStoreType = CredentialType.TRUSTSTORE_CERT.getCode();
    private String sshType = CredentialType.SSH_KEYPAIR.getCode();
    private String VALID_SSHKEYPAIR = "254AD0C664B44B198EE736BD89509444";
    private String VALID_TRUSTSTORE = "4463C90421854F23876C11A7A39FA41F";
    private static final String PWD_ERROR = ".* Password cannot be Empty.";
    private static final String UNAME_ERROR = ".* Username cannot be Empty.";
    private static final String TRUSTSTORE_ERROR = ".* Trust store Certificate cannot be Empty.";
    private static final String SSH_ERROR = ".* SSH Key Pair cannot be Empty.";
    private String includeFiles = ".txt";
    private String excludeFiles = ".pdf";
    private boolean createFolderInRemote = true;

    /**
     * @throws java.lang.Exception
     */
    @BeforeMethod
    public void setUp() throws Exception {
        System.setProperty("com.liaison.secure.properties.path", "invalid");
        System.setProperty("archaius.deployment.applicationId", "g2mailboxservice");
        System.setProperty("archaius.deployment.environment", "test");
        InitInitialDualDBContext.init();
    }

    /**
     * Method constructs Processor with valid data.
     * @throws Exception
     */
    @Test
    public void testCreateAndReadProcessorUsingPguid() throws Exception {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, aclManifest, mbxDTO.getModifiedBy());

        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());

        //Adding Profile
        AddProfileRequestDTO profileRequestDTO = new AddProfileRequestDTO();
        ProfileDTO profileDTO = constructDummyProfileDTO(System.currentTimeMillis());
        profileRequestDTO.setProfile(profileDTO);

        ProfileConfigurationService profileService = new ProfileConfigurationService();
        AddProfileResponseDTO profileResponse = profileService.createProfile(profileRequestDTO);

        Assert.assertEquals(SUCCESS, profileResponse.getResponse().getStatus());

        // Adding the processor
        AddProcessorToMailboxRequestDTO procRequestDTO = MailBoxUtil.unmarshalFromJSON(ServiceUtils.readFileFromClassPath("requests/processor/create_processor_legacy.json"), AddProcessorToMailboxRequestDTO.class);
        procRequestDTO.getProcessorLegacy().setLinkedMailboxId(response.getMailBox().getGuid());

        Set<String> profiles = new HashSet<String>();
        profiles.add(profileDTO.getName());
        procRequestDTO.getProcessorLegacy().setLinkedProfiles(profiles);
        ProcessorConfigurationService procService = new ProcessorConfigurationService();
        AddProcessorToMailboxResponseDTO procResponseDTO = procService.createProcessor(response.getMailBox().getGuid(), procRequestDTO, serviceInstanceId, procRequestDTO.getProcessor().getModifiedBy());

        // Get the processor
        GetProcessorResponseDTO procGetResponseDTO = procService.getProcessor(response.getMailBox().getGuid(), procResponseDTO.getProcessor().getGuId());

        // Assertion
        Assert.assertEquals(SUCCESS, procGetResponseDTO.getResponse().getStatus());
        Assert.assertEquals(procRequestDTO.getProcessorLegacy().getName(), procGetResponseDTO.getProcessor().getName());
        Assert.assertEquals(procRequestDTO.getProcessorLegacy().getStatus(), procGetResponseDTO.getProcessor().getStatus());
        Assert.assertEquals(procRequestDTO.getProcessorLegacy().getType(), procGetResponseDTO.getProcessor().getType());
        Assert.assertEquals(procRequestDTO.getProcessorLegacy().getProtocol(), procGetResponseDTO.getProcessor().getProtocol());

    }

    /**
     * @throws JAXBException
     * @throws IOException
     */
    @Test
    public void testCreateAndReviseProcessor() throws JAXBException, IOException {

        // Adding Mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mailboxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mailboxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, aclManifest, mailboxDTO.getModifiedBy());

        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());
        String mailboxId = response.getMailBox().getGuid();

        // Adding Processor
        AddProcessorToMailboxRequestDTO processorCreateRequestDTO = new AddProcessorToMailboxRequestDTO();
        ProcessorLegacyDTO processorLegacy = constructLegacyProcessorDTO(ProcessorType.REMOTEDOWNLOADER.getCode(), Protocol.FTP.name(), EntityStatus.ACTIVE.value());
        processorLegacy.setLinkedMailboxId(mailboxId);
        processorCreateRequestDTO.setProcessorLegacy(processorLegacy);
        ProcessorConfigurationService processorService = new ProcessorConfigurationService();
        AddProcessorToMailboxResponseDTO processorResponse = processorService.createProcessor(mailboxId, processorCreateRequestDTO, serviceInstanceId, processorCreateRequestDTO.getProcessor().getModifiedBy());
        Assert.assertEquals(SUCCESS, processorResponse.getResponse().getStatus());

        // revise Processor
        ReviseProcessorRequestDTO processorReviseRequestDTO = new ReviseProcessorRequestDTO();
        processorLegacy.setGuid(processorResponse.getProcessor().getGuId());
        processorLegacy.setDescription("description modified");
        processorLegacy.setName("Processor Modified" + System.currentTimeMillis());
        //processorLegacy.setStatus(EntityStatus.INACTIVE.value());

        processorLegacy.getRemoteProcessorProperties().setConnectionTimeout(60000);
        processorLegacy.getRemoteProcessorProperties().setRetryAttempts(3);
        processorReviseRequestDTO.setProcessorLegacy(processorLegacy);
        ReviseProcessorResponseDTO processorReviseResponse = processorService.reviseProcessor(processorReviseRequestDTO, response.getMailBox().getGuid(), processorResponse.getProcessor().getGuId(), processorCreateRequestDTO.getProcessor().getModifiedBy());
        Assert.assertEquals(SUCCESS, processorReviseResponse.getResponse().getStatus());

        // read Processor
        GetProcessorResponseDTO processorReadResponse = processorService.getProcessor(processorReviseResponse.getProcessor().getGuId(), false);
        Assert.assertEquals(SUCCESS, processorReadResponse.getResponse().getStatus());
        Assert.assertEquals(processorLegacy.getDescription(), processorReadResponse.getProcessor().getDescription());
        Assert.assertEquals(processorLegacy.getName(), processorReadResponse.getProcessor().getName());
        Assert.assertEquals(processorLegacy.getStatus(), processorReadResponse.getProcessor().getStatus());

        assertRemoteProcessorStaticCheck("url", processorLegacy.getRemoteProcessorProperties().getUrl(), processorReadResponse);
    }

    /**
     * Method to create sweeper processor with valid data.
     *
     * @throws JAXBException
     * @throws IOException
     */
    @Test
    public void testCreateSweeperProcessor() throws MailBoxConfigurationServicesException, JAXBException, IOException {

        // Adding Mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mailboxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mailboxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, aclManifest, mailboxDTO.getModifiedBy());

        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());
        String mailboxId = response.getMailBox().getGuid();

        // Adding Processor
        AddProcessorToMailboxRequestDTO processorCreateRequestDTO = new AddProcessorToMailboxRequestDTO();
        ProcessorLegacyDTO processorLegacy = constructLegacyProcessorDTO(ProcessorType.SWEEPER.getCode(), Protocol.SWEEPER.name(), EntityStatus.ACTIVE.value());
        processorLegacy.setLinkedMailboxId(mailboxId);
        processorCreateRequestDTO.setProcessorLegacy(processorLegacy);
        ProcessorConfigurationService processorService = new ProcessorConfigurationService();
        AddProcessorToMailboxResponseDTO processorResponse = processorService.createProcessor(mailboxId, processorCreateRequestDTO, serviceInstanceId, processorCreateRequestDTO.getProcessor().getModifiedBy());
        Assert.assertEquals(SUCCESS, processorResponse.getResponse().getStatus());

        GetProcessorResponseDTO processorReadResponse = processorService.getProcessor(processorResponse.getProcessor().getGuId(), false);
        assertRemoteProcessorStaticCheck("includeFiles", processorLegacy.getRemoteProcessorProperties().getIncludeFiles(), processorReadResponse);
        assertRemoteProcessorStaticCheck("excludeFiles", processorLegacy.getRemoteProcessorProperties().getExcludeFiles(), processorReadResponse);

    }

    /**
     * Method to create sweeper processor with valid data.
     *
     * @throws JAXBException
     * @throws IOException
     */
    @Test
    public void testCreateSweeperProcessorWithInvalidPipelineId() throws MailBoxConfigurationServicesException, JAXBException, IOException {

        // Adding Mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mailboxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mailboxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, aclManifest, mailboxDTO.getModifiedBy());

        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());
        String mailboxId = response.getMailBox().getGuid();

        // Adding Processor
        AddProcessorToMailboxRequestDTO processorCreateRequestDTO = new AddProcessorToMailboxRequestDTO();
        ProcessorLegacyDTO processorLegacy = constructLegacyProcessorDTO(ProcessorType.SWEEPER.getCode(), Protocol.SWEEPER.name(), EntityStatus.ACTIVE.value());
        processorLegacy.getRemoteProcessorProperties().setPipeLineID("INVALID");
        processorLegacy.setLinkedMailboxId(mailboxId);
        processorCreateRequestDTO.setProcessorLegacy(processorLegacy);
        ProcessorConfigurationService processorService = new ProcessorConfigurationService();
        AddProcessorToMailboxResponseDTO processorResponse = processorService.createProcessor(mailboxId, processorCreateRequestDTO, serviceInstanceId, processorCreateRequestDTO.getProcessor().getModifiedBy());
        Assert.assertEquals(FAILURE, processorResponse.getResponse().getStatus());

    }

    /**
     * Method constructs Processor with valid credential data.
     * @throws Exception
     *
     */
    @Test
    public void testCreateProcessorWithValidCredentials() throws Exception {

        // Adding Mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mailboxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mailboxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, aclManifest, mailboxDTO.getModifiedBy());

        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());
        String mailboxId = response.getMailBox().getGuid();

        AddProcessorToMailboxRequestDTO processorCreateRequestDTO = new AddProcessorToMailboxRequestDTO();

        // Adding FTP Processor
        ProcessorLegacyDTO ftpProcessorLegacy = constructLegacyProcessorDTO(ProcessorType.REMOTEDOWNLOADER.getCode(), Protocol.FTP.name(), EntityStatus.ACTIVE.value());
        ftpProcessorLegacy.setCredentials(null);
        Set<CredentialDTO> ftpCredentials = constructDummyCredentialDTO(ftpUserId, password, loginType, null);
        ftpProcessorLegacy.setCredentials(ftpCredentials);
        ftpProcessorLegacy.setLinkedMailboxId(mailboxId);
        processorCreateRequestDTO.setProcessorLegacy(ftpProcessorLegacy);
        ProcessorConfigurationService ftpProcessorService = new ProcessorConfigurationService();
        AddProcessorToMailboxResponseDTO ftpProcessorResponse = ftpProcessorService.createProcessor(mailboxId, processorCreateRequestDTO, serviceInstanceId, processorCreateRequestDTO.getProcessor().getModifiedBy());
        Assert.assertEquals(SUCCESS, ftpProcessorResponse.getResponse().getStatus());

        // Adding FTPS Processor with uname and pwd
        ProcessorLegacyDTO ftpsProcessorLegacy = constructLegacyProcessorDTO(ProcessorType.REMOTEDOWNLOADER.getCode(), Protocol.FTPS.name(), EntityStatus.ACTIVE.value());
        ftpsProcessorLegacy.setCredentials(null);
        Set<CredentialDTO> ftpsCredentials = constructDummyCredentialDTO(ftpsUserId, password, loginType, null);
        ftpsProcessorLegacy.setCredentials(ftpsCredentials);
        ftpsProcessorLegacy.setLinkedMailboxId(mailboxId);
        processorCreateRequestDTO.setProcessorLegacy(ftpsProcessorLegacy);
        ProcessorConfigurationService ftpsProcessorService = new ProcessorConfigurationService();
        AddProcessorToMailboxResponseDTO ftpsProcessorResponse = ftpsProcessorService.createProcessor(mailboxId, processorCreateRequestDTO, serviceInstanceId, processorCreateRequestDTO.getProcessor().getModifiedBy());
        Assert.assertEquals(SUCCESS, ftpsProcessorResponse.getResponse().getStatus());

        // Adding FTPS Processor with uname, pwd and truststore
        ProcessorLegacyDTO ftpsProcessorLegacy1 = constructLegacyProcessorDTO(ProcessorType.REMOTEDOWNLOADER.getCode(), Protocol.FTPS.name(), EntityStatus.ACTIVE.value());
        ftpsProcessorLegacy1.setCredentials(null);
        Set<CredentialDTO> ftpsCredentials1 = constructDummyCredentialDTO(ftpsUserId, password, loginType, null);
        Set<CredentialDTO> ftpsCredentials2 = constructDummyCredentialDTO(null, null, trustStoreType, VALID_TRUSTSTORE);
        ftpsCredentials2.addAll(ftpsCredentials1);
        ftpsProcessorLegacy1.setCredentials(ftpsCredentials2);
        ftpsProcessorLegacy1.setLinkedMailboxId(mailboxId);
        processorCreateRequestDTO.setProcessorLegacy(ftpsProcessorLegacy1);
        ProcessorConfigurationService ftpsProcessorService1 = new ProcessorConfigurationService();
        AddProcessorToMailboxResponseDTO ftpsProcessorResponse1 = ftpsProcessorService1.createProcessor(mailboxId, processorCreateRequestDTO, serviceInstanceId, ftpsProcessorLegacy1.getModifiedBy());
        Assert.assertEquals(SUCCESS, ftpsProcessorResponse1.getResponse().getStatus());

        // Adding FTPS Processor with truststore
        ProcessorLegacyDTO ftpsProcessorLegacy2 = constructLegacyProcessorDTO(ProcessorType.REMOTEDOWNLOADER.getCode(), Protocol.FTPS.name(), EntityStatus.ACTIVE.value());
        ftpsProcessorLegacy2.setCredentials(null);
        Set<CredentialDTO> ftpsCredentials3 = constructDummyCredentialDTO(null, null, trustStoreType, VALID_TRUSTSTORE);
        ftpsProcessorLegacy2.setCredentials(ftpsCredentials3);
        ftpsProcessorLegacy2.setLinkedMailboxId(mailboxId);
        processorCreateRequestDTO.setProcessorLegacy(ftpsProcessorLegacy2);
        ProcessorConfigurationService ftpsProcessorService2 = new ProcessorConfigurationService();
        AddProcessorToMailboxResponseDTO ftpsProcessorResponse2 = ftpsProcessorService2.createProcessor(mailboxId, processorCreateRequestDTO, serviceInstanceId, ftpsProcessorLegacy2.getModifiedBy());
        Assert.assertEquals(SUCCESS, ftpsProcessorResponse2.getResponse().getStatus());

        // Adding SFTP Processor with uname and pwd
        ProcessorLegacyDTO sftpProcessorLegacy = constructLegacyProcessorDTO(ProcessorType.REMOTEDOWNLOADER.getCode(), Protocol.SFTP.name(), EntityStatus.ACTIVE.value());
        sftpProcessorLegacy.setCredentials(null);
        Set<CredentialDTO> sftpCredentials = constructDummyCredentialDTO(sftpUserId, password, loginType, null);
        sftpProcessorLegacy.setCredentials(sftpCredentials);
        sftpProcessorLegacy.setLinkedMailboxId(mailboxId);
        processorCreateRequestDTO.setProcessorLegacy(sftpProcessorLegacy);
        ProcessorConfigurationService sftpProcessorService = new ProcessorConfigurationService();
        AddProcessorToMailboxResponseDTO sftpProcessorResponse = sftpProcessorService.createProcessor(mailboxId, processorCreateRequestDTO, serviceInstanceId, sftpProcessorLegacy.getModifiedBy());
        Assert.assertEquals(SUCCESS, sftpProcessorResponse.getResponse().getStatus());

        // Adding SFTP Processor with uname, pwd and SSH Keypair
        ProcessorLegacyDTO sftpProcessorLegacy1 = constructLegacyProcessorDTO(ProcessorType.REMOTEDOWNLOADER.getCode(), Protocol.SFTP.name(), EntityStatus.ACTIVE.value());
        sftpProcessorLegacy1.setCredentials(null);
        Set<CredentialDTO> sftpCredentials1 = constructDummyCredentialDTO(sftpUserId, password, loginType, null);
        Set<CredentialDTO> sftpCredentials2 = constructDummyCredentialDTO(null, null, sshType, VALID_SSHKEYPAIR);
        sftpCredentials2.addAll(sftpCredentials1);
        sftpProcessorLegacy1.setCredentials(sftpCredentials2);
        sftpProcessorLegacy1.setLinkedMailboxId(mailboxId);
        processorCreateRequestDTO.setProcessorLegacy(sftpProcessorLegacy1);
        ProcessorConfigurationService sftpProcessorService1 = new ProcessorConfigurationService();
        AddProcessorToMailboxResponseDTO sftpProcessorResponse1 = sftpProcessorService1.createProcessor(mailboxId, processorCreateRequestDTO, serviceInstanceId, sftpProcessorLegacy1.getModifiedBy());
        Assert.assertEquals(SUCCESS, sftpProcessorResponse1.getResponse().getStatus());

        // Adding SFTP Processor with uname and SSH Keypair
        ProcessorLegacyDTO sftpProcessorLegacy2 = constructLegacyProcessorDTO(ProcessorType.REMOTEDOWNLOADER.getCode(), Protocol.SFTP.name(), EntityStatus.ACTIVE.value());
        sftpProcessorLegacy2.setCredentials(null);
        Set<CredentialDTO> sftpCredentials3 = constructDummyCredentialDTO(sftpUserId, null, loginType, null);
        Set<CredentialDTO> sftpCredentials4 = constructDummyCredentialDTO(null, null, sshType, VALID_SSHKEYPAIR);
        sftpCredentials4.addAll(sftpCredentials3);
        sftpProcessorLegacy2.setCredentials(sftpCredentials4);
        sftpProcessorLegacy2.setLinkedMailboxId(mailboxId);
        processorCreateRequestDTO.setProcessorLegacy(sftpProcessorLegacy2);
        ProcessorConfigurationService sftpProcessorService2 = new ProcessorConfigurationService();
        AddProcessorToMailboxResponseDTO sftpProcessorResponse2 = sftpProcessorService2.createProcessor(mailboxId, processorCreateRequestDTO, serviceInstanceId, sftpProcessorLegacy2.getModifiedBy());
        Assert.assertEquals(SUCCESS, sftpProcessorResponse2.getResponse().getStatus());

        // Adding SFTP Processor with SSH Keypair
        ProcessorLegacyDTO sftpProcessorLegacy3 = constructLegacyProcessorDTO(ProcessorType.REMOTEDOWNLOADER.getCode(), Protocol.SFTP.name(), EntityStatus.ACTIVE.value());
        sftpProcessorLegacy3.setCredentials(null);
        Set<CredentialDTO> sftpCredentials5 = constructDummyCredentialDTO(null, null, sshType, VALID_SSHKEYPAIR);
        sftpProcessorLegacy3.setCredentials(sftpCredentials5);
        sftpProcessorLegacy3.setLinkedMailboxId(mailboxId);
        processorCreateRequestDTO.setProcessorLegacy(sftpProcessorLegacy3);
        ProcessorConfigurationService sftpProcessorService3 = new ProcessorConfigurationService();
        AddProcessorToMailboxResponseDTO sftpProcessorResponse3 = sftpProcessorService3.createProcessor(mailboxId, processorCreateRequestDTO, serviceInstanceId, sftpProcessorLegacy3.getModifiedBy());
        Assert.assertEquals(SUCCESS, sftpProcessorResponse3.getResponse().getStatus());

    }

    /**
     * Method constructs Processor with invalid credential data.
     * @throws IOException
     * @throws JAXBException
     *
     */
    @Test(expectedExceptions = {RuntimeException.class})
    public void testCreateFTPProcessorWithoutPwd() throws JAXBException, IOException {

        // Adding Mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mailboxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mailboxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, aclManifest, mailboxDTO.getModifiedBy());

        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());
        String mailboxId = response.getMailBox().getGuid();

        AddProcessorToMailboxRequestDTO processorCreateRequestDTO = new AddProcessorToMailboxRequestDTO();

        // Adding FTP Processor without pwd
        ProcessorLegacyDTO ftpProcessorLegacy = constructLegacyProcessorDTO(ProcessorType.REMOTEDOWNLOADER.getCode(), Protocol.FTP.name(), EntityStatus.ACTIVE.value());
        ftpProcessorLegacy.setCredentials(null);
        Set<CredentialDTO> ftpCredentials = constructDummyCredentialDTO(ftpUserId, null, loginType, null);
        ftpProcessorLegacy.setCredentials(ftpCredentials);
        ftpProcessorLegacy.setLinkedMailboxId(mailboxId);
        processorCreateRequestDTO.setProcessorLegacy(ftpProcessorLegacy);
        ProcessorConfigurationService ftpProcessorService = new ProcessorConfigurationService();
        AddProcessorToMailboxResponseDTO ftpProcessorResponse = ftpProcessorService.createProcessor(mailboxId, processorCreateRequestDTO, serviceInstanceId, ftpProcessorLegacy.getModifiedBy());
        Assert.assertEquals(FAILURE, ftpProcessorResponse.getResponse().getStatus());

    }

    /**
     * Method constructs Processor with invalid credential data.
     * @throws IOException
     * @throws JAXBException
     *
     */
    @Test(expectedExceptions = {RuntimeException.class})
    public void testCreateFTPProcessorWithoutName() throws JAXBException, IOException {

        // Adding Mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mailboxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mailboxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, aclManifest, mailboxDTO.getModifiedBy());

        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());
        String mailboxId = response.getMailBox().getGuid();

        AddProcessorToMailboxRequestDTO processorCreateRequestDTO = new AddProcessorToMailboxRequestDTO();

        // Adding FTP Processor without uname
        ProcessorLegacyDTO ftpProcessorLegacy1 = constructLegacyProcessorDTO(ProcessorType.REMOTEDOWNLOADER.getCode(), Protocol.FTP.name(), EntityStatus.ACTIVE.value());
        ftpProcessorLegacy1.setCredentials(null);
        Set<CredentialDTO> ftpCredentials1 = constructDummyCredentialDTO(null, password, loginType, null);
        ftpProcessorLegacy1.setCredentials(ftpCredentials1);
        ftpProcessorLegacy1.setLinkedMailboxId(mailboxId);
        processorCreateRequestDTO.setProcessorLegacy(ftpProcessorLegacy1);
        ProcessorConfigurationService ftpProcessorService1 = new ProcessorConfigurationService();
        AddProcessorToMailboxResponseDTO ftpProcessorResponse1 = ftpProcessorService1.createProcessor(mailboxId, processorCreateRequestDTO, serviceInstanceId, ftpProcessorLegacy1.getModifiedBy());
        Assert.assertEquals(FAILURE, ftpProcessorResponse1.getResponse().getStatus());
    }

    /**
     * Method constructs Processor with invalid credential data.
     * @throws IOException
     * @throws JAXBException
     *
     */
    @Test(expectedExceptions = {RuntimeException.class})
    public void testCreateFTPProcessorWithoutUnameAndPwd() throws JAXBException, IOException {

        // Adding Mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mailboxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mailboxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, aclManifest, mailboxDTO.getModifiedBy());

        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());
        String mailboxId = response.getMailBox().getGuid();

        AddProcessorToMailboxRequestDTO processorCreateRequestDTO = new AddProcessorToMailboxRequestDTO();

        // Adding FTP Processor without uname and pwd
        ProcessorLegacyDTO ftpProcessorLegacy2 = constructLegacyProcessorDTO(ProcessorType.REMOTEDOWNLOADER.getCode(), Protocol.FTP.name(), EntityStatus.ACTIVE.value());
        ftpProcessorLegacy2.setCredentials(null);
        Set<CredentialDTO> ftpCredentials2 = constructDummyCredentialDTO(null, null, loginType, null);
        ftpProcessorLegacy2.setCredentials(ftpCredentials2);
        ftpProcessorLegacy2.setLinkedMailboxId(mailboxId);
        processorCreateRequestDTO.setProcessorLegacy(ftpProcessorLegacy2);
        ProcessorConfigurationService ftpProcessorService2 = new ProcessorConfigurationService();
        AddProcessorToMailboxResponseDTO ftpProcessorResponse2 = ftpProcessorService2.createProcessor(mailboxId, processorCreateRequestDTO, serviceInstanceId, ftpProcessorLegacy2.getModifiedBy());
        Assert.assertEquals(FAILURE, ftpProcessorResponse2.getResponse().getStatus());
    }

    /**
     * Method constructs Processor with invalid credential data.
     * @throws IOException
     * @throws JAXBException
     */
    @Test(expectedExceptions = {RuntimeException.class})
    public void testCreateFTPProcessorWithoutCredentialType() throws JAXBException, IOException {

        // Adding Mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mailboxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mailboxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, aclManifest, mailboxDTO.getModifiedBy());

        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());
        String mailboxId = response.getMailBox().getGuid();

        AddProcessorToMailboxRequestDTO processorCreateRequestDTO = new AddProcessorToMailboxRequestDTO();

        // Adding FTP Processor without credentialType
        ProcessorLegacyDTO ftpProcessorLegacy3 = constructLegacyProcessorDTO(ProcessorType.REMOTEDOWNLOADER.getCode(), Protocol.FTP.name(), EntityStatus.ACTIVE.value());
        ftpProcessorLegacy3.setCredentials(null);
        Set<CredentialDTO> ftpCredentials3 = constructDummyCredentialDTO(null, null, null, null);
        ftpProcessorLegacy3.setCredentials(ftpCredentials3);
        ftpProcessorLegacy3.setLinkedMailboxId(mailboxId);
        processorCreateRequestDTO.setProcessorLegacy(ftpProcessorLegacy3);
        ProcessorConfigurationService ftpProcessorService3 = new ProcessorConfigurationService();
        AddProcessorToMailboxResponseDTO ftpProcessorResponse3 = ftpProcessorService3.createProcessor(mailboxId, processorCreateRequestDTO, serviceInstanceId, ftpProcessorLegacy3.getModifiedBy());
        Assert.assertEquals(FAILURE, ftpProcessorResponse3.getResponse().getStatus());
    }

    /**
     * Method constructs Processor with invalid credential data.
     * @throws IOException
     * @throws JAXBException
     */
    @Test(expectedExceptions = {RuntimeException.class})
    public void testCreateFTPSProcessorWithoutUname() throws JAXBException, IOException {

        // Adding Mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mailboxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mailboxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, aclManifest, mailboxDTO.getModifiedBy());

        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());
        String mailboxId = response.getMailBox().getGuid();

        AddProcessorToMailboxRequestDTO processorCreateRequestDTO = new AddProcessorToMailboxRequestDTO();

        // Adding FTPS Processor without uname
        ProcessorLegacyDTO ftpsProcessorLegacy = constructLegacyProcessorDTO(ProcessorType.REMOTEDOWNLOADER.getCode(), Protocol.FTPS.name(), EntityStatus.ACTIVE.value());
        ftpsProcessorLegacy.setCredentials(null);
        Set<CredentialDTO> ftpsCredentials = constructDummyCredentialDTO(null, password, loginType, null);
        ftpsProcessorLegacy.setCredentials(ftpsCredentials);
        ftpsProcessorLegacy.setLinkedMailboxId(mailboxId);
        processorCreateRequestDTO.setProcessorLegacy(ftpsProcessorLegacy);
        ProcessorConfigurationService ftpsProcessorService = new ProcessorConfigurationService();
        AddProcessorToMailboxResponseDTO ftpsProcessorResponse = ftpsProcessorService.createProcessor(mailboxId, processorCreateRequestDTO, serviceInstanceId, ftpsProcessorLegacy.getModifiedBy());
        Assert.assertEquals(FAILURE, ftpsProcessorResponse.getResponse().getStatus());
    }

    /**
     * Method constructs Processor with invalid credential data.
     * @throws IOException
     * @throws JAXBException
     */
    @Test(expectedExceptions = {RuntimeException.class})
    public void testCreateFTPSProcessorWithoutPwd() throws JAXBException, IOException {

        // Adding Mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mailboxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mailboxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, aclManifest, mailboxDTO.getModifiedBy());

        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());
        String mailboxId = response.getMailBox().getGuid();

        AddProcessorToMailboxRequestDTO processorCreateRequestDTO = new AddProcessorToMailboxRequestDTO();

        // Adding FTPS Processor without pwd
        ProcessorLegacyDTO ftpsProcessorLegacy1 = constructLegacyProcessorDTO(ProcessorType.REMOTEDOWNLOADER.getCode(), Protocol.FTPS.name(), EntityStatus.ACTIVE.value());
        ftpsProcessorLegacy1.setCredentials(null);
        Set<CredentialDTO> ftpsCredentials1 = constructDummyCredentialDTO(ftpsUserId, null, loginType, null);
        ftpsProcessorLegacy1.setCredentials(ftpsCredentials1);
        ftpsProcessorLegacy1.setLinkedMailboxId(mailboxId);
        processorCreateRequestDTO.setProcessorLegacy(ftpsProcessorLegacy1);
        ProcessorConfigurationService ftpsProcessorService1 = new ProcessorConfigurationService();
        AddProcessorToMailboxResponseDTO ftpsProcessorResponse1 = ftpsProcessorService1.createProcessor(mailboxId, processorCreateRequestDTO, serviceInstanceId, ftpsProcessorLegacy1.getModifiedBy());
        Assert.assertEquals(FAILURE, ftpsProcessorResponse1.getResponse().getStatus());
    }

    /**
     * Method constructs Processor with invalid credential data.
     * @throws IOException
     * @throws JAXBException
     */
    @Test(expectedExceptions = {RuntimeException.class})
    public void testCreateFTPSProcessorWithoutUnameAndPwd() throws JAXBException, IOException {

        // Adding Mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mailboxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mailboxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, aclManifest, mailboxDTO.getModifiedBy());

        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());
        String mailboxId = response.getMailBox().getGuid();

        AddProcessorToMailboxRequestDTO processorCreateRequestDTO = new AddProcessorToMailboxRequestDTO();

        // Adding FTPS Processor without uname and pwd
        ProcessorLegacyDTO ftpsProcessorLegacy2 = constructLegacyProcessorDTO(ProcessorType.REMOTEDOWNLOADER.getCode(), Protocol.FTPS.name(), EntityStatus.ACTIVE.value());
        ftpsProcessorLegacy2.setCredentials(null);
        Set<CredentialDTO> ftpsCredentials2 = constructDummyCredentialDTO(null, null, loginType, null);
        ftpsProcessorLegacy2.setCredentials(ftpsCredentials2);
        ftpsProcessorLegacy2.setLinkedMailboxId(mailboxId);
        processorCreateRequestDTO.setProcessorLegacy(ftpsProcessorLegacy2);
        ProcessorConfigurationService ftpsProcessorService2 = new ProcessorConfigurationService();
        AddProcessorToMailboxResponseDTO ftpsProcessorResponse2 = ftpsProcessorService2.createProcessor(mailboxId, processorCreateRequestDTO, serviceInstanceId, ftpsProcessorLegacy2.getModifiedBy());
        Assert.assertEquals(FAILURE, ftpsProcessorResponse2.getResponse().getStatus());
    }

    /**
     * Method constructs Processor with invalid credential data.
     * @throws IOException
     * @throws JAXBException
     */
    @Test(expectedExceptions = {RuntimeException.class})
    public void testCreateFTPSProcessorWithoutIdpUri() throws JAXBException, IOException {

        // Adding Mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mailboxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mailboxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, aclManifest, mailboxDTO.getModifiedBy());

        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());
        String mailboxId = response.getMailBox().getGuid();

        AddProcessorToMailboxRequestDTO processorCreateRequestDTO = new AddProcessorToMailboxRequestDTO();

        // Adding FTPS Processor without idpUri
        ProcessorLegacyDTO ftpsProcessorLegacy3 = constructLegacyProcessorDTO(ProcessorType.REMOTEDOWNLOADER.getCode(), Protocol.FTPS.name(), EntityStatus.ACTIVE.value());
        ftpsProcessorLegacy3.setCredentials(null);
        Set<CredentialDTO> ftpsCredentials3 = constructDummyCredentialDTO(null, null, trustStoreType, null);
        ftpsProcessorLegacy3.setCredentials(ftpsCredentials3);
        ftpsProcessorLegacy3.setLinkedMailboxId(mailboxId);
        processorCreateRequestDTO.setProcessorLegacy(ftpsProcessorLegacy3);
        ProcessorConfigurationService ftpsProcessorService3 = new ProcessorConfigurationService();
        AddProcessorToMailboxResponseDTO ftpsProcessorResponse3 = ftpsProcessorService3.createProcessor(mailboxId, processorCreateRequestDTO, serviceInstanceId, ftpsProcessorLegacy3.getModifiedBy());
        Assert.assertEquals(FAILURE, ftpsProcessorResponse3.getResponse().getStatus());
    }

    /**
     * Method constructs Processor with invalid credential data.
     * @throws IOException
     * @throws JAXBException
     */
    @Test(expectedExceptions = {RuntimeException.class})
    public void testCreateSFTPProcessorWithoutUname() throws JAXBException, IOException {

        // Adding Mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mailboxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mailboxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, aclManifest, mailboxDTO.getModifiedBy());

        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());
        String mailboxId = response.getMailBox().getGuid();

        AddProcessorToMailboxRequestDTO processorCreateRequestDTO = new AddProcessorToMailboxRequestDTO();

        // Adding SFTP Processor without uname
        ProcessorLegacyDTO sftpProcessorLegacy = constructLegacyProcessorDTO(ProcessorType.REMOTEDOWNLOADER.getCode(), Protocol.SFTP.name(), EntityStatus.ACTIVE.value());
        sftpProcessorLegacy.setCredentials(null);
        Set<CredentialDTO> sftpCredentials = constructDummyCredentialDTO(null, password, loginType, null);
        sftpProcessorLegacy.setCredentials(sftpCredentials);
        sftpProcessorLegacy.setLinkedMailboxId(mailboxId);
        processorCreateRequestDTO.setProcessorLegacy(sftpProcessorLegacy);
        ProcessorConfigurationService sftpProcessorService = new ProcessorConfigurationService();
        AddProcessorToMailboxResponseDTO sftpProcessorResponse = sftpProcessorService.createProcessor(mailboxId, processorCreateRequestDTO, serviceInstanceId, sftpProcessorLegacy.getModifiedBy());
        Assert.assertEquals(FAILURE, sftpProcessorResponse.getResponse().getStatus());
    }

    /**
     * Method constructs Processor with invalid credential data.
     * @throws IOException
     * @throws JAXBException
     */
    @Test(expectedExceptions = {RuntimeException.class})
    public void testCreateSFTPProcessorWithoutPwdAndSSHKeyPair() throws JAXBException, IOException {

        // Adding Mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mailboxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mailboxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, aclManifest, mailboxDTO.getModifiedBy());

        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());
        String mailboxId = response.getMailBox().getGuid();

        AddProcessorToMailboxRequestDTO processorCreateRequestDTO = new AddProcessorToMailboxRequestDTO();

        // Adding SFTP Processor without pwd and SSH keyPair
        ProcessorLegacyDTO sftpProcessorLegacy1 = constructLegacyProcessorDTO(ProcessorType.REMOTEDOWNLOADER.getCode(), Protocol.SFTP.name(), EntityStatus.ACTIVE.value());
        sftpProcessorLegacy1.setCredentials(null);
        Set<CredentialDTO> sftpCredentials1 = constructDummyCredentialDTO(sftpUserId, null, sshType, null);
        sftpProcessorLegacy1.setCredentials(sftpCredentials1);
        sftpProcessorLegacy1.setLinkedMailboxId(mailboxId);
        processorCreateRequestDTO.setProcessorLegacy(sftpProcessorLegacy1);
        ProcessorConfigurationService sftpProcessorService1 = new ProcessorConfigurationService();
        AddProcessorToMailboxResponseDTO sftpProcessorResponse1 = sftpProcessorService1.createProcessor(mailboxId, processorCreateRequestDTO, serviceInstanceId, sftpProcessorLegacy1.getModifiedBy());
        Assert.assertEquals(FAILURE, sftpProcessorResponse1.getResponse().getStatus());
    }

    private ProcessorLegacyDTO constructLegacyProcessorDTO(String type, String protocol, String status) {

        ProcessorLegacyDTO processorLegacyDTO = new ProcessorLegacyDTO();
        processorLegacyDTO.setName("Processor" + System.currentTimeMillis());
        processorLegacyDTO.setType(type);
        processorLegacyDTO.setProtocol(protocol);
        processorLegacyDTO.setStatus(status);
        processorLegacyDTO.setDescription("test description");
        processorLegacyDTO.setFolders(setFolderDetails(protocol, type));
        processorLegacyDTO.setCredentials(setCredentialDetails(protocol));
        processorLegacyDTO.setLinkedProfiles(setLinkedProfileDetails());
        processorLegacyDTO.setRemoteProcessorProperties(constructLegacyProperties(type, protocol));
        processorLegacyDTO.setModifiedBy("unknown-user");
        return processorLegacyDTO;

    }

    private RemoteProcessorPropertiesDTO constructLegacyProperties(String type, String protocol) {

        RemoteProcessorPropertiesDTO legacyProperties = new RemoteProcessorPropertiesDTO();
        switch (protocol.toLowerCase()) {

            case "ftp":
                legacyProperties.setUrl(ftpURL);
                legacyProperties.setPort(21);
                break;
            case "ftps":
                legacyProperties.setUrl(ftpsURL);
                legacyProperties.setPort(22);
                legacyProperties.setPassive(true);
                break;
            case "sftp":
                legacyProperties.setUrl(sftpURL);
                legacyProperties.setPort(22);
                break;
            case "sweeper":
                legacyProperties.setDeleteFileAfterSweep(true);
                legacyProperties.setPipeLineID("E074B40BB4B74431AEDBA186D26CF3DC");
                legacyProperties.setIncludeFiles(includeFiles);
                legacyProperties.setExcludeFiles(excludeFiles);
        }

        if (ProcessorType.REMOTEUPLOADER.getCode().equals(type)) {
            legacyProperties.setCreateFoldersInRemote(createFolderInRemote);
        }

        return legacyProperties;
    }

    private FolderDTO constructDummyFolderDTO(String folderType, String folderURI) {
        FolderDTO folderDTO = new FolderDTO();
        folderDTO.setFolderType(folderType);
        folderDTO.setFolderURI(folderURI);
        folderDTO.setFolderDesc("Test Description");
        return folderDTO;
    }

    private Set<FolderDTO> setFolderDetails(String protocol, String type) {

        Set<FolderDTO> folders = new HashSet<>();
        switch (protocol.toLowerCase()) {

            case "ftp":

                if (type.equals(ProcessorType.REMOTEDOWNLOADER.getCode())) {

                    folders.add(constructDummyFolderDTO("PAYLOAD_LOCATION", "/inbox"));
                    folders.add(constructDummyFolderDTO("RESPONSE_LOCATION", "/data/ftp/ftp_user_dev-int/outbox"));
                } else if (type.equals(ProcessorType.REMOTEUPLOADER.getCode())) {

                    folders.add(constructDummyFolderDTO("PAYLOAD_LOCATION", "/data/ftp/ftp_user_dev-int/outbox"));
                    folders.add(constructDummyFolderDTO("RESPONSE_LOCATION", "/inbox"));
                }
                break;
            case "ftps":

                if (type.equals(ProcessorType.REMOTEDOWNLOADER.getCode())) {

                    folders.add(constructDummyFolderDTO("PAYLOAD_LOCATION", "/inbox"));
                    folders.add(constructDummyFolderDTO("RESPONSE_LOCATION", "/data/ftps/ftps_user_dev-int/outbox"));
                } else if (type.equals(ProcessorType.REMOTEUPLOADER.getCode())) {

                    folders.add(constructDummyFolderDTO("PAYLOAD_LOCATION", "/data/ftps/ftps_user_dev-int/outbox"));
                    folders.add(constructDummyFolderDTO("RESPONSE_LOCATION", "/inbox"));
                }
                break;
            case "sftp":

                if (type.equals(ProcessorType.REMOTEDOWNLOADER.getCode())) {

                    folders.add(constructDummyFolderDTO("PAYLOAD_LOCATION", "/inbox"));
                    folders.add(constructDummyFolderDTO("RESPONSE_LOCATION", "/data/sftp/sftp_user_dev-int/outbox"));
                } else if (type.equals(ProcessorType.REMOTEUPLOADER.getCode())) {

                    folders.add(constructDummyFolderDTO("PAYLOAD_LOCATION", "/data/sftp/sftp_user_dev-int/outbox"));
                    folders.add(constructDummyFolderDTO("RESPONSE_LOCATION", "/inbox"));
                }
                break;
            case "sweeper":
                folders.add(constructDummyFolderDTO("PAYLOAD_LOCATION", "/data/ftp/ftp_user_dev-int/inbox"));
                break;
            case "filewriter":
                folders.add(constructDummyFolderDTO("FILE_WRITE_LOCATIN", "/data/ftp/ftp_user_dev-int/inbox"));
                break;
        }
        return folders;
    }

    private Set<CredentialDTO> setCredentialDetails(String protocol) {

        Set<CredentialDTO> credentials = new HashSet<>();
        String credentialType = CredentialType.LOGIN_CREDENTIAL.getCode();
        switch (protocol.toLowerCase()) {

            case "ftp":
                credentials.addAll(constructDummyCredentialDTO(ftpUserId, password, credentialType, null));
                break;
            case "ftps":
                credentials.addAll(constructDummyCredentialDTO(ftpsUserId, password, credentialType, null));
                break;
            case "sftp":
                credentials.addAll(constructDummyCredentialDTO(sftpUserId, password, credentialType, null));
                break;
        }
        return credentials;
    }

    private Set<CredentialDTO> constructDummyCredentialDTO(String userId, String password, String credentialType, String idpURI) {

        Set<CredentialDTO> credentails = new HashSet<>();

        CredentialDTO credential = new CredentialDTO();
        credential.setUserId(userId);
        credential.setPassword(password);
        credential.setCredentialType(credentialType);
        credential.setIdpURI(idpURI);

        credentails.add(credential);
        return credentails;

    }

    private Set<String> setLinkedProfileDetails() {

        Set<String> profiles = new HashSet<>();

        // create a profile
        AddProfileRequestDTO profileRequest = new AddProfileRequestDTO();
        ProfileDTO profile = constructDummyProfileDTO(System.currentTimeMillis());
        profileRequest.setProfile(profile);
        ProfileConfigurationService profileService = new ProfileConfigurationService();
        AddProfileResponseDTO response = profileService.createProfile(profileRequest);

        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());

        profiles.add(profile.getName());
        return profiles;

    }

    /**
     * Helper method for assert Remote Processor Static properties values success.
     *
     * @param property
     * @param request
     * @param procGetResponseDTO
     */
    public void assertRemoteProcessorStaticCheck(String property, Object request, GetProcessorResponseDTO procGetResponseDTO) {

        List<ProcessorPropertyDTO> respFolderProp = procGetResponseDTO.getProcessor().getProcessorPropertiesInTemplateJson().getStaticProperties();

        int respStaticPropCount = respFolderProp.size();

        for (int i = 0; i < respStaticPropCount; i++) {

            if (property.equals(respFolderProp.get(i).getName())) {
                Assert.assertEquals(request.toString(), respFolderProp.get(i).getValue());
                break;
            }
        }
    }

    @Test
    public void test() throws JAXBException, IOException {

        String json = "{\n" +
                "  \"addProcessorToMailBoxRequest\": {\n" +
                "    \"processorLegacy\": {\n" +
                "      \"guid\": \"\",\n" +
                "      \"name\": \"google testremote downloader\",\n" +
                "      \"type\": \"REMOTEDOWNLOADER\",\n" +
                "      \"status\": \"ACTIVE\",\n" +
                "      \"description\": \"\",\n" +
                "      \"protocol\": \"HTTP\",\n" +
                "      \"remoteProcessorProperties\": {\n" +
                "        \"httpVersion\": \"1.1\",\n" +
                "        \"httpVerb\": \"GET\",\n" +
                "        \"retryAttempts\": \"1\",\n" +
                "        \"socketTimeout\": \"60000\",\n" +
                "        \"connectionTimeout\": \"60000\",\n" +
                "        \"url\": \"http://soi.uat.liaison.com/soi/rest/s/r/javascript-invoker/soi-test-response\",\n" +
                "        \"port\": \"\",\n" +
                "        \"chunkedEncoding\": \"true\",\n" +
                "        \"otherRequestHeader\": [\n" +
                "          {\n" +
                "            \"name\": \"\",\n" +
                "            \"value\": \"\"\n" +
                "          }\n" +
                "        ],\n" +
                "        \"contentType\": \"\",\n" +
                "        \"encodingFormat\": \"\"\n" +
                "      },\n" +
                "      \"javaScriptURI\": null,\n" +
                "      \"folders\": [\n" +
                "        {\n" +
                "          \"folderURI\": \"/trigger\",\n" +
                "          \"folderType\": \"RESPONSE_LOCATION\",\n" +
                "          \"folderDesc\": \"test\"\n" +
                "        }\n" +
                "      ],\n" +
                "      \"credentials\": [\n" +
                "        {\n" +
                "          \"credentialType\": \"LOGIN_CREDENTIAL\",\n" +
                "          \"credentialURI\": \"\",\n" +
                "          \"userId\": \"test\",\n" +
                "          \"password\": \"test\",\n" +
                "          \"idpType\": \"test\",\n" +
                "          \"idpURI\": \"test\"\n" +
                "        }\n" +
                "      ],\n" +
                "      \"dynamicProperties\": [\n" +
                "        {\n" +
                "          \"name\": \"filename\",\n" +
                "          \"value\": \"test\"\n" +
                "        }\n" +
                "      ],\n" +
                "      \"linkedMailboxId\": \"2CA5B1B6C0A801AD0B4385E4060808F5\",\n" +
                "      \"linkedProfiles\": []\n" +
                "    }\n" +
                "  }\n" +
                "}";
        System.out.println(MailBoxUtil.unmarshalFromJSON(json, AddProcessorToMailboxRequestDTO.class));

        AddProcessorToMailboxRequestDTO serviceRequest = MailBoxUtil.unmarshalFromJSON(json,
                AddProcessorToMailboxRequestDTO.class);
    }

}
        