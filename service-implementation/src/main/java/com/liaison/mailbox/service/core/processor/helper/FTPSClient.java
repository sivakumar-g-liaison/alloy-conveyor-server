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
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.xml.bind.JAXBException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.operator.OperatorCreationException;
import org.codehaus.jettison.json.JSONException;

import com.google.gson.JsonParseException;
import com.liaison.commons.exception.BootstrapingFailedException;
import com.liaison.commons.exception.LiaisonException;
import com.liaison.commons.security.pkcs7.SymmetricAlgorithmException;
import com.liaison.commons.util.client.ftps.G2FTPSClient;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.dtdm.model.Credential;
import com.liaison.mailbox.enums.CredentialType;
import com.liaison.mailbox.enums.ProcessorType;
import com.liaison.mailbox.service.core.processor.AbstractProcessor;
import com.liaison.mailbox.service.dto.configuration.processor.properties.FTPDownloaderPropertiesDTO;
import com.liaison.mailbox.service.dto.configuration.processor.properties.FTPUploaderPropertiesDTO;
import com.liaison.mailbox.service.dto.configuration.processor.properties.SFTPDownloaderPropertiesDTO;
import com.liaison.mailbox.service.dto.configuration.processor.properties.SFTPUploaderPropertiesDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.util.KMSUtil;
import com.liaison.mailbox.service.util.MailBoxUtil;

/**
 * @author VNagarajan
 *
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
			
			if (processor.getConfigurationInstance().getProcessorType().equals(ProcessorType.REMOTEUPLOADER)) {
				ftpUploaderStaticProperties = (FTPUploaderPropertiesDTO)processor.getProperties();
				url = ftpUploaderStaticProperties.getUrl();
				connectionTimeout = ftpUploaderStaticProperties.getConnectionTimeout();
				socketTimeout = ftpUploaderStaticProperties.getSocketTimeout();
				retryAttempts = ftpUploaderStaticProperties.getRetryAttempts();
			} else if (processor.getConfigurationInstance().getProcessorType().equals(ProcessorType.REMOTEDOWNLOADER)) {
				ftpDownloaderStaticProperties = (FTPDownloaderPropertiesDTO)processor.getProperties();
				url = ftpDownloaderStaticProperties.getUrl();
				connectionTimeout = ftpDownloaderStaticProperties.getConnectionTimeout();
				socketTimeout = ftpDownloaderStaticProperties.getSocketTimeout();
				retryAttempts = ftpDownloaderStaticProperties.getRetryAttempts();
			}
			// retrieve required properties
			/*ArrayList<String> propertyNames = new ArrayList<String>();
			propertyNames.add(MailBoxConstants.PROPERTY_URL);
			propertyNames.add(MailBoxConstants.PROPERTY_CONNECTION_TIMEOUT);
			propertyNames.add(MailBoxConstants.PROPERTY_SOCKET_TIMEOUT);
			propertyNames.add(MailBoxConstants.PROPERTY_RETRY_ATTEMPTS);
			Map<String, String> requiredProperties = ProcessorPropertyJsonMapper.getProcessorProperties(properties, propertyNames);*/

			G2FTPSClient ftpsRequest = new G2FTPSClient();
			ftpsRequest.setURI(url);
			ftpsRequest.setDiagnosticLogger(LOGGER);
			ftpsRequest.setCommandLogger(LOGGER);
			ftpsRequest.setConnectionTimeout(connectionTimeout);

			ftpsRequest.setSocketTimeout(socketTimeout);
			ftpsRequest.setRetryCount(retryAttempts);

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
					String trustStoreID = (MailBoxUtil.isEmpty(trustStoreCredential.getCredsIdpUri()))
							? (MailBoxUtil.getEnvironmentProperties().getString(MailBoxConstants.DEFAULT_GLOBAL_TRUSTSTORE_GROUP_ID))
							: trustStoreCredential.getCredsIdpUri();

					try (InputStream instream = KMSUtil.fetchTrustStore(trustStoreID)) {
						trustStore.load(instream, null);
					}

					ftpsRequest.setTrustStore(trustStore);
				}

			}
			return ftpsRequest;

		} catch (MailBoxConfigurationServicesException | JAXBException | IOException | LiaisonException
				| MailBoxServicesException | SymmetricAlgorithmException | CertificateException
				| UnrecoverableKeyException | OperatorCreationException | KeyStoreException
				| NoSuchAlgorithmException | JsonParseException | CMSException
				| BootstrapingFailedException | JSONException | IllegalAccessException | NoSuchFieldException e) {
			throw new RuntimeException(e);
		}

	}

}
