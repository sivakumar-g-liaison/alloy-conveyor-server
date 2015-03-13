/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.executor.javascript.unit.test;

import java.net.URI;
import java.net.URISyntaxException;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.liaison.mailbox.dtdm.model.Processor;
import com.liaison.mailbox.service.core.processor.HttpRemoteDownloader;
import com.liaison.mailbox.service.core.processor.ProcessorJavascriptI;
import com.liaison.mailbox.service.executor.javascript.JavaScriptExecutorUtil;

/**
 * Unit tests for Javascript Executor
 *
 * @author VNagarajan
 *
 */
public class JavascriptExecutorTest {

	public ProcessorJavascriptI processorService;

	@BeforeClass
	public void createProcessor() {
		processorService = new HttpRemoteDownloader(new Processor());
	}

	@Test
	public void testExecutor() throws URISyntaxException {

		System.setProperty("archaius.deployment.applicationId", "scripting");
		System.setProperty("archaius.deployment.environment", "test");

		String scriptRelativePath = "processor-scripts/veera/noop.js";
		URI scriptUri = new URI("gitlab:/" + scriptRelativePath);

		JavaScriptExecutorUtil.executeJavaScript(scriptUri, processorService);
	}

	//java.lang.RuntimeException: Script 'gitlab:/processor-scripts/veera/invalidfunction.js' is invalid: Script 'gitlab:/processor-scripts/veera/invalidfunction.js' is missing required function 'process'.
	@Test(expectedExceptions = java.lang.RuntimeException.class)
	public void testExecutor_MissingRequiredFunction_FailureWithJavaRuntimeException() throws URISyntaxException {

		System.setProperty("archaius.deployment.applicationId", "scripting");
		System.setProperty("archaius.deployment.environment", "test");

		String scriptRelativePath = "processor-scripts/veera/invalidfunction.js";
		URI scriptUri = new URI("gitlab:/" + scriptRelativePath);

		JavaScriptExecutorUtil.executeJavaScript(scriptUri, processorService);

	}

	//java.lang.RuntimeException: After evaluating script 'gitlab:/processor-scripts/veera/invalid.js', script engine is null. There must have been an error evaluating the script.
	@Test(expectedExceptions = java.lang.RuntimeException.class)
	public void testExecutor_InvalidScriptURI_FailureWithJavaRuntimeException() throws URISyntaxException {

		System.setProperty("archaius.deployment.applicationId", "scripting");
		System.setProperty("archaius.deployment.environment", "test");

		String scriptRelativePath = "processor-scripts/veera/invalid.js";
		URI scriptUri = new URI("gitlab:/" + scriptRelativePath);

		JavaScriptExecutorUtil.executeJavaScript(scriptUri, processorService);

	}

}
