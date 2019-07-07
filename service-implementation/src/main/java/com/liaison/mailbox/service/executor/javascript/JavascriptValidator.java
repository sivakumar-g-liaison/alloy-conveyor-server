/**
 * Copyright 2014 Liaison Technologies, Inc.
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.mailbox.service.executor.javascript;

import com.liaison.commons.scripting.javascript.JavascriptScriptContext;


/**
 * Validates that a given Javascript is valid for execution by the Service Broker.
 *
 * @author OFS
 */
public class JavascriptValidator
{
	public static final String REQUIRES_FUNCTION_NAME = "requires";
	public static final String PROCESS_FUNCTION_NAME = "process";
	public static final String CLEANUP_FUNCTION_NAME = "cleanup";

	protected String script = null;
	protected JavascriptScriptContext javascriptScriptContext = null;
	protected boolean valid = true;
	protected StringBuilder message = null;


	public JavascriptValidator (String script, JavascriptScriptContext javascriptScriptContext)
	{
		this.script = script;
		this.javascriptScriptContext = javascriptScriptContext;

		message = new StringBuilder();

		validate();
	}

	public JavascriptValidator (String script, JavascriptScriptContext javascriptScriptContext, String method) {

	    this.script = script;
	    this.javascriptScriptContext = javascriptScriptContext;

	    message = new StringBuilder();
	    validate(method);
	}

	public boolean isValidScript ()
	{
		return valid;
	}

	public String getErrorMessage ()
	{
		return message.toString();
	}

	protected void validate ()
	{
	    validate(REQUIRES_FUNCTION_NAME);
	    validate(PROCESS_FUNCTION_NAME);
	    validate(CLEANUP_FUNCTION_NAME);
	}

	protected void validate (String funtion_name) {
	    if (!hasRequiredValue(funtion_name)) { valid = false; }
	}

	protected boolean hasRequiredValue (String name)
	{
		if (javascriptScriptContext.getAttribute(name) == null)
		{
			message.append(String.format("Script '%s' is missing required function '%s'.  ", script, name));
			return false;
		}

		return true;
	}
}