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

import java.sql.SQLException;

import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import oracle.ucp.UniversalConnectionPoolException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liaison.commons.jpa.DAOUtil;
import com.liaison.commons.util.UUIDGen;
import com.liaison.commons.util.datasource.OracleDataSource;
import com.liaison.commons.util.settings.DecryptableConfiguration;
import com.liaison.commons.util.settings.LiaisonConfigurationFactory;
import com.liaison.framework.audit.AuditStatement;
import com.liaison.framework.audit.DefaultAuditStatement;
import com.liaison.framework.audit.pci.PCIV20Requirement;

/**
 * Initialization Servlet
 * <p/>
 * <P>Bootstrapper
 * <p/>
 * TODO:  Probably not the best place for this.  Should likely move this (and all servlets) to
 * TODO within the guice framework.
 *
 * @author Robert.Christian
 * @version 1.0
 */
public class InitializationServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(InitializationServlet.class);
    private static DecryptableConfiguration configuration = LiaisonConfigurationFactory.getConfiguration();
    private static boolean isInitialized = false;

    public void init(ServletConfig config) throws ServletException {
        if (!isInitialized) {
            try {
                OracleDataSource.initOracleDataSource(); // TODO This needs to be moved to JMX
                DAOUtil.init(); // TODO This does the work of loading all JAP entity files.  We should change to allow the query string to be passed.
                UUIDGen.init();
                
                isInitialized = true;
                DefaultAuditStatement audit = new DefaultAuditStatement(PCIV20Requirement.PCI10_2_6, AuditStatement.Status.SUCCEED, "Initialization via servlet");   	
                logger.info("Servlet Init", audit);
             } catch (SQLException | NamingException | UniversalConnectionPoolException e) {
                logger.error(e.getMessage(), e);
                throw new ServletException("JPA Persistence initialialization failed! See log for details.");
            }        
        }
    }

}
