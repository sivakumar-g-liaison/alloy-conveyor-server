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

import com.liaison.mailbox.jpa.model.Processor;
import com.liaison.mailbox.service.dto.configuration.ProcessorDTO;

/**
 * @author karthikeyanm
 * 
 */
public class ReviseProcessorRequestDTO {

	private ProcessorDTO processor;

	/**
	 * @return the processor
	 */
	public ProcessorDTO getProcessor() {
		return processor;
	}

	/**
	 * @param processor
	 *            the processor to set
	 */
	public void setProcessor(ProcessorDTO processor) {
		this.processor = processor;
	}

	public void copyToEntity(Processor entity) {
		this.getProcessor().copyToEntity(entity, false);
	}
}
