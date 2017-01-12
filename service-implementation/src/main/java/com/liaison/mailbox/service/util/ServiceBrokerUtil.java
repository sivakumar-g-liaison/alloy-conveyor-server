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

import java.io.IOException;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.liaison.gem.service.client.GEMHelper;
import com.liaison.gem.service.client.GEMManifestResponse;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.service.dto.OrganizationDTO;

/**
 * This class is used to access service broker entities
 */
public class ServiceBrokerUtil {

    private static final Logger LOG = LogManager.getLogger(ServiceBrokerUtil.class);

    /**
     * This method is used to retrieve entities from service broker
     * 
     * @param type service broker entity type
     * @param pguid pguid of the entity
     * @return
     */
    public static String getEntity(String type, String pguid) {

        try {

            String sbBasUrl = MailBoxUtil.getEnvironmentProperties().getString(MailBoxConstants.SERVICE_BROKER_BASE_URL);
            String url = sbBasUrl + "read/edm/" + type + "/" + pguid;
            GEMManifestResponse gemManifestFromGEM = GEMHelper.getACLManifest();
            Map<String, String> headerMap = GEMHelper.getRequestHeaders(gemManifestFromGEM, "application/json");
            return HTTPClientUtil.getHTTPResponseInString(LOG, url, headerMap);

        } catch (Exception e) {
            throw new RuntimeException(MailBoxConstants.MAILBOX + " Client HTTP GET request failed, " + e.getMessage());
        }
    }

    /**
     * Method to get organization details using pipeline id
     * 
     * @param pipelineId
     * @return
     */
    public static OrganizationDTO getOrganizationDetails(String pipelineId) {
        String response = getEntity("Pileline", pipelineId);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode responseNode;
        try {
            responseNode = mapper.readTree(response);
            JsonNode dtoNode = responseNode.get("dataTransferObject").get("process").get("orgEntity");
            String id = dtoNode.get("pguid").textValue();
            String name = dtoNode.get("name").textValue();
            return new OrganizationDTO(id, name);
        } catch (IOException e) {
            throw new RuntimeException(MailBoxConstants.MAILBOX + " Failed to get organization details, " + e.getMessage());
        }
    }
}
