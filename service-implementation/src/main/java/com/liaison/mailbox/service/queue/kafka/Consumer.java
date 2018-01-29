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
import com.liaison.mailbox.service.thread.pool.KafkaConsumerThreadPool;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.Properties;

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

public class Consumer implements AutoCloseable {

    private static final Logger LOG = LogManager.getLogger(Consumer.class);
    private static final DecryptableConfiguration configuration = LiaisonArchaiusConfiguration.getInstance();
    private static final String STREAM = configuration.getString(KAFKA_STREAM)
            + configuration.getString(KAFKA_CONSUMER_TOPIC_NAME);
    private static final String AUTO_OFFSET_RESET_DEFAULT = "latest";
    private static int TIMEOUT = 200;
    private KafkaConsumer<String, String> consumer = null;

    public void consume() {

        consumer = new KafkaConsumer<>(getProperties());
        TIMEOUT = configuration.getInt(KAFKA_CONSUMER_PREFIX + TIMEOUT, TIMEOUT);
        consumer.subscribe(Collections.singletonList(STREAM));

        while (true) {

            ConsumerRecords<String, String> records = consumer.poll(TIMEOUT);
            for (ConsumerRecord<String, String> record : records) {
                KafkaConsumerThreadPool.getExecutorService().submit(new KafkaMessageService(record.value()));
            }
        }
    }

    private static Properties getProperties() {

        Properties properties = new Properties();
        properties.put(SERVERS, configuration.getString(KAFKA_CONSUMER_PREFIX + SERVERS));
        properties.put(KEY_DESERIALIZER, configuration.getString(KAFKA_CONSUMER_PREFIX + KEY_DESERIALIZER, KEY_DESERIALIZER_DEFAULT));
        properties.put(VALUE_DESERIALIZER, configuration.getString(KAFKA_CONSUMER_PREFIX + VALUE_DESERIALIZER, VALUE_DESERIALIZER_DEFAULT));
        properties.put(AUTO_OFFSET_RESET, configuration.getString(KAFKA_CONSUMER_PREFIX + AUTO_OFFSET_RESET, AUTO_OFFSET_RESET_DEFAULT));
        properties.setProperty(GROUP_ID, configuration.getString(KAFKA_CONSUMER_PREFIX + GROUP_ID));

        return properties;
    }

    @Override
    public void close() {
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
}
