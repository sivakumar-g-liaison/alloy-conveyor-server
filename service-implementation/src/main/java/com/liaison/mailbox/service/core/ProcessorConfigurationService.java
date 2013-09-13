package com.liaison.mailbox.service.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.enums.ProcessorType;
import com.liaison.mailbox.jpa.dao.MailBoxScheduleProfileConfigurationDAO;
import com.liaison.mailbox.jpa.dao.MailBoxScheduleProfileConfigurationDAOBase;
import com.liaison.mailbox.jpa.dao.ProcessorConfigurationDAO;
import com.liaison.mailbox.jpa.dao.ProcessorConfigurationDAOBase;
import com.liaison.mailbox.jpa.model.MailBoxSchedProfile;
import com.liaison.mailbox.jpa.model.Processor;
import com.liaison.mailbox.jpa.model.RemoteDownloader;
import com.liaison.mailbox.jpa.model.RemoteUploader;
import com.liaison.mailbox.service.dto.ResponseDTO;
import com.liaison.mailbox.service.dto.configuration.MailBoxResponseDTO;
import com.liaison.mailbox.service.dto.configuration.ProcessorDTO;
import com.liaison.mailbox.service.dto.configuration.request.AddProcessorToMailboxRequestDTO;
import com.liaison.mailbox.service.dto.configuration.request.ReviseProcessorRequestDTO;
import com.liaison.mailbox.service.dto.configuration.response.AddProcessorToMailboxResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.DeActivateProcessorResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.GetProcessorResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.ProcessorResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.ReviseProcessorResponseDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;

/**
 * @author sivakumarg
 *
 */
