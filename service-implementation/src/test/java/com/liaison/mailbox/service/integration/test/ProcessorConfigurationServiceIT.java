/**
 * Copyright 2016 Liaison Technologies, Inc.
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.mailbox.service.integration.test;

import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.enums.EntityStatus;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.enums.ProcessorType;
import com.liaison.mailbox.service.base.test.BaseServiceTest;
import com.liaison.mailbox.service.core.MailBoxConfigurationService;
import com.liaison.mailbox.service.core.MailBoxService;
import com.liaison.mailbox.service.core.ProcessorAffinityService;
import com.liaison.mailbox.service.core.ProcessorConfigurationService;
import com.liaison.mailbox.service.core.ProfileConfigurationService;
import com.liaison.mailbox.service.dto.GenericSearchFilterDTO;
import com.liaison.mailbox.service.dto.configuration.MailBoxDTO;
import com.liaison.mailbox.service.dto.configuration.ProcessorDTO;
import com.liaison.mailbox.service.dto.configuration.ProfileDTO;
import com.liaison.mailbox.service.dto.configuration.request.AddMailboxRequestDTO;
import com.liaison.mailbox.service.dto.configuration.request.AddProcessorToMailboxRequestDTO;
import com.liaison.mailbox.service.dto.configuration.request.AddProfileRequestDTO;
import com.liaison.mailbox.service.dto.configuration.request.ReviseMailBoxRequestDTO;
import com.liaison.mailbox.service.dto.configuration.request.ReviseProcessorRequestDTO;
import com.liaison.mailbox.service.dto.configuration.response.AddMailBoxResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.AddProcessorToMailboxResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.AddProfileResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.ClusterTypeResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.DeActivateProcessorResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.GetDatacenterResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.GetProcessorIdResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.GetProcessorResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.ReviseProcessorResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.SearchProcessorResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.TriggerProfileResponseDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.util.MailBoxUtil;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ProcessorConfigurationServiceIT extends BaseServiceTest {

    private final String dummyValue = "dummy";

    /**
     * Method constructs Processor with valid data.
     */
    @Test
    public void testCreateAndReadProcessorUsingPguid() throws Exception {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, mbxDTO.getModifiedBy());

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
        Assert.assertEquals(procRequestDTO.getProcessor().getClusterType(), procGetResponseDTO.getProcessor().getClusterType());

    }

    /**
     * Method constructs Processor with valid data.
     */
    @Test
    public void testCreateAndReadProcessorUsingInvalidPguid() throws Exception {

        // Get the processor
        GetProcessorResponseDTO procGetResponseDTO = new ProcessorConfigurationService().getProcessor("Invalid", false);

        // Assertion
        Assert.assertEquals(FAILURE, procGetResponseDTO.getResponse().getStatus());

    }

    /**
     * Method constructs Processor with empty service instance Id.
     */
    @Test
    public void testCreateProcessorWithEmptyServiceInstanceId() throws Exception {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();

        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, mbxDTO.getModifiedBy());
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
     */
    @Test
    public void testCreateProcessorwithWrongLinkedMailId() throws Exception {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, mbxDTO.getModifiedBy());

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
     */
    @Test
    public void testCreateProcessorWithProcessorNull() throws Exception {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, mbxDTO.getModifiedBy());

        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());

        // Adding the processor
        AddProcessorToMailboxRequestDTO procRequestDTO = constructDummyProcessorDTO(response.getMailBox().getGuid(), mbxDTO);
        ProcessorConfigurationService procService = new ProcessorConfigurationService();
        procRequestDTO.setProcessor(null);
        AddProcessorToMailboxResponseDTO procResponseDTO = procService.createProcessor(response.getMailBox().getGuid(), procRequestDTO, serviceInstanceId, "unknown");


        // Assertion
        Assert.assertEquals(FAILURE, procResponseDTO.getResponse().getStatus());
        Assert.assertTrue(procResponseDTO.getResponse().getMessage().contains(Messages.INVALID_REQUEST.value()));

    }

    /**
     * Method constructs Processor with wrong processor name.
     */
    @Test
    public void testCreateProcessorWithProcessorWithWrongProcessorName() throws Exception {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, mbxDTO.getModifiedBy());

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
     */
    @Test
    public void testCreateProcessorWithServiceInstanceIdNull() throws Exception {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, mbxDTO.getModifiedBy());

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
        Assert.assertEquals(procRequestDTO.getProcessor().getClusterType(), procGetResponseDTO.getProcessor().getClusterType());

    }

    /**
     * Method constructs Processor with ProcessorService Null.
     */
    @Test
    public void testCreateProcessorWithProcessorServiceNull() throws Exception {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, mbxDTO.getModifiedBy());

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
        Assert.assertEquals(procRequestDTO.getProcessor().getClusterType(), procGetResponseDTO.getProcessor().getClusterType());

    }

    /**
     * Method constructs REMOTEDOWNLOADER Processor with Invalid Processor protocol.
     */
    @Test
    public void testCreateRemoteUploaderWithInvalidProtocol() throws Exception {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, mbxDTO.getModifiedBy());

        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());

        // Adding the REMOTEDOWNLOADER processor with invalid protocol
        AddProcessorToMailboxRequestDTO procRequestDTO = constructDummyProcessorDTO(response.getMailBox().getGuid(), mbxDTO);
        ProcessorConfigurationService procService = new ProcessorConfigurationService();
        procRequestDTO.getProcessor().setProtocol(MailBoxConstants.SWEEPER);
        AddProcessorToMailboxResponseDTO procResponseDTO = procService.createProcessor(response.getMailBox().getGuid(), procRequestDTO, serviceInstanceId, procRequestDTO.getProcessor().getModifiedBy());

        // Assertion
        Assert.assertEquals(FAILURE, procResponseDTO.getResponse().getStatus());        
    }
    
    /**
     * Method constructs REMOTEUPLOADER Processor with Invalid Processor protocol.
     */
    @Test
    public void testCreateRemoteDownloaderWithInvalidProtocol() throws Exception {
        
        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, mbxDTO.getModifiedBy());

        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());
        
        // Adding the REMOTEUPLOADER processor with invalid protocol
        AddProcessorToMailboxRequestDTO procRequestDTO = constructDummyProcessorDTO(response.getMailBox().getGuid(), mbxDTO);
        ProcessorConfigurationService procService = new ProcessorConfigurationService();
        procRequestDTO.getProcessor().setType(MailBoxConstants.REMOTEUPLOADER);
        procRequestDTO.getProcessor().setProtocol(MailBoxConstants.SWEEPER);
        AddProcessorToMailboxResponseDTO procResponseDTO = procService.createProcessor(response.getMailBox().getGuid(), procRequestDTO, serviceInstanceId, procRequestDTO.getProcessor().getModifiedBy());

        // Assertion
        Assert.assertEquals(FAILURE, procResponseDTO.getResponse().getStatus());
    }
    
    /**
     * Method constructs HTTPSYNCPROCESSOR Processor with Invalid Processor protocol.
     */
    @Test
    public void testCreateHttpSyncWithInvalidProtocol() throws Exception {
        
        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, mbxDTO.getModifiedBy());

        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());
        
        // Adding the HTTPSYNCPROCESSOR processor with invalid protocol
        AddProcessorToMailboxRequestDTO procRequestDTO = constructDummyProcessorDTO(response.getMailBox().getGuid(), mbxDTO);
        ProcessorConfigurationService procService = new ProcessorConfigurationService();
        procRequestDTO.getProcessor().setType(MailBoxConstants.HTTPSYNCPROCESSOR);
        procRequestDTO.getProcessor().setProtocol(MailBoxConstants.SWEEPER);
        AddProcessorToMailboxResponseDTO procResponseDTO = procService.createProcessor(response.getMailBox().getGuid(), procRequestDTO, serviceInstanceId, procRequestDTO.getProcessor().getModifiedBy());

        // Assertion
        Assert.assertEquals(FAILURE, procResponseDTO.getResponse().getStatus());        
    }
        
    /**
     * Method constructs HTTPASYNCPROCESSOR Processor with Invalid Processor protocol.
     */
    @Test
    public void testCreateHttpAsyncWithInvalidProtocol() throws Exception {
        
        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, mbxDTO.getModifiedBy());

        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());
            
        // Adding the HTTPASYNCPROCESSOR processor with invalid protocol
        AddProcessorToMailboxRequestDTO procRequestDTO = constructDummyProcessorDTO(response.getMailBox().getGuid(), mbxDTO);
        ProcessorConfigurationService procService = new ProcessorConfigurationService();
        procRequestDTO.getProcessor().setType(MailBoxConstants.HTTPASYNCPROCESSOR);
        procRequestDTO.getProcessor().setProtocol(MailBoxConstants.SWEEPER);
        AddProcessorToMailboxResponseDTO procResponseDTO = procService.createProcessor(response.getMailBox().getGuid(), procRequestDTO, serviceInstanceId, procRequestDTO.getProcessor().getModifiedBy());

        // Assertion
        Assert.assertEquals(FAILURE, procResponseDTO.getResponse().getStatus());        
    }
    
    /**
     * Method constructs SWEEPER Processor with Invalid Processor protocol.
     */
    @Test
    public void testCreateSweeperWithInvalidProtocol() throws Exception {
        
        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, mbxDTO.getModifiedBy());

        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());
        
        // Adding the SWEEPER processor with invalid protocol
        AddProcessorToMailboxRequestDTO procRequestDTO = constructDummyProcessorDTO(response.getMailBox().getGuid(), mbxDTO);
        ProcessorConfigurationService procService = new ProcessorConfigurationService();
        procRequestDTO.getProcessor().setType(MailBoxConstants.SWEEPER);
        procRequestDTO.getProcessor().setProtocol(MailBoxConstants.FILEWRITER);
        AddProcessorToMailboxResponseDTO procResponseDTO = procService.createProcessor(response.getMailBox().getGuid(), procRequestDTO, serviceInstanceId, procRequestDTO.getProcessor().getModifiedBy());

        // Assertion
        Assert.assertEquals(FAILURE, procResponseDTO.getResponse().getStatus());        
    }
    
    /**
     * Method constructs CONDITIONALSWEEPER Processor with Invalid Processor protocol.
     */
    @Test
    public void testCreateConditionalSweeperWithInvalidProtocol() throws Exception {
        
        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, mbxDTO.getModifiedBy());

        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());
        
        // Adding the CONDITIONALSWEEPER processor with invalid protocol
        AddProcessorToMailboxRequestDTO procRequestDTO = constructDummyProcessorDTO(response.getMailBox().getGuid(), mbxDTO);
        ProcessorConfigurationService procService = new ProcessorConfigurationService();
        procRequestDTO.getProcessor().setType(MailBoxConstants.CONDITIONALSWEEPER);
        procRequestDTO.getProcessor().setProtocol(MailBoxConstants.SWEEPER);
        AddProcessorToMailboxResponseDTO procResponseDTO = procService.createProcessor(response.getMailBox().getGuid(), procRequestDTO, serviceInstanceId, procRequestDTO.getProcessor().getModifiedBy());

        // Assertion
        Assert.assertEquals(FAILURE, procResponseDTO.getResponse().getStatus());        
    }
    
    /**
     * Method constructs FILEWRITER Processor with Invalid Processor protocol.
     */
    @Test
    public void testCreateFilewriterWithInvalidProtocol() throws Exception {
        
        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, mbxDTO.getModifiedBy());

        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());
        
        // Adding the FILEWRITER processor with invalid protocol
        AddProcessorToMailboxRequestDTO procRequestDTO = constructDummyProcessorDTO(response.getMailBox().getGuid(), mbxDTO);
        ProcessorConfigurationService procService = new ProcessorConfigurationService();
        procRequestDTO.getProcessor().setType(MailBoxConstants.FILEWRITER);
        procRequestDTO.getProcessor().setProtocol(MailBoxConstants.SWEEPER);
        AddProcessorToMailboxResponseDTO procResponseDTO = procService.createProcessor(response.getMailBox().getGuid(), procRequestDTO, serviceInstanceId, procRequestDTO.getProcessor().getModifiedBy());

        // Assertion
        Assert.assertEquals(FAILURE, procResponseDTO.getResponse().getStatus());
    }
    
    /**
     * Method get Processor with invalid data.
     */
    @Test
    public void testReadProcessorNull() throws Exception {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, mbxDTO.getModifiedBy());

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
     */
    @Test
    public void testDeactivateProcessor() throws Exception {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, mbxDTO.getModifiedBy());

        Assert.assertEquals(response.getResponse().getStatus(), SUCCESS);

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
        Assert.assertEquals(procRequestDTO.getProcessor().getClusterType(), procGetResponseDTO.getProcessor().getClusterType());

        DeActivateProcessorResponseDTO procDeactResponseDTO = procService.deactivateProcessor(response.getMailBox().getGuid(), procResponseDTO.getProcessor().getGuId(), procRequestDTO.getProcessor().getModifiedBy());

        // Assertion
        Assert.assertEquals(SUCCESS, procDeactResponseDTO.getResponse().getStatus());
        Assert.assertTrue(procDeactResponseDTO.getResponse().getMessage().contains(Messages.DELETED_SUCCESSFULLY.value().replaceAll("%s", MailBoxConstants.MAILBOX_PROCESSOR)));
    }

    /**
     * Method Deactivate Processor will Fail when passing invalid Processor Guid.
     */
    @Test
    public void testDeactivateProcessorWithProcessorNull() throws Exception {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, mbxDTO.getModifiedBy());

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
        Assert.assertEquals(procRequestDTO.getProcessor().getClusterType(), procGetResponseDTO.getProcessor().getClusterType());

        DeActivateProcessorResponseDTO procDeactResponseDTO = procService.deactivateProcessor(response.getMailBox().getGuid(), dummyValue, procRequestDTO.getProcessor().getModifiedBy());

        // Assertion
        Assert.assertEquals(FAILURE, procDeactResponseDTO.getResponse().getStatus());
        Assert.assertTrue(procDeactResponseDTO.getResponse().getMessage().contains(Messages.PROCESSOR_DOES_NOT_EXIST.value().replaceAll("%s", dummyValue)));
    }

    /**
     * Method Deactivate Processor will Fail.
     */
    @Test(enabled = false)
    public void testDeactivateProcessorFail() throws Exception {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, mbxDTO.getModifiedBy());

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
        Assert.assertEquals(procRequestDTO.getProcessor().getClusterType(), procGetResponseDTO.getProcessor().getClusterType());

        DeActivateProcessorResponseDTO procDeactResponseDTO = procService.deactivateProcessor(dummyValue, procResponseDTO.getProcessor().getGuId(), procRequestDTO.getProcessor().getModifiedBy());

        // Assertion
        Assert.assertEquals(FAILURE, procDeactResponseDTO.getResponse().getStatus());
        Assert.assertTrue(procDeactResponseDTO.getResponse().getMessage().contains(Messages.DEACTIVATION_FAILED.value().replaceAll("%s", MailBoxConstants.MAILBOX_PROCESSOR)));
    }

    /**
     * Method Revise Processor with valid data.
     */
    @Test
    public void testReviseProcessor() throws Exception {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, mbxDTO.getModifiedBy());

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
     */
    @Test
    public void testReviseProcessorWithInvalidLinkedMailId() throws Exception {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, mbxDTO.getModifiedBy());

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
     */
    @Test
    public void testReviseProcessorWithInvalidProcessorGuid() throws Exception {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, mbxDTO.getModifiedBy());

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
     */
    @Test
    public void testReviseProcessorWithProcessorNull() throws Exception {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, mbxDTO.getModifiedBy());

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
     */
    @Test
    public void testReviseProcessorWithNullProcessorValue() throws Exception {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, mbxDTO.getModifiedBy());

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
     */
    @Test
    public void testReviseProcessorWithCreateConfiguredLocation() throws Exception {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, mbxDTO.getModifiedBy());

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
     */
    @Test
    public void testReviseProcessorWithLinkedProfilesWrongNames() throws Exception {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, mbxDTO.getModifiedBy());

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

        ReviseProcessorResponseDTO procReviseResponseDTO = procService.reviseProcessor(revProcRequestDTO, response.getMailBox().getGuid(), procResponseDTO.getProcessor().getGuId(), procRequestDTO.getProcessor().getModifiedBy());

        // Assertion
        Assert.assertEquals(FAILURE, procReviseResponseDTO.getResponse().getStatus());
        Assert.assertTrue(procReviseResponseDTO.getResponse().getMessage().contains(Messages.PROFILE_NAME_DOES_NOT_EXIST.value().replaceAll("%s", requestProfileDTO.getProfile().getName() + "X")));
    }

    /**
     * Method Revise Processor with valid data.
     */
    @Test
    public void testReviseProcessorWithLinkedProfiles() throws Exception {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, mbxDTO.getModifiedBy());

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
     * Method to Revise Processor Type
     */
    @Test
    public void testReviseProcessorType() throws Exception {
        
        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, mbxDTO.getModifiedBy());

        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());

        // Adding the processor
        AddProcessorToMailboxRequestDTO procRequestDTO = constructDummyProcessorDTO(response.getMailBox().getGuid(), mbxDTO);
        ProcessorConfigurationService procService = new ProcessorConfigurationService();
        AddProcessorToMailboxResponseDTO procResponseDTO = procService.createProcessor(response.getMailBox().getGuid(), procRequestDTO, serviceInstanceId, procRequestDTO.getProcessor().getModifiedBy());

        ReviseProcessorRequestDTO revProcRequestDTO = constructReviseProcessorDTO(response.getMailBox().getGuid(), mbxDTO);
        revProcRequestDTO.getProcessor().setType(ProcessorType.HTTPASYNCPROCESSOR.toString());
        revProcRequestDTO.getProcessor().setGuid(procResponseDTO.getProcessor().getGuId());
        ReviseProcessorResponseDTO procReviseResponseDTO = procService.reviseProcessor(revProcRequestDTO, response.getMailBox().getGuid(), procResponseDTO.getProcessor().getGuId(), procRequestDTO.getProcessor().getModifiedBy());

        // Assertion
        Assert.assertEquals(FAILURE, procReviseResponseDTO.getResponse().getStatus()); 
    }
    
    /**
     * Method to Revise Processor Protocol
     */
    @Test
    public void testReviseProcessorProtocol() throws Exception {
        
        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, mbxDTO.getModifiedBy());

        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());

        // Adding the processor
        AddProcessorToMailboxRequestDTO procRequestDTO = constructDummyProcessorDTO(response.getMailBox().getGuid(), mbxDTO);
        ProcessorConfigurationService procService = new ProcessorConfigurationService();
        AddProcessorToMailboxResponseDTO procResponseDTO = procService.createProcessor(response.getMailBox().getGuid(), procRequestDTO, serviceInstanceId, procRequestDTO.getProcessor().getModifiedBy());

        ReviseProcessorRequestDTO revProcRequestDTO = constructReviseProcessorDTO(response.getMailBox().getGuid(), mbxDTO);
        revProcRequestDTO.getProcessor().setProtocol("FTPS");
        revProcRequestDTO.getProcessor().setGuid(procResponseDTO.getProcessor().getGuId());
        ReviseProcessorResponseDTO procReviseResponseDTO = procService.reviseProcessor(revProcRequestDTO, response.getMailBox().getGuid(), procResponseDTO.getProcessor().getGuId(), procRequestDTO.getProcessor().getModifiedBy());

        // Assertion
        Assert.assertEquals(FAILURE, procReviseResponseDTO.getResponse().getStatus()); 
    }
    /**
     * Method Get Http Listener Properties
     *
     * @throws JsonMappingException
     * @throws JsonParseException
     * @throws IOException
     * @throws JAXBException
     * @throws MailBoxConfigurationServicesException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws SecurityException
     * @throws NoSuchFieldException
     */
    @Test
    public void testGetHttpListenerPropertiesByMailboxName() throws Exception {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, mbxDTO.getModifiedBy());

        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());

        // Adding the processor
        AddProcessorToMailboxRequestDTO procRequestDTO = constructHttpProcessorDTO(response.getMailBox().getGuid(),
                mbxDTO);
        ProcessorConfigurationService procService = new ProcessorConfigurationService();
        AddProcessorToMailboxResponseDTO procResponseDTO = procService.createProcessor(response.getMailBox().getGuid(), procRequestDTO, serviceInstanceId, procRequestDTO.getProcessor().getModifiedBy());

        // Assertion
        Assert.assertEquals(SUCCESS, procResponseDTO.getResponse().getStatus());

        ProcessorConfigurationService procsrService = new ProcessorConfigurationService();
        Map<String, String> httpListenerProperties = procsrService.getHttpListenerProperties(mbxDTO.getName(), ProcessorType.HTTPASYNCPROCESSOR, false);

        // Assertion
        Assert.assertEquals("false", httpListenerProperties.get("lensVisibility"));
        Assert.assertEquals("false", httpListenerProperties.get("securedPayload"));
        Assert.assertEquals("false", httpListenerProperties.get("httpListenerAuthCheckRequired"));
        Assert.assertEquals(serviceInstanceId, httpListenerProperties.get("SERVICE_INSTANCE_ID"));
    }

    /**
     * Method Get Http Listener Properties With Empty From Date And Status
     *
     * @throws IOException
     * @throws JAXBException
     * @throws MailBoxConfigurationServicesException
     */
    @Test
    public void testGetHttpListenerPropertiesByMailboxNameWithProcessorNull() throws MailBoxConfigurationServicesException, JAXBException, IOException {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, mbxDTO.getModifiedBy());

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
     *
     * @throws IOException
     * @throws JAXBException
     * @throws MailBoxConfigurationServicesException
     */
    @Test
    public void testGetHttpListenerPropertiesByMailboxNameWithNotAHttpProcessor() throws MailBoxConfigurationServicesException, IOException, JAXBException {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, mbxDTO.getModifiedBy());

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
    public void testGetHttpListenerPropertiesByMailboxId() throws Exception {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, mbxDTO.getModifiedBy());

        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());

        // Adding the processor
        AddProcessorToMailboxRequestDTO procRequestDTO = constructHttpProcessorDTO(response.getMailBox().getGuid(),
                mbxDTO);
        ProcessorConfigurationService procService = new ProcessorConfigurationService();
        AddProcessorToMailboxResponseDTO procResponseDTO = procService.createProcessor(response.getMailBox().getGuid(), procRequestDTO, serviceInstanceId, procRequestDTO.getProcessor().getModifiedBy());

        // Assertion
        Assert.assertEquals(SUCCESS, procResponseDTO.getResponse().getStatus());

        ProcessorConfigurationService procsrService = new ProcessorConfigurationService();
        Map<String, String> httpListenerProperties = procsrService.getHttpListenerProperties(response.getMailBox().getGuid(), ProcessorType.HTTPASYNCPROCESSOR, true);

        // Assertion
        Assert.assertEquals("false", httpListenerProperties.get("lensVisibility"));
        Assert.assertEquals("false", httpListenerProperties.get("securedPayload"));
        Assert.assertEquals("false", httpListenerProperties.get("httpListenerAuthCheckRequired"));
        Assert.assertEquals(serviceInstanceId, httpListenerProperties.get("SERVICE_INSTANCE_ID"));
    }
    
    /**
     * Test method to get http listener properties for inactive processor and check the processor status.
     * 
     * @throws Exception
     */
    @Test
    public void testGetHttpListenerPropertiesByMailboxIdForInactiveProcessor() throws Exception {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, mbxDTO.getModifiedBy());

        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());

        // Adding the processor
        AddProcessorToMailboxRequestDTO procRequestDTO = constructHttpProcessorDTO(response.getMailBox().getGuid(),
                mbxDTO);
        ProcessorConfigurationService procService = new ProcessorConfigurationService();
        AddProcessorToMailboxResponseDTO procResponseDTO = procService.createProcessor(response.getMailBox().getGuid(), procRequestDTO, serviceInstanceId, procRequestDTO.getProcessor().getModifiedBy());

        // Assertion
        Assert.assertEquals(SUCCESS, procResponseDTO.getResponse().getStatus());

        ReviseProcessorRequestDTO revProcRequestDTO = constructReviseHTTPAsyncProcessorDTO(response.getMailBox().getGuid(), mbxDTO);
        revProcRequestDTO.getProcessor().setGuid(procResponseDTO.getProcessor().getGuId());
        revProcRequestDTO.getProcessor().setStatus(EntityStatus.INACTIVE.value());
        procService.reviseProcessor(revProcRequestDTO, response.getMailBox().getGuid(), procResponseDTO.getProcessor().getGuId(), procRequestDTO.getProcessor().getModifiedBy());

        ProcessorConfigurationService procsrService = new ProcessorConfigurationService();
        Map<String, String> httpListenerProperties = procsrService.getHttpListenerProperties(response.getMailBox().getGuid(), ProcessorType.HTTPASYNCPROCESSOR, true);

        // Assertion
        Assert.assertEquals(revProcRequestDTO.getProcessor().getStatus(), httpListenerProperties.get(MailBoxConstants.PROCSR_STATUS));
    }
    
    /**
     * Test method to get http listener properties for inactive mailbox and check the processor status.
     * 
     * @throws Exception
     */
    @Test
    public void testGetHttpListenerPropertiesByMailboxIdForInactiveMailBox() throws Exception {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, mbxDTO.getModifiedBy());

        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());

        // Adding the processor
        AddProcessorToMailboxRequestDTO procRequestDTO = constructHttpProcessorDTO(response.getMailBox().getGuid(),
                mbxDTO);
        ProcessorConfigurationService procService = new ProcessorConfigurationService();
        AddProcessorToMailboxResponseDTO procResponseDTO = procService.createProcessor(response.getMailBox().getGuid(), procRequestDTO, serviceInstanceId, procRequestDTO.getProcessor().getModifiedBy());

        // Assertion
        Assert.assertEquals(SUCCESS, procResponseDTO.getResponse().getStatus());

        ReviseMailBoxRequestDTO reviseRequestDTO = new ReviseMailBoxRequestDTO();
        mbxDTO.setName(mbxDTO.getName());
        mbxDTO.setDescription("RevisedMailBox Desc");
        mbxDTO.setStatus("INACTIVE");
        mbxDTO.setShardKey("RevisedShard");
        mbxDTO.setGuid(response.getMailBox().getGuid());
        reviseRequestDTO.setMailBox(mbxDTO);

        service.reviseMailBox(reviseRequestDTO, mbxDTO.getGuid(), "dummy" + System.currentTimeMillis(), true, mbxDTO.getModifiedBy());

        ProcessorConfigurationService procsrService = new ProcessorConfigurationService();
        Map<String, String> httpListenerProperties = procsrService.getHttpListenerProperties(response.getMailBox().getGuid(), ProcessorType.HTTPASYNCPROCESSOR, true);

        // Assertion
        Assert.assertEquals(reviseRequestDTO.getMailBox().getStatus(), httpListenerProperties.get(MailBoxConstants.MAILBOX_STATUS));
    }

    /**
     * Method Get Http Listener Properties With Empty From Date And Status
     *
     * @throws IOException
     * @throws JAXBException
     * @throws MailBoxConfigurationServicesException
     */
    @Test
    public void testGetHttpListenerPropertiesByMailboxIdWithProcessorNull() throws Exception {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, mbxDTO.getModifiedBy());

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
     *
     * @throws IOException
     * @throws JAXBException
     * @throws MailBoxConfigurationServicesException
     */
    @Test
    public void testGetHttpListenerPropertiesByMailboxIdWithNotAHttpProcessor() throws Exception {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, mbxDTO.getModifiedBy());

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
     *
     * @throws IOException
     * @throws JAXBException
     * @throws JsonMappingException
     * @throws JsonParseException
     * @throws MailBoxConfigurationServicesException
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
     */
    @Test
    public void testGetMailBoxNamesWithUnavailableName() {

        ProcessorConfigurationService processor = new ProcessorConfigurationService();
        GenericSearchFilterDTO searchFilter = new GenericSearchFilterDTO();
        searchFilter.setMbxName("MBX_TEST1442825541687test");
        SearchProcessorResponseDTO serviceResponse = processor.getMailBoxNames(searchFilter);

        // Assertion
        Assert.assertEquals(SUCCESS, serviceResponse.getResponse().getStatus());
        Assert.assertTrue(serviceResponse.getResponse().getMessage().contains(Messages.NO_COMPONENT_EXISTS.value().replaceAll("%s", MailBoxConstants.MAILBOX)));
    }

    /**
     * Method Search Profile by name
     */
    @Test
    public void testGetProfileNames() {

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
     */
    @Test
    public void testGetProfileNamesWithUnavailableName() {

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
     */
    @Test
    public void testSearchProcessor() throws Exception {

        ProcessorConfigurationService processor = new ProcessorConfigurationService();
        GenericSearchFilterDTO searchFilter = new GenericSearchFilterDTO();
        searchFilter.setMbxName("MBX_TEST");
        searchFilter.setMatchMode(GenericSearchFilterDTO.MATCH_MODE_EQUALS_CHR);
        GetProcessorResponseDTO serviceResponse = processor.searchProcessor(searchFilter);

        // Assertion
        Assert.assertEquals(SUCCESS, serviceResponse.getResponse().getStatus());
        Assert.assertTrue(serviceResponse.getResponse().getMessage().contains(Messages.READ_SUCCESSFUL.value().replaceAll("%s", MailBoxConstants.MAILBOX_PROCESSOR)));
    }

    /**
     * Method Search Processor by unavailable name
     */
    @Test
    public void testSearchProcessorWithUnavailableMbxName() throws Exception {

        ProcessorConfigurationService processor = new ProcessorConfigurationService();
        GenericSearchFilterDTO searchFilter = new GenericSearchFilterDTO();
        searchFilter.setProfileName("MBX_TEST");
        searchFilter.setMatchMode(GenericSearchFilterDTO.MATCH_MODE_EQUALS_CHR);
        GetProcessorResponseDTO serviceResponse = processor.searchProcessor(searchFilter);

        // Assertion
        Assert.assertEquals(SUCCESS, serviceResponse.getResponse().getStatus());
        Assert.assertTrue(serviceResponse.getResponse().getMessage().contains(Messages.NO_COMPONENT_EXISTS.value().replaceAll("%s", MailBoxConstants.MAILBOX_PROCESSOR)));
    }

    /**
     * Method to test read processor by valid Pguid
     */
    @Test
    public void testReadProcessorByPguid() throws Exception {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, mbxDTO.getModifiedBy());

        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());

        // Adding the processor
        AddProcessorToMailboxRequestDTO procRequestDTO = constructDummyProcessorDTO(response.getMailBox().getGuid(), mbxDTO);
        ProcessorConfigurationService procService = new ProcessorConfigurationService();
        AddProcessorToMailboxResponseDTO procResponseDTO = procService.createProcessor(response.getMailBox().getGuid(), procRequestDTO, serviceInstanceId, procRequestDTO.getProcessor().getModifiedBy());

        Assert.assertEquals(SUCCESS, procResponseDTO.getResponse().getStatus());

        // Get the processor by guid
        GetProcessorResponseDTO procGetResponseDTO = procService.getProcessor(procResponseDTO.getProcessor().getGuId(), false);

        // Assertion
        Assert.assertEquals(SUCCESS, procGetResponseDTO.getResponse().getStatus());
        Assert.assertEquals(procRequestDTO.getProcessor().getName(), procGetResponseDTO.getProcessor().getName());
        Assert.assertEquals(procRequestDTO.getProcessor().getStatus(), procGetResponseDTO.getProcessor().getStatus());
        Assert.assertEquals(procRequestDTO.getProcessor().getType(), procGetResponseDTO.getProcessor().getType());
        Assert.assertEquals(procRequestDTO.getProcessor().getProtocol(), procGetResponseDTO.getProcessor().getProtocol());
        Assert.assertEquals(procRequestDTO.getProcessor().getClusterType(), procGetResponseDTO.getProcessor().getClusterType());

    }
    
    /**
     * Method to test read processor by processor guid and mailbox guid as null/empty
     * @throws Exception
     */
    @Test
    public void testReadProcessorByPguidAndMailBoxGuidNull() throws Exception {
        
        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);
        
        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, mbxDTO.getModifiedBy());
        
        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());
        
        // Adding the processor
        AddProcessorToMailboxRequestDTO procRequestDTO = constructDummyProcessorDTO(response.getMailBox().getGuid(), mbxDTO);
        ProcessorConfigurationService procService = new ProcessorConfigurationService();
        AddProcessorToMailboxResponseDTO procResponseDTO = procService.createProcessor(response.getMailBox().getGuid(), procRequestDTO, serviceInstanceId, procRequestDTO.getProcessor().getModifiedBy());
        
        Assert.assertEquals(SUCCESS, procResponseDTO.getResponse().getStatus());
        
        // Get the processor by guid and mailbox guid as null
        GetProcessorResponseDTO procGetResponseDTO = procService.getProcessor(null, procResponseDTO.getProcessor().getGuId());
        
        // Assertion
        Assert.assertEquals(SUCCESS, procGetResponseDTO.getResponse().getStatus());
        Assert.assertEquals(procRequestDTO.getProcessor().getName(), procGetResponseDTO.getProcessor().getName());
        Assert.assertEquals(procRequestDTO.getProcessor().getStatus(), procGetResponseDTO.getProcessor().getStatus());
        Assert.assertEquals(procRequestDTO.getProcessor().getType(), procGetResponseDTO.getProcessor().getType());
        Assert.assertEquals(procRequestDTO.getProcessor().getProtocol(), procGetResponseDTO.getProcessor().getProtocol());
        Assert.assertEquals(procRequestDTO.getProcessor().getClusterType(), procGetResponseDTO.getProcessor().getClusterType());
        
    }

    /**
     * Method to test read processor by valid Pguid
     */
    @Test
    public void testReadProcessorByName() throws Exception {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, mbxDTO.getModifiedBy());

        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());

        // Adding the processor
        AddProcessorToMailboxRequestDTO procRequestDTO = constructDummyProcessorDTO(response.getMailBox().getGuid(), mbxDTO);
        ProcessorConfigurationService procService = new ProcessorConfigurationService();
        AddProcessorToMailboxResponseDTO procResponseDTO = procService.createProcessor(response.getMailBox().getGuid(), procRequestDTO, serviceInstanceId, procRequestDTO.getProcessor().getModifiedBy());

        Assert.assertEquals(SUCCESS, procResponseDTO.getResponse().getStatus());

        // Get the processor by Name
        GetProcessorResponseDTO procGetResponseDTO = procService.getProcessor(procRequestDTO.getProcessor().getName(), false);

        // Assertion
        Assert.assertEquals(SUCCESS, procGetResponseDTO.getResponse().getStatus());
        List<ProcessorDTO> retrievedProcessors = procGetResponseDTO.getProcessors();
        for (ProcessorDTO proc : retrievedProcessors) {
            Assert.assertEquals(procRequestDTO.getProcessor().getName(), proc.getName());
        }

    }

    /**
     * Method to test read processor by invalid Pguid/Name
     */
    @Test
    public void testReadProcessorByInvalidPguidOrName() throws Exception {

        ProcessorConfigurationService procService = new ProcessorConfigurationService();
        // Get the processor by Name
        GetProcessorResponseDTO procGetResponseDTO = procService.getProcessor("invalid", false);
        // Assertion
        Assert.assertEquals(FAILURE, procGetResponseDTO.getResponse().getStatus());
    }

    /**
     * Method to test read processor by invalid Pguid/Name
     */
    @Test
    public void testReadProcessorByPguidOrNameAsNull() throws Exception {

        ProcessorConfigurationService procService = new ProcessorConfigurationService();
        // Get the processor by Name
        GetProcessorResponseDTO procGetResponseDTO = procService.getProcessor(null, false);
        // Assertion
        Assert.assertEquals(FAILURE, procGetResponseDTO.getResponse().getStatus());
    }
    
    /**
     * Test method to delete the processor
     * 
     * @throws JAXBException
     * @throws IOException
     * @throws IllegalAccessException
     * @throws NoSuchFieldException
     */
    @Test
    public void testDeleteProcessor() throws JAXBException, IOException, IllegalAccessException, NoSuchFieldException {
        
        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, mbxDTO.getModifiedBy());

        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getResponse());
        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());

        // Adding the processor
        AddProcessorToMailboxRequestDTO procRequestDTO = constructDummyProcessorDTO(response.getMailBox().getGuid(), mbxDTO);
        ProcessorConfigurationService procService = new ProcessorConfigurationService();
        AddProcessorToMailboxResponseDTO procResponseDTO = procService.createProcessor(response.getMailBox().getGuid(), procRequestDTO, serviceInstanceId, procRequestDTO.getProcessor().getModifiedBy());

        // Get the processor
        Assert.assertNotNull(procResponseDTO);
        Assert.assertNotNull(procResponseDTO.getResponse());
        Assert.assertEquals(SUCCESS, procResponseDTO.getResponse().getStatus());
        
        GetProcessorResponseDTO procGetResponseDTO = procService.getProcessor(response.getMailBox().getGuid(), procResponseDTO.getProcessor().getGuId());

        // Assertion
        Assert.assertNotNull(procGetResponseDTO);
        Assert.assertNotNull(procGetResponseDTO.getResponse());
        Assert.assertEquals(procResponseDTO.getProcessor().getGuId(), procGetResponseDTO.getProcessor().getGuid());
        Assert.assertEquals(SUCCESS, procGetResponseDTO.getResponse().getStatus());

        ReviseProcessorRequestDTO revProcRequestDTO = constructReviseProcessorDTO(response.getMailBox().getGuid(), mbxDTO);
        revProcRequestDTO.getProcessor().setGuid(procResponseDTO.getProcessor().getGuId());
        revProcRequestDTO.getProcessor().setStatus(EntityStatus.DELETED.value());
        ReviseProcessorResponseDTO procReviseResponseDTO = procService.reviseProcessor(revProcRequestDTO, response.getMailBox().getGuid(), procResponseDTO.getProcessor().getGuId(), procRequestDTO.getProcessor().getModifiedBy());
        Assert.assertEquals(FAILURE, procReviseResponseDTO.getResponse().getStatus());

        DeActivateProcessorResponseDTO procDeactResponseDTO = procService.deactivateProcessor(response.getMailBox().getGuid(), procResponseDTO.getProcessor().getGuId(), procRequestDTO.getProcessor().getModifiedBy());
        Assert.assertEquals(SUCCESS, procDeactResponseDTO.getResponse().getStatus());
        Assert.assertTrue(procDeactResponseDTO.getResponse().getMessage().contains(Messages.DELETED_SUCCESSFULLY.value().replaceAll("%s", MailBoxConstants.MAILBOX_PROCESSOR)));
    }
    
    /**
     * Test method to delete and read the deleted processor
     * 
     * @throws JAXBException
     * @throws IOException
     * @throws IllegalAccessException
     * @throws NoSuchFieldException
     */
    @Test
    public void testReadDeletedProcessor() throws JAXBException, IOException, IllegalAccessException, NoSuchFieldException {
        
        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, mbxDTO.getModifiedBy());

        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getResponse());
        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());

        // Adding the processor
        AddProcessorToMailboxRequestDTO procRequestDTO = constructDummyProcessorDTO(response.getMailBox().getGuid(), mbxDTO);
        ProcessorConfigurationService procService = new ProcessorConfigurationService();
        AddProcessorToMailboxResponseDTO procResponseDTO = procService.createProcessor(response.getMailBox().getGuid(), procRequestDTO, serviceInstanceId, procRequestDTO.getProcessor().getModifiedBy());

        // Get the processor
        Assert.assertNotNull(procResponseDTO);
        Assert.assertNotNull(procResponseDTO.getResponse());
        Assert.assertEquals(SUCCESS, procResponseDTO.getResponse().getStatus());
        
        GetProcessorResponseDTO procGetResponseDTO = procService.getProcessor(response.getMailBox().getGuid(), procResponseDTO.getProcessor().getGuId());

        // Assertion
        Assert.assertNotNull(procGetResponseDTO);
        Assert.assertNotNull(procGetResponseDTO.getResponse());
        Assert.assertEquals(procResponseDTO.getProcessor().getGuId(), procGetResponseDTO.getProcessor().getGuid());
        Assert.assertEquals(SUCCESS, procGetResponseDTO.getResponse().getStatus());

        ReviseProcessorRequestDTO revProcRequestDTO = constructReviseProcessorDTO(response.getMailBox().getGuid(), mbxDTO);
        revProcRequestDTO.getProcessor().setGuid(procResponseDTO.getProcessor().getGuId());
        revProcRequestDTO.getProcessor().setStatus(EntityStatus.DELETED.value());
        ReviseProcessorResponseDTO procReviseResponseDTO = procService.reviseProcessor(revProcRequestDTO, response.getMailBox().getGuid(), procResponseDTO.getProcessor().getGuId(), procRequestDTO.getProcessor().getModifiedBy());
        Assert.assertEquals(FAILURE, procReviseResponseDTO.getResponse().getStatus());

        DeActivateProcessorResponseDTO procDeactResponseDTO = procService.deactivateProcessor(response.getMailBox().getGuid(), procResponseDTO.getProcessor().getGuId(), procRequestDTO.getProcessor().getModifiedBy());
        Assert.assertEquals(SUCCESS, procDeactResponseDTO.getResponse().getStatus());
        Assert.assertTrue(procDeactResponseDTO.getResponse().getMessage().contains(Messages.DELETED_SUCCESSFULLY.value().replaceAll("%s", MailBoxConstants.MAILBOX_PROCESSOR)));

        //Getting the deleted processor
        GetProcessorResponseDTO procDeletedGetResponseDTO = procService.getProcessor(response.getMailBox().getGuid(), procResponseDTO.getProcessor().getGuId());
        
        Assert.assertEquals(FAILURE, procDeletedGetResponseDTO.getResponse().getStatus());
        Assert.assertTrue(procDeletedGetResponseDTO.getResponse().getMessage().contains(Messages.PROCESSOR_DOES_NOT_EXIST.value().replaceAll("%s", procResponseDTO.getProcessor().getGuId())));
    }
    
    /**
     * Test method to delete and list the deleted processor
     * 
     * @throws JAXBException
     * @throws IOException
     * @throws IllegalAccessException
     * @throws NoSuchFieldException
     */
    @Test
    public void testListDeletedProcessor() throws JAXBException, IOException, IllegalAccessException, NoSuchFieldException {
        
        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, mbxDTO.getModifiedBy());

        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getResponse());
        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());

        // Adding the processor
        AddProcessorToMailboxRequestDTO procRequestDTO = constructDummyProcessorDTO(response.getMailBox().getGuid(), mbxDTO);
        ProcessorConfigurationService procService = new ProcessorConfigurationService();
        AddProcessorToMailboxResponseDTO procResponseDTO = procService.createProcessor(response.getMailBox().getGuid(), procRequestDTO, serviceInstanceId, procRequestDTO.getProcessor().getModifiedBy());

        // Get the processor
        Assert.assertNotNull(procResponseDTO);
        Assert.assertNotNull(procResponseDTO.getResponse());
        Assert.assertEquals(SUCCESS, procResponseDTO.getResponse().getStatus());
        
        GetProcessorResponseDTO procGetResponseDTO = procService.getProcessor(response.getMailBox().getGuid(), procResponseDTO.getProcessor().getGuId());

        // Assertion
        Assert.assertNotNull(procGetResponseDTO);
        Assert.assertNotNull(procGetResponseDTO.getResponse());
        Assert.assertEquals(procResponseDTO.getProcessor().getGuId(), procGetResponseDTO.getProcessor().getGuid());
        Assert.assertEquals(SUCCESS, procGetResponseDTO.getResponse().getStatus());

        ReviseProcessorRequestDTO revProcRequestDTO = constructReviseProcessorDTO(response.getMailBox().getGuid(), mbxDTO);
        revProcRequestDTO.getProcessor().setGuid(procResponseDTO.getProcessor().getGuId());
        revProcRequestDTO.getProcessor().setStatus(EntityStatus.DELETED.value());
        ReviseProcessorResponseDTO procReviseResponseDTO = procService.reviseProcessor(revProcRequestDTO, response.getMailBox().getGuid(), procResponseDTO.getProcessor().getGuId(), procRequestDTO.getProcessor().getModifiedBy());
        Assert.assertEquals(FAILURE, procReviseResponseDTO.getResponse().getStatus());

        DeActivateProcessorResponseDTO procDeactResponseDTO = procService.deactivateProcessor(response.getMailBox().getGuid(), procResponseDTO.getProcessor().getGuId(), procRequestDTO.getProcessor().getModifiedBy());
        Assert.assertEquals(SUCCESS, procDeactResponseDTO.getResponse().getStatus());
        Assert.assertTrue(procDeactResponseDTO.getResponse().getMessage().contains(Messages.DELETED_SUCCESSFULLY.value().replaceAll("%s", MailBoxConstants.MAILBOX_PROCESSOR)));
        
        ProcessorConfigurationService processor = new ProcessorConfigurationService();
        GenericSearchFilterDTO searchFilter = new GenericSearchFilterDTO();
        searchFilter.setMbxName(requestDTO.getMailBox().getName());
        searchFilter.setMatchMode(GenericSearchFilterDTO.MATCH_MODE_EQUALS_STR);
        GetProcessorResponseDTO serviceResponse = processor.searchProcessor(searchFilter);

        // Assertion
        Assert.assertEquals(SUCCESS, serviceResponse.getResponse().getStatus());
        Assert.assertTrue(serviceResponse.getTotalItems() == 0);
    }
    
    /**
     * Test method to trigger the profile which added in the deleted processor
     * 
     * @throws JAXBException
     * @throws IOException
     * @throws IllegalAccessException
     * @throws NoSuchFieldException
     */
    @Test
    public void testTriggerProfileForDeletedProcessor() throws JAXBException, IOException, IllegalAccessException, NoSuchFieldException {
        
        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, mbxDTO.getModifiedBy());

        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getResponse());
        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());

        // Adding the processor
        AddProcessorToMailboxRequestDTO procRequestDTO = constructDummyProcessorDTO(response.getMailBox().getGuid(), mbxDTO);
        ProcessorConfigurationService procService = new ProcessorConfigurationService();
        
        // Adding a profile
        AddProfileRequestDTO requestProfileDTO = new AddProfileRequestDTO();
        ProfileDTO profileDTO = constructDummyProfileDTO(System.currentTimeMillis());
        requestProfileDTO.setProfile(profileDTO);

        ProfileConfigurationService profConfigservice = new ProfileConfigurationService();
        profConfigservice.createProfile(requestProfileDTO);
        Set<String> profiles = new HashSet<String>();
        profiles.add(requestProfileDTO.getProfile().getName());
        procRequestDTO.getProcessor().setLinkedProfiles(profiles);
        
        AddProcessorToMailboxResponseDTO procResponseDTO = procService.createProcessor(response.getMailBox().getGuid(), procRequestDTO, serviceInstanceId, procRequestDTO.getProcessor().getModifiedBy());

        // Get the processor
        Assert.assertNotNull(procResponseDTO);
        Assert.assertNotNull(procResponseDTO.getResponse());
        Assert.assertEquals(SUCCESS, procResponseDTO.getResponse().getStatus());
        
        GetProcessorResponseDTO procGetResponseDTO = procService.getProcessor(response.getMailBox().getGuid(), procResponseDTO.getProcessor().getGuId());

        // Assertion
        Assert.assertNotNull(procGetResponseDTO);
        Assert.assertNotNull(procGetResponseDTO.getResponse());
        Assert.assertEquals(procResponseDTO.getProcessor().getGuId(), procGetResponseDTO.getProcessor().getGuid());
        Assert.assertEquals(SUCCESS, procGetResponseDTO.getResponse().getStatus());

        ReviseProcessorRequestDTO revProcRequestDTO = constructReviseProcessorDTO(response.getMailBox().getGuid(), mbxDTO);
        revProcRequestDTO.getProcessor().setGuid(procResponseDTO.getProcessor().getGuId());
        revProcRequestDTO.getProcessor().setStatus(EntityStatus.DELETED.value());
        ReviseProcessorResponseDTO procReviseResponseDTO = procService.reviseProcessor(revProcRequestDTO, response.getMailBox().getGuid(), procResponseDTO.getProcessor().getGuId(), procRequestDTO.getProcessor().getModifiedBy());
        Assert.assertEquals(FAILURE, procReviseResponseDTO.getResponse().getStatus());

        DeActivateProcessorResponseDTO procDeactResponseDTO = procService.deactivateProcessor(response.getMailBox().getGuid(), procResponseDTO.getProcessor().getGuId(), procRequestDTO.getProcessor().getModifiedBy());
        Assert.assertEquals(SUCCESS, procDeactResponseDTO.getResponse().getStatus());
        Assert.assertTrue(procDeactResponseDTO.getResponse().getMessage().contains(Messages.DELETED_SUCCESSFULLY.value().replaceAll("%s", MailBoxConstants.MAILBOX_PROCESSOR)));

        // Triggering the profile attached only to the deleted processor
        MailBoxService triggerProfileService = new MailBoxService();
        TriggerProfileResponseDTO triggerResponseDTO = triggerProfileService.triggerProfile(profileDTO.getName(), "", "");
        
        // Assertion
        Assert.assertEquals(FAILURE, triggerResponseDTO.getResponse().getStatus());
        Assert.assertTrue(triggerResponseDTO.getResponse().getMessage().contains(Messages.NO_PROC_CONFIG_PROFILE.value()));
    }

    /**
     * Method to search processor by mailbox guid
     */
    @Test
    public void testSearcProcessorByMbxPguid() throws Exception {
        
        // create the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);
        
        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, mbxDTO.getModifiedBy());
        
        // Adding mailbox to processor
        List<ProcessorDTO> processors = new ArrayList<>();
        ProcessorConfigurationService procService = new ProcessorConfigurationService();
        AddProcessorToMailboxRequestDTO procRequestDTO = constructDummyProcessorDTO(response.getMailBox().getGuid(), mbxDTO);
        AddProcessorToMailboxResponseDTO procResponseDTO = procService.createProcessor(response.getMailBox().getGuid(), procRequestDTO, serviceInstanceId, procRequestDTO.getProcessor().getModifiedBy());
        // Get the processor
        GetProcessorResponseDTO procGetResponseDTO = procService.getProcessor(response.getMailBox().getGuid(), procResponseDTO.getProcessor().getGuId());
        processors.add(procGetResponseDTO.getProcessor());
        
        procRequestDTO = constructDummyProcessorDTO(response.getMailBox().getGuid(), mbxDTO);
        procResponseDTO = procService.createProcessor(response.getMailBox().getGuid(), procRequestDTO, serviceInstanceId, procRequestDTO.getProcessor().getModifiedBy());
        // Get the processor
        procGetResponseDTO = procService.getProcessor(response.getMailBox().getGuid(), procResponseDTO.getProcessor().getGuId());
        processors.add(procGetResponseDTO.getProcessor());
        
        procRequestDTO = constructDummyProcessorDTO(response.getMailBox().getGuid(), mbxDTO);
        procResponseDTO = procService.createProcessor(response.getMailBox().getGuid(), procRequestDTO, serviceInstanceId, procRequestDTO.getProcessor().getModifiedBy());
        // Get the processor
        procGetResponseDTO = procService.getProcessor(response.getMailBox().getGuid(), procResponseDTO.getProcessor().getGuId());
        processors.add(procGetResponseDTO.getProcessor());
        
        //search processor by filter
        ProcessorConfigurationService processor = new ProcessorConfigurationService();
        GenericSearchFilterDTO searchFilter = new GenericSearchFilterDTO();
        searchFilter.setMbxGuid(response.getMailBox().getGuid());
        searchFilter.setMatchMode(GenericSearchFilterDTO.MATCH_MODE_EQUALS_CHR);
        GetProcessorResponseDTO serviceResponse = processor.searchProcessor(searchFilter);
        
        Assert.assertEquals(serviceResponse.getProcessors().size(), processors.size());
        Assert.assertEquals(SUCCESS, serviceResponse.getResponse().getStatus());
        Assert.assertTrue(serviceResponse.getResponse().getMessage().contains(Messages.READ_SUCCESSFUL.value().replaceAll("%s", MailBoxConstants.MAILBOX_PROCESSOR)));
    }

     /**
     * Method to search processor by unavailable mailbox guid
     */
    @Test
    public void testSearchProcessorWithUnavailableMbxPguid() throws Exception {
        
        // create the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);
        
        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, mbxDTO.getModifiedBy());
        
        // Adding mailbox to processor
        List<ProcessorDTO> processors = new ArrayList<>();
        ProcessorConfigurationService procService = new ProcessorConfigurationService();
        AddProcessorToMailboxRequestDTO procRequestDTO = constructDummyProcessorDTO(response.getMailBox().getGuid(), mbxDTO);
        AddProcessorToMailboxResponseDTO procResponseDTO = procService.createProcessor(response.getMailBox().getGuid(), procRequestDTO, serviceInstanceId, procRequestDTO.getProcessor().getModifiedBy());
        // Get the processor
        GetProcessorResponseDTO procGetResponseDTO = procService.getProcessor(response.getMailBox().getGuid(), procResponseDTO.getProcessor().getGuId());
        processors.add(procGetResponseDTO.getProcessor());
        
        procRequestDTO = constructDummyProcessorDTO(response.getMailBox().getGuid(), mbxDTO);
        procResponseDTO = procService.createProcessor(response.getMailBox().getGuid(), procRequestDTO, serviceInstanceId, procRequestDTO.getProcessor().getModifiedBy());
        // Get the processor
        procGetResponseDTO = procService.getProcessor(response.getMailBox().getGuid(), procResponseDTO.getProcessor().getGuId());
        processors.add(procGetResponseDTO.getProcessor());
        
        procRequestDTO = constructDummyProcessorDTO(response.getMailBox().getGuid(), mbxDTO);
        procResponseDTO = procService.createProcessor(response.getMailBox().getGuid(), procRequestDTO, serviceInstanceId, procRequestDTO.getProcessor().getModifiedBy());
        // Get the processor
        procGetResponseDTO = procService.getProcessor(response.getMailBox().getGuid(), procResponseDTO.getProcessor().getGuId());
        processors.add(procGetResponseDTO.getProcessor());
        
        //search processor by filter
        ProcessorConfigurationService processor = new ProcessorConfigurationService();
        GenericSearchFilterDTO searchFilter = new GenericSearchFilterDTO();
        searchFilter.setMbxGuid(response.getMailBox().getGuid() + "1");
        searchFilter.setMatchMode(GenericSearchFilterDTO.MATCH_MODE_EQUALS_CHR);
        GetProcessorResponseDTO serviceResponse = processor.searchProcessor(searchFilter);
        
        Assert.assertEquals(0, serviceResponse.getProcessors().size());
        Assert.assertEquals(SUCCESS, serviceResponse.getResponse().getStatus());
        Assert.assertTrue(serviceResponse.getResponse().getMessage().contains(Messages.NO_COMPONENT_EXISTS.value().replaceAll("%s", MailBoxConstants.MAILBOX_PROCESSOR)));
    }
    
    /**
     * Method to search processor by script name
     */
    @Test
    public void testSearcProcessorByScriptName() throws Exception {
        
        // create the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);
        
        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, mbxDTO.getModifiedBy());
        
        // Adding mailbox to processor
        ProcessorConfigurationService procService = new ProcessorConfigurationService();
        AddProcessorToMailboxRequestDTO procRequestDTO = constructDummyProcessorDTO(response.getMailBox().getGuid(), mbxDTO);
        AddProcessorToMailboxResponseDTO procResponseDTO = procService.createProcessor(response.getMailBox().getGuid(), procRequestDTO, serviceInstanceId, procRequestDTO.getProcessor().getModifiedBy());
        // Get the processor
        GetProcessorResponseDTO procGetResponseDTO = procService.getProcessor(response.getMailBox().getGuid(), procResponseDTO.getProcessor().getGuId());

        //search processor by filter
        ProcessorConfigurationService processor = new ProcessorConfigurationService();
        GenericSearchFilterDTO searchFilter = new GenericSearchFilterDTO();
        searchFilter.setScriptName(procGetResponseDTO.getProcessor().getJavaScriptURI());
        searchFilter.setMatchMode(GenericSearchFilterDTO.MATCH_MODE_EQUALS_CHR);
        GetProcessorResponseDTO serviceResponse = processor.searchProcessor(searchFilter);

        Assert.assertEquals(SUCCESS, serviceResponse.getResponse().getStatus());
        Assert.assertTrue(serviceResponse.getResponse().getMessage().contains(Messages.READ_SUCCESSFUL.value().replaceAll("%s", MailBoxConstants.MAILBOX_PROCESSOR)));
    }
    
    /**
     * Method to search processor by unavailable script name
     */
    @Test
    public void testSearchProcessorWithUnavailableScriptName() throws Exception {
        
        // create the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);
        
        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, mbxDTO.getModifiedBy());
        
        // Adding mailbox to processor
        ProcessorConfigurationService procService = new ProcessorConfigurationService();
        AddProcessorToMailboxRequestDTO procRequestDTO = constructDummyProcessorDTO(response.getMailBox().getGuid(), mbxDTO);
        AddProcessorToMailboxResponseDTO procResponseDTO = procService.createProcessor(response.getMailBox().getGuid(), procRequestDTO, serviceInstanceId, procRequestDTO.getProcessor().getModifiedBy());
        // Get the processor
        GetProcessorResponseDTO procGetResponseDTO = procService.getProcessor(response.getMailBox().getGuid(), procResponseDTO.getProcessor().getGuId());
        
        //search processor by filter
        ProcessorConfigurationService processor = new ProcessorConfigurationService();
        GenericSearchFilterDTO searchFilter = new GenericSearchFilterDTO();
        searchFilter.setScriptName(procGetResponseDTO.getProcessor().getJavaScriptURI() + "1");
        searchFilter.setMatchMode(GenericSearchFilterDTO.MATCH_MODE_EQUALS_CHR);
        GetProcessorResponseDTO serviceResponse = processor.searchProcessor(searchFilter);
        
        Assert.assertEquals(SUCCESS, serviceResponse.getResponse().getStatus());
        Assert.assertTrue(serviceResponse.getResponse().getMessage().contains(Messages.NO_COMPONENT_EXISTS.value().replaceAll("%s", MailBoxConstants.MAILBOX_PROCESSOR)));
    }
    
    /**
     * Method to read cluster type using Processor guid.
     */
    @Test
    public void testReadClusterTypeByProcessorId() throws Exception {
        
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);
        
        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, mbxDTO.getModifiedBy());
        
        ProcessorConfigurationService procService = new ProcessorConfigurationService();
        AddProcessorToMailboxRequestDTO procRequestDTO = constructDummyProcessorDTO(response.getMailBox().getGuid(), mbxDTO);
        AddProcessorToMailboxResponseDTO procResponseDTO = procService.createProcessor(response.getMailBox().getGuid(), procRequestDTO, serviceInstanceId, procRequestDTO.getProcessor().getModifiedBy());
        
        ClusterTypeResponseDTO clusterTypeResponseDTO = procService.getClusterType(procResponseDTO.getProcessor().getGuId());
        
        Assert.assertEquals(MailBoxUtil.CLUSTER_TYPE, clusterTypeResponseDTO.getClusterType());
        Assert.assertEquals(SUCCESS, clusterTypeResponseDTO.getResponse().getStatus());
        Assert.assertTrue(clusterTypeResponseDTO.getResponse().getMessage().contains(Messages.READ_SUCCESSFUL.value().replaceAll("%s", MailBoxConstants.CLUSTER_TYPE)));
    }
    
    /**
     * Method to read cluster type using invalid Processor guid.
     */
    @Test
    public void testReadClusterTypeByInvalidProcessorId() throws Exception {
        
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);
        
        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, mbxDTO.getModifiedBy());
        
        ProcessorConfigurationService procService = new ProcessorConfigurationService();
        AddProcessorToMailboxRequestDTO procRequestDTO = constructDummyProcessorDTO(response.getMailBox().getGuid(), mbxDTO);
        AddProcessorToMailboxResponseDTO procResponseDTO = procService.createProcessor(response.getMailBox().getGuid(), procRequestDTO, serviceInstanceId, procRequestDTO.getProcessor().getModifiedBy());
        
        ClusterTypeResponseDTO clusterTypeResponseDTO = procService.getClusterType(procResponseDTO.getProcessor().getGuId() + "1");
        
        Assert.assertEquals(FAILURE, clusterTypeResponseDTO.getResponse().getStatus());
        Assert.assertTrue(clusterTypeResponseDTO.getResponse().getMessage().contains(Messages.READ_OPERATION_FAILED.value().replaceAll("%s", MailBoxConstants.CLUSTER_TYPE)));
    }

    /**
     * Method to read cluster type using invalid Processor guid.
     */
    @Test
    public void testReadClusterTypeByEmptyProcessorId() throws Exception {
        
        ProcessorConfigurationService procService = new ProcessorConfigurationService();
        
        ClusterTypeResponseDTO clusterTypeResponseDTO = procService.getClusterType("");
        
        Assert.assertEquals(FAILURE, clusterTypeResponseDTO.getResponse().getStatus());
        Assert.assertTrue(clusterTypeResponseDTO.getResponse().getMessage().contains(Messages.READ_OPERATION_FAILED.value().replaceAll("%s", MailBoxConstants.CLUSTER_TYPE)));
    }
    
    /*
     * Method to search  Processor by cluster type.
     */
    @Test
    public void testSearchProcessorByClusterType() throws Exception {
    
        // create the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, mbxDTO.getModifiedBy());

        //Adding mailbox to processor
        List<ProcessorDTO> processors = new ArrayList<>();
        ProcessorConfigurationService procService = new ProcessorConfigurationService();
        AddProcessorToMailboxRequestDTO procRequestDTO = constructDummyProcessorDTO(response.getMailBox().getGuid(), mbxDTO);
        AddProcessorToMailboxResponseDTO procResponseDTO = procService.createProcessor(response.getMailBox().getGuid(), procRequestDTO, serviceInstanceId, procRequestDTO.getProcessor().getModifiedBy());
        // Get the processor
        GetProcessorResponseDTO procGetResponseDTO = procService.getProcessor(response.getMailBox().getGuid(), procResponseDTO.getProcessor().getGuId());
        processors.add(procGetResponseDTO.getProcessor());

        procRequestDTO = constructDummyProcessorDTO(response.getMailBox().getGuid(), mbxDTO);
        procResponseDTO = procService.createProcessor(response.getMailBox().getGuid(), procRequestDTO, serviceInstanceId, procRequestDTO.getProcessor().getModifiedBy());
        // Get the processor
        procGetResponseDTO = procService.getProcessor(response.getMailBox().getGuid(), procResponseDTO.getProcessor().getGuId());
        processors.add(procGetResponseDTO.getProcessor());

        procRequestDTO = constructDummyProcessorDTO(response.getMailBox().getGuid(), mbxDTO);
        procRequestDTO.getProcessor().setClusterType(LOWSECURE);
        procResponseDTO = procService.createProcessor(response.getMailBox().getGuid(), procRequestDTO, serviceInstanceId, procRequestDTO.getProcessor().getModifiedBy());
        // Get the processor
        procGetResponseDTO = procService.getProcessor(response.getMailBox().getGuid(), procResponseDTO.getProcessor().getGuId());
        processors.add(procGetResponseDTO.getProcessor());
        
        //search processor by cluster Type is lowsecure
        GenericSearchFilterDTO lowSecureSearchFilter = new GenericSearchFilterDTO();
        lowSecureSearchFilter.setClusterType(LOWSECURE);
        lowSecureSearchFilter.setMatchMode(GenericSearchFilterDTO.MATCH_MODE_EQUALS_STR);
        GetProcessorResponseDTO lowsecureSearchResponse = procService.searchProcessor(lowSecureSearchFilter);

        //search processor by cluster Type is secure
        GenericSearchFilterDTO secureSearchFilter = new GenericSearchFilterDTO();
        secureSearchFilter.setClusterType(SECURE);
        secureSearchFilter.setMatchMode(GenericSearchFilterDTO.MATCH_MODE_EQUALS_CHR);
        GetProcessorResponseDTO secureSearchResponse = procService.searchProcessor(secureSearchFilter);
        
        //search processor by cluster Type and unavailable processor guid
        GenericSearchFilterDTO searchFilter = new GenericSearchFilterDTO();
        searchFilter.setClusterType(SECURE);
        searchFilter.setProcessorGuid("1A2S3D4F5G6H7J8K9L0ZQXWCEVRBTNYM");
        searchFilter.setMatchMode(GenericSearchFilterDTO.MATCH_MODE_EQUALS_CHR);
        GetProcessorResponseDTO searchResponse = procService.searchProcessor(searchFilter);

        Assert.assertEquals(processors.size(), 3);

        Assert.assertEquals(SUCCESS, secureSearchResponse.getResponse().getStatus());
        Assert.assertEquals(SUCCESS, lowsecureSearchResponse.getResponse().getStatus());
        Assert.assertTrue(secureSearchResponse.getResponse().getMessage().contains(Messages.READ_SUCCESSFUL.value().replaceAll("%s", MailBoxConstants.MAILBOX_PROCESSOR)));
        Assert.assertTrue(lowsecureSearchResponse.getResponse().getMessage().contains(Messages.READ_SUCCESSFUL.value().replaceAll("%s", MailBoxConstants.MAILBOX_PROCESSOR)));
        Assert.assertTrue(searchResponse.getResponse().getMessage().contains(Messages.NO_COMPONENT_EXISTS.value().replaceAll("%s", MailBoxConstants.MAILBOX_PROCESSOR)));

    }
    
    /*
     * fetch the Datacenter's and corresponding processors details.
     */
    @Test
    public void testGetDatacenters() throws Exception {
        GetDatacenterResponseDTO serviceResponse = new ProcessorAffinityService().getDatacenters();
        Assert.assertEquals(SUCCESS, serviceResponse.getResponse().getStatus());
    }
    
    /**
     * Method to retrieve the processor name using processor pguid.
     */
    @Test
    public void testGetProcessorNameByPguid() throws Exception {
        
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);
        
        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, mbxDTO.getModifiedBy());
        
        ProcessorConfigurationService procService = new ProcessorConfigurationService();
        AddProcessorToMailboxRequestDTO procRequestDTO = constructDummyProcessorDTO(response.getMailBox().getGuid(), mbxDTO);
        AddProcessorToMailboxResponseDTO procResponseDTO = procService.createProcessor(response.getMailBox().getGuid(), procRequestDTO, serviceInstanceId, procRequestDTO.getProcessor().getModifiedBy());
        GetProcessorIdResponseDTO serviceResponse = procService.getProcessorNameByPguid(procResponseDTO.getProcessor().getGuId());
        
        Assert.assertEquals(SUCCESS, serviceResponse.getResponse().getStatus());
        Assert.assertEquals(procRequestDTO.getProcessor().getName(), serviceResponse.getProcessorName());
        Assert.assertTrue(serviceResponse.getResponse().getMessage().contains(Messages.READ_SUCCESSFUL.value().replaceAll("%s", MailBoxConstants.MAILBOX_PROCESSOR)));
    }
    
    /**
     * Method used to retrieve the processor Id using mailbox name processor name.
     */
    @Test
    public void testGetProcessorIdByProcNameAndMbxName() throws Exception {
        
        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, mbxDTO.getModifiedBy());

        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());

        // Adding the processor
        AddProcessorToMailboxRequestDTO procRequestDTO = constructDummyProcessorDTO(response.getMailBox().getGuid(), mbxDTO);
        ProcessorConfigurationService procService = new ProcessorConfigurationService();
        AddProcessorToMailboxResponseDTO procResponseDTO = procService.createProcessor(response.getMailBox().getGuid(), procRequestDTO, serviceInstanceId, procRequestDTO.getProcessor().getModifiedBy());
        GetProcessorResponseDTO procGetResponseDTO = procService.getProcessor(response.getMailBox().getGuid(), procResponseDTO.getProcessor().getGuId());
        
        // Assertion
        Assert.assertNotNull(procGetResponseDTO);
        Assert.assertNotNull(procGetResponseDTO.getResponse());
        Assert.assertEquals(procResponseDTO.getProcessor().getGuId(), procGetResponseDTO.getProcessor().getGuid());
        Assert.assertEquals(SUCCESS, procGetResponseDTO.getResponse().getStatus());

        GetProcessorIdResponseDTO serviceResponse = procService.getProcessorIdByProcNameAndMbxName(mbxDTO.getName(),procGetResponseDTO.getProcessor().getName());
        
        Assert.assertEquals(SUCCESS, serviceResponse.getResponse().getStatus());
        Assert.assertTrue(serviceResponse.getResponse().getMessage().contains(Messages.READ_SUCCESSFUL.value().replaceAll("%s", MailBoxConstants.MAILBOX_PROCESSOR)));
    }
    
    /**
     * Get the Processor names for the search processor.
     */
    @Test
    public void testGetProcessorNamesWithEmpty() throws Exception {
        
        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, mbxDTO.getModifiedBy());

        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());

        // Adding the processor
        AddProcessorToMailboxRequestDTO procRequestDTO = constructDummyProcessorDTO(response.getMailBox().getGuid(), mbxDTO);
        
        //search processor by cluster Type is lowsecure
        ProcessorConfigurationService processor = new ProcessorConfigurationService();
        GenericSearchFilterDTO secureSearchFilter = new GenericSearchFilterDTO();
        secureSearchFilter.setProcessorName(procRequestDTO.getProcessor().getName());
        secureSearchFilter.setClusterType(LOWSECURE);
        SearchProcessorResponseDTO serviceResponse = processor.getProcessorNames(secureSearchFilter);

        // Assertion
        Assert.assertEquals(SUCCESS, serviceResponse.getResponse().getStatus());
        Assert.assertTrue(serviceResponse.getResponse().getMessage().contains(Messages.NO_COMPONENT_EXISTS.value().replaceAll("%s", MailBoxConstants.PROCESSOR)));
    }
    
    /**
     * Get the Processor names for the search processor.
     */
    @Test
    public void testGetProcessorNames() throws Exception {
        
        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, mbxDTO.getModifiedBy());

        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());

        // Adding the processor
        AddProcessorToMailboxRequestDTO procRequestDTO = constructDummyProcessorDTO(response.getMailBox().getGuid(), mbxDTO);
        ProcessorConfigurationService procService = new ProcessorConfigurationService();
        procService.createProcessor(response.getMailBox().getGuid(), procRequestDTO, serviceInstanceId, procRequestDTO.getProcessor().getModifiedBy());
        
        ProcessorConfigurationService processor = new ProcessorConfigurationService();
        GenericSearchFilterDTO secureSearchFilter = new GenericSearchFilterDTO();
        secureSearchFilter.setProcessorName(procRequestDTO.getProcessor().getName());
        secureSearchFilter.setClusterType(LOWSECURE);
        SearchProcessorResponseDTO serviceResponse = processor.getProcessorNames(secureSearchFilter);

        // Assertion
        Assert.assertEquals(SUCCESS, serviceResponse.getResponse().getStatus());
        Assert.assertEquals(procRequestDTO.getProcessor().getName(), serviceResponse.getProcessor().get(0).getName());
        Assert.assertTrue(serviceResponse.getResponse().getMessage().contains(Messages.READ_SUCCESSFUL.value().replaceAll("%s", MailBoxConstants.PROCESSOR)));
    }
}