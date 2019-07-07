/**
 * Copyright 2014 Liaison Technologies, Inc.
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.mailbox.service.executor.javascript;

import com.liaison.commons.scripting.javascript.JavascriptExecutor;
import com.liaison.commons.scripting.javascript.JavascriptFunction;
import com.liaison.commons.scripting.javascript.JavascriptScriptContext;
import com.liaison.mailbox.service.core.processor.ProcessorJavascriptI;
import com.liaison.mailbox.service.util.MailBoxUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.util.Map;

import static com.liaison.mailbox.MailBoxConstants.PROPERTY_GITLAB_ACTIVITY_SERVER_FOLDER;

/**
 * This class load the javascript content from various protocol and execute the javascript content in Java ScriptEngine.
 *
 * @author OFS
 *
 */
public final class JavaScriptExecutorUtil {

	private static final Logger LOGGER = LogManager.getLogger(JavaScriptExecutorUtil.class);

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
    public static Object executeJavaScript(String scriptPath, String methodName, Object... parameters) {

        ContextCreator contextCreator = new ContextCreator(scriptPath);
        JavascriptScriptContext scriptContext = contextCreator.getJavascriptContext();
        JavascriptExecutor javascriptExecutor = new JavascriptExecutor();

        URI scriptUri = null;

        try {
            String scriptName = scriptPath;
            String gitlabDirectory = (String) MailBoxUtil.getEnvironmentProperties().getProperty(
                    PROPERTY_GITLAB_ACTIVITY_SERVER_FOLDER);
            scriptPath = gitlabDirectory + "/" + scriptPath;

            if (scriptPath.contains("gitlab:")) {
                scriptUri = new URI(scriptPath);
                LOGGER.debug("The process script uri is {}", scriptUri);
            }

            Object returnValue = javascriptExecutor.executeInContext(
                    scriptContext,
                    scriptPath,

                    new JavascriptFunction((obj) -> {
                        JavascriptValidator validator = new JavascriptValidator(scriptName, scriptContext, methodName);
                        if (!validator.isValidScript()) {
                            throw new RuntimeException(String.format("Script '%s' is invalid: %s", scriptName, validator.getErrorMessage()));
                        }

                        return null;
                    }, scriptUri, null),
                    new JavascriptFunction(scriptUri, methodName, parameters));

            Map<String, Exception> exceptionMap = ((Map<String, Exception>) scriptContext.getAttribute(JavascriptExecutor.SCRIPT_EXCEPTIONS));
            Exception expectedException = exceptionMap.get(scriptName + ":" + methodName);
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
					  PROPERTY_GITLAB_ACTIVITY_SERVER_FOLDER);
			scriptPath = gitlabDirectory+"/"+scriptPath;

			if (scriptPath.contains("gitlab:")) {
				scriptUri = new URI(scriptPath);
			}

		    JavascriptExecutorService exec = new JavascriptExecutorService(scriptUri.toString(), processorService);
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

	/**
	 * Executes the specified method in the javascript available
	 *  in the script uri provided using G2 custom Js engine.
	 *
	 * @Param parameters Object
	 * @return Object
	 * @throws Exception
	 *
	 */
	public static Object executeJavaScript(URI scriptUri, ProcessorJavascriptI processorService) {

		JavascriptScriptContext scriptContext = null;

		try {

		    JavascriptExecutorService exec = new JavascriptExecutorService(scriptUri.toString(), processorService);
		    scriptContext = exec.call();

		    // did my function call throw?
		    Map<String, Exception> exceptionMap = ((Map<String, Exception>) scriptContext.getAttribute(JavascriptExecutor.SCRIPT_EXCEPTIONS));
		    Exception expectedException = exceptionMap.get(scriptUri.toString() + ":" + JavascriptValidator.PROCESS_FUNCTION_NAME);
		    if (null != expectedException) {
		       	throw expectedException;
		    }

		    return scriptContext;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
