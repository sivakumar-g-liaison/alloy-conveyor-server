package com.liaison.mailbox.service.core.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liaison.mailbox.service.core.MailBoxService;
import com.liaison.mailbox.service.dto.configuration.response.TriggerProfileResponseDTO;

class ProcessorInvoker implements Runnable {
	
	private String processorId = null;

	private static final Logger logger = LoggerFactory.getLogger(ProcessorInvoker.class);

	public ProcessorInvoker(String processorId){
		this.processorId=processorId;
	}
	
	public MailBoxService getService(){
	    return new MailBoxService();
	}

	@Override
	public void run() {

		logger.info("processor with id:"+processorId+"handed over to thread name:"+Thread.currentThread().getName()+" id:"+Thread.currentThread().getId());
		
		TriggerProfileResponseDTO serviceResponse = getService().executeProcessor(processorId);
		
		logger.info("processor with id:"+processorId+" is completed by thread name:"+Thread.currentThread().getName()+" id:"+Thread.currentThread().getId());
		MailboxProcessorQueueConsumer.getMailboxProcessorQueueConsumerInstance().printExecutorDiagonostics();
	}
}