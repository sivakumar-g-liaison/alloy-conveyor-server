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
import com.liaison.commons.util.settings.DecryptableConfiguration;
import com.liaison.commons.util.settings.LiaisonConfigurationFactory;
import com.liaison.health.check.file.FileReadDeleteCheck;
import com.liaison.health.check.jdbc.JdbcConnectionCheck;
import com.liaison.health.core.LiaisonHealthCheckRegistry;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.service.queue.QueueProcessInitializer;


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

	public static final String PROPERTY_SERVICE_NFS_MOUNT = "com.liaison.service.nfs.mount";

    public void init(ServletConfig config) throws ServletException {

        DecryptableConfiguration configuration = LiaisonConfigurationFactory.getConfiguration();
		boolean isDropbox = configuration.getBoolean(QueueProcessInitializer.START_DROPBOX_QUEUE, false);
        // nfs health check
        // check only if current service is not dropbox
        if(!isDropbox) {
            String[] serviceNfsMount = configuration.getStringArray(PROPERTY_SERVICE_NFS_MOUNT);
            if(serviceNfsMount != null) {
                for(String mount : serviceNfsMount) {
                    LiaisonHealthCheckRegistry.INSTANCE.register(mount + "_read_delete_check",
                            new FileReadDeleteCheck(mount));
                }
            }
        }

    	logger.info(new DefaultAuditStatement(Status.SUCCEED,"initialize", com.liaison.commons.audit.pci.PCIV20Requirement.PCI10_2_6));

        QueueProcessInitializer.initialize();
    	DAOUtil.init();

		// db health check
		LiaisonHealthCheckRegistry.INSTANCE.register("dtdm_db_connection_check",
				new JdbcConnectionCheck(
                        configuration.getString(MailBoxConstants.DTDM_DB_DRIVER_PROPERTY),
                        configuration.getString(MailBoxConstants.DTDM_DB_URL_PROPERTY),
                        configuration.getString(MailBoxConstants.DTDM_DB_USER_PROPERTY),
                        configuration.getString(MailBoxConstants.DTDM_DB_PASSWORD_PROPERTY)
				));
		LiaisonHealthCheckRegistry.INSTANCE.register("rtdm_db_connection_check",
				new JdbcConnectionCheck(
                        configuration.getString(MailBoxConstants.RTDM_DB_DRIVER_PROPERTY),
                        configuration.getString(MailBoxConstants.RTDM_DB_URL_PROPERTY),
                        configuration.getString(MailBoxConstants.RTDM_DB_USER_PROPERTY),
                        configuration.getString(MailBoxConstants.RTDM_DB_PASSWORD_PROPERTY)
				));

		// Set ACL Filter Signature Verifier
		SignatureVerifier aclSignatureVerifier = new RemoteURLPublicKeyVerifier();

		ACLUtil.setSignatureVerifier(aclSignatureVerifier);
		logger.info(new DefaultAuditStatement(Status.SUCCEED, "ACL Filter Signature Verifier Set: " + aclSignatureVerifier.getClass().getName(), com.liaison.commons.audit.pci.PCIV20Requirement.PCI10_2_6));
		logger.info(new DefaultAuditStatement(Status.SUCCEED, "initialize via InitializationServlet", com.liaison.commons.audit.pci.PCIV20Requirement.PCI10_2_6));

	}

}
