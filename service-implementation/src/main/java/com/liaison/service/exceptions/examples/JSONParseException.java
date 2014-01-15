package com.liaison.service.exceptions.examples;

import com.liaison.commons.audit.AuditStandardsRequirement;
import com.liaison.commons.audit.exception.LiaisonAuditableRuntimeException;

public class JSONParseException extends LiaisonAuditableRuntimeException {

	private static final long serialVersionUID = 1564159737669670209L;

	public JSONParseException(AuditStandardsRequirement... requirements) {
		super(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR, requirements);
	}

	public JSONParseException(String message,
			AuditStandardsRequirement... requirements) {
		super(message, javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR, requirements);
	
	}

	public JSONParseException(Throwable cause,
			AuditStandardsRequirement... requirements) {
		super(cause, javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR, requirements);
	}

	public JSONParseException(String message, Throwable cause,
			AuditStandardsRequirement... requirements) {
		super(message, cause, javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR, requirements);
	}

}
