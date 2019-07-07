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
import com.liaison.commons.util.settings.DecryptableConfiguration;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.enums.DeploymentType;
import com.liaison.mailbox.service.module.GuiceInjector;
import com.liaison.mailbox.service.queue.consumer.MailboxQueuePooledListenerContainer;
import com.liaison.mailbox.service.queue.consumer.ServiceBrokerToDropboxQueueProcessor;
import com.liaison.mailbox.service.queue.kafka.processor.ServiceBrokerToDropbox;
import com.liaison.mailbox.service.thread.pool.AsyncProcessThreadPool;
import com.liaison.mailbox.service.util.MailBoxUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.liaison.mailbox.MailBoxConstants.DEPLOYMENT_APP_ID;
import static com.liaison.mailbox.MailBoxConstants.DROPBOX_QUEUE;
import static com.liaison.mailbox.MailBoxConstants.SKIP_QUEUE_INITIALIZER;
import static com.liaison.mailbox.MailBoxConstants.TOPIC_SERVICE_BROKER_TO_DROPBOX_ADDITIONAL_TOPIC_SUFFIXES;
import static com.liaison.mailbox.MailBoxConstants.TOPIC_SERVICE_BROKER_TO_DROPBOX_DEFAULT_TOPIC_SUFFIX;
import static com.liaison.mailbox.MailBoxConstants.availabilityMinHeadRoom;


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
        if (DeploymentType.valueOf(deploymentType) == DeploymentType.CONVEYOR) {// Initialize the dropbox queue
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

        if (ServiceBrokerToDropbox.class == clazz) {
            return injector.getInstance(Key.get(KafkaTextMessageProcessor.class, ServiceBrokerToDropbox.class));
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
