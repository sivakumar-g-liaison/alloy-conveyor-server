/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall 
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.heatlh;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.karyon.spi.HealthCheckHandler;

public class HealthCheck implements HealthCheckHandler {

    private static final Logger logger = LoggerFactory.getLogger(HealthCheck.class);

    @PostConstruct
    public void init() {
        logger.info("Health check initialized.");
    }

    @Override
    public int getStatus() {
        // TODO: Health check logic.
        logger.info("Health check invoked.");
        return 200;
    }
}
