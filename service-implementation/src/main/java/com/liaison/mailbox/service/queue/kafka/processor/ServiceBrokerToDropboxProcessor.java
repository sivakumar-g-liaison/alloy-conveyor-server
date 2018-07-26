/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.queue.kafka.processor;

import com.liaison.commons.jaxb.JAXBUtility;
import com.liaison.commons.message.glass.dom.StatusType;
import com.liaison.commons.messagebus.common.KafkaTextMessageProcessor;
import com.liaison.commons.util.StatusLogger;
import com.liaison.commons.util.StatusLoggerFactory;
import com.liaison.dto.queue.WorkTicket;
import com.liaison.mailbox.service.dropbox.DropboxService;
import com.liaison.mailbox.service.thread.pool.AsyncProcessThreadPool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;

/**
 * ServiceBrokerToDropbox processor for Consumer usage
 *
 */
public class ServiceBrokerToDropboxProcessor implements KafkaTextMessageProcessor {

    private static final Logger LOG = LogManager.getLogger(ServiceBrokerToDropboxProcessor.class);

    private StatusLoggerFactory statusLoggerFactory;

    @Inject
    public ServiceBrokerToDropboxProcessor(StatusLoggerFactory statusLoggerFactory) {
        this.statusLoggerFactory = statusLoggerFactory;
    }

    @Override
    public void processMessage(String message, String topic) {
        AsyncProcessThreadPool.getExecutorService().submit(new DropboxService(message));
    }

    @Override
    public void logConsumeEvent(String message, String topic) {
        WorkTicket workTicket = getWorkTicketFromMessageText(message);
        if (workTicket != null) {
            StatusLogger statusLogger = statusLoggerFactory.create(workTicket.getGlobalProcessId(), workTicket.getGlobalProcessId(), workTicket.getPipelineId());
            statusLogger.log(StatusType.QUEUED, String.format("Message consumed from topic %s", topic));
        }
    }

    private WorkTicket getWorkTicketFromMessageText(String messageText) {
        try {
            return JAXBUtility.unmarshalFromJSON(messageText, WorkTicket.class);
        } catch (Exception e) {
            LOG.error("Relay: Error while parsing WorkTicket JSON. Input JSON data: '{}'.", messageText, e);
            return null;
        }
    }
}
