package com.liaison.mailbox.service.core.processor;

import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.dtdm.model.Processor;
import com.liaison.mailbox.enums.ExecutionEvents;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.service.core.fsm.MailboxFSM;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.executor.javascript.JavaScriptExecutorUtil;
import com.liaison.mailbox.service.storage.util.StorageUtilities;
import com.liaison.mailbox.service.util.GlassMessage;
import com.liaison.mailbox.service.util.MailBoxUtil;
import com.liaison.mailbox.service.util.TransactionVisibilityClient;

public class FileWriterProcessor extends AbstractProcessor implements
		MailBoxProcessorI {
	private static final Logger LOGGER = LogManager.getLogger(FileWriterProcessor.class);
	
	@SuppressWarnings("unused")
	private FileWriterProcessor() {
		// to force creation of instance only by passing the processor entity
	}
	
	public FileWriterProcessor(Processor configurationInstance) {
		super(configurationInstance);
	}
	
	TransactionVisibilityClient glassLogger = null;
	GlassMessage glassMessage = null;

	@Override
	public void invoke(String executionId, MailboxFSM fsm) {
	
		LOGGER.debug("Entering in invoke.");
		try {
 
			if (Boolean.valueOf(getProperties().isHandOverExecutionToJavaScript())) {
				fsm.handleEvent(fsm.createEvent(ExecutionEvents.PROCESSOR_EXECUTION_HANDED_OVER_TO_JS));
				// Use custom G2JavascriptEngine
				JavaScriptExecutorUtil.executeJavaScript(configurationInstance.getJavaScriptUri(), this);
			} else {
				executeRequest();
			}
		} catch(JAXBException |IOException e) {
			throw new RuntimeException(e);
		}

	}
	
	/**
	 * Method to execute FileWriterProcessor and write the payload from spectrum
	 * to the configured file write location of processor
	 * 
	 */
	protected void  executeRequest()  {
				
		try {
			String spectrumURL = null;
			
			// retrieving the spectrum URL
			spectrumURL = retrieveSpectrumURL();
			if (MailBoxUtil.isEmpty(spectrumURL)) {
				LOGGER.error("Spectrum URL is Empty");
			}
			LOGGER.info("The spectrumURL is {}", spectrumURL);
			
			//get payload from spectrum
			InputStream payload = StorageUtilities.retrievePayload(spectrumURL);

			if (null == payload) {
				LOGGER.error("Failed to retrieve payload from spectrum");
				throw new MailBoxServicesException("Failed to retrieve payload from spectrum", Response.Status.BAD_REQUEST);
			}

			//get file write location from processor
			String fileWriteLocation = getFileWriteLocation();

			if (null == fileWriteLocation) {
				LOGGER.error("file write location not configured for processor {}", this.configurationInstance.getProcsrName());
				throw new MailBoxServicesException(Messages.LOCATION_NOT_CONFIGURED, MailBoxConstants.PAYLOAD_LOCATION, Response.Status.CONFLICT);
			}
			LOGGER.debug("The File Write Location is {}", fileWriteLocation);
			
			// Write the payload from spectrum to the file write location configured in processor
			LOGGER.debug("Started writing payload from spectrum to processor file write location");
			String fileName = this.configurationInstance.getProcsrName() + System.nanoTime();
			MailBoxUtil.writeDataToGivenLocation(payload, fileWriteLocation, fileName, true);
			LOGGER.debug("Payload from spectrum is successfully written to given file write location");
			
		} catch (MailBoxServicesException | IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Method to retrieve the spectrum URL
	 * 
	 * @return SpectrumURL as String
	 */
	private String retrieveSpectrumURL() {
	
		//String spectrumURL = "sfs2:/mailboxsweeper/payload/1.0/13BD64360A0A007D0A180BCD85F93951";
		String spectrumURL = "fs2://secure@dev-int/mailbox/payload/1.0/BA2668600A0A01700A4DA3CDF9111849";
		return spectrumURL;
	}
	
	
	@Override
	public Object getClient() {
		// TODO Auto-generated method stub
		return null;
	}

}
