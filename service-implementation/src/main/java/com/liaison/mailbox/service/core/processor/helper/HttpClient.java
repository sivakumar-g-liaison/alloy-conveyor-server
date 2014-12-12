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
import com.liaison.commons.util.client.http.HTTPRequest;
import com.liaison.commons.util.client.http.authentication.BasicAuthenticationHandler;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.dtdm.model.Credential;
import com.liaison.mailbox.enums.CredentialType;
import com.liaison.mailbox.service.core.processor.AbstractProcessor;
import com.liaison.mailbox.service.dto.configuration.request.HttpOtherRequestHeaderDTO;
import com.liaison.mailbox.service.dto.configuration.request.RemoteProcessorPropertiesDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.util.KMSUtil;
import com.liaison.mailbox.service.util.MailBoxUtil;

/**
 * @author VNagarajan
 *
 * Helper class to construct http request
 */
public class HttpClient {

	private static final Logger LOGGER = LogManager.getLogger(HttpClient.class);

	/**
	 * @param processor
	 * @return
	 */
	public static Object getClient(AbstractProcessor processor) {

		try {

			LOGGER.info("Started injecting HTTP/S configurations to HTTPClient");

			// Create HTTPRequest and set the properties
			HTTPRequest request = new HTTPRequest(null);
			request.setLogger(LOGGER);

			// Convert the json string to DTO
			RemoteProcessorPropertiesDTO properties = processor.getProperties();

			// Set url to HTTPRequest
			request.setUrl(properties.getUrl());

			// Set configurations
			request.setVersion(properties.getHttpVersion());
			request.setMethod(properties.getHttpVerb());
			request.setNumberOfRetries(properties.getRetryAttempts());
			request.setConnectionTimeout(properties.getConnectionTimeout());
			request.setChunkedEncoding(properties.isChunkedEncoding());

			if (properties.getSocketTimeout() > 0) {
				request.setSocketTimeout(properties.getSocketTimeout());
			}
			if (properties.getPort() > 0) {
				request.setPort(properties.getPort());
			}

			// Set the Other header to HttpRequest
			if (properties.getOtherRequestHeader() != null) {
				for (HttpOtherRequestHeaderDTO header : properties.getOtherRequestHeader()) {
					request.addHeader(header.getName(), header.getValue());
				}
			}

			// Set the content type header to HttpRequest
			if (!MailBoxUtil.isEmpty(properties.getContentType())) {
				request.addHeader("Content-Type", properties.getContentType());
			}

			// Set the basic auth header for http request
			Credential loginCredential = processor.getCredentialOfSpecificType(CredentialType.LOGIN_CREDENTIAL);

			if ((loginCredential != null)
					&& !MailBoxUtil.isEmpty(loginCredential.getCredsUsername())
					&& !MailBoxUtil.isEmpty(loginCredential.getCredsPassword())) {
				String passwordFromKMS = KMSUtil.getSecretFromKMS(loginCredential.getCredsPassword());
				request.setAuthenticationHandler(new BasicAuthenticationHandler(loginCredential.getCredsUsername(), passwordFromKMS));
			}

			// Configure keystore for HTTPS request
			if (processor.getConfigurationInstance().getProcsrProtocol().equalsIgnoreCase("https")) {

				KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
				Credential trustStoreCredential = processor.getCredentialOfSpecificType(CredentialType.TRUSTSTORE_CERT);

				if (trustStoreCredential != null) {

					// If no certificate is configured then use default global trustoreid
					String trustStoreID = (MailBoxUtil.isEmpty(trustStoreCredential.getCredsIdpUri()))
							? (MailBoxUtil.getEnvironmentProperties().getString(MailBoxConstants.DEFAULT_GLOBAL_TRUSTSTORE_GROUP_ID))
							: trustStoreCredential.getCredsIdpUri();

					try (InputStream instream = KMSUtil.fetchTrustStore(trustStoreID)) {

						//if (instream == null) { //TODO Veera:do we need this check???
						//	throw new MailBoxServicesException(Messages.CERTIFICATE_RETRIEVE_FAILED, Response.Status.BAD_REQUEST);
						//}
						trustStore.load(instream, null);
					}

					request.truststore(trustStore);
				}

			}
			LOGGER.info("Returns HTTP/S configured HTTPClient");
			return request;

		} catch (MailBoxConfigurationServicesException | JAXBException | IOException | LiaisonException
				| MailBoxServicesException | SymmetricAlgorithmException | UnrecoverableKeyException
				| OperatorCreationException | KeyStoreException | NoSuchAlgorithmException
				| JsonParseException | CMSException | BootstrapingFailedException | CertificateException | JSONException e) {
			throw new RuntimeException(e);
		}
	}
}
