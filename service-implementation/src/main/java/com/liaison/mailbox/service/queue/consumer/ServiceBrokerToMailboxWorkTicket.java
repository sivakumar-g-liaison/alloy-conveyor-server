/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall 
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.queue.consumer;

import com.liaison.commons.messagebus.hornetq.HornetQRecoveringCoreReceiveClient;

public class ServiceBrokerToMailboxWorkTicket extends HornetQRecoveringCoreReceiveClient {

	 public static final String QUEUE_NAME = "processedPayload";
     private static ServiceBrokerToMailboxWorkTicket ourInstance = new ServiceBrokerToMailboxWorkTicket();

     public static ServiceBrokerToMailboxWorkTicket getInstance() {
         return ourInstance;
     }

     private ServiceBrokerToMailboxWorkTicket() {
        super(QUEUE_NAME);
     }
}
