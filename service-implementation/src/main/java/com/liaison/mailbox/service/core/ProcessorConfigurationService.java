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

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

import com.liaison.commons.jpa.DAOUtil;
import com.liaison.commons.security.pkcs7.SymmetricAlgorithmException;
import com.liaison.commons.util.client.sftp.StringUtil;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.dtdm.dao.MailBoxConfigurationDAO;
import com.liaison.mailbox.dtdm.dao.MailBoxConfigurationDAOBase;
import com.liaison.mailbox.dtdm.dao.MailboxDTDMDAO;
import com.liaison.mailbox.dtdm.dao.MailboxServiceInstanceDAO;
import com.liaison.mailbox.dtdm.dao.MailboxServiceInstanceDAOBase;
import com.liaison.mailbox.dtdm.dao.ProcessorConfigurationDAO;
import com.liaison.mailbox.dtdm.dao.ProcessorConfigurationDAOBase;
import com.liaison.mailbox.dtdm.dao.ProfileConfigurationDAO;
import com.liaison.mailbox.dtdm.dao.ProfileConfigurationDAOBase;
import com.liaison.mailbox.dtdm.dao.ServiceInstanceDAO;
import com.liaison.mailbox.dtdm.dao.ServiceInstanceDAOBase;
import com.liaison.mailbox.dtdm.model.MailBox;
import com.liaison.mailbox.dtdm.model.MailboxServiceInstance;
import com.liaison.mailbox.dtdm.model.Processor;
import com.liaison.mailbox.dtdm.model.ProcessorProperty;
import com.liaison.mailbox.dtdm.model.ScheduleProfileProcessor;
import com.liaison.mailbox.dtdm.model.ScheduleProfilesRef;
import com.liaison.mailbox.dtdm.model.ServiceInstance;
import com.liaison.mailbox.enums.EntityStatus;
import com.liaison.mailbox.enums.ExecutionEvents;
import com.liaison.mailbox.enums.ExecutionState;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.enums.ProcessorType;
import com.liaison.mailbox.enums.Protocol;
import com.liaison.mailbox.rtdm.dao.FSMStateDAO;
import com.liaison.mailbox.rtdm.dao.FSMStateDAOBase;
import com.liaison.mailbox.rtdm.dao.ProcessorExecutionStateDAO;
import com.liaison.mailbox.rtdm.dao.ProcessorExecutionStateDAOBase;
import com.liaison.mailbox.rtdm.model.FSMStateValue;
import com.liaison.mailbox.service.core.fsm.MailboxFSM;
import com.liaison.mailbox.service.core.processor.MailBoxProcessorFactory;
import com.liaison.mailbox.service.core.processor.MailBoxProcessorI;
import com.liaison.mailbox.service.dto.GenericSearchFilterDTO;
import com.liaison.mailbox.service.dto.HTTPListenerHelperDTO;
import com.liaison.mailbox.service.dto.ResponseDTO;
import com.liaison.mailbox.service.dto.configuration.DynamicPropertiesDTO;
import com.liaison.mailbox.service.dto.configuration.MailBoxDTO;
import com.liaison.mailbox.service.dto.configuration.ProcessorDTO;
import com.liaison.mailbox.service.dto.configuration.ProfileDTO;
import com.liaison.mailbox.service.dto.configuration.PropertyDTO;
import com.liaison.mailbox.service.dto.configuration.processor.properties.HTTPListenerPropertiesDTO;
import com.liaison.mailbox.service.dto.configuration.request.AddProcessorToMailboxRequestDTO;
import com.liaison.mailbox.service.dto.configuration.request.ReviseProcessorRequestDTO;
import com.liaison.mailbox.service.dto.configuration.response.AddProcessorToMailboxResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.DeActivateProcessorResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.GetProcessorResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.InterruptExecutionEventResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.ProcessorResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.ReviseProcessorResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.SearchProcessorResponseDTO;
import com.liaison.mailbox.service.dto.ui.GetExecutingProcessorDTO;
import com.liaison.mailbox.service.dto.ui.GetExecutingProcessorResponseDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.exception.ProcessorManagementFailedException;
import com.liaison.mailbox.service.util.MailBoxUtil;
import com.liaison.mailbox.service.util.ProcessorPropertyJsonMapper;
import com.liaison.mailbox.service.validation.GenericValidator;

/**
 * Class which has Processor configuration related operations.
 *
 * @author OFS
 */
public class ProcessorConfigurationService {

	private static final Logger LOGGER = LogManager.getLogger(ProcessorConfigurationService.class);
	private static final String PROCESSOR = "Processor";

