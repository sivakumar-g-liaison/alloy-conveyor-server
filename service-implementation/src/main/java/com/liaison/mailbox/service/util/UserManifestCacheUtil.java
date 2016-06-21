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
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.liaison.commons.acl.manifest.dto.RoleBasedAccessControl;
import com.liaison.commons.util.settings.DecryptableConfiguration;
import com.liaison.commons.util.settings.LiaisonConfigurationFactory;
import com.liaison.gem.service.client.GEMHelper;
import com.liaison.gem.service.client.GEMManifestResponse;
import com.liaison.metrics.cache.CacheStatsRegistrar;

public class UserManifestCacheUtil {
	
    private static final Logger logger = LogManager.getLogger(UserManifestCacheUtil.class);

	
	public static final String PROPERTY_NAME_MAX_CACHE_SIZE = "com.liaison.manifest.cache.max.size";
    public static final String PROPERTY_NAME_CACHE_TTL = "com.liaison.manifest.cache.expire.timeout";
    public static final String PROPERTY_NAME_CACHE_TTL_UNIT = "com.liaison.manifest.cache.expire.timeunit";
    
	//Cache for manifest resource
	private static LoadingCache<String, GEMManifestResponse> manifestCache;

	//Script cache settings
    private static int maxCacheSize;
    private static long cacheTimeToLive;
    private static TimeUnit cacheTimeToLiveUnit;
    
    /**
     * Initialize the manifest cache
     */
	static {

        DecryptableConfiguration serviceConfig = LiaisonConfigurationFactory.getConfiguration();
        maxCacheSize = serviceConfig.getInt(PROPERTY_NAME_MAX_CACHE_SIZE, 100);
        cacheTimeToLive = serviceConfig.getLong(PROPERTY_NAME_CACHE_TTL, 5L);
        cacheTimeToLiveUnit = TimeUnit.valueOf(serviceConfig.getString(PROPERTY_NAME_CACHE_TTL_UNIT, "MINUTES"));

        manifestCache =  CacheBuilder.newBuilder()
	        .maximumSize(maxCacheSize)
	        .expireAfterWrite(cacheTimeToLive, cacheTimeToLiveUnit)
	        .recordStats()
	        .build(new CacheLoader<String, GEMManifestResponse>() {
        			public GEMManifestResponse load(String loginId) throws MalformedURLException, IOException, URISyntaxException {
        				
        			    try {
        				    return GEMHelper.getACLManifestByloginId(loginId, null);
        				} catch (Exception e) {
        					//retry after the first failure
        				    return GEMHelper.getACLManifestByloginId(loginId, null);
						}
        			}
	            }
        	 );
        
      //Register Manifst Cache
      CacheStatsRegistrar.register("manifest-cache", manifestCache);
	}

	
    /**
     * Helper method construct the manifest by Login Id and sign also invokes the GEM for manifest.
     *
     * This is especially for service broker
     *
     * @param loginId loginId
     * @return GEMManifestResponse
     */
     public static GEMManifestResponse getACLManifestByloginId(String loginId) {
    	 
    	logger.debug("retrieving manifest by given login id"); 
    	try {
            return manifestCache.get(loginId);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
     
     /**
      * Method to get all rbacs from acl manifest Json
      *
      * @param String - aclManifestJson
      * @return list of rbacs
      * @throws IOException
      */

     public static List <RoleBasedAccessControl> getDomainsFromACLManifest(String aclManifestJson) throws IOException {
    	 
     	logger.debug("retrieving domains from given manifest json"); 
		return GEMHelper.getDomainsFromACLManifest(aclManifestJson);
     } 
}
