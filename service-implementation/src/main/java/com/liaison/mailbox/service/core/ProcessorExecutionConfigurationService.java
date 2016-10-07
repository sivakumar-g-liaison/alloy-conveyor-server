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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.commons.util.client.sftp.StringUtil;
import com.liaison.mailbox.enums.ExecutionState;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.rtdm.dao.ProcessorExecutionStateDAO;
import com.liaison.mailbox.rtdm.dao.ProcessorExecutionStateDAOBase;
import com.liaison.mailbox.rtdm.model.ProcessorExecutionState;
import com.liaison.mailbox.service.dto.GenericSearchFilterDTO;
import com.liaison.mailbox.service.dto.ResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.ExecutingProcessorsDTO;
import com.liaison.mailbox.service.dto.configuration.response.GetProcessorExecutionStateResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.UpdateProcessorExecutionStateResponseDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.util.MailBoxUtil;

/**
 * class which contains processor execution configuration information.
 * @author 
 *
 */

public class ProcessorExecutionConfigurationService {
	
	private static final Logger LOG = LogManager.getLogger(ProcessorExecutionConfigurationService.class);
	private static final String PROCESSORS = "Processors";
	private static final String EXECUTING_PROCESSORS = "running processors";
	
	/**
	 * Method to get the executing processors
	 * @param searchFilter
	 * @return
	 */
	public GetProcessorExecutionStateResponseDTO findExecutingProcessors(GenericSearchFilterDTO searchFilter) {

		GetProcessorExecutionStateResponseDTO response = new GetProcessorExecutionStateResponseDTO();

		try {

			int totalCount = 0;
			Map<String, Integer> pageOffsetDetails = null;
			List<ExecutingProcessorsDTO> executingProcessorsDTO = new ArrayList<ExecutingProcessorsDTO>();
			List<String> executingProcessorIds = new ArrayList<String>();
			ProcessorExecutionStateDAO processorDao = new ProcessorExecutionStateDAOBase();

			// setting the page offset details
			totalCount = processorDao.findAllExecutingProcessors();
			pageOffsetDetails = MailBoxUtil.getPagingOffsetDetails(searchFilter.getPage(), searchFilter.getPageSize(),
					totalCount);
			List<ProcessorExecutionState> executingProcessors = processorDao.findExecutingProcessors(pageOffsetDetails);

			if (executingProcessors.size() != 0) {

			    ExecutingProcessorsDTO executingProcessor = null;
				for (ProcessorExecutionState processorState : executingProcessors) {

					executingProcessorIds.add(processorState.getProcessorId());
					executingProcessor = new ExecutingProcessorsDTO();
					executingProcessor.copyFromEntity(processorState);
					executingProcessorsDTO.add(executingProcessor);
				}
				response.setResponse(new ResponseDTO(Messages.READ_SUCCESSFUL, Messages.PROCESSORS_LIST.value(),
						Messages.SUCCESS));
				response.setExecutingProcessorIds(executingProcessorIds);
				response.setProcessors(executingProcessorsDTO);
				response.setTotalItems(totalCount);
			} else {
				response.setResponse(new ResponseDTO(Messages.NO_EXECUTING_PROCESSORS_AVAIL, EXECUTING_PROCESSORS,
						Messages.FAILURE));
			}

			return response;

		} catch (MailBoxConfigurationServicesException e) {

			LOG.error(Messages.SEARCH_OPERATION_FAILED.name(), e);
			response.setResponse(new ResponseDTO(Messages.SEARCH_OPERATION_FAILED, PROCESSORS, Messages.FAILURE, e
					.getMessage()));
			return response;
		}

	}
	
	/**
	 * Method to update the state of executing processor
	 *
	 * @param processorId
	 */
	public UpdateProcessorExecutionStateResponseDTO updateExecutingProcessor(String processorId, String userId) {

		UpdateProcessorExecutionStateResponseDTO response = new UpdateProcessorExecutionStateResponseDTO();

		try {

			if (StringUtil.isNullOrEmptyAfterTrim(processorId)) {
				throw new MailBoxConfigurationServicesException(Messages.PROCESSOR_ID_NOT_AVAILABLE,
						Response.Status.BAD_REQUEST);
			}
			ProcessorExecutionStateDAOBase processorDao = new ProcessorExecutionStateDAOBase();

			ProcessorExecutionState processorExecutionState = processorDao.findByProcessorId(processorId);
			if (null == processorExecutionState) {
				throw new MailBoxConfigurationServicesException(Messages.PROCESSOR_EXECUTION_STATE_NOT_EXIST,
						processorId, Response.Status.BAD_REQUEST);
			}

			if (ExecutionState.PROCESSING.value().equals(processorExecutionState.getExecutionStatus())) {

			    Thread threadToStop = MailBoxUtil.getThreadByName(processorExecutionState.getThreadName());
			    threadToStop.interrupt();
				processorExecutionState.setExecutionStatus(ExecutionState.FAILED.value());
				// TODO set Other properties too.
				processorDao.updateProcessorExecutionState(processorExecutionState);
				response.setResponse(new ResponseDTO(Messages.REVISED_SUCCESSFULLY,
						"The processor execution status for processor with id : " + processorId + " is ",
						Messages.SUCCESS));
				LOG.info("The processor execution status updated for processor id {}", processorId);
			} else {
				throw new MailBoxConfigurationServicesException(Messages.PROCESSOR_EXECUTION_STATE_NOT_PROCESSING,
						processorId, Response.Status.BAD_REQUEST);
			}
			return response;

		} catch (MailBoxConfigurationServicesException e) {

			LOG.error(Messages.REVISE_OPERATION_FAILED.name(), e);
			response.setResponse(new ResponseDTO(Messages.REVISE_OPERATION_FAILED, PROCESSORS, Messages.FAILURE, e
					.getMessage()));
			return response;
		}
	}
	
}
