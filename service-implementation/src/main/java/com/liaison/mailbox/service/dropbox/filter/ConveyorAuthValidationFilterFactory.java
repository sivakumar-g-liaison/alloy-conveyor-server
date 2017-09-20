/**
 * Copyright 2017 Liaison Technologies, Inc.
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.mailbox.service.dropbox.filter;


import com.liaison.usermanagement.service.client.filter.Auth;
import com.liaison.usermanagement.service.client.filter.AuthenticationFilter;
import com.netflix.config.ConfigurationManager;
import com.sun.jersey.api.model.AbstractMethod;
import com.sun.jersey.spi.container.ResourceFilter;
import com.sun.jersey.spi.container.ResourceFilterFactory;
import org.apache.commons.configuration.AbstractConfiguration;

import java.util.ArrayList;
import java.util.List;


public final class ConveyorAuthValidationFilterFactory implements ResourceFilterFactory {

    private static final String PROPERTY_AUTH_SKIP = "com.liaison.skip.authentication";
    private static AbstractConfiguration configuration = ConfigurationManager.getConfigInstance();

    @Override
    public List<ResourceFilter> create(AbstractMethod abstractMethod) {

        Boolean skipAuth = true;
        //verifies Auth annotation exists on the method
        if (null != abstractMethod.getAnnotation(ConveyorAuth.class)) {
            skipAuth = false;
        }

        //skip auth based on application level property
        if (configuration.getBoolean(PROPERTY_AUTH_SKIP, false)) {
            skipAuth = true;
        }

        List<ResourceFilter> filters = new ArrayList<>();
        if (!skipAuth) {
            filters.add(0, new ConveyorAuthFilter());
        }
        return filters;
    }

}