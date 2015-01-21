package com.liaison.mailbox.service.dropbox;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

import com.liaison.commons.jaxb.JAXBUtility;
import com.liaison.dto.queue.WorkTicket;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.service.dto.dropbox.StagedFileDTO;
import com.liaison.mailbox.service.dto.dropbox.request.StagePayloadRequestDTO;

public class DropboxService {

	private static final Logger LOG = LogManager.getLogger(DropboxService.class);

	/**
	 * Method which will consume request from dropbox queue and log a staged
	 * event in StagedFiles Table in DB
	 * 
	 * @param request
	 * @throws IOException
	 * @throws JAXBException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 */
	public void invokeDropboxQueue(String request) throws JsonParseException, JsonMappingException, JAXBException,
			IOException {

		LOG.info("#####################----DROPBOX INVOCATION BLOCK-AFTER CONSUMING FROM QUEUE---############################################");
		
		WorkTicket workTicket = JAXBUtility.unmarshalFromJSON(request, WorkTicket.class);
		
		DropboxStagedFilesService stageFileService = new DropboxStagedFilesService();
		StagePayloadRequestDTO dtoReq = new StagePayloadRequestDTO();
		StagedFileDTO stageFileReqDTO = new StagedFileDTO(workTicket.getFileName(), "", workTicket.getAdditionalContext()
				.get(MailBoxConstants.KEY_FILE_PATH).toString(), workTicket.getPayloadSize().toString(), workTicket.getAdditionalContext().get(
						MailBoxConstants.KEY_MAILBOX_ID).toString(), workTicket.getPayloadURI());
		dtoReq.setStagedFile(stageFileReqDTO);
		
		stageFileService.addStagedFile(dtoReq);
	}
}
