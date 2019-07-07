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
import com.liaison.commons.util.client.http.HTTPRequest;
import com.liaison.commons.util.client.http.authentication.BasicAuthenticationHandler;
import com.liaison.mailbox.dtdm.model.Credential;
import com.liaison.mailbox.enums.CredentialType;
import com.liaison.mailbox.enums.ProcessorType;
import com.liaison.mailbox.service.core.processor.AbstractProcessor;
import com.liaison.mailbox.service.dto.configuration.processor.properties.HTTPDownloaderPropertiesDTO;
import com.liaison.mailbox.service.dto.configuration.processor.properties.HTTPUploaderPropertiesDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.util.KMSUtil;
import com.liaison.mailbox.service.util.MailBoxUtil;

/**
 * @author VNagarajan
 * 
 *         Helper class to construct http request
 */
public class HTTPClient {

	private static final Logger LOGGER = LogManager.getLogger(HTTPClient.class);

	/**
	 * @param processor
	 * @return
	 */
	public static Object getClient(AbstractProcessor processor) {

		try {

			LOGGER.debug("Started injecting HTTP/S configurations to HTTPClient");

			// Create HTTPRequest and set the properties
			HTTPRequest request = new HTTPRequest(null);
			request.setLogger(LOGGER);

			// Convert the json string to DTO
			HTTPUploaderPropertiesDTO httpUploaderStaticProperties = null;
			HTTPDownloaderPropertiesDTO httpDownloaderStaticProperties = null;
			String url = null;
			int connectionTimeout = 0;
			int socketTimeout = 0;
			int retryAttempts = 0;
			String otherRequestHeaders = null;
			boolean chunkedEncoding = false;
			String httpVerb = null;
			String httpVersion = null;
			String contentType = null;
			int port = 0;

			if (processor.getConfigurationInstance().getProcessorType().equals(ProcessorType.REMOTEUPLOADER)) {
				httpUploaderStaticProperties = (HTTPUploaderPropertiesDTO) processor.getProperties();
				url = httpUploaderStaticProperties.getUrl();
				connectionTimeout = httpUploaderStaticProperties.getConnectionTimeout();
				socketTimeout = httpUploaderStaticProperties.getSocketTimeout();
				retryAttempts = httpUploaderStaticProperties.getRetryAttempts();
				otherRequestHeaders = httpUploaderStaticProperties.getOtherRequestHeader();
				chunkedEncoding = httpUploaderStaticProperties.isChunkedEncoding();
				httpVerb = httpUploaderStaticProperties.getHttpVerb();
				httpVersion = httpUploaderStaticProperties.getHttpVersion();
				contentType = httpUploaderStaticProperties.getContentType();
				port = httpUploaderStaticProperties.getPort();
			} else if (processor.getConfigurationInstance().getProcessorType().equals(ProcessorType.REMOTEDOWNLOADER)) {
				httpDownloaderStaticProperties = (HTTPDownloaderPropertiesDTO) processor.getProperties();
				url = httpDownloaderStaticProperties.getUrl();
				connectionTimeout = httpDownloaderStaticProperties.getConnectionTimeout();
				socketTimeout = httpDownloaderStaticProperties.getSocketTimeout();
				retryAttempts = httpDownloaderStaticProperties.getRetryAttempts();
				otherRequestHeaders = httpDownloaderStaticProperties.getOtherRequestHeader();
				chunkedEncoding = httpDownloaderStaticProperties.isChunkedEncoding();
				httpVerb = httpDownloaderStaticProperties.getHttpVerb();
				httpVersion = httpDownloaderStaticProperties.getHttpVersion();
				port = httpDownloaderStaticProperties.getPort();
			}

			// Set url to HTTPRequest
			request.setUrl(url);

			// Set configurations
			request.setVersion(httpVersion);
			request.setMethod(httpVerb);
			request.setNumberOfRetries(retryAttempts);
			request.setConnectionTimeout(connectionTimeout);
			request.setChunkedEncoding(chunkedEncoding);

			if (socketTimeout > 0) {
				request.setSocketTimeout(socketTimeout);
			}
			if (port > 0) {
				request.setPort(port);
			}

			// Set the Other header to HttpRequest
			if (otherRequestHeaders != null) {
				for (String s : otherRequestHeaders.split(",")) {
					String headers[] = s.split(":");
					if (headers.length == 2)
						request.addHeader(headers[0], headers[1]);
				}
			}

			// Set the content type header to HttpRequest
			if (!MailBoxUtil.isEmpty(contentType)) {
				request.addHeader("Content-Type", contentType);
			}

			// Set the basic auth header for http request
			Credential loginCredential = processor.getCredentialOfSpecificType(CredentialType.LOGIN_CREDENTIAL);

			if ((loginCredential != null) && !MailBoxUtil.isEmpty(loginCredential.getCredsUsername())
					&& !MailBoxUtil.isEmpty(loginCredential.getCredsPassword())) {
				String passwordFromKMS = KMSUtil.getSecretFromKMS(loginCredential.getCredsPassword());
				request.setAuthenticationHandler(new BasicAuthenticationHandler(loginCredential.getCredsUsername(),
						passwordFromKMS));
			}

			// Configure keystore for HTTPS request
			if (processor.getConfigurationInstance().getProcsrProtocol().equalsIgnoreCase("https")) {

				KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
				Credential trustStoreCredential = processor.getCredentialOfSpecificType(CredentialType.TRUSTSTORE_CERT);

				if (trustStoreCredential != null) {

					// If no certificate is configured then use default global trustoreid
					String trustStoreID = trustStoreCredential.getCredsIdpUri();
					try (InputStream instream = KMSUtil.fetchTrustStore(trustStoreID)) {
						trustStore.load(instream, null);
					}

					request.truststore(trustStore);
				}

			}
			LOGGER.debug("Returns HTTP/S configured HTTPClient");
			return request;

		} catch (MailBoxConfigurationServicesException | JAXBException | IOException | LiaisonException
				| MailBoxServicesException | KeyStoreException | NoSuchAlgorithmException | JsonParseException
				| CertificateException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
}