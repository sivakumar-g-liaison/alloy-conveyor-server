/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall 
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.com.liaison.queue;

import com.liaison.commons.messagebus.hornetq.HornetQRecoveringCoreSendReceiveClient;

/**
 * Created by jeremyfranklin-ross on 7/17/14.
 */
public class ProcessorQueue extends HornetQRecoveringCoreSendReceiveClient {

    public static final String QUEUE_NAME = "processor";
    private static ProcessorQueue ourInstance = new ProcessorQueue();

    public static ProcessorQueue getInstance() {
        return ourInstance;
    }

    private ProcessorQueue() {
         super(QUEUE_NAME);
    }

}
