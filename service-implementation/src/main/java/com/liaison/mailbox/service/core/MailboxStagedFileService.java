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

import com.liaison.commons.jpa.DAOUtil;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.enums.EntityStatus;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.rtdm.dao.MailboxRTDMDAO;
import com.liaison.mailbox.rtdm.dao.StagedFileDAO;
import com.liaison.mailbox.rtdm.dao.StagedFileDAOBase;
import com.liaison.mailbox.rtdm.model.StagedFile;
import com.liaison.mailbox.service.dto.ResponseDTO;
import com.liaison.mailbox.service.dto.configuration.request.ReviseStagedFileRequestDTO;
import com.liaison.mailbox.service.dto.dropbox.StagedFileDTO;
import com.liaison.mailbox.service.dto.dropbox.response.GetStagedFilesResponseDTO;
import com.liaison.mailbox.service.util.MailBoxUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

/**
 * Class used to retrieve the staged file details in relay.
 * 
 * @author OFS
 */
public class MailboxStagedFileService extends GridServiceRTDM<StagedFile> {

    private static final Logger LOG = LogManager.getLogger(MailboxStagedFileService.class);
    private static final String STAGED_FILE = "Staged files";
    private static final String STAGED_FILE_MESSAGE = "Staged file must be in staged status to inactivate the file";
    private static final String STAGED_FILE_NOT_EXISTS = "Staged file does not exists in the system";
    private static final String STAGED_FILE_UPDATE_MESSAGE = "Staged files updated successfully";

    /**
     * Method to list the staged files
     * 
     * @param page
     * @param pageSize
     * @param sortInfo
     * @param filterText
     * @return serviceResponse
    */
    public GetStagedFilesResponseDTO getStagedFiles(String page, String pageSize, String sortInfo, String filterText) {

        LOG.debug("Entering into get all StagedFiles.");
        GetStagedFilesResponseDTO serviceResponse = new GetStagedFilesResponseDTO();

        try {

            GridResult<StagedFile> result = getGridItems(StagedFile.class, filterText, sortInfo,
                    page, pageSize);
            List<StagedFile> stagedFiles = result.getResultList();
            List<StagedFileDTO> stagedFileDTO = new ArrayList<StagedFileDTO>();

            if (null == stagedFiles || stagedFiles.isEmpty()) {
                serviceResponse.setResponse(new ResponseDTO(Messages.NO_COMPONENT_EXISTS, STAGED_FILE, Messages.SUCCESS));
                serviceResponse.setStagedFiles(stagedFileDTO);
                return serviceResponse;
            }

            StagedFileDTO stagedFile = null;
            for (StagedFile file : stagedFiles) {
                stagedFile = new StagedFileDTO();
                stagedFile.copyFromEntity(file);
                stagedFileDTO.add(stagedFile);
            }

            // response message construction
            serviceResponse.setResponse(new ResponseDTO(Messages.READ_SUCCESSFUL, STAGED_FILE, Messages.SUCCESS));
            serviceResponse.setStagedFiles(stagedFileDTO);
            serviceResponse.setTotalItems((int) result.getTotalItems());

            LOG.debug("Exiting from get all StagedFiles.");
            return serviceResponse;
        } catch (Exception e) {

            LOG.error(Messages.READ_OPERATION_FAILED.name(), e);
            serviceResponse.setResponse(new ResponseDTO(Messages.READ_OPERATION_FAILED, STAGED_FILE, Messages.FAILURE,
                    e.getMessage()));
            return serviceResponse;
        }
    }

    /**
     * Method to deactivate the staged file
     * 
     * @param pguid
     * @return response
    */
    public Response deactivateStagedFile(String pguid) {
	
        LOG.debug("Entering into deactivate StagedFiles.");
        EntityManager em = null;
        StagedFileDAO dao = new StagedFileDAOBase();
        StagedFile file = null;

        try {

            em = DAOUtil.getEntityManager(MailboxRTDMDAO.PERSISTENCE_UNIT_NAME);
            if (!MailBoxUtil.isEmpty(pguid)) {
	
                file = em.find(StagedFile.class, pguid);
                if (null == file) {
                    throw new RuntimeException(STAGED_FILE_NOT_EXISTS);
                }

                if (EntityStatus.STAGED.value().equals(file.getStagedFileStatus())) {
	
                    file.setStagedFileStatus(EntityStatus.FAILED.value());
                    file.setModifiedDate(MailBoxUtil.getTimestamp());

                    // updating the file with inactive status
                    dao.merge(file);
                } else {
                    throw new RuntimeException(STAGED_FILE_MESSAGE);
                }
            } else {
                throw new RuntimeException(Messages.INVALID_REQUEST.value());
            }
        } catch (Exception e) {
            LOG.error(Messages.REVISE_OPERATION_FAILED.name(), e);
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            if (null != em) {
                em.close();
            }
        }
        return Response.ok().entity(file.getPguid()).build();
    }

    /**
     *  Method to deactivate the bulk staged files
     * 
     * @param requestDTO
     * @return response
     */
    public Response deactivateBulkStagedFiles(ReviseStagedFileRequestDTO requestDTO) {

        LOG.debug("Entering into deactivate bulk StagedFiles.");
        EntityManager em = null;
        EntityTransaction tx = null;

        try {

            List<String> guids = requestDTO.getGuids();
            if (MailBoxUtil.isEmptyList(guids)) {
                throw new RuntimeException(Messages.INVALID_REQUEST.value());
            }

            em = DAOUtil.getEntityManager(MailboxRTDMDAO.PERSISTENCE_UNIT_NAME);
            tx = em.getTransaction();
            tx.begin();

            String query = "UPDATE STAGED_FILE SET STATUS = 'FAILED', MODIFIED_DATE = :" + StagedFileDAO.MODIFIED_DATE +
                    " WHERE PGUID IN (:" + StagedFileDAO.STAGED_FILE_IDS + ")" +
                    " AND STATUS = 'STAGED'";

            Query q = em.createNativeQuery(query)
                    .setParameter(StagedFileDAO.MODIFIED_DATE, MailBoxUtil.getTimestamp())
                    .setParameter(StagedFileDAO.STAGED_FILE_IDS, guids);

            //Update the selected files
            q.executeUpdate();
            tx.commit();
        } catch (Exception e) {

            if (null != tx && tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException(e);
        } finally {
            if (null != em) {
                em.close();
            }
        }
        return Response.ok().entity(STAGED_FILE_UPDATE_MESSAGE).build();
     }
}
