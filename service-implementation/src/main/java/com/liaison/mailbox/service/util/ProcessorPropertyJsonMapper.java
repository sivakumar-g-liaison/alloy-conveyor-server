/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.util;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

import com.liaison.framework.util.ServiceUtils;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.dtdm.model.Credential;
import com.liaison.mailbox.dtdm.model.Folder;
import com.liaison.mailbox.dtdm.model.Processor;
import com.liaison.mailbox.dtdm.model.ProcessorProperty;
import com.liaison.mailbox.enums.FolderType;
import com.liaison.mailbox.enums.ProcessorType;
import com.liaison.mailbox.enums.Protocol;
import com.liaison.mailbox.service.dto.configuration.CredentialDTO;
import com.liaison.mailbox.service.dto.configuration.FolderDTO;
import com.liaison.mailbox.service.dto.configuration.processor.properties.DropboxProcessorPropertiesDTO;
import com.liaison.mailbox.service.dto.configuration.processor.properties.FTPDownloaderPropertiesDTO;
import com.liaison.mailbox.service.dto.configuration.processor.properties.FTPUploaderPropertiesDTO;
import com.liaison.mailbox.service.dto.configuration.processor.properties.FileWriterPropertiesDTO;
import com.liaison.mailbox.service.dto.configuration.processor.properties.HTTPDownloaderPropertiesDTO;
import com.liaison.mailbox.service.dto.configuration.processor.properties.HTTPListenerPropertiesDTO;
import com.liaison.mailbox.service.dto.configuration.processor.properties.HTTPUploaderPropertiesDTO;
import com.liaison.mailbox.service.dto.configuration.processor.properties.ProcessorCredentialPropertyDTO;
import com.liaison.mailbox.service.dto.configuration.processor.properties.ProcessorFolderPropertyDTO;
import com.liaison.mailbox.service.dto.configuration.processor.properties.ProcessorPropertyDTO;
import com.liaison.mailbox.service.dto.configuration.processor.properties.ProcessorPropertyUITemplateDTO;
import com.liaison.mailbox.service.dto.configuration.processor.properties.SFTPDownloaderPropertiesDTO;
import com.liaison.mailbox.service.dto.configuration.processor.properties.SFTPUploaderPropertiesDTO;
import com.liaison.mailbox.service.dto.configuration.processor.properties.StaticProcessorPropertiesDTO;
import com.liaison.mailbox.service.dto.configuration.processor.properties.SweeperPropertiesDTO;
import com.liaison.mailbox.service.dto.configuration.request.HttpOtherRequestHeaderDTO;
import com.liaison.mailbox.service.dto.configuration.request.RemoteProcessorPropertiesDTO;

/**
 * Utilities for ProcessorProperties.
 *
 * @author OFS
 */
public class ProcessorPropertyJsonMapper {

	private static final Logger LOGGER = LogManager.getLogger(ProcessorPropertyJsonMapper.class);

	public static final String PROP_HANDOVER_EXECUTION_TO_JS = "handOverExecutionToJavaScript";
	public static final String JSON_ROOT_PATH = "processor/properties/";

	private static Map <String, String> propertyMapper = null;

    static {

        propertyMapper = new HashMap<String, String>();
        propertyMapper.put(MailBoxConstants.HTTPLISTENER_AUTH_CHECK, MailBoxConstants.PROPERTY_HTTPLISTENER_AUTH_CHECK);
        propertyMapper.put(MailBoxConstants.SWEEPED_FILE_LOCATION, MailBoxConstants.PROPERTY_SWEEPED_FILE_LOCATION);
        propertyMapper.put(MailBoxConstants.ERROR_FILE_LOCATION, MailBoxConstants.PROPERTY_ERROR_FILE_LOCATION);
        propertyMapper.put(MailBoxConstants.PROCESSED_FILE_LOCATION, MailBoxConstants.PROPERTY_PROCESSED_FILE_LOCATION);
        propertyMapper.put(MailBoxConstants.NUMBER_OF_FILES_THRESHOLD, MailBoxConstants.PROPERTY_NO_OF_FILES_THRESHOLD);
        propertyMapper.put(MailBoxConstants.PAYLOAD_SIZE_THRESHOLD, MailBoxConstants.PROPERTY_PAYLOAD_SIZE_THRESHOLD);
        propertyMapper.put(MailBoxConstants.FILE_RENAME_FORMAT_PROP_NAME, MailBoxConstants.PROPERTY_FILE_RENAME_FORMAT);
    }

	/**
	 * Method to retrieve specific processor property given name
	 *
	 * @param processorDTO
	 * @param propertyName
	 * @return
	 */
    public static Map<String, String> getProcessorPropertiesAsMap(ProcessorPropertyUITemplateDTO uiPropertyTemplate,
            List<String> propertyNames) {

        String propertyValue = null;
        HashMap<String, String> retrievedProperties = new HashMap<String, String>();
        for (ProcessorPropertyDTO property : uiPropertyTemplate.getStaticProperties()) {
            if (propertyNames.contains(property.getName())) {
                retrievedProperties.put(property.getName(), property.getValue());
                LOGGER.debug("The property value is", propertyValue);
            }
        }
        return retrievedProperties;
    }

