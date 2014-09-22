package com.liaison.mailbox.com.liaison.queue;

import com.liaison.commons.messagebus.jms.HornetQRecoveringCoreClientPool;

public class MailboxToServiceBrokerWorkTicket extends HornetQRecoveringCoreClientPool {

	 public static final String QUEUE_NAME = "mailboxToServiceBrokerWorkTicket";
	    private static MailboxToServiceBrokerWorkTicket ourInstance = new MailboxToServiceBrokerWorkTicket();

	    public static MailboxToServiceBrokerWorkTicket getInstance() {
	        return ourInstance;
	    }

	    private MailboxToServiceBrokerWorkTicket() {
	         super(QUEUE_NAME);
	    }
}
