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
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Properties;

import static com.liaison.mailbox.service.queue.kafka.QueueServiceConstants.KAFKA_CONSUMER_PREFIX;
import static com.liaison.mailbox.service.queue.kafka.QueueServiceConstants.KAFKA_PRODUCER_PREFIX;
import static com.liaison.mailbox.service.queue.kafka.QueueServiceConstants.KAFKA_PRODUCER_TOPIC_NAME;
import static com.liaison.mailbox.service.queue.kafka.QueueServiceConstants.KAFKA_STREAM;
import static com.liaison.mailbox.service.queue.kafka.QueueServiceConstants.KEY_SERIALIZER;
import static com.liaison.mailbox.service.queue.kafka.QueueServiceConstants.KEY_SERIALIZER_DEFAULT;
import static com.liaison.mailbox.service.queue.kafka.QueueServiceConstants.SERVERS;
import static com.liaison.mailbox.service.queue.kafka.QueueServiceConstants.VALUE_SERIALIZER;
import static com.liaison.mailbox.service.queue.kafka.QueueServiceConstants.VALUE_SERIALIZER_DEFAULT;

public class Producer implements AutoCloseable {

    private static final Logger LOG = LogManager.getLogger(Producer.class);
    private static DecryptableConfiguration configuration = LiaisonArchaiusConfiguration.getInstance();
    private static KafkaProducer<String, String> producer;
    private static String TOPIC_NAME = configuration.getString(KAFKA_STREAM)
            + configuration.getString(KAFKA_PRODUCER_TOPIC_NAME);

    public void produce(String message) {

        LOG.info("MapR Streams PRODUCER message to send" + message);

        try {
            producer = new KafkaProducer<>(getProperties());
            producer.send(new ProducerRecord<>(TOPIC_NAME, message));
        } catch (Exception e) {
            throw new RuntimeException("Unable to send message to topic " + TOPIC_NAME + ". " + e.getMessage(), e);
        }
    }

    private static Properties getProperties() {

        Properties producerProperties = new Properties();
        producerProperties.setProperty(SERVERS, configuration.getString(KAFKA_CONSUMER_PREFIX + SERVERS));
        producerProperties.setProperty(KEY_SERIALIZER, configuration.getString(KAFKA_PRODUCER_PREFIX + KEY_SERIALIZER, KEY_SERIALIZER_DEFAULT));
        producerProperties.setProperty(VALUE_SERIALIZER, configuration.getString(KAFKA_PRODUCER_PREFIX + VALUE_SERIALIZER, VALUE_SERIALIZER_DEFAULT));
        return producerProperties;
    }

    @Override
    public void close() {
        if (producer != null) {

            try {
                producer.close();
                LOG.info("MapR Streams PRODUCER successfully flushed and closed!");
            } catch (Exception e) {
                LOG.error("An error occurred while flushing/closing MapR Streams PRODUCER. " + e.getMessage(), e);
                // Retry once
                try {
                    producer.close();
                    LOG.info("MapR Streams PRODUCER successfully flushed and closed after retry!");
                } catch (Exception ex) {
                    LOG.error("An error occurred while flushing/closing MapR Streams PRODUCER. " + ex.getMessage(), e);
                }
            }
        } else {
            LOG.error("MapR Streams PRODUCER was not initialized and thus cannot be closed");
        }
    }
}
