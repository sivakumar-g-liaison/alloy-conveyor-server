/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.util;

import com.liaison.commons.util.UUIDGen;
import com.liaison.commons.util.settings.DecryptableConfiguration;
import com.liaison.commons.util.settings.LiaisonArchaiusConfiguration;
import com.liaison.fs2.metadata.FS2MetaSnapshot;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.dtdm.model.Processor;
import com.liaison.mailbox.dtdm.model.ProcessorProperty;
import com.liaison.mailbox.enums.DeploymentType;
import com.liaison.mailbox.enums.ProcessorType;
import com.liaison.mailbox.enums.Protocol;
import com.liaison.mailbox.service.dto.configuration.processor.properties.DropboxProcessorPropertiesDTO;
import com.liaison.mailbox.service.dto.configuration.processor.properties.HTTPListenerPropertiesDTO;
import com.liaison.mailbox.service.dto.configuration.processor.properties.SweeperPropertiesDTO;
import com.liaison.mailbox.service.dto.configuration.request.RemoteProcessorPropertiesDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.validation.GenericValidator;
import com.netflix.config.ConfigurationManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.introspect.JacksonAnnotationIntrospector;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.liaison.mailbox.MailBoxConstants.CATEGORY;
import static com.liaison.mailbox.MailBoxConstants.CONFIGURATION_QUEUE_SERVICE_ENABLED;
import static com.liaison.mailbox.MailBoxConstants.DIRECT_UPLOAD;
import static com.liaison.mailbox.MailBoxConstants.PIPELINE;
import static com.liaison.mailbox.MailBoxConstants.PROPERTY_PIPELINEID;
import static com.liaison.mailbox.MailBoxConstants.PROPERTY_STALE_FILE_TTL;
import static com.liaison.mailbox.MailBoxConstants.PROPERTY_URL;
import static com.liaison.mailbox.MailBoxConstants.PROP_DATA_FOLDER_PATTERN;
import static com.liaison.mailbox.MailBoxConstants.USE_FILE_SYSTEM;
import static com.liaison.mailbox.enums.Messages.ID_IS_INVALID;
import static com.liaison.mailbox.enums.Messages.INVALID_CONNECTION_TIMEOUT;
import static com.liaison.mailbox.enums.Messages.MANDATORY_FIELD_MISSING;
import static com.liaison.mailbox.enums.ProcessorType.CONDITIONALSWEEPER;
import static com.liaison.mailbox.enums.ProcessorType.DROPBOXPROCESSOR;
import static com.liaison.mailbox.enums.ProcessorType.HTTPASYNCPROCESSOR;
import static com.liaison.mailbox.enums.ProcessorType.HTTPSYNCPROCESSOR;
import static com.liaison.mailbox.enums.ProcessorType.REMOTEDOWNLOADER;
import static com.liaison.mailbox.enums.ProcessorType.REMOTEUPLOADER;
import static com.liaison.mailbox.enums.ProcessorType.SWEEPER;

/**
 * Utilities for MailBox.
 *
 * @author veerasamyn
 */
public class MailBoxUtil {

    private static final Logger LOGGER = LogManager.getLogger(MailBoxUtil.class);

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

    public static final DecryptableConfiguration CONFIGURATION = LiaisonArchaiusConfiguration.getInstance();
    public static final boolean QUEUE_SERVICE_ENABLED = CONFIGURATION.getBoolean(CONFIGURATION_QUEUE_SERVICE_ENABLED, false);

    /**
     * Initialize the cluster type.
     */
    public static String CLUSTER_TYPE;

    /**
     * Initialize the deployment type.
     */
    public static String DEPLOYMENT_TYPE;

    /**
     * Pattern to validate the path before directory creation
     */
    public static String DATA_FOLDER_PATTERN;

    static {

        CLUSTER_TYPE = CONFIGURATION.getString(MailBoxConstants.DEPLOYMENT_TYPE, DeploymentType.RELAY.getValue());
        DEPLOYMENT_TYPE = CLUSTER_TYPE;

        if (DeploymentType.LOW_SECURE_RELAY.getValue().equals(CLUSTER_TYPE)) {
            CLUSTER_TYPE = MailBoxConstants.LOWSECURE;
        } else {
            CLUSTER_TYPE = MailBoxConstants.SECURE;
        }

        String[] patterns = CONFIGURATION.getStringArray(PROP_DATA_FOLDER_PATTERN);
        DATA_FOLDER_PATTERN = String.join(",", patterns);
    }

