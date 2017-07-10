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

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.mailbox.MailBoxConstants;
import com.google.gson.Gson;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.rtdm.dao.UploadedFileDAO;
import com.liaison.mailbox.rtdm.model.UploadedFile;
import com.liaison.mailbox.service.dto.GenericSearchFilterDTO;
import com.liaison.mailbox.service.dto.ResponseDTO;
import com.liaison.mailbox.service.dto.dropbox.UploadedFileDTO;
import com.liaison.mailbox.service.dto.dropbox.response.GetUploadedFilesResponseDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.util.MailBoxUtil;
import com.liaison.spectrum.client.model.table.DataTableRow;


/**
 * Class which has Dropbox uploaded file related operations.
 *
 * @author OFS
 */
public class DropboxUploadedFileService extends DropboxBaseService {

    private static final Logger LOGGER = LogManager.getLogger(DropboxUploadedFileService.class);
	private static final String UPLOADED_FILES = "uploaded Files";
    
    /**
     * Method add Uploaded file details.
     * 
     * @param dto
     */
    public void addUploadedFile(UploadedFileDTO dto, boolean isCreate) {
        
        LOGGER.debug("Enter into addUploadedFile ()");
        
        try {
            
            UploadedFile uploadedFile = new UploadedFile();
            uploadedFile.copyFromDto(dto, isCreate);
            
            UploadedFileDAO dao = new UploadedFileDAOBase();
            dao.persist(uploadedFile);          
            
            LOGGER.debug("Exit from addUploadedFile ()");
            
        } catch (Exception e) {
            LOGGER.error(e);            
            throw new RuntimeException("Failed to add the uploaded file");            
        }        
    }
    
    /**
     * Method get Uploaded file details.
     * 
     * @param dto
     * @throws ParseException 
     */
	public GetUploadedFilesResponseDTO getuploadedFiles(GenericSearchFilterDTO searchFilter, String loginId)
			throws IOException, JAXBException, ParseException {
		
		LOGGER.debug("Entering into get uploaded files service.");
		
		GetUploadedFilesResponseDTO serviceResponse = new GetUploadedFilesResponseDTO();
		
		int totalCount = 0;
		Map<String, Integer> pageOffsetDetails = null;
		List<UploadedFileDTO> uploadedFileDTOs = new ArrayList<UploadedFileDTO>();
		
		UploadedFileDAO uploadedFileDAO = new UploadedFileDAOBase();
		
        if (MailBoxUtil.isEmpty(loginId)) {
            throw new RuntimeException("Failed to get upload history: loginId null or empty");
        }
				
		totalCount = uploadedFileDAO.getUploadedFilesCountByUserId(loginId, searchFilter.getUploadedFileName());
		
        pageOffsetDetails = MailBoxUtil.getPagingOffsetDetails(
                searchFilter.getPage(),
                searchFilter.getPageSize(),
                totalCount);
        
        //Invalid Page validation
        if (pageOffsetDetails.get(MailBoxConstants.PAGING_COUNT) <= 0
                && !MailBoxUtil.isEmpty(searchFilter.getPage())
                && pageOffsetDetails.get(MailBoxConstants.PAGE_VALUE) != 1) {
            throw new MailBoxServicesException("Invalid Page Number", Response.Status.BAD_REQUEST);
        }
        
        serviceResponse.setTotalItems(totalCount);
        
        List<UploadedFile> uploadedFiles = uploadedFileDAO.findUploadedFiles(loginId, searchFilter, pageOffsetDetails);
        
        UploadedFileDTO uploadedFileDTO;
        for (UploadedFile uploadedFile : uploadedFiles) {
        	
        	uploadedFileDTO = new UploadedFileDTO();
        	uploadedFileDTO.copyFromEntity(uploadedFile);
        	uploadedFileDTOs.add(uploadedFileDTO);
        }
        
        serviceResponse.setResponse(new ResponseDTO(Messages.RETRIEVE_SUCCESSFUL, UPLOADED_FILES, Messages.SUCCESS));
        serviceResponse.setUploadedFile(uploadedFileDTOs);
        
		LOGGER.debug("Exit from get uploaded files service.");

		return serviceResponse;
		
	}
    
    /**
     * Method delete Uploaded file.
     * 
     * @param dto
     */
    public void deleteUploadedFile(String guid) {
        
        LOGGER.debug("Enter into deleteUploadedFile ()");
        
        try {
            
            UploadedFileDAO dao = new UploadedFileDAOBase();
            
            UploadedFile uploadedFile = dao.find(UploadedFile.class, guid);
            
            if (uploadedFile == null) {
                throw new MailBoxConfigurationServicesException(Messages.UPLOADED_FILE_DOES_NOT_EXIST, guid,
                        Response.Status.BAD_REQUEST);
            }           
            
            dao.remove(uploadedFile);
            
            LOGGER.debug("Exit from deleteUploadedFile ()");
            
        } catch (Exception e) {
            LOGGER.error(e);            
            throw new RuntimeException("Failed to delete the uploaded file");            
        }        
    }
    
    /**
     * Data Migration for Uploaded files.
     * 
     * @param tableRows
     */
    public void dataMigration(DataTableRow[] tableRows) {
        
        LOGGER.debug("Enter into dataMigration ()");
        
        UploadedFileDTO fileDTO;
        try {
            
            for (DataTableRow row : tableRows) {
                
                fileDTO = new Gson().fromJson(new Gson().toJson(row.getColumns()), UploadedFileDTO.class);
                fileDTO.setTtl(String.valueOf(row.getTtl()));
                addUploadedFile(fileDTO, false);                
            }
            
            LOGGER.info("Data Migration has done for upload files successfully");
            
        } catch (Exception e) {
            LOGGER.error(e);            
            throw new RuntimeException("Failed data migration for uploaded files");            
        }  
        
        LOGGER.debug("Exit from dataMigration ()");        
    }

}
