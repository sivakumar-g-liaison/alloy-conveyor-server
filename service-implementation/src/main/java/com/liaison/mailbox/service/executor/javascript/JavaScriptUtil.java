/**
 * Copyright 2014 Liaison Technologies, Inc.
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.mailbox.service.executor.javascript;

import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URI;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.commons.scripting.javascript.JavascriptExecutor;
import com.liaison.commons.scripting.javascript.JavascriptScriptContext;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.service.core.processor.ProcessorJavascriptI;
import com.liaison.mailbox.service.util.MailBoxUtil;

/**
 * This class load the javascript content from various protocol and execute the javascript content in Java ScriptEngine.
 *
 * @author OFS
 *
 */
public final class JavaScriptUtil {

	//TODO use logging
	private static final Logger LOGGER = LogManager.getLogger(JavaScriptUtil.class);

	/**
	 * Executes the specified method in the javascript available
	 *  in the script path provided using G2 custom Js engine.
	 *
	 * @param scriptPath String
	 * @param methodName String
	 * @Param parameters Object
	 * @return Object
	 * @throws Exception
	 *
	 */
	public static Object executeJavaScript(String scriptPath, String methodName,  Object... parameters) {

		Exception expectedException = null;
		JavascriptExecutor scriptExecutor = new JavascriptExecutor();
		JavascriptScriptContext scriptContext = null;
		URI myUri = null;

		try {

			String scriptName = scriptPath;

			String gitlabDirectory = (String) MailBoxUtil.getEnvironmentProperties().getProperty(
					  MailBoxConstants.PROPERTY_GITLAB_ACTIVITY_SERVER_FOLDER );
			scriptPath = gitlabDirectory+"/"+scriptPath;

			if(scriptPath.contains("gitlab:")) {
				myUri = new URI(scriptPath);
			}

			if (scriptContext == null) {

			    try (InputStreamReader reader = new InputStreamReader(System.in); PrintWriter outputWriter = new PrintWriter(System.out);
			            PrintWriter errorWriter = new PrintWriter(System.err)) {
			        scriptContext = new JavascriptScriptContext(reader, outputWriter, errorWriter);
			    }

			}
		    scriptExecutor.setScriptContext(scriptContext);

		    Object returnValue = scriptExecutor.executeInContext(scriptContext, scriptName, myUri, methodName, parameters);

		    // did my function call throw?
		    expectedException = ((Map<String, Exception>)scriptContext.getAttribute(JavascriptExecutor.SCRIPT_EXCEPTIONS)).get(scriptName + ":" + methodName);
		    if (null != expectedException) {
		       	throw expectedException;
		    }

		    return returnValue;

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Executes the specified method in the javascript available
	 *  in the script path provided using G2 custom Js engine.
	 *
	 * @param scriptPath String
	 * @param methodName String
	 * @Param parameters Object
	 * @return Object
	 * @throws Exception
	 *
	 */
	public static Object executeJavaScript(String scriptPath, ProcessorJavascriptI processorService) {

		JavascriptScriptContext scriptContext = null;
		URI scriptUri = null;
		String scriptName = scriptPath;

		try {

			String gitlabDirectory = (String) MailBoxUtil.getEnvironmentProperties().getProperty(
					  MailBoxConstants.PROPERTY_GITLAB_ACTIVITY_SERVER_FOLDER );
			scriptPath = gitlabDirectory+"/"+scriptPath;

			if (scriptPath.contains("gitlab:")) {
				scriptUri = new URI(scriptPath);
			}

		    com.liaison.mailbox.service.executor.javascript.JavascriptExecutor exec = new com.liaison.mailbox.service.executor.javascript.JavascriptExecutor(scriptUri.toString(), processorService);
		    scriptContext = exec.call();

		    // did my function call throw?
		    Map<String, Exception> exceptionMap = ((Map<String, Exception>) scriptContext.getAttribute(JavascriptExecutor.SCRIPT_EXCEPTIONS));
		    Exception expectedException = exceptionMap.get(scriptName + ":" + JavascriptValidator.PROCESS_FUNCTION_NAME);
		    if (null != expectedException) {
		       	throw expectedException;
		    }

		    return scriptContext;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}

