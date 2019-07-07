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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.annotate.JsonRootName;

import com.liaison.commons.security.pkcs7.SymmetricAlgorithmException;
import com.liaison.mailbox.dtdm.model.Processor;
import com.liaison.mailbox.dtdm.model.ScheduleProfileProcessor;
import com.liaison.mailbox.service.dto.CommonResponseDTO;
import com.liaison.mailbox.service.dto.configuration.MailBoxDTO;
import com.liaison.mailbox.service.dto.configuration.ProcessorDTO;
import com.liaison.mailbox.service.dto.configuration.ProfileDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;

/**
 * Customized DTO for responses.
 * 
 * @author OFS
 */
@JsonRootName("searchProcessorResponse")
public class SearchProcessorResponseDTO extends CommonResponseDTO {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String mailBoxName;
	private String processorName;
	private String pipeLineId;
	private List<ProfileDTO> profiles;
	private List<MailBoxDTO> mailbox;
	private List<ProcessorDTO> processor;
	
	public SearchProcessorResponseDTO() {
		super();
	}
	
	public String getMailBoxName() {
		return mailBoxName;
	}

	public void setMailBoxName(String mailBoxName) {
		this.mailBoxName = mailBoxName;
	}	

	public String getProcessorName() {
		return processorName;
	}

	public void setProcessorName(String processorName) {
		this.processorName = processorName;
	}

	public String getPipeLineId() {
		return pipeLineId;
	}

	public void setPipeLineId(String pipeLineId) {
		this.pipeLineId = pipeLineId;
	}
	
	public List<ProfileDTO> getProfiles() {

		if (null == profiles) {
			profiles = new ArrayList<>();
		}
		return profiles;
	}

	public void setProfiles(List<ProfileDTO> profiles) {
		this.profiles = profiles;
	}
	
	public List<MailBoxDTO> getMailbox() {
		return mailbox;
	}

	public void setMailbox(List<MailBoxDTO> mailbox) {
		this.mailbox = mailbox;
	}
	
	public List<ProcessorDTO> getProcessor() {
		return processor;
	}

	public void setProcessor(List<ProcessorDTO> processor) {
		this.processor = processor;
	}

	/**
	 * Copies the values from Entity to DTO.
	 *
	 * @param processor
	 * @throws IOException
	 * @throws JAXBException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 * @throws MailBoxConfigurationServicesException
	 * @throws SymmetricAlgorithmException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 */
	public void copyFromEntity(Processor processor) throws JsonParseException, JsonMappingException, JAXBException, IOException,
			MailBoxConfigurationServicesException, SymmetricAlgorithmException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {

		this.setMailBoxName(processor.getMailbox().getMbxName());
		this.setPipeLineId(processor.getProcsrProperties());
		
		if (null != processor.getScheduleProfileProcessors()) {

			ProfileDTO profile = null;
			for (ScheduleProfileProcessor scheduleProfileProcessor : processor.getScheduleProfileProcessors()) {

				profile = new ProfileDTO();
				profile.copyFromEntity(scheduleProfileProcessor.getScheduleProfilesRef());
				this.getProfiles().add(profile);
			}
		}
	}
}
