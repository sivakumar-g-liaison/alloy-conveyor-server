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

/**
 * Objects, including throwable implementing AuditStatement will be caught via the AuditSyslogAppender
 * 
 * @author jeremyfranklin-ross
 */
public interface AuditStatement {

    public enum Status {
        FAILED,
        SUCCEED,
        ATTEMPT,
        POTENTIAL
    }

    public String getMessage();

    public Status getStatus();

    public AuditStandardsRequirement getAuditStandardsRequirement();

}