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

import com.liaison.commons.messagebus.hornetq.jms.HornetQJMSRoundRobinReceiveClient;

/**
*
* @author OFS
*
*/
public class ServiceBrokerToMailboxWorkTicketQueue extends HornetQJMSRoundRobinReceiveClient {

	 public static final String QUEUE_NAME = "processedPayload";
     private static ServiceBrokerToMailboxWorkTicketQueue ourInstance = new ServiceBrokerToMailboxWorkTicketQueue();

     public static ServiceBrokerToMailboxWorkTicketQueue getInstance() {
         return ourInstance;
     }

     private ServiceBrokerToMailboxWorkTicketQueue() {
        super(QUEUE_NAME);
     }
}
