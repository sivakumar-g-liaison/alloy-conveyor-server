/**
 * Copyright 2014 Liaison Technologies, Inc.
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.storage.util;

import com.liaison.commons.jaxb.JAXBUtility;
import com.liaison.commons.util.UUIDGen;
import com.liaison.commons.util.settings.DecryptableConfiguration;
import com.liaison.dto.queue.WorkTicket;
import com.liaison.fs2.api.FS2Configuration;
import com.liaison.fs2.api.FS2Factory;
import com.liaison.fs2.api.FS2ObjectHeaders;
import com.liaison.fs2.api.FS2Options;
import com.liaison.fs2.api.FS2StorageIdentifier;
import com.liaison.fs2.api.FlexibleStorageSystem;
import com.liaison.fs2.api.encryption.impl.KeyManagerKEKProvider;
import com.liaison.fs2.api.encryption.impl.SimpleEncryptionProvider;
import com.liaison.fs2.api.exceptions.FS2Exception;
import com.liaison.fs2.api.exceptions.FS2ObjectAlreadyExistsException;
import com.liaison.fs2.api.exceptions.FS2PayloadNotFoundException;
import com.liaison.fs2.metadata.FS2MetaSnapshot;
import com.liaison.fs2.storage.file.FS2DefaultFileConfig;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.util.MailBoxUtil;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author VNagarajan
 *         <p>
 *         This utility class persists and retrieves the payload from various file systems. Currently there
 *         spectrum(secure/unsecure), boss(secure/unsecure) and flat file system.
 */
public class StorageUtilities {

    private static final Logger LOGGER = LogManager.getLogger(StorageUtilities.class);

    /**
     * Used to read properties
     */
    private static final DecryptableConfiguration configuration = MailBoxUtil.getEnvironmentProperties();

    /**
     * Constants used to read the specturm values
     */
    private static final String PROPERTY_LOCATION_DEFAULT = "fs2.storage.location.default";

    /**
     * Constants used to read file system values
     */
    private static final String PROPERTY_FS2_STORAGE_FILE_DEFAULT_USE = "fs2.storage.file.default.use";
    private static final String PROPERTY_FS2_STORAGE_FILE_DEFAULT_TYPE = "fs2.storage.file.default.type";
    private static final String PROPERTY_FS2_STORAGE_FILE_DEFAULT_LOCATION = "fs2.storage.file.default.location";
    private static final String PROPERTY_FS2_STORAGE_FILE_DEFAULT_MOUNT = "fs2.storage.file.default.mount";

    /**
     * Constants of Storage Identifiers
     */
    private static final String SPECTRUM_STORAGE_IDENTIFIER = "SPECTRUM";
    private static final String BOSS_STORAGE_IDENTIFIER = "BOSS";
    private static final String STORAGE_IDENTIFIER_BOSS_SUFFIX = "-boss";

    /**
     * Moniker to identify secure and unsecure
     */
    private static final String BOSS_MONIKER_PREFIX = "boss-";
    private static final String SECURE_MONIKER = "secure";
    private static final String UNSECURE_MONIKER = "unsecure";

    /**
     * FS2 URI path for mailbox. There is no difference between sync and async
     */
    private static final String FS2_URI_MBX_PAYLOAD = "/mailbox/payload/1.0/";

    private static FlexibleStorageSystem FS2 = null;
    private static List<FS2Configuration> filesystemConfigs;

    public static final String GLOBAL_PROCESS_ID_HEADER = "GLOBAL_PROCESS_ID";

    /*
     * Initialize FS2
     */
    static {

        LOGGER.debug("Initializing FS2");
        configureFilesystem();

        FS2 = FS2Factory.newInstance(configuration,
                (storageId) -> {
                    if (storageId.getStorageType().equals(SECURE_MONIKER)) {
                        return new SimpleEncryptionProvider();
                    }

                    return null;
                },
                (storageId) -> {
                    if (storageId.getStorageType().equals(SECURE_MONIKER)) {
                        return new KeyManagerKEKProvider();
                    }

                    return null;
                },
                filesystemConfigs);

        LOGGER.info("FS2 Initialized Successfully ");
    }

