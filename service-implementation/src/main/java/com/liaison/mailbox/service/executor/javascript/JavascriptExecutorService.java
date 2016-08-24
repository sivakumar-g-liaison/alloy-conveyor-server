/**
 * Copyright 2014 Liaison Technologies, Inc.
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.mailbox.service.executor.javascript;

import com.liaison.commons.scripting.ScriptExecutorBase;
import com.liaison.commons.scripting.javascript.JavascriptExecutor;
import com.liaison.commons.scripting.javascript.JavascriptFunction;
import com.liaison.commons.scripting.javascript.JavascriptScriptContext;
import com.liaison.commons.scripting.javascript.ScriptExecutionEnvironment;
import com.liaison.mailbox.service.core.processor.ProcessorJavascriptI;

import jdk.nashorn.api.scripting.ScriptObjectMirror;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.liaison.mailbox.service.executor.javascript.JavascriptValidator.CLEANUP_FUNCTION_NAME;
import static com.liaison.mailbox.service.executor.javascript.JavascriptValidator.PROCESS_FUNCTION_NAME;
import static com.liaison.mailbox.service.executor.javascript.JavascriptValidator.REQUIRES_FUNCTION_NAME;

/**
 * This actually executes a javascript in a thread. Siblings to this could include a JavaExecutor or any other
 * server-side scripting the JVM supports.
 *
 * Includes a specialized classloader for whitelisting classes.
 *
 * Use of a CompiledScript cache for faster execution of scripts.
 *
 * Function cleanup() is automatically called, forcing an api to which all scripts must adhere to.
 *
 * Collection of diagnostics such as clock time, system time, and user time are reported.
 *
 * A specialized context is used and injected into the script that is separate from the engine message context. Right
 * now the script context is just merged with the message context once the script has finished execution.
 *
 * @author joshuaw
 * @author vnagarajan on 11/14
 */
public class JavascriptExecutorService extends ScriptExecutorBase implements ScriptExecutionEnvironment {

	protected JavascriptScriptContext scriptContext = null;
	protected String script = null;
	protected ProcessorJavascriptI processor = null;
	private final Object lock = new Object();

	private static final Logger logger = LogManager.getLogger(JavascriptExecutorService.class);

	public JavascriptExecutorService(String script, ProcessorJavascriptI processorService) {

		if (logger.isDebugEnabled()) {
			logger.debug("mailbox - JavascriptExecutor.JavascriptExecutor(): called(): script: " + script);
		}
		this.script = script;
		this.processor = processorService;
	}

	public JavascriptExecutorService(URI script, ProcessorJavascriptI processorService) {

		if (logger.isDebugEnabled()) {
			logger.debug("mailbox - JavascriptExecutor.JavascriptExecutor(): called(): script: " + script);
		}
		this.script = script.toString();
		this.processor = processorService;
	}


	public JavascriptScriptContext call() {

		if (script != null) {

			if (logger.isDebugEnabled()) {
				logger.debug("mailbox - JavascriptExecutor.call(): Starting script: " + script);
			}

			scriptContext = runScript();
			if (logger.isDebugEnabled()) {
				logger.debug("mailbox - JavascriptExecutor.call(): Finishing script: " + script);
			}
		} else {
			logger.warn("Script is null or not available");
		}
		return scriptContext;
	}

