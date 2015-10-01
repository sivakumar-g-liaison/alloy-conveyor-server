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

import com.liaison.commons.messagebus.hornetq.HornetQRecoveringCoreSendClient;

/**
 * Created by jeremyfranklin-ross on 7/17/14.
 */
public class ProcessorSendQueue extends HornetQRecoveringCoreSendClient {

    public static final String QUEUE_NAME = "processor";
    private static ProcessorSendQueue ourInstance = new ProcessorSendQueue();

    public static ProcessorSendQueue getInstance() {
        return ourInstance;
    }

    private ProcessorSendQueue() {
         super(QUEUE_NAME);
    }

}
