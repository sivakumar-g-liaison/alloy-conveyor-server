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

import com.liaison.commons.jaxb.JAXBUtility;
import com.liaison.commons.util.settings.DecryptableConfiguration;
import com.liaison.commons.util.settings.LiaisonArchaiusConfiguration;
import com.liaison.dto.queue.WorkTicket;
import com.liaison.mailbox.service.queue.kafka.KafkaMessageService.KafkaMessageType;
import com.liaison.mailbox.service.util.MailBoxUtil;
import com.liaison.usermanagement.service.dto.DirectoryMessageDTO;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Properties;

import javax.xml.bind.JAXBException;

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
    private static KafkaProducer<String, String> kafkaProducer;
    private static String TOPIC_NAME = configuration.getString(KAFKA_STREAM)
            + configuration.getString(KAFKA_PRODUCER_TOPIC_NAME);

    private static Producer producer = null;
    
    private Producer() {
        
    }
    
    public static Producer getInstance() {
        if (null == producer) {
            synchronized (Producer.class) {
                if (null == producer) {
                    producer = new Producer();
                }
            }
        }
        return producer;
    }
    
    public void produce(String message) {
        
        if (MailBoxUtil.isEmpty(message)) {
            throw new RuntimeException("Unable to send message to topic " + TOPIC_NAME + ". " + " Message is empty");
        }

        LOG.info("MapR Streams PRODUCER message to send" + message);

        try {
            kafkaProducer = new KafkaProducer<>(getProperties());
            kafkaProducer.send(new ProducerRecord<>(TOPIC_NAME, message));
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
        if (kafkaProducer != null) {

            try {
                kafkaProducer.close();
                LOG.info("MapR Streams PRODUCER successfully flushed and closed!");
            } catch (Exception e) {
                LOG.error("An error occurred while flushing/closing MapR Streams PRODUCER. " + e.getMessage(), e);
                // Retry once
                try {
                    kafkaProducer.close();
                    LOG.info("MapR Streams PRODUCER successfully flushed and closed after retry!");
                } catch (Exception ex) {
                    LOG.error("An error occurred while flushing/closing MapR Streams PRODUCER. " + ex.getMessage(), e);
                }
            }
        } else {
            LOG.error("MapR Streams PRODUCER was not initialized and thus cannot be closed");
        }
    }

    /**
     * To send execute Directory Operation details.
     * @param directoryCreation
     * @param message
     */
    public void produce(KafkaMessageType directoryCreationOrDeletion, DirectoryMessageDTO message) {
        KafkaMessage kafkaMessage = new KafkaMessage();
        kafkaMessage.setMessageType(directoryCreationOrDeletion);
        kafkaMessage.setDirectoryMessageDTO(message);
        produce(marshalToJSON(kafkaMessage));
    }

    /**
     * To send workticket for filewriter operation
     * @param filewriterCreate
     * @param workTicket
     */
    public void produce(KafkaMessageType filewriterCreate, WorkTicket workTicket) {
        KafkaMessage kafkaMessage = new KafkaMessage();
        kafkaMessage.setMessageType(filewriterCreate);
        kafkaMessage.setFilewriterWorkTicket(workTicket);
        produce(marshalToJSON(kafkaMessage));
    }

    /**
     * To send local folders creation details.
     * @param directoryCreation
     * @param processor
     */
    public void produce(KafkaMessageType directoryCreation, String processorGuid) {
        KafkaMessage kafkaMessage = new KafkaMessage();
        kafkaMessage.setMessageType(directoryCreation);
        kafkaMessage.setProcessorGuid(processorGuid);
        produce(marshalToJSON(kafkaMessage));
    }
    
    private String marshalToJSON(KafkaMessage kafkaMessage) {
        
        try {
            return JAXBUtility.marshalToJSON(kafkaMessage);
        } catch (JAXBException | IOException e) {
            LOG.error(e.getMessage(), e);
        }
        return null;
    }
}
