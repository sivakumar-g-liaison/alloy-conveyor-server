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
import com.liaison.mailbox.service.dto.configuration.request.ReviseMailBoxRequestDTO;
import com.liaison.mailbox.service.dto.configuration.response.AddMailBoxResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.DeActivateMailBoxResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.GetMailBoxResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.GetPropertiesValueResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.ReviseMailBoxResponseDTO;
import com.liaison.mailbox.service.dto.ui.SearchMailBoxDTO;
import com.liaison.mailbox.service.dto.ui.SearchMailBoxResponseDTO;
import com.liaison.mailbox.service.dto.ui.SearchMailBoxDetailedResponseDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.util.MailBoxUtil;
import com.liaison.mailbox.service.util.ServiceBrokerUtil;
import com.liaison.mailbox.service.validation.GenericValidator;

import static com.liaison.mailbox.MailBoxConstants.SERVICE_INSTANCE;
import static com.liaison.mailbox.enums.Messages.ID_IS_INVALID;

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
	 * @param userId 
	 * @return The responseDTO.
	 * @throws IOException
	 * @throws JAXBException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 */
	public AddMailBoxResponseDTO createMailBox(AddMailboxRequestDTO request, String serviceInstanceId,
			String aclManifestJson, String userId)
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

            // service instance id validation
            String response = ServiceBrokerUtil.getEntity(SERVICE_INSTANCE, serviceInstanceId);
            if (MailBoxUtil.isEmpty(response)) {
                throw new MailBoxConfigurationServicesException(
                        ID_IS_INVALID,
                        SERVICE_INSTANCE,
                        Response.Status.BAD_REQUEST);
            }

            // validation
            GenericValidator validator = new GenericValidator();
			validator.validate(mailboxDTO);
			for (PropertyDTO property : mailboxDTO.getProperties()) {
				validator.validate(property);
			}

			MailBox mailBox = new MailBox();
			mailboxDTO.copyToEntity(mailBox);
			// validating the mailbox guid
			if (MailBoxUtil.isEmpty(mailboxDTO.getGuid())) {
				mailBox.setPguid(MailBoxUtil.getGUID());
			} else {
				mailBox.setPguid(mailboxDTO.getGuid());
			}			

			mailBox.setOriginatingDc(MailBoxUtil.DATACENTER_NAME);
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
			
			mailBox.setModifiedBy(userId);
			mailBox.setModifiedDate(new Timestamp(System.currentTimeMillis()));

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
				serviceInstance.setOriginatingDc(MailBoxUtil.DATACENTER_NAME);
				serviceInstanceDAO.persist(serviceInstance);
			}

			MailboxServiceInstanceDAO msiDao = new MailboxServiceInstanceDAOBase();
			int count = msiDao.getMailboxServiceInstanceCount(mailbox.getPguid(), serviceInstance.getPguid());

			Set<MailboxServiceInstance> mbxServiceInstances = new HashSet<MailboxServiceInstance>();
			if (count ==  0) {
				// Creates relationship mailbox and service instance id
				MailboxServiceInstance msi = new MailboxServiceInstance();
				msi.setPguid(MailBoxUtil.getGUID());
				msi.setOriginatingDc(MailBoxUtil.DATACENTER_NAME);
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
	 * @throws IOException
	 */
	public GetMailBoxResponseDTO getMailBox(String guid, boolean addConstraint, String serviceInstanceId,
			String aclManifestJson) throws IOException {

		LOG.debug("Entering into get mailbox.");
		LOG.info("The retrieve guid is {} ", guid);

		GetMailBoxResponseDTO serviceResponse = new GetMailBoxResponseDTO();

		try {

			// check if service instance id is available in query param if not throw an exception
			if (addConstraint && MailBoxUtil.isEmpty(serviceInstanceId)) {
				throw new MailBoxConfigurationServicesException(Messages.SERVICE_INSTANCE_ID_NOT_AVAILABLE,
						Response.Status.BAD_REQUEST);
			}

			// Getting mailbox
			MailBoxConfigurationDAO configDao = new MailBoxConfigurationDAOBase();

			MailBox mailBox = configDao.find(MailBox.class, guid);
			if (mailBox == null || EntityStatus.DELETED.value().equals(mailBox.getMbxStatus())) {
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
	 * @param userId 
	 * @throws IOException
	 * @throws JAXBException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 */
	public ReviseMailBoxResponseDTO reviseMailBox(ReviseMailBoxRequestDTO request, String guid,
			String serviceInstanceId, String aclManifestJson, boolean addConstraint, String userId) throws IOException {

	    EntityManager em = null;
        EntityTransaction tx = null;
		LOG.debug("Entering into revise mailbox.The revise request is for {} ", guid);

		ReviseMailBoxResponseDTO serviceResponse = new ReviseMailBoxResponseDTO();

		try {

		    em = DAOUtil.getEntityManager(MailboxDTDMDAO.PERSISTENCE_UNIT_NAME);

			// check if service instance id is available in query param if not throw an exception
			if (addConstraint && MailBoxUtil.isEmpty(serviceInstanceId)) {
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
			
			ProcessorConfigurationDAO dao = new ProcessorConfigurationDAOBase();
			
			if (EntityStatus.DELETED.value().equals(mailboxDTO.getStatus()) && 
					dao.isMailboxHasProcessor(guid, serviceInstanceId, true)) {
				throw new MailBoxConfigurationServicesException(Messages.MBX_NON_DELETED_PROCESSOR, MAILBOX,
						Response.Status.PRECONDITION_FAILED);
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
			
            if (EntityStatus.DELETED.value().equals(mailboxDTO.getStatus())) {
                retrievedMailBox.setMbxName(mailboxDTO.getName() + MailBoxUtil.getTimestamp().toString());
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

			retrievedMailBox.setModifiedBy(userId);
			retrievedMailBox.setModifiedDate(new Timestamp(System.currentTimeMillis()));
			//Merge the changes and commit the transaction
			em.merge(retrievedMailBox);

			//commit the transaction
			tx.commit();

			// response message construction
            if (EntityStatus.DELETED.value().equals(retrievedMailBox.getMbxStatus())) {
                serviceResponse.setResponse(new ResponseDTO(Messages.DELETED_SUCCESSFULLY, MAILBOX, Messages.SUCCESS));
            } else {
                serviceResponse.setResponse(new ResponseDTO(Messages.REVISED_SUCCESSFULLY, MAILBOX, Messages.SUCCESS));
            }

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
	 * @param userId 
	 * @throws IOException
	 */
	public DeActivateMailBoxResponseDTO deactivateMailBox(String guid, String aclManifestJson, String userId)
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
			
			retrievedMailBox.setModifiedBy(userId);
			retrievedMailBox.setModifiedDate(new Timestamp(System.currentTimeMillis()));
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
     * @param searchFilter search filter contains mailbxo and profile name
     * @param aclManifestJson manifest json
     * @return search response
     * @throws IOException
     */
	public SearchMailBoxDetailedResponseDTO searchMailBox(GenericSearchFilterDTO searchFilter, String aclManifestJson) throws IOException {

		LOG.debug("Entering into search mailbox.");

		int totalCount = 0;
		SearchMailBoxDetailedResponseDTO serviceResponse = new SearchMailBoxDetailedResponseDTO();

		try {
			
			// Getting mailbox
			MailBoxConfigurationDAO configDao = new MailBoxConfigurationDAOBase();
			List<MailBox> mailboxes = new ArrayList<MailBox>();

            List<String> tenancyKeyGuids = getTenancyKeyGuids(searchFilter, aclManifestJson);
            List<MailBox> retrievedMailBoxes = new ArrayList<>();
            totalCount = search(searchFilter, tenancyKeyGuids, retrievedMailBoxes);

			mailboxes.addAll(retrievedMailBoxes);
			serviceResponse.setTotalItems(totalCount);

			// Constructing the SearchMailBoxDTO from retrieved mailboxes
			List<MailBoxDTO> mailBoxDTOList = new ArrayList<MailBoxDTO>();
			MailBoxDTO mailBoxDTO = null;

			String tenancyKeyDisplayName;
			Set<Processor> processors;
			ProcessorConfigurationDAO processorDao = new ProcessorConfigurationDAOBase();
			for (MailBox mbx : mailboxes) {

				processors = processorDao.findProcessorByMbx(mbx.getPguid(), false);
	            mbx.setMailboxProcessors(processors);
	            tenancyKeyDisplayName = MailBoxUtil.getTenancyKeyNameByGuid(aclManifestJson, mbx.getTenancyKey());

                mailBoxDTO = new MailBoxDTO();
                mailBoxDTO.copyFromEntity(mbx);
                mailBoxDTO.setTenancyKeyDisplayName(tenancyKeyDisplayName);
                mailBoxDTOList.add(mailBoxDTO);
            }

			// Constructing the responses.
			serviceResponse.setMailBox(mailBoxDTOList);
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
	 * Searches the mailbox using mailbox name and profile name for UI resonse.
	 *
	 * @param searchFilter search filter contains mailbox and profile name
	 * @param aclManifestJson manifest json which contains tenancy keys
	 *
	 * @return The SearchMailBoxResponseDTO
	 * @throws IOException
	 */
	public SearchMailBoxResponseDTO searchMailBoxUIResponse(GenericSearchFilterDTO searchFilter, String aclManifestJson)
			throws IOException {

		LOG.debug("Entering into search mailbox.");

		int totalCount = 0;
		SearchMailBoxResponseDTO serviceResponse = new SearchMailBoxResponseDTO();

		try {
			
			// Getting mailbox
			ProcessorConfigurationDAO dao = new ProcessorConfigurationDAOBase();

			List<MailBox> mailboxes = new ArrayList<MailBox>();

            List<String> tenancyKeyGuids = getTenancyKeyGuids(searchFilter, aclManifestJson);
            List<MailBox> retrievedMailBoxes = new ArrayList<>();
            totalCount = search(searchFilter, tenancyKeyGuids, retrievedMailBoxes);

			mailboxes.addAll(retrievedMailBoxes);
			serviceResponse.setTotalItems(totalCount);

			// Constructing the SearchMailBoxDTO from retrieved mailboxes
			List<SearchMailBoxDTO> searchMailBoxDTOList = new ArrayList<SearchMailBoxDTO>();
			SearchMailBoxDTO searchMailBoxDTO = null;

			for (MailBox mbx : mailboxes) {

                searchMailBoxDTO = new SearchMailBoxDTO();
                searchMailBoxDTO.copyFromEntity(mbx,
                        dao.isMailboxHasProcessor(mbx.getPguid(), searchFilter.getServiceInstanceId(), searchFilter.isDisableFilters()));
                searchMailBoxDTOList.add(searchMailBoxDTO);
            }

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
     * reads tenancy keys from the manifest
     * @param searchFilter search filter
     * @param aclManifestJson manifest json
     * @return tenancy keys
     * @throws IOException
     */
    private List<String> getTenancyKeyGuids(GenericSearchFilterDTO searchFilter,
                                            String aclManifestJson) throws IOException {

        List<String> tenancyKeyGuids = new ArrayList<>();
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

        return tenancyKeyGuids;
    }

    /**
     * Common method for both UI and MIGRATOR search
     *
     * @param searchFilter search filters
     * @param tenancyKeyGuids tenancy key guids
     * @param retrievedMailBoxes mailbox results
     * @return total count and retrieved mailboxes
     */
    private int search(GenericSearchFilterDTO searchFilter,
                       List<String> tenancyKeyGuids,
                       List<MailBox> retrievedMailBoxes) {

        int totalCount = 0;
        Map<String, Integer> pageOffsetDetails = null;
        List<MailBox> results = null;
        MailBoxConfigurationDAO configDao = new MailBoxConfigurationDAOBase();


        if (!MailBoxUtil.isEmpty(searchFilter.getProfileName())) {

            totalCount = configDao.getMailboxCountByProfile(searchFilter, tenancyKeyGuids);
            pageOffsetDetails = MailBoxUtil.getPagingOffsetDetails(searchFilter.getPage(),
                    searchFilter.getPageSize(),
                    totalCount);
            results = configDao.find(searchFilter, tenancyKeyGuids, pageOffsetDetails);
        } else {

            // If the profile name is empty it will use findByName
            totalCount = configDao.getMailboxCountByName(searchFilter, tenancyKeyGuids);
            pageOffsetDetails = MailBoxUtil.getPagingOffsetDetails(searchFilter.getPage(),
                    searchFilter.getPageSize(), totalCount);
            results = configDao.findByName(searchFilter, tenancyKeyGuids, pageOffsetDetails);
        }

        if (!results.isEmpty()) {
            retrievedMailBoxes.addAll(results);
        }
        return totalCount;
    }

	/**
	 * getting values from java properties file
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
	public GetMailBoxResponseDTO readMailbox(String guid) {

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
			} else if (EntityStatus.DELETED.value().equals(mailBox.getMbxStatus())) {
				throw new MailBoxConfigurationServicesException(Messages.NO_SUCH_COMPONENT_EXISTS, MAILBOX,
						Response.Status.BAD_REQUEST);
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
