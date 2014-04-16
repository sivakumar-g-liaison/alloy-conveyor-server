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
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.xml.bind.JAXBException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.introspect.JacksonAnnotationIntrospector;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liaison.commons.acl.manifest.dto.ACLManifest;
import com.liaison.commons.acl.manifest.dto.NestedServiceDependencyContraint;
import com.liaison.commons.acl.manifest.dto.Platform;
import com.liaison.commons.util.UUIDGen;
import com.liaison.commons.util.settings.DecryptableConfiguration;
import com.liaison.commons.util.settings.LiaisonConfigurationFactory;
import com.liaison.framework.util.ServiceUtils;

/**
 * Utilities for MailBox.
 * 
 * @author veerasamyn
 */
public class MailBoxUtility {

	private static final UUIDGen UUID = new UUIDGen();
	private static final Logger LOGGER = LoggerFactory.getLogger(MailBoxUtility.class);
	private static final Properties properties = new Properties();

	/**
	 * Utility is used to un-marshal from JSON String to Object.
	 * 
	 * @param serializedJson
	 *            The serialized JSON String.
	 * @param clazz
	 *            The corresponding class of the serialized JSON.
	 * @return Object The instance of the give Class.
	 * @throws JAXBException
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public static <T> T unmarshalFromJSON(String serializedJson, Class<T> clazz) throws JAXBException, JsonParseException,
			JsonMappingException, IOException {
		LOGGER.debug("Input JSON is {}", serializedJson);
		ObjectMapper mapper = new ObjectMapper();
		AnnotationIntrospector primary = new JaxbAnnotationIntrospector();
		AnnotationIntrospector secondary = new JacksonAnnotationIntrospector();
		AnnotationIntrospector introspector = new AnnotationIntrospector.Pair(primary, secondary);

		// make deserializer use JAXB annotations (only)
		mapper.getDeserializationConfig().withAnnotationIntrospector(introspector);
		// make serializer use JAXB annotations (only)
		mapper.getSerializationConfig().withAnnotationIntrospector(introspector);

		// added to support the root level element
		mapper.configure(DeserializationConfig.Feature.UNWRAP_ROOT_VALUE, true);

		T postManifest = mapper.readValue(serializedJson, clazz);

		return postManifest;

	}

	/**
	 * Utility is used to marshal the Object to JSON.
	 * 
	 * @param object
	 * @return
	 * @throws JAXBException
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public static String marshalToJSON(Object object) throws JAXBException, JsonGenerationException, JsonMappingException,
			IOException {

		ObjectMapper mapper = new ObjectMapper();
		AnnotationIntrospector primary = new JaxbAnnotationIntrospector();
		AnnotationIntrospector secondary = new JacksonAnnotationIntrospector();
		AnnotationIntrospector introspector = new AnnotationIntrospector.Pair(primary, secondary);
		// make deserializer use JAXB annotations (only)
		mapper.getDeserializationConfig().withAnnotationIntrospector(introspector);
		// make serializer use JAXB annotations (only)
		mapper.getSerializationConfig().withAnnotationIntrospector(introspector);

		// added to support root level element.
		mapper.configure(SerializationConfig.Feature.WRAP_ROOT_VALUE, true);
		String jsonBuilt = mapper.writeValueAsString(object);
		LOGGER.debug("JSON Built is {}", jsonBuilt);
		return jsonBuilt;
	}

	/**
	 * Method is used to get the unique id from UUIDGen Utility.
	 * 
	 * @return UUID The 32bit string.
	 */
	public static String getGUID() {
		return UUID.getUUID();
	}

	/**
	 * Checks the given string is empty or not.
	 * 
	 * @param str
	 *            The input String
	 * @return boolean
	 */
	public static boolean isEmpty(String str) {
		return str == null || str.isEmpty();
	}

	public static DecryptableConfiguration getEnvironmentProperties() throws IOException {
		
		DecryptableConfiguration configuration = LiaisonConfigurationFactory.getConfiguration();
		/*
		if (properties.isEmpty()) {

			Object env = ConfigurationManager.getDeploymentContext().getDeploymentEnvironment();
			String propertyFileName = "g2mailboxservice-" + env + ".properties";			
			String props = ServiceUtils.readFileFromClassPath(propertyFileName);
			InputStream is = new ByteArrayInputStream(props.getBytes("UTF-8"));
			properties.load(is);
		}*/

		return configuration;
	}
	
	/**
	 * Method to get the current timestmp to insert into database.
	 * 
	 * @return
	 */
	public static Timestamp getTimestamp() {
		
		Date d = new Date();
		return new Timestamp(d.getTime());
	}
	
	/**
	 * @param aclManifestDTO
	 * @return
	 */
	public static String getServiceInstanceIdFromACLManifest(ACLManifest aclManifestDTO) {
		
		Platform platform = aclManifestDTO.getPlatform().get(0);
		NestedServiceDependencyContraint nestedDependency = (platform != null)? platform.getNestedServiceDependencyContraint().get(0):null;
		String primaryServiceInstanceId = (nestedDependency != null)? nestedDependency.getPrimaryId():null;
		String secondaryServiceInstanceId = (nestedDependency != null)? nestedDependency.getNestedServiceId().get(0):null;
		if(primaryServiceInstanceId != null) return primaryServiceInstanceId;
		if(secondaryServiceInstanceId != null) return secondaryServiceInstanceId;
		return null;
		
	}
	
	/**
	 * @param aclManifestDTO
	 * @return
	 */
	public static String getPrimaryServiceInstanceIdFromACLManifest(ACLManifest aclManifestDTO) {
		
		Platform platform = aclManifestDTO.getPlatform().get(0);
		NestedServiceDependencyContraint nestedDependency = (platform != null)? platform.getNestedServiceDependencyContraint().get(0):null;
		String primaryServiceInstanceId = (nestedDependency != null)? nestedDependency.getPrimaryId():null;
		if(primaryServiceInstanceId != null) return primaryServiceInstanceId;
		return null;
	}
	
	/**
	 * @param aclManifestDTO
	 * @return
	 */
	public static List<String> getSecondaryServiceInstanceIdSFromACLManifest(ACLManifest aclManifestDTO) {
		
		List<String> secondayServiceInstanceIDs = new ArrayList<String>();
		Platform platform = aclManifestDTO.getPlatform().get(0);
		NestedServiceDependencyContraint nestedDependency = (platform != null)? platform.getNestedServiceDependencyContraint().get(0):null;
		if (nestedDependency != null) {
			List<String> nestedDependencies = nestedDependency.getNestedServiceId();
			for (String secondaryServiceInstance : nestedDependencies) {
				secondayServiceInstanceIDs.add(secondaryServiceInstance);
			}
		}
		return secondayServiceInstanceIDs;
		
	}

}
