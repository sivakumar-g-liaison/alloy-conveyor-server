/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.queue.kafka;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.commons.messagebus.SendClient;
import com.liaison.commons.messagebus.client.exceptions.ClientUnavailableException;
import com.liaison.commons.util.settings.LiaisonArchaiusConfiguration;

public class KafkaSendClient implements SendClient {
    
    private static final Logger LOG = LogManager.getLogger(KafkaSendClient.class);
    private static final String PROPERTY_SKIP_KAFKA_QUEUE = "com.liaison.skip.kafka.queue";

    @Override
    public void close() throws Exception {
        
    }

    @Override
    public void sendMessage(String message) throws ClientUnavailableException {

        if (!LiaisonArchaiusConfiguration.getInstance().getBoolean(PROPERTY_SKIP_KAFKA_QUEUE, false)) {
            
            try {
                Producer producer = new Producer();
                producer.produce(message);
            } catch (IOException e) {
                LOG.error("An error occurred in kafka producer. " + e.getMessage());
            }
        } else {
            LOG.info("Kafka queue service in not enabled");
        }
    }

    @Override
    public void sendMessage(String message, long delay) throws ClientUnavailableException {
        
    }

}
