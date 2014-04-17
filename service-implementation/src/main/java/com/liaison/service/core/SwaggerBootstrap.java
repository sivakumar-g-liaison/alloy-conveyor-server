/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall 
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.service.core;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.servlet.http.HttpServlet;

import com.wordnik.swagger.config.ConfigFactory;
import com.wordnik.swagger.config.SwaggerConfig;

/**
 * @author OFS
 *
 */
public class SwaggerBootstrap extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1212764286594977168L;

	static {

		SwaggerConfig config = new SwaggerConfig();
		String basePath = config.getBasePath();
		String[] basePathParts = null;

		basePathParts = basePath.split("/");

		InetAddress ip = null;

		try {
			ip = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

		if(!basePathParts[2].equals("localhost:8080")) {
			config.setBasePath("http://" + ip.getHostAddress().toString() + ":8080/g2mailboxservice/rest");
		} else {
			config.setBasePath("http://localhost:8080/g2mailboxservice/rest");
		}

		ConfigFactory.setConfig(config);
	}
}
