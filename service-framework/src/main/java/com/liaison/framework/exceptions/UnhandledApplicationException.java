/**
 * Copyright 2019 Liaison Technologies, Inc.
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.framework.exceptions;

import com.liaison.commons.audit.AuditStandardsRequirement;
import com.liaison.commons.audit.exception.LiaisonAuditableRuntimeException;

public class UnhandledApplicationException extends LiaisonAuditableRuntimeException {

	private static final long serialVersionUID = 1564159737669670209L;

	public UnhandledApplicationException(AuditStandardsRequirement... requirements) {
		super(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR, requirements);
	}

	public UnhandledApplicationException(String message,
			AuditStandardsRequirement... requirements) {
		super(message, javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR, requirements);
	}

	public UnhandledApplicationException(Throwable cause,
			AuditStandardsRequirement... requirements) {
		super(cause, javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR, requirements);
	}

	public UnhandledApplicationException(String message, Throwable cause,
			AuditStandardsRequirement... requirements) {
		super(message, cause, javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR, requirements);
	}

}
