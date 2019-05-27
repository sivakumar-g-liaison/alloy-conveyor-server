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

import static com.liaison.mailbox.MailBoxConstants.DEPLOYMENT_APP_ID;
import static com.liaison.mailbox.MailBoxConstants.TOPIC_RELATIVE_RELAY_TOPIC_SUFFIX;
import static com.liaison.mailbox.service.util.MailBoxUtil.CONFIGURATION;
import static com.liaison.mailbox.service.util.MailBoxUtil.QUEUE_SERVICE_ENABLED;

import com.liaison.commons.messagebus.SendClient;
import com.liaison.commons.messagebus.client.exceptions.ClientUnavailableException;
import com.liaison.commons.messagebus.queue.QueueTextSendClient;
import com.liaison.mailbox.service.queue.kafka.Producer;

public class RelativeRelaySendQueue implements AutoCloseable {

    private static final String QUEUE_NAME = "relativeRelay";
    private static SendClient sendClient = new QueueTextSendClient(QUEUE_NAME);

    private static final String TOPIC_SUFFIX = CONFIGURATION.getString(TOPIC_RELATIVE_RELAY_TOPIC_SUFFIX);

    private static SendClient getInstance() {
        return sendClient;
    }

    private RelativeRelaySendQueue() {
    }

    @Override
    public void close() throws Exception {
        sendClient.close();
    }

    public static void post(String message) throws ClientUnavailableException {
        if (QUEUE_SERVICE_ENABLED) {
            Producer.produceMessageToQS(message, DEPLOYMENT_APP_ID, TOPIC_SUFFIX, 0L);
        } else {
            getInstance().sendMessage(message);
        }
    }
}
