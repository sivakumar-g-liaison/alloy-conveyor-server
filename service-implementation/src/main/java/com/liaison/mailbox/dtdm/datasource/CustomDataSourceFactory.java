/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.dtdm.datasource;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.spi.ObjectFactory;

import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceFactory;

import org.apache.commons.configuration.ConversionException;

import com.liaison.commons.util.settings.DecryptableConfiguration;
import com.liaison.commons.util.settings.LiaisonConfigurationFactory;

/**
 * This class is a CustomDataSource Factory that can be used to configured at Tomcat Container or Tomcat WEB App Context Level.
 * Through the container declaration, a JNDI name can be automatically bound and available for Application to use
 *
 * HOW TO CONFIGURE/DEPLOY this class inside TOMCAT 7
 * (Assuming that you are building a FOO.WAR application)
 *
 * 1. Put ojdbc*.jar and ucp*.jar in  {tomcat-home}/lib folder
 * 2. In your FOO.WAR app, add a context.xml file (see below)
 *

<Context>
	<!--  factory="oracle.ucp.jdbc.PoolDataSourceImpl" -->
	<Resource
		name="jdbc/UCPPool"
		auth="Container"

		factory="com.liaison.datasource.CustomDataSourceFactory"

		type="oracle.ucp.jdbc.PoolDataSource"
		connectionFactoryClassName="oracle.jdbc.pool.OracleDataSource"

		description="Pas testing UCP Pool in Tomcat"

		minPoolSize="2" maxPoolSize="5" inactiveConnectionTimeout="20"
		user="ianh_kilimain_dba" password="12345678"

		url="jdbc:oracle:thin:@//lsildb23d.liaison.dev:1521/G2DEV.liaison.dev"

		connectionPoolName="UCPPool" validateConnectionOnBorrow="true"
		sqlForValidateConnection="select 1 from DUAL" driverClassName="oracle.jdbc.OracleDriver" />
</Context>


 *
 */
