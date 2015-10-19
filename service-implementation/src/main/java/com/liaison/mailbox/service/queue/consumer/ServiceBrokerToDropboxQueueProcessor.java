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

import com.liaison.commons.messagebus.queueprocessor.QueueProcessor;
import com.liaison.mailbox.service.dropbox.DropboxService;
import com.liaison.mailbox.service.util.MailBoxUtil;

public class ServiceBrokerToDropboxQueueProcessor implements QueueProcessor {

	private static final Logger logger = LogManager.getLogger(ServiceBrokerToDropboxQueueProcessor.class);

	@Override
	public void processMessage(String message) {

		logger.info("Consumed WORKTICKET [" + message + "]");

		try {

			new DropboxService().invokeDropboxQueue(message);
			logger.info("Processed WORKTICKET [" + message + "]");

		} catch (JAXBException | IOException |JSONException e) {
			logger.error(MailBoxUtil.constructMessage(null, null, "Stage file failed"), e);
		}
	}
}
