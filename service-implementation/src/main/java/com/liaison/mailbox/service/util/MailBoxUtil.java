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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;

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

import com.liaison.commons.acl.manifest.dto.RoleBasedAccessControl;
import com.liaison.commons.util.UUIDGen;
import com.liaison.commons.util.client.sftp.StringUtil;
import com.liaison.commons.util.settings.DecryptableConfiguration;
import com.liaison.commons.util.settings.LiaisonConfigurationFactory;
import com.liaison.gem.service.client.GEMACLClient;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.dtdm.model.Processor;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.enums.Protocol;
import com.liaison.mailbox.service.dto.configuration.TenancyKeyDTO;
import com.liaison.mailbox.service.dto.configuration.request.RemoteProcessorPropertiesDTO;
import com.liaison.mailbox.service.exception.MailBoxServicesException;

/**
 * Utilities for MailBox.
 *
 * @author veerasamyn
 */
public class MailBoxUtil {

	private static final Logger LOGGER = LogManager.getLogger(MailBoxUtil.class);
	private static final DecryptableConfiguration CONFIGURATION = LiaisonConfigurationFactory.getConfiguration();

	private static String propDataRetentionTTL = "fs2.storage.spectrum.%sdataRetentionTTL";

	// for logging dropbox related details.
	public static final String seperator = ": ";
	
	private static GEMACLClient gemClient = new GEMACLClient();

	/**
	 * Utility is used to un-marshal from JSON String to Object.
	 *
	 * @param serializedJson The serialized JSON String.
	 * @param clazz The corresponding class of the serialized JSON.
	 * @return Object The instance of the give Class.
	 * @throws JAXBException
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public static <T> T unmarshalFromJSON(String serializedJson, Class<T> clazz) throws IOException {

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
	public static String marshalToJSON(Object object) throws IOException {

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
		return UUIDGen.getCustomUUID();
	}

	/**
	 * Checks the given string is empty or not.
	 *
	 * @param str The input String
	 * @return boolean
	 */
	public static boolean isEmpty(String str) {
		return str == null || str.trim().isEmpty();
	}

