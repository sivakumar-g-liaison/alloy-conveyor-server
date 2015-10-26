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

import com.liaison.commons.messagebus.hornetq.jms.HornetQJMSRoundRobinSendClient;

/**
 *
 * @author OFS
 *
 */
public class DummyJMSClientQueue extends HornetQJMSRoundRobinSendClient {

	 public static final String QUEUE_NAME = "processedPayload";
     private static DummyJMSClientQueue ourInstance = new DummyJMSClientQueue();

     public static DummyJMSClientQueue getInstance() {
         return ourInstance;
     }

     private DummyJMSClientQueue() {
        super(QUEUE_NAME);
     }

}
