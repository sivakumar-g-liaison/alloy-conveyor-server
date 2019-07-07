/**
 * Copyright 2018 Liaison Technologies, Inc.
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.mailbox.service.integration.test;

import com.liaison.mailbox.service.base.test.BaseServiceTest;
import com.liaison.mailbox.service.core.MailBoxConfigurationService;
import com.liaison.mailbox.service.core.ProcessorConfigurationService;
import com.liaison.mailbox.service.core.ProcessorScriptService;
import com.liaison.mailbox.service.dto.GenericSearchFilterDTO;
import com.liaison.mailbox.service.dto.configuration.MailBoxDTO;
import com.liaison.mailbox.service.dto.configuration.request.AddMailboxRequestDTO;
import com.liaison.mailbox.service.dto.configuration.request.AddProcessorToMailboxRequestDTO;
import com.liaison.mailbox.service.dto.configuration.response.AddMailBoxResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.AddProcessorToMailboxResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.ProcessorScriptResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.ScriptLinkedProcessorsResponseDTO;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ProcessorScriptServiceIT extends BaseServiceTest {

    /**
     * Method constructs Processor with valid data.
     */
    @Test
    public void testRetriveScriptURIUsingJavaScriptURI() throws Exception {

        // create the mailbox and Processor
        createMailboxandProcessor(VALID_SCRIPT);

        // Get the ScriptURIList of the Processor
        ProcessorScriptService processorScriptService = new ProcessorScriptService();
        GenericSearchFilterDTO searchFilter = new GenericSearchFilterDTO();
        searchFilter.setScriptName(VALID_SCRIPT);
        searchFilter.setSortDirection(ASC_DIRECTION);
        searchFilter.setPage(PAGE);
        searchFilter.setPageSize(PAGE_SIZE);

        ProcessorScriptResponseDTO serviceResponse = processorScriptService.getListOfScriptURI(searchFilter);

        // Assertion
        Assert.assertEquals(SUCCESS, serviceResponse.getResponse().getStatus());
    }

    /**
     * Method constructs without sciptURI name.
     */
    @Test
    public void testRetriveScriptURIWithoutJavaScriptURI() throws Exception {

        // create the mailbox and Processor
        createMailboxandProcessor(VALID_SCRIPT);

        // Get the ScriptURIList of the Processor
        ProcessorScriptService processorScriptService = new ProcessorScriptService();
        GenericSearchFilterDTO searchFilter = new GenericSearchFilterDTO();
        searchFilter.setSortDirection(ASC_DIRECTION);
        searchFilter.setPage(PAGE);
        searchFilter.setPageSize(PAGE_SIZE);

        ProcessorScriptResponseDTO serviceResponse = processorScriptService.getListOfScriptURI(searchFilter);

        // Assertion
        Assert.assertEquals(SUCCESS, serviceResponse.getResponse().getStatus());
    }

    /**
     * Method constructs Processor with valid data.
     */
    @Test
    public void testRetriveScriptURIInACSOrder() throws Exception {

        // create the mailbox and Processor
        createMailboxandProcessor(VALID_SCRIPT);

        // Get the ScriptURIList of the Processor
        ProcessorScriptService processorScriptService = new ProcessorScriptService();
        GenericSearchFilterDTO searchFilter = new GenericSearchFilterDTO();
        searchFilter.setScriptName(VALID_SCRIPT);
        searchFilter.setSortDirection(ASC_DIRECTION);
        searchFilter.setPage(PAGE);
        searchFilter.setPageSize(PAGE_SIZE);

        ProcessorScriptResponseDTO serviceResponse = processorScriptService.getListOfScriptURI(searchFilter);

        // Assertion
        Assert.assertEquals(SUCCESS, serviceResponse.getResponse().getStatus());
    }

    /**
     * Method constructs Processor with valid data.
     */
    @Test
    public void testRetriveScriptURIInDESCOrder() throws Exception {

        // create the mailbox and Processor
        createMailboxandProcessor(VALID_SCRIPT);

        // Get the ScriptURIList of the Processor
        ProcessorScriptService processorScriptService = new ProcessorScriptService();
        GenericSearchFilterDTO searchFilter = new GenericSearchFilterDTO();
        searchFilter.setScriptName(VALID_SCRIPT);
        searchFilter.setSortDirection(DESC_DIRECTION);
        searchFilter.setPage(PAGE);
        searchFilter.setPageSize(PAGE_SIZE);

        ProcessorScriptResponseDTO serviceResponse = processorScriptService.getListOfScriptURI(searchFilter);

        // Assertion
        Assert.assertEquals(SUCCESS, serviceResponse.getResponse().getStatus());
    }

    /**
     * Method constructs without Valid sciptURI name.
     */
    @Test
    public void testRetriveScriptURIWithEmptyJavaScriptURI() throws Exception {

        // create the mailbox and Processor
        createMailboxandProcessor(VALID_SCRIPT);

        // Get the ScriptURIList of the Processor
        ProcessorScriptService processorScriptService = new ProcessorScriptService();
        GenericSearchFilterDTO searchFilter = new GenericSearchFilterDTO();
        searchFilter.setScriptName(EMPTY_SCRIPT);
        searchFilter.setSortDirection(ASC_DIRECTION);
        searchFilter.setPage(PAGE);
        searchFilter.setPageSize(PAGE_SIZE);

        ProcessorScriptResponseDTO serviceResponse = processorScriptService.getListOfScriptURI(searchFilter);

        // Assertion
        Assert.assertEquals(SUCCESS, serviceResponse.getResponse().getStatus());
    }

    /**
     * Method constructs with Valid sciptURI name and Sort Field name as PGUID.
     */
    @Test
    public void testRetriveProcessorListWithValidJavaScriptURISortAsPGUID() throws Exception {

        // create the mailbox and Processor
        createMailboxandProcessor(VALID_SCRIPT);
        String filterText= "";
        // Get the ScriptURIList of the Processor
        ProcessorScriptService processorScriptService = new ProcessorScriptService();
        GenericSearchFilterDTO searchFilter = new GenericSearchFilterDTO();
        searchFilter.setScriptName(VALID_SCRIPT);
        searchFilter.setSortDirection(DESC_DIRECTION);
        searchFilter.setSortField(SORT_FIELD_PGUID);
        searchFilter.setPage(PAGE);
        searchFilter.setPageSize(PAGE_SIZE);

        ScriptLinkedProcessorsResponseDTO serviceResponse = processorScriptService.getLinkedScriptProcessor(searchFilter, filterText);

        // Assertion
        Assert.assertEquals(SUCCESS, serviceResponse.getResponse().getStatus());
        Assert.assertEquals(serviceResponse.getResponse().getMessage().contains("Processor read successfully."), true);
    }
    
    /**
     * Method constructs with Valid sciptURI name and Sort Field name as ProcessorName.
     */
    @Test
    public void testRetriveProcessorListWithValidJavaScriptURIAndName() throws Exception {

        // create the mailbox and Processor
        createMailboxandProcessor(VALID_SCRIPT);
        String filterText= "";
        // Get the ScriptURIList of the Processor
        ProcessorScriptService processorScriptService = new ProcessorScriptService();
        GenericSearchFilterDTO searchFilter = new GenericSearchFilterDTO();
        searchFilter.setScriptName(VALID_SCRIPT);
        searchFilter.setSortDirection(DESC_DIRECTION);
        searchFilter.setSortField(SORT_FIELD_NAME);
        searchFilter.setPage(PAGE);
        searchFilter.setPageSize(PAGE_SIZE);

        ScriptLinkedProcessorsResponseDTO serviceResponse = processorScriptService.getLinkedScriptProcessor(searchFilter, filterText);

        // Assertion
        Assert.assertEquals(SUCCESS, serviceResponse.getResponse().getStatus());
        Assert.assertEquals(serviceResponse.getResponse().getMessage().contains("Processor read successfully."), true);
    }

    /**
     * Method constructs with InValid sciptURI name.
     */
    @Test
    public void testRetriveProcessorListWithInvalidJavaScriptURI() throws Exception {

        // create the mailbox and Processor
        createMailboxandProcessor(VALID_SCRIPT);
        String filterText= "";
        
        // Get the ScriptURIList of the Processor
        ProcessorScriptService processorScriptService = new ProcessorScriptService();
        GenericSearchFilterDTO searchFilter = new GenericSearchFilterDTO();
        searchFilter.setScriptName(INVALID_SCRIPT);
        searchFilter.setSortDirection(DESC_DIRECTION);
        searchFilter.setSortField(SORT_FIELD_NAME);
        searchFilter.setPage(PAGE);
        searchFilter.setPageSize(PAGE_SIZE);

        ScriptLinkedProcessorsResponseDTO serviceResponse = processorScriptService.getLinkedScriptProcessor(searchFilter, filterText);

        // Assertion
        Assert.assertEquals(SUCCESS, serviceResponse.getResponse().getStatus());
        Assert.assertEquals(serviceResponse.getResponse().getMessage().contains("No Processor available in the system."), true);
    }

    /**
     * Method constructs without Valid sciptURI name.
     */
    @Test
    public void testRetriveProcessorListWithoutValidJavaScriptURI() throws Exception {

        // create the mailbox and Processor
        createMailboxandProcessor(VALID_SCRIPT);
        String filterText= "";
        // Get the ScriptURIList of the Processor
        ProcessorScriptService processorScriptService = new ProcessorScriptService();
        GenericSearchFilterDTO searchFilter = new GenericSearchFilterDTO();
        searchFilter.setSortDirection(DESC_DIRECTION);
        searchFilter.setSortField(SORT_FIELD_NAME);
        searchFilter.setPage(PAGE);
        searchFilter.setPageSize(PAGE_SIZE);
        ScriptLinkedProcessorsResponseDTO serviceResponse = processorScriptService.getLinkedScriptProcessor(searchFilter, filterText);

        // Assertion
        Assert.assertEquals(SUCCESS, serviceResponse.getResponse().getStatus());
        Assert.assertEquals(serviceResponse.getResponse().getMessage().contains("No Processor available in the system."), true);
    }

    private void createMailboxandProcessor(String script) throws Exception {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, mbxDTO.getModifiedBy());

        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());

        // Adding the processor
        AddProcessorToMailboxRequestDTO procRequestDTO = constructDummyProcessorDTO(response.getMailBox().getGuid(), mbxDTO);
        procRequestDTO.getProcessor().setJavaScriptURI(script);
        ProcessorConfigurationService procService = new ProcessorConfigurationService();
        AddProcessorToMailboxResponseDTO procResponseDTO = procService.createProcessor(response.getMailBox().getGuid(), procRequestDTO, serviceInstanceId, procRequestDTO.getProcessor().getModifiedBy());

        Assert.assertEquals(SUCCESS, procResponseDTO.getResponse().getStatus());

    }

}
