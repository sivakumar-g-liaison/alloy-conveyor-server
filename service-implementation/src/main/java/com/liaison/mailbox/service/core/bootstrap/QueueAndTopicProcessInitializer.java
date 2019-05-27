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
import com.liaison.commons.messagebus.kafka.LiaisonConsumerManager;
import com.liaison.commons.messagebus.queue.LiaisonConsumer;
import com.liaison.commons.messagebus.queue.QueuePooledListenerContainer;
import com.liaison.commons.messagebus.topic.TopicPooledListenerContainer;
import com.liaison.commons.util.settings.DecryptableConfiguration;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.enums.DeploymentType;
import com.liaison.mailbox.service.module.GuiceInjector;
import com.liaison.mailbox.service.queue.consumer.FileStageReplicationRetryQueueProcessor;
import com.liaison.mailbox.service.queue.consumer.InboundFileQueueProcessor;
import com.liaison.mailbox.service.queue.consumer.MailboxProcessorQueueProcessor;
import com.liaison.mailbox.service.queue.consumer.MailboxQueuePooledListenerContainer;
import com.liaison.mailbox.service.queue.consumer.RelativeRelayQueueConsumer;
import com.liaison.mailbox.service.queue.consumer.RunningProcessorRetryQueueConsumer;
import com.liaison.mailbox.service.queue.consumer.ServiceBrokerToDropboxQueueProcessor;
import com.liaison.mailbox.service.queue.consumer.ServiceBrokerToMailboxQueueProcessor;
import com.liaison.mailbox.service.queue.consumer.UserManagementToRelayDirectoryQueueProcessor;
import com.liaison.mailbox.service.queue.kafka.processor.FileStageReplicationRetry;
import com.liaison.mailbox.service.queue.kafka.processor.InboundFile;
import com.liaison.mailbox.service.queue.kafka.processor.Mailbox;
import com.liaison.mailbox.service.queue.kafka.processor.RunningProcessorRetry;
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

