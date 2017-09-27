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

import com.liaison.commons.acl.util.ACLUtil;
import com.liaison.commons.acl.util.RemoteURLPublicKeyVerifier;
import com.liaison.commons.acl.util.SignatureVerifier;
import com.liaison.commons.audit.AuditStatement.Status;
import com.liaison.commons.audit.DefaultAuditStatement;
import com.liaison.commons.jpa.DAOUtil;
import com.liaison.commons.util.client.http.HTTPRequest;
import com.liaison.commons.util.settings.DecryptableConfiguration;
import com.liaison.commons.util.settings.LiaisonArchaiusConfiguration;
import com.liaison.health.check.file.FileReadDeleteCheck;
import com.liaison.health.check.jdbc.JdbcConnectionCheck;
import com.liaison.health.core.LiaisonHealthCheckRegistry;
import com.liaison.health.core.management.ThreadBlockedHealthCheck;
import com.liaison.health.core.management.ThreadDeadlockHealthCheck;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.enums.DeploymentType;
import com.liaison.mailbox.service.core.ProcessorExecutionConfigurationService;
import com.liaison.mailbox.service.core.bootstrap.QueueAndTopicProcessInitializer;
import com.liaison.mailbox.service.util.MailBoxUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import java.net.URL;

import static com.liaison.mailbox.MailBoxConstants.CONFIGURATION_SERVICE_BROKER_ASYNC_URI;


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

    private static final String PROPERTY_SERVICE_NFS_MOUNT = "com.liaison.service.nfs.mount";

    public void init(ServletConfig config) throws ServletException {

        DecryptableConfiguration configuration = LiaisonArchaiusConfiguration.getInstance();
        String deploymentType = configuration.getString(MailBoxConstants.DEPLOYMENT_TYPE, DeploymentType.RELAY.getValue());

        // nfs health check
        // check only if current service is not dropbox
        if (!DeploymentType.CONVEYOR.getValue().equals(deploymentType)) {
            String[] serviceNfsMount = configuration.getStringArray(PROPERTY_SERVICE_NFS_MOUNT);
            if (serviceNfsMount != null) {
                for (String mount : serviceNfsMount) {
                    LiaisonHealthCheckRegistry.INSTANCE.register(mount + "_read_delete_check",
                            new FileReadDeleteCheck(mount));
                }
            }
        }

    	logger.info(new DefaultAuditStatement(Status.SUCCEED,"initialize", com.liaison.commons.audit.pci.PCIV20Requirement.PCI10_2_6));

    	DAOUtil.init();
    	// Check stuck processors (ie., processorExecutionState is "PROCESSING") during the application startup.
    	// Update the status from "PROCESSING" to "FAILED" for the current node.
        ProcessorExecutionConfigurationService.updateExecutionStateOnInit();

        //QUEUE and TOPIC consumers initialization
        QueueAndTopicProcessInitializer.initialize();

		// db health check
		LiaisonHealthCheckRegistry.INSTANCE.register("dtdm_db_connection_check",
				new JdbcConnectionCheck("jdbc/UCPPool-DTDM"));
		LiaisonHealthCheckRegistry.INSTANCE.register("rtdm_db_connection_check",
				new JdbcConnectionCheck("jdbc/UCPPool-RTDM"));
		LiaisonHealthCheckRegistry.INSTANCE.register("thread_deadlock_check",
				new ThreadDeadlockHealthCheck(1));
		LiaisonHealthCheckRegistry.INSTANCE.register("thread_blocked_check",
				new ThreadBlockedHealthCheck(10));

		// Set ACL Filter Signature Verifier
		SignatureVerifier aclSignatureVerifier = new RemoteURLPublicKeyVerifier();

		ACLUtil.setSignatureVerifier(aclSignatureVerifier);
		logger.info(new DefaultAuditStatement(Status.SUCCEED, "ACL Filter Signature Verifier Set: " + aclSignatureVerifier.getClass().getName(), com.liaison.commons.audit.pci.PCIV20Requirement.PCI10_2_6));
		logger.info(new DefaultAuditStatement(Status.SUCCEED, "initialize via InitializationServlet", com.liaison.commons.audit.pci.PCIV20Requirement.PCI10_2_6));

        //Register sb http async host
        String serviceBrokerUri = configuration.getString(CONFIGURATION_SERVICE_BROKER_ASYNC_URI);
        if (!MailBoxUtil.isEmpty(serviceBrokerUri)) {
            try {

                URL uri = new URL(serviceBrokerUri);
                HTTPRequest.registerHostForSeparateConnectionPool(uri.getHost());
                HTTPRequest.registerHealthCheck();
            } catch (Exception e) {
                logger.error("Unable to register http sbasync pool", e);
            }
        }

	}

}
