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

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.commons.util.settings.LiaisonArchaiusConfiguration;
import com.liaison.health.check.threadpool.ThreadPoolCheck;
import com.liaison.health.core.LiaisonHealthCheckRegistry;
import com.liaison.threadmanagement.LiaisonExecutorServiceBuilder;

public class SweeperEventProcessThreadPool {

    public static final int DEFAULT_RELATIVE_RELAY_PROCESSING_THREAD_POOL_SIZE = 10;
    public static final int DEFAULT_RELATIVE_RELAY_KEEPALIVE_MINUTES = 1;

    public static final String PROPERTY_RELATIVE_RELAY_THREADPOOL_SIZE = "com.liaison.mailbox.relative.relay.threadpool.size";
    public static final String PROPERTY_RELATIVE_RELAY_KEEPALIVE_MINUTES = "com.liaison.mailbox.relative.relay.threadpool.keepalive.minutes";
    public static final String PROPERTY_RELATIVE_RELAY_COREPOOLSIZE = "com.liaison.mailbox.relative.relay.threadpool.corepoolsize";
    public static final String RELATIVE_RELAY_PROCESS_THREADPOOL_NAME = "g2-pool-relative-relay-processing";

    public static int keepAlive = LiaisonArchaiusConfiguration.getInstance().getInt(PROPERTY_RELATIVE_RELAY_KEEPALIVE_MINUTES, DEFAULT_RELATIVE_RELAY_KEEPALIVE_MINUTES);

    private static final Logger logger = LogManager.getLogger(SweeperProcessThreadPool.class);

    private static ThreadPoolExecutor executorService;
    private static int sweeperEventProcessingThreadPoolSize;
    private static int corePoolSize;

    static {
        sweeperEventProcessingThreadPoolSize = LiaisonArchaiusConfiguration.getInstance().getInt(PROPERTY_RELATIVE_RELAY_THREADPOOL_SIZE, DEFAULT_RELATIVE_RELAY_PROCESSING_THREAD_POOL_SIZE);

        int defaultCorePoolSize = Math.round(sweeperEventProcessingThreadPoolSize/2);
        corePoolSize = LiaisonArchaiusConfiguration.getInstance().getInt(PROPERTY_RELATIVE_RELAY_COREPOOLSIZE, defaultCorePoolSize);

        // keep pool trimmed to half during slack time for resource cleanup
        executorService = (ThreadPoolExecutor) LiaisonExecutorServiceBuilder.newExecutorService(
                RELATIVE_RELAY_PROCESS_THREADPOOL_NAME, 
                corePoolSize, 
                sweeperEventProcessingThreadPoolSize, 
                keepAlive, 
                TimeUnit.MINUTES);

        // threadpool check
        LiaisonHealthCheckRegistry.INSTANCE.register(RELATIVE_RELAY_PROCESS_THREADPOOL_NAME + "_check",
                new ThreadPoolCheck(RELATIVE_RELAY_PROCESS_THREADPOOL_NAME, 20));
    }

    public static ThreadPoolExecutor getExecutorService() {
        return executorService;
    }

    public static int getThreadPoolSize() {
        return sweeperEventProcessingThreadPoolSize;
    }

    /**
     * ThreadPool should not be instantiated.
     */
    private SweeperEventProcessThreadPool() { }
}