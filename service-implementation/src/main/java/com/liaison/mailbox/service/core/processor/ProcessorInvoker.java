package com.liaison.mailbox.service.core.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ProcessorInvoker implements Runnable {
	private String processorId = null;

	private static final Logger logger = LoggerFactory.getLogger(ProcessorInvoker.class);
	
	public ProcessorInvoker(String processorId){
		this.processorId=processorId;
	}

	@Override
	public void run() {

		{

			logger.info("processor with id:"+processorId+"handed over to thread name:"+Thread.currentThread().getName()+" id:"+Thread.currentThread().getId());
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			logger.info("processor with id:"+processorId+" is completed by thread name:"+Thread.currentThread().getName()+" id:"+Thread.currentThread().getId());
			MailboxProcessorQueueConsumer.getMailboxProcessorQueueConsumerInstance().printExecutorDiagonostics();
		}
	}
}
