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
import com.liaison.mailbox.enums.FailoverMessageType;
import com.liaison.mailbox.service.queue.kafka.KafkaMessageService.KafkaMessageType;
import com.liaison.mailbox.service.util.MailBoxUtil;
import com.liaison.usermanagement.service.dto.DirectoryMessageDTO;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.Properties;

import static com.liaison.mailbox.MailBoxConstants.FAILOVER_MSG_TYPE;
import static com.liaison.mailbox.MailBoxConstants.GLOBAL_PROCESS_ID;
import static com.liaison.mailbox.MailBoxConstants.KEY_FILE_NAME;
import static com.liaison.mailbox.MailBoxConstants.KEY_FILE_PATH;
import static com.liaison.mailbox.MailBoxConstants.KEY_OVERWRITE;
import static com.liaison.mailbox.MailBoxConstants.PROPERTY_SKIP_KAFKA_QUEUE;
import static com.liaison.mailbox.MailBoxConstants.RETRY_COUNT;
import static com.liaison.mailbox.MailBoxConstants.TRIGGER_FILE;
import static com.liaison.mailbox.MailBoxConstants.URI;
import static com.liaison.mailbox.service.queue.kafka.QueueServiceConstants.KAFKA_PRODUCER_PREFIX;
import static com.liaison.mailbox.service.queue.kafka.QueueServiceConstants.KAFKA_RELAY_PRODUCER_STREAM;
import static com.liaison.mailbox.service.queue.kafka.QueueServiceConstants.KAFKA_TOPIC_NAME_CREATE_DEFAULT;
import static com.liaison.mailbox.service.queue.kafka.QueueServiceConstants.KAFKA_TOPIC_NAME_CREATE_LOWSECURE;
import static com.liaison.mailbox.service.queue.kafka.QueueServiceConstants.KAFKA_TOPIC_NAME_DELETE_DEFAULT;
import static com.liaison.mailbox.service.queue.kafka.QueueServiceConstants.KAFKA_TOPIC_NAME_DELETE_LOWSECURE;
import static com.liaison.mailbox.service.queue.kafka.QueueServiceConstants.KEY_SERIALIZER;
import static com.liaison.mailbox.service.queue.kafka.QueueServiceConstants.KEY_SERIALIZER_DEFAULT;
import static com.liaison.mailbox.service.queue.kafka.QueueServiceConstants.META_MAX_AGE_MS;
import static com.liaison.mailbox.service.queue.kafka.QueueServiceConstants.META_MAX_AGE_MS_DEFAULT;
import static com.liaison.mailbox.service.queue.kafka.QueueServiceConstants.STREAMS_BUFFER_MAX_TIME_MS;
import static com.liaison.mailbox.service.queue.kafka.QueueServiceConstants.STREAMS_BUFFER_MAX_TIME_MS_DEFAULT;
import static com.liaison.mailbox.service.queue.kafka.QueueServiceConstants.VALUE_SERIALIZER;
import static com.liaison.mailbox.service.queue.kafka.QueueServiceConstants.VALUE_SERIALIZER_DEFAULT;

/**
 * To produce message to Kafka
 */
public class Producer {

    private static final Logger LOG = LogManager.getLogger(Producer.class);
    private static DecryptableConfiguration configuration = LiaisonArchaiusConfiguration.getInstance();
    private static KafkaProducer<String, String> KAFKA_PRODUCER;
    private static String TOPIC_NAME_CREATE;
    private static String TOPIC_NAME_DELETE;

    static {

        String deploymentType = configuration.getString(MailBoxConstants.DEPLOYMENT_TYPE, DeploymentType.RELAY.getValue());

        // We have to check relay and legacy relay; No need for check conveyor.
        if (DeploymentType.RELAY.getValue().equals(deploymentType)) {
            TOPIC_NAME_CREATE = configuration.getString(KAFKA_RELAY_PRODUCER_STREAM) + configuration.getString(KAFKA_TOPIC_NAME_CREATE_DEFAULT);
            TOPIC_NAME_DELETE = configuration.getString(KAFKA_RELAY_PRODUCER_STREAM) + configuration.getString(KAFKA_TOPIC_NAME_DELETE_DEFAULT);
        } else {
            TOPIC_NAME_CREATE = configuration.getString(KAFKA_RELAY_PRODUCER_STREAM) + configuration.getString(KAFKA_TOPIC_NAME_CREATE_LOWSECURE);
            TOPIC_NAME_DELETE = configuration.getString(KAFKA_RELAY_PRODUCER_STREAM) + configuration.getString(KAFKA_TOPIC_NAME_DELETE_LOWSECURE);
        }

        if (!configuration.getBoolean(PROPERTY_SKIP_KAFKA_QUEUE, true)) {
            KAFKA_PRODUCER = new KafkaProducer<>(getProperties());
        }
    }

    /**
     * Send message to topic
     *
     * @param message
     * @param topicName
     */
    private static void produce(String message, String topicName) {

        if (configuration.getBoolean(PROPERTY_SKIP_KAFKA_QUEUE, true)) {
            LOG.warn(" SKIP_KAFKA_QUEUE is enabled: Unable to send message to topic");
            return;
        }

        if (MailBoxUtil.isEmpty(message)) {
            throw new RuntimeException("Unable to send message to topic " + topicName + ". " + " Message is empty");
        }

        LOG.info("MapR Streams PRODUCER message to send {}", message);
        try {
            KAFKA_PRODUCER.send(new ProducerRecord<>(topicName, message));
        } catch (Exception e) {
            throw new RuntimeException("Unable to send message to topic " + topicName + ". " + e.getMessage(), e);
        }
    }

