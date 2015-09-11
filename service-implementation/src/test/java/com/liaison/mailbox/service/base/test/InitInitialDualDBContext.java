/**
 * Copyright 2014 Liaison Technologies, Inc.
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.mailbox.service.base.test;

import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.liaison.commons.util.settings.DecryptableConfiguration;
import com.liaison.commons.util.settings.LiaisonConfigurationFactory;

import oracle.jdbc.pool.OracleConnectionPoolDataSource;

public class InitInitialDualDBContext {

	public static final String DB_DRIVER_DTDM = "com.liaison.dtdm.DB_DRIVER";
	public static final String DB_PASSWORD_DTDM = "com.liaison.dtdm.DB_PASSWORD";
	public static final String DB_URL_DTDM = "com.liaison.dtdm.DB_URL";
	public static final String DB_USER_DTDM = "com.liaison.dtdm.DB_USER";

	public static final String DB_DRIVER_RTDM = "com.liaison.rtdm.DB_DRIVER";
	public static final String DB_PASSWORD_RTDM = "com.liaison.rtdm.DB_PASSWORD";
	public static final String DB_URL_RTDM = "com.liaison.rtdm.DB_URL";
	public static final String DB_USER_RTDM = "com.liaison.rtdm.DB_USER";

	//property of our db connection initialization query
	public static final String INITIALIZATION_QUERY_PROPERTY = "com.liaison.initializationQuery";

	//jpa persistence unit name
	public static final String PERSISTENCE_UNIT_NAME_PROPERTY_EDM = "com.liaison.dtdm.persistenceUnitName";
	public static final String PERSISTENCE_UNIT_NAME_PROPERTY_RTDM = "com.liaison.rtdm.persistenceUnitName";

	private static DecryptableConfiguration configuration = LiaisonConfigurationFactory.getConfiguration();
	private static boolean isInitialized = false;

	public static void init() throws SQLException, ClassNotFoundException, NamingException {
        if (!isInitialized) {
            System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.naming.java.javaURLContextFactory");
            System.setProperty(Context.URL_PKG_PREFIXES, "org.apache.naming");
            InitialContext ic = new InitialContext();

            ic.createSubcontext("java:");
            ic.createSubcontext("java:comp");
            ic.createSubcontext("java:comp/env");
            ic.createSubcontext("java:comp/env/jdbc");

            String databaseDriverClassName = configuration.getString(DB_DRIVER_DTDM);
            if (null == databaseDriverClassName) {
                throw new RuntimeException("Database drive property is missing. Verify the load of configuration properties.");
            }
            try {
                Class.forName(configuration.getString(DB_DRIVER_DTDM));
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }

            OracleConnectionPoolDataSource pds11;
            try {
                pds11 = new OracleConnectionPoolDataSource();
                pds11.setURL(configuration.getString(DB_URL_DTDM));
                pds11.setUser(configuration.getString(DB_USER_DTDM));
                pds11.setPassword(String.valueOf(configuration.getDecryptedCharArray(DB_PASSWORD_DTDM, false)));
                pds11.setDriverType(databaseDriverClassName);
            } catch (Exception e1) {
                throw new RuntimeException("Unable to create a pooled datasource.", e1);
            }

            ic.bind("java:comp/env/jdbc/UCPPool-DTDM", pds11);

            databaseDriverClassName = configuration.getString(DB_DRIVER_RTDM);
            if (null == databaseDriverClassName) {
                throw new RuntimeException("Database drive property is missing. Verify the load of configuration properties.");
            }
            try {
                Class.forName(configuration.getString(DB_DRIVER_RTDM));
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }

            OracleConnectionPoolDataSource pds22;
            try {
                pds22 = new OracleConnectionPoolDataSource();
                pds22.setURL(configuration.getString(DB_URL_RTDM));
                pds22.setUser(configuration.getString(DB_USER_RTDM));
                pds22.setPassword(String.valueOf(configuration.getDecryptedCharArray(DB_PASSWORD_DTDM, false)));
                pds22.setDriverType(databaseDriverClassName);
            } catch (Exception e1) {
                throw new RuntimeException("Unable to create a pooled datasource.", e1);
            }

            ic.bind("java:comp/env/jdbc/UCPPool-RTDM", pds22);

            isInitialized = true;
        }

	}
}
