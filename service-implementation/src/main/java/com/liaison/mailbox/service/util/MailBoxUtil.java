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
import java.util.Date;

import javax.xml.bind.JAXBException;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.introspect.JacksonAnnotationIntrospector;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;

import com.liaison.commons.util.UUIDGen;
import com.liaison.commons.util.settings.DecryptableConfiguration;
import com.liaison.commons.util.settings.LiaisonConfigurationFactory;
/**
 * Utilities for MailBox.
 * 
 * @author veerasamyn
 */
public class MailBoxUtil {

	private static final UUIDGen UUID = new UUIDGen();
	private static final Logger LOGGER = LogManager.getLogger(MailBoxUtil.class);
	private static final String REQUEST_HEADER = "Request Header";
	private static final String PROPERTIES_FILE = "Properties file";

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
	 * Method to get the dependency constraint corresponding to the service name configured in properties file
	 * 
	 * @param aclManifestDTO
	 * @return
	 * @throws IOException
	 */
	/*private static NestedServiceDependencyContraint  getDependencyConstraintFromACLManifest(ACLManifest aclManifestDTO) throws IOException {
		
		//retrieve the very first platform object from acl manifest json
		Platform platform = aclManifestDTO.getPlatform().get(0);
		
		// retrieve the dependency constraint having service name as per the configuration in properties file
		List <NestedServiceDependencyContraint> dependencyConstraints = (platform != null)? platform.getNestedServiceDependencyContraint():null;
		for (NestedServiceDependencyContraint dependencyConstraint : dependencyConstraints) {
			
			if (dependencyConstraint.getServiceName().equalsIgnoreCase(getEnvironmentProperties().getString("acl.constraint.service.name"))) {
				return dependencyConstraint;
			}
		}
		return null;

	}*/
	
	
	/**
	 * Method to retrieve the primary service instance id from the given acl manifest dto
	 * 
	 * @param  String aclManifestJson
	 * @return String primary Service instance id
	 * @throws IOException 
	 * @throws JAXBException 
	 */
	/*public static String getPrimaryServiceInstanceIdFromACLManifest(String aclManifestJson) throws IOException, JAXBException {
		
		// retrieve the service instance id from acl manifest
		LOGGER.info("deserializing the acl manifest DTO from manifest json");
		ACLManifest aclManifest = ACLUtil.readACLManifest(aclManifestJson,false);
		LOGGER.info("acl Manifest DTO deserialized successfully");
		NestedServiceDependencyContraint dependencyConstraint = getDependencyConstraintFromACLManifest(aclManifest);
		LOGGER.info("Retrieving the service instance id from acl Manifest DTO");
		String primaryServiceInstanceId  = (dependencyConstraint != null)?dependencyConstraint.getPrimaryId():null;
		return primaryServiceInstanceId;
	}*/
	
	/**
	 * Method to retrieve the list of secondary service instance ids from the given acl manifest dto
	 * 
	 * @param String aclManifestJson
	 * @return
	 * @throws IOException 
	 * @throws JAXBException 
	 */
	/*public static List<String> getSecondaryServiceInstanceIdSFromACLManifest(String aclManifestJson) throws IOException, JAXBException {
		
		// retrieve the service instance id from acl manifest
		LOGGER.info("deserializing the acl manifest DTO from manifest json");
		ACLManifest aclManifest = ACLUtil.readACLManifest(aclManifestJson,false);
		LOGGER.info("acl Manifest DTO deserialized successfully");
		NestedServiceDependencyContraint dependencyConstraint = getDependencyConstraintFromACLManifest(aclManifest);
		LOGGER.info("Retrieving the service instance id from acl Manifest DTO");
		List<String> secondayServiceInstanceIDs = (dependencyConstraint != null)?dependencyConstraint.getNestedServiceId():null;
		return secondayServiceInstanceIDs;
		
	}*/
	
	/**
	 * Method to retrieve the base64 decoded acl manifest json
	 * 
	 * @param manifestJson
	 * @return
	 * @throws IOException
	 * @throws MailBoxConfigurationServicesException
	 */
	/*public static String getDecodedManifestJson(String manifestJson) throws IOException, MailBoxConfigurationServicesException {
		
		String decodedManifestJson = null;
		
		// if manifest is available in the header then use acl-manifest in the header irrespective of
		// the property "use.dummy.manifest" configured in properties file
		if (!MailBoxUtil.isEmpty(manifestJson)) {
			LOGGER.info("acl manifest available in the header");
		} else {
			// check the value of property "use.dummy.manifest"
			// if it is true use dummy manifest else throw an error due to the 
			// non-availability of manifest in header
			if ((MailBoxUtil.getEnvironmentProperties().getString("use.dummy.manifest.as.backup")).equals("true")) {
				
				LOGGER.info("Retrieving the dummy acl manifest json from properties file");
				manifestJson = MailBoxUtil.getEnvironmentProperties().getString("dummy.acl.manifest.json");
				if (MailBoxUtil.isEmpty(manifestJson)) {
					LOGGER.error("dummy acl manifest is not available in the properties file");
					throw new MailBoxConfigurationServicesException(Messages.ACL_MANIFEST_NOT_AVAILABLE, PROPERTIES_FILE);
				}
	
			} else {
				LOGGER.error("acl manifest is not available in the request header");
				throw new MailBoxConfigurationServicesException(Messages.ACL_MANIFEST_NOT_AVAILABLE, REQUEST_HEADER);
			}
			
		}
	
		// decode the manifest using base64
		LOGGER.info("decoding the acl manifest");
		decodedManifestJson = new String(Base64.decodeBase64(manifestJson));
		LOGGER.info("acl manifest decoded successfully");
		return decodedManifestJson;
	}*/

}