	/**
	 * Method to retrieve specific processor property given name
	 * 
	 * @param processorDTO
	 * @param propertyName
	 * @return String it should return property Value by given property Name
	 */
    public static String getProcessorPropertyByName(ProcessorPropertyUITemplateDTO processorProperties,
            String propertyName) {

        String propertyValue = null;
        for (ProcessorPropertyDTO property : processorProperties.getStaticProperties()) {
            if (property.getName().equals(propertyName)) {
                propertyValue = property.getValue();
                LOGGER.debug("The property value is", propertyValue);
                break;
            }
        }
        return propertyValue;
    }

	/**
	 * Method to retrieve ProcessorPropertyUITemplateDTO in format stored in Template JSON from the properties JSON stored in DB
	 * This method will convert the json to ProcessorPropertyUITemplateDTO format even if it is in older format (RemoteProcessorPropertiesDTO)
	 *
	 * @param propertyJson
	 * @param processor
	 * @return ProcessorPropertyUITemplateDTO
	 * @throws IOException
	 * @throws NoSuchFieldException
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws JAXBException
	 */
    public static ProcessorPropertyUITemplateDTO getHydratedUIPropertyTemplate(String propertyJson, Processor processor)
            throws IOException, NoSuchFieldException, SecurityException, IllegalArgumentException,
            IllegalAccessException, JAXBException {

        Protocol protocol = Protocol.findByCode(processor.getProcsrProtocol());
        ProcessorPropertyUITemplateDTO uiPropTemplate = getTemplate(processor.getProcessorType(), protocol);
        if (!MailBoxUtil.isEmpty(propertyJson)) {

            // In order to provide backward compatibility for older processor
            // entities
            // try to unmarshal the properties json with new class
            // "ProcessorPropertyUITemplateDTO"
            // if the unmarshalling fails then try to unmarshal it with old
            // class "RemoteProcessorPropertiesDTO"
            try {
                StaticProcessorPropertiesDTO staticProperties = getProcessorBasedStaticProps(propertyJson);
                uiPropTemplate.setHandOverExecutionToJavaScript(staticProperties.isHandOverExecutionToJavaScript());
                hydrateTemplate(staticProperties, uiPropTemplate);

            } catch (JAXBException | JsonMappingException | JsonParseException e) {

                RemoteProcessorPropertiesDTO legacyProps = MailBoxUtil.unmarshalFromJSON(propertyJson,
                        RemoteProcessorPropertiesDTO.class);
                mapLegacyPropsToTemplate(uiPropTemplate, legacyProps);

            }
        }
        // handle dynamic properties also
        setDynamicPropsinTemplate(processor, uiPropTemplate);
        // handle folder properties also
        constructFolderTemplate(processor, uiPropTemplate);
        // handle the credential Properties
        constructCredentialPropertiesTemplate(processor, uiPropTemplate);
        return uiPropTemplate;
    }

	/**
	 * Method to retrieve staticproperties in format stored in DB (ie StaticProcessorPropertiesDTO) from the properties JSON stored in DB
	 * This method will convert the json to StaticProcessorPropertiesDTO format even if it is in older format (RemoteProcessorPropertiesDTO)
	 *
	 * @param propertyJson
	 * @param processor
	 * @return
	 * @throws IOException
	 * @throws JAXBException
	 * @throws NoSuchFieldException
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
    public static StaticProcessorPropertiesDTO getProcessorBasedStaticPropsFromJson(String propertyJson,
            Processor processor) throws IOException, JAXBException, NoSuchFieldException, SecurityException,
            IllegalArgumentException, IllegalAccessException {

        StaticProcessorPropertiesDTO staticProcessorProperties = null;
        Protocol protocol = Protocol.findByCode(processor.getProcsrProtocol());
        try {
            staticProcessorProperties = getProcessorBasedStaticProps(propertyJson);

        } catch (JAXBException | JsonMappingException | JsonParseException e) {

            RemoteProcessorPropertiesDTO leagcyProps = MailBoxUtil.unmarshalFromJSON(propertyJson,
                    RemoteProcessorPropertiesDTO.class);
            staticProcessorProperties = getProcessorBasedStaticPropsFrmLegacyProps(leagcyProps,
                    processor.getProcessorType(), protocol);
            handleDynamicProperties(staticProcessorProperties, processor);
        }
        return staticProcessorProperties;

    }
    
	/**
	 * Method to retrieve StaticProcessorPropertiesDTO by given procsrProperties retrieved from db.
	 * 
	 * @param propertyJson
	 * @return    The propertyJson is string of procsrProperties retrieved from db.
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws JAXBException
	 * @throws IOException
	 */