	/**
	 * Creates processor for the mailbox.
	 *
	 * @param mailBoxGuid Mailbox GUID
	 * @param serviceRequest Processor request
	 * @param serviceInstanceId Service Instance Id
	 * @return AddProcessorToMailboxResponseDTO
	 */
	public AddProcessorToMailboxResponseDTO createProcessor(String mailBoxGuid,
			AddProcessorToMailboxRequestDTO serviceRequest, String serviceInstanceId) {

		AddProcessorToMailboxResponseDTO serviceResponse = new AddProcessorToMailboxResponseDTO();
		
		try {

            ProcessorDTO processorDTO = serviceRequest.getProcessor();
            if (processorDTO == null) {
                  throw new MailBoxConfigurationServicesException(Messages.INVALID_REQUEST, Response.Status.BAD_REQUEST);
            }

            LOGGER.debug("call receive to insert the processor ::{}", processorDTO);
			// check if service instance id is available in query param if not throw an exception
			if (MailBoxUtil.isEmpty(serviceInstanceId)) {
				throw new MailBoxConfigurationServicesException(Messages.SERVICE_INSTANCE_ID_NOT_AVAILABLE,
						Response.Status.BAD_REQUEST);
			}

			if (!mailBoxGuid.equals(processorDTO.getLinkedMailboxId())) {
				throw new MailBoxConfigurationServicesException(Messages.GUID_DOES_NOT_MATCH, MailBoxConstants.MAILBOX,
						Response.Status.CONFLICT);
			}

			ProcessorConfigurationDAO configDAO = new ProcessorConfigurationDAOBase();
			Processor retrievedEntity = configDAO.findProcessorByNameAndMbx(mailBoxGuid, processorDTO.getName());
			if (null != retrievedEntity) {
				throw new MailBoxConfigurationServicesException(Messages.ENTITY_ALREADY_EXIST, MailBoxConstants.MAILBOX_PROCESSOR,
						Response.Status.CONFLICT);
			}
			
			ProcessorType foundProcessorType = ProcessorType.findByName(processorDTO.getType());
            if ((ProcessorType.FILEWRITER.equals(foundProcessorType) 
                    || ProcessorType.HTTPSYNCPROCESSOR.equals(foundProcessorType) 
                    || ProcessorType.HTTPASYNCPROCESSOR.equals(foundProcessorType))
                    && !MailBoxUtil.isEmptySet(serviceRequest.getProcessor().getLinkedProfiles())) {
                throw new MailBoxConfigurationServicesException(Messages.PROCESSOR_PROFILE_NOT_ALLOWED, processorDTO.getType(),
                        Response.Status.BAD_REQUEST);
            }

			GenericValidator validator = new GenericValidator();
			validator.validate(processorDTO);

			ServiceInstanceDAO serviceInstanceDAO = new ServiceInstanceDAOBase();
			ServiceInstance serviceInstance = serviceInstanceDAO.findById(serviceInstanceId);
			if (serviceInstance == null) {
				serviceInstance = new ServiceInstance();
				serviceInstance.setName(serviceInstanceId);
				serviceInstance.setPguid(MailBoxUtil.getGUID());
				serviceInstanceDAO.persist(serviceInstance);
			}

			// Instantiate the processor and copying the values from DTO to entity.
			Processor processor = Processor.processorInstanceFactory(foundProcessorType);
			processorDTO.copyToEntity(processor, true);

			// create local folders if not available
			if (processorDTO.isCreateConfiguredLocation()) {
				MailBoxProcessorI processorService = MailBoxProcessorFactory.getInstance(processor);
				if (processorService != null) {
					processorService.createLocalPath();
				}
			}

			createMailBoxAndProcessorLink(serviceRequest, null, processor);

			createScheduleProfileAndProcessorLink(serviceRequest, null, processor);

			// adding service instance id
			processor.setServiceInstance(serviceInstance);

			// persist the processor.
			configDAO.persist(processor);

			// persist the processor execution state with status READY
			ProcessorExecutionStateDAO executionDAO = new ProcessorExecutionStateDAOBase();
			executionDAO.addProcessorExecutionState(processor.getPguid(), ExecutionState.READY.value());

			// linking mailbox and service instance id
			MailboxServiceInstanceDAO msiDao = new MailboxServiceInstanceDAOBase();
			int count = msiDao.getMailboxServiceInstanceCount(processor.getMailbox().getPguid(), serviceInstance.getPguid());

			MailBoxConfigurationDAO mailBoxConfigDAO = new MailBoxConfigurationDAOBase();
			MailBox mailBox = mailBoxConfigDAO.find(MailBox.class, processor.getMailbox().getPguid());
			if (null == mailBox) {
				throw new MailBoxConfigurationServicesException(Messages.MBX_DOES_NOT_EXIST,
						processor.getMailbox().getPguid(), Response.Status.BAD_REQUEST);
			}

			if (count == 0) {
				// Creates relationship mailbox and service instance id
				MailboxServiceInstance msi = new MailboxServiceInstance();
				msi.setPguid(MailBoxUtil.getGUID());
				msi.setServiceInstance(serviceInstance);
				msi.setMailbox(mailBox);
				msiDao.persist(msi);
			}

			serviceResponse.setResponse(new ResponseDTO(Messages.CREATED_SUCCESSFULLY, MailBoxConstants.MAILBOX_PROCESSOR, Messages.SUCCESS));
			serviceResponse.setProcessor(new ProcessorResponseDTO(String.valueOf(processor.getPrimaryKey())));
			LOGGER.debug("Exit from create processor.");
			return serviceResponse;

		} catch (MailBoxConfigurationServicesException e) {

			LOGGER.error(Messages.CREATE_OPERATION_FAILED.name(), e);
			serviceResponse.setResponse(new ResponseDTO(Messages.CREATE_OPERATION_FAILED, MailBoxConstants.MAILBOX_PROCESSOR, Messages.FAILURE,
					e.getMessage()));
			return serviceResponse;

		}

	}

	/**
	 * Creates link between scheduleProfileref and processor.
	 *
	 * @param addRequest The AddProcessorToMailboxRequest DTO
	 * @param reviseRequest The ReviseProcessorRequest DTO
	 * @param processor The processor Entity
	 */
	private void createScheduleProfileAndProcessorLink(AddProcessorToMailboxRequestDTO addRequest,
			ReviseProcessorRequestDTO reviseRequest, Processor processor) {

	    Set<String> linkedProfiles = null;
        if (null == reviseRequest) {
            linkedProfiles = addRequest.getProcessor().getLinkedProfiles();
        } else {
            linkedProfiles = reviseRequest.getProcessor().getLinkedProfiles();
        }

        Set<ScheduleProfilesRef> scheduleProfilesRef = new HashSet<>();
        if (null != linkedProfiles && !linkedProfiles.isEmpty()) {

            ProfileConfigurationDAO scheduleProfileDAO = new ProfileConfigurationDAOBase();
            ScheduleProfilesRef scheduleProfile = null;
            for (String profileName : linkedProfiles) {

                scheduleProfile = scheduleProfileDAO.findProfileByName(profileName);
                if (scheduleProfile == null) {
                    throw new MailBoxConfigurationServicesException(Messages.PROFILE_NAME_DOES_NOT_EXIST, profileName,
                            Response.Status.BAD_REQUEST);
                }

                scheduleProfilesRef.add(scheduleProfile);
            }

        }

        // Creates relationship processor and schedprofile.
        if (!scheduleProfilesRef.isEmpty()) {

            Set<ScheduleProfileProcessor> scheduleProfileProcessors = new HashSet<>();
            ScheduleProfileProcessor profileProcessor = null;
            for (ScheduleProfilesRef profile : scheduleProfilesRef) {

                profileProcessor = new ScheduleProfileProcessor();
                profileProcessor.setPguid(MailBoxUtil.getGUID());
                profileProcessor.setScheduleProfilesRef(profile);
                profileProcessor.setProcessor(processor);
                scheduleProfileProcessors.add(profileProcessor);
            }

            if (!scheduleProfileProcessors.isEmpty()) {
                processor.getScheduleProfileProcessors().addAll(scheduleProfileProcessors);
            }

        }

	}

