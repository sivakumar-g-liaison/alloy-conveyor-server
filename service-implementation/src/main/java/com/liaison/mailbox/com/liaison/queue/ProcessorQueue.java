package com.liaison.mailbox.com.liaison.queue;

import com.liaison.commons.com.liaison.commons.messagbus.jms.HornetQRecoveringClientPool;

/**
 * Created by jeremyfranklin-ross on 7/17/14.
 */
public class ProcessorQueue extends HornetQRecoveringClientPool {

    public static final String QUEUE_NAME = "processor";
    private static ProcessorQueue ourInstance = new ProcessorQueue();

    public static ProcessorQueue getInstance() {
        return ourInstance;
    }

    private ProcessorQueue() {
         super(QUEUE_NAME);
    }
}
