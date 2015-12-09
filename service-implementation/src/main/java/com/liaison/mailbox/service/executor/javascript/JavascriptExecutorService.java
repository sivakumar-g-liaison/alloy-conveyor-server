/**
 * Copyright 2014 Liaison Technologies, Inc.
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.mailbox.service.executor.javascript;

import java.io.StringWriter;
import java.io.Writer;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.common.log4j2.markers.GlassMessageMarkers;
import com.liaison.commons.message.glass.dom.Metric;
import com.liaison.commons.message.glass.dom.MetricAPI;
import com.liaison.commons.message.glass.dom.MetricTag;
import com.liaison.commons.scripting.ScriptExecutorBase;
import com.liaison.commons.scripting.javascript.JavascriptExecutor;
import com.liaison.commons.scripting.javascript.JavascriptScriptContext;
import com.liaison.framework.util.IdentifierUtil;
import com.liaison.mailbox.service.core.processor.ProcessorJavascriptI;

import jdk.nashorn.api.scripting.ScriptObjectMirror;

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
public class JavascriptExecutorService extends ScriptExecutorBase {

	protected JavascriptScriptContext scriptContext = null;
	protected String script = null;
	protected ProcessorJavascriptI processor = null;
	protected MetricAPI metricAPI = new MetricAPI();

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

	public void setScriptContext(JavascriptScriptContext context) {
		scriptContext = context;
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
    		je.executeInContext(scriptContext, script, scriptUri, null);
    		if (logger.isDebugEnabled()) {
    			logger.debug("mailbox: JavascriptExecutor.runScript(): executeInContext() called. No function name.");
    		}
    		ScriptEngine scriptEngine = je.getScriptEngine();
    
    		if (scriptEngine == null) {
    			throw new RuntimeException(
    					String.format(
    							"After evaluating script '%s', script engine is null. There must have been an error evaluating the script.",
    							script));
    		}
    
    		scriptEngine.setContext(scriptContext);
    
    		validateScript(scriptContext);
    		handleRequires(je, scriptContext, scriptUri);
    		callProcess(je, scriptContext, scriptUri);
    		callCleanup(je, scriptContext, scriptUri);
    		logContext(scriptContext);
    
    		String elapsedTime = String.valueOf(System.currentTimeMillis() - start);
    		if (logger.isDebugEnabled()) {
    			logger.debug("mailbox: JavascriptExecutor.runScript() end: : Script FQN: " + script + " Elapsed time: "
    					+ elapsedTime + " ms.");
    		}
    
    		logMetrics(elapsedTime, je.getMetricData());
		} catch (ScriptException e) {
            throw new RuntimeException(e.getMessage());
        }

		return scriptContext;
	}

	private void logMetrics(String elapsedTime, Map<String, String> metricData) {

		Metric m = new Metric();
		m.setMetricName("system.metrics.javascriptExecutor.processTime");
		m.setPayloadByteLength(BigInteger.ZERO);

		MetricTag metricTag = new MetricTag();
		metricTag.setName("scriptTime");
		metricTag.setValue(elapsedTime);
		m.getMetricTags().add(metricTag);

		Map<String, String> scriptExecutionTimes = metricData;
		String[] keys = scriptExecutionTimes.keySet().toArray(new String[1]);
		int count = scriptExecutionTimes.size();
		
		for (int i = 0; i < count; i++) {
			metricTag = new MetricTag();
			metricTag.setName(keys[i]);
			metricTag.setValue(scriptExecutionTimes.get(keys[i]));
			m.getMetricTags().add(metricTag);
		}

		 metricAPI.setGlassMessageId(IdentifierUtil.getUuid());
		// metricAPI.setPipelineProcessId(messageContext.getPipelineProcessID());
		// metricAPI.setGlobalId(messageContext.getGlobalProcessID());
		 metricAPI.setMetric(m);
		 logger.info(GlassMessageMarkers.METRICS_GM_MARKER, metricAPI);
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

	protected void validateScript(JavascriptScriptContext javascriptScriptContext) {

		if (logger.isDebugEnabled())
		{
			logger.debug(String.format("Validating script '%s'", script));
		}

		JavascriptValidator scriptValidator = new JavascriptValidator(script, javascriptScriptContext);

		if (!scriptValidator.isValidScript())
		{
			String message = String.format("Script '%s' is invalid: %s", script, scriptValidator.getErrorMessage());
			logger.error(message);
			throw new RuntimeException(message);
		}

		if (logger.isInfoEnabled())
		{
			logger.info(String.format("Script '%s' is valid.", script));
		}
	}

	protected void handleRequires(JavascriptExecutor je,
			JavascriptScriptContext scriptContext, URI scriptUri) throws ScriptException
	{

		if (logger.isDebugEnabled())
		{
			logger.debug(String.format("Handling Requires for script '%s'", script));
		}
		// JIRA GSB-783. Function name was incorrect.
		ArrayList<String> requiredScriptUriList = callScriptFunctionReturningListOfStrings(je, scriptContext, scriptUri,
				JavascriptValidator.REQUIRES_FUNCTION_NAME);

		if (requiredScriptUriList == null || requiredScriptUriList.size() == 0)
		{
			// This is OK, it just means there are no required scripts.
			if (logger.isDebugEnabled())
			{
				logger.debug(String.format("Script '%s' does not require any additional scripts.", script));
			}
			return;
		}

		for (String requiredScriptUri : requiredScriptUriList)
		{
			if (!requiredScriptUri.startsWith("gitlab:/")) {
				throw new RuntimeException(requiredScriptUri + " is not the fully qualified name of a library script.");
			}
			handleRequiredScript(requiredScriptUri, je, scriptContext);
		}
	}

	protected void handleRequiredScript(String scriptUri,
			JavascriptExecutor je, JavascriptScriptContext scriptContext) throws ScriptException
	{
		if (logger.isDebugEnabled())
		{
			logger.debug(String.format("Script '%s' requires script with alias '%s", script, scriptUri));
		}

		URI requiredScriptURI = null;
		try {
			requiredScriptURI = new URI(scriptUri);
		} catch (URISyntaxException e) {
			throw new RuntimeException(String.format("Script  '%s' has invalid URI.", script), e);
		}

		// Pass a null function name because we just want to eval the script to "include" it.
		je.executeInContext(scriptContext, scriptUri, requiredScriptURI, null);
		if (logger.isInfoEnabled()) {
			logger.info(String.format("Script '%s' successfully loaded required script with alias '%s", script,
					scriptUri));
		}
	}

	protected void callProcess(JavascriptExecutor je,
			JavascriptScriptContext scriptContext, URI scriptUri) throws ScriptException
	{
		if (logger.isDebugEnabled())
		{
			logger.debug(String.format("Script '%s', loading user configuration values.", script));
		}

		if (logger.isDebugEnabled())
		{
			logger.debug(String.format("Script '%s', invoking function %s", script,
					JavascriptValidator.PROCESS_FUNCTION_NAME));
		}

		je.executeInContext(scriptContext, script, scriptUri, JavascriptValidator.PROCESS_FUNCTION_NAME, processor);
		if (logger.isDebugEnabled()) {
			logger.debug(String.format("Script '%s', function %s complete.", script,
					JavascriptValidator.PROCESS_FUNCTION_NAME));
		}
	}

	protected void callCleanup(JavascriptExecutor je,
			JavascriptScriptContext scriptContext, URI scriptUri) throws ScriptException
	{
		if (logger.isDebugEnabled())
		{
			logger.debug(String.format("Script '%s', invoking function %s", script,
					JavascriptValidator.CLEANUP_FUNCTION_NAME));
		}

		je.executeInContext(scriptContext, script, scriptUri, JavascriptValidator.CLEANUP_FUNCTION_NAME, processor);

		if (logger.isDebugEnabled())
		{
			logger.debug(String.format("Script '%s', function %s complete.", script,
					JavascriptValidator.CLEANUP_FUNCTION_NAME));
		}
	}

	@SuppressWarnings("rawtypes")
	protected ArrayList<String> callScriptFunctionReturningListOfStrings(JavascriptExecutor je,
			JavascriptScriptContext scriptContext, URI scriptUri, String functionName) throws ScriptException
	{

		Object listObject = je.executeInContext(scriptContext, script, scriptUri, functionName);

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

}