	/**
	 * Creates link between mailbox and processor.
	 *
	 * @param addRequest The AddProcessorToMailboxRequest DTO
	 * @param reviseRequest The ReviseProcessorRequest DTO
	 * @param processor The processor Entity
	 */
	private void createMailBoxAndProcessorLink(AddProcessorToMailboxRequestDTO addRequest,
			ReviseProcessorRequestDTO reviseRequest, Processor processor) {

		String mailBoxId = null;
		if (null == reviseRequest) {
			mailBoxId = addRequest.getProcessor().getLinkedMailboxId();
		} else {
			mailBoxId = reviseRequest.getProcessor().getLinkedMailboxId();
		}

		MailBoxConfigurationDAO mailBoxConfigDAO = new MailBoxConfigurationDAOBase();
		MailBox mailBox = mailBoxConfigDAO.find(MailBox.class, mailBoxId);
		if (null == mailBox) {
			throw new MailBoxConfigurationServicesException(Messages.MBX_DOES_NOT_EXIST, mailBoxId,
					Response.Status.BAD_REQUEST);
		}
		processor.setMailbox(mailBox);

	}

	/**
	 * Get the Processor details of the mailbox using guid.
	 *
	 * @param mailBoxGuid The pguid of the mailbox
	 * @param processorGuid The pguid of the processor
	 * @return serviceResponse GetProcessorResponseDTO
	 * @throws NoSuchFieldException
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws IOException
	 */
	public GetProcessorResponseDTO getProcessor(String mailBoxGuid, String processorGuid)
			throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException, IOException {

		GetProcessorResponseDTO serviceResponse = new GetProcessorResponseDTO();

		try {

			LOGGER.debug("Entering into get processor.");
            LOGGER.debug("The retrieve guid is {} ", processorGuid);

			ProcessorConfigurationDAO config = new ProcessorConfigurationDAOBase();
			Processor processor = config.find(Processor.class, processorGuid);

			if (processor == null) {
				throw new MailBoxConfigurationServicesException(Messages.PROCESSOR_DOES_NOT_EXIST, processorGuid,
						Response.Status.BAD_REQUEST);
			}

			// validates the given processor is belongs to given mailbox
			validateProcessorBelongToMbx(mailBoxGuid, processor);

			ProcessorDTO dto = new ProcessorDTO();
			dto.copyFromEntity(processor, true);

			serviceResponse.setProcessor(dto);
			serviceResponse.setResponse(new ResponseDTO(Messages.READ_SUCCESSFUL, MailBoxConstants.MAILBOX_PROCESSOR, Messages.SUCCESS));
			LOGGER.debug("Exit from get mailbox.");
			return serviceResponse;

		} catch (MailBoxConfigurationServicesException e) {

			LOGGER.error(Messages.READ_OPERATION_FAILED.name(), e);
			serviceResponse.setResponse(new ResponseDTO(Messages.READ_OPERATION_FAILED, MailBoxConstants.MAILBOX_PROCESSOR, Messages.FAILURE,
					e.getMessage()));
			return serviceResponse;
		}
	}

	/**
	 * Deactivate the processor using guid.
	 *
	 * @param processorGuid The guid of the Processor.
	 * @return The responseDTO.
	 */
	public DeActivateProcessorResponseDTO deactivateProcessor(String mailBoxGuid, String processorGuid) {

		DeActivateProcessorResponseDTO serviceResponse = new DeActivateProcessorResponseDTO();

		try {

            LOGGER.debug("Deactivate guid is {} ", processorGuid);

			ProcessorConfigurationDAO config = new ProcessorConfigurationDAOBase();
			Processor retrievedProcessor = config.find(Processor.class, processorGuid);
			if (null == retrievedProcessor) {
				throw new MailBoxConfigurationServicesException(Messages.PROCESSOR_DOES_NOT_EXIST, processorGuid,
						Response.Status.BAD_REQUEST);
			}

			// validates the given processor is belongs to given mailbox
			validateProcessorBelongToMbx(mailBoxGuid, retrievedProcessor);

			// Changing the processor status
			retrievedProcessor.setProcsrStatus(EntityStatus.INACTIVE.value());
			config.merge(retrievedProcessor);

			// response message construction
			serviceResponse.setResponse(new ResponseDTO(Messages.DEACTIVATION_SUCCESSFUL, MailBoxConstants.MAILBOX_PROCESSOR, Messages.SUCCESS));
			serviceResponse.setProcessor(new ProcessorResponseDTO(processorGuid));
			LOGGER.debug("Exit from deactivate mailbox.");
			return serviceResponse;

		} catch (MailBoxConfigurationServicesException e) {

			LOGGER.error(Messages.DEACTIVATION_FAILED.name(), e);
			serviceResponse.setResponse(new ResponseDTO(Messages.DEACTIVATION_FAILED, MailBoxConstants.MAILBOX_PROCESSOR, Messages.FAILURE,
					e.getMessage()));
			return serviceResponse;
		}

	}

