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

import com.liaison.commons.util.settings.LiaisonArchaiusConfiguration;
import com.liaison.health.check.threadpool.ThreadPoolCheck;
import com.liaison.health.core.LiaisonHealthCheckRegistry;
import com.liaison.threadmanagement.LiaisonExecutorServiceBuilder;
import com.liaison.threadmanagement.LiaisonExecutorServiceDetail;
import com.liaison.threadmanagement.LiaisonExecutorServiceRegistrar;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class SweeperProcessThreadPool {

    public static final int DEFAULT_SWEEPER_PROCESSING_THREAD_POOL_SIZE = 10;
    public static final int DEFAULT_SWEEPER_KEEPALIVE_MINUTES = 1;

    public static final String PROPERTY_SWEEPER_THREADPOOL_SIZE = "com.liaison.mailbox.sweeper.threadpool.size";
    public static final String PROPERTY_SWEEPER_KEEPALIVE_MINUTES = "com.liaison.mailbox.sweeper.threadpool.keepalive.minutes";
    public static final String PROPERTY_SWEEPER_COREPOOLSIZE = "com.liaison.mailbox.sweeper.threadpool.corepoolsize";
    public static final String SWEEPER_PROCESS_THREADPOOL_NAME = "g2-pool-sweeper-processing";

    public static int keepAlive = LiaisonArchaiusConfiguration.getInstance().getInt(PROPERTY_SWEEPER_KEEPALIVE_MINUTES, DEFAULT_SWEEPER_KEEPALIVE_MINUTES);

    private static final Logger logger = LogManager.getLogger(SweeperProcessThreadPool.class);

    private static ThreadPoolExecutor executorService;
    private static int sweeperProcessingThreadPoolSize;
    private static int corePoolSize;

    static {
        sweeperProcessingThreadPoolSize = LiaisonArchaiusConfiguration.getInstance().getInt(PROPERTY_SWEEPER_THREADPOOL_SIZE, DEFAULT_SWEEPER_PROCESSING_THREAD_POOL_SIZE);

        int defaultCorePoolSize = Math.round(sweeperProcessingThreadPoolSize/2);
        corePoolSize = LiaisonArchaiusConfiguration.getInstance().getInt(PROPERTY_SWEEPER_COREPOOLSIZE, defaultCorePoolSize);

        // keep pool trimmed to half during slack time for resource cleanup
        executorService = (ThreadPoolExecutor) LiaisonExecutorServiceBuilder.newExecutorService(
                SWEEPER_PROCESS_THREADPOOL_NAME, 
                corePoolSize, 
                sweeperProcessingThreadPoolSize, 
                keepAlive, 
                TimeUnit.MINUTES);

        // threadpool check
        LiaisonHealthCheckRegistry.INSTANCE.register(SWEEPER_PROCESS_THREADPOOL_NAME + "_check",
                new ThreadPoolCheck(SWEEPER_PROCESS_THREADPOOL_NAME, 20));
    }

    public static ThreadPoolExecutor getExecutorService() {
        return executorService;
    }

    public static int getThreadPoolSize() {
        return sweeperProcessingThreadPoolSize;
    }

    /**
     * ThreadPool should not be instantiated.
     */
    private SweeperProcessThreadPool() { }
}