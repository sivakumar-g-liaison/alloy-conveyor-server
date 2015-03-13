/**
 * Copyright 2014 Liaison Technologies, Inc.
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.mailbox.service.executor.javascript;

import javax.script.ScriptEngine;


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
	protected ScriptEngine scriptEngine = null;
	protected boolean valid = true;
	protected StringBuilder message = null;


	public JavascriptValidator (String script, ScriptEngine scriptEngine)
	{
		this.script = script;
		this.scriptEngine = scriptEngine;

		message = new StringBuilder();

		validate();
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
		if (!hasRequiredValue(REQUIRES_FUNCTION_NAME)) { valid = false; }
		if (!hasRequiredValue(PROCESS_FUNCTION_NAME)) { valid = false; }
		if (!hasRequiredValue(CLEANUP_FUNCTION_NAME)) { valid = false; }
	}

	protected boolean hasRequiredValue (String name)
	{
		if (scriptEngine.get(name) == null)
		{
			message.append(String.format("Script '%s' is missing required function '%s'.  ", script, name));
			return false;
		}

		return true;
	}
}
