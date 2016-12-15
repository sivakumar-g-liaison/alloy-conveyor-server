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

import com.liaison.commons.security.pkcs7.SymmetricAlgorithmException;
import com.liaison.mailbox.dtdm.model.Credential;
import com.liaison.mailbox.dtdm.model.Folder;
import com.liaison.mailbox.dtdm.model.Processor;
import com.liaison.mailbox.dtdm.model.ProcessorProperty;
import com.liaison.mailbox.dtdm.model.ScheduleProfileProcessor;
import com.liaison.mailbox.enums.EntityStatus;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.enums.ProcessorType;
import com.liaison.mailbox.enums.Protocol;
import com.liaison.mailbox.service.dto.configuration.request.RemoteProcessorPropertiesDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.util.MailBoxUtil;
import com.liaison.mailbox.service.validation.GenericValidator;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.liaison.mailbox.MailBoxConstants.PROPERTY_PIPELINEID;
import static com.liaison.mailbox.MailBoxConstants.PROPERTY_URL;
import static com.liaison.mailbox.enums.Messages.INVALID_CONNECTION_TIMEOUT;
import static com.liaison.mailbox.enums.Messages.MANDATORY_FIELD_MISSING;
import static com.liaison.mailbox.enums.ProcessorType.DROPBOXPROCESSOR;
import static com.liaison.mailbox.enums.ProcessorType.HTTPASYNCPROCESSOR;
import static com.liaison.mailbox.enums.ProcessorType.HTTPSYNCPROCESSOR;
import static com.liaison.mailbox.enums.ProcessorType.REMOTEDOWNLOADER;
import static com.liaison.mailbox.enums.ProcessorType.REMOTEUPLOADER;
import static com.liaison.mailbox.enums.ProcessorType.SWEEPER;

/**
 * Processor DTO to support migrator service
 * 
 * @author VNagarajan
 *
 */
public class ProcessorLegacyDTO extends ProcessorDTO {

	
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
				processor.setOriginatingDc(MailBoxUtil.DATACENTER_NAME);
				//processor.setProcsrExecutionStatus(ExecutionState.READY.value());
			}

			RemoteProcessorPropertiesDTO propertiesDTO = this.getRemoteProcessorProperties();
			GenericValidator validator = new GenericValidator();
			validator.validate(propertiesDTO);

            ProcessorType processorType = processor.getProcessorType();
            MailBoxUtil.validateURL(processorType, propertiesDTO);
            MailBoxUtil.validateConnectionTimeout(processorType, propertiesDTO.getConnectionTimeout(), validator);
            MailBoxUtil.validatePipelineId(processorType, propertiesDTO);

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
				folder.setOriginatingDc(MailBoxUtil.DATACENTER_NAME);
				folder.setProcessor(processor);
				folders.add(folder);
			}

			if (!folders.isEmpty()) {
				processor.getFolders().addAll(folders);
			}

			// Setting the credentials
			Credential credential = null;
			Set<Credential> credentialList = new HashSet<>();
			List<CredentialDTO> credentials = new ArrayList<CredentialDTO>(this.getCredentials());
			for (CredentialDTO credentialDTO : this.getCredentials()) {
				
				// Validate credentials
				validator.validate(credentialDTO);
				validateCredentials(processor.getProcessorType(), processor.getProcsrProtocol(), credentialDTO, credentials);
				credential = new Credential();
				credentialDTO.copyToEntity(credential);
				credential.setPguid(MailBoxUtil.getGUID());
				credential.setOriginatingDc(MailBoxUtil.DATACENTER_NAME);
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
			Set<String> propertySet = new HashSet<>();
			for (PropertyDTO propertyDTO : this.getDynamicProperties()) {
				
				if(propertySet.contains(propertyDTO.getName())) {
					throw new MailBoxConfigurationServicesException(Messages.PROPERTY_DUPLICATE_ERROR, Response.Status.BAD_REQUEST);
				}
				
				validator.validate(propertyDTO);
				property = new ProcessorProperty();
				propertyDTO.copyToEntity(property, false);
				property.setProcessor(processor);
				propertySet.add(propertyDTO.getName());
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
