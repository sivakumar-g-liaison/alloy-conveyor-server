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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

import com.liaison.commons.security.pkcs7.SymmetricAlgorithmException;
import com.liaison.commons.util.client.sftp.StringUtil;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.dtdm.dao.MailBoxConfigurationDAO;
import com.liaison.mailbox.dtdm.dao.MailBoxConfigurationDAOBase;
import com.liaison.mailbox.dtdm.dao.MailboxServiceInstanceDAO;
import com.liaison.mailbox.dtdm.dao.MailboxServiceInstanceDAOBase;
import com.liaison.mailbox.dtdm.dao.ProcessorConfigurationDAO;
import com.liaison.mailbox.dtdm.dao.ProcessorConfigurationDAOBase;
import com.liaison.mailbox.dtdm.dao.ServiceInstanceDAO;
import com.liaison.mailbox.dtdm.dao.ServiceInstanceDAOBase;
import com.liaison.mailbox.dtdm.model.MailBox;
import com.liaison.mailbox.dtdm.model.MailboxServiceInstance;
import com.liaison.mailbox.dtdm.model.Processor;
import com.liaison.mailbox.dtdm.model.ServiceInstance;
import com.liaison.mailbox.enums.MailBoxStatus;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.service.dto.ResponseDTO;
import com.liaison.mailbox.service.dto.configuration.MailBoxDTO;
import com.liaison.mailbox.service.dto.configuration.MailBoxResponseDTO;
import com.liaison.mailbox.service.dto.configuration.PropertiesFileDTO;
import com.liaison.mailbox.service.dto.configuration.PropertyDTO;
import com.liaison.mailbox.service.dto.configuration.TenancyKeyDTO;
import com.liaison.mailbox.service.dto.configuration.request.AddMailboxRequestDTO;
import com.liaison.mailbox.service.dto.configuration.request.FileInfoDTO;
import com.liaison.mailbox.service.dto.configuration.request.ReviseMailBoxRequestDTO;
import com.liaison.mailbox.service.dto.configuration.response.AddMailBoxResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.DeActivateMailBoxResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.GetMailBoxResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.GetPropertiesValueResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.ReviseMailBoxResponseDTO;
import com.liaison.mailbox.service.dto.ui.SearchMailBoxDTO;
import com.liaison.mailbox.service.dto.ui.SearchMailBoxResponseDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.util.MailBoxUtil;
import com.liaison.mailbox.service.validation.GenericValidator;

/**
 * Class which has mailbox configuration related operations.
 *
 * @author veerasamyn
 */
public class MailBoxConfigurationService {

	private static final Logger LOG = LogManager.getLogger(MailBoxConfigurationService.class);
	private static final String MAILBOX = "Mailbox";

