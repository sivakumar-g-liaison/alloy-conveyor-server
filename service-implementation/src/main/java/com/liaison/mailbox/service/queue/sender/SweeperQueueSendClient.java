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


import com.liaison.commons.jaxb.JAXBUtility;
import com.liaison.commons.messagebus.SendClient;
import com.liaison.commons.messagebus.client.exceptions.ClientUnavailableException;
import com.liaison.commons.messagebus.queue.QueueTextSendClient;
import com.liaison.dto.queue.WorkTicket;
import com.liaison.dto.queue.WorkTicketGroup;
import com.liaison.mailbox.service.util.MailBoxUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.xml.bind.JAXBException;
import java.io.IOException;

import static com.liaison.mailbox.MailBoxConstants.SKIP_QUEUE_INITIALIZER;
import static com.liaison.mailbox.MailBoxConstants.TOPIC_SWEEPER_DEFAULT_TOPIC_SUFFIX;
import static com.liaison.mailbox.service.util.MailBoxUtil.CONFIGURATION;

/**
 * Owner of singleton SendClient
 * 
 * Created by jeremyfranklin-ross on 7/17/14.
 */
public class SweeperQueueSendClient implements AutoCloseable {

    private static final String QUEUE_NAME = "sweeper";
    private static final Logger LOGGER = LogManager.getLogger(SweeperQueueSendClient.class);

    private static final String TOPIC_SUFFIX = CONFIGURATION.getString(TOPIC_SWEEPER_DEFAULT_TOPIC_SUFFIX);

    private static SendClient SEND_CLIENT = null;

    static {

        if (MailBoxUtil.getEnvironmentProperties().getBoolean(SKIP_QUEUE_INITIALIZER, false)) {
            SEND_CLIENT = new SweeperQueueSendClient.NullSendClient();
        } else {
            SEND_CLIENT = new QueueTextSendClient(QUEUE_NAME);
        }
    }

    private static SendClient getInstance() {
        return SEND_CLIENT;
    }

    @Override
    public void close() throws Exception {
        SEND_CLIENT.close();
    }

    private static class NullSendClient implements SendClient {

        @Override
        public void sendMessage(String message) throws ClientUnavailableException {
            LOGGER.warn("Queue disabled. Unable to push message {}", message);
        }

        @Override
        public void sendMessage(String message, long delay) throws ClientUnavailableException {
            LOGGER.warn("Queue disabled. Unable to push message {}", message);
        }

        @Override
        public void close() throws Exception {

        }
    }

    public static void post(WorkTicket workticket) {

        try {
            getInstance().sendMessage(JAXBUtility.marshalToJSON(workticket));
        } catch(ClientUnavailableException | JAXBException | IOException e) {
            throw new RuntimeException(e);
        }

    }

    private static WorkTicket getWorkTicketFromMessageText(String messageText) {
        try {
            return JAXBUtility.unmarshalFromJSON(messageText, WorkTicket.class);
        } catch (Exception e) {
            String errorMessage = "Relay: Error while parsing WorkTicket JSON. Input JSON data: " + messageText;
            LOGGER.error(errorMessage, e);
            throw new RuntimeException(errorMessage, e);
        }
    }

    private static WorkTicketGroup getWorkTicketGroupFromMessageText(String messageText) {
        try {
            return JAXBUtility.unmarshalFromJSON(messageText, WorkTicketGroup.class);
        } catch (Exception e) {
            String errorMessage = "Relay: Error while parsing WorkTicketGroup JSON. Input JSON data: " + messageText;
            LOGGER.error(errorMessage, e);
            throw new RuntimeException(errorMessage, e);
        }
    }
}
