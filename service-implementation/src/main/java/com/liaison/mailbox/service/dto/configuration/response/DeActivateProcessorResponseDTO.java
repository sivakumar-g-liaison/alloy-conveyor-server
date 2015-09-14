/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.dto.configuration.response;

import org.codehaus.jackson.map.annotate.JsonRootName;

import com.liaison.mailbox.service.dto.CommonResponseDTO;

/**
 * Data Transfer Object used for sending processor deactivation Responses.
 *
 * @author OFS
 */
@JsonRootName("deActivateProcessorResponse")
public class DeActivateProcessorResponseDTO extends CommonResponseDTO {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private ProcessorResponseDTO processor;

	public ProcessorResponseDTO getProcessor() {
		return processor;
	}
	public void setProcessor(ProcessorResponseDTO processor) {
		this.processor = processor;
	}
}
