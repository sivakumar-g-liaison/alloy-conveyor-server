/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall 
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.integration.test;

import java.net.URI;
import java.net.URISyntaxException;

import javax.script.ScriptException;

import com.liaison.mailbox.dtdm.model.Processor;
import com.liaison.mailbox.service.core.processor.HTTPRemoteUploader;
import com.liaison.mailbox.service.executor.javascript.JavaScriptExecutorUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * 
 * @author OFS
 *
 */
public class JavaScriptEngineUtilIT {
	
	private static final Logger logger = LogManager.getLogger(JavaScriptEngineUtilIT.class);
	
	/**
	 * Method to test execute JavaScript.
	 * @throws ScriptException 
	 * 
	 * @throws Exception
	 */
	@Test
	public void testExecuteJavaScript() {
		
		String testJs = "gitlab:/processor-scripts/sample_unit_test.js";
		URI myUri = null;
		try {
			myUri = new URI(testJs);
		} catch (URISyntaxException e) {
			Assert.assertTrue(false);
		}

		JavaScriptExecutorUtil.executeJavaScript(myUri, new HTTPRemoteUploader(new Processor()));
	}

	@Test
	public void testExecuteJavaScriptSweeper() {

		String testJs = "gitlab:/processor-scripts/sweeper_unit_test.js";
		Object returnValue = JavaScriptExecutorUtil.executeJavaScript(testJs, "process", 4, 5);
		Assert.assertEquals(returnValue, 20);
	}

    @Test
    public void testExecuteJavaScripteFailure() {

        String testJs = "gitlab:/processor-scripts/sample_unit_test_failure.js";
        URI myUri = null;
        try {
            myUri = new URI(testJs);
        } catch (URISyntaxException e) {
            Assert.assertTrue(false);
        }

        try {
            JavaScriptExecutorUtil.executeJavaScript(myUri, new HTTPRemoteUploader(new Processor()));
        } catch (Exception e) {
            if (e.getMessage().contains("StackOverflowError")) {
                Assert.assertTrue(true);
            } else {
                Assert.assertTrue(false);
            }
        }
    }
}
