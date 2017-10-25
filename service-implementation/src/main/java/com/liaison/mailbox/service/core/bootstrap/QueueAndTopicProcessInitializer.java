/**
 * Copyright 2016 Liaison Technologies, Inc.
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.mailbox.service.core.bootstrap;

import com.liaison.commons.messagebus.queue.QueuePooledListenerContainer;
import com.liaison.commons.messagebus.topic.TopicPooledListenerContainer;
import com.liaison.commons.util.settings.DecryptableConfiguration;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.enums.DeploymentType;
import com.liaison.mailbox.service.queue.consumer.FileStageReplicationRetryQueueProcessor;
import com.liaison.mailbox.service.queue.consumer.MailboxProcessorQueueProcessor;
import com.liaison.mailbox.service.queue.consumer.MailboxQueuePooledListenerContainer;
import com.liaison.mailbox.service.queue.consumer.ServiceBrokerToDropboxQueueProcessor;
import com.liaison.mailbox.service.queue.consumer.ServiceBrokerToMailboxQueueProcessor;
import com.liaison.mailbox.service.queue.consumer.UserManagementToRelayDirectoryQueueProcessor;
import com.liaison.mailbox.service.thread.pool.AsyncProcessThreadPool;
import com.liaison.mailbox.service.topic.MailBoxTopicPooledListenerContainer;
import com.liaison.mailbox.service.topic.consumer.MailBoxTopicMessageConsumer;
import com.liaison.mailbox.service.util.MailBoxUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
public class QueueAndTopicProcessInitializer {

    private static final Logger logger = LogManager.getLogger(QueueAndTopicProcessInitializer.class);
    private static DecryptableConfiguration configuration = MailBoxUtil.getEnvironmentProperties();

    /**
     * Property to decide whether to initialize queue processors or not
     */
    private static final String SKIP_QUEUE_INITIALIZER = "com.liaison.skip.queue";

    /**
     * Queue Processor default thread count
     */
    private static final int DEFAULT_THREAD_COUNT = configuration.getInt("com.liaison.queue.processor.default.thread.count", 10);

    /**
     * Properties required for Minimum headroom
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
    private static final String TOPIC_POOL_NAME = "mailboxProcessorTopic";
    private static final String USERMANAGEMENT_RELAY_DIRECTORY_QUEUE = "userManagementRelayDirectoryQueue";
    private static final String FILE_STAGE_REPLICATON_RETRY = "fileStage";

    public static void initialize() {

        AsyncProcessThreadPool.AsyncProcessThreadPoolProcessorAvailability asyncProcessThreadPoolProcessorAvailability = new AsyncProcessThreadPool.AsyncProcessThreadPoolProcessorAvailability(
                availabilityMinHeadRoom);

        /**
         * THIS IS TO START THE SERVER IN LOCAL WITHOUT QUEUE SERVERS AND NOT FOR PRODUCTION
         */
        if (configuration.getBoolean(SKIP_QUEUE_INITIALIZER, false)) {
            return;
        }

        String deploymentType = configuration.getString(MailBoxConstants.DEPLOYMENT_TYPE, DeploymentType.RELAY.getValue());
        switch (DeploymentType.valueOf(deploymentType)) {

            case CONVEYOR:

                // Initialize the dropbox queue
                try {

                    logger.info("Starting Dropbox Queue Listener");
                    QueuePooledListenerContainer dropboxQueue = new MailboxQueuePooledListenerContainer(ServiceBrokerToDropboxQueueProcessor.class, DROPBOX_QUEUE);
                    dropboxQueue.initializeProcessorAvailabilityMonitor(asyncProcessThreadPoolProcessorAvailability);
                    logger.info("Started Dropbox Queue Listener");
                } catch (Exception e) {
                    logger.warn("Queue listener for Conveyor Server could not be initialized.", e);
                }
                break;

            case RELAY:
            case LOW_SECURE_RELAY:

                // Initialize processor queue and processedPayload queue
                try {

                    logger.info("Starting MAILBOX_PROCESSOR_QUEUE Listener");
                    QueuePooledListenerContainer mailboxProcessorQueue = new MailboxQueuePooledListenerContainer(MailboxProcessorQueueProcessor.class, MAILBOX_PROCESSOR_QUEUE);
                    mailboxProcessorQueue.initializeProcessorAvailabilityMonitor(asyncProcessThreadPoolProcessorAvailability);
                    logger.info("Started MAILBOX_PROCESSOR_QUEUE Listener");
                } catch (Exception e) {
                    logger.warn("Queue listener for Processor could not be initialized.", e);
                }

                try {

                    logger.info("Starting MAILBOX_PROCESSED_PAYLOAD_QUEUE Listener");
                    QueuePooledListenerContainer mailboxProcessedPayloadQueue = new MailboxQueuePooledListenerContainer(ServiceBrokerToMailboxQueueProcessor.class, MAILBOX_PROCESSED_PAYLOAD_QUEUE);
                    mailboxProcessedPayloadQueue.initializeProcessorAvailabilityMonitor(asyncProcessThreadPoolProcessorAvailability);
                    logger.info("Started MAILBOX_PROCESSED_PAYLOAD_QUEUE Listener");
                } catch (Exception e) {
                    logger.warn("Queue listener for SB to Relay Server could not be initialized.", e);
                }

                try {

                    logger.info("Starting MAILBOX_TOPIC_POOLED_LISTENER_CONTAINER Listener");
                    TopicPooledListenerContainer mailBoxTopicPooledListenerContainer = new MailBoxTopicPooledListenerContainer(MailBoxTopicMessageConsumer.class, TOPIC_POOL_NAME);
                    mailBoxTopicPooledListenerContainer.initializeProcessorAvailabilityMonitor(asyncProcessThreadPoolProcessorAvailability);
                    logger.info("Started MAILBOX_TOPIC_POOLED_LISTENER_CONTAINER Listener");
                } catch (Exception e) {
                    logger.warn("Topic listener for Relay Server could not be initialized.", e);
                }

                try {

                    logger.info("Starting USERMANAGEMENT_RELAY_DIRECTORY_OPERATIONS_QUEUE Listener");
                    QueuePooledListenerContainer umDirOprsQueue = new MailboxQueuePooledListenerContainer(UserManagementToRelayDirectoryQueueProcessor.class, USERMANAGEMENT_RELAY_DIRECTORY_QUEUE);
                    umDirOprsQueue.initializeProcessorAvailabilityMonitor(asyncProcessThreadPoolProcessorAvailability);
                    logger.info("Started USERMANAGEMENT_RELAY_DIRECTORY_OPERATIONS_QUEUE Listener");
                } catch (Exception e) {
                    logger.warn("Queue listener for UserManagement Directory Creation could not be initialized.", e);
                }

                try {

                    logger.info("Starting FILE_STAGE_REPLICATION_RETRY Listener");
                    QueuePooledListenerContainer fileStageReplicationQueue = new QueuePooledListenerContainer(FileStageReplicationRetryQueueProcessor.class, FILE_STAGE_REPLICATON_RETRY);
                    fileStageReplicationQueue.initializeProcessorAvailabilityMonitor(asyncProcessThreadPoolProcessorAvailability);
                    logger.info("Started FILE_STAGE_REPLICATON_RETRY Listener");
                }  catch (Exception e) {
                    logger.warn("Queue listener for FILE_STAGE_REPLICATION_RETRY could not be initialized.", e);
                }

        }
    }

}
