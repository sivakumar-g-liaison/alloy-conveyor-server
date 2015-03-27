package com.liaison.mailbox.service.core.processor;

import java.io.IOException;

import javax.ws.rs.core.Response;

import com.liaison.mailbox.dtdm.model.Processor;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.service.core.fsm.MailboxFSM;
import com.liaison.mailbox.service.dto.configuration.ProcessorDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;

public class FileWriter extends AbstractProcessor implements MailBoxProcessorI {
	
	@SuppressWarnings("unused")
	private FileWriter() {
		// to force creation of instance only by passing the processor entity
	}

	public FileWriter(Processor configurationInstance) {
		super(configurationInstance);
	}

	@Override
	public Object getClient() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void downloadDirectory(Object client, String remotePayloadLocation,
			String localTargetLocation) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void uploadDirectory(Object client, String localPayloadLocation,
			String remoteTargetLocation) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void cleanup() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void invoke(String executionId, MailboxFSM fsm) {
		// TODO Auto-generated method stub
		
	}
    
	/**
	 * This Method create local folders if not available.
	 * 
	 * * @param processorDTO it have details of processor
	 * 
	 */
	@Override
	public void createLocalFolders(ProcessorDTO processorDTO) {
		String configuredPath = null;
		try {
			configuredPath = getFileWriteLocation();
			createPathIfNotAvailable(processorDTO, configuredPath);

		} catch (IOException e) {
			throw new MailBoxConfigurationServicesException(Messages.LOCAL_FOLDERS_CREATION_FAILED,
					configuredPath, Response.Status.BAD_REQUEST);
		}

	}

}
