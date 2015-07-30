/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.queue.consumer;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONException;

import com.liaison.mailbox.service.dropbox.DropboxService;
import com.liaison.mailbox.service.util.MailBoxUtil;

/**
*
* @author OFS
*
*/
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
		} catch (JAXBException | IOException |JSONException e) {
			logger.error(MailBoxUtil.constructMessage(null, null, "Stage file failed"), e);
		}


		logger.info("ServiceBrokerToDropboxWorkTicket with id:"+request+" is completed by thread name:"+Thread.currentThread().getName()+" id:"+Thread.currentThread().getId());

		  try {
			   ServiceBrokerToDropboxWorkTicketQueueConsumer.getDropboxQueueConsumerInstance().printExecutorDiagonostics();
		  } catch(Exception e) {
				logger.error("ServiceBroker to Dropbox queue consumer thread count error", e);
		  }

	}
}
