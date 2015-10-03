package com.liaison.mailbox.service.core.processor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;

import com.liaison.commons.jaxb.JAXBUtility;
import com.liaison.commons.message.glass.dom.StatusType;
import com.liaison.dto.queue.WorkTicket;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.dtdm.model.Processor;
import com.liaison.mailbox.enums.EntityStatus;
import com.liaison.mailbox.enums.ExecutionState;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.rtdm.dao.StagedFileDAO;
import com.liaison.mailbox.rtdm.dao.StagedFileDAOBase;
import com.liaison.mailbox.rtdm.model.StagedFile;
import com.liaison.mailbox.service.core.fsm.MailboxFSM;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.storage.util.StorageUtilities;
import com.liaison.mailbox.service.util.GlassMessage;
import com.liaison.mailbox.service.util.MailBoxUtil;
import com.liaison.mailbox.service.util.TransactionVisibilityClient;

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

                //TargetDirectory - From SB, Payload Location - Mailbox Filewriter
                //Filewriter supports targetDirectory from the workticket if it is available otherwise it would use the configured payload location.
                //It takes decision based on mode, either to append the path to the payload location or ignore the payload location and use the targetDirectory.
                String targetDirectory = workTicket.getAdditionalContextItem(MailBoxConstants.KEY_TARGET_DIRECTORY);
                if (MailBoxUtil.isEmpty(targetDirectory)) {
                	processorPayloadLocation = getFileWriteLocation();
                } else {

                	String mode = workTicket.getAdditionalContextItem(MailBoxConstants.KEY_TARGET_DIRECTORY_MODE);
                	if (!MailBoxUtil.isEmpty(mode)
                			&& MailBoxConstants.TARGET_DIRECTORY_MODE_OVERWRITE.equals(mode)) {
                		createPathIfNotAvailable(targetDirectory);
                		processorPayloadLocation = targetDirectory;
                	} else {
                		processorPayloadLocation = getFileWriteLocation() + File.separatorChar + targetDirectory;
                		createPathIfNotAvailable(processorPayloadLocation);
                	}
                }

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
            StringBuilder message = new StringBuilder()
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
	 * This method will get the file write location of filewriter and check if any file exist in that specified location
	 *
	 * @param processor
	 * @return boolean - if the file exists it will return value of true otherwise a value of false.
	 * @throws MailBoxServicesException
	 * @throws IOException
	 */
	public List<String> checkFileExistence() throws MailBoxServicesException, IOException {

		LOG.debug ("Entering file Existence check for File Writer processor");
		String fileWriteLocation = getFileWriteLocation();
		List<String> fileList = new ArrayList<>();
		if (null == fileWriteLocation) {
			LOG.error("filewrite location not configured for processor {}", configurationInstance.getProcsrName());
			throw new MailBoxServicesException(Messages.LOCATION_NOT_CONFIGURED, MailBoxConstants.FILEWRITE_LOCATION, Response.Status.CONFLICT);
		}
		File fileWriteLocationDirectory = new File(fileWriteLocation);
		if (fileWriteLocationDirectory.isDirectory() && fileWriteLocationDirectory.exists()) {
			String[] files =  fileWriteLocationDirectory.list();
			// Log Message to lens
			fileList = Arrays.asList(files);
			logGlassMessage(Arrays.asList(files));
		} else {
			throw new MailBoxServicesException(Messages.INVALID_DIRECTORY, Response.Status.BAD_REQUEST);
		}
		LOG.debug("File Eixstence check completed for File writer. File exists - {}", fileList.isEmpty());
		return fileList;

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

	 /**
     * Logs TVAPI status and event message in LENS
     *
     * @param message Message String to be logged in LENS event log
     * @param file java.io.File
     * @param status Status of the LENS logging
     */
	protected void logGlassMessage(List<String> files) {

        StagedFileDAO stagedFileDAO = new StagedFileDAOBase();
        List <StagedFile> stagedFiles = stagedFileDAO.findStagedFilesOfProcessorsBasedOnMeta(configurationInstance.getPguid());

        for (StagedFile stagedFile : stagedFiles) {

        	// if the files contain the stagedFile Name, then the file is not picked up
        	// by the customer so continue to next staged file
        	if (files.contains(stagedFile.getFileName())) {
        		LOG.info("File {} is not picked up by the customer", stagedFile.getFileName());
        		continue;
        	}
            TransactionVisibilityClient transactionVisibilityClient = new TransactionVisibilityClient();
            GlassMessage glassMessage = new GlassMessage();
            glassMessage.setGlobalPId(stagedFile.getPguid());
            glassMessage.setCategory(configurationInstance.getProcessorType());
            glassMessage.setProtocol(configurationInstance.getProcsrProtocol());

            glassMessage.setStatus(ExecutionState.COMPLETED);
            glassMessage.setOutAgent(configurationInstance.getProcsrProtocol());
            glassMessage.setOutSize(null);
            glassMessage.setOutboundFileName(stagedFile.getFileName());

            StringBuilder message = new StringBuilder()
            					.append("File ")
            					.append(stagedFile.getFileName())
            					.append(" is picked up by the customer");
            glassMessage.logProcessingStatus(StatusType.SUCCESS, message.toString());

            //Fourth corner timestamp
            glassMessage.logFourthCornerTimestamp();

            //TVAPI
            transactionVisibilityClient.logToGlass(glassMessage);

            // Inactivate the stagedFile
            stagedFile.setStagedFileStatus(EntityStatus.INACTIVE.value());
            stagedFileDAO.merge(stagedFile);
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
