package com.liaison.mailbox.com.liaison.queue;

import com.liaison.commons.messagebus.jms.HornetQRecoveringCoreClientPool;

public class WatchDogQueue extends HornetQRecoveringCoreClientPool {

	 public static final String QUEUE_NAME = "watchdog";
	    private static WatchDogQueue ourInstance = new WatchDogQueue();

	    public static WatchDogQueue getInstance() {
	        return ourInstance;
	    }

	    private WatchDogQueue() {
	         super(QUEUE_NAME);
	    }
}
