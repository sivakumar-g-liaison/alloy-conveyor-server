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

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.commons.util.settings.DecryptableConfiguration;
import com.liaison.commons.util.settings.LiaisonArchaiusConfiguration;
import com.liaison.mailbox.service.thread.pool.KafkaConsumerThreadPool;

import java.util.Collections;
import java.util.Properties;

public class Consumer {

    private static final Logger LOG = LogManager.getLogger(Consumer.class);
    private static final String AUTO_OFFSET_RESET_DEFAULT = "latest";

    private static DecryptableConfiguration configuration = LiaisonArchaiusConfiguration.getInstance();
    private int timeout = 200;
    private KafkaConsumer<String, String> consumer = null;

    public void consume() {

        Properties properties = getProperties();

        consumer = new KafkaConsumer<>(properties);
        timeout = configuration.getInt(QueueServiceConstants.KAFKA_CONSUMER_PREFIX + QueueServiceConstants.TIMEOUT, timeout);
        consumer.subscribe(Collections.singletonList(configuration.getString(QueueServiceConstants.KAFKA_STREAM) + configuration.getString(QueueServiceConstants.KAFKA_CONSUMER_TOPIC_NAME)));

        while (true) {

            ConsumerRecords<String, String> records = consumer.poll(timeout);
            for (ConsumerRecord<String, String> record : records) {
                KafkaConsumerThreadPool.getExecutorService().submit(new KafkaMessageService(record.value()));
            }
        }
    }

    public void stop() throws Exception {

        if (consumer != null) {
            
            try {
                consumer.unsubscribe();
                consumer.close();
                LOG.info("MapR Streams consumer successfully closed!");
            } catch (Exception e) {
                LOG.error("An error occurred while closing MapR Streams consumer. " + e.getMessage());
                // Retry once
                try {
                    consumer.close();
                    LOG.info("MapR Streams consumer successfully closed!");
                } catch (Exception ex) {
                    LOG.error("An error occurred while closing MapR Streams consumer. " + ex.getMessage());
                }
            }
        }
    }

    private static Properties getProperties() {

        Properties properties = new Properties();
        properties.put(QueueServiceConstants.SERVERS, configuration.getString(QueueServiceConstants.KAFKA_CONSUMER_PREFIX + QueueServiceConstants.SERVERS));
        properties.put(QueueServiceConstants.KEY_DESERIALIZER, configuration.getString(QueueServiceConstants.KAFKA_CONSUMER_PREFIX + QueueServiceConstants.KEY_DESERIALIZER, QueueServiceConstants.KEY_DESERIALIZER_DEFAULT));
        properties.put(QueueServiceConstants.VALUE_DESERIALIZER, configuration.getString(QueueServiceConstants.KAFKA_CONSUMER_PREFIX + QueueServiceConstants.VALUE_DESERIALIZER, QueueServiceConstants.VALUE_DESERIALIZER_DEFAULT));
        properties.put(QueueServiceConstants.AUTO_OFFSET_RESET, configuration.getString(QueueServiceConstants.KAFKA_CONSUMER_PREFIX + QueueServiceConstants.AUTO_OFFSET_RESET, AUTO_OFFSET_RESET_DEFAULT));
        properties.setProperty(QueueServiceConstants.GROUP_ID, configuration.getString(QueueServiceConstants.KAFKA_CONSUMER_PREFIX + QueueServiceConstants.GROUP_ID));

        return properties;
    }
}
