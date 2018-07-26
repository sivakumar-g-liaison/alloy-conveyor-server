/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.queue.kafka.processor;

import com.liaison.commons.messagebus.common.KafkaTextMessageProcessor;
import com.liaison.mailbox.service.core.ProcessorExecutionConfigurationService;
import com.liaison.mailbox.service.thread.pool.AsyncProcessThreadPool;
import com.liaison.mailbox.service.topic.consumer.MailBoxTopicMessageConsumer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * MailboxTopicMessageProcessor handles broadcast message processing
 *
 */
public class MailboxTopicMessageProcessor implements KafkaTextMessageProcessor {

    private final Logger LOGGER = LogManager.getLogger(MailBoxTopicMessageConsumer.class);

    @Override
    public void processMessage(String message, String topic) {
        LOGGER.debug("Message received from Topic");
        AsyncProcessThreadPool.getExecutorService().submit(new ProcessorExecutionConfigurationService(message));
    }

    @Override
    public void logConsumeEvent(String message, String topic) {
        //Cannot log because there's no globalProcessId available
    }
}
