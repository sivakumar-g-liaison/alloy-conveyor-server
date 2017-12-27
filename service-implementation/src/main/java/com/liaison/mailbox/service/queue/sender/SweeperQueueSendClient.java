/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.mailbox.service.queue.sender;


import com.liaison.commons.messagebus.SendClient;
import com.liaison.commons.messagebus.client.exceptions.ClientUnavailableException;
import com.liaison.commons.messagebus.queue.QueueTextSendClient;
import com.liaison.mailbox.service.util.MailBoxUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static com.liaison.mailbox.service.core.bootstrap.QueueAndTopicProcessInitializer.SKIP_QUEUE_INITIALIZER;

/**
 * Owner of singleton SendClient
 * 
 * Created by jeremyfranklin-ross on 7/17/14.
 */
public class SweeperQueueSendClient implements AutoCloseable {

    private static final String QUEUE_NAME = "sweeper";
    private static final Logger LOGGER = LogManager.getLogger(SweeperQueueSendClient.class);
    private static SendClient SEND_CLIENT = null;

    static {

        if (MailBoxUtil.getEnvironmentProperties().getBoolean(SKIP_QUEUE_INITIALIZER, false)) {
            SEND_CLIENT = new SweeperQueueSendClient.NullSendClient();
        } else {
            SEND_CLIENT = new QueueTextSendClient(QUEUE_NAME);
        }
    }

    public static SendClient getInstance() {
        return SEND_CLIENT;
    }

    @Override
    public void close() throws Exception {
        SEND_CLIENT.close();
    }

    private static class NullSendClient implements SendClient {

        @Override
        public void sendMessage(String message) throws ClientUnavailableException {
            LOGGER.warn("Queue disabled. Unable to push message {}", message);
        }

        @Override
        public void sendMessage(String message, long delay) throws ClientUnavailableException {
            LOGGER.warn("Queue disabled. Unable to push message {}", message);
        }

        @Override
        public void close() throws Exception {

        }
    }
}
