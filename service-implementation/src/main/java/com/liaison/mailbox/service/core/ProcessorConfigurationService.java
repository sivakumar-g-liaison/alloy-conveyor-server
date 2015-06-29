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
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.liaison.commons.exception.LiaisonRuntimeException;
import com.liaison.commons.security.pkcs7.SymmetricAlgorithmException;
import com.liaison.commons.util.client.sftp.StringUtil;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.dtdm.dao.MailBoxConfigurationDAO;
import com.liaison.mailbox.dtdm.dao.MailBoxConfigurationDAOBase;
import com.liaison.mailbox.dtdm.dao.MailboxServiceInstanceDAO;
import com.liaison.mailbox.dtdm.dao.MailboxServiceInstanceDAOBase;
import com.liaison.mailbox.dtdm.dao.ProcessorConfigurationDAO;
import com.liaison.mailbox.dtdm.dao.ProcessorConfigurationDAOBase;
import com.liaison.mailbox.dtdm.dao.ProfileConfigurationDAO;
import com.liaison.mailbox.dtdm.dao.ProfileConfigurationDAOBase;
import com.liaison.mailbox.dtdm.dao.ServiceInstanceDAO;
import com.liaison.mailbox.dtdm.dao.ServiceInstanceDAOBase;
import com.liaison.mailbox.dtdm.model.HTTPAsyncProcessor;
import com.liaison.mailbox.dtdm.model.HTTPSyncProcessor;
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
import com.liaison.mailbox.rtdm.dao.FSMStateDAO;
import com.liaison.mailbox.rtdm.dao.FSMStateDAOBase;
import com.liaison.mailbox.rtdm.dao.ProcessorExecutionStateDAO;
import com.liaison.mailbox.rtdm.dao.ProcessorExecutionStateDAOBase;
import com.liaison.mailbox.rtdm.model.FSMStateValue;
import com.liaison.mailbox.service.core.fsm.MailboxFSM;
import com.liaison.mailbox.service.core.processor.MailBoxProcessorFactory;
import com.liaison.mailbox.service.core.processor.MailBoxProcessorI;
import com.liaison.mailbox.service.dto.ResponseDTO;
import com.liaison.mailbox.service.dto.configuration.DynamicPropertiesDTO;
import com.liaison.mailbox.service.dto.configuration.ProcessorDTO;
import com.liaison.mailbox.service.dto.configuration.PropertyDTO;
import com.liaison.mailbox.service.dto.configuration.TrustStoreDTO;
import com.liaison.mailbox.service.dto.configuration.processor.properties.HTTPListenerPropertiesDTO;
import com.liaison.mailbox.service.dto.configuration.request.AddProcessorToMailboxRequestDTO;
import com.liaison.mailbox.service.dto.configuration.request.ReviseProcessorRequestDTO;
import com.liaison.mailbox.service.dto.configuration.response.AddProcessorToMailboxResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.DeActivateProcessorResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.GetProcessorResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.GetTrustStoreResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.InterruptExecutionEventResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.ProcessorResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.ReviseProcessorResponseDTO;
import com.liaison.mailbox.service.dto.ui.GetExecutingProcessorDTO;
import com.liaison.mailbox.service.dto.ui.GetExecutingProcessorResponseDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.exception.ProcessorManagementFailedException;
import com.liaison.mailbox.service.util.KMSUtil;
import com.liaison.mailbox.service.util.MailBoxUtil;
import com.liaison.mailbox.service.util.ProcessorPropertyJsonMapper;
import com.liaison.mailbox.service.validation.GenericValidator;

/**
 * @author OFS
 *
 */
public class ProcessorConfigurationService {

	private static final Logger LOGGER = LogManager.getLogger(ProcessorConfigurationService.class);
	private static String PROCESSOR = "Processor";
	private static String TRUSTSTORE = "TrustStore";
	private static String MAILBOX = "MailBox";
	private static final String PROCESSOR_STATUS = "Processor Status";
	private static String INTERRUPT_SIGNAL = "Interrupt Signal";
	private static String EXECUTING_PROCESSORS = "Executing Processors";