	private static StaticProcessorPropertiesDTO getProcessorBasedStaticProps(String propertyJson)
			throws JsonParseException, JsonMappingException, JAXBException, IOException {
		return JSONUtil.unmarshalFromJSON(propertyJson, StaticProcessorPropertiesDTO.class);
	}

    
	/**
	 * Method to retrieve ProcessorPropertyUITemplateDTO from JsonTemplate by
	 * given processorType and protocol
	 * 
	 * @param processorType
	 *            The processorType of the Processor
	 * @param protocol
	 *            The protocol of the Processor
	 * @return ProcessorPropertyUITemplateDTO
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws JAXBException
	 * @throws IOException
	 * @throws NoSuchFieldException
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
    public static ProcessorPropertyUITemplateDTO getTemplate(ProcessorType processorType, Protocol protocol)
            throws JsonParseException, JsonMappingException, JAXBException, IOException, NoSuchFieldException,
            SecurityException, IllegalArgumentException, IllegalAccessException {

        ProcessorPropertyUITemplateDTO processorProperties = null;
        String propertiesJson = null;
        String dotSaparator = ".";
        String fileFormate = "json";
        String jsonFileName = null;

        if (processorType.name().equalsIgnoreCase(protocol.name())) {
            jsonFileName = JSON_ROOT_PATH + processorType + dotSaparator + fileFormate;
        } else {
            jsonFileName = JSON_ROOT_PATH + processorType + dotSaparator + protocol + dotSaparator + fileFormate;
        }

        propertiesJson = ServiceUtils.readFileFromClassPath(jsonFileName);
        processorProperties = MailBoxUtil.unmarshalFromJSON(propertiesJson, ProcessorPropertyUITemplateDTO.class);
        return processorProperties;

    }

	/**
	 * Method to build new JSON format from the older format
	 *
	 * @param uiPropertyTemplate
	 * @param legacyProps
	 * @return
	 * @throws NoSuchFieldException
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
    private static ProcessorPropertyUITemplateDTO mapLegacyPropsToTemplate(
            ProcessorPropertyUITemplateDTO uiPropertyTemplate, RemoteProcessorPropertiesDTO legacyProps)
            throws SecurityException, IllegalArgumentException, IllegalAccessException {

        for (ProcessorPropertyDTO property : uiPropertyTemplate.getStaticProperties()) {

            if (property.getName().equals(MailBoxConstants.ADD_NEW_PROPERTY)) {
                continue;
            }
            Field field;
            try {
                field = legacyProps.getClass().getDeclaredField(property.getName());
            } catch (NoSuchFieldException e) {
                LOGGER.debug("The field {} is not available in legacy prop.so continuing..", property.getName());
                continue;
            }
            String propertyValue = null;
            field.setAccessible(true);
            Object fieldValue = field.get(legacyProps);
            if (property.getName().equals(MailBoxConstants.PROPERTY_OTHER_REQUEST_HEADERS)) {
                List<HttpOtherRequestHeaderDTO> otherRequestHeaders = (List<HttpOtherRequestHeaderDTO>) fieldValue;
                propertyValue = handleOtherRequestHeaders(otherRequestHeaders);
            } else {
                propertyValue = fieldValue.toString();
            }
            property.setValue(propertyValue);

        }

        // handle handoverExecutionToJavascript from old processorproperties
        // JSON
        Field field;
        try {
            field = legacyProps.getClass().getDeclaredField(PROP_HANDOVER_EXECUTION_TO_JS);
        } catch (NoSuchFieldException e) {
            LOGGER.debug("The field {} is not available in legacy prop.so continuing..", PROP_HANDOVER_EXECUTION_TO_JS);
            return uiPropertyTemplate;
        }
        field.setAccessible(true);
        Object fieldValue = field.get(legacyProps);
        uiPropertyTemplate.setHandOverExecutionToJavaScript(Boolean.getBoolean(fieldValue.toString()));
        return uiPropertyTemplate;
    }

	/**
	 * Method to retrieve the proper Name of dynamic Properties
	 * In older Implementation some properties like "nooffilesthreshold", "payloadsizethreshold", "filerenameformat"
	 * "processedfilelocation", "errorfilelocation", "sweepedfilelocation", "httplistenerauthcheckrequired" are stored
	 * as dynamic properties but displayed to the user as static properties in dropdown. As per new implementation all properties which are
	 * known priorly will be considered as static properties and the properties added by user through "addNew" will
	 * be considered as dynamic properties. The above mentioned properties needs to be mapped as static properties.
	 * Those properties are in lowercase and needs to be converted to camel case. This method will return the camelCase version property Name
	 *
	 * @param Name
	 * @return String camelCase version of given propertyName
	 */
    private static String getPropertyNameOfDynamicProperty(String name) {

        if (propertyMapper.keySet().contains(name)) {
            return propertyMapper.get(name);
        }
        return name;
    }


	/**
	 * Method to handle dynamic properties
	 *
	 * @param processor
	 * @param propertiesDTO
	 */

