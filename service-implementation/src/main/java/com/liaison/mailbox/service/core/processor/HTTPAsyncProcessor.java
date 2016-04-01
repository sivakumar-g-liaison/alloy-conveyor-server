/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.core.processor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.commons.exception.LiaisonRuntimeException;
import com.liaison.commons.message.glass.dom.StatusType;
import com.liaison.dto.queue.WorkTicket;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.service.glass.util.GlassMessage;
import com.liaison.mailbox.service.rest.HTTPListenerResource;
import com.liaison.mailbox.service.util.WorkTicketUtil;

/**
 * Class that deals with processing of async request.
 * 
 * @author OFS
 */
public class HTTPAsyncProcessor extends HTTPAbstractProcessor {

    private static final Logger logger = LogManager.getLogger(HTTPListenerResource.class);

    public void processWorkTicket(WorkTicket workTicket, GlassMessage glassMessage) {

        try {
            WorkTicketUtil.postWrkTcktToQ(workTicket);
            glassMessage.logProcessingStatus(StatusType.QUEUED, "Http Async - Work Ticket queued.", MailBoxConstants.HTTPASYNCPROCESSOR);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new LiaisonRuntimeException("Unable to Read Request. " + e.getMessage());
        }
    }

}
