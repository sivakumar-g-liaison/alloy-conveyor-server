package com.liaison.mailbox.service.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

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
	 * Method to construct FS2ObjectHeaders from the given workTicket
	 *
	 * @param workTicket
	 * @return FS2ObjectHeaders
	 * @throws IOException
	 * @throws MailBoxServicesException
	 */
	public static FS2ObjectHeaders constructFS2Headers(WorkTicket workTicket, Map <String, String> httpListenerProperties) {

		FS2ObjectHeaders fs2Header = new FS2ObjectHeaders();
		fs2Header.addHeader(MailBoxConstants.KEY_GLOBAL_PROCESS_ID, workTicket.getGlobalProcessId());
		fs2Header.addHeader(MailBoxConstants.KEY_PIPELINE_ID, workTicket.getPipelineId());
		fs2Header.addHeader(MailBoxConstants.KEY_SERVICE_INSTANCE_ID, httpListenerProperties.get(MailBoxConstants.KEY_SERVICE_INSTANCE_ID));
		fs2Header.addHeader(MailBoxConstants.KEY_TENANCY_KEY, (MailBoxConstants.PIPELINE_FULLY_QUALIFIED_PACKAGE + ":" + workTicket.getPipelineId()));
		LOGGER.debug("FS2 Headers set are {}", fs2Header.getHeaders());
		return fs2Header;
	}
	
	/**
	 * retrieve the pipeline id configured in httplistener of mailbox
	 *
	 * @param mailboxpguid
	 * @Param isSync boolean
	 * @return String pipeline id
	 *
	 *
	 */
	private static String retrievePipelineId(Map <String, String> httpListenerProperties) {

		String pipelineId = null;
		pipelineId = httpListenerProperties.get(MailBoxConstants.HTTPLISTENER_PIPELINEID);
		LOGGER.info("PIPELINE ID is set to be :"+pipelineId);
		return pipelineId;
	}
	
	/**
	 * Copies all the request header from HttpServletRequest to WorkTicket.
	 *
	 * @param request
	 *        HttpServletRequest
	 * @param request
	 *        workTicket
	 *
	 */
	public static void copyRequestHeadersToWorkTicket (HttpServletRequest request , WorkTicket workTicket)	{

		Enumeration<String> headerNames = request.getHeaderNames();
		while (headerNames.hasMoreElements())
		{
			String headerName = headerNames.nextElement();
			List<String> headerValues = new ArrayList<>();
			Enumeration<String> values = request.getHeaders(headerName);

			while (values.hasMoreElements())
			{
				headerValues.add(values.nextElement());
			}

			workTicket.addHeaders(headerName,  headerValues);
		}

	}
	
	/**
	 * This method will persist payload in spectrum.
	 *
	 * @param request
	 * @param workTicket
	 * @throws IOException
	 */
	public static void storePayload(HttpServletRequest request,	WorkTicket workTicket, Map <String, String> httpListenerProperties) throws Exception {

	  try (InputStream payloadToPersist = request.getInputStream()) {

              FS2ObjectHeaders fs2Header = constructFS2Headers(workTicket, httpListenerProperties);
              FS2MetaSnapshot metaSnapShot = StorageUtilities.persistPayload(payloadToPersist, workTicket.getGlobalProcessId(),
                            fs2Header, Boolean.valueOf(httpListenerProperties.get(MailBoxConstants.HTTPLISTENER_SECUREDPAYLOAD)));
              LOGGER.info("The received path uri is {} ", metaSnapShot.getURI().toString());
              //Hack
              workTicket.setPayloadSize(Long.valueOf(metaSnapShot.getHeader(MailBoxConstants.KEY_RAW_PAYLOAD_SIZE)[0]));
              workTicket.setPayloadURI(metaSnapShot.getURI().toString());
	    }
	}
	
	/**
	 * This method will create workTicket by given request.
	 *
	 * @param request
	 * @param mailboxPguid
	 * @param httpListenerProperties
	 * @return WorkTicket
	 */
	public static WorkTicket createWorkTicket(HttpServletRequest request, String mailboxPguid, Map <String, String> httpListenerProperties) {
		WorkTicket workTicket = new WorkTicket();
		workTicket.setAdditionalContext("httpMethod", request.getMethod());
		workTicket.setAdditionalContext("httpQueryString", request.getQueryString());
		workTicket.setAdditionalContext("httpRemotePort", request.getRemotePort());
		workTicket.setAdditionalContext("httpCharacterEncoding", (request.getCharacterEncoding() != null ? request.getCharacterEncoding() : ""));
		workTicket.setAdditionalContext("httpRemoteUser", (request.getRemoteUser() != null ? request.getRemoteUser() : "unknown-user"));
		workTicket.setAdditionalContext("mailboxId", mailboxPguid);
		workTicket.setAdditionalContext("httpRemoteAddress", request.getRemoteAddr());
		workTicket.setAdditionalContext("httpRequestPath", request.getRequestURL().toString());
		workTicket.setAdditionalContext("httpContentType", request.getContentType());
		workTicket.setPipelineId(retrievePipelineId(httpListenerProperties));
		copyRequestHeadersToWorkTicket(request, workTicket);
		assignGlobalProcessId(workTicket);
		assignTimestamp(workTicket);

		return workTicket;
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
