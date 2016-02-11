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
 * Owner of singleton SendClient for "processor" queue
 * 
 * Created by jeremyfranklin-ross on 7/17/14.
 */
public class ProcessorSendQueue implements AutoCloseable {

    public static final String QUEUE_NAME = "processor";
    private static SendClient sendClient = new QueueTextSendClient(QUEUE_NAME);

    public static SendClient getInstance() {
        return sendClient;
    }

    private ProcessorSendQueue() {
        //NOP
    }

    @Override
    public void close() throws Exception {
        sendClient.close();
    }
}