	/**
	 * Method revise the processor configuration
	 *
	 * @param request The Revise Processor Request DTO
	 * @param mailBoxId The guid of the mailbox.The given processor should belongs to the given mailbox.
	 * @param processorId The processor guid which is to be revised.
	 * @return The Revise Processor ResponseDTO
	 */
	public ReviseProcessorResponseDTO reviseProcessor(ReviseProcessorRequestDTO request, String mailBoxId, String processorId) {

	    EntityManager em = null;
        EntityTransaction tx = null;
		LOGGER.debug("Entering into revising processor.");
		ReviseProcessorResponseDTO serviceResponse = new ReviseProcessorResponseDTO();

		try {

		    ProcessorDTO processorDTO = request.getProcessor();
	        if (processorDTO == null) {
	            throw new MailBoxConfigurationServicesException(Messages.INVALID_REQUEST, Response.Status.BAD_REQUEST);
	        }

            LOGGER.debug("Request guid is {} ", processorDTO.getGuid());
			if (!mailBoxId.equals(processorDTO.getLinkedMailboxId())) {
				throw new MailBoxConfigurationServicesException(Messages.GUID_DOES_NOT_MATCH, MailBoxConstants.MAILBOX,
						Response.Status.CONFLICT);
			}

			if (!processorId.equals(processorDTO.getGuid())) {
				throw new MailBoxConfigurationServicesException(Messages.GUID_DOES_NOT_MATCH, MailBoxConstants.MAILBOX_PROCESSOR,
						Response.Status.CONFLICT);
			}

			GenericValidator validator = new GenericValidator();
			validator.validate(processorDTO);

			// validates the processor type
			ProcessorType foundProcessorType = ProcessorType.findByName(processorDTO.getType());
			if (foundProcessorType == null) {
				throw new MailBoxConfigurationServicesException(Messages.ENUM_TYPE_DOES_NOT_SUPPORT, MailBoxConstants.MAILBOX_PROCESSOR,
						Response.Status.BAD_REQUEST);
			}

			// validates the processor status
			EntityStatus foundStatusType = EntityStatus.findByName(processorDTO.getStatus());
			if (foundStatusType == null) {
				throw new MailBoxConfigurationServicesException(Messages.ENUM_TYPE_DOES_NOT_SUPPORT, MailBoxConstants.PROCESSOR_STATUS,
						Response.Status.BAD_REQUEST);
			}

			// Getting the mailbox.
			em = DAOUtil.getEntityManager(MailboxDTDMDAO.PERSISTENCE_UNIT_NAME);
            tx = em.getTransaction();
            tx.begin();

			Processor processor = em.find(Processor.class, processorDTO.getGuid());
			if (processor == null) {
				throw new MailBoxConfigurationServicesException(Messages.PROCESSOR_DOES_NOT_EXIST,
						processorDTO.getGuid(), Response.Status.BAD_REQUEST);
			}

			// validates the given processor is belongs to given mailbox
			validateProcessorBelongToMbx(mailBoxId, processor);

			if (processor.getFolders() != null) {
                processor.getFolders().clear();
            }
            if (processor.getCredentials() != null) {
                processor.getCredentials().clear();
            }
            if (processor.getScheduleProfileProcessors() != null) {
                processor.getScheduleProfileProcessors().clear();
            }
            if (processor.getDynamicProperties() != null) {
                processor.getDynamicProperties().clear();
            }
            //Flush required to avoid unique constraint violation exception
            //This is because of hibernate query execution order
            em.flush();

			createMailBoxAndProcessorLink(null, request, processor);
			createScheduleProfileAndProcessorLink(null, request, processor);

			// Copying the new details of the processor and merging.
			processorDTO.copyToEntity(processor, false);

			// create local folders if not available
			if (processorDTO.isCreateConfiguredLocation()) {
				MailBoxProcessorI processorService = MailBoxProcessorFactory.getInstance(processor);
				if (processorService != null) {
					processorService.createLocalPath();
				}
			}

			//Merge the changes and commit the transaction
			em.merge(processor);
		    tx.commit();

			// Change the execution order if existing and incoming does not match
			// changeExecutionOrder(request, configDao, processor);

			// response message construction
			ProcessorResponseDTO dto = new ProcessorResponseDTO(String.valueOf(processor.getPrimaryKey()));
			serviceResponse.setResponse(new ResponseDTO(Messages.REVISED_SUCCESSFULLY, MailBoxConstants.MAILBOX_PROCESSOR, Messages.SUCCESS));
			serviceResponse.setProcessor(dto);

		} catch (MailBoxConfigurationServicesException e) {

		    if (tx != null && tx.isActive()) {
                tx.rollback();
            }

			LOGGER.error(Messages.REVISE_OPERATION_FAILED.name(), e);
			serviceResponse.setResponse(new ResponseDTO(Messages.REVISE_OPERATION_FAILED, MailBoxConstants.MAILBOX_PROCESSOR, Messages.FAILURE,
					e.getMessage()));
			return serviceResponse;
		} catch (Exception e) {

            if (tx != null && tx.isActive()) {
                tx.rollback();
            }

            throw e;
        } finally {
		    if (em != null) {
                em.close();
            }
		}

		LOGGER.debug("Exit from revise processor.");

		return serviceResponse;
	}

	/**
	 * Method for add and update the dynamic processorProperty to Processor entity
	 *
	 * @param guid The processor guid
	 * @param propertyDTO The dynamic properties
	 */
	public void addOrUpdateProcessorProperties(String guid, DynamicPropertiesDTO propertyDTO) {

		ProcessorConfigurationDAO configDao = new ProcessorConfigurationDAOBase();
		Processor processor = configDao.find(Processor.class, guid);
		ProcessorProperty processorProperty = null;

		// update the property
		Set<ProcessorProperty> existingProperties = processor.getDynamicProperties();
		// new property from DTO
		List<PropertyDTO> newProperties = propertyDTO.getDynamicProperties();
		// new property to add entity
		Set<ProcessorProperty> processorPropertyList = new HashSet<ProcessorProperty>();

		for (PropertyDTO properties : newProperties) {

			// Add the property if empty
			if (existingProperties == null || existingProperties.isEmpty()) {

				processorProperty = getProcessorProperty(properties);
				processorPropertyList.add(processorProperty);
				processor.setDynamicProperties(processorPropertyList);

			} else {

				ProcessorProperty property = isPropertyAlreadyExists(existingProperties, properties.getName());

				// if property already exists just change the value to the new value
				if (null != property) {
					property.setProcsrPropValue(properties.getValue());
				} else { // else create a new property and add it to the existing properties
					processorProperty = getProcessorProperty(properties);
					existingProperties.add(processorProperty);
				}
			}
		}
		configDao.merge(processor);
	}

	/**
	 * Helper to construct processor property entity
	 *
	 * @param properties properties dto
	 * @return ProcessorProperty entity
     */
	private ProcessorProperty getProcessorProperty(PropertyDTO properties) {

		ProcessorProperty processorProperty = new ProcessorProperty();
		processorProperty.setPguid(MailBoxUtil.getGUID());
		processorProperty.setProcsrPropName(properties.getName());
		processorProperty.setProcsrPropValue(properties.getValue());
		return processorProperty;
	}

	/**
	 * Validates the given processor is belongs to the given mailbox.
	 *
	 * @param mailBoxGuid The guid of the mailbox
	 * @param processor The processor of the mailbox
	 */
	private void validateProcessorBelongToMbx(String mailBoxGuid, Processor processor) {

		MailBox mbx = processor.getMailbox();
		if (!mailBoxGuid.equals(mbx.getPrimaryKey())) {
			throw new MailBoxConfigurationServicesException(Messages.PROC_DOES_NOT_BELONG_TO_MBX,
					Response.Status.BAD_REQUEST);
		}
	}

