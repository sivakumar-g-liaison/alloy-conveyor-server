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

import com.liaison.commons.util.settings.LiaisonArchaiusConfiguration;
import com.liaison.health.check.threadpool.ThreadPoolCheck;
import com.liaison.health.core.LiaisonHealthCheckRegistry;
import com.liaison.threadmanagement.LiaisonExecutorServiceBuilder;

public class KafkaConsumerThreadPool {

    public static final int DEFAULT_KAFKA_CONSUMER_THREAD_POOL_SIZE = 10;
    public static final int DEFAULT_KAFKA_CONSUMER_KEEPALIVE_MINUTES = 1;

    public static final String PROPERTY_KAFKA_CONSUMER_THREADPOOL_SIZE = "com.liaison.mailbox.kafka.consumer.threadpool.size";
    public static final String PROPERTY_KAFKA_CONSUMER_KEEPALIVE_MINUTES = "com.liaison.mailbox.kafka.consumer.threadpool.keepalive.minutes";
    public static final String PROPERTY_KAFKA_CONSUMER_COREPOOLSIZE = "com.liaison.mailbox.kafka.consumer.threadpool.corepoolsize";

    public static final String KAFKA_CONSUMER_THREADPOOL_NAME = "g2-pool-kafka-consumer";

    public static int keepAlive = LiaisonArchaiusConfiguration.getInstance().getInt(PROPERTY_KAFKA_CONSUMER_KEEPALIVE_MINUTES, DEFAULT_KAFKA_CONSUMER_KEEPALIVE_MINUTES);

    private static final Logger logger = LogManager.getLogger(KafkaConsumerThreadPool.class);

    private static ExecutorService executorService;
    private static int kafkaConsumerThreadPoolSize;
    private static int corePoolSize;
    
    static {
    	kafkaConsumerThreadPoolSize = LiaisonArchaiusConfiguration.getInstance().getInt(PROPERTY_KAFKA_CONSUMER_THREADPOOL_SIZE, DEFAULT_KAFKA_CONSUMER_THREAD_POOL_SIZE);

    	int defaultCorePoolSize = Math.round(kafkaConsumerThreadPoolSize/2);
    	corePoolSize = LiaisonArchaiusConfiguration.getInstance().getInt(PROPERTY_KAFKA_CONSUMER_COREPOOLSIZE, defaultCorePoolSize);

    	// keep pool trimmed to half during slack time for resource cleanup
    	executorService = LiaisonExecutorServiceBuilder.newExecutorService(
    	        KAFKA_CONSUMER_THREADPOOL_NAME, 
        		corePoolSize, 
        		kafkaConsumerThreadPoolSize, 
        		keepAlive, 
        		TimeUnit.MINUTES);

        // threadpool check
        LiaisonHealthCheckRegistry.INSTANCE.register(KAFKA_CONSUMER_THREADPOOL_NAME + "_check", new ThreadPoolCheck(KAFKA_CONSUMER_THREADPOOL_NAME, 20));
    }

    public static ExecutorService getExecutorService() {
        return executorService;
    }

    public static int getThreadPoolSize() {
        return kafkaConsumerThreadPoolSize;
    }

    /**
     * ThreadPool should not be instantiated.
     */
    private KafkaConsumerThreadPool() { }
}

