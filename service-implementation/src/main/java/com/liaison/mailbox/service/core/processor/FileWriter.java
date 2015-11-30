package com.liaison.mailbox.service.core.processor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
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
import com.liaison.mailbox.enums.ProcessorType;
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

/**
 * This will place and manages the file with respect to the given location.
 * 
 * @author OFS
 */
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
        ProcessorType processorType = null;
        boolean writeStatus = false;

        try {

            glassMessage = new GlassMessage(workTicket);
            glassMessage.setStatus(ExecutionState.COMPLETED);
            glassMessage.logProcessingStatus(StatusType.RUNNING, "File Staging is started", configurationInstance.getProcsrProtocol(), configurationInstance.getProcessorType().name());

            LOG.info(constructMessage("Start Run"));
            LOG.info(constructMessage("Workticket received from SB {}"), new JSONObject(JAXBUtility.marshalToJSON(workTicket)).toString(2));
            long startTime = System.currentTimeMillis();           
            String fileName = workTicket.getFileName();
            LOG.info(constructMessage("Global PID", seperator, workTicket.getGlobalProcessId(), "retrieved from workticket for file", fileName));
            processorType = configurationInstance.getProcessorType();
            glassMessage.setCategory(processorType);
            glassMessage.setProtocol(processorType.getCode());
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

                //Supports targetDirectory from the workticket if it is available otherwise it would use the configured payload location.
                //It takes decision based on mode, either to append the path to the payload location or ignore the payload location and use the targetDirectory.
                //The only allowed location to write the paylaod is /data/(sftp/ftp/ftps)/**/(inbox/outbox)
                String targetDirectory = workTicket.getAdditionalContextItem(MailBoxConstants.KEY_TARGET_DIRECTORY);
                if (MailBoxUtil.isEmpty(targetDirectory)) {
                	processorPayloadLocation = getFileWriteLocation();
                	createPathIfNotAvailable(processorPayloadLocation);
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

                LOG.info(constructMessage("Global PID",
                        seperator,
                        workTicket.getGlobalProcessId(),
                        seperator,
                        "Started writing payload to ",
                        processorPayloadLocation,
                        seperator,
                        fileName));

                // write the payload retrieved from spectrum to the configured location of processor
                writeStatus = writeDataToGivenLocation(payload, processorPayloadLocation, fileName, workTicket);
                if (writeStatus) {

                	LOG.info(constructMessage("Global PID",
                			seperator,
                			workTicket.getGlobalProcessId(),
                			seperator,
                			"Payload is successfully written to ",
                			processorPayloadLocation,
                			seperator,
                			fileName));
                } else {

                	LOG.info(constructMessage("Global PID",
                			seperator,
                			workTicket.getGlobalProcessId(),
                			seperator,
                			"File {} already exists at {} and should not be overwritten"),
                			fileName,
                			processorPayloadLocation);

                	//To aviod staged file entry
                	workTicket.setAdditionalContext(MailBoxConstants.FILE_EXISTS, Boolean.TRUE.toString());
                }

            }

            //GLASS LOGGING BEGINS//
            glassMessage.setOutAgent(processorPayloadLocation);

            //GLASS LOGGING CORNER 4 //
            StringBuilder message = new StringBuilder()
                    .append(writeStatus ? "Payload written at target location : " : "File already exists at ")
                    .append(processorPayloadLocation)
                    .append(File.separatorChar)
                    .append(fileName);

            glassMessage.logProcessingStatus(StatusType.SUCCESS, message.toString(), configurationInstance.getProcsrProtocol(), configurationInstance.getProcessorType().name());
            glassMessage.logFourthCornerTimestamp();
             //GLASS LOGGING ENDS//

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
        List <StagedFile> stagedFiles = stagedFileDAO.findStagedFilesByProcessorId(configurationInstance.getPguid());

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
            glassMessage.logProcessingStatus(StatusType.SUCCESS, message.toString(), configurationInstance.getProcsrProtocol(), configurationInstance.getProcessorType().name());

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
    public void cleanup() {
    }

    /**
	 * method to write the given inputstream to given location
	 *
	 * @param response payload
	 * @param targetLocation location to write the payload
	 * @param filename file name 
	 * @param isOverwrite whether to overwrite or not
	 * @return true if it is successfully written the file to the location, otherwise false
	 * @throws IOException
	 */
	public boolean writeDataToGivenLocation(InputStream response, String targetLocation, String filename, WorkTicket workTicket) throws IOException {

		LOG.debug("Started writing given inputstream to given location {}", targetLocation);
		StagedFileDAOBase dao = new StagedFileDAOBase();
		String isOverwrite = String.valueOf(workTicket.getAdditionalContextItem(MailBoxConstants.KEY_OVERWRITE)).toLowerCase();

		File file = new File(targetLocation + File.separatorChar + filename);

		if (file.exists()) {

			if (MailBoxConstants.OVERWRITE_FALSE.equals(isOverwrite)) {
				return false;
			} else if (MailBoxConstants.OVERWRITE_TRUE.equals(isOverwrite)) {

				StagedFile stagedFile = dao.findStagedFilesByProcessorId(configurationInstance.getPguid(), filename);
				if (null != stagedFile) {

					//In-activate the old entity
					stagedFile.setStagedFileStatus(EntityStatus.INACTIVE.name());
	        		dao.merge(stagedFile);
					logDuplicateStatus(stagedFile, workTicket.getGlobalProcessId());
				}

				//write the file
				try (FileOutputStream outputStream = new FileOutputStream(file)) {
					IOUtils.copy(response, outputStream);
				}
				
				//To add more details in staged file
            	workTicket.setAdditionalContext(MailBoxConstants.KEY_FILE_PATH, targetLocation);

				//Persist the new file deatils
				dao.persistStagedFile(workTicket, configurationInstance.getPguid(), configurationInstance.getProcessorType().name());

				return true;
			} else {

				StringBuilder message = new StringBuilder();
				message.append("The file(").append(filename).append(") exists at the location - ").append(targetLocation);
				throw new MailBoxServicesException(message.toString(), Response.Status.BAD_REQUEST);
			}
		} else {

			logGlassMessage(Messages.FILE_WRITER_SUCCESS_MESSAGE.value(), file, ExecutionState.COMPLETED);
			try (FileOutputStream outputStream = new FileOutputStream(file)) {
				IOUtils.copy(response, outputStream);
			}

			//To add more details in staged file
        	workTicket.setAdditionalContext(MailBoxConstants.KEY_FILE_PATH, targetLocation);

			//Persist if no file exists
			dao.persistStagedFile(workTicket, configurationInstance.getPguid(), configurationInstance.getProcessorType().name());
			return true;
		}

	}

}
