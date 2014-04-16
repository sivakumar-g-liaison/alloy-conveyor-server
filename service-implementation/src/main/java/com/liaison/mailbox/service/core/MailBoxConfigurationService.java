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

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.liaison.commons.acl.manifest.dto.ACLManifest;
import com.liaison.commons.security.pkcs7.SymmetricAlgorithmException;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.enums.MailBoxStatus;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.jpa.dao.MailBoxConfigurationDAO;
import com.liaison.mailbox.jpa.dao.MailBoxConfigurationDAOBase;
import com.liaison.mailbox.jpa.dao.MailboxServiceInstanceDAO;
import com.liaison.mailbox.jpa.dao.MailboxServiceInstanceDAOBase;
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
import com.liaison.mailbox.service.dto.configuration.request.AddMailboxRequestDTO;
import com.liaison.mailbox.service.dto.configuration.request.FileInfoDTO;
import com.liaison.mailbox.service.dto.configuration.request.ReviseMailBoxRequestDTO;
import com.liaison.mailbox.service.dto.configuration.request.SearchMailboxRequestDTO;
import com.liaison.mailbox.service.dto.configuration.response.AddMailBoxResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.DeActivateMailBoxResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.GetMailBoxResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.GetPropertiesValueResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.ReviseMailBoxResponseDTO;
import com.liaison.mailbox.service.dto.ui.SearchMailBoxDTO;
import com.liaison.mailbox.service.dto.ui.SearchMailBoxResponseDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.util.MailBoxUtility;
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
	 * @throws JAXBException 
	 */
	public AddMailBoxResponseDTO createMailBox(AddMailboxRequestDTO request, String aclManifestJson) throws MailBoxConfigurationServicesException, IOException, JAXBException {

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
			mailBox.setPguid(MailBoxUtility.getGUID());
			
			// retrieve the service instance id from acl-manifest only if the boolean check "get-serviceinstanceid-from-manifest" is true
			// otherwise use the Dummy service instance id 
			String serviceInstanceId = null;
			String isReadFromACLManifest = MailBoxUtility.getEnvironmentProperties().getString("get-serviceinstanceid-from-manifest");
			if (isReadFromACLManifest.equals("true")) {
				
				if (aclManifestJson == null) {
					LOG.error("acl manifest is missing in the request header");
					throw new MailBoxConfigurationServicesException(Messages.ACL_MANIFEST_MISSING);
				}
				LOG.info("desearializing the acl manifest DTO from manifest json");
				ACLManifest aclManifest = MailBoxUtility.unmarshalFromJSON(aclManifestJson, ACLManifest.class);
				LOG.info("acl Manifest DTO deserialized successfully");
				serviceInstanceId = MailBoxUtility.getServiceInstanceIdFromACLManifest(aclManifest);
				LOG.info("Retrieving the service instance id from acl Manifest DTO");
				if (serviceInstanceId == null) {
					 LOG.error("retrieval of service instance id from acl manifest failed");
					 throw new MailBoxConfigurationServicesException(Messages.SERVICE_INSTANCE_ID_RETRIEVAL_FAILED);
				}
				
			} else {
				LOG.info("Dummy service instance id is set since boolean check 'get-serviceinstanceid-from-manifest' is false");
				serviceInstanceId = MailBoxConstants.DUMMY_SERVICE_INSTANCE_ID;
			}
						
			//creating a link between mailbox and service instance table
			createMailboxServiceInstanceIdLink(serviceInstanceId, mailBox);
			//createMailboxServiceInstanceIdLink(request.getMailBox().getServiceInstanceId(), mailBox);

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
				serviceInstance.setPguid(MailBoxUtility.getGUID());
				serviceInstanceDAO.persist(serviceInstance);
			}
				
			MailboxServiceInstanceDAO msiDao = new MailboxServiceInstanceDAOBase();
			MailboxServiceInstance mailboxServiceInstance = msiDao.findByGuids(mailbox.getPguid(), serviceInstance.getPguid());
			
			List<MailboxServiceInstance> mbxServiceInstances = new ArrayList<MailboxServiceInstance>();
			if (mailboxServiceInstance == null) {
				//Creates relationship mailbox and service instance id
				MailboxServiceInstance msi = new MailboxServiceInstance();
				msi.setPguid(MailBoxUtility.getGUID());
				msi.setServiceInstanceId(serviceInstance);
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
	public GetMailBoxResponseDTO getMailBox(String guid, String  serviceInstanceId, boolean addConstraint) throws JsonParseException, JsonMappingException, JAXBException,
		IOException, SymmetricAlgorithmException {

		LOG.info("Entering into get mailbox.");
		LOG.info("The retrieve guid is {} ", guid);
		
		GetMailBoxResponseDTO serviceResponse = new GetMailBoxResponseDTO();
		
		try {
		
			// Getting mailbox
			MailBoxConfigurationDAO configDao = new MailBoxConfigurationDAOBase();
			MailBox mailBox = configDao.find(MailBox.class, guid);
			List<Processor> filteredProcessors = new ArrayList<Processor>();
			if(addConstraint) {	
				for(Processor proc : mailBox.getMailboxProcessors()) {
					if(proc.getServiceInstance().getName().equals(serviceInstanceId)) {
						filteredProcessors.add(proc);
					}
				}
				mailBox.setMailboxProcessors(filteredProcessors);
			} 
			
			if (mailBox == null) {
				throw new MailBoxConfigurationServicesException(Messages.MBX_DOES_NOT_EXIST, guid);
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
	 * @throws JAXBException 
	 */
	public ReviseMailBoxResponseDTO reviseMailBox(ReviseMailBoxRequestDTO request, String guid, String aclManifestJson) throws JAXBException {

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
			configDao.merge(retrievedMailBox);
			
			// retrieve the service instance id from acl-manifest only if the boolean check "get-serviceinstanceid-from-manifest" is true
			// otherwise use the Dummy service instance id 
			String serviceInstanceId = null;
			LOG.info(MailBoxUtility.getEnvironmentProperties().getString("get-serviceinstanceid-from-manifest"));
			String isReadFromACLManifest = MailBoxUtility.getEnvironmentProperties().getString("get-serviceinstanceid-from-manifest");
			if (isReadFromACLManifest.equals("true")) {
				if (aclManifestJson == null) {
					LOG.error("acl manifest is missing in the request header");
					throw new MailBoxConfigurationServicesException(Messages.ACL_MANIFEST_MISSING);
				}
				LOG.info("desearializing the acl manifest DTO from manifest json");
				ACLManifest aclManifest = MailBoxUtility.unmarshalFromJSON(aclManifestJson, ACLManifest.class);
				LOG.info("acl Manifest DTO deserialized successfully");
				serviceInstanceId = MailBoxUtility.getServiceInstanceIdFromACLManifest(aclManifest);
				LOG.info("Retrieving the service instance id from acl Manifest DTO");
				if (serviceInstanceId == null) {
					 LOG.error("retrieval of service instance id from acl manifest failed");
					 throw new MailBoxConfigurationServicesException(Messages.SERVICE_INSTANCE_ID_RETRIEVAL_FAILED);
				}
				
			} else {
				LOG.info("Dummy service instance id is set since boolean check 'get-serviceinstanceid-from-manifest' is false");
				serviceInstanceId = MailBoxConstants.DUMMY_SERVICE_INSTANCE_ID;
			}

			//creating a link between mailbox and service instance table
//			createMailboxServiceInstanceIdLink(c, retrievedMailBox);
			ServiceInstanceDAO serviceInstanceDAO = new ServiceInstanceDAOBase();
			ServiceInstance serviceInstance = serviceInstanceDAO.findById(serviceInstanceId);
			//ServiceInstance serviceInstance = serviceInstanceDAO.findById(request.getMailBox().getServiceInstanceId());
			if (serviceInstance == null) {
				serviceInstance = new ServiceInstance();
				serviceInstance.setName(request.getMailBox().getServiceInstanceId());
				serviceInstance.setPguid(MailBoxUtility.getGUID());
				serviceInstanceDAO.persist(serviceInstance);
			}
			
			MailboxServiceInstanceDAO msiDao = new MailboxServiceInstanceDAOBase();
			MailboxServiceInstance mailboxServiceInstance = msiDao.findByGuids(guid, serviceInstance.getPguid());
			if (mailboxServiceInstance == null) {
				
				//Creates relationship mailbox and service instance id
				MailboxServiceInstance msi = new MailboxServiceInstance();
				msi.setPguid(MailBoxUtility.getGUID());
				msi.setServiceInstanceId(serviceInstance);
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
		} catch (IOException e) {
			
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
	 */
	public DeActivateMailBoxResponseDTO deactivateMailBox(String guid) {

		LOG.info("Entering into deactivate mailbox.");
		DeActivateMailBoxResponseDTO serviceResponse = new DeActivateMailBoxResponseDTO();
		try {

			LOG.info("The deactivate request is for {} ", guid);

			MailBoxConfigurationDAO configDao = new MailBoxConfigurationDAOBase();
			MailBox retrievedMailBox = configDao.find(MailBox.class, guid);
			if (retrievedMailBox == null) {
				throw new MailBoxConfigurationServicesException(Messages.MBX_DOES_NOT_EXIST, guid);
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
	 * @throws JAXBException 
	 */
	public SearchMailBoxResponseDTO searchMailBox(SearchMailboxRequestDTO searchMailboxRequestDTO, String mbxName, String profName, String aclManifestJson) throws JAXBException {

		LOG.info("Entering into search mailbox.");

		SearchMailBoxResponseDTO serviceResponse = new SearchMailBoxResponseDTO();

		try {
			
			/*String primarySIId = searchMailboxRequestDTO.getPrimaryServiceInstanceId();
			List<String> secondarySIIds = searchMailboxRequestDTO.getSecondaryServiceInstanceIds();*/
			String primarySIId = null;
			List<String> secondarySIIds = null;
			
			// retrieve service instance id from acl manifest only if the boolean check "get-serviceinstanceid-from-manifest" is true
			// otherwise use the dummy service instance id
			String isReadFromACLManifest = MailBoxUtility.getEnvironmentProperties().getString("get-serviceinstanceid-from-manifest");
			if (isReadFromACLManifest.equals("true")) {
				
				if (aclManifestJson == null) {
					LOG.error("acl manifest is missing in the request header");
					throw new MailBoxConfigurationServicesException(Messages.ACL_MANIFEST_MISSING);
				}
				LOG.info("desearializing the acl manifest DTO from manifest json");
				ACLManifest aclManifest = MailBoxUtility.unmarshalFromJSON(aclManifestJson, ACLManifest.class);
				LOG.info("acl Manifest DTO deserialized successfully");
				LOG.info("Retrieving the service instance id from acl Manifest DTO");
				primarySIId = MailBoxUtility.getPrimaryServiceInstanceIdFromACLManifest(aclManifest);
				secondarySIIds = MailBoxUtility.getSecondaryServiceInstanceIdSFromACLManifest(aclManifest);
			} else {
				LOG.info("Dummy service instance id is set since boolean check 'get-serviceinstanceid-from-manifest' is false");
				primarySIId = MailBoxConstants.DUMMY_SERVICE_INSTANCE_ID;
			}
		
			if (primarySIId == null) {
				 throw new MailBoxConfigurationServicesException(Messages.SERVICE_INSTANCE_ID_RETRIEVAL_FAILED);
			}
			
			//combining the primary and secondary SI ids
			Set<String> combinedServiceInstanceIds = new HashSet<>();
			combinedServiceInstanceIds.add(primarySIId);
			combinedServiceInstanceIds.addAll(secondarySIIds);

			// Getting mailbox
			MailBoxConfigurationDAO configDao = new MailBoxConfigurationDAOBase();

			Set<MailBox> retrievedMailBoxesTobeSent = new HashSet<>();
			//below checking will filter the mailboxes based on primary and secondary service instance ids
			if (!MailBoxUtility.isEmpty(profName)) {
				
				Set<MailBox> retrievedMailBoxes = configDao.find(mbxName, profName);
				
				for(MailBox mb : retrievedMailBoxes) {
					List<MailboxServiceInstance> mbsis = mb.getMailboxServiceInstances();
					for(MailboxServiceInstance mbsi : mbsis) {						
						for(String SIId : combinedServiceInstanceIds) {
							if(mbsi.getServiceInstanceId().getName().equals(SIId)) {
								retrievedMailBoxesTobeSent.add(mb);
							}
						}
					}
				}
			}
			
	        
			// If the profile name is empty it will use findByName
			if (MailBoxUtility.isEmpty(profName) && !MailBoxUtility.isEmpty(mbxName)) {

				Set<MailBox> retrievedMailBoxesUsingName = configDao.findByName(mbxName);
				
				for(MailBox mb : retrievedMailBoxesUsingName) {
					List<MailboxServiceInstance> mbsis = mb.getMailboxServiceInstances();
					for(MailboxServiceInstance mbsi : mbsis) {
						for(String SIId : combinedServiceInstanceIds) {
							if(mbsi.getServiceInstanceId().getName().equals(SIId)) {
								retrievedMailBoxesTobeSent.add(mb);
							}
						}
					}
				}
			}
			
			if (MailBoxUtility.isEmpty(profName) && MailBoxUtility.isEmpty(mbxName)){
				throw new MailBoxConfigurationServicesException(Messages.INVALID_DATA);
			}

			// Constructing the SearchMailBoxDTO from retrieved mailboxes
			List<SearchMailBoxDTO> searchMailBoxDTOList = new ArrayList<SearchMailBoxDTO>();
			SearchMailBoxDTO serachMailBoxDTO = null;
			for (MailBox mbx : retrievedMailBoxesTobeSent) {

				serachMailBoxDTO = new SearchMailBoxDTO();
				serachMailBoxDTO.copyFromEntity(mbx);
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
		} catch (IOException e) {
			
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
			
			String globalTrustStoreId 	   = MailBoxUtility.getEnvironmentProperties().getString("globalTrustStoreId");
			String globalTrustStoreGroupId = MailBoxUtility.getEnvironmentProperties().getString("globalTrustStoreGroupId");
			String gitlabHost = MailBoxUtility.getEnvironmentProperties().getString("com.liaison.gitlab.script.server.host");
			String gitlabPort = MailBoxUtility.getEnvironmentProperties().getString("com.liaison.gitlab.script.server.port");
			String gitlabProjectName = MailBoxUtility.getEnvironmentProperties().getString("com.liaison.gitlab.script.project.name");
			String gitlabBranchName = MailBoxUtility.getEnvironmentProperties().getString("com.liaison.gitlab.script.branch.name");
			String listJobsIntervalInHours = MailBoxUtility.getEnvironmentProperties().getString("listJobsIntervalInHours");
			String fsmEventCheckIntervalInSeconds = MailBoxUtility.getEnvironmentProperties().getString("fsmEventCheckIntervalInSeconds");
			
			dto.setTrustStoreId(globalTrustStoreId);
			dto.setTrustStoreGroupId(globalTrustStoreGroupId);
			dto.setGitlabHost(gitlabHost);
			dto.setGitlabPort(gitlabPort);
			dto.setGitlabBranchName(gitlabBranchName);
			dto.setGitlabProjectName(gitlabProjectName);
			dto.setListJobsIntervalInHours(listJobsIntervalInHours);
			dto.setFsmEventCheckIntervalInSeconds(fsmEventCheckIntervalInSeconds);
			
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
