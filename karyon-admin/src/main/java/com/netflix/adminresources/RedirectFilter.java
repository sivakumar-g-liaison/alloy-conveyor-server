/*
 * Copyright 2013 Netflix, Inc.
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 */

package com.netflix.adminresources;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import com.google.inject.Singleton;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;
import com.netflix.karyon.spi.PropertyNames;

/**
 * A {@link javax.servlet.Filter} implementation to redirect root requests to a default location as specified by
 * the dynamic property {@link AdminResourcesContainer#DEFAULT_PAGE}
 *
 * @author pkamath
 * @author Nitesh Kant
 */

@Singleton
public class RedirectFilter implements Filter {
	public static final String DEFAULT_PAGE_PROP_NAME = PropertyNames.KARYON_PROPERTIES_PREFIX + "admin.default.page";
	private static final Logger logger = LoggerFactory.getLogger(RedirectFilter.class);
	
    public static final DynamicStringProperty DEFAULT_PAGE = DynamicPropertyFactory.getInstance().getStringProperty(DEFAULT_PAGE_PROP_NAME, "/healthcheck");
    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        if (httpRequest.getRequestURI().equals("/")) {
            String defaultLocation = DEFAULT_PAGE.get();
            ((HttpServletResponse) response).sendRedirect(defaultLocation);
            return;
        }
        chain.doFilter(httpRequest, response);
    }

    @Override
    public void init(FilterConfig config) throws ServletException {
    	logger.info("inside init() of RedirectFilter()");
    }
}
