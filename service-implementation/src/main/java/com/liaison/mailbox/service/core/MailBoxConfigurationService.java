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
import java.util.Set;

import javax.xml.bind.JAXBException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

import com.liaison.commons.security.pkcs12.SymmetricAlgorithmException;
import com.liaison.mailbox.enums.MailBoxStatus;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.jpa.dao.MailBoxConfigurationDAO;
import com.liaison.mailbox.jpa.dao.MailBoxConfigurationDAOBase;
import com.liaison.mailbox.jpa.dao.MailboxServiceInstanceDAO;
import com.liaison.mailbox.jpa.dao.MailboxServiceInstanceDAOBase;
import com.liaison.mailbox.jpa.dao.ProcessorConfigurationDAO;
import com.liaison.mailbox.jpa.dao.ProcessorConfigurationDAOBase;
import com.liaison.mailbox.jpa.dao.ServiceInstanceDAO;
import com.liaison.mailbox.jpa.dao.ServiceInstanceDAOBase;
import com.liaison.mailbox.jpa.model.MailBox;
import com.liaison.mailbox.jpa.model.MailboxServiceInstance;
import com.liaison.mailbox.jpa.model.Processor;
import com.liaison.mailbox.jpa.model.ServiceInstance;
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

	private static final GenericValidator validator = new GenericValidator();

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
	public AddMailBoxResponseDTO createMailBox(AddMailboxRequestDTO request, String serviceInstanceId, String aclManifestJson) throws MailBoxConfigurationServicesException, JsonParseException, JsonMappingException, JAXBException, IOException{

		LOG.info("Entering into create mailbox.");
		AddMailBoxResponseDTO serviceResponse = new AddMailBoxResponseDTO();
		try {

			MailBoxDTO mailboxDTO = request.getMailBox();
			if (mailboxDTO == null) {
				throw new MailBoxConfigurationServicesException(Messages.INVALID_REQUEST);
			}

			// validation
			validator.validate(mailboxDTO);
			for (PropertyDTO property : mailboxDTO.getProperties()) {
				validator.validate(property);
			}

			MailBox mailBox = new MailBox();
			mailboxDTO.copyToEntity(mailBox);
			mailBox.setPguid(MailBoxUtil.getGUID());
			
			/*String serviceInstanceId = MailBoxUtil.getPrimaryServiceInstanceIdFromACLManifest(aclManifestJson);
			if (MailBoxUtil.isEmpty(serviceInstanceId)) {
				 throw new MailBoxConfigurationServicesException(Messages.SERVICE_INSTANCE_ID_RETRIEVAL_FAILED);
			}*/
			
			// retrieve the tenancy key from acl manifest
			List <TenancyKeyDTO> tenancyKeys = MailBoxUtil.getTenancyKeysFromACLManifest(aclManifestJson);
			if (tenancyKeys.isEmpty()) {
				 LOG.error("retrieval of tenancy key from acl manifest failed");
				// throw new MailBoxConfigurationServicesException(Messages.TENANCY_KEY_RETRIEVAL_FAILED);
			}
						
			//creating a link between mailbox and service instance table
			createMailboxServiceInstanceIdLink(serviceInstanceId, mailBox);

			// persisting the mailbox entity
			MailBoxConfigurationDAO configDao = new MailBoxConfigurationDAOBase();
			configDao.persist(mailBox);

			// response message construction
			serviceResponse.setResponse(new ResponseDTO(Messages.CREATED_SUCCESSFULLY, MAILBOX, Messages.SUCCESS));
			serviceResponse.setMailBox(new MailBoxResponseDTO(String.valueOf(mailBox.getPrimaryKey())));
			LOG.info("Exit from create mailbox.");
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
	 *           The serviceInstanceID of the mailbox
	 * @param mailbox
	 * @throws MailBoxConfigurationServicesException
	 */
	public void createMailboxServiceInstanceIdLink(String serviceInstanceID, MailBox mailbox) throws MailBoxConfigurationServicesException {

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
			MailboxServiceInstance mailboxServiceInstance = msiDao.findByGuids(mailbox.getPguid(), serviceInstance.getPguid());
			
			List<MailboxServiceInstance> mbxServiceInstances = new ArrayList<MailboxServiceInstance>();
			if (mailboxServiceInstance == null) {
				//Creates relationship mailbox and service instance id
				MailboxServiceInstance msi = new MailboxServiceInstance();
				msi.setPguid(MailBoxUtil.getGUID());
				msi.setServiceInstance(serviceInstance);
				mbxServiceInstances.add(msi);
				mailbox.setMailboxServiceInstances(mbxServiceInstances);
			} 
		} catch (Exception e) {
			throw new MailBoxConfigurationServicesException("Invalid service instance id.");
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
	public GetMailBoxResponseDTO getMailBox(String guid, boolean addConstraint, String serviceInstanceId, String aclManifestJson) throws JsonParseException, JsonMappingException, JAXBException,
		IOException, SymmetricAlgorithmException {

		LOG.info("Entering into get mailbox.");
		LOG.info("The retrieve guid is {} ", guid);
		
		GetMailBoxResponseDTO serviceResponse = new GetMailBoxResponseDTO();
		
		try {

			// Getting mailbox
			MailBoxConfigurationDAO configDao = new MailBoxConfigurationDAOBase();
			
			// retrieve the tenancy key from acl manifest
			List<TenancyKeyDTO> tenancyKeys = MailBoxUtil.getTenancyKeysFromACLManifest(aclManifestJson);
			if (tenancyKeys.isEmpty()) {
				 LOG.error("retrieval of tenancy key from acl manifest failed");
				 //throw new MailBoxConfigurationServicesException(Messages.TENANCY_KEY_RETRIEVAL_FAILED);
			}
			
			MailBox mailBox = configDao.find(MailBox.class, guid);

			if (mailBox == null) {
				throw new MailBoxConfigurationServicesException(Messages.MBX_DOES_NOT_EXIST, guid);
			}
			
			// retrieve the actual tenancykey guids from DTO
			List <String> tenancyKeyGuids = MailBoxUtil.getTenancyKeyGuidsFromTenancyKeys(tenancyKeys);
			
			// checking if the tenancy key in acl manifest matches with tenancy key in mailbox
			if (!tenancyKeyGuids.contains(mailBox.getTenancyKey().toLowerCase())) {
				LOG.error("Tenancy Key present Manifest does not match the Tenancy Key of mailbox.");
				//throw new MailBoxConfigurationServicesException(Messages.TENANCY_KEY_MISMATCH);
			}
			

			ProcessorConfigurationDAO processorDao = new ProcessorConfigurationDAOBase();
			if (addConstraint) {
				List<Processor> filteredProcessor = processorDao.findProcessorByMbxAndServiceInstance(mailBox.getPguid(), serviceInstanceId);
				mailBox.setMailboxProcessors(filteredProcessor);
			} else {
				List<Processor> processors = processorDao.findProcessorByMbx(mailBox.getPguid());
				mailBox.setMailboxProcessors(processors);
			}

			MailBoxDTO dto = new MailBoxDTO();
			dto.copyFromEntity(mailBox);

			serviceResponse.setMailBox(dto);
			serviceResponse.setResponse(new ResponseDTO(Messages.READ_SUCCESSFUL, MAILBOX, Messages.SUCCESS));
			LOG.info("Exit from get mailbox.");
			return serviceResponse;

		} catch (MailBoxConfigurationServicesException e) {
		
			LOG.error(Messages.READ_OPERATION_FAILED.name(), e);
			serviceResponse
					.setResponse(new ResponseDTO(Messages.READ_OPERATION_FAILED, MAILBOX, Messages.FAILURE, e.getMessage()));
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
	public ReviseMailBoxResponseDTO reviseMailBox(ReviseMailBoxRequestDTO request, String guid, String serviceInstanceId, String aclManifestJson) throws JsonParseException, JsonMappingException, JAXBException, IOException {

		LOG.info("Entering into revise mailbox.The revise request is for {} ", guid);

		ReviseMailBoxResponseDTO serviceResponse = new ReviseMailBoxResponseDTO();

		try {

			MailBoxDTO mailboxDTO = request.getMailBox();
			if (mailboxDTO == null) {
				throw new MailBoxConfigurationServicesException(Messages.INVALID_REQUEST);
			}

			// Validation
			validator.validate(mailboxDTO);
			for (PropertyDTO property : mailboxDTO.getProperties()) {
				validator.validate(property);
			}

			if (!guid.equals(mailboxDTO.getGuid())) {
				throw new MailBoxConfigurationServicesException(Messages.GUID_DOES_NOT_MATCH, MAILBOX);
			}

			// Getting the mailbox.
			MailBoxConfigurationDAO configDao = new MailBoxConfigurationDAOBase();
			MailBox retrievedMailBox = configDao.find(MailBox.class, guid);
			if (retrievedMailBox == null) {
				throw new MailBoxConfigurationServicesException(Messages.GUID_NOT_AVAIL);
			}


			// Removing the child items.
			retrievedMailBox.getMailboxProperties().clear();
			
			// updates the mail box data
			mailboxDTO.copyToEntity(retrievedMailBox);
			
			// retrieve the tenancy key from acl manifest
			List<TenancyKeyDTO> tenancyKeys = MailBoxUtil.getTenancyKeysFromACLManifest(aclManifestJson);
			if (tenancyKeys.isEmpty()) {
				 LOG.error("retrieval of tenancy key from acl manifest failed");
				 //throw new MailBoxConfigurationServicesException(Messages.TENANCY_KEY_RETRIEVAL_FAILED);
			}
			
			// retrieve the actual tenancykey guids from DTO
			List <String> tenancyKeyGuids = MailBoxUtil.getTenancyKeyGuidsFromTenancyKeys(tenancyKeys);
			
			// checking if the tenancy key in acl manifest matches with tenancy key in mailbox
			if (!tenancyKeyGuids.contains(retrievedMailBox.getTenancyKey().toLowerCase())) {
				LOG.error("Tenancy Key present Manifest does not match the Tenancy Key of mailbox.");
				//throw new MailBoxConfigurationServicesException(Messages.TENANCY_KEY_MISMATCH);
			}
						
			configDao.merge(retrievedMailBox);
			
			// retrieve the service instance id from acl manifest
			/*String serviceInstanceId = MailBoxUtil.getPrimaryServiceInstanceIdFromACLManifest(aclManifestJson);
			if (MailBoxUtil.isEmpty(serviceInstanceId)) {
				 LOG.error("retrieval of service instance id from acl manifest failed");
				 throw new MailBoxConfigurationServicesException(Messages.SERVICE_INSTANCE_ID_RETRIEVAL_FAILED);
			}*/
			
			//creating a link between mailbox and service instance table
//			createMailboxServiceInstanceIdLink(c, retrievedMailBox);
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
				
				//Creates relationship mailbox and service instance id
				MailboxServiceInstance msi = new MailboxServiceInstance();
				msi.setPguid(MailBoxUtil.getGUID());
				msi.setServiceInstance(serviceInstance);
				msi.setMailbox(retrievedMailBox);
				msiDao.persist(msi);
			}
			
			// response message construction
			serviceResponse.setResponse(new ResponseDTO(Messages.REVISED_SUCCESSFULLY, MAILBOX, Messages.SUCCESS));
			serviceResponse.setMailBox(new MailBoxResponseDTO(String.valueOf(retrievedMailBox.getPrimaryKey())));
			LOG.info("Exit from revise mailbox.");
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

		LOG.info("Entering into deactivate mailbox.");
		DeActivateMailBoxResponseDTO serviceResponse = new DeActivateMailBoxResponseDTO();
		try {

			LOG.info("The deactivate request is for {} ", guid);

			MailBoxConfigurationDAO configDao = new MailBoxConfigurationDAOBase();
						
			MailBox retrievedMailBox = configDao.find(MailBox.class, guid);
			if (retrievedMailBox == null) {
				throw new MailBoxConfigurationServicesException(Messages.MBX_DOES_NOT_EXIST, guid);
			}
			
			// retrieve the tenancy key from acl manifest
			List<TenancyKeyDTO> tenancyKeys = MailBoxUtil.getTenancyKeysFromACLManifest(aclManifestJson);
			if (tenancyKeys.isEmpty()) {
				 LOG.error("retrieval of tenancy key from acl manifest failed");
				 //throw new MailBoxConfigurationServicesException(Messages.TENANCY_KEY_RETRIEVAL_FAILED);
			}
			
			// retrieve the actual tenancykey guids from DTO
			List <String> tenancyKeyGuids = MailBoxUtil.getTenancyKeyGuidsFromTenancyKeys(tenancyKeys);
			
			// checking if the tenancy key in acl manifest matches with tenancy key in mailbox
			if (!tenancyKeyGuids.contains(retrievedMailBox.getTenancyKey().toLowerCase())) {
				LOG.error("Tenancy Key present Manifest does not match the Tenancy Key of mailbox.");
				//throw new MailBoxConfigurationServicesException(Messages.TENANCY_KEY_MISMATCH);
			}
			
			// Changing the mailbox status
			retrievedMailBox.setMbxStatus(MailBoxStatus.INACTIVE.value());
			configDao.merge(retrievedMailBox);

			// response message construction
			serviceResponse.setResponse(new ResponseDTO(Messages.DEACTIVATION_SUCCESSFUL, MAILBOX, Messages.SUCCESS));
			serviceResponse.setMailBox(new MailBoxResponseDTO(guid));
			LOG.info("Exit from deactivate mailbox.");
			return serviceResponse;

		} catch (MailBoxConfigurationServicesException e) {

			LOG.error(Messages.DEACTIVATION_FAILED.value(), e);
			serviceResponse.setResponse(new ResponseDTO(Messages.DEACTIVATION_FAILED, MAILBOX, Messages.FAILURE, e.getMessage()));
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
	 * 
	 * @return The SearchMailBoxResponseDTO
	 * @throws IOException 
	 * @throws JAXBException 
	 * @throws JsonMappingException 
	 * @throws JsonParseException 
	 */
	public SearchMailBoxResponseDTO searchMailBox(String mbxName, String profName, String aclManifestJson) throws JsonParseException, JsonMappingException, JAXBException, IOException {

		LOG.info("Entering into search mailbox.");

		SearchMailBoxResponseDTO serviceResponse = new SearchMailBoxResponseDTO();

		try {
						
			// Getting mailbox
			MailBoxConfigurationDAO configDao = new MailBoxConfigurationDAOBase();
			ProcessorConfigurationDAO dao = new ProcessorConfigurationDAOBase();

			Set<MailBox> mailboxes = new HashSet<>();
			
			// retrieve tenancy key from acl manifest
			List <TenancyKeyDTO> tenancyKeys = MailBoxUtil.getTenancyKeysFromACLManifest(aclManifestJson);
			
			if (tenancyKeys.isEmpty()) {
				 LOG.error("retrieval of tenancy key from acl manifest failed");
				 throw new MailBoxConfigurationServicesException(Messages.TENANCY_KEY_RETRIEVAL_FAILED);
			}
			List <String> tenancyKeyGuids = MailBoxUtil.getTenancyKeyGuidsFromTenancyKeys(tenancyKeys);
			
			if (!MailBoxUtil.isEmpty(profName)) {

				Set<MailBox> retrievedMailBoxes = configDao.find(mbxName, profName, tenancyKeyGuids);
				mailboxes.addAll(retrievedMailBoxes);
			}

			// If the profile name is empty it will use findByName
			if (MailBoxUtil.isEmpty(profName) && !MailBoxUtil.isEmpty(mbxName)) {

				Set<MailBox> retrievedMailBoxes = configDao.findByName(mbxName, tenancyKeyGuids);
				mailboxes.addAll(retrievedMailBoxes);
			}

			if (MailBoxUtil.isEmpty(profName) && MailBoxUtil.isEmpty(mbxName)){
				throw new MailBoxConfigurationServicesException(Messages.INVALID_DATA);
			}

			// Constructing the SearchMailBoxDTO from retrieved mailboxes
			List<SearchMailBoxDTO> searchMailBoxDTOList = new ArrayList<SearchMailBoxDTO>();
			SearchMailBoxDTO serachMailBoxDTO = null;
			for (MailBox mbx : mailboxes) {

				serachMailBoxDTO = new SearchMailBoxDTO();
				serachMailBoxDTO.copyFromEntity(mbx, dao.isMailboxHasProcessor(mbx.getPguid()));
				searchMailBoxDTOList.add(serachMailBoxDTO);
			}

			// Constructing the responses.
			serviceResponse.setMailBox(searchMailBoxDTOList);
			serviceResponse.setResponse(new ResponseDTO(Messages.SEARCH_SUCCESSFUL, MAILBOX, Messages.SUCCESS));
			LOG.info("Exit from search mailbox.");
			return serviceResponse;

		} catch (MailBoxConfigurationServicesException e) {

			LOG.error(Messages.READ_OPERATION_FAILED.name(), e);
			serviceResponse
					.setResponse(new ResponseDTO(Messages.SEARCH_OPERATION_FAILED, MAILBOX, Messages.FAILURE, e.getMessage()));
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
	public GetPropertiesValueResponseDTO getValuesFromPropertiesFile() throws IOException {
	
		GetPropertiesValueResponseDTO serviceResponse = new GetPropertiesValueResponseDTO(); 

		try {
			
			PropertiesFileDTO dto = new PropertiesFileDTO();
			
			String globalTrustStoreId 	   = MailBoxUtil.getEnvironmentProperties().getString("mailbox.global.truststore.id");
			String globalTrustStoreGroupId = MailBoxUtil.getEnvironmentProperties().getString("mailbox.global.trustgroup.id");
			String gitlabHost = MailBoxUtil.getEnvironmentProperties().getString("com.liaison.gitlab.script.server.host");
			String gitlabPort = MailBoxUtil.getEnvironmentProperties().getString("com.liaison.gitlab.script.server.port");
			String gitlabProjectName = MailBoxUtil.getEnvironmentProperties().getString("com.liaison.gitlab.script.project.name");
			String gitlabBranchName = MailBoxUtil.getEnvironmentProperties().getString("com.liaison.gitlab.script.branch.name");
			String listJobsIntervalInHours = MailBoxUtil.getEnvironmentProperties().getString("default.job.search.period.in.hours");
			String fsmEventCheckIntervalInSeconds = MailBoxUtil.getEnvironmentProperties().getString("check.for.interrupt.signal.frequency.in.sec");
			String mailboxPguidDisplayPrefix = MailBoxUtil.getEnvironmentProperties().getString("maibox.pguid.display.prefix");
			
			dto.setTrustStoreId(globalTrustStoreId);
			dto.setTrustStoreGroupId(globalTrustStoreGroupId);
			dto.setGitlabHost(gitlabHost);
			dto.setGitlabPort(gitlabPort);
			dto.setGitlabBranchName(gitlabBranchName);
			dto.setGitlabProjectName(gitlabProjectName);
			dto.setListJobsIntervalInHours(listJobsIntervalInHours);
			dto.setFsmEventCheckIntervalInSeconds(fsmEventCheckIntervalInSeconds);
			dto.setMailboxPguidDisplayPrefix(mailboxPguidDisplayPrefix);
			
			serviceResponse.setProperties(dto);
			
			serviceResponse.setResponse(new ResponseDTO(Messages.READ_JAVA_PROPERTIES_SUCCESSFULLY, MAILBOX, Messages.SUCCESS));
			
			return serviceResponse;
		
		} catch (IOException e) {
	
			LOG.error(Messages.READ_OPERATION_FAILED.name(), e);
			serviceResponse.setResponse(new ResponseDTO(Messages.READ_JAVA_PROPERTIES_FAILED, MAILBOX, Messages.FAILURE, e.getMessage()));
			return serviceResponse;
		}
		
	}
}
