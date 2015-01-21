package com.liaison.mailbox.service.queue.consumer;

import com.liaison.commons.messagebus.hornetq.HornetQRecoveringCoreReceiveClient;

public class ServiceBrokerToDropboxWorkTicketQueue extends HornetQRecoveringCoreReceiveClient {

	 public static final String QUEUE_NAME = "dropboxQueue";
     private static ServiceBrokerToDropboxWorkTicketQueue ourInstance = new ServiceBrokerToDropboxWorkTicketQueue();

     public static ServiceBrokerToDropboxWorkTicketQueue getInstance() {
         return ourInstance;
     }

     private ServiceBrokerToDropboxWorkTicketQueue() {
        super(QUEUE_NAME);
     }
}
