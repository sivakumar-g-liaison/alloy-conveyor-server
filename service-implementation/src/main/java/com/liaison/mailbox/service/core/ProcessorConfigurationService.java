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

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.liaison.commons.security.pkcs12.SymmetricAlgorithmException;
import com.liaison.framework.util.ServiceUtils;
import com.liaison.mailbox.enums.ExecutionEvents;
import com.liaison.mailbox.enums.ExecutionState;
import com.liaison.mailbox.enums.MailBoxStatus;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.enums.ProcessorType;
import com.liaison.mailbox.jpa.dao.FSMStateDAO;
import com.liaison.mailbox.jpa.dao.FSMStateDAOBase;
import com.liaison.mailbox.jpa.dao.MailBoxConfigurationDAO;
import com.liaison.mailbox.jpa.dao.MailBoxConfigurationDAOBase;
import com.liaison.mailbox.jpa.dao.MailboxServiceInstanceDAO;
import com.liaison.mailbox.jpa.dao.MailboxServiceInstanceDAOBase;
import com.liaison.mailbox.jpa.dao.ProcessorConfigurationDAO;
import com.liaison.mailbox.jpa.dao.ProcessorConfigurationDAOBase;
import com.liaison.mailbox.jpa.dao.ProfileConfigurationDAO;
import com.liaison.mailbox.jpa.dao.ProfileConfigurationDAOBase;
import com.liaison.mailbox.jpa.dao.ServiceInstanceDAO;
import com.liaison.mailbox.jpa.dao.ServiceInstanceDAOBase;
import com.liaison.mailbox.jpa.model.FSMStateValue;
import com.liaison.mailbox.jpa.model.MailBox;
import com.liaison.mailbox.jpa.model.MailboxServiceInstance;
import com.liaison.mailbox.jpa.model.Processor;
import com.liaison.mailbox.jpa.model.ProcessorProperty;
import com.liaison.mailbox.jpa.model.ScheduleProfileProcessor;
import com.liaison.mailbox.jpa.model.ScheduleProfilesRef;
import com.liaison.mailbox.jpa.model.ServiceInstance;
import com.liaison.mailbox.service.core.fsm.MailboxFSM;
import com.liaison.mailbox.service.dto.ResponseDTO;
import com.liaison.mailbox.service.dto.configuration.CredentialDTO;
import com.liaison.mailbox.service.dto.configuration.DynamicPropertiesDTO;
import com.liaison.mailbox.service.dto.configuration.FSMEventDTO;
import com.liaison.mailbox.service.dto.configuration.FolderDTO;
import com.liaison.mailbox.service.dto.configuration.ProcessorDTO;
import com.liaison.mailbox.service.dto.configuration.PropertyDTO;
import com.liaison.mailbox.service.dto.configuration.TrustStoreDTO;
import com.liaison.mailbox.service.dto.configuration.request.AddProcessorToMailboxRequestDTO;
import com.liaison.mailbox.service.dto.configuration.request.InterruptExecutionEventRequestDTO;
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
import com.liaison.mailbox.service.exception.ProcessorManagementFailedException;
import com.liaison.mailbox.service.util.MailBoxUtil;
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
	 * @param serviceRequest
	 *            The AddProcessorToMailboxRequestDTO
	 * @return The AddProcessorToMailboxResponseDTO
	 * @throws IOException
	 * @throws JAXBException
	 * @throws JsonMappingException
	 * @throws JsonGenerationException
	 * @throws SymmetricAlgorithmException
	 * @throws JSONException
	 */
	public AddProcessorToMailboxResponseDTO createProcessor(String mailBoxGuid, AddProcessorToMailboxRequestDTO serviceRequest, String serviceInstanceId)
			throws JsonGenerationException, JsonMappingException, JAXBException, IOException, SymmetricAlgorithmException, JSONException {

		LOGGER.debug("call receive to insert the processor ::{}", serviceRequest.getProcessor());
		AddProcessorToMailboxResponseDTO serviceResponse = new AddProcessorToMailboxResponseDTO();

		try {

	          // check if service instance id is available in query param if not throw an exception
            if (MailBoxUtil.isEmpty(serviceInstanceId)) {
                throw new MailBoxConfigurationServicesException(Messages.SERVICE_INSTANCE_ID_NOT_AVAILABLE);
            }       
			if (!mailBoxGuid.equals(serviceRequest.getProcessor().getLinkedMailboxId())) {
				throw new MailBoxConfigurationServicesException(Messages.GUID_DOES_NOT_MATCH, MAILBOX);
			}

			ProcessorDTO processorDTO = serviceRequest.getProcessor();
			if (processorDTO == null) {
				throw new MailBoxConfigurationServicesException(Messages.INVALID_REQUEST);
			}
			
			GenericValidator validator = new GenericValidator();
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

			createMailBoxAndProcessorLink(serviceRequest, null, processor);

			createScheduleProfileAndProcessorLink(serviceRequest, null, processor);

			// adding service instance id
			processor.setServiceInstance(serviceInstance);
			// set self-signed to 0 by default since that field is having NOTNULL constraint
			processor.setIsSelfSigned(0);

			// persist the processor.
			ProcessorConfigurationDAO configDAO = new ProcessorConfigurationDAOBase();
			configDAO.persist(processor);

			// linking mailbox and service instance id
			MailboxServiceInstanceDAO msiDao = new MailboxServiceInstanceDAOBase();
			MailboxServiceInstance mailboxServiceInstance = msiDao.findByGuids(processor.getMailbox().getPguid(), serviceInstance.getPguid());

			MailBoxConfigurationDAO mailBoxConfigDAO = new MailBoxConfigurationDAOBase();
			MailBox mailBox = mailBoxConfigDAO.find(MailBox.class, processor.getMailbox().getPguid());
			if (null == mailBox) {
				throw new MailBoxConfigurationServicesException(Messages.MBX_DOES_NOT_EXIST, processor.getMailbox().getPguid());
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
			serviceResponse.setResponse(new ResponseDTO(Messages.CREATE_OPERATION_FAILED, PROCESSOR, Messages.FAILURE, e
					.getMessage()));
			return serviceResponse;

		}

	}

	/**
	 * Creates link between scheduleProfileref and processor.
	 * 
	 * @param addRequest
	 *            The AddProcessorToMailboxRequest DTO
	 * @param reviseRequest
	 *            The ReviseProcessorRequest DTO
	 * @param processor
	 *            The processor Entity
	 * @throws MailBoxConfigurationServicesException
	 */
	private void createScheduleProfileAndProcessorLink(AddProcessorToMailboxRequestDTO addRequest,
			ReviseProcessorRequestDTO reviseRequest,
			Processor processor)
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
					throw new MailBoxConfigurationServicesException(Messages.PROFILE_NAME_DOES_NOT_EXIST, profileName);
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
	 * @param addRequest
	 *            The AddProcessorToMailboxRequest DTO
	 * @param reviseRequest
	 *            The ReviseProcessorRequest DTO
	 * @param processor
	 *            The processor Entity
	 * @throws MailBoxConfigurationServicesException
	 */
	private void createMailBoxAndProcessorLink(AddProcessorToMailboxRequestDTO addRequest,
			ReviseProcessorRequestDTO reviseRequest,
			Processor processor)
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
			throw new MailBoxConfigurationServicesException(Messages.MBX_DOES_NOT_EXIST, mailBoxId);
		}
		processor.setMailbox(mailBox);

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

			LOGGER.debug("Entering into get processor.");
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
			LOGGER.debug("Exit from get mailbox.");
			return serviceResponse;

		} catch (MailBoxConfigurationServicesException e) {

			LOGGER.error(Messages.READ_OPERATION_FAILED.name(), e);
			serviceResponse.setResponse(new ResponseDTO(Messages.READ_OPERATION_FAILED, PROCESSOR, Messages.FAILURE, e
					.getMessage()));
			return serviceResponse;
		}
	}
	
	/**
	 *  Get the TrustStoreResponse.
	 *  
	 * @return GetTrustStoreResponseDTO
	 * @throws MailBoxConfigurationServicesException
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws JSONException
	 */
	public GetTrustStoreResponseDTO uploadSelfSignedTrustStore() throws MailBoxConfigurationServicesException, ClientProtocolException, IOException, JSONException {

		GetTrustStoreResponseDTO serviceResponse = new GetTrustStoreResponseDTO();

		try {

			String request = ServiceUtils.readFileFromClassPath("requests/keymanager/truststorerequest.json");
			JSONObject jsonRequest = new JSONObject(request);
			jsonRequest.put("serviceInstanceId", System.currentTimeMillis());

			HttpPost httpPost = new HttpPost(MailBoxUtil.getEnvironmentProperties().getString("kms-base-url") 
					+ "upload/truststore"); 
			DefaultHttpClient httpclient = new DefaultHttpClient();

			StringBody jsonRequestBody = new StringBody(jsonRequest.toString(), ContentType.APPLICATION_JSON);
			FileBody trustStore = new FileBody(new File(MailBoxUtil.getEnvironmentProperties().getString("certificateDirectory")));

			HttpEntity reqEntity = MultipartEntityBuilder.create()
					.addPart("request", jsonRequestBody)
					.addPart("key", trustStore)
					.build();

			httpPost.setEntity(reqEntity);
			HttpResponse response = httpclient.execute(httpPost);

			// TODO Check for 200 Status code, Consume entity then get GUID and return

			if (response.getStatusLine().getStatusCode() == 201) {

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

			} else throw new MailBoxConfigurationServicesException(Messages.SELFSIGNED_TRUSTSTORE_CREATION_FAILED);

		} catch (MailBoxConfigurationServicesException e) {

			LOGGER.error(Messages.CREATE_OPERATION_FAILED.name(), e);
			serviceResponse.setResponse(new ResponseDTO(Messages.CREATE_OPERATION_FAILED, TRUSTSTORE, Messages.FAILURE, e
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

			LOGGER.debug("Entering into get processor.");
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
			LOGGER.debug("Exit from deactivate mailbox.");
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
	 *            The guid of the mailbox.The given processor should belongs to the given mailbox.
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

		LOGGER.debug("Entering into revising processor.");
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
			
			GenericValidator validator = new GenericValidator();
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
			if (processor.getScheduleProfileProcessors() != null) {
				processor.getScheduleProfileProcessors().clear();
			}

			createMailBoxAndProcessorLink(null, request, processor);
			createScheduleProfileAndProcessorLink(null, request, processor);

			// Copying the new details of the processor and merging.
			processorDTO.copyToEntity(processor, false);

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
			serviceResponse.setResponse(new ResponseDTO(Messages.REVISE_OPERATION_FAILED, PROCESSOR, Messages.FAILURE, e
					.getMessage()));
			return serviceResponse;
		}

		LOGGER.debug("Exit from revise processor.");

		return serviceResponse;
	}

	/**
	 * Method for add and update the dynamic processorProperty to Processor entity
	 * 
	 * @param guid
	 *            The processor guid
	 * @param propertyDTO
	 *            The dynamic properties
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
	 * @param mailBoxGuid
	 *            The guid of the mailbox
	 * @param processor
	 *            The processor of the mailbox
	 * @throws MailBoxConfigurationServicesException
	 */
	private void validateProcessorBelongToMbx(String mailBoxGuid, Processor processor)
			throws MailBoxConfigurationServicesException {

		MailBox mbx = processor.getMailbox();
		if (!mailBoxGuid.equals(mbx.getPrimaryKey())) {
			throw new MailBoxConfigurationServicesException(Messages.PROC_DOES_NOT_BELONG_TO_MBX);
		}
	}

	/**
	 * Get the executing processors
	 * 
	 * @throws MailBoxConfigurationServicesException
	 * @throws IOException 
	 */
	public GetExecutingProcessorResponseDTO getExecutingProcessors(String status, String frmDate, String toDate) throws IOException {
		
		GetExecutingProcessorResponseDTO serviceResponse = new GetExecutingProcessorResponseDTO();
		LOGGER.debug("Entering into getExecutingProcessors.");
		try {
	
			String listJobsIntervalInHours = MailBoxUtil.getEnvironmentProperties().getString("default.job.search.period.in.hours"); 
			Timestamp timeStmp = new Timestamp(new Date().getTime());
			
			Calendar cal = Calendar.getInstance();
			cal.setTime(timeStmp);
			cal.add(Calendar.HOUR, -Integer.parseInt(listJobsIntervalInHours));
			timeStmp.setTime(cal.getTime().getTime());
			timeStmp = new Timestamp(cal.getTime().getTime());
			
			FSMStateDAO procDAO = new FSMStateDAOBase();
			
			List<FSMStateValue> listfsmStateVal = new ArrayList<FSMStateValue>();
			
			if((!MailBoxUtil.isEmpty(frmDate) && MailBoxUtil.isEmpty(toDate)) || (MailBoxUtil.isEmpty(frmDate) && !MailBoxUtil.isEmpty(toDate))) {
				throw new ProcessorManagementFailedException(Messages.INVALID_DATE_RANGE);
			}
			
			if(!MailBoxUtil.isEmpty(status) && ExecutionState.findByCode(status) == null) {
				throw new ProcessorManagementFailedException(Messages.INVALID_PROCESSOR_STATUS);
			}
			
			if(!MailBoxUtil.isEmpty(status) && !MailBoxUtil.isEmpty(frmDate) && !MailBoxUtil.isEmpty(toDate)) {
				listfsmStateVal = procDAO.findProcessorsExecutingByValueAndDate(status, frmDate, toDate);
			}
			
			if (!MailBoxUtil.isEmpty(status) && MailBoxUtil.isEmpty(frmDate) && MailBoxUtil.isEmpty(toDate)) {
				listfsmStateVal = procDAO.findProcessorsExecutingByValue(status, timeStmp);
			}
			
			if(!MailBoxUtil.isEmpty(frmDate) && !MailBoxUtil.isEmpty(toDate) && MailBoxUtil.isEmpty(status)) {
				listfsmStateVal = procDAO.findProcessorsExecutingByDate(frmDate, toDate);
			}
			
			if(MailBoxUtil.isEmpty(status) && MailBoxUtil.isEmpty(frmDate) && MailBoxUtil.isEmpty(toDate)) {
				listfsmStateVal = procDAO.findAllProcessorsExecuting(timeStmp);
			}
			
			List<GetExecutingProcessorDTO> getExecutingProcessorDTOList = new ArrayList<GetExecutingProcessorDTO>();
			GetExecutingProcessorDTO getExecutingDTO = null;
			for (FSMStateValue fsmv : listfsmStateVal) {
	
				getExecutingDTO = new GetExecutingProcessorDTO();
				getExecutingDTO.copyFromEntity(fsmv);
				getExecutingProcessorDTOList.add(getExecutingDTO);
			}
	
			serviceResponse.setExecutingProcessor(getExecutingProcessorDTOList);
			
			if(getExecutingProcessorDTOList == null || getExecutingProcessorDTOList.isEmpty()) {
				serviceResponse.setResponse(new ResponseDTO(Messages.NO_PROCESSORS_AVAIL, EXECUTING_PROCESSORS, Messages.SUCCESS));
			} else {
				serviceResponse.setResponse(new ResponseDTO(Messages.READ_SUCCESSFUL, EXECUTING_PROCESSORS, Messages.SUCCESS));
			}
			
			LOGGER.debug("Exit from getExecutingProcessors.");
			return serviceResponse;
			
		} catch(ProcessorManagementFailedException e) {
			
			LOGGER.error(Messages.READ_OPERATION_FAILED.name(), e);
			serviceResponse.setResponse(new ResponseDTO(Messages.READ_OPERATION_FAILED, EXECUTING_PROCESSORS, Messages.FAILURE, e
					.getMessage()));
			return serviceResponse;
		}
		
	}

	/**
	 * 
	 * Interrupt the execution of running processor
	 * 
	 * @param serviceRequest
	 * @throws MailBoxConfigurationServicesException 
	 * 
	 */
	public InterruptExecutionEventResponseDTO interruptRunningProcessor( InterruptExecutionEventRequestDTO serviceRequest) throws MailBoxConfigurationServicesException {
		
		LOGGER.debug("Entering into interrupt processor.");
		InterruptExecutionEventResponseDTO serviceResponse = new InterruptExecutionEventResponseDTO();

		try {

			FSMEventDTO fsmEventDTO = serviceRequest.getFsmEvent();
			if (fsmEventDTO == null) {
				throw new ProcessorManagementFailedException(Messages.INVALID_REQUEST);
			}
			
			GenericValidator validator = new GenericValidator();
			validator.validate(fsmEventDTO);

			MailboxFSM fsm = new MailboxFSM();
			LOGGER.info("############################################################################");
			LOGGER.info("Interrupt signal received for   "+fsmEventDTO.getExecutionID());
			LOGGER.info("#############################################################################");

			// persisting the FSMEvent entity
			fsm.createEvent(ExecutionEvents.INTERRUPT_SIGNAL_RECIVED, fsmEventDTO.getExecutionID());

			// response message construction
			serviceResponse.setResponse(new ResponseDTO(Messages.RECEIVED_SUCCESSFULLY, INTERRUPT_SIGNAL, Messages.SUCCESS));

			LOGGER.debug("Exiting from interrupt processor.");

			return serviceResponse;
		} catch (ProcessorManagementFailedException e) {

			LOGGER.error(Messages.RECEIVED_OPERATION_FAILED.name(), e);
			serviceResponse.setResponse(new ResponseDTO(Messages.RECEIVED_OPERATION_FAILED, INTERRUPT_SIGNAL, Messages.FAILURE, e
					.getMessage()));

			return serviceResponse;
		} catch (MailBoxConfigurationServicesException e) {

			LOGGER.error(Messages.RECEIVED_OPERATION_FAILED.name(), e);
			serviceResponse.setResponse(new ResponseDTO(Messages.RECEIVED_OPERATION_FAILED, INTERRUPT_SIGNAL, Messages.FAILURE, e
					.getMessage()));

			return serviceResponse;
		}
	}
}
