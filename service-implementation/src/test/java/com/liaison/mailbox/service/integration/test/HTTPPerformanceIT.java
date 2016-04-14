/**
 * Copyright 2016 Liaison Technologies, Inc.
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.mailbox.service.integration.test;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.liaison.commons.util.client.http.HTTPRequest;
import com.liaison.commons.util.client.http.HTTPRequest.HTTP_METHOD;
import com.liaison.commons.util.client.http.HTTPResponse;
import com.liaison.commons.util.client.http.HTTPStringData;
import com.liaison.mailbox.service.base.test.BaseServiceTest;

/**
 * @author VNagarajan
 *
 */
public class HTTPPerformanceIT extends BaseServiceTest {

    private static final Logger LOG = LogManager.getLogger(HTTPPerformanceIT.class);

    // Auth HEADER information
    private static final String HTTP_HEADER_BASIC_AUTH = "Authorization";
    private static final String AUTH_CREDENTIALS = "dropbox@liaison.dev:61efc8114f#j!3#t2!$i91c60dc8";
    private static final String AUTH_HEADER = "Basic " + Base64.encodeBase64String(AUTH_CREDENTIALS.getBytes());

    // sync and async url
    private static final String SYNC_URL = "http://at4d-vpmbox.liaison.dev/g2mailboxservice/process/sync?mailboxId=37FFD6A75C0E40979A7784D1D79AD5D3";
    private static final String ASYNC_URL = "http://at4d-vpmbox.liaison.dev/g2mailboxservice/process/async?mailboxId=C612109F65AE42E5A85604E76B7F78FF";
    
    @Test(enabled = false)
    public void testSync() {

        Parallel.For(0, 1, new Parallel.Action<Long>() {
            @Override
            public void doAction(Long element) {

                HTTPRequest request;
                HTTPResponse response;
                StopWatch totalExecutionTime = new StopWatch();

                try {

                    totalExecutionTime.start();

                    request = constructHTTPRequest(SYNC_URL, HTTP_METHOD.POST, null, LOG);
                    request.addHeader(HTTP_HEADER_BASIC_AUTH, AUTH_HEADER);
                    request.setInputData(new HTTPStringData("Test Data"));
                    response = request.execute();
                    Assert.assertEquals("OK", response.getReasonPhrease());
                    totalExecutionTime.stop();
                    System.out.println("SYNC - TOTAL EXECUTION TIME : " + totalExecutionTime);

                } catch (Exception up) {
                    LOG.error(up.getMessage());
                }
            }
        });

    }

    @Test(enabled = false)
    public void testAsync() {

        Parallel.For(0, 1, new Parallel.Action<Long>() {
            @Override
            public void doAction(Long element) {

                HTTPRequest request;
                HTTPResponse response;
                StopWatch totalExecutionTime = new StopWatch();

                try {

                    totalExecutionTime.start();

                    request = constructHTTPRequest(ASYNC_URL, HTTP_METHOD.POST, null, LOG);
                    request.addHeader(HTTP_HEADER_BASIC_AUTH, AUTH_HEADER);
                    request.setInputData(new HTTPStringData("Test Data"));
                    response = request.execute();
                    Assert.assertEquals("Accepted", response.getReasonPhrease());
                    totalExecutionTime.stop();
                    System.out.println("ASYNC- TOTAL EXECUTION TIME : " + totalExecutionTime);

                } catch (Exception up) {
                    LOG.error(up.getMessage());
                }
            }
        });

    }
}
