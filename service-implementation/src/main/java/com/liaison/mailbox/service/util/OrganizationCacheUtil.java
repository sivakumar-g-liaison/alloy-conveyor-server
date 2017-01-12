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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.liaison.commons.util.settings.DecryptableConfiguration;
import com.liaison.mailbox.service.dto.OrganizationDTO;
import com.liaison.metrics.cache.CacheStatsRegistrar;

public class OrganizationCacheUtil {
	
    private static final Logger logger = LogManager.getLogger(OrganizationCacheUtil.class);

	
	public static final String PROPERTY_NAME_MAX_CACHE_SIZE = "com.liaison.organization.cache.max.size";
    public static final String PROPERTY_NAME_CACHE_TTL = "com.liaison.organization.cache.expire.timeout";
    public static final String PROPERTY_NAME_CACHE_TTL_UNIT = "com.liaison.organization.cache.expire.timeunit";
    
	//Cache for organization
	private static LoadingCache<String, OrganizationDTO> organizationCache;

	//Script cache settings
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

        organizationCache =  CacheBuilder.newBuilder()
	        .maximumSize(maxCacheSize)
	        .expireAfterWrite(cacheTimeToLive, cacheTimeToLiveUnit)
	        .recordStats()
	        .build(new CacheLoader<String, OrganizationDTO>() {
        			public OrganizationDTO load(String pipelineId) throws MalformedURLException, IOException, URISyntaxException {
        			    return ServiceBrokerUtil.getOrganizationDetails(pipelineId);
        			}
	            }
        	 );
        
      //Register Organization Cache
      CacheStatsRegistrar.register("cache-organization", organizationCache);
	}

	
    /**
     * Helper method construct the organization by pipeline.
     *
     * This is especially for service broker
     *
     * @param loginId loginId
     * @return OrganizationDTO
     */
     public static OrganizationDTO getOrganizationByPipelineId(String pipelineId) {
    	 
    	logger.debug("retrieving organization by given id"); 
    	try {
            return organizationCache.get(pipelineId);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