	private static String PAGING_OFFSET = "pagingoffset";
	private static String PAGING_COUNT = "pagingcount";
	/**
	 * Creates Mail Box.
	 *
	 * @param request
	 *            The request DTO.
	 * @return The responseDTO.
	 * @throws IOException
	 * @throws JAXBException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 */
	public AddMailBoxResponseDTO createMailBox(AddMailboxRequestDTO request, String serviceInstanceId,
			String aclManifestJson) throws MailBoxConfigurationServicesException, JsonParseException,
			JsonMappingException, JAXBException, IOException {

		LOG.debug("Entering into create mailbox.");
		AddMailBoxResponseDTO serviceResponse = new AddMailBoxResponseDTO();

		try {

			// check if service instance id is available in query param if not throw an exception
			if (MailBoxUtil.isEmpty(serviceInstanceId)) {
				throw new MailBoxConfigurationServicesException(Messages.SERVICE_INSTANCE_ID_NOT_AVAILABLE, Response.Status.BAD_REQUEST);
			}

			MailBoxDTO mailboxDTO = request.getMailBox();
			if (mailboxDTO == null) {
				throw new MailBoxConfigurationServicesException(Messages.INVALID_REQUEST, Response.Status.BAD_REQUEST);
			}

			// Getting the mailbox.
			MailBoxConfigurationDAO configDao = new MailBoxConfigurationDAOBase();
			MailBox retrievedEntity = configDao.findByMailBoxNameAndTenancyKeyName(mailboxDTO.getName(), mailboxDTO.getTenancyKey());

			if (null != retrievedEntity) {
	            throw new MailBoxConfigurationServicesException(Messages.ENTITY_ALREADY_EXIST,MAILBOX, Response.Status.CONFLICT);
	        }
			// validation
			GenericValidator validator = new GenericValidator();
			validator.validate(mailboxDTO);
			for (PropertyDTO property : mailboxDTO.getProperties()) {
				validator.validate(property);
			}

			MailBox mailBox = new MailBox();
			mailboxDTO.copyToEntity(mailBox);
			mailBox.setPguid(MailBoxUtil.getGUID());

			// retrieve the tenancy key from acl manifest
			List<TenancyKeyDTO> tenancyKeys = MailBoxUtil.getTenancyKeysFromACLManifest(aclManifestJson);
			if (tenancyKeys.isEmpty()) {
				LOG.error("retrieval of tenancy key from acl manifest failed");
			}

			// creating a link between mailbox and service instance table
			createMailboxServiceInstanceIdLink(serviceInstanceId, mailBox);

			// persisting the mailbox entity
			configDao.persist(mailBox);

			// response message construction
			serviceResponse.setResponse(new ResponseDTO(Messages.CREATED_SUCCESSFULLY, MAILBOX, Messages.SUCCESS));
			serviceResponse.setMailBox(new MailBoxResponseDTO(String.valueOf(mailBox.getPrimaryKey())));
			LOG.debug("Exit from create mailbox.");
			return serviceResponse;

		} catch (MailBoxConfigurationServicesException e) {

			LOG.error(Messages.CREATE_OPERATION_FAILED.name(), e);
			serviceResponse.setResponse(new ResponseDTO(Messages.CREATE_OPERATION_FAILED, MAILBOX, Messages.FAILURE, e
					.getMessage()));
			return serviceResponse;

		}

	}

	/**
	 * Create Mailbox ServiceInstance Id.
	 *
	 * @param serviceInstanceID
	 *            The serviceInstanceID of the mailbox
	 * @param mailbox
	 * @throws MailBoxConfigurationServicesException
	 */
	public void createMailboxServiceInstanceIdLink(String serviceInstanceID, MailBox mailbox)
			throws MailBoxConfigurationServicesException {

		try {

			ServiceInstanceDAO serviceInstanceDAO = new ServiceInstanceDAOBase();
			ServiceInstance serviceInstance = serviceInstanceDAO.findById(serviceInstanceID);
			if (serviceInstance == null) {
				serviceInstance = new ServiceInstance();
				serviceInstance.setName(serviceInstanceID);
				serviceInstance.setPguid(MailBoxUtil.getGUID());
				serviceInstanceDAO.persist(serviceInstance);
			}

			MailboxServiceInstanceDAO msiDao = new MailboxServiceInstanceDAOBase();
			MailboxServiceInstance mailboxServiceInstance = msiDao.findByGuids(mailbox.getPguid(),
					serviceInstance.getPguid());

			List<MailboxServiceInstance> mbxServiceInstances = new ArrayList<MailboxServiceInstance>();
			if (mailboxServiceInstance == null) {
				// Creates relationship mailbox and service instance id
				MailboxServiceInstance msi = new MailboxServiceInstance();
				msi.setPguid(MailBoxUtil.getGUID());
				msi.setServiceInstance(serviceInstance);
				mbxServiceInstances.add(msi);
				mailbox.setMailboxServiceInstances(mbxServiceInstances);
			}
		} catch (Exception e) {
			throw new MailBoxConfigurationServicesException("Invalid service instance id.", Response.Status.BAD_REQUEST);
		}
	}

