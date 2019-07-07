/**
 * Copyright 2016 Liaison Technologies, Inc.
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.mailbox.service.topic.producer;

import com.liaison.commons.messagebus.SendClient;
import com.liaison.commons.messagebus.client.exceptions.ClientUnavailableException;
import com.liaison.commons.messagebus.topic.TopicTextSendClient;
import com.liaison.mailbox.service.queue.kafka.Producer;

import static com.liaison.mailbox.MailBoxConstants.DEPLOYMENT_APP_ID;
import static com.liaison.mailbox.MailBoxConstants.TOPIC_MAILBOX_TOPIC_MESSAGE_DEFAULT_TOPIC_SUFFIX;
import static com.liaison.mailbox.service.util.MailBoxUtil.CONFIGURATION;
import static com.liaison.mailbox.service.util.MailBoxUtil.QUEUE_SERVICE_ENABLED;

/**
 * Created by VNagarajan on 11/1/2016.
 */
public class MailBoxTopicMessageProducer implements AutoCloseable {

    public static final String TOPIC_NAME = "mailboxProcessorTopic";
    private static final String TOPIC_SUFFIX = CONFIGURATION.getString(TOPIC_MAILBOX_TOPIC_MESSAGE_DEFAULT_TOPIC_SUFFIX);
    private static SendClient sendClient = new TopicTextSendClient(TOPIC_NAME);

    public static SendClient getInstance() {
        return sendClient;
    }

    @Override
    public void close() throws Exception {
        sendClient.close();
    }

    public static void post(String message) throws ClientUnavailableException {
        if (QUEUE_SERVICE_ENABLED) {
            Producer.produceMessageToQS(message, DEPLOYMENT_APP_ID, TOPIC_SUFFIX, 0);
        } else {
            getInstance().sendMessage(message);
        }
    }
}
