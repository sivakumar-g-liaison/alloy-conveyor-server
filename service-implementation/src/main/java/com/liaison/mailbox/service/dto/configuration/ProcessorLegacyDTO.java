/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.mailbox.service.dto.configuration;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

import com.liaison.commons.security.pkcs7.SymmetricAlgorithmException;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.dtdm.model.Credential;
import com.liaison.mailbox.dtdm.model.Folder;
import com.liaison.mailbox.dtdm.model.Processor;
import com.liaison.mailbox.dtdm.model.ProcessorProperty;
import com.liaison.mailbox.dtdm.model.ScheduleProfileProcessor;
import com.liaison.mailbox.enums.CredentialType;
import com.liaison.mailbox.enums.EntityStatus;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.enums.ProcessorType;
import com.liaison.mailbox.enums.Protocol;
import com.liaison.mailbox.service.dto.configuration.request.RemoteProcessorPropertiesDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.util.MailBoxUtil;
import com.liaison.mailbox.service.validation.GenericValidator;

/**
 * Processor DTO to support migrator service
 * 
 * @author VNagarajan
 *
 */
public class ProcessorLegacyDTO extends ProcessorDTO {

	
	private static final String TRUSTSTORE_CERT_NOT_PROVIDED = "Trust store Certificate cannot be Empty.";
	private static final String SSH_KEYPAIR_NOT_PROVIDED= "SSH Key Pair cannot be Empty.";
	private RemoteProcessorPropertiesDTO remoteProcessorProperties;
	private Set<FolderDTO> folders;
	private Set<CredentialDTO> credentials;
	private Set<PropertyDTO> dynamicProperties;

	private boolean createConfiguredLocation = false;
	public ProcessorLegacyDTO() {
		super();
	}

	public Set<FolderDTO> getFolders() {
		if (folders == null) {
			folders = new HashSet<FolderDTO>();
		}
		return folders;
	}

	public void setFolders(Set<FolderDTO> folders) {
		this.folders = folders;
	}

	public Set<CredentialDTO> getCredentials() {
		if (credentials == null) {
			credentials = new HashSet<CredentialDTO>();
		}
		return credentials;
	}

	public void setCredentials(Set<CredentialDTO> credentials) {
		this.credentials = credentials;
	}

	public Set<PropertyDTO> getDynamicProperties() {

		if (dynamicProperties == null) {
			dynamicProperties = new HashSet<PropertyDTO>();
		}
		return dynamicProperties;
	}

	public void setDynamicProperties(Set<PropertyDTO> dynamicProperties) {
		this.dynamicProperties = dynamicProperties;
	}

	public RemoteProcessorPropertiesDTO getRemoteProcessorProperties() {
		return remoteProcessorProperties;
	}

	public void setRemoteProcessorProperties(RemoteProcessorPropertiesDTO remoteProcessorProperties) {
		this.remoteProcessorProperties = remoteProcessorProperties;
	}
	
	public boolean isCreateConfiguredLocation() {
		return createConfiguredLocation;
	}

	public void setCreateConfiguredLocation(boolean createConfiguredLocation) {
		this.createConfiguredLocation = createConfiguredLocation;
	}

