/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
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
import com.liaison.mailbox.service.util.MailBoxUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * class which has file delete replication activities
 * 
 * @author OFS
 *
 */
public class FileDeleteReplicationService {

    private static final Logger LOGGER = LogManager.getLogger(FileDeleteReplicationService.class);
    private static final String PATH = "path";
    private static final String USER_UID = "user_uid";
    
    /**
     * Method to in-activate staged file entry and update lens status.
     * 
     * @param requestString
     */
    public void inactivateStageFileAndUpdateLens(String requestString) {
        
        try {
            JSONObject reqeustObj = new JSONObject(requestString);
            String path = (String) reqeustObj.get(PATH);
            String uId = (String) reqeustObj.get(USER_UID);
            String filePath = path.substring(0, path.lastIndexOf("/"));
            String fileName = path.substring(path.lastIndexOf("/") + 1);
            
            StagedFileDAO stagedFileDAO = new StagedFileDAOBase();
            StagedFile deletedStagedFile= stagedFileDAO.findStagedFilesForFileWriterByFileNameAndPath(filePath, fileName);
            
            if (null != deletedStagedFile) {
                ThreadContext.clearMap();
                ThreadContext.put(LogTags.GLOBAL_PROCESS_ID, deletedStagedFile.getGPID());
                
                deletedStagedFile.setStagedFileStatus(EntityStatus.INACTIVE.value());
                deletedStagedFile.setModifiedDate(MailBoxUtil.getTimestamp());
                stagedFileDAO.merge(deletedStagedFile);
                
                GlassMessageDTO glassMessageDTO = new GlassMessageDTO();
                glassMessageDTO.setGlobalProcessId(deletedStagedFile.getGPID());
                glassMessageDTO.setProcessorType(ProcessorType.findByName(deletedStagedFile.getProcessorType()));
                glassMessageDTO.setProcessProtocol(MailBoxUtil.getProtocolFromFilePath(filePath));
                glassMessageDTO.setFileName(fileName);
                glassMessageDTO.setFilePath(filePath);
                glassMessageDTO.setFileLength(0);
                glassMessageDTO.setStatus(ExecutionState.COMPLETED);
                glassMessageDTO.setMessage("File is picked/deleted by the customer and the uid is " + uId);
                glassMessageDTO.setPipelineId(null);
                glassMessageDTO.setFirstCornerTimeStamp(null);
                
                MailboxGlassMessageUtil.logGlassMessage(glassMessageDTO);
                LOGGER.info("Updated LENS status for the file " + deletedStagedFile.getFileName() + " and location is " + deletedStagedFile.getFilePath());
            } else {
                LOGGER.info("File " + fileName + " in location " + filePath +" is might be deleted by WatchDogService");
            }
            
        } catch (JSONException e) {
            throw new RuntimeException(e);
        } finally {
            ThreadContext.clearMap();
        }
    }
    
    /**
     * Method to delete file 
     * 
     * @param requestString
     */
    public void deleteReplicatedFile(String requestString) {
        
        try {
            JSONObject reqeustObj = new JSONObject(requestString);
            String path = (String) reqeustObj.get(PATH);
            String filePath = path.substring(0, path.lastIndexOf("/"));
            String fileName = path.substring(path.lastIndexOf("/") + 1);
            
            if (Files.exists(Paths.get(filePath + File.separatorChar + fileName))) {
                try {
                    Files.delete(Paths.get(filePath + File.separatorChar + fileName));
                    LOGGER.warn("File " + fileName +" is deleted in the filePath "+filePath);
                } catch (IOException e) {
                    LOGGER.error("Unable to delete file "+ fileName + " in the filePath " + filePath);
                }
            }
            
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
    
}
