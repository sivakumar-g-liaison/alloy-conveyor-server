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


import com.liaison.commons.messagebus.hornetq.HornetQRecoveringCoreSendClient;

/**
 * Queue where the swept file details are enqueued as worktickets. 
 * 
 * Created by jeremyfranklin-ross on 7/17/14.
 */
public class SweeperQueue extends HornetQRecoveringCoreSendClient {

    public static final String QUEUE_NAME = "sweeper";
    private static SweeperQueue ourInstance = new SweeperQueue();

    public static SweeperQueue getInstance() {
        return ourInstance;
    }

    private SweeperQueue() {
         super(QUEUE_NAME);
    }
}
