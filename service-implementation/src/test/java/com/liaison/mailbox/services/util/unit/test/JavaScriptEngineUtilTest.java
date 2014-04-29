/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall 
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.services.util.unit.test;

import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.liaison.commons.scripting.javascript.JavascriptExecutor;
import com.liaison.commons.scripting.javascript.JavascriptScriptContext;

/**
 * 
 * @author OFS
 *
 */
public class JavaScriptEngineUtilTest {
	
	/**
	 * Method to test execute JavaScript.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testExecuteJavaScript() {
		
		System.setProperty("archaius.deployment.applicationId", "g2mailboxservice");
		System.setProperty("archaius.deployment.environment", "ci");
		
		JavascriptExecutor scriptExecutor = new JavascriptExecutor();
		JavascriptScriptContext scriptContext = null;
		String testJs = "gitlab:/src/test/resources/sandbox-tests.js";
		URI myUri = null;
		try {
			myUri = new URI(testJs);
		} catch (URISyntaxException e) {
		Assert.assertTrue(false);
		}
		
		
		 if (scriptContext == null) {
		     
			 scriptContext = new JavascriptScriptContext(new InputStreamReader(System.in), new PrintWriter(System.out), new PrintWriter(System.err));
		 }
	    scriptExecutor.setScriptContext(scriptContext);
	    
	    Object returnValue = scriptExecutor.executeInContext(scriptContext, "sandbox-tests.js", myUri, "testHappyPathWithArgs", "Test");
        System.out.println("returnValue "+returnValue);
	    // did my function call throw?
	    Exception expectedException = ((Map<String, Exception>)scriptContext.getAttribute(JavascriptExecutor.SCRIPT_EXCEPTIONS)).get("sandbox-tests.js" + ":" + "testHappyPathWithArgs");
	    if (null != expectedException) {
	    	expectedException.printStackTrace();
	    	Assert.assertTrue(false);
	    }
	   
	}

}
