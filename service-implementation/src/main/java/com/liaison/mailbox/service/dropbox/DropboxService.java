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

import javax.xml.bind.JAXBException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.liaison.commons.jaxb.JAXBUtility;
import com.liaison.commons.message.glass.dom.StatusType;
import com.liaison.dto.queue.WorkTicket;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.enums.ExecutionState;
import com.liaison.mailbox.enums.ProcessorType;
import com.liaison.mailbox.enums.Protocol;
import com.liaison.mailbox.service.dto.dropbox.StagedFileDTO;
import com.liaison.mailbox.service.dto.dropbox.request.StagePayloadRequestDTO;
import com.liaison.mailbox.service.util.GlassMessage;
import com.liaison.mailbox.service.util.MailBoxUtil;

/**
 * Class which has Dropbox related operations.
 *
 * @author OFS
 */
public class DropboxService {

	private static final Logger LOG = LogManager.getLogger(DropboxService.class);

	/**
	 * Method which will consume request from dropbox queue and log a staged event in StagedFiles Table in DB
	 *
	 * @param request
	 * @throws IOException
	 * @throws JAXBException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 * @throws JSONException
	 */
	public void invokeDropboxQueue(String request) throws JAXBException, IOException, JSONException {

		LOG.info("#####################----DROPBOX INVOCATION BLOCK-AFTER CONSUMING FROM QUEUE---############################################");

		LOG.info(MailBoxUtil.constructMessage(null, null, "JSON received from SB {}"), new JSONObject(request).toString(2));

		WorkTicket workTicket = JAXBUtility.unmarshalFromJSON(request, WorkTicket.class);

	    GlassMessage glassMessage = new GlassMessage(workTicket);
	    glassMessage.setCategory(ProcessorType.DROPBOXPROCESSOR);
	    glassMessage.setProtocol(Protocol.DROPBOXPROCESSOR.getCode());
	    glassMessage.setStatus(ExecutionState.READY);

        // log activity status
        glassMessage.logProcessingStatus(StatusType.RUNNING, MailBoxConstants.DROPBOX_SERVICE_NAME + ": " + MailBoxConstants.DROPBOX_WORKTICKET_CONSUMED);
        // log timestamp
        glassMessage.logBeginTimestamp(MailBoxConstants.DROPBOX_FILE_TRANSFER);

        DropboxStagedFilesService stageFileService = new DropboxStagedFilesService();

		StagePayloadRequestDTO dtoReq = new StagePayloadRequestDTO();
		StagedFileDTO stageFileReqDTO = new StagedFileDTO(workTicket);
		dtoReq.setStagedFile(stageFileReqDTO);
		stageFileService.addStagedFile(dtoReq, glassMessage);

	}
}
