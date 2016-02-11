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
import com.liaison.commons.messagebus.queue.QueueTextSendClient;

/**
 * Owner of singleton SendClient
 * 
 * Created by jeremyfranklin-ross on 7/17/14.
 */
public class SweeperQueueSendClient implements AutoCloseable {

    public static final String QUEUE_NAME = "sweeper";
    private static SendClient sendClient = new QueueTextSendClient(QUEUE_NAME);

    public static SendClient getInstance() {
        return sendClient;
    }


    @Override
    public void close() throws Exception {
        sendClient.close();
    }
}
