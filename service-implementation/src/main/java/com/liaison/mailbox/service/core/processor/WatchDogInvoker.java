package com.liaison.mailbox.service.core.processor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.mailbox.service.core.MailboxSLAWatchDogService;

public class WatchDogInvoker implements Runnable {

	private String request = null;

	private static final Logger logger = LogManager.getLogger(WatchDogInvoker.class);

	public WatchDogInvoker(String request){
		this.request=request;
	}
	
	public MailboxSLAWatchDogService getService(){
	    return new MailboxSLAWatchDogService();
	}

	
	@Override
	public void run() {
		
		logger.info("watchdog request:"+request+"handed over to thread name:"+Thread.currentThread().getName()+" id:"+Thread.currentThread().getId());
		
		getService().invokeWatchDog(request);
		
		logger.info("watchdog request:"+request+" is completed by thread name:"+Thread.currentThread().getName()+" id:"+Thread.currentThread().getId());
		  try {
			   MailboxToServiceBrokerWorkTicketConsumer.getMailboxWatchDogQueueConsumerInstance().printExecutorDiagonostics();
		  } catch(Exception e) {
				logger.error("Mailbox watchdog queue consumer thread count error", e);
		  }
	}

}
