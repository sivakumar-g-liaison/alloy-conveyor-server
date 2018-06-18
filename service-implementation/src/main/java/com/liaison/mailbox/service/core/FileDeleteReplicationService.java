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
import com.liaison.mailbox.enums.EntityStatus;
import com.liaison.mailbox.enums.ExecutionState;
import com.liaison.mailbox.enums.ProcessorType;
import com.liaison.mailbox.rtdm.dao.StagedFileDAO;
import com.liaison.mailbox.rtdm.dao.StagedFileDAOBase;
import com.liaison.mailbox.rtdm.model.StagedFile;
import com.liaison.mailbox.service.dto.GlassMessageDTO;
import com.liaison.mailbox.service.glass.util.MailboxGlassMessageUtil;
import com.liaison.mailbox.service.queue.kafka.KafkaMessage;
import com.liaison.mailbox.service.queue.kafka.KafkaMessageService;
import com.liaison.mailbox.service.queue.kafka.Producer;
import com.liaison.mailbox.service.util.MailBoxUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

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
    private static final String PATH = "object_path";
    private static final String USER_SID = "user_sid";

    /**
     * Method to in-activate staged file entry and update lens status.
     *
     * @param message
     */
    public void inactivateStageFileAndUpdateLens(KafkaMessage message) {

        try {

            //Just delete the file if it comes from other datacenter
            if (StringUtils.isNotBlank(message.getDatacenter())) {
                deleteReplicatedFile(message.getFileDeleteMessage());
                return;
            }

            String path = message.getFileDeleteMessage();
            String filePath = path.substring(0, path.lastIndexOf("/"));
            String fileName = path.substring(path.lastIndexOf("/") + 1);

            StagedFileDAO stagedFileDAO = new StagedFileDAOBase();
            StagedFile deletedStagedFile = stagedFileDAO.findStagedFilesByFileNameAndPath(filePath, fileName, Arrays.asList(ProcessorType.FILEWRITER.getCode(), ProcessorType.REMOTEUPLOADER.getCode()));
            if (null != deletedStagedFile) {
                ThreadContext.clearMap();
                ThreadContext.put(LogTags.GLOBAL_PROCESS_ID, deletedStagedFile.getGPID());

                deletedStagedFile.setStagedFileStatus(EntityStatus.INACTIVE.value());
                deletedStagedFile.setModifiedDate(MailBoxUtil.getTimestamp());
                stagedFileDAO.merge(deletedStagedFile);

                if (ProcessorType.FILEWRITER.getCode().equals(deletedStagedFile.getProcessorType())) {

                    GlassMessageDTO glassMessageDTO = new GlassMessageDTO();
                    glassMessageDTO.setGlobalProcessId(deletedStagedFile.getGPID());
                    glassMessageDTO.setProcessorType(ProcessorType.findByName(deletedStagedFile.getProcessorType()));
                    glassMessageDTO.setProcessProtocol(MailBoxUtil.getProtocolFromFilePath(filePath));
                    glassMessageDTO.setFileName(fileName);
                    glassMessageDTO.setFilePath(filePath);
                    glassMessageDTO.setFileLength(0);
                    glassMessageDTO.setStatus(ExecutionState.COMPLETED);
                    glassMessageDTO.setMessage("File is picked/deleted by the customer");
                    glassMessageDTO.setPipelineId(null);
                    glassMessageDTO.setFirstCornerTimeStamp(null);

                    MailboxGlassMessageUtil.logGlassMessage(glassMessageDTO);
                    LOGGER.info("Updated LENS status for the file " + deletedStagedFile.getFileName() + " and location is " + deletedStagedFile.getFilePath());
                } else {
                    LOGGER.info("The processor type is Remote Uploader and ignoring the glass updates");
                }

                //Post the deleted message to other datacenter
                Producer.produce(FILE_DELETE, message.getFileDeleteMessage(), DATACENTER_NAME);
            } else {
                LOGGER.info("File " + fileName + " in location " + filePath + " is might be deleted by WatchDogService");
            }

        } finally {
            ThreadContext.clearMap();
        }
    }

    /**
     * Method to delete file 
     *
     * @param path
     */
    private void deleteReplicatedFile(String path) {

        String filePath = path.substring(0, path.lastIndexOf("/"));
        String fileName = path.substring(path.lastIndexOf("/") + 1);
        Path file = Paths.get(filePath + File.separatorChar + fileName);
        if (Files.exists(file)) {
            try {
                Files.delete(file);
                LOGGER.warn("File " + fileName + " is deleted in the filePath " + filePath);
            } catch (IOException e) {
                LOGGER.error("Unable to delete file " + fileName + " in the filePath " + filePath);
            }
        }

    }

}
