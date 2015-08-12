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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.commons.message.glass.dom.StatusType;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.dtdm.dao.MailBoxConfigurationDAO;
import com.liaison.mailbox.dtdm.dao.MailBoxConfigurationDAOBase;
import com.liaison.mailbox.dtdm.model.MailBox;
import com.liaison.mailbox.enums.EntityStatus;
import com.liaison.mailbox.enums.ExecutionState;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.rtdm.dao.StagedFileDAO;
import com.liaison.mailbox.rtdm.dao.StagedFileDAOBase;
import com.liaison.mailbox.rtdm.model.StagedFile;
import com.liaison.mailbox.service.dto.GenericSearchFilterDTO;
import com.liaison.mailbox.service.dto.ResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.DropBoxUnStagedFileResponseDTO;
import com.liaison.mailbox.service.dto.dropbox.StagedFileDTO;
import com.liaison.mailbox.service.dto.dropbox.request.StagePayloadRequestDTO;
import com.liaison.mailbox.service.dto.dropbox.response.GetStagedFilesResponseDTO;
import com.liaison.mailbox.service.dto.dropbox.response.StagePayloadResponseDTO;
import com.liaison.mailbox.service.dto.dropbox.response.StagedFileResponseDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.util.GlassMessage;
import com.liaison.mailbox.service.util.MailBoxUtil;
import com.liaison.mailbox.service.util.TransactionVisibilityClient;
import com.liaison.mailbox.service.validation.GenericValidator;

/**
 * Class which has Dropbox related operations.
 *
 * @author OFS
 */
public class DropboxStagedFilesService {

	private static final Logger LOG = LogManager.getLogger(DropboxStagedFilesService.class);

	public static final String STAGED_FILES = "Staged Files";
	public static final String STAGED_FILE = "Staged File";

	/**
	 * Method to retrieve all staged files of the mailboxes linked to tenancy keys available in the manifest
	 *
	 * @param request
	 * @param aclManifest
	 * @return list of StagedFiles
	 * @throws IOException
	 * @throws JAXBException
	 */
	public GetStagedFilesResponseDTO getStagedFiles(GenericSearchFilterDTO searchFilter, String aclManifestJson)
			throws IOException, JAXBException {

		LOG.debug("Entering into get staged files service.");

		int totalCount = 0;
		Map<String, Integer> pageOffsetDetails = null;

		GetStagedFilesResponseDTO serviceResponse = new GetStagedFilesResponseDTO();
		List<StagedFileDTO> stagedFileDTOs = new ArrayList<StagedFileDTO>();

		LOG.info("Retrieving tenancy keys from acl-manifest");

		// retrieve the tenancy key from acl manifest
		List<String> tenancyKeys = MailBoxUtil.getTenancyKeyGuids(aclManifestJson);
		if (tenancyKeys.isEmpty()) {
			LOG.error("Retrieval of tenancy key from acl manifest failed");
			throw new MailBoxServicesException(Messages.TENANCY_KEY_RETRIEVAL_FAILED, Response.Status.BAD_REQUEST);
		}

		LOG.debug("The retrieved tenancykey values are {}", tenancyKeys);

		// retrieve corresponding mailboxes of the available tenancyKeys.
		MailBoxConfigurationDAO mailboxDao = new MailBoxConfigurationDAOBase();
		LOG.debug("retrieve all mailboxes linked to tenancykeys {}", tenancyKeys);
		List<String> mailboxIds = mailboxDao.findAllMailboxesLinkedToTenancyKeys(tenancyKeys);

		if (mailboxIds.isEmpty()) {
			LOG.error("There are no mailboxes linked to the tenancyKeys");
			throw new MailBoxServicesException("There are no mailboxes available for tenancykeys",
					Response.Status.NOT_FOUND);
		}

		// retrieve searched staged files of mailboxes.
		StagedFileDAO stagedFileDao = new StagedFileDAOBase();
		totalCount = stagedFileDao.getStagedFilesCountByName(mailboxIds, searchFilter.getStagedFileName(),
				searchFilter.getStatus());
		pageOffsetDetails = MailBoxUtil.getPagingOffsetDetails(searchFilter.getPage(), searchFilter.getPageSize(),
				totalCount);
		serviceResponse.setTotalItems(totalCount);
		List<StagedFile> stagedFiles = stagedFileDao.findStagedFilesOfMailboxes(mailboxIds, searchFilter,
				pageOffsetDetails);

		if (stagedFiles.isEmpty()) {
			LOG.info("There are no staged files available for linked mailboxes");
		}

		for (StagedFile stagedFile : stagedFiles) {

			StagedFileDTO stagedFileDTO = new StagedFileDTO();
			stagedFile.copyToDto(stagedFileDTO, false);
			stagedFileDTOs.add(stagedFileDTO);
		}

		serviceResponse.setResponse(new ResponseDTO(Messages.RETRIEVE_SUCCESSFUL, STAGED_FILES, Messages.SUCCESS));
		serviceResponse.setStagedFiles(stagedFileDTOs);

		LOG.debug("Exit from get staged files service.");

		return serviceResponse;
	}

