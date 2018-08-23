/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.rtdm.dao;

import com.liaison.commons.jpa.GenericDAO;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.rtdm.model.InboundFile;

import java.util.List;

/**
 * The dao class for the INBOUND_FILE database table.
 *
 * @author OFS
 */

public interface InboundFileDAO extends GenericDAO<InboundFile> {
    
    String FILE_PATH = "filePath";
    String FILE_NAME = "fileName";
    String STATUS = "status";
    String GUID = "guid";
    String PROCESSOR_GUID = "processorGuid";
    String TRIGGER_FILE_GUID = "triggerFileGuid";
    String PARENT_GUID = "parentGuid";
    String PROCESS_DC = "processDC";
    String GET_INPROGRESS_TRIGGER_FILE = "InboundFile.findTriggerFile";
    String LIKE_OPR = "%";
    String MODIFIED_BY = "modified_by";
    String MODIFIED_DATE = "modified_date";
    
    /**
     * Returns list of inbound_file rows
     * 
     * @param filePath
     * @param processorGuid
     * @return inbound_file
     */
    List<InboundFile> findInboundFiles(String filePath, String processorGuid);
    
    /**
     * Returns list of inbound_file rows 
     * 
     * @param filePath
     * @param processorGuid
     * @return inbound_file
     */
    List<InboundFile> findInboundFilesByRecurse(String filePath, String processorGuid);
    
    /**
     * Returns list of inbound_file rows for conditional sweeper
     * 
     * @param filePath
     * @param processorGuid
     * @param triggerFileName
     * @return
     */
    List<InboundFile> findInboundFilesForConditionalSweeper(String filePath, String processorGuid, String triggerFileName);
    
    /**
     * Returns list of inbound_file rows for conditional sweeper
     * 
     * @param filePath
     * @param processorGuid
     * @param triggerFileName
     * @param triggerFileGuid
     * @return inbound_file list
     */
    List<InboundFile> findInboundFilesForConditionalSweeperByRecurse(String filePath, String processorGuid, String triggerFileName, String triggerFileGuid);

    /**
     * Returns list of inbound_file rows for conditional sweeper inprogress trigger file
     * @param filePath
     * @param parentGuid
     * @return inbound_file list
     */
    List<InboundFile> findInboundFilesForInprogressTriggerFile(String filePath, String parentGuid);

    /**
     * Update the InboundFile Status by processorId
     * 
     * @param guid
     * @param status
     * @param modifiedBy
     */
    void updateInboundFileStatusByGuid(String guid, String status, String modifiedBy);
    
    /**
     * To get in-progress trigger file name
     * @param payloadLocation
     * @param processorGuid
     * @return inbound_file
     */
    InboundFile findInprogressTriggerFile(String payloadLocation, String processorGuid);

    /**
     * To get Inbound_file for trigger file
     * @param payloadLocation
     * @param triggerFileName
     * @param datacenter
     * @param processorGuid
     * @return inbound_file
     */
    InboundFile findInboundFileForTriggerFile(String payloadLocation, String triggerFileName, String datacenter, String processorGuid);
    
    /**
     * To get Inbound_file 
     * @param payloadLocation
     * @param fileName
     * @param datacenter
     * @return inbound_file
     */
    InboundFile findInboundFile(String payloadLocation, String fileName, String datacenter);
    
    /**
     * Update parent guid for conditional sweeper files
     * 
     * @param guids
     * @param parentGuid
     */
    void updateParentGuidForConditionalSweeper(List<String> guids, String parentGuid);

    void updateInboundFileProcessDCByProcessorGUid(List<String> processorGuids, String newProcessDc);
    
    String FIND_INBOUND_FILES_BY_FILE_PATH = "SELECT inbound_file FROM InboundFile inbound_file" +
            " WHERE inbound_file.filePath =:" +
            FILE_PATH +
            " AND inbound_file.processDc =:" +
            MailBoxConstants.PROCESS_DC +
            " AND inbound_file.processorId =:" +
            PROCESSOR_GUID +
            " AND inbound_file.status =:" +
            STATUS;
    