    /**
     * A helper method to retrieve the payload from spectrum(secure/unsecure) or file system.
     *
     * @param payloadURL fs2 uri
     * @return payload
     */
    public static InputStream retrievePayload(String payloadURL) {

        try {
            URI payloadURI = new URI(payloadURL);
            LOGGER.debug("Retrieving payload from fs2 storage");
            return FS2.getFS2PayloadInputStream(payloadURI);
        } catch (FS2PayloadNotFoundException | URISyntaxException e) {
            LOGGER.error(Messages.PAYLOAD_READ_ERROR.value(), e);
            throw new MailBoxServicesException(Messages.PAYLOAD_READ_ERROR, Response.Status.BAD_REQUEST);
        }
    }

    /**
     * A helper method to persist the payload into spectrum and file system.
     * 
     * @param payload                     input payload
     * @param httpListenerProperties      additional properties
     * @param triggerFileGlobalProcessId  Parent global process Id
     * @param tenancyKey 
     * @return
     */
    public static FS2MetaSnapshot persistPayload(InputStream payload, Map<String, String> httpListenerProperties, String triggerFileGlobalProcessId, String tenancyKey) {
    	
        try {
        	
            boolean isSecure = Boolean.valueOf(httpListenerProperties.get(MailBoxConstants.PROPERTY_HTTPLISTENER_SECUREDPAYLOAD));
            String uri = FS2_URI_MBX_PAYLOAD + triggerFileGlobalProcessId;

            // persists the message in spectrum.
            LOGGER.debug("Persist the payload **");
            URI requestUri = createPayloadURI(uri, isSecure, httpListenerProperties.get(MailBoxConstants.STORAGE_IDENTIFIER_TYPE));

            // fetch the metdata includes payload size
            FS2MetaSnapshot metaSnapshot;
            FS2ObjectHeaders fs2Header = new FS2ObjectHeaders();
            if (null != tenancyKey) {
                fs2Header.addHeader(MailBoxConstants.KEY_TENANCY_KEY, tenancyKey);
            }

            try {
                metaSnapshot = FS2.createObjectEntry(requestUri, fs2Header, payload);
            } finally {
                if (payload != null) {
                    payload.close();
                }
            }

            LOGGER.debug("Successfully persist the payload in fs2 storage to url {} ", requestUri);
            return metaSnapshot;

    	} catch (FS2ObjectAlreadyExistsException e) {
            throw new MailBoxServicesException(Messages.PAYLOAD_ALREADY_EXISTS, Response.Status.CONFLICT);
        } catch (FS2Exception | IOException e) {
            throw new MailBoxServicesException(Messages.PAYLOAD_PERSIST_ERROR, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * A helper method to persist the payload into spectrum(secure/unsecure)/boss(secure/unsecure) and file system.
     *
     * @param payload                input payload
     * @param workTicket             wokticket
     * @param httpListenerProperties additional properties
     * @param isDropbox              boolean to denote the use case
     * @return meta snapshot
     */
    public static FS2MetaSnapshot persistPayload(InputStream payload, WorkTicket workTicket,
                                                 Map<String, String> httpListenerProperties, boolean isDropbox) {

        try {

            String tenancyKey = httpListenerProperties.get(MailBoxConstants.KEY_TENANCY_KEY);
            String loginId = httpListenerProperties.get(MailBoxConstants.LOGIN_ID);
            String globalProcessorId = workTicket.getGlobalProcessId();
            String localProcessorId = workTicket.getGlobalProcessId();
            String messageName = workTicket.getFileName() + MailBoxUtil.getGUID();

            boolean isSecure = Boolean.parseBoolean(httpListenerProperties.get(MailBoxConstants.PROPERTY_HTTPLISTENER_SECUREDPAYLOAD));

            long startTime = 0;
            long endTime = 0;

            FS2ObjectHeaders fs2Header = constructFS2Headers(workTicket, httpListenerProperties);

            String uri = FS2_URI_MBX_PAYLOAD + globalProcessorId;
            if (isDropbox) {
                uri = "/" + tenancyKey + "/" + loginId + "/" + globalProcessorId + "." + localProcessorId + "_"
                        + messageName;
            }

            // persists the message in spectrum.
            LOGGER.debug("Persist the payload **");
            URI requestUri = createPayloadURI(uri, isSecure, httpListenerProperties.get(MailBoxConstants.STORAGE_IDENTIFIER_TYPE));

            // fetch the metdata includes payload size
            FS2MetaSnapshot metaSnapshot;
            try {
            	metaSnapshot = FS2.createObjectEntry(requestUri, null, null, null);
//                metaSnapshot = FS2.createObjectEntry(requestUri, generateFS2Options(workTicket), fs2Header, payload);
                LOGGER.debug("Time spent on uploading file {} of size {} to fs2 storage only is {} ms",
                        workTicket.getFileName(), metaSnapshot.getPayloadSize(), endTime - startTime);
            } finally {
                if (payload != null) {
                    payload.close();
                }
            }

            LOGGER.debug("Successfully persist the payload in fs2 storage to url {} ", requestUri);
            return metaSnapshot;

        } catch (FS2ObjectAlreadyExistsException e) {
            LOGGER.error(Messages.PAYLOAD_ALREADY_EXISTS.value(), e);
            throw new MailBoxServicesException(Messages.PAYLOAD_ALREADY_EXISTS, Response.Status.CONFLICT);
        } catch (FS2Exception | IOException e) {
            LOGGER.error(Messages.PAYLOAD_PERSIST_ERROR.value(), e);
            throw new MailBoxServicesException(Messages.PAYLOAD_PERSIST_ERROR, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * A helper method to persist the payload into spectrum(secure/unsecure) and file system.
     *
     * @param workTicket workticket
     * @param properties properties
     */
    public static void persistWorkTicket(WorkTicket workTicket, Map<String, String> properties) {

        try {

            String globalProcessorId = workTicket.getGlobalProcessId();

            long startTime = 0;
            long endTime = 0;

            FS2ObjectHeaders fs2Header = constructFS2Headers(workTicket, properties);
            fs2Header.addHeader(MailBoxConstants.KEY_MESSAGE_NAME, "MAILBOX_WORKTICKET");
            fs2Header.addHeader(MailBoxConstants.KEY_PAYLOAD_DESCRIPTION, "Workticket created by mailbox sweeper");

            String uri = FS2_URI_MBX_PAYLOAD + globalProcessorId + "_WORKTICKET";

            // persists the message in spectrum.
            LOGGER.debug("Persist the workticket **");
            URI requestUri = createPayloadURI(uri, false, properties.get(MailBoxConstants.STORAGE_IDENTIFIER_TYPE));

            // fetch the metadata includes payload size
            FS2MetaSnapshot metaSnapshot;
            InputStream is = null;
            try {
                is = new ByteArrayInputStream(JAXBUtility.marshalToJSON(workTicket).getBytes());
                metaSnapshot = FS2.createObjectEntry(requestUri, generateFS2Options(workTicket), fs2Header, is);
                LOGGER.debug("Time spent on uploading workticket {} of size {} to spectrum only is {} ms",
                        workTicket.getFileName(), metaSnapshot.getPayloadSize(), endTime - startTime);
            } finally {
                if (is != null) {
                    is.close();
                }
            }

            LOGGER.debug("Successfully persisted the workticket in spectrum to url {} ", requestUri);

        } catch (FS2ObjectAlreadyExistsException e) {
            LOGGER.error(Messages.PAYLOAD_ALREADY_EXISTS.value(), e);
            throw new MailBoxServicesException(Messages.PAYLOAD_ALREADY_EXISTS, Response.Status.CONFLICT);
        } catch (FS2Exception | IOException | JAXBException e) {
            LOGGER.error(Messages.PAYLOAD_PERSIST_ERROR.value(), e);
            throw new MailBoxServicesException(Messages.PAYLOAD_PERSIST_ERROR, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * A helper method to automatically create a spectrum URI in the format needed to determine the secure spectrum or
     * the unsecure spectrum at the default location.
     *
     * @param path              path
     * @param secure            boolean to denote the storage type
     * @param storageIdentifier storage identifier
     * @return fs2 uri
     */
    private static URI createPayloadURI(String path, boolean secure, String storageIdentifier) {

        URI uri;
        boolean defaultFileuse = configuration.getBoolean(PROPERTY_FS2_STORAGE_FILE_DEFAULT_USE, false);

        if (defaultFileuse) {
            String defaultType = configuration.getString(PROPERTY_FS2_STORAGE_FILE_DEFAULT_TYPE, "file");
            String defaultFileLocation = configuration.getString(PROPERTY_FS2_STORAGE_FILE_DEFAULT_LOCATION, "local");
            uri = createPayloadURI(path, defaultType, defaultFileLocation);
            return uri;
        }

        if (secure) {
            uri = createPayloadURI(path, SECURE_MONIKER, getStorageIdentifierType(storageIdentifier));
        } else {
            uri = createPayloadURI(path, UNSECURE_MONIKER, getStorageIdentifierType(storageIdentifier));
        }

        return uri;
    }

    /**
     * A helper method to get the storageIdentifier type.
     *
     * @param storageIdentifier storage identifier like secure@uat
     * @return string
     */
    private static String getStorageIdentifierType(String storageIdentifier) {

        String defaultLocation = configuration.getString(PROPERTY_LOCATION_DEFAULT);
        if (StringUtils.isEmpty(storageIdentifier)) {
            return defaultLocation;
        } else {

            switch (storageIdentifier.toUpperCase()) {
                case BOSS_STORAGE_IDENTIFIER:
                    return defaultLocation.endsWith(STORAGE_IDENTIFIER_BOSS_SUFFIX)
                            ? defaultLocation
                            : defaultLocation + STORAGE_IDENTIFIER_BOSS_SUFFIX;
                case SPECTRUM_STORAGE_IDENTIFIER:
                    return defaultLocation.endsWith(STORAGE_IDENTIFIER_BOSS_SUFFIX)
                            ? defaultLocation.substring(0, defaultLocation.lastIndexOf("-"))
                            : defaultLocation;
                default:
                    LOGGER.warn("Invalid storage identifier - {}, and using default storage - {}.", storageIdentifier, defaultLocation);
                    return defaultLocation;
            }
        }
    }

    /**
     * A helper method to automatically create a spectrum URI in the format needed. Throws an exception if the type and
     * location don't match a configured value.
     *
     * @param path     path
     * @param type     secure/unsecure
     * @param location location
     * @return uri
     */
    private static URI createPayloadURI(String path, String type, String location) {

        URI uri;

        // We need to ensure the path starts with a path or a format
        // exception is thrown.
        if (!path.startsWith("/")) {
            path = "/" + path;
        }

        String authority = type + "@" + location;
        boolean noMatch = true;

        for (FS2Configuration fs2Config : FS2.getStorageConfigs()) {
            FS2StorageIdentifier id = fs2Config.getStorageIdentifier();
            if (id.getStorageType().equals(type) && id.getStorageLocation().equals(location)) {
                noMatch = false;
            }
        }

        if (noMatch) {
            throw new MailBoxServicesException(String.format("Unable to create a URI because no storage configuration exists for type [%s] and location [%s].", type, location), Response.Status.PRECONDITION_FAILED);
        }

        try {
            uri = new URI("fs2", authority, path, null, null);
        } catch (URISyntaxException e) {
            LOGGER.error("Unable to create a URI", e);
            throw new MailBoxServicesException(Messages.COMMON_SYNC_ERROR_MESSAGE, Response.Status.INTERNAL_SERVER_ERROR);
        }
        return uri;
    }


    /**
     * configures file system providers
     */
    private static void configureFilesystem() {

        final String defaultType = configuration.getString(PROPERTY_FS2_STORAGE_FILE_DEFAULT_TYPE, "file");
        final String defaultLocation = configuration.getString(PROPERTY_FS2_STORAGE_FILE_DEFAULT_LOCATION, "local");
        final String defaultFileMountPoint = configuration.getString(PROPERTY_FS2_STORAGE_FILE_DEFAULT_MOUNT, "/tmp");

        filesystemConfigs = new ArrayList<>();
        FS2DefaultFileConfig config = new FS2DefaultFileConfig(new FS2StorageIdentifier(defaultType, defaultLocation)) {

            @Override
            public boolean shouldDeleteExistingData() {
                return false;
            }

            @Override
            public String getMountPoint() {
                return defaultFileMountPoint;
            }
        };
        filesystemConfigs.add(config);

    }

    /**
     * This method will persist payload in spectrum or boss.
     *
     * @param stream                 input payload
     * @param workTicket             workticket
     * @param httpListenerProperties props
     * @param isDropbox              boolean to denote the use case
     * @throws Exception auto close exception
     */
    public static void storePayload(InputStream stream, WorkTicket workTicket,
                                    Map<String, String> httpListenerProperties, boolean isDropbox)
            throws Exception {

        try (InputStream payloadToPersist = stream) {
            FS2MetaSnapshot metaSnapshot = StorageUtilities.persistPayload(payloadToPersist, workTicket, httpListenerProperties, isDropbox);
            LOGGER.info("Payload URL for the file {} is {}. File size in bytes {} ", workTicket.getFileName(), metaSnapshot.getURI().toString(), metaSnapshot.getPayloadSize());
            workTicket.setPayloadSize(metaSnapshot.getPayloadSize());
            workTicket.setPayloadURI(metaSnapshot.getURI().toString());
        }
    }

    /**
     * Method to construct FS2ObjectHeaders from the given workTicket
     *
     * @param workTicket workticket
     * @return httpListenerProperties additional properties
     */
    private static FS2ObjectHeaders constructFS2Headers(WorkTicket workTicket, Map<String, String> httpListenerProperties) {

        FS2ObjectHeaders fs2Header = new FS2ObjectHeaders();
        if (null != workTicket.getGlobalProcessId()) {
            fs2Header.addHeader(MailBoxConstants.KEY_GLOBAL_PROCESS_ID, workTicket.getGlobalProcessId());
        }
        if (null != workTicket.getPipelineId()) {
            fs2Header.addHeader(MailBoxConstants.KEY_PIPELINE_ID, workTicket.getPipelineId());
            fs2Header.addHeader(MailBoxConstants.KEY_TENANCY_KEY,
                    (MailBoxConstants.PIPELINE_FULLY_QUALIFIED_PACKAGE + ":" + workTicket.getPipelineId()));
        }
        if (null != httpListenerProperties.get(MailBoxConstants.KEY_SERVICE_INSTANCE_ID)) {
            fs2Header.addHeader(MailBoxConstants.KEY_SERVICE_INSTANCE_ID,
                    httpListenerProperties.get(MailBoxConstants.KEY_SERVICE_INSTANCE_ID));
        }
        if (null != httpListenerProperties.get(MailBoxConstants.PROPERTY_LENS_VISIBILITY)) {
            fs2Header.addHeader(MailBoxConstants.KEY_LENS_VISIBILITY, httpListenerProperties.get(MailBoxConstants.PROPERTY_LENS_VISIBILITY));
        }
        if (null != httpListenerProperties.get(MailBoxConstants.CONTENT_TYPE)) {
            fs2Header.addHeader(MailBoxConstants.CONTENT_TYPE.toLowerCase(), httpListenerProperties.get(MailBoxConstants.CONTENT_TYPE));
        }
        if (null != workTicket.getAdditionalContextItem(MailBoxConstants.KEY_TENANCY_KEY)) {
            fs2Header.addHeader(MailBoxConstants.KEY_TENANCY_KEY, workTicket.getAdditionalContextItem(MailBoxConstants.KEY_TENANCY_KEY));
        }
        LOGGER.debug("FS2 Headers set are {}", fs2Header.getHeaders());

        return fs2Header;
    }

    /**
     * Creates the FS2 Options.
     *
     * @param workTicket workticket
     * @return FS2Options
     */
    private static FS2Options generateFS2Options(WorkTicket workTicket) {

        FS2Options fs2Options = new FS2Options();
        int ttl = workTicket.getTtlDays();
        if (ttl != -1) {
            // set up custom ttl
            fs2Options.setTtl((int) TimeUnit.DAYS.toSeconds(ttl));
        }

        return fs2Options;
    }

    /**
     * A helper method to retrieve the payload headers.
     *
     * @param payloadURL Payload URL
     * @return FS2ObjectHeaders
     */
    public static FS2ObjectHeaders retrievePayloadHeaders(String payloadURL) {

        try {
            URI payloadURI = new URI(payloadURL);
            LOGGER.debug("Retrieving payload headers from spectrum/boss");
            return FS2.getHeaders(payloadURI);
        } catch (URISyntaxException | FS2Exception e) {
            LOGGER.error(Messages.PAYLOAD_HEADERS_READ_ERROR, e);
            throw new MailBoxServicesException(Messages.PAYLOAD_HEADERS_READ_ERROR, Response.Status.BAD_REQUEST);
        }
    }

    /**
     * A helper method to retrieve fs2 meta data
     *
     * @param payloadURL Payload URL
     * @return FS2MetaSnapshot fs2 meta data
     */
    public static FS2MetaSnapshot getMetaData(String payloadURL) {

        try {
            URI payloadURI = new URI(payloadURL);
            return FS2.fetchObject(payloadURI);
        } catch (URISyntaxException | FS2Exception e) {
            LOGGER.error(Messages.META_DATA_READ_ERROR, e);
            throw new MailBoxServicesException(Messages.META_DATA_READ_ERROR, Response.Status.BAD_REQUEST);
        }
    }

    /**
     * Method to check whether the payload exists at given path
     *
     * @param path - path of payload
     * @return true if payload exists otherwise false
     */
    public static boolean isPayloadExists(String path) {

        try {
            return FS2.exists(new URI(path));
        } catch (URISyntaxException | FS2Exception e) {
            LOGGER.error(Messages.PAYLOAD_DOES_NOT_EXIST, e);
            throw new MailBoxServicesException(Messages.PAYLOAD_DOES_NOT_EXIST, Response.Status.BAD_REQUEST);
        }
    }

    /**
     * Reads payload size from the fs2 meta data
     *
     * @param messageUri message uri
     * @return long payload size
     */
    public static long getPayloadSize(String messageUri) {
        FS2MetaSnapshot metaSnapshot;
        try {
            metaSnapshot = FS2.fetchObject(new URI(messageUri));
        } catch (FS2Exception | URISyntaxException e) {
            throw new RuntimeException("Failed to get the meta data.", e);
        }
        return metaSnapshot.getPayloadSize();
    }
    
    /**
     * 
     * @param payload
     * @param workTicket
     * @param httpListenerProperties
     * @return
     */
    public static FS2MetaSnapshot persistPayload(ByteArrayOutputStream payload, WorkTicket workTicket, Map<String, String> httpListenerProperties) {

        try {

            FS2ObjectHeaders fs2Header = constructFS2Headers(workTicket, httpListenerProperties);
            String uri = FS2_URI_MBX_PAYLOAD + workTicket.getGlobalProcessId() + "." + UUIDGen.getCustomUUID();

            // persists the message in spectrum.
            LOGGER.debug("Persist the payload **");
            URI requestUri = createPayloadURI(uri, true, httpListenerProperties.get(MailBoxConstants.STORAGE_IDENTIFIER_TYPE));

            // fetch the metdata includes payload size            
            FS2MetaSnapshot metaSnapshot;
            OutputStream persistedOutputStream = null;

            try {

                metaSnapshot = FS2.createObjectEntry(requestUri, generateFS2Options(workTicket), fs2Header, null);
                persistedOutputStream = FS2.getFS2PayloadOutputStream(requestUri, false);
                persistedOutputStream.write(payload.toByteArray());
            } finally {
                if (persistedOutputStream != null) {
                    persistedOutputStream.close();
                }
                if (payload != null) {
                    payload.close();
                }
            }

            LOGGER.debug("Successfully persist the payload in fs2 storage to url {} ", requestUri);
            return metaSnapshot;

        } catch (FS2ObjectAlreadyExistsException e) {
            LOGGER.error(Messages.PAYLOAD_ALREADY_EXISTS.value(), e);
            throw new MailBoxServicesException(Messages.PAYLOAD_ALREADY_EXISTS, Response.Status.CONFLICT);

        } catch (FS2Exception | IOException e) {
            LOGGER.error(Messages.PAYLOAD_PERSIST_ERROR.value(), e);
            throw new MailBoxServicesException(Messages.PAYLOAD_PERSIST_ERROR, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     *
     * @param payload
     * @param workTicket
     * @param httpListenerProperties
     * @return
     */
    public static FS2MetaSnapshot persistPayload(InputStream payload, WorkTicket workTicket, Map<String, String> httpListenerProperties) {

        try {

            FS2ObjectHeaders fs2Header = constructFS2Headers(workTicket, httpListenerProperties);
            String uri = FS2_URI_MBX_PAYLOAD + workTicket.getGlobalProcessId() + "." + UUIDGen.getCustomUUID();

            // persists the message in spectrum.
            LOGGER.debug("Persist the payload **");
            URI requestUri = createPayloadURI(uri, true, httpListenerProperties.get(MailBoxConstants.STORAGE_IDENTIFIER_TYPE));

            // fetch the metdata includes payload size
            FS2MetaSnapshot metaSnapshot;
            try {
                metaSnapshot = FS2.createObjectEntry(requestUri, generateFS2Options(workTicket), fs2Header, payload);
            } finally {
                if (payload != null) {
                    payload.close();
                }
            }

            LOGGER.debug("Successfully persist the payload in fs2 storage to url {} ", requestUri);
            return metaSnapshot;

        } catch (FS2ObjectAlreadyExistsException e) {
            LOGGER.error(Messages.PAYLOAD_ALREADY_EXISTS.value(), e);
            throw new MailBoxServicesException(Messages.PAYLOAD_ALREADY_EXISTS, Response.Status.CONFLICT);

        } catch (FS2Exception | IOException e) {
            LOGGER.error(Messages.PAYLOAD_PERSIST_ERROR.value(), e);
            throw new MailBoxServicesException(Messages.PAYLOAD_PERSIST_ERROR, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }
}
