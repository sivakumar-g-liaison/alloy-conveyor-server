/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall 
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.health;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.netflix.karyon.spi.HealthCheckHandler;

/**
 * 
 * @author OFS
 *
 */
public class HealthCheck implements HealthCheckHandler {

    private static final Logger logger = LogManager.getLogger(HealthCheck.class);

    @PostConstruct
    public void init() {
        logger.debug("Health check initialized.");
    }

    @Override
    public int getStatus() {
        logger.debug("Health check invoked.");
        return 200;
    }
}
