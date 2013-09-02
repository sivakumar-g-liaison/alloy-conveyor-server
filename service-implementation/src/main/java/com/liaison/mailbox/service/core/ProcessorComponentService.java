package com.liaison.mailbox.service.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liaison.mailbox.enums.ProcessorComponentType;
import com.liaison.mailbox.grammer.ResponseDTO;
import com.liaison.mailbox.grammer.dto.AddProcessorToMailboxRequestDTO;
import com.liaison.mailbox.grammer.dto.AddProcessorToMailboxResponseDTO;
import com.liaison.mailbox.grammer.dto.ProcessorResponseDTO;
import com.liaison.mailbox.grammer.dto.ProfileConfigurationRequest;
import com.liaison.mailbox.grammer.dto.ProfileConfigurationResponse;
import com.liaison.mailbox.jpa.dao.MailBoxComponentDAO;
import com.liaison.mailbox.jpa.dao.MailBoxComponentDAOBase;
import com.liaison.mailbox.jpa.dao.ProcessorComponentDAO;
import com.liaison.mailbox.jpa.dao.ProcessorComponentDAOBase;
import com.liaison.mailbox.jpa.model.MailBoxComponent;
import com.liaison.mailbox.jpa.model.Processor;
import com.liaison.mailbox.jpa.model.RemoteDownloader;
import com.liaison.mailbox.jpa.model.RemoteUploader;

/**
 * @author ganeshramr
 *
 */
public class ProcessorComponentService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ProcessorComponentService.class);

	/**
	 * Method inserts the analytic config details into the mail box processor.
	 * 
	 * @param serviceRequest
	 * @return
	 */
	public AddProcessorToMailboxResponseDTO insertProcessorComponents(AddProcessorToMailboxRequestDTO serviceRequest) {

		LOGGER.info("call receive to insert the profile ::{}", serviceRequest.getProcessor());
		Processor processor = null;
		
		if (ProcessorComponentType.REMOTEDOWNLOADER.toString()
				.equalsIgnoreCase(serviceRequest.getProcessor().getType())) {
			processor = new RemoteDownloader();
		} else if (ProcessorComponentType.REMOTEUPLOADER.toString()
				.equalsIgnoreCase(serviceRequest.getProcessor().getType())) {
			processor = new RemoteUploader();
		} else {
			processor = null;
		}

		if (processor != null) {
			ProcessorComponentDAO componenDao = new ProcessorComponentDAOBase();
			componenDao.persist(processor);
		}
		//Temporarily returns the id alone.
		AddProcessorToMailboxResponseDTO serviceResponse = new AddProcessorToMailboxResponseDTO();
		serviceResponse.setResponse(new ResponseDTO());
		serviceResponse.setProcessor(new ProcessorResponseDTO());
		serviceResponse.getProcessor().setGuId(processor.getPguid());
		
		return serviceResponse;
	}

}
