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
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;

import com.liaison.mailbox.enums.ProcessorType;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.validation.GenericValidator;

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
import com.liaison.mailbox.dtdm.model.ProcessorProperty;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.enums.Protocol;
import com.liaison.mailbox.service.dto.configuration.TenancyKeyDTO;
import com.liaison.mailbox.service.dto.configuration.request.RemoteProcessorPropertiesDTO;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.netflix.config.ConfigurationManager;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import static com.liaison.mailbox.MailBoxConstants.DIRECT_UPLOAD;
import static com.liaison.mailbox.MailBoxConstants.PROPERTY_PIPELINEID;
import static com.liaison.mailbox.MailBoxConstants.PROPERTY_URL;
import static com.liaison.mailbox.enums.Messages.INVALID_CONNECTION_TIMEOUT;
import static com.liaison.mailbox.enums.Messages.MANDATORY_FIELD_MISSING;
import static com.liaison.mailbox.enums.ProcessorType.DROPBOXPROCESSOR;
import static com.liaison.mailbox.enums.ProcessorType.HTTPASYNCPROCESSOR;
import static com.liaison.mailbox.enums.ProcessorType.HTTPSYNCPROCESSOR;
import static com.liaison.mailbox.enums.ProcessorType.REMOTEDOWNLOADER;
import static com.liaison.mailbox.enums.ProcessorType.REMOTEUPLOADER;
import static com.liaison.mailbox.enums.ProcessorType.SWEEPER;
import static com.liaison.mailbox.MailBoxConstants.PROPERTY_STALE_FILE_TTL;

/**
 * Utilities for MailBox.
 *
 * @author veerasamyn
 */
public class MailBoxUtil {

	private static final Logger LOGGER = LogManager.getLogger(MailBoxUtil.class);
	private static final DecryptableConfiguration CONFIGURATION = LiaisonConfigurationFactory.getConfiguration();
	//for fetch datacenter name.
	public static final String DATACENTER_NAME = System.getProperty("archaius.deployment.datacenter");
	// for logging dropbox related details.
	public static final String seperator = ": ";
	private static final float SECONDS_PER_MIN = 60;
	private static final float MINUTES_PER_HOUR = 60;
	private static final float HOURS_PER_DAY = 24;
	private static final float DAYS_IN_WEEK = 7;
	private static final float DAYS_IN_MONTH = 30;
	private static final float DAYS_IN_YEAR = 365;

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

        LOGGER.debug("List of Tenancy keys retrieved are {}", tenancyKeys);
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
		
