/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.integration.test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.Test;

import com.liaison.commons.util.StreamUtil;
import com.liaison.dto.queue.WorkTicket;
import com.liaison.fs2.api.FS2ObjectHeaders;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.service.storage.util.PayloadDetail;
import com.liaison.mailbox.service.storage.util.StorageUtilities;

/**
 *
 * @author OFS
 *
 */
public class StorageUtilitiesTest {

	private static final Logger logger = LogManager.getLogger(StorageUtilitiesTest.class);

	@Test
	public void testWriteAndReadUnsecurePayload() throws IOException {

		System.setProperty("archaius.deployment.applicationId", "g2mailboxservice");
		System.setProperty("archaius.deployment.environment", "test");

		String exampleString = "This is the sample string";
		InputStream stream = new ByteArrayInputStream(exampleString.getBytes(StandardCharsets.UTF_8));

		//Dummy headers
		long globalProcessId = System.currentTimeMillis();
		FS2ObjectHeaders fs2Header = new FS2ObjectHeaders();
		fs2Header.addHeader(MailBoxConstants.KEY_GLOBAL_PROCESS_ID, String.valueOf(globalProcessId));
		logger.debug("FS2 Headers set are {}", fs2Header.getHeaders());

		WorkTicket wTicket = new WorkTicket();
		wTicket.setGlobalProcessId(String.valueOf(globalProcessId));
		Map <String, String>properties = new HashMap <String, String>();
		properties.put(MailBoxConstants.PROPERTY_HTTPLISTENER_SECUREDPAYLOAD, String.valueOf(true));

		PayloadDetail detail = StorageUtilities.persistPayload(stream, wTicket, properties, false);

		try (InputStream is = StorageUtilities.retrievePayload(detail.getMetaSnapshot().getURI().toString())) {

			String paylaod = new String(StreamUtil.streamToBytes(is));
			logger.info("The received payload is \"{}\"", paylaod);
		}

	}

	//@Test
	public void readAPalyload() throws Exception {

		System.setProperty("archaius.deployment.applicationId","g2mailboxservice");
		System.setProperty("archaius.deployment.environment", "test");
		String payloadURI = "fs2://unsecure@dev-int/SERVICE_BROKER/dropboxadmin@liaison.dev/B4535568ACA745E59C984169B5B5C5E0.B4535568ACA745E59C984169B5B5C5E0_35010B085CE74611B35964598D028ADD";
		try (InputStream is = StorageUtilities.retrievePayload(payloadURI)) {

			try (OutputStream outputStream = new FileOutputStream(new File("spectrumPayload.txt"))) {
				int read = 0;
				byte[] bytes = new byte[1024];
				System.out.println("Started writing!");
				while ((read = is.read(bytes)) != -1) {
					outputStream.write(bytes, 0, read);

				}
			}

		}

		System.out.println("Done writing!");

	}

	@Test(enabled=false)
	public void testWriteAndReadSecurePayload() throws IOException {

		System.setProperty("archaius.deployment.applicationId", "g2mailboxservice");
		System.setProperty("archaius.deployment.environment", "test");

		String exampleString = "This is the sample string";
		InputStream stream = new ByteArrayInputStream(exampleString.getBytes(StandardCharsets.UTF_8));

		//Dummy headers
		long globalProcessId = System.currentTimeMillis();
		FS2ObjectHeaders fs2Header = new FS2ObjectHeaders();
		fs2Header.addHeader(MailBoxConstants.KEY_GLOBAL_PROCESS_ID, String.valueOf(globalProcessId));
		logger.debug("FS2 Headers set are {}", fs2Header.getHeaders());

		WorkTicket wTicket = new WorkTicket();
		wTicket.setGlobalProcessId(String.valueOf(globalProcessId));
		Map <String, String>properties = new HashMap <String, String>();
		properties.put(MailBoxConstants.PROPERTY_HTTPLISTENER_SECUREDPAYLOAD, String.valueOf(true));

		PayloadDetail detail = StorageUtilities.persistPayload(stream, wTicket, properties, false);
		try (InputStream is = StorageUtilities.retrievePayload(detail.getMetaSnapshot().getURI().toString())) {

			String paylaod = new String(StreamUtil.streamToBytes(is));
			logger.info("The received payload is \"{}\"", paylaod);
		}

	}

	@Test(enabled = false)
	public void testWriteAndReadSecurePayload_LocalPayload() throws IOException {

		System.setProperty("archaius.deployment.applicationId", "g2mailboxservice");
		System.setProperty("archaius.deployment.environment", "test");

		File file = new File("\\opt\\100mbfile.txt");
		InputStream stream = new FileInputStream(file);

		//Dummy headers
		long globalProcessId = System.currentTimeMillis();
		FS2ObjectHeaders fs2Header = new FS2ObjectHeaders();
		fs2Header.addHeader(MailBoxConstants.KEY_GLOBAL_PROCESS_ID, String.valueOf(globalProcessId));
		logger.debug("FS2 Headers set are {}", fs2Header.getHeaders());

		WorkTicket wTicket = new WorkTicket();
		wTicket.setGlobalProcessId(String.valueOf(globalProcessId));
		Map <String, String>properties = new HashMap <String, String>();
		properties.put(MailBoxConstants.PROPERTY_HTTPLISTENER_SECUREDPAYLOAD, String.valueOf(false));

		PayloadDetail detail = StorageUtilities.persistPayload(stream, wTicket, properties, false);
		System.out.println(detail.getMetaSnapshot().getURI());

	}

}
