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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

import com.liaison.commons.jaxb.JAXBUtility;
import com.liaison.commons.logging.LogTags;
import com.liaison.commons.message.glass.dom.StatusType;
import com.liaison.dto.queue.WorkTicket;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.enums.ExecutionState;
import com.liaison.mailbox.enums.ProcessorType;
import com.liaison.mailbox.enums.Protocol;
import com.liaison.mailbox.service.dto.dropbox.StagedFileDTO;
import com.liaison.mailbox.service.dto.dropbox.request.StagePayloadRequestDTO;
import com.liaison.mailbox.service.glass.util.GlassMessage;
import com.liaison.mailbox.service.util.MailBoxUtil;

/**
 * Class which has Dropbox related operations.
 *
 * @author OFS
 */
public class DropboxService implements Runnable {

	private static final Logger LOG = LogManager.getLogger(DropboxService.class);
	private String message;
	
	public DropboxService(String message) {
		this.message = message;
		
	}

	/**
	 * Method which will consume request from dropbox queue and log a staged event in StagedFiles Table in DB
	 *
	 * @param request workticket json
	 */
	public void invokeDropboxQueue(String request) {

		try {

			LOG.info("Consumed WORKTICKET [" + request + "]");
			WorkTicket workTicket = JAXBUtility.unmarshalFromJSON(request, WorkTicket.class);

			//Fish tag global process id
			ThreadContext.clearMap(); //set new context after clearing
			ThreadContext.put(LogTags.GLOBAL_PROCESS_ID, workTicket.getGlobalProcessId());

		    GlassMessage glassMessage = new GlassMessage(workTicket);
		    glassMessage.setCategory(ProcessorType.DROPBOXPROCESSOR);
		    glassMessage.setProtocol(Protocol.DROPBOXPROCESSOR.getCode());
		    glassMessage.setStatus(ExecutionState.READY);

	        // log activity status
	        glassMessage.logProcessingStatus(StatusType.RUNNING, MailBoxConstants.DROPBOX_SERVICE_NAME + ": " + MailBoxConstants.DROPBOX_WORKTICKET_CONSUMED, MailBoxConstants.DROPBOXPROCESSOR, null);
	        // log timestamp
	        glassMessage.logBeginTimestamp(MailBoxConstants.DROPBOX_FILE_TRANSFER);

	        DropboxStagedFilesService stageFileService = new DropboxStagedFilesService();

			StagePayloadRequestDTO dtoReq = new StagePayloadRequestDTO();
			StagedFileDTO stageFileReqDTO = new StagedFileDTO(workTicket);
			dtoReq.setStagedFile(stageFileReqDTO);
			stageFileService.addStagedFile(dtoReq, glassMessage);

		} catch(Exception e) {
			LOG.error(MailBoxUtil.constructMessage(null, null, "Stage file failed"), e);
		} finally {
			ThreadContext.clearMap(); //set new context after clearing
		}
		LOG.info("Processed WORKTICKET [" + request + "]");

	}

	@Override
	public void run() {
		this.invokeDropboxQueue(message);
	}
}