	/**
	 * Get the mailbox using guid and its processor using service instance id.
	 *
	 * @param guid
	 *            The guid of the mailbox.
	 * @return The responseDTO.
	 * @throws SymmetricAlgorithmException
	 * @throws IOException
	 * @throws JAXBException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 */
	public GetMailBoxResponseDTO getMailBox(String guid, boolean addConstraint, String serviceInstanceId,
			String aclManifestJson) throws JAXBException, IOException, SymmetricAlgorithmException {

		LOG.debug("Entering into get mailbox.");
		LOG.info("The retrieve guid is {} ", guid);

		GetMailBoxResponseDTO serviceResponse = new GetMailBoxResponseDTO();

		try {

			// check if service instance id is available in query param if not throw an exception
			if (MailBoxUtil.isEmpty(serviceInstanceId)) {
				throw new MailBoxConfigurationServicesException(Messages.SERVICE_INSTANCE_ID_NOT_AVAILABLE, Response.Status.BAD_REQUEST);
			}

			// Getting mailbox
			MailBoxConfigurationDAO configDao = new MailBoxConfigurationDAOBase();

			// retrieve the tenancy key from acl manifest
			List<TenancyKeyDTO> tenancyKeys = MailBoxUtil.getTenancyKeysFromACLManifest(aclManifestJson);
			if (tenancyKeys.isEmpty()) {
				LOG.error("retrieval of tenancy key from acl manifest failed");
			}

			MailBox mailBox = configDao.find(MailBox.class, guid);
			if (mailBox == null) {
				throw new MailBoxConfigurationServicesException(Messages.MBX_DOES_NOT_EXIST, guid, Response.Status.BAD_REQUEST);
			}

			// retrieve the actual tenancykey Display Name from TenancyKeys
			String tenancyKeyDisplayName = MailBoxUtil.getTenancyKeyWithGuid(mailBox.getTenancyKey(), tenancyKeys);

			// if the tenancy key display name is not available then error will be logged as the given tenancyKey is
			// not available in tenancyKeys retrieved from acl manifest
			if (StringUtil.isNullOrEmptyAfterTrim(tenancyKeyDisplayName)) {
				LOG.error("Tenancy Key present Manifest does not match the Tenancy Key of mailbox.");
			}

			ProcessorConfigurationDAO processorDao = new ProcessorConfigurationDAOBase();
			if (addConstraint) {
				List<Processor> filteredProcessor = processorDao.findProcessorByMbxAndServiceInstance(
						mailBox.getPguid(), serviceInstanceId);
				mailBox.setMailboxProcessors(filteredProcessor);
			} else {
				List<Processor> processors = processorDao.findProcessorByMbx(mailBox.getPguid());
				mailBox.setMailboxProcessors(processors);
			}

			MailBoxDTO dto = new MailBoxDTO();
			dto.copyFromEntity(mailBox);
			dto.setTenancyKeyDisplayName(tenancyKeyDisplayName);

			serviceResponse.setMailBox(dto);
			serviceResponse.setResponse(new ResponseDTO(Messages.READ_SUCCESSFUL, MAILBOX, Messages.SUCCESS));
			LOG.debug("Exit from get mailbox.");
			return serviceResponse;

		} catch (MailBoxConfigurationServicesException e) {

			LOG.error(Messages.READ_OPERATION_FAILED.name(), e);
			serviceResponse.setResponse(new ResponseDTO(Messages.READ_OPERATION_FAILED, MAILBOX, Messages.FAILURE, e.getMessage()));
			return serviceResponse;
		}

	}

