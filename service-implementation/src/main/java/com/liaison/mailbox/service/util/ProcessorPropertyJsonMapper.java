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
import com.liaison.mailbox.dtdm.model.Processor;
import com.liaison.mailbox.dtdm.model.ProcessorProperty;
import com.liaison.mailbox.enums.ProcessorType;
import com.liaison.mailbox.enums.Protocol;
import com.liaison.mailbox.service.dto.configuration.processor.properties.DropboxProcessorPropertiesDTO;
import com.liaison.mailbox.service.dto.configuration.processor.properties.FTPDownloaderPropertiesDTO;
import com.liaison.mailbox.service.dto.configuration.processor.properties.FTPUploaderPropertiesDTO;
import com.liaison.mailbox.service.dto.configuration.processor.properties.FileWriterPropertiesDTO;
import com.liaison.mailbox.service.dto.configuration.processor.properties.HTTPDownloaderPropertiesDTO;
import com.liaison.mailbox.service.dto.configuration.processor.properties.HTTPListenerPropertiesDTO;
import com.liaison.mailbox.service.dto.configuration.processor.properties.HTTPUploaderPropertiesDTO;
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

	public static final String FTP_DOWNLOADER_PROPERTIES_JSON = "processor/properties/ftpdownloader.json";
	public static final String FTP_UPLOADER_PROPERTIES_JSON = "processor/properties/ftpuploader.json";
	public static final String FTPS_DOWNLOADER_PROPERTIES_JSON = "processor/properties/ftpsdownloader.json";
	public static final String FTPS_UPLOADER_PROPERTIES_JSON = "processor/properties/ftpsuploader.json";
	public static final String SFTP_DOWNLOADER_PROPERTIES_JSON = "processor/properties/sftpdownloader.json";
	public static final String SFTP_UPLOADER_PROPERTIES_JSON = "processor/properties/sftpuploader.json";
	public static final String HTTP_DOWNLOADER_PROPERTIES_JSON = "processor/properties/httpdownloader.json";
	public static final String HTTP_UPLOADER_PROPERTIES_JSON = "processor/properties/httpdownloader.json";
	public static final String SWEEPER_PROPERTIES_JSON = "processor/properties/sweeper.json";
	public static final String FILE_WRITER_PROPERTIES_JSON = "processor/properties/fileWriter.json";
	public static final String HTTP_LISTENER_PROPERTIES_JSON = "processor/properties/httpsyncAndAsync.json";
	public static final String DROPBOX_PROCESSOR_PROPERTIES_JSON = "processor/properties/dropboxProcessor.json";
	public static final String PROP_HANDOVER_EXECUTION_TO_JS = "handOverExecutionToJavaScript";

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
	public static Map<String, String> getProcessorPropertiesAsMap(ProcessorPropertyUITemplateDTO uiPropertyTemplate, List<String> propertyNames) {

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
	 * @return
	 */
	public static String getProcessorPropertyByName(ProcessorPropertyUITemplateDTO processorProperties, String propertyName) {

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
	 * Method to retrieve staticproperties in format stored in Template JSON (ie ProcessorPropertyUITemplateDTO) from the properties JSON stored in DB
	 * This method will convert the json to ProcessorPropertyUITemplateDTO format even if it is in older format (RemoteProcessorPropertiesDTO)
	 *
	 * @param propertyJson
	 * @param processor
	 * @return
	 * @throws IOException
	 * @throws NoSuchFieldException
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws JAXBException
	 */
	public static ProcessorPropertyUITemplateDTO getHydratedUIPropertyTemplate(String propertyJson, Processor processor) throws IOException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, JAXBException {

		Protocol protocol = Protocol.findByCode(processor.getProcsrProtocol());
		ProcessorPropertyUITemplateDTO uiPropTemplate =  getTemplate(processor.getProcessorType(), protocol);
		if (!MailBoxUtil.isEmpty(propertyJson)) {

		// In order to provide backward compatibility for older processor entities
		// try to unmarshal the properties json with new class "ProcessorPropertyUITemplateDTO"
		// if the unmarshalling fails then try to unmarshal it with old class "RemoteProcessorPropertiesDTO"
		try {
				StaticProcessorPropertiesDTO staticProperties = getProcessorBasedStaticProps(propertyJson, processor.getProcessorType(), protocol);
				uiPropTemplate.setHandOverExecutionToJavaScript(staticProperties.isHandOverExecutionToJavaScript());
				hydrateTemplate(staticProperties, uiPropTemplate);

			}catch (JAXBException | JsonMappingException | JsonParseException e) {

			RemoteProcessorPropertiesDTO legacyProps = MailBoxUtil.unmarshalFromJSON(propertyJson, RemoteProcessorPropertiesDTO.class);
			mapLegacyPropsToTemplate(uiPropTemplate, legacyProps);

		   }
		}
		// handle dynamic properites also
		setDynamicPropsinTemplate(processor, uiPropTemplate);
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
	 public static StaticProcessorPropertiesDTO getProcessorBasedStaticPropsFromJson(String propertyJson, Processor processor) throws IOException, JAXBException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {

		StaticProcessorPropertiesDTO staticProcessorProperties = null;
		Protocol protocol = Protocol.findByCode(processor.getProcsrProtocol());
		try {
			staticProcessorProperties = getProcessorBasedStaticProps(propertyJson, processor.getProcessorType(), protocol);

		} catch (JAXBException | JsonMappingException | JsonParseException e) {

			RemoteProcessorPropertiesDTO leagcyProps = MailBoxUtil.unmarshalFromJSON(propertyJson, RemoteProcessorPropertiesDTO.class);
			staticProcessorProperties = getProcessorBasedStaticPropsFrmLegacyProps(leagcyProps, processor.getProcessorType(), protocol);
			handleDynamicProperties(staticProcessorProperties, processor);
		}
		return staticProcessorProperties;

	}

	private static StaticProcessorPropertiesDTO getProcessorBasedStaticProps(String propertyJson, ProcessorType processorType, Protocol protocol) throws JsonParseException, JsonMappingException, JAXBException, IOException {

		 switch(processorType) {

		 case REMOTEDOWNLOADER:
			switch (protocol) {

			case FTPS:
				FTPDownloaderPropertiesDTO ftpsDownloaderPropertiesDTOInDB = MailBoxUtil.unmarshalFromJSON(propertyJson, FTPDownloaderPropertiesDTO.class);
				return ftpsDownloaderPropertiesDTOInDB;
			case FTP:
				FTPDownloaderPropertiesDTO ftpDownloaderPropertiesDTOInDB = MailBoxUtil.unmarshalFromJSON(propertyJson, FTPDownloaderPropertiesDTO.class);
				return ftpDownloaderPropertiesDTOInDB;
			case SFTP:
				SFTPDownloaderPropertiesDTO sftpDownloaderPropertiesDTOInDB = MailBoxUtil.unmarshalFromJSON(propertyJson, SFTPDownloaderPropertiesDTO.class);
				return sftpDownloaderPropertiesDTOInDB;
			case HTTP:
				HTTPDownloaderPropertiesDTO httpDownloaderPropertiesDTOInDB = MailBoxUtil.unmarshalFromJSON(propertyJson, HTTPDownloaderPropertiesDTO.class);
				return httpDownloaderPropertiesDTOInDB;
			case HTTPS:
				HTTPDownloaderPropertiesDTO httpsDownloaderPropertiesDTOInDB = MailBoxUtil.unmarshalFromJSON(propertyJson, HTTPDownloaderPropertiesDTO.class);
				return httpsDownloaderPropertiesDTOInDB;
			default:
				break;

			}
			break;
		 case REMOTEUPLOADER:
			switch (protocol) {

			case FTPS:
				FTPUploaderPropertiesDTO ftpsUploaderPropertiesDTOInDB = MailBoxUtil.unmarshalFromJSON(propertyJson, FTPUploaderPropertiesDTO.class);
				return ftpsUploaderPropertiesDTOInDB;
			case FTP:
				FTPUploaderPropertiesDTO ftpUploaderPropertiesDTOInDB = MailBoxUtil.unmarshalFromJSON(propertyJson, FTPUploaderPropertiesDTO.class);
				return ftpUploaderPropertiesDTOInDB;
			case SFTP:
				SFTPUploaderPropertiesDTO sftpUploaderPropertiesDTOInDB = MailBoxUtil.unmarshalFromJSON(propertyJson, SFTPUploaderPropertiesDTO.class);
				return sftpUploaderPropertiesDTOInDB;
			case HTTP:
				HTTPUploaderPropertiesDTO httpUploaderPropertiesDTOInDB = MailBoxUtil.unmarshalFromJSON(propertyJson, HTTPUploaderPropertiesDTO.class);
				return httpUploaderPropertiesDTOInDB;
			case HTTPS:
				HTTPUploaderPropertiesDTO httpsUploaderPropertiesDTOInDB = MailBoxUtil.unmarshalFromJSON(propertyJson, HTTPUploaderPropertiesDTO.class);
				return httpsUploaderPropertiesDTOInDB;
			default:
				break;

			}
			break;
		 case SWEEPER:
			SweeperPropertiesDTO sweeperPropertiesDTOInDB = MailBoxUtil.unmarshalFromJSON(propertyJson, SweeperPropertiesDTO.class);
			return sweeperPropertiesDTOInDB;

		 case DROPBOXPROCESSOR:
			DropboxProcessorPropertiesDTO dropboxProcessorPropertiesDTOInDB = MailBoxUtil.unmarshalFromJSON(propertyJson, DropboxProcessorPropertiesDTO.class);
			return dropboxProcessorPropertiesDTOInDB;

		 case HTTPASYNCPROCESSOR:
			HTTPListenerPropertiesDTO httpAsyncProcessorPropertiesDTOInDB = MailBoxUtil.unmarshalFromJSON(propertyJson, HTTPListenerPropertiesDTO.class);
			return httpAsyncProcessorPropertiesDTOInDB;

		 case HTTPSYNCPROCESSOR:
			HTTPListenerPropertiesDTO httpSyncProcessorPropertiesDTOInDB = MailBoxUtil.unmarshalFromJSON(propertyJson, HTTPListenerPropertiesDTO.class);
			return httpSyncProcessorPropertiesDTOInDB;

		 case FILEWRITER :
			FileWriterPropertiesDTO fileWriterPropertiesDTOInDB = MailBoxUtil.unmarshalFromJSON(propertyJson, FileWriterPropertiesDTO.class);
			return fileWriterPropertiesDTOInDB;
		 }
		return null;
	}



	public static ProcessorPropertyUITemplateDTO getTemplate(ProcessorType processorType, Protocol protocol) throws JsonParseException, JsonMappingException, JAXBException, IOException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {

		ProcessorPropertyUITemplateDTO processorProperties = null;
		String propertiesJson = null;

		switch(processorType) {
			case REMOTEDOWNLOADER:
				switch (protocol) {

				case FTPS:
					propertiesJson = ServiceUtils.readFileFromClassPath(FTPS_DOWNLOADER_PROPERTIES_JSON);
					processorProperties = MailBoxUtil.unmarshalFromJSON(propertiesJson, ProcessorPropertyUITemplateDTO.class);
					break;

				case FTP:
					propertiesJson = ServiceUtils.readFileFromClassPath(FTP_DOWNLOADER_PROPERTIES_JSON);
					processorProperties = MailBoxUtil.unmarshalFromJSON(propertiesJson, ProcessorPropertyUITemplateDTO.class);
					break;

				case SFTP:
					propertiesJson = ServiceUtils.readFileFromClassPath(SFTP_DOWNLOADER_PROPERTIES_JSON);
					processorProperties = MailBoxUtil.unmarshalFromJSON(propertiesJson, ProcessorPropertyUITemplateDTO.class);
					break;

				case HTTP:
					propertiesJson = ServiceUtils.readFileFromClassPath(HTTP_DOWNLOADER_PROPERTIES_JSON);
					processorProperties = MailBoxUtil.unmarshalFromJSON(propertiesJson, ProcessorPropertyUITemplateDTO.class);
					break;
				case HTTPS:
					propertiesJson = ServiceUtils.readFileFromClassPath(HTTP_DOWNLOADER_PROPERTIES_JSON);
					processorProperties = MailBoxUtil.unmarshalFromJSON(propertiesJson, ProcessorPropertyUITemplateDTO.class);
					break;
				default:
					break;

				}
				break;
			case REMOTEUPLOADER:

				switch (protocol) {

				case FTPS:
					propertiesJson = ServiceUtils.readFileFromClassPath(FTPS_UPLOADER_PROPERTIES_JSON);
					processorProperties = MailBoxUtil.unmarshalFromJSON(propertiesJson, ProcessorPropertyUITemplateDTO.class);
					break;
				case FTP:
					propertiesJson = ServiceUtils.readFileFromClassPath(FTP_UPLOADER_PROPERTIES_JSON);
					processorProperties = MailBoxUtil.unmarshalFromJSON(propertiesJson, ProcessorPropertyUITemplateDTO.class);
					break;
				case SFTP:
					propertiesJson = ServiceUtils.readFileFromClassPath(SFTP_UPLOADER_PROPERTIES_JSON);
					processorProperties = MailBoxUtil.unmarshalFromJSON(propertiesJson, ProcessorPropertyUITemplateDTO.class);
					break;
				case HTTP:
					propertiesJson = ServiceUtils.readFileFromClassPath(HTTP_UPLOADER_PROPERTIES_JSON);
					processorProperties = MailBoxUtil.unmarshalFromJSON(propertiesJson, ProcessorPropertyUITemplateDTO.class);
					break;
				case HTTPS:
					propertiesJson = ServiceUtils.readFileFromClassPath(HTTP_UPLOADER_PROPERTIES_JSON);
					processorProperties = MailBoxUtil.unmarshalFromJSON(propertiesJson, ProcessorPropertyUITemplateDTO.class);
					break;
				default:
					break;

				}
				break;
			case SWEEPER:
				propertiesJson = ServiceUtils.readFileFromClassPath(SWEEPER_PROPERTIES_JSON);
				processorProperties = MailBoxUtil.unmarshalFromJSON(propertiesJson, ProcessorPropertyUITemplateDTO.class);
				break;
			case DROPBOXPROCESSOR:
				propertiesJson = ServiceUtils.readFileFromClassPath(DROPBOX_PROCESSOR_PROPERTIES_JSON);
				processorProperties = MailBoxUtil.unmarshalFromJSON(propertiesJson, ProcessorPropertyUITemplateDTO.class);
				break;
			case HTTPSYNCPROCESSOR:
				propertiesJson = ServiceUtils.readFileFromClassPath(HTTP_LISTENER_PROPERTIES_JSON);
				processorProperties = MailBoxUtil.unmarshalFromJSON(propertiesJson, ProcessorPropertyUITemplateDTO.class);
				break;
			case HTTPASYNCPROCESSOR:
				propertiesJson = ServiceUtils.readFileFromClassPath(HTTP_LISTENER_PROPERTIES_JSON);
				processorProperties = MailBoxUtil.unmarshalFromJSON(propertiesJson, ProcessorPropertyUITemplateDTO.class);
				break;
			case FILEWRITER:
				propertiesJson = ServiceUtils.readFileFromClassPath(FILE_WRITER_PROPERTIES_JSON);
				processorProperties = MailBoxUtil.unmarshalFromJSON(propertiesJson, ProcessorPropertyUITemplateDTO.class);
				break;
		}

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
	private static ProcessorPropertyUITemplateDTO mapLegacyPropsToTemplate(ProcessorPropertyUITemplateDTO uiPropertyTemplate, RemoteProcessorPropertiesDTO legacyProps) throws SecurityException, IllegalArgumentException, IllegalAccessException {

		for (ProcessorPropertyDTO property : uiPropertyTemplate.getStaticProperties()) {

			if (property.getName().equals(MailBoxConstants.ADD_NEW_PROPERTY)) {
				continue;
			}
			Field field;
			try {
				field = legacyProps.getClass().getDeclaredField(property.getName());
			} catch (NoSuchFieldException e) {
				LOGGER.debug("The field {} is not available in legacy prop.so continuing..",property.getName());
				continue;
			}
			String propertyValue = null;
			field.setAccessible(true);
			Object fieldValue = field.get(legacyProps);
			if (property.getName().equals(MailBoxConstants.PROPERTY_OTHER_REQUEST_HEADERS)) {
				List <HttpOtherRequestHeaderDTO> otherRequestHeaders = (List<HttpOtherRequestHeaderDTO>)fieldValue;
				propertyValue = handleOtherRequestHeaders(otherRequestHeaders);
			} else {
				propertyValue = fieldValue.toString();
			}
			property.setValue(propertyValue);

		}

		// handle handoverExecutionToJavascript from old processorproperties JSON
		Field field;
		try {
			field = legacyProps.getClass().getDeclaredField(PROP_HANDOVER_EXECUTION_TO_JS);
		} catch (NoSuchFieldException e) {
			LOGGER.debug("The field {} is not available in legacy prop.so continuing..",PROP_HANDOVER_EXECUTION_TO_JS);
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

		// hanlding dynamic properties of  processors
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
	public static void separateStaticAndDynamicProperties(List<ProcessorPropertyDTO> staticProperties, List<ProcessorPropertyDTO> dynamicProperties) {

		for (ProcessorPropertyDTO properties :staticProperties) {
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
	public static StaticProcessorPropertiesDTO getProcessorSpecificStaticPropsFrmTemplt(List<ProcessorPropertyDTO> staticProperties, ProcessorType processorType, Protocol protocol ) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {

		StaticProcessorPropertiesDTO propertiesDTO = null;

		 switch(processorType) {


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
	private static StaticProcessorPropertiesDTO getProcessorSpecificStaticProps(List<ProcessorPropertyDTO> staticPropertiesInTemplateJson, StaticProcessorPropertiesDTO staticPropertiesDTO) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {

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
			} else if (field.getType().equals(String.class)){
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
	private static void hydrateTemplate (StaticProcessorPropertiesDTO staticPropertiesDTO, ProcessorPropertyUITemplateDTO processorPropertiesDefinitionDto) throws IllegalArgumentException, IllegalAccessException, JsonParseException, JsonMappingException, NoSuchFieldException, SecurityException, JAXBException, IOException {

		for (ProcessorPropertyDTO staticProperty : processorPropertiesDefinitionDto.getStaticProperties()) {

			if (staticProperty.getName().equals(MailBoxConstants.ADD_NEW_PROPERTY)) {
				continue;
			}

			Field field = staticPropertiesDTO.getClass().getDeclaredField(staticProperty.getName());
			field.setAccessible(true);
			Object fieldValue = field.get(staticPropertiesDTO);
			String propertyValue = fieldValue.toString();

			if (staticProperty.getName().equals(MailBoxConstants.PORT_PROPERTY) && !MailBoxUtil.isEmpty((Integer.parseInt(propertyValue) == 0)? "":propertyValue)) {
				staticProperty.setReadOnly(true);
			}
			boolean isValueAvailable = !(MailBoxUtil.isEmpty(propertyValue));
			if (field.getType().equals(Boolean.TYPE)) {
				isValueAvailable =(Boolean.valueOf(propertyValue).equals(true)?true:false);
			}
			else if(field.getType().equals(Integer.TYPE)) {
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

	private static StaticProcessorPropertiesDTO getProcessorBasedStaticPropsFrmLegacyProps(RemoteProcessorPropertiesDTO remoteProcessorProperties, ProcessorType processorType, Protocol protocol) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {

		StaticProcessorPropertiesDTO propertiesDTO = null;

		 switch(processorType) {

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

		 case FILEWRITER :
			propertiesDTO = new FileWriterPropertiesDTO();
			break;
		 }
		return propertiesDTO;

	}

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

	private static String handleOtherRequestHeaders(List <HttpOtherRequestHeaderDTO> otherRequestHeaders) {
		StringBuilder otherRequestHeader = new StringBuilder();
		for (HttpOtherRequestHeaderDTO header : otherRequestHeaders) {
			otherRequestHeader.append(header.getName()).append(":").append(header.getValue()).append(",");
		}
		String otherRequestHeaderStr = otherRequestHeader.toString();
		return otherRequestHeaderStr.substring(0, otherRequestHeaderStr.length() - 2);
	}

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

}