	public static DecryptableConfiguration getEnvironmentProperties() {
		return CONFIGURATION;
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
	public static List<TenancyKeyDTO> getTenancyKeysFromACLManifest(String aclManifestJson)
			throws IOException {

		List<TenancyKeyDTO> tenancyKeys = new ArrayList<TenancyKeyDTO>();

		List<RoleBasedAccessControl> roleBasedAccessControls = gemClient.getDomainsFromACLManifest(aclManifestJson);
		TenancyKeyDTO tenancyKey = null;
		for (RoleBasedAccessControl rbac : roleBasedAccessControls) {

			tenancyKey = new TenancyKeyDTO();
			tenancyKey.setName(rbac.getDomainName());
			// if domainInternalName is not available then exception will be thrown.
			if (StringUtil.isNullOrEmptyAfterTrim(rbac.getDomainInternalName())) {
				throw new MailBoxServicesException(Messages.DOMAIN_INTERNAL_NAME_MISSING_IN_MANIFEST,
						Response.Status.CONFLICT);
			} else {
				tenancyKey.setGuid(rbac.getDomainInternalName());
			}
			tenancyKeys.add(tenancyKey);
		}

		LOGGER.info("List of Tenancy keys retrieved are {}", tenancyKeys);
		return tenancyKeys;
	}

	public static List<String> getTenancyKeyGuids(String aclManifestJson)
			throws IOException {

		List<String> tenancyKeyGuids = new ArrayList<String>();
		List<RoleBasedAccessControl> roleBasedAccessControls = gemClient.getDomainsFromACLManifest(aclManifestJson);

		for (RoleBasedAccessControl rbac : roleBasedAccessControls) {
			tenancyKeyGuids.add(rbac.getDomainInternalName());
		}
		return tenancyKeyGuids;

	}

	/**
	 * This Method will retrieve the TenancyKey Name from the given guid
	 *
	 * @param tenancyKeyGuid
	 * @param tenancyKeys
	 * @return
	 * @throws IOException
	 */
	public static String getTenancyKeyNameByGuid(String aclManifestJson, String tenancyKeyGuid)
			throws IOException {

		String tenancyKeyDisplayName = null;
		List<RoleBasedAccessControl> roleBasedAccessControls = gemClient.getDomainsFromACLManifest(aclManifestJson);

		for (RoleBasedAccessControl rbac : roleBasedAccessControls) {

			if (rbac.getDomainInternalName().equals(tenancyKeyGuid)) {
				tenancyKeyDisplayName = rbac.getDomainName();
				break;
			}
		}

		return tenancyKeyDisplayName;
	}

	/**
	 * Method to calculate the elapsed time between two given time limits
	 *
	 * @param startTime
	 * @param endTime
	 * @param taskToCalulateElapsedTime
	 */
	public static void calculateElapsedTime(long startTime, long endTime) {

		LOGGER.debug("start time - {}", startTime);
		LOGGER.debug("end time - {}", endTime);
		Long elapsedTime = endTime - startTime;
		LOGGER.debug("elapsed time is {}", elapsedTime);

	}

	/**
	 * Method to get pagingOffsetDetails
	 *
	 * @param page
	 * @param pageSize
	 * @param totalCount
	 * @return Map
	 */
	public static Map<String, Integer> getPagingOffsetDetails(String page, String pageSize, int totalCount) {

		Map<String, Integer> pageParameters = new HashMap<String, Integer>();
		// Calculate page size parameters
		Integer pageValue = 1;
		Integer pageSizeValue = 10;
		if (page != null && !page.isEmpty()) {
			pageValue = Integer.parseInt(page);
			if (pageValue < 0) {
				pageValue = 1;
			}
		} else {
			pageValue = 1;
		}
		if (pageSize != null && !pageSize.isEmpty()) {
			pageSizeValue = Integer.parseInt(pageSize);
			if (pageSizeValue < 0) {
				pageSizeValue = 10;
			}
		} else {
			pageSizeValue = 100;
		}

		Integer fromIndex = (pageValue - 1) * pageSizeValue;
		pageParameters.put(MailBoxConstants.PAGING_OFFSET, fromIndex);

		int toIndex = fromIndex + pageSizeValue;
		if (toIndex > totalCount) {
			toIndex = (totalCount - fromIndex);
		} else {
			toIndex = pageSizeValue;
		}
		pageParameters.put(MailBoxConstants.PAGING_COUNT, toIndex);

		return pageParameters;
	}

	/**
	 * Method to get the data retention value from the properties
	 *
	 * @param identifier
	 * @return
	 */
	public static Integer getDataRetentionTTL(String identifier) {
	    String ttl = String.format(propDataRetentionTTL, identifier != null ? identifier + "." : "");
	    return getEnvironmentProperties().getInteger(ttl, 2592000);
	}


	/**
	 * Method to convertTTLIntoSeconds
	 *
	 * @param ttlUnit
	 * @param ttlNumber
	 * @return Integer
	 */
	public static Integer convertTTLIntoSeconds(String ttlUnit, Integer ttlNumber) {

		if (ttlUnit.equals(MailBoxConstants.TTL_UNIT_YEARS)) {
			return ttlNumber * 365 * 24 * 60 * 60;
		} else if (ttlUnit.equals(MailBoxConstants.TTL_UNIT_MONTHS)) {
			return ttlNumber * 30 * 24 * 60 * 60;
		} else if (ttlUnit.equals(MailBoxConstants.TTL_UNIT_WEEKS)) {
			return ttlNumber * 7 * 24 * 60 * 60;
		} else if (ttlUnit.equals(MailBoxConstants.TTL_UNIT_DAYS)) {
			return ttlNumber * 24 * 60 * 60;
		} else if (ttlUnit.equals(MailBoxConstants.TTL_UNIT_HOURS)) {
			return ttlNumber * 60 * 60;
		} else if (ttlUnit.equals(MailBoxConstants.TTL_UNIT_MINUTES)) {
			return ttlNumber * 60;
		} else {
			return ttlNumber;
		}
	}


	/**
	* Converts the given work ticket lifetime (TTL) in days - Round up to next integer.
	* Example: 0.1 rounded off to 1
	*
	* @param ttlUnit specifies the type of TTL value  which can be Year,Month,Week,Day,Hours,Minutes.
	* @param ttlNumber specifies the TTL value in days.
	* @return Integer
	*/
	public static Integer convertTTLIntoDays(String ttlUnit, Integer ttlNumber) {
		// decimal value is used with operators in order to round up to next no
		if (ttlUnit.equals(MailBoxConstants.TTL_UNIT_YEARS)) {
			return (int) Math.ceil(ttlNumber * 365.0);
		} else if (ttlUnit.equals(MailBoxConstants.TTL_UNIT_MONTHS)) {
			return (int) Math.ceil(ttlNumber * 30.0);
		} else if (ttlUnit.equals(MailBoxConstants.TTL_UNIT_WEEKS)) {
			return (int) Math.ceil(ttlNumber * 7.0);
		} else if (ttlUnit.equals(MailBoxConstants.TTL_UNIT_DAYS)) {
			return ttlNumber;
		} else if (ttlUnit.equals(MailBoxConstants.TTL_UNIT_HOURS)) {
			return (int) Math.ceil(ttlNumber / 24.0);
		} else if (ttlUnit.equals(MailBoxConstants.TTL_UNIT_MINUTES)) {
			return  (int) Math.ceil(ttlNumber / (24.0 * 60.0));
		} else {
			return (int) Math.ceil(ttlNumber / (24.0 * 60.0 * 60.0));
		}
	}

	/**
	 * Method to add given TimeToLive value in seconds to the CurrentTime
	 *
	 * @param seconds
	 *
	 * @return Timestamp
	 */
	public static Timestamp addTTLToCurrentTime(int seconds) {

		Timestamp currentTimeStamp = new Timestamp(System.currentTimeMillis());
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(currentTimeStamp.getTime());
		cal.add(Calendar.SECOND, seconds);
		return new Timestamp(cal.getTime().getTime());
	}

    /**
     * Method to construct log messages for easy visibility
     *
     * @param messages append to prefix, please make sure the order of the inputs
     * @return constructed string
     */
    public static String constructMessage(Processor processor, String transferProfile, String... messages) {

        StringBuilder logPrefix = null;
    	 if (null == processor) {

    		 logPrefix = new StringBuilder()
                 .append("DROPBOX")
                 .append(seperator);
         } else {

        	 logPrefix = new StringBuilder()
            .append("DROPBOX")
            .append(seperator)
            .append((transferProfile == null ? "NONE" : transferProfile))
            .append(seperator)
            .append(processor.getProcessorType())
            .append(seperator)
            .append(processor.getProcsrName())
            .append(seperator)
            .append(processor.getMailbox().getMbxName())
            .append(seperator)
            .append(processor.getMailbox().getPguid())
            .append(seperator);
        }

    	 StringBuilder msgBuf = new StringBuilder().append(logPrefix);
        for (String str : messages) {
            msgBuf.append(str);
        }

        return msgBuf.toString();
    }

    /**
     * Checks whether a file is modified with in the given time limit
     *
     * @param timelimit
     *            to check the file is modified with in the given time limit
     * @param file
     *            File object
     * @return true if it is updated with in the given time limit, false otherwise
     */
    public static boolean validateLastModifiedTolerance(Path file) {

        long timelimit = CONFIGURATION.getLong(MailBoxConstants.LAST_MODIFIED_TOLERANCE);

        long system = System.currentTimeMillis();
        long lastmo = file.toFile().lastModified();

        LOGGER.debug("System time millis: {}, Last Modified {}, timelimit: {}", system, lastmo, timelimit);
        LOGGER.debug("(system - lastmo)/1000) = {}", ((system - lastmo)/1000));

        if (((system - lastmo)/1000) < timelimit) {
            return true;
        }
        return false;
    }
    
    /**
     * Method to construct the valid URL
     * 
     * @param propertiesDTO
     * @throws URISyntaxException
     * @throws MalformedURLException
     */
    public static void constructURLAndPort(RemoteProcessorPropertiesDTO propertiesDTO) throws URISyntaxException, MalformedURLException {
        
        URI uri = new URI(propertiesDTO.getUrl());
        String scheme = uri.getScheme();
        if (propertiesDTO.getPort() == 0 && (uri.getPort() == -1 || uri.getPort() == 0)) {
            
            if (Protocol.FTP.getCode().equalsIgnoreCase(scheme) || Protocol.FTPS.getCode().toString().equalsIgnoreCase(scheme)) {
                propertiesDTO.setPort(MailBoxConstants.FTPS_PORT);
                propertiesDTO.setUrl((new URI(scheme, null, uri.getHost(), MailBoxConstants.FTPS_PORT, uri.getPath() == null ? "" : uri.getPath(), null, null).toString()));
            } else if (Protocol.SFTP.getCode().toString().equalsIgnoreCase(scheme)) {
                propertiesDTO.setPort(MailBoxConstants.SFTP_PORT);
                propertiesDTO.setUrl((new URI(scheme, null, uri.getHost(), MailBoxConstants.SFTP_PORT, uri.getPath() == null ? "" : uri.getPath(), null, null).toString()));
            } else if (Protocol.HTTP.getCode().toString().equalsIgnoreCase(scheme)) {
                propertiesDTO.setPort(MailBoxConstants.HTTP_PORT);
                propertiesDTO.setUrl((new URI(scheme, null, uri.getHost(), MailBoxConstants.HTTP_PORT, uri.getPath() == null ? "" : uri.getPath(), null, null).toString()));
            } else if (Protocol.HTTPS.getCode().toString().equalsIgnoreCase(scheme)) {
                propertiesDTO.setPort(MailBoxConstants.HTTPS_PORT);
                propertiesDTO.setUrl((new URI(scheme, null, uri.getHost(), MailBoxConstants.HTTPS_PORT, uri.getPath() == null ? "" : uri.getPath(), null, null).toString()));
            } 
        } else if (uri.getPort() != -1 &&  propertiesDTO.getPort() == 0) {
            propertiesDTO.setPort(uri.getPort());
        } else if (uri.getPort() != propertiesDTO.getPort()) {
            propertiesDTO.setUrl((new URI(scheme, null, uri.getHost(), propertiesDTO.getPort(), uri.getPath() == null ? "" : uri.getPath(), null, null).toString()));
        }
        
    }

}