	/**
	 * Method revise the mailbox configurations.
	 *
	 * @param guid
	 *            The mailbox pguid.
	 * @throws IOException
	 * @throws JAXBException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 */
	public ReviseMailBoxResponseDTO reviseMailBox(ReviseMailBoxRequestDTO request, String guid,
			String serviceInstanceId, String aclManifestJson) throws JsonParseException, JsonMappingException,
			JAXBException, IOException {

		LOG.debug("Entering into revise mailbox.The revise request is for {} ", guid);

		ReviseMailBoxResponseDTO serviceResponse = new ReviseMailBoxResponseDTO();

		try {

			// check if service instance id is available in query param if not throw an exception
			if (MailBoxUtil.isEmpty(serviceInstanceId)) {
				throw new MailBoxConfigurationServicesException(Messages.SERVICE_INSTANCE_ID_NOT_AVAILABLE, Response.Status.BAD_REQUEST);
			}

			MailBoxDTO mailboxDTO = request.getMailBox();
			if (mailboxDTO == null) {
				throw new MailBoxConfigurationServicesException(Messages.INVALID_REQUEST, Response.Status.BAD_REQUEST);
			}

			// Validation
			GenericValidator validator = new GenericValidator();
			validator.validate(mailboxDTO);
			for (PropertyDTO property : mailboxDTO.getProperties()) {
				validator.validate(property);
			}

			if (!guid.equals(mailboxDTO.getGuid())) {
				throw new MailBoxConfigurationServicesException(Messages.GUID_DOES_NOT_MATCH, MAILBOX, Response.Status.CONFLICT);
			}

			// Getting the mailbox.
			MailBoxConfigurationDAO configDao = new MailBoxConfigurationDAOBase();
			MailBox retrievedMailBox = configDao.find(MailBox.class, guid);
			if (retrievedMailBox == null) {
				throw new MailBoxConfigurationServicesException(Messages.GUID_NOT_AVAIL, Response.Status.BAD_REQUEST);
			}

			if (!mailboxDTO.getName().equals(retrievedMailBox.getMbxName())) {
				// Getting the mailbox.
				MailBox retrievedEntity = configDao.findByMailBoxNameAndTenancyKeyName(mailboxDTO.getName(), mailboxDTO.getTenancyKey());

				if (null != retrievedEntity) {
		            throw new MailBoxConfigurationServicesException(Messages.ENTITY_ALREADY_EXIST,MAILBOX, Response.Status.CONFLICT);
		        }
		  }

			// Removing the child items.
			retrievedMailBox.getMailboxProperties().clear();

			// updates the mail box data
			mailboxDTO.copyToEntity(retrievedMailBox);

			// retrieve the tenancy key from acl manifest
			List<TenancyKeyDTO> tenancyKeys = MailBoxUtil.getTenancyKeysFromACLManifest(aclManifestJson);
			if (tenancyKeys.isEmpty()) {
				LOG.error("retrieval of tenancy key from acl manifest failed");
			}

			// retrieve the actual tenancykey guids from DTO
			List<String> tenancyKeyGuids = MailBoxUtil.getTenancyKeyGuidsFromTenancyKeys(tenancyKeys);

			// checking if the tenancy key in acl manifest matches with tenancy key in mailbox
			if (!tenancyKeyGuids.contains(retrievedMailBox.getTenancyKey())) {
				LOG.error("Tenancy Key present Manifest does not match the Tenancy Key of mailbox.");
			}

			configDao.merge(retrievedMailBox);

			ServiceInstanceDAO serviceInstanceDAO = new ServiceInstanceDAOBase();
			ServiceInstance serviceInstance = serviceInstanceDAO.findById(serviceInstanceId);
			if (serviceInstance == null) {
				serviceInstance = new ServiceInstance();
				serviceInstance.setName(serviceInstanceId);
				serviceInstance.setPguid(MailBoxUtil.getGUID());
				serviceInstanceDAO.persist(serviceInstance);
			}

			MailboxServiceInstanceDAO msiDao = new MailboxServiceInstanceDAOBase();
			MailboxServiceInstance mailboxServiceInstance = msiDao.findByGuids(guid, serviceInstance.getPguid());
			if (mailboxServiceInstance == null) {

				// Creates relationship mailbox and service instance id
				MailboxServiceInstance msi = new MailboxServiceInstance();
				msi.setPguid(MailBoxUtil.getGUID());
				msi.setServiceInstance(serviceInstance);
				msi.setMailbox(retrievedMailBox);
				msiDao.persist(msi);
			}

			// response message construction
			serviceResponse.setResponse(new ResponseDTO(Messages.REVISED_SUCCESSFULLY, MAILBOX, Messages.SUCCESS));
			serviceResponse.setMailBox(new MailBoxResponseDTO(String.valueOf(retrievedMailBox.getPrimaryKey())));
			LOG.debug("Exit from revise mailbox.");
			return serviceResponse;

		} catch (MailBoxConfigurationServicesException e) {

			LOG.error(Messages.REVISE_OPERATION_FAILED.name(), e);
			serviceResponse.setResponse(new ResponseDTO(Messages.REVISE_OPERATION_FAILED, MAILBOX, Messages.FAILURE, e
					.getMessage()));
			return serviceResponse;
		}

	}

