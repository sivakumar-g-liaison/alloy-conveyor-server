package com.liaison.mailbox.service.core.processor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.commons.exception.LiaisonRuntimeException;
import com.liaison.commons.message.glass.dom.GatewayType;
import com.liaison.dto.queue.WorkTicket;
import com.liaison.mailbox.enums.ExecutionState;
import com.liaison.mailbox.enums.ProcessorType;
import com.liaison.mailbox.enums.Protocol;
import com.liaison.mailbox.service.rest.HTTPListenerResource;
import com.liaison.mailbox.service.util.GlassMessage;
import com.liaison.mailbox.service.util.MailBoxUtil;
import com.liaison.mailbox.service.util.TransactionVisibilityClient;
import com.liaison.mailbox.service.util.WorkTicketUtil;

public class HTTPAsyncProcessor extends HTTPAbstractProcessor{
	
	private static final Logger logger = LogManager.getLogger(HTTPListenerResource.class);
	
	public void processWorkTicket(WorkTicket workTicket,String mailboxPguid){
		
		try {
			WorkTicketUtil.postWrkTcktToQ(workTicket);
			//GLASS LOGGING BEGINS//
			TransactionVisibilityClient glassLogger = new TransactionVisibilityClient(MailBoxUtil.getGUID());
			GlassMessage glassMessage = new GlassMessage();
			glassMessage.setCategory(ProcessorType.HTTPASYNCPROCESSOR);
			glassMessage.setProtocol(Protocol.HTTPASYNCPROCESSOR.getCode());
			glassMessage.setGlobalPId(workTicket.getGlobalProcessId());
			glassMessage.setMailboxId(mailboxPguid);
			glassMessage.setStatus(ExecutionState.QUEUED);
			glassMessage.setPipelineId(workTicket.getPipelineId());
			glassMessage.setInAgent(GatewayType.REST);
			glassLogger.logToGlass(glassMessage);
		//GLASS LOGGING ENDS//
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new LiaisonRuntimeException("Unable to Read Request. " + e.getMessage());
		}
	}

}
