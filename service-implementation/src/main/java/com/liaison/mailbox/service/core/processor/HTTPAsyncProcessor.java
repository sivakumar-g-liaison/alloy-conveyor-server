package com.liaison.mailbox.service.core.processor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.commons.exception.LiaisonRuntimeException;
import com.liaison.commons.message.glass.dom.StatusType;
import com.liaison.dto.queue.WorkTicket;
import com.liaison.mailbox.service.rest.HTTPListenerResource;
import com.liaison.mailbox.service.util.GlassMessage;
import com.liaison.mailbox.service.util.WorkTicketUtil;

public class HTTPAsyncProcessor extends HTTPAbstractProcessor {

    private static final Logger logger = LogManager.getLogger(HTTPListenerResource.class);

    public void processWorkTicket(WorkTicket workTicket, String mailboxPguid, GlassMessage glassMessage) {

        try {
            WorkTicketUtil.postWrkTcktToQ(workTicket);
            glassMessage.logProcessingStatus(StatusType.QUEUED, "Http Asyc - Work Ticket queued.");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new LiaisonRuntimeException("Unable to Read Request. " + e.getMessage());
        }
    }

}
