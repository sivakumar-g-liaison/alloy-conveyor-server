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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.liaison.mailbox.enums.EntityStatus;
import com.liaison.mailbox.enums.ExecutionState;
import com.liaison.mailbox.enums.ProcessorType;
import com.liaison.mailbox.rtdm.dao.StagedFileDAO;
import com.liaison.mailbox.rtdm.dao.StagedFileDAOBase;
import com.liaison.mailbox.rtdm.model.StagedFile;
import com.liaison.mailbox.service.dto.GlassMessageDTO;
import com.liaison.mailbox.service.glass.util.MailboxGlassMessageUtil;
import com.liaison.mailbox.service.util.MailBoxUtil;

/**
 * class which has file delete replication activities
 * 
 * @author OFS
 *
 */
public class FileDeleteReplicationService {

    private static final Logger LOGGER = LogManager.getLogger(FileDeleteReplicationService.class);
    private static final String logPrefix = "FileDeleteReplication ";
    private static final String seperator = " :";
    
    private String uniqueId;
    
    private String constructMessage(String... messages) {
        
        StringBuilder msgBuf = new StringBuilder()
            .append(logPrefix)
            .append(seperator)
            .append(uniqueId)
            .append(seperator);
        for (String str : messages) {
            msgBuf.append(str).append(seperator);
        }
        return msgBuf.toString();
    }
    
    public FileDeleteReplicationService() {
        uniqueId = MailBoxUtil.getGUID();
    }
    
    /**
     * Method to in-activate staged file entry and update lens status.
     * 
     * @param requestString
     */
    public void inactivateStageFileAndUpldateLens(String requestString) {
        
        try {
            JSONObject innerObj = new JSONObject(requestString);
            String path = (String) innerObj.get("path");
            String filePath = path.substring(0, path.lastIndexOf("/"));
            String fileName = path.substring(path.lastIndexOf("/") + 1);
            
            StagedFileDAO stagedFileDAO = new StagedFileDAOBase();
            StagedFile deletedStagedFile= stagedFileDAO.findStagedFilesForFileWriterByFileNameAndPath(filePath, fileName);
            
            if (null != deletedStagedFile) {
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
                glassMessageDTO.setMessage("File is deleted by the customer");
                glassMessageDTO.setPipelineId(null);
                glassMessageDTO.setFirstCornerTimeStamp(null);
                
                MailboxGlassMessageUtil.logGlassMessage(glassMessageDTO);
                LOGGER.info(constructMessage("{} : Updated LENS status for the file {} and location is {}"), deletedStagedFile.getProcessorId(), deletedStagedFile.getFileName(), deletedStagedFile.getFilePath());
            } else {
                LOGGER.info(constructMessage("File {} in location {} is might be deleted by WatchDogService"), fileName, filePath);
            }
            
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
    
}
