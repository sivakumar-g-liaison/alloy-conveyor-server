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
import com.liaison.mailbox.rtdm.dao.DropboxDAO;
import com.liaison.mailbox.rtdm.dao.DropboxDAOBase;
import com.liaison.mailbox.rtdm.model.StagedFile;
import com.liaison.mailbox.service.dto.ResponseDTO;
import com.liaison.mailbox.service.dto.configuration.StagedFileDTO;
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

	public GetStagedFilesResponseDTO getStagedFiles(HttpServletRequest request) {

		GetStagedFilesResponseDTO serviceResponse = new GetStagedFilesResponseDTO();
		List<StagedFileDTO> stagedFiles = new ArrayList<StagedFileDTO>();
		// Dummy json holding 4 records
		for (int i = 0; i < 5; i++) {
			StagedFileDTO stagedFile = new StagedFileDTO();
			stagedFile.setFilePguid("Dummy staged file id" + i);
			stagedFile.setFileName("Dummy staged file Name" + i);
			stagedFile.setFilePath("Dummy staged file path" + i);
			stagedFiles.add(stagedFile);
		}

		serviceResponse.setResponse(new ResponseDTO(Messages.READ_SUCCESSFUL, STAGED_FILES, Messages.SUCCESS));
		serviceResponse.setStagedFiles(stagedFiles);
		return serviceResponse;
	}

	public String validateIfFileIdBelongsToAnyOrganisation(String fileId, List<String> tenancyKeys) {

		DropboxDAO dropboxDao = new DropboxDAOBase();
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

		StagedFileDTO stagedFileDTO = request.getStagedFile();
		if (stagedFileDTO == null) {
			throw new MailBoxConfigurationServicesException(Messages.INVALID_REQUEST, Response.Status.BAD_REQUEST);
		}

		// validation
		GenericValidator validator = new GenericValidator();
		validator.validate(stagedFileDTO);

		DropboxDAO dropboxDao = new DropboxDAOBase();

		StagedFile stagedFile = new StagedFile();
		stagedFileDTO.copyToEntity(stagedFile);

		dropboxDao.persist(stagedFile);

		serviceResponse.setResponse(new ResponseDTO(Messages.CREATED_SUCCESSFULLY, STAGED_FILE, Messages.SUCCESS));
		serviceResponse.setStagedFile(new StagedFileResponseDTO(String.valueOf(stagedFile.getPrimaryKey())));

		LOG.debug("Exit from add staged file.");
		return serviceResponse;
	}
}
