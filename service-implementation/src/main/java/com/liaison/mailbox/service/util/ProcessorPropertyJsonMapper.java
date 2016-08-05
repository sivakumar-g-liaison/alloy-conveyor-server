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
import java.util.Set;

import javax.xml.bind.JAXBException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

import com.liaison.commons.jaxb.JAXBUtility;
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
import com.liaison.mailbox.service.dto.configuration.request.HTTPOtherRequestHeaderDTO;
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
	private static final String DOT_SEPERATOR = ".";
	private static final String FILE_FORMAT = "json";

	private static Map<String, String> propertyMapper = null;

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
	 * Method to retrieve ProcessorPropertyUITemplateDTO in format stored in Template JSON from the properties JSON
	 * stored in DB. This method will convert the json to ProcessorPropertyUITemplateDTO format even if it is in older
	 * format (RemoteProcessorPropertiesDTO)
	 *
	 * @param propertyJson
	 * @param processor
	 * @return ProcessorPropertyUITemplateDTO
	 * @throws IOException
	 * @throws NoSuchFieldException
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public static ProcessorPropertyUITemplateDTO getHydratedUIPropertyTemplate(String propertyJson, Processor processor)
			throws IOException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {

		Protocol protocol = Protocol.findByCode(processor.getProcsrProtocol());
		ProcessorPropertyUITemplateDTO uiPropTemplate = getTemplate(processor.getProcessorType(), protocol);
		if (!MailBoxUtil.isEmpty(propertyJson)) {

			// In order to provide backward compatibility for older processor
			// entities
			// try to unmarshal the properties json with new class
			// "ProcessorPropertyUITemplateDTO"
			// if the unmarshalling fails then try to unmarshal it with old
			// class "RemoteProcessorPropertiesDTO"
			StaticProcessorPropertiesDTO staticProperties = getProcessorBasedStaticPropsFromJson(propertyJson,
					processor);
			hydrateTemplate(staticProperties, uiPropTemplate);
			uiPropTemplate.setHandOverExecutionToJavaScript(staticProperties.isHandOverExecutionToJavaScript());
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
	 * Method to retrieve static properties in format stored in DB (ie StaticProcessorPropertiesDTO) from the properties
	 * JSON stored in DB. This method will convert the json to StaticProcessorPropertiesDTO format even if it is in
	 * older format (RemoteProcessorPropertiesDTO)
	 *
	 * @param propertyJson
	 * @param processor
	 * @return
	 * @throws IOException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public static StaticProcessorPropertiesDTO getProcessorBasedStaticPropsFromJson(String propertyJson, Processor processor)
			throws IOException, IllegalArgumentException, IllegalAccessException {

		return getProcessorBasedStaticPropsFromJson(propertyJson, 
											Protocol.findByCode(processor.getProcsrProtocol()), 
											processor.getProcessorType(), 
											processor.getDynamicProperties());
	}
	
	
	/**
	 * Method to retrieve static properties in format stored in DB (ie StaticProcessorPropertiesDTO) from the properties
	 * JSON stored in DB. This method will convert the json to StaticProcessorPropertiesDTO format even if it is in
	 * older format (RemoteProcessorPropertiesDTO)
	 *
	 * @param propertyJson
	 * 				Processor Properties stored as Json in DB
	 * @param Protocol
	 * 				Protocol of the processor
	 * @param ProcessorType
	 * 				Type of the Processor
	 * @param dyamicProperties
	 * 				set contain dynamic properties of the processor
	 * @return StaticProcessorPropertiesDTO
	 * @throws IOException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public static StaticProcessorPropertiesDTO getProcessorBasedStaticPropsFromJson(String propertyJson, Protocol protocol, ProcessorType procsrType, Set<ProcessorProperty> dynamicProperties)
			throws IOException, IllegalArgumentException, IllegalAccessException {

		StaticProcessorPropertiesDTO staticProcessorProperties = null;
		try {
			staticProcessorProperties = getProcessorBasedStaticProps(propertyJson);
		} catch (JsonMappingException | JsonParseException | JAXBException e) {

			RemoteProcessorPropertiesDTO leagcyProps = MailBoxUtil.unmarshalFromJSON(propertyJson, RemoteProcessorPropertiesDTO.class);
			staticProcessorProperties = getProcessorBasedStaticPropsFrmLegacyProps(procsrType, protocol);
			mapLegacyProps(leagcyProps, staticProcessorProperties);
			handleDynamicProperties(staticProcessorProperties, dynamicProperties);
		}
		return staticProcessorProperties;

	}

	/**
	 * Method to retrieve StaticProcessorPropertiesDTO by given procsrProperties retrieved from db.
	 *
	 * @param propertyJson
	 * @return The propertyJson is string of procsrProperties retrieved from db.
	 * @throws JAXBException
	 * @throws IOException
	 */

	private static StaticProcessorPropertiesDTO getProcessorBasedStaticProps(String propertyJson) throws JAXBException, IOException {
		return JAXBUtility.unmarshalFromJSON(propertyJson, StaticProcessorPropertiesDTO.class);
	}


	/**
	 * Method to retrieve ProcessorPropertyUITemplateDTO from JsonTemplate by given processorType and protocol
	 *
	 * @param processorType The processorType of the Processor
	 * @param protocol The protocol of the Processor
	 * @return ProcessorPropertyUITemplateDTO
	 * @throws IOException
	 */
	public static ProcessorPropertyUITemplateDTO getTemplate(ProcessorType processorType, Protocol protocol) throws IOException {

		ProcessorPropertyUITemplateDTO processorProperties = null;
		String propertiesJson = null;
		StringBuilder jsonFileName = new StringBuilder().append(JSON_ROOT_PATH)
		                 .append(processorType)
		                 .append(DOT_SEPERATOR);

		if (!processorType.name().equalsIgnoreCase(protocol.name())) {
		    jsonFileName.append(protocol)
		                .append(DOT_SEPERATOR);
		}
		jsonFileName.append(FILE_FORMAT);

		propertiesJson = ServiceUtils.readFileFromClassPath(jsonFileName.toString());
		processorProperties = MailBoxUtil.unmarshalFromJSON(propertiesJson, ProcessorPropertyUITemplateDTO.class);
		return processorProperties;

	}


	/**
	 * Method to retrieve the proper Name of dynamic Properties In older Implementation some properties like
	 * "nooffilesthreshold", "payloadsizethreshold", "filerenameformat" "processedfilelocation", "errorfilelocation",
	 * "sweepedfilelocation", "httplistenerauthcheckrequired" are stored as dynamic properties but displayed to the user
	 * as static properties in dropdown. As per new implementation all properties which are known priorly will be
	 * considered as static properties and the properties added by user through "addNew" will be considered as dynamic
	 * properties. The above mentioned properties needs to be mapped as static properties. Those properties are in
	 * lowercase and needs to be converted to camel case. This method will return the camelCase version property Name
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
				if (isDynamic) {
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

	/**
	 * Method to retrieve StaticProcessorProperitesDTO from staticProperties in template json
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

	public static StaticProcessorPropertiesDTO getProcessorPropInstanceFor(ProcessorType processorType , Protocol protocol) {

		switch (processorType) {

			case REMOTEDOWNLOADER:
				switch (protocol) {

					case FTPS:
						return new FTPDownloaderPropertiesDTO();
					case FTP:
						return new FTPDownloaderPropertiesDTO();
					case SFTP:
						return new SFTPDownloaderPropertiesDTO();
					case HTTP:
						return new HTTPDownloaderPropertiesDTO();
					case HTTPS:
						return new HTTPDownloaderPropertiesDTO();
					default:
						LOGGER.error("The processor type {} and protocol {} is not supported",processorType,protocol);
						return null;
				}

			case REMOTEUPLOADER:
				switch (protocol) {

					case FTPS:
						return new FTPUploaderPropertiesDTO();
					case FTP:
						return new FTPUploaderPropertiesDTO();
					case SFTP:
						return new SFTPUploaderPropertiesDTO();
					case HTTP:
						return new HTTPUploaderPropertiesDTO();
					case HTTPS:
						return new HTTPUploaderPropertiesDTO();
					default:
						LOGGER.error("The processor type {} and protocol {} is not supported",processorType,protocol);
						return null;

				}

			case SWEEPER:
				return new SweeperPropertiesDTO();
			case DROPBOXPROCESSOR:
				return new DropboxProcessorPropertiesDTO();
			case HTTPASYNCPROCESSOR:
				return new HTTPListenerPropertiesDTO();
			case HTTPSYNCPROCESSOR:
				return new HTTPListenerPropertiesDTO();
			case FILEWRITER:
				return new FileWriterPropertiesDTO();

			}

		LOGGER.error("The processor type {} and protocol {} is not supported",processorType,protocol);
		return null;
	}


	/**
	 * Method to retrieve the static properties DTO after mapping static properties in JSON template
	 *
	 * @param from
	 * @param to
	 * @return
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	public static void transferProps(List<ProcessorPropertyDTO> from, StaticProcessorPropertiesDTO to)
			throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {

		for (ProcessorPropertyDTO property : from) {

			if (property.getName().equals(MailBoxConstants.ADD_NEW_PROPERTY)) {
				continue;
			}
			Field field = to.getClass().getDeclaredField(property.getName());
			field.setAccessible(true);
			String propertyValue = property.getValue();
			if (field.getType().equals(Boolean.TYPE)) {
				field.setBoolean(to, Boolean.valueOf(propertyValue));
			} else if (field.getType().equals(Integer.TYPE)) {
				if (!MailBoxUtil.isEmpty(propertyValue)) {
					field.setInt(to, Integer.valueOf(propertyValue).intValue());
				} else {
					field.setInt(to, 0);
				}
			} else if (field.getType().equals(String.class)) {
				field.set(to, propertyValue);
			}
		}

	}

	/**
	 * Method to retrieve list of static properties from staticPropertiesDTO object
	 *
	 * @param staticPropertiesDTO
	 * @return list of static properties
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 */
	private static void hydrateTemplate(StaticProcessorPropertiesDTO staticPropertiesDTO,
			ProcessorPropertyUITemplateDTO processorPropertiesDefinitionDto) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {

		for (ProcessorPropertyDTO staticProperty : processorPropertiesDefinitionDto.getStaticProperties()) {

			if (MailBoxConstants.ADD_NEW_PROPERTY.equals(staticProperty.getName())) {
				continue;
			}

			Field field = staticPropertiesDTO.getClass().getDeclaredField(staticProperty.getName());
			field.setAccessible(true);
			Object fieldValue = field.get(staticPropertiesDTO);
			String propertyValue = (null == fieldValue) ? "" : fieldValue.toString();

			if (MailBoxConstants.PORT_PROPERTY.equals(staticProperty.getName())
					&& !MailBoxUtil.isEmpty((Integer.parseInt(propertyValue) == 0) ? "" : propertyValue)) {
				staticProperty.setReadOnly(true);
			}
			boolean isValueAvailable = !(MailBoxUtil.isEmpty(propertyValue));
			if (Boolean.TYPE.equals(field.getType())) {
				isValueAvailable = (Boolean.valueOf(propertyValue).equals(true) ? true : false);
			} else if (Integer.TYPE.equals(field.getType())) {
				if (Integer.parseInt(propertyValue) == 0) {
					propertyValue = "";
					isValueAvailable = false;
				} else {
					isValueAvailable = !(MailBoxUtil.isEmpty(propertyValue));
				}
			}
            if (MailBoxConstants.PROPERTY_LENS_VISIBILITY.equals(staticProperty.getName())) {
                String value = Boolean.toString(true).equals(propertyValue) ? MailBoxConstants.LENS_VISIBLE : MailBoxConstants.LENS_INVISIBLE;
                staticProperty.setValue(value);
            } else {
                staticProperty.setValue(propertyValue);
            }
			staticProperty.setValueProvided(isValueAvailable);
		}
	}

	/**
	 * Method to retrieve StaticProcessorProperitesDTO from remoteProcessorProperties
	 *
	 * @param remoteProcessorProperties The RemoteProcessorPropertiesDTO
	 * @param processorType
	 * @param protocol
	 * @return StaticProcessorPropertiesDTO
	 * @throws NoSuchFieldException
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	private static StaticProcessorPropertiesDTO getProcessorBasedStaticPropsFrmLegacyProps(ProcessorType processorType, Protocol protocol) {

		switch (processorType) {

			case REMOTEDOWNLOADER:
				switch (protocol) {

					case FTPS:
						return new FTPDownloaderPropertiesDTO();
					case FTP:
						return new FTPDownloaderPropertiesDTO();
					case SFTP:
						return new SFTPDownloaderPropertiesDTO();
					case HTTP:
						return new HTTPDownloaderPropertiesDTO();
					case HTTPS:
						return new HTTPDownloaderPropertiesDTO();
					default:
						LOGGER.error("The processor type {} and protocol {} is not supported", processorType, protocol);
						return null;

				}
			case REMOTEUPLOADER:
				switch (protocol) {

					case FTPS:
						return new FTPUploaderPropertiesDTO();
					case FTP:
						return new FTPUploaderPropertiesDTO();
					case SFTP:
						return new SFTPUploaderPropertiesDTO();
					case HTTP:
						return new HTTPUploaderPropertiesDTO();
					case HTTPS:
						return new HTTPUploaderPropertiesDTO();
					default:
						LOGGER.error("The processor type {} and protocol {} is not supported", processorType, protocol);
						return null;

				}
			case SWEEPER:
				return new SweeperPropertiesDTO();
			case DROPBOXPROCESSOR:
				return new DropboxProcessorPropertiesDTO();
			case HTTPASYNCPROCESSOR:
				return new HTTPListenerPropertiesDTO();
			case HTTPSYNCPROCESSOR:
				return new HTTPListenerPropertiesDTO();
			case FILEWRITER:
				return new FileWriterPropertiesDTO();
		}
		LOGGER.error("The processor type {} and protocol {} is not supported", processorType, protocol);
		return null;

	}

	/**
	 * Method StaticProcessorPropertiesDTO from RemoteProcessorPropertiesDTO object
	 *
	 * @param source The RemoteProcessorPropertiesDTO
	 * @param target The StaticProcessorPropertiesDTO
	 *
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	@SuppressWarnings("unchecked")
	private static void mapLegacyProps(RemoteProcessorPropertiesDTO source, StaticProcessorPropertiesDTO target)
			throws IllegalArgumentException, IllegalAccessException {

		for (Field field : target.getClass().getDeclaredFields()) {

			Field fieldInOldJson;
			try {
				fieldInOldJson = source.getClass().getDeclaredField(field.getName());
				
				// GMB-645:Handling of Synthetic Fields while converting from legacy processor properties json template to latest json template
				// The synthetic fields are compiler generated fields and they need not to be considered for processing
				if (fieldInOldJson.isSynthetic()) {
					continue;
				}
				
			} catch (NoSuchFieldException e) {
				if ((propertyMapper.values().contains(field.getName()))) {
					LOGGER.debug("Dynamic Property is handled in another method ignore this exception", field.getName());
				} else {
					LOGGER.debug("The field {} is not available in legacy prop.so continuing..", field.getName());
				}
				continue;
			}

			field.setAccessible(true);
			fieldInOldJson.setAccessible(true);
			Object propertyValue = fieldInOldJson.get(source);
			if (field.getName().equals(MailBoxConstants.PROPERTY_OTHER_REQUEST_HEADERS)) {
				List<HTTPOtherRequestHeaderDTO> otherRequestHeaders = (List<HTTPOtherRequestHeaderDTO>) propertyValue;
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
	private static String handleOtherRequestHeaders(List<HTTPOtherRequestHeaderDTO> otherRequestHeaders) {
		StringBuilder otherRequestHeader = new StringBuilder();
		for (HTTPOtherRequestHeaderDTO header : otherRequestHeaders) {
			otherRequestHeader.append(header.getName()).append(":").append(header.getValue()).append(",");
		}

		String otherRequestHeaderStr = otherRequestHeader.toString();
		return otherRequestHeaderStr.length() > 0 ? otherRequestHeaderStr.substring(0, otherRequestHeaderStr.length() - 1) : "";
	}

	/**
	 * Method retrieve dynamic properties from processor for construct ProcessorPropertyUITemplate.
	 *
	 * @param staticProcessorPropertiesDTO The staticProcessorPropertiesDTO of the processor
	 * @param processor
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	private static void handleDynamicProperties(StaticProcessorPropertiesDTO staticProcessorPropertiesDTO, Set<ProcessorProperty> dynamicProperties)
			throws IllegalArgumentException, IllegalAccessException {

		if (null != dynamicProperties) {

			for (ProcessorProperty property : dynamicProperties) {
				Field field;
				String propertyName = getPropertyNameOfDynamicProperty(property.getProcsrPropName());
				boolean isDynamic = (propertyMapper.keySet().contains(property.getProcsrPropName())) ? false : true;
				if (!isDynamic) {
					try {
						field = staticProcessorPropertiesDTO.getClass().getDeclaredField(propertyName);
					} catch (NoSuchFieldException e) {
						LOGGER.debug("The field {} is not available in legacy prop.so continuing..", propertyName);
						continue;
					}

					field.setAccessible(true);
					String propertyValue = property.getProcsrPropValue();
					if (field.getType().equals(Boolean.TYPE)) {
						field.setBoolean(staticProcessorPropertiesDTO, Boolean.valueOf(propertyValue));
					} else if (field.getType().equals(Integer.TYPE)) {
						if (!MailBoxUtil.isEmpty(propertyValue)) {
							field.setInt(staticProcessorPropertiesDTO, Integer.valueOf(propertyValue).intValue());
						}
					} else if (field.getType().equals(String.class)) {
						field.set(staticProcessorPropertiesDTO, propertyValue);
					}
				}
			}
		}
	}


	/**
	 * Method to construct list of folderDTO from the folderDTOTemplateList
	 *
	 * @param folderPropertiesDTO The ProcessorFolderPropertyDTO
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
	 * Method to construct FolderTemplate by given processor which is to be retrieved from db.
	 *
	 * @param processor The Processor of the the mailBox
	 * @param uiPropTemplate The ProcessorPropertyUITemplateDTO of the Processor
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws JAXBException
	 * @throws IOException
	 */
	public static void constructFolderTemplate(Processor processor, ProcessorPropertyUITemplateDTO processorPropertiesDefinitionDto) {

		if (null != processor.getFolders() && !processor.getFolders().isEmpty()) {

			for (ProcessorFolderPropertyDTO propsFolderDTO : processorPropertiesDefinitionDto.getFolderProperties()) {
				for (Folder folder : processor.getFolders()) {
					if (propsFolderDTO.getFolderType().equals(folder.getFldrType())) {

						switch (processor.getProcessorType()) {
							case FILEWRITER:
								handleFolderProperties(propsFolderDTO, folder);
								propsFolderDTO.setFolderDisplayType(MailBoxConstants.PROPERTY_FILEWRITE_DISPLAYTYPE);
								propsFolderDTO.setReadOnly(true);
								break;
							case SWEEPER:
								handleFolderProperties(propsFolderDTO, folder);
								propsFolderDTO.setFolderDisplayType(MailBoxConstants.PROPERTY_SWEEPER_DISPLAYTYPE);
								propsFolderDTO.setReadOnly(true);
								break;

							case REMOTEUPLOADER:
								handleFolderProperties(propsFolderDTO, folder);
								propsFolderDTO.setFolderDisplayType((propsFolderDTO.getFolderType().equalsIgnoreCase(
										FolderType.PAYLOAD_LOCATION.name()) ? MailBoxConstants.PROPERTY_LOCAL_PAYLOAD_LOCATION_DISPLAYTYPE : MailBoxConstants.PROPERTY_REMOTE_TARGET_LOCATION_DISPLAYTYPE));
								propsFolderDTO.setReadOnly(false);
								break;

							case REMOTEDOWNLOADER:
								handleFolderProperties(propsFolderDTO, folder);
								propsFolderDTO.setFolderDisplayType((propsFolderDTO.getFolderType().equalsIgnoreCase(
										FolderType.PAYLOAD_LOCATION.name()) ? MailBoxConstants.PROPERTY_REMOTE_PAYLOAD_LOCATION_DISPLAYTYPE : MailBoxConstants.PROPERTY_LOCAL_TARGET_LOCATION_DISPLAYTYPE));
								propsFolderDTO.setReadOnly(false);
								break;

							default:
								break;
						}
						break;

					}

				}

			}

		}
	}

	/**
	 * Method to construct ProcessorFolderPropertyDTO from Folder which is to be retrieve from db.
	 *
	 * @param property The ProcessorFolderPropertyDTO which is to be construct from Folder
	 * @param folder The Folder for construct ProcessorFolderPropertyDTO
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
	 * @param credential The credential Entity in DB from which the display Name has to be deduced
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
				credentialDisplayType = MailBoxConstants.PROPERTY_SSH_KEYPAIR_DISPLAY_TYPE;
				break;
		}
		return credentialDisplayType;

	}


	/**
	 * Method to construct credential Template from credential Entity
	 *
	 * @param credentialTemplate The template in Json for credentialProperties to be constructed from credential Entity
	 * @param credential The credential Entity in DB from which credentialTempalte has to be constructed
	 *
	 */
	private static void handleCredentialProperties(ProcessorCredentialPropertyDTO credentialPropertyTemplate,
			Credential credential) {

		String credentialDisplayType = getCredentialDisplayType(credential);
		credentialPropertyTemplate.setCredentialType(credential.getCredsType());
		credentialPropertyTemplate.setCredentialURI(MailBoxUtil.isEmpty(credential.getCredsUri()) ? credential.getCredsUri() : null);
		credentialPropertyTemplate.setIdpType(MailBoxUtil.isEmpty(credential.getCredsIdpType()) ? credential.getCredsIdpType() : null);
		credentialPropertyTemplate.setIdpURI(credential.getCredsIdpUri());
		credentialPropertyTemplate.setCredentialDisplayType(credentialDisplayType);
		credentialPropertyTemplate.setUserId(credential.getCredsUsername());
		credentialPropertyTemplate.setPassword(credential.getCredsPassword());
		credentialPropertyTemplate.setValueProvided(true);
	}

	/**
	 * Method to construct list of CredentialDTO from the CredentialDTOTemRplateList
	 *
	 * @param folderPropertiesDTO The ProcessorFolderPropertyDTO
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
	 * Method to construct Credential Template form the given Processor entity and set that in the
	 * propertiesJsonTemplate DTO
	 *
	 * @param processor The processor entity from which the credential Template has to be constructed
	 * @param propertiesJsonTemplate The processor properties JSON template in which the credentialTemplate needs to be
	 *            set
	 */
	public static void constructCredentialPropertiesTemplate(Processor processor,
			ProcessorPropertyUITemplateDTO propertiesJsonTemplate) {

		List<ProcessorCredentialPropertyDTO> processorCredentialProperties = new ArrayList<ProcessorCredentialPropertyDTO>();
		ProcessorCredentialPropertyDTO credentialPropertyTemplate = null;
		boolean isLoginCredentialsAvailable = false;
		if (null != processor.getCredentials()) {

			for (Credential credential : processor.getCredentials()) {

				if (credential.getCredsType().equalsIgnoreCase(MailBoxConstants.LOGIN_CREDENTIAL))
					isLoginCredentialsAvailable = true;

				// handle credential property to provide backward compatibility with already uploaded ssh keypairs
				// after upload provision removal only one entry will be available under type ssh_keypair
				// the legacy processor will have two entries one for private and other for public this has to converted to one entry
				if (credential.getCredsType().equalsIgnoreCase(MailBoxConstants.SSH_KEYPAIR)
						&& !MailBoxUtil.isEmpty(credential.getCredsIdpType())
						&& credential.getCredsIdpType().equals("PRIVATE")) {
					// if private just skip that
					continue;
				}
				credentialPropertyTemplate = new ProcessorCredentialPropertyDTO();
				handleCredentialProperties(credentialPropertyTemplate, credential);
				processorCredentialProperties.add(credentialPropertyTemplate);
			}
			// if Login Credential is not available then Login Credential from
			// template JSON has to be added
			if (!isLoginCredentialsAvailable)
				processorCredentialProperties.addAll(propertiesJsonTemplate.getCredentialProperties());
			if (processorCredentialProperties.size() > 0)
				propertiesJsonTemplate.setCredentialProperties(processorCredentialProperties);
		}
	}
}
