/**
 * Copyright 2014 Liaison Technologies, Inc.
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.storage.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.core.Response;

import org.apache.commons.io.input.CountingInputStream;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.commons.util.settings.DecryptableConfiguration;
import com.liaison.fs2.api.FS2Configuration;
import com.liaison.fs2.api.FS2Factory;
import com.liaison.fs2.api.FS2MetaSnapshot;
import com.liaison.fs2.api.FS2ObjectHeaders;
import com.liaison.fs2.api.FS2StorageIdentifier;
import com.liaison.fs2.api.FlexibleStorageSystem;
import com.liaison.fs2.api.encryption.FS2EncryptionProvider;
import com.liaison.fs2.api.encryption.FS2KEKProvider;
import com.liaison.fs2.api.encryption.impl.KeyManagerKEKProvider;
import com.liaison.fs2.api.encryption.impl.SimpleEncryptionProvider;
import com.liaison.fs2.api.exceptions.FS2Exception;
import com.liaison.fs2.api.exceptions.FS2PayloadNotFoundException;
import com.liaison.fs2.storage.file.FS2DefaultFileConfig;
import com.liaison.fs2.storage.spectrum.FS2DefaultSpectrumStorageConfig;
import com.liaison.fs2.storage.spectrum.SpectrumConfigBuilder;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.util.MailBoxUtil;


/**
 * @author VNagarajan
 *
 * This utility class persists and retrieves the payload from various file systems.
 * Currently there spectrum(secure/unsecure) and flat file system.
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
	public static final String PROPERTY_SPECTRUM_TYPES = "fs2.storage.spectrum.types";
	public static final String PROPERTY_SPECTRUM_LOCATIONS = "fs2.storage.spectrum.locations";
	public static final String PROPERTY_SPECTRUM_LOCATION_DEFAULT = "fs2.storage.spectrum.location.default";

	/**
	 * Constants used to read file system values
	 */
	public static final String PROPERTY_FS2_STORAGE_FILE_DEFAULT_USE = "fs2.storage.file.default.use";
	public static final String PROPERTY_FS2_STORAGE_FILE_DEFAULT_TYPE = "fs2.storage.file.default.type";
	public static final String PROPERTY_FS2_STORAGE_FILE_DEFAULT_LOCATION = "fs2.storage.file.default.location";
	public static final String PROPERTY_FS2_STORAGE_FILE_DEFAULT_MOUNT = "fs2.storage.file.default.mount";
	public static final String DEFAULT_VALUE_FOR_FILE_LOCATION = "local";
	public static final String DEFAULT_VALUE_FOR_FILE_TYPE = "file";

	/**
	 * Moniker to identify secure and unsecure
	 */
	public static final String SECURE_MONIKER = "secure";
	public static final String UNSECURE_MONIKER = "unsecure";

	/**
	 * FS2 URI path for mailbox. There is no difference between sync and async
	 */
	public static final String FS2_URI_MBX_PAYLOAD = "/mailbox/payload/1.0/";

	private static FlexibleStorageSystem FS2 = null;

	private static FS2Configuration[] spectrumConfigs;
	private static FS2Configuration[] filesystemConfigs;

	static {
		configureSpectrum();
		configureFilesystem();
		FS2 = FS2Factory.newInstance(ArrayUtils.addAll(spectrumConfigs, filesystemConfigs));
	}

	/**
	 * A helper method to retrieve the payload from spectrum(secure/unsecure) or file system.
	 *
	 * @param payloadURL
	 * @return
	 * @throws MailBoxServicesException
	 */
	public static InputStream retrievePayload(String payloadURL) throws MailBoxServicesException  {

		try {
			URI spectrumURI = new URI(payloadURL);
			LOGGER.info("Retrieving payload from spectrum");
			return FS2.getFS2PayloadInputStream(spectrumURI);
		} catch (FS2PayloadNotFoundException | URISyntaxException  e) {
			LOGGER.error("Failed to retrieve payload from spectrum due to error", e);
			throw new MailBoxServicesException("Failed to retrieve payload from spectrum due to error"+e.getMessage(), Response.Status.BAD_REQUEST);
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
	public static PayloadDetail persistPayload(InputStream payload, String globalProcessId, FS2ObjectHeaders fs2Headers, boolean isSecure) {

		try {

			//persists the message in spectrum.
			LOGGER.debug("Persist the payload **");
			URI requestUri = createSpectrumURI(FS2_URI_MBX_PAYLOAD + globalProcessId, isSecure);
			FS2MetaSnapshot metaSnapshot = FS2.createObjectEntry(requestUri, fs2Headers, payload);

			//fetch the metdata includes payload size
			PayloadDetail detail = null;
			try (CountingInputStream inputStream = new CountingInputStream(payload)) {

				detail = new PayloadDetail();
				FS2.writePayloadFromStream(metaSnapshot.getURI(), inputStream);
				detail.setMetaSnapshot(metaSnapshot);
				detail.setPayloadSize(inputStream.getCount());
			}
			LOGGER.debug("Successfully persist the payload in spectrum to url {} ", requestUri);
			return detail;
		} catch (FS2Exception | IOException e) {
			LOGGER.error("Failed to persist the payload in spectrum due to error", e);
			throw new MailBoxServicesException("Failed to write payload in spectrum : "+ e.getMessage(), Response.Status.INTERNAL_SERVER_ERROR);
		}

	}

	/**
	 * A helper method to automatically create a spectrum URI in the format
	 * needed to determine the secure spectrum or the unsecure spectrum at the
	 * default location.
	 *
	 * @param path
	 * @param secure
	 * @return
	 */
	public static URI createSpectrumURI(String path, boolean secure) {

		URI uri = null;
		boolean defaultFileuse = configuration.getBoolean(PROPERTY_FS2_STORAGE_FILE_DEFAULT_USE, false);

		if (defaultFileuse) {
			String defaultFileLocation = configuration.getString(PROPERTY_FS2_STORAGE_FILE_DEFAULT_LOCATION, DEFAULT_VALUE_FOR_FILE_LOCATION);
			String defaultFileType = configuration.getString(PROPERTY_FS2_STORAGE_FILE_DEFAULT_TYPE, DEFAULT_VALUE_FOR_FILE_TYPE);
			uri = createSpectrumURI(path, defaultFileType, defaultFileLocation);
			return uri;
		}

		String defaultLocation = configuration.getString(PROPERTY_SPECTRUM_LOCATION_DEFAULT);
		if(secure) {
			uri = createSpectrumURI(path, SECURE_MONIKER, defaultLocation);
		} else {
			uri = createSpectrumURI(path, UNSECURE_MONIKER, defaultLocation);
		}

		return uri;
	}

	/**
	 * A helper method to automatically create a spectrum URI in the format
	 * needed.  Throws an exception if the type and location don't
	 * match a configured value.
	 *
	 * @param path
	 * @param secure
	 * @return
	 */
	public static URI createSpectrumURI(String path, String type, String location) {

		URI uri = null;

		// We need to ensure the path starts with a path or a format
		// exception is thrown.
		if(!path.startsWith("/")) {
			path = "/" + path;
		}

		String authority = type + "@" + location;
		boolean noMatch = true;

		for (FS2Configuration fs2Config : getConfigs()) {
			FS2StorageIdentifier id = fs2Config.getStorageIdentifier();
			if (id.getStorageType().equals(type) && id.getStorageLocation().equals(location)) {
				noMatch = false;
			}
		}

		if (noMatch) {
			throw new MailBoxServicesException(String.format("Unable to create a URI because no spectrum configuration"
					+ " exists for type [%s] and location [%s].", type, location), Response.Status.PRECONDITION_FAILED);
		}


		try {
			uri = new URI("fs2", authority, path, null, null);
		} catch (URISyntaxException e) {
			throw new MailBoxServicesException(e.getMessage(), Response.Status.INTERNAL_SERVER_ERROR);
		}
		return uri;
	}


	/**
	 * Configures specturm(secure/unsecure) providers
	 */
	protected static void configureSpectrum() {

        FS2EncryptionProvider encryptionProvider = new SimpleEncryptionProvider();
        FS2KEKProvider kekProvider = new KeyManagerKEKProvider();

		String[] spectrumTypes = configuration.getStringArray(PROPERTY_SPECTRUM_TYPES);
		String[] spectrumLocations = configuration.getStringArray(PROPERTY_SPECTRUM_LOCATIONS);

		if (spectrumTypes.length == 0 || spectrumLocations.length == 0) {
			throw new RuntimeException("There must be at least one spectrum type and one spectrum location configured.");
		}

		spectrumConfigs = new FS2Configuration[spectrumTypes.length * spectrumLocations.length];

		int i = 0;

		for (String type : spectrumTypes) {
			for(String location : spectrumLocations) {

				String identifier = type + "." + location;
				FS2Configuration config;

				if (type.equals(SECURE_MONIKER)) {
					config = new FS2DefaultSpectrumStorageConfig(
			        		new FS2StorageIdentifier(type, location), SpectrumConfigBuilder.buildFromConfiguration(
			        				identifier, configuration), encryptionProvider, kekProvider) {
					    @Override
					    public boolean doCalcPayloadSize() {
					    	return false;
					    }
					};
				} else {
					config = new FS2DefaultSpectrumStorageConfig(
		        		new FS2StorageIdentifier(type, location), SpectrumConfigBuilder.buildFromConfiguration(
		        				identifier, configuration), null, null) {
						@Override
					    public boolean doCalcPayloadSize() {
							return false;
					    }
					};
				}
				spectrumConfigs[i] = config;
				i++;
			}
		}
	}

	/**
	 * configures file system providers
	 */
	protected static void configureFilesystem() {

		final String defaultType = configuration.getString(PROPERTY_FS2_STORAGE_FILE_DEFAULT_TYPE, "file");
		final String defaultLocation = configuration.getString(PROPERTY_FS2_STORAGE_FILE_DEFAULT_LOCATION, "local");
        final String defaultFileMountPoint = configuration.getString(PROPERTY_FS2_STORAGE_FILE_DEFAULT_MOUNT, "/tmp");

        filesystemConfigs = new FS2Configuration[1];
		filesystemConfigs[0] = new FS2DefaultFileConfig(new FS2StorageIdentifier(defaultType, defaultLocation)) {

            @Override
            public boolean shouldDeleteExistingData() {
                return false;
            }

            @Override
            public String getMountPoint() {
                return defaultFileMountPoint;
            }
        };

	}

	public static FS2Configuration[] getConfigs() {
		return ArrayUtils.addAll(spectrumConfigs, filesystemConfigs);
	}

}