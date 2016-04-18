/**
 * Copyright 2014 Liaison Technologies, Inc.
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.storage.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.commons.jaxb.JAXBUtility;
import com.liaison.commons.util.settings.DecryptableConfiguration;
import com.liaison.dto.queue.WorkTicket;
import com.liaison.fs2.api.FS2Configuration;
import com.liaison.fs2.api.FS2Factory;
import com.liaison.fs2.api.FS2ObjectHeaders;
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

/**
 * @author VNagarajan
 * 
 *         This utility class persists and retrieves the payload from various file systems. Currently there
 *         spectrum(secure/unsecure), boss(secure/unsecure) and flat file system.
 * 
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
	public static final String PROPERTY_LOCATION_DEFAULT = "fs2.storage.location.default";

	/**
	 * Constants used to read file system values
	 */
	public static final String PROPERTY_FS2_STORAGE_FILE_DEFAULT_USE = "fs2.storage.file.default.use";
	public static final String PROPERTY_FS2_STORAGE_FILE_DEFAULT_TYPE = "fs2.storage.file.default.type";
	public static final String PROPERTY_FS2_STORAGE_FILE_DEFAULT_LOCATION = "fs2.storage.file.default.location";
	public static final String PROPERTY_FS2_STORAGE_FILE_DEFAULT_MOUNT = "fs2.storage.file.default.mount";

	/**
	 * Moniker to identify secure and unsecure
	 */
	public static final String BOSS_MONIKER_PREFIX = "boss-";
	public static final String SECURE_MONIKER = "secure";
	public static final String UNSECURE_MONIKER = "unsecure";

	/**
	 * FS2 URI path for mailbox. There is no difference between sync and async
	 */
	public static final String FS2_URI_MBX_PAYLOAD = "/mailbox/payload/1.0/";

	private static FlexibleStorageSystem FS2 = null;
	private static List<FS2Configuration> filesystemConfigs;

	public static final String GLOBAL_PROCESS_ID_HEADER = "GLOBAL_PROCESS_ID";

	/**
	 * Initialize FS2
	 */
	static {

		LOGGER.debug("Initializing FS2");
		configureFilesystem();

		FS2 = FS2Factory.newInstance(configuration,
                (storageId) -> {
                    if(storageId.getStorageType().equals(SECURE_MONIKER)) {
                        return new SimpleEncryptionProvider();
                    }
                    
                    return null;
                },
                (storageId) -> {
                    if(storageId.getStorageType().equals(SECURE_MONIKER)) {
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
	 * @param payloadURL
	 * @return
	 * @throws MailBoxServicesException
	 */
	public static InputStream retrievePayload(String payloadURL)
			throws MailBoxServicesException {

		try {
			URI payloadURI = new URI(payloadURL);
			LOGGER.debug("Retrieving payload from spectrum");
			return FS2.getFS2PayloadInputStream(payloadURI);
		} catch (FS2PayloadNotFoundException | URISyntaxException e) {
			LOGGER.error(Messages.PAYLOAD_READ_ERROR.value(), e);
			throw new MailBoxServicesException(Messages.PAYLOAD_READ_ERROR, Response.Status.BAD_REQUEST);
		}
	}

	/**
	 * A helper method to persist the payload into spectrum(secure/unsecure)/boss(secure/unsecure) and file system.
	 * 
	 * @param payload
	 * @param globalProcessId
	 * @param fs2Headers
	 * @param isSecure
	 * @return
	 */
	public static FS2MetaSnapshot persistPayload(InputStream payload, WorkTicket workTicket,
			Map<String, String> httpListenerProperties, boolean isDropbox) {

		try {

			String tenancyKey = httpListenerProperties.get(MailBoxConstants.KEY_TENANCY_KEY);
			String loginId = httpListenerProperties.get(MailBoxConstants.LOGIN_ID);
			String globalProcessorId = workTicket.getGlobalProcessId();
			String localProcessorId = workTicket.getGlobalProcessId();
			String messageName = workTicket.getFileName() + MailBoxUtil.getGUID();

			boolean isSecure = Boolean.parseBoolean((String) httpListenerProperties.get(MailBoxConstants.PROPERTY_HTTPLISTENER_SECUREDPAYLOAD));

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
			URI requestUri = createPayloadURI(uri, isSecure);

			// fetch the metdata includes payload size
			FS2MetaSnapshot metaSnapshot = null;
			try (InputStream is = payload) {
				metaSnapshot = FS2.createObjectEntry(requestUri, fs2Header, is);
				LOGGER.debug("Time spent on uploading file {} of size {} to spectrum only is {} ms",
						workTicket.getFileName(), metaSnapshot.getPayloadSize(), endTime - startTime);
			}

			LOGGER.debug("Successfully persist the payload in spectrum to url {} ", requestUri);
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
	 * @param payload
	 * @param globalProcessId
	 * @param fs2Headers
	 * @param isSecure
	 * @return
	 */
	public static FS2MetaSnapshot persistWorkTicket(WorkTicket workTicket, Map<String, String> properties) {

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
			URI requestUri = createPayloadURI(uri, false);

			// fetch the metdata includes payload size
			FS2MetaSnapshot metaSnapshot = null;
			try (InputStream is = new ByteArrayInputStream(JAXBUtility.marshalToJSON(workTicket).getBytes())) {
				metaSnapshot = FS2.createObjectEntry(requestUri, fs2Header, is);
				LOGGER.debug("Time spent on uploading workticket {} of size {} to spectrum only is {} ms",
						workTicket.getFileName(), metaSnapshot.getPayloadSize(), endTime - startTime);
			}

			LOGGER.debug("Successfully persisted the workticket in spectrum to url {} ", requestUri);
			return metaSnapshot;

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
	 * @param path
	 * @param secure
	 * @return
	 */
	public static URI createPayloadURI(String path, boolean secure) {

		URI uri = null;
		boolean defaultFileuse = configuration.getBoolean(PROPERTY_FS2_STORAGE_FILE_DEFAULT_USE, false);

		if (defaultFileuse) {
			String defaultType = configuration.getString(PROPERTY_FS2_STORAGE_FILE_DEFAULT_TYPE, "file");
			String defaultFileLocation = configuration.getString(PROPERTY_FS2_STORAGE_FILE_DEFAULT_LOCATION, "local");
			uri = createPayloadURI(path, defaultType, defaultFileLocation);
			return uri;
		}

		String defaultLocation = configuration.getString(PROPERTY_LOCATION_DEFAULT);
		if (secure) {
			uri = createPayloadURI(path, SECURE_MONIKER, defaultLocation);
		} else {
			uri = createPayloadURI(path, UNSECURE_MONIKER, defaultLocation);
		}

		return uri;
	}

	/**
	 * A helper method to automatically create a spectrum URI in the format needed. Throws an exception if the type and
	 * location don't match a configured value.
	 * 
	 * @param path
	 * @param secure
	 * @return
	 */
	public static URI createPayloadURI(String path, String type, String location) {

		URI uri = null;

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
	protected static void configureFilesystem() {

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
	 * @param request
	 * @param workTicket
	 * @throws IOException
	 */
	public static void storePayload(InputStream stream, WorkTicket workTicket,
			Map<String, String> httpListenerProperties, boolean isDropbox)
			throws Exception {

		try (InputStream payloadToPersist = stream) {

			FS2MetaSnapshot metaSnapshot = StorageUtilities.persistPayload(payloadToPersist, workTicket,
					httpListenerProperties, isDropbox);
			LOGGER.info("Payload URL for the file {} is {}. File size in bytes {} ", workTicket.getFileName(),
					metaSnapshot.getURI().toString(), metaSnapshot.getPayloadSize());

			workTicket.setPayloadSize(metaSnapshot.getPayloadSize());
			workTicket.setPayloadURI(metaSnapshot.getURI().toString());
		}
	}

	/**
	 * Method to construct FS2ObjectHeaders from the given workTicket
	 * 
	 * @param workTicket
	 * @return FS2ObjectHeaders
	 * @throws IOException
	 * @throws MailBoxServicesException
	 */
	public static FS2ObjectHeaders constructFS2Headers(WorkTicket workTicket, Map<String, String> httpListenerProperties) {

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
        if (workTicket.getTtlDays() != -1) {
            Integer ttlValue = MailBoxUtil.convertTTLIntoSeconds(MailBoxConstants.TTL_UNIT_DAYS,
                    workTicket.getTtlDays());
            fs2Header.addHeader(FlexibleStorageSystem.OPTION_TTL, ttlValue.toString());
        }
        LOGGER.debug("FS2 Headers set are {}", fs2Header.getHeaders());

		return fs2Header;
	}

	/**
	 * A helper method to retrieve the payload headers.
	 *
	 * @param payloadURL Payload URL
	 * @return FS2ObjectHeaders
	 * @throws MailBoxServicesException
	 */
	public static FS2ObjectHeaders retrievePayloadHeaders(String payloadURL)
			throws MailBoxServicesException {

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
	 * Method to check whether the payload exists at given path
	 * 
	 * @param path - path of payload 
	 * @return true if payload exists otherwise false
	 * @throws FS2Exception
	 * @throws URISyntaxException
	 */
	public static boolean isPayloadExists(String path) {
		
		try {
			return FS2.exists(new URI(path));
		} catch (URISyntaxException | FS2Exception e) {
			LOGGER.error(Messages.PAYLOAD_DOES_NOT_EXIST, e);
			throw new MailBoxServicesException(Messages.PAYLOAD_DOES_NOT_EXIST, Response.Status.BAD_REQUEST);
		}
	}
}
