/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.dropbox;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.mailbox.rtdm.dao.UploadedFileDAO;
import com.liaison.mailbox.rtdm.model.UploadedFile;
import com.liaison.mailbox.service.dto.dropbox.UploadedFileDTO;

/**
 * Class which has Dropbox uploaded file related operations.
 *
 * @author OFS
 */
public class DropboxUploadedFileService extends DropboxBaseService {

    private static final Logger LOG = LogManager.getLogger(DropboxUploadedFileService.class);
    
    /**
     * Method add Uploaded file details.
     * 
     * @param dto
     * @return Response
     */
    public void addUploadedFile(UploadedFileDTO dto) {
        
        LOG.debug("Enter into addUploadedFile ()");
        try {
            
            UploadedFile uploadedFile= new UploadedFile();
            uploadedFile.copyFromDto(dto, true);
            
            UploadedFileDAO dao = new UploadedFileDAOBase();
            dao.persist(uploadedFile);          
            
            LOG.debug("Exit from addUploadedFile ()");
            
        } catch (Exception e) {
            LOG.error(e);            
            throw new RuntimeException("Failed to add the uploaded file");            
        }        
    }

}
