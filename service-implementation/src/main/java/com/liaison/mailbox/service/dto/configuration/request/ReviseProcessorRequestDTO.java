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

import com.liaison.mailbox.service.dto.configuration.ProcessorDTO;

/**
 * Data Transfer Object that contains the fields required for processor revision.
 * 
 * @author OFS
 */

@JsonRootName("reviseProcessorRequest")
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
    
}
