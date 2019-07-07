/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.mailbox.service.core.processor.helper;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.xml.bind.JAXBException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonParseException;
import com.liaison.commons.exception.LiaisonException;
import com.liaison.commons.security.pkcs7.SymmetricAlgorithmException;
import com.liaison.commons.util.client.ftps.G2FTPSClient;
import com.liaison.mailbox.dtdm.model.Credential;
import com.liaison.mailbox.enums.CredentialType;
import com.liaison.mailbox.enums.ProcessorType;
import com.liaison.mailbox.service.core.processor.AbstractProcessor;
import com.liaison.mailbox.service.dto.configuration.processor.properties.FTPDownloaderPropertiesDTO;
import com.liaison.mailbox.service.dto.configuration.processor.properties.FTPUploaderPropertiesDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.util.KMSUtil;
import com.liaison.mailbox.service.util.MailBoxUtil;

/**
 * Helper class to construct FTPS request.
 * 
 * @author OFS
 */
public class FTPSClient {

	private static final Logger LOGGER = LogManager.getLogger(FTPSClient.class);

	/**
	 * @param processor
	 * @return
	 */
	public static Object getClient(AbstractProcessor processor) {

		try {

			// Convert the json string to DTO
			FTPUploaderPropertiesDTO ftpUploaderStaticProperties = null;
			FTPDownloaderPropertiesDTO ftpDownloaderStaticProperties = null;
			String url = null;
			int connectionTimeout = 0;
			int socketTimeout = 0;
			int retryAttempts = 0;
			int retryInterval = 0;
			boolean debugTranscript = false;

			if (processor.getConfigurationInstance().getProcessorType().equals(ProcessorType.REMOTEUPLOADER)) {
				ftpUploaderStaticProperties = (FTPUploaderPropertiesDTO)processor.getProperties();
				url = ftpUploaderStaticProperties.getUrl();
				connectionTimeout = ftpUploaderStaticProperties.getConnectionTimeout();
				socketTimeout = ftpUploaderStaticProperties.getSocketTimeout();
				retryAttempts = ftpUploaderStaticProperties.getRetryAttempts();
				debugTranscript =  ftpUploaderStaticProperties.isDebugTranscript();
				retryInterval  = ftpUploaderStaticProperties.getRetryInterval();
			} else if (processor.getConfigurationInstance().getProcessorType().equals(ProcessorType.REMOTEDOWNLOADER)) {
				ftpDownloaderStaticProperties = (FTPDownloaderPropertiesDTO)processor.getProperties();
				url = ftpDownloaderStaticProperties.getUrl();
				connectionTimeout = ftpDownloaderStaticProperties.getConnectionTimeout();
				socketTimeout = ftpDownloaderStaticProperties.getSocketTimeout();
				retryAttempts = ftpDownloaderStaticProperties.getRetryAttempts();
				debugTranscript = ftpDownloaderStaticProperties.isDebugTranscript();
				retryInterval  = ftpDownloaderStaticProperties.getRetryInterval();
			}
			// retrieve required properties
			G2FTPSClient ftpsRequest = new G2FTPSClient();
			ftpsRequest.setURI(url);

			// set debug transcript property
			ftpsRequest.setCanLogTranscript(debugTranscript);
			ftpsRequest.setDiagnosticLogger(LOGGER);
			ftpsRequest.setCommandLogger(LOGGER);
			ftpsRequest.setConnectionTimeout(connectionTimeout);

			ftpsRequest.setSocketTimeout(socketTimeout);
			ftpsRequest.setRetryCount(retryAttempts);
			ftpsRequest.setRetryInterval(retryInterval);

			Credential loginCredential = processor.getCredentialOfSpecificType(CredentialType.LOGIN_CREDENTIAL);

			/*
			 * For FTPS, SFTP, and FTP processors credential password will be getting from KM
			 */
			if ((loginCredential != null)) {

				String passwordFromKMS = KMSUtil.getSecretFromKMS(loginCredential.getCredsPassword());

				if (!MailBoxUtil.isEmpty(loginCredential.getCredsUsername())
						&& !MailBoxUtil.isEmpty(passwordFromKMS)) {
					ftpsRequest.setUser(loginCredential.getCredsUsername());
					ftpsRequest.setPassword(passwordFromKMS);
				}
			}

			// Configure keystore for HTTPS request
			if (processor.getConfigurationInstance().getProcsrProtocol().equalsIgnoreCase("ftps")) {

				KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
				Credential trustStoreCredential = processor.getCredentialOfSpecificType(CredentialType.TRUSTSTORE_CERT);

				if (trustStoreCredential != null) {

					// If no certificate is configured then use default global trustoreid
					String trustStoreID = trustStoreCredential.getCredsIdpUri();

					try (InputStream instream = KMSUtil.fetchTrustStore(trustStoreID)) {
						trustStore.load(instream, null);
					}

					ftpsRequest.setTrustStore(trustStore);
				}

			}
			return ftpsRequest;

		} catch (MailBoxConfigurationServicesException | JAXBException | IOException | LiaisonException
				| MailBoxServicesException | CertificateException
				| KeyStoreException | NoSuchAlgorithmException | JsonParseException
				| IllegalAccessException e) {
			throw new RuntimeException(e);
		}

	}

}