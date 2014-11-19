/**
 * Copyright 2014 Liaison Technologies, Inc.
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.mailbox.service.executor.javascript;


import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.commons.scripting.javascript.JavascriptScriptContext;

/**
 * @author VNagarajan on 11/14/2014
 *
 *
 */
public class ContextCreator implements Serializable {

    /**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private static final Logger logger = LogManager.getLogger(ContextCreator.class);

    public static final String SCRIPT = "SCRIPT";

    private String script;

	public ContextCreator(String script) {
		if (logger.isDebugEnabled()) {
			logger.debug("mailbox: : ContextCreator.ContextCreator() called.");
		}
		this.script = script;
		if (logger.isDebugEnabled()) {
			logger.debug("mailbox: : ContextCreator.ContextCreator() end.");
		}
	}

	public JavascriptScriptContext getJavascriptContext() {

		logger.debug("mailbox: : ContextCreator.getJavascriptContext() called.");

		Reader inputReader = new StringReader(""); // Empty input.
		Writer outputWriter = new StringWriter();
		Writer errorWriter = new StringWriter();
		JavascriptScriptContext scriptContext = new JavascriptScriptContext(inputReader, outputWriter, errorWriter);

		scriptContext.setAttribute(SCRIPT, script);
		if (logger.isDebugEnabled()) {
			logger.debug("mailbox: ContextCreator.getJavascriptContext() end: return: " + scriptContext.toString());
		}

		return scriptContext;
	}

	public String getScript() {
		return script;
	}

	public void setScript(String script) {
		this.script = script;
	}
}
