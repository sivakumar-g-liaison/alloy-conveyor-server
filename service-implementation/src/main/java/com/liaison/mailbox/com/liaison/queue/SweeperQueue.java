package com.liaison.mailbox.com.liaison.queue;

import com.liaison.commons.com.liaison.commons.messagbus.jms.HornetQRecoveringClientPool;

/**
 * Created by jeremyfranklin-ross on 7/17/14.
 */
public class SweeperQueue extends HornetQRecoveringClientPool {

    public static final String QUEUE_NAME = "sweeper";
    private static SweeperQueue ourInstance = new SweeperQueue();

    public static SweeperQueue getInstance() {
        return ourInstance;
    }

    private SweeperQueue() {
         super(QUEUE_NAME);
    }
}
