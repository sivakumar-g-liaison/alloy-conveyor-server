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

import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.rtdm.dao.UploadedFileDAO;
import com.liaison.mailbox.rtdm.model.UploadedFile;
import com.liaison.mailbox.service.dto.dropbox.UploadedFileDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;

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
     */
    public void addUploadedFile(UploadedFileDTO dto) {
        
        LOG.debug("Enter into addUploadedFile ()");
        
        try {
            
            UploadedFile uploadedFile = new UploadedFile();
            uploadedFile.copyFromDto(dto, true);
            
            UploadedFileDAO dao = new UploadedFileDAOBase();
            dao.persist(uploadedFile);          
            
            LOG.debug("Exit from addUploadedFile ()");
            
        } catch (Exception e) {
            LOG.error(e);            
            throw new RuntimeException("Failed to add the uploaded file");            
        }        
    }
    
    /**
     * Method delete Uploaded file.
     * 
     * @param dto
     */
    public void deleteUploadedFile(String guid) {
        
        LOG.debug("Enter into deleteUploadedFile ()");
        
        try {
            
            UploadedFileDAO dao = new UploadedFileDAOBase();
            
            UploadedFile uploadedFile = dao.find(UploadedFile.class, guid);
            
            if (uploadedFile == null) {
                throw new MailBoxConfigurationServicesException(Messages.UPLOADED_FILE_DOES_NOT_EXIST, guid,
                        Response.Status.BAD_REQUEST);
            }           
            
            dao.remove(uploadedFile);
            
            LOG.debug("Exit from deleteUploadedFile ()");
            
        } catch (Exception e) {
            LOG.error(e);            
            throw new RuntimeException("Failed to delete the uploaded file");            
        }        
    }

}