	/**
	 * Creates processor for the mailbox.
	 *
	 * @param serviceRequest The AddProcessorToMailboxRequestDTO
	 * @return The AddProcessorToMailboxResponseDTO
	 * @throws IOException
	 * @throws JAXBException
	 * @throws JsonMappingException
	 * @throws JsonGenerationException
	 * @throws SymmetricAlgorithmException
	 * @throws JSONException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 */
	public AddProcessorToMailboxResponseDTO createProcessor(String mailBoxGuid,
			AddProcessorToMailboxRequestDTO serviceRequest, String serviceInstanceId)
			throws JsonGenerationException, JsonMappingException, JAXBException, IOException,
			SymmetricAlgorithmException, JSONException, NoSuchFieldException, SecurityException,
			IllegalArgumentException, IllegalAccessException {

		LOGGER.debug("call receive to insert the processor ::{}", serviceRequest.getProcessor());
		AddProcessorToMailboxResponseDTO serviceResponse = new AddProcessorToMailboxResponseDTO();

		try {

			// check if service instance id is available in query param if not throw an exception
			if (MailBoxUtil.isEmpty(serviceInstanceId)) {
				throw new MailBoxConfigurationServicesException(Messages.SERVICE_INSTANCE_ID_NOT_AVAILABLE,
						Response.Status.BAD_REQUEST);
			}

			if (!mailBoxGuid.equals(serviceRequest.getProcessor().getLinkedMailboxId())) {
				throw new MailBoxConfigurationServicesException(Messages.GUID_DOES_NOT_MATCH, MAILBOX,
						Response.Status.CONFLICT);
			}

			ProcessorDTO processorDTO = serviceRequest.getProcessor();
			if (processorDTO == null) {
				throw new MailBoxConfigurationServicesException(Messages.INVALID_REQUEST, Response.Status.BAD_REQUEST);
			}
			ProcessorConfigurationDAO configDAO = new ProcessorConfigurationDAOBase();
			Processor retrievedEntity = configDAO.findProcessorByNameAndMbx(mailBoxGuid, processorDTO.getName());
			if (null != retrievedEntity) {
				throw new MailBoxConfigurationServicesException(Messages.ENTITY_ALREADY_EXIST, PROCESSOR,
						Response.Status.CONFLICT);
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
			ProcessorType foundProcessorType = ProcessorType.findByName(serviceRequest.getProcessor().getType());
			Processor processor = Processor.processorInstanceFactory(foundProcessorType);
			serviceRequest.getProcessor().copyToEntity(processor, true);

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
			MailboxServiceInstance mailboxServiceInstance = msiDao.findByGuids(processor.getMailbox().getPguid(),
					serviceInstance.getPguid());

			MailBoxConfigurationDAO mailBoxConfigDAO = new MailBoxConfigurationDAOBase();
			MailBox mailBox = mailBoxConfigDAO.find(MailBox.class, processor.getMailbox().getPguid());
			if (null == mailBox) {
				throw new MailBoxConfigurationServicesException(Messages.MBX_DOES_NOT_EXIST,
						processor.getMailbox().getPguid(), Response.Status.BAD_REQUEST);
			}

			if (mailboxServiceInstance == null) {
				// Creates relationship mailbox and service instance id
				MailboxServiceInstance msi = new MailboxServiceInstance();
				msi.setPguid(MailBoxUtil.getGUID());
				msi.setServiceInstance(serviceInstance);
				msi.setMailbox(mailBox);
				msiDao.persist(msi);
			}

			serviceResponse.setResponse(new ResponseDTO(Messages.CREATED_SUCCESSFULLY, PROCESSOR, Messages.SUCCESS));
			serviceResponse.setProcessor(new ProcessorResponseDTO(String.valueOf(processor.getPrimaryKey())));
			LOGGER.debug("Exit from create processor.");
			return serviceResponse;

		} catch (MailBoxConfigurationServicesException e) {

			LOGGER.error(Messages.CREATE_OPERATION_FAILED.name(), e);
			serviceResponse.setResponse(new ResponseDTO(Messages.CREATE_OPERATION_FAILED, PROCESSOR, Messages.FAILURE,
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
	 * @throws MailBoxConfigurationServicesException
	 */
	private void createScheduleProfileAndProcessorLink(AddProcessorToMailboxRequestDTO addRequest,
			ReviseProcessorRequestDTO reviseRequest, Processor processor)
			throws MailBoxConfigurationServicesException {

		List<String> linkedProfiles = null;
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

			List<ScheduleProfileProcessor> scheduleProfileProcessors = new ArrayList<>();
			ScheduleProfileProcessor profileProcessor = null;
			for (ScheduleProfilesRef profile : scheduleProfilesRef) {

				profileProcessor = new ScheduleProfileProcessor();
				profileProcessor.setPguid(MailBoxUtil.getGUID());
				profileProcessor.setScheduleProfilesRef(profile);
				scheduleProfileProcessors.add(profileProcessor);
			}

			if (!scheduleProfileProcessors.isEmpty()) {
				processor.setScheduleProfileProcessors(scheduleProfileProcessors);
			}

		}
	}

	/**
	 * Creates link between mailbox and processor.
	 *
	 * @param addRequest The AddProcessorToMailboxRequest DTO
	 * @param reviseRequest The ReviseProcessorRequest DTO
	 * @param processor The processor Entity
	 * @throws MailBoxConfigurationServicesException
	 */
	private void createMailBoxAndProcessorLink(AddProcessorToMailboxRequestDTO addRequest,
			ReviseProcessorRequestDTO reviseRequest, Processor processor)
			throws MailBoxConfigurationServicesException {

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
	 * @param processorGuid The guid of the Processor.
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
	public GetProcessorResponseDTO getProcessor(String mailBoxGuid, String processorGuid)
			throws JsonParseException, JsonMappingException, JAXBException, IOException, SymmetricAlgorithmException,
			NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {

		GetProcessorResponseDTO serviceResponse = new GetProcessorResponseDTO();

		try {

			LOGGER.debug("Entering into get processor.");
			LOGGER.info("The retrieve guid is {} ", processorGuid);

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
			serviceResponse.setResponse(new ResponseDTO(Messages.READ_SUCCESSFUL, PROCESSOR, Messages.SUCCESS));
			LOGGER.debug("Exit from get mailbox.");
			return serviceResponse;

		} catch (MailBoxConfigurationServicesException e) {

			LOGGER.error(Messages.READ_OPERATION_FAILED.name(), e);
			serviceResponse.setResponse(new ResponseDTO(Messages.READ_OPERATION_FAILED, PROCESSOR, Messages.FAILURE,
					e.getMessage()));
			return serviceResponse;
		}
	}

	/**
	 * Get the TrustStoreResponse.
	 *
	 * @return GetTrustStoreResponseDTO
	 * @throws MailBoxConfigurationServicesException
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws JSONException
	 */
	public GetTrustStoreResponseDTO uploadSelfSignedTrustStore()
			throws MailBoxConfigurationServicesException, ClientProtocolException, IOException, JSONException {

		GetTrustStoreResponseDTO serviceResponse = new GetTrustStoreResponseDTO();

		try {

			HttpResponse response = KMSUtil.uploadSelfSignedTrustStoreCertificate();

			if ((response.getStatusLine().getStatusCode() >= 200) && (response.getStatusLine().getStatusCode() < 300)) {

				JSONObject obj = new JSONObject(EntityUtils.toString(response.getEntity()));

				// Setting TrustStore ID
				JSONArray arr = obj.getJSONObject("dataTransferObject").getJSONArray("trustStores");
				String trustStoreId = (((JSONObject) arr.get(0)).getString("pguid"));
				TrustStoreDTO dto = new TrustStoreDTO();
				dto.setTrustStoreId(trustStoreId);

				// Setting TrustStore Group ID
				dto.setTrustStoreGroupId(obj.getJSONObject("dataTransferObject").getString("pguid"));

				serviceResponse.setTrustStore(dto);
				serviceResponse.setResponse(new ResponseDTO(Messages.CREATED_SUCCESSFULLY, TRUSTSTORE, Messages.SUCCESS));
				LOGGER.debug("Exit from get mailbox.");
				return serviceResponse;

			} else {
				throw new MailBoxConfigurationServicesException(Messages.SELFSIGNED_TRUSTSTORE_CREATION_FAILED,
						Response.Status.BAD_REQUEST);
			}

		} catch (MailBoxConfigurationServicesException e) {

			LOGGER.error(Messages.CREATE_OPERATION_FAILED.name(), e);
			serviceResponse.setResponse(new ResponseDTO(Messages.CREATE_OPERATION_FAILED, TRUSTSTORE, Messages.FAILURE,
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

			LOGGER.debug("Entering into get processor.");
			LOGGER.info("Deactivate guid is {} ", processorGuid);

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
			serviceResponse.setResponse(new ResponseDTO(Messages.DEACTIVATION_SUCCESSFUL, PROCESSOR, Messages.SUCCESS));
			serviceResponse.setProcessor(new ProcessorResponseDTO(processorGuid));
			LOGGER.debug("Exit from deactivate mailbox.");
			return serviceResponse;

		} catch (MailBoxConfigurationServicesException e) {

			LOGGER.error(Messages.DEACTIVATION_FAILED.name(), e);
			serviceResponse.setResponse(new ResponseDTO(Messages.DEACTIVATION_FAILED, PROCESSOR, Messages.FAILURE,
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
	 * @throws IOException
	 * @throws JAXBException
	 * @throws JsonMappingException
	 * @throws JsonGenerationException
	 * @throws SymmetricAlgorithmException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 */
	public ReviseProcessorResponseDTO reviseProcessor(ReviseProcessorRequestDTO request, String mailBoxId,
			String processorId)
			throws JsonGenerationException, JsonMappingException, JAXBException, IOException,
			SymmetricAlgorithmException, NoSuchFieldException, SecurityException, IllegalArgumentException,
			IllegalAccessException {

		LOGGER.debug("Entering into revising processor.");
		LOGGER.info("Request guid is {} ", request.getProcessor().getGuid());
		ReviseProcessorResponseDTO serviceResponse = new ReviseProcessorResponseDTO();

		try {

			if (!mailBoxId.equals(request.getProcessor().getLinkedMailboxId())) {
				throw new MailBoxConfigurationServicesException(Messages.GUID_DOES_NOT_MATCH, MAILBOX,
						Response.Status.CONFLICT);
			}

			if (!processorId.equals(request.getProcessor().getGuid())) {
				throw new MailBoxConfigurationServicesException(Messages.GUID_DOES_NOT_MATCH, PROCESSOR,
						Response.Status.CONFLICT);
			}

			ProcessorDTO processorDTO = request.getProcessor();
			if (processorDTO == null) {
				throw new MailBoxConfigurationServicesException(Messages.INVALID_REQUEST, Response.Status.BAD_REQUEST);
			}

			GenericValidator validator = new GenericValidator();
			validator.validate(processorDTO);

			// validates the processor type
			ProcessorType foundProcessorType = ProcessorType.findByName(processorDTO.getType());
			if (foundProcessorType == null) {
				throw new MailBoxConfigurationServicesException(Messages.ENUM_TYPE_DOES_NOT_SUPPORT, "Processor",
						Response.Status.BAD_REQUEST);
			}

			// validates the processor status
			EntityStatus foundStatusType = EntityStatus.findByName(processorDTO.getStatus());
			if (foundStatusType == null) {
				throw new MailBoxConfigurationServicesException(Messages.ENUM_TYPE_DOES_NOT_SUPPORT, PROCESSOR_STATUS,
						Response.Status.BAD_REQUEST);
			}

			ProcessorConfigurationDAO configDao = new ProcessorConfigurationDAOBase();
			Processor processor = configDao.find(Processor.class, processorDTO.getGuid());
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

			configDao.merge(processor);

			// Change the execution order if existing and incoming does not
			// matche
			// changeExecutionOrder(request, configDao, processor);

			// response message construction
			ProcessorResponseDTO dto = new ProcessorResponseDTO(String.valueOf(processor.getPrimaryKey()));
			serviceResponse.setResponse(new ResponseDTO(Messages.REVISED_SUCCESSFULLY, PROCESSOR, Messages.SUCCESS));
			serviceResponse.setProcessor(dto);

		} catch (MailBoxConfigurationServicesException e) {

			LOGGER.error(Messages.REVISE_OPERATION_FAILED.name(), e);
			serviceResponse.setResponse(new ResponseDTO(Messages.REVISE_OPERATION_FAILED, PROCESSOR, Messages.FAILURE,
					e.getMessage()));
			return serviceResponse;
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
		List<ProcessorProperty> existingProperties = processor.getDynamicProperties();
		// new property from DTO
		List<PropertyDTO> newProperties = propertyDTO.getDynamicProperties();
		// new property to add entity
		List<ProcessorProperty> processorPropertyList = new ArrayList<ProcessorProperty>();

		for (PropertyDTO properties : newProperties) {

			// Add the property if empty
			if (existingProperties == null || existingProperties.isEmpty()) {

				processorProperty = new ProcessorProperty();
				processorProperty.setPguid(MailBoxUtil.getGUID());
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
						processorProperty.setPguid(MailBoxUtil.getGUID());
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
	 * @param mailBoxGuid The guid of the mailbox
	 * @param processor The processor of the mailbox
	 * @throws MailBoxConfigurationServicesException
	 */
	private void validateProcessorBelongToMbx(String mailBoxGuid, Processor processor)
			throws MailBoxConfigurationServicesException {

		MailBox mbx = processor.getMailbox();
		if (!mailBoxGuid.equals(mbx.getPrimaryKey())) {
			throw new MailBoxConfigurationServicesException(Messages.PROC_DOES_NOT_BELONG_TO_MBX,
					Response.Status.BAD_REQUEST);
		}
	}

	/**
	 * Get the executing processors
	 *
	 * @throws MailBoxConfigurationServicesException
	 * @throws IOException
	 */
	public GetExecutingProcessorResponseDTO getExecutingProcessors(String status, String frmDate, String toDate)
			throws IOException {

		GetExecutingProcessorResponseDTO serviceResponse = new GetExecutingProcessorResponseDTO();
		LOGGER.debug("Entering into getExecutingProcessors.");
		try {

			String listJobsIntervalInHours = MailBoxUtil.getEnvironmentProperties().getString(
					MailBoxConstants.DEFAULT_JOB_SEARCH_PERIOD_IN_HOURS);
			Timestamp timeStmp = new Timestamp(new Date().getTime());

			Calendar cal = Calendar.getInstance();
			cal.setTime(timeStmp);
			cal.add(Calendar.HOUR, -Integer.parseInt(listJobsIntervalInHours));
			timeStmp.setTime(cal.getTime().getTime());
			timeStmp = new Timestamp(cal.getTime().getTime());

			FSMStateDAO procDAO = new FSMStateDAOBase();

			List<FSMStateValue> listfsmStateVal = new ArrayList<FSMStateValue>();

			if ((!MailBoxUtil.isEmpty(frmDate) && MailBoxUtil.isEmpty(toDate))
					|| (MailBoxUtil.isEmpty(frmDate) && !MailBoxUtil.isEmpty(toDate))) {
				throw new ProcessorManagementFailedException(Messages.INVALID_DATE_RANGE);
			}

			if (!MailBoxUtil.isEmpty(status) && ExecutionState.findByCode(status) == null) {
				throw new ProcessorManagementFailedException(Messages.INVALID_PROCESSOR_STATUS);
			}

			if (!MailBoxUtil.isEmpty(status) && !MailBoxUtil.isEmpty(frmDate) && !MailBoxUtil.isEmpty(toDate)) {
				listfsmStateVal = procDAO.findExecutingProcessorsByValueAndDate(status, frmDate, toDate);
			}

			if (!MailBoxUtil.isEmpty(status) && MailBoxUtil.isEmpty(frmDate) && MailBoxUtil.isEmpty(toDate)) {
				listfsmStateVal = procDAO.findExecutingProcessorsByValue(status, timeStmp);
			}

			if (!MailBoxUtil.isEmpty(frmDate) && !MailBoxUtil.isEmpty(toDate) && MailBoxUtil.isEmpty(status)) {
				listfsmStateVal = procDAO.findExecutingProcessorsByDate(frmDate, toDate);
			}

			if (MailBoxUtil.isEmpty(status) && MailBoxUtil.isEmpty(frmDate) && MailBoxUtil.isEmpty(toDate)) {
				listfsmStateVal = procDAO.findAllExecutingProcessors(timeStmp);
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
				serviceResponse.setResponse(new ResponseDTO(Messages.NO_PROCESSORS_AVAIL, EXECUTING_PROCESSORS,
						Messages.SUCCESS));
			} else {
				serviceResponse.setResponse(new ResponseDTO(Messages.READ_SUCCESSFUL, EXECUTING_PROCESSORS,
						Messages.SUCCESS));
			}

			LOGGER.debug("Exit from getExecutingProcessors.");
			return serviceResponse;

		} catch (ProcessorManagementFailedException e) {

			LOGGER.error(Messages.READ_OPERATION_FAILED.name(), e);
			serviceResponse.setResponse(new ResponseDTO(Messages.READ_OPERATION_FAILED, EXECUTING_PROCESSORS,
					Messages.FAILURE, e.getMessage()));
			return serviceResponse;
		}

	}

	/**
	 *
	 * Interrupt the execution of running processor
	 *
	 * @param executionID
	 * @throws MailBoxConfigurationServicesException
	 *
	 */
	public InterruptExecutionEventResponseDTO interruptRunningProcessor(String executionID)
			throws MailBoxConfigurationServicesException {

		LOGGER.debug("Entering into interrupt processor.");
		InterruptExecutionEventResponseDTO serviceResponse = new InterruptExecutionEventResponseDTO();

		try {

			if (StringUtil.isNullOrEmptyAfterTrim(executionID)) {
				throw new ProcessorManagementFailedException(Messages.INVALID_REQUEST);
			}


			MailboxFSM fsm = new MailboxFSM();
			LOGGER.info("############################################################################");
			LOGGER.info("Interrupt signal received for   " + executionID);
			LOGGER.info("#############################################################################");

			// persisting the FSMEvent entity
			fsm.createEvent(ExecutionEvents.INTERRUPT_SIGNAL_RECIVED, executionID);

			// response message construction
			serviceResponse.setResponse(new ResponseDTO(Messages.RECEIVED_SUCCESSFULLY, INTERRUPT_SIGNAL,
					Messages.SUCCESS));

			LOGGER.debug("Exiting from interrupt processor.");

			return serviceResponse;
		} catch (ProcessorManagementFailedException e) {

			LOGGER.error(Messages.RECEIVED_OPERATION_FAILED.name(), e);
			serviceResponse.setResponse(new ResponseDTO(Messages.RECEIVED_OPERATION_FAILED, INTERRUPT_SIGNAL,
					Messages.FAILURE, e.getMessage()));

			return serviceResponse;
		} catch (MailBoxConfigurationServicesException e) {

			LOGGER.error(Messages.RECEIVED_OPERATION_FAILED.name(), e);
			serviceResponse.setResponse(new ResponseDTO(Messages.RECEIVED_OPERATION_FAILED, INTERRUPT_SIGNAL,
					Messages.FAILURE, e.getMessage()));

			return serviceResponse;
		}
	}

	/**
	 * Method to retrieve the properties of HTTPListner of type Sync/Async
	 *
	 * @param mailboxGuid
	 * @param httpListenerType
	 * @return a Map containing the HttpListenerSpecific Properties
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 * @throws MailBoxConfigurationServicesException
	 */
	public Map<String, String> getHttpListenerProperties(String mailboxGuid, ProcessorType httpListenerType)
			throws MailBoxConfigurationServicesException, NoSuchFieldException, SecurityException,
			IllegalArgumentException, IllegalAccessException {

		Map<String, String> httpListenerProperties = new HashMap<String, String>();

		// retrieve the list of processors of specific type
		ProcessorConfigurationDAO config = new ProcessorConfigurationDAOBase();
		List<Processor> processors = config.findProcessorByMbx(mailboxGuid, true);

		if (processors.isEmpty()) {
			throw new MailBoxServicesException(Messages.MISSING_PROCESSOR, httpListenerType.getCode(),
					Response.Status.NOT_FOUND);
		}

		try {

			ProcessorDTO processorDTO = null;

			for (Processor processor : processors) {

				if (((processor instanceof HTTPSyncProcessor) && (httpListenerType.getCode().equals(ProcessorType.HTTPSYNCPROCESSOR.getCode())))
						|| ((processor instanceof HTTPAsyncProcessor) && (httpListenerType.getCode().equals(ProcessorType.HTTPASYNCPROCESSOR.getCode())))) {
					processorDTO = new ProcessorDTO();
					processorDTO.copyFromEntity(processor, false);
				}

				if (null != processorDTO) {

					// retrieve required properties
					HTTPListenerPropertiesDTO httpListenerStaticProperties = (HTTPListenerPropertiesDTO) ProcessorPropertyJsonMapper.getProcessorBasedStaticPropsFromJson(
							processor.getProcsrProperties(), processor);

					String pipeLineId = httpListenerStaticProperties.getHttpListenerPipeLineId();
					boolean securedPayload = httpListenerStaticProperties.isSecuredPayload();
					boolean authCheckRequired = httpListenerStaticProperties.isHttpListenerAuthCheckRequired();

					httpListenerProperties.put(MailBoxConstants.KEY_SERVICE_INSTANCE_ID,
							processor.getServiceInstance().getName());
					// Commented by Veera -Not needed, because tenancy key format has changed as per service broker
					// httpListenerProperties.put(MailBoxConstants.KEY_TENANCY_KEY,
					// processor.getMailbox().getTenancyKey());
					httpListenerProperties.put(MailBoxConstants.PROPERTY_HTTPLISTENER_SECUREDPAYLOAD,
							String.valueOf(securedPayload));
					httpListenerProperties.put(MailBoxConstants.PROPERTY_HTTPLISTENER_AUTH_CHECK,
							String.valueOf(authCheckRequired));

					if (!MailBoxUtil.isEmpty(pipeLineId))
						httpListenerProperties.put(MailBoxConstants.PROPERTY_HTTPLISTENER_PIPELINEID, pipeLineId);
					break;
				}

			}

			if (null == processorDTO) {
				throw new MailBoxServicesException(Messages.MISSING_PROCESSOR, httpListenerType.getCode(),
						Response.Status.NOT_FOUND);
			}

		} catch (JAXBException | IOException | SymmetricAlgorithmException e) {
			LOGGER.error("unable to retrieve processor of type {} of mailbox {}", httpListenerType, mailboxGuid);
			LOGGER.error("Retrieval of processor failed", e);
			throw new RuntimeException(e);
		}


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
	public GetProcessorResponseDTO getAllProcessors()
			throws JsonParseException, JsonMappingException, JAXBException, IOException, SymmetricAlgorithmException,
			NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException  {

		GetProcessorResponseDTO serviceResponse = new GetProcessorResponseDTO();

		try {

			LOGGER.debug("Entering into get all processors.");			

			ProcessorConfigurationDAO config = new ProcessorConfigurationDAOBase();
			List<Processor> processors = config.getAllProcessors();
			
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
			serviceResponse.setResponse(new ResponseDTO(Messages.READ_SUCCESSFUL, PROCESSOR, Messages.SUCCESS));
			serviceResponse.setProcessors(prsDTO);
			
			LOGGER.debug("Exit from get all processors.");
			return serviceResponse;
		} catch (MailBoxConfigurationServicesException e) {

			LOGGER.error(Messages.READ_OPERATION_FAILED.name(), e);
			serviceResponse.setResponse(new ResponseDTO(Messages.READ_OPERATION_FAILED, PROCESSOR, Messages.FAILURE,
					e.getMessage()));
			return serviceResponse;
		} 
	}
}
