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
import com.liaison.mailbox.service.dto.GlassMessageDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.glass.util.GlassMessage;
import com.liaison.mailbox.service.glass.util.MailboxGlassMessageUtil;
import com.liaison.mailbox.service.storage.util.StorageUtilities;
import com.liaison.mailbox.service.util.MailBoxUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

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
	public void runProcessor(Object dto) {

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

            String message = "";
            if (this.canUseFileSystem() || ProcessorType.FILEWRITER.equals(configurationInstance.getProcessorType())) {

                //get payload from spectrum
                InputStream payload = null;
                try {

                    payload = StorageUtilities.retrievePayload(workTicket.getPayloadURI());
                    if (null == payload) {
                        LOG.error("Failed to retrieve payload from spectrum");
                        throw new MailBoxServicesException("Failed to retrieve payload from spectrum", Response.Status.BAD_REQUEST);
                    }

                    processorPayloadLocation = getPayloadLocation(workTicket);
                    if (null == processorPayloadLocation) {
                        LOG.error("payload or filewrite location not configured for processor {}", configurationInstance.getProcsrName());
                        throw new MailBoxServicesException(Messages.LOCATION_NOT_CONFIGURED, MailBoxConstants.COMMON_LOCATION, Response.Status.CONFLICT);
                    }

                    // write the payload retrieved from spectrum to the configured location of processor
                    LOG.info("Started writing payload to {} and the filename is {}", processorPayloadLocation, fileName);
                    writeStatus = writeDataToGivenLocation(payload, processorPayloadLocation, fileName, workTicket);
                    if (writeStatus) {
                        LOG.info("Payload is successfully written to {}", processorPayloadLocation);
                    } else {

                        LOG.info("File {} already exists at {} and should not be overwritten", fileName, processorPayloadLocation);
                        //To avoid staged file entry
                        workTicket.setAdditionalContext(MailBoxConstants.FILE_EXISTS, Boolean.TRUE.toString());
                    }

                } finally {
                    if (payload != null) {
                        payload.close();
                    }
                }

                message = (writeStatus ? "Payload written at target location : " : "File already exists at the location - ")
                        + processorPayloadLocation
                        + File.separatorChar
                        + fileName;

            } else {

                //do remote uploader operation
                processorPayloadLocation = getPayloadLocation(workTicket);
                LOG.info("Started staging payload in staged file table and payload location {} and the filename is {}", processorPayloadLocation, fileName);
                writeStatus = addAnEntryToStagedFile(processorPayloadLocation, fileName, workTicket);

                if (writeStatus) {
                    LOG.info("Payload is successfully staged to STAGED_FILE with the location {}", processorPayloadLocation);
                } else {
                    //To avoid staged file entry
                    LOG.info("File {} already exists at STAGED_FILE with location {} and should not be overwritten", fileName, processorPayloadLocation);
                    workTicket.setAdditionalContext(MailBoxConstants.FILE_EXISTS, Boolean.TRUE.toString());
                }

                message = (writeStatus ? "Added an entry in STAGED FILE for the file - " : "File already exists in STAGED_FILE - ")
                        + processorPayloadLocation
                        + File.separatorChar
                        + fileName;
            }

            //GLASS LOGGING BEGINS//
            MailboxGlassMessageUtil.logProcessingStatus(glassMessage, StatusType.SUCCESS, message);
            //GLASS LOGGING ENDS//

        } catch (Throwable e) {
            
            //caught Throwable to handle the class initialization errors and this is needed to update proper LENS status
            LOG.error("File Staging failed", e);
            //GLASS LOGGING ENDS//
            throw new RuntimeException(e);
        } finally {
            stopwatch.stop();
            LOG.info("Total time taken to process files {}", stopwatch.getTime());
        }

	}

    /**
     * get payload location from the workticket if it is given or processor configuration
     *
     * @param workTicket workticket
     * @return payload location
     * @throws IOException
     */
    private String getPayloadLocation(WorkTicket workTicket) throws IOException {

        //Supports targetDirectory from the workticket if it is available otherwise it would use the configured payload location.
        //It takes decision based on mode, either to append the path to the payload location or ignore the payload location and use the targetDirectory.
        //The only allowed location to write the paylaod is /data/(sftp/ftp/ftps)/**/(inbox/outbox)
        String processorPayloadLocation;
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
        return processorPayloadLocation;
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
	private boolean writeDataToGivenLocation(InputStream response, String targetLocation, String filename, WorkTicket workTicket) throws IOException {

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

                throw new MailBoxServicesException("The file(" + filename + ") exists at the location - " + targetLocation,
                        Response.Status.BAD_REQUEST);
			}
		} else {

			logToLens(Messages.FILE_WRITER_SUCCESS_MESSAGE.value(), file, ExecutionState.COMPLETED);
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

        //Persist the new file details
        dao.persistStagedFile(workTicket,
                configurationInstance.getPguid(),
                configurationInstance.getProcessorType().name(),
                this.isDirectUploadEnabled());
        
    }

    /**
     * Adds an entry to staged file table for the uploader
     *
     * @param processorPayloadLocation processor payload location
     * @param fileName name of the file
     * @param workTicket workticket received from SB
     * @return true if it staged successfully otherwise false
     */
    private boolean addAnEntryToStagedFile(String processorPayloadLocation, String fileName, WorkTicket workTicket) {

        StagedFileDAOBase dao = new StagedFileDAOBase();
        String isOverwrite = workTicket.getAdditionalContextItem(MailBoxConstants.KEY_OVERWRITE).toString().toLowerCase();
        File file = new File(processorPayloadLocation + File.separatorChar + fileName);

        StagedFile stagedFile = dao.findStagedFilesByProcessorId(configurationInstance.getPguid(), file.getParent(), fileName);
        if (null != stagedFile) {

            if (MailBoxConstants.OVERWRITE_FALSE.equals(isOverwrite)) {
                workTicket.setAdditionalContext(MailBoxConstants.KEY_FILE_PATH, file.getParent());
                return false;
            } else if (MailBoxConstants.OVERWRITE_TRUE.equals(isOverwrite)) {

                //In-activate the old entity
                stagedFile.setStagedFileStatus(EntityStatus.INACTIVE.name());
                dao.merge(stagedFile);
                logDuplicateStatus(stagedFile.getFileName(), stagedFile.getFilePath(), stagedFile.getGlobalProcessId(), workTicket.getGlobalProcessId());

                workTicket.setAdditionalContext(MailBoxConstants.KEY_FILE_PATH, file.getParent());
                dao.persistStagedFile(workTicket,
                        configurationInstance.getPguid(),
                        configurationInstance.getProcessorType().name(),
                        this.isDirectUploadEnabled());
                return true;
            } else {
                throw new MailBoxServicesException("The file(" + fileName + ") exists at the location - " + processorPayloadLocation,
                        Response.Status.BAD_REQUEST);
            }

        } else {

            if (workTicket.getPayloadSize() == 0 ||  workTicket.getPayloadSize() == -1) {
                workTicket.setPayloadSize(StorageUtilities.getPayloadSize(workTicket.getPayloadURI()));
            }
            workTicket.setAdditionalContext(MailBoxConstants.KEY_FILE_PATH, file.getParent());
            dao.persistStagedFile(workTicket,
                    configurationInstance.getPguid(),
                    configurationInstance.getProcessorType().name(),
                    this.isDirectUploadEnabled());
            return true;
        }
    }

}