import static com.liaison.mailbox.MailBoxConstants.DEPLOYMENT_APP_ID;
import static com.liaison.mailbox.MailBoxConstants.TOPIC_INBOUND_FILE_ADDITIONAL_TOPIC_SUFFIXES;
import static com.liaison.mailbox.MailBoxConstants.TOPIC_INBOUND_FILE_DEFAULT_TOPIC_SUFFIX;
import static com.liaison.mailbox.MailBoxConstants.TOPIC_MAILBOX_PROCESSOR_ADDITIONAL_TOPIC_SUFFIXES;
import static com.liaison.mailbox.MailBoxConstants.TOPIC_MAILBOX_PROCESSOR_DEFAULT_TOPIC_SUFFIX;
import static com.liaison.mailbox.MailBoxConstants.TOPIC_MAILBOX_TOPIC_MESSAGE_DEFAULT_TOPIC_SUFFIX;
import static com.liaison.mailbox.MailBoxConstants.TOPIC_REPLICATION_FAILOVER_ADDITIONAL_TOPIC_SUFFIXES;
import static com.liaison.mailbox.MailBoxConstants.TOPIC_REPLICATION_FAILOVER_DEFAULT_TOPIC_SUFFIX;
import static com.liaison.mailbox.MailBoxConstants.TOPIC_RUNNING_PROCESSOR_ADDITIONAL_TOPIC_SUFFIXES;
import static com.liaison.mailbox.MailBoxConstants.TOPIC_RUNNING_PROCESSOR_DEFAULT_TOPIC_SUFFIX;
import static com.liaison.mailbox.MailBoxConstants.TOPIC_SERVICE_BROKER_TO_DROPBOX_ADDITIONAL_TOPIC_SUFFIXES;
import static com.liaison.mailbox.MailBoxConstants.TOPIC_SERVICE_BROKER_TO_DROPBOX_DEFAULT_TOPIC_SUFFIX;
import static com.liaison.mailbox.MailBoxConstants.TOPIC_SERVICE_BROKER_TO_MAILBOX_ADDITIONAL_TOPIC_SUFFIXES;
import static com.liaison.mailbox.MailBoxConstants.TOPIC_SERVICE_BROKER_TO_MAILBOX_DEFAULT_TOPIC_SUFFIX;
import static com.liaison.mailbox.MailBoxConstants.TOPIC_USER_MANAGEMENT_TO_RELAY_DIRECTORY_ADDITIONAL_TOPIC_SUFFIXES;
import static com.liaison.mailbox.MailBoxConstants.TOPIC_USER_MANAGEMENT_TO_RELAY_DIRECTORY_DEFAULT_TOPIC_SUFFIX;

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
    private static final String INBOUND_FILE_QUEUE = "inboundFile";
    private static final String RUNNING_PROCESSOR_RETRY_QUEUE = "runningProcessorRetry";
    private static final String RELATIVE_RELAY_QUEUE = "relativeRelay";

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

                        // Service-Broker -> DropBox Worktickets
                        logger.info("Starting Dropbox Queue Listener with QS integration");
                        //KafkaTextMessageProcessor serviceBrokerToDropboxProcessor = injector.getInstance(Key.get(KafkaTextMessageProcessor.class, ServiceBrokerToDropbox.class));

                        // Read topics info from properties
                        Map<String, List<String>> serviceBrokerToDropboxConsumerTopics = getConsumerTopics(
                                TOPIC_SERVICE_BROKER_TO_DROPBOX_DEFAULT_TOPIC_SUFFIX,
                                TOPIC_SERVICE_BROKER_TO_DROPBOX_ADDITIONAL_TOPIC_SUFFIXES);

                        //Create a consumer instance, add ProcessorAvailabilityMonitor and start a consumer
                        /*LiaisonKafkaConsumer serviceBrokerToDropboxConsumer = liaisonKafkaConsumerFactory.create(serviceBrokerToDropboxProcessor);
                        serviceBrokerToDropboxConsumer.startConsumer(serviceBrokerToDropboxConsumerTopics);
                        serviceBrokerToDropboxConsumer.initializeProcessorAvailabilityMonitor(asyncProcessThreadPoolProcessorAvailability);*/
                        startConsumer(asyncProcessThreadPoolProcessorAvailability,
                                serviceBrokerToDropboxConsumerTopics,
                                getKafkaTextMessageProcessor(injector, ServiceBrokerToDropbox.class));

                        logger.info("Started Dropbox Queue Listener with QS integration");

                    } catch (Exception e) {
                        logger.error("Error when initializing LiaisonKafkaConsumer.", e);
                        //There's no sense to keep app up and running if a consumer doesn't start
                        System.exit(1);
                    }
                }

                break;

            case RELAY:
                
                try {

                    logger.info("Starting RUNNING_PROCESSOR_RETRY_QUEUE Listener");
                    QueuePooledListenerContainer relativeRelay = new MailboxQueuePooledListenerContainer(RelativeRelayQueueConsumer.class, RELATIVE_RELAY_QUEUE);
                    relativeRelay.initializeProcessorAvailabilityMonitor(asyncProcessThreadPoolProcessorAvailability);
                    logger.info("Started RUNNING_PROCESSOR_RETRY_QUEUE Listener");
                } catch (Exception e) {
                    logger.warn("Queue listener for inbound file could not be initialized.", e);
                }
                
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

                try {

                    logger.info("Starting INBOUND_FILE_QUEUE Listener");
                    QueuePooledListenerContainer inboundFileQueue = new MailboxQueuePooledListenerContainer(InboundFileQueueProcessor.class, INBOUND_FILE_QUEUE);
                    inboundFileQueue.initializeProcessorAvailabilityMonitor(asyncProcessThreadPoolProcessorAvailability);
                    logger.info("Started INBOUND_FILE_QUEUE Listener");
                } catch (Exception e) {
                    logger.warn("Queue listener for inbound file could not be initialized.", e);
                }

                try {

                    logger.info("Starting RUNNING_PROCESSOR_RETRY_QUEUE Listener");
                    QueuePooledListenerContainer runningProcessor = new MailboxQueuePooledListenerContainer(RunningProcessorRetryQueueConsumer.class, RUNNING_PROCESSOR_RETRY_QUEUE);
                    runningProcessor.initializeProcessorAvailabilityMonitor(asyncProcessThreadPoolProcessorAvailability);
                    logger.info("Started RUNNING_PROCESSOR_RETRY_QUEUE Listener");
                } catch (Exception e) {
                    logger.warn("Queue listener for inbound file could not be initialized.", e);
                }
                
                // Start consuming from Queue Service for different processor types
                if (configuration.getBoolean(MailBoxConstants.CONFIGURATION_QUEUE_SERVICE_ENABLED, false)) {
                    try {
                        Injector injector = GuiceInjector.getInjector();

                        // Relay -> Relay file stage replication retry
                        logger.info("Starting FILE_STAGE_REPLICATION_RETRY Listener with QS integration");

                        // Read topics info from properties
                        Map<String, List<String>> fileStageReplicationRetryConsumerTopics = getConsumerTopics(
                                TOPIC_REPLICATION_FAILOVER_DEFAULT_TOPIC_SUFFIX,
                                TOPIC_REPLICATION_FAILOVER_ADDITIONAL_TOPIC_SUFFIXES);

               /*         KafkaTextMessageProcessor fileStageReplicationRetryProcessor = injector.getInstance(Key.get(KafkaTextMessageProcessor.class, FileStageReplicationRetry.class));
                        LiaisonKafkaConsumer fileStageReplicationRetryConsumer = liaisonKafkaConsumerFactory.create(fileStageReplicationRetryProcessor);
                        fileStageReplicationRetryConsumer.startConsumer(fileStageReplicationRetryConsumerTopics);
                        fileStageReplicationRetryConsumer.initializeProcessorAvailabilityMonitor(asyncProcessThreadPoolProcessorAvailability);*/
                        startConsumer(asyncProcessThreadPoolProcessorAvailability,
                                fileStageReplicationRetryConsumerTopics,
                                getKafkaTextMessageProcessor(injector, FileStageReplicationRetry.class));

                        logger.info("Started FILE_STAGE_REPLICATION_RETRY Listener with QS integration");

                        // Relay -> Relay profile trigger
                        logger.info("Starting MAILBOX_PROCESSOR_QUEUE Listener with QS integration");

                        // Read topics info from properties
                        Map<String, List<String>> mailboxProcessorConsumerTopics = getConsumerTopics(
                                TOPIC_MAILBOX_PROCESSOR_DEFAULT_TOPIC_SUFFIX,
                                TOPIC_MAILBOX_PROCESSOR_ADDITIONAL_TOPIC_SUFFIXES);

                        /*KafkaTextMessageProcessor mailboxProcessor = injector.getInstance(Key.get(KafkaTextMessageProcessor.class, Mailbox.class));
                        LiaisonKafkaConsumer mailboxProcessorConsumer = liaisonKafkaConsumerFactory.create(mailboxProcessor);
                        mailboxProcessorConsumer.startConsumer(mailboxProcessorConsumerTopics);
                        mailboxProcessorConsumer.initializeProcessorAvailabilityMonitor(asyncProcessThreadPoolProcessorAvailability);*/
                        startConsumer(asyncProcessThreadPoolProcessorAvailability,
                                mailboxProcessorConsumerTopics,
                                getKafkaTextMessageProcessor(injector, Mailbox.class));

                        logger.info("Started MAILBOX_PROCESSOR_QUEUE Listener with QS integration");


                        // Service-Broker -> Relay
                        logger.info("Starting MAILBOX_PROCESSED_PAYLOAD_QUEUE Listener with QS integration");

                        // Read topics info from properties
                        Map<String, List<String>> serviceBrokerToMailboxConsumerTopics = getConsumerTopics(
                                TOPIC_SERVICE_BROKER_TO_MAILBOX_DEFAULT_TOPIC_SUFFIX,
                                TOPIC_SERVICE_BROKER_TO_MAILBOX_ADDITIONAL_TOPIC_SUFFIXES);
                        
                        /*KafkaTextMessageProcessor serviceBrokerToMailboxProcessor = injector.getInstance(Key.get(KafkaTextMessageProcessor.class, ServiceBrokerToMailbox.class));
                        LiaisonKafkaConsumer serviceBrokerToMailboxConsumer = liaisonKafkaConsumerFactory.create(serviceBrokerToMailboxProcessor);
                        serviceBrokerToMailboxConsumer.startConsumer(serviceBrokerToMailboxConsumerTopics);
                        serviceBrokerToMailboxConsumer.initializeProcessorAvailabilityMonitor(asyncProcessThreadPoolProcessorAvailability);*/

                        startConsumer(asyncProcessThreadPoolProcessorAvailability,
                                serviceBrokerToMailboxConsumerTopics,
                                getKafkaTextMessageProcessor(injector, ServiceBrokerToMailbox.class));

                        logger.info("Started MAILBOX_PROCESSED_PAYLOAD_QUEUE Listener with QS integration");

                        logger.info("Starting MAILBOX_TOPIC_POOLED_LISTENER_CONTAINER Listener with QS integration");

                        // Read topics info from properties
                        /*Map<String, List<String>> mailboxTopicMessageConsumerTopics = new HashMap<>();
                        List<String> mailboxTopicMessageTopicSuffixes = new ArrayList<>();
                        mailboxTopicMessageTopicSuffixes.add(configuration.getString(TOPIC_MAILBOX_TOPIC_MESSAGE_DEFAULT_TOPIC_SUFFIX));
                        mailboxTopicMessageConsumerTopics.put(configuration.getString(TOPIC_MAILBOX_TOPIC_MESSAGE_RECEIVER_ID), mailboxTopicMessageTopicSuffixes);*/

                        Map<String, List<String>> mailboxTopicMessageConsumerTopics = getConsumerTopics(
                                TOPIC_MAILBOX_TOPIC_MESSAGE_DEFAULT_TOPIC_SUFFIX,
                                TOPIC_RUNNING_PROCESSOR_ADDITIONAL_TOPIC_SUFFIXES);

                        /*LiaisonConsumer broadcastConsumer = injector.getInstance(Key.get(LiaisonConsumer.class, BroadcastConsumer.class));
                        broadcastConsumer.startConsumer(mailboxTopicMessageConsumerTopics);
                        broadcastConsumer.initializeProcessorAvailabilityMonitor(asyncProcessThreadPoolProcessorAvailability);*/
                        startConsumer(asyncProcessThreadPoolProcessorAvailability,
                                mailboxTopicMessageConsumerTopics,
                                getKafkaTextMessageProcessor(injector, BroadcastConsumer.class), true);

                        logger.info("Started MAILBOX_TOPIC_POOLED_LISTENER_CONTAINER Listener with QS integration");

                        // User Management -> Relay directory
                        logger.info("Starting USERMANAGEMENT_RELAY_DIRECTORY_OPERATIONS_QUEUE Listener with QS integration");

                        // Read topics info from properties
                        Map<String, List<String>> userManagementToRelayDirectoryConsumerTopics = getConsumerTopics(
                                TOPIC_USER_MANAGEMENT_TO_RELAY_DIRECTORY_DEFAULT_TOPIC_SUFFIX,
                                TOPIC_USER_MANAGEMENT_TO_RELAY_DIRECTORY_ADDITIONAL_TOPIC_SUFFIXES);

                        /*KafkaTextMessageProcessor userManagementToRelayDirectoryProcessor = injector.getInstance(Key.get(KafkaTextMessageProcessor.class, UserManagementToRelayDirectory.class));
                        LiaisonKafkaConsumer userManagementToRelayDirectoryConsumer = liaisonKafkaConsumerFactory.create(userManagementToRelayDirectoryProcessor);
                        userManagementToRelayDirectoryConsumer.startConsumer(userManagementToRelayDirectoryConsumerTopics);
                        userManagementToRelayDirectoryConsumer.initializeProcessorAvailabilityMonitor(asyncProcessThreadPoolProcessorAvailability);*/
                        startConsumer(asyncProcessThreadPoolProcessorAvailability,
                                userManagementToRelayDirectoryConsumerTopics,
                                getKafkaTextMessageProcessor(injector, UserManagementToRelayDirectory.class));

                        logger.info("Started USERMANAGEMENT_RELAY_DIRECTORY_OPERATIONS_QUEUE Listener with QS integration");

                        logger.info("Starting INBOUND_FILE_QUEUE Listener with QS integration");

                        // Read topics info from properties
                        Map<String, List<String>> inboundFileConsumerTopics = getConsumerTopics(
                                TOPIC_INBOUND_FILE_DEFAULT_TOPIC_SUFFIX,
                                TOPIC_INBOUND_FILE_ADDITIONAL_TOPIC_SUFFIXES);

                        startConsumer(asyncProcessThreadPoolProcessorAvailability,
                                inboundFileConsumerTopics,
                                getKafkaTextMessageProcessor(injector, InboundFile.class));

                        logger.info("Started INBOUND_FILE_QUEUE Listener with QS integration");

                        logger.info("Starting RUNNING_PROCESSOR_RETRY_QUEUE Listener with QS integration");

                        // Read topics info from properties
                        Map<String, List<String>> runningProcessorRetryConsumerTopics = getConsumerTopics(
                                TOPIC_RUNNING_PROCESSOR_DEFAULT_TOPIC_SUFFIX,
                                TOPIC_RUNNING_PROCESSOR_ADDITIONAL_TOPIC_SUFFIXES);

                        startConsumer(asyncProcessThreadPoolProcessorAvailability,
                                runningProcessorRetryConsumerTopics,
                                getKafkaTextMessageProcessor(injector, RunningProcessorRetry.class));

                        logger.info("Started RUNNING_PROCESSOR_RETRY_QUEUE Listener with QS integration");

                    } catch (Exception e) {
                        logger.error("Error when initializing LiaisonKafkaConsumer.", e);
                        //There's no sense to keep app up and running if a consumer doesn't start
                        System.exit(1);
                    }
                }
        }
    }

    private static void startConsumer(AsyncProcessThreadPool.AsyncProcessThreadPoolProcessorAvailability asyncProcessThreadPoolProcessorAvailability,
                                      Map<String, List<String>> consumerTopics,
                                      KafkaTextMessageProcessor kafkaTextMessageProcessor) {
        startConsumer(asyncProcessThreadPoolProcessorAvailability, consumerTopics, kafkaTextMessageProcessor, false);
    }


    private static void startConsumer(AsyncProcessThreadPool.AsyncProcessThreadPoolProcessorAvailability asyncProcessThreadPoolProcessorAvailability,
                                      Map<String, List<String>> consumerTopics,
                                      KafkaTextMessageProcessor kafkaTextMessageProcessor, boolean isBroadcast) {

        LiaisonConsumerManager consumerManager = GuiceInjector.getInjector().getInstance(LiaisonConsumerManager.class);
        LiaisonConsumer consumer;
        if (isBroadcast) {
            consumer = consumerManager.createBroadcastConsumer(kafkaTextMessageProcessor);
        } else {
            consumer = consumerManager.createConsumer(kafkaTextMessageProcessor);
        }
        consumer.startConsumer(consumerTopics);
        consumer.initializeProcessorAvailabilityMonitor(asyncProcessThreadPoolProcessorAvailability);
    }

    private static KafkaTextMessageProcessor getKafkaTextMessageProcessor(Injector injector, Class clazz) {

        if (FileStageReplicationRetry.class == clazz) {
            return injector.getInstance(Key.get(KafkaTextMessageProcessor.class, FileStageReplicationRetry.class));
        } else if (Mailbox.class == clazz) {
            return injector.getInstance(Key.get(KafkaTextMessageProcessor.class, Mailbox.class));
        } else if (ServiceBrokerToDropbox.class == clazz) {
            return injector.getInstance(Key.get(KafkaTextMessageProcessor.class, ServiceBrokerToDropbox.class));
        } else if (ServiceBrokerToMailbox.class == clazz) {
            return injector.getInstance(Key.get(KafkaTextMessageProcessor.class, ServiceBrokerToMailbox.class));
        } else if (UserManagementToRelayDirectory.class == clazz) {
            return injector.getInstance(Key.get(KafkaTextMessageProcessor.class, UserManagementToRelayDirectory.class));
        } else if (InboundFile.class == clazz) {
            return injector.getInstance(Key.get(KafkaTextMessageProcessor.class, InboundFile.class));
        } else if (RunningProcessorRetry.class == clazz) {
            return injector.getInstance(Key.get(KafkaTextMessageProcessor.class, RunningProcessorRetry.class));
        } else  if (BroadcastConsumer.class == clazz) {
            return injector.getInstance(Key.get(KafkaTextMessageProcessor.class, BroadcastConsumer.class));
        } else {
            throw new RuntimeException("Invalid class - " + clazz);
        }
    }

    private static Map<String, List<String>> getConsumerTopics(String topicReplicationFailoverDefaultTopicSuffix,
                                                               String topicReplicationFailoverAdditionalTopicSuffixes) {

        Map<String, List<String>> consumerTopics = new HashMap<>();
        List<String> topicSuffixes = new ArrayList<>();
        topicSuffixes.add(configuration.getString(topicReplicationFailoverDefaultTopicSuffix));
        List<String> additionalTopicSuffixes = Arrays.asList(configuration.getStringArray(topicReplicationFailoverAdditionalTopicSuffixes));
        if (!additionalTopicSuffixes.isEmpty()) {
            topicSuffixes.addAll(additionalTopicSuffixes);
        }
        consumerTopics.put(DEPLOYMENT_APP_ID, topicSuffixes);
        return consumerTopics;
    }
}
