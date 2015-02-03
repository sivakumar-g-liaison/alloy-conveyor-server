package com.liaison.mailbox.service.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Map;

import javax.servlet.ServletInputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.commons.jaxb.JAXBUtility;
import com.liaison.commons.util.UUIDGen;
import com.liaison.dto.queue.WorkTicket;
import com.liaison.fs2.api.FS2MetaSnapshot;
import com.liaison.fs2.api.FS2ObjectHeaders;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.queue.sender.SweeperQueue;
import com.liaison.mailbox.service.storage.util.StorageUtilities;

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

	/**
	 * This method will persist payload in spectrum.
	 * 
	 * @param request
	 * @param workTicket
	 * @throws IOException
	 */
	public static void storePayload(ServletInputStream stream, WorkTicket workTicket,
			Map<String, String> httpListenerProperties) throws Exception {

		try (InputStream payloadToPersist = stream) {

			FS2ObjectHeaders fs2Header = constructFS2Headers(workTicket, httpListenerProperties);
			FS2MetaSnapshot metaSnapShot = StorageUtilities.persistPayload(payloadToPersist,
					workTicket.getGlobalProcessId(), fs2Header,
					Boolean.valueOf(httpListenerProperties.get(MailBoxConstants.HTTPLISTENER_SECUREDPAYLOAD)));
			LOGGER.info("The received path uri is {} ", metaSnapShot.getURI().toString());
			// Hack
			workTicket.setPayloadSize(Long.valueOf(metaSnapShot.getHeader(MailBoxConstants.KEY_RAW_PAYLOAD_SIZE)[0]));
			workTicket.setPayloadURI(metaSnapShot.getURI().toString());
		}
	}

	/**
	 * Method to construct FS2ObjectHeaders from the given workTicket
	 * 
	 * @param workTicket
	 * @return FS2ObjectHeaders
	 * @throws IOException
	 * @throws MailBoxServicesException
	 */
	public static FS2ObjectHeaders constructFS2Headers(WorkTicket workTicket, Map<String, String> httpListenerProperties) {

		FS2ObjectHeaders fs2Header = new FS2ObjectHeaders();
		fs2Header.addHeader(MailBoxConstants.KEY_GLOBAL_PROCESS_ID, workTicket.getGlobalProcessId());
		fs2Header.addHeader(MailBoxConstants.KEY_PIPELINE_ID, workTicket.getPipelineId());
		fs2Header.addHeader(MailBoxConstants.KEY_SERVICE_INSTANCE_ID,
				httpListenerProperties.get(MailBoxConstants.KEY_SERVICE_INSTANCE_ID));
		fs2Header.addHeader(MailBoxConstants.KEY_TENANCY_KEY,
				(MailBoxConstants.PIPELINE_FULLY_QUALIFIED_PACKAGE + ":" + workTicket.getPipelineId()));
		LOGGER.debug("FS2 Headers set are {}", fs2Header.getHeaders());
		return fs2Header;
	}
}