    String FIND_INBOUND_FILES_BY_FILE_PATH_RECURSE = "SELECT inbound_file FROM InboundFile inbound_file" +
            " WHERE inbound_file.filePath LIKE :" +
            FILE_PATH +
            " AND inbound_file.processDc =:" +
            MailBoxConstants.PROCESS_DC +
            " AND inbound_file.processorId =:" +
            PROCESSOR_GUID +
            " AND inbound_file.status =:" +
            STATUS;

    String FIND_INBOUND_FILES_FOR_CONDITIONAL_SWEEPER_BY_FILE_PATH = "SELECT inbound_file FROM InboundFile inbound_file" +
            " WHERE inbound_file.filePath =:" +
            FILE_PATH +
            " AND inbound_file.processDc =:" +
            MailBoxConstants.PROCESS_DC +
            " AND inbound_file.processorId =:" +
            PROCESSOR_GUID +
            " AND inbound_file.fileName <>:" +
            FILE_NAME +
            " AND inbound_file.status =:" +
            STATUS;

    String FIND_INBOUND_FILES_FOR_CONDITIONAL_SWEEPER_BY_FILE_PATH_RECURSE = "SELECT inbound_file FROM InboundFile inbound_file" +
            " WHERE inbound_file.filePath LIKE :" +
            FILE_PATH +
            " AND inbound_file.processDc =:" +
            MailBoxConstants.PROCESS_DC +
            " AND inbound_file.processorId =:" +
            PROCESSOR_GUID +
            " AND inbound_file.fileName <>:" +
            FILE_NAME +
            " AND inbound_file.pguid <>:" +
            TRIGGER_FILE_GUID +
            " AND inbound_file.status =:" +
            STATUS;

    String UPDATE_INBOUND_FILE_STATUS_BY_GUID = "UPDATE INBOUND_FILE" +
            " SET STATUS =:" +
            STATUS +
            ", MODIFIED_BY =:" +
            MODIFIED_BY +
            ", MODIFIED_DATE =:" +
            MODIFIED_DATE +
            " WHERE PGUID =:" +
            GUID;

    String FIND_INBOUND_FILE_BY_TRIGGER_FILE = "SELECT inbound_file FROM InboundFile inbound_file" +
            " WHERE inbound_file.filePath =:" +
            FILE_PATH +
            " AND inbound_file.fileName =:" +
            FILE_NAME +
            " AND inbound_file.processorId =:" +
            PROCESSOR_GUID +
            " AND inbound_file.processDc =:" +
            MailBoxConstants.PROCESS_DC +
            " AND inbound_file.status =:" +
            STATUS;

    String FIND_INBOUND_FILE = "SELECT inbound_file FROM InboundFile inbound_file" +
            " WHERE inbound_file.filePath =:" +
            FILE_PATH +
            " AND inbound_file.fileName =:" +
            FILE_NAME +
            " AND inbound_file.processDc =:" +
            MailBoxConstants.PROCESS_DC +
            " AND inbound_file.status =:" +
            STATUS;
    
    String FIND_INBOUND_FILES_BY_INPROGRESS_TRIGGER_FILE = "SELECT inbound_file FROM InboundFile inbound_file" +
            " WHERE inbound_file.parentGlobalProcessId =:" +
            PARENT_GUID +
            " AND inbound_file.processDc =:" +
            MailBoxConstants.PROCESS_DC;

    String UPDATE_PARENT_GUID_FOR_CONDITIONAL_SWEEPER_FILES = "UPDATE INBOUND_FILE" +
            " SET PARENT_GLOBAL_PROCESS_GUID =:" + PARENT_GUID +
            " WHERE PGUID IN (:" + GUID + ")";

    String UPDATE_PROCESS_DC_BY_PROCESSOR_GUID = "UPDATE INBOUND_FILE" +
            " SET PROCESS_DC =:" + PROCESS_DC +
            " WHERE STATUS !=:" + STATUS +
            " AND PROCESSOR_ID IN (:" + GUID + ")";
}
