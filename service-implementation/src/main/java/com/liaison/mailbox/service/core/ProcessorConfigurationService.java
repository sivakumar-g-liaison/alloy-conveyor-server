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

import com.liaison.commons.jpa.DAOUtil;
import com.liaison.commons.util.settings.LiaisonArchaiusConfiguration;
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
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.enums.ProcessorType;
import com.liaison.mailbox.enums.Protocol;
import com.liaison.mailbox.rtdm.dao.StagedFileDAOBase;
import com.liaison.mailbox.service.dto.GenericSearchFilterDTO;
import com.liaison.mailbox.service.dto.ResponseDTO;
import com.liaison.mailbox.service.dto.configuration.DynamicPropertiesDTO;
import com.liaison.mailbox.service.dto.configuration.MailBoxDTO;
import com.liaison.mailbox.service.dto.configuration.ProcessorDTO;
import com.liaison.mailbox.service.dto.configuration.ProfileDTO;
import com.liaison.mailbox.service.dto.configuration.PropertyDTO;
import com.liaison.mailbox.service.dto.configuration.processor.properties.ProcessorFolderPropertyDTO;
import com.liaison.mailbox.service.dto.configuration.request.AddProcessorToMailboxRequestDTO;
import com.liaison.mailbox.service.dto.configuration.request.ReviseProcessorDCRequestDTO;
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
import com.liaison.mailbox.service.util.MailBoxUtil;
import com.liaison.mailbox.service.validation.GenericValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.ws.rs.core.Response;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.liaison.mailbox.MailBoxConstants.ALL_DATACENTER;



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
					|| ProcessorType.LITEHTTPSYNCPROCESSOR.equals(foundProcessorType)
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
            
            // validate the protocol against processor type for the current deployment
            boolean isvalidprotocol = validateProtocolAgainstProcessorType(foundProcessorType, foundProtocolType, clusterType);
            if (!isvalidprotocol) {
                String errorMessage = String.format(Messages.INVALID_PROTOCOL_ERROR_MESSAGE.value(), foundProtocolType, foundProcessorType);
                throw new MailBoxConfigurationServicesException(errorMessage, Response.Status.BAD_REQUEST);
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
            processorDTO.copyToEntity(processor, true, validateProcessDc(processorDTO.getProcessorDC()));

            //Creates link between mailbox and processor.
            processor.setMailbox(mailBox);
			createScheduleProfileAndProcessorLink(serviceRequest, null, processor);

			// adding service instance id
			processor.setServiceInstance(serviceInstance);
			processor.setModifiedBy(userId);
            processor.setModifiedDate(new Timestamp(System.currentTimeMillis()));
			// persist the processor.
			configDAO.persist(processor);

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
     * validates processorDC from Request
     * 
     * @return process dc
     */
    private String validateProcessDc(String processDC) {
       
        if (!MailBoxUtil.isEmpty(processDC)) {
            if (processDC.equalsIgnoreCase(ALL_DATACENTER)) {
                return processDC.toUpperCase();
            } else {
                List<Object> processDcList = LiaisonArchaiusConfiguration.getInstance().getList(MailBoxConstants.PROCESS_DC_LIST);
                if (processDcList.contains(processDC.toLowerCase())) {
                    return processDC.toLowerCase();
                } else {
                    throw new MailBoxConfigurationServicesException(Messages.INVALID_PROCESS_DC, processDC, Response.Status.BAD_REQUEST);
                }
            }
        }
        return null;
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
			
			// validate whether the processor type is changed
			if (!processor.getProcessorType().toString().equals(processorDTO.getType())) {
			    throw new MailBoxConfigurationServicesException(Messages.PROCESSOR_TYPE_REVISION_NOT_ALLOWED, Response.Status.BAD_REQUEST);
			}
			
			// validate whether the protocol is changed
			if (!processor.getProcsrProtocol().equals(processorDTO.getProtocol().toLowerCase())) {
			    throw new MailBoxConfigurationServicesException(Messages.PROCESSOR_PROTOCOL_REVISION_NOT_ALLOWED, Response.Status.BAD_REQUEST);
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

            String processDC = null;
            if (!processor.getProcessDc().equalsIgnoreCase(processorDTO.getProcessorDC())) {
                processDC = validateProcessDc(processorDTO.getProcessorDC());
            } 
            // Copying the new details of the processor and merging.
            processorDTO.copyToEntity(processor, false, processDC);

            processor.setModifiedBy(userId);
            processor.setModifiedDate(new Timestamp(System.currentTimeMillis()));
			//Merge the changes and commit the transaction
			em.merge(processor);
		    tx.commit();

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
	 * Method to revise the processor dc
	 * 
	 * @param request
	 * @param userId
	 * @return The Revise Processor ResponseDTO
	 */
    public ReviseProcessorResponseDTO reviseProcessorDC(ReviseProcessorDCRequestDTO request, String userId) {
        
        LOGGER.debug("Entering into revising processor dc.");
        ReviseProcessorResponseDTO serviceResponse = new ReviseProcessorResponseDTO();

        try {

            String processorGuid = request.getProcessorGuid();
            String processorDCToUpdate = request.getProcessorDC();

            if (processorGuid == null || processorDCToUpdate == null) {
                throw new MailBoxConfigurationServicesException(Messages.INVALID_REQUEST, Response.Status.BAD_REQUEST);
            }

            if (ProcessorType.HTTPASYNCPROCESSOR.getCode().equals(request.getProcessorType())
                    || ProcessorType.HTTPSYNCPROCESSOR.getCode().equals(request.getProcessorType())
                    || ProcessorType.FILEWRITER.getCode().equals(request.getProcessorType())
                    || ProcessorType.DROPBOXPROCESSOR.getCode().equals(request.getProcessorType())) {
                throw new MailBoxConfigurationServicesException(Messages.INVALID_PROCESS_TYPE_TO_UPDATE_DC, request.getProcessorType(), Response.Status.BAD_REQUEST);
            }

            ProcessorConfigurationDAO dao = new ProcessorConfigurationDAOBase();
            dao.updateProcessDcByGuid(processorGuid, processorDCToUpdate);

            if (ProcessorType.REMOTEUPLOADER.getCode().equals(request.getProcessorType())) {
                //Update Staged File Process DC
                if (!ALL_DATACENTER.equals(processorDCToUpdate)) {
                    new StagedFileDAOBase().updateStagedFileProcessDCByProcessorGuid(Collections.singletonList(processorGuid), processorDCToUpdate);
                }
            }

            // response message construction
            ProcessorResponseDTO dto = new ProcessorResponseDTO(processorGuid);
            serviceResponse.setResponse(new ResponseDTO(Messages.REVISED_SUCCESSFULLY, MailBoxConstants.MAILBOX_PROCESSOR, Messages.SUCCESS));
            serviceResponse.setProcessor(dto);
            LOGGER.debug("Exit from revise processor.");
            return serviceResponse;

        } catch (MailBoxConfigurationServicesException e) {

            LOGGER.error(Messages.REVISE_OPERATION_FAILED.name(), e);
            serviceResponse.setResponse(new ResponseDTO(Messages.REVISE_OPERATION_FAILED, MailBoxConstants.MAILBOX_PROCESSOR, Messages.FAILURE, e.getMessage()));
            return serviceResponse;
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

			boolean includeUITemplate = !searchFilter.isMinResponse();
			ProcessorDTO processorDTO = null;
			for (Processor processor : processors) {
				processorDTO = new ProcessorDTO();
				processorDTO.copyFromEntity(processor, includeUITemplate);
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
                        ProcessorType.LITEHTTPSYNCPROCESSOR,
                        ProcessorType.HTTPASYNCPROCESSOR,
                        ProcessorType.HTTPSYNCPROCESSOR,
                        ProcessorType.SWEEPER,
                        ProcessorType.CONDITIONALSWEEPER,
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
                        Protocol.CONDITIONALSWEEPER,
                        Protocol.LITEHTTPSYNCPROCESSOR,
                        Protocol.HTTPSYNCPROCESSOR,
                        Protocol.HTTPASYNCPROCESSOR,
                        Protocol.FILEWRITER).anyMatch(s -> s.equals(protocol));

                isValidLegacyProtocol = Stream.of(Protocol.FTP,
                        Protocol.FTPS,
                        Protocol.HTTP,
                        Protocol.HTTPS,
                        Protocol.SWEEPER,
                        Protocol.CONDITIONALSWEEPER,
                        Protocol.HTTPSYNCPROCESSOR,
                        Protocol.HTTPASYNCPROCESSOR,
                        Protocol.FILEWRITER).anyMatch(s -> s.equals(protocol));

                return (MailBoxConstants.SECURE.equals(clusterType) && isValidProtocol)
                        || (MailBoxConstants.LOWSECURE.equals(clusterType) && isValidLegacyProtocol);
            case MailBoxConstants.LOW_SECURE_RELAY:
                isValidProtocol = Stream.of(Protocol.FTP,
                        Protocol.FTPS,
                        Protocol.HTTP,
                        Protocol.HTTPS,
                        Protocol.SWEEPER,
                        Protocol.CONDITIONALSWEEPER,
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

    /**
     * Helper method validate the Protocol and processor type by deployment type
     * 
     * @param processorType
     * @param protocol
     * @param clusterType
     * @return
     */
    private boolean validateProtocolAgainstProcessorType(ProcessorType processorType, Protocol protocol, String clusterType) {
        
        switch (MailBoxUtil.DEPLOYMENT_TYPE) {
        
            case MailBoxConstants.RELAY:
            case MailBoxConstants.LOW_SECURE_RELAY:
                switch (processorType.toString()) {
                   
                    case MailBoxConstants.REMOTEDOWNLOADER:                       
                    case MailBoxConstants.REMOTEUPLOADER:
                        return clusterType.equals(MailBoxConstants.SECURE) ? 
                                Stream.of(Protocol.FTP,
                                          Protocol.FTPS,
                                          Protocol.SFTP,
                                          Protocol.HTTPS,
                                          Protocol.HTTP).anyMatch(s -> s.equals(protocol)) :
                                    
                                Stream.of(Protocol.FTP,
                                          Protocol.FTPS,
                                          Protocol.HTTPS,
                                          Protocol.HTTP).anyMatch(s -> s.equals(protocol));
					case MailBoxConstants.LITEHTTPSYNCPROCESSOR:
						return Protocol.LITEHTTPSYNCPROCESSOR.equals(protocol);
					case MailBoxConstants.HTTPASYNCPROCESSOR:
                        return Protocol.HTTPASYNCPROCESSOR.equals(protocol);
                    case MailBoxConstants.HTTPSYNCPROCESSOR:
                        return Protocol.HTTPSYNCPROCESSOR.equals(protocol);
                    case MailBoxConstants.SWEEPER:
                        return Protocol.SWEEPER.equals(protocol);
                    case MailBoxConstants.CONDITIONALSWEEPER:
                        return Protocol.CONDITIONALSWEEPER.equals(protocol);
                    case MailBoxConstants.FILEWRITER:
                        return Protocol.FILEWRITER.equals(protocol);
                }
                
            case MailBoxConstants.CONVEYOR:
                return MailBoxConstants.SECURE.equals(clusterType) && Protocol.DROPBOXPROCESSOR.equals(protocol);
            default:
                return false;
        }
    }

}
