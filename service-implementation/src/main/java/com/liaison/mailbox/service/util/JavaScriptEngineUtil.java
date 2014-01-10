/**
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.mailbox.service.util;

import java.net.MalformedURLException;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liaison.commons.exceptions.LiaisonException;
import com.liaison.framework.util.ServiceUtils;
import com.liaison.mailbox.service.core.processor.DirectorySweeper;

/**
 * This class load the javascript content from various protocol and execute the javascript content in Java ScriptEngine.
 *  
 * @author sivakumarg
 *
 */
public final class JavaScriptEngineUtil {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DirectorySweeper.class);
	private static final String SCTIPT_TYPE = "JavaScript";
	
    //create a script engine manager
    ScriptEngineManager manager = new ScriptEngineManager();
	
    /**
     * Load the script from provided script path and create the new ScriptEngine object and executes the script in script engine. 
     * Call the Javascript method using <b>invokeFunction</b> and return the script response.
     * 
     * @param scriptPath String
     * @param methodName String
     * @param parameters Object[]
     * @return Object
     * @throws NoSuchMethodException
     * @throws MalformedURLException
     * @throws ScriptException
     * @throws LiaisonException
     */
	protected Object executeJavaScriptMethod(String scriptPath, String methodName, Object... parameters) 
			throws NoSuchMethodException, MalformedURLException, ScriptException, LiaisonException {
		
		return getScriptEngineInvocable(scriptPath).invokeFunction(methodName, parameters);
	}
	
	/**
	 * Invoke the javascript method in ScriptEngine and return the invoked javascript response object.
	 * 
	 * @param invocable Invocable
	 * @param methodName String
	 * @param parameters Object[]
	 * @return Object
	 * @throws NoSuchMethodException
	 * @throws MalformedURLException
	 * @throws ScriptException
	 * @throws LiaisonException
	 */
	protected Object executeJavaScriptMethod(Invocable invocable, String methodName, Object... parameters) 
			throws NoSuchMethodException, MalformedURLException, ScriptException, LiaisonException {
		
		return invocable.invokeFunction(methodName, parameters);
	}
	
	/**
	 * Create the new ScriptEngine object and executes the specified script in script engine.
	 * 
	 * @param scriptPath String
	 * @return Invocable
	 * @throws ScriptException
	 * @throws MalformedURLException
	 * @throws LiaisonException
	 */
	protected Invocable getScriptEngineInvocable(String scriptPath) 
		throws ScriptException, MalformedURLException, LiaisonException {
		
		ScriptEngine engine = getScriptEngine();
		String script = getScriptContent(scriptPath);		
		loadJavaScriptInEngine(engine, script);
		return (Invocable) engine;
	}
	
	private String getScriptContent(String scriptPath) 
		throws MalformedURLException, LiaisonException {
		
		String response = null;
	
		if (scriptPath.startsWith("http://")) {
			response = HTTPClientUtil.getHTTPResponseInString(LOGGER, scriptPath, null);
        } else if (scriptPath.startsWith("https://")) {
        	// TODO HTTPClient not support https protocol 
        } else if (scriptPath.startsWith("ftp://")) {
			//TODO
		} else {
			response = ServiceUtils.readFileFromClassPath(scriptPath);
		}
		
		return response;
	}
	
	/**
	 * Get the <b>ScriptEngine</b> object from <b>ScriptEngineManager</b> using script engine name.
	 * 
	 * @return ScriptEngine
	 */
	private ScriptEngine getScriptEngine() {
		return manager.getEngineByName(SCTIPT_TYPE);
	}
	/**
	 * Executes the specified script in given script engine.
	 * 
	 * @param engine ScriptEngine
	 * @param script String
	 * @throws ScriptException
	 */
	private void loadJavaScriptInEngine(ScriptEngine engine, String script) throws ScriptException {
		 engine.eval(script);
	}
}

