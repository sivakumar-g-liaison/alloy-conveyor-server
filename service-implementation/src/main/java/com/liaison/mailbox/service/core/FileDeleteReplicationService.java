/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.core;

import com.liaison.commons.logging.LogTags;
import com.liaison.mailbox.dtdm.dao.ProcessorConfigurationDAO;
import com.liaison.mailbox.dtdm.dao.ProcessorConfigurationDAOBase;
import com.liaison.mailbox.dtdm.model.Processor;
import com.liaison.mailbox.enums.EntityStatus;
import com.liaison.mailbox.enums.ExecutionState;
import com.liaison.mailbox.enums.ProcessorType;
import com.liaison.mailbox.rtdm.dao.StagedFileDAO;
import com.liaison.mailbox.rtdm.dao.StagedFileDAOBase;
import com.liaison.mailbox.rtdm.model.StagedFile;
import com.liaison.mailbox.service.core.hazelcast.HazelcastProvider;
import com.liaison.mailbox.service.dto.GlassMessageDTO;
import com.liaison.mailbox.service.glass.util.MailboxGlassMessageUtil;
import com.liaison.mailbox.service.queue.kafka.KafkaMessage;
import com.liaison.mailbox.service.queue.kafka.Producer;
import com.liaison.mailbox.service.util.MailBoxUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static com.liaison.mailbox.service.queue.kafka.KafkaMessageService.KafkaMessageType.FILE_DELETE;
import static com.liaison.mailbox.service.util.MailBoxUtil.DATACENTER_NAME;

/**
 * class which has file delete replication activities
 *
 * @author OFS
 *
 */
public class FileDeleteReplicationService {

    private static final Logger LOGGER = LogManager.getLogger(FileDeleteReplicationService.class);

    /**
     * Method to in-activate staged file entry and update lens status.
     *
     * @param message
     */
    public void inactivateStageFileAndUpdateLens(KafkaMessage message) {

        try {

            //Just delete the file if it comes from other datacenter
            if (StringUtils.isNotBlank(message.getDatacenter())) {
                deleteReplicatedFile(message);
                return;
            }

            String path = message.getFileDeleteMessage();
            String filePath = path.substring(0, path.lastIndexOf("/"));
            String fileName = path.substring(path.lastIndexOf("/") + 1);

            StagedFileDAO stagedFileDAO = new StagedFileDAOBase();

            //We do not set the DELETED status in StagedFile and the idea is to get the entities irrespective of the FILE STAGE STATUS
            StagedFile stagedFile = stagedFileDAO.findStagedFilesByFileNameAndPath(
                    filePath,
                    fileName,
                    EntityStatus.INACTIVE.value(),
                    Arrays.asList(ProcessorType.FILEWRITER.getCode(), ProcessorType.REMOTEUPLOADER.getCode()));
            if (null != stagedFile) {
                ThreadContext.clearMap();
                ThreadContext.put(LogTags.GLOBAL_PROCESS_ID, stagedFile.getGPID());

                //handle filewriter logic
                if (ProcessorType.FILEWRITER.getCode().equals(stagedFile.getProcessorType())) {
                    handleFileWriter(message, filePath, fileName, stagedFileDAO, stagedFile);
                } else { //handle remote uploader logic
                    handleRemoteUploader(message, stagedFileDAO, stagedFile);
                }
            } else {
                LOGGER.warn("Invalid file path {} and filename {}", filePath, fileName);
            }

        } finally {
            ThreadContext.clearMap();
        }
    }

    private void handleFileWriter(KafkaMessage message, String filePath, String fileName, StagedFileDAO stagedFileDAO, StagedFile stagedFile) {

        if (EntityStatus.INACTIVE.value().equals(stagedFile.getStagedFileStatus())) {
            // Ignore if it is already in-active state
            LOGGER.info("File " + fileName + " in location " + filePath + " is might be deleted by WatchDogService");
            return;
        }

        stagedFile.setStagedFileStatus(EntityStatus.INACTIVE.value());
        stagedFile.setModifiedDate(MailBoxUtil.getTimestamp());
        stagedFileDAO.merge(stagedFile);

        GlassMessageDTO glassMessageDTO = new GlassMessageDTO();
        glassMessageDTO.setGlobalProcessId(stagedFile.getGPID());
        //Post disable for all the filewriter

        //Get the processor details to update the lens status with proper category
        ProcessorConfigurationDAO processorConfigurationDAO = new ProcessorConfigurationDAOBase();
        Processor processor = processorConfigurationDAO.find(Processor.class, stagedFile.getProcessorId());

        glassMessageDTO.setProcessorType(ProcessorType.findByName(stagedFile.getProcessorType()), MailBoxUtil.getCategory(processor.getProcsrProperties()));
        glassMessageDTO.setProcessProtocol(MailBoxUtil.getProtocolFromFilePath(filePath));
        glassMessageDTO.setFileName(fileName);
        glassMessageDTO.setFilePath(filePath);
        glassMessageDTO.setFileLength(0);
        glassMessageDTO.setStatus(ExecutionState.COMPLETED);
        glassMessageDTO.setMessage("File is picked/deleted by the customer");
        glassMessageDTO.setPipelineId(null);
        glassMessageDTO.setFirstCornerTimeStamp(null);

        MailboxGlassMessageUtil.logGlassMessage(glassMessageDTO);
        LOGGER.info("Updated LENS status for the file " + stagedFile.getFileName() + " and location is " + stagedFile.getFilePath());

        //Post the deleted message to other datacenter
        Producer.produce(FILE_DELETE, message.getFileDeleteMessage(), DATACENTER_NAME, stagedFile.getGPID());
    }

    private void handleRemoteUploader(KafkaMessage message, StagedFileDAO stagedFileDAO, StagedFile stagedFile) {

        if (EntityStatus.INACTIVE.value().equals(stagedFile.getStagedFileStatus())) {
            //Post the deleted message to other datacenter
            if (MailBoxUtil.DATACENTER_NAME.equals(stagedFile.getProcessDc())) {
                Producer.produce(FILE_DELETE, message.getFileDeleteMessage(), DATACENTER_NAME, stagedFile.getGPID());
                LOGGER.info("File {} may be deleted by WatchDogService", message.getFileDeleteMessage());
            }
        } else {

            stagedFile.setStagedFileStatus(EntityStatus.INACTIVE.value());
            stagedFile.setModifiedDate(MailBoxUtil.getTimestamp());
            stagedFileDAO.merge(stagedFile);
            Producer.produce(FILE_DELETE, message.getFileDeleteMessage(), DATACENTER_NAME, stagedFile.getGPID());
        }

    }

    /**
     * Method to delete file 
     *
     * @param message
     */
    private void deleteReplicatedFile(KafkaMessage message) {

        String path = message.getFileDeleteMessage();
        String filePath = path.substring(0, path.lastIndexOf("/"));
        String fileName = path.substring(path.lastIndexOf("/") + 1);
        Path file = Paths.get(filePath + File.separatorChar + fileName);
        if (Files.exists(file)) {
            try {
                Files.delete(file);
                HazelcastProvider.put(message.getGpid(), message.getFileDeleteMessage());
                LOGGER.warn("File " + fileName + " is deleted in the filePath " + filePath);
            } catch (IOException e) {
                LOGGER.error("Unable to delete file " + fileName + " in the filePath " + filePath);
            }
        } else {
            HazelcastProvider.put(message.getGpid(), message.getFileDeleteMessage());
        }

    }

}
