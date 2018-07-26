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
import com.liaison.mailbox.service.core.MailBoxService;
import com.liaison.mailbox.service.thread.pool.AsyncProcessThreadPool;

/**
 * Mailbox processor for Consumer usage
 *
 */
public class MailboxProcessor implements KafkaTextMessageProcessor {

    @Override
    public void processMessage(String message, String topic) {
        AsyncProcessThreadPool.getExecutorService().submit(new MailBoxService(message, MailBoxService.QueueMessageType.TRIGGERPROFILEREQUEST));
    }

    @Override
    public void logConsumeEvent(String message, String topic) {
        //Cannot log because there's no globalProcessId available
    }
}
