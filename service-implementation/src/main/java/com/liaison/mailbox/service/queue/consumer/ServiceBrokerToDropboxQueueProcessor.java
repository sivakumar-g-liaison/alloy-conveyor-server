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

import com.liaison.commons.messagebus.queue.QueueTextMessageProcessor;
import com.liaison.mailbox.service.dropbox.DropboxService;
import com.liaison.mailbox.service.thread.pool.AsyncProcessThreadPool;

public class ServiceBrokerToDropboxQueueProcessor implements QueueTextMessageProcessor {

	@Override
	public void processMessage(String message) {
		AsyncProcessThreadPool.getExecutorService().submit(new DropboxService(message));
	}

}
