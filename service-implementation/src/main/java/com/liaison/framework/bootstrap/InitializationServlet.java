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

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.commons.acl.util.ACLUtil;
import com.liaison.commons.acl.util.RemoteURLPublicKeyVerifier;
import com.liaison.commons.acl.util.SignatureVerifier;
import com.liaison.commons.audit.AuditStatement.Status;
import com.liaison.commons.audit.DefaultAuditStatement;
import com.liaison.commons.jpa.DAOUtil;
import com.liaison.commons.util.UUIDGen;
import com.liaison.commons.util.settings.DecryptableConfiguration;
import com.liaison.commons.util.settings.LiaisonConfigurationFactory;
import com.liaison.mailbox.service.queue.consumer.ProcessorQueuePoller;
import com.liaison.mailbox.service.queue.consumer.ServiceBrokerToDropboxWorkTicketQueuePoller;
import com.liaison.mailbox.service.queue.consumer.ServiceBrokerToMailboxWorkTicketPoller;


/**
 * Initialization Servlet
 * <p/>
 * <P>
 * Bootstrapper
 * <p/>
 * 
 * @author Robert.Christian
 * @version 1.0
 */
public class InitializationServlet extends HttpServlet {

	private static final long serialVersionUID = -8418412083748649428L;
	private static final Logger logger = LogManager.getLogger(InitializationServlet.class);	
	DecryptableConfiguration configuration = LiaisonConfigurationFactory.getConfiguration();  
	public static final String START_DROPBOX_QUEUE = "com.liaison.deployAsDropbox";

    public void init(ServletConfig config) throws ServletException {
    	
    	if (configuration.getBoolean(START_DROPBOX_QUEUE, false)) {
    	    logger.debug("dropbox queue starts to poll");
    		ServiceBrokerToDropboxWorkTicketQueuePoller.startPolling();       
        } else {
            logger.debug("processor and sweeper queues starts to poll");
        	ProcessorQueuePoller.startPolling();
        	ServiceBrokerToMailboxWorkTicketPoller.startPolling();
        }
        
    	logger.info(new DefaultAuditStatement(Status.SUCCEED,"initialize", com.liaison.commons.audit.pci.PCIV20Requirement.PCI10_2_6));

        DAOUtil.init();
        UUIDGen.init();
        
      //Set ACL Filter Signature Verifier
      SignatureVerifier aclSignatureVerifier = new RemoteURLPublicKeyVerifier();

      ACLUtil.setSignatureVerifier(aclSignatureVerifier);
      logger.info(new DefaultAuditStatement(Status.SUCCEED, "ACL Filter Signature Verifier Set: " + aclSignatureVerifier.getClass().getName(), com.liaison.commons.audit.pci.PCIV20Requirement.PCI10_2_6));
      logger.info(new DefaultAuditStatement(Status.SUCCEED,"initialize via InitializationServlet", com.liaison.commons.audit.pci.PCIV20Requirement.PCI10_2_6));

    }

}