    public static void setDynamicPropsinTemplate(Processor processor, ProcessorPropertyUITemplateDTO propertiesDTO) {

        // hanlding dynamic properties of processors
        if (null != processor.getDynamicProperties()) {

            ProcessorPropertyDTO propertyDTO = null;
            for (ProcessorProperty property : processor.getDynamicProperties()) {
                propertyDTO = new ProcessorPropertyDTO();
                String propertyName = getPropertyNameOfDynamicProperty(property.getProcsrPropName());
                boolean isDynamic = (propertyMapper.keySet().contains(property.getProcsrPropName())) ? false : true;
                propertyDTO.setName(propertyName);
                propertyDTO.setDisplayName(propertyName);
                propertyDTO.setValue(property.getProcsrPropValue());
                propertyDTO.setDynamic(isDynamic);
                propertyDTO.setValueProvided(true);
                propertyDTO.setType("textarea");
                propertyDTO.setDefaultValue("");
                propertiesDTO.getStaticProperties().add(propertyDTO);
            }
        }

    }

	/**
	 * Method to separate static and dynamic properties
	 *
	 * @param staticProperties
	 * @param dynamicProperties
	 */
    public static void separateStaticAndDynamicProperties(List<ProcessorPropertyDTO> staticProperties,
            List<ProcessorPropertyDTO> dynamicProperties) {

        for (ProcessorPropertyDTO properties : staticProperties) {
            if (properties.isDynamic()) {
                dynamicProperties.add(properties);
            }
        }
        staticProperties.removeAll(dynamicProperties);

    }

	/**Method to retrieve StaticProcessorProperitesDTO from staticProperties in template json
	 *
	 *
	 * @param staticProperties
	 * @param processorType
	 * @param protocol
	 *
	 * @return StaticProcessorProperitesDTO
	 *
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws JAXBException
	 * @throws IOException
	 * @throws NoSuchFieldException
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */

	public static StaticProcessorPropertiesDTO getProcessorSpecificStaticPropsFrmTemplt(List<ProcessorPropertyDTO> staticProperties, Processor processor, boolean isCreate ) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, JsonParseException, JsonMappingException, IOException, JAXBException {

		StaticProcessorPropertiesDTO propertiesDTO = null;

		if (!isCreate) {
			propertiesDTO = JSONUtil.unmarshalFromJSON(processor.getProcsrProperties(),
					StaticProcessorPropertiesDTO.class);
			getProcessorSpecificStaticProps(staticProperties, propertiesDTO);
			return propertiesDTO;
		} else {

        	 Protocol protocol = Protocol.findByCode(processor.getProcsrProtocol());
			 switch (processor.getProcessorType()) {

			 case REMOTEDOWNLOADER:
				switch (protocol) {

				case FTPS:
					propertiesDTO = new FTPDownloaderPropertiesDTO();
					getProcessorSpecificStaticProps(staticProperties, propertiesDTO);
					break;
				case FTP:
					propertiesDTO = new FTPDownloaderPropertiesDTO();
					getProcessorSpecificStaticProps(staticProperties, propertiesDTO);
					break;
				case SFTP:
					propertiesDTO = new SFTPDownloaderPropertiesDTO();
					getProcessorSpecificStaticProps(staticProperties, propertiesDTO);
					break;
				case HTTP:
					propertiesDTO = new HTTPDownloaderPropertiesDTO();
					getProcessorSpecificStaticProps(staticProperties, propertiesDTO);
					break;
				case HTTPS:
					propertiesDTO = new HTTPDownloaderPropertiesDTO();
					getProcessorSpecificStaticProps(staticProperties, propertiesDTO);
					break;
				default:
					break;

				}
				break;
			 case REMOTEUPLOADER:
				switch (protocol) {

				case FTPS:
					propertiesDTO = new FTPUploaderPropertiesDTO();
					getProcessorSpecificStaticProps(staticProperties, propertiesDTO);
					break;
				case FTP:
					propertiesDTO = new FTPUploaderPropertiesDTO();
					getProcessorSpecificStaticProps(staticProperties, propertiesDTO);
					break;
				case SFTP:
					propertiesDTO = new SFTPUploaderPropertiesDTO();
					getProcessorSpecificStaticProps(staticProperties, propertiesDTO);
					break;
				case HTTP:
					propertiesDTO = new HTTPUploaderPropertiesDTO();
					getProcessorSpecificStaticProps(staticProperties, propertiesDTO);
					break;
				case HTTPS:
					propertiesDTO = new HTTPUploaderPropertiesDTO();
					getProcessorSpecificStaticProps(staticProperties, propertiesDTO);
					break;
				default:
					break;

				}
				break;
			 case SWEEPER:
				propertiesDTO = new SweeperPropertiesDTO();
				getProcessorSpecificStaticProps(staticProperties, propertiesDTO);
				break;

			 case DROPBOXPROCESSOR:
				propertiesDTO = new DropboxProcessorPropertiesDTO();
				getProcessorSpecificStaticProps(staticProperties, propertiesDTO);
				break;

			 case HTTPASYNCPROCESSOR:
				propertiesDTO = new HTTPListenerPropertiesDTO();
				getProcessorSpecificStaticProps(staticProperties, propertiesDTO);
				break;

			 case HTTPSYNCPROCESSOR:
				propertiesDTO = new HTTPListenerPropertiesDTO();
				getProcessorSpecificStaticProps(staticProperties, propertiesDTO);
				break;

			 case FILEWRITER :
				propertiesDTO = new FileWriterPropertiesDTO();
				getProcessorSpecificStaticProps(staticProperties, propertiesDTO);
				break;
			 }
			return propertiesDTO;
		}
	}	


