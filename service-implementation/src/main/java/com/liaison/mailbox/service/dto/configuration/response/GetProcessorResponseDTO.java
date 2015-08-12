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

import java.util.List;

import org.codehaus.jackson.map.annotate.JsonRootName;

import com.liaison.mailbox.service.dto.CommonResponseDTO;
import com.liaison.mailbox.service.dto.configuration.ProcessorDTO;
import com.liaison.mailbox.service.dto.configuration.ProfileDTO;

/**
 *
 *
 * @author OFS
 */
@JsonRootName("getProcessorResponse")
public class GetProcessorResponseDTO extends CommonResponseDTO {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private ProcessorDTO processor;
	private List<ProcessorDTO> processors;
	private int totalItems;

	public ProcessorDTO getProcessor() {
		return processor;
	}
	public void setProcessor(ProcessorDTO processor) {
		this.processor = processor;
	}
	public List<ProcessorDTO> getProcessors() {
		return processors;
	}
	public void setProcessors(List<ProcessorDTO> processors) {
		this.processors = processors;
	}
	public int getTotalItems() {
		return totalItems;
	}
	public void setTotalItems(int totalItems) {
		this.totalItems = totalItems;
	}
}
