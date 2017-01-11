/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.util;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.gem.service.client.GEMHelper;
import com.liaison.gem.service.client.GEMManifestResponse;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.service.core.MailBoxConfigurationService;

/**
 * This class is used to access service broker entities
 * @author ofs
 *
 */
public class ServiceBrokerUtil {
    
    private static final Logger LOG = LogManager.getLogger(MailBoxConfigurationService.class);

    /**
     * This method is used to retrieve entities from service broker
     * 
     * @param type
     * @param pguid
     * @return
     */
    public static String getEntity(String type , String pguid) {

        try {

            String sbBasUrl = MailBoxUtil.getEnvironmentProperties().getString(MailBoxConstants.SERVICE_BROKER_BASE_URL);           
            String url = sbBasUrl + "read/edm/" + type + "/" + pguid;
            GEMManifestResponse gemManifestFromGEM = GEMHelper.getACLManifest();
            Map<String, String> headerMap = GEMHelper.getRequestHeaders(gemManifestFromGEM, "application/json");
            return HTTPClientUtil.getHTTPResponseInString(LOG, url, headerMap);

        } catch (Exception e) {
            throw new RuntimeException(MailBoxConstants.MAILBOX + "Client HTTP GET request failed, " + e.getMessage());
        }     
    }
}
