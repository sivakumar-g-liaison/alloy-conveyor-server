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
import com.liaison.mailbox.service.dto.configuration.processor.properties.FileWriterPropertiesDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.glass.util.GlassMessage;
import com.liaison.mailbox.service.glass.util.MailboxGlassMessageUtil;
import com.liaison.mailbox.service.queue.kafka.KafkaMessageService.KafkaMessageType;
import com.liaison.mailbox.service.queue.kafka.Producer;
import com.liaison.mailbox.service.storage.util.StorageUtilities;
import com.liaison.mailbox.service.util.DirectoryCreationUtil;
import com.liaison.mailbox.service.util.FileWriterUtil;
import com.liaison.mailbox.service.util.MailBoxUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONException;

import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Timestamp;

/**
 * This will place and manages the file with respect to the given location.
 * 
 * @author OFS
 */
public class FileWriter extends AbstractProcessor implements MailBoxProcessorI {

    private static final Logger LOG = LogManager.getLogger(FileWriter.class);
    private static String VALUE_OVERWRITE = "false";

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
            // stage file if Use file system is true and not HTTP or HTTPS remote uploader or file writer
            if ((this.canUseFileSystem() && !MailBoxUtil.isHttpOrHttpsRemoteUploader(configurationInstance)) 
                    || ProcessorType.FILEWRITER.equals(configurationInstance.getProcessorType())) {

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
                        Producer.produce(KafkaMessageType.FILEWRITER_CREATE, workTicket);
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

            } else if (MailBoxUtil.isHttpOrHttpsRemoteUploader(configurationInstance)) {

                writeStatus = addAnEntryToStagedFile(null, null, workTicket);

                if (writeStatus) {
                    LOG.info("Payload is successfully staged to STAGED_FILE ");
                } 
                message = (writeStatus ? "Added an entry in STAGED FILE for the file - " : "File already exists in STAGED_FILE - ")
                        + fileName + " - "
                        + workTicket.getGlobalProcessId();
            } else {

                //do remote uploader operation
                processorPayloadLocation = getPayloadLocation(workTicket);
                LOG.info("Started staging payload in staged file table and payload location {} and the filename is {}", processorPayloadLocation, fileName);
                writeStatus = addAnEntryToStagedFile(processorPayloadLocation, fileName, workTicket);

                if (writeStatus) {
                    LOG.info("Payload is successfully staged to STAGED_FILE with the location {}", processorPayloadLocation);

                    // For conditional sweeper: If canUseFileSystem is false:
                    // Persist the file entry in staged file
                    if (isAllFilesProcessedInFileGroup(workTicket)) {
                        persistTriggerFileEntry(workTicket, processorPayloadLocation);
                    }
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
     * Writes the trigger file
     * 
     * @param workTicket
     * @param processorPayloadLocation
     * @throws IOException
     * @throws JSONException 
     */
    private void writeTriggerFile(WorkTicket workTicket, String processorPayloadLocation) throws IOException, JSONException {

        String configuredPayloadLocation = getFileWriteLocation();
        String payloadLocation = MailBoxUtil.isEmpty(configuredPayloadLocation) ? processorPayloadLocation : configuredPayloadLocation;

        FileWriterUtil.writeTriggerFile(payloadLocation,
                                        workTicket.getAdditionalContext().get(MailBoxConstants.KEY_TRIGGER_FILE_NAME).toString(),
                                        workTicket.getAdditionalContext().get(MailBoxConstants.KEY_TRIGGER_FILE_URI).toString());

        Producer.produce(KafkaMessageType.FILEWRITER_CREATE,
                        workTicket.getAdditionalContext().get(MailBoxConstants.KEY_TRIGGER_FILE_URI).toString(),
                        workTicket.getAdditionalContext().get(MailBoxConstants.KEY_TRIGGER_FILE_PARENT_GPID).toString(),
                        workTicket.getAdditionalContext().get(MailBoxConstants.KEY_TRIGGER_FILE_NAME).toString(),
                        payloadLocation,
                        VALUE_OVERWRITE,
                        true);
    }
    
    /**
     * If all the file entries related with the file group are persists returns true;
     * else returns false;
     * 
     * @param workTicket
     * @return boolean
     */
    private boolean isAllFilesProcessedInFileGroup(WorkTicket workTicket) {

        if (null != workTicket.getAdditionalContext().get(MailBoxConstants.KEY_FILE_GROUP)
                && Boolean.valueOf(workTicket.getAdditionalContext().get(MailBoxConstants.KEY_FILE_GROUP).toString())) {

            StagedFileDAOBase dao = new StagedFileDAOBase();
            long dbCount = dao.findStagedFilesByParentGlobalProcessId(workTicket.getAdditionalContext().get(MailBoxConstants.KEY_TRIGGER_FILE_PARENT_GPID).toString());
            String fileCount = workTicket.getAdditionalContext().get(MailBoxConstants.KEY_FILE_COUNT).toString();
            int totalCount = Integer.parseInt(fileCount.split(MailBoxConstants.FILE_COUNT_SEPARATOR)[1]);
            return totalCount == dbCount;
        }

        return false;
    }

    /**
     * To persist trigger file entry in staged file.
     * For Shell MFT changes : GMB-1100
     * 
     * @param workTicket
     * @param processorPayloadLocation 
     */
    private void persistTriggerFileEntry(WorkTicket workTicket, String processorPayloadLocation) {

        String configuredPayloadLocation = getFileWriteLocation();
        String path = MailBoxUtil.isEmpty(configuredPayloadLocation) ? processorPayloadLocation : configuredPayloadLocation;

        StagedFileDAO stagedFileDAO = new StagedFileDAOBase();
        //Ignore if the trigger file is already staged in the current batch
        StagedFile sf = stagedFileDAO.findStagedFileByGpid(workTicket.getAdditionalContext().get(MailBoxConstants.KEY_TRIGGER_FILE_PARENT_GPID).toString());
        if (null != sf) {
            return;
        }

        //Overwrite the existing trigger file based on the processor guid and trigger file name
        String triggerFileName = workTicket.getAdditionalContext().get(MailBoxConstants.KEY_TRIGGER_FILE_NAME).toString();
        stagedFileDAO.updateRelayTriggerFileStatusInStagedFile(configurationInstance.getPguid(), EntityStatus.INACTIVE.name(), triggerFileName);

        //Persist Stated File
        StagedFile stagedFile = new StagedFile();
        stagedFile.setPguid(MailBoxUtil.getGUID());
        Timestamp timeStamp = MailBoxUtil.getTimestamp();
        stagedFile.setCreatedDate(timeStamp);
        stagedFile.setModifiedDate(timeStamp);
        stagedFile.setProcessorId(configurationInstance.getPguid());
        stagedFile.setGlobalProcessId(workTicket.getAdditionalContext().get(MailBoxConstants.KEY_TRIGGER_FILE_PARENT_GPID).toString());
        stagedFile.setStagedFileStatus(EntityStatus.ACTIVE.name());
        stagedFile.setFilePath(path);
        stagedFile.setFileName(triggerFileName);
        stagedFile.setSpectrumUri(workTicket.getAdditionalContext().get(MailBoxConstants.KEY_TRIGGER_FILE_URI).toString());
        stagedFile.setClusterType(MailBoxUtil.CLUSTER_TYPE);
        stagedFile.setProcessDc(MailBoxUtil.DATACENTER_NAME);
        stagedFile.setFileSize("0");
        stagedFile.setParentGlobalProcessId(MailBoxConstants.TRIGGER_FILE);
        stagedFile.setProcessorType(configurationInstance.getProcessorType().name());
        stagedFile.setMailboxId((null != workTicket.getAdditionalContext().get(MailBoxConstants.KEY_MAILBOX_ID))
                ? workTicket.getAdditionalContext().get(MailBoxConstants.KEY_MAILBOX_ID).toString() : null);
        stagedFileDAO.persist(stagedFile);
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
            DirectoryCreationUtil.createPathIfNotAvailable(processorPayloadLocation);
        } else {

            String mode = workTicket.getAdditionalContextItem(MailBoxConstants.KEY_TARGET_DIRECTORY_MODE);
            if (!MailBoxUtil.isEmpty(mode)
                    && MailBoxConstants.TARGET_DIRECTORY_MODE_OVERWRITE.equals(mode)) {
                DirectoryCreationUtil.createPathIfNotAvailable(targetDirectory);
                processorPayloadLocation = targetDirectory;
            } else {
                processorPayloadLocation = getFileWriteLocation() + File.separatorChar + targetDirectory;
                DirectoryCreationUtil.createPathIfNotAvailable(processorPayloadLocation);
            }
        }
        return processorPayloadLocation;
    }

    /**
	 * This Method create local folders if not available and returns the path.
	 *
	 * * @param processorDTO it have details of processor
	 *
	 */
	@Override
	public String createLocalPath() {
		String configuredPath = null;
		try {
			configuredPath = getFileWriteLocation();
			DirectoryCreationUtil.createPathIfNotAvailable(configuredPath);
			return configuredPath;

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
     * @throws JSONException 
	 */
    private boolean writeDataToGivenLocation(InputStream response, String targetLocation, String filename, WorkTicket workTicket) throws IOException, JSONException {

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
					if (null != workTicket.getAdditionalContext().get(MailBoxConstants.KEY_TRIGGER_FILE_PARENT_GPID)) {
					    stagedFile.setParentGlobalProcessId(workTicket.getAdditionalContext().get(MailBoxConstants.KEY_TRIGGER_FILE_PARENT_GPID).toString());
					}
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
     * @throws JSONException 
     */
    private void persistFile(InputStream response, File file, WorkTicket workTicket, StagedFileDAOBase dao) throws IOException, JSONException {
        
        //write the file
        FileOutputStream outputStream = null;
        try {

            //add status indicator if specified to indicate that uploading is in progress
            String statusIndicator = getStatusIndicator();
            String stagingFileName = (MailBoxUtil.isEmpty(statusIndicator))
                    ? file.getAbsolutePath()
                    : file.getAbsolutePath() + MailBoxConstants.DOT_OPERATOR + statusIndicator;
            File stagingFile = new File(stagingFileName);

            outputStream = new FileOutputStream(stagingFile);
            long fileSize = (long) IOUtils.copy(response, outputStream);
            workTicket.setPayloadSize(fileSize);
            
            // Renames the uploaded file to original extension if the fileStatusIndicator is given by User
            if (!MailBoxUtil.isEmpty(statusIndicator)) {
                Files.move(stagingFile.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (outputStream != null) {
                outputStream.close();
            }
        }

        //To add more details in staged file
        workTicket.setAdditionalContext(MailBoxConstants.KEY_FILE_PATH, file.getParent());

        //Persist the new file details
        dao.persistStagedFile(workTicket,
                configurationInstance.getPguid(),
                configurationInstance.getProcessorType().name(),
                this.isDirectUploadEnabled());

        // For conditional sweeper: If canUseFileSystem is true:
        // Write the trigger file in the disc location if all the files are processed in the file group.
        // And persist the file entry in staged file
        if (isAllFilesProcessedInFileGroup(workTicket)) {
            writeTriggerFile(workTicket, file.getParent());
            persistTriggerFileEntry(workTicket, file.getParent());
        }

    }

    /**
     * It returns the file transfer status indicator string.
     * It only applicable for filewriter.
     * 
     * @return
     * @throws IOException
     * @throws IllegalAccessException
     */
    private String getStatusIndicator() throws IOException, IllegalAccessException {

        if (ProcessorType.FILEWRITER.equals(configurationInstance.getProcessorType())) {
            return ((FileWriterPropertiesDTO) getProperties()).getFileTransferStatusIndicator();
        }
        return null;
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
        File file = null;
        StagedFile stagedFile = null;

        if (!MailBoxUtil.isEmpty(processorPayloadLocation) && !MailBoxUtil.isEmpty(fileName)) {
            file = new File(processorPayloadLocation + File.separatorChar + fileName);
            stagedFile = dao.findStagedFilesByProcessorId(configurationInstance.getPguid(), file.getParent(), fileName);
        } else {
            file = new File(workTicket.getPayloadURI() + File.separatorChar + workTicket.getFileName());
            stagedFile = dao.findStagedFileByProcessorIdAndGpid(configurationInstance.getPguid(), workTicket.getGlobalProcessId());
        }

        if (null != stagedFile) {

            if (MailBoxConstants.OVERWRITE_FALSE.equals(isOverwrite)) {
                workTicket.setAdditionalContext(MailBoxConstants.KEY_FILE_PATH, file.getParent());
                return false;
            } else if (MailBoxConstants.OVERWRITE_TRUE.equals(isOverwrite)) {

                //In-activate the old entity
                stagedFile.setStagedFileStatus(EntityStatus.INACTIVE.name());
                dao.merge(stagedFile);
                logDuplicateStatus(stagedFile.getFileName(), stagedFile.getFilePath(), stagedFile.getGlobalProcessId(), workTicket.getGlobalProcessId());

                if (workTicket.getPayloadSize() == 0 ||  workTicket.getPayloadSize() == -1) {
                    workTicket.setPayloadSize(StorageUtilities.getPayloadSize(workTicket.getPayloadURI()));
                }
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