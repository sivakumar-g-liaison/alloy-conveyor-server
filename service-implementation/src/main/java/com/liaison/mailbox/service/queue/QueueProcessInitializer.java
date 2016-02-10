/**
 * Copyright 2016 Liaison Technologies, Inc.
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.mailbox.service.queue;

import com.liaison.commons.messagebus.queue.QueuePooledListenerContainer;
import com.liaison.health.check.threadpool.ThreadPoolCheck;
import com.liaison.health.core.LiaisonHealthCheckRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.commons.util.settings.DecryptableConfiguration;
import com.liaison.mailbox.service.queue.consumer.MailboxProcessorQueueProcessor;
import com.liaison.mailbox.service.queue.consumer.ServiceBrokerToDropboxQueueProcessor;
import com.liaison.mailbox.service.queue.consumer.ServiceBrokerToMailboxQueueProcessor;
import com.liaison.mailbox.service.thread.pool.AsyncProcessThreadPool;
import com.liaison.mailbox.service.util.MailBoxUtil;

/**
 * Initializes all queue pollers.
 *
 *
 * Because pollers are pulling from one thread pool and pushing into another
 * pool we stipulate a minimum headroom value, which is by default the processor
 * pool size (default {@value #DEFAULT_QUEUE_PROCESSOR_POOL_SIZE})
 *
 * This value may be overridden with property:
 * {@value #PROPERTY_QUEUE_PROCESSOR_POOL_AVAILABILITY_MIN_HEADROOM}
 *
 * Override queue poller pool size with property:
 * {@value #PROPERTY_QUEUE_PROCESSOR_POOL_SIZE}
 *
 * @author VNagarajan
 *
 */
public class QueueProcessInitializer {

    private static final Logger logger = LogManager.getLogger(QueueProcessInitializer.class);
    private static DecryptableConfiguration configuration = MailBoxUtil.getEnvironmentProperties();

    /**
     * Property to decide whether application should initialize dropbox queue or
     * not
     */
    public static final String START_DROPBOX_QUEUE = "com.liaison.deployAsDropbox";

    /**
     * Queue Processor default thread count
     */
    private static final int DEFAULT_THREAD_COUNT = configuration.getInt("com.liaison.queue.processor.default.thread.count", 10);

    /**
     * Properties required for Mininum headrool
     */
    private static final String PROPERTY_QUEUE_PROCESSOR_POOL_AVAILABILITY_MIN_HEADROOM = "com.liaison.queue.process.pool.availability.min.headroom";
    private static final int DEFAULT_QUEUE_PROCESSOR_POOL_AVAILABILITY_MIN_HEADROOM = DEFAULT_THREAD_COUNT;
    private static int availabilityMinHeadRoom = configuration.getInt(PROPERTY_QUEUE_PROCESSOR_POOL_AVAILABILITY_MIN_HEADROOM,
            DEFAULT_QUEUE_PROCESSOR_POOL_AVAILABILITY_MIN_HEADROOM);

    /**
     * Monikers for the queues to be initialize
     */
    private static final String DROPBOX_QUEUE = "dropboxQueue";
    private static final String MAILBOX_PROCESSOR_QUEUE = "processor";
    private static final String MAILBOX_PROCESSED_PAYLOAD_QUEUE = "processedPayload";

    public static QueuePooledListenerContainer dropboxQueue;
    public static QueuePooledListenerContainer mailboxProcessorQueue;
    public static QueuePooledListenerContainer mailboxProcessedPayloadQueue;


    public static void initialize() {

        AsyncProcessThreadPool.AsyncProcessThreadPoolProcessorAvailability asyncProcessThreadPoolProcessorAvailability = new AsyncProcessThreadPool.AsyncProcessThreadPoolProcessorAvailability(
                availabilityMinHeadRoom);

        if (configuration.getBoolean(START_DROPBOX_QUEUE, false)) {

            logger.info("Starting Dropbox Queue Listener");
            dropboxQueue = new QueuePooledListenerContainer(ServiceBrokerToDropboxQueueProcessor.class, DROPBOX_QUEUE);
            dropboxQueue.initializeProcessorAvailabilityMonitor(asyncProcessThreadPoolProcessorAvailability);
            logger.info("Started Dropbox Queue Listener");


//            // threadpool check
//            String dropboxPoolName = QueueProcessorManager.THREAD_POOL_NAME_PREFIX + DROPBOX_QUEUE;
//            LiaisonHealthCheckRegistry.INSTANCE.register(dropboxPoolName + "_check",
//                    new ThreadPoolCheck(dropboxPoolName, DEFAULT_THREAD_COUNT + 20));

        } else {

            // Initialize processor queue and processedPayload queue
            logger.info("Starting MAILBOX_PROCESSOR_QUEUE Listener");
            mailboxProcessorQueue = new QueuePooledListenerContainer(MailboxProcessorQueueProcessor.class, MAILBOX_PROCESSOR_QUEUE);
            mailboxProcessorQueue.initializeProcessorAvailabilityMonitor(asyncProcessThreadPoolProcessorAvailability);
            logger.info("Started MAILBOX_PROCESSOR_QUEUE Listener");



            logger.info("Starting MAILBOX_PROCESSED_PAYLOAD_QUEUE Listener");
            mailboxProcessedPayloadQueue = new QueuePooledListenerContainer(ServiceBrokerToMailboxQueueProcessor.class, MAILBOX_PROCESSED_PAYLOAD_QUEUE);
            mailboxProcessedPayloadQueue.initializeProcessorAvailabilityMonitor(asyncProcessThreadPoolProcessorAvailability);
            logger.info("Started MAILBOX_PROCESSED_PAYLOAD_QUEUE Listener");

//            // threadpool check
//            String processorPoolName = QueueProcessorManager.THREAD_POOL_NAME_PREFIX + MAILBOX_PROCESSOR_QUEUE;
//            LiaisonHealthCheckRegistry.INSTANCE.register(processorPoolName + "_check",
//                    new ThreadPoolCheck(processorPoolName, DEFAULT_THREAD_COUNT + 20));
//
//            // threadpool check
//            String payloadPoolName = QueueProcessorManager.THREAD_POOL_NAME_PREFIX + MAILBOX_PROCESSED_PAYLOAD_QUEUE;
//            LiaisonHealthCheckRegistry.INSTANCE.register(payloadPoolName + "_check",
//                    new ThreadPoolCheck(payloadPoolName, DEFAULT_THREAD_COUNT + 20));
        }
    }

}