	/**
	 * Get the executing processors
	 */
	public GetExecutingProcessorResponseDTO getExecutingProcessors(String status, String frmDate, String toDate) {

		GetExecutingProcessorResponseDTO serviceResponse = new GetExecutingProcessorResponseDTO();
		LOGGER.debug("Entering into getExecutingProcessors.");
		try {

			String listJobsIntervalInHours = MailBoxUtil.getEnvironmentProperties().getString(
					MailBoxConstants.DEFAULT_JOB_SEARCH_PERIOD_IN_HOURS);
			Timestamp timeStamp = new Timestamp(new Date().getTime());

			Calendar cal = Calendar.getInstance();
			cal.setTime(timeStamp);
			cal.add(Calendar.HOUR, -Integer.parseInt(listJobsIntervalInHours));
			timeStamp.setTime(cal.getTime().getTime());
			timeStamp = new Timestamp(cal.getTime().getTime());

			FSMStateDAO procDAO = new FSMStateDAOBase();

			List<FSMStateValue> listfsmStateVal = new ArrayList<FSMStateValue>();
			boolean isFrmDate = MailBoxUtil.isEmpty(frmDate);
			boolean isToDate = MailBoxUtil.isEmpty(toDate);
			boolean isStatus = MailBoxUtil.isEmpty(status);

			if ((!isFrmDate && isToDate)
					|| (isFrmDate && !isToDate)) {
				throw new ProcessorManagementFailedException(Messages.INVALID_DATE_RANGE);
			}

			if (!isStatus && ExecutionState.findByCode(status) == null) {
				throw new ProcessorManagementFailedException(Messages.INVALID_PROCESSOR_STATUS);
			}

			if (!isStatus && !isFrmDate && !isToDate) {
				listfsmStateVal = procDAO.findExecutingProcessorsByValueAndDate(status, frmDate, toDate);
			}

			if (!isStatus && isFrmDate && isToDate) {
				listfsmStateVal = procDAO.findExecutingProcessorsByValue(status, timeStamp);
			}

			if (!isFrmDate && !isToDate && isStatus) {
				listfsmStateVal = procDAO.findExecutingProcessorsByDate(frmDate, toDate);
			}

			if (isStatus && isFrmDate && isToDate) {
				listfsmStateVal = procDAO.findAllExecutingProcessors(timeStamp);
			}

			List<GetExecutingProcessorDTO> getExecutingProcessorDTOList = new ArrayList<GetExecutingProcessorDTO>();
			GetExecutingProcessorDTO getExecutingDTO = null;
			for (FSMStateValue fsmv : listfsmStateVal) {

				getExecutingDTO = new GetExecutingProcessorDTO();
				getExecutingDTO.copyFromEntity(fsmv);
				getExecutingProcessorDTOList.add(getExecutingDTO);
			}

			serviceResponse.setExecutingProcessor(getExecutingProcessorDTOList);

			if (getExecutingProcessorDTOList == null || getExecutingProcessorDTOList.isEmpty()) {
				serviceResponse.setResponse(new ResponseDTO(Messages.NO_PROCESSORS_AVAIL, MailBoxConstants.EXECUTING_PROCESSORS,
						Messages.SUCCESS));
			} else {
				serviceResponse.setResponse(new ResponseDTO(Messages.READ_SUCCESSFUL, MailBoxConstants.EXECUTING_PROCESSORS,
						Messages.SUCCESS));
			}

			LOGGER.debug("Exit from getExecutingProcessors.");
			return serviceResponse;

		} catch (ProcessorManagementFailedException e) {

			LOGGER.error(Messages.READ_OPERATION_FAILED.name(), e);
			serviceResponse.setResponse(new ResponseDTO(Messages.READ_OPERATION_FAILED, MailBoxConstants.EXECUTING_PROCESSORS,
					Messages.FAILURE, e.getMessage()));
			return serviceResponse;
		}

	}

	/**
	 *
	 * Interrupt the execution of running processor
	 *
	 * @param executionID
	 */
	public InterruptExecutionEventResponseDTO interruptRunningProcessor(String executionID) {

		LOGGER.debug("Entering into interrupt processor.");
		InterruptExecutionEventResponseDTO serviceResponse = new InterruptExecutionEventResponseDTO();

		try {

			if (StringUtil.isNullOrEmptyAfterTrim(executionID)) {
				throw new ProcessorManagementFailedException(Messages.INVALID_REQUEST);
			}


			MailboxFSM fsm = new MailboxFSM();
			LOGGER.info("Interrupt signal received for - " + executionID);

			// persisting the FSMEvent entity
			fsm.createEvent(ExecutionEvents.INTERRUPT_SIGNAL_RECIVED, executionID);

			// response message construction
			serviceResponse.setResponse(new ResponseDTO(Messages.RECEIVED_SUCCESSFULLY, MailBoxConstants.INTERRUPT_SIGNAL,
					Messages.SUCCESS));

			LOGGER.debug("Exiting from interrupt processor.");

			return serviceResponse;
		} catch (ProcessorManagementFailedException | MailBoxConfigurationServicesException e) {

			LOGGER.error(Messages.RECEIVED_OPERATION_FAILED.name(), e);
			serviceResponse.setResponse(new ResponseDTO(Messages.RECEIVED_OPERATION_FAILED, MailBoxConstants.INTERRUPT_SIGNAL,
					Messages.FAILURE, e.getMessage()));

			return serviceResponse;
		}
	}
	
	/**
	 * Method to construct processorProperty with name and value
	 * 
	 * @param name
	 * 			processor property name
	 * @param value
	 * 			processor property value
	 * @return processorProperty obj
	 */
	private ProcessorProperty constructProcessorProperty(String name, String value) {
		ProcessorProperty procsrProperty = new ProcessorProperty();
		procsrProperty.setProcsrPropName(name);
		procsrProperty.setProcsrPropValue(value);
		return procsrProperty;
	}
	