	/**
	 * Method to retrieve the static properties DTO after mapping static properties in JSON template
	 *
	 * @param staticPropertiesInTemplateJson
	 * @param staticPropertiesDTO
	 * @return
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
    private static StaticProcessorPropertiesDTO getProcessorSpecificStaticProps(
            List<ProcessorPropertyDTO> staticPropertiesInTemplateJson, StaticProcessorPropertiesDTO staticPropertiesDTO)
            throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {

        for (ProcessorPropertyDTO property : staticPropertiesInTemplateJson) {

            if (property.getName().equals(MailBoxConstants.ADD_NEW_PROPERTY)) {
                continue;
            }
            Field field = staticPropertiesDTO.getClass().getDeclaredField(property.getName());
            field.setAccessible(true);
            String propertyValue = property.getValue();
            if (field.getType().equals(Boolean.TYPE)) {
                field.setBoolean(staticPropertiesDTO, Boolean.valueOf(propertyValue));
            } else if (field.getType().equals(Integer.TYPE)) {
                if (!MailBoxUtil.isEmpty(propertyValue)) {
                    field.setInt(staticPropertiesDTO, Integer.valueOf(propertyValue).intValue());
                }
            } else if (field.getType().equals(String.class)) {
                field.set(staticPropertiesDTO, propertyValue);
            }
        }
        return staticPropertiesDTO;

    }

	/**
	 * Method to retrieve list of static properties from staticPropertiesDTO object
	 *
	 * @param staticPropertiesDTO
	 * @return list of static properties
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws IOException
	 * @throws JAXBException
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 */
    private static void hydrateTemplate(StaticProcessorPropertiesDTO staticPropertiesDTO,
            ProcessorPropertyUITemplateDTO processorPropertiesDefinitionDto) throws IllegalArgumentException,
            IllegalAccessException, JsonParseException, JsonMappingException, NoSuchFieldException, SecurityException,
            JAXBException, IOException {

        for (ProcessorPropertyDTO staticProperty : processorPropertiesDefinitionDto.getStaticProperties()) {

            if (staticProperty.getName().equals(MailBoxConstants.ADD_NEW_PROPERTY)) {
                continue;
            }

            Field field = staticPropertiesDTO.getClass().getDeclaredField(staticProperty.getName());
            field.setAccessible(true);
            Object fieldValue = field.get(staticPropertiesDTO);
            String propertyValue = fieldValue.toString();

            if (staticProperty.getName().equals(MailBoxConstants.PORT_PROPERTY)
                    && !MailBoxUtil.isEmpty((Integer.parseInt(propertyValue) == 0) ? "" : propertyValue)) {
                staticProperty.setReadOnly(true);
            }
            boolean isValueAvailable = !(MailBoxUtil.isEmpty(propertyValue));
            if (field.getType().equals(Boolean.TYPE)) {
                isValueAvailable = (Boolean.valueOf(propertyValue).equals(true) ? true : false);
            } else if (field.getType().equals(Integer.TYPE)) {
                if (Integer.parseInt(propertyValue) == 0) {
                    propertyValue = "";
                    isValueAvailable = false;
                } else {
                    propertyValue = fieldValue.toString();
                    isValueAvailable = !(MailBoxUtil.isEmpty(propertyValue));
                }
            }
            staticProperty.setValue(propertyValue);
            staticProperty.setValueProvided(isValueAvailable);
        }
    }
    
