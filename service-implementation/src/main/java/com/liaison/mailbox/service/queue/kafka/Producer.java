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
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.enums.DeploymentType;
import com.liaison.mailbox.service.queue.kafka.KafkaMessageService.KafkaMessageType;
import com.liaison.mailbox.service.util.MailBoxUtil;
import com.liaison.usermanagement.service.dto.DirectoryMessageDTO;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.io.IOException;
import java.util.Properties;

import javax.xml.bind.JAXBException;

import static com.liaison.mailbox.MailBoxConstants.GLOBAL_PROCESS_ID;
import static com.liaison.mailbox.MailBoxConstants.KEY_FILE_NAME;
import static com.liaison.mailbox.MailBoxConstants.KEY_FILE_PATH;
import static com.liaison.mailbox.MailBoxConstants.KEY_OVERWRITE;
import static com.liaison.mailbox.MailBoxConstants.KEY_PROCESSOR_ID;
import static com.liaison.mailbox.MailBoxConstants.PROPERTY_SKIP_KAFKA_QUEUE;
import static com.liaison.mailbox.MailBoxConstants.RETRY_COUNT;
import static com.liaison.mailbox.MailBoxConstants.URI;
import static com.liaison.mailbox.service.queue.kafka.QueueServiceConstants.KAFKA_PRODUCER_PREFIX;
import static com.liaison.mailbox.service.queue.kafka.QueueServiceConstants.KAFKA_TOPIC_NAME_CREATE_DEFAULT;
import static com.liaison.mailbox.service.queue.kafka.QueueServiceConstants.KAFKA_TOPIC_NAME_DELETE_DEFAULT;
import static com.liaison.mailbox.service.queue.kafka.QueueServiceConstants.KAFKA_TOPIC_NAME_CREATE_LOWSECURE;
import static com.liaison.mailbox.service.queue.kafka.QueueServiceConstants.KAFKA_TOPIC_NAME_DELETE_LOWSECURE;
import static com.liaison.mailbox.service.queue.kafka.QueueServiceConstants.KAFKA_RELAY_PRODUCER_STREAM;
import static com.liaison.mailbox.service.queue.kafka.QueueServiceConstants.KEY_SERIALIZER;
import static com.liaison.mailbox.service.queue.kafka.QueueServiceConstants.KEY_SERIALIZER_DEFAULT;
import static com.liaison.mailbox.service.queue.kafka.QueueServiceConstants.VALUE_SERIALIZER;
import static com.liaison.mailbox.service.queue.kafka.QueueServiceConstants.VALUE_SERIALIZER_DEFAULT;

/**
 * To produce message to Kafka
 */
public class Producer {

    private static final Logger LOG = LogManager.getLogger(Producer.class);
    private static DecryptableConfiguration configuration = LiaisonArchaiusConfiguration.getInstance();
    private static KafkaProducer<String, String> kafkaProducer;
    private static String TOPIC_NAME_CREATE;
    private static String TOPIC_NAME_DELETE;

    private static Producer producer = null;

