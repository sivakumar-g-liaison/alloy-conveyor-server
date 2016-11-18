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
import com.liaison.commons.messagebus.topic.TopicTextSendClient;

/**
 * Created by VNagarajan on 11/1/2016.
 */
public class MailBoxTopicMessageProducer implements AutoCloseable {

    public static final String TOPIC_NAME = "mailboxProcessorTopic";
    private static SendClient sendClient = new TopicTextSendClient(TOPIC_NAME);

    public static SendClient getInstance() {
        return sendClient;
    }

    @Override
    public void close() throws Exception {
        sendClient.close();
    }
}
