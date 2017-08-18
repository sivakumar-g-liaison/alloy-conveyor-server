/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.dropbox;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
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
     * @param dto uploaded files details
     * @param isCreate boolean to differentiate creation and migration
     */
    public void addUploadedFile(UploadedFileDTO dto, boolean isCreate) {

        LOGGER.debug("Enter into addUploadedFile ()");

        try {

            UploadedFile uploadedFile = new UploadedFile();
            uploadedFile.copyFromDto(dto, isCreate);

            UploadedFileDAO dao = new UploadedFileDAOBase();
            if (isCreate) {
                dao.persist(uploadedFile);
            } else {
                //only for migration stuff
                dao.merge(uploadedFile);
            }
            LOGGER.debug("Exit from addUploadedFile ()");

        } catch (Exception e) {
            throw new RuntimeException("Failed to add the uploaded file - " + dto.getFileName(), e);
        }
    }

    /**
     * lists uploaded files history
     *
     * @param searchFilter search filters
     * @param loginId user id
     * @return uploaded files
     */
    public GetUploadedFilesResponseDTO listUploadedFiles(GenericSearchFilterDTO searchFilter, String loginId) {

        LOGGER.debug("Entering into get uploaded files service.");
        GetUploadedFilesResponseDTO serviceResponse = new GetUploadedFilesResponseDTO();

        int totalCount = 0;
        Map<String, Integer> pageOffsetDetails = null;
        List<UploadedFileDTO> uploadedFileDTOs = new ArrayList<>();

        if (MailBoxUtil.isEmpty(loginId)) {
            throw new RuntimeException("Failed to get upload history: loginId null or empty");
        }

        UploadedFileDAO uploadedFileDAO = new UploadedFileDAOBase();
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

        List<UploadedFile> uploadedFiles = uploadedFileDAO.fetchUploadedFiles(loginId, searchFilter, pageOffsetDetails);

        UploadedFileDTO uploadedFileDTO;
        for (UploadedFile uploadedFile : uploadedFiles) {

            uploadedFileDTO = new UploadedFileDTO();
            uploadedFileDTO.copyFromEntity(uploadedFile);
            uploadedFileDTOs.add(uploadedFileDTO);
        }

        serviceResponse.setResponse(new ResponseDTO(Messages.RETRIEVE_SUCCESSFUL, UPLOADED_FILES, Messages.SUCCESS));
        serviceResponse.setUploadedFiles(uploadedFileDTOs);

        LOGGER.debug("Exit from get uploaded files service.");
        return serviceResponse;

    }

    /**
     * Hard delete the uploaded file from the history
     *
     * @param guid uploaded file guid
     */
    public void deleteUploadedFile(String guid) {

        LOGGER.debug("Enter into deleteUploadedFile ()");

        try {

            UploadedFileDAO dao = new UploadedFileDAOBase();
            UploadedFile uploadedFile = dao.find(UploadedFile.class, guid);
            if (null == uploadedFile) {
                throw new MailBoxConfigurationServicesException(Messages.UPLOADED_FILE_DOES_NOT_EXIST, guid, Response.Status.BAD_REQUEST);
            }

            dao.remove(uploadedFile);
            LOGGER.debug("Exit from deleteUploadedFile ()");

        } catch (Exception e) {
            throw new RuntimeException("Failed to delete the uploaded file", e);
        }
    }
}