	/**
	 * Method to map received results from DB to corresponding httpListnerHelperDTO
	 * 
	 * @param receivedResults
	 * 					actual resutlSet from DB
	 * @return Map of HTTPListnerHelperDTO with processorID as key
	 */
	private Map<String, HTTPListenerHelperDTO> mapResultSet(List<Object[]> receivedResults) {
		
		Map<String, HTTPListenerHelperDTO> httpListenerDetails = new HashMap<>();
		for (Object[] obj : receivedResults) {
			
			String processorId = (String) obj[0];
			String procsrType = (String) obj[1];
			String protocol = (String) obj[2];
			String propertiesJson = (String) obj[3];
			String procsrPropName = (String) obj[4];
			String procsrPropValue = (String) obj[5];
			String serviceInstanceId = (String) obj[6];
			String mbxId = (String) obj[7];
			String mbxName = (String) obj[8];
			String tenancyKey = (String) obj[9];
			String mbxPropName = (String) obj[10];
			String mbxPropValue = (String) obj[11];
			
			// if the details are already available handle the processorProperty and mbx property alone
			HTTPListenerHelperDTO helperDTO = httpListenerDetails.get(processorId);
			if (null != helperDTO) {
				
				// handle dynamic properties of a processor
				if (!MailBoxUtil.isEmpty(procsrPropName) && !MailBoxUtil.isEmpty(procsrPropValue)) {
					ProcessorProperty procsrProperty = constructProcessorProperty(procsrPropName, procsrPropValue);
					helperDTO.getDynamicProperties().add(procsrProperty);
				}
				// handle only ttl value and ttl unit in mbx properties
				if (!MailBoxUtil.isEmpty(mbxPropName) 
								&& !MailBoxUtil.isEmpty(mbxPropValue)
								&& MailBoxUtil.isEmpty(helperDTO.getTtlValue()) 
								&& (mbxPropName.equals(MailBoxConstants.TTL))) {
					helperDTO.setTtlValue(mbxPropValue);
				}
				if (!MailBoxUtil.isEmpty(mbxPropName) 
								&& !MailBoxUtil.isEmpty(mbxPropValue)
								&& MailBoxUtil.isEmpty(helperDTO.getTtlUnit()) 
								&& (mbxPropName.equals(MailBoxConstants.TTL_UNIT))) {
					helperDTO.setTtlUnit(mbxPropValue);
				}

			} else {
				String ttlValue = null;
				String ttlUnit = null;
				// if the details are not already available construct helperDTO
				if (!MailBoxUtil.isEmpty(mbxPropName) 
							&& !MailBoxUtil.isEmpty(mbxPropValue)
							&& mbxPropName.equals(MailBoxConstants.TTL)) {
					ttlValue = mbxPropValue;
				}
				if (!MailBoxUtil.isEmpty(mbxPropName)
							&& !MailBoxUtil.isEmpty(mbxPropValue)
							&& mbxPropName.equals(MailBoxConstants.TTL_UNIT)) {
					ttlUnit = mbxPropValue;
				}
				Set<ProcessorProperty> dynamicProperties = new HashSet<>();
				if (!MailBoxUtil.isEmpty(procsrPropName) && !MailBoxUtil.isEmpty(procsrPropValue)) {
					ProcessorProperty procsrProperty = constructProcessorProperty(procsrPropName, procsrPropValue);
					dynamicProperties.add(procsrProperty);
				}
				// if the details are not already available construct a new helperDTO
				helperDTO = new HTTPListenerHelperDTO(processorId, protocol, procsrType, propertiesJson, 
														serviceInstanceId, mbxId, mbxName, tenancyKey, ttlValue, ttlUnit, dynamicProperties);
				httpListenerDetails.put(processorId, helperDTO);
			}
		}
		return httpListenerDetails;
	}
	
	/**
	 * @param httpListnerDetailsDTOs
	 * @return
	 * @throws IOException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	private Map<String, String> buildHTTPListenerProperties(Collection<HTTPListenerHelperDTO> httpListnerDetailsDTOs) 
									throws IllegalArgumentException, IllegalAccessException, IOException { 
		
		Map<String, String> httpListenerProperties = new HashMap<String, String>();
		for (HTTPListenerHelperDTO httpListenerDetail : httpListnerDetailsDTOs) {
			
			Protocol protocol = Protocol.findByCode(httpListenerDetail.getProcsrProtocol());
			ProcessorType procsrType = ProcessorType.findByCode(httpListenerDetail.getProcsrType());
			// retrieve required properties
			HTTPListenerPropertiesDTO httpListenerStaticProperties = (HTTPListenerPropertiesDTO) ProcessorPropertyJsonMapper.getProcessorBasedStaticPropsFromJson(httpListenerDetail.getProcsrPropertyJson(), protocol, procsrType, httpListenerDetail.getDynamicProperties());
			String pipeLineId = httpListenerStaticProperties.getHttpListenerPipeLineId();
			boolean securedPayload = httpListenerStaticProperties.isSecuredPayload();
			boolean authCheckRequired = httpListenerStaticProperties.isHttpListenerAuthCheckRequired();
			boolean lensVisibility = httpListenerStaticProperties.isLensVisibility();
			int connectionTimeout = httpListenerStaticProperties.getConnectionTimeout();
			
			httpListenerProperties.put(MailBoxConstants.KEY_SERVICE_INSTANCE_ID, httpListenerDetail.getServiceInstanceId());
			httpListenerProperties.put(MailBoxConstants.PROPERTY_TENANCY_KEY, httpListenerDetail.getTenancyKey());
			httpListenerProperties.put(MailBoxConstants.PROPERTY_HTTPLISTENER_SECUREDPAYLOAD, String.valueOf(securedPayload));
			httpListenerProperties.put(MailBoxConstants.PROPERTY_HTTPLISTENER_AUTH_CHECK, String.valueOf(authCheckRequired));
			httpListenerProperties.put(MailBoxConstants.KEY_MAILBOX_ID, httpListenerDetail.getMbxId());
			httpListenerProperties.put(MailBoxConstants.KEY_MAILBOX_NAME, httpListenerDetail.getMbxName());
			httpListenerProperties.put(MailBoxConstants.STORAGE_IDENTIFIER_TYPE, MailBoxUtil.getStorageType(httpListenerDetail.getDynamicProperties()));
			httpListenerProperties.put(MailBoxConstants.PROPERTY_LENS_VISIBILITY, String.valueOf(lensVisibility));
			httpListenerProperties.put(MailBoxConstants.CONNECTION_TIMEOUT, String.valueOf(connectionTimeout));
			if (!MailBoxUtil.isEmpty(httpListenerDetail.getTtlUnit()) && !MailBoxUtil.isEmpty(httpListenerDetail.getTtlValue())) {
				Integer ttlNumber = Integer.parseInt(httpListenerDetail.getTtlValue());
				httpListenerProperties.put(MailBoxConstants.TTL_IN_SECONDS, String.valueOf(MailBoxUtil.convertTTLIntoSeconds(httpListenerDetail.getTtlUnit(), ttlNumber)));
			}
			if (!MailBoxUtil.isEmpty(pipeLineId)) {
				httpListenerProperties.put(MailBoxConstants.PROPERTY_HTTPLISTENER_PIPELINEID, pipeLineId);
			}
			break;
		}
		return httpListenerProperties;
	}

	/**
	 * Method to retrieve the properties of HTTPListner of type Sync/Async
	 *
	 * @param mailboxInfo - can be mailbox pguid or mailbox Name
	 * @param httpListenerType
	 * @param isMailboxIdAvailable - specify whether the mailbox info is id or not
	 * @return a Map containing the HttpListenerSpecific Properties
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 */
	public Map<String, String> getHttpListenerProperties(String mailboxInfo, ProcessorType httpListenerType, boolean isMailboxIdAvailable)
			throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		
		long startTime = System.currentTimeMillis();
		long endTime = 0;
		Map<String, String> httpListenerProperties = null;

