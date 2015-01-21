package com.liaison.mailbox.service.dropbox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.mailbox.dtdm.dao.MailBoxConfigurationDAO;
import com.liaison.mailbox.dtdm.dao.MailBoxConfigurationDAOBase;
import com.liaison.mailbox.dtdm.model.MailBox;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.rtdm.dao.StagedFileDAO;
import com.liaison.mailbox.rtdm.dao.StagedFileDAOBase;
import com.liaison.mailbox.rtdm.model.StagedFile;
import com.liaison.mailbox.service.dto.ResponseDTO;
import com.liaison.mailbox.service.dto.dropbox.StagedFileDTO;
import com.liaison.mailbox.service.dto.dropbox.request.StagePayloadRequestDTO;
import com.liaison.mailbox.service.dto.dropbox.response.GetStagedFilesResponseDTO;
import com.liaison.mailbox.service.dto.dropbox.response.StagePayloadResponseDTO;
import com.liaison.mailbox.service.dto.dropbox.response.StagedFileResponseDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.validation.GenericValidator;


public class DropboxStagedFilesService {

	private static final Logger LOG = LogManager.getLogger(DropboxStagedFilesService.class);

	public static final String STAGED_FILES = "Staged Files";
	public static final String STAGED_FILE = "Staged File";


	
	/**
	 * Method to retrieve all staged files of the mailboxes linked to 
	 * tenancykeys available in the manifest
	 * 
	 * @param request
	 * @param aclManifest
	 * @return list of StagedFiles
	 * @throws IOException
	 */
	public GetStagedFilesResponseDTO getStagedFiles(HttpServletRequest request, String aclManifest) throws IOException {
		
		GetStagedFilesResponseDTO serviceResponse = new GetStagedFilesResponseDTO();
		List <StagedFileDTO> stagedFileDTOs = new ArrayList<StagedFileDTO>();
		
		LOG.info("Retrieving tenancy keys from acl-manifest");
		
		//TODO: Facing problem while fetching the staged files need to resolve so currently implementation is commented out
		
		// retrieve the tenancy key from acl manifest
		/*List<TenancyKeyDTO> tenancyKeys = MailBoxUtil.getTenancyKeysFromACLManifest(aclManifest);
		if (tenancyKeys.isEmpty()) {
			LOG.error("retrieval of tenancy key from acl manifest failed");
			throw new MailBoxServicesException(Messages.TENANCY_KEY_RETRIEVAL_FAILED, Response.Status.BAD_REQUEST);
		}
		
		LOG.debug("retrieve tenancyKey Values from tenancyKeyDTO");
		List <String> tenancyKeyValues = new ArrayList<String>();
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
			throw new MailBoxServicesException("There are no mailboxes available for tenancykeys", Response.Status.NOT_FOUND);
		}
		
		// retrieve all staged files of mailboxes.
		StagedFileDAO stagedFileDao = new StagedFileDAOBase();
		List<StagedFile> stagedFiles = stagedFileDao.findStagedFilesOfMailboxes(mailboxIds);
		
		if (stagedFiles.isEmpty()) {
			LOG.error("There are no staged files available for linked mailboxes");
			throw new MailBoxServicesException("There are no staged Files available", Response.Status.NOT_FOUND);
		}
		
		for (StagedFile stagedFile : stagedFiles) {
			
			StagedFileDTO stagedFileDTO = new StagedFileDTO();
			stagedFileDTO.copyFromEntity(stagedFile);
			stagedFileDTOs.add(stagedFileDTO);
		}	*/
		// Dummy json holding 4 records
		for (int i = 0; i < 5; i++)  {

			StagedFileDTO stagedFile = new StagedFileDTO();
			stagedFile.setFilePguid("Dummy staged file id" + i);
			stagedFile.setFileName("Dummy staged file Name" + i);
			stagedFile.setFilePath("Dummy staged file path" + i);
			stagedFileDTOs.add(stagedFile);

		}

		serviceResponse.setResponse(new ResponseDTO(Messages.RETRIEVE_SUCCESSFUL, STAGED_FILES, Messages.SUCCESS));
		serviceResponse.setStagedFiles(stagedFileDTOs);
		return serviceResponse;
	}

	public String validateIfFileIdBelongsToAnyOrganisation(String fileId, List<String> tenancyKeys) {
		
		StagedFileDAO dropboxDao = new StagedFileDAOBase();

		StagedFile stagedFile = dropboxDao.find(StagedFile.class, fileId);
		if (stagedFile == null) {
			throw new MailBoxConfigurationServicesException(Messages.STAGED_FILEID_DOES_NOT_EXIST, fileId,
					Response.Status.BAD_REQUEST);
		}

		MailBoxConfigurationDAO mailboxDao = new MailBoxConfigurationDAOBase();
		MailBox mailbox = mailboxDao.find(MailBox.class, stagedFile.getMailboxId());
		if (mailbox == null) {
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

	public StagePayloadResponseDTO addStagedFile(StagePayloadRequestDTO request) throws IOException {

		LOG.debug("Entering into add staged file.");

		StagePayloadResponseDTO serviceResponse = new StagePayloadResponseDTO();
		
		try {

			StagedFileDTO stagedFileDTO = request.getStagedFile();
			if (stagedFileDTO == null) {
				throw new MailBoxConfigurationServicesException(Messages.INVALID_REQUEST, Response.Status.BAD_REQUEST);
			}
	
			// validation
			GenericValidator validator = new GenericValidator();
			validator.validate(stagedFileDTO);
	
			StagedFileDAO dropboxDao = new StagedFileDAOBase();
			StagedFile stagedFile = new StagedFile();
			stagedFile.copyToDto(stagedFileDTO);			
			dropboxDao.persist(stagedFile);
	
			serviceResponse.setResponse(new ResponseDTO(Messages.CREATED_SUCCESSFULLY, STAGED_FILE, Messages.SUCCESS));
			serviceResponse.setStagedFile(new StagedFileResponseDTO(String.valueOf(stagedFile.getPrimaryKey())));
	
			LOG.debug("Exit from add staged file.");
			return serviceResponse;
			
		} catch (MailBoxConfigurationServicesException e) {
	
			LOG.error(Messages.CREATE_OPERATION_FAILED.name(), e);
			serviceResponse.setResponse(new ResponseDTO(Messages.CREATE_OPERATION_FAILED, STAGED_FILE, Messages.FAILURE, e
					.getMessage()));
			return serviceResponse;
	
		}
	}
}