public class CustomDataSourceFactory
    implements ObjectFactory
{
	public static final String DB_DRIVER_PROPERTY                        = "com.liaison.dtdm.DB_DRIVER";
    public static final String DB_URL_PROPERTY                           = "com.liaison.dtdm.DB_URL";
    public static final String DB_USER_PROPERTY                          = "com.liaison.dtdm.DB_USER";
    public static final String DB_PASSWORD_PROPERTY                      = "com.liaison.dtdm.DB_PASSWORD";
    public static final String DB_MINPOOLSIZE_PROPERTY                   = "com.liaison.dtdm.DB_MINPOOLSIZE";
    public static final String DB_MAXPOOLSIZE_PROPERTY                   = "com.liaison.dtdm.DB_MAXPOOLSIZE";
    public static final String DB_CONNECTIONFACTORYCLASSNAME_PROPERTY    = "com.liaison.dtdm.DB_CONNECTIONFACTORYCLASSNAME";
    public static final String DB_INACTIVECONNECTIONTIMEOUT_PROPERTY     = "com.liaison.dtdm.DB_INACTIVECONNECTIONTIMEOUT";
    public static final String DB_VALIDATECONNECTIONONBORROW_PROPERTY    = "com.liaison.dtdm.DB_VALIDATECONNECTIONONBORROW";
    public static final String DB_DESCRIPTION_PROPERTY                   = "com.liaison.dtdm.DB_DESCRIPTION";
    public static final String DB_ABANDONEDCONNECTIONTIMEOUT_PROPERTY    = "com.liaison.dtdm.DB_ABANDONEDCONNECTIONTIMEOUT";
    public static final String DB_CONNECTIONHARVESTMAXCOUNT_PROPERTY     = "com.liaison.dtdm.DB_CONNECTIONHARVESTMAXCOUNT";
    public static final String DB_CONNECTIONHARVESTTRIGGERCOUNT_PROPERTY = "com.liaison.dtdm.DB_CONNECTIONHARVESTTRIGGERCOUNT";
    public static final String DB_CONNECTIONWAITTIMEOUT_PROPERTY         = "com.liaison.dtdm.DB_CONNECTIONWAITTIMEOUT";
    public static final String DB_FASTCONNECTIONFAILOVERENABLED_PROPERTY = "com.liaison.dtdm.DB_FASTCONNECTIONFAILOVERENABLED";
    public static final String DB_INITIALPOOLSIZE_PROPERTY               = "com.liaison.dtdm.DB_INITIALPOOLSIZE";
    public static final String DB_MAXCONNECTIONREUSECOUNT_PROPERTY       = "com.liaison.dtdm.DB_MAXCONNECTIONREUSECOUNT";
    public static final String DB_MAXCONNECTIONREUSETIME_PROPERTY        = "com.liaison.dtdm.DB_MAXCONNECTIONREUSETIME";
    public static final String DB_MAXIDLETIME_PROPERTY                   = "com.liaison.dtdm.DB_MAXIDLETIME";
    public static final String DB_TIMEOUTCHECKINTERVAL_PROPERTY          = "com.liaison.dtdm.DB_TIMEOUTCHECKINTERVAL";
    public static final String DB_TIMETOLIVECONNECTIONTIMEOUT_PROPERTY   = "com.liaison.dtdm.DB_TIMETOLIVECONNECTIONTIMEOUT";

    protected static final String FALSE = "false";
    protected static final String TRUE = "true";


	@Override
	public Object getObjectInstance (Object          object,
			                         Name            name,
			                         Context         nameCtx,
			                         Hashtable<?, ?> environment)
        throws Exception
    {
		System.out.println("CustomDataSourceFactory.getObjectInstance(): start");

		try
		{
			PoolDataSource poolDataSource = PoolDataSourceFactory.getPoolDataSource();

			getPoolConfiguration(poolDataSource);
			dumpConnectionInfo(poolDataSource);

			return poolDataSource;
		}
		finally
		{
			System.out.println("CustomDataSourceFactory.getObjectInstance(): end");
		}
    }

    protected void getPoolConfiguration (PoolDataSource poolDataSource)
        throws Exception
    {
		DecryptableConfiguration configuration = LiaisonConfigurationFactory.getConfiguration();
		String value = null;
		boolean boolValue;
		Integer intValue = null;

		// Required configuration
		poolDataSource.setURL(getRequiredStringPoolConfig(configuration, DB_URL_PROPERTY));
		poolDataSource.setUser(getRequiredStringPoolConfig(configuration, DB_USER_PROPERTY));

		if (!configuration.containsKey(DB_PASSWORD_PROPERTY))
		{
			throw new Exception("Required Configuration value '" + DB_PASSWORD_PROPERTY + "' not provided");
		}
		String pw = new String(configuration.getDecryptedCharArray(DB_PASSWORD_PROPERTY, false));
		poolDataSource.setPassword(pw);

		poolDataSource.setConnectionFactoryClassName(getRequiredStringPoolConfig(configuration, DB_CONNECTIONFACTORYCLASSNAME_PROPERTY));
		poolDataSource.setMinPoolSize(getRequiredIntPoolConfig(configuration, DB_MINPOOLSIZE_PROPERTY));
		poolDataSource.setMaxPoolSize(getRequiredIntPoolConfig(configuration, DB_MAXPOOLSIZE_PROPERTY));

		// Optional configuration
		intValue = getIntPoolConfig(configuration, DB_INACTIVECONNECTIONTIMEOUT_PROPERTY);
		if (intValue != null)
		{
			poolDataSource.setInactiveConnectionTimeout(intValue);
		}

		boolValue = getBooleanPoolConfig(configuration, DB_VALIDATECONNECTIONONBORROW_PROPERTY);
		if (boolValue) {
			poolDataSource.setValidateConnectionOnBorrow(true);
		} else if (boolValue == false) {
			poolDataSource.setValidateConnectionOnBorrow(false);
		} else {
			throw new Exception("Configuration value '" + DB_VALIDATECONNECTIONONBORROW_PROPERTY
					+ "' has an invalid value, must be '" + TRUE + "' or '" + FALSE + "'");
		}

		value = getStringPoolConfig(configuration, DB_DESCRIPTION_PROPERTY);
		if (value != null)
		{
			poolDataSource.setDescription(value);
		}

		intValue = getIntPoolConfig(configuration, DB_ABANDONEDCONNECTIONTIMEOUT_PROPERTY);
		if (intValue != null)
		{
			poolDataSource.setAbandonedConnectionTimeout(intValue);
		}

		intValue = getIntPoolConfig(configuration, DB_CONNECTIONHARVESTMAXCOUNT_PROPERTY);
		if (intValue != null)
		{
			poolDataSource.setConnectionHarvestMaxCount(intValue);
		}

		intValue = getIntPoolConfig(configuration, DB_CONNECTIONHARVESTTRIGGERCOUNT_PROPERTY);
		if (intValue != null)
		{
			poolDataSource.setConnectionHarvestTriggerCount(intValue);
		}

		intValue = getIntPoolConfig(configuration, DB_CONNECTIONWAITTIMEOUT_PROPERTY);
		if (intValue != null)
		{
			poolDataSource.setConnectionWaitTimeout(intValue);
		}

		boolValue = getBooleanPoolConfig(configuration, DB_FASTCONNECTIONFAILOVERENABLED_PROPERTY);
		if (boolValue) {
			poolDataSource.setFastConnectionFailoverEnabled(true);
		} else if (boolValue == false) {
			poolDataSource.setFastConnectionFailoverEnabled(false);
		} else {
			throw new Exception("Configuration value '" + DB_FASTCONNECTIONFAILOVERENABLED_PROPERTY
					+ "' has an invalid value, must be '" + TRUE + "' or '" + FALSE + "'");
		}

		intValue = getIntPoolConfig(configuration, DB_INITIALPOOLSIZE_PROPERTY);
		if (intValue != null)
		{
			poolDataSource.setInitialPoolSize(intValue);
		}

		intValue = getIntPoolConfig(configuration, DB_INACTIVECONNECTIONTIMEOUT_PROPERTY);
		if (intValue != null)
		{
			poolDataSource.setInactiveConnectionTimeout(intValue);
		}

		intValue = getIntPoolConfig(configuration, DB_MAXCONNECTIONREUSECOUNT_PROPERTY);
		if (intValue != null)
		{
			poolDataSource.setMaxConnectionReuseCount(intValue);
		}

		intValue = getIntPoolConfig(configuration, DB_MAXCONNECTIONREUSETIME_PROPERTY);
		if (intValue != null)
		{
			poolDataSource.setMaxConnectionReuseTime(intValue);
		}

		intValue = getIntPoolConfig(configuration, DB_MAXIDLETIME_PROPERTY);
		if (intValue != null)
		{
			poolDataSource.setMaxIdleTime(intValue);
		}

		intValue = getIntPoolConfig(configuration, DB_TIMEOUTCHECKINTERVAL_PROPERTY);
		if (intValue != null)
		{
			poolDataSource.setTimeoutCheckInterval(intValue);
		}

		intValue = getIntPoolConfig(configuration, DB_TIMETOLIVECONNECTIONTIMEOUT_PROPERTY);
		if (intValue != null)
		{
			poolDataSource.setTimeToLiveConnectionTimeout(intValue);
		}
    }

	protected String getRequiredStringPoolConfig (DecryptableConfiguration configuration, String configurationName)
		throws Exception
	{
		try
		{
			String value = configuration.getString(configurationName);

			if ((value == null) || (value.trim().length() == 0))
			{
				throw new Exception("Required Configuration value '" + configurationName + "' not provided");
			}

			return value;
		}
		catch (ConversionException e)
		{
			throw new Exception("Required Configuration value '" + configurationName + "' has an invalid format", e);
		}
	}

	protected Integer getRequiredIntPoolConfig (DecryptableConfiguration configuration, String configurationName)
		throws Exception
	{
		try
		{
			if (configuration.containsKey(configurationName))
			{
				int value = configuration.getInt(configurationName);

				if (value == 0)
				{
					throw new Exception("Required Configuration value '" + configurationName + "' has invalid value");
				}

				return value;
			}
			else
			{
				throw new Exception("Required Configuration value '" + configurationName + "' not provided");
			}
		}
		catch (ConversionException e)
		{
			throw new Exception("Required Configuration value '" + configurationName + "' has an invalid format", e);
		}
	}

	protected String getStringPoolConfig (DecryptableConfiguration configuration, String configurationName)
		throws Exception
	{
		try
		{
			String value = configuration.getString(configurationName);
			return value;
		}
		catch (ConversionException e)
		{
			throw new Exception("Required Configuration value '" + configurationName + "' has an invalid format", e);
		}
	}

	protected boolean getBooleanPoolConfig(DecryptableConfiguration configuration, String configurationName)
			throws Exception {

		try {

			boolean value = configuration.getBoolean(configurationName, false);
			return value;
		} catch (ConversionException e) {
			throw new Exception("Required Configuration value '" + configurationName + "' has an invalid format", e);
		}

	}
	
	protected Integer getIntPoolConfig (DecryptableConfiguration configuration, String configurationName)
		throws Exception
	{
		try
		{
			if (!configuration.containsKey(configurationName))
			{
				return null;
			}

			int value = configuration.getInt(configurationName);
			return value;
		}
		catch (ConversionException e)
		{
			throw new Exception("Required Configuration value '" + configurationName + "' has an invalid format", e);
		}
	}

	protected void dumpConnectionInfo (PoolDataSource poolDataSource)
	{
		System.out.println("Creating PoolDataSource with the following values:");
		System.out.println("    User:                     '" + poolDataSource.getUser() + "'");
		System.out.println("    Password:                 '********' (hidden)");
		System.out.println("    URL:                      '" + poolDataSource.getURL() + "'");
		System.out.println("    Connection Factory Class: '" + poolDataSource.getConnectionFactoryClassName() + "'");
	}

}
