package com.liaison.mailbox.service.util;

import java.util.Date;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.commons.jaxb.JAXBUtility;
import com.liaison.commons.util.UUIDGen;
import com.liaison.dto.queue.WorkTicket;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.service.queue.sender.SweeperQueue;

public class WorkTicketUtil {

	private static final Logger LOGGER = LogManager.getLogger(WorkTicketUtil.class);

	/**
	 * retrieve the pipeline id configured in httplistener of mailbox
	 *
	 * @param mailboxpguid
	 * @Param isSync boolean
	 * @return String pipeline id
	 *
	 *
	 */
	public static String retrievePipelineId(Map<String, String> httpListenerProperties) {

		String pipelineId = null;
		pipelineId = httpListenerProperties.get(MailBoxConstants.HTTPLISTENER_PIPELINEID);
		LOGGER.info("PIPELINE ID is set to be :" + pipelineId);
		return pipelineId;
	}

	public static void constructMetaDataJson(WorkTicket workTicket) throws Exception {
		String workTicketJson = JAXBUtility.marshalToJSON(workTicket);
		postToQueue(workTicketJson);
	}

	public static void postToQueue(String message) throws Exception {
		SweeperQueue.getInstance().sendMessages(message);
		LOGGER.debug("postToQueue, message: {}", message);

	}

	public static void assignGlobalProcessId(WorkTicket workTicket) {
		UUIDGen uuidGen = new UUIDGen();
		String uuid = uuidGen.getUUID();
		workTicket.setGlobalProcessId(uuid);
	}

	public static void assignTimestamp(WorkTicket workTicket) {
		workTicket.setCreatedTime(new Date());
	}
}