	/**
	 * Method is used to copy the values from DTO to Entity. It does not create relationship between
	 * MailBoxSchedProfile and Processor. That step will be done in the service.
	 * 
	 * @param processor
	 *            The processor entity
	 * @param isCreate
	 *            The boolean value use to differentiate create and revise processor operation.
	 * @throws MailBoxConfigurationServicesException
	 * @throws JAXBException
	 * @throws JsonMappingException
	 * @throws JsonGenerationException
	 */
	public void copyToEntity(Processor processor, boolean isCreate) throws MailBoxConfigurationServicesException {

		try {
			if (isCreate) {
				processor.setPguid(MailBoxUtil.getGUID());
				//processor.setProcsrExecutionStatus(ExecutionState.READY.value());
			}

			RemoteProcessorPropertiesDTO propertiesDTO = this.getRemoteProcessorProperties();
			GenericValidator validator = new GenericValidator();
            validator.validate(propertiesDTO);
            
            if (ProcessorType.REMOTEUPLOADER.equals(processor.getProcessorType()) ||
                    ProcessorType.REMOTEDOWNLOADER.equals(processor.getProcessorType())) {
                if (!MailBoxUtil.isEmpty(propertiesDTO.getUrl())) {
                    MailBoxUtil.constructURLAndPort(propertiesDTO);
                } else {
                    throw new MailBoxConfigurationServicesException(Messages.MANDATORY_FIELD_MISSING, MailBoxConstants.PROPERTY_URL.toUpperCase(), Response.Status.BAD_REQUEST);
                }
            }
            
			if (null != propertiesDTO) {
				String propertiesJSON = MailBoxUtil.marshalToJSON(this.getRemoteProcessorProperties());
				processor.setProcsrProperties(propertiesJSON);
			}

			processor.setProcsrDesc(this.getDescription());
			processor.setProcsrName(this.getName());
			processor.setJavaScriptUri(this.getJavaScriptURI());
			processor.setProcsrProtocol(this.getProtocol());

			// Setting the folders.
			Folder folder = null;
			Set<Folder> folders = new HashSet<>();
			for (FolderDTO folderDTO : this.getFolders()) {
				
				validator.validate(folderDTO);
				folder = new Folder();
				folderDTO.copyToEntity(folder);

				folder.setPguid(MailBoxUtil.getGUID());
				folder.setProcessor(processor);
				folders.add(folder);
			}

			if (!folders.isEmpty()) {
				processor.getFolders().addAll(folders);
			}

			// Setting the credentials
			Credential credential = null;
			Set<Credential> credentialList = new HashSet<>();
			for (CredentialDTO credentialDTO : this.getCredentials()) {
				
				// Validate credentials
				validateCredentials(processor, credentialDTO);
				
				validator.validate(credentialDTO);
				credential = new Credential();
				credentialDTO.copyToEntity(credential);
				credential.setPguid(MailBoxUtil.getGUID());
				credential.setProcessor(processor);
				credentialList.add(credential);
			}

			if (!credentialList.isEmpty()) {
				processor.getCredentials().addAll(credentialList);
			}

			// Setting the property
			if (null != processor.getDynamicProperties()) {
				processor.getDynamicProperties().clear();
			}
			ProcessorProperty property = null;
			Set<ProcessorProperty> properties = new HashSet<>();
			for (PropertyDTO propertyDTO : this.getDynamicProperties()) {
				
				validator.validate(propertyDTO);
				property = new ProcessorProperty();
				propertyDTO.copyToEntity(property, false);
				property.setProcessor(processor);
				properties.add(property);
			}
			if (!properties.isEmpty()) {
				processor.getDynamicProperties().addAll(properties);
			}

			// Set the protocol
			Protocol protocol = Protocol.findByName(this.getProtocol());
			processor.setProcsrProtocol(protocol.getCode());
			
				// Set the status
			EntityStatus foundStatusType = EntityStatus.findByName(this.getStatus());
			processor.setProcsrStatus(foundStatusType.value());
		} catch(Exception e) {
			throw new RuntimeException(e);
		}

	}
	
	/**
	 * Method is used to Validate the credentials given
	 * 
	 * @param processor
	 *            The processor entity
	 * @param credentialDTO 
	 * 			  Login Credential Details
	 */
	public void validateLoginCredentials (Processor processor, CredentialDTO credentialDTO) {
		
		if (ProcessorType.REMOTEUPLOADER.equals(processor.getProcessorType()) ||
                ProcessorType.REMOTEDOWNLOADER.equals(processor.getProcessorType())) {
			
			Protocol protocol = Protocol.findByName(processor.getProcsrProtocol());
			
				switch (protocol) {
	
					case FTP:
					case FTPS:
					case HTTPS:
						if (MailBoxUtil.isEmpty(credentialDTO.getUserId())) {
							throw new MailBoxConfigurationServicesException(Messages.USERNAME_EMPTY, Response.Status.BAD_REQUEST);
						} else if (MailBoxUtil.isEmpty(credentialDTO.getPassword())) {
							throw new MailBoxConfigurationServicesException(Messages.PWD_EMPTY, Response.Status.BAD_REQUEST);
						}
						break;
					case SFTP:
						if (!MailBoxUtil.isEmpty(credentialDTO.getUserId()) && MailBoxUtil.isEmpty(credentialDTO.getPassword())) {
							if(!isSSHKeyPairAvailable()) {
								throw new MailBoxConfigurationServicesException(Messages.PASSWORD_OR_SSH_KEYPAIR_EMPTY, Response.Status.BAD_REQUEST);
							}
						} else if (MailBoxUtil.isEmpty(credentialDTO.getUserId())) {
							throw new MailBoxConfigurationServicesException(Messages.USERNAME_EMPTY, Response.Status.BAD_REQUEST);
						} else if (MailBoxUtil.isEmpty(credentialDTO.getPassword())) {
							throw new MailBoxConfigurationServicesException(Messages.PWD_EMPTY, Response.Status.BAD_REQUEST);
						}
						break;
					default:
						break;
				}
		}
	}
	
