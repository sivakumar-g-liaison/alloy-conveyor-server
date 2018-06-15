/**
 * Copyright 2018 Liaison Technologies, Inc.
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.mailbox.service.integration.test;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.liaison.gem.enums.Messages;
import com.liaison.mailbox.enums.ExecutionState;
import com.liaison.mailbox.rtdm.dao.ProcessorExecutionStateDAO;
import com.liaison.mailbox.rtdm.dao.ProcessorExecutionStateDAOBase;
import com.liaison.mailbox.service.base.test.BaseServiceTest;
import com.liaison.mailbox.service.core.MailBoxConfigurationService;
import com.liaison.mailbox.service.core.ProcessorConfigurationService;
import com.liaison.mailbox.service.core.ProcessorExecutionConfigurationService;
import com.liaison.mailbox.service.dto.configuration.MailBoxDTO;
import com.liaison.mailbox.service.dto.configuration.request.AddMailboxRequestDTO;
import com.liaison.mailbox.service.dto.configuration.request.AddProcessorToMailboxRequestDTO;
import com.liaison.mailbox.service.dto.configuration.response.AddMailBoxResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.AddProcessorToMailboxResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.GetProcessorExecutionStateResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.GetProcessorIdResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.GetProcessorResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.UpdateProcessorExecutionStateResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.UpdateProcessorsExecutionStateResponseDTO;

public class ProcessorExecutionConfigurationServiceIT extends BaseServiceTest {

    @Test
    public void testUpdateExecutingProcessor() throws Exception {
    	
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
        
        ProcessorExecutionConfigurationService runningProcessor = new ProcessorExecutionConfigurationService();
        ProcessorExecutionStateDAO processorExecutionStateDAO = new ProcessorExecutionStateDAOBase();
        Object[] result = processorExecutionStateDAO.findByProcessorIdAndUpdateStatus(procGetResponseDTO.getProcessor().getGuid());
        
        Assert.assertEquals(ExecutionState.PROCESSING.name(), result[1]);
        
        // update the state of executing processor
        UpdateProcessorExecutionStateResponseDTO executingStateResponse = runningProcessor.updateExecutingProcessor(procGetResponseDTO.getProcessor().getGuid(), "unknown-user", "true");
        Assert.assertEquals(SUCCESS, executingStateResponse.getResponse().getStatus());
    }
    
    @Test
    public void testUpdateExecutingProcessorWithProcessorGuidIsNull() throws Exception {
    	
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
        
        ProcessorExecutionConfigurationService runningProcessor = new ProcessorExecutionConfigurationService();
        ProcessorExecutionStateDAO processorExecutionStateDAO = new ProcessorExecutionStateDAOBase();
        Object[] result = processorExecutionStateDAO.findByProcessorIdAndUpdateStatus(procGetResponseDTO.getProcessor().getGuid());
        
        Assert.assertEquals(ExecutionState.PROCESSING.name(), result[1]);
        
        // update the state of executing processor
        UpdateProcessorExecutionStateResponseDTO executingStateResponse = runningProcessor.updateExecutingProcessor(null, procGetResponseDTO.getProcessor().getModifiedBy(), "true");
        Assert.assertEquals(FAILURE, executingStateResponse.getResponse().getStatus());
    }
    
    /**
     * Method to update failed status processor.
     * 
     * @throws Exception
     */
    @Test
    public void testUpdateFailedExecutionProcessor() throws Exception {

        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, mbxDTO.getModifiedBy());

        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());

        AddProcessorToMailboxRequestDTO procRequestDTO = constructDummyProcessorDTO(response.getMailBox().getGuid(), mbxDTO);
        ProcessorConfigurationService procService = new ProcessorConfigurationService();
        AddProcessorToMailboxResponseDTO procResponseDTO = procService.createProcessor(response.getMailBox().getGuid(),
                procRequestDTO, serviceInstanceId, procRequestDTO.getProcessor().getModifiedBy());
        GetProcessorResponseDTO procGetResponseDTO = procService.getProcessor(response.getMailBox().getGuid(), procResponseDTO.getProcessor().getGuId());
        
        // Assertion
        Assert.assertNotNull(procGetResponseDTO);
        Assert.assertNotNull(procGetResponseDTO.getResponse());
        Assert.assertEquals(procResponseDTO.getProcessor().getGuId(), procGetResponseDTO.getProcessor().getGuid());
        Assert.assertEquals(SUCCESS, procGetResponseDTO.getResponse().getStatus());
        
        ProcessorExecutionStateDAO processorExecutionStateDAO = new ProcessorExecutionStateDAOBase();
        Object[] result = processorExecutionStateDAO.findByProcessorIdAndUpdateStatus(procGetResponseDTO.getProcessor().getGuid());
        
        Assert.assertEquals(ExecutionState.PROCESSING.name(), result[1]);
        
        ProcessorExecutionConfigurationService runningProcessors = new ProcessorExecutionConfigurationService();

        UpdateProcessorExecutionStateResponseDTO runningProcessorDTO = runningProcessors
                .updateExecutingProcessor(procResponseDTO.getProcessor().getGuId(), "unknown-user", "true");
        
        Assert.assertEquals(Messages.SUCCESS.value(), runningProcessorDTO.getResponse().getStatus());

    }
    
    /**
     * Method to test processor execution state for invalid processor.
     */
    @Test
    public void testUpdateProcessorExecutionStateWithInvalidProcessor() {
        
        ProcessorExecutionConfigurationService runningProcessors = new ProcessorExecutionConfigurationService();
        UpdateProcessorExecutionStateResponseDTO runningProcessorDTO =runningProcessors
                .updateExecutingProcessor(Long.toString(System.currentTimeMillis()), "unknown-user", "true");
        
        Assert.assertEquals(FAILURE, runningProcessorDTO.getResponse().getStatus());
    }
    
    /**
     * Test method to notify stuck processors
     */
    @Test
    public void testNotifyStcukProcessors() throws Exception {
        ProcessorExecutionConfigurationService executingProcessors = new ProcessorExecutionConfigurationService();
        executingProcessors.notifyStuckProcessors();
    }
   
    /**
     * Test method to update the processor state from "PROCESSING" to "FAILED" on starting the server
     */
    @Test
    public void testUpdateExecutionStateOnInit() throws Exception {
        ProcessorExecutionConfigurationService executingProcessors = new ProcessorExecutionConfigurationService();
        executingProcessors.updateExecutionStateOnInit();
    }
    
    /**
     * Method to read executing processors
     * @throws Exception
     */
    @Test
    public void testReadExecutingProcessorsWithEmpty() throws Exception {
        
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, mbxDTO.getModifiedBy());

        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());

        AddProcessorToMailboxRequestDTO procRequestDTO = constructDummyProcessorDTO(response.getMailBox().getGuid(), mbxDTO);
        ProcessorConfigurationService procService = new ProcessorConfigurationService();
        AddProcessorToMailboxResponseDTO procResponseDTO = procService.createProcessor(response.getMailBox().getGuid(),
                procRequestDTO, serviceInstanceId, procRequestDTO.getProcessor().getModifiedBy());
        GetProcessorResponseDTO procGetResponseDTO = procService.getProcessor(response.getMailBox().getGuid(), procResponseDTO.getProcessor().getGuId());
        
        // Assertion
        Assert.assertNotNull(procGetResponseDTO);
        Assert.assertNotNull(procGetResponseDTO.getResponse());
        Assert.assertEquals(procResponseDTO.getProcessor().getGuId(), procGetResponseDTO.getProcessor().getGuid());
        Assert.assertEquals(SUCCESS, procGetResponseDTO.getResponse().getStatus());
        
        ProcessorExecutionStateDAO processorExecutionStateDAO = new ProcessorExecutionStateDAOBase();
        Object[] result = processorExecutionStateDAO.findByProcessorIdAndUpdateStatus(procGetResponseDTO.getProcessor().getGuid());
        
        Assert.assertEquals(ExecutionState.PROCESSING.name(), result[1]);
        
        ProcessorExecutionConfigurationService runningProcessor = new ProcessorExecutionConfigurationService();
        GetProcessorExecutionStateResponseDTO executingStateResponse = runningProcessor.getExecutingProcessors("0", "0", "0", "0");
        
        Assert.assertEquals(FAILURE, executingStateResponse.getResponse().getStatus());
    }
    
    /**
     * Method to update the state of executing processors to failed.
     */
    @Test
    public void testUpdateExecutingProcessorsWithEmptyProcesssorGuids() throws Exception {
    	
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
        
        ProcessorExecutionConfigurationService processorService = new ProcessorExecutionConfigurationService();
        GetProcessorIdResponseDTO serviceResponse = procService.getProcessorNameByPguid(procResponseDTO.getProcessor().getGuId());
       
        // update the state of executing processors to failed
        UpdateProcessorsExecutionStateResponseDTO executingStateResponse = processorService.updateExecutingProcessors(serviceResponse.getProcessorGuids(),"unknown-user","unknown-user");
        
        Assert.assertEquals(FAILURE, executingStateResponse.getResponse().getStatus());
    }
    
    /**
     * Method to update the state of executing processors to failed.
     */
    @Test
    public void testUpdateExecutingProcessors() throws Exception {
    	
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
        ProcessorExecutionStateDAO processorExecutionStateDAO = new ProcessorExecutionStateDAOBase();
        Object[] result = processorExecutionStateDAO.findByProcessorIdAndUpdateStatus(procGetResponseDTO.getProcessor().getGuid());
        
        Assert.assertEquals(ExecutionState.PROCESSING.name(), result[1]);
        
        // update the state of executing processors to failed
        ProcessorExecutionConfigurationService processorService = new ProcessorExecutionConfigurationService();
        UpdateProcessorsExecutionStateResponseDTO executingStateResponse = processorService.updateExecutingProcessors(serviceResponse.getProcessorGuids(), "unknown-user", "true");
        
        Assert.assertEquals(SUCCESS, executingStateResponse.getResponse().getStatus());
    }
}
