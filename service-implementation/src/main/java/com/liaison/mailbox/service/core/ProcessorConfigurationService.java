package com.liaison.mailbox.service.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liaison.commons.security.pkcs7.SymmetricAlgorithmException;
import com.liaison.mailbox.enums.MailBoxStatus;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.enums.ProcessorType;
import com.liaison.mailbox.jpa.dao.MailBoxScheduleProfileConfigurationDAO;
import com.liaison.mailbox.jpa.dao.MailBoxScheduleProfileConfigurationDAOBase;
import com.liaison.mailbox.jpa.dao.ProcessorConfigurationDAO;
import com.liaison.mailbox.jpa.dao.ProcessorConfigurationDAOBase;
import com.liaison.mailbox.jpa.model.MailBox;
import com.liaison.mailbox.jpa.model.MailBoxSchedProfile;
import com.liaison.mailbox.jpa.model.Processor;
import com.liaison.mailbox.jpa.model.ProcessorProperty;
import com.liaison.mailbox.service.dto.ResponseDTO;
import com.liaison.mailbox.service.dto.configuration.CredentialDTO;
import com.liaison.mailbox.service.dto.configuration.DynamicPropertiesDTO;
import com.liaison.mailbox.service.dto.configuration.FolderDTO;
import com.liaison.mailbox.service.dto.configuration.ProcessorDTO;
import com.liaison.mailbox.service.dto.configuration.ProcessorPropertyDTO;
import com.liaison.mailbox.service.dto.configuration.request.AddProcessorToMailboxRequestDTO;
import com.liaison.mailbox.service.dto.configuration.request.ReviseProcessorRequestDTO;
import com.liaison.mailbox.service.dto.configuration.response.AddProcessorToMailboxResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.DeActivateProcessorResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.GetProcessorResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.ProcessorResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.ReviseProcessorResponseDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.util.MailBoxUtility;
import com.liaison.mailbox.service.util.ProcessorExecutionOrderComparator;
import com.liaison.mailbox.service.validation.GenericValidator;

/**
 * @author sivakumarg
 * 
 */
