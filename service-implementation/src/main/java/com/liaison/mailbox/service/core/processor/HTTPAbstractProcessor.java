/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.core.processor;

import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.commons.util.settings.DecryptableConfiguration;
import com.liaison.commons.util.settings.LiaisonConfigurationFactory;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.enums.ProcessorType;
import com.liaison.mailbox.service.core.ProcessorConfigurationService;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.rest.HTTPListenerResource;
import com.liaison.usermanagement.service.client.UserManagementClient;

/**
 * Base class that provides implementation common to both sync and async processors.
 *
 * @author OFS
 */
public abstract class HTTPAbstractProcessor {

	private static final Logger logger = LogManager.getLogger(HTTPListenerResource.class);

	private static final String CONFIGURATION_MAX_REQUEST_SIZE = "com.liaison.servicebroker.sync.max.request.size";
	public static final String AUTHENTICATION_HEADER_PREFIX = "Basic ";

	/**
	 * This method will validate the size of the request.
	 *
	 * @param contentLength the content length
	 */
	public  void validateRequestSize(long contentLength) {
		DecryptableConfiguration config = LiaisonConfigurationFactory.getConfiguration();
		int maxRequestSize = config.getInt(CONFIGURATION_MAX_REQUEST_SIZE);

		if (contentLength > maxRequestSize) {
			throw new RuntimeException("Request has content length of " + contentLength
					+ " which exceeds the configured maximum size of " + maxRequestSize);
		}
	}

	public void authenticateRequestor(String[] authenticationCredentials) {

        if (authenticationCredentials.length == 2) {

        	String loginId = authenticationCredentials[0];
        	// encode the password using base64 bcoz UM will expect a base64
            // encoded token
            String token = new String(Base64.encodeBase64(authenticationCredentials[1].getBytes()));
            // if both username and password is present call UM client to
            // authenticate
        	UserManagementClient UMClient = new UserManagementClient();
        	UMClient.addAccount(UserManagementClient.TYPE_NAME_PASSWORD, loginId, token);
        	UMClient.authenticate();
        	if (!UMClient.isSuccessful()) {
        		throw new RuntimeException(UMClient.getMessage());
        	}
        } else {
        	throw new RuntimeException("Authorization Header does not contain UserName and Password");
        }

	}

    public static String[] getAuthenticationCredentials(String basicAuthenticationHeader) {

        // trim the prefix basic and get the username:password part
        basicAuthenticationHeader = basicAuthenticationHeader.replaceFirst(AUTHENTICATION_HEADER_PREFIX, "");
        // decode the string to get username and password
        String authenticationDetails = new String(Base64.decodeBase64(basicAuthenticationHeader));
        String[] authenticationCredentials = authenticationDetails.split(":");
        return authenticationCredentials;
    }

	/**
	 * Method to retrieve http listener properties of processor of specific type
	 * by given mailboxGuid
	 *
	 * @param mailboxInfo
	 *            it can be either mailbox Pguid or mailbox Name
	 * @param isMailboxIdAvailable
	 *            boolean specify if mailboxId presents
	 * @return
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 * @throws MailBoxConfigurationServicesException
	 */
	public  Map<String, String> retrieveHttpListenerProperties(String mailboxInfo, ProcessorType processorType, boolean isMailboxIdAvailable)
			throws MailBoxConfigurationServicesException, NoSuchFieldException, SecurityException,
			IllegalArgumentException, IllegalAccessException {

        logger.debug("retrieving the properties configured in httplistener of mailbox {}", mailboxInfo);
		ProcessorConfigurationService procsrService = new ProcessorConfigurationService();
		return procsrService.getHttpListenerProperties(mailboxInfo, processorType, isMailboxIdAvailable);

	}

	/**
	 * Method to retrieve the mailbox from the pguid and return the value of
	 * HTTPListener propery "Http Listner Auth Check Required "
	 *
	 * @param httpListenerProperties
	 * @return
	 * @throws Exception
	 */
	public  boolean isAuthenticationCheckRequired(Map<String, String> httpListenerProperties) {

		boolean isAuthCheckRequired = true;
		isAuthCheckRequired = Boolean
				.parseBoolean(httpListenerProperties.get(MailBoxConstants.PROPERTY_HTTPLISTENER_AUTH_CHECK));
        logger.debug("Property httplistenerauthcheckrequired is configured in the mailbox and set to be {}",
				isAuthCheckRequired);
		return isAuthCheckRequired;
	}


}
