/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall 
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.mailbox.service.rest;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.commons.jaxb.JAXBUtility;
import com.liaison.dto.queue.WorkTicket;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.service.queue.DummyJMSClientQueue;
import com.wordnik.swagger.annotations.ApiParam;

@Path("/jmsclient/{mailboxId}/{spectrumURL}/{targetFileName}")
public class DummyJMSClient {
	
	private static final Logger logger = LogManager.getLogger(DummyJMSClient.class);
	
	@POST
	public Response postDummyProcesssedPayload(
			@Context HttpServletRequest request, 
			@PathParam(value = "mailboxId") final @ApiParam(name = "mailboxId", required = true, value = "mailbox guid") String mailboxId,
			@PathParam(value = "spectrumURL")  @ApiParam(name = "spectrumURL", required = true, value = "spectrum URL") String spectrumURL, 
			@PathParam(value = "targetFileName") final @ApiParam(name = "targetFileName", required = true, value = "target File Name") String targetFileName) {
	
		try {
			//spectrumURL = URLEncoder.encode(spectrumURL, "UTF-8");
			//spectrumURL = "fs2:/mailboxsweeper/payload/1.0/55FB3A3F0A0A000C0A774FB208B57192";
			spectrumURL = "sfs2:/mailboxsweeper/payload/1.0/13BD64360A0A007D0A180BCD85F93951";
			//spectrumURL = "fs2:/mllp/payload/1.0/A067FB260A0A11A611857541B17AC518"; //URLDecoder.decode(spectrumURL, "UTF-8");
			WorkTicket ticketRequest = new WorkTicket();
			ticketRequest.setPayloadURI(spectrumURL);
			ticketRequest.setFileName(targetFileName);
			ticketRequest.setAdditionalContext(MailBoxConstants.KEY_MAILBOX_ID, mailboxId);
			ticketRequest.setAdditionalContext(MailBoxConstants.KEY_OVERWRITE, Boolean.FALSE);		
			String payloadTicket = JAXBUtility.marshalToJSON(ticketRequest);
			DummyJMSClientQueue.getInstance().sendMessages(payloadTicket);
			return Response.status(200).header("Content-Type", MediaType.TEXT_PLAIN).entity("Posted to Queue Successfully").build();
		} catch (JAXBException | IOException e) {
			logger.error("Unable to marshal PayloadTicketRequestDTO", e);
			return Response.status(500).header("Content-Type", MediaType.TEXT_PLAIN).entity("Posted to Queue got failed").build();
		}
	}

}
