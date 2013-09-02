package com.liaison.mailbox.service.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.enums.ProcessorType;
import com.liaison.mailbox.jpa.dao.MailBoxConfigurationDAO;
import com.liaison.mailbox.jpa.dao.MailBoxConfigurationDAOBase;
import com.liaison.mailbox.jpa.dao.ProcessorConfigurationDAO;
import com.liaison.mailbox.jpa.dao.ProcessorConfigurationDAOBase;
import com.liaison.mailbox.jpa.model.MailBox;
import com.liaison.mailbox.jpa.model.Processor;
import com.liaison.mailbox.jpa.model.RemoteDownloader;
import com.liaison.mailbox.jpa.model.RemoteUploader;
import com.liaison.mailbox.service.dto.ResponseDTO;
import com.liaison.mailbox.service.dto.configuration.MailBoxDTO;
import com.liaison.mailbox.service.dto.configuration.ProcessorDTO;
import com.liaison.mailbox.service.dto.configuration.request.AddProcessorToMailboxRequestDTO;
import com.liaison.mailbox.service.dto.configuration.response.AddProcessorToMailboxResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.DeActivateProcessorResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.GetMailBoxResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.GetProcessorResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.ProcessorResponseDTO;

/**
 * @author sivakumarg
 *
 */
public class ProcessorConfigurationService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ProcessorConfigurationService.class);

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
			ProcessorConfigurationDAO componenDao = new ProcessorConfigurationDAOBase();
			componenDao.persist(processor);
		}
		AddProcessorToMailboxResponseDTO serviceResponse = new AddProcessorToMailboxResponseDTO();
		serviceResponse.setResponse(new ResponseDTO());
		serviceResponse.getResponse().setStatus(MailBoxConstants.SUCCESS);
		serviceResponse.getResponse().setMessage(MailBoxConstants.CREATE_PROCESSOR_SUCCESS);
		serviceResponse.setProcessor(new ProcessorResponseDTO());
		serviceResponse.getProcessor().setGuId(processor.getPguid());
		
		return serviceResponse;
	}
	
	/**
	 * Get the Processor using guid.
	 * 
	 * @param guid The guid of the Processor.
	 * @return The responseDTO.
	 */
	public GetProcessorResponseDTO getProcessor(String guid) {

		LOGGER.info("Entering into get processor.");
		LOGGER.info("The retrieve guid is {} ", guid);

		ProcessorConfigurationDAO config = new ProcessorConfigurationDAOBase();
		Processor processor = config.find(Processor.class, guid);

		//Response Construction
		GetProcessorResponseDTO serviceResponse = new GetProcessorResponseDTO();

		ResponseDTO response = new ResponseDTO();
		
		if (null != processor) {

			response.setMessage(MailBoxConstants.GET_PROCESSOR_SUCCESS);
			response.setStatus(MailBoxConstants.SUCCESS);

			ProcessorDTO dto = new ProcessorDTO();
			dto.copyFromEntity(processor);

			serviceResponse.setProcessor(dto);
			serviceResponse.setResponse(response);

			LOGGER.info("Exit from get processor.");
			return serviceResponse;

		} else {
			
			response.setMessage(MailBoxConstants.GET_PROCESSOR_SUCCESS);
			response.setStatus(MailBoxConstants.SUCCESS);

			serviceResponse.setResponse(response);

			LOGGER.info("Exit from get processor.");
			return serviceResponse;
		}
	}
	
	/**
	 * Get the Processor using guid.
	 * 
	 * @param guid The guid of the Processor.
	 * @return The responseDTO.
	 */
	public DeActivateProcessorResponseDTO deactivateProcessor(String guid) {

		LOGGER.info("Entering into get processor.");
		LOGGER.info("Deacticate guid is {} ", guid);

		ProcessorConfigurationDAO config = new ProcessorConfigurationDAOBase();
		config.softRemove(guid);

		DeActivateProcessorResponseDTO serviceResponse = new DeActivateProcessorResponseDTO();

		ResponseDTO response = new ResponseDTO();

		response.setMessage(MailBoxConstants.DELETE_PROCESSOR_SUCCESS);
		response.setStatus(MailBoxConstants.SUCCESS);

		ProcessorResponseDTO dto = new ProcessorResponseDTO();
		dto.setGuId(guid);
		serviceResponse.setProcessor(dto);
		serviceResponse.setResponse(response);

		LOGGER.info("Exit from deactivate processor.");
		return serviceResponse;
	}

}