    /**
     * Utility is used to un-marshal from JSON String to Object.
     *
     * @param serializedJson The serialized JSON String.
     * @param clazz The corresponding class of the serialized JSON.
     * @return Object The instance of the give Class.
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

        return mapper.readValue(serializedJson, clazz);

    }

    /**
     * Utility is used to marshal the Object to JSON.
     *
     * @param object
     * @return
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
     * Method to calculate the elapsed time between two given time limits
     *
     * @param startTime
     * @param endTime
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

        Map<String, Integer> pageParameters = new HashMap<>();
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
        pageParameters.put(MailBoxConstants.PAGE_VALUE, pageValue);

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
                return (int) (ttlNumber * DAYS_IN_MONTH * HOURS_PER_DAY * MINUTES_PER_HOUR * SECONDS_PER_MIN);
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
     * @param file File object
     * @return true if it is updated with in the given time limit, false otherwise
     */
    public static boolean validateLastModifiedTolerance(Path file) {

        long timelimit = CONFIGURATION.getLong(MailBoxConstants.LAST_MODIFIED_TOLERANCE);

        long system = System.currentTimeMillis();
        long lastmo = file.toFile().lastModified();

        LOGGER.debug("System time millis: {}, Last Modified {}, timelimit: {}", system, lastmo, timelimit);
        LOGGER.debug("(system - lastmo)/1000) = {}", ((system - lastmo) / 1000));

        return ((system - lastmo) / 1000) < timelimit;
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

            if (Protocol.FTP.getCode().equalsIgnoreCase(scheme) || Protocol.FTPS.getCode().equalsIgnoreCase(scheme)) {
                propertiesDTO.setPort(MailBoxConstants.FTPS_PORT);
            } else if (Protocol.SFTP.getCode().equalsIgnoreCase(scheme)) {
                propertiesDTO.setPort(MailBoxConstants.SFTP_PORT);
            } else if (Protocol.HTTP.getCode().equalsIgnoreCase(scheme)) {
                propertiesDTO.setPort(MailBoxConstants.HTTP_PORT);
            } else if (Protocol.HTTPS.getCode().equalsIgnoreCase(scheme)) {
                propertiesDTO.setPort(MailBoxConstants.HTTPS_PORT);
            }
            propertiesDTO.setUrl((new URI(scheme, null, uri.getHost(), propertiesDTO.getPort(), uri.getPath() == null ? "" : uri.getPath(), uri.getQuery(), null).toString()));
        } else if (uri.getPort() != -1 && propertiesDTO.getPort() == 0) {
            propertiesDTO.setPort(uri.getPort());
        } else if (uri.getPort() != propertiesDTO.getPort()) {
            propertiesDTO.setUrl((new URI(scheme, null, uri.getHost(), propertiesDTO.getPort(), uri.getPath() == null ? "" : uri.getPath(), uri.getQuery(), null).toString()));
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
        return fileValidity.before(currentTimestamp);
    }

