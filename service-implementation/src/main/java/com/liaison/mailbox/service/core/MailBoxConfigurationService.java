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
import com.liaison.commons.util.settings.DecryptableConfiguration;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.dtdm.dao.MailBoxConfigurationDAO;
import com.liaison.mailbox.dtdm.dao.MailBoxConfigurationDAOBase;
import com.liaison.mailbox.dtdm.dao.MailboxDTDMDAO;
import com.liaison.mailbox.dtdm.dao.MailboxServiceInstanceDAO;
import com.liaison.mailbox.dtdm.dao.MailboxServiceInstanceDAOBase;
import com.liaison.mailbox.dtdm.dao.ProcessorConfigurationDAO;
import com.liaison.mailbox.dtdm.dao.ProcessorConfigurationDAOBase;
import com.liaison.mailbox.dtdm.dao.ServiceInstanceDAO;
import com.liaison.mailbox.dtdm.dao.ServiceInstanceDAOBase;
import com.liaison.mailbox.dtdm.model.MailBox;
import com.liaison.mailbox.dtdm.model.MailBoxProperty;
import com.liaison.mailbox.dtdm.model.MailboxServiceInstance;
import com.liaison.mailbox.dtdm.model.Processor;
import com.liaison.mailbox.dtdm.model.ServiceInstance;
import com.liaison.mailbox.enums.EntityStatus;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.service.dto.GenericSearchFilterDTO;
import com.liaison.mailbox.service.dto.ResponseDTO;
import com.liaison.mailbox.service.dto.configuration.MailBoxDTO;
import com.liaison.mailbox.service.dto.configuration.MailBoxResponseDTO;
import com.liaison.mailbox.service.dto.configuration.PropertiesFileDTO;
import com.liaison.mailbox.service.dto.configuration.PropertyDTO;
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

	/**
	 * Creates Mail Box.
	 *
	 * @param request The request DTO.
	 * @return The responseDTO.
	 * @throws IOException
	 * @throws JAXBException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 */
	public AddMailBoxResponseDTO createMailBox(AddMailboxRequestDTO request, String serviceInstanceId,
			String aclManifestJson)
			throws MailBoxConfigurationServicesException, JsonParseException, JsonMappingException, JAXBException,
			IOException {

		LOG.debug("Entering into create mailbox.");
		AddMailBoxResponseDTO serviceResponse = new AddMailBoxResponseDTO();

		try {

			// check if service instance id is available in query param if not throw an exception
			if (MailBoxUtil.isEmpty(serviceInstanceId)) {
				throw new MailBoxConfigurationServicesException(Messages.SERVICE_INSTANCE_ID_NOT_AVAILABLE,
						Response.Status.BAD_REQUEST);
			}

			MailBoxDTO mailboxDTO = request.getMailBox();
			if (mailboxDTO == null) {
				throw new MailBoxConfigurationServicesException(Messages.INVALID_REQUEST, Response.Status.BAD_REQUEST);
			}

			// Getting the mailbox.
			MailBoxConfigurationDAO configDao = new MailBoxConfigurationDAOBase();
			MailBox retrievedEntity = configDao.findByMailBoxNameAndTenancyKeyName(mailboxDTO.getName(),
					mailboxDTO.getTenancyKey());

			if (null != retrievedEntity) {
				throw new MailBoxConfigurationServicesException(Messages.ENTITY_ALREADY_EXIST, MAILBOX,
						Response.Status.CONFLICT);
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

			//Mailbox properties
            MailBoxProperty property = null;
            Set<MailBoxProperty> properties = new HashSet<>();
            for (PropertyDTO propertyDTO : mailboxDTO.getProperties()) {

                property = new MailBoxProperty();
                property.setMailbox(mailBox);
                propertyDTO.copyToEntity(property, true);
                properties.add(property);
            }
            mailBox.getMailboxProperties().addAll(properties);

			// retrieve the tenancy key from acl manifest
			List<String> tenancyKeys = MailBoxUtil.getTenancyKeyGuids(aclManifestJson);
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
			serviceResponse.setResponse(new ResponseDTO(Messages.CREATE_OPERATION_FAILED, MAILBOX, Messages.FAILURE,
					e.getMessage()));
			return serviceResponse;

		}

	}

	/**
	 * Create Mailbox ServiceInstance Id.
	 *
	 * @param serviceInstanceID The serviceInstanceID of the mailbox
	 * @param mailbox
	 */
	public void createMailboxServiceInstanceIdLink(String serviceInstanceID, MailBox mailbox) {

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

			Set<MailboxServiceInstance> mbxServiceInstances = new HashSet<MailboxServiceInstance>();
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
	 * @param guid The guid of the mailbox.
	 * @return The responseDTO.
	 * @throws SymmetricAlgorithmException
	 * @throws IOException
	 * @throws JAXBException 
	 */
	public GetMailBoxResponseDTO getMailBox(String guid, boolean addConstraint, String serviceInstanceId,
			String aclManifestJson) throws IOException, JAXBException, SymmetricAlgorithmException {

		LOG.debug("Entering into get mailbox.");
		LOG.info("The retrieve guid is {} ", guid);

		GetMailBoxResponseDTO serviceResponse = new GetMailBoxResponseDTO();

		try {

			// check if service instance id is available in query param if not throw an exception
			if (MailBoxUtil.isEmpty(serviceInstanceId)) {
				throw new MailBoxConfigurationServicesException(Messages.SERVICE_INSTANCE_ID_NOT_AVAILABLE,
						Response.Status.BAD_REQUEST);
			}

			// Getting mailbox
			MailBoxConfigurationDAO configDao = new MailBoxConfigurationDAOBase();

			MailBox mailBox = configDao.find(MailBox.class, guid);
			if (mailBox == null) {
				throw new MailBoxConfigurationServicesException(Messages.MBX_DOES_NOT_EXIST, guid,
						Response.Status.BAD_REQUEST);
			}

			// retrieve the actual tenancykey Display Name from TenancyKeys
			String tenancyKeyDisplayName = MailBoxUtil.getTenancyKeyNameByGuid(aclManifestJson, mailBox.getTenancyKey());

			// if the tenancy key display name is not available then error will be logged as the given tenancyKey is
			// not available in tenancyKeys retrieved from acl manifest
			if (StringUtil.isNullOrEmptyAfterTrim(tenancyKeyDisplayName)) {
				LOG.error("Tenancy Key present in Manifest does not match the Tenancy Key of mailbox.");
			}

			ProcessorConfigurationDAO processorDao = new ProcessorConfigurationDAOBase();
			if (addConstraint) {
				Set<Processor> filteredProcessor = processorDao.findProcessorByMbxAndServiceInstance(
						mailBox.getPguid(), serviceInstanceId);
				mailBox.setMailboxProcessors(filteredProcessor);
			} else {
				Set<Processor> processors = processorDao.findProcessorByMbx(mailBox.getPguid(), false);
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
			serviceResponse.setResponse(new ResponseDTO(Messages.READ_OPERATION_FAILED, MAILBOX, Messages.FAILURE,
					e.getMessage()));
			return serviceResponse;
		}

	}

	/**
	 * Method revise the mailbox configurations.
	 *
	 * @param guid The mailbox pguid.
	 * @throws IOException
	 * @throws JAXBException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 */
	public ReviseMailBoxResponseDTO reviseMailBox(ReviseMailBoxRequestDTO request, String guid,
			String serviceInstanceId, String aclManifestJson) throws IOException {

	    EntityManager em = null;
        EntityTransaction tx = null;
		LOG.debug("Entering into revise mailbox.The revise request is for {} ", guid);

		ReviseMailBoxResponseDTO serviceResponse = new ReviseMailBoxResponseDTO();

		try {

		    em = DAOUtil.getEntityManager(MailboxDTDMDAO.PERSISTENCE_UNIT_NAME);

			// check if service instance id is available in query param if not throw an exception
			if (MailBoxUtil.isEmpty(serviceInstanceId)) {
				throw new MailBoxConfigurationServicesException(Messages.SERVICE_INSTANCE_ID_NOT_AVAILABLE,
						Response.Status.BAD_REQUEST);
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
				throw new MailBoxConfigurationServicesException(Messages.GUID_DOES_NOT_MATCH, MAILBOX,
						Response.Status.CONFLICT);
			}

			// Getting the mailbox.
			tx = em.getTransaction();
			tx.begin();
			MailBox retrievedMailBox = em.find(MailBox.class, guid);
			if (retrievedMailBox == null) {
				throw new MailBoxConfigurationServicesException(Messages.GUID_NOT_AVAIL, Response.Status.BAD_REQUEST);
			}

			if (!mailboxDTO.getName().equals(retrievedMailBox.getMbxName())) {
				// Getting the mailbox.
			    MailBoxConfigurationDAO configDao = new MailBoxConfigurationDAOBase();
				MailBox retrievedEntity = configDao.findByMailBoxNameAndTenancyKeyName(mailboxDTO.getName(),
						mailboxDTO.getTenancyKey());

				if (null != retrievedEntity) {
					throw new MailBoxConfigurationServicesException(Messages.ENTITY_ALREADY_EXIST, MAILBOX,
							Response.Status.CONFLICT);
				}
			}

			// Removing the child items.
			retrievedMailBox.getMailboxProperties().clear();
			//Flush required to avoid unique constraint violation exception
            //This is because of hibernate query execution order
			em.flush();

			// updates the mail box data
            mailboxDTO.copyToEntity(retrievedMailBox);

            //Mailbox properties
            MailBoxProperty property = null;
            Set<MailBoxProperty> properties = new HashSet<>();
            for (PropertyDTO propertyDTO : mailboxDTO.getProperties()) {
                property = new MailBoxProperty();
                property.setMailbox(retrievedMailBox);
                propertyDTO.copyToEntity(property, true);
                properties.add(property);

            }
            retrievedMailBox.getMailboxProperties().addAll(properties);

			// updates the mail box data
			mailboxDTO.copyToEntity(retrievedMailBox);

			// retrieve the actual tenancykey guids from DTO
			List<String> tenancyKeyGuids = MailBoxUtil.getTenancyKeyGuids(aclManifestJson);

			if (tenancyKeyGuids.isEmpty()) {
				LOG.error("retrieval of tenancy key from acl manifest failed");
			}

			// checking if the tenancy key in acl manifest matches with tenancy key in mailbox
			if (!tenancyKeyGuids.contains(retrievedMailBox.getTenancyKey())) {
				LOG.error("Tenancy Key present in Manifest does not match the Tenancy Key of mailbox.");
			}

			//Merge the changes and commit the transaction
			em.merge(retrievedMailBox);

			ServiceInstanceDAO serviceInstanceDAO = new ServiceInstanceDAOBase();
			ServiceInstance serviceInstance = serviceInstanceDAO.findById(serviceInstanceId);
			if (serviceInstance == null) {
				serviceInstance = new ServiceInstance();
				serviceInstance.setName(serviceInstanceId);
				serviceInstance.setPguid(MailBoxUtil.getGUID());
				em.persist(serviceInstance);
			}

			MailboxServiceInstanceDAO msiDao = new MailboxServiceInstanceDAOBase();
			MailboxServiceInstance mailboxServiceInstance = msiDao.findByGuids(guid, serviceInstance.getPguid());
			if (mailboxServiceInstance == null) {

				// Creates relationship mailbox and service instance id
				MailboxServiceInstance msi = new MailboxServiceInstance();
				msi.setPguid(MailBoxUtil.getGUID());
				msi.setServiceInstance(serviceInstance);
				msi.setMailbox(retrievedMailBox);
				em.persist(msi);
			}

			//commit the transaction
			tx.commit();

			// response message construction
			serviceResponse.setResponse(new ResponseDTO(Messages.REVISED_SUCCESSFULLY, MAILBOX, Messages.SUCCESS));
			serviceResponse.setMailBox(new MailBoxResponseDTO(String.valueOf(retrievedMailBox.getPrimaryKey())));
			LOG.debug("Exit from revise mailbox.");
			return serviceResponse;

		} catch (MailBoxConfigurationServicesException e) {

		    if (tx != null && tx.isActive()) {
                tx.rollback();
            }
			LOG.error(Messages.REVISE_OPERATION_FAILED.name(), e);
			serviceResponse.setResponse(new ResponseDTO(Messages.REVISE_OPERATION_FAILED, MAILBOX, Messages.FAILURE,
					e.getMessage()));
			return serviceResponse;
		} catch (IOException e) {
		    if (tx.isActive()) {
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
	 * Method revise the mailbox configurations.
	 *
	 * @param guid The mailbox pguid.
	 * @throws IOException
	 */
	public DeActivateMailBoxResponseDTO deactivateMailBox(String guid, String aclManifestJson)
			throws IOException {

		LOG.debug("Entering into deactivate mailbox.");
		DeActivateMailBoxResponseDTO serviceResponse = new DeActivateMailBoxResponseDTO();
		try {

			LOG.info("The deactivate request is for {} ", guid);

			MailBoxConfigurationDAO configDao = new MailBoxConfigurationDAOBase();

			MailBox retrievedMailBox = configDao.find(MailBox.class, guid);
			if (retrievedMailBox == null) {
				throw new MailBoxConfigurationServicesException(Messages.MBX_DOES_NOT_EXIST, guid,
						Response.Status.BAD_REQUEST);
			}

			// retrieve the actual tenancykey guids from DTO
			List<String> tenancyKeyGuids = MailBoxUtil.getTenancyKeyGuids(aclManifestJson);

			if (tenancyKeyGuids.isEmpty()) {
				LOG.error("retrieval of tenancy key from acl manifest failed");
			}

			// checking if the tenancy key in acl manifest matches with tenancy key in mailbox
			if (!tenancyKeyGuids.contains(retrievedMailBox.getTenancyKey())) {
				LOG.error("Tenancy Key present in Manifest does not match the Tenancy Key of mailbox.");
			}
			// Changing the mailbox status
			retrievedMailBox.setMbxStatus(EntityStatus.INACTIVE.value());
			configDao.merge(retrievedMailBox);

			// response message construction
			serviceResponse.setResponse(new ResponseDTO(Messages.DEACTIVATION_SUCCESSFUL, MAILBOX, Messages.SUCCESS));
			serviceResponse.setMailBox(new MailBoxResponseDTO(guid));
			LOG.debug("Exit from deactivate mailbox.");
			return serviceResponse;

		} catch (MailBoxConfigurationServicesException e) {

			LOG.error(Messages.DEACTIVATION_FAILED.value(), e);
			serviceResponse.setResponse(new ResponseDTO(Messages.DEACTIVATION_FAILED, MAILBOX, Messages.FAILURE,
					e.getMessage()));
			return serviceResponse;

		}
	}

	/**
	 * Searches the mailbox using mailbox name and profile name.
	 *
	 * @param mbxName The name of the mailbox
	 *
	 * @param profName The name of the profile
	 * @param serviceInstId
	 *
	 * @return The SearchMailBoxResponseDTO
	 * @throws IOException
	 * @throws JAXBException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 */
	public SearchMailBoxResponseDTO searchMailBox(GenericSearchFilterDTO searchFilter, String aclManifestJson)
			throws JsonParseException, JsonMappingException, JAXBException, IOException {

		LOG.debug("Entering into search mailbox.");

		int totalCount = 0;
		List<MailBox> retrievedMailBoxes = null;
		Map<String, Integer> pageOffsetDetails = null;
		SearchMailBoxResponseDTO serviceResponse = new SearchMailBoxResponseDTO();

		try {
			
			// Getting mailbox
			MailBoxConfigurationDAO configDao = new MailBoxConfigurationDAOBase();
			ProcessorConfigurationDAO dao = new ProcessorConfigurationDAOBase();

			List<MailBox> mailboxes = new ArrayList<MailBox>();
			List<String> tenancyKeyGuids = new ArrayList<String>();
			
			if (!searchFilter.isDisableFilters()) {

				// check if service instance id is available in query param if not throw an exception
				if (MailBoxUtil.isEmpty(searchFilter.getServiceInstanceId())) {
					LOG.error(Messages.SERVICE_INSTANCE_ID_NOT_AVAILABLE);
				}	
				
				// retrieve the actual tenancykey guids from DTO
				tenancyKeyGuids = MailBoxUtil.getTenancyKeyGuids(aclManifestJson);
	
				if (tenancyKeyGuids.isEmpty()) {
					LOG.error("retrieval of tenancy key from acl manifest failed");
					throw new MailBoxConfigurationServicesException(Messages.TENANCY_KEY_RETRIEVAL_FAILED,
							Response.Status.BAD_REQUEST);
				}
			}
	
			if (!MailBoxUtil.isEmpty(searchFilter.getProfileName())) {
	
				totalCount = configDao.getMailboxCountByProfile(searchFilter, tenancyKeyGuids);
				pageOffsetDetails = MailBoxUtil.getPagingOffsetDetails(searchFilter.getPage(),
						searchFilter.getPageSize(), totalCount);
				retrievedMailBoxes = configDao.find(searchFilter, tenancyKeyGuids, pageOffsetDetails);
	
			} else {
	
				// If the profile name is empty it will use findByName
				totalCount = configDao.getMailboxCountByName(searchFilter, tenancyKeyGuids);
				pageOffsetDetails = MailBoxUtil.getPagingOffsetDetails(searchFilter.getPage(),
						searchFilter.getPageSize(), totalCount);
				retrievedMailBoxes = configDao.findByName(searchFilter, tenancyKeyGuids, pageOffsetDetails);
			}			
			
			mailboxes.addAll(retrievedMailBoxes);
			serviceResponse.setTotalItems(totalCount);

			// Constructing the SearchMailBoxDTO from retrieved mailboxes
			List<SearchMailBoxDTO> searchMailBoxDTOList = new ArrayList<SearchMailBoxDTO>();
			SearchMailBoxDTO serachMailBoxDTO = null;
			
			long lStartTime = System.currentTimeMillis();
						
			for (MailBox mbx : mailboxes) {

                serachMailBoxDTO = new SearchMailBoxDTO();
                serachMailBoxDTO.copyFromEntity(mbx,
                        dao.isMailboxHasProcessor(mbx.getPguid(), searchFilter.getServiceInstanceId()));
                searchMailBoxDTOList.add(serachMailBoxDTO);
            }
			
			long lEndTime = System.currentTimeMillis();
			 
			long difference = lEndTime - lStartTime;
		 
			LOG.info("Elapsed time in milliseconds: " + difference);
			// Constructing the responses.
			serviceResponse.setMailBox(searchMailBoxDTOList);
			serviceResponse.setDisableFilter(searchFilter.isDisableFilters());
			serviceResponse.setResponse(new ResponseDTO(Messages.SEARCH_SUCCESSFUL, MAILBOX, Messages.SUCCESS));
			
			LOG.debug("Exit from search mailbox.");
			return serviceResponse;

		} catch (MailBoxConfigurationServicesException e) {

			LOG.error(Messages.READ_OPERATION_FAILED.name(), e);
			serviceResponse.setResponse(new ResponseDTO(Messages.SEARCH_OPERATION_FAILED, MAILBOX, Messages.FAILURE,
					e.getMessage()));
			return serviceResponse;
		}

	}

	/**
	 * Service to list the directory structure for browse component.
	 *
	 * @param file The root directory location
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
	public GetPropertiesValueResponseDTO readPropertiesFile() {
		LOG.debug("Entering into readPropertiesFile.");

		GetPropertiesValueResponseDTO serviceResponse = new GetPropertiesValueResponseDTO();
		PropertiesFileDTO dto = new PropertiesFileDTO();

		try {

		    DecryptableConfiguration config = MailBoxUtil.getEnvironmentProperties();
			dto.setListJobsIntervalInHours(config.getString(MailBoxConstants.DEFAULT_JOB_SEARCH_PERIOD_IN_HOURS));
			dto.setFsmEventCheckIntervalInSeconds(config.getString(
			        MailBoxConstants.DEFAULT_INTERRUPT_SIGNAL_FREQUENCY_IN_SEC));
			dto.setProcessorSyncUrlDisplayPrefix(config.getString(MailBoxConstants.PROCESSOR_SYNC_URL_DISPLAY_PREFIX));
			dto.setProcessorAsyncUrlDisplayPrefix(config.getString(MailBoxConstants.PROCESSOR_ASYNC_URL_DISPLAY_PREFIX));
			dto.setDefaultScriptTemplateName(config.getString(MailBoxConstants.DEFAULT_SCRIPT_TEMPLATE_NAME));

			serviceResponse.setProperties(dto);
			serviceResponse.setResponse(new ResponseDTO(Messages.READ_JAVA_PROPERTIES_SUCCESSFULLY, MAILBOX,
					Messages.SUCCESS));
			LOG.debug("Exit from readPropertiesFile.");
			return serviceResponse;

		} catch (Exception e) {

			LOG.error(Messages.READ_OPERATION_FAILED.name(), e);
			serviceResponse.setResponse(new ResponseDTO(Messages.READ_JAVA_PROPERTIES_FAILED, MAILBOX,
					Messages.FAILURE, e.getMessage()));
			return serviceResponse;
		}

	}
	
	
	/**
	 * Method to read Mailbox details based on given mailbox guid or name
	 * 
	 * @param guid
	 * @return Mailbox
	 * @throws IOException
	 * @throws JAXBException
	 * @throws SymmetricAlgorithmException
	 */
	public GetMailBoxResponseDTO readMailbox(String guid) throws IOException, JAXBException, SymmetricAlgorithmException {

		LOG.debug("Entering into read mailbox.");
		LOG.info("The retrieve guid or name  is {} ", guid);

		GetMailBoxResponseDTO serviceResponse = new GetMailBoxResponseDTO();

		try {

			// Getting mailbox
			MailBoxConfigurationDAO configDao = new MailBoxConfigurationDAOBase();
			
			if (null == guid) {
				throw new MailBoxConfigurationServicesException(Messages.MANDATORY_FIELD_MISSING, "Maibox Id or Name",
						Response.Status.BAD_REQUEST);
			}

			// while retrieving mailbox first preference will be given to guid.
			// if mailbox is null then we will try to retrieve mailbox by name
			MailBox mailBox = configDao.find(MailBox.class, guid);
			if (null == mailBox) {
				mailBox = configDao.getMailboxByName(guid);
				if (null == mailBox) {
					throw new MailBoxConfigurationServicesException(Messages.NO_SUCH_COMPONENT_EXISTS, MAILBOX,
							Response.Status.BAD_REQUEST);
				}
			}

			ProcessorConfigurationDAO processorDao = new ProcessorConfigurationDAOBase();
			Set<Processor> processors = processorDao.findProcessorByMbx(mailBox.getPguid(), false);
			mailBox.setMailboxProcessors(processors);

			MailBoxDTO dto = new MailBoxDTO();
			dto.copyFromEntity(mailBox);

			serviceResponse.setMailBox(dto);
			serviceResponse.setResponse(new ResponseDTO(Messages.READ_SUCCESSFUL, MAILBOX, Messages.SUCCESS));
			LOG.debug("Exit from read mailbox.");
			return serviceResponse;

		} catch (MailBoxConfigurationServicesException e) {

			LOG.error(Messages.READ_OPERATION_FAILED.name(), e);
			serviceResponse.setResponse(new ResponseDTO(Messages.READ_OPERATION_FAILED, MAILBOX, Messages.FAILURE,
					e.getMessage()));
			return serviceResponse;
		}

	}

}
