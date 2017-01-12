/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.util;

import com.liaison.gem.service.client.GEMHelper;
import com.liaison.gem.service.client.GEMManifestResponse;
import com.liaison.mailbox.MailBoxConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ws.rs.core.UriBuilder;
import java.util.Map;

import static com.liaison.mailbox.MailBoxConstants.SERVICE_BROKER_BASE_URL;

/**
 * This class is used to access service broker entities
 */
public class ServiceBrokerUtil {

    private static final String READ_EDM = "read/edm";
    private static final Logger LOG = LogManager.getLogger(ServiceBrokerUtil.class);

    /**
     * This method is used to retrieve entities from service broker
     *
     * @param type  service broker entity type
     * @param pguid pguid of the entity
     * @return entity
     */
    public static String getEntity(String type, String pguid) {

        try {

            //url construction to fetch the fallback keys
            String sbBasUrl = MailBoxUtil.getEnvironmentProperties().getString(SERVICE_BROKER_BASE_URL);
            UriBuilder uri = UriBuilder.fromUri(sbBasUrl);
            uri.path(READ_EDM).path(type).path(pguid);

            GEMManifestResponse gemManifestFromGEM = GEMHelper.getACLManifest();
            Map<String, String> headerMap = GEMHelper.getRequestHeaders(gemManifestFromGEM, "application/json");
            return HTTPClientUtil.getHTTPResponseInString(LOG, uri.build().toString(), headerMap);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
