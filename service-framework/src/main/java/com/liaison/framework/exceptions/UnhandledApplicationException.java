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
