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

import com.google.gson.Gson;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.rtdm.dao.UploadedFileDAO;
import com.liaison.mailbox.rtdm.model.UploadedFile;
import com.liaison.mailbox.service.dto.dropbox.UploadedFileDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.spectrum.client.model.table.DataTableRow;

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
    public void addUploadedFile(UploadedFileDTO dto, boolean isCreate) {
        
        LOG.debug("Enter into addUploadedFile ()");
        
        try {
            
            UploadedFile uploadedFile = new UploadedFile();
            uploadedFile.copyFromDto(dto, isCreate);
            
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
    
    /**
     * Data Migration for Uploaded files.
     * 
     * @param tableRows
     */
    public void dataMigration(DataTableRow[] tableRows) {
        
        LOG.debug("Enter into dataMigration ()");
        
        UploadedFileDTO fileDTO;
        try {
            
            for (DataTableRow row : tableRows) {
                
                fileDTO = new Gson().fromJson(new Gson().toJson(row.getColumns()), UploadedFileDTO.class);
                fileDTO.setTtl(String.valueOf(row.getTtl()));
                addUploadedFile(fileDTO, false);                
            }
            
            LOG.info("Data Migration has done for upload files successfully");
            
        } catch (Exception e) {
            LOG.error(e);            
            throw new RuntimeException("Failed data migration for uploaded files");            
        }  
        
        LOG.debug("Exit from dataMigration ()");        
    }

}
