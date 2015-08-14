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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.JsonMappingException;

import com.liaison.commons.security.pkcs7.SymmetricAlgorithmException;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.dtdm.model.Credential;
import com.liaison.mailbox.dtdm.model.Folder;
import com.liaison.mailbox.dtdm.model.Processor;
import com.liaison.mailbox.dtdm.model.ProcessorProperty;
import com.liaison.mailbox.dtdm.model.ScheduleProfileProcessor;
import com.liaison.mailbox.enums.EntityStatus;
import com.liaison.mailbox.enums.Protocol;
import com.liaison.mailbox.service.dto.configuration.processor.properties.ProcessorCredentialPropertyDTO;
import com.liaison.mailbox.service.dto.configuration.processor.properties.ProcessorFolderPropertyDTO;
import com.liaison.mailbox.service.dto.configuration.processor.properties.ProcessorPropertyDTO;
import com.liaison.mailbox.service.dto.configuration.processor.properties.ProcessorPropertyUITemplateDTO;
import com.liaison.mailbox.service.dto.configuration.processor.properties.StaticProcessorPropertiesDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.util.JSONUtil;
import com.liaison.mailbox.service.util.MailBoxUtil;
import com.liaison.mailbox.service.util.ProcessorPropertyJsonMapper;
import com.liaison.mailbox.service.validation.DataValidation;
import com.liaison.mailbox.service.validation.GenericValidator;
import com.liaison.mailbox.service.validation.Mandatory;
import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

/**
 *
 *
 * @author OFS
 */
@ApiModel(value = "processor")
public class ProcessorDTO {
	@JsonIgnore
	private static final Logger LOGGER = LogManager.getLogger(ProcessorDTO.class);

	private String guid;
	private String name;
	@ApiModelProperty( value = "Processor type", required = true)
	private String type;
	private ProcessorPropertyUITemplateDTO processorPropertiesInTemplateJson;
	private String javaScriptURI;
	private String description;
	@ApiModelProperty( value = "Processor status", required = true)
	private String status;
	@ApiModelProperty( value = "Processor protocol", required = true)
	private String protocol;
	@ApiModelProperty( value = "Mailbox id", required = true)
	private String linkedMailboxId;
	private List<String> linkedProfiles;
	private List<ProfileDTO> profiles;
	private boolean createConfiguredLocation;
	private String mailboxName;


	public ProcessorDTO() {
		super();
	}

	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Mandatory(errorMessage = "Processor type is mandatory.")
	@DataValidation(errorMessage = "Processor type set to a value that is not supported.", type = MailBoxConstants.PROCESSOR_TYPE)
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getJavaScriptURI() {
		return javaScriptURI;
	}

