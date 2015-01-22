package com.liaison.mailbox.service.rest;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.commons.jaxb.JAXBUtility;
import com.liaison.dto.queue.WorkTicket;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.service.queue.DummyDropboxQueue;

@Path("dummydropboxqueueclient")
public class DummyDropboxQueueClient {
private static final Logger logger = LogManager.getLogger(DummyDropboxQueueClient.class);
	
	@POST
	public Response postDummyProcesssedPayload(
			@Context HttpServletRequest request, 
			@QueryParam(value = "mailboxId") final String mailboxId,
			@QueryParam(value = "spectrumURL") String spectrumURL) {
	
		try {
			
			//spectrumURL = "fs2://secure@dev-int/mailbox/payload/1.0/BA2668600A0A01700A4DA3CDF9111849";
			//spectrumURL = "fs2:/mllp/payload/1.0/A067FB260A0A11A611857541B17AC518"; //URLDecoder.decode(spectrumURL, "UTF-8");
			WorkTicket ticketRequest = new WorkTicket();
			ticketRequest.setPayloadURI(spectrumURL);
			ticketRequest.setPayloadSize(1024l);
			ticketRequest.setFileName("Dummy name");
			ticketRequest.setAdditionalContext(MailBoxConstants.KEY_MAILBOX_ID, mailboxId);
			ticketRequest.setAdditionalContext(MailBoxConstants.KEY_OVERWRITE, Boolean.FALSE);	
			ticketRequest.setAdditionalContext(MailBoxConstants.KEY_FILE_PATH, "Dummy Path");
			String payloadTicket = JAXBUtility.marshalToJSON(ticketRequest);
			DummyDropboxQueue.getInstance().sendMessages(payloadTicket);
			return Response.status(200).header("Content-Type", MediaType.TEXT_PLAIN).entity("Posted to Queue Successfully").build();
		} catch (JAXBException | IOException e) {
			logger.error("Unable to marshal PayloadTicketRequestDTO", e);
			return Response.status(500).header("Content-Type", MediaType.TEXT_PLAIN).entity("Posted to Queue got failed").build();
		}
	}
}