		// retrieve the list of processors of specific type
		ProcessorConfigurationDAO config = new ProcessorConfigurationDAOBase();
		String processorType = httpListenerType.getCode();
		List<Object[]> receivedResults = (isMailboxIdAvailable)
										? config.findProcessorsByMailboxIdAndProcessorType(mailboxInfo, processorType)
										: config.findProcessorsByMailboxNameAndProcessorType(mailboxInfo, processorType);

		if (null == receivedResults || receivedResults.isEmpty()) {
			throw new MailBoxServicesException(Messages.MISSING_PROCESSOR, httpListenerType.getCode(),
					Response.Status.NOT_FOUND);
		}
		try {

			Map<String, HTTPListenerHelperDTO> httpListenerDetails = mapResultSet(receivedResults);
			httpListenerProperties = buildHTTPListenerProperties(httpListenerDetails.values());
		} catch (IOException e) {
			LOGGER.error("unable to retrieve processor of type {} of mailbox {}", httpListenerType, mailboxInfo);
			LOGGER.error("Retrieval of processor failed", e);
			throw new RuntimeException(e);
		}
		endTime = System.currentTimeMillis();
		MailBoxUtil.calculateElapsedTime(startTime, endTime);
		return httpListenerProperties;
	}

	/**
	 * Get the Processor details of the mailbox using guid.
	 *
	 * @return The responseDTO.
	 * @throws IOException
	 * @throws JAXBException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 * @throws SymmetricAlgorithmException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 */
	public GetProcessorResponseDTO searchProcessor(GenericSearchFilterDTO searchFilter) throws NoSuchFieldException,
			SecurityException, IllegalArgumentException, IllegalAccessException, IOException, JAXBException {

		GetProcessorResponseDTO serviceResponse = new GetProcessorResponseDTO();

		try {

			LOGGER.debug("Entering into get all processors.");

			ProcessorConfigurationDAO config = new ProcessorConfigurationDAOBase();
			int totalCount = 0;
			Map<String, Integer> pageOffsetDetails = null;

			totalCount = config.getFilteredProcessorsCount(searchFilter);
			pageOffsetDetails = MailBoxUtil.getPagingOffsetDetails(searchFilter.getPage(),
					searchFilter.getPageSize(), totalCount);

			List<Processor> processors = config.getAllProcessors(searchFilter, pageOffsetDetails);

			List<ProcessorDTO> prsDTO = new ArrayList<ProcessorDTO>();
			if (null == processors || processors.isEmpty()) {
				throw new MailBoxConfigurationServicesException(Messages.NO_PROCESSORS_EXIST, Response.Status.NOT_FOUND);
			}

			ProcessorDTO processorDTO = null;
			for (Processor processor : processors) {
				processorDTO = new ProcessorDTO();
				processorDTO.copyFromEntity(processor, false);
				prsDTO.add(processorDTO);
			}
			// response message construction
			serviceResponse.setResponse(new ResponseDTO(Messages.READ_SUCCESSFUL, MailBoxConstants.MAILBOX_PROCESSOR, Messages.SUCCESS));
			serviceResponse.setTotalItems(totalCount);
			serviceResponse.setProcessors(prsDTO);

			LOGGER.debug("Exit from get all processors.");
			return serviceResponse;
		} catch (MailBoxConfigurationServicesException e) {

			LOGGER.error(Messages.READ_OPERATION_FAILED.name(), e);
			serviceResponse.setResponse(new ResponseDTO(Messages.READ_OPERATION_FAILED, MailBoxConstants.MAILBOX_PROCESSOR, Messages.FAILURE,
					e.getMessage()));
			return serviceResponse;
		}
	}

	/**
	 * Get the Mailbox names for the search processor typeahead
	 *
	 * @param searchFilter
	 * @return list of names
	 */
	public SearchProcessorResponseDTO getMailBoxNames(GenericSearchFilterDTO searchFilter) {

		SearchProcessorResponseDTO serviceResponse = new SearchProcessorResponseDTO();

		try {

			LOGGER.debug("Entering into get mailbox names.");

			ProcessorConfigurationDAO config = new ProcessorConfigurationDAOBase();

			List<MailBox> mailboxList = config.getMailboxNames(searchFilter);

			List<MailBoxDTO> mbxDTO = new ArrayList<MailBoxDTO>();
			if (null == mailboxList || mailboxList.isEmpty()) {
				throw new MailBoxConfigurationServicesException(Messages.NO_MBX_NAMES_EXIST, Response.Status.NOT_FOUND);
			}
			MailBoxDTO mailboxDTO = null;
			for (MailBox mailbox : mailboxList) {
				mailboxDTO = new MailBoxDTO();
				mailboxDTO.setName(mailbox.getMbxName());
				mbxDTO.add(mailboxDTO);
			}
			// response message construction
			serviceResponse.setResponse(new ResponseDTO(Messages.READ_SUCCESSFUL, MailBoxConstants.MAILBOX, Messages.SUCCESS));
			serviceResponse.setMailbox(mbxDTO);

			LOGGER.debug("Exit from get mailbox names.");
			return serviceResponse;
		} catch (MailBoxConfigurationServicesException e) {

			LOGGER.error(Messages.READ_OPERATION_FAILED.name(), e);
			serviceResponse.setResponse(new ResponseDTO(Messages.READ_OPERATION_FAILED, MailBoxConstants.MAILBOX, Messages.FAILURE,
					e.getMessage()));
			return serviceResponse;
		}
	}
	
	/**
	 * Get the Processor names for the search processor typeahead
	 *
	 * @param searchFilter
	 * @return list of names
	 */
	public SearchProcessorResponseDTO getProcessorNames(GenericSearchFilterDTO searchFilter) {

		SearchProcessorResponseDTO serviceResponse = new SearchProcessorResponseDTO();

		try {

			LOGGER.debug("Entering into get processor names.");

			ProcessorConfigurationDAO config = new ProcessorConfigurationDAOBase();

			List<Processor> processorList = config.getProcessorNames(searchFilter);

			List<ProcessorDTO> processorDTO = new ArrayList<ProcessorDTO>();
			if (null == processorList || processorList.isEmpty()) {
				throw new MailBoxConfigurationServicesException(Messages.NO_PROC_NAMES_EXIST, Response.Status.NOT_FOUND);
			}
			ProcessorDTO procDTO = null;
			for (Processor processor : processorList) {
				procDTO = new ProcessorDTO();
				procDTO.setName(processor.getProcsrName());
				processorDTO.add(procDTO);
			}
			// response message construction
			serviceResponse.setResponse(new ResponseDTO(Messages.READ_SUCCESSFUL, MailBoxConstants.PROCESSOR, Messages.SUCCESS));
			serviceResponse.setProcessor(processorDTO);

			LOGGER.debug("Exit from get processor names.");
			return serviceResponse;
		} catch (MailBoxConfigurationServicesException e) {

			LOGGER.error(Messages.READ_OPERATION_FAILED.name(), e);
			serviceResponse.setResponse(new ResponseDTO(Messages.READ_OPERATION_FAILED, MailBoxConstants.PROCESSOR, Messages.FAILURE,
					e.getMessage()));
			return serviceResponse;
		}
	}

	/**
	 * Get the Profile names.
	 *
	 * @param searchFilter
	 * @return list of profile names
	 */
	public SearchProcessorResponseDTO getProfileNames (GenericSearchFilterDTO searchFilter) {

		SearchProcessorResponseDTO serviceResponse = new SearchProcessorResponseDTO();

		try {

			LOGGER.debug("Entering into get profile names.");

			ProcessorConfigurationDAO config = new ProcessorConfigurationDAOBase();

			List<ScheduleProfilesRef> profiles = config.getProfileNames(searchFilter);

			List<ProfileDTO> profilesDTO = new ArrayList<ProfileDTO>();
			if (profiles == null || profiles.isEmpty()) {
				serviceResponse.setResponse(new ResponseDTO(Messages.NO_COMPONENT_EXISTS, MailBoxConstants.PROFILE, Messages.SUCCESS));
				serviceResponse.setProfiles(profilesDTO);
				return serviceResponse;
			}

			ProfileDTO profileDTO = null;
			for (ScheduleProfilesRef prof : profiles) {
				profileDTO = new ProfileDTO();
				profileDTO.setName(prof.getSchProfName());
				profilesDTO.add(profileDTO);
			}
			// response message construction
			serviceResponse.setResponse(new ResponseDTO(Messages.READ_SUCCESSFUL, MailBoxConstants.PROFILE, Messages.SUCCESS));
			serviceResponse.setProfiles(profilesDTO);

			LOGGER.debug("Exit from get profile names.");
			return serviceResponse;
		} catch (MailBoxConfigurationServicesException e) {

			LOGGER.error(Messages.READ_OPERATION_FAILED.name(), e);
			serviceResponse.setResponse(new ResponseDTO(Messages.READ_OPERATION_FAILED, MailBoxConstants.PROFILE, Messages.FAILURE,
					e.getMessage()));
			return serviceResponse;
		}
	}

	/**
	 * Method to check if a given custom property given through js already exists. if exists the existing property will
	 * be returned otherwise null will be returned
	 *
	 * @param existingProperties - existing properties in which checking has to be done
	 * @param propertyName - the property name that has to be searched
	 * @return ProcessorProperty or null
	 */
	private ProcessorProperty isPropertyAlreadyExists(Set<ProcessorProperty> existingProperties, String propertyName) {

		for (ProcessorProperty processorProperty : existingProperties) {
			if (processorProperty.getProcsrPropName().equals(propertyName)) {
				return processorProperty;
			}
		}
		return null;
	}

	/**
	 * Get the Processor details using guid.
	 *
	 * @param processorGuid The pguid of the processor
	 * @return serviceResponse GetProcessorResponseDTO
	 * @throws NoSuchFieldException
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws IOException
	 */
	public GetProcessorResponseDTO getProcessor(String processorGuid) {

		GetProcessorResponseDTO serviceResponse = new GetProcessorResponseDTO();

		try {

			LOGGER.debug("The processor guid is {}", processorGuid);
			List <Processor> processors = null;

			if (null == processorGuid) {
				throw new MailBoxConfigurationServicesException(Messages.MANDATORY_FIELD_MISSING, "Processor Id or Name",
						Response.Status.BAD_REQUEST);
			}

			ProcessorConfigurationDAO config = new ProcessorConfigurationDAOBase();
			Processor processor = config.find(Processor.class, processorGuid);
			
			// if read by guid fails try to read processor by given name
			if (null == processor) {
				processors = config.findProcessorsByName(processorGuid);
			}

			if (processor == null && processors.isEmpty()) {
				throw new MailBoxConfigurationServicesException(Messages.NO_SUCH_COMPONENT_EXISTS, PROCESSOR,
						Response.Status.BAD_REQUEST);
			}
			
			// if processor is available then it is for read processor by guid 
			if (null != processor) {
				
				ProcessorDTO dto = new ProcessorDTO();
				dto.copyFromEntity(processor, true);
				serviceResponse.setProcessor(dto);
				serviceResponse.setResponse(new ResponseDTO(Messages.READ_SUCCESSFUL, MailBoxConstants.MAILBOX_PROCESSOR, Messages.SUCCESS));
				LOGGER.debug("Exit from get processor by guid.");
				return serviceResponse;
			}
			
			// This is for retrieving list of processors by Name
			List<ProcessorDTO> processorDtos = new ArrayList<>();
			for (Processor procsr : processors) {
				ProcessorDTO dto = new ProcessorDTO();
				dto.copyFromEntity(procsr, true);
				processorDtos.add(dto);
			}
			serviceResponse.setProcessors(processorDtos);
			serviceResponse.setResponse(new ResponseDTO(Messages.READ_SUCCESSFUL, MailBoxConstants.MAILBOX_PROCESSOR, Messages.SUCCESS));
			LOGGER.debug("Exit from get processor by name.");
			return serviceResponse;

		} catch (Exception e) {

			LOGGER.error(Messages.READ_OPERATION_FAILED.name(), e);
			serviceResponse.setResponse(new ResponseDTO(Messages.READ_OPERATION_FAILED, MailBoxConstants.MAILBOX_PROCESSOR, Messages.FAILURE,
					e.getMessage()));
			return serviceResponse;
		}
	}
}
