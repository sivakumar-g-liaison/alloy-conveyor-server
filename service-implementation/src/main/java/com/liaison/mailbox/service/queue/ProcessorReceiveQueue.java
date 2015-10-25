/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall 
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.queue;


import com.liaison.commons.messagebus.hornetq.HornetQRecoveringCoreReceiveClient;

/**
 * Created by jeremyfranklin-ross on 7/17/14.
 */
public class ProcessorReceiveQueue extends HornetQRecoveringCoreReceiveClient {

    public static final String QUEUE_NAME = "processor";
    private static ProcessorReceiveQueue ourInstance = new ProcessorReceiveQueue();

    public static ProcessorReceiveQueue getInstance() {
        return ourInstance;
    }

    private ProcessorReceiveQueue() {
         super(QUEUE_NAME);
    }

}
