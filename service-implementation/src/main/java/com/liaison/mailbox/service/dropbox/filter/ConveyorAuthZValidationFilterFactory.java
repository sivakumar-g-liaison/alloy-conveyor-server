/**
 * Copyright 2017 Liaison Technologies, Inc.
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.mailbox.service.dropbox.filter;


import com.netflix.config.ConfigurationManager;
import org.apache.commons.configuration.AbstractConfiguration;

import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;


public final class ConveyorAuthZValidationFilterFactory implements DynamicFeature {

    private static final String PROPERTY_AUTH_SKIP = "com.liaison.skip.authentication";
    private static AbstractConfiguration configuration = ConfigurationManager.getConfigInstance();

    @Override
    public void configure(ResourceInfo resourceInfo, FeatureContext context) {

        Boolean skipAuth = true;
        //verifies Auth annotation exists on the method
        if (null != resourceInfo.getResourceMethod().getAnnotation(ConveyorAuthZ.class)) {
            skipAuth = false;
        }

        //skip auth based on application level property
        if (configuration.getBoolean(PROPERTY_AUTH_SKIP, false)) {
            skipAuth = true;
        }

        if (!skipAuth) {
            context.register(new ConveyorAuthZFilter());
        }

    }
}