/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.integration.test;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.script.ScriptException;

import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.dtdm.model.MailBox;
import com.liaison.mailbox.dtdm.model.Processor;
import com.liaison.mailbox.service.base.test.BaseServiceTest;
import com.liaison.mailbox.service.core.processor.ConditionalSweeper;
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
public class JavaScriptEngineUtilIT extends BaseServiceTest {

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

        MailBox mailbox = new MailBox();
        mailbox.setTenancyKey("TestNG");

        Processor processor = new Processor();
        processor.setMailbox(mailbox);
        JavaScriptExecutorUtil.executeJavaScript(myUri, new HTTPRemoteUploader(processor));
    }

    /**
     * Method to test sweeper script.
     */
    @Test
    public void testExecuteJavaScriptSweeper() {

        String testJs = "sweeper_unit_test.js";
        double returnValue = (double) JavaScriptExecutorUtil.executeJavaScript(testJs, "process", 4, 5);
        Assert.assertEquals(returnValue, 20d);
    }
    
    /**
     * Method to test conditional sweeper filter script.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testExecuteJavaScriptConditionalSweeper() {

        MailBox mailbox = new MailBox();
        mailbox.setTenancyKey("TestNG");

        Processor processor = new Processor();
        processor.setMailbox(mailbox);

        List<Path> result = new ArrayList<>();

        for (int i = 1 ; i <= 5; i++) {
            result.add(Paths.get("Saabfile"+ i +".txt"));
            result.add(Paths.get("Volvofile"+ i +".txt"));
            result.add(Paths.get("BMWfile"+ i +".txt"));
        }

        String testJs = "conditinal_sweeper_unit_test.js";
        List<List<Path>> returnValue = (List<List<Path>>) JavaScriptExecutorUtil.executeJavaScript(testJs, MailBoxConstants.FILTER, new ConditionalSweeper(processor), result);

        Assert.assertEquals(returnValue.size(), 3);
    }

    /**
     * Method to test conditional sweeper filter script with invalid method.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testExecuteJavaScriptConditionalSweeperInvalidMethod() {

        MailBox mailbox = new MailBox();
        mailbox.setTenancyKey("TestNG");

        Processor processor = new Processor();
        processor.setMailbox(mailbox);

        List<Path> result = new ArrayList<>();

        for (int i = 1 ; i <= 5; i++) {
            result.add(Paths.get("Saabfile"+ i +".txt"));
            result.add(Paths.get("Volvofile"+ i +".txt"));
            result.add(Paths.get("BMWfile"+ i +".txt"));
        }

        String testJs = "conditinal_sweeper_unit_test.js";
        try {
            List<List<Path>> returnValue = (List<List<Path>>) JavaScriptExecutorUtil.executeJavaScript(testJs, "processInvalid", new ConditionalSweeper(processor), result);
        } catch (Exception e) {
            if (e.getMessage().contains(String.format("Script '%s' is missing required function '%s'.  ", testJs, "processInvalid"))) {
                Assert.assertTrue(true);
            } else {
                Assert.assertTrue(false);
            }
        }
    }
    
    /**
     * Method to test conditional sweeper filter error script.
     */
    @Test(expectedExceptions = RuntimeException.class)
    public void testExecuteJavaScriptConditionalSweeperWithFailureScript() {

        MailBox mailbox = new MailBox();
        mailbox.setTenancyKey("TestNG");

        Processor processor = new Processor();
        processor.setMailbox(mailbox);

        List<Path> result = new ArrayList<>();

        for (int i = 1 ; i <= 5; i++) {
            result.add(Paths.get("Saabfile"+ i +".txt"));
            result.add(Paths.get("Volvofile"+ i +".txt"));
            result.add(Paths.get("BMWfile"+ i +".txt"));
        }

        String testJs = "conditinal_sweeper_error_script_unit_test.js";
        JavaScriptExecutorUtil.executeJavaScript(testJs, MailBoxConstants.FILTER, new ConditionalSweeper(processor), result);
    }
    
    /**
     * Method to test conditional sweeper filter script failure.
     */
    @Test
    public void testExecuteJavaScriptConditionalSweeperScriptFailure() {

        MailBox mailbox = new MailBox();
        mailbox.setTenancyKey("TestNG");

        Processor processor = new Processor();
        processor.setMailbox(mailbox);

        List<Path> result = new ArrayList<>();

        for (int i = 1 ; i <= 5; i++) {
            result.add(Paths.get("Saabfile"+ i +".txt"));
            result.add(Paths.get("Volvofile"+ i +".txt"));
            result.add(Paths.get("BMWfile"+ i +".txt"));
        }

        try {
            String testJs = "conditinal_sweeper_error_script_unit_test.js";
            JavaScriptExecutorUtil.executeJavaScript(testJs, MailBoxConstants.FILTER, new ConditionalSweeper(processor), result);
        } catch (Exception e) {
            if (e.getMessage().contains("javax.script.ScriptException")) {
                Assert.assertTrue(true);
            } else {
                Assert.assertTrue(false);
            }
        }
    }

    @Test
    public void testExecuteJavaScriptFailure() {

        String testJs = "gitlab:/processor-scripts/sample_unit_test_failure.js";
        URI myUri = null;
        try {
            myUri = new URI(testJs);
        } catch (URISyntaxException e) {
            Assert.assertTrue(false);
        }

        try {

            MailBox mailbox = new MailBox();
            mailbox.setTenancyKey("TestNG");

            Processor processor = new Processor();
            processor.setMailbox(mailbox);

            JavaScriptExecutorUtil.executeJavaScript(myUri, new HTTPRemoteUploader(processor));
        } catch (Exception e) {
            if (e.getMessage().contains("StackOverflowError")) {
                Assert.assertTrue(true);
            } else {
                Assert.assertTrue(false);
            }
        }
    }
}
