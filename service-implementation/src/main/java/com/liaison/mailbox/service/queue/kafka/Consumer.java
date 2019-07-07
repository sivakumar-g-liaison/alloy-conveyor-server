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
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.enums.DeploymentType;
import com.liaison.threadmanagement.LiaisonExecutorServiceDetail;
import com.liaison.threadmanagement.LiaisonExecutorServiceManager;
import com.liaison.threadmanagement.LiaisonExecutorServiceRegistrar;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.liaison.mailbox.service.queue.kafka.QueueServiceConstants.AUTO_OFFSET_RESET;
import static com.liaison.mailbox.service.queue.kafka.QueueServiceConstants.AUTO_OFFSET_RESET_DEFAULT;
import static com.liaison.mailbox.service.queue.kafka.QueueServiceConstants.GROUP_ID;
import static com.liaison.mailbox.service.queue.kafka.QueueServiceConstants.KAFKA_CONSUMER_PREFIX;
import static com.liaison.mailbox.service.queue.kafka.QueueServiceConstants.KAFKA_FILE_EVENT_READER_CONSUMER_STREAM;
import static com.liaison.mailbox.service.queue.kafka.QueueServiceConstants.KAFKA_LEGACY_RELAY_CONSUMER_STREAM;
import static com.liaison.mailbox.service.queue.kafka.QueueServiceConstants.KAFKA_RELAY_CONSUMER_STREAM;
import static com.liaison.mailbox.service.queue.kafka.QueueServiceConstants.KAFKA_TOPIC_NAME_CREATE_DEFAULT;
import static com.liaison.mailbox.service.queue.kafka.QueueServiceConstants.KAFKA_TOPIC_NAME_DELETE_DEFAULT;
import static com.liaison.mailbox.service.queue.kafka.QueueServiceConstants.KAFKA_TOPIC_NAME_FS_EVENT_DEFAULT;
import static com.liaison.mailbox.service.queue.kafka.QueueServiceConstants.KAFKA_TOPIC_NAME_FS_EVENT_LOWSECURE;
import static com.liaison.mailbox.service.queue.kafka.QueueServiceConstants.KEY_DESERIALIZER;
import static com.liaison.mailbox.service.queue.kafka.QueueServiceConstants.KEY_DESERIALIZER_DEFAULT;
import static com.liaison.mailbox.service.queue.kafka.QueueServiceConstants.VALUE_DESERIALIZER;
import static com.liaison.mailbox.service.queue.kafka.QueueServiceConstants.VALUE_DESERIALIZER_DEFAULT;

/**
 * Kafka consumer to consume message from topic.
 *
 */
public class Consumer extends ThreadPoolExecutor {

    private static final Logger LOG = LogManager.getLogger(Consumer.class);
    private static final DecryptableConfiguration CONFIGURATION = LiaisonArchaiusConfiguration.getInstance();

    private static final String STREAM_CREATE_DEFAULT = CONFIGURATION.getString(KAFKA_RELAY_CONSUMER_STREAM)
            + CONFIGURATION.getString(KAFKA_TOPIC_NAME_CREATE_DEFAULT);
    private static final String STREAM_DELETE_DEFAULT = CONFIGURATION.getString(KAFKA_RELAY_CONSUMER_STREAM)
            + CONFIGURATION.getString(KAFKA_TOPIC_NAME_DELETE_DEFAULT);
    private static final String STREAM_CREATE_LEGACY = CONFIGURATION.getString(KAFKA_LEGACY_RELAY_CONSUMER_STREAM)
            + CONFIGURATION.getString(KAFKA_TOPIC_NAME_CREATE_DEFAULT);
    private static final String STREAM_DELETE_LEGACY = CONFIGURATION.getString(KAFKA_LEGACY_RELAY_CONSUMER_STREAM)
            + CONFIGURATION.getString(KAFKA_TOPIC_NAME_DELETE_DEFAULT);
    private static final String FS_EVENT_STREAM = CONFIGURATION.getString(KAFKA_FILE_EVENT_READER_CONSUMER_STREAM)
            + CONFIGURATION.getString(KAFKA_TOPIC_NAME_FS_EVENT_DEFAULT);
    private static final String FS_EVENT_STREAM_LEGACY = CONFIGURATION.getString(KAFKA_FILE_EVENT_READER_CONSUMER_STREAM)
            + CONFIGURATION.getString(KAFKA_TOPIC_NAME_FS_EVENT_LOWSECURE);
    
    private static final int DEFAULT_KAFKA_CONSUMER_THREAD_POOL_SIZE = 10;
    private static final int DEFAULT_KAFKA_CONSUMER_KEEPALIVE_MINUTES = 1;
    private static final int DEFAULT_KAFKA_CONSUMER_TIMEOUT = 200;

