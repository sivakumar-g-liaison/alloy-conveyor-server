/**
 * Copyright 2016 Liaison Technologies, Inc.
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
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBException;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jettison.json.JSONException;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.liaison.commons.exception.LiaisonException;
import com.liaison.commons.security.pkcs7.SymmetricAlgorithmException;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.enums.ProcessorType;
import com.liaison.mailbox.service.base.test.BaseServiceTest;
import com.liaison.mailbox.service.base.test.InitInitialDualDBContext;
import com.liaison.mailbox.service.core.MailBoxConfigurationService;
import com.liaison.mailbox.service.core.ProcessorConfigurationService;
import com.liaison.mailbox.service.core.ProfileConfigurationService;
import com.liaison.mailbox.service.dto.GenericSearchFilterDTO;
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
import com.liaison.mailbox.service.dto.configuration.response.DeActivateProcessorResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.GetProcessorResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.InterruptExecutionEventResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.ReviseProcessorResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.SearchProcessorResponseDTO;
import com.liaison.mailbox.service.dto.ui.GetExecutingProcessorResponseDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.util.MailBoxUtil;

public class ProcessorConfigurationServiceIT extends BaseServiceTest {

    private String serviceInstanceId = "5D9C3B487184426E9F9629EFEE7C5913";
    private String aclManifest = "H4sIAAAAAAAAAO1YbW/aMBD+K5U/TqRNokACn5aW0EUroaLRJrWqIpMckVfHjpwQlVb973NeKKBWXcdWEVV8QbLvfPfwnO8ewyMCVgDlKaDBIwoF4ByiofxAA6SrWldR+4qq+7o6MPoD1Tg2e8Y16qCY8hmmboQGbEFpB6VYAMs31oKHkGXrjUUGolyhguEYC/wLs6+UYJJxdhxBgWqPERFZ7uEENo9d4O29BuTp0k5TSkKcE85q25NMTHE+5yJBg5vH50V9Gp3rMo3gFE5xBpEdlgjPOMvlVuUe8QQT9uwcDJ0fgev5wWR6Lg/WVn9ZMoXklu2517bvTrxnm8tyEAzTlxHGE8/97kyb9DKZNNrDseuh25IrUhAKMVQgBGR8IcIywJfSv1k2eaeQ5dOVx9pafgu4z6WD5KsgISgzwe9ASJcUREKyrOJIhi8wXaxir01N9J/fXN+5cK989HT71PlnLGXxEizrDYm8HPvFEuuyQnTG7xuC9ovmDpZKW5iJcI4lmDTd93WpZwqUTSRbIoOaoD2DSihNlZCSvZepAlJe3v/JyEnI2ZzEJ7Hgi3QHUCAEFwrjOZmvBvHHYGNypGZ7B1hB3FKJIST8aCJizMhDFV7bRSlGPVMdmYap2n1dNcyRZmjGmTOybN1ynJ7R3dCNAkDgoFq9JR3bo7ehciEqiLvw+N5RXvJxuTa9TWjn/dxvuF4600A3Jmd+oKraDiUb1zoQlDWIxepO/HXNXg+zKtMr1sCOEsI+teC3SdbaJfhtegp9ZsF/2e6nE8f1dnq/1ydfebs3ho95wLfr3h6my6Gf29XPrh/4Ekgof8LrvaMrYBGIrRfYTmJOGpEec0bkRVdCs6tapmYq4XxmKIY615SZZYZKV7Ow1Y90sGawMRH+COog/gfxbwUzn3tYyP5iMjFEV3Xdh5CWvcjCZfU/n2x8mfqm9PwNJYKk5vgUAAA=";
    private final String dummyValue = "dummy";
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
     * @throws MailBoxConfigurationServicesException
     *
     * @throws LiaisonException
     * @throws JSONException
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws JAXBException
     * @throws IOException
     * @throws SymmetricAlgorithmException
     * @throws SecurityException
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    @Test
	public void testCreateandReadProcessorUsingPguid()
			throws MailBoxConfigurationServicesException, JsonParseException, JsonMappingException, JAXBException,
			IOException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, aclManifest, mbxDTO.getModifiedBy());

        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());

        // Adding the processor
        AddProcessorToMailboxRequestDTO procRequestDTO = constructDummyProcessorDTO(response.getMailBox().getGuid(), mbxDTO);
        ProcessorConfigurationService procService = new ProcessorConfigurationService();
        AddProcessorToMailboxResponseDTO procResponseDTO = procService.createProcessor(response.getMailBox().getGuid(), procRequestDTO, serviceInstanceId, procRequestDTO.getProcessor().getModifiedBy());

        // Get the processor
        GetProcessorResponseDTO procGetResponseDTO = procService.getProcessor(response.getMailBox().getGuid(), procResponseDTO.getProcessor().getGuId());

        // Assertion
        Assert.assertEquals(SUCCESS, procGetResponseDTO.getResponse().getStatus());
        Assert.assertEquals(procRequestDTO.getProcessor().getName(), procGetResponseDTO.getProcessor().getName());
        Assert.assertEquals(procRequestDTO.getProcessor().getStatus(), procGetResponseDTO.getProcessor().getStatus());
        Assert.assertEquals(procRequestDTO.getProcessor().getType(), procGetResponseDTO.getProcessor().getType());
        Assert.assertEquals(procRequestDTO.getProcessor().getProtocol(), procGetResponseDTO.getProcessor().getProtocol());

    }

    /**
     * Method constructs Processor with valid data.
     * @throws MailBoxConfigurationServicesException
     *
     * @throws LiaisonException
     * @throws JSONException
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws JAXBException
     * @throws IOException
     * @throws SymmetricAlgorithmException
     * @throws SecurityException
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    @Test
    public void testCreateandReadProcessorUsingInvalidPguid()
            throws MailBoxConfigurationServicesException, JsonParseException, JsonMappingException, JAXBException,
            IOException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {

        // Get the processor
        GetProcessorResponseDTO procGetResponseDTO = new ProcessorConfigurationService().getProcessor("Invalid");

        // Assertion
        Assert.assertEquals(FAILURE, procGetResponseDTO.getResponse().getStatus());

    }

    /**
     * Method constructs Processor with empty service instance Id.
     *
     * @throws LiaisonException
     * @throws JSONException
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws JAXBException
     * @throws IOException
     * @throws SymmetricAlgorithmException
     * @throws SecurityException
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    @Test
    public void testCreateProcessorwithEmptyServiceInstanceId()
            throws LiaisonException, JSONException, JsonParseException, JsonMappingException, JAXBException,
            IOException, SymmetricAlgorithmException, SecurityException, NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, aclManifest, mbxDTO.getModifiedBy());

        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());

        // Adding the processor
        AddProcessorToMailboxRequestDTO procRequestDTO = constructDummyProcessorDTO(response.getMailBox().getGuid(), mbxDTO);
        ProcessorConfigurationService procService = new ProcessorConfigurationService();
        AddProcessorToMailboxResponseDTO procResponseDTO = procService.createProcessor(response.getMailBox().getGuid(), procRequestDTO, "", procRequestDTO.getProcessor().getModifiedBy());

        // Assertion
        Assert.assertEquals(FAILURE, procResponseDTO.getResponse().getStatus());
        Assert.assertTrue(procResponseDTO.getResponse().getMessage().contains(Messages.SERVICE_INSTANCE_ID_NOT_AVAILABLE.value()));

    }

    /**
     * Method constructs Processor with Wrong LinkedMailId.
     *
     * @throws LiaisonException
     * @throws JSONException
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws JAXBException
     * @throws IOException
     * @throws SymmetricAlgorithmException
     * @throws SecurityException
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    @Test
    public void testCreateProcessorwithWrongLinkedMailId()
            throws LiaisonException, JSONException, JsonParseException, JsonMappingException, JAXBException,
            IOException, SymmetricAlgorithmException, SecurityException, NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, aclManifest, mbxDTO.getModifiedBy());

        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());

        // Adding the processor
        AddProcessorToMailboxRequestDTO procRequestDTO = constructDummyProcessorDTO(response.getMailBox().getGuid(), mbxDTO);
        ProcessorConfigurationService procService = new ProcessorConfigurationService();
        AddProcessorToMailboxResponseDTO procResponseDTO = procService.createProcessor(response.getMailBox().getGuid() + System.currentTimeMillis(), procRequestDTO, serviceInstanceId, procRequestDTO.getProcessor().getModifiedBy());

        // Assertion
        Assert.assertEquals(FAILURE, procResponseDTO.getResponse().getStatus());
        Assert.assertTrue(procResponseDTO.getResponse().getMessage().contains(Messages.GUID_DOES_NOT_MATCH.value().replaceAll("%s", MailBoxConstants.MAILBOX)));

    }

    /**
     * Method constructs Processor with processor null.
     *
     * @throws LiaisonException
     * @throws JSONException
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws JAXBException
     * @throws IOException
     * @throws SymmetricAlgorithmException
     * @throws SecurityException
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    @Test
    public void testCreateProcessorWithProcessorNull()
            throws LiaisonException, JSONException, JsonParseException, JsonMappingException, JAXBException,
            IOException, SymmetricAlgorithmException, SecurityException, NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, aclManifest, mbxDTO.getModifiedBy());

        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());

        // Adding the processor
        AddProcessorToMailboxRequestDTO procRequestDTO = constructDummyProcessorDTO(response.getMailBox().getGuid(), mbxDTO);
        ProcessorConfigurationService procService = new ProcessorConfigurationService();
        procRequestDTO.setProcessor(null);
        AddProcessorToMailboxResponseDTO procResponseDTO = procService.createProcessor(response.getMailBox().getGuid(), procRequestDTO, serviceInstanceId, procRequestDTO.getProcessor().getModifiedBy());


        // Assertion
        Assert.assertEquals(FAILURE, procResponseDTO.getResponse().getStatus());
        Assert.assertTrue(procResponseDTO.getResponse().getMessage().contains(Messages.INVALID_REQUEST.value()));

    }

    /**
     * Method constructs Processor with wrong processor name.
     *
     * @throws LiaisonException
     * @throws JSONException
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws JAXBException
     * @throws IOException
     * @throws SymmetricAlgorithmException
     * @throws SecurityException
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    @Test
    public void testCreateProcessorWithProcessorWithWrongProcessorName()
            throws LiaisonException, JSONException, JsonParseException, JsonMappingException, JAXBException,
            IOException, SymmetricAlgorithmException, SecurityException, NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, aclManifest, mbxDTO.getModifiedBy());

        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());

        // Adding the processor
        AddProcessorToMailboxRequestDTO procRequestDTO = constructDummyProcessorDTO(response.getMailBox().getGuid(), mbxDTO);
        ProcessorConfigurationService procService = new ProcessorConfigurationService();
        AddProcessorToMailboxResponseDTO procResponseDTO = procService.createProcessor(response.getMailBox().getGuid(), procRequestDTO, serviceInstanceId, procRequestDTO.getProcessor().getModifiedBy());

        Assert.assertEquals(SUCCESS, procResponseDTO.getResponse().getStatus());

        // Adding the processor
        AddProcessorToMailboxRequestDTO processorRequestDTO = constructDummyProcessorDTO(response.getMailBox().getGuid(), mbxDTO);
        processorRequestDTO.getProcessor().setName(procRequestDTO.getProcessor().getName());
        AddProcessorToMailboxResponseDTO processorResponseDTO = procService.createProcessor(response.getMailBox().getGuid(), processorRequestDTO, serviceInstanceId, procRequestDTO.getProcessor().getModifiedBy());

        // Assertion
        Assert.assertEquals(FAILURE, processorResponseDTO.getResponse().getStatus());
        Assert.assertTrue(processorResponseDTO.getResponse().getMessage().contains(Messages.ENTITY_ALREADY_EXIST.value().replaceAll("%s", MailBoxConstants.MAILBOX_PROCESSOR)));

    }

    /**
     * Method constructs Processor with ServiceInstanceId Null.
     *
     * @throws LiaisonException
     * @throws JSONException
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws JAXBException
     * @throws IOException
     * @throws SymmetricAlgorithmException
     * @throws SecurityException
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    @Test
    public void testCreateProcessorWithServiceInstanceIdNull()
            throws LiaisonException, JSONException, JsonParseException, JsonMappingException, JAXBException,
            IOException, SymmetricAlgorithmException, SecurityException, NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, aclManifest, mbxDTO.getModifiedBy());

        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());

        // Adding the processor
        AddProcessorToMailboxRequestDTO procRequestDTO = constructDummyProcessorDTO(response.getMailBox().getGuid(), mbxDTO);
        ProcessorConfigurationService procService = new ProcessorConfigurationService();
        AddProcessorToMailboxResponseDTO procResponseDTO = procService.createProcessor(response.getMailBox().getGuid(), procRequestDTO, MailBoxUtil.getGUID(), procRequestDTO.getProcessor().getModifiedBy());

        // Get the processor
        GetProcessorResponseDTO procGetResponseDTO = procService.getProcessor(response.getMailBox().getGuid(), procResponseDTO.getProcessor().getGuId());

        // Assertion
        Assert.assertEquals(SUCCESS, procGetResponseDTO.getResponse().getStatus());
        Assert.assertEquals(procRequestDTO.getProcessor().getName(), procGetResponseDTO.getProcessor().getName());
        Assert.assertEquals(procRequestDTO.getProcessor().getStatus(), procGetResponseDTO.getProcessor().getStatus());
        Assert.assertEquals(procRequestDTO.getProcessor().getType(), procGetResponseDTO.getProcessor().getType());
        Assert.assertEquals(procRequestDTO.getProcessor().getProtocol(), procGetResponseDTO.getProcessor().getProtocol());

    }

    /**
     * Method constructs Processor with ProcessorService Null.
     *
     * @throws LiaisonException
     * @throws JSONException
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws JAXBException
     * @throws IOException
     * @throws SymmetricAlgorithmException
     * @throws SecurityException
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    @Test
    public void testCreateProcessorWithProcessorServiceNull()
            throws LiaisonException, JSONException, JsonParseException, JsonMappingException, JAXBException,
            IOException, SymmetricAlgorithmException, SecurityException, NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, aclManifest, mbxDTO.getModifiedBy());

        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());

        // Adding the processor
        AddProcessorToMailboxRequestDTO procRequestDTO = constructDummyProcessorDTO(response.getMailBox().getGuid(), mbxDTO);
        ProcessorConfigurationService procService = new ProcessorConfigurationService();
        procRequestDTO.getProcessor().setCreateConfiguredLocation(true);
        AddProcessorToMailboxResponseDTO procResponseDTO = procService.createProcessor(response.getMailBox().getGuid(), procRequestDTO, serviceInstanceId, procRequestDTO.getProcessor().getModifiedBy());

        // Get the processor
        GetProcessorResponseDTO procGetResponseDTO = procService.getProcessor(response.getMailBox().getGuid(), procResponseDTO.getProcessor().getGuId());

        // Assertion
        Assert.assertEquals(SUCCESS, procGetResponseDTO.getResponse().getStatus());
        Assert.assertEquals(procRequestDTO.getProcessor().getName(), procGetResponseDTO.getProcessor().getName());
        Assert.assertEquals(procRequestDTO.getProcessor().getStatus(), procGetResponseDTO.getProcessor().getStatus());
        Assert.assertEquals(procRequestDTO.getProcessor().getType(), procGetResponseDTO.getProcessor().getType());
        Assert.assertEquals(procRequestDTO.getProcessor().getProtocol(), procGetResponseDTO.getProcessor().getProtocol());

    }

    /**
     * Method get Processor with invalid data.
     *
     * @throws LiaisonException
     * @throws JSONException
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws JAXBException
     * @throws IOException
     * @throws SymmetricAlgorithmException
     * @throws SecurityException
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    @Test
    public void testReadProcessorNull()
            throws LiaisonException, JSONException, JsonParseException, JsonMappingException, JAXBException,
            IOException, SymmetricAlgorithmException, SecurityException, NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, aclManifest, mbxDTO.getModifiedBy());

        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());

        // Adding the processor
        AddProcessorToMailboxRequestDTO procRequestDTO = constructDummyProcessorDTO(response.getMailBox().getGuid(), mbxDTO);
        ProcessorConfigurationService procService = new ProcessorConfigurationService();
        procService.createProcessor(response.getMailBox().getGuid(), procRequestDTO, serviceInstanceId, procRequestDTO.getProcessor().getModifiedBy());

        // Get the processor
        GetProcessorResponseDTO procGetResponseDTO = procService.getProcessor(response.getMailBox().getGuid(), dummyValue);

        // Assertion
        Assert.assertEquals(FAILURE, procGetResponseDTO.getResponse().getStatus());
        Assert.assertTrue(procGetResponseDTO.getResponse().getMessage().contains(Messages.PROCESSOR_DOES_NOT_EXIST.value().replaceAll("%s", dummyValue)));

    }

    /**
     * Method Deactivate Processor with valid data.
     *
     * @throws LiaisonException
     * @throws JSONException
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws JAXBException
     * @throws IOException
     * @throws SymmetricAlgorithmException
     * @throws SecurityException
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    @Test
    public void testDeactivateProcessor()
            throws LiaisonException, JSONException, JsonParseException, JsonMappingException, JAXBException,
            IOException, SymmetricAlgorithmException, SecurityException, NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, aclManifest, mbxDTO.getModifiedBy());

        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());

        // Adding the processor
        AddProcessorToMailboxRequestDTO procRequestDTO = constructDummyProcessorDTO(response.getMailBox().getGuid(), mbxDTO);
        ProcessorConfigurationService procService = new ProcessorConfigurationService();
        AddProcessorToMailboxResponseDTO procResponseDTO = procService.createProcessor(response.getMailBox().getGuid(), procRequestDTO, serviceInstanceId, procRequestDTO.getProcessor().getModifiedBy());

        // Get the processor
        GetProcessorResponseDTO procGetResponseDTO = procService.getProcessor(response.getMailBox().getGuid(), procResponseDTO.getProcessor().getGuId());

        // Assertion
        Assert.assertEquals(SUCCESS, procGetResponseDTO.getResponse().getStatus());
        Assert.assertEquals(procRequestDTO.getProcessor().getName(), procGetResponseDTO.getProcessor().getName());
        Assert.assertEquals(procRequestDTO.getProcessor().getStatus(), procGetResponseDTO.getProcessor().getStatus());
        Assert.assertEquals(procRequestDTO.getProcessor().getType(), procGetResponseDTO.getProcessor().getType());
        Assert.assertEquals(procRequestDTO.getProcessor().getProtocol(), procGetResponseDTO.getProcessor().getProtocol());

        DeActivateProcessorResponseDTO procDeactResponseDTO = procService.deactivateProcessor(response.getMailBox().getGuid(), procResponseDTO.getProcessor().getGuId(), procRequestDTO.getProcessor().getModifiedBy());

        // Assertion
        Assert.assertEquals(SUCCESS, procDeactResponseDTO.getResponse().getStatus());
        Assert.assertTrue(procDeactResponseDTO.getResponse().getMessage().contains(Messages.DEACTIVATION_SUCCESSFUL.value().replaceAll("%s", MailBoxConstants.MAILBOX_PROCESSOR)));
    }

    /**
     * Method Deactivate Processor will Fail when passing invalid Processor Guid.
     *
     * @throws LiaisonException
     * @throws JSONException
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws JAXBException
     * @throws IOException
     * @throws SymmetricAlgorithmException
     * @throws SecurityException
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    @Test
    public void testDeactivateProcessorWithProcessorNull()
            throws LiaisonException, JSONException, JsonParseException, JsonMappingException, JAXBException,
            IOException, SymmetricAlgorithmException, SecurityException, NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, aclManifest, mbxDTO.getModifiedBy());

        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());

        // Adding the processor
        AddProcessorToMailboxRequestDTO procRequestDTO = constructDummyProcessorDTO(response.getMailBox().getGuid(), mbxDTO);
        ProcessorConfigurationService procService = new ProcessorConfigurationService();
        AddProcessorToMailboxResponseDTO procResponseDTO = procService.createProcessor(response.getMailBox().getGuid(), procRequestDTO, serviceInstanceId, procRequestDTO.getProcessor().getModifiedBy());

        // Get the processor
        GetProcessorResponseDTO procGetResponseDTO = procService.getProcessor(response.getMailBox().getGuid(), procResponseDTO.getProcessor().getGuId());

        // Assertion
        Assert.assertEquals(SUCCESS, procGetResponseDTO.getResponse().getStatus());
        Assert.assertEquals(procRequestDTO.getProcessor().getName(), procGetResponseDTO.getProcessor().getName());
        Assert.assertEquals(procRequestDTO.getProcessor().getStatus(), procGetResponseDTO.getProcessor().getStatus());
        Assert.assertEquals(procRequestDTO.getProcessor().getType(), procGetResponseDTO.getProcessor().getType());
        Assert.assertEquals(procRequestDTO.getProcessor().getProtocol(), procGetResponseDTO.getProcessor().getProtocol());

        DeActivateProcessorResponseDTO procDeactResponseDTO = procService.deactivateProcessor(response.getMailBox().getGuid(), dummyValue, procRequestDTO.getProcessor().getModifiedBy());

        // Assertion
        Assert.assertEquals(FAILURE, procDeactResponseDTO.getResponse().getStatus());
        Assert.assertTrue(procDeactResponseDTO.getResponse().getMessage().contains(Messages.PROCESSOR_DOES_NOT_EXIST.value().replaceAll("%s", dummyValue)));
    }

    /**
     * Method Deactivate Processor will Fail.
     *
     * @throws LiaisonException
     * @throws JSONException
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws JAXBException
     * @throws IOException
     * @throws SymmetricAlgorithmException
     * @throws SecurityException
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    @Test
    public void testDeactivateProcessorFail()
            throws LiaisonException, JSONException, JsonParseException, JsonMappingException, JAXBException,
            IOException, SymmetricAlgorithmException, SecurityException, NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, aclManifest, mbxDTO.getModifiedBy());

        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());

        // Adding the processor
        AddProcessorToMailboxRequestDTO procRequestDTO = constructDummyProcessorDTO(response.getMailBox().getGuid(), mbxDTO);
        ProcessorConfigurationService procService = new ProcessorConfigurationService();
        AddProcessorToMailboxResponseDTO procResponseDTO = procService.createProcessor(response.getMailBox().getGuid(), procRequestDTO, serviceInstanceId, procRequestDTO.getProcessor().getModifiedBy());

        // Get the processor
        GetProcessorResponseDTO procGetResponseDTO = procService.getProcessor(response.getMailBox().getGuid(), procResponseDTO.getProcessor().getGuId());

        // Assertion
        Assert.assertEquals(SUCCESS, procGetResponseDTO.getResponse().getStatus());
        Assert.assertEquals(procRequestDTO.getProcessor().getName(), procGetResponseDTO.getProcessor().getName());
        Assert.assertEquals(procRequestDTO.getProcessor().getStatus(), procGetResponseDTO.getProcessor().getStatus());
        Assert.assertEquals(procRequestDTO.getProcessor().getType(), procGetResponseDTO.getProcessor().getType());
        Assert.assertEquals(procRequestDTO.getProcessor().getProtocol(), procGetResponseDTO.getProcessor().getProtocol());

        DeActivateProcessorResponseDTO procDeactResponseDTO = procService.deactivateProcessor(dummyValue, procResponseDTO.getProcessor().getGuId(), procRequestDTO.getProcessor().getModifiedBy());

        // Assertion
        Assert.assertEquals(FAILURE, procDeactResponseDTO.getResponse().getStatus());
        Assert.assertTrue(procDeactResponseDTO.getResponse().getMessage().contains(Messages.DEACTIVATION_FAILED.value().replaceAll("%s", MailBoxConstants.MAILBOX_PROCESSOR)));
    }

    /**
     * Method Revise Processor with valid data.
     *
     * @throws LiaisonException
     * @throws JSONException
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws JAXBException
     * @throws IOException
     * @throws SymmetricAlgorithmException
     * @throws SecurityException
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    @Test
    public void testReviseProcessor()
            throws LiaisonException, JSONException, JsonParseException, JsonMappingException, JAXBException,
            IOException, SymmetricAlgorithmException, SecurityException, NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, aclManifest, mbxDTO.getModifiedBy());

        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());

        // Adding the processor
        AddProcessorToMailboxRequestDTO procRequestDTO = constructDummyProcessorDTO(response.getMailBox().getGuid(), mbxDTO);
        ProcessorConfigurationService procService = new ProcessorConfigurationService();
        AddProcessorToMailboxResponseDTO procResponseDTO = procService.createProcessor(response.getMailBox().getGuid(), procRequestDTO, serviceInstanceId, procRequestDTO.getProcessor().getModifiedBy());

        // Get the processor
        GetProcessorResponseDTO procGetResponseDTO = procService.getProcessor(response.getMailBox().getGuid(), procResponseDTO.getProcessor().getGuId());

        // Assertion
        Assert.assertEquals(SUCCESS, procGetResponseDTO.getResponse().getStatus());

        ReviseProcessorRequestDTO revProcRequestDTO = constructReviseProcessorDTO(response.getMailBox().getGuid(), mbxDTO);
        revProcRequestDTO.getProcessor().setGuid(procResponseDTO.getProcessor().getGuId());
        ReviseProcessorResponseDTO procReviseResponseDTO = procService.reviseProcessor(revProcRequestDTO, response.getMailBox().getGuid(), procResponseDTO.getProcessor().getGuId(), procRequestDTO.getProcessor().getModifiedBy());

        // Assertion
        Assert.assertEquals(SUCCESS, procReviseResponseDTO.getResponse().getStatus());
        Assert.assertTrue(procReviseResponseDTO.getResponse().getMessage().contains(Messages.REVISED_SUCCESSFULLY.value().replaceAll("%s", MailBoxConstants.MAILBOX_PROCESSOR)));
    }

    /**
     * Method Revise Processor with Invalid LinkedMailId.
     *
     * @throws LiaisonException
     * @throws JSONException
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws JAXBException
     * @throws IOException
     * @throws SymmetricAlgorithmException
     * @throws SecurityException
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    @Test
    public void testReviseProcessorWithInvalidLinkedMailId()
            throws LiaisonException, JSONException, JsonParseException, JsonMappingException, JAXBException,
            IOException, SymmetricAlgorithmException, SecurityException, NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, aclManifest, mbxDTO.getModifiedBy());

        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());

        // Adding the processor
        AddProcessorToMailboxRequestDTO procRequestDTO = constructDummyProcessorDTO(response.getMailBox().getGuid(), mbxDTO);
        ProcessorConfigurationService procService = new ProcessorConfigurationService();
        AddProcessorToMailboxResponseDTO procResponseDTO = procService.createProcessor(response.getMailBox().getGuid(), procRequestDTO, serviceInstanceId, procRequestDTO.getProcessor().getModifiedBy());

        // Get the processor
        GetProcessorResponseDTO procGetResponseDTO = procService.getProcessor(response.getMailBox().getGuid(), procResponseDTO.getProcessor().getGuId());

        // Assertion
        Assert.assertEquals(SUCCESS, procGetResponseDTO.getResponse().getStatus());

        ReviseProcessorRequestDTO revProcRequestDTO = constructReviseProcessorDTO(response.getMailBox().getGuid(), mbxDTO);
        revProcRequestDTO.getProcessor().setGuid(procResponseDTO.getProcessor().getGuId());

        //Set invalid MailBox Guid
        revProcRequestDTO.getProcessor().setLinkedMailboxId(MailBoxUtil.getGUID());
        ReviseProcessorResponseDTO procReviseResponseDTO = procService.reviseProcessor(revProcRequestDTO, response.getMailBox().getGuid(), procResponseDTO.getProcessor().getGuId(), procRequestDTO.getProcessor().getModifiedBy());

        // Assertion
        Assert.assertEquals(FAILURE, procReviseResponseDTO.getResponse().getStatus());
        Assert.assertTrue(procReviseResponseDTO.getResponse().getMessage().contains(Messages.GUID_DOES_NOT_MATCH.value().replaceAll("%s", MailBoxConstants.MAILBOX)));
    }

    /**
     * Method Revise Processor with invalid Processor guid.
     *
     * @throws LiaisonException
     * @throws JSONException
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws JAXBException
     * @throws IOException
     * @throws SymmetricAlgorithmException
     * @throws SecurityException
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    @Test
    public void testReviseProcessorWithInvalidProcessorGuid()
            throws LiaisonException, JSONException, JsonParseException, JsonMappingException, JAXBException,
            IOException, SymmetricAlgorithmException, SecurityException, NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, aclManifest, mbxDTO.getModifiedBy());

        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());

        // Adding the processor
        AddProcessorToMailboxRequestDTO procRequestDTO = constructDummyProcessorDTO(response.getMailBox().getGuid(), mbxDTO);
        ProcessorConfigurationService procService = new ProcessorConfigurationService();
        AddProcessorToMailboxResponseDTO procResponseDTO = procService.createProcessor(response.getMailBox().getGuid(), procRequestDTO, serviceInstanceId, procRequestDTO.getProcessor().getModifiedBy());

        // Get the processor
        GetProcessorResponseDTO procGetResponseDTO = procService.getProcessor(response.getMailBox().getGuid(), procResponseDTO.getProcessor().getGuId());

        // Assertion
        Assert.assertEquals(SUCCESS, procGetResponseDTO.getResponse().getStatus());

        ReviseProcessorRequestDTO revProcRequestDTO = constructReviseProcessorDTO(response.getMailBox().getGuid(), mbxDTO);
        revProcRequestDTO.getProcessor().setGuid(procResponseDTO.getProcessor().getGuId());

        //Set invalid Processor Guid
        revProcRequestDTO.getProcessor().setGuid(MailBoxUtil.getGUID());
        ReviseProcessorResponseDTO procReviseResponseDTO = procService.reviseProcessor(revProcRequestDTO, response.getMailBox().getGuid(), procResponseDTO.getProcessor().getGuId(), procRequestDTO.getProcessor().getModifiedBy());

        // Assertion
        Assert.assertEquals(FAILURE, procReviseResponseDTO.getResponse().getStatus());
        Assert.assertTrue(procReviseResponseDTO.getResponse().getMessage().contains(Messages.GUID_DOES_NOT_MATCH.value().replaceAll("%s", MailBoxConstants.MAILBOX_PROCESSOR)));
    }

    /**
     * Method Revise Processor with Processor null.
     *
     * @throws LiaisonException
     * @throws JSONException
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws JAXBException
     * @throws IOException
     * @throws SymmetricAlgorithmException
     * @throws SecurityException
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    @Test
    public void testReviseProcessorWithProcessorNull()
            throws LiaisonException, JSONException, JsonParseException, JsonMappingException, JAXBException,
            IOException, SymmetricAlgorithmException, SecurityException, NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, aclManifest, mbxDTO.getModifiedBy());

        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());

        // Adding the processor
        AddProcessorToMailboxRequestDTO procRequestDTO = constructDummyProcessorDTO(response.getMailBox().getGuid(), mbxDTO);
        ProcessorConfigurationService procService = new ProcessorConfigurationService();
        AddProcessorToMailboxResponseDTO procResponseDTO = procService.createProcessor(response.getMailBox().getGuid(), procRequestDTO, serviceInstanceId, procRequestDTO.getProcessor().getModifiedBy());

        // Get the processor
        GetProcessorResponseDTO procGetResponseDTO = procService.getProcessor(response.getMailBox().getGuid(), procResponseDTO.getProcessor().getGuId());

        // Assertion
        Assert.assertEquals(SUCCESS, procGetResponseDTO.getResponse().getStatus());

        ReviseProcessorRequestDTO revProcRequestDTO = constructReviseProcessorDTO(response.getMailBox().getGuid(), mbxDTO);
        revProcRequestDTO.getProcessor().setGuid(procResponseDTO.getProcessor().getGuId());

        //Set invalid Processor Guid
        revProcRequestDTO.setProcessor(null);
        ReviseProcessorResponseDTO procReviseResponseDTO = procService.reviseProcessor(revProcRequestDTO, response.getMailBox().getGuid(), procResponseDTO.getProcessor().getGuId(), procRequestDTO.getProcessor().getModifiedBy());

        // Assertion
        Assert.assertEquals(FAILURE, procReviseResponseDTO.getResponse().getStatus());
        Assert.assertTrue(procReviseResponseDTO.getResponse().getMessage().contains(Messages.INVALID_REQUEST.value()));
    }

    /**
     * Method Revise Processor with Processor Status invalid.
     *
     * @throws LiaisonException
     * @throws JSONException
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws JAXBException
     * @throws IOException
     * @throws SymmetricAlgorithmException
     * @throws SecurityException
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    @Test
    public void testReviseProcessorWithNullProcessorValue()
            throws LiaisonException, JSONException, JsonParseException, JsonMappingException, JAXBException,
            IOException, SymmetricAlgorithmException, SecurityException, NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, aclManifest, mbxDTO.getModifiedBy());

        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());

        // Adding the processor
        AddProcessorToMailboxRequestDTO procRequestDTO = constructDummyProcessorDTO(response.getMailBox().getGuid(), mbxDTO);
        ProcessorConfigurationService procService = new ProcessorConfigurationService();
        AddProcessorToMailboxResponseDTO procResponseDTO = procService.createProcessor(response.getMailBox().getGuid(), procRequestDTO, serviceInstanceId, procRequestDTO.getProcessor().getModifiedBy());

        // Get the processor
        GetProcessorResponseDTO procGetResponseDTO = procService.getProcessor(response.getMailBox().getGuid(), procResponseDTO.getProcessor().getGuId());

        // Assertion
        Assert.assertEquals(SUCCESS, procGetResponseDTO.getResponse().getStatus());

        ReviseProcessorRequestDTO revProcRequestDTO = constructReviseProcessorDTO(response.getMailBox().getGuid(), mbxDTO);
        revProcRequestDTO.getProcessor().setGuid(procResponseDTO.getProcessor().getGuId());

        revProcRequestDTO.getProcessor().setGuid(dummyValue);
        ReviseProcessorResponseDTO procReviseResponseDTO = procService.reviseProcessor(revProcRequestDTO, response.getMailBox().getGuid(), dummyValue, procRequestDTO.getProcessor().getModifiedBy());

        // Assertion
        Assert.assertEquals(FAILURE, procReviseResponseDTO.getResponse().getStatus());
        Assert.assertTrue(procReviseResponseDTO.getResponse().getMessage().contains(Messages.PROCESSOR_DOES_NOT_EXIST.value().replaceAll("%s", revProcRequestDTO.getProcessor().getGuid())));
    }

    /**
     * Method Revise Processor with Processor Status invalid.
     *
     * @throws LiaisonException
     * @throws JSONException
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws JAXBException
     * @throws IOException
     * @throws SymmetricAlgorithmException
     * @throws SecurityException
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    @Test
    public void testReviseProcessorWithCreateConfiguredLocation()
            throws LiaisonException, JSONException, JsonParseException, JsonMappingException, JAXBException,
            IOException, SymmetricAlgorithmException, SecurityException, NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, aclManifest, mbxDTO.getModifiedBy());

        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());

        // Adding the processor
        AddProcessorToMailboxRequestDTO procRequestDTO = constructDummyProcessorDTO(response.getMailBox().getGuid(), mbxDTO);
        ProcessorConfigurationService procService = new ProcessorConfigurationService();
        AddProcessorToMailboxResponseDTO procResponseDTO = procService.createProcessor(response.getMailBox().getGuid(), procRequestDTO, serviceInstanceId, procRequestDTO.getProcessor().getModifiedBy());

        // Get the processor
        GetProcessorResponseDTO procGetResponseDTO = procService.getProcessor(response.getMailBox().getGuid(), procResponseDTO.getProcessor().getGuId());

        // Assertion
        Assert.assertEquals(SUCCESS, procGetResponseDTO.getResponse().getStatus());

        ReviseProcessorRequestDTO revProcRequestDTO = constructReviseProcessorDTO(response.getMailBox().getGuid(), mbxDTO);
        revProcRequestDTO.getProcessor().setGuid(procResponseDTO.getProcessor().getGuId());

        revProcRequestDTO.getProcessor().setCreateConfiguredLocation(true);
        ReviseProcessorResponseDTO procReviseResponseDTO = procService.reviseProcessor(revProcRequestDTO, response.getMailBox().getGuid(), procResponseDTO.getProcessor().getGuId(), procRequestDTO.getProcessor().getModifiedBy());

        // Assertion
        Assert.assertEquals(SUCCESS, procReviseResponseDTO.getResponse().getStatus());
        Assert.assertTrue(procReviseResponseDTO.getResponse().getMessage().contains(Messages.REVISED_SUCCESSFULLY.value().replaceAll("%s", MailBoxConstants.MAILBOX_PROCESSOR)));
    }

    /**
     * Method Revise Processor with valid data.
     *
     * @throws LiaisonException
     * @throws JSONException
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws JAXBException
     * @throws IOException
     * @throws SymmetricAlgorithmException
     * @throws SecurityException
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    @Test
    public void testReviseProcessorWithLinkedProfilesWrongNames()
            throws LiaisonException, JSONException, JsonParseException, JsonMappingException, JAXBException,
            IOException, SymmetricAlgorithmException, SecurityException, NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, aclManifest, mbxDTO.getModifiedBy());

        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());

        // Adding the processor
        AddProcessorToMailboxRequestDTO procRequestDTO = constructDummyProcessorDTO(response.getMailBox().getGuid(), mbxDTO);
        ProcessorConfigurationService procService = new ProcessorConfigurationService();
        AddProcessorToMailboxResponseDTO procResponseDTO = procService.createProcessor(response.getMailBox().getGuid(), procRequestDTO, serviceInstanceId, procRequestDTO.getProcessor().getModifiedBy());

        // Get the processor
        GetProcessorResponseDTO procGetResponseDTO = procService.getProcessor(response.getMailBox().getGuid(), procResponseDTO.getProcessor().getGuId());

        // Assertion
        Assert.assertEquals(SUCCESS, procGetResponseDTO.getResponse().getStatus());

        ReviseProcessorRequestDTO revProcRequestDTO = constructReviseProcessorDTO(response.getMailBox().getGuid(), mbxDTO);
        revProcRequestDTO.getProcessor().setGuid(procResponseDTO.getProcessor().getGuId());

        // Adding a profile
        AddProfileRequestDTO requestProfileDTO = new AddProfileRequestDTO();
        ProfileDTO profileDTO = constructDummyProfileDTO(System.currentTimeMillis());
        requestProfileDTO.setProfile(profileDTO);

        ProfileConfigurationService profConfigservice = new ProfileConfigurationService();
        profConfigservice.createProfile(requestProfileDTO);
        Set<String> profiles = new HashSet<String>();
        profiles.add(requestProfileDTO.getProfile().getName() + "X");
        revProcRequestDTO.getProcessor().setLinkedProfiles(profiles);

        ReviseProcessorResponseDTO procReviseResponseDTO = procService.reviseProcessor(revProcRequestDTO,response.getMailBox().getGuid(), procResponseDTO.getProcessor().getGuId(), procRequestDTO.getProcessor().getModifiedBy());

        // Assertion
        Assert.assertEquals(FAILURE, procReviseResponseDTO.getResponse().getStatus());
        Assert.assertTrue(procReviseResponseDTO.getResponse().getMessage().contains(Messages.PROFILE_NAME_DOES_NOT_EXIST.value().replaceAll("%s",requestProfileDTO.getProfile().getName().toString() + "X")));
    }

    /**
     * Method Revise Processor with valid data.
     *
     * @throws LiaisonException
     * @throws JSONException
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws JAXBException
     * @throws IOException
     * @throws SymmetricAlgorithmException
     * @throws SecurityException
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    @Test
    public void testReviseProcessorWithLinkedProfiles()
            throws LiaisonException, JSONException, JsonParseException, JsonMappingException, JAXBException,
            IOException, SymmetricAlgorithmException, SecurityException, NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, aclManifest, mbxDTO.getModifiedBy());

        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());

        // Adding the processor
        AddProcessorToMailboxRequestDTO procRequestDTO = constructDummyProcessorDTO(response.getMailBox().getGuid(), mbxDTO);
        ProcessorConfigurationService procService = new ProcessorConfigurationService();
        AddProcessorToMailboxResponseDTO procResponseDTO = procService.createProcessor(response.getMailBox().getGuid(), procRequestDTO, serviceInstanceId, procRequestDTO.getProcessor().getModifiedBy());

        // Get the processor
        GetProcessorResponseDTO procGetResponseDTO = procService.getProcessor(response.getMailBox().getGuid(), procResponseDTO.getProcessor().getGuId());

        // Assertion
        Assert.assertEquals(SUCCESS, procGetResponseDTO.getResponse().getStatus());

        ReviseProcessorRequestDTO revProcRequestDTO = constructReviseProcessorDTO(response.getMailBox().getGuid(), mbxDTO);
        revProcRequestDTO.getProcessor().setGuid(procResponseDTO.getProcessor().getGuId());

        // Adding a profile
        AddProfileRequestDTO requestProfileDTO = new AddProfileRequestDTO();
        ProfileDTO profileDTO = constructDummyProfileDTO(System.currentTimeMillis());
        requestProfileDTO.setProfile(profileDTO);

        ProfileConfigurationService profConfigservice = new ProfileConfigurationService();
        profConfigservice.createProfile(requestProfileDTO);
        Set<String> profiles = new HashSet<String>();
        profiles.add(requestProfileDTO.getProfile().getName());
        revProcRequestDTO.getProcessor().setLinkedProfiles(profiles);

        ReviseProcessorResponseDTO procReviseResponseDTO = procService.reviseProcessor(revProcRequestDTO, response.getMailBox().getGuid(), procResponseDTO.getProcessor().getGuId(), procRequestDTO.getProcessor().getModifiedBy());

        // Assertion
        Assert.assertEquals(SUCCESS, procReviseResponseDTO.getResponse().getStatus());

    }

    /**
     * Method Interrupt the execution of running processor with execution ID with valid data.
     *
     */
    @Test
    public void testInterruptRunningProcessorWithExecId() {

        ProcessorConfigurationService procConfigurationService = new ProcessorConfigurationService();
        InterruptExecutionEventResponseDTO serviceResponse = procConfigurationService.interruptRunningProcessor("1234");

        // Assertion
        Assert.assertEquals(SUCCESS, serviceResponse.getResponse().getStatus());
        Assert.assertTrue(serviceResponse.getResponse().getMessage().contains(Messages.RECEIVED_SUCCESSFULLY.value().replaceAll("%s", MailBoxConstants.INTERRUPT_SIGNAL)));
    }

    /**
     * Method Interrupt the execution of running processor with null value
     *
     */
    @Test
    public void testInterruptRunningProcessorWithProcessorNull() {

        ProcessorConfigurationService procConfigurationService = new ProcessorConfigurationService();
        InterruptExecutionEventResponseDTO interruptExecutionEventResponseDTO = procConfigurationService.interruptRunningProcessor(null);

        // Assertion
        Assert.assertEquals(FAILURE, interruptExecutionEventResponseDTO.getResponse().getStatus());
        Assert.assertTrue(interruptExecutionEventResponseDTO.getResponse().getMessage().contains(Messages.RECEIVED_OPERATION_FAILED.value().replaceAll("%s", MailBoxConstants.INTERRUPT_SIGNAL)));
    }

    /**
     * Method Get Executing Processors With No Processor Available
     *
     */
    @Test
    public void testGetExecutingProcessorsWithNoProcessorAvail() {

        GetExecutingProcessorResponseDTO serviceResponse = null;
        ProcessorConfigurationService processor = new ProcessorConfigurationService();
        serviceResponse = new GetExecutingProcessorResponseDTO();

        // get the list processors latest state yyyy-mm-dd hh:mm:ss
        serviceResponse = processor.getExecutingProcessors("READY", "2015-09-14 12:48:32", "2015-09-15 12:48:32");

        // Assertion
        Assert.assertEquals(SUCCESS, serviceResponse.getResponse().getStatus());
        Assert.assertTrue(serviceResponse.getResponse().getMessage().contains(Messages.NO_PROCESSORS_AVAIL.value()));
    }

    /**
     * Method Get Executing Processors With Invalid Status
     *
     */
    @Test
    public void testGetExecutingProcessorsWithInvalidStatus() {

        GetExecutingProcessorResponseDTO serviceResponse = null;
        ProcessorConfigurationService processor = new ProcessorConfigurationService();
        serviceResponse = new GetExecutingProcessorResponseDTO();

        // get the list processors latest state yyyy-mm-dd hh:mm:ss
        serviceResponse = processor.getExecutingProcessors("ACTIVE", "2015-09-14 12:48:32", "2015-09-15 12:48:32");

        // Assertion
        Assert.assertEquals(FAILURE, serviceResponse.getResponse().getStatus());
        Assert.assertTrue(serviceResponse.getResponse().getMessage().contains(Messages.INVALID_PROCESSOR_STATUS.value()));
    }

    /**
     * Method Get Executing Processors With Empty Status
     *
     */
    @Test
    public void testGetExecutingProcessorsWithEmptyStatus() {

        GetExecutingProcessorResponseDTO serviceResponse = null;
        ProcessorConfigurationService processor = new ProcessorConfigurationService();
        serviceResponse = new GetExecutingProcessorResponseDTO();

        // get the list processors latest state yyyy-mm-dd hh:mm:ss
        serviceResponse = processor.getExecutingProcessors("", "2020-09-14 12:48:32", "2015-09-14 12:48:32");

        // Assertion
        Assert.assertEquals(SUCCESS, serviceResponse.getResponse().getStatus());
    }

    /**
     * Method Get Executing Processors With Empty Date
     *
     */
    @Test
    public void testGetExecutingProcessorsWithEmptyDate() {

        GetExecutingProcessorResponseDTO serviceResponse = null;
        ProcessorConfigurationService processor = new ProcessorConfigurationService();
        serviceResponse = new GetExecutingProcessorResponseDTO();

        // get the list processors latest state yyyy-mm-dd hh:mm:ss
        serviceResponse = processor.getExecutingProcessors("READY", "", "");

        // Assertion
        Assert.assertEquals(SUCCESS, serviceResponse.getResponse().getStatus());
    }

    /**
     * Method Get Executing Processors With Empty From Date
     *
     */
    @Test
    public void testGetExecutingProcessorsWithEmptyFromDate() {

        GetExecutingProcessorResponseDTO serviceResponse = null;
        ProcessorConfigurationService processor = new ProcessorConfigurationService();
        serviceResponse = new GetExecutingProcessorResponseDTO();

        // get the list processors latest state yyyy-mm-dd hh:mm:ss
        serviceResponse = processor.getExecutingProcessors("READY", "", "2016-08-04 12:48:32");

        // Assertion
        Assert.assertEquals(FAILURE, serviceResponse.getResponse().getStatus());
    }

    /**
     * Method Get Executing Processors With Empty To Date
     *
     */
    @Test
    public void testGetExecutingProcessorsWithEmptyToDate() {

        GetExecutingProcessorResponseDTO serviceResponse = null;
        ProcessorConfigurationService processor = new ProcessorConfigurationService();
        serviceResponse = new GetExecutingProcessorResponseDTO();

        // get the list processors latest state yyyy-mm-dd hh:mm:ss
        serviceResponse = processor.getExecutingProcessors("READY", "2016-08-04 12:48:32", "");

        // Assertion
        Assert.assertEquals(FAILURE, serviceResponse.getResponse().getStatus());
    }

    /**
     * Method Get Executing Processors With Empty Date And Status
     *
     */
    @Test
    public void testGetExecutingProcessorsWithEmptyFromDateToDateAndStatus() {

        GetExecutingProcessorResponseDTO serviceResponse = null;
        ProcessorConfigurationService processor = new ProcessorConfigurationService();
        serviceResponse = new GetExecutingProcessorResponseDTO();

        // get the list processors latest state yyyy-mm-dd hh:mm:ss
        serviceResponse = processor.getExecutingProcessors("", "", "");

        // Assertion
        Assert.assertEquals(SUCCESS, serviceResponse.getResponse().getStatus());
    }

    /**
     * Method Get Executing Processors With Empty From Date And Status
     *
     */
    @Test
    public void testGetExecutingProcessorsWithEmptyFromDateAndStatus() {

        GetExecutingProcessorResponseDTO serviceResponse = null;
        ProcessorConfigurationService processor = new ProcessorConfigurationService();
        serviceResponse = new GetExecutingProcessorResponseDTO();

        // get the list processors latest state yyyy-mm-dd hh:mm:ss
        serviceResponse = processor.getExecutingProcessors("", "", "2016-08-04 12:48:32");

        // Assertion
        Assert.assertEquals(FAILURE, serviceResponse.getResponse().getStatus());
    }

    /**
     * Method Get Http Listener Properties
     * @throws JsonMappingException
     * @throws JsonParseException
     * @throws IOException
     * @throws JAXBException
     * @throws MailBoxConfigurationServicesException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws SecurityException
     * @throws NoSuchFieldException
     *
     */
    @Test
    public void testGetHttpListenerPropertiesByMailboxName() throws MailBoxConfigurationServicesException, JsonParseException, JsonMappingException, JAXBException, IOException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, aclManifest, mbxDTO.getModifiedBy());

        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());

        // Adding the processor
        AddProcessorToMailboxRequestDTO procRequestDTO = constructHttpProcessorDTO(response.getMailBox().getGuid(),
                mbxDTO);
        ProcessorConfigurationService procService = new ProcessorConfigurationService();
        AddProcessorToMailboxResponseDTO procResponseDTO = procService.createProcessor(response.getMailBox().getGuid(), procRequestDTO, serviceInstanceId, procRequestDTO.getProcessor().getModifiedBy());

        // Assertion
        Assert.assertEquals(SUCCESS, procResponseDTO.getResponse().getStatus());

        ProcessorConfigurationService procsrService = new ProcessorConfigurationService();
        Map<String, String> httpListenerProperties =  procsrService.getHttpListenerProperties(mbxDTO.getName(), ProcessorType.HTTPASYNCPROCESSOR, false);

        // Assertion
        Assert.assertEquals("false", httpListenerProperties.get("lensVisibility"));
        Assert.assertEquals("false", httpListenerProperties.get("securedPayload"));
        Assert.assertEquals("false", httpListenerProperties.get("httpListenerAuthCheckRequired"));
        Assert.assertEquals("5D9C3B487184426E9F9629EFEE7C5913", httpListenerProperties.get("SERVICE_INSTANCE_ID"));
    }

    /**
     * Method Get Http Listener Properties With Empty From Date And Status
     * @throws IOException
     * @throws JAXBException
     * @throws MailBoxConfigurationServicesException
     *
     */
    @Test
    public void testGetHttpListenerPropertiesByMailboxNameWithProcessorNull() throws MailBoxConfigurationServicesException, JAXBException, IOException {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, aclManifest, mbxDTO.getModifiedBy());

        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());

        ProcessorConfigurationService procsrService = new ProcessorConfigurationService();
        try {
            procsrService.getHttpListenerProperties(mbxDTO.getName(), ProcessorType.HTTPASYNCPROCESSOR, false);
        } catch (Exception e) {
            Assert.assertEquals(Messages.MISSING_PROCESSOR.value().replaceAll("%s", ProcessorType.HTTPASYNCPROCESSOR.getCode()), e.getLocalizedMessage());
        }
    }

    /**
     * Method Get Http Listener Properties With Other than HttpProcessor
     * @throws IOException
     * @throws JAXBException
     * @throws MailBoxConfigurationServicesException
     *
     */
    @Test
    public void testGetHttpListenerPropertiesByMailboxNameWithNotAHttpProcessor() throws MailBoxConfigurationServicesException,  IOException, JAXBException {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, aclManifest, mbxDTO.getModifiedBy());

        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());

        // Adding the processor
        AddProcessorToMailboxRequestDTO procRequestDTO = constructDummyProcessorDTO(response.getMailBox().getGuid(), mbxDTO);
        ProcessorConfigurationService procService = new ProcessorConfigurationService();
        AddProcessorToMailboxResponseDTO procResponseDTO = procService.createProcessor(response.getMailBox().getGuid(), procRequestDTO, serviceInstanceId, procRequestDTO.getProcessor().getModifiedBy());

        // Assertion
        Assert.assertEquals(SUCCESS, procResponseDTO.getResponse().getStatus());

        ProcessorConfigurationService procsrService = new ProcessorConfigurationService();
        try {
            procsrService.getHttpListenerProperties(mbxDTO.getName(), ProcessorType.HTTPASYNCPROCESSOR, false);
        } catch (Exception e) {
            Assert.assertEquals(Messages.MISSING_PROCESSOR.value().replaceAll("%s", ProcessorType.HTTPASYNCPROCESSOR.getCode()), e.getLocalizedMessage());
        }
    }

    @Test
    public void testGetHttpListenerPropertiesByMailboxId() throws MailBoxConfigurationServicesException, JsonParseException, JsonMappingException, JAXBException, IOException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, aclManifest, mbxDTO.getModifiedBy());

        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());

        // Adding the processor
        AddProcessorToMailboxRequestDTO procRequestDTO = constructHttpProcessorDTO(response.getMailBox().getGuid(),
                mbxDTO);
        ProcessorConfigurationService procService = new ProcessorConfigurationService();
        AddProcessorToMailboxResponseDTO procResponseDTO = procService.createProcessor(response.getMailBox().getGuid(), procRequestDTO, serviceInstanceId, procRequestDTO.getProcessor().getModifiedBy());

        // Assertion
        Assert.assertEquals(SUCCESS, procResponseDTO.getResponse().getStatus());

        ProcessorConfigurationService procsrService = new ProcessorConfigurationService();
        Map<String, String> httpListenerProperties =  procsrService.getHttpListenerProperties(response.getMailBox().getGuid(), ProcessorType.HTTPASYNCPROCESSOR, true);

        // Assertion
        Assert.assertEquals("false", httpListenerProperties.get("lensVisibility"));
        Assert.assertEquals("false", httpListenerProperties.get("securedPayload"));
        Assert.assertEquals("false", httpListenerProperties.get("httpListenerAuthCheckRequired"));
        Assert.assertEquals("5D9C3B487184426E9F9629EFEE7C5913", httpListenerProperties.get("SERVICE_INSTANCE_ID"));
    }

    /**
     * Method Get Http Listener Properties With Empty From Date And Status
     * @throws IOException
     * @throws JAXBException
     * @throws MailBoxConfigurationServicesException
     *
     */
    @Test
    public void testGetHttpListenerPropertiesByMailboxIdWithProcessorNull() throws MailBoxConfigurationServicesException, JAXBException, IOException {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, aclManifest, mbxDTO.getModifiedBy());

        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());

        ProcessorConfigurationService procsrService = new ProcessorConfigurationService();
        try {
            procsrService.getHttpListenerProperties(response.getMailBox().getGuid(), ProcessorType.HTTPASYNCPROCESSOR, true);
        } catch (Exception e) {
            Assert.assertEquals(Messages.MISSING_PROCESSOR.value().replaceAll("%s", ProcessorType.HTTPASYNCPROCESSOR.getCode()), e.getLocalizedMessage());
        }
    }

    /**
     * Method Get Http Listener Properties With Other than HttpProcessor
     * @throws IOException
     * @throws JAXBException
     * @throws MailBoxConfigurationServicesException
     *
     */
    @Test
    public void testGetHttpListenerPropertiesByMailboxIdWithNotAHttpProcessor() throws MailBoxConfigurationServicesException,  IOException, JAXBException {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, aclManifest, mbxDTO.getModifiedBy());

        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());

        // Adding the processor
        AddProcessorToMailboxRequestDTO procRequestDTO = constructDummyProcessorDTO(response.getMailBox().getGuid(), mbxDTO);
        ProcessorConfigurationService procService = new ProcessorConfigurationService();
        AddProcessorToMailboxResponseDTO procResponseDTO = procService.createProcessor(response.getMailBox().getGuid(), procRequestDTO, serviceInstanceId, procRequestDTO.getProcessor().getModifiedBy());

        // Assertion
        Assert.assertEquals(SUCCESS, procResponseDTO.getResponse().getStatus());

        ProcessorConfigurationService procsrService = new ProcessorConfigurationService();
        try {
            procsrService.getHttpListenerProperties(response.getMailBox().getGuid(), ProcessorType.HTTPASYNCPROCESSOR, true);
        } catch (Exception e) {
            Assert.assertEquals(Messages.MISSING_PROCESSOR.value().replaceAll("%s", ProcessorType.HTTPASYNCPROCESSOR.getCode()), e.getLocalizedMessage());
        }
    }

    /**
     * Method Search MailBox by name
     * @throws IOException 
     * @throws JAXBException 
     * @throws JsonMappingException 
     * @throws JsonParseException 
     * @throws MailBoxConfigurationServicesException 
     *
     */
    @Test
    public void testGetMailBoxNames() {
    	
        ProcessorConfigurationService processor = new ProcessorConfigurationService();
        GenericSearchFilterDTO searchFilter = new GenericSearchFilterDTO();
        searchFilter.setMbxName("MBX_TEST");
        SearchProcessorResponseDTO serviceResponse = processor.getMailBoxNames(searchFilter);

        // Assertion
        Assert.assertEquals(SUCCESS, serviceResponse.getResponse().getStatus());
        Assert.assertTrue(serviceResponse.getResponse().getMessage().contains(Messages.READ_SUCCESSFUL.value().replaceAll("%s", MailBoxConstants.MAILBOX)));
    }

    /**
     * Method Search MailBox by unavailable name
     *
     */
    @Test
    public void testgetMailBoxNamesWithUnavailableName()  {

        ProcessorConfigurationService processor = new ProcessorConfigurationService();
        GenericSearchFilterDTO searchFilter = new GenericSearchFilterDTO();
        searchFilter.setMbxName("MBX_TEST1442825541687test");
        SearchProcessorResponseDTO serviceResponse = processor.getMailBoxNames(searchFilter);

        // Assertion
        Assert.assertEquals(FAILURE, serviceResponse.getResponse().getStatus());
        Assert.assertTrue(serviceResponse.getResponse().getMessage().contains(Messages.READ_OPERATION_FAILED.value().replaceAll("%s", MailBoxConstants.MAILBOX)));
    }

    /**
     * Method Search Profile by name
     *
     */
    @Test
    public void testgetProfileNames() {

    	//Adding a profile
		AddProfileRequestDTO requestDTO = new AddProfileRequestDTO();
		ProfileDTO profileDTO = constructDummyProfileDTO(System.currentTimeMillis());
		requestDTO.setProfile(profileDTO);

		ProfileConfigurationService service = new ProfileConfigurationService();
		AddProfileResponseDTO response = service.createProfile(requestDTO);

		Assert.assertEquals(SUCCESS, response.getResponse().getStatus());

        ProcessorConfigurationService processor = new ProcessorConfigurationService();
        GenericSearchFilterDTO searchFilter = new GenericSearchFilterDTO();
        searchFilter.setProfileName(profileDTO.getName());
        SearchProcessorResponseDTO serviceResponse = processor.getProfileNames(searchFilter);

        // Assertion
        Assert.assertEquals(SUCCESS, serviceResponse.getResponse().getStatus());
        Assert.assertTrue(serviceResponse.getResponse().getMessage().contains(Messages.READ_SUCCESSFUL.value().replaceAll("%s", MailBoxConstants.PROFILE)));
    }

    /**
     * Method Search Profile by unavailable name
     *
     */
    @Test
    public void testgetProfileNamesWithUnavailableName() {

        ProcessorConfigurationService processor = new ProcessorConfigurationService();
        GenericSearchFilterDTO searchFilter = new GenericSearchFilterDTO();
        searchFilter.setProfileName("MBX_TEST");
        SearchProcessorResponseDTO serviceResponse = processor.getProfileNames(searchFilter);

        // Assertion
        Assert.assertEquals(SUCCESS, serviceResponse.getResponse().getStatus());
        Assert.assertTrue(serviceResponse.getResponse().getMessage().contains(Messages.NO_COMPONENT_EXISTS.value().replaceAll("%s", MailBoxConstants.PROFILE)));
    }

    /**
     * Method Search Processor by name
     * @throws JAXBException
     * @throws IOException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws SecurityException
     * @throws NoSuchFieldException
     *
     */
    @Test
    public void testSearchProcessor() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, IOException, JAXBException {

        ProcessorConfigurationService processor = new ProcessorConfigurationService();
        GenericSearchFilterDTO searchFilter = new GenericSearchFilterDTO();
        searchFilter.setMbxName("MBX_TEST");
        GetProcessorResponseDTO serviceResponse = processor.searchProcessor(searchFilter);

        // Assertion
        Assert.assertEquals(SUCCESS, serviceResponse.getResponse().getStatus());
        Assert.assertTrue(serviceResponse.getResponse().getMessage().contains(Messages.READ_SUCCESSFUL.value().replaceAll("%s", MailBoxConstants.MAILBOX_PROCESSOR)));
    }

    /**
     * Method Search Processor by unavailable name
     * @throws JAXBException
     * @throws IOException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws SecurityException
     * @throws NoSuchFieldException
     *
     */
    @Test
    public void testSearchProcessorWithUnavailableMbxName() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, IOException, JAXBException {

        ProcessorConfigurationService processor = new ProcessorConfigurationService();
        GenericSearchFilterDTO searchFilter = new GenericSearchFilterDTO();
        searchFilter.setProfileName("MBX_TEST");
        GetProcessorResponseDTO serviceResponse = processor.searchProcessor(searchFilter);

        // Assertion
        Assert.assertEquals(FAILURE, serviceResponse.getResponse().getStatus());
        Assert.assertTrue(serviceResponse.getResponse().getMessage().contains(Messages.READ_OPERATION_FAILED.value().replaceAll("%s", MailBoxConstants.MAILBOX_PROCESSOR)));
    }
    
    /**
     * Method to test read processor by valid Pguid
     * @throws MailBoxConfigurationServicesException
     *
     * @throws LiaisonException
     * @throws JSONException
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws JAXBException
     * @throws IOException
     * @throws SymmetricAlgorithmException
     * @throws SecurityException
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    @Test
	public void testReadProcessorByPguid()
			throws MailBoxConfigurationServicesException, JsonParseException, JsonMappingException, JAXBException,
			IOException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, aclManifest, mbxDTO.getModifiedBy());

        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());

        // Adding the processor
        AddProcessorToMailboxRequestDTO procRequestDTO = constructDummyProcessorDTO(response.getMailBox().getGuid(), mbxDTO);
        ProcessorConfigurationService procService = new ProcessorConfigurationService();
        AddProcessorToMailboxResponseDTO procResponseDTO = procService.createProcessor(response.getMailBox().getGuid(), procRequestDTO, serviceInstanceId, procRequestDTO.getProcessor().getModifiedBy());
        
        Assert.assertEquals(SUCCESS, procResponseDTO.getResponse().getStatus());

        // Get the processor by guid
        GetProcessorResponseDTO procGetResponseDTO = procService.getProcessor(procResponseDTO.getProcessor().getGuId());

        // Assertion
        Assert.assertEquals(SUCCESS, procGetResponseDTO.getResponse().getStatus());
        Assert.assertEquals(procRequestDTO.getProcessor().getName(), procGetResponseDTO.getProcessor().getName());
        Assert.assertEquals(procRequestDTO.getProcessor().getStatus(), procGetResponseDTO.getProcessor().getStatus());
        Assert.assertEquals(procRequestDTO.getProcessor().getType(), procGetResponseDTO.getProcessor().getType());
        Assert.assertEquals(procRequestDTO.getProcessor().getProtocol(), procGetResponseDTO.getProcessor().getProtocol());

    }
    
    /**
     * Method to test read processor by valid Pguid
     * @throws MailBoxConfigurationServicesException
     *
     * @throws LiaisonException
     * @throws JSONException
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws JAXBException
     * @throws IOException
     * @throws SymmetricAlgorithmException
     * @throws SecurityException
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    @Test
	public void testReadProcessorByName()
			throws MailBoxConfigurationServicesException, JsonParseException, JsonMappingException, JAXBException,
			IOException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, aclManifest, mbxDTO.getModifiedBy());

        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());

        // Adding the processor
        AddProcessorToMailboxRequestDTO procRequestDTO = constructDummyProcessorDTO(response.getMailBox().getGuid(), mbxDTO);
        ProcessorConfigurationService procService = new ProcessorConfigurationService();
        AddProcessorToMailboxResponseDTO procResponseDTO = procService.createProcessor(response.getMailBox().getGuid(), procRequestDTO, serviceInstanceId, procRequestDTO.getProcessor().getModifiedBy());
        
        Assert.assertEquals(SUCCESS, procResponseDTO.getResponse().getStatus());

        // Get the processor by Name
        GetProcessorResponseDTO procGetResponseDTO = procService.getProcessor(procRequestDTO.getProcessor().getName());

        // Assertion
        Assert.assertEquals(SUCCESS, procGetResponseDTO.getResponse().getStatus());
        List<ProcessorDTO> retrievedProcessors = procGetResponseDTO.getProcessors();
        for (ProcessorDTO proc : retrievedProcessors) {
        	Assert.assertEquals(procRequestDTO.getProcessor().getName(), proc.getName());
        }

    }
    
    /**
     * Method to test read processor by invalid Pguid/Name
     * @throws MailBoxConfigurationServicesException
     *
     * @throws LiaisonException
     * @throws JSONException
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws JAXBException
     * @throws IOException
     * @throws SymmetricAlgorithmException
     * @throws SecurityException
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    @Test
	public void testReadProcessorByInvalidPguidOrName()
			throws MailBoxConfigurationServicesException, JsonParseException, JsonMappingException, JAXBException,
			IOException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {

        ProcessorConfigurationService procService = new ProcessorConfigurationService();
        // Get the processor by Name
        GetProcessorResponseDTO procGetResponseDTO = procService.getProcessor("invalid");
        // Assertion
        Assert.assertEquals(FAILURE, procGetResponseDTO.getResponse().getStatus());
    }

    /**
     * Method to test read processor by invalid Pguid/Name
     * @throws MailBoxConfigurationServicesException
     *
     * @throws LiaisonException
     * @throws JSONException
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws JAXBException
     * @throws IOException
     * @throws SymmetricAlgorithmException
     * @throws SecurityException
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    @Test
	public void testReadProcessorByPguidOrNameAsNull()
			throws MailBoxConfigurationServicesException, JsonParseException, JsonMappingException, JAXBException,
			IOException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {

        ProcessorConfigurationService procService = new ProcessorConfigurationService();
        // Get the processor by Name
        GetProcessorResponseDTO procGetResponseDTO = procService.getProcessor(null);
        // Assertion
        Assert.assertEquals(FAILURE, procGetResponseDTO.getResponse().getStatus());
    }
}
