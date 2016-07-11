/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall 
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.core.processor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.StopWatch;
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
import com.liaison.mailbox.service.glass.util.GlassMessage;
import com.liaison.mailbox.service.glass.util.MailboxGlassMessageUtil;
import com.liaison.mailbox.service.storage.util.StorageUtilities;
import com.liaison.mailbox.service.util.MailBoxUtil;

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
        boolean writeStatus = false;
        StopWatch stopwatch = new StopWatch();

        try {

            stopwatch.start();
            glassMessage = MailboxGlassMessageUtil.getGlassMessage(
                    workTicket,
                    configurationInstance.getProcsrProtocol(),
                    configurationInstance.getProcessorType());
            MailboxGlassMessageUtil.logProcessingStatus(glassMessage, StatusType.RUNNING, "File Staging is started");

            String fileName = workTicket.getFileName();
            LOG.info("filename from the workticket - {}", fileName);

            //get payload from spectrum
            try (InputStream payload = StorageUtilities.retrievePayload(workTicket.getPayloadURI())) {

                if (null == payload) {
                    LOG.error("Failed to retrieve payload from spectrum");
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
                    LOG.error("payload or filewrite location not configured for processor {}", configurationInstance.getProcsrName());
                    throw new MailBoxServicesException(Messages.LOCATION_NOT_CONFIGURED, MailBoxConstants.COMMON_LOCATION, Response.Status.CONFLICT);
                }
                LOG.info("Started writing payload to {} and the filename is {}", processorPayloadLocation, fileName);

                // write the payload retrieved from spectrum to the configured location of processor
                writeStatus = writeDataToGivenLocation(payload, processorPayloadLocation, fileName, workTicket);
                if (writeStatus) {
                	LOG.info("Payload is successfully written to {}", processorPayloadLocation);
                } else {

                	LOG.info("File {} already exists at {} and should not be overwritten", fileName, processorPayloadLocation);
                	//To avoid staged file entry
                	workTicket.setAdditionalContext(MailBoxConstants.FILE_EXISTS, Boolean.TRUE.toString());
                }

            }

            //GLASS LOGGING BEGINS//
            StringBuilder message = new StringBuilder()
                    .append(writeStatus ? "Payload written at target location : " : "File already exists at ")
                    .append(processorPayloadLocation)
                    .append(File.separatorChar)
                    .append(fileName);

            MailboxGlassMessageUtil.logProcessingStatus(glassMessage, StatusType.SUCCESS, message.toString());
            glassMessage.logFourthCornerTimestamp();
             //GLASS LOGGING ENDS//

            stopwatch.stop();
            LOG.info("Total time taken to process files {}", stopwatch.getTime());

        } catch (Exception e) {
            LOG.error("File Staging failed", e);
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

	 /**
     * Logs TVAPI status and event message in LENS
     *
     * @param files file names
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

            StringBuilder message = new StringBuilder()
            					.append("File ")
            					.append(stagedFile.getFileName())
            					.append(" is picked up by the customer");

            MailboxGlassMessageUtil.logGlassMessage(
                    stagedFile.getGPID(),
                    configurationInstance.getProcessorType(),
                    configurationInstance.getProcsrProtocol(),
                    stagedFile.getFileName(),
                    stagedFile.getFilePath(),
                    0,
                    ExecutionState.COMPLETED,
                    message.toString(),
					null);

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
	 * @return true if it is successfully written the file to the location, otherwise false
	 * @throws IOException
	 */
	public boolean writeDataToGivenLocation(InputStream response, String targetLocation, String filename, WorkTicket workTicket) throws IOException {

		LOG.debug("Started writing given inputstream to given location {}", targetLocation);
		StagedFileDAOBase dao = new StagedFileDAOBase();
		String isOverwrite = workTicket.getAdditionalContextItem(MailBoxConstants.KEY_OVERWRITE).toString().toLowerCase();

		File file = new File(targetLocation + File.separatorChar + filename);

		if (file.exists()) {

			if (MailBoxConstants.OVERWRITE_FALSE.equals(isOverwrite)) {
				workTicket.setAdditionalContext(MailBoxConstants.KEY_FILE_PATH, file.getParent());
				return false;
			} else if (MailBoxConstants.OVERWRITE_TRUE.equals(isOverwrite)) {

				StagedFile stagedFile = dao.findStagedFilesByProcessorId(configurationInstance.getPguid(), file.getParent(), filename);
				if (null != stagedFile) {

					//In-activate the old entity
					stagedFile.setStagedFileStatus(EntityStatus.INACTIVE.name());
	        		dao.merge(stagedFile);
                    logDuplicateStatus(stagedFile.getFileName(), stagedFile.getFilePath(), stagedFile.getGlobalProcessId(), workTicket.getGlobalProcessId());
				}

				persistFile(response, file, workTicket, dao);
				return true;
			} else {

				StringBuilder message = new StringBuilder();
				message.append("The file(").append(filename).append(") exists at the location - ").append(targetLocation);
				throw new MailBoxServicesException(message.toString(), Response.Status.BAD_REQUEST);
			}
		} else {

			logGlassMessage(Messages.FILE_WRITER_SUCCESS_MESSAGE.value(), file, ExecutionState.COMPLETED);
			persistFile(response, file, workTicket, dao);
			return true;
		}

	}

    /**
     * method to set the file size to workticket and persist the file 
     * 
     * @param response
     * @param file
     * @param workTicket
     * @param dao
     * @throws IOException
     */
    private void persistFile(InputStream response, File file, WorkTicket workTicket, StagedFileDAOBase dao) throws IOException {
        
        //write the file
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            long fileSize = (long) IOUtils.copy(response, outputStream);
            workTicket.setPayloadSize(fileSize);
        }

        //To add more details in staged file
        workTicket.setAdditionalContext(MailBoxConstants.KEY_FILE_PATH, file.getParent());

        //Persist the new file deatils
        dao.persistStagedFile(workTicket, configurationInstance.getPguid(), configurationInstance.getProcessorType().name());
        
    }

}
