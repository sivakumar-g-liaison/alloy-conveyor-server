package com.liaison.mailbox.service.rest;

import java.io.IOException;
import java.net.URLEncoder;
import java.net.URLDecoder;

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
import com.liaison.mailbox.com.liaison.queue.ServiceBrokerToMailboxWorkTicket;
import com.liaison.mailbox.service.dto.configuration.PayloadTicketRequestDTO;

@Path("/jmsclient")
public class DummyJMSClient {
	
	private static final Logger logger = LogManager.getLogger(DummyJMSClient.class);
	
	@POST
	@Path("/{mailboxId}/{spectrumURL}/{targetFileName}")
	public Response postDummyProcesssedPayload(@Context HttpServletRequest request, @PathParam(value = "mailboxId") String mailboxId, @PathParam(value = "spectrumURL") String spectrumURL, @PathParam(value = "targetFileName") String targetFileName) {
	
		try {
			//spectrumURL = URLEncoder.encode(spectrumURL, "UTF-8");
			spectrumURL = "fs2:/mllp/payload/1.0/A067FB260A0A11A611857541B17AC518"; //URLDecoder.decode(spectrumURL, "UTF-8");
			PayloadTicketRequestDTO ticketRequest = new PayloadTicketRequestDTO(mailboxId, spectrumURL, targetFileName, false);
			String payloadTicket = JAXBUtility.marshalToJSON(ticketRequest);
			ServiceBrokerToMailboxWorkTicket.getInstance().pushMessages(payloadTicket);
			return Response.status(200).header("Content-Type", MediaType.TEXT_PLAIN).entity("Posted to Queue Successfully").build();
		} catch (JAXBException | IOException e) {
			logger.error("Unable to marshal PayloadTicketRequestDTO", e);
			return Response.status(500).header("Content-Type", MediaType.TEXT_PLAIN).entity("Posted to Queue got failed").build();
		}
	}

}
