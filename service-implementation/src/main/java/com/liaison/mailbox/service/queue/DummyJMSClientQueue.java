package com.liaison.mailbox.service.queue;

import com.liaison.commons.messagebus.hornetq.HornetQRecoveringCoreSendClient;

public class DummyJMSClientQueue extends HornetQRecoveringCoreSendClient {
	
	 public static final String QUEUE_NAME = "processedPayload";
     private static DummyJMSClientQueue ourInstance = new DummyJMSClientQueue();

     public static DummyJMSClientQueue getInstance() {
         return ourInstance;
     }

     private DummyJMSClientQueue() {
        super(QUEUE_NAME);
     }

}
