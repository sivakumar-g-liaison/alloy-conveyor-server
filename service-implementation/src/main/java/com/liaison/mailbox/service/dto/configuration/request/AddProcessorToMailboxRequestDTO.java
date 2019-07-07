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
import com.liaison.mailbox.service.dto.configuration.ProcessorLegacyDTO;

/**
 * Data Transfer Object that implements fields required for mailbox
 * configuration request.
 * 
 * @author OFS
 */

@JsonRootName("addProcessorToMailBoxRequest")
public class AddProcessorToMailboxRequestDTO {

	private ProcessorDTO processor;
	private ProcessorLegacyDTO processorLegacy;

	public ProcessorDTO getProcessor() {
		if (null == processor) {
			return processorLegacy;
		}
		return processor;
	}

	public void setProcessor(ProcessorDTO processor) {
		this.processor = processor;
	}

	public ProcessorLegacyDTO getProcessorLegacy() {
		return processorLegacy;
	}

	public void setProcessorLegacy(ProcessorLegacyDTO processorLegacy) {
		this.processorLegacy = processorLegacy;
	}
	
}
