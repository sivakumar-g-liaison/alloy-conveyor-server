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

import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.dtdm.dao.MailBoxConfigurationDAO;
import com.liaison.mailbox.dtdm.dao.MailBoxConfigurationDAOBase;
import com.liaison.mailbox.dtdm.model.MailBox;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.rtdm.dao.StagedFileDAO;
import com.liaison.mailbox.rtdm.dao.StagedFileDAOBase;
import com.liaison.mailbox.rtdm.model.StagedFile;
import com.liaison.mailbox.service.dto.ResponseDTO;
import com.liaison.mailbox.service.dto.configuration.TenancyKeyDTO;
import com.liaison.mailbox.service.dto.dropbox.StagedFileDTO;
import com.liaison.mailbox.service.dto.dropbox.request.StagePayloadRequestDTO;
import com.liaison.mailbox.service.dto.dropbox.response.GetStagedFilesResponseDTO;
import com.liaison.mailbox.service.dto.dropbox.response.StagePayloadResponseDTO;
import com.liaison.mailbox.service.dto.dropbox.response.StagedFileResponseDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.util.MailBoxUtil;
import com.liaison.mailbox.service.validation.GenericValidator;

/**
 * Class which has  Dropbox related operations.
 *
 * @author OFS
 */
public class DropboxStagedFilesService {

	private static final Logger LOG = LogManager.getLogger(DropboxStagedFilesService.class);

	public static final String STAGED_FILES = "Staged Files";
	public static final String STAGED_FILE = "Staged File";

	/**
	 * Method to retrieve all staged files of the mailboxes linked to tenancy
	 * keys available in the manifest
	 *
	 * @param request
	 * @param aclManifest
	 * @return list of StagedFiles
	 * @throws IOException
	 * @throws JAXBException
	 */
	public GetStagedFilesResponseDTO getStagedFiles(String aclManifest, String fileName, String page, String pageSize,
			String sortField, String sortDirection) throws IOException, JAXBException {

		LOG.debug("Entering into get staged files service.");

		int totalCount = 0;
		int startOffset = 0;
		int count = 0;
		Map <String, Integer> pageOffsetDetails = null;

		GetStagedFilesResponseDTO serviceResponse = new GetStagedFilesResponseDTO();
		List<StagedFileDTO> stagedFileDTOs = new ArrayList<StagedFileDTO>();

		LOG.info("Retrieving tenancy keys from acl-manifest");

		// retrieve the tenancy key from acl manifest
		List<TenancyKeyDTO> tenancyKeys = MailBoxUtil.getTenancyKeysFromACLManifest(aclManifest);
		if (tenancyKeys.isEmpty()) {
			LOG.error("Retrieval of tenancy key from acl manifest failed");
			throw new MailBoxServicesException(Messages.TENANCY_KEY_RETRIEVAL_FAILED, Response.Status.BAD_REQUEST);
		}

		LOG.debug("retrieve tenancyKey Values from tenancyKeyDTO");
		List<String> tenancyKeyValues = new ArrayList<String>();
		for (TenancyKeyDTO tenancyKeyDTO : tenancyKeys) {
			tenancyKeyValues.add(tenancyKeyDTO.getGuid());
		}
		LOG.debug("The retrieved tenancykey values are {}", tenancyKeyValues);

		// retrieve corresponding mailboxes of the available tenancyKeys.
		MailBoxConfigurationDAO mailboxDao = new MailBoxConfigurationDAOBase();
		LOG.debug("retrieve all mailboxes linked to tenancykeys {}", tenancyKeyValues);
		List<String> mailboxIds = mailboxDao.findAllMailboxesLinkedToTenancyKeys(tenancyKeyValues);

		if (mailboxIds.isEmpty()) {
			LOG.error("There are no mailboxes linked to the tenancyKeys");
			throw new MailBoxServicesException("There are no mailboxes available for tenancykeys",
					Response.Status.NOT_FOUND);
		}

		// retrieve searched staged files of mailboxes.
		StagedFileDAO stagedFileDao = new StagedFileDAOBase();
		totalCount = stagedFileDao.getStagedFilesCountByName(mailboxIds, fileName);
		pageOffsetDetails = MailBoxUtil.getPagingOffsetDetails(page, pageSize, totalCount);
		startOffset = pageOffsetDetails.get(MailBoxConstants.PAGING_OFFSET);
		count = pageOffsetDetails.get(MailBoxConstants.PAGING_COUNT);
		serviceResponse.setTotalItems(totalCount);
		List<StagedFile> stagedFiles = stagedFileDao.findStagedFilesOfMailboxes(mailboxIds, fileName, startOffset, count, sortField, sortDirection);

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

	public String validateIfFileIdBelongsToAnyOrganisation(String fileId, List<String> tenancyKeys) {

		StagedFileDAO dropboxDao = new StagedFileDAOBase();

		StagedFile stagedFile = dropboxDao.find(StagedFile.class, fileId);
		if (stagedFile == null) {
			LOG.error("Staged file id missing.");
			throw new MailBoxConfigurationServicesException(Messages.STAGED_FILEID_DOES_NOT_EXIST, fileId,
					Response.Status.BAD_REQUEST);
		}

		MailBoxConfigurationDAO mailboxDao = new MailBoxConfigurationDAOBase();
		MailBox mailbox = mailboxDao.find(MailBox.class, stagedFile.getMailboxId());
		if (mailbox == null) {
			LOG.error("Given mailbox id doesn't exists.");
			throw new MailBoxConfigurationServicesException(Messages.MBX_DOES_NOT_EXIST, stagedFile.getMailboxId(),
					Response.Status.BAD_REQUEST);
		}

		for (String tkey : tenancyKeys) {
			if (mailbox.getTenancyKey().equals(tkey)) {
				return stagedFile.getSpectrumUri();
			}
		}

		return null;
	}

	public StagePayloadResponseDTO addStagedFile(StagePayloadRequestDTO request) throws IOException, JAXBException {

		LOG.debug("Entering into add staged file.");

		StagePayloadResponseDTO serviceResponse = new StagePayloadResponseDTO();

		try {

			StagedFileDTO stagedFileDTO = request.getStagedFile();
			if (stagedFileDTO == null) {
				LOG.error("Invalid request json.");
				throw new MailBoxConfigurationServicesException(Messages.INVALID_REQUEST, Response.Status.BAD_REQUEST);
			}

			// validation
			GenericValidator validator = new GenericValidator();
			validator.validate(stagedFileDTO);

			StagedFileDAO dropboxDao = new StagedFileDAOBase();
			StagedFile stagedFile = new StagedFile();
			stagedFile.copyFromDto(stagedFileDTO, true);
			dropboxDao.persist(stagedFile);

			serviceResponse.setResponse(new ResponseDTO(Messages.CREATED_SUCCESSFULLY, STAGED_FILE, Messages.SUCCESS));
			serviceResponse.setStagedFile(new StagedFileResponseDTO(String.valueOf(stagedFile.getPrimaryKey())));

			LOG.debug("Exit from add staged file.");
			return serviceResponse;

		} catch (MailBoxConfigurationServicesException e) {

			LOG.error(Messages.CREATE_OPERATION_FAILED.name(), e);
			serviceResponse.setResponse(new ResponseDTO(Messages.CREATE_OPERATION_FAILED, STAGED_FILE,
					Messages.FAILURE, e.getMessage()));
			return serviceResponse;

		}
	}
}
