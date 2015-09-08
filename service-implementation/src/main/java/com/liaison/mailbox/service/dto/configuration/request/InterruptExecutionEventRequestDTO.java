/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall 
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.dto.configuration.request;

import org.codehaus.jackson.map.annotate.JsonRootName;

import com.liaison.mailbox.rtdm.model.FSMEvent;
import com.liaison.mailbox.service.dto.configuration.FSMEventDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;

/**
 * Data Transfer Object that contains the FSMevent details which can be used to interrupt the running processors.
 * 
 * @author OFS
 */
@JsonRootName("interruptExecutionRequest")
public class InterruptExecutionEventRequestDTO {
	
	private FSMEventDTO fsmEvent;
	
	public FSMEventDTO getFsmEvent() {
		return fsmEvent;
	}

	public void setFsmEvent(FSMEventDTO fsmEvent) {
		this.fsmEvent = fsmEvent;
	}

	public void copyToEntity(FSMEvent entity) throws MailBoxConfigurationServicesException {
		
	}

}
