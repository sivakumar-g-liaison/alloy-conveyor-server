/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.framework;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * This annotation should be used to mark all application "configuration" REST
 * end points. Resources marked with this annotation must have the jax-rs
 * 
 * @Path("config/**")
 * 
 * @author OFS
 * 
 */
@Target(ElementType.TYPE)
public @interface AppConfigurationResource {

}
