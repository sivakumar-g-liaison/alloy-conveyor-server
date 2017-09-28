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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.liaison.commons.util.settings.DecryptableConfiguration;
import com.liaison.commons.util.settings.LiaisonArchaiusConfiguration;
import com.liaison.mailbox.service.core.processor.HTTPAbstractProcessor;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.metrics.cache.CacheStatsRegistrar;
import com.liaison.usermanagement.service.client.UserManagementClient;
import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ws.rs.core.Response;
import java.util.concurrent.TimeUnit;

/**
 * Class that authenticate the user by using the authentication cache.
 * 
 * @author OFS
 */
public class UserAuthCacheUtil {

	private static final Logger logger = LogManager.getLogger(UserAuthCacheUtil.class);
	
	public static final String PROPERTY_NAME_MAX_CACHE_SIZE = "com.liaison.authenticate.cache.max.size";
    public static final String PROPERTY_NAME_CACHE_TTL = "com.liaison.authenticate.cache.expire.timeout";
    public static final String PROPERTY_NAME_CACHE_TTL_UNIT = "com.liaison.authenticate.cache.expire.timeunit";
    public static final String PROPERTY_NAME_CACHE_CONCURRENCY_LEVEL = "com.liaison.authenticate.cache.concurrency.level";
    public static final String INVALID_AUTHORIZATION_HEADER = "Invalid Authorization Header";
    
    //Cache for authentication resource
    private static LoadingCache<String, String> authenticationCache;
	
    //cache settings
    private static int maxCacheSize;
    private static long cacheTimeToLive;
    private static int concurrencyLevel;
    private static TimeUnit cacheTimeToLiveUnit;
    
    /**
     * Initialize the authentication cache
     */
    static {
    	
        DecryptableConfiguration serviceConfig = LiaisonArchaiusConfiguration.getInstance();
        maxCacheSize = serviceConfig.getInt(PROPERTY_NAME_MAX_CACHE_SIZE, 100);
        cacheTimeToLive = serviceConfig.getLong(PROPERTY_NAME_CACHE_TTL, 1L);
        cacheTimeToLiveUnit = TimeUnit.valueOf(serviceConfig.getString(PROPERTY_NAME_CACHE_TTL_UNIT, "MINUTES"));
        concurrencyLevel = serviceConfig.getInt(PROPERTY_NAME_CACHE_CONCURRENCY_LEVEL, 50);
        
        authenticationCache =  CacheBuilder.newBuilder()
    	        .maximumSize(maxCacheSize)
    	        .expireAfterWrite(cacheTimeToLive, cacheTimeToLiveUnit)
    	        .concurrencyLevel(concurrencyLevel)
    	        .recordStats()
    	        .build(new CacheLoader<String, String>() {
            			public String load(String authenticationHeader) {
            				
            				String[] authenticationCredentials = HTTPAbstractProcessor.getAuthenticationCredentials(authenticationHeader);
            				if (authenticationCredentials.length != 2) {
            					throw new MailBoxServicesException(INVALID_AUTHORIZATION_HEADER, Response.Status.UNAUTHORIZED);
            				}
            				
    		                String loginId = authenticationCredentials[0];
    		                // encode the password using base64 bcoz UM will expect a base64
    		                // encoded token
    		                String headerToken = authenticationCredentials[1];
    		                String token = new String(Base64.encodeBase64(headerToken.getBytes()));
    		                // if both username and password is present call UM client to
    		                // authenticate
    		                UserManagementClient umClient = new UserManagementClient();
    		                umClient.addAccount(UserManagementClient.TYPE_NAME_PASSWORD, loginId, token);
    		                umClient.authenticate();
    		                if (!umClient.isSuccessful()) {
    		                    throw new MailBoxServicesException(umClient.getMessage(), Response.Status.UNAUTHORIZED);
    		                }
    		                return umClient.getAuthenticationToken();
            			}
    	            }
            	 );
        
    	CacheStatsRegistrar.register("cache-authentication", authenticationCache);
    }
    
    /**
     * Helper method to authenticate the given token
     * 
     * @param token basic auth header
     */
    public static String authenticate(String token) {

    	try {
    		
    		if (MailBoxUtil.isEmpty(authenticationCache.get(token))) {
    			throw new MailBoxServicesException(INVALID_AUTHORIZATION_HEADER, Response.Status.UNAUTHORIZED);
    		} else {
    			logger.debug("User authentication successfull");
    		}
    		return authenticationCache.get(token);
        } catch (Exception e) {
			throw new MailBoxServicesException(e.getMessage(), Response.Status.UNAUTHORIZED);
        }
    }
}
