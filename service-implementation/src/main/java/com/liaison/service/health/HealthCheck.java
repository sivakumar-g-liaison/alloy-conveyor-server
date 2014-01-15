package com.liaison.service.health;

import com.netflix.karyon.spi.HealthCheckHandler;


import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HealthCheck implements HealthCheckHandler {

	private static final Logger logger = LogManager.getLogger(HealthCheck.class);

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