    /**
     * Method to get the storage type
     *
     * @param dynamicProperties
     * @return storageType
     */
    public static String getStorageType(Set<ProcessorProperty> dynamicProperties) {

        String storageType = null;
        for (ProcessorProperty property : dynamicProperties) {
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
     *
     * @param json processor properties json
     * @return boolean
     */
    public static boolean isDirectUploadEnabled(String json) {

        try {
            Object obj = getJSONObject(json, DIRECT_UPLOAD);
            return Boolean.TRUE.equals(obj);
        } catch (JSONException e) {
            return false;
        }
    }

    /**
     * Util method to read the direct upload value from processor properties
     *
     * @param json processor properties json
     * @return boolean true by default, false if it is overridden in UI
     */
    public static boolean isUseFileSystemEnabled(String json) {

        try {
            Object obj = getJSONObject(json, USE_FILE_SYSTEM);
            return Boolean.TRUE.equals(obj);
        } catch (JSONException e) {
            return true;
        }
    }

    /**
     * Util method to read the stale file TTL value from processor properties
     *
     * @param json processor properties json
     * @return String TTl value
     */
    public static int getStaleFileTTLValue(String json) {

        try {
            Object o = getJSONObject(json, PROPERTY_STALE_FILE_TTL);
            return (int) o;
        } catch (JSONException e) {
            return 0;
        }
    }

    public static String getCategory(String json) {
        try {
            JSONObject o = getJSONObject(json);
            if (!JSONObject.NULL.equals(o)) {
                return o.getString(CATEGORY);
            }
        } catch (JSONException e) {
            return null;
        }
        return null;
    }

    /**
     * Reads a value from processor properties
     *
     * @param json processor properties
     * @param key  key to read a value
     * @return value
     * @throws JSONException
     */
    private static Object getJSONObject(String json, String key) throws JSONException {

        JSONObject obj = getJSONObject(json);
        return obj.get(key);
    }

    /**
     *  Get JSON Object
     * @param json
     * @return
     * @throws JSONException
     */
    private static JSONObject getJSONObject(String json) throws JSONException {

        String remotePrcsr = "remoteProcessorProperties";
        JSONObject obj;
        if (json.contains(remotePrcsr)) {
            JSONObject innerObj = new JSONObject(json);
            obj = innerObj.getJSONObject(remotePrcsr);
        } else {
            obj = new JSONObject(json);
        }
        return obj;
    }

    /**
     * validates pipeline id
     *
     * @param processorType processor type and
     * @param propertiesDTO remote processor properties dto
     */
    public static void validatePipelineId(ProcessorType processorType, Object obj) {

        String pipelineId = null;
        if (obj instanceof RemoteProcessorPropertiesDTO) {

            RemoteProcessorPropertiesDTO propertiesDTO = (RemoteProcessorPropertiesDTO) obj;
            if (HTTPSYNCPROCESSOR.equals(processorType) ||
                    HTTPASYNCPROCESSOR.equals(processorType) ||
                    DROPBOXPROCESSOR.equals(processorType)) {
                pipelineId = propertiesDTO.getHttpListenerPipeLineId();
            } else if (SWEEPER.equals(processorType) ||
                    CONDITIONALSWEEPER.equals(processorType)) {
                pipelineId = propertiesDTO.getPipeLineID();
            } else {
                return;
            }

        } else if (obj instanceof HTTPListenerPropertiesDTO) {
            HTTPListenerPropertiesDTO httPropDTO = (HTTPListenerPropertiesDTO) obj;
            pipelineId = httPropDTO.getHttpListenerPipeLineId();
        } else if (obj instanceof DropboxProcessorPropertiesDTO) {
            DropboxProcessorPropertiesDTO dbxPropDTO = (DropboxProcessorPropertiesDTO) obj;
            pipelineId = dbxPropDTO.getHttpListenerPipeLineId();
        } else if (obj instanceof SweeperPropertiesDTO) {
            SweeperPropertiesDTO sweeperPropDTO = (SweeperPropertiesDTO) obj;
            pipelineId = sweeperPropDTO.getPipeLineID();
        } else {
            return;
        }

        if (isEmpty(pipelineId)) {
            throw new MailBoxConfigurationServicesException(MANDATORY_FIELD_MISSING, PROPERTY_PIPELINEID.toUpperCase(), Response.Status.BAD_REQUEST);
        }

        if (isEmpty(ServiceBrokerUtil.getEntity(PIPELINE, pipelineId))) {
            throw new MailBoxConfigurationServicesException(ID_IS_INVALID, PIPELINE, Response.Status.BAD_REQUEST);
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
     * Construct a URL from a partial path. Base URL comes from properties.
     * 
     * @param property
     * @param path
     * @return
     * @throws IOException
     */
    public static String constructUrl(String property, String path) throws IOException {
    	
    	String baseUrl = MailBoxUtil.getEnvironmentProperties().getString(property);
    	if (baseUrl == null) {
    	    throw new RuntimeException(String.format("Property [%s] cannot be null", property));
    	}
    	// strip trailing slashes
    	while (baseUrl.endsWith("/")) {
    	    baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
    	}
    	return baseUrl + path;
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
    private static Thread getThreadByName(String threadName) {

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
        return null != runningThread && runningThread.isInterrupted();
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

    /**
     * Constructs expiration date of the payload based on created date and TTL
     *
     * @param meta fs2 meta data
     * @return timestamp of the expiration date
     */
    public static Timestamp getExpirationDate(FS2MetaSnapshot meta) {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(meta.getCreatedOn().toInstant(), ZoneId.systemDefault());
        return Timestamp.valueOf(localDateTime.plusSeconds(meta.getTTL()));
    }

    /**
     * This method is used to concatenate a unique system timestamp to the end
     * of the name in order to make it unique during logical delete. If the the
     * new name is longer than the field length, the original string is trimmed
     * at the original string is trimmed at the end first and then the timestamp
     * is appended to fit.
     *
     * @param str field value
     * @param fieldLength field length
     * @return String
     */
    public static String generateName(final String str, final int fieldLength) {

        String strTimestamp = new Timestamp(System.currentTimeMillis()).toString();
        final int totalLength = str.length() + 1 + strTimestamp.length();

        if (totalLength > fieldLength) {
            return str.substring(0, str.length() - (totalLength - fieldLength)) + "." + strTimestamp;
        } else {
            return str + "." + strTimestamp;

        }
    }

    /**
     * This method used to get the cluster types
     * 1. It will return 'LOWSECURE' if it is in LOW SECURE RELAY OR
     * 2. It will return both 'LOWSECURE' and 'SECURE' if it is SECURE RELAY.
     *
     * @return return list of cluster types
     */
    public static List<String> getClusterTypes() {
        return CLUSTER_TYPE.equals(MailBoxConstants.LOWSECURE) ? Collections.singletonList(MailBoxConstants.LOWSECURE) :
                Arrays.asList(MailBoxConstants.SECURE, MailBoxConstants.LOWSECURE);
    }

    /**
     * This method check whether the deployment type conveyor or not
     *
     * 1. It will return true if it is conveyor.
     *
     * @return boolean
     */
    public static boolean isConveyorType() {

        String deploymentType = MailBoxUtil.getEnvironmentProperties()
                .getString(MailBoxConstants.DEPLOYMENT_TYPE, DeploymentType.RELAY.getValue());
        return deploymentType.equals(DeploymentType.CONVEYOR.getValue());
    }
    
    /**
     * This method check the processor type and Protocol type
     *
     * It will return true if processor type is RemoteUploader and protocol is HTTP or HTTPS.
     * @param configurationInstance 
     *
     * @return boolean
     */
    public static boolean isHttpOrHttpsRemoteUploader(Processor configurationInstance) {
        return (ProcessorType.REMOTEUPLOADER.equals(configurationInstance.getProcessorType()) && 
                (Protocol.HTTP.equals(Protocol.findByCode(configurationInstance.getProcsrProtocol())) ||
                Protocol.HTTPS.equals(Protocol.findByCode(configurationInstance.getProcsrProtocol()))));
    }

    /**
     * To validate process dc list 
     * @param datacenterMap
     * @return
     */
    public static boolean validateProcessDc(Map<String, String> datacenterMap) {
    	
        for (String key : datacenterMap.keySet()) {
            if (!CONFIGURATION.getList(MailBoxConstants.PROCESS_DC_LIST).contains(key)) {
                return false;
            } 
        }
        
        return true;
    }

    /**
     * Get the Process dc to update
     * @param datacenterMap
     * @return
     */
    public static String getDcToUpdate(Map<String, String> datacenterMap) {

        for (String key : datacenterMap.keySet()) {
            if ("100".equals(datacenterMap.get(key))) {
                return key;
            }
        }
        
        return null;
    }

    public static <T> Collection<List<T>> partition(List<T> list, int size) {
        final AtomicInteger counter = new AtomicInteger(0);

        return list.stream()
                .collect(Collectors.groupingBy(it -> counter.getAndIncrement() / size))
                .values();
    }

}