	/**
	 * Method to retrieve StaticProcessorProperitesDTO from
	 * remoteProcessorProperties
	 * 
	 * @param remoteProcessorProperties
	 *            The RemoteProcessorPropertiesDTO
	 * @param processorType
	 * @param protocol
	 * @return StaticProcessorPropertiesDTO
	 * @throws NoSuchFieldException
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
    private static StaticProcessorPropertiesDTO getProcessorBasedStaticPropsFrmLegacyProps(
            RemoteProcessorPropertiesDTO remoteProcessorProperties, ProcessorType processorType, Protocol protocol)
            throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {

        StaticProcessorPropertiesDTO propertiesDTO = null;

        switch (processorType) {

        case REMOTEDOWNLOADER:
            switch (protocol) {

            case FTPS:
                propertiesDTO = new FTPDownloaderPropertiesDTO();
                mapLegacyProps(remoteProcessorProperties, propertiesDTO);
                break;
            case FTP:
                propertiesDTO = new FTPDownloaderPropertiesDTO();
                mapLegacyProps(remoteProcessorProperties, propertiesDTO);
                break;
            case SFTP:
                propertiesDTO = new SFTPDownloaderPropertiesDTO();
                mapLegacyProps(remoteProcessorProperties, propertiesDTO);
                break;
            case HTTP:
                propertiesDTO = new HTTPDownloaderPropertiesDTO();
                mapLegacyProps(remoteProcessorProperties, propertiesDTO);
                break;
            case HTTPS:
                propertiesDTO = new HTTPDownloaderPropertiesDTO();
                mapLegacyProps(remoteProcessorProperties, propertiesDTO);
                break;
            default:
                break;

            }
            break;
        case REMOTEUPLOADER:
            switch (protocol) {

            case FTPS:
                propertiesDTO = new FTPUploaderPropertiesDTO();
                mapLegacyProps(remoteProcessorProperties, propertiesDTO);
                break;
            case FTP:
                propertiesDTO = new FTPUploaderPropertiesDTO();
                mapLegacyProps(remoteProcessorProperties, propertiesDTO);
                break;
            case SFTP:
                propertiesDTO = new SFTPUploaderPropertiesDTO();
                mapLegacyProps(remoteProcessorProperties, propertiesDTO);
                break;
            case HTTP:
                propertiesDTO = new HTTPUploaderPropertiesDTO();
                mapLegacyProps(remoteProcessorProperties, propertiesDTO);
                break;
            case HTTPS:
                propertiesDTO = new HTTPUploaderPropertiesDTO();
                mapLegacyProps(remoteProcessorProperties, propertiesDTO);
                break;
            default:
                break;

            }
            break;
        case SWEEPER:
            propertiesDTO = new SweeperPropertiesDTO();
            mapLegacyProps(remoteProcessorProperties, propertiesDTO);
            break;

        case DROPBOXPROCESSOR:
            propertiesDTO = new DropboxProcessorPropertiesDTO();
            mapLegacyProps(remoteProcessorProperties, propertiesDTO);
            break;

        case HTTPASYNCPROCESSOR:
            propertiesDTO = new HTTPListenerPropertiesDTO();
            mapLegacyProps(remoteProcessorProperties, propertiesDTO);
            break;

        case HTTPSYNCPROCESSOR:
            propertiesDTO = new HTTPListenerPropertiesDTO();
            mapLegacyProps(remoteProcessorProperties, propertiesDTO);
            break;

        case FILEWRITER:
            propertiesDTO = new FileWriterPropertiesDTO();
            break;
        }
        return propertiesDTO;

    }
    
	/**
	 * Method StaticProcessorPropertiesDTO from RemoteProcessorPropertiesDTO
	 * object
	 * 
	 * @param source
	 *            The RemoteProcessorPropertiesDTO
	 * @param target
	 *            The StaticProcessorPropertiesDTO
	 * 
	 * @throws NoSuchFieldException
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	private static void mapLegacyProps(RemoteProcessorPropertiesDTO source, StaticProcessorPropertiesDTO target) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {

		for (Field field : target.getClass().getDeclaredFields()) {

			Field fieldInOldJson;
            try {
            	fieldInOldJson = source.getClass().getDeclaredField(field.getName());
            } catch(NoSuchFieldException e) {
	           	if ((propertyMapper.values().contains(field.getName()))) {
	           	    LOGGER.debug("Dynamic Property is handled in another method ignore this exception",field.getName());
	           	} else {
	           		LOGGER.debug("The field {} is not available in legacy prop.so continuing..",field.getName());
	           	}
	           	continue;
	        }

			  field.setAccessible(true);
			  fieldInOldJson.setAccessible(true);
			  Object propertyValue = fieldInOldJson.get(source);
			  if (field.getName().equals(MailBoxConstants.PROPERTY_OTHER_REQUEST_HEADERS)) {
				  List <HttpOtherRequestHeaderDTO> otherRequestHeaders = (List<HttpOtherRequestHeaderDTO>)propertyValue;
				  propertyValue = handleOtherRequestHeaders(otherRequestHeaders);
			  }
			  field.set(target, propertyValue);

		}
		target.setHandOverExecutionToJavaScript(source.isHandOverExecutionToJavaScript());

	}
    
	/**
	 * Method to construct HttpOtherRequestHeaderDTO from template Json.
	 * 
	 * @param otherRequestHeaders
	 * @return String Value of otherRequestHeader
	 */
	private static String handleOtherRequestHeaders(List <HttpOtherRequestHeaderDTO> otherRequestHeaders) {
		StringBuilder otherRequestHeader = new StringBuilder();
		for (HttpOtherRequestHeaderDTO header : otherRequestHeaders) {
			otherRequestHeader.append(header.getName()).append(":").append(header.getValue()).append(",");
		}
		String otherRequestHeaderStr = otherRequestHeader.toString();
		return otherRequestHeaderStr.substring(0, otherRequestHeaderStr.length() - 2);
	}
    
