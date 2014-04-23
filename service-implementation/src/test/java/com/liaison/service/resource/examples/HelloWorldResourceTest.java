/** * Copyright Liaison Technologies, Inc. All rights reserved. * * This software is the confidential and proprietary information of * Liaison Technologies, Inc. ("Confidential Information").  You shall  * not disclose such Confidential Information and shall use it only in * accordance with the terms of the license agreement you entered into * with Liaison Technologies. */package com.liaison.service.resource.examples;import org.apache.http.HttpResponse;import org.apache.http.client.HttpClient;import org.apache.http.client.methods.HttpGet;import org.apache.http.conn.HttpHostConnectException;import org.apache.http.impl.client.DefaultHttpClient;import org.testng.Assert;import org.testng.annotations.Test;
public class HelloWorldResourceTest {
	
    
	public static final int LISTEN_PORT_DEFAULT = 8989;
    private static final int httpRetries = 10;
    private static final long sleepTimeout = 1000;
    private static final String helloEndPoint  = "/hello-world/rest/v1/hello/to/me";

        /**     * Method to test response.     *      * @throws Exception     */
    @Test
    public void testHelloWorld() throws Exception {
    	
    		HttpClient client = new DefaultHttpClient();
	   		HttpGet helloWorld = new HttpGet("http://localhost:" + LISTEN_PORT_DEFAULT + helloEndPoint);	   		
	    	HttpResponse response = testPing(client, helloWorld, httpRetries);
	    	Assert.assertEquals( 200, response.getStatusLine().getStatusCode());
    	
    }    
   
    /**     * Method to test ping with urlpage and client.     *      * @param client     * @param urlPage     * @param retries     * @return HttpResponse     * @throws Exception     */
    @Test	protected HttpResponse testPing(HttpClient client, HttpGet urlPage, int retries) throws Exception {
    	if (retries < 0) {
            throw new Exception("Failed to connect. Retries exceeded.");
        }
    	HttpResponse response = null;
        try {
            Thread.sleep(sleepTimeout); 
            response = client.execute(urlPage);
        } catch (HttpHostConnectException e) {
            try {
                response = client.execute(urlPage);
            } catch (HttpHostConnectException e2) {
                response = testPing(client, urlPage, --retries);
            }
        }
        return response;
    }
       
    
}