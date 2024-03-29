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

import com.liaison.commons.jaxb.JAXBUtility;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.dtdm.model.Credential;
import com.liaison.mailbox.dtdm.model.Folder;
import com.liaison.mailbox.dtdm.model.Processor;
import com.liaison.mailbox.dtdm.model.ProcessorProperty;
import com.liaison.mailbox.dtdm.model.ScheduleProfileProcessor;
import com.liaison.mailbox.enums.EntityStatus;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.enums.ProcessorType;
import com.liaison.mailbox.enums.Protocol;
import com.liaison.mailbox.service.dto.configuration.processor.properties.ProcessorCredentialPropertyDTO;
import com.liaison.mailbox.service.dto.configuration.processor.properties.ProcessorFolderPropertyDTO;
import com.liaison.mailbox.service.dto.configuration.processor.properties.ProcessorPropertyDTO;
import com.liaison.mailbox.service.dto.configuration.processor.properties.ProcessorPropertyUITemplateDTO;
import com.liaison.mailbox.service.dto.configuration.processor.properties.StaticProcessorPropertiesDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.util.MailBoxUtil;
import com.liaison.mailbox.service.util.ProcessorPropertyJsonMapper;
import com.liaison.mailbox.service.validation.DataValidation;
import com.liaison.mailbox.service.validation.GenericValidator;
import com.liaison.mailbox.service.validation.Mandatory;
import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.annotate.JsonIgnore;

import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.liaison.mailbox.service.util.MailBoxUtil.DATACENTER_NAME;

