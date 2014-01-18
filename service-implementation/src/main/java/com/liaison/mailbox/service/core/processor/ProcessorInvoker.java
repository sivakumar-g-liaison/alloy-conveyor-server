/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall 
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.mailbox.service.core.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liaison.mailbox.service.core.MailBoxService;
import com.liaison.mailbox.service.dto.configuration.response.TriggerProfileResponseDTO;

class ProcessorInvoker implements Runnable {
	
	private String request = null;

	private static final Logger logger = LoggerFactory.getLogger(ProcessorInvoker.class);

	public ProcessorInvoker(String request){
		this.request=request;
	}
	
	public MailBoxService getService(){
	    return new MailBoxService();
	}

	@Override
	public void run() {

		logger.info("processor with id:"+request+"handed over to thread name:"+Thread.currentThread().getName()+" id:"+Thread.currentThread().getId());
		
		TriggerProfileResponseDTO serviceResponse = getService().executeProcessor(request);
		
		logger.info("processor with id:"+request+" is completed by thread name:"+Thread.currentThread().getName()+" id:"+Thread.currentThread().getId());
		MailboxProcessorQueueConsumer.getMailboxProcessorQueueConsumerInstance().printExecutorDiagonostics();
	}
}