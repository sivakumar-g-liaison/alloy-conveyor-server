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
public class UnexpectedNameException extends LiaisonAuditableRuntimeException {

	private static final long serialVersionUID = 1564159737669670209L;

	public UnexpectedNameException(javax.ws.rs.core.Response.Status responseStatus,
			AuditStandardsRequirement... requirements) {
		super(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR, requirements);
		// TODO Auto-generated constructor stub
	}

	public UnexpectedNameException(String message,
			javax.ws.rs.core.Response.Status responseStatus,
			AuditStandardsRequirement... requirements) {
		super(message, responseStatus, requirements);
		// TODO Auto-generated constructor stub
	}

	public UnexpectedNameException(Throwable cause,
			javax.ws.rs.core.Response.Status responseStatus,
			AuditStandardsRequirement... requirements) {
		super(cause, responseStatus, requirements);
		// TODO Auto-generated constructor stub
	}

	public UnexpectedNameException(String message, Throwable cause,
			javax.ws.rs.core.Response.Status responseStatus,
			AuditStandardsRequirement... requirements) {
		super(message, cause, responseStatus, requirements);
		// TODO Auto-generated constructor stub
	}

}