	/**
	 * Method revise the mailbox configurations.
	 *
	 * @param guid
	 *            The mailbox pguid.
	 * @throws IOException
	 */
	public DeActivateMailBoxResponseDTO deactivateMailBox(String guid, String aclManifestJson) throws IOException {

		LOG.debug("Entering into deactivate mailbox.");
		DeActivateMailBoxResponseDTO serviceResponse = new DeActivateMailBoxResponseDTO();
		try {

			LOG.info("The deactivate request is for {} ", guid);

			MailBoxConfigurationDAO configDao = new MailBoxConfigurationDAOBase();

			MailBox retrievedMailBox = configDao.find(MailBox.class, guid);
			if (retrievedMailBox == null) {
				throw new MailBoxConfigurationServicesException(Messages.MBX_DOES_NOT_EXIST, guid, Response.Status.BAD_REQUEST);
			}

			// retrieve the tenancy key from acl manifest
			List<TenancyKeyDTO> tenancyKeys = MailBoxUtil.getTenancyKeysFromACLManifest(aclManifestJson);
			if (tenancyKeys.isEmpty()) {
				LOG.error("retrieval of tenancy key from acl manifest failed");
			}

			// retrieve the actual tenancykey guids from DTO
			List<String> tenancyKeyGuids = MailBoxUtil.getTenancyKeyGuidsFromTenancyKeys(tenancyKeys);

			// checking if the tenancy key in acl manifest matches with tenancy key in mailbox
			if (!tenancyKeyGuids.contains(retrievedMailBox.getTenancyKey())) {
				LOG.error("Tenancy Key present Manifest does not match the Tenancy Key of mailbox.");
			}

			// Changing the mailbox status
			retrievedMailBox.setMbxStatus(MailBoxStatus.INACTIVE.value());
			configDao.merge(retrievedMailBox);

			// response message construction
			serviceResponse.setResponse(new ResponseDTO(Messages.DEACTIVATION_SUCCESSFUL, MAILBOX, Messages.SUCCESS));
			serviceResponse.setMailBox(new MailBoxResponseDTO(guid));
			LOG.debug("Exit from deactivate mailbox.");
			return serviceResponse;

		} catch (MailBoxConfigurationServicesException e) {

			LOG.error(Messages.DEACTIVATION_FAILED.value(), e);
			serviceResponse.setResponse(new ResponseDTO(Messages.DEACTIVATION_FAILED, MAILBOX, Messages.FAILURE, e
					.getMessage()));
			return serviceResponse;

		}
	}

