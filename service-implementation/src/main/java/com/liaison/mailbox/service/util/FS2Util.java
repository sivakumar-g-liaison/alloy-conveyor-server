/**
 * Copyright 2014 Liaison Technologies, Inc.
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.util;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.commons.util.settings.LiaisonConfigurationFactory;
import com.liaison.framework.fs2.KeyManagerKEKProvider;
import com.liaison.framework.fs2.PayloadEncryptionProvider;
import com.liaison.fs2.api.CoreFS2Utils;
import com.liaison.fs2.api.FS2Factory;
import com.liaison.fs2.api.FS2MetaSnapshot;
import com.liaison.fs2.api.FS2ObjectHeaders;
import com.liaison.fs2.api.FlexibleStorageSystem;
import com.liaison.fs2.api.encryption.FS2EncryptionProvider;
import com.liaison.fs2.api.encryption.FS2KEKProvider;
import com.liaison.fs2.api.exceptions.FS2Exception;
import com.liaison.fs2.api.exceptions.FS2PayloadNotFoundException;
import com.liaison.fs2.storage.spectrum.SpectrumConfigBuilder;
import com.liaison.fs2.storage.spectrum.SpectrumStorageConfig;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.service.exception.MailBoxServicesException;


public class FS2Util {

	private static final Logger LOGGER = LogManager.getLogger(FS2Util.class);
	private static FlexibleStorageSystem FS2 = null;
	private static FlexibleStorageSystem FS2WithoutEncryption = null;
	public static boolean isEncryptionRequired = true;

	public static InputStream retrievePayloadFromSpectrum(String payloadURL) throws MailBoxServicesException  {

		InputStream payload = null; //For storing payload

		try {
			
			URI spectrumURI = new URI(payloadURL);
			if (isEncryptionRequired) {
				getFS2Instance();
				LOGGER.info("Retrieving payload from spectrum");
				payload = FS2.getFS2PayloadInputStream(spectrumURI);
			} else {
				getFS2InstanceWithoutEncryption();
				LOGGER.info("Retrieving payload without encryption from spectrum");
				payload = FS2WithoutEncryption.getFS2PayloadInputStream(spectrumURI);
			}
			
			//payload = new FileInputStream("D:\\opt\\addmailbox.txt");
		} catch (FS2PayloadNotFoundException | URISyntaxException  e) {
			LOGGER.error("Failed to retrieve payload from spectrum due to error", e);
			throw new MailBoxServicesException("Failed to retrieve payload from spectrum due to error"+e.getMessage(), Response.Status.BAD_REQUEST);
		}
		return payload;
	}
	
	public static FS2MetaSnapshot persistPayloadInSpectrum(InputStream payload, String globalProcessId, FS2ObjectHeaders fs2Headers) {
		
		FS2MetaSnapshot snapshot = null;
		try {
			
			//persists the message in spectrum.
			String path = MailBoxConstants.SPECTRUM_PAYLOAD_PREFIX + globalProcessId;
			if (isEncryptionRequired) {
				getFS2Instance();
				snapshot = FS2.createObjectEntry(CoreFS2Utils.genURIFromPath("sfs2",path.toString()), fs2Headers, payload);
			} else {
				getFS2InstanceWithoutEncryption();
				snapshot = FS2WithoutEncryption.createObjectEntry(CoreFS2Utils.genURIFromPath("fs2",path.toString()), fs2Headers, payload);
			}			
			LOGGER.info("FS2 payload path {}" + path);

			// retrieve payload from spectrum for testing
			/*URI uri = snapshot.getURI();
			InputStream retrievedPayload = (isEncryptionRequired) ? FS2.getFS2PayloadInputStream(uri): FS2WithoutEncryption.getFS2PayloadInputStream(uri);
			LOGGER.debug("payload retrieved is {}", retrievedPayload);*/
			LOGGER.debug("Successfully wrote the payload in spectrum to url {}.", snapshot.getURI());
		} catch (FS2Exception  e) {
			e.printStackTrace();
			LOGGER.error("Failed to write payload to spectrum due to error", e);
			throw new MailBoxServicesException("Failed to write payload to spectrum due to error"+e.getMessage(), Response.Status.BAD_REQUEST);
		}		
		return snapshot;
	}

	 /**
	 * Method which creates an FS2 instance
	 *
	 */
	public static synchronized void getFS2Instance() {

		if (FS2 == null) {
			
			FS2EncryptionProvider encryptionProvider = new PayloadEncryptionProvider();
			FS2KEKProvider kekProvider = new KeyManagerKEKProvider();
			FS2 = FS2Factory.newInstance(new SpectrumStorageConfig(SpectrumConfigBuilder.buildFromConfiguration(LiaisonConfigurationFactory.getConfiguration()), encryptionProvider, kekProvider));
		}
	 }
	
	public static synchronized void getFS2InstanceWithoutEncryption() {

		if (FS2WithoutEncryption == null) {
			FS2WithoutEncryption = FS2Factory.newInstance(new SpectrumStorageConfig(SpectrumConfigBuilder.buildFromConfiguration(LiaisonConfigurationFactory.getConfiguration()), null, null));
		}
	 }

}