    private Producer() {
        
        String deploymentType = configuration.getString(MailBoxConstants.DEPLOYMENT_TYPE, DeploymentType.RELAY.getValue());
        
        // We have to check relay and legacy relay; No need for check conveyor.
        if (DeploymentType.RELAY.getValue().equals(deploymentType)) {
            TOPIC_NAME_CREATE = configuration.getString(KAFKA_RELAY_PRODUCER_STREAM) + configuration.getString(KAFKA_TOPIC_NAME_CREATE_DEFAULT);
            TOPIC_NAME_DELETE = configuration.getString(KAFKA_RELAY_PRODUCER_STREAM) + configuration.getString(KAFKA_TOPIC_NAME_DELETE_DEFAULT);
        } else {
            TOPIC_NAME_CREATE = configuration.getString(KAFKA_RELAY_PRODUCER_STREAM) + configuration.getString(KAFKA_TOPIC_NAME_CREATE_LOWSECURE);
            TOPIC_NAME_DELETE = configuration.getString(KAFKA_RELAY_PRODUCER_STREAM) + configuration.getString(KAFKA_TOPIC_NAME_DELETE_LOWSECURE);
        }
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
    
    /**
     * Send message to topic
     * 
     * @param message
     * @param topicName
     */
    private void produce(String message, String topicName) {
        
        if (configuration.getBoolean(PROPERTY_SKIP_KAFKA_QUEUE, false)) {
            LOG.error(" SKIP_KAFKA_QUEUE is enabled: Unable to send message to topic");
            return;
        }
        
        if (MailBoxUtil.isEmpty(message)) {
            throw new RuntimeException("Unable to send message to topic " + topicName + ". " + " Message is empty");
        }

        LOG.info("MapR Streams PRODUCER message to send" + message);

        try {
            kafkaProducer = new KafkaProducer<>(getProperties());
            kafkaProducer.send(new ProducerRecord<>(topicName, message));
        } catch (Exception e) {
            throw new RuntimeException("Unable to send message to topic " + topicName + ". " + e.getMessage(), e);
        }
    }

    private static Properties getProperties() {

        Properties producerProperties = new Properties();
        
        // The client will make use of all servers irrespective of which servers are specified here for bootstrapping
        // Refer here for more details : https://kafka.apache.org/documentation/
        producerProperties.setProperty(KEY_SERIALIZER, configuration.getString(KAFKA_PRODUCER_PREFIX + KEY_SERIALIZER, KEY_SERIALIZER_DEFAULT));
        producerProperties.setProperty(VALUE_SERIALIZER, configuration.getString(KAFKA_PRODUCER_PREFIX + VALUE_SERIALIZER, VALUE_SERIALIZER_DEFAULT));
        return producerProperties;
    }

    /**
     * To stop send request to the kafka producer.
     */
    public void stop() {
        
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
     * @param kafkaMessageType type of the message
     * @param message directory message dto
     */
    public void produce(KafkaMessageType kafkaMessageType, DirectoryMessageDTO message) {

        KafkaMessage kafkaMessage = new KafkaMessage();
        kafkaMessage.setMessageType(kafkaMessageType);
        kafkaMessage.setDirectoryMessageDTO(message);
        produce(marshalToJSON(kafkaMessage), KafkaMessageType.USERACCOUNT_CREATE.equals(kafkaMessageType) ? TOPIC_NAME_CREATE : TOPIC_NAME_DELETE);
    }

    /**
     * To send workticket for filewriter operation
     * @param kafkaMessageType
     * @param processorGuid
     * @param workTicket
     * @throws JSONException
     */
    public void produce(KafkaMessageType kafkaMessageType, WorkTicket workTicket, String processorGuid) throws JSONException {

        KafkaMessage kafkaMessage = new KafkaMessage();
        kafkaMessage.setMessageType(kafkaMessageType);

        JSONObject requestObj = new JSONObject();
        requestObj.put(URI, workTicket.getPayloadURI());
        requestObj.put(GLOBAL_PROCESS_ID, workTicket.getGlobalProcessId());
        requestObj.put(KEY_PROCESSOR_ID, processorGuid);
        requestObj.put(KEY_FILE_NAME, workTicket.getFileName());
        requestObj.put(KEY_FILE_PATH, workTicket.getFileName());
        requestObj.put(KEY_OVERWRITE, workTicket.getAdditionalContextItem(MailBoxConstants.KEY_OVERWRITE).toString().toLowerCase());
        requestObj.put(RETRY_COUNT, 0);

        kafkaMessage.setFileWriterMsg(requestObj.toString());
        produce(marshalToJSON(kafkaMessage), TOPIC_NAME_CREATE);
    }

    /**
     * To send local folders creation details.
     * @param kafkaMessageType
     * @param processorGuid
     */
    public void produce(KafkaMessageType kafkaMessageType, String processorGuid) {
        KafkaMessage kafkaMessage = new KafkaMessage();
        kafkaMessage.setMessageType(kafkaMessageType);
        kafkaMessage.setProcessorGuid(processorGuid);
        produce(marshalToJSON(kafkaMessage), TOPIC_NAME_CREATE);
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
