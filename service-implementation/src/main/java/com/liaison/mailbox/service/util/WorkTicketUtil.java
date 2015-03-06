package com.liaison.mailbox.service.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import org.apache.http.entity.ContentType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.commons.jaxb.JAXBUtility;
import com.liaison.commons.util.client.sftp.StringUtil;
import com.liaison.dto.queue.WorkTicket;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.queue.sender.SweeperQueue;
import com.liaison.mailbox.service.rest.HttpListener;

public class WorkTicketUtil {

	private static final Logger LOGGER = LogManager.getLogger(WorkTicketUtil.class);
	private static final int GLOBAL_PROCESS_ID_MINLENGTH = 3;
	private static final int GLOBAL_PROCESS_ID_MAXLENGTH = 32;
	private static final String GLOBAL_PROCESS_ID_PATTERN = "^[a-zA-Z0-9]*$";

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
		pipelineId = httpListenerProperties.get(MailBoxConstants.PROPERTY_HTTPLISTENER_PIPELINEID);
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

	/**
     * Validates the global process id if it is available in the header and set in the workticket.
     * Generates new guid if it is not available
     *
     * @param workTicket
     * @param globalProcessId - got from request header.
     */
    public void setGlobalProcessId(WorkTicket workTicket, String globalProcessId) {

            if (!StringUtil.isNullOrEmptyAfterTrim(globalProcessId)) {

                if (GLOBAL_PROCESS_ID_MAXLENGTH < globalProcessId.length()
                    || GLOBAL_PROCESS_ID_MINLENGTH > globalProcessId.length()
                    || !(globalProcessId.matches(GLOBAL_PROCESS_ID_PATTERN))) {
                    throw new MailBoxServicesException("The global process id is invalid", Response.Status.BAD_REQUEST);
                } else {
                    workTicket.setGlobalProcessId(globalProcessId);
                }

            } else {
                workTicket.setGlobalProcessId(MailBoxUtil.getGUID());
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
    public WorkTicket createWorkTicket(HttpServletRequest request, String mailboxPguid, Map <String, String> httpListenerProperties) {

        WorkTicket workTicket = new WorkTicket();
        workTicket.setAdditionalContext("httpMethod", request.getMethod());
        workTicket.setAdditionalContext("httpQueryString", request.getQueryString());
        workTicket.setAdditionalContext("httpRemotePort", request.getRemotePort());
        workTicket.setAdditionalContext("httpCharacterEncoding", (request.getCharacterEncoding() != null ? request.getCharacterEncoding() : ""));
        workTicket.setAdditionalContext("httpRemoteUser", (request.getRemoteUser() != null ? request.getRemoteUser() : "unknown-user"));
        workTicket.setAdditionalContext("mailboxId", mailboxPguid);
        workTicket.setAdditionalContext("httpRemoteAddress", request.getRemoteAddr());
        workTicket.setAdditionalContext("httpRequestPath", request.getRequestURL().toString());
        workTicket.setAdditionalContext("httpContentType", (request.getContentType() != null ? request.getContentType() : ContentType.TEXT_PLAIN.getMimeType()));
        copyRequestHeadersToWorkTicket(request, workTicket);
        workTicket.setCreatedTime(new Date());
        if(null != httpListenerProperties)
            workTicket.setPipelineId(WorkTicketUtil.retrievePipelineId(httpListenerProperties));
        setGlobalProcessId(workTicket, request.getHeader(HttpListener.GLOBAL_PROCESS_ID_HEADER));

        return workTicket;
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
    public void copyRequestHeadersToWorkTicket (HttpServletRequest request , WorkTicket workTicket)  {

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
}
