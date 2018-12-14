/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.queue.kafka.processor;

import com.liaison.commons.messagebus.common.KafkaTextMessageProcessor;
import com.liaison.mailbox.service.core.ProcessorAffinityService;
import com.liaison.mailbox.service.thread.pool.AsyncProcessThreadPool;

public class RunningProcessorRetryProcessor implements KafkaTextMessageProcessor {
    
    @Override
    public void processMessage(String message, String topic) {
        AsyncProcessThreadPool.getExecutorService().submit(new ProcessorAffinityService(message));
    }

    @Override
    public void logConsumeEvent(String message, String topic) {
        //Cannot log because there's no globalProcessId available
    }
}