// TODO check web.xml - this should be deprecated.


/*
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall 
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.framework.bootstrap;


import com.liaison.mailbox.com.liaison.queue.ProcessorQueuePoller;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.commons.audit.AuditStatement.Status;
import com.liaison.commons.audit.DefaultAuditStatement;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;


/**
 * Initialization Servlet
 * <p/>
 * <P>
 * Bootstrapper
 * <p/>
 * TODO: Probably not the best place for this. Should likely move this (and all servlets) to TODO
 * within the guice framework.
 * 
 * @author Robert.Christian
 * @version 1.0
 */
public class InitializationServlet extends HttpServlet {


	private static final long serialVersionUID = -8418412083748649428L;
	private static final Logger logger = LogManager.getLogger(InitializationServlet.class);


    public void init(ServletConfig config) throws ServletException {
        ProcessorQueuePoller.startPolling();
    	logger.info(new DefaultAuditStatement(Status.SUCCEED,"initilize", com.liaison.commons.audit.pci.PCIV20Requirement.PCI10_2_6));
    }

}
