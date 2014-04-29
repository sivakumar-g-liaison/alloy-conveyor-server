/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall 
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.service.exceptions.examples;

import com.liaison.commons.audit.AuditStandardsRequirement;
import com.liaison.commons.audit.exception.LiaisonAuditableRuntimeException;

/**
 * 
 * @author OFS
 *
 */
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
