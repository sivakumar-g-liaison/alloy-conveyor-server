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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;

import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.introspect.JacksonAnnotationIntrospector;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;

import com.liaison.commons.acl.manifest.dto.ACLManifest;
import com.liaison.commons.acl.manifest.dto.Platform;
import com.liaison.commons.acl.manifest.dto.RoleBasedAccessControl;
import com.liaison.commons.acl.util.ACLUtil;
import com.liaison.commons.util.LiaisonGZipUtils;
import com.liaison.commons.util.UUIDGen;
import com.liaison.commons.util.client.sftp.StringUtil;
import com.liaison.commons.util.settings.DecryptableConfiguration;
import com.liaison.commons.util.settings.LiaisonConfigurationFactory;
import com.liaison.framework.util.ServiceUtils;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.service.dto.configuration.TenancyKeyDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.netflix.config.ConfigurationManager;

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
	private static final Object lock = new Object();
	private static final Properties properties = new Properties();
	private static final String ACL_BACKWARD_COMPATABILITY_PROPERTY = "mailbox.acl.manifest.backward.compatibility.mode";
	private static final String DUMMY_MANIFEST_USAGE_PROPERTY = "use.dummy.manifest.as.backup";
	private static final String DUMMY_MANIFEST_PROPERTY = "dummy.acl.manifest.json";

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

		return LiaisonConfigurationFactory.getConfiguration();
	}

	public static Properties getEnvProperties() throws IOException {

        synchronized (lock) {

            if (properties.isEmpty()) {
                Object env = ConfigurationManager.getDeploymentContext().getDeploymentEnvironment();
                String propertyFileName = "g2mailboxservice-" + env + ".properties";
                String props = ServiceUtils.readFileFromClassPath(propertyFileName);
                InputStream is = new ByteArrayInputStream(props.getBytes("UTF-8"));
                properties.load(is);
            }
            return properties;
        }
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
	 * Method to get all tenancy keys from acl manifest Json
	 *
	 * @param String - aclManifestJson
	 * @return list of tenancy keys
	 * @throws IOException
	 */
	public static List <TenancyKeyDTO>  getTenancyKeysFromACLManifest(String aclManifestJson) throws IOException {

		LOGGER.info("deserializing the acl manifest DTO from manifest json");
		String decodedManifestString = new String(LiaisonGZipUtils.uncompress(Base64.decodeBase64(aclManifestJson)));
		ACLManifest aclManifestDTO = ACLUtil.readACLManifest(decodedManifestString, false, false);
		LOGGER.info("acl Manifest DTO deserialized successfully");
		List<TenancyKeyDTO> tenancyKeys = new ArrayList<TenancyKeyDTO>();

		//retrieve the very first platform object from acl manifest json
		Platform platform = aclManifestDTO.getPlatform().get(0);

		// retrieve all domains present in platform
		List <RoleBasedAccessControl> roleBasedAccessControls = (platform != null)? platform.getRoleBasedAccessControl():null;
		LOGGER.info("Retrieving tenancy key from acl manifest");
		for (RoleBasedAccessControl rbac : roleBasedAccessControls) {
				
				TenancyKeyDTO tenancyKey = new TenancyKeyDTO();
				tenancyKey.setName(rbac.getDomainName());
				// if domainInternalName is not available then domainName will be used
				// only if acl manifest backward compatibility mode is on otherwise exception will be thrown.
				if (StringUtil.isNullOrEmptyAfterTrim(rbac.getDomainInternalName()) && 
						!Boolean.valueOf(MailBoxUtil.getEnvironmentProperties().getString(ACL_BACKWARD_COMPATABILITY_PROPERTY))) {
					throw new MailBoxServicesException(Messages.DOMAIN_INTERNAL_NAME_MISSING_IN_MANIFEST, Response.Status.CONFLICT);
				}  
				tenancyKey.setGuid(StringUtil.isNullOrEmptyAfterTrim(rbac.getDomainInternalName()) ? handleTenancyKeyForBackwardCompatilbility(rbac.getDomainName()) : rbac.getDomainInternalName());
				tenancyKeys.add(tenancyKey);
		}
		LOGGER.info("List of Tenancy keys retrieved are {}", tenancyKeys);
		return tenancyKeys;

	}

	public static List <String> getTenancyKeyGuidsFromTenancyKeys (List <TenancyKeyDTO> tenancyKeys) {

		List<String> tenancyKeyGuids = new ArrayList<String>();
		for (TenancyKeyDTO tenancyKey : tenancyKeys) {
			tenancyKeyGuids.add(tenancyKey.getGuid());
		}
		return tenancyKeyGuids;

	}

	/**
	 * Method to retrieve the dummy acl manifest json from properties file
	 *
	 * @param manifestJson
	 * @return
	 * @throws IOException
	 * @throws MailBoxConfigurationServicesException
	 */
	public static String getDummyManifestJson() throws IOException, MailBoxConfigurationServicesException {

		String dummyManifestJson = null;
		
		// check the value of property "use.dummy.manifest"
		// if it is true use dummy manifest else throw an error due to the
		// non-availability of manifest in header
		if (Boolean.valueOf(MailBoxUtil.getEnvironmentProperties().getString(DUMMY_MANIFEST_USAGE_PROPERTY))) {
	
			LOGGER.info("Retrieving the dummy acl manifest json from properties file");
			dummyManifestJson = MailBoxUtil.getEnvironmentProperties().getString(DUMMY_MANIFEST_PROPERTY);
			if (MailBoxUtil.isEmpty(dummyManifestJson)) {
				LOGGER.error("dummy acl manifest is not available in the properties file");
				throw new MailBoxConfigurationServicesException(Messages.ACL_MANIFEST_NOT_AVAILABLE, PROPERTIES_FILE, Response.Status.BAD_REQUEST);
			}
	
		} else {
			LOGGER.error("acl manifest is not available in the request header");
			throw new MailBoxConfigurationServicesException(Messages.ACL_MANIFEST_NOT_AVAILABLE, REQUEST_HEADER, Response.Status.BAD_REQUEST);
		}

		return dummyManifestJson;
	}
	
	/**
	 * This Method will truncate and trim the given tenancyKey
	 * 
	 * @param actualTenancyKey
	 * @return
	 * @throws IOException
	 */
	public static String handleTenancyKeyForBackwardCompatilbility(String tenancyKeyValue) throws IOException {
		
		String truncatedTenancyKey =  (Boolean.valueOf(MailBoxUtil.getEnvironmentProperties().getString(ACL_BACKWARD_COMPATABILITY_PROPERTY)) && 
				tenancyKeyValue.length() > MailBoxConstants.GUID_LENGTH) ? 
				tenancyKeyValue.substring(0, MailBoxConstants.GUID_LENGTH).trim():tenancyKeyValue.trim();
		return truncatedTenancyKey;
	}


}
