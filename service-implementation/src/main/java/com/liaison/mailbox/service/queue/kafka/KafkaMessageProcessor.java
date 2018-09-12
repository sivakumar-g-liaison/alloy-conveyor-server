/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.queue.kafka;

import com.liaison.commons.exception.LiaisonException;
import com.liaison.commons.message.glass.dom.StatusType;
import com.liaison.commons.messagebus.common.KafkaTextMessageProcessor;
import com.liaison.commons.messagebus.queue.LiaisonProducer;
import com.liaison.commons.util.StatusLogger;
import com.liaison.commons.util.StatusLoggerFactory;
import com.liaison.dto.queue.WorkResult;
import com.liaison.dto.queue.WorkTicket;
import com.liaison.dto.queue.WorkTicketGroup;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;

import static com.liaison.mailbox.MailBoxConstants.DEPLOYMENT_APP_ID;

public class KafkaMessageProcessor implements KafkaTextMessageProcessor {

    private static final Logger LOG = LogManager.getLogger(KafkaMessageProcessor.class);

    private LiaisonProducer producer;
    private StatusLoggerFactory statusLoggerFactory;

    @Override
    public void processMessage(String message, String topic) {
    }

    @Inject
    public KafkaMessageProcessor(LiaisonProducer producer, StatusLoggerFactory statusLoggerFactory) {
        this.producer = producer;
        this.statusLoggerFactory = statusLoggerFactory;
    }

    @Override
    public void logConsumeEvent(String message, String topic) {
    }

    /**
     * Produce message to Queue Service via lib-message-bus
     *  @param message String JSON data
     * @param receiverId String receiver app id
     * @param delay long
     */
    public void postToQueue(String message, String receiverId, String topicSuffix, long delay) {
        try {
            producer.produce(DEPLOYMENT_APP_ID, receiverId, message, topicSuffix, delay);
        } catch (LiaisonException e) {
            LOG.error("Message produce to " + receiverId + " topic failed. Retrying...", e);
            try {
                producer.produce(DEPLOYMENT_APP_ID, receiverId, message, topicSuffix);
            } catch (LiaisonException e1) {
                LOG.error("Produce retry failed. Sending failure event.", e1);
            }
        }
    }

    /**
     * Produce message to Queue Service via lib-message-bus
     *
     * @param workResult WorkResult workResult
     * @param receiverId String receiver app id
     */
    public void postToQueue(WorkResult workResult, String receiverId, String topicSuffix) {

        try {
            producer.produce(DEPLOYMENT_APP_ID, receiverId, workResult, topicSuffix);
        } catch (LiaisonException e) {
            LOG.error("WorkResult produce to " + receiverId + " topic failed. Retrying...", e);
            try {
                producer.produce(DEPLOYMENT_APP_ID, receiverId, workResult, topicSuffix);
            } catch (LiaisonException e1) {
                LOG.error("Produce retry failed. Sending failure event.", e1);
                StatusLogger statusLogger = statusLoggerFactory.create(workResult.getProcessId(), workResult.getProcessId(), workResult.getPipelineId());
                statusLogger.log(StatusType.ERROR, "WorkResult queuing to SB failed.", e1.getMessage());
            }
        }
    }

    /**
     * Produce message to Queue Service via lib-message-bus
     *
     * @param workTicket WorkTicket
     * @param receiverId String receiver app id
     */
    public void postToQueue(WorkTicket workTicket, String receiverId, String topicSuffix) {
        try {
            producer.produce(DEPLOYMENT_APP_ID, receiverId, workTicket, topicSuffix);
        } catch (LiaisonException e) {
            LOG.error("WorkTicket produce to " + receiverId + " topic failed. Retrying...", e);
            try {
                producer.produce(DEPLOYMENT_APP_ID, receiverId, workTicket, topicSuffix);
            } catch (LiaisonException e1) {
                LOG.error("Produce retry failed. Sending failure event.", e1);
                StatusLogger statusLogger = statusLoggerFactory.create(workTicket.getGlobalProcessId(), workTicket.getGlobalProcessId(), workTicket.getPipelineId());
                statusLogger.log(StatusType.ERROR, "WorkTicket queuing to SB failed.", e1.getMessage());
            }
        }
    }

    /**
     * Produce message to Queue Service via lib-message-bus
     *
     * @param workTicketGroup WorkTicketGroup
     * @param receiverId String receiver app id
     */
    public void postToQueue(WorkTicketGroup workTicketGroup, String receiverId, String topicSuffix) {
        try {
            producer.produce(DEPLOYMENT_APP_ID, receiverId, workTicketGroup, topicSuffix);
        } catch (LiaisonException e) {
            LOG.error("WorkTicketGroup produce to " + receiverId + " topic failed. Retrying...", e);
            try {
                producer.produce(DEPLOYMENT_APP_ID, receiverId, workTicketGroup, topicSuffix);
            } catch (LiaisonException e1) {
                LOG.error("Produce retry failed. Sending failure event.", e1);
            }
        }
    }
}
