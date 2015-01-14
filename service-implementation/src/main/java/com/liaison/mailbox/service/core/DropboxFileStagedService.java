package com.liaison.mailbox.service.core;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.service.dto.ResponseDTO;
import com.liaison.mailbox.service.dto.configuration.StagedFileDTO;
import com.liaison.mailbox.service.dto.configuration.response.GetStagedFilesResponseDTO;

public class DropboxFileStagedService {

	private static final Logger LOG = LogManager.getLogger(DropboxFileStagedService.class);
	
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
}
