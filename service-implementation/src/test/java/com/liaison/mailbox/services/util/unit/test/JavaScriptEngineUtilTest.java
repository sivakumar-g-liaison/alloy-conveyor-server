package com.liaison.mailbox.services.util.unit.test;

import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URI;
import java.util.Map;

import org.testng.annotations.Test;

import com.liaison.commons.scripting.javascript.JavascriptExecutor;
import com.liaison.commons.scripting.javascript.JavascriptScriptContext;

public class JavaScriptEngineUtilTest {
	
	@Test
	public void testExecuteJavaScript() throws Exception{
		
		System.setProperty("archaius.deployment.applicationId", "g2mailboxservice");
		System.setProperty("archaius.deployment.environment", "dev");
		
		JavascriptExecutor scriptExecutor = new JavascriptExecutor();
		JavascriptScriptContext scriptContext = null;
		String testJs = "gitlab:/src/test/resources/sandbox-tests.js";
		URI myUri = new URI(testJs);
		String scriptName = null;
		
		 if (scriptContext == null) {
		     
			 scriptContext = new JavascriptScriptContext(new InputStreamReader(System.in), new PrintWriter(System.out), new PrintWriter(System.err));
		 }
	    scriptExecutor.setScriptContext(scriptContext);
	    
	    Object returnValue = scriptExecutor.executeInContext(scriptContext, "sandbox-tests.js", myUri, "testHappyPath", null);

	    // did my function call throw?
	    Exception expectedException = ((Map<String, Exception>)scriptContext.getAttribute(JavascriptExecutor.SCRIPT_EXCEPTIONS)).get("sandbox-tests.js" + ":" + "testHappyPath");
	    if (null != expectedException) {
	       	throw expectedException;
	    }
	   
	}

}
