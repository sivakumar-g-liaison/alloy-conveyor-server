package com.liaison.mailbox.service.queue;

import com.liaison.commons.messagebus.hornetq.HornetQRecoveringCoreSendClient;

public class DummyDropboxQueue extends HornetQRecoveringCoreSendClient {
	
	 public static final String QUEUE_NAME = "dropboxQueue";
     private static DummyDropboxQueue ourInstance = new DummyDropboxQueue();

     public static DummyDropboxQueue getInstance() {
         return ourInstance;
     }

     private DummyDropboxQueue() {
        super(QUEUE_NAME);
     }
}
