/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall 
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.rest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.commons.audit.AuditStatement;
import com.liaison.commons.audit.AuditStatement.Status;
import com.liaison.commons.audit.DefaultAuditStatement;
import com.liaison.commons.audit.pci.PCIV20Requirement;

public class BaseResource {
	private static final Logger logger = LogManager.getLogger(BaseResource.class);
	protected void auditAttempt(String message){
		
		 AuditStatement auditStatement = new DefaultAuditStatement(Status.ATTEMPT,
				 													  message,
														              PCIV20Requirement.PCI10_2_5,
														              PCIV20Requirement.PCI10_2_2);
		 logger.info(auditStatement); 
		
	}
	
	protected void auditSuccess(String message){
		
		 AuditStatement auditStatement = new DefaultAuditStatement(Status.SUCCEED, message + "- operation is success");
		 logger.info(auditStatement); 
		
	}
	
	protected void auditFailure(String message){
		
		 AuditStatement auditStatement = new DefaultAuditStatement(Status.FAILED, message + "- operation failed");
		 logger.info(auditStatement); 
		
	}
	
	
}


