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

import com.liaison.mailbox.service.dto.ResponseDTO;
import com.liaison.mailbox.service.dto.configuration.MailBoxDTO;
import com.liaison.mailbox.service.dto.configuration.ProcessorDTO;

/**
 * 
 *
 * @author sivakumarg
 */
public class GetProcessorResponseDTO {

	private ResponseDTO response;
	private ProcessorDTO processor;
	
	public ResponseDTO getResponse() {
		return response;
	}
	public void setResponse(ResponseDTO response) {
		this.response = response;
	}
	public ProcessorDTO getProcessor() {
		return processor;
	}
	public void setProcessor(ProcessorDTO processor) {
		this.processor = processor;
	}
}