	public String validateIfFileIdBelongsToAnyOrganisation(String fileId, List<String> tenancyKeys,
			GlassMessage glassMessage) {

		StagedFileDAO dropboxDao = new StagedFileDAOBase();

		StagedFile stagedFile = dropboxDao.find(StagedFile.class, fileId);
		if (stagedFile == null) {
			LOG.error("Staged file id missing.");
			throw new MailBoxConfigurationServicesException(Messages.STAGED_FILEID_DOES_NOT_EXIST, fileId,
					Response.Status.BAD_REQUEST);
		}
		glassMessage.setMeta(stagedFile.getFileMetaData());
		glassMessage.setStagedFileId(fileId);
		glassMessage.setOutSize(Long.parseLong(stagedFile.getFileSize()));

		MailBoxConfigurationDAO mailboxDao = new MailBoxConfigurationDAOBase();
		MailBox mailbox = mailboxDao.find(MailBox.class, stagedFile.getMailboxId());
		if (mailbox == null) {
			LOG.error("Given mailbox id doesn't exists.");
			throw new MailBoxConfigurationServicesException(Messages.MBX_DOES_NOT_EXIST, stagedFile.getMailboxId(),
					Response.Status.BAD_REQUEST);
		}
		glassMessage.setMailboxId(mailbox.getPguid());
		for (String tkey : tenancyKeys) {
			if (mailbox.getTenancyKey().equals(tkey)) {
				glassMessage.setTenancyKey(tkey);
				return stagedFile.getSpectrumUri();
			}
		}

		return null;
	}

	public StagePayloadResponseDTO addStagedFile(StagePayloadRequestDTO request, GlassMessage glassMessage) {

		LOG.debug("Entering into add staged file.");

		TransactionVisibilityClient transactionVisibilityClient = new TransactionVisibilityClient();
		StagePayloadResponseDTO serviceResponse = new StagePayloadResponseDTO();

		try {

			StagedFileDTO stagedFileDTO = request.getStagedFile();
			if (stagedFileDTO == null) {
				LOG.error(MailBoxUtil.constructMessage(null, null, "Invalid request json."));
				throw new MailBoxConfigurationServicesException(Messages.INVALID_REQUEST, Response.Status.BAD_REQUEST);
			}

			// validation starts
            if (MailBoxUtil.isEmpty(stagedFileDTO.getMailboxGuid())) {
                throw new MailBoxServicesException(Messages.MANDATORY_FIELD_MISSING, "Mailbox Id", Response.Status.CONFLICT);
            }

            MailBoxConfigurationDAO mailboxDAO = new MailBoxConfigurationDAOBase();
            MailBox mailbox = mailboxDAO.find(MailBox.class, stagedFileDTO.getMailboxGuid());
            if (mailbox == null || EntityStatus.INACTIVE.name().equals(mailbox.getMbxStatus())) {
                StringBuilder msg = new StringBuilder().append("The given mailbox(")
                        .append(stagedFileDTO.getMailboxGuid())
                        .append(") is inactive or not avaialble in the system");
                throw new MailBoxServicesException(msg.toString(), Response.Status.NOT_FOUND);
            }

            if (MailBoxUtil.isEmpty(stagedFileDTO.getSpectrumUri())) {
                throw new MailBoxServicesException(Messages.MANDATORY_FIELD_MISSING, "Payload URI", Response.Status.CONFLICT);
            }
            // validation ends

			StagedFileDAO dropboxDao = new StagedFileDAOBase();
			StagedFile stagedFile = new StagedFile();
			stagedFile.copyFromDto(stagedFileDTO, true);
			dropboxDao.persist(stagedFile);

			serviceResponse.setResponse(new ResponseDTO(Messages.CREATED_SUCCESSFULLY, STAGED_FILE, Messages.SUCCESS));
			serviceResponse.setStagedFile(new StagedFileResponseDTO(String.valueOf(stagedFile.getPrimaryKey())));

			// successfully staged
			if (null != glassMessage) {
				glassMessage.logProcessingStatus(StatusType.SUCCESS, MailBoxConstants.FILE_STAGED_SUCCESSFULLY);
				glassMessage.logEndTimestamp(MailBoxConstants.DROPBOX_FILE_TRANSFER);
				transactionVisibilityClient.logToGlass(glassMessage);
			}
			LOG.info(MailBoxUtil.constructMessage(null, null, "File {} staged successfully for mailbox {} with stagedFileId {}"),
					stagedFileDTO.getName(), stagedFileDTO.getMailboxGuid(), stagedFile.getPrimaryKey());
			LOG.debug("Exit from add staged file.");

		     // log TVA status
			return serviceResponse;

		} catch (Exception e) {

			LOG.error(MailBoxUtil.constructMessage(null, null, "Stage file failed"), e);

			// glass log in case of failure during file staging
			if (null != glassMessage) {
				glassMessage.logProcessingStatus(StatusType.ERROR, "File Stage Failed :" + e.getMessage());
				glassMessage.setStatus(ExecutionState.FAILED);
				transactionVisibilityClient.logToGlass(glassMessage);
			}

			serviceResponse.setResponse(new ResponseDTO(Messages.CREATE_OPERATION_FAILED, STAGED_FILE,
					Messages.FAILURE, e.getMessage()));
			return serviceResponse;

		}
	}

