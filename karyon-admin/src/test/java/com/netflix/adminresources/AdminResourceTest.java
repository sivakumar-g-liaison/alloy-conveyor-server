/*
 * Copyright 2013 Netflix, Inc.
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 */

package com.netflix.adminresources;

import com.google.inject.Injector;
import com.netflix.config.ConfigurationManager;
import com.netflix.karyon.server.KaryonServer;
import com.netflix.karyon.server.eureka.SyncHealthCheckInvocationStrategy;
import com.netflix.karyon.spi.PropertyNames;
//import junit.framework.Assert;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.DefaultHttpClient;
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;



/**
 * @author Nitesh Kant
 */
public class AdminResourceTest {

    public static final String CUSTOM_LISTEN_PORT = "9999";
	public static final String CONTAINER_LISTEN_PORT = "netflix.platform.admin.resources.port";
    public static final int LISTEN_PORT_DEFAULT = 8077;
    private KaryonServer server;
    private static final int httpRetries = 10;
    private static final long sleepTimeout = 1000;

    @BeforeClass
    public void setUp() throws Exception {
        System.setProperty(PropertyNames.SERVER_BOOTSTRAP_BASE_PACKAGES_OVERRIDE, "com.test");
        System.setProperty(PropertyNames.HEALTH_CHECK_TIMEOUT_MILLIS, "60000");
        System.setProperty(PropertyNames.HEALTH_CHECK_STRATEGY, SyncHealthCheckInvocationStrategy.class.getName());
        System.setProperty(PropertyNames.DISABLE_EUREKA_INTEGRATION, "true");
    }

    @org.testng.annotations.AfterClass
    public void tearDown() throws Exception {
        ConfigurationManager.getConfigInstance().clearProperty(PropertyNames.DISABLE_APPLICATION_DISCOVERY_PROP_NAME);
        ConfigurationManager.getConfigInstance().clearProperty(PropertyNames.EXPLICIT_APPLICATION_CLASS_PROP_NAME);
        server.close();
    }

    @Test
    public void testBasic() throws Exception {
    	try {
    		HttpClient client = new DefaultHttpClient();
    		ConfigurationManager.getConfigInstance().setProperty(CONTAINER_LISTEN_PORT, LISTEN_PORT_DEFAULT);
    		HttpGet healthGet = new HttpGet("http://localhost:" + LISTEN_PORT_DEFAULT + "/healthcheck");
    		startServer();
	    	HttpResponse response = doBasicTestHack(client, healthGet, httpRetries);
	    	Assert.assertEquals( 200, response.getStatusLine().getStatusCode());
	    	//Assert.assertEquals( "admin resource health check failed.",200, response.getStatusLine().getStatusCode());
    	} finally {
	    	if (server != null) {
	    		server.close();
	    	}
    	}
    }

    // HACK! to get around the fact that startServer() does not wait until the server is up
    protected HttpResponse doBasicTestHack(HttpClient client, HttpGet healthGet, int retries) throws Exception {
    	if (retries < 0) {
            throw new Exception("Failed to connect. Retries exceeded.");
        }
    	HttpResponse response = null;
        try {
            Thread.sleep(sleepTimeout); 
            response = client.execute(healthGet);
        } catch (HttpHostConnectException e) {
            try {
                response = client.execute(healthGet);
            } catch (HttpHostConnectException e2) {
                response = doBasicTestHack(client, healthGet, --retries);
            }
        }
        return response;
    }

 private Injector startServer() throws Exception {
        server = new KaryonServer();
        Injector injector = server.initialize();
        server.start();
        return injector;
    }
}
