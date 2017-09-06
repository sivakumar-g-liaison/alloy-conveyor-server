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
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.commons.jpa.DAOUtil;
import com.liaison.commons.util.UUIDGen;
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
import com.liaison.mailbox.enums.ExecutionState;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.enums.ProcessorType;
import com.liaison.mailbox.enums.Protocol;
import com.liaison.mailbox.rtdm.dao.ProcessorExecutionStateDAOBase;
import com.liaison.mailbox.rtdm.dao.RuntimeProcessorsDAOBase;
import com.liaison.mailbox.rtdm.dao.StagedFileDAOBase;
import com.liaison.mailbox.service.core.fsm.ProcessorExecutionStateDTO;
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
import com.liaison.mailbox.service.dto.configuration.processor.properties.ProcessorFolderPropertyDTO;
import com.liaison.mailbox.service.dto.configuration.request.AddProcessorToMailboxRequestDTO;
import com.liaison.mailbox.service.dto.configuration.request.ReviseProcessorRequestDTO;
import com.liaison.mailbox.service.dto.configuration.response.AddProcessorToMailboxResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.ClusterTypeResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.DeActivateProcessorResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.GetProcessorIdResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.GetProcessorResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.ProcessorResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.ReviseProcessorResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.SearchProcessorResponseDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
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

	/**
	 * Creates processor for the mailbox.
	 *
	 * @param mailBoxGuid Mailbox GUID
	 * @param serviceRequest Processor request
	 * @param serviceInstanceId Service Instance Id
	 * @param userId 
	 * @return AddProcessorToMailboxResponseDTO
	 */
	public AddProcessorToMailboxResponseDTO createProcessor(String mailBoxGuid,
			AddProcessorToMailboxRequestDTO serviceRequest, String serviceInstanceId, String userId) {

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

            MailBoxConfigurationDAO mailBoxConfigDAO = new MailBoxConfigurationDAOBase();
            MailBox mailBox = mailBoxConfigDAO.find(MailBox.class, mailBoxGuid);
            if (null == mailBox) {
                throw new MailBoxConfigurationServicesException(Messages.MBX_DOES_NOT_EXIST,
                        mailBoxGuid, Response.Status.BAD_REQUEST);
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

            Protocol foundProtocolType = Protocol.findByName(processorDTO.getProtocol());
            String clusterType = (MailBoxUtil.isEmpty(processorDTO.getClusterType()))
                    ? MailBoxUtil.CLUSTER_TYPE
                    : processorDTO.getClusterType();
            if (!isValidProtocol(foundProtocolType, clusterType) || !isValidProcessorType(foundProcessorType)) {
                throw new MailBoxConfigurationServicesException(Messages.PROCESSOR_NOT_ALLOWED, MailBoxUtil.DEPLOYMENT_TYPE,
                        Response.Status.BAD_REQUEST);
            }

			ServiceInstanceDAO serviceInstanceDAO = new ServiceInstanceDAOBase();
			ServiceInstance serviceInstance = serviceInstanceDAO.findById(serviceInstanceId);
			if (serviceInstance == null) {
				serviceInstance = new ServiceInstance();
				serviceInstance.setName(serviceInstanceId);
				serviceInstance.setPguid(MailBoxUtil.getGUID());
				serviceInstance.setOriginatingDc(MailBoxUtil.DATACENTER_NAME);
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

            //Creates link between mailbox and processor.
            processor.setMailbox(mailBox);
			createScheduleProfileAndProcessorLink(serviceRequest, null, processor);

			// adding service instance id
			processor.setServiceInstance(serviceInstance);
			processor.setModifiedBy(userId);
            processor.setModifiedDate(new Timestamp(System.currentTimeMillis()));
			// persist the processor.
			configDAO.persist(processor);

			// persist the processor execution state with status READY
            ProcessorExecutionStateDTO executionDTO = new ProcessorExecutionStateDTO();
            executionDTO.setPguid(UUIDGen.getCustomUUID());
            executionDTO.setProcessorId(processor.getPguid());
            executionDTO.setExecutionStatus(ExecutionState.READY.value());
            executionDTO.setModifiedDate(new Date());
            executionDTO.setModifiedBy(userId);
            new RuntimeProcessorsDAOBase().addProcessor(executionDTO, processor.getClusterType());

			// linking mailbox and service instance id
			MailboxServiceInstanceDAO msiDao = new MailboxServiceInstanceDAOBase();
			int count = msiDao.getMailboxServiceInstanceCount(processor.getMailbox().getPguid(), serviceInstance.getPguid());

			if (count == 0) {
				// Creates relationship mailbox and service instance id
				MailboxServiceInstance msi = new MailboxServiceInstance();
				msi.setPguid(MailBoxUtil.getGUID());
				msi.setOriginatingDc(MailBoxUtil.DATACENTER_NAME);
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
                profileProcessor.setOriginatingDc(MailBoxUtil.DATACENTER_NAME);
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
	 */
	public GetProcessorResponseDTO getProcessor(String mailBoxGuid, String processorGuid) {

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
			if (!MailBoxUtil.isEmpty(mailBoxGuid)) {
			    validateProcessorBelongToMbx(mailBoxGuid, processor);
			}

			ProcessorDTO dto = new ProcessorDTO();
			dto.copyFromEntity(processor, true);

			serviceResponse.setProcessor(dto);
			serviceResponse.setResponse(new ResponseDTO(Messages.READ_SUCCESSFUL, MailBoxConstants.MAILBOX_PROCESSOR, Messages.SUCCESS));
			LOGGER.debug("Exit from get mailbox.");
			return serviceResponse;

		} catch (Exception e) {

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
     * @param userId        user login id
     * @return The responseDTO.
     */
    public DeActivateProcessorResponseDTO deactivateProcessor(String mailBoxGuid, String processorGuid, String userId) {

        DeActivateProcessorResponseDTO serviceResponse = new DeActivateProcessorResponseDTO();

        try {

            LOGGER.info("The processor guid to delete is {} and requested by the user {} ", processorGuid, userId);

            ProcessorConfigurationDAO config = new ProcessorConfigurationDAOBase();
            Processor retrievedProcessor = config.find(Processor.class, processorGuid);
            if (null == retrievedProcessor) {
                throw new MailBoxConfigurationServicesException(Messages.PROCESSOR_DOES_NOT_EXIST, processorGuid,
                        Response.Status.BAD_REQUEST);
            }
            
            //Updating the stagedFile Status as INACTIVE during deleting the corresponding processor
            new StagedFileDAOBase().updateStagedFileStatusByProcessorId(processorGuid, EntityStatus.INACTIVE.name());
            
            //Updating the ProcessorExecutionState status as COMPLETED during deleting the corresponding processor
            new ProcessorExecutionStateDAOBase().updateProcessorExecutionStateStatusByProcessorId(processorGuid, ExecutionState.COMPLETED.name());
            
            // Changing the processor status
            retrievedProcessor.setProcsrName(MailBoxUtil.generateName(retrievedProcessor.getProcsrName(), 512));
            retrievedProcessor.setProcsrStatus(EntityStatus.DELETED.value());
            retrievedProcessor.setModifiedBy(userId);
            retrievedProcessor.setModifiedDate(new Timestamp(System.currentTimeMillis()));
            config.merge(retrievedProcessor);

            // response message construction
            serviceResponse.setResponse(new ResponseDTO(Messages.DELETED_SUCCESSFULLY, MailBoxConstants.MAILBOX_PROCESSOR, Messages.SUCCESS));
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
	 * @param userId user login id
	 * @return The Revise Processor ResponseDTO
	 */
	public ReviseProcessorResponseDTO reviseProcessor(ReviseProcessorRequestDTO request, String mailBoxId, String processorId, String userId) {

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

            Protocol foundProtocolType = Protocol.findByName(processorDTO.getProtocol());
            String inputClusterType = (MailBoxUtil.isEmpty(processorDTO.getClusterType()))
                    ? MailBoxUtil.CLUSTER_TYPE
                    : processorDTO.getClusterType();
            if (!isValidProtocol(foundProtocolType, inputClusterType) || !isValidProcessorType(foundProcessorType)) {
                throw new MailBoxConfigurationServicesException(Messages.PROCESSOR_NOT_ALLOWED, MailBoxUtil.DEPLOYMENT_TYPE,
                        Response.Status.BAD_REQUEST);
            }
            
			// validates the processor status
			EntityStatus foundStatusType = EntityStatus.findByName(processorDTO.getStatus());
			if (foundStatusType == null) {
				throw new MailBoxConfigurationServicesException(Messages.ENUM_TYPE_DOES_NOT_SUPPORT, MailBoxConstants.PROCESSOR_STATUS,
						Response.Status.BAD_REQUEST);
			}

            //processor delete is not allowed in revise operation
            if (EntityStatus.DELETED.value().equals(processorDTO.getStatus())) {
                throw new MailBoxConfigurationServicesException(Messages.DELETE_OPERATION_NOT_ALLOWED, Response.Status.BAD_REQUEST);
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
			
			String clusterType = processor.getClusterType();
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

            processor.setModifiedBy(userId);
            processor.setModifiedDate(new Timestamp(System.currentTimeMillis()));
			//Merge the changes and commit the transaction
			em.merge(processor);
		    tx.commit();

			// Change the execution order if existing and incoming does not match
			// changeExecutionOrder(request, configDao, processor);
		    
		    //updates processor cluster type in the runtime processors table
            if (!inputClusterType.equals(clusterType)) {
                new RuntimeProcessorsDAOBase().updateClusterType(inputClusterType, processorId);
            }
            // response message construction
            ProcessorResponseDTO dto = new ProcessorResponseDTO(String.valueOf(processor.getPrimaryKey()));
            serviceResponse.setResponse(new ResponseDTO(Messages.REVISED_SUCCESSFULLY, MailBoxConstants.MAILBOX_PROCESSOR, Messages.SUCCESS));
            serviceResponse.setProcessor(dto);
            LOGGER.debug("Exit from revise processor.");
            return serviceResponse;

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
		processorProperty.setOriginatingDc(MailBoxUtil.DATACENTER_NAME);
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
			String procsrStatus = (String) obj[4];
			String procsrPropName = (String) obj[5];
			String procsrPropValue = (String) obj[6];
			String serviceInstanceId = (String) obj[7];
			String mbxId = (String) obj[8];
			String mbxName = (String) obj[9];
			String tenancyKey = (String) obj[10];
			String mbxStatus = (String) obj[11];
			String mbxPropName = (String) obj[12];
			String mbxPropValue = (String) obj[13];
			
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
				helperDTO = new HTTPListenerHelperDTO(processorId, protocol,
				                    procsrType, propertiesJson, procsrStatus,
				                    serviceInstanceId, mbxId, mbxName, mbxStatus,
				                    tenancyKey, ttlValue, ttlUnit, dynamicProperties);
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
			httpListenerProperties.put(MailBoxConstants.PROCSR_STATUS, httpListenerDetail.getProcsrStatus());
			httpListenerProperties.put(MailBoxConstants.MAILBOX_STATUS, httpListenerDetail.getMbxStatus());
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
            throw new RuntimeException(String.format("unable to retrieve processor of type %s of mailbox %s", httpListenerType, mailboxInfo), e);
        }
		endTime = System.currentTimeMillis();
		MailBoxUtil.calculateElapsedTime(startTime, endTime);
		return httpListenerProperties;
	}

	/**
	 * Get the Processor details of the mailbox using guid.
	 *
	 * @return The responseDTO.
	 */
	public GetProcessorResponseDTO searchProcessor(GenericSearchFilterDTO searchFilter) {

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
			    serviceResponse.setResponse(new ResponseDTO(Messages.NO_COMPONENT_EXISTS, MailBoxConstants.MAILBOX_PROCESSOR, Messages.SUCCESS));
			    serviceResponse.setProcessors(prsDTO);
			    return serviceResponse;
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
		} catch (Exception e) {

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
			    serviceResponse.setResponse(new ResponseDTO(Messages.NO_COMPONENT_EXISTS, MailBoxConstants.MAILBOX, Messages.SUCCESS));
			    serviceResponse.setMailbox(mbxDTO);
			    return serviceResponse;
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
			    serviceResponse.setResponse(new ResponseDTO(Messages.NO_COMPONENT_EXISTS, MailBoxConstants.PROCESSOR, Messages.SUCCESS));
			    serviceResponse.setProcessor(processorDTO);
			    return serviceResponse;
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
     * @param trimResponse true to trim the response
	 * @return serviceResponse GetProcessorResponseDTO
	 */
	public GetProcessorResponseDTO getProcessor(String processorGuid, boolean trimResponse) {

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
				throw new MailBoxConfigurationServicesException(Messages.NO_SUCH_COMPONENT_EXISTS, MailBoxConstants.MAILBOX_PROCESSOR,
						Response.Status.BAD_REQUEST);
			}
			
			// if processor is available then it is for read processor by guid 
			if (null != processor) {
				
				ProcessorDTO dto = new ProcessorDTO();
				dto.copyFromEntity(processor, true);

                //GMB-711
                if (trimResponse) {
                    List<ProcessorFolderPropertyDTO> folderProp = dto.getProcessorPropertiesInTemplateJson().getFolderProperties();
                    folderProp = folderProp.stream()
                            .filter(folder -> !MailBoxUtil.isEmpty(folder.getFolderURI()))
                            .collect(Collectors.toList());
                    dto.getProcessorPropertiesInTemplateJson().setFolderProperties(folderProp);
                }

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

    /**
     * Method to get the cluster type of the mailbox based on processor id.
     *
     * @param processorId pguid of the processor
     * @return clusterTypeResponseDTO
     */
    public ClusterTypeResponseDTO getClusterType(String processorId) {

        LOGGER.debug("Entering into getClusterType.");
        LOGGER.debug("The retrieve processor id is {} ", processorId);

        ClusterTypeResponseDTO clusterTypeResponseDTO = new ClusterTypeResponseDTO();
        String clusterType = null;

        if (null == processorId) {
            throw new MailBoxConfigurationServicesException(Messages.MANDATORY_FIELD_MISSING, "Processor Id",
                    Response.Status.BAD_REQUEST);
        }

        try {

            ProcessorConfigurationDAO configDao = new ProcessorConfigurationDAOBase();
            clusterType = configDao.getClusterType(processorId);

            clusterTypeResponseDTO.setClusterType(clusterType);
            clusterTypeResponseDTO.setResponse(new ResponseDTO(Messages.READ_SUCCESSFUL, MailBoxConstants.CLUSTER_TYPE, Messages.SUCCESS));
            return clusterTypeResponseDTO;

        } catch (NoResultException | MailBoxConfigurationServicesException e) {
            clusterTypeResponseDTO.setResponse(new ResponseDTO(Messages.READ_OPERATION_FAILED,
                    MailBoxConstants.CLUSTER_TYPE,
                    Messages.FAILURE,
                    e.getMessage()));
            return clusterTypeResponseDTO;
        }
    }

    /*
            * Method used to retrieve the processor Id using mailbox name processor name
     *
             * @param mbxName
     * @param processorName
     * @return serviceResponse
     */
    public GetProcessorIdResponseDTO getProcessorIdByProcNameAndMbxName(String mbxName, String processorName) {

        GetProcessorIdResponseDTO serviceResponse = new GetProcessorIdResponseDTO();
        List<String> processorGuids = new ArrayList<>();

        try {

            LOGGER.debug("The processor name is {}", processorName);
            if (MailBoxUtil.isEmpty(processorName)) {
                throw new RuntimeException("Processor name cannot be null or empty");
            }

            ProcessorConfigurationDAO config = new ProcessorConfigurationDAOBase();
            if (MailBoxUtil.isEmpty(mbxName)) {
                processorGuids = config.getProcessorIdByName(processorName);
            } else {

                LOGGER.debug("The mailbox name is {}", mbxName);
                String processorId = config.getProcessorIdByProcNameAndMbxName(mbxName, processorName);
                if (null != processorId) {
                    processorGuids.add(processorId);
                }
            }

            serviceResponse.setProcessorGuids(processorGuids);
            if (null != processorGuids && !MailBoxUtil.isEmptyList(processorGuids)) {
                serviceResponse.setResponse(new ResponseDTO(Messages.READ_SUCCESSFUL, MailBoxConstants.MAILBOX_PROCESSOR, Messages.SUCCESS));
            } else {
                serviceResponse.setResponse(new ResponseDTO(Messages.NO_COMPONENT_EXISTS, MailBoxConstants.MAILBOX_PROCESSOR, Messages.SUCCESS));
            }
            return serviceResponse;
        } catch (Exception e) {

            LOGGER.error(Messages.READ_OPERATION_FAILED.name(), e);
            serviceResponse.setResponse(new ResponseDTO(Messages.READ_OPERATION_FAILED, MailBoxConstants.MAILBOX_PROCESSOR, Messages.FAILURE,
                    e.getMessage()));
            return serviceResponse;
        }
    }

    /**
     * Method used to retrieve the processor name using processor pguid
     *
     * @param pguid pguid of the procesor
     * @return serviceResponse
     */
    public GetProcessorIdResponseDTO getProcessorNameByPguid(String pguid) {

        GetProcessorIdResponseDTO serviceResponse = new GetProcessorIdResponseDTO();
        String processorName = null;

        try {

            LOGGER.debug("The processor id is {}", pguid);
            if (MailBoxUtil.isEmpty(pguid)) {
                throw new RuntimeException("Processor id cannot be null or empty");
            }

            ProcessorConfigurationDAO config = new ProcessorConfigurationDAOBase();
            processorName = config.getProcessorNameByPguid(pguid);

            if (!MailBoxUtil.isEmpty(processorName)) {
                serviceResponse.setProcessorName(processorName);
                serviceResponse.setResponse(new ResponseDTO(Messages.READ_SUCCESSFUL, MailBoxConstants.MAILBOX_PROCESSOR, Messages.SUCCESS));
            } else {
                serviceResponse.setResponse(new ResponseDTO(Messages.NO_COMPONENT_EXISTS, MailBoxConstants.MAILBOX_PROCESSOR, Messages.FAILURE));
            }
            return serviceResponse;
        } catch (Exception e) {

            LOGGER.error(Messages.READ_OPERATION_FAILED.name(), e);
            serviceResponse.setResponse(new ResponseDTO(Messages.READ_OPERATION_FAILED, MailBoxConstants.MAILBOX_PROCESSOR, Messages.FAILURE,
                    e.getMessage()));
            return serviceResponse;
        }
    }
    
    /**
     * Helper method validate the Processor type by deployment type
     * 
     * @param processorType
     * @return boolean
     */
    private boolean isValidProcessorType(ProcessorType processorType) {

        switch (MailBoxUtil.DEPLOYMENT_TYPE) {
        
            case MailBoxConstants.RELAY:
            case MailBoxConstants.LOW_SECURE_RELAY:
                return Stream.of(ProcessorType.REMOTEDOWNLOADER,
                        ProcessorType.REMOTEUPLOADER,
                        ProcessorType.HTTPASYNCPROCESSOR,
                        ProcessorType.HTTPSYNCPROCESSOR,
                        ProcessorType.SWEEPER,
                        ProcessorType.FILEWRITER).anyMatch(s -> s.equals(processorType));
            case MailBoxConstants.CONVEYOR:
                return ProcessorType.DROPBOXPROCESSOR.equals(processorType);
            default:
                return false;
        }
    }
    
    /**
     * Helper method validate the Protocol by deployment type
     * 
     * @param protocol
     * @param clusterType
     * @return boolean
     */
    private boolean isValidProtocol(Protocol protocol, String clusterType) {
    	
    	boolean isValidProtocol;
    	boolean isValidLegacyProtocol;

        switch (MailBoxUtil.DEPLOYMENT_TYPE) {
		
            case MailBoxConstants.RELAY:
                isValidProtocol = Stream.of(Protocol.FTP,
                        Protocol.FTPS,
                        Protocol.SFTP,
                        Protocol.HTTPS,
                        Protocol.HTTP,
                        Protocol.SWEEPER,
                        Protocol.HTTPSYNCPROCESSOR,
                        Protocol.HTTPASYNCPROCESSOR,
                        Protocol.FILEWRITER).anyMatch(s -> s.equals(protocol));

                isValidLegacyProtocol = Stream.of(Protocol.FTP,
                        Protocol.FTPS,
                        Protocol.HTTP,
                        Protocol.SWEEPER,
                        Protocol.HTTPSYNCPROCESSOR,
                        Protocol.HTTPASYNCPROCESSOR,
                        Protocol.FILEWRITER).anyMatch(s -> s.equals(protocol));

                return (MailBoxConstants.SECURE.equals(clusterType) && isValidProtocol)
                        || (MailBoxConstants.LOWSECURE.equals(clusterType) && isValidLegacyProtocol);
            case MailBoxConstants.LOW_SECURE_RELAY:
                isValidProtocol = Stream.of(Protocol.FTP,
                        Protocol.FTPS,
                        Protocol.SWEEPER,
                        Protocol.HTTPSYNCPROCESSOR,
                        Protocol.HTTPASYNCPROCESSOR,
                        Protocol.FILEWRITER).anyMatch(s -> s.equals(protocol));

                return MailBoxConstants.LOWSECURE.equals(clusterType) && isValidProtocol;
            case MailBoxConstants.CONVEYOR:
        	    return MailBoxConstants.SECURE.equals(clusterType) && Protocol.DROPBOXPROCESSOR.equals(protocol);
            default:
                return false;
        }
    }
}
