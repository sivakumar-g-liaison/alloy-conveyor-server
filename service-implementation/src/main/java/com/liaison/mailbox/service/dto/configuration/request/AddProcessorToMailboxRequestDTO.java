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

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.annotate.JsonRootName;

import com.liaison.commons.security.pkcs7.SymmetricAlgorithmException;
import com.liaison.mailbox.jpa.model.Processor;
import com.liaison.mailbox.service.dto.configuration.ProcessorDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;

/**
 * Data Transfer Object that implements fields required for mailbox
 * configuration request.
 * 
 * @author sivakumarg
 */

@JsonRootName("addProcessorToMailBoxRequest")
public class AddProcessorToMailboxRequestDTO {

	private ProcessorDTO processor;

	public ProcessorDTO getProcessor() {
		return processor;
	}

	public void setProcessor(ProcessorDTO processor) {
		this.processor = processor;
	}

	public void copyToEntity(Processor entity) throws MailBoxConfigurationServicesException, JsonGenerationException,
			JsonMappingException, JAXBException, IOException, SymmetricAlgorithmException {
		this.getProcessor().copyToEntity(entity, true);

	}
}
