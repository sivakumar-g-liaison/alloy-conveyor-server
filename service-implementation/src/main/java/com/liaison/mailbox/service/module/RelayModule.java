/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.module;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Names;
import com.liaison.commons.messagebus.common.KafkaTextMessageProcessor;
import com.liaison.commons.messagebus.kafka.BroadcastConsumer;
import com.liaison.commons.messagebus.kafka.KafkaBroadcastConsumerProvider;
import com.liaison.commons.messagebus.kafka.KafkaConsumerProvider;
import com.liaison.commons.messagebus.kafka.LiaisonBroadcastKafkaConsumer;
import com.liaison.commons.messagebus.kafka.LiaisonBroadcastKafkaConsumerFactory;
import com.liaison.commons.messagebus.kafka.LiaisonConsumerManager;
import com.liaison.commons.messagebus.kafka.LiaisonConsumerManagerImpl;
import com.liaison.commons.messagebus.kafka.LiaisonKafkaConsumer;
import com.liaison.commons.messagebus.kafka.LiaisonKafkaConsumerFactory;
import com.liaison.commons.messagebus.kafka.LiaisonKafkaProducer;
import com.liaison.commons.messagebus.queue.LiaisonConsumer;
import com.liaison.commons.messagebus.queue.LiaisonProducer;
import com.liaison.commons.messagebus.rest.ConsumerResource;
import com.liaison.commons.util.GlassLoggerService;
import com.liaison.commons.util.StatusLogger;
import com.liaison.commons.util.StatusLoggerFactory;
import com.liaison.commons.util.UUIDGen;
import com.liaison.commons.util.settings.DecryptableConfiguration;
import com.liaison.commons.util.settings.LiaisonArchaiusConfiguration;
import com.liaison.mailbox.service.queue.kafka.KafkaMessageProcessor;
import com.liaison.mailbox.service.queue.kafka.KafkaMessageProcessorMock;
import com.liaison.mailbox.service.queue.kafka.Producer;
import com.liaison.mailbox.service.queue.kafka.processor.FileStageReplicationRetry;
import com.liaison.mailbox.service.queue.kafka.processor.FileStageReplicationRetryProcessor;
import com.liaison.mailbox.service.queue.kafka.processor.InboundFile;
import com.liaison.mailbox.service.queue.kafka.processor.InboundFileProcessor;
import com.liaison.mailbox.service.queue.kafka.processor.Mailbox;
import com.liaison.mailbox.service.queue.kafka.processor.MailboxProcessor;
import com.liaison.mailbox.service.queue.kafka.processor.MailboxTopicMessageProcessor;
import com.liaison.mailbox.service.queue.kafka.processor.RunningProcessorRetry;
import com.liaison.mailbox.service.queue.kafka.processor.RunningProcessorRetryProcessor;
import com.liaison.mailbox.service.queue.kafka.processor.ServiceBrokerToDropbox;
import com.liaison.mailbox.service.queue.kafka.processor.ServiceBrokerToDropboxProcessor;
import com.liaison.mailbox.service.queue.kafka.processor.ServiceBrokerToMailbox;
import com.liaison.mailbox.service.queue.kafka.processor.ServiceBrokerToMailboxProcessor;
import com.liaison.mailbox.service.queue.kafka.processor.UserManagementToRelayDirectory;
import com.liaison.mailbox.service.queue.kafka.processor.UserManagementToRelayDirectoryProcessor;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static com.liaison.mailbox.MailBoxConstants.CONFIGURATION_QUEUE_SERVICE_ENABLED;
import static com.liaison.mailbox.MailBoxConstants.TOPIC_MAILBOX_TOPIC_MESSAGE_DEFAULT_TOPIC_SUFFIX;

/**
 * Guice module for QS related Kafka bindings
 */
public class RelayModule extends AbstractModule {

    private static final Logger LOG = LogManager.getLogger(RelayModule.class);
    private static final String GROUP_ID = "groupId";

    @Provides
    DecryptableConfiguration provideLiaisonConfiguration() {
        return LiaisonArchaiusConfiguration.getInstance();
    }

    @Override
    protected void configure() {

        if (provideLiaisonConfiguration().getBoolean(CONFIGURATION_QUEUE_SERVICE_ENABLED, false)) {

            LOG.info("Guice module configuration for kafka. This means that the QS integration is enabled");

            // Bind producer to make KafkaTextMessageProcessor binding to work
            requestStaticInjection(Producer.class);

            bind(KafkaTextMessageProcessor.class).annotatedWith(FileStageReplicationRetry.class).to(FileStageReplicationRetryProcessor.class);
            bind(KafkaTextMessageProcessor.class).annotatedWith(Mailbox.class).to(MailboxProcessor.class);
            bind(KafkaTextMessageProcessor.class).annotatedWith(ServiceBrokerToMailbox.class).to(ServiceBrokerToMailboxProcessor.class);
            bind(KafkaTextMessageProcessor.class).annotatedWith(ServiceBrokerToDropbox.class).to(ServiceBrokerToDropboxProcessor.class);
            bind(KafkaTextMessageProcessor.class).annotatedWith(UserManagementToRelayDirectory.class).to(UserManagementToRelayDirectoryProcessor.class);
            bind(KafkaTextMessageProcessor.class).annotatedWith(InboundFile.class).to(InboundFileProcessor.class);
            bind(KafkaTextMessageProcessor.class).annotatedWith(RunningProcessorRetry.class).to(RunningProcessorRetryProcessor.class);
            bind(KafkaTextMessageProcessor.class).to(KafkaMessageProcessor.class);

            // Bindings for topic message
            bindConstant().annotatedWith(Names.named(GROUP_ID)).to(provideLiaisonConfiguration().getString(TOPIC_MAILBOX_TOPIC_MESSAGE_DEFAULT_TOPIC_SUFFIX) + "-" + UUIDGen.getCustomUUID());
            bind(new TypeLiteral<Consumer<String, String>>(){}).annotatedWith(BroadcastConsumer.class).toProvider(KafkaBroadcastConsumerProvider.class);
            bind(KafkaTextMessageProcessor.class).annotatedWith(BroadcastConsumer.class).to(MailboxTopicMessageProcessor.class);

            bind(LiaisonProducer.class).to(LiaisonKafkaProducer.class);
            bind(new TypeLiteral<Consumer<String, String>>() {
            }).toProvider(KafkaConsumerProvider.class);
            requestInjection(ConsumerResource.class);
            bind(LiaisonConsumerManager.class).to(LiaisonConsumerManagerImpl.class);

            install(new FactoryModuleBuilder().implement(StatusLogger.class, GlassLoggerService.class)
                    .build(StatusLoggerFactory.class));

            install(new FactoryModuleBuilder().implement(LiaisonConsumer.class, LiaisonKafkaConsumer.class)
                    .build(LiaisonKafkaConsumerFactory.class));

            install(new FactoryModuleBuilder().implement(LiaisonConsumer.class, LiaisonBroadcastKafkaConsumer.class)
                    .build(LiaisonBroadcastKafkaConsumerFactory.class));

        } else {
            // When queue service usage is disabled, the mock is needed to avoid null binding
            bind(KafkaTextMessageProcessor.class).to(KafkaMessageProcessorMock.class);
        }
    }
}
