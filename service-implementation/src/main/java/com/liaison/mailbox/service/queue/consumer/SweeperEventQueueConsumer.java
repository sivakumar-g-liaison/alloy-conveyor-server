/**
 * Copyright 2019 Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.queue.consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.commons.messagebus.queue.QueueTextMessageProcessor;
import com.liaison.mailbox.service.core.SweeperEventExecutionService;
import com.liaison.mailbox.service.thread.pool.SweeperEventProcessThreadPool;

public class SweeperEventQueueConsumer implements QueueTextMessageProcessor {

    private static final Logger LOGGER = LogManager.getLogger(SweeperEventQueueConsumer.class);

    @Override
    public void processMessage(String message) {
        SweeperEventProcessThreadPool.getExecutorService().submit(new SweeperEventExecutionService(message));
    }
}
