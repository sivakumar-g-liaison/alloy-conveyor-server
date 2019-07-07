/**
 * Copyright 2016 Liaison Technologies, Inc.
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.mailbox.service.topic.consumer;

import com.liaison.commons.messagebus.topic.TopicTextMessageProcessor;
import com.liaison.mailbox.service.core.ProcessorExecutionConfigurationService;
import com.liaison.mailbox.service.thread.pool.AsyncProcessThreadPool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by VNagarajan on 11/1/2016.
 */
public class MailBoxTopicMessageConsumer implements TopicTextMessageProcessor {

    private final Logger logger = LogManager.getLogger(MailBoxTopicMessageConsumer.class);

    /**
     * Process the message.
     */
    @Override
    public void processMessage(String message) {

        if (message != null) {
            logger.debug("Message received from Topic");
            AsyncProcessThreadPool.getExecutorService().submit(new ProcessorExecutionConfigurationService(message));
        }
    }
}
