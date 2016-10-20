/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.topic.sender;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.inject.Inject;
import com.liaison.commons.messagebus.client.exceptions.ClientUnavailableException;
import com.liaison.commons.messagebus.topic.TopicTextSendClient;


public class MailBoxTopicPoolSender {
    
    private static final Logger logger = LogManager.getLogger(MailBoxTopicPoolSender.class);

    @Inject
    private TopicTextSendClient pool;

    /**
     * Sends the message to sender topic pool
     * 
     * @param message
     */
    public void sendMessage(String message) {

        try {

            logger.debug("Posting message to topic [" + message + "]");
            pool.sendMessage(message);
        } catch (ClientUnavailableException e) {
            e.printStackTrace();
        }
    }

}