/**
 * Data Transfer Object for processor details.
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
	private Set<String> linkedProfiles;
	private List<ProfileDTO> profiles;
	private boolean createConfiguredLocation;
	private String mailboxName;
	private String mailboxStatus;
	private String modifiedBy;
	private String modifiedDate;
    private String clusterType;
    private String processorDC;


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

	public Set<String> getLinkedProfiles() {
		return linkedProfiles;
	}

	public void setLinkedProfiles(Set<String> linkedProfiles) {
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
	
	public String getMailboxStatus() {
		return mailboxStatus;
	}

	public void setMailboxStatus(String mailboxStatus) {
		this.mailboxStatus = mailboxStatus;
	}

	public String getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public String getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(String modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    @DataValidation(errorMessage = "Processor clusterType set to a value that is not supported.", type = MailBoxConstants.CLUSTER_TYPE)
    public String getClusterType() {
        return clusterType;
    }

    public void setClusterType(String clusterType) {
        this.clusterType = clusterType;
    }

    public void setProcessorDC(String processorDC) {
        this.processorDC = processorDC;
    }

    public String getProcessorDC() {
        return processorDC;
    }

    /**
     * Method is used to copy the values from DTO to Entity. It does not create relationship between
     * MailBoxSchedProfile and Processor. That step will be done in the service.
     *
     * @param processor The processor entity
     * @param isCreate  The boolean value use to differentiate create and revise processor operation.
     * @throws MailBoxConfigurationServicesException
     */
    public void copyToEntity(Processor processor, boolean isCreate, String processDC) throws MailBoxConfigurationServicesException {

        try {

            if (isCreate) {
                processor.setPguid(MailBoxUtil.getGUID());
                processor.setOriginatingDc(DATACENTER_NAME);
                validateAndSetProcessDc(processor, processDC);
            } else {
                if (!MailBoxUtil.isEmpty(processDC)) {
                    processor.setProcessDc(processDC);
                }
            }
            // Set the protocol
            Protocol protocol = Protocol.findByName(this.getProtocol());
            processor.setProcsrProtocol(protocol.getCode());

            ProcessorPropertyUITemplateDTO propertiesDTO = this.getProcessorPropertiesInTemplateJson();

            // separate static and dynamic and folder properties
            List<ProcessorPropertyDTO> dynamicPropertiesDTO = new ArrayList<ProcessorPropertyDTO>();
            List<ProcessorPropertyDTO> procPropertiesFromTemplate = propertiesDTO.getStaticProperties();
            for (ProcessorPropertyDTO property : procPropertiesFromTemplate) {

                if (MailBoxConstants.PROPERTY_LENS_VISIBILITY.equals(property.getName())) {

                    String value = MailBoxConstants.LENS_VISIBLE.equals(property.getValue()) ? Boolean.toString(true) : Boolean.toString(false);
                    property.setValue(value);
                    continue;
                }
                if (ProcessorType.REMOTEUPLOADER.equals(processor.getProcessorType()) &&
                         (Protocol.findByCode(processor.getProcsrProtocol()).equals(Protocol.HTTP) || 
                                Protocol.findByCode(processor.getProcsrProtocol()).equals(Protocol.HTTPS)) &&
                        MailBoxConstants.USE_FILE_SYSTEM.equals(property.getName())) {
                    property.setValue(Boolean.toString(false));
                }
            }
            ProcessorPropertyJsonMapper.separateStaticAndDynamicProperties(procPropertiesFromTemplate, dynamicPropertiesDTO);

            StaticProcessorPropertiesDTO processorPropsDTO = ProcessorPropertyJsonMapper.getProcessorPropInstanceFor(processor.getProcessorType(), Protocol.findByCode(processor.getProcsrProtocol()));

            ProcessorPropertyJsonMapper.transferProps(procPropertiesFromTemplate, processorPropsDTO);
            //validate PipelineId
            MailBoxUtil.validatePipelineId(processor.getProcessorType(), processorPropsDTO);
            GenericValidator validator = new GenericValidator();
            validator.validate(processorPropsDTO);
            if (processorPropsDTO != null) {
                processorPropsDTO.setHandOverExecutionToJavaScript(propertiesDTO.isHandOverExecutionToJavaScript());
            }
            // set static properties into properties json to be stored in DB
            String propertiesJSON = JAXBUtility.marshalToJSON(processorPropsDTO);
            processor.setProcsrProperties(propertiesJSON);
            processor.setProcsrDesc(this.getDescription());
            processor.setProcsrName(this.getName());
            processor.setJavaScriptUri(this.getJavaScriptURI());
            processor.setClusterType(MailBoxUtil.isConveyorType()
                    ? MailBoxConstants.SECURE
                    : (MailBoxUtil.isEmpty(this.getClusterType()) ? MailBoxUtil.CLUSTER_TYPE : this.getClusterType().toUpperCase()));

            // handling of folder properties
            List<ProcessorFolderPropertyDTO> folderProperties = propertiesDTO.getFolderProperties();

            //Construct FOLDER DTO LIST
            List<FolderDTO> folderDTOList = ProcessorPropertyJsonMapper.getFolderProperties(folderProperties);

            // Setting the folders.
            Folder folder = null;
            Set<Folder> folders = new HashSet<>();
            for (FolderDTO folderDTO : folderDTOList) {

                folder = new Folder();
                validator.validate(folderDTO);
                folderDTO.copyToEntity(folder);
                folder.setProcessor(processor);
                folder.setPguid(MailBoxUtil.getGUID());
                folder.setOriginatingDc(MailBoxUtil.DATACENTER_NAME);
                folders.add(folder);
            }

            if (!folders.isEmpty()) {
                processor.getFolders().addAll(folders);
            }

            // handling of credential properties
            List<ProcessorCredentialPropertyDTO> credentialTemplateDTOList = propertiesDTO.getCredentialProperties();

            // construct credentialDTO from credentialPropertyDTO in template json
            List<CredentialDTO> credentialDTOList = ProcessorPropertyJsonMapper.getCredentialProperties(credentialTemplateDTOList);

            // Setting the credentials
            Credential credential = null;
            Set<Credential> credentialList = new HashSet<>();
            for (CredentialDTO credentialDTO : credentialDTOList) {

                validator.validate(credentialDTO);
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
            for (ProcessorPropertyDTO propertyDTO : dynamicPropertiesDTO) {

                if (propertyDTO.getName().equals(MailBoxConstants.ADD_NEW_PROPERTY)) {
                    continue;
                }
                validateDynamicProperty(propertyDTO);
                property = new ProcessorProperty();
                propertyDTO.copyToEntity(property);
                property.setProcessor(processor);
                properties.add(property);
            }
            if (!properties.isEmpty()) {
                processor.getDynamicProperties().addAll(properties);
            }

            // Set the status
            EntityStatus foundStatusType = EntityStatus.findByName(this.getStatus());
            processor.setProcsrStatus(foundStatusType.value());

        } catch (NoSuchFieldException | SecurityException
                | IllegalArgumentException | IllegalAccessException | JAXBException | IOException e) {
            LOGGER.error(e);
            throw new MailBoxConfigurationServicesException("Revise Operation failed:" + e.getMessage(), Response.Status.INTERNAL_SERVER_ERROR);

        }
    }

    /**
     * Copies the values from Entity to DTO.
     *
     * @param processor
     * @throws IOException
     * @throws IllegalAccessException
     * @throws NoSuchFieldException
     */
    public void copyFromEntity(Processor processor, boolean includeUITemplate) throws IllegalAccessException, NoSuchFieldException, IOException {

        this.setGuid(processor.getPguid());
        this.setDescription(processor.getProcsrDesc());
        this.setClusterType(processor.getClusterType());
        this.setProcessorDC(processor.getProcessDc());

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
        this.setMailboxStatus(processor.getMailbox().getMbxStatus());

        this.setModifiedBy(processor.getModifiedBy());
        if (null != processor.getModifiedDate()) {
            this.setModifiedDate(processor.getModifiedDate().toString());
        }

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

    /**
     * Method to validate if name and value are present for dynamic properties
     *
     * @param propertyDTO - propertyDTO which needs to be validated
     */
    private void validateDynamicProperty(ProcessorPropertyDTO propertyDTO) {

        if (propertyDTO.isDynamic() && MailBoxUtil.isEmpty(propertyDTO.getName())) {
            throw new MailBoxConfigurationServicesException(Messages.MANDATORY_FIELD_MISSING, "Property Name", Response.Status.BAD_REQUEST);
        }
        if (propertyDTO.isDynamic() && MailBoxUtil.isEmpty(propertyDTO.getValue())) {
            throw new MailBoxConfigurationServicesException(Messages.MANDATORY_FIELD_MISSING, "Property Value", Response.Status.BAD_REQUEST);
        }
    }

    /**
     * Method is used to check whether the SSH Key pair is available
     *
     * @param credentials
     * @return boolean
     */
    public boolean isSSHKeyPairAvailable(List<CredentialDTO> credentials) {

        for (CredentialDTO credType : credentials) {
            if (credType.getCredentialType().equalsIgnoreCase(MailBoxConstants.SSH_KEYPAIR) && !MailBoxUtil.isEmpty(credType.getIdpURI())) {
                return true;
            }
        }
        return false;
    }

    public void validateAndSetProcessDc(Processor processor, String processDC) {

        if (MailBoxUtil.isEmpty(processDC) && processor.getProcessorType().equals(ProcessorType.REMOTEDOWNLOADER)) {
            processor.setProcessDc(DATACENTER_NAME);
        } else if (!MailBoxUtil.isEmpty(processDC)) {
            processor.setProcessDc(processDC);
        } else {
            processor.setProcessDc(MailBoxConstants.ALL_DATACENTER);
        }
    }

}
