package com.liaison.mailbox.service.queue.consumer;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.mailbox.service.dropbox.DropboxService;

public class DropboxQueueInvoker implements Runnable {

	private String request = null;

	private static final Logger logger = LogManager.getLogger(ProcessorInvoker.class);

	public DropboxQueueInvoker(String request){
		this.request=request;
	}
	
	public DropboxService getService(){
	    return new DropboxService();
	}

	@Override
	public void run() {

		logger.info("ServiceBrokerToDropboxWorkTicket Request with id:"+request+"handed over to thread name:"+Thread.currentThread().getName()+" id:"+Thread.currentThread().getId());
		
		try {
			getService().invokeDropboxQueue(request);
		} catch (JAXBException | IOException e) {
			logger.error("Stage file failed", e);
		}
		
		
		logger.info("ServiceBrokerToDropboxWorkTicket with id:"+request+" is completed by thread name:"+Thread.currentThread().getName()+" id:"+Thread.currentThread().getId());

		  try {
			   ServiceBrokerToDropboxWorkTicketQueueConsumer.getDropboxQueueConsumerInstance().printExecutorDiagonostics();
		  } catch(Exception e) {
				logger.error("ServiceBroker to Dropbox queue consumer thread count error", e);
		  }

	}
}
