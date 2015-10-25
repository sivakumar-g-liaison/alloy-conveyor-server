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
* Class that enqueues the workticket from service broker to dropbox.
*
* @author OFS
*/
public class ServiceBrokerToDropboxWorkTicketQueue extends HornetQJMSRoundRobinReceiveClient {

	 public static final String QUEUE_NAME = "dropboxQueue";
     private static ServiceBrokerToDropboxWorkTicketQueue ourInstance = new ServiceBrokerToDropboxWorkTicketQueue();

     public static ServiceBrokerToDropboxWorkTicketQueue getInstance() {
         return ourInstance;
     }

     private ServiceBrokerToDropboxWorkTicketQueue() {
        super(QUEUE_NAME);
     }
}
