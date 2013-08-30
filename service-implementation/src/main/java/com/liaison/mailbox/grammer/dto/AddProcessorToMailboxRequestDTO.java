/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall 
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.grammer.dto;


/**
 * Data Transfer Object that implements fields required for mailbox
 * configuration request. 
 *
 * @author veerasamyn
 */
public class AddProcessorToMailboxRequestDTO {

	private ProcessorDTO processor;

	public ProcessorDTO getProcessor() {
		return processor;
	}

	public void setProcessor(ProcessorDTO processor) {
		this.processor = processor;
	}
	
	public void copyToEntity(Object entity) {
		this.getProcessor().copyToEntity(entity);

	}
}
