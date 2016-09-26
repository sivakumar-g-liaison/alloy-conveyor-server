/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.queue.sender;


import com.liaison.commons.messagebus.SendClient;
import com.liaison.commons.messagebus.queue.QueueTextSendClient;

/**
 *
 */
public class MailboxToServiceBrokerWorkResultQueue implements AutoCloseable {

    public static final String QUEUE_NAME = "mailboxWorkResult";
    private static SendClient sendClient = new QueueTextSendClient(QUEUE_NAME);

    public static SendClient getInstance() {
        return sendClient;
    }

    private MailboxToServiceBrokerWorkResultQueue() {
    }

    @Override
    public void close() throws Exception {
        sendClient.close();
    }
}

