/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall 
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.queue.kafka;


import com.liaison.commons.messagebus.SendClient;

/**
 * Owner of singleton SendClient for kafka
 */
public class KafkaSendQueue implements AutoCloseable {

    private static SendClient sendClient = new KafkaSendClient();

    public static SendClient getInstance() {
        return sendClient;
    }

    private KafkaSendQueue() {
        //NOP
    }

    @Override
    public void close() throws Exception {
        sendClient.close();
    }
}

