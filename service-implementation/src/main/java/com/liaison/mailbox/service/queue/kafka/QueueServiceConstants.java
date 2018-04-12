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

public interface QueueServiceConstants {

    String KAFKA_RELAY_PRODUCER_STREAM = "kafka.relay.producer.stream";
    String KAFKA_RELAY_CONSUMER_STREAM = "kafka.relay.consumer.stream";
    String KAFKA_FILE_EVENT_READER_CONSUMER_STREAM= "kafka.fileeventreader.consumer.stream";
    
    String KAFKA_TOPIC_NAME_CREATE_DEFAULT = "kafka.topic.name.create.default";
    String KAFKA_TOPIC_NAME_DELETE_DEFAULT = "kafka.topic.name.delete.default";
    
    String KAFKA_TOPIC_NAME_CREATE_LOWSECURE = "kafka.topic.name.create.lowsecure";
    String KAFKA_TOPIC_NAME_DELETE_LOWSECURE = "kafka.topic.name.delete.lowsecure";

    String KAFKA_TOPIC_NAME_FS_EVENT_DEFAULT = "kafka.topic.name.fs.event.default";
    String KAFKA_TOPIC_NAME_FS_EVENT_LOWSECURE = "kafka.topic.name.fs.event.lowsecure";

    String KAFKA_CONSUMER_PREFIX = "kafka.consumer.";
    String GROUP_ID = "group.id";
    String AUTO_COMMIT = "enable.auto.commit";
    String KEY_DESERIALIZER = "key.deserializer";
    String VALUE_DESERIALIZER = "value.deserializer";
    String AUTO_OFFSET_RESET = "auto.offset.reset";
    String TIMEOUT = "timeout";
    String DEFAULT_STREAM_NAME = "streams.consumer.default.stream";

    String KEY_DESERIALIZER_DEFAULT = "org.apache.kafka.common.serialization.StringDeserializer";
    String VALUE_DESERIALIZER_DEFAULT = "org.apache.kafka.common.serialization.StringDeserializer";

    String KAFKA_PRODUCER_PREFIX = "kafka.producer.";
    String KEY_SERIALIZER = "key.serializer";
    String VALUE_SERIALIZER = "value.serializer";
    String KEY_SERIALIZER_DEFAULT = "org.apache.kafka.common.serialization.StringSerializer";
    String VALUE_SERIALIZER_DEFAULT = "org.apache.kafka.common.serialization.StringSerializer";

}
