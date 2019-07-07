/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall 
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.queue.sender;


import com.liaison.commons.messagebus.SendClient;
import com.liaison.commons.messagebus.client.exceptions.ClientUnavailableException;
import com.liaison.commons.messagebus.queue.QueueTextSendClient;
import com.liaison.mailbox.service.queue.kafka.Producer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

import static com.liaison.mailbox.MailBoxConstants.DEPLOYMENT_APP_ID;
import static com.liaison.mailbox.MailBoxConstants.TOPIC_MAILBOX_PROCESSOR_DEFAULT_TOPIC_SUFFIX;
import static com.liaison.mailbox.service.util.MailBoxUtil.CONFIGURATION;
import static com.liaison.mailbox.service.util.MailBoxUtil.QUEUE_SERVICE_ENABLED;

/**
 * Owner of singleton SendClient for "processor" queue
 * 
 * Created by jeremyfranklin-ross on 7/17/14.
 */
public class ProcessorSendQueue implements AutoCloseable {

    private static final Logger LOG = LogManager.getLogger(ProcessorSendQueue.class);
    private static final String QUEUE_NAME = "processor";
    private static final String TOPIC_SUFFIX = CONFIGURATION.getString(TOPIC_MAILBOX_PROCESSOR_DEFAULT_TOPIC_SUFFIX);

    private static SendClient sendClient = new QueueTextSendClient(QUEUE_NAME);

    public static SendClient getInstance() {
        return sendClient;
    }

    private ProcessorSendQueue() {
        //NOP
    }

    @Override
    public void close() throws Exception {
        sendClient.close();
    }

    public static void post(List<String> messages) throws ClientUnavailableException {

        if (QUEUE_SERVICE_ENABLED) {
            messages.forEach(message -> {
                LOG.debug("ABOUT TO get Producer produce {}", (Object) messages.toArray(new String[0]));
                Producer.produceMessageToQS(message, DEPLOYMENT_APP_ID, TOPIC_SUFFIX, 0L);
            });
        } else {
            for (String message : messages) {
                LOG.debug("ABOUT TO get ProcessorSendQueue Instance {}", (Object) messages.toArray(new String[0]));
                getInstance().sendMessage(message);
            }
        }

    }
}

