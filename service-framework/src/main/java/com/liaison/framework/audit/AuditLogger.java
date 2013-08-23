/*
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall 
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.framework.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

/**
 * Audit Logger
 * <p/>
 * <P>Log Audit statements here.
 *
 * @author Robert.Christian
 * @version 1.0
 */
public class AuditLogger {

    // leverage log4j here so that we have control over specific audit targets
    private static final Logger logger = LoggerFactory.getLogger(AuditLogger.class);

    public static void log(AuditStandardsRequirement requirement, AuditStatement.Status status, String message) {
        logger.error (Marker.ANY_MARKER, new DefaultAuditStatement(requirement, status, message));
        Thread.currentThread().getStackTrace().toString();
    }

}
