/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.core;

import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.dtdm.dao.ProcessorConfigurationDAO;
import com.liaison.mailbox.dtdm.dao.ProcessorConfigurationDAOBase;
import com.liaison.mailbox.dtdm.model.Processor;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.service.dto.GenericSearchFilterDTO;
import com.liaison.mailbox.service.dto.ResponseDTO;
import com.liaison.mailbox.service.dto.configuration.ProcessorLinkedScriptDTO;
import com.liaison.mailbox.service.dto.configuration.ProcessorScriptDTO;
import com.liaison.mailbox.service.dto.configuration.response.ProcessorScriptResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.ScriptLinkedProcessorsResponseDTO;
import com.liaison.mailbox.service.util.MailBoxUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Class which has Processor ScriptURI related operations.
 *
 */
public class ProcessorScriptService {

    private static final Logger LOGGER = LogManager.getLogger(ProcessorScriptService.class);

    /**
     * Get the Processor details of the Selected ScriptUI.
     * 
     * @param searchFilter The GenericSearchFilterDTO DTO
     * @param filterText     
     * @return serviceResponse ProcessorLinkedScriptResponseDTO
     */
    public ScriptLinkedProcessorsResponseDTO getLinkedScriptProcessor(GenericSearchFilterDTO searchFilter, String filterText) {

        ScriptLinkedProcessorsResponseDTO serviceResponse = new ScriptLinkedProcessorsResponseDTO();

        try {

            LOGGER.debug("Entering into getLinkedScriptProcessor ().");

            ProcessorConfigurationDAO config = new ProcessorConfigurationDAOBase();
            int totalCount = 0;
            Map<String, Integer> pageOffsetDetails = null;

            totalCount = config.getFilteredScriptLinkedProcessorsCount(searchFilter);
            pageOffsetDetails = MailBoxUtil.getPagingOffsetDetails(searchFilter.getPage(), searchFilter.getPageSize(), totalCount);

            List<Processor> processors = config.getScriptLinkedProcessors(searchFilter, pageOffsetDetails, filterText);

            List<ProcessorLinkedScriptDTO> prsScriptDTO = new ArrayList<ProcessorLinkedScriptDTO>();
            if (null == processors || processors.isEmpty()) {
                serviceResponse.setResponse(new ResponseDTO(Messages.NO_COMPONENT_EXISTS, MailBoxConstants.MAILBOX_PROCESSOR, Messages.SUCCESS));
                serviceResponse.setProcessors(prsScriptDTO);
                return serviceResponse;
            }

            ProcessorLinkedScriptDTO processorScriptDTO = null;
            for (Processor processor : processors) {
                processorScriptDTO = new ProcessorLinkedScriptDTO();
                processorScriptDTO.copyFromEntity(processor);
                prsScriptDTO.add(processorScriptDTO);
            }
            // response message construction
            serviceResponse.setResponse(new ResponseDTO(Messages.READ_SUCCESSFUL, MailBoxConstants.MAILBOX_PROCESSOR, Messages.SUCCESS));
            serviceResponse.setTotalItems(totalCount);
            serviceResponse.setProcessors(prsScriptDTO);

            LOGGER.debug("Exit from getLinkedScriptProcessor ().");
            return serviceResponse;
        } catch (Exception e) {

            LOGGER.error(Messages.READ_OPERATION_FAILED.name(), e);
            serviceResponse.setResponse(new ResponseDTO(Messages.READ_OPERATION_FAILED, MailBoxConstants.MAILBOX_PROCESSOR, Messages.FAILURE, e.getMessage()));
            return serviceResponse;
        }
    }

    /**
     * Get the ScriptURI from Processor.
     * 
     * @param searchFilter The GenericSearchFilterDTO DTO            
     * @return serviceResponse ProcessorScriptResponseDTO
     */
    public ProcessorScriptResponseDTO getListOfScriptURI(GenericSearchFilterDTO searchFilter) {

        LOGGER.debug("Enter into getListOfScriptURI () ");
        ProcessorScriptResponseDTO serviceResponse = new ProcessorScriptResponseDTO();

        try {

            LOGGER.debug("Entering into get all scriptUI.");
            ProcessorConfigurationDAO dao = new ProcessorConfigurationDAOBase();
            int totalCount = 0;
            Map<String, Integer> pageOffsetDetails = null;
            totalCount = dao.getFilteredProcessorsScriptCount(searchFilter);
            pageOffsetDetails = MailBoxUtil.getPagingOffsetDetails(searchFilter.getPage(), searchFilter.getPageSize(), totalCount);

            // fetch the all scriptUi
            List<String> scriptURIList = dao.getAllScriptURI(searchFilter, pageOffsetDetails);
            List<ProcessorScriptDTO> prsScriptDTO = new ArrayList<ProcessorScriptDTO>();
            if (null == scriptURIList || scriptURIList.isEmpty()) {
                serviceResponse.setResponse(new ResponseDTO(Messages.NO_COMPONENT_EXISTS, MailBoxConstants.SCRIPT, Messages.SUCCESS));
                serviceResponse.setScripts(prsScriptDTO);
                return serviceResponse;
            }

            ProcessorScriptDTO processorScriptDTO = null;
            for (String script : scriptURIList) {
                processorScriptDTO = new ProcessorScriptDTO();
                processorScriptDTO.setName(script);
                prsScriptDTO.add(processorScriptDTO);
            }
            // response message construction
            serviceResponse.setResponse(new ResponseDTO(Messages.READ_SUCCESSFUL, MailBoxConstants.SCRIPT, Messages.SUCCESS));
            serviceResponse.setTotalItems(totalCount);
            serviceResponse.setScripts(prsScriptDTO);

            LOGGER.debug("Exit from getListOfScriptURI () ");

        } catch (Exception e) {
            LOGGER.error(Messages.READ_OPERATION_FAILED.name(), e);
            serviceResponse.setResponse(new ResponseDTO(Messages.READ_OPERATION_FAILED, MailBoxConstants.SCRIPT, Messages.FAILURE, e.getMessage()));
        }

        return serviceResponse;
    }

}