		if (null == tenancyKeyDisplayName) {
			tenancyKeyDisplayName = tenancyKeyGuid;
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
	 * Method to convertTTLIntoSeconds
	 *
	 * @param ttlUnit
	 * @param ttlNumber
	 * @return Integer
	 */
	public static Integer convertTTLIntoSeconds(String ttlUnit, Integer ttlNumber) {

		switch (ttlUnit) {
        case MailBoxConstants.TTL_UNIT_YEARS:
            return (int) (ttlNumber * DAYS_IN_YEAR * HOURS_PER_DAY * MINUTES_PER_HOUR * SECONDS_PER_MIN);
        case MailBoxConstants.TTL_UNIT_MONTHS:
            return (int) (ttlNumber * DAYS_IN_MONTH  * HOURS_PER_DAY * MINUTES_PER_HOUR * SECONDS_PER_MIN);
        case MailBoxConstants.TTL_UNIT_WEEKS:
            return (int) (ttlNumber * DAYS_IN_WEEK * HOURS_PER_DAY * MINUTES_PER_HOUR * SECONDS_PER_MIN);
        case MailBoxConstants.TTL_UNIT_DAYS:
            return (int) (ttlNumber * HOURS_PER_DAY * MINUTES_PER_HOUR * SECONDS_PER_MIN);
        case MailBoxConstants.TTL_UNIT_HOURS:
            return (int) (ttlNumber * MINUTES_PER_HOUR * SECONDS_PER_MIN);
        case MailBoxConstants.TTL_UNIT_MINUTES:
            return (int) (ttlNumber * SECONDS_PER_MIN);
        default:
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
	    
		switch (ttlUnit) {
        case MailBoxConstants.TTL_UNIT_YEARS:
            return (int) Math.ceil(ttlNumber * DAYS_IN_YEAR);
        case MailBoxConstants.TTL_UNIT_MONTHS:
            return (int) Math.ceil(ttlNumber * DAYS_IN_MONTH);
        case MailBoxConstants.TTL_UNIT_WEEKS:
            return (int) Math.ceil(ttlNumber * DAYS_IN_WEEK);
        case MailBoxConstants.TTL_UNIT_DAYS:
            return ttlNumber;
        case MailBoxConstants.TTL_UNIT_HOURS:
            return (int) Math.ceil(ttlNumber / HOURS_PER_DAY);
        case MailBoxConstants.TTL_UNIT_MINUTES:
            return (int) Math.ceil(ttlNumber / (HOURS_PER_DAY * MINUTES_PER_HOUR));
        default:
            return (int) Math.ceil(ttlNumber / (HOURS_PER_DAY * MINUTES_PER_HOUR * SECONDS_PER_MIN));
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
            } else if (Protocol.SFTP.getCode().toString().equalsIgnoreCase(scheme)) {
                propertiesDTO.setPort(MailBoxConstants.SFTP_PORT);
            } else if (Protocol.HTTP.getCode().toString().equalsIgnoreCase(scheme)) {
                propertiesDTO.setPort(MailBoxConstants.HTTP_PORT);
            } else if (Protocol.HTTPS.getCode().toString().equalsIgnoreCase(scheme)) {
                propertiesDTO.setPort(MailBoxConstants.HTTPS_PORT);
            } 
            propertiesDTO.setUrl((new URI(scheme, null, uri.getHost(), propertiesDTO.getPort(), uri.getPath() == null ? "" : uri.getPath(), null, null).toString()));
        } else if (uri.getPort() != -1 &&  propertiesDTO.getPort() == 0) {
            propertiesDTO.setPort(uri.getPort());
        } else if (uri.getPort() != propertiesDTO.getPort()) {
            propertiesDTO.setUrl((new URI(scheme, null, uri.getHost(), propertiesDTO.getPort(), uri.getPath() == null ? "" : uri.getPath(), null, null).toString()));
        }
        
    }

    /**
     * Checks the given list is empty or not.
     * 
     * @param list
     * @return boolean
     */
    public static boolean isEmptyList(List<String> list) {
        return list == null || list.isEmpty();
    }
    
    /**
     * Method to find if the file expired after the stale file ttl configured in properties file for sweeper
     * file is considered as expired if the (last modified time + ttl) is before current time
     * 
     * @param lastModified - the last modified time of file which needs to be validated for expiry
     * @param staleFileTTL  ttl for the file in filesystem
     * @return true if file expired otherwise false
     */
    public static boolean isFileExpired(long lastModified, int staleFileTTL) {
    	
        if (0 == staleFileTTL) {
            staleFileTTL = CONFIGURATION.getInt(MailBoxConstants.PROPERTY_STALE_FILE_CLEAN_UP,
                                                    MailBoxConstants.STALE_FILE_CLEAN_UP_TTL);
        }
		// calculate file validity
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(lastModified);
		cal.add(Calendar.DATE, staleFileTTL);
        Timestamp fileValidity = new Timestamp(cal.getTime().getTime());
        LOGGER.debug("The file validity is {}", fileValidity);
        
        Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
        LOGGER.debug("Current time is {}", currentTimestamp);
		return fileValidity.before(currentTimestamp) ;
    }

	/**
	 * Method to get the storage type
	 * 
	 * @param dynamicProperties
	 * @return storageType
	 */
	public static String getStorageType(Set<ProcessorProperty> dynamicProperties) {
		
		String storageType = null;
		Iterator<ProcessorProperty> iterator = dynamicProperties.iterator();
		while (iterator.hasNext()) {
			ProcessorProperty property = iterator.next();
			if (MailBoxConstants.STORAGE_IDENTIFIER_TYPE.equals(property.getProcsrPropName())) {
				storageType = property.getProcsrPropValue();
				break;
			}
		}
		return storageType;
	}

    /**
     * http success code validation
     *
     * @param http_status
     * @return
     */
    public static boolean isSuccessful(int http_status) {
		return http_status >= 200 && http_status <= 299;
    } 
    
    /**
     * Checks the given set is empty or not.
     * 
     * @param set
     * @return boolean
     */
    public static boolean isEmptySet(Set<String> set) {
        return set == null || set.isEmpty();
    }

    /**
     * Util method to read the direct upload value from processor properties
     * @param json processor properties json
     * @return boolean
     */
    public static boolean isDirectUploadEnabled(String json) {

		String remotePrcsr = "remoteProcessorProperties";

        try {

			JSONObject obj = null;
			if (json.contains(remotePrcsr)) {
				JSONObject innerObj = new JSONObject(json);
				obj = innerObj.getJSONObject(remotePrcsr);
			} else {
				obj = new JSONObject(json);
			}

            Object o = obj.get(DIRECT_UPLOAD);
            return Boolean.TRUE.equals(o);
        } catch (JSONException e) {
            return false;
        }

    }

    /**
     * validates pipeline id
     *
     * @param processorType processor type and
     * @param propertiesDTO remote processor properties dto
     */
    public static void validatePipelineId(ProcessorType processorType, RemoteProcessorPropertiesDTO propertiesDTO) {

        String pipelineId = null;
        if (HTTPSYNCPROCESSOR.equals(processorType) ||
                HTTPASYNCPROCESSOR.equals(processorType) ||
                DROPBOXPROCESSOR.equals(processorType)) {
            pipelineId = propertiesDTO.getHttpListenerPipeLineId();
        } else if (SWEEPER.equals(processorType)) {
            pipelineId = propertiesDTO.getPipeLineID();
        } else {
			return;
		}

        if (isEmpty(pipelineId)) {
            throw new MailBoxConfigurationServicesException(MANDATORY_FIELD_MISSING, PROPERTY_PIPELINEID.toUpperCase(), Response.Status.BAD_REQUEST);
        }
    }

    /**
     * validates connection timeout
     *
     * @param processorType the processor type
     * @param connectionTimeout connection timeout
     * @param validator generic validator instance
     */
    public static void validateConnectionTimeout(ProcessorType processorType, int connectionTimeout, GenericValidator validator) {

        if (HTTPSYNCPROCESSOR.equals(processorType) ||
                HTTPASYNCPROCESSOR.equals(processorType)) {
            if ((0 != connectionTimeout) && !validator.isHttpBetweenRange(connectionTimeout)) {
                throw new MailBoxConfigurationServicesException(INVALID_CONNECTION_TIMEOUT, Response.Status.BAD_REQUEST);
            }
        } else if (REMOTEUPLOADER.equals(processorType) ||
                REMOTEDOWNLOADER.equals(processorType)) {
            if ((0 != connectionTimeout) && !validator.isBetweenRange(connectionTimeout)) {
                throw new MailBoxConfigurationServicesException(INVALID_CONNECTION_TIMEOUT, Response.Status.BAD_REQUEST);
            }
        }
    }

    /**
     * validates url
     *
     * @param processorType the processor type
     * @param propertiesDTO remote processor properties dto
     * @throws URISyntaxException
     * @throws MalformedURLException
     */
    public static void validateURL(ProcessorType processorType, RemoteProcessorPropertiesDTO propertiesDTO) throws URISyntaxException, MalformedURLException {

        if (REMOTEUPLOADER.equals(processorType) ||
                REMOTEDOWNLOADER.equals(processorType)) {
            if (!MailBoxUtil.isEmpty(propertiesDTO.getUrl())) {
                MailBoxUtil.constructURLAndPort(propertiesDTO);
            } else {
                throw new MailBoxConfigurationServicesException(MANDATORY_FIELD_MISSING, PROPERTY_URL.toUpperCase(), Response.Status.BAD_REQUEST);
            }
        }
    }
    
    /**
     * Util method to read the stale file TTL value from processor properties
     * @param json processor properties json
     * @return String TTl value
     */
    public static int getStaleFileTTLValue(String json) {

        String remotePrcsr = "remoteProcessorProperties";
        try {

            JSONObject obj = null;
            if (json.contains(remotePrcsr)) {
                JSONObject innerObj = new JSONObject(json);
                obj = innerObj.getJSONObject(remotePrcsr);
            } else {
                obj = new JSONObject(json);
            }

            Object o = obj.get(PROPERTY_STALE_FILE_TTL);
            return (int) o;
        } catch (JSONException e) {
            return 0;
        }
    }

    /**
     * To get current execution node
     *
     * @return node hostname
     */
    public static String getNode() {

        if (MailBoxUtil.isEmpty(ConfigurationManager.getDeploymentContext().getDeploymentServerId())) {
            try {
                return InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }
        } else {
            return ConfigurationManager.getDeploymentContext().getDeploymentServerId();
        }
    }

    /**
     * To get thread by name
     *
     * @param threadName thred name
     * @return Thread
     */
    public static Thread getThreadByName(String threadName) {

        for (Thread t : Thread.getAllStackTraces().keySet()) {
            if (t.getName().equals(threadName)) return t;
        }
        return null;
    }

    /**
     * To check thread interrupt status.
     *
     * @param threadName thread name
     * @return true if it is interrupted
     */
    public static boolean isInterrupted(String threadName) {

        Thread runningThread = getThreadByName(threadName);
        if (null != runningThread) {
            return runningThread.isInterrupted();
        }
        return false;
    }

    /**
     * To interrupt a thread by name
     *
     * @param threadName thread name
     */
    public static void interruptThread(String threadName) {

        Thread runningThread = getThreadByName(threadName);
        if (null != runningThread) {
            runningThread.interrupt();
        }
    }

    /**
     * Helper to get protocol from filepath
     * 
     * @param filePath
     * @return
     */
    public static String getProtocolFromFilePath(String filePath) {
        
        if (filePath.contains(Protocol.FTP.getCode())) {
            return Protocol.FTP.getCode();
        } else if (filePath.contains(Protocol.FTPS.getCode())) {
            return Protocol.FTPS.getCode();
        } else if (filePath.contains(Protocol.SFTP.getCode())) {
            return Protocol.SFTP.getCode();
        } else {
            return filePath;
        }
    }
}