	/**
	 * Searches the mailbox using mailbox name and profile name.
	 *
	 * @param mbxName
	 *            The name of the mailbox
	 *
	 * @param profName
	 *            The name of the profile
	 * @param serviceInstId
	 *
	 * @return The SearchMailBoxResponseDTO
	 * @throws IOException
	 * @throws JAXBException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 */
	public SearchMailBoxResponseDTO searchMailBox(String mbxName,String serviceInstanceId, String profName, String aclManifestJson, String page, String pageSize, String sortField, String sortDirection)
			throws JsonParseException, JsonMappingException, JAXBException, IOException {

		LOG.debug("Entering into search mailbox.");

		int totalCount = 0;
		int startOffset = 0;
		int count = 0;
		List<MailBox> retrievedMailBoxes = null;
		Map <String, Integer> pageOffsetDetails = null;
		SearchMailBoxResponseDTO serviceResponse = new SearchMailBoxResponseDTO();

		try {

			// check if service instance id is available in query param if not throw an exception
			if (MailBoxUtil.isEmpty(serviceInstanceId)) {
				LOG.error(Messages.SERVICE_INSTANCE_ID_NOT_AVAILABLE);
			}

			// Getting mailbox
			MailBoxConfigurationDAO configDao = new MailBoxConfigurationDAOBase();
			ProcessorConfigurationDAO dao = new ProcessorConfigurationDAOBase();

			List<MailBox> mailboxes = new ArrayList<MailBox>();
			// retrieve tenancy key from acl manifest
			List<TenancyKeyDTO> tenancyKeys = MailBoxUtil.getTenancyKeysFromACLManifest(aclManifestJson);

			if (tenancyKeys.isEmpty()) {
				LOG.error("retrieval of tenancy key from acl manifest failed");
				throw new MailBoxConfigurationServicesException(Messages.TENANCY_KEY_RETRIEVAL_FAILED, Response.Status.BAD_REQUEST);
			}

			List<String> tenancyKeyGuids = MailBoxUtil.getTenancyKeyGuidsFromTenancyKeys(tenancyKeys);
			if (!MailBoxUtil.isEmpty(profName)) {

				totalCount = configDao.getMailboxCountByProtocol(mbxName, profName, tenancyKeyGuids);
				pageOffsetDetails = getPagingOffsetDetails(page, pageSize, totalCount);
				startOffset = pageOffsetDetails.get(PAGING_OFFSET);
				count = pageOffsetDetails.get(PAGING_COUNT);
				retrievedMailBoxes = configDao.find(mbxName, profName, tenancyKeyGuids, startOffset , count, sortField , sortDirection);
				mailboxes.addAll(retrievedMailBoxes);
				serviceResponse.setTotalItems(totalCount);

			} else if (MailBoxUtil.isEmpty(profName) && !MailBoxUtil.isEmpty(mbxName)) {

				// If the profile name is empty it will use findByName
				totalCount = configDao.getMailboxCountByName(mbxName, tenancyKeyGuids);
				pageOffsetDetails = getPagingOffsetDetails(page, pageSize, totalCount);
				startOffset = pageOffsetDetails.get(PAGING_OFFSET);
				count = pageOffsetDetails.get(PAGING_COUNT);
				retrievedMailBoxes = configDao.findByName(mbxName, tenancyKeyGuids, startOffset , count, sortField , sortDirection);
				mailboxes.addAll(retrievedMailBoxes);
				serviceResponse.setTotalItems(totalCount);

			} else if (MailBoxUtil.isEmpty(profName) && MailBoxUtil.isEmpty(mbxName)) {
				throw new MailBoxConfigurationServicesException(Messages.INVALID_DATA, Response.Status.BAD_REQUEST);
			}

			// Constructing the SearchMailBoxDTO from retrieved mailboxes
			List<SearchMailBoxDTO> searchMailBoxDTOList = new ArrayList<SearchMailBoxDTO>();
			SearchMailBoxDTO serachMailBoxDTO = null;
			for (MailBox mbx : mailboxes) {

				serachMailBoxDTO = new SearchMailBoxDTO();
				serachMailBoxDTO.copyFromEntity(mbx, dao.isMailboxHasProcessor(mbx.getPguid(), serviceInstanceId));
				searchMailBoxDTOList.add(serachMailBoxDTO);
			}

			// Constructing the responses.
			serviceResponse.setMailBox(searchMailBoxDTOList);
			serviceResponse.setResponse(new ResponseDTO(Messages.SEARCH_SUCCESSFUL, MAILBOX, Messages.SUCCESS));

			LOG.debug("Exit from search mailbox.");
			return serviceResponse;

		} catch (MailBoxConfigurationServicesException e) {

			LOG.error(Messages.READ_OPERATION_FAILED.name(), e);
			serviceResponse
					.setResponse(new ResponseDTO(Messages.SEARCH_OPERATION_FAILED, MAILBOX, Messages.FAILURE, e
							.getMessage()));
			return serviceResponse;
		}

	}

