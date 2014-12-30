/**
 * Copyright 2014 Liaison Technologies, Inc.
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.rest;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.ws.rs.Path;

/**
 * A delegate class for wrapping Resource activities
 * 
 * @author israel.evans
 * @param <T>
 * 
 */
public abstract class AbstractResourceDelegate<T> implements Callable<T> {
	protected String id = null;
	protected String actionLabel = "SERVICE REQUEST";
	protected Map<String, String> queryParams = new HashMap<String, String>();

	public String getID() {
		return id;
	}

	public String getActionLabel() {
		return actionLabel;
	}

	public Map<String, String> getQueryParams() {
		return queryParams;
	}

	public String getFishTagPath() {
		Method enclosingMethod = this.getClass().getEnclosingMethod();
		if (null == enclosingMethod) {
			return "UNABLE TO DETERMINE PATH, PLEASE USE AbstractResourceDelegate as METHOD LOCAL ANONYMOUS INNER CLASS";
		}
		Path pathAnnotation = enclosingMethod.getDeclaringClass().getAnnotation(Path.class);
		if (null == pathAnnotation) {
			return "UNABLE TO DETERMINE PATH, PLEASE USE AbstractResourceDelegate as METHOD LOCAL ANONYMOUS INNER CLASS FROM METHOD WITH PATH ANNOTATION";
		}
		return pathAnnotation.value()+"/"+enclosingMethod.getName();
	}

	public String getFishTagService() {
		Method enclosingMethod = this.getClass().getEnclosingMethod();
		if (null == enclosingMethod) {
			return "UNABLE TO DETERMINE PATH, PLEASE USE AbstractResourceDelegate as METHOD LOCAL ANONYMOUS INNER CLASS";
		}
		return enclosingMethod.getName();
	}

}