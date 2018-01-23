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

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.commons.util.settings.DecryptableConfiguration;
import com.liaison.commons.util.settings.LiaisonArchaiusConfiguration;

import java.util.Arrays;
import java.util.Properties;
import java.util.Random;
import java.io.IOException;

public class Producer {

    private static final Logger LOG = LogManager.getLogger(Producer.class);
    private static DecryptableConfiguration configuration = LiaisonArchaiusConfiguration.getInstance();
    private KafkaProducer<String, String> producer = null;

    public void produce(int messageCount) throws IOException {

        LOG.info("MapR Streams producer message count to send" + messageCount);
        
        Properties properties = getProperties();
        producer = new KafkaProducer<>(properties);

        Random random = new Random();
        String[] sentences = new String[] {
                "the cow jumped over the moon",
                "an apple a day keeps the doctor away",
                "four score and seven years ago",
                "snow white and the seven dwarfs",
                "i am at two with nature" };

        String progressAnimation = "|/-\\";
        String topicName = configuration.getString(QueueServiceConstants.KAFKA_STREAM) + configuration.getString(QueueServiceConstants.KAFKA_TOPIC_RELAY);

        try {
            for (int i = 0; i < messageCount; i++) {

                String sentence = sentences[random.nextInt(sentences.length)];
                producer.send(new ProducerRecord<>(topicName, sentence));
                String progressBar = "\r" + progressAnimation.charAt(i % progressAnimation.length()) + " " + i;
                LOG.info("MapR Streams producer" + Arrays.toString(progressBar.getBytes()));
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to send message to topic " + topicName + ". " + e.getMessage(), e);
        } finally {
            stop();
        }
    }

    public void stop() {

        if (producer != null) {
            
            try {
                producer.close();
                LOG.info("MapR Streams producer successfully flushed and closed!");
            } catch (Exception e) {
                LOG.error("An error occurred while flushing/closing MapR Streams producer. " + e.getMessage());
                // Retry once
                try {
                    producer.close();
                    LOG.info("MapR Streams producer successfully flushed and closed after retry!");
                } catch (Exception ex) {
                    LOG.error("An error occurred while flushing/closing MapR Streams producer. " + ex.getMessage());
                }
            }
        } else {
            LOG.error("MapR Streams producer was not initialized and thus cannot be closed");
        }
    }

    private static Properties getProperties() {

        Properties producerProperties = new Properties();
        producerProperties.setProperty(QueueServiceConstants.SERVERS, configuration.getString(QueueServiceConstants.KAFKA_CONSUMER_PREFIX + QueueServiceConstants.SERVERS));
        producerProperties.setProperty(QueueServiceConstants.KEY_SERIALIZER, configuration.getString(QueueServiceConstants.KAFKA_PRODUCER_PREFIX + QueueServiceConstants.KEY_SERIALIZER, QueueServiceConstants.KEY_SERIALIZER_DEFAULT));
        producerProperties.setProperty(QueueServiceConstants.VALUE_SERIALIZER, configuration.getString(QueueServiceConstants.KAFKA_PRODUCER_PREFIX + QueueServiceConstants.VALUE_SERIALIZER, QueueServiceConstants.VALUE_SERIALIZER_DEFAULT));

        return producerProperties;
    }
}