	/**
	 * Service to list the directory structure for browse component.
	 *
	 * @param file
	 *            The root directory location
	 * @return The FileInfoDTO
	 */
	public FileInfoDTO getFileDetail(File file) {

		FileInfoDTO info = new FileInfoDTO();
		info.setRoleName(file.getAbsolutePath());
		info.setRoleId(file.getName());
		info.setChildren(new ArrayList<FileInfoDTO>());

		if (file.isDirectory() && file.list().length > 0) {

			for (File f : file.listFiles()) {
				info.getChildren().add(getFileDetail(f));
			}
		}

		return info;
	}

	/**
	 *
	 * getting values from java properties file
	 *
	 * @param trustStore
	 * @return
	 * @throws IOException
	 */
	public GetPropertiesValueResponseDTO getValuesFromPropertiesFile() {
		LOG.debug("Entering into getValuesFromPropertiesFile.");

		GetPropertiesValueResponseDTO serviceResponse = new GetPropertiesValueResponseDTO();
		PropertiesFileDTO dto = new PropertiesFileDTO();

		try {

			Properties prop = MailBoxUtil.getEnvProperties();

			dto.setTrustStoreId(prop.getProperty(MailBoxConstants.DEFAULT_GLOBAL_TRUSTSTORE_ID));
			dto.setTrustStoreGroupId(prop.getProperty(MailBoxConstants.DEFAULT_GLOBAL_TRUSTSTORE_GROUP_ID));
			dto.setListJobsIntervalInHours(prop.getProperty(MailBoxConstants.DEFAULT_JOB_SEARCH_PERIOD_IN_HOURS));
			dto.setFsmEventCheckIntervalInSeconds(prop.getProperty(MailBoxConstants.DEFAULT_INTERRUPT_SIGNAL_FREQUENCY_IN_SEC));
			dto.setMailboxPguidDisplayPrefix(prop.getProperty(MailBoxConstants.DEFAULT_PGUID_DISPLAY_PREFIX));
			dto.setDefaultScriptTemplateName(prop.getProperty(MailBoxConstants.DEFAULT_SCRIPT_TEMPLATE_NAME));

			serviceResponse.setProperties(dto);
			serviceResponse.setResponse(new ResponseDTO(Messages.READ_JAVA_PROPERTIES_SUCCESSFULLY, MAILBOX,
					Messages.SUCCESS));
			LOG.debug("Exit from getValuesFromPropertiesFile.");
			return serviceResponse;

		} catch (IOException e) {

			LOG.error(Messages.READ_OPERATION_FAILED.name(), e);
			serviceResponse
					.setResponse(new ResponseDTO(Messages.READ_JAVA_PROPERTIES_FAILED, MAILBOX, Messages.FAILURE, e
							.getMessage()));
			return serviceResponse;
		}

	}

	/**
	 * Method to get pagingOffsetDetails
	 * @param page
	 * @param pageSize
	 * @param totalCount
	 * @return Map
	 */
	private Map<String, Integer> getPagingOffsetDetails(String page, String pageSize, int totalCount) {

		Map <String, Integer> pageParameters = new HashMap<String, Integer>();
		//Calculate page size parameters
		Integer pageValue = 1;
		Integer pageSizeValue = 10;
		if (page != null && !page.isEmpty()) {
			pageValue = Integer.parseInt(page);
			if (pageValue < 0) {
				pageValue = 1;
			}
		}
		if (pageSize != null && !pageSize.isEmpty()) {
			pageSizeValue = Integer.parseInt(pageSize);
			if (pageSizeValue < 0) {
				pageSizeValue = 10;
			}
		}

		Integer fromIndex = (pageValue - 1) * pageSizeValue;
		pageParameters.put(PAGING_OFFSET, fromIndex);

		if(page != null && pageSize != null) {
			int toIndex = fromIndex + pageSizeValue;
			if (toIndex > totalCount) {
				toIndex = (totalCount - fromIndex);
			} else {
				toIndex = pageSizeValue;
			}
			pageParameters.put(PAGING_COUNT, toIndex);
		}
		return pageParameters;
	}
}