    private static Properties getProperties() {

        Properties producerProperties = new Properties();

        // The client will make use of all servers irrespective of which servers are specified here for bootstrapping
        // Refer here for more details : https://kafka.apache.org/documentation/
        producerProperties.put(KEY_SERIALIZER, configuration.getString(KAFKA_PRODUCER_PREFIX + KEY_SERIALIZER, KEY_SERIALIZER_DEFAULT));
        producerProperties.put(VALUE_SERIALIZER, configuration.getString(KAFKA_PRODUCER_PREFIX + VALUE_SERIALIZER, VALUE_SERIALIZER_DEFAULT));
        producerProperties.put(STREAMS_BUFFER_MAX_TIME_MS, configuration.getInt(KAFKA_PRODUCER_PREFIX + STREAMS_BUFFER_MAX_TIME_MS, STREAMS_BUFFER_MAX_TIME_MS_DEFAULT));
        producerProperties.put(META_MAX_AGE_MS, configuration.getInt(KAFKA_PRODUCER_PREFIX + META_MAX_AGE_MS, META_MAX_AGE_MS_DEFAULT));

        return producerProperties;
    }

    /**
     * To stop send request to the kafka producer.
     */
    public static void stop() {

        if (KAFKA_PRODUCER != null) {

            try {
                KAFKA_PRODUCER.close();
                LOG.info("MapR Streams PRODUCER successfully flushed and closed!");
            } catch (Exception e) {
                LOG.error("An error occurred while flushing/closing MapR Streams PRODUCER. " + e.getMessage(), e);
                // Retry once
                try {
                    KAFKA_PRODUCER.close();
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
     *
     * @param kafkaMessageType type of the message
     * @param message          directory message dto
     */
    public static void produce(KafkaMessageType kafkaMessageType, DirectoryMessageDTO message) {

        KafkaMessage kafkaMessage = new KafkaMessage();
        kafkaMessage.setMessageType(kafkaMessageType);
        kafkaMessage.setDirectoryMessageDTO(message);
        produce(marshalToJSON(kafkaMessage), KafkaMessageType.USERACCOUNT_CREATE.equals(kafkaMessageType) ? TOPIC_NAME_CREATE : TOPIC_NAME_DELETE);
    }

    /**
     * To send workticket for filewriter operation
     *
     * @param kafkaMessageType
     * @param workTicket
     * @throws JSONException
     */
    public static void produce(KafkaMessageType kafkaMessageType, WorkTicket workTicket) throws JSONException {

        produce(kafkaMessageType,
                workTicket.getPayloadURI(),
                workTicket.getGlobalProcessId(),
                workTicket.getFileName(),
                workTicket.getAdditionalContextItem(MailBoxConstants.KEY_FILE_PATH).toString(),
                workTicket.getAdditionalContextItem(MailBoxConstants.KEY_OVERWRITE).toString().toLowerCase(),
                false);
    }

    /**
     * For file replications
     * 
     * @param kafkaMessageType
     * @param payloadURI
     * @param globalProcessId
     * @param fileName
     * @param filePath
     * @param overwrite
     * @param triggerFile
     * @throws JSONException
     */
    public static void produce(KafkaMessageType kafkaMessageType,
                                String payloadURI,
                                String globalProcessId,
                                String fileName,
                                String filePath,
                                String overwrite,
                                boolean triggerFile) throws JSONException {

        KafkaMessage kafkaMessage = new KafkaMessage();
        kafkaMessage.setMessageType(kafkaMessageType);

        JSONObject requestObj = new JSONObject();
        requestObj.put(URI, payloadURI);
        requestObj.put(GLOBAL_PROCESS_ID, globalProcessId);
        requestObj.put(KEY_FILE_NAME, fileName);
        requestObj.put(KEY_FILE_PATH, filePath);
        requestObj.put(KEY_OVERWRITE, overwrite);
        requestObj.put(RETRY_COUNT, 0);
        requestObj.put(TRIGGER_FILE, triggerFile);
        requestObj.put(FAILOVER_MSG_TYPE, FailoverMessageType.FILE);

        kafkaMessage.setFileWriterMsg(requestObj.toString());
        produce(marshalToJSON(kafkaMessage), TOPIC_NAME_CREATE);
    }

    /**
     * To send local folders creation details.
     *
     * @param kafkaMessageType
     * @param dirAbsolutePath
     */
    public static void produce(KafkaMessageType kafkaMessageType, String dirAbsolutePath) {
        KafkaMessage kafkaMessage = new KafkaMessage();
        kafkaMessage.setMessageType(kafkaMessageType);
        kafkaMessage.setDirAbsolutePath(dirAbsolutePath);
        produce(marshalToJSON(kafkaMessage), TOPIC_NAME_CREATE);
    }

    /**
     * Sends file delete notification to other datacenter
     *
     * @param kafkaMessageType
     * @param deleteMessage
     * @param datacenter
     */
    public static void produce(KafkaMessageType kafkaMessageType, String deleteMessage, String datacenter) {
        KafkaMessage kafkaMessage = new KafkaMessage();
        kafkaMessage.setMessageType(kafkaMessageType);
        kafkaMessage.setFileDeleteMessage(deleteMessage);
        kafkaMessage.setDatacenter(datacenter);
        produce(marshalToJSON(kafkaMessage), TOPIC_NAME_DELETE);
    }

    private static String marshalToJSON(KafkaMessage kafkaMessage) {

        try {
            return JAXBUtility.marshalToJSON(kafkaMessage);
        } catch (JAXBException | IOException e) {
            LOG.error(e.getMessage(), e);
        }
        return null;
    }
}
