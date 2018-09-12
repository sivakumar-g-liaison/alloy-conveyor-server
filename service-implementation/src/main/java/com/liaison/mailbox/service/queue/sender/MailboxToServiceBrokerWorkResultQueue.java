/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.queue.sender;


import com.liaison.commons.jaxb.JAXBUtility;
import com.liaison.commons.messagebus.SendClient;
import com.liaison.commons.messagebus.client.exceptions.ClientUnavailableException;
import com.liaison.commons.messagebus.queue.QueueTextSendClient;
import com.liaison.dto.queue.WorkResult;
import com.liaison.mailbox.service.queue.kafka.Producer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.xml.bind.JAXBException;
import java.io.IOException;

import static com.liaison.mailbox.MailBoxConstants.SERVICE_BROKER_APP_ID;
import static com.liaison.mailbox.service.util.MailBoxUtil.QUEUE_SERVICE_ENABLED;

/**
 *
 */
public class MailboxToServiceBrokerWorkResultQueue implements AutoCloseable {

    private static final Logger LOG = LogManager.getLogger(MailboxToServiceBrokerWorkResultQueue.class);

    private static final String QUEUE_NAME = "mailboxWorkResult";
    private static SendClient sendClient = new QueueTextSendClient(QUEUE_NAME);

    private static final String TOPIC_SUFFIX = "workResult";

    private static SendClient getInstance() {
        return sendClient;
    }

    private MailboxToServiceBrokerWorkResultQueue() {
    }

    @Override
    public void close() throws Exception {
        sendClient.close();
    }

    public static void post(WorkResult message) throws JAXBException, IOException, ClientUnavailableException {
        if (QUEUE_SERVICE_ENABLED) {
            LOG.debug("Producing a workResult to QS with topic suffix " + TOPIC_SUFFIX);
            Producer.produceWorkResultToQS(message, SERVICE_BROKER_APP_ID, TOPIC_SUFFIX);
        } else {
            getInstance().sendMessage(JAXBUtility.marshalToJSON(message));
        }
    }
}

