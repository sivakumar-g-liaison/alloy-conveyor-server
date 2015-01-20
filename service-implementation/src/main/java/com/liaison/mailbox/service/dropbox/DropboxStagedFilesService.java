package com.liaison.mailbox.service.dropbox;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.service.dto.ResponseDTO;
import com.liaison.mailbox.service.dto.configuration.StagedFileDTO;
import com.liaison.mailbox.service.dto.dropbox.response.GetStagedFilesResponseDTO;
import com.liaison.mailbox.service.storage.util.StorageUtilities;

public class DropboxStagedFilesService {

	private static final Logger LOG = LogManager.getLogger(DropboxStagedFilesService.class);
	
	public static final String STAGED_FILES = "Staged Files";
	
	public GetStagedFilesResponseDTO getStagedFiles(HttpServletRequest request) {
		
		GetStagedFilesResponseDTO serviceResponse = new GetStagedFilesResponseDTO();
		List <StagedFileDTO> stagedFiles = new ArrayList<StagedFileDTO>();
		// Dummy json holding 4 records
		for (int i = 0; i < 5; i++)  {
			StagedFileDTO stagedFile = new StagedFileDTO();
			stagedFile.setFilePguid("Dummy staged file id" + i);
			stagedFile.setFileName("Dummy staged file Name" +  i);
			stagedFile.setFilePath("Dummy staged file path" + i);
			stagedFiles.add(stagedFile);
		}
		
		serviceResponse.setResponse(new ResponseDTO(Messages.READ_SUCCESSFUL, STAGED_FILES, Messages.SUCCESS));
		serviceResponse.setStagedFiles(stagedFiles);
		return serviceResponse;
	}
	
	public InputStream getStagedFileStream(String fileId) {
		
		//TODO getting spectrum url for the given fileId
		String payloadURI = "Payload uri from db";
		
		//get payload from spectrum
		InputStream payload = StorageUtilities.retrievePayload(payloadURI);
		
		return payload;
	}
	
	public boolean validateFileId(String fileId) {
		
		return false;
	}
}
