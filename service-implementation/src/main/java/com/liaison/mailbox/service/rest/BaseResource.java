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
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.service.dto.ResponseDTO;

/**
 * 
 * @author OFS
 *
 */
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
	
	/**
	 * Audits it based on the response status
	 *
	 * @param response
	 * @param operationName
	 */
	public void doAudit(ResponseDTO response, String operationName) {

		if (isSuccess(response)) {
			auditSuccess(operationName);
		} else {
			auditFailure(operationName);
		}
	}

	/**
	 * Checks the response status
	 *
	 * @param serviceResponse
	 * @return true if it is success, false otherwise
	 */
	public boolean isSuccess(ResponseDTO serviceResponse) {

		if (Messages.SUCCESS.value().equals(serviceResponse.getStatus())) {
			return true;
		}

		return false;
	}
	
}