    private static final String PROPERTY_KAFKA_CONSUMER_THREADPOOL_SIZE = "com.liaison.mailbox.kafka.consumer.threadpool.size";
    private static final String PROPERTY_KAFKA_CONSUMER_KEEPALIVE_MINUTES = "com.liaison.mailbox.kafka.consumer.threadpool.keepalive.minutes";
    private static final String PROPERTY_KAFKA_CONSUMER_COREPOOLSIZE = "com.liaison.mailbox.kafka.consumer.threadpool.corepoolsize";
    private static final String PROPERTY_KAFKA_CONSUMER_TIMEOUT = "kafka.consumer.timeout";
    private static final String KAFKA_CONSUMER_THREADPOOL_NAME = "g2-pool-kafka-consumer";

    private static int keepAlive = CONFIGURATION.getInt(PROPERTY_KAFKA_CONSUMER_KEEPALIVE_MINUTES, DEFAULT_KAFKA_CONSUMER_KEEPALIVE_MINUTES);

    private static int KAKFA_CONSUMER_THREAD_POOL_SIZE;
    private static int CORE_POOL_SIZE;
    private static int TIMEOUT;
    
    private static KafkaConsumer<String, String> consumer = null;

    static {
        KAKFA_CONSUMER_THREAD_POOL_SIZE = LiaisonArchaiusConfiguration.getInstance().getInt(PROPERTY_KAFKA_CONSUMER_THREADPOOL_SIZE, DEFAULT_KAFKA_CONSUMER_THREAD_POOL_SIZE);
        int defaultCorePoolSize = Math.round(KAKFA_CONSUMER_THREAD_POOL_SIZE /2);
        CORE_POOL_SIZE = LiaisonArchaiusConfiguration.getInstance().getInt(PROPERTY_KAFKA_CONSUMER_COREPOOLSIZE, defaultCorePoolSize);
        TIMEOUT = LiaisonArchaiusConfiguration.getInstance().getInt(PROPERTY_KAFKA_CONSUMER_TIMEOUT, DEFAULT_KAFKA_CONSUMER_TIMEOUT);
    }

    public Consumer() {

        super(CORE_POOL_SIZE, KAKFA_CONSUMER_THREAD_POOL_SIZE, keepAlive, TimeUnit.MINUTES, new LinkedBlockingQueue<>());
        LiaisonExecutorServiceDetail esd = new LiaisonExecutorServiceDetail(KAFKA_CONSUMER_THREADPOOL_NAME, this);
        LiaisonExecutorServiceRegistrar.INSTANCE.registerExecutor(KAFKA_CONSUMER_THREADPOOL_NAME, esd);

        consumer = new KafkaConsumer<>(getProperties());

        // It will called if the type is not conveyor. We have to check relay and legacy relay;
        String deploymentType = CONFIGURATION.getString(MailBoxConstants.DEPLOYMENT_TYPE, DeploymentType.RELAY.getValue());
        if (DeploymentType.RELAY.getValue().equals(deploymentType)) {
            consumer.subscribe(Arrays.asList(STREAM_CREATE_DEFAULT, STREAM_DELETE_DEFAULT, FS_EVENT_STREAM));
        } else {
            consumer.subscribe(Arrays.asList(STREAM_CREATE_LEGACY, STREAM_DELETE_LEGACY, FS_EVENT_STREAM_LEGACY));
        }
        
        //Shutdown hook to stop the kakfa consumer during JVM shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                LiaisonExecutorServiceManager.INSTANCE.bleed(KAFKA_CONSUMER_THREADPOOL_NAME);
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            } finally {
                LOG.info("Consumer Shutdown hook called");
            }
        }));
    }

    public void consume() {

        submit(() -> {
            try {
                while (true) {
                    ConsumerRecords<String, String> records = consumer.poll(TIMEOUT);
                    for (ConsumerRecord<String, String> record : records) {
                        submit(new KafkaMessageService(record.value()));
                    }
                }
            } catch (WakeupException e) {
                // do nothing we are shutting down
                LOG.error("MapR Streams consumer shutting down!");
            }
        });
    }

    @Override
    public void shutdown() {

        // shutting down the consumer
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
        //shutting down the executor service
        super.shutdown();
    }

    private static Properties getProperties() {

        Properties properties = new Properties();
        // The client will make use of all servers irrespective of which servers are specified here for bootstrapping
        // Refer here for more details : https://kafka.apache.org/documentation/
        properties.put(KEY_DESERIALIZER, CONFIGURATION.getString(KAFKA_CONSUMER_PREFIX + KEY_DESERIALIZER, KEY_DESERIALIZER_DEFAULT));
        properties.put(VALUE_DESERIALIZER, CONFIGURATION.getString(KAFKA_CONSUMER_PREFIX + VALUE_DESERIALIZER, VALUE_DESERIALIZER_DEFAULT));
        properties.put(AUTO_OFFSET_RESET, CONFIGURATION.getString(KAFKA_CONSUMER_PREFIX + AUTO_OFFSET_RESET, AUTO_OFFSET_RESET_DEFAULT));
        properties.put(GROUP_ID, CONFIGURATION.getString(KAFKA_CONSUMER_PREFIX + GROUP_ID));

        return properties;
    }

}