	public void setJavaScriptURI(String javaScriptURI) {
		this.javaScriptURI = javaScriptURI;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Mandatory(errorMessage = "Processor status is mandatory.")
	@DataValidation(errorMessage = "Processor status set to a value that is not supported.", type = MailBoxConstants.MBX_STATUS)
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@Mandatory(errorMessage = "Processor protocol is mandatory.")
	@DataValidation(errorMessage = "Processor protocol set to a value that is not supported.", type = MailBoxConstants.PROCESSOR_PROTOCOL)
	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	@Mandatory(errorMessage = "Mailbox Id is mandatory.")
	public String getLinkedMailboxId() {
		return linkedMailboxId;
	}

	public void setLinkedMailboxId(String linkedMailboxId) {
		this.linkedMailboxId = linkedMailboxId;
	}

	public List<String> getLinkedProfiles() {
		return linkedProfiles;
	}

	public void setLinkedProfiles(List<String> linkedProfiles) {
		this.linkedProfiles = linkedProfiles;
	}

	public List<ProfileDTO> getProfiles() {

		if (null == profiles) {
			profiles = new ArrayList<>();
		}
		return profiles;
	}

	public void setProfiles(List<ProfileDTO> profiles) {
		this.profiles = profiles;
	}

	public ProcessorPropertyUITemplateDTO getProcessorPropertiesInTemplateJson() {
		return processorPropertiesInTemplateJson;
	}

	public void setProcessorPropertiesInTemplateJson(
			ProcessorPropertyUITemplateDTO processorPropertiesInTemplateJson) {
		this.processorPropertiesInTemplateJson = processorPropertiesInTemplateJson;
	}

	public boolean isCreateConfiguredLocation() {
		return createConfiguredLocation;
	}

	public void setCreateConfiguredLocation(boolean createConfiguredLocation) {
		this.createConfiguredLocation = createConfiguredLocation;
	}

	public String getMailboxName() {
		return mailboxName;
	}

	public void setMailboxName(String mailboxName) {
		this.mailboxName = mailboxName;
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
	public void copyToEntity(Processor processor, boolean isCreate) throws MailBoxConfigurationServicesException{

		try {

		if (isCreate) {
			processor.setPguid(MailBoxUtil.getGUID());
		}
		// Set the protocol
		Protocol protocol = Protocol.findByName(this.getProtocol());
		processor.setProcsrProtocol(protocol.getCode());

		ProcessorPropertyUITemplateDTO propertiesDTO = this.getProcessorPropertiesInTemplateJson();

		// separate static and dynamic and folder properties
		List <ProcessorPropertyDTO> dynamicPropertiesDTO = new ArrayList<ProcessorPropertyDTO>();
		//TODO rename the staticProperties to just properties in JSON - GMB-538
		List <ProcessorPropertyDTO> procPropertiesFromTemplate = propertiesDTO.getStaticProperties();
		ProcessorPropertyJsonMapper.separateStaticAndDynamicProperties(procPropertiesFromTemplate, dynamicPropertiesDTO);

		StaticProcessorPropertiesDTO processorPropsDTO = ProcessorPropertyJsonMapper.getProcessorPropInstanceFor(processor.getProcessorType(),
																												 Protocol.findByCode(processor.getProcsrProtocol()));
		ProcessorPropertyJsonMapper.transferProps(procPropertiesFromTemplate, processorPropsDTO);
		processorPropsDTO.setHandOverExecutionToJavaScript(propertiesDTO.isHandOverExecutionToJavaScript());
		// set static properties into properties json to be stored in DB
		String propertiesJSON = JSONUtil.marshalToJSON(processorPropsDTO);
		processor.setProcsrProperties(propertiesJSON);
	    processor.setProcsrDesc(this.getDescription());
		processor.setProcsrName(this.getName());
		processor.setJavaScriptUri(this.getJavaScriptURI());

		GenericValidator validator = new GenericValidator();

		// handling of folder properties
		List <ProcessorFolderPropertyDTO> folderProperties = propertiesDTO.getFolderProperties();

        //Construct FOLDER DTO LIST
        List <FolderDTO> folderDTOList = ProcessorPropertyJsonMapper.getFolderProperties (folderProperties);
        
        Set<Folder> existingProperties = processor.getFolders();
		if (null != existingProperties && !existingProperties.isEmpty()) {
			Set<Folder> base = new HashSet<>();
			List<FolderDTO> existingPropertiesInDTO = new ArrayList<FolderDTO>();
			for (Folder exist : existingProperties) {
				for (FolderDTO newProp : folderDTOList) {
					
					if (exist.getFldrType().equals(newProp.getFolderType())) {
						exist.setFldrUri(newProp.getFolderURI());
						exist.setFldrDesc(newProp.getFolderDesc());
						existingPropertiesInDTO.add(newProp);
						break;
					} 
				}
				base.add(exist);		
				}
			if (!existingPropertiesInDTO.isEmpty()) {
				
				folderDTOList.removeAll(existingPropertiesInDTO);
				Folder property = null;
				for (FolderDTO folderDTO : folderDTOList) {
					
					property = new Folder();
					property.setProcessor(processor);
					validator.validate(folderDTO);
					folderDTO.copyToEntity(property);
					existingProperties.add(property);
				}
				//retrievedMailBox.getMailboxProperties().removeAll(base);
				processor.setFolders(existingProperties);
			}
		} else {

			Folder property = null;
			Set<Folder> properties = new HashSet<>();
			for (FolderDTO folderDTO : folderDTOList) {
				property = new Folder();
				property.setProcessor(processor);
				validator.validate(folderDTO);
				folderDTO.copyToEntity(property);
				properties.add(property);
			}
			processor.setFolders(properties);
		}

		// handling of credential properties
		List<ProcessorCredentialPropertyDTO> credentialTemplateDTOList = propertiesDTO.getCredentialProperties();

		// construct credentialDTO from credentialPropertyDTO in template json
		List <CredentialDTO> credentialDTOList = ProcessorPropertyJsonMapper.getCredentialProperties(credentialTemplateDTOList);

    	Set<Credential> existingCredProperties = processor.getCredentials();
		if (null != existingCredProperties && !existingCredProperties.isEmpty()) {
			Set<Credential> base = new HashSet<>();
			List<CredentialDTO> existingPropertiesInDTO = new ArrayList<CredentialDTO>();
			for (Credential exist : existingCredProperties) {
				for (CredentialDTO newProp : credentialDTOList) {
					if (exist.getCredsIdpType().equals(newProp.getIdpType())) {
						exist.setCredsIdpType(newProp.getIdpType());
						exist.setCredsIdpUri(newProp.getIdpURI());
						exist.setCredsPassword(newProp.getPassword());
						exist.setCredsType(newProp.getCredentialType());
						exist.setCredsUri(newProp.getCredentialURI());
						exist.setCredsUsername(newProp.getUserId());
						existingPropertiesInDTO.add(newProp);
						break;
					} 
				}
				base.add(exist);		
				}
			if (!existingPropertiesInDTO.isEmpty()) {
				credentialDTOList.removeAll(existingPropertiesInDTO);
				Credential credential = null;
				for (CredentialDTO folderDTO : credentialDTOList) {
					credential = new Credential();
					credential.setProcessor(processor);
					folderDTO.copyToEntity(credential);
					credential.setPguid(MailBoxUtil.getGUID());
					existingCredProperties.add(credential);
				}
				processor.setCredentials(existingCredProperties);
			}
		} else {

			Credential credential = null;
			Set<Credential> properties = new HashSet<>();
			for (CredentialDTO credentialDTO : credentialDTOList) {
				credential = new Credential();
				credential.setProcessor(processor);
				credentialDTO.copyToEntity(credential);
				credential.setPguid(MailBoxUtil.getGUID());
				properties.add(credential);
			}
			processor.setCredentials(properties);
		}
		
		Set<ProcessorProperty> existingProps = processor.getDynamicProperties();
		if (null != existingProperties && !existingProperties.isEmpty()) {
			Set<ProcessorProperty> base = new HashSet<>();
			List<ProcessorPropertyDTO> existingPropertiesInDTO = new ArrayList<ProcessorPropertyDTO>();
			for (ProcessorProperty exist : existingProps) {
				for (ProcessorPropertyDTO newProp : dynamicPropertiesDTO) {
					if (newProp.getName().equals(MailBoxConstants.ADD_NEW_PROPERTY)) {
						continue;
					}
					if (exist.getProcsrPropName().equals(newProp.getName())) {
						exist.setProcsrPropValue(newProp.getValue());
						existingPropertiesInDTO.add(newProp);
						break;
					} 
				}
				base.add(exist);		
				}
			if (!existingPropertiesInDTO.isEmpty()) {
				dynamicPropertiesDTO.removeAll(existingPropertiesInDTO);
				ProcessorProperty property = null;
				for (ProcessorPropertyDTO propertyDTO : dynamicPropertiesDTO) {
					if (propertyDTO.getName().equals(MailBoxConstants.ADD_NEW_PROPERTY)) {
						continue;
					}
					property = new ProcessorProperty();
					property.setProcessor(processor);
					propertyDTO.copyToEntity(property);
					existingProps.add(property);
				}
				processor.setDynamicProperties(existingProps);
			}
		} else {

			ProcessorProperty property = null;
			Set<ProcessorProperty> properties = new HashSet<>();
			for (ProcessorPropertyDTO propertyDTO : dynamicPropertiesDTO) {
				if (propertyDTO.getName().equals(MailBoxConstants.ADD_NEW_PROPERTY)) {
					continue;
				}
				property = new ProcessorProperty();
				property.setProcessor(processor);
				propertyDTO.copyToEntity(property);
				properties.add(property);
			}
			processor.setDynamicProperties(properties);
		}
	
		// Set the status
		EntityStatus foundStatusType = EntityStatus.findByName(this.getStatus());
		processor.setProcsrStatus(foundStatusType.value());

		} catch (NoSuchFieldException | SecurityException
				| IllegalArgumentException | IllegalAccessException | JAXBException | IOException | SymmetricAlgorithmException e) {
			LOGGER.error(e);
			throw new MailBoxConfigurationServicesException("Revise Operation failed:"+e.getMessage(),Response.Status.INTERNAL_SERVER_ERROR);

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
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 */
    public void copyFromEntity(Processor processor, boolean includeUITemplate) throws NoSuchFieldException,
            SecurityException, IllegalArgumentException, IllegalAccessException, IOException, JAXBException {

		this.setGuid(processor.getPguid());
		this.setDescription(processor.getProcsrDesc());

		String status = processor.getProcsrStatus();
		if (!MailBoxUtil.isEmpty(status)) {
			EntityStatus foundStatus = EntityStatus.findByCode(status);
			this.setStatus(foundStatus.name());
		}

		this.setType(processor.getProcessorType().name());
		this.setJavaScriptURI(processor.getJavaScriptUri());
		this.setName(processor.getProcsrName());
		this.setLinkedMailboxId(processor.getMailbox().getPguid());
		this.setMailboxName(processor.getMailbox().getMbxName());

		Protocol protocol = Protocol.findByCode(processor.getProcsrProtocol());
		// Set protocol
		this.setProtocol(protocol.name());

		if (null != processor.getScheduleProfileProcessors()) {

			ProfileDTO profile = null;
			for (ScheduleProfileProcessor scheduleProfileProcessor : processor.getScheduleProfileProcessors()) {

				profile = new ProfileDTO();
				profile.copyFromEntity(scheduleProfileProcessor.getScheduleProfilesRef());
				this.getProfiles().add(profile);
			}

		}

		if (includeUITemplate) {
			this.setProcessorPropertiesInTemplateJson(ProcessorPropertyJsonMapper.getHydratedUIPropertyTemplate(processor.getProcsrProperties(), processor));
 		}
	}

}