	/**
	 * Method retrieve dynamic properties from processor for construct
	 * ProcessorPropertyUITemplate.
	 * 
	 * @param staticProcessorPropertiesDTO
	 *            The staticProcessorPropertiesDTO of the processor
	 * @param processor
	 * @throws NoSuchFieldException
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	private static void handleDynamicProperties(StaticProcessorPropertiesDTO staticProcessorPropertiesDTO, Processor processor) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {

		if (null != processor.getDynamicProperties()) {

		    for (ProcessorProperty property : processor.getDynamicProperties()) {
		        String propertyName = getPropertyNameOfDynamicProperty(property.getProcsrPropName());
		        boolean isDynamic = (propertyMapper.keySet().contains(property.getProcsrPropName())) ? false : true;
		        if (!isDynamic) {
		        	Field field = staticProcessorPropertiesDTO.getClass().getDeclaredField(propertyName);
		        	field.setAccessible(true);
					String propertyValue = property.getProcsrPropValue();
					if (field.getType().equals(Boolean.TYPE)) {
						field.setBoolean(staticProcessorPropertiesDTO, Boolean.valueOf(propertyValue));
					} else if (field.getType().equals(Integer.TYPE)) {
						if (!MailBoxUtil.isEmpty(propertyValue)) {
							field.setInt(staticProcessorPropertiesDTO, Integer.valueOf(propertyValue).intValue());
						}
					} else if (field.getType().equals(String.class)){
						field.set(staticProcessorPropertiesDTO, propertyValue);
					}
		        }
		    }
		}
	}
    
	/**
	 * Method to construct list of  folderDTO from the folderDTOTemplateList
	 * 
	 * @param folderPropertiesDTO
	 *            The ProcessorFolderPropertyDTO
	 * 
	 * @return List<FolderDTO> return List of FolderDto's
	 */
    public static List<FolderDTO> getFolderProperties(List<ProcessorFolderPropertyDTO> folderPropertiesDTO) {

        List<FolderDTO> folderProperties = new ArrayList<FolderDTO>();
        FolderDTO folder = null;
        for (ProcessorFolderPropertyDTO folderProperty : folderPropertiesDTO) {
            folder = new FolderDTO();
            folder.setFolderURI(folderProperty.getFolderURI());
            folder.setFolderType(folderProperty.getFolderType());
            folder.setFolderDesc(folderProperty.getFolderDesc());
            folderProperties.add(folder);
        }
        return folderProperties;
    }
    
    /**
     * Method to construct FolderTemplate by given processor which is to be
     * retrieved from db.
     * 
     * @param processor
     *            The Processor of the the mailBox
     * @param uiPropTemplate
     *            The ProcessorPropertyUITemplateDTO of the Processor
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws JAXBException
     * @throws IOException
     */
    public static void constructFolderTemplate(Processor processor,
            ProcessorPropertyUITemplateDTO processorPropertiesDefinitionDto) throws JsonParseException,
            JsonMappingException, JAXBException, IOException {

        List<ProcessorFolderPropertyDTO> processorFolderProperties = new ArrayList<ProcessorFolderPropertyDTO>();
        ProcessorFolderPropertyDTO property = null;
        if (null != processor.getFolders()) {

            for (Folder folder : processor.getFolders()) {

                property = new ProcessorFolderPropertyDTO();
                property.setValidationRules(processorPropertiesDefinitionDto.getFolderProperties().get(0)
                        .getValidationRules());

                switch (processor.getProcessorType()) {
                case FILEWRITER:
                    handleFolderProperties(property, folder);
                    property.setFolderDisplayType(MailBoxConstants.PROPERTY_FILEWRITE_DISPLAYTYPE);
                    property.setReadOnly(true);
                    break;
                case SWEEPER:
                    handleFolderProperties(property, folder);
                    property.setFolderDisplayType(MailBoxConstants.PROPERTY_SWEEPER_DISPLAYTYPE);
                    property.setReadOnly(true);
                    break;

                case REMOTEUPLOADER:
                    handleFolderProperties(property, folder);
                    property.setFolderDisplayType((folder.getFldrType().equalsIgnoreCase(
                            FolderType.PAYLOAD_LOCATION.name()) ? MailBoxConstants.PROPERTY_LOCAL_PAYLOAD_LOCATION_DISPLAYTYPE
                            : MailBoxConstants.PROPERTY_REMOTE_TARGET_LOCATION_DISPLAYTYPE));
                    property.setReadOnly(false);
                    break;

                case REMOTEDOWNLOADER:
                    handleFolderProperties(property, folder);
                    property.setFolderDisplayType((folder.getFldrType().equalsIgnoreCase(
                            FolderType.PAYLOAD_LOCATION.name()) ? MailBoxConstants.PROPERTY_REMOTE_PAYLOAD_LOCATION_DISPLAYTYPE
                            : MailBoxConstants.PROPERTY_LOCAL_TARGET_LOCATION_DISPLAYTYPE));
                    property.setReadOnly(false);
                    break;

                default:
                    break;
                }
                processorFolderProperties.add(property);
            }
            processorPropertiesDefinitionDto.setFolderProperties(processorFolderProperties);
        }
    }
    
