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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.service.core.MailBoxConfigurationService;

public class ServiceBrokerUtil {
    
    private static final Logger LOG = LogManager.getLogger(MailBoxConfigurationService.class);
    
    public static String getEntity(String type , String pguid) {
        
        try {
            String sbBasUrl = MailBoxUtil.getEnvironmentProperties().getString(MailBoxConstants.SERVICE_BROKER_BASE_URL);           
            String url = sbBasUrl + "read/edm/" + type + "/" + pguid;
            return HTTPClientUtil.getHTTPResponseInString(LOG, url, null);
            
        } catch (Exception e) {
            throw new RuntimeException(MailBoxConstants.MAILBOX + "Client HTTP GET request failed, " + e.getMessage());
        }
               
    }

}
