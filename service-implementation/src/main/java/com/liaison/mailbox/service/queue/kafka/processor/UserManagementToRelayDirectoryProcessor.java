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
import com.liaison.mailbox.service.directory.DirectoryService;
import com.liaison.mailbox.service.thread.pool.AsyncProcessThreadPool;

/**
 * UserManagementToRelayDirectory processor for Consumer usage
 *
 */
public class UserManagementToRelayDirectoryProcessor implements KafkaTextMessageProcessor {

    @Override
    public void processMessage(String message, String topic) {
        AsyncProcessThreadPool.getExecutorService().submit(new DirectoryService(message));
    }

    @Override
    public void logConsumeEvent(String message, String topic) {
        //No global process ID available so cannot log
    }
}