	/**
	 * Method to construct ProcessorFolderPropertyDTO from Folder which is to be
	 * retrieve from db.
	 * 
	 * @param property
	 *            The ProcessorFolderPropertyDTO which is to be construct from
	 *            Folder
	 * @param folder
	 *            The Folder for construct ProcessorFolderPropertyDTO
	 */
    private static void handleFolderProperties(ProcessorFolderPropertyDTO property, Folder folder) {

        property.setFolderURI(folder.getFldrUri());
        property.setFolderType(folder.getFldrType());
        property.setFolderDesc(folder.getFldrDesc());
        if (MailBoxUtil.isEmpty(folder.getFldrUri())) {
            property.setMandatory(false);
            property.setValueProvided(false);
        } else {
            property.setMandatory(true);
            property.setValueProvided(true);
        }

    }
    
    /**
     * Method to retrieve the credential Display Type from given credential Entity
     * 
     * @param credential
     *        The credential Entity in DB from which the display Name has to be deduced
     * 
     * @return String - DisplayType of given credential
     */
    private static String getCredentialDisplayType(Credential credential) {

        String credentialDisplayType = null;

        switch (credential.getCredsType().toLowerCase()) {

        case MailBoxConstants.LOGIN_CREDENTIAL:
            credentialDisplayType = MailBoxConstants.PROPERTY_LOGIN_CREDENTIAL_DISPLAY_TYPE;
            break;
        case MailBoxConstants.TRUSTSTORE_CERT:
            credentialDisplayType = MailBoxConstants.PROPERTY_TRUSTORE_DISPLAY_TYPE;
            break;
        case MailBoxConstants.SSH_KEYPAIR:
            credentialDisplayType = (credential.getCredsIdpType().equals("PRIVATE")) ? MailBoxConstants.PROPERTY_SSH_PRIVATE_KEY_DISPLAY_TYPE
                    : MailBoxConstants.PROPERTY_SSH_PUBLIC_KEY_DISPLAY_TYPE;
            break;
        }
        return credentialDisplayType;

    }
    
    
    /**
     * Method to construct credential Template from credential Entity
     * 
     * @param credentialTemplate
     *         The template in Json for credentialProperties to be constructed from credential Entity
     * @param credential
     *          The credential Entity in DB from which credentialTempalte has to be constructed
     *         
     */
    private static void handleCredentialProperties(ProcessorCredentialPropertyDTO credentialPropertyTemplate,
            Credential credential) {

        String credentialDisplayType = getCredentialDisplayType(credential);
        credentialPropertyTemplate.setCredentialType(credential.getCredsType());
        credentialPropertyTemplate.setCredentialURI(credential.getCredsUri());
        credentialPropertyTemplate.setIdpType(credential.getCredsIdpType());
        credentialPropertyTemplate.setIdpURI(credential.getCredsIdpUri());
        credentialPropertyTemplate.setCredentialDisplayType(credentialDisplayType);
        credentialPropertyTemplate.setUserId(credential.getCredsUsername());
        credentialPropertyTemplate.setPassword(credential.getCredsPassword());
        credentialPropertyTemplate.setValueProvided(true);
    }
    
    /**
     * Method to construct list of  CredentialDTO from the CredentialDTOTemRplateList
     * 
     * @param folderPropertiesDTO
     *            The ProcessorFolderPropertyDTO
     * 
     * @return List<FolderDTO> return List of CredentialDTO
     */
    public static List<CredentialDTO> getCredentialProperties(
            List<ProcessorCredentialPropertyDTO> credentialPropertiesTemplateDTO) {

        List<CredentialDTO> credentialProperties = new ArrayList<CredentialDTO>();
        CredentialDTO credential = null;
        for (ProcessorCredentialPropertyDTO credentialProperty : credentialPropertiesTemplateDTO) {
            credential = new CredentialDTO();
            credential.setCredentialType(credentialProperty.getCredentialType());
            credential.setCredentialURI(credentialProperty.getCredentialURI());
            credential.setIdpType(credentialProperty.getIdpType());
            credential.setIdpURI(credentialProperty.getIdpURI());
            credential.setUserId(credentialProperty.getUserId());
            credential.setPassword(credentialProperty.getPassword());
            credentialProperties.add(credential);
        }
        return credentialProperties;
    }
    
    /**
     * Method to construct Credential Template form the given Processor entity
     * and set that in the propertiesJsonTemplate DTO
     * 
     * @param processor
     *            The processor entity from which the credential Template has to
     *            be constructed
     * @param propertiesJsonTemplate
     *            The processor properties JSON template in which the
     *            credentialTemplate needs to be set
     */
    public static void constructCredentialPropertiesTemplate(Processor processor,
            ProcessorPropertyUITemplateDTO propertiesJsonTemplate) {

        List<ProcessorCredentialPropertyDTO> processorCredentialProperties = new ArrayList<ProcessorCredentialPropertyDTO>();
        ProcessorCredentialPropertyDTO credentialPropertyTemplate = null;
        if (null != processor.getCredentials()) {

            for (Credential credential : processor.getCredentials()) {

                credentialPropertyTemplate = new ProcessorCredentialPropertyDTO();
                handleCredentialProperties(credentialPropertyTemplate, credential);
                processorCredentialProperties.add(credentialPropertyTemplate);
            }
            if (processorCredentialProperties.size() > 0) propertiesJsonTemplate.setCredentialProperties(processorCredentialProperties);
        }
    }
}