public class ProcessorConfigurationService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProcessorConfigurationService.class);
	private static String PROCESSOR = "Processor";
	private static String MAILBOX = "MailBox";
	private static final String PROCESSOR_STATUS = "Processor Status";

	private static final GenericValidator validator = new GenericValidator();

	/**
	 * Creates processor for the mailbox.
	 * 
	 * @param serviceRequest
	 *            The AddProcessorToMailboxRequestDTO
	 * @return The AddProcessorToMailboxResponseDTO
	 * @throws IOException
	 * @throws JAXBException
	 * @throws JsonMappingException
	 * @throws JsonGenerationException
	 * @throws SymmetricAlgorithmException
	 */
	public AddProcessorToMailboxResponseDTO createProcessor(String mailBoxGuid, AddProcessorToMailboxRequestDTO serviceRequest)
			throws JsonGenerationException, JsonMappingException, JAXBException, IOException, SymmetricAlgorithmException {

		LOGGER.info("call receive to insert the processor ::{}", serviceRequest.getProcessor());
		AddProcessorToMailboxResponseDTO serviceResponse = new AddProcessorToMailboxResponseDTO();

		try {

			if (!mailBoxGuid.equals(serviceRequest.getProcessor().getLinkedMailboxId())) {
				throw new MailBoxConfigurationServicesException(Messages.GUID_DOES_NOT_MATCH, MAILBOX);
			}

			ProcessorDTO processorDTO = serviceRequest.getProcessor();
			if (processorDTO == null) {
				throw new MailBoxConfigurationServicesException(Messages.INVALID_REQUEST);
			}

			validator.validate(processorDTO);
			if (null != processorDTO.getFolders()) {
				for (FolderDTO folderDTO : processorDTO.getFolders()) {
					validator.validate(folderDTO);
				}
			}
			if (null != processorDTO.getCredentials()) {
				for (CredentialDTO credentialDTO : processorDTO.getCredentials()) {
					validator.validate(credentialDTO);
				}
			}

			ProcessorType foundProcessorType = ProcessorType.findByName(serviceRequest.getProcessor().getType());
			if (foundProcessorType == null) {
				throw new MailBoxConfigurationServicesException(Messages.ENUM_TYPE_DOES_NOT_SUPPORT, PROCESSOR);
			}

			// Validation for processor status
			MailBoxStatus foundStatusType = MailBoxStatus.findByName(serviceRequest.getProcessor().getStatus());
			if (foundStatusType == null) {
				throw new MailBoxConfigurationServicesException(Messages.ENUM_TYPE_DOES_NOT_SUPPORT, PROCESSOR_STATUS);
			}

			// Instantiate the processor and copying the values from DTO to
			// entity.
			Processor processor = Processor.processorInstanceFactory(foundProcessorType);
			serviceRequest.getProcessor().copyToEntity(processor, true);
			processor.setProcsrStatus(foundStatusType.value());

			// Finding the MailBoxSchedProfile from the guid
			String linkerId = serviceRequest.getProcessor().getLinkedProfileId();
			MailBoxScheduleProfileConfigurationDAO scheduleProfileDAO = new MailBoxScheduleProfileConfigurationDAOBase();
			MailBoxSchedProfile scheduleProfile = scheduleProfileDAO.find(MailBoxSchedProfile.class, linkerId);

			if (scheduleProfile == null) {
				throw new MailBoxConfigurationServicesException(Messages.MBX_PROFILE_LINK_DOES_NOT_EXIST, linkerId);
			}

			// Sets the execution order
			if (scheduleProfile.getProcessors() == null || scheduleProfile.getProcessors().isEmpty()) {
				processor.setExecutionOrder(1);
			} else {
				processor.setExecutionOrder(scheduleProfile.getProcessors().size() + 1);
			}

			// Creates relationship processor and mailboxschedprofile.
			processor.setMailboxSchedProfile(scheduleProfile);

			// persist the processor.
			ProcessorConfigurationDAO configDAO = new ProcessorConfigurationDAOBase();
			configDAO.persist(processor);

			serviceResponse.setResponse(new ResponseDTO(Messages.CREATED_SUCCESSFULLY, PROCESSOR, Messages.SUCCESS));
			serviceResponse.setProcessor(new ProcessorResponseDTO(String.valueOf(processor.getPrimaryKey())));
			LOGGER.info("Exit from create processor.");
			return serviceResponse;

		} catch (MailBoxConfigurationServicesException e) {

			LOGGER.error(Messages.CREATE_OPERATION_FAILED.name(), e);
			serviceResponse.setResponse(new ResponseDTO(Messages.CREATE_OPERATION_FAILED, PROCESSOR, Messages.FAILURE, e
					.getMessage()));
			return serviceResponse;

		}

	}

	/**
	 * Get the Processor details of the mailbox using guid.
	 * 
	 * @param processorGuid
	 *            The guid of the Processor.
	 * @return The responseDTO.
	 * @throws IOException
	 * @throws JAXBException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 * @throws SymmetricAlgorithmException
	 */
	public GetProcessorResponseDTO getProcessor(String mailBoxGuid, String processorGuid) throws JsonParseException,
			JsonMappingException, JAXBException, IOException, SymmetricAlgorithmException {

		GetProcessorResponseDTO serviceResponse = new GetProcessorResponseDTO();

		try {

			LOGGER.info("Entering into get processor.");
			LOGGER.info("The retrieve guid is {} ", processorGuid);

			ProcessorConfigurationDAO config = new ProcessorConfigurationDAOBase();
			Processor processor = config.find(Processor.class, processorGuid);

			if (processor == null) {
				throw new MailBoxConfigurationServicesException(Messages.PROCESSOR_DOES_NOT_EXIST, processorGuid);
			}

			// validates the given processor is belongs to given mailbox
			validateProcessorBelongToMbx(mailBoxGuid, processor);

			ProcessorDTO dto = new ProcessorDTO();
			dto.copyFromEntity(processor);

			serviceResponse.setProcessor(dto);
			serviceResponse.setResponse(new ResponseDTO(Messages.READ_SUCCESSFUL, PROCESSOR, Messages.SUCCESS));
			LOGGER.info("Exit from get mailbox.");
			return serviceResponse;

		} catch (MailBoxConfigurationServicesException e) {

			LOGGER.error(Messages.READ_OPERATION_FAILED.name(), e);
			serviceResponse.setResponse(new ResponseDTO(Messages.READ_OPERATION_FAILED, PROCESSOR, Messages.FAILURE, e
					.getMessage()));
			return serviceResponse;
		}
	}

	/**
	 * Deactivate the processor using guid.
	 * 
	 * @param processorGuid
	 *            The guid of the Processor.
	 * @return The responseDTO.
	 */
	public DeActivateProcessorResponseDTO deactivateProcessor(String mailBoxGuid, String processorGuid) {

		DeActivateProcessorResponseDTO serviceResponse = new DeActivateProcessorResponseDTO();

		try {

			LOGGER.info("Entering into get processor.");
			LOGGER.info("Deactivate guid is {} ", processorGuid);

			ProcessorConfigurationDAO config = new ProcessorConfigurationDAOBase();
			Processor retrievedProcessor = config.find(Processor.class, processorGuid);
			if (null == retrievedProcessor) {
				throw new MailBoxConfigurationServicesException(Messages.PROCESSOR_DOES_NOT_EXIST, processorGuid);
			}

			// validates the given processor is belongs to given mailbox
			validateProcessorBelongToMbx(mailBoxGuid, retrievedProcessor);

			// Changing the processor status
			retrievedProcessor.setProcsrStatus(MailBoxStatus.INACTIVE.value());
			config.merge(retrievedProcessor);

			// response message construction
			serviceResponse.setResponse(new ResponseDTO(Messages.DEACTIVATION_SUCCESSFUL, PROCESSOR, Messages.SUCCESS));
			serviceResponse.setProcessor(new ProcessorResponseDTO(processorGuid));
			LOGGER.info("Exit from deactivate mailbox.");
			return serviceResponse;

		} catch (MailBoxConfigurationServicesException e) {

			LOGGER.error(Messages.DEACTIVATION_FAILED.name(), e);
			serviceResponse
					.setResponse(new ResponseDTO(Messages.DEACTIVATION_FAILED, PROCESSOR, Messages.FAILURE, e.getMessage()));
			return serviceResponse;
		}

	}

	/**
	 * Method revise the processor configuration
	 * 
	 * @param request
	 *            The Revise Processor Request DTO
	 * @param mailBoxId
	 *            The guid of the mailbox.The given processor should belongs to
	 *            the given mailbox.
	 * @param processorId
	 *            The processor guid which is to be revised.
	 * @return The Revise Processor ResponseDTO
	 * @throws IOException
	 * @throws JAXBException
	 * @throws JsonMappingException
	 * @throws JsonGenerationException
	 * @throws SymmetricAlgorithmException
	 */
	public ReviseProcessorResponseDTO reviseProcessor(ReviseProcessorRequestDTO request, String mailBoxId, String processorId)
			throws JsonGenerationException, JsonMappingException, JAXBException, IOException, SymmetricAlgorithmException {

		LOGGER.info("Entering into revising processor.");
		LOGGER.info("Request guid is {} ", request.getProcessor().getGuid());
		ReviseProcessorResponseDTO serviceResponse = new ReviseProcessorResponseDTO();

		try {

			if (!mailBoxId.equals(request.getProcessor().getLinkedMailboxId())) {
				throw new MailBoxConfigurationServicesException(Messages.GUID_DOES_NOT_MATCH, MAILBOX);
			}

			if (!processorId.equals(request.getProcessor().getGuid())) {
				throw new MailBoxConfigurationServicesException(Messages.GUID_DOES_NOT_MATCH, PROCESSOR);
			}

			ProcessorDTO processorDTO = request.getProcessor();
			if (processorDTO == null) {
				throw new MailBoxConfigurationServicesException(Messages.INVALID_REQUEST);
			}

			validator.validate(processorDTO);
			if (null != processorDTO.getFolders()) {
				for (FolderDTO folderDTO : processorDTO.getFolders()) {
					validator.validate(folderDTO);
				}
			}
			if (null != processorDTO.getCredentials()) {
				for (CredentialDTO credentialDTO : processorDTO.getCredentials()) {
					validator.validate(credentialDTO);
				}
			}

			// validates the processor type
			ProcessorType foundProcessorType = ProcessorType.findByName(processorDTO.getType());
			if (foundProcessorType == null) {
				throw new MailBoxConfigurationServicesException(Messages.ENUM_TYPE_DOES_NOT_SUPPORT, "Processor");
			}

			// validates the processor status
			MailBoxStatus foundStatusType = MailBoxStatus.findByName(processorDTO.getStatus());
			if (foundStatusType == null) {
				throw new MailBoxConfigurationServicesException(Messages.ENUM_TYPE_DOES_NOT_SUPPORT, PROCESSOR_STATUS);
			}

			ProcessorConfigurationDAO configDao = new ProcessorConfigurationDAOBase();
			Processor processor = configDao.find(Processor.class, processorDTO.getGuid());
			if (processor == null) {
				throw new MailBoxConfigurationServicesException(Messages.PROCESSOR_DOES_NOT_EXIST, processorDTO.getGuid());
			}

			// validates the given processor is belongs to given mailbox
			validateProcessorBelongToMbx(mailBoxId, processor);

			if (processor.getFolders() != null) {
				processor.getFolders().clear();
			}
			if (processor.getCredentials() != null) {
				processor.getCredentials().clear();
			}

			// Copying the new details of the processor and merging.
			processorDTO.copyToEntity(processor, false);
			configDao.merge(processor);

			// Change the execution order if existing and incoming does not
			// matche
			changeExecutionOrder(request, configDao, processor);

			// response message construction
			ProcessorResponseDTO dto = new ProcessorResponseDTO(String.valueOf(processor.getPrimaryKey()));
			serviceResponse.setResponse(new ResponseDTO(Messages.REVISED_SUCCESSFULLY, PROCESSOR, Messages.SUCCESS));
			serviceResponse.setProcessor(dto);

		} catch (MailBoxConfigurationServicesException e) {

			LOGGER.error(Messages.REVISE_OPERATION_FAILED.name(), e);
			serviceResponse.setResponse(new ResponseDTO(Messages.REVISE_OPERATION_FAILED, PROCESSOR, Messages.FAILURE, e
					.getMessage()));
			return serviceResponse;
		}

		LOGGER.info("Exit from revise processor.");

		return serviceResponse;
	}

	/**
	 * Changing the execution order using duplicate Processor entity.The change
	 * will occur when the incoming execution order does not match with the
	 * existing one.
	 * 
	 * @param request
	 *            The revise processor request DTO.
	 * @param config
	 *            The Processor Configuration DAO instance
	 * @param processor
	 *            The Current processor Entity
	 */
	private void changeExecutionOrder(ReviseProcessorRequestDTO request, ProcessorConfigurationDAO config, Processor processor) {

		int currentExecutionOrder = processor.getExecutionOrder();
		int inputExecution = request.getProcessor().getExecutionOrder();

		if (inputExecution != 0 && currentExecutionOrder != inputExecution) {

			// Logic for updating execution order using original Processor
			// entity
			List<Processor> processorList = processor.getMailboxSchedProfile().getProcessors();

			// Need to add entity objects in another list because calling
			// remove() on original
			// list causes entity object removal
			List<Processor> procs = new ArrayList<Processor>();
			procs.addAll(processorList);

			// TODO When we use order by, we are facing NPE in JPA level. Need
			// to look into
			// that.
			Collections.sort(procs, new ProcessorExecutionOrderComparator());

			// Changing the processor entity as per given one. This
			// rearrangement taken care by
			// arraylist.
			Processor procsrToSwap = procs.get(currentExecutionOrder - 1);
			procs.remove(currentExecutionOrder - 1);
			procs.add(inputExecution - 1, procsrToSwap);

			// Changing the execution order
			int index = 0;
			for (Processor procsr : procs) {
				procsr.setExecutionOrder(++index);
				config.merge(procsr);
			}
		}
	}

	/**
	 * Method for add and update the dynamic processorProperty to Processor
	 * entity
	 * 
	 * @param processor
	 *            The processor guid
	 * @param name
	 *            The property name
	 * @param value
	 *            The property value
	 */
	public void addOrUpdateProcessorProperties(String guid, DynamicPropertiesDTO propertyDTO) {

		ProcessorConfigurationDAO configDao = new ProcessorConfigurationDAOBase();
		Processor processor = configDao.find(Processor.class, guid);
		ProcessorProperty processorProperty = null;

		// update the property
		List<ProcessorProperty> existingProperties = processor.getDynamicProperties();
		// new property from DTO
		List<ProcessorPropertyDTO> newProperties = propertyDTO.getDynamicProperties();
		// new property to add entity
		List<ProcessorProperty> processorPropertyList = new ArrayList<ProcessorProperty>();

		for (ProcessorPropertyDTO properties : newProperties) {

			// Add the property if empty
			if (existingProperties == null || existingProperties.isEmpty()) {

				processorProperty = new ProcessorProperty();
				processorProperty.setPguid(MailBoxUtility.getGUID());
				processorProperty.setProcsrPropName(properties.getName());
				processorProperty.setProcsrPropValue(properties.getValue());

				processorPropertyList.add(processorProperty);
				processor.setDynamicProperties(processorPropertyList);

			} else {

				for (ProcessorProperty property : existingProperties) {

					String existingName = property.getProcsrPropName();

					// Update the property value if property name already exist
					if (existingName != null && existingName.equals(properties.getName())) {
						property.setProcsrPropValue(properties.getValue());
					} else {
						// add new property name and value
						processorProperty = new ProcessorProperty();
						processorProperty.setPguid(MailBoxUtility.getGUID());
						processorProperty.setProcsrPropName(properties.getName());
						processorProperty.setProcsrPropValue(properties.getValue());

					}
				}
				if (null != processorProperty) {
					existingProperties.add(processorProperty);
				}
			}
		}

		configDao.merge(processor);
	}

	/**
	 * Validates the given processor is belongs to the given mailbox.
	 * 
	 * @param mailBoxGuid
	 *            The guid of the mailbox
	 * @param processor
	 *            The processor of the mailbox
	 * @throws MailBoxConfigurationServicesException
	 */
	private void validateProcessorBelongToMbx(String mailBoxGuid, Processor processor)
			throws MailBoxConfigurationServicesException {

		MailBox mbx = processor.getMailboxSchedProfile().getMailbox();
		if (!mailBoxGuid.equals(mbx.getPrimaryKey())) {
			throw new MailBoxConfigurationServicesException(Messages.PROC_DOES_NOT_BELONG_TO_MBX);
		}
	}

}
