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

import com.liaison.commons.message.glass.dom.StatusType;
import com.liaison.commons.messagebus.common.KafkaTextMessageProcessor;
import com.liaison.commons.util.StatusLogger;
import com.liaison.commons.util.StatusLoggerFactory;
import com.liaison.mailbox.service.core.FileStageReplicationService;
import com.liaison.mailbox.service.thread.pool.AsyncProcessThreadPool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.inject.Inject;

import static com.liaison.mailbox.MailBoxConstants.GLOBAL_PROCESS_ID;

/**
 * FileStageReplicationRetry processor for Consumer usage
 *
 */
public class FileStageReplicationRetryProcessor implements KafkaTextMessageProcessor {

    private static final Logger LOG = LogManager.getLogger(ServiceBrokerToMailboxProcessor.class);

    private StatusLoggerFactory statusLoggerFactory;

    @Inject
    public FileStageReplicationRetryProcessor(StatusLoggerFactory statusLoggerFactory) {
        this.statusLoggerFactory = statusLoggerFactory;
    }

    @Override
    public void processMessage(String message, String topic) {
        AsyncProcessThreadPool.getExecutorService().submit(new FileStageReplicationService(message));
    }

    @Override
    public void logConsumeEvent(String message, String topic) {
        LOG.info("logConsumeEvent for message " + message);
        JSONObject requestObj;
        try {
            requestObj = new JSONObject(message);
            String globalProcessId = requestObj.getString(GLOBAL_PROCESS_ID);
            StatusLogger statusLogger = statusLoggerFactory.create(globalProcessId, globalProcessId, "");
            statusLogger.log(StatusType.QUEUED, String.format("Message consumed from topic %s", topic));
        } catch (JSONException e) {
            LOG.error("Relay: Error while parsing request object JSON. Input JSON data: '{}'.", message, e);
        }
    }
}
