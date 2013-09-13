package com.liaison.mailbox.service.core;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.enums.ProcessorType;
import com.liaison.mailbox.jpa.dao.MailBoxScheduleProfileConfigurationDAO;
import com.liaison.mailbox.jpa.dao.MailBoxScheduleProfileConfigurationDAOBase;
import com.liaison.mailbox.jpa.dao.ProcessorConfigurationDAO;
import com.liaison.mailbox.jpa.dao.ProcessorConfigurationDAOBase;
import com.liaison.mailbox.jpa.dao.ProcessorForMergeConfigurationDAO;
import com.liaison.mailbox.jpa.dao.ProcessorForMergeConfigurationDAOBase;
import com.liaison.mailbox.jpa.model.MailBoxSchedProfile;
import com.liaison.mailbox.jpa.model.Processor;
import com.liaison.mailbox.jpa.model.ProcessorForMerge;
import com.liaison.mailbox.jpa.model.RemoteDownloader;
import com.liaison.mailbox.jpa.model.RemoteUploader;
import com.liaison.mailbox.service.dto.ResponseDTO;
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

		LOGGER.info("call receive to insert the processor ::{}", serviceRequest.getProcessor());
		AddProcessorToMailboxResponseDTO serviceResponse = new AddProcessorToMailboxResponseDTO();

		try {

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

				String linkerId = serviceRequest.getProcessor().getLinkedProfileId();

				MailBoxScheduleProfileConfigurationDAO scheduleProfileDAO = new MailBoxScheduleProfileConfigurationDAOBase();
				MailBoxSchedProfile scheduleProfile = scheduleProfileDAO.find(MailBoxSchedProfile.class, linkerId);

				if (scheduleProfile.getProcessors() != null) {
					processor.setExecutionOrder(scheduleProfile.getProcessors().size() + 1);
				}

				processor.setMailboxSchedProfile(scheduleProfile);
				ProcessorConfigurationDAO componenDao = new ProcessorConfigurationDAOBase();
				componenDao.persist(processor);
			}

			serviceResponse.setResponse(new ResponseDTO(Messages.CREATED_SUCCESSFULLY,PROCESSOR,Messages.SUCCESS));			
			serviceResponse.setProcessor(new ProcessorResponseDTO(String.valueOf(processor.getPguid())));
			LOGGER.info("Exit from create processor.");
			return serviceResponse;

		} catch (Exception e) {

			LOGGER.error(Messages.CREATE_OPERATION_FAILED.name(), e);			
			serviceResponse.setResponse(new ResponseDTO(Messages.CREATE_OPERATION_FAILED,PROCESSOR,Messages.FAILURE,e.getMessage()));
			return serviceResponse;

		}


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
	 * @param processorGuid
	 *            The guid of the Processor.
	 * @return The responseDTO.
	 */
	public DeActivateProcessorResponseDTO deactivateProcessor(String processorGuid) {

		DeActivateProcessorResponseDTO serviceResponse = new DeActivateProcessorResponseDTO();		

		try {

			LOGGER.info("Entering into get processor.");
			LOGGER.info("Deactivate guid is {} ", processorGuid);

			ProcessorConfigurationDAO config = new ProcessorConfigurationDAOBase();
			config.deactivate(processorGuid);

			// response message construction			
			serviceResponse.setResponse(new ResponseDTO(Messages.DEACTIVATION_SUCCESSFUL,PROCESSOR,Messages.SUCCESS));			
			serviceResponse.setProcessor(new ProcessorResponseDTO(processorGuid));
			LOGGER.info("Exit from deactivate mailbox.");
			return serviceResponse;					

		} catch (Exception e) {

			LOGGER.error(Messages.DEACTIVATION_FAILED.name(), e);
			serviceResponse.setResponse(new ResponseDTO(Messages.DEACTIVATION_FAILED, PROCESSOR,Messages.FAILURE, e.getMessage()));
			return serviceResponse;			
		}


	}

	/**
	 * Method revise the mailbox configurations.
	 * 
	 * @param request
	 */

	public ReviseProcessorResponseDTO reviseProcessor(ReviseProcessorRequestDTO request) {

		LOGGER.info("Entering into revising processor.");
		LOGGER.info("Request guid is {} ", request.getProcessor().getGuid());
		ReviseProcessorResponseDTO serviceResponse = new ReviseProcessorResponseDTO();

		try {

			ProcessorDTO processorDTO = request.getProcessor();
			if(processorDTO == null){
				throw new MailBoxConfigurationServicesException(Messages.INVALID_REQUEST);
			}

			ProcessorConfigurationDAO configDao = new ProcessorConfigurationDAOBase();
			Processor processor = configDao.find(Processor.class, request.getProcessor().getGuid());
			if (processor.getFolders() != null) {

				processor.getFolders().clear();
			}
			if (processor.getCredentials() != null) {

				processor.getCredentials().clear();
			}

			processorDTO.copyToEntity(processor,false);
			configDao.merge(processor);

			int currentExecutionOrder = processor.getExecutionOrder();
			int inputExecution = request.getProcessor().getExecutionOrder();

			if (currentExecutionOrder != inputExecution) {

				// changeExecutionOrder(request, prevExecutionOrder, currentExecutionOrder);
				// Logic for updating execution order using original Processor entity
				List<Processor> processorList = processor.getMailboxSchedProfile().getProcessors();

				// Need to add entity objects in another list because calling remove() on original list causes entity object removal

				List<Processor> procs = new ArrayList<Processor>();
				procs.addAll(processorList);

				// TODO  You need not do this since the list is retrived ordered by( look into MailBoxSchedProfile) execution order but pls test though.
				//Collections.sort(procs, new ProcessorExecutionOrderComparator());

				Processor procsrToSwap = procs.get(currentExecutionOrder - 1);
				procs.remove(currentExecutionOrder - 1);
				procs.add(inputExecution - 1, procsrToSwap);

				int index = 0;
				for (Processor procsr : procs) {
					procsr.setExecutionOrder(++index);
					configDao.merge(procsr);
				}
			}

			// response message construction	
			ProcessorDTO dto = new ProcessorDTO();
			dto.setGuid(String.valueOf(processor.getPrimaryKey()));
			serviceResponse.setResponse(new ResponseDTO(Messages.REVISED_SUCCESSFULLY,PROCESSOR,Messages.SUCCESS));			
			serviceResponse.setProcessor(dto);
			LOGGER.info("Exit from deactivate mailbox.");

		} catch (Exception e) {

			LOGGER.error(Messages.REVISE_OPERATION_FAILED.name(), e);
			serviceResponse.setResponse(new ResponseDTO(Messages.REVISE_OPERATION_FAILED, PROCESSOR,Messages.FAILURE, e.getMessage()));
			return serviceResponse;	
		}

		LOGGER.info("Exit from revising processor.");

		return serviceResponse;
	}


	//TODO REMOVE LATER IF UNUSED
	/**
	 * Method for changing the execution order using Duplicate Processor(ProcessorForMerge) entity
	 * 
	 * @param request
	 * @param prevExecutionOrder
	 * @param currentExecutionOrder
	 */
	@SuppressWarnings("unused")
	private void changeExecutionOrder(ReviseProcessorRequestDTO request,
			int prevExecutionOrder, int currentExecutionOrder) {

		String mailboxId = request.getProcessor().getLinkedMailboxId();
		String profileId = request.getProcessor().getLinkedProfileId();

		ProcessorForMergeConfigurationDAO mergeConfig = new ProcessorForMergeConfigurationDAOBase();
		List<ProcessorForMerge> processorList = mergeConfig.find(mailboxId, profileId);

		// Need to throw exception
		if (processorList.isEmpty()) {
			return;
		}

		List<ProcessorForMerge> procs = new ArrayList<ProcessorForMerge>();

		for (ProcessorForMerge p : processorList) {
			procs.add(p);
		}

		ProcessorForMerge procsrToSwap = procs.get(prevExecutionOrder - 1);
		procs.remove(prevExecutionOrder - 1);
		procs.add(currentExecutionOrder - 1, procsrToSwap);

		int i = 0;
		for (ProcessorForMerge procsr : procs) {

			procsr.setProcsrExecutionOrder(++i);
			mergeConfig.merge(procsr);
		}
	}

}
