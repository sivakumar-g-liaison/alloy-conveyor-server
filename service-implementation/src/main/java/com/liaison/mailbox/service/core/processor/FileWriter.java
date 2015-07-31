package com.liaison.mailbox.service.core.processor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;

import com.liaison.commons.jaxb.JAXBUtility;
import com.liaison.commons.message.glass.dom.StatusType;
import com.liaison.dto.queue.WorkTicket;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.dtdm.model.Processor;
import com.liaison.mailbox.enums.ExecutionState;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.service.core.fsm.MailboxFSM;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.storage.util.StorageUtilities;
import com.liaison.mailbox.service.util.GlassMessage;
import com.liaison.mailbox.service.util.MailBoxUtil;

public class FileWriter extends AbstractProcessor implements MailBoxProcessorI {
    
    private static final Logger LOG = LogManager.getLogger(FileWriter.class);
	
	@SuppressWarnings("unused")
	private FileWriter() {
		// to force creation of instance only by passing the processor entity
	}

	public FileWriter(Processor configurationInstance) {
		super(configurationInstance);
	}

	@Override
	public void runProcessor(Object dto, MailboxFSM fsm) {
	    
	    WorkTicket workTicket = (WorkTicket) dto;
        GlassMessage glassMessage = null;
        String processorPayloadLocation = null;

        try {

            LOG.info("#####################----WATCHDOG INVOCATION BLOCK-AFTER CONSUMING FROM QUEUE---############################################");

            glassMessage = new GlassMessage(workTicket);
            glassMessage.setStatus(ExecutionState.COMPLETED);
            glassMessage.logProcessingStatus(StatusType.RUNNING, "File Staging is started");

            LOG.info(constructMessage("Start Run"));
            LOG.info(constructMessage("JSON received from SB {}"), new JSONObject(JAXBUtility.marshalToJSON(workTicket)).toString(2));
            long startTime = System.currentTimeMillis();

            // check if file Name is available in the payloadTicketRequest if so save the file with the
            // provided file Name if not save with processor Name with Timestamp
            String fileName = (workTicket.getFileName() == null)
                    ? (configurationInstance.getProcsrName() + System.nanoTime())
                    : workTicket.getFileName();

            LOG.info(constructMessage("Global PID", seperator, workTicket.getGlobalProcessId(), "retrieved from workticket for file", fileName));
            glassMessage.setCategory(configurationInstance.getProcessorType());
            glassMessage.setProtocol(configurationInstance.getProcessorType().getCode());
            LOG.info(constructMessage("Found the processor to write the payload in the local payload location"));

            //get payload from spectrum
            try (InputStream payload = StorageUtilities.retrievePayload(workTicket.getPayloadURI())) {

                if (null == payload) {
                    LOG.error(constructMessage("Global PID",
                            seperator,
                            workTicket.getGlobalProcessId(),
                            seperator,
                            "Failed to retrieve payload from spectrum"));
                    throw new MailBoxServicesException("Failed to retrieve payload from spectrum", Response.Status.BAD_REQUEST);
                }

                //get local payload location from uploader/filewriter
                processorPayloadLocation = getFileWriteLocation();
                if (null == processorPayloadLocation) {
                    LOG.error(constructMessage("Global PID",
                            seperator,
                            workTicket.getGlobalProcessId(),
                            seperator,
                            "payload or filewrite location not configured for processor {}"), configurationInstance.getProcsrName());
                    throw new MailBoxServicesException(Messages.LOCATION_NOT_CONFIGURED, MailBoxConstants.COMMON_LOCATION, Response.Status.CONFLICT);
                }

                boolean isOverwrite = (workTicket.getAdditionalContextItem(MailBoxConstants.KEY_OVERWRITE) == Boolean.TRUE) ? true : false;
                LOG.info(constructMessage("Global PID",
                        seperator,
                        workTicket.getGlobalProcessId(),
                        seperator,
                        "Started writing payload to ",
                        processorPayloadLocation,
                        seperator,
                        fileName));

                // write the payload retrieved from spectrum to the configured location of processor
                MailBoxUtil.writeDataToGivenLocation(payload, processorPayloadLocation, fileName, isOverwrite);
                LOG.info(constructMessage("Global PID",
                        seperator,
                        workTicket.getGlobalProcessId(),
                        seperator,
                        "Payload is successfully written to ",
                        processorPayloadLocation,
                        seperator,
                        fileName));
            }

            //GLASS LOGGING BEGINS//
            glassMessage.setOutAgent(processorPayloadLocation);

            //GLASS LOGGING CORNER 4 //
            StringBuffer message = new StringBuffer()
                    .append("Payload written at target location : ")
                    .append(processorPayloadLocation)
                    .append(File.separatorChar)
                    .append(fileName);

            glassMessage.logProcessingStatus(StatusType.SUCCESS, message.toString());
            glassMessage.logFourthCornerTimestamp();
             //GLASS LOGGING ENDS//
            LOG.info("#################################################################");

            long endTime = System.currentTimeMillis();
            LOG.info(constructMessage("Number of files processed 1"));
            LOG.info(constructMessage("Total time taken to process files {}"), endTime - startTime);
            LOG.info(constructMessage("End run"));

        } catch (Exception e) {
            LOG.error(constructMessage("File Staging failed", seperator, e.getMessage()), e);
             //GLASS LOGGING ENDS//
            throw new RuntimeException(e);
        }
		
	}
    
	/**
	 * This Method create local folders if not available.
	 * 
	 * * @param processorDTO it have details of processor
	 * 
	 */
	@Override
	public void createLocalPath() {
		String configuredPath = null;
		try {
			configuredPath = getFileWriteLocation();
			createPathIfNotAvailable(configuredPath);

		} catch (IOException e) {
			throw new MailBoxConfigurationServicesException(Messages.LOCAL_FOLDERS_CREATION_FAILED,
					configuredPath, Response.Status.BAD_REQUEST,e.getMessage());
		}

	}
	
	@Override
    public Object getClient() {
        return null;
    }

    @Override
    public void downloadDirectory(Object client, String remotePayloadLocation, String localTargetLocation) {
    }

    @Override
    public void uploadDirectory(Object client, String localPayloadLocation, String remoteTargetLocation) {
    }

    @Override
    public void cleanup() {
    }

}