	/**
	 * Method to validate credentials 
	 * 
	 * @param processor
	 * 			processor entity
	 * @param credentialDTO
	 * 			credential Details
	 */
	public void validateCredentials (Processor processor, CredentialDTO credentialDTO) {
		
		CredentialType credentialType = CredentialType.findByName(credentialDTO.getCredentialType());
		switch(credentialType) {
		
		case LOGIN_CREDENTIAL:
			validateLoginCredentials(processor, credentialDTO);
			break;
		case SSH_KEYPAIR:
			validateSSHKeypair(credentialDTO.getIdpURI());
			break;
		case TRUSTSTORE_CERT:
			validateTruststoreCertificate(credentialDTO.getIdpURI());
			break;
		}
	}
	
	/**
	 * Method to validate ssh keypair 
	 * 
	 * @param sshKeypairGroupId
	 */
	public void validateSSHKeypair(String sshKeypairGroupId) {
		
		if (MailBoxUtil.isEmpty(sshKeypairGroupId)) {
			throw new MailBoxConfigurationServicesException(SSH_KEYPAIR_NOT_PROVIDED, Response.Status.BAD_REQUEST);
		}
	}
	
	/**
	 * Method to validate trust store
	 * 
	 * @param trustStoreGroupId
	 */
	public void validateTruststoreCertificate(String trustStoreGroupId) {
		if (MailBoxUtil.isEmpty(trustStoreGroupId)) {
			throw new MailBoxConfigurationServicesException(TRUSTSTORE_CERT_NOT_PROVIDED, Response.Status.BAD_REQUEST);
		}	
	}
	/**
	 * Method is used to check whether the SSH Key pair is available
	 * 
	 * @return boolean
	 */
	public boolean isSSHKeyPairAvailable () {
		
		Set<CredentialDTO> processorCredential = this.getCredentials();
		for (CredentialDTO credType : processorCredential) {
			if (credType.getCredentialType().toString().equalsIgnoreCase(MailBoxConstants.SSH_KEYPAIR) && !MailBoxUtil.isEmpty(credType.getIdpURI())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Copies the values from Entity to DTO.
	 * 
	 * @param processor
	 * @throws IOException
	 * @throws JAXBException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 * @throws MailBoxConfigurationServicesException
	 * @throws SymmetricAlgorithmException
	 */
	public void copyFromEntity(Processor processor) throws IOException, MailBoxConfigurationServicesException {

		this.setGuid(processor.getPguid());
		this.setDescription(processor.getProcsrDesc());

		String propertyJSON = processor.getProcsrProperties();
		if (!MailBoxUtil.isEmpty(propertyJSON)) {

			RemoteProcessorPropertiesDTO propertiesDTO = MailBoxUtil.unmarshalFromJSON(propertyJSON, RemoteProcessorPropertiesDTO.class);
			this.setRemoteProcessorProperties(propertiesDTO);
		}

		String status = processor.getProcsrStatus();
		if (!MailBoxUtil.isEmpty(status)) {
			EntityStatus foundStatus = EntityStatus.findByCode(status);
			this.setStatus(foundStatus.name());
		}

		this.setType(processor.getProcessorType().name());
		this.setJavaScriptURI(processor.getJavaScriptUri());
		this.setName(processor.getProcsrName());

		// Set folders
		if (null != processor.getFolders()) {

			FolderDTO folderDTO = null;
			for (Folder folder : processor.getFolders()) {
				folderDTO = new FolderDTO();
				folderDTO.copyFromEntity(folder);
				this.getFolders().add(folderDTO);
			}
		}

		// Set credentials
		if (null != processor.getCredentials()) {

			CredentialDTO credentialDTO = null;
			for (Credential credential : processor.getCredentials()) {
				credentialDTO = new CredentialDTO();
				credentialDTO.copyFromEntity(credential);
				this.getCredentials().add(credentialDTO);
			}
		}

		// Set properties
		if (null != processor.getDynamicProperties()) {

			PropertyDTO propertyDTO = null;
			for (ProcessorProperty property : processor.getDynamicProperties()) {
				propertyDTO = new PropertyDTO();
				propertyDTO.copyFromEntity(property, false);
				this.getDynamicProperties().add(propertyDTO);
			}
		}

		// Set protocol
		this.setProtocol(Protocol.findByCode(processor.getProcsrProtocol()).name());

		if (null != processor.getScheduleProfileProcessors()) {

			ProfileDTO profile = null;
			for (ScheduleProfileProcessor scheduleProfileProcessor : processor.getScheduleProfileProcessors()) {

				profile = new ProfileDTO();
				profile.copyFromEntity(scheduleProfileProcessor.getScheduleProfilesRef());
				this.getProfiles().add(profile);
			}

		}
	}

}
