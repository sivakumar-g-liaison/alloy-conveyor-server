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
import java.util.ArrayList;
import java.util.Map;

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
import com.liaison.mailbox.service.dto.configuration.processor.properties.ProcessorPropertiesDefinitionDTO;
import com.liaison.mailbox.service.dto.configuration.request.HttpOtherRequestHeaderDTO;
import com.liaison.mailbox.service.dto.configuration.request.RemoteProcessorPropertiesDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.util.KMSUtil;
import com.liaison.mailbox.service.util.MailBoxUtil;
import com.liaison.mailbox.service.util.ProcessorPropertyJsonMapper;

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
			ProcessorPropertiesDefinitionDTO properties = processor.getProperties();
			
			// retrieve required properties
			ArrayList<String> propertyNames = new ArrayList<String>();
			propertyNames.add(MailBoxConstants.PROPERTY_URL);
			propertyNames.add(MailBoxConstants.PROPERTY_PORT);
			propertyNames.add(MailBoxConstants.PROPERTY_CONNECTION_TIMEOUT);
			propertyNames.add(MailBoxConstants.PROPERTY_SOCKET_TIMEOUT);
			propertyNames.add(MailBoxConstants.PROPERTY_RETRY_ATTEMPTS);
			propertyNames.add(MailBoxConstants.PROPERTY_OTHER_REQUEST_HEADERS);
			propertyNames.add(MailBoxConstants.PROPERTY_CHUNKED_ENCODING);
			propertyNames.add(MailBoxConstants.PROPERTY_HTTP_VERB);
			propertyNames.add(MailBoxConstants.PROPERTY_HTTP_VERSION);
			propertyNames.add(MailBoxConstants.PROPERTY_CONTENT_TYPE);
			Map<String, String> requiredProperties = ProcessorPropertyJsonMapper.getProcessorProperties(properties, propertyNames);

			// Set url to HTTPRequest
			request.setUrl(requiredProperties.get(MailBoxConstants.PROPERTY_URL));

			// Set configurations
			request.setVersion(requiredProperties.get(MailBoxConstants.PROPERTY_HTTP_VERSION));
			request.setMethod(requiredProperties.get(MailBoxConstants.PROPERTY_HTTP_VERB));
			request.setNumberOfRetries(Integer.valueOf(requiredProperties.get(MailBoxConstants.PROPERTY_RETRY_ATTEMPTS)).intValue());
			request.setConnectionTimeout(Integer.valueOf(requiredProperties.get(MailBoxConstants.PROPERTY_CONNECTION_TIMEOUT)).intValue());
			request.setChunkedEncoding(Boolean.valueOf(requiredProperties.get(MailBoxConstants.PROPERTY_CHUNKED_ENCODING)).booleanValue());
			
			int socketTimeout = Integer.valueOf(requiredProperties.get(MailBoxConstants.PROPERTY_SOCKET_TIMEOUT)).intValue();
			if (socketTimeout > 0) {
				request.setSocketTimeout(socketTimeout);
			}
			int port = Integer.valueOf(requiredProperties.get(MailBoxConstants.PROPERTY_PORT)).intValue();
			if (port > 0) {
				request.setPort(port);
			}

			String otherRequestHeaders = requiredProperties.get(MailBoxConstants.PROPERTY_OTHER_REQUEST_HEADERS);
			// Set the Other header to HttpRequest
			if (otherRequestHeaders != null) {
				for (String s : otherRequestHeaders.split(",") ) {
					String headers [] = s.split(":");
					if(headers.length == 2)request.addHeader(headers[0], headers[1]);
				}
			}
			
			String contentType = requiredProperties.get(MailBoxConstants.PROPERTY_CONTENT_TYPE);
			// Set the content type header to HttpRequest
			if (!MailBoxUtil.isEmpty(contentType)) {
				request.addHeader("Content-Type", contentType);
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
				| JsonParseException | CMSException | BootstrapingFailedException | CertificateException 
				| JSONException | IllegalAccessException | NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}
}
