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

import com.liaison.commons.scripting.javascript.ScriptExecutionEnvironment;
import com.liaison.mailbox.dtdm.model.MailBox;
import com.liaison.mailbox.service.base.test.BaseServiceTest;
import com.liaison.mailbox.service.core.processor.AbstractProcessor;
import com.liaison.mailbox.service.core.processor.SFTPRemoteDownloader;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.liaison.mailbox.dtdm.model.Processor;
import com.liaison.mailbox.service.core.processor.HTTPRemoteDownloader;
import com.liaison.mailbox.service.core.processor.ProcessorJavascriptI;
import com.liaison.mailbox.service.executor.javascript.JavaScriptExecutorUtil;

/**
 * Unit tests for Javascript Executor
 *
 * @author VNagarajan
 *
 */
public class JavascriptExecutorIT extends BaseServiceTest {

	public ProcessorJavascriptI processorService;

	@BeforeClass
	public void createProcessor() {

		MailBox mailbox = new MailBox();
		mailbox.setTenancyKey("JUNIT");

		Processor processor = new Processor();
		processor.setMailbox(mailbox);

		processorService = new SFTPRemoteDownloader(processor);
	}

	@Test
	public void testExecutorWithNashorn() throws URISyntaxException {

		String scriptRelativePath = "processor-scripts/veera/sample_script.ns";
		URI scriptUri = new URI("gitlab:/" + scriptRelativePath);
		JavaScriptExecutorUtil.executeJavaScript(scriptUri, processorService);
	}
	
	@Test
	public void testExecutor() throws URISyntaxException {

		String scriptRelativePath = "processor-scripts/veera/noop.js";
		URI scriptUri = new URI("gitlab:/" + scriptRelativePath);
		JavaScriptExecutorUtil.executeJavaScript(scriptUri, processorService);
	}

	@Test(expectedExceptions = java.lang.RuntimeException.class, enabled = false)
	public void testExecutor_MissingRequiredFunction_FailureWithJavaRuntimeException() throws URISyntaxException {

		String scriptRelativePath = "processor-scripts/veera/invalidfunction.js";
		URI scriptUri = new URI("gitlab:/" + scriptRelativePath);
		JavaScriptExecutorUtil.executeJavaScript(scriptUri, processorService);

	}

	@Test(expectedExceptions = java.lang.RuntimeException.class)
	public void testExecutor_InvalidScriptURI_FailureWithJavaRuntimeException() throws URISyntaxException {

		String scriptRelativePath = "processor-scripts/veeras/invalidfunction.js";
		URI scriptUri = new URI("gitlab:/" + scriptRelativePath);
		JavaScriptExecutorUtil.executeJavaScript(scriptUri, processorService);

	}

    @Test(expectedExceptions = java.lang.RuntimeException.class, enabled = false)
    public void testExecutor_Timeout() throws URISyntaxException {

		((AbstractProcessor) processorService).setMaxExecutionTimeout(1);
        String scriptRelativePath = "processor-scripts/veera/timeout_test.js";
        URI scriptUri = new URI("gitlab:/" + scriptRelativePath);
        JavaScriptExecutorUtil.executeJavaScript(scriptUri, processorService);

    }

	@Test
	public void testExecutor_Stacktrace() throws URISyntaxException {

		((AbstractProcessor) processorService).setMaxExecutionTimeout(1);
		String scriptRelativePath = "processor-scripts/veera/handle_java_exception.ns";
		URI scriptUri = new URI("gitlab:/" + scriptRelativePath);
		JavaScriptExecutorUtil.executeJavaScript(scriptUri, processorService);

	}

}
