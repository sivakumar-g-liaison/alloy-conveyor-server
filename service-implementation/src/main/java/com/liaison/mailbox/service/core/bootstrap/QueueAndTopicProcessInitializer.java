/**
 * Copyright 2016 Liaison Technologies, Inc.
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.mailbox.service.core.bootstrap;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.liaison.commons.messagebus.common.KafkaTextMessageProcessor;
import com.liaison.commons.messagebus.kafka.BroadcastConsumer;
import com.liaison.commons.messagebus.kafka.LiaisonKafkaConsumer;
import com.liaison.commons.messagebus.kafka.LiaisonKafkaConsumerFactory;
import com.liaison.commons.messagebus.queue.LiaisonConsumer;
import com.liaison.commons.messagebus.queue.QueuePooledListenerContainer;
import com.liaison.commons.messagebus.topic.TopicPooledListenerContainer;
import com.liaison.commons.util.settings.DecryptableConfiguration;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.enums.DeploymentType;
import com.liaison.mailbox.service.module.GuiceInjector;
import com.liaison.mailbox.service.queue.consumer.FileStageReplicationRetryQueueProcessor;
import com.liaison.mailbox.service.queue.consumer.MailboxProcessorQueueProcessor;
import com.liaison.mailbox.service.queue.consumer.MailboxQueuePooledListenerContainer;
import com.liaison.mailbox.service.queue.consumer.ServiceBrokerToDropboxQueueProcessor;
import com.liaison.mailbox.service.queue.consumer.ServiceBrokerToMailboxQueueProcessor;
import com.liaison.mailbox.service.queue.consumer.UserManagementToRelayDirectoryQueueProcessor;
import com.liaison.mailbox.service.queue.kafka.processor.FileStageReplicationRetry;
import com.liaison.mailbox.service.queue.kafka.processor.Mailbox;
import com.liaison.mailbox.service.queue.kafka.processor.ServiceBrokerToDropbox;
import com.liaison.mailbox.service.queue.kafka.processor.ServiceBrokerToMailbox;
import com.liaison.mailbox.service.queue.kafka.processor.UserManagementToRelayDirectory;
import com.liaison.mailbox.service.thread.pool.AsyncProcessThreadPool;
import com.liaison.mailbox.service.topic.MailBoxTopicPooledListenerContainer;
import com.liaison.mailbox.service.topic.consumer.MailBoxTopicMessageConsumer;
import com.liaison.mailbox.service.util.MailBoxUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.liaison.mailbox.MailBoxConstants.TOPIC_MAILBOX_PROCESSOR_ADDITIONAL_TOPIC_SUFFIXES;
import static com.liaison.mailbox.MailBoxConstants.TOPIC_MAILBOX_PROCESSOR_DEFAULT_TOPIC_SUFFIX;
import static com.liaison.mailbox.MailBoxConstants.TOPIC_MAILBOX_PROCESSOR_RECEIVER_ID;
import static com.liaison.mailbox.MailBoxConstants.TOPIC_MAILBOX_TOPIC_MESSAGE_DEFAULT_TOPIC_SUFFIX;
import static com.liaison.mailbox.MailBoxConstants.TOPIC_MAILBOX_TOPIC_MESSAGE_RECEIVER_ID;
import static com.liaison.mailbox.MailBoxConstants.TOPIC_REPLICATION_FAILOVER_ADDITIONAL_TOPIC_SUFFIXES;
import static com.liaison.mailbox.MailBoxConstants.TOPIC_REPLICATION_FAILOVER_DEFAULT_TOPIC_SUFFIX;
import static com.liaison.mailbox.MailBoxConstants.TOPIC_REPLICATION_FAILOVER_RECEIVER_ID;
import static com.liaison.mailbox.MailBoxConstants.TOPIC_SERVICE_BROKER_TO_DROPBOX_ADDITIONAL_TOPIC_SUFFIXES;
import static com.liaison.mailbox.MailBoxConstants.TOPIC_SERVICE_BROKER_TO_DROPBOX_DEFAULT_TOPIC_SUFFIX;
import static com.liaison.mailbox.MailBoxConstants.TOPIC_SERVICE_BROKER_TO_DROPBOX_RECEIVER_ID;
import static com.liaison.mailbox.MailBoxConstants.TOPIC_SERVICE_BROKER_TO_MAILBOX_ADDITIONAL_TOPIC_SUFFIXES;
import static com.liaison.mailbox.MailBoxConstants.TOPIC_SERVICE_BROKER_TO_MAILBOX_DEFAULT_TOPIC_SUFFIX;
import static com.liaison.mailbox.MailBoxConstants.TOPIC_SERVICE_BROKER_TO_MAILBOX_RECEIVER_ID;
import static com.liaison.mailbox.MailBoxConstants.TOPIC_USER_MANAGEMENT_TO_RELAY_DIRECTORY_ADDITIONAL_TOPIC_SUFFIXES;
import static com.liaison.mailbox.MailBoxConstants.TOPIC_USER_MANAGEMENT_TO_RELAY_DIRECTORY_DEFAULT_TOPIC_SUFFIX;
import static com.liaison.mailbox.MailBoxConstants.TOPIC_USER_MANAGEMENT_TO_RELAY_DIRECTORY_RECEIVER_ID;

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
    public static final String SKIP_QUEUE_INITIALIZER = "com.liaison.skip.queue";

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

                // Start consuming from Queue Service for different processor types
                if (configuration.getBoolean(MailBoxConstants.CONFIGURATION_QUEUE_SERVICE_ENABLED, false)) {
                    try {
                        Injector injector = GuiceInjector.getInjector();
                        LiaisonKafkaConsumerFactory liaisonKafkaConsumerFactory = injector.getInstance(LiaisonKafkaConsumerFactory.class);

                        // Service-Broker -> DropBox Worktickets
                        logger.info("Starting Dropbox Queue Listener with QS integration");
                        KafkaTextMessageProcessor serviceBrokerToDropboxProcessor = injector.getInstance(Key.get(KafkaTextMessageProcessor.class, ServiceBrokerToDropbox.class));

                        // Read topics info from properties
                        Map<String, List<String>> serviceBrokerToDropboxConsumerTopics = new HashMap<>();
                        List<String> serviceBrokerToDropboxProcessorTopicSuffixes = new ArrayList<>();
                        serviceBrokerToDropboxProcessorTopicSuffixes.add(configuration.getString(TOPIC_SERVICE_BROKER_TO_DROPBOX_DEFAULT_TOPIC_SUFFIX));
                        List<String> serviceBrokerToDropboxProcessorAdditionalTopicSuffixes = Arrays.asList(configuration.getStringArray(TOPIC_SERVICE_BROKER_TO_DROPBOX_ADDITIONAL_TOPIC_SUFFIXES));
                        if (!serviceBrokerToDropboxProcessorAdditionalTopicSuffixes.isEmpty()) {
                            serviceBrokerToDropboxProcessorTopicSuffixes.addAll(serviceBrokerToDropboxProcessorAdditionalTopicSuffixes);
                        }
                        serviceBrokerToDropboxConsumerTopics.put(configuration.getString(TOPIC_SERVICE_BROKER_TO_DROPBOX_RECEIVER_ID), serviceBrokerToDropboxProcessorTopicSuffixes);

                        //Create a consumer instance, add ProcessorAvailabilityMonitor and start a consumer
                        LiaisonKafkaConsumer serviceBrokerToDropboxConsumer = liaisonKafkaConsumerFactory.create(serviceBrokerToDropboxProcessor);
                        serviceBrokerToDropboxConsumer.startConsumer(serviceBrokerToDropboxConsumerTopics);
                        serviceBrokerToDropboxConsumer.initializeProcessorAvailabilityMonitor(asyncProcessThreadPoolProcessorAvailability);

                        logger.info("Started Dropbox Queue Listener with QS integration");

                    } catch (Exception e) {
                        logger.error("Error when initializing LiaisonKafkaConsumer.", e);
                        //There's no sense to keep app up and running if a consumer doesn't start
                        System.exit(1);
                    }
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

                // Start consuming from Queue Service for different processor types
                if (configuration.getBoolean(MailBoxConstants.CONFIGURATION_QUEUE_SERVICE_ENABLED, false)) {
                    try {
                        Injector injector = GuiceInjector.getInjector();
                        LiaisonKafkaConsumerFactory liaisonKafkaConsumerFactory = injector.getInstance(LiaisonKafkaConsumerFactory.class);

                        // Relay -> Relay file stage replication retry
                        logger.info("Starting FILE_STAGE_REPLICATION_RETRY Listener with QS integration");

                        // Read topics info from properties
                        Map<String, List<String>> fileStageReplicationRetryConsumerTopics = new HashMap<>();
                        List<String> fileStageReplicationRetryProcessorTopicSuffixes = new ArrayList<>();
                        fileStageReplicationRetryProcessorTopicSuffixes.add(configuration.getString(TOPIC_REPLICATION_FAILOVER_DEFAULT_TOPIC_SUFFIX));
                        List<String> fileStageReplicationRetryProcessorAdditionalTopicSuffixes = Arrays.asList(configuration.getStringArray(TOPIC_REPLICATION_FAILOVER_ADDITIONAL_TOPIC_SUFFIXES));
                        if (!fileStageReplicationRetryProcessorAdditionalTopicSuffixes.isEmpty()) {
                            fileStageReplicationRetryProcessorTopicSuffixes.addAll(fileStageReplicationRetryProcessorAdditionalTopicSuffixes);
                        }
                        fileStageReplicationRetryConsumerTopics.put(configuration.getString(TOPIC_REPLICATION_FAILOVER_RECEIVER_ID), fileStageReplicationRetryProcessorTopicSuffixes);

                        KafkaTextMessageProcessor fileStageReplicationRetryProcessor = injector.getInstance(Key.get(KafkaTextMessageProcessor.class, FileStageReplicationRetry.class));
                        LiaisonKafkaConsumer fileStageReplicationRetryConsumer = liaisonKafkaConsumerFactory.create(fileStageReplicationRetryProcessor);
                        fileStageReplicationRetryConsumer.startConsumer(fileStageReplicationRetryConsumerTopics);
                        fileStageReplicationRetryConsumer.initializeProcessorAvailabilityMonitor(asyncProcessThreadPoolProcessorAvailability);

                        logger.info("Started FILE_STAGE_REPLICATON_RETRY Listener with QS integration");

                        // Relay -> Relay profile trigger
                        logger.info("Starting MAILBOX_PROCESSOR_QUEUE Listener with QS integration");

                        // Read topics info from properties
                        Map<String, List<String>> mailboxProcessorConsumerTopics = new HashMap<>();
                        List<String> mailboxProcessorTopicSuffixes = new ArrayList<>();
                        mailboxProcessorTopicSuffixes.add(configuration.getString(TOPIC_MAILBOX_PROCESSOR_DEFAULT_TOPIC_SUFFIX));
                        List<String> mailboxProcessorAdditionalTopicSuffixes = Arrays.asList(configuration.getStringArray(TOPIC_MAILBOX_PROCESSOR_ADDITIONAL_TOPIC_SUFFIXES));
                        if (!mailboxProcessorAdditionalTopicSuffixes.isEmpty()) {
                            mailboxProcessorTopicSuffixes.addAll(mailboxProcessorAdditionalTopicSuffixes);
                        }
                        mailboxProcessorConsumerTopics.put(configuration.getString(TOPIC_MAILBOX_PROCESSOR_RECEIVER_ID), mailboxProcessorTopicSuffixes);

                        KafkaTextMessageProcessor mailboxProcessor = injector.getInstance(Key.get(KafkaTextMessageProcessor.class, Mailbox.class));
                        LiaisonKafkaConsumer mailboxProcessorConsumer = liaisonKafkaConsumerFactory.create(mailboxProcessor);
                        mailboxProcessorConsumer.startConsumer(mailboxProcessorConsumerTopics);
                        mailboxProcessorConsumer.initializeProcessorAvailabilityMonitor(asyncProcessThreadPoolProcessorAvailability);

                        logger.info("Started MAILBOX_PROCESSOR_QUEUE Listener with QS integration");


                        // Service-Broker -> Relay
                        logger.info("Starting MAILBOX_PROCESSED_PAYLOAD_QUEUE Listener with QS integration");

                        // Read topics info from properties
                        Map<String, List<String>> serviceBrokerToMailboxConsumerTopics = new HashMap<>();
                        List<String> serviceBrokerToMailboxProcessorTopicSuffixes = new ArrayList<>();
                        serviceBrokerToMailboxProcessorTopicSuffixes.add(configuration.getString(TOPIC_SERVICE_BROKER_TO_MAILBOX_DEFAULT_TOPIC_SUFFIX));
                        List<String> serviceBrokerToMailboxProcessorAdditionalTopicSuffixes = Arrays.asList(configuration.getStringArray(TOPIC_SERVICE_BROKER_TO_MAILBOX_ADDITIONAL_TOPIC_SUFFIXES));
                        if (!serviceBrokerToMailboxProcessorAdditionalTopicSuffixes.isEmpty()) {
                            serviceBrokerToMailboxProcessorTopicSuffixes.addAll(serviceBrokerToMailboxProcessorAdditionalTopicSuffixes);
                        }
                        serviceBrokerToMailboxConsumerTopics.put(configuration.getString(TOPIC_SERVICE_BROKER_TO_MAILBOX_RECEIVER_ID), serviceBrokerToMailboxProcessorTopicSuffixes);

                        KafkaTextMessageProcessor serviceBrokerToMailboxProcessor = injector.getInstance(Key.get(KafkaTextMessageProcessor.class, ServiceBrokerToMailbox.class));
                        LiaisonKafkaConsumer serviceBrokerToMailboxConsumer = liaisonKafkaConsumerFactory.create(serviceBrokerToMailboxProcessor);
                        serviceBrokerToMailboxConsumer.startConsumer(serviceBrokerToMailboxConsumerTopics);
                        serviceBrokerToMailboxConsumer.initializeProcessorAvailabilityMonitor(asyncProcessThreadPoolProcessorAvailability);

                        logger.info("Started MAILBOX_PROCESSED_PAYLOAD_QUEUE Listener with QS integration");

                        logger.info("Starting MAILBOX_TOPIC_POOLED_LISTENER_CONTAINER Listener with QS integration");

                        // Read topics info from properties
                        Map<String, List<String>> mailboxTopicMessageConsumerTopics = new HashMap<>();
                        List<String> mailboxTopicMessageTopicSuffixes = new ArrayList<>();
                        mailboxTopicMessageTopicSuffixes.add(configuration.getString(TOPIC_MAILBOX_TOPIC_MESSAGE_DEFAULT_TOPIC_SUFFIX));
                        mailboxTopicMessageConsumerTopics.put(configuration.getString(TOPIC_MAILBOX_TOPIC_MESSAGE_RECEIVER_ID), mailboxTopicMessageTopicSuffixes);
                        LiaisonConsumer broadcastConsumer = injector.getInstance(Key.get(LiaisonConsumer.class, BroadcastConsumer.class));
                        broadcastConsumer.startConsumer(mailboxTopicMessageConsumerTopics);
                        broadcastConsumer.initializeProcessorAvailabilityMonitor(asyncProcessThreadPoolProcessorAvailability);

                        logger.info("Started MAILBOX_TOPIC_POOLED_LISTENER_CONTAINER Listener with QS integration");

                        // User Management -> Relay directory
                        logger.info("Starting USERMANAGEMENT_RELAY_DIRECTORY_OPERATIONS_QUEUE Listener with QS integration");

                        // Read topics info from properties
                        Map<String, List<String>> userManagementToRelayDirectoryConsumerTopics = new HashMap<>();
                        List<String> userManagementToRelayDirectoryProcessorTopicSuffixes = new ArrayList<>();
                        userManagementToRelayDirectoryProcessorTopicSuffixes.add(configuration.getString(TOPIC_USER_MANAGEMENT_TO_RELAY_DIRECTORY_DEFAULT_TOPIC_SUFFIX));

                        List<String> userManagementToRelayDirectoryProcessorAdditionalTopicSuffixes = Arrays.asList(configuration.getStringArray(TOPIC_USER_MANAGEMENT_TO_RELAY_DIRECTORY_ADDITIONAL_TOPIC_SUFFIXES));
                        if (!userManagementToRelayDirectoryProcessorAdditionalTopicSuffixes.isEmpty()) {
                            userManagementToRelayDirectoryProcessorTopicSuffixes.addAll(userManagementToRelayDirectoryProcessorAdditionalTopicSuffixes);
                        }
                        userManagementToRelayDirectoryConsumerTopics.put(configuration.getString(TOPIC_USER_MANAGEMENT_TO_RELAY_DIRECTORY_RECEIVER_ID), userManagementToRelayDirectoryProcessorTopicSuffixes);

                        KafkaTextMessageProcessor userManagementToRelayDirectoryProcessor = injector.getInstance(Key.get(KafkaTextMessageProcessor.class, UserManagementToRelayDirectory.class));
                        LiaisonKafkaConsumer userManagementToRelayDirectoryConsumer = liaisonKafkaConsumerFactory.create(userManagementToRelayDirectoryProcessor);
                        userManagementToRelayDirectoryConsumer.startConsumer(userManagementToRelayDirectoryConsumerTopics);
                        userManagementToRelayDirectoryConsumer.initializeProcessorAvailabilityMonitor(asyncProcessThreadPoolProcessorAvailability);

                        logger.info("Started USERMANAGEMENT_RELAY_DIRECTORY_OPERATIONS_QUEUE Listener with QS integration");

                    } catch (Exception e) {
                        logger.error("Error when initializing LiaisonKafkaConsumer.", e);
                        //There's no sense to keep app up and running if a consumer doesn't start
                        System.exit(1);
                    }
                }
        }
    }
}