	public DropBoxUnStagedFileResponseDTO getDroppedStagedFileResponse(String aclManifest, String guid)
			throws IOException, JAXBException {

		LOG.debug("Entering into drop staged files service.");

		LOG.info("Retrieving tenancy keys from acl-manifest");
		// retrieve the tenancy key from acl manifest
		List<String> tenancyKeys = MailBoxUtil.getTenancyKeyGuids(aclManifest);
		if (tenancyKeys.isEmpty()) {
			LOG.error("Retrieval of tenancy key from acl manifest failed");
			throw new MailBoxServicesException(Messages.TENANCY_KEY_RETRIEVAL_FAILED, Response.Status.BAD_REQUEST);
		}

		LOG.debug("The retrieved tenancykey values are {}", tenancyKeys);

		// retrieve corresponding mailboxes of the available tenancyKeys.
		MailBoxConfigurationDAO mailboxDao = new MailBoxConfigurationDAOBase();
		LOG.debug("retrieve all mailboxes linked to tenancykeys {}", tenancyKeys);
		List<String> mailboxIds = mailboxDao.findAllMailboxesLinkedToTenancyKeys(tenancyKeys);

		if (mailboxIds.isEmpty()) {
			LOG.error("There are no mailboxes linked to the tenancyKeys");
			throw new MailBoxServicesException("There are no mailboxes available for tenancykeys",
					Response.Status.NOT_FOUND);
		}

		DropBoxUnStagedFileResponseDTO dropBoxUnStagedResponse = new DropBoxUnStagedFileResponseDTO();
		StagedFileDAO stagedFileDAO = new StagedFileDAOBase();
		// validation the ResponseDTO
		GenericValidator validator = new GenericValidator();
		validator.validate(dropBoxUnStagedResponse);

		// Find the staged file based on given GUID and mailboxIds
		List<StagedFile> stagedFiles = stagedFileDAO.findStagedFilesOfMailboxesBasedonGUID(mailboxIds, guid);

		if (stagedFiles.isEmpty()) {
			throw new MailBoxConfigurationServicesException(Messages.STAGED_FILEID_DOES_NOT_EXIST, guid,
					Response.Status.BAD_REQUEST);
		}

		// UnStaging the stagedFile by changing its status to INACTIVE
		StagedFile unStagingFile = stagedFiles.get(0);
		unStagingFile.setStagedFileStatus(EntityStatus.INACTIVE.value());
		stagedFileDAO.merge(unStagingFile);

		// Setting the necessary details to the response
		dropBoxUnStagedResponse.setGUID(guid);
		dropBoxUnStagedResponse.setResponse(new ResponseDTO(Messages.DELETE_ONDEMAND_SUCCESSFUL, STAGED_FILE,
				Messages.SUCCESS));
		LOG.debug("Exit from drop staged files service.");

		return dropBoxUnStagedResponse;
	}
}
