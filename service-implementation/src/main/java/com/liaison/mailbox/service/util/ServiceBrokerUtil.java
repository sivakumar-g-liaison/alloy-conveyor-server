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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.liaison.commons.util.settings.DecryptableConfiguration;
import com.liaison.gem.service.client.GEMHelper;
import com.liaison.gem.service.client.GEMManifestResponse;
import com.liaison.gem.service.dto.OrganizationDTO;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.metrics.cache.CacheStatsRegistrar;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static com.liaison.mailbox.MailBoxConstants.PIPELINE;
import static com.liaison.mailbox.MailBoxConstants.SERVICE_BROKER_BASE_URL;

/**
 * This class is used to access service broker entities
 */
public class ServiceBrokerUtil {

    private static final String DATA_TRANSFER_OBJECT = "dataTransferObject";
    private static final Logger LOG = LogManager.getLogger(ServiceBrokerUtil.class);
    private static final String NAME = "name";
    private static final String ORG_ENTITY = "orgEntity";
    private static final String PGUID = "pguid";
    private static final String PROCESS = "process";
    private static final String READ_EDM = "read/edm";

    /**
     * cache properties
     */
    private static final String PROPERTY_NAME_MAX_CACHE_SIZE = "com.liaison.organization.cache.max.size";
    private static final String PROPERTY_NAME_CACHE_TTL = "com.liaison.organization.cache.expire.timeout";
    private static final String PROPERTY_NAME_CACHE_TTL_UNIT = "com.liaison.organization.cache.expire.timeunit";

    //Cache for organization
    private static LoadingCache<String, OrganizationDTO> organizationCache;

    //Organization cache settings
    private static int maxCacheSize;
    private static long cacheTimeToLive;
    private static TimeUnit cacheTimeToLiveUnit;

    /**
     * Initialize the organization cache
     */
    static {

        DecryptableConfiguration envConfig = MailBoxUtil.getEnvironmentProperties();
        maxCacheSize = envConfig.getInt(PROPERTY_NAME_MAX_CACHE_SIZE, 100);
        cacheTimeToLive = envConfig.getLong(PROPERTY_NAME_CACHE_TTL, 15L);
        cacheTimeToLiveUnit = TimeUnit.valueOf(envConfig.getString(PROPERTY_NAME_CACHE_TTL_UNIT, "MINUTES"));

        organizationCache = CacheBuilder.newBuilder()
                .maximumSize(maxCacheSize)
                .expireAfterWrite(cacheTimeToLive, cacheTimeToLiveUnit)
                .recordStats()
                .build(new CacheLoader<String, OrganizationDTO>() {
                           public OrganizationDTO load(String pipelineId) throws IOException {
                               return getOrganizationDetails(pipelineId);
                           }
                       }
                );

        //Register Organization Cache
        CacheStatsRegistrar.register("cache-organization", organizationCache);
    }

    /**
     * Fetch org details from SB by using pipelineId
     *
     * @param pipelineId pipeline id to fetch the org details
     * @return OrganizationDTO organization dto contains name and pguid
     */
    public static OrganizationDTO getOrganizationByPipelineId(String pipelineId) {

        LOG.debug("retrieving organization by given pipeline id");
        try {
            return organizationCache.get(pipelineId);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

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

    /**
     * Method to get organization details using pipeline id
     *
     * @param pipelineId pipeline id configured in the processor
     * @return organization dto
     */
    public static OrganizationDTO getOrganizationDetails(String pipelineId) {

        String response = getEntity(PIPELINE, pipelineId);

        try {

            ObjectMapper mapper = new ObjectMapper();
            JsonNode responseNode = mapper.readTree(response);
            JsonNode dtoNode = responseNode.get(DATA_TRANSFER_OBJECT).get(PROCESS).get(ORG_ENTITY);
            String id = dtoNode.get(PGUID).textValue();
            String name = dtoNode.get(NAME).textValue();

            OrganizationDTO org = new OrganizationDTO();
            org.setPguid(id);
            org.setName(name);
            return org;
        } catch (IOException e) {
            throw new RuntimeException(MailBoxConstants.MAILBOX + " Failed to get organization details, " + e.getMessage());

        }
    }

}
