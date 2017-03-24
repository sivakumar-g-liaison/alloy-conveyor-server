/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.performance.test;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.lang.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.liaison.mailbox.service.base.test.Parallel;
import com.liaison.mailbox.service.util.UserAuthCacheUtil;

/**
 * Test class for user authentication cache.
 * 
 * @author OFS
 */
public class UserAuthCacheUtilIT {

	private static final Logger logger = LogManager.getLogger(UserAuthCacheUtilIT.class);
	private static final String token = "Basic bmVlbGltYS5kb2RsYUBsaWFpc29uLmRldjpEZW5tYXJrMzMh";
	
    /**
     * @throws Exception
     */
    @BeforeMethod
    public void setUp() throws Exception {
        System.setProperty("com.liaison.secure.properties.path", "invalid");
        System.setProperty("archaius.deployment.applicationId", "g2mailboxservice");
        System.setProperty("archaius.deployment.environment", "dev-int");
    }
    
    @Test(enabled = false)
    public void testAuthenticate() throws InterruptedException, ExecutionException {
    	
    	int threadCount = 100;
    	
    	Callable<String> addition = new Callable<String>() {
            public String call() {
                return UserAuthCacheUtil.authenticate(token);
            }
        };
        
        List<Callable<String>> tasks = Collections.nCopies(threadCount, addition);
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        List<Future<String>> futures = executorService.invokeAll(tasks);
        
        for (Future<String> future : futures) {
        	String res  = future.get();
        	logger.debug(res);
            Assert.assertNotNull(res);
        }
    }
    
    @Test(enabled = false)
    public void testAuthenticateExecutionTime() {
    	
        Parallel.For(0, 1000, new Parallel.Action<Long>() {
            @Override
            public void doAction(Long element) {
                try {

                    StopWatch totalExecutionTime = new StopWatch();
                    totalExecutionTime.start();
                    String response = UserAuthCacheUtil.authenticate(token);
                    logger.debug(response);
                    totalExecutionTime.stop();
                    logger.info("TOTAL EXECUTION TIME : " + totalExecutionTime.getTime());
                } catch (Exception up) {
                    logger.error(up);
                    Assert.fail();
                }
            }
        });
    }
}
