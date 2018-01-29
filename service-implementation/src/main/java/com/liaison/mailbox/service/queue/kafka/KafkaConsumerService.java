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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class KafkaConsumerService implements Runnable {

    private static final Logger LOG = LogManager.getLogger(KafkaConsumerService.class);

    @Override
    public void run() {
        LOG.info("Starting Kafka consumer");
        new Consumer().consume();
    }

}