	public JavascriptScriptContext runScript() {

		long start = System.currentTimeMillis();
		if (logger.isDebugEnabled()) {
			logger.debug("mailbox: JavascriptExecutor.runScript() called: Script FQN: " + script);
		}

		ContextCreator cr = new ContextCreator(script);
		JavascriptScriptContext scriptContext = cr.getJavascriptContext();
		JavascriptExecutor je = new JavascriptExecutor();

		URI scriptUri = null;

		try {
			scriptUri = new URI(script);
		} catch (URISyntaxException e) {
			throw new RuntimeException(String.format("Script '%s' has an invalid URI", script), e);
		}

    	try {
    		// Eval the script so we can check to be sure it is valid (has the expected functions).
    		// Passing fqn as the function name will just eval the script and not run anything.
    		//je.executeInContext(scriptContext, script, scriptUri, null);

			je.executeInContext(scriptContext, script,

					// validation
					// post action to make sure script has all required functions after script is evaluated (loaded into script context)
					// then return null to terminate.
					new JavascriptFunction(
							(obj) -> {
								JavascriptValidator validator = new JavascriptValidator(script, scriptContext);
								if(!validator.isValidScript()) {
									throw new RuntimeException(String.format("Script '%s' is invalid: %s", script, validator.getErrorMessage()));
								}

								return null;
							}, scriptUri, null),

					// requires.
					// post action to get uris from script aliases,
					// then execute all required scripts to load them into script context
					new JavascriptFunction(
							(obj) -> {

								List<String> requiredScriptUriList = callScriptFunctionReturningListOfStrings(obj, REQUIRES_FUNCTION_NAME);
								if (requiredScriptUriList == null || requiredScriptUriList.size() == 0) {
									return null;
								}

								List<JavascriptFunction> functionList = new ArrayList<>();
								for (String requiredScriptUri : requiredScriptUriList) {
									if (!requiredScriptUri.startsWith("gitlab:/")) {
										throw new RuntimeException(requiredScriptUri + " is not the fully qualified name of a library script.");
									}
									try {
										functionList.add(new JavascriptFunction(new URI(requiredScriptUri), null));
									} catch (URISyntaxException e) {
										throw new RuntimeException(String.format("Script '%s' has an invalid URI", requiredScriptUri), e);
									}
								}

								// return post execution task to init re-execution of scripts provided by this task
								return new JavascriptFunction.PostExecutionTask(JavascriptFunction.ExecutionType.EXECUTE, functionList);
							}, scriptUri, REQUIRES_FUNCTION_NAME),

					// process. no post action. return as is
					new JavascriptFunction(scriptUri, PROCESS_FUNCTION_NAME, processor),

					// cleanup. no post action. return as is
					new JavascriptFunction(scriptUri, CLEANUP_FUNCTION_NAME, processor));

			logContext(scriptContext);
			logJavascriptErrors(scriptContext);

    		String elapsedTime = String.valueOf(System.currentTimeMillis() - start);
    		if (logger.isDebugEnabled()) {
    			logger.debug("mailbox: JavascriptExecutor.runScript() end: : Script FQN: " + script + " Elapsed time: " + elapsedTime + " ms.");
    		}
    
		} catch (ScriptException e) {
            throw new RuntimeException(e.getMessage());
        }

		return scriptContext;
	}

	protected void logContext(JavascriptScriptContext context) {

		Writer outWriter = context.getWriter();
		Writer errWriter = context.getErrorWriter();

		if (outWriter instanceof StringWriter)
		{
			StringWriter outStringWriter = (StringWriter) outWriter;
			if (outStringWriter.getBuffer().length() > 0) { // GSB-1601 Only log if there is something to log
				logger.info(String.format("Output from script [%s] : [%s]", script, outStringWriter.toString()));
			}
		}

		if (errWriter instanceof StringWriter)
		{
			StringWriter errStringWriter = (StringWriter) errWriter;
			if (errStringWriter.getBuffer().length() > 0) { // GSB-1601 Only log if there is something to log
				logger.error(String.format("Error output from script [%s] : [%s]", script, errStringWriter.toString()));
			}
		}
	}

	protected void logJavascriptErrors(JavascriptScriptContext context) {

		if (context.getAttribute(JavascriptExecutor.SCRIPT_EXCEPTIONS) != null) {
			// GSB-2550 Ensure TVC is initialized during rehyrdration

			@SuppressWarnings("unchecked")
			Map<String, Throwable> exceptions = (Map<String, Throwable>) context.getAttribute(JavascriptExecutor.SCRIPT_EXCEPTIONS);
			for (Map.Entry<String, Throwable> e : exceptions.entrySet()) {
				throw new RuntimeException(e.getValue());
			}
		}
	}

	@SuppressWarnings("rawtypes")
	protected ArrayList<String> callScriptFunctionReturningListOfStrings(Object listObject, String functionName) {

		// The caller will need to decide if this is ok or not.
		if (listObject == null)
		{
			return null;
		}

		ArrayList<String> listOfStrings = new ArrayList<>();
		Object[] arr;
		if (listObject instanceof List) {
			List list = (List) listObject;
			arr = list.toArray();
		} else if (listObject instanceof ScriptObjectMirror) {
			ScriptObjectMirror mirror = (ScriptObjectMirror) listObject;
			arr = mirror.to(Object[].class);
		} else {
			throw new RuntimeException(String.format("Script '%s' %s function did not return a list.", script, functionName));
		}

		for (int i = 0; i < arr.length; i++) {

			Object itemObject = arr[i];

			if (!(itemObject instanceof String))
			{
				String message = String.format("Script '%s' %s function returned a non-String in position %d.", script, functionName, i);
				throw new RuntimeException(message);
			}

			String s = (String) itemObject;
			listOfStrings.add(s);
		}

		return listOfStrings;
	}

	
	private int scriptExecutionTimeout;
	
	public void setMaxExecutionTimeout(int executionTimeout) {
		synchronized (lock) {
	           this.scriptExecutionTimeout = executionTimeout;
	       }
	}
	
	@Override
	public int getMaxExecutionTimeout() {
		return scriptExecutionTimeout;
	}

	@Override
	public String getOrganization() {
		// TODO Auto-generated method stub
		return null;
	}

}
