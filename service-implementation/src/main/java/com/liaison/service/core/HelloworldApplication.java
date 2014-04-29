/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall 
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.service.core;

import javax.annotation.PostConstruct;

import com.netflix.karyon.spi.Application;

/**
 * 
 * @author OFS
 *
 */
@Application
public class HelloworldApplication {

    @PostConstruct
    public void initialize() {
        //TODO: Initialization if any.
    }
}
