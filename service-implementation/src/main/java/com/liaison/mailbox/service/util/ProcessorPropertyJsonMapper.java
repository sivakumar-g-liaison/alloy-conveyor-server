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
import com.liaison.mailbox.dtdm.model.Processor;
import com.liaison.mailbox.dtdm.model.ProcessorProperty;
import com.liaison.mailbox.enums.ProcessorType;
import com.liaison.mailbox.enums.Protocol;
import com.liaison.mailbox.service.dto.configuration.processor.properties.ProcessorPropertiesDefinitionDTO;
import com.liaison.mailbox.service.dto.configuration.processor.properties.ProcessorPropertyDTO;
import com.liaison.mailbox.service.dto.configuration.request.RemoteProcessorPropertiesDTO;

public class ProcessorPropertyJsonMapper {
	
	private static final Logger LOGGER = LogManager.getLogger(ProcessorPropertyJsonMapper.class);
	
	public static final String FTP_DOWNLOADER_PROPERTIES_JSON = "processor/properties/httpdownloader.json";
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
	
	
	
	
	/**
	 * Method to retrieve specific processor property given name
	 * 
	 * @param processorDTO
	 * @param propertyName
	 * @return
	 */
	public static Map<String, String> getProcessorProperties(ProcessorPropertiesDefinitionDTO processorProperties, List<String> propertyNames) {
		
		String propertyValue = null;
		HashMap<String, String> retrievedProperties = new HashMap<String, String>();
		for (ProcessorPropertyDTO property : processorProperties.getStaticProperties()) {
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
	public static String getProcessorProperty(ProcessorPropertiesDefinitionDTO processorProperties, String propertyName) {
		
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
	
	public static ProcessorPropertiesDefinitionDTO retrieveProcessorProperties(String propertyJson, Processor processor) throws IOException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, JAXBException {
		
		ProcessorPropertiesDefinitionDTO propertiesDTO =  null;
		
		// In order to provide backward compatability for older processor entities
		// try to unmarshal the properties json with new class "ProcessorPropertiesDefinitionDTO"
		// if the unmarshalling fails then try to unmarshal it with old class "RemoteProcessorPropertiesDTO"
		try {
			if (!MailBoxUtil.isEmpty(propertyJson)) {

				propertiesDTO = MailBoxUtil.unmarshalFromJSON(propertyJson, ProcessorPropertiesDefinitionDTO.class);

			}
		} catch (JAXBException | JsonMappingException | JsonParseException e) {
			
			RemoteProcessorPropertiesDTO remoteProcessorPropertiesDTO = MailBoxUtil.unmarshalFromJSON(propertyJson, RemoteProcessorPropertiesDTO.class);
			Protocol protocol = Protocol.findByCode(processor.getProcsrProtocol());
			propertiesDTO = retrieveProcessorPropertiesDTO(remoteProcessorPropertiesDTO, processor.getProcessorType(), protocol);
			
			// hanlding dynamic properties of already added processors
			if (null != processor.getDynamicProperties()) {

				ProcessorPropertyDTO propertyDTO = null;
				for (ProcessorProperty property : processor.getDynamicProperties()) {
					propertyDTO = new ProcessorPropertyDTO();
					propertyDTO.setName(property.getProcsrPropName());
					propertyDTO.setDisplayName(property.getProcsrPropName());
					propertyDTO.setValue(property.getProcsrPropValue());
					propertyDTO.setDynamic(true);
					propertiesDTO.getStaticProperties().add(propertyDTO);
				}
			}			
		}
		return propertiesDTO;
	}
	
	/**
	 * Method to retrieve the property json based on given processor type and protocol
	 * after converting the older format of json to match with the newly defined format
	 * 
	 * @param remoteProcessorProperties
	 * @param processorType
	 * @param protocol
	 * @return
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws JAXBException
	 * @throws IOException
	 * @throws NoSuchFieldException
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public static ProcessorPropertiesDefinitionDTO retrieveProcessorPropertiesDTO(RemoteProcessorPropertiesDTO remoteProcessorProperties, ProcessorType processorType,Protocol protocol ) throws JsonParseException, JsonMappingException, JAXBException, IOException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		
		ProcessorPropertiesDefinitionDTO processorProperties = null;
		String propertiesJson = null;

		if (ProcessorType.REMOTEDOWNLOADER.equals(processorType)) {

			switch (protocol) {

			case FTPS:
				propertiesJson = ServiceUtils.readFileFromClassPath(FTPS_DOWNLOADER_PROPERTIES_JSON);
				processorProperties = MailBoxUtil.unmarshalFromJSON(propertiesJson, ProcessorPropertiesDefinitionDTO.class);
				processorProperties = buildPropertiesDTOByMappingValuesFromOlderFormat(processorProperties, remoteProcessorProperties);
				break;

			case FTP:
				propertiesJson = ServiceUtils.readFileFromClassPath(FTP_DOWNLOADER_PROPERTIES_JSON);
				processorProperties = MailBoxUtil.unmarshalFromJSON(propertiesJson, ProcessorPropertiesDefinitionDTO.class);
				processorProperties = buildPropertiesDTOByMappingValuesFromOlderFormat(processorProperties, remoteProcessorProperties);
				break;

			case SFTP:
				propertiesJson = ServiceUtils.readFileFromClassPath(SFTP_DOWNLOADER_PROPERTIES_JSON);
				processorProperties = MailBoxUtil.unmarshalFromJSON(propertiesJson, ProcessorPropertiesDefinitionDTO.class);
				processorProperties = buildPropertiesDTOByMappingValuesFromOlderFormat(processorProperties, remoteProcessorProperties);
				break;

			case HTTP:
				propertiesJson = ServiceUtils.readFileFromClassPath(HTTP_DOWNLOADER_PROPERTIES_JSON);
				processorProperties = MailBoxUtil.unmarshalFromJSON(propertiesJson, ProcessorPropertiesDefinitionDTO.class);
				processorProperties = buildPropertiesDTOByMappingValuesFromOlderFormat(processorProperties, remoteProcessorProperties);
				break;
			case HTTPS:
				propertiesJson = ServiceUtils.readFileFromClassPath(HTTP_DOWNLOADER_PROPERTIES_JSON);
				processorProperties = MailBoxUtil.unmarshalFromJSON(propertiesJson, ProcessorPropertiesDefinitionDTO.class);
				processorProperties = buildPropertiesDTOByMappingValuesFromOlderFormat(processorProperties, remoteProcessorProperties);
				break;
			default:
				break;

			}
		} else if (ProcessorType.REMOTEUPLOADER.equals(processorType)) {

			switch (protocol) {

			case FTPS:
				propertiesJson = ServiceUtils.readFileFromClassPath(FTPS_UPLOADER_PROPERTIES_JSON);
				processorProperties = MailBoxUtil.unmarshalFromJSON(propertiesJson, ProcessorPropertiesDefinitionDTO.class);
				processorProperties = buildPropertiesDTOByMappingValuesFromOlderFormat(processorProperties, remoteProcessorProperties);
				break;

			case FTP:
				propertiesJson = ServiceUtils.readFileFromClassPath(FTP_UPLOADER_PROPERTIES_JSON);
				processorProperties = MailBoxUtil.unmarshalFromJSON(propertiesJson, ProcessorPropertiesDefinitionDTO.class);
				processorProperties = buildPropertiesDTOByMappingValuesFromOlderFormat(processorProperties, remoteProcessorProperties);
				break;

			case SFTP:
				propertiesJson = ServiceUtils.readFileFromClassPath(SFTP_UPLOADER_PROPERTIES_JSON);
				processorProperties = MailBoxUtil.unmarshalFromJSON(propertiesJson, ProcessorPropertiesDefinitionDTO.class);
				processorProperties = buildPropertiesDTOByMappingValuesFromOlderFormat(processorProperties, remoteProcessorProperties);
				break;

			case HTTP:
				propertiesJson = ServiceUtils.readFileFromClassPath(HTTP_UPLOADER_PROPERTIES_JSON);
				processorProperties = MailBoxUtil.unmarshalFromJSON(propertiesJson, ProcessorPropertiesDefinitionDTO.class);
				processorProperties = buildPropertiesDTOByMappingValuesFromOlderFormat(processorProperties, remoteProcessorProperties);
				break;
				
			case HTTPS:
				propertiesJson = ServiceUtils.readFileFromClassPath(HTTP_UPLOADER_PROPERTIES_JSON);
				processorProperties = MailBoxUtil.unmarshalFromJSON(propertiesJson, ProcessorPropertiesDefinitionDTO.class);
				processorProperties = buildPropertiesDTOByMappingValuesFromOlderFormat(processorProperties, remoteProcessorProperties);
				break;
			default:
				break;

			}
		} else if (ProcessorType.SWEEPER.equals(processorType)) {
			
			propertiesJson = ServiceUtils.readFileFromClassPath(SWEEPER_PROPERTIES_JSON);
			processorProperties = MailBoxUtil.unmarshalFromJSON(propertiesJson, ProcessorPropertiesDefinitionDTO.class);
			processorProperties = buildPropertiesDTOByMappingValuesFromOlderFormat(processorProperties, remoteProcessorProperties);
			
		} else if (ProcessorType.DROPBOXPROCESSOR.equals(processorType)) {
			
			propertiesJson = ServiceUtils.readFileFromClassPath(DROPBOX_PROCESSOR_PROPERTIES_JSON);
			processorProperties = MailBoxUtil.unmarshalFromJSON(propertiesJson, ProcessorPropertiesDefinitionDTO.class);
			processorProperties = buildPropertiesDTOByMappingValuesFromOlderFormat(processorProperties, remoteProcessorProperties);
			
		}  else if (ProcessorType.HTTPASYNCPROCESSOR.equals(processorType) || ProcessorType.HTTPSYNCPROCESSOR.equals(processorType)) {
			
			propertiesJson = ServiceUtils.readFileFromClassPath(HTTP_LISTENER_PROPERTIES_JSON);
			processorProperties = MailBoxUtil.unmarshalFromJSON(propertiesJson, ProcessorPropertiesDefinitionDTO.class);
			processorProperties = buildPropertiesDTOByMappingValuesFromOlderFormat(processorProperties, remoteProcessorProperties);
			
		} else if (ProcessorType.FILEWRITER.equals(processorType)) {
			
			propertiesJson = ServiceUtils.readFileFromClassPath(FILE_WRITER_PROPERTIES_JSON);
			processorProperties = MailBoxUtil.unmarshalFromJSON(propertiesJson, ProcessorPropertiesDefinitionDTO.class);
			processorProperties = buildPropertiesDTOByMappingValuesFromOlderFormat(processorProperties, remoteProcessorProperties);
		}  

		return processorProperties;
		
	}
	
	/**
	 * Method to build new JSON format from the older format
	 * 
	 * @param newProcessorPropertiesDto
	 * @param oldProcessorPropertiesDto
	 * @return
	 * @throws NoSuchFieldException
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	private static ProcessorPropertiesDefinitionDTO buildPropertiesDTOByMappingValuesFromOlderFormat(ProcessorPropertiesDefinitionDTO newProcessorPropertiesDto, RemoteProcessorPropertiesDTO oldProcessorPropertiesDto) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		
		for (ProcessorPropertyDTO property : newProcessorPropertiesDto.getStaticProperties()) {
			
			Field field = oldProcessorPropertiesDto.getClass().getDeclaredField(property.getName());
			String propertyValue = null;		
			field.setAccessible(true);
			Object fieldValue = field.get(oldProcessorPropertiesDto);
			propertyValue = fieldValue.toString();
			property.setValue(propertyValue);
			
		}		
		return newProcessorPropertiesDto;		
	}

}
