/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.thread.pool;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.commons.util.settings.LiaisonConfigurationFactory;
import com.liaison.threadmanagement.LiaisonExecutorServiceBuilder;

public class AsyncProcessThreadPool {

    public static final int DEFAULT_ASYNC_PROCESSING_THREAD_POOL_SIZE = 50;
    public static final int DEFAULT_ASYNC_KEEPALIVE_MINUTES = 1;
    
    public static final String PROPERTY_ASYNC_THREADPOOL_SIZE = "com.liaison.mailbox.async.threadpool.size";
    public static final String PROPERTY_ASYNC_KEEPALIVE_MINUTES = "com.liaison.mailbox.async.threadpool.keepalive.minutes";
    public static final String PROPERTY_ASYNC_COREPOOLSIZE = "com.liaison.mailbox.async.threadpool.corepoolsize";
     
    public static final String ASYNC_PROCESS_THREADPOOL_NAME = "g2-pool-async-processing";
    
    public static int keepAlive = LiaisonConfigurationFactory.getConfiguration().getInt(PROPERTY_ASYNC_KEEPALIVE_MINUTES, DEFAULT_ASYNC_KEEPALIVE_MINUTES);
    
    private static final Logger logger = LogManager.getLogger(AsyncProcessThreadPool.class);
    
    private static ExecutorService executorService;
    private static int asyncProcessingThreadPoolSize;
    private static int corePoolSize;
    
    static {
    	asyncProcessingThreadPoolSize = LiaisonConfigurationFactory.getConfiguration().getInt(PROPERTY_ASYNC_THREADPOOL_SIZE, DEFAULT_ASYNC_PROCESSING_THREAD_POOL_SIZE);
    	
    	int defaultCorePoolSize = Math.round(asyncProcessingThreadPoolSize/2);
    	corePoolSize = LiaisonConfigurationFactory.getConfiguration().getInt(PROPERTY_ASYNC_COREPOOLSIZE, defaultCorePoolSize);
    	
    	// keep pool trimmed to half during slack time for resource cleanup
    	executorService = LiaisonExecutorServiceBuilder.newExecutorService(
        		ASYNC_PROCESS_THREADPOOL_NAME, 
        		corePoolSize, 
        		asyncProcessingThreadPoolSize, 
        		keepAlive, 
        		TimeUnit.MINUTES);
    }

    public static ExecutorService getExecutorService() {
        return executorService;
    }
    
    public static int getThreadPoolSize() {
        return asyncProcessingThreadPoolSize;
    }
      
    /**
     * ThreadPool should not be instantiated.
     */
    private AsyncProcessThreadPool() { }
}