public class ProcessorConfigurationService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ProcessorConfigurationService.class);
	private static String PROCESSOR = "Processor";

	/**
	 * Method inserts the analytic config details into the mail box processor.
	 * 
	 * @param serviceRequest
	 * @return
	 */
	public AddProcessorToMailboxResponseDTO createProcessor(AddProcessorToMailboxRequestDTO serviceRequest) {

		LOGGER.info("call receive to insert the profile ::{}", serviceRequest.getProcessor());
		Processor processor = null;
		
		if (ProcessorType.REMOTEDOWNLOADER.toString()
				.equalsIgnoreCase(serviceRequest.getProcessor().getType())) {
			processor = new RemoteDownloader();
		} else if (ProcessorType.REMOTEUPLOADER.toString()
				.equalsIgnoreCase(serviceRequest.getProcessor().getType())) {
			processor = new RemoteUploader();
		} else {
			processor = null;
		}

		if (processor != null) {

			serviceRequest.getProcessor().copyToEntity(processor, true);
			String mailboxId = serviceRequest.getProcessor().getLinkedMailboxId();
			String profileId = serviceRequest.getProcessor().getLinkedProfileId();
			MailBoxScheduleProfileConfigurationDAO scheduleProfileDAO = new MailBoxScheduleProfileConfigurationDAOBase();
			MailBoxSchedProfile scheduleProfile = scheduleProfileDAO.find(mailboxId, profileId);
			processor.setMailboxSchedProfile(scheduleProfile);
			ProcessorConfigurationDAO componenDao = new ProcessorConfigurationDAOBase();
			componenDao.persist(processor);
		}
		AddProcessorToMailboxResponseDTO serviceResponse = new AddProcessorToMailboxResponseDTO();
		serviceResponse.setResponse(new ResponseDTO(Messages.CREATED_SUCCESSFULLY,PROCESSOR,Messages.SUCCESS));			
		serviceResponse.setProcessor(new ProcessorResponseDTO(String.valueOf(processor.getPguid())));
		LOGGER.info("Exit from create processor.");
		return serviceResponse;

	}
	
	/**
	 * Get the Processor using guid.
	 * 
	 * @param processorGuid
	 *            The guid of the Processor.
	 * @return The responseDTO.
	 */
	public GetProcessorResponseDTO getProcessor(String processorGuid) {

		GetProcessorResponseDTO serviceResponse = new GetProcessorResponseDTO();
		
		try {

			LOGGER.info("Entering into get processor.");
			LOGGER.info("The retrieve guid is {} ", processorGuid);

			ProcessorConfigurationDAO config = new ProcessorConfigurationDAOBase();
			Processor processor = config.find(Processor.class, processorGuid);
			if (processor == null) {
				throw new MailBoxConfigurationServicesException(Messages.PROCESSOR_DOES_NOT_EXIST, processorGuid);
			}

			ProcessorDTO dto = new ProcessorDTO();
			dto.copyFromEntity(processor);

			serviceResponse.setProcessor(dto);
			serviceResponse.setResponse(new ResponseDTO(
					Messages.READ_SUCCESSFUL, PROCESSOR, Messages.SUCCESS));
			LOGGER.info("Exit from get mailbox.");
			return serviceResponse;

		} catch (MailBoxConfigurationServicesException e) {

			LOGGER.error(Messages.READ_OPERATION_FAILED.name(), e);
			serviceResponse.setResponse(new ResponseDTO(Messages.READ_OPERATION_FAILED, PROCESSOR,Messages.FAILURE, e.getMessage()));
			return serviceResponse;
		}

	}

	/**
	 * Get the Processor using guid.
	 * 
	 * @param processorGuid The guid of the Processor.
	 * @return The responseDTO.
	 */
	public DeActivateProcessorResponseDTO deactivateProcessor(String processorGuid) {
		
		DeActivateProcessorResponseDTO serviceResponse = null;
		ResponseDTO response = null;

		try {
			
			if (processorGuid == null || processorGuid.equals("")) {
				throw new MailBoxConfigurationServicesException("Processor cannot be found");
			}
			
			LOGGER.info("Entering into get processor.");
			LOGGER.info("Deactivate guid is {} ", processorGuid);
			
			ProcessorConfigurationDAO config = new ProcessorConfigurationDAOBase();
			config.deactivate(processorGuid);
			
			response = new ResponseDTO();
			serviceResponse = new DeActivateProcessorResponseDTO();
			
			response.setMessage(MailBoxConstants.DELETE_PROCESSOR_SUCCESS);
			response.setStatus(MailBoxConstants.SUCCESS);
			
			ProcessorResponseDTO dto = new ProcessorResponseDTO();
			dto.setGuId(processorGuid);
			serviceResponse.setProcessor(dto);
			serviceResponse.setResponse(response);
			
			LOGGER.info("Exit from deactivate processor.");
		} catch (Exception e) {
			
			response = new ResponseDTO();
			serviceResponse = new DeActivateProcessorResponseDTO();
			response.setMessage(MailBoxConstants.DELETE_PROCESSOR_FAILURE + e.getMessage());
			response.setStatus(MailBoxConstants.FAILURE);
		}
		
		return serviceResponse;
	}
	
	/**
	 * Method revise the mailbox configurations.
	 * 
	 * @param request
	 */
	
	public ReviseProcessorResponseDTO reviseProcessor(ReviseProcessorRequestDTO request) {
		
		LOGGER.info("Entering into revising processor.");
		LOGGER.info("Request guid is {} ", request.getProcessor().getGuid());
		
		ProcessorConfigurationDAO config = new ProcessorConfigurationDAOBase();
		Processor processor = config.find(Processor.class, request.getProcessor().getGuid());
		
		processor.getFolders().clear();
		processor.getCredentials().clear();
		
		request.copyToEntity(processor);
		
		config.merge(processor);
		
		//Response Construction
		ReviseProcessorResponseDTO serviceResponse = new ReviseProcessorResponseDTO();

		ResponseDTO response = new ResponseDTO();
		response.setMessage(MailBoxConstants.REVISE_PROCESSOR_SUCCESS);
		response.setStatus(MailBoxConstants.SUCCESS);

		ProcessorDTO dto = new ProcessorDTO();
		dto.setGuid(String.valueOf(processor.getPrimaryKey()));
		
		serviceResponse.setProcessor(dto);
		serviceResponse.setResponse(response);

		LOGGER.info("Exit from revising processor.");
		
		return serviceResponse;
		
	}

}
