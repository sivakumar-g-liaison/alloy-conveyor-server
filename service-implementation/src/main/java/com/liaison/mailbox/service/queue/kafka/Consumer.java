/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.queue.kafka;

import com.liaison.commons.util.settings.DecryptableConfiguration;
import com.liaison.commons.util.settings.LiaisonArchaiusConfiguration;
import com.liaison.health.check.threadpool.ThreadPoolCheck;
import com.liaison.health.core.LiaisonHealthCheckRegistry;
import com.liaison.threadmanagement.LiaisonExecutorServiceBuilder;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static com.liaison.mailbox.service.queue.kafka.QueueServiceConstants.AUTO_OFFSET_RESET;
import static com.liaison.mailbox.service.queue.kafka.QueueServiceConstants.GROUP_ID;
import static com.liaison.mailbox.service.queue.kafka.QueueServiceConstants.KAFKA_CONSUMER_PREFIX;
import static com.liaison.mailbox.service.queue.kafka.QueueServiceConstants.KAFKA_CONSUMER_TOPIC_NAME;
import static com.liaison.mailbox.service.queue.kafka.QueueServiceConstants.KAFKA_STREAM;
import static com.liaison.mailbox.service.queue.kafka.QueueServiceConstants.KEY_DESERIALIZER;
import static com.liaison.mailbox.service.queue.kafka.QueueServiceConstants.KEY_DESERIALIZER_DEFAULT;
import static com.liaison.mailbox.service.queue.kafka.QueueServiceConstants.SERVERS;
import static com.liaison.mailbox.service.queue.kafka.QueueServiceConstants.VALUE_DESERIALIZER;
import static com.liaison.mailbox.service.queue.kafka.QueueServiceConstants.VALUE_DESERIALIZER_DEFAULT;

public class Consumer {

    private static final Logger LOG = LogManager.getLogger(Consumer.class);
    private static final DecryptableConfiguration CONFIGURATION = LiaisonArchaiusConfiguration.getInstance();
    private static final String STREAM = CONFIGURATION.getString(KAFKA_STREAM)
            + CONFIGURATION.getString(KAFKA_CONSUMER_TOPIC_NAME);
    private static final String AUTO_OFFSET_RESET_DEFAULT = "latest";


    private static final int DEFAULT_KAFKA_CONSUMER_THREAD_POOL_SIZE = 10;
    private static final int DEFAULT_KAFKA_CONSUMER_KEEPALIVE_MINUTES = 1;

    private static final String PROPERTY_KAFKA_CONSUMER_THREADPOOL_SIZE = "com.liaison.mailbox.kafka.consumer.threadpool.size";
    private static final String PROPERTY_KAFKA_CONSUMER_KEEPALIVE_MINUTES = "com.liaison.mailbox.kafka.consumer.threadpool.keepalive.minutes";
    private static final String PROPERTY_KAFKA_CONSUMER_COREPOOLSIZE = "com.liaison.mailbox.kafka.consumer.threadpool.corepoolsize";
    private static final String KAFKA_CONSUMER_THREADPOOL_NAME = "g2-pool-kafka-consumer";

    private static int keepAlive = CONFIGURATION.getInt(PROPERTY_KAFKA_CONSUMER_KEEPALIVE_MINUTES, DEFAULT_KAFKA_CONSUMER_KEEPALIVE_MINUTES);

    private static ExecutorService executorService;
    private static int kafkaConsumerThreadPoolSize;
    private static int corePoolSize;

    private static int timeout;
    private static KafkaConsumer<String, String> consumer = null;

    public Consumer() {

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

        consumer = new KafkaConsumer<>(getProperties());
        timeout = 200;
        consumer.subscribe(Collections.singletonList(STREAM));
    }

    public void consume() {
        executorService.submit((Runnable) () -> {
            //while (true) {
                ConsumerRecords<String, String> records = consumer.poll(timeout);
                for (ConsumerRecord<String, String> record : records) {
                    executorService.submit(new KafkaMessageService(record.value()));
                }
            //}
        });
    }

    public void shutdown() {

        if (consumer != null) {

            try {
                consumer.unsubscribe();
                consumer.close();
                LOG.info("MapR Streams consumer successfully closed!");
            } catch (Exception e) {
                LOG.error("An error occurred while closing MapR Streams consumer. " + e.getMessage(), e);
                // Retry once
                try {
                    consumer.close();
                    LOG.info("MapR Streams consumer successfully closed!");
                } catch (Exception ex) {
                    LOG.error("An error occurred while closing MapR Streams consumer. " + ex.getMessage(), e);
                }
            }
        }
    }

    private static Properties getProperties() {

        Properties properties = new Properties();
        properties.put(SERVERS, CONFIGURATION.getString(KAFKA_CONSUMER_PREFIX + SERVERS));
        properties.put(KEY_DESERIALIZER, CONFIGURATION.getString(KAFKA_CONSUMER_PREFIX + KEY_DESERIALIZER, KEY_DESERIALIZER_DEFAULT));
        properties.put(VALUE_DESERIALIZER, CONFIGURATION.getString(KAFKA_CONSUMER_PREFIX + VALUE_DESERIALIZER, VALUE_DESERIALIZER_DEFAULT));
        properties.put(AUTO_OFFSET_RESET, CONFIGURATION.getString(KAFKA_CONSUMER_PREFIX + AUTO_OFFSET_RESET, AUTO_OFFSET_RESET_DEFAULT));
        properties.setProperty(GROUP_ID, CONFIGURATION.getString(KAFKA_CONSUMER_PREFIX + GROUP_ID));

        return properties;
    }

